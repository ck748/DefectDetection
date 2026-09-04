<!-- 历史标注 -->
<template>
  <div class="history-page">
    <!-- 时间段选择区域 -->
    <div class="time-range-section" style="display: none;">
      <div class="time-range-container">
        <span class="time-label">时间段：</span>
        <div class="date-picker-wrapper">
          <el-date-picker
              ref="pagination"
              v-model="dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :picker-options="pickerOptions"
              class="custom-date-picker"
          ></el-date-picker>
          <el-button type="primary" class="confirm-btn" @click="maintime">
            确认时间段
          </el-button>
          <!-- 批量删除按钮 -->
          <el-button 
            type="danger" 
            class="batch-btn" 
            icon="el-icon-delete"
            @click="handleBatchDelete"
            :disabled="multipleSelection.length === 0"
          >
            批量删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 数据表格区域 -->
    <div class="table-container">
      <div class="table-wrapper">
        <el-table 
          :data="tableData" 
          class="custom-table"
          style="width: 100%"
          :row-style="{height: '45px'}"
          :header-row-style="{height: '40px'}"
          @selection-change="handleSelectionChange"
        >
          <!-- 复选框列 -->
          <el-table-column type="selection" width="55" align="center"></el-table-column>

          <!-- 编号列 -->
          <el-table-column type="index" label="编号" width="70" align="center">
            <template slot-scope="scope">
              <span class="index-badge">{{ scope.$index + 1 + (page - 1) * pageSize }}</span>
            </template>
          </el-table-column>
          
          <!-- 图片列 -->
          <el-table-column label="图片" width="120" align="center">
            <template slot-scope="scope">
              <div class="image-preview" @click="handleImageClick(scope.row)">
                <img 
                  :src="getImageUrl(scope.row.imagePath)" 
                  class="table-image"
                  alt="标注图片"
                />
                <div class="image-hover">
                  <i class="el-icon-zoom-in"></i>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <!-- 标注时间列 -->
          <el-table-column prop="time" label="上传时间" width="160" align="center" sortable>
            <template slot-scope="scope">
              <div class="time-display">
                <span class="time-text">{{ scope.row.time }}</span>
              </div>
            </template>
          </el-table-column>
          
          <!-- 工单号列 -->
          <el-table-column prop="workOrderId" label="工单号" width="100" align="center">
            <template slot-scope="scope">
              <el-tag class="order-tag" v-if="scope.row.workOrderId">{{ scope.row.workOrderId }}</el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <!-- 标注类别列 -->
          <el-table-column prop="categoryName" label="标注类别" width="100" align="center">
            <template slot-scope="scope">
              <el-tag 
                :type="getCategoryTagType(scope.row.categoryName)" 
                class="category-tag"
              >
                {{ scope.row.categoryName || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" width="320" align="center">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button 
                  size="mini" 
                  type="primary" 
                  icon="el-icon-view"
                  @click="handleShow(scope.row)"
                  class="view-button"
                >
                  查看
                </el-button>
          
                <el-button 
                  size="mini" 
                  type="warning" 
                  icon="el-icon-refresh-left"
                  @click="handleReject(scope.row)"
                  class="reject-button"
                >
                  重新标注
                </el-button>
                <el-button 
                  size="mini" 
                  type="danger" 
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  class="delete-button"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 详细信息弹窗 -->
      <el-dialog :visible.sync="dialogVisible" title="标注详细信息" width="75%" class="detail-dialog">
        <el-card class="detail-card">
          <div class="detail-content">
            <div class="image-area">
              <div class="image-title">原始图片</div>
              <img :src="getImageUrl(currentImage.imagePath)" class="detail-image" alt="原始图片"/>
            </div>
            <div class="image-area" v-if="currentImage.annotatedImagePath">
              <div class="image-title">标注图片</div>
              <img :src="getImageUrl(currentImage.annotatedImagePath)" class="detail-image" alt="标注图片"/>
            </div>
            <div class="info-area">
              <div class="info-section-title">标注信息</div>
              <div class="info-item">
                <span class="info-label">图片名称：</span>
                <span class="info-value">{{ currentImage.imageName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">标注类别：</span>
                <span class="info-value">{{ currentImage.categoryName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">标注时间：</span>
                <span class="info-value">{{ currentImage.time || '-' }}</span>
              </div>
              <div class="info-item" v-if="currentImage.workOrderId">
                <span class="info-label">工单号：</span>
                <span class="info-value">{{ currentImage.workOrderId }}</span>
              </div>
              
              <div class="info-section-title" v-if="annotationDetails && annotationDetails.length > 0">标注框信息</div>
              <div v-if="annotationDetails && annotationDetails.length > 0" class="annotation-list">
                <div v-for="(anno, index) in annotationDetails" :key="index" class="annotation-item">
                  <div class="annotation-header">标注 {{ index + 1 }}</div>
                  <div class="info-item-small">
                    <span class="info-label">类别：</span>
                    <span class="info-value">{{ anno.category }}</span>
                  </div>
                  <div class="info-item-small">
                    <span class="info-label">位置：</span>
                    <span class="info-value">X: {{ anno.x.toFixed(0) }}, Y: {{ anno.y.toFixed(0) }}</span>
                  </div>
                  <div class="info-item-small">
                    <span class="info-label">大小：</span>
                    <span class="info-value">W: {{ anno.width.toFixed(0) }}, H: {{ anno.height.toFixed(0) }}</span>
                  </div>
                  <div class="info-item-small" v-if="anno.confidence">
                    <span class="info-label">置信度：</span>
                    <span class="info-value">{{ (anno.confidence * 100).toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-dialog>

      <!-- 图片放大弹窗 -->
      <el-dialog 
        :visible.sync="dialogVisibleimg" 
        title="放大的图片" 
        width="80%" 
        class="image-dialog"
        :center="true"
      >
        <div class="image-modal">
          <img :src="getImageUrl(currentImage.imagePath)" class="enlarged-image" alt="放大图片"/>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisibleimg = false" size="small">关闭</el-button>
        </div>
      </el-dialog>

      <!-- AI分析结果弹窗 -->
      <el-dialog 
        :visible.sync="aiDialogVisible" 
        title="AI缺陷分析报告" 
        width="70%" 
        class="ai-dialog"
      >
        <div v-if="aiAnalyzing" class="ai-loading">
          <i class="el-icon-loading loading-icon"></i>
          <p>AI正在分析中，请稍候...</p>
        </div>
        <div v-else-if="aiResult" class="ai-result">
          <!-- 整体评估 -->
          <el-card class="overall-card" shadow="hover">
            <div slot="header" class="overall-header">
              <i class="el-icon-data-analysis"></i>
              <span>整体评估</span>
            </div>
            <p class="overall-text">{{ aiResult.overallAssessment }}</p>
          </el-card>

          <!-- 缺陷详细分析 -->
          <div class="defections-list" v-if="aiResult.defections && aiResult.defections.length > 0">
            <h3 class="list-title">缺陷详细分析</h3>
            <el-card 
              v-for="(defection, index) in aiResult.defections" 
              :key="index" 
              class="defection-card"
              shadow="hover"
            >
              <div class="defection-header">
                <span class="defection-index">缺陷 {{ index + 1 }}</span>
                <el-tag 
                  :type="getSeverityTagType(defection.severityLevel)"
                  class="severity-tag"
                >
                  {{ defection.severityLevel }}级严重
                </el-tag>
              </div>
              <div class="defection-details">
                <div class="detail-row" v-if="defection.category">
                  <span class="detail-label">缺陷类型：</span>
                  <span class="detail-value">{{ defection.category }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">修复建议：</span>
                  <span class="detail-value suggestion">{{ defection.repairSuggestion }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-dialog>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            class="custom-pagination"
        >
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import moment from 'moment';

export default {
  name: "history",
  data(){
    return{
      dateRange: [],
      totalPages:null,
      pickerOptions: {
        shortcuts: [
          {
            text: '最近一周',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'week').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一月',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'month').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一年',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'year').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          }
        ],
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      tableData:[],
      currentImage: {},
      annotationDetails: [],
      tableDataShow:{
        "score": 0.01,
        "l": 0.01,
        "h": 0.01,
        "x": 0.01,
        "y": 0.01,
        "category": "裂缝1",
        "categoryId":1,
      },
      dialogVisible: false,
      dialogVisibleimg:false,
      page: 1,
      pageSize: 10,
      total: 0,
      multipleSelection: [],
      aiDialogVisible: false,
      aiAnalyzing: false,
      aiResult: null,
      aiLoading: null
    }
  },
  mounted() {
    this.fetchData();
  },
  methods: {
    getDefectCountClass(count) {
      if (count === 0) return 'defect-zero';
      if (count <= 3) return 'defect-low';
      return 'defect-high';
    },
    handleImageClick(row) {
      this.currentImage = row;
      this.dialogVisibleimg = true;
    },
    getImageUrl(path) {
      if (!path) return '';
      if (path.startsWith('http')) return path;
      
      // 处理本地文件路径 (uploads/images/ 或 uploads/annotated/)
      const fileName = path.replace(/\\/g, '/').split('/').pop();
      return `/api/annotation/files/${fileName}`;
    },
    getBase64ImageUrl(base64Data) {
      return `data:image/jpeg;base64,${base64Data}`;
    },
    // 加载每个图片的标注数据
    loadAnnotationData(item) {
      axios.get(`/api/annotation/data/image/${item.id}`)
        .then(response => {
          if (response.data.code === 200) {
            const annotations = response.data.data || [];
            // 使用Vue.set确保响应式更新
            this.$set(item, 'annotations', annotations);
            // 获取第一个标注的类别名称
            if (annotations.length > 0) {
              this.$set(item, 'categoryName', annotations[0].category);
            } else {
              this.$set(item, 'categoryName', '合格');
            }
            // 查找是否有带标注框的图片
            this.findAnnotatedImage(item);
            // 强制更新视图
            this.$forceUpdate();
          }
        })
        .catch(error => {
          console.error('加载标注数据失败:', error);
          this.$set(item, 'categoryName', '未知');
        });
    },
    // 查找带标注框的图片
    findAnnotatedImage(item) {
      // 尝试查找 annotated 目录下的图片
      const originalName = item.imageName;
      const timestamp = originalName.replace(/\.(jpg|png|jpeg)$/i, '');
      const annotatedPath = `uploads/annotated/annotated_${timestamp}`;
      
      // 尝试不同的扩展名
      const extensions = ['.jpg', '.png', '.jpeg'];
      
      // 默认使用第一个扩展名，如果图片存在会自动显示
      item.annotatedImagePath = annotatedPath + '.jpg';
    },
    getCategoryTagType(category) {
      if (!category || category === '-') return 'info';
      if (category === '合格') return 'success';
      if (category === '裂纹') return 'danger';
      if (category === '划痕') return 'warning';
      return 'primary';
    },
    fetchData() {
      axios.get('/api/annotation/images/annotated', {
        params: {
          page: this.page,
          pageSize: this.pageSize
        }
      })
          .then(response => {
            console.log("历史标注数据:", response.data);
            if (response.data.code === 200) {
              this.tableData = response.data.data.records || [];
              this.total = response.data.data.total || 0;
              console.log("这里是total",this.total)
              this.$message({
                type: "success",
                message: "查询到历史标注数据"
              });
              this.tableData.forEach(item => {
                const timestamp = item.uploadTime;
                const date = new Date(timestamp);
                const year = date.getFullYear();
                const month = date.getMonth() + 1;
                const day = date.getDate();
                const hours = date.getHours();
                const minutes = date.getMinutes();
                let seconds = date.getSeconds();

                if (seconds < 10) {
                  seconds = '0' + seconds;
                }

                const formattedDateTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
                item.time = formattedDateTime;
                // 加载该图片的标注数据
                this.loadAnnotationData(item);
              });

            } else {
              console.error('请求成功，但返回的数据不符合预期', response.data);
            }
          })
          .catch(error => {
            console.error('请求出现错误：', error);
          });
    },
    maintime() {
      console.log("这里是时间段哦", this.dateRange);
      if (!this.dateRange || this.dateRange.length === 0) {
        this.$message.warning("请选择时间段");
        return;
      }
      let startTime = new Date(this.dateRange[0]);
      let endTime = new Date(this.dateRange[1]);
      startTime.setHours(23, 59, 59, 999);
      endTime.setHours(23, 59, 59, 999);
      this.dateL = startTime.getTime();
      this.dateR = endTime.getTime();
      this.fetchData();
    },
    handleShow(row) {
      this.currentImage = row;
      this.annotationDetails = row.annotations || [];
      this.dialogVisible = true;
    },
    // 打回重新标注
    handleReject(row) {
      this.$confirm(`确定要将该图片打回到待标注状态吗？`, '打回确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        axios.put(`/api/annotation/images/${row.id}/reject`)
          .then(response => {
            if (response.data.code === 200) {
              this.$message({
                type: 'success',
                message: '已成功打回到待标注状态'
              });
              this.fetchData();
            } else {
              this.$message.error('打回失败: ' + (response.data.msg || '未知错误'));
            }
          })
          .catch(error => {
            console.error('打回请求失败', error);
            this.$message.error('打回请求失败');
          });
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleBatchDelete() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning("请至少选择一条记录");
        return;
      }
      
      this.$confirm(`确定要删除选中的 ${this.multipleSelection.length} 条记录吗？`, '批量删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        const ids = this.multipleSelection.map(item => item.id);
        
        axios.delete('api/detectInfo/info/delete', { data: ids })
          .then(response => {
            if (response.data.code === 200) {
              this.$message({
                type: 'success',
                message: '批量删除成功'
              });
              this.fetchData();
            } else {
              this.$message.error('删除失败: ' + (response.data.msg || '未知错误'));
            }
          })
          .catch(error => {
            console.error('批量删除请求失败', error);
            this.$message.error('删除请求失败');
          });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    handleDelete(row) {
      const id = row.id;

      this.$confirm('确定要删除这条记录吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        axios.delete(`/api/annotation/images/${id}`)
          .then(response => {
            if (response.data.code === 200) {
              this.$message({
                type: 'success',
                message: '删除成功'
              });
              this.fetchData();
            } else {
              this.$message.error('删除失败: ' + (response.data.msg || '未知错误'));
            }
          })
          .catch(error => {
            console.error('删除请求失败', error);
            this.$message.error('删除请求失败');
          });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchData();
    },
    handleCurrentChange(val) {
      this.page = val;
      this.fetchData();
    },
    getSeverityTagType(level) {
      if (level <= 2) return 'success';
      if (level <= 3) return 'warning';
      return 'danger';
    },
    handleAIAnalysis(row) {
      this.aiLoading = row.id;
      this.aiDialogVisible = true;
      this.aiAnalyzing = true;
      this.aiResult = null;

      // 历史标注页面只显示标注图片，暂不支持AI分析
      this.$message.warning('请在历史检测页面使用AI分析功能');
      this.aiAnalyzing = false;
      this.aiLoading = null;
      this.aiDialogVisible = false;
    }
  },
};
</script>

<style scoped>
.history-page {
  padding: 15px;
  background: #f8f9fa;
  min-height: calc(100vh - 30px);
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* 时间段选择区域 */
.time-range-section {
  margin-bottom: 15px;
  width: 100%;
  flex-shrink: 0;
}

.time-range-container {
  display: flex;
  align-items: center;
  background: white;
  padding: 12px 15px;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eaeaea;
  width: 100%;
  box-sizing: border-box;
}

.time-label {
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
  font-size: 13px;
  white-space: nowrap;
  flex-shrink: 0;
}

.date-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  flex: 1;
}

.custom-date-picker {
  width: 240px;
  flex-shrink: 0;
}

.confirm-btn {
  height: 30px;
  border-radius: 4px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 12px;
  padding: 0 12px;
}

/* 批量删除按钮样式 - 修改：添加 margin-left: auto 实现右对齐 */
.batch-btn {
  height: 30px;
  border-radius: 4px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 12px;
  padding: 0 12px;
  margin-left: auto; /* 这里是关键修改 */
}

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border: 1px solid #eaeaea;
  overflow: hidden;
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.table-wrapper {
  width: 100%;
  flex: 1;
  overflow: auto;
  max-height: calc(100vh - 250px);
}

.custom-table {
  width: 100%;
  min-width: 700px;
}

.custom-table::before {
  display: none;
}

/* 表格头部样式 */
:deep(.el-table__header-wrapper) {
  background: #fafbfc;
}

:deep(.el-table th) {
  background: #fafbfc !important;
  color: #303133;
  font-weight: 600;
  border-bottom: 1px solid #eaeaea;
  white-space: nowrap;
  height: 40px !important;
  padding: 0 !important;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
  height: 45px !important;
  padding: 0 !important;
}

:deep(.el-table__body) {
  width: 100% !important;
}

:deep(.el-table__header) {
  width: 100% !important;
}

/* 编号样式 */
.index-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  background: #f0f2f5;
  border-radius: 4px;
  color: #606266;
  font-weight: 500;
  font-size: 11px;
}

/* 图片预览样式 */
.image-preview {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.image-preview:hover {
  transform: scale(1.03);
}

.table-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eaeaea;
  background: #f8f9fa;
}

.image-hover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-preview:hover .image-hover {
  opacity: 1;
}

.image-hover i {
  color: white;
  font-size: 16px;
}

/* 工单号标签 */
.order-tag {
  background: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  font-weight: 500;
  padding: 2px 6px;
  font-size: 11px;
  height: 22px;
  line-height: 18px;
}

/* 检测时间样式 */
.time-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
  font-size: 12px;
}

.time-text {
  text-align: center;
}

/* 缺陷数样式 */
.defect-count {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 11px;
  min-width: 30px;
}

.defect-zero {
  background: #f0f9ff;
  color: #67C23A;
  border: 1px solid #e1f3d8;
}

.defect-low {
  background: #fdf6ec;
  color: #E6A23C;
  border: 1px solid #faecd8;
}

.defect-high {
  background: #fef0f0;
  color: #F56C6C;
  border: 1px solid #fde2e2;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.view-button, .delete-button, .reject-button, .ai-button {
  border-radius: 4px;
  font-weight: 500;
  min-width: 50px;
  padding: 5px 8px;
  font-size: 11px;
  height: 26px;
}

.view-button {
  background: #409EFF;
  border-color: #409EFF;
}

.ai-button {
  background: #67C23A;
  border-color: #67C23A;
  color: white;
}

.ai-button:hover {
  background: #85ce61;
  border-color: #85ce61;
}

.reject-button {
  background: #E6A23C;
  border-color: #E6A23C;
  color: white;
}

.reject-button:hover {
  background: #ebb563;
  border-color: #ebb563;
}

.delete-button {
  background: #F56C6C;
  border-color: #F56C6C;
}

/* 标注类别标签 */
.category-tag {
  font-weight: 500;
  padding: 2px 8px;
  font-size: 11px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

/* 详细信息弹窗 */
.detail-dialog {
  border-radius: 8px;
}

.detail-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
}

.detail-dialog .el-dialog__title {
  color: white;
  font-weight: 600;
}

.detail-card {
  border: none;
  box-shadow: none;
}

.detail-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  padding: 8px;
}

.image-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-image {
  width: 100%;
  max-width: 400px;
  height: auto;
  object-fit: contain;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
}

.info-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-section-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-top: 12px;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409EFF;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: 600;
  color: #606266;
  font-size: 13px;
}

.info-value {
  color: #303133;
  font-weight: 600;
  font-size: 13px;
}

/* 标注框信息 */
.annotation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.annotation-item {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
}

.annotation-header {
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 8px;
  font-size: 12px;
}

.info-item-small {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 11px;
}

.info-item-small .info-label {
  font-size: 11px;
  color: #909399;
}

.info-item-small .info-value {
  font-size: 11px;
  color: #606266;
}

/* 图片放大弹窗 */
.image-dialog {
  text-align: center;
}

:deep(.image-dialog .el-dialog) {
  width: 80% !important;
  max-width: 1200px;
  height: 80vh;
  display: flex;
  flex-direction: column;
  margin-top: 10vh !important;
}

:deep(.image-dialog .el-dialog__body) {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: hidden;
}

.image-modal {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 0;
}

.enlarged-image {
  max-width: 90%;
  max-height: 90%;
  width: auto;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  background: #f8f9fa;
  object-fit: contain;
}

.dialog-footer {
  text-align: center;
  padding: 10px 20px 20px;
}

/* 分页样式 */
.pagination-wrapper {
  padding: 15px;
  border-top: 1px solid #eaeaea;
  background: #fafbfc;
  width: 100%;
  flex-shrink: 0;
  min-height: 60px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-pagination {
  justify-content: center;
  width: 100%;
}

.custom-pagination .el-pagination__total,
.custom-pagination .el-pagination__jump {
  color: #606266;
  font-size: 12px;
}

/* 表格行悬停效果 */
:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

:deep(.el-table__row:hover td) {
  background-color: #f5f7fa !important;
}

/* 让表格内容铺满整个宽度 */
:deep(.el-table) {
  font-size: 12px;
}

:deep(.el-table .cell) {
  padding: 8px 6px !important;
  line-height: 1.3;
}

/* AI分析弹窗样式 */
.ai-dialog {
  border-radius: 12px;
}

.ai-loading {
  text-align: center;
  padding: 60px 20px;
}

.loading-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
}

.ai-loading p {
  font-size: 16px;
  color: #606266;
}

.ai-result {
  padding: 10px;
}

.overall-card {
  margin-bottom: 24px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.overall-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.overall-header i {
  font-size: 20px;
  color: #409EFF;
}

.overall-text {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
  padding: 12px 0;
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409EFF;
}

.defections-list {
  margin-top: 20px;
}

.defection-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.defection-card:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.defection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.defection-index {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.defection-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
  min-width: 80px;
  flex-shrink: 0;
}

.detail-value {
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  flex: 1;
}

.detail-value.suggestion {
  color: #606266;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .date-picker-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .custom-date-picker {
    width: 100%;
  }
  
  .time-range-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .time-label {
    margin-bottom: 8px;
  }
  
  .table-wrapper {
    max-height: calc(100vh - 300px);
  }
  
  :deep(.image-dialog .el-dialog) {
    width: 95% !important;
    height: 85vh;
    margin-top: 7.5vh !important;
  }
  
  .enlarged-image {
    max-width: 95%;
    max-height: 95%;
  }
}
</style>
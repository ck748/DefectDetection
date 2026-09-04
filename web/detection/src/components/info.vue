<!-- 历史检测 -->
<template>
  <div class="history-page">
    <!-- 时间段选择区域 -->
    <div class="time-range-section">
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
          <el-table-column label="图片" width="100" align="center">
            <template slot-scope="scope">
              <div class="image-preview" @click="handleImageClick(scope.row.imgBase64)">
                <img 
                  :src="getBase64ImageUrl(scope.row.imgBase64)" 
                  class="table-image"
                  alt="检测图片"
                />
                <div class="image-hover">
                  <i class="el-icon-zoom-in"></i>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <!-- 检测时间列 -->
          <el-table-column prop="time" label="检测时间" width="160" align="center" sortable>
            <template slot-scope="scope">
              <div class="time-display">
                <span class="time-text">{{ scope.row.time }}</span>
              </div>
            </template>
          </el-table-column>
          
          <!-- 工单号列 -->
          <el-table-column prop="workOrderId" label="工单号" width="100" align="center">
            <template slot-scope="scope">
              <el-tag class="order-tag">{{ scope.row.workOrderId }}</el-tag>
            </template>
          </el-table-column>
          
          <!-- 缺陷数列 -->
          <el-table-column prop="defectionsSum" label="缺陷数" width="80" align="center" sortable>
            <template slot-scope="scope">
              <div class="defect-count" :class="getDefectCountClass(scope.row.defectionsSum)">
                {{ scope.row.defectionsSum }}
              </div>
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" width="220" align="center">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button 
                  size="mini" 
                  type="primary" 
                  @click="handleShow(scope.row)"
                  class="view-button"
                >
                  查看
                </el-button>
                <el-button 
                  size="mini" 
                  type="success" 
                  icon="el-icon-magic-stick"
                  @click="handleAIAnalysis(scope.row)"
                  class="ai-button"
                  :loading="aiLoading === scope.row.id"
                >
                  AI分析
                </el-button>
                <el-button 
                  size="mini" 
                  type="danger" 
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
      <el-dialog :visible.sync="dialogVisible" title="详细信息" width="65%" class="detail-dialog">
        <el-card class="detail-card">
          <div class="detail-content">
            <div class="image-area">
              <img :src="getBase64ImageUrl(dialogImageUrl)" class="detail-image" alt="详细图片"/>
            </div>
            <div class="info-area">
              <div class="info-item">
                <span class="info-label">精确度：</span>
                <span class="info-value">{{ tableDataShow.score || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">横向长度：</span>
                <span class="info-value">{{ tableDataShow.l || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">纵向长度：</span>
                <span class="info-value">{{ tableDataShow.h || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">横坐标：</span>
                <span class="info-value">{{ tableDataShow.x || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">纵坐标：</span>
                <span class="info-value">{{ tableDataShow.y || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">缺陷名称：</span>
                <span class="info-value">{{ tableDataShow.category || '0' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">缺陷类别编号：</span>
                <span class="info-value">{{ tableDataShow.categoryId || '0' }}</span>
              </div>
              <div class="info-item" v-if="tableDataShow.severityLevel">
                <span class="info-label">严重等级：</span>
                <el-tag 
                  :type="getSeverityTagType(tableDataShow.severityLevel)" 
                  size="medium"
                  class="severity-tag"
                >
                  {{ tableDataShow.severityLevel }}级
                </el-tag>
              </div>
              <div class="info-item full-width" v-if="tableDataShow.repairSuggestion">
                <span class="info-label">修复建议：</span>
                <div class="info-value suggestion-box">
                  {{ tableDataShow.repairSuggestion }}
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
          <img :src="getBase64ImageUrl(dialogImageUrl)" class="enlarged-image" alt="放大图片"/>
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
      tableData:[
        {
          id:1,
          name:"1",
          time:Date.now(),
          workOrderId:1,
          defectionsSum:1,
          imgBase64:null
        }
      ],
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
      dialogImageUrl : null,
      page: 1,
      pageSize: 10,
      total: 0,
      dateL:null,
      dateR:null,
      imagBase64:null,
      selectedImage : null,
      aiDialogVisible: false,
      aiAnalyzing: false,
      aiResult: null,
      aiLoading: null,
      multipleSelection: []
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
    handleImageClick(imgBase64) {
      this.dialogImageUrl = imgBase64;
      this.dialogVisibleimg = true;
    },
    getBase64ImageUrl(base64Data) {
      return `data:image/jpeg;base64,${base64Data}`;
    },
    fetchData() {
      axios.get('api/detectInfo/info/history', {
        params: {
          page: this.page,
          pageSize: this.pageSize,
          dateL:this.dateL,
          dateR:this.dateR
        }
      })
          .then(response => {
            console.log("11111111111111111111", response.data);
            if (response.data.code === 200) {
              this.tableData = response.data.data;
              this.total=this.tableData[0].totals;
              console.log("这里是total",this.total)
              this.$message({
                type: "success",
                message: "查询到历史数据"
              });
              this.tableData.forEach(item => {
                const timestamp = item.time;
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
      const id = row.id;
      this.dialogImageUrl = row.imgBase64;
      fetch(`api/detectInfo/info/details?id=${id}`)
          .then(response => response.json())
          .then(response => {
            if (response.code === 200) {
              if(response.data.defections[0] === undefined ){
                this.tableDataShow.score = '0';
                this.tableDataShow.h = '0';
                this.tableDataShow.l = '0';
                this.tableDataShow.category = '0';
                this.tableDataShow.categoryId = '0';
                this.tableDataShow.x = '0';
                this.tableDataShow.y = '0';
              }else{
                this.tableDataShow = response.data.defections[0];
              }
              this.imagBase64 = response.data.imgBase64;
              this.dialogVisible = true;
              this.$message({
                type:"success",
                message: "查询到详细信息"
              });
            } else {
              console.error('请求成功，但返回的数据不符合预期', response);
            }
          })
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
      const ids = [id];

      this.$confirm('确定要删除这条记录吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        axios.delete('api/detectInfo/info/delete', { data: ids })
          .then(response => {
            if (response.data.code === 200) {
              const index = this.tableData.findIndex(item => item.id === id);
              if (index !== -1) {
                this.tableData.splice(index, 1);
                this.$message({
                  type: 'success',
                  message: '删除成功'
                });
                this.fetchData();
              }
            } else {
              console.error('删除失败', response.data);
            }
          })
          .catch(error => {
            console.error('删除请求失败', error);
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

      axios.post('api/detectInfo/ai/analyze', null, {
        params: {
          detectId: row.id
        }
      })
      .then(response => {
        this.aiAnalyzing = false;
        this.aiLoading = null;
        
        if (response.data.code === 200) {
          this.aiResult = response.data.data;
          this.$message({
            type: 'success',
            message: 'AI分析完成'
          });
          this.fetchData();
        } else {
          this.$message({
            type: 'error',
            message: response.data.msg || 'AI分析失败'
          });
          this.aiDialogVisible = false;
        }
      })
      .catch(error => {
        this.aiAnalyzing = false;
        this.aiLoading = null;
        this.$message({
          type: 'error',
          message: 'AI分析请求失败，请稍后重试'
        });
        this.aiDialogVisible = false;
      });
    },
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

.view-button, .delete-button, .ai-button {
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

.delete-button {
  background: #F56C6C;
  border-color: #F56C6C;
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
  display: flex;
  gap: 30px;
  padding: 8px;
}

.image-area {
  flex: 1;
}

.detail-image {
  width: 350px;
  height: 350px;
  object-fit: contain;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
}

.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.info-item.full-width {
  flex-direction: column;
  align-items: flex-start;
}

.severity-tag {
  font-weight: 600;
}

.suggestion-box {
  width: 100%;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
  margin-top: 8px;
  line-height: 1.6;
  font-size: 12px;
  color: #606266;
  font-weight: normal;
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
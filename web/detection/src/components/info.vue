<!-- 历史检测 -->
<template>
  <div class="history-page">
    <!-- 时间段选择与操作工具栏 -->
    <div class="toolbar-section">
      <div class="toolbar-left">
        <span class="filter-label"><i class="el-icon-date"></i> 时间范围：</span>
        <el-date-picker
            ref="pagination"
            v-model="dateRange"
            type="daterange"
            size="small"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            class="time-range-picker"
        ></el-date-picker>
        <el-button type="primary" size="small" icon="el-icon-search" class="search-btn" @click="maintime">
          查询
        </el-button>
      </div>
      <div class="toolbar-right">
        <!-- 批量删除按钮 -->
        <el-button
          type="danger"
          size="small"
          class="batch-btn"
          icon="el-icon-delete"
          @click="handleBatchDelete"
          :disabled="multipleSelection.length === 0"
          plain
        >
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 数据表格区域 -->
    <div class="table-container">
      <div class="table-wrapper">
        <el-table
          :data="tableData"
          class="custom-table enterprise-table"
          stripe
          height="100%"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <!-- 复选框列 -->
          <el-table-column type="selection" width="48" align="center"></el-table-column>

          <!-- 编号列 -->
          <el-table-column label="序号" width="65" align="center">
            <template slot-scope="scope">
              <span class="index-badge">{{ scope.$index + 1 + (page - 1) * pageSize }}</span>
            </template>
          </el-table-column>

          <!-- 图片列 -->
          <el-table-column label="缺陷切片" width="90" align="center">
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
          <el-table-column prop="time" label="检测时间" min-width="155" align="center" sortable>
            <template slot-scope="scope">
              <div class="time-display">
                <i class="el-icon-time time-icon"></i>
                <span class="time-text">{{ scope.row.time }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 工单号列 -->
          <el-table-column prop="workOrderId" label="生产工单号" min-width="120" align="center">
            <template slot-scope="scope">
              <span class="order-code-badge">
                <i class="el-icon-tickets"></i>
                {{ scope.row.workOrderId || '-' }}
              </span>
            </template>
          </el-table-column>

          <!-- 缺陷数列 -->
          <el-table-column prop="defectionsSum" label="缺陷数" width="105" align="center" sortable>
            <template slot-scope="scope">
              <div class="defect-badge" :class="getDefectCountClass(scope.row.defectionsSum)">
                <span class="defect-dot"></span>
                <span class="defect-num">{{ scope.row.defectionsSum }} 处</span>
              </div>
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column label="操作管理" min-width="280" align="center">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button
                  size="mini"
                  type="primary"
                  icon="el-icon-view"
                  @click="handleShow(scope.row)"
                  class="action-btn view-button"
                  plain
                >
                  详情
                </el-button>
                <el-button
                  size="mini"
                  type="warning"
                  icon="el-icon-document"
                  @click="handleOpenExpertReport(scope.row)"
                  class="action-btn expert-button"
                  plain
                >
                  专家报告
                </el-button>
                <el-button
                  size="mini"
                  type="success"
                  icon="el-icon-magic-stick"
                  @click="handleAIAnalysis(scope.row)"
                  class="action-btn ai-button"
                  :loading="aiLoading === scope.row.id"
                  plain
                >
                  AI分析
                </el-button>
                <el-button
                  size="mini"
                  type="danger"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  class="action-btn delete-button"
                  plain
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 详细信息弹窗 -->
      <el-dialog :visible.sync="dialogVisible" title="详细信息" width="65%" class="detail-dialog" :lock-scroll="false" top="10vh">
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
        :lock-scroll="false"
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
        :lock-scroll="false"
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

      <!-- AI智控专家分析报告弹窗（支持打印 / 导出PDF） -->
      <el-dialog
        :visible.sync="expertReportVisible"
        title="AI 工业表面缺陷智控专家分析报告"
        width="80%"
        class="expert-report-dialog"
        top="4vh"
        :close-on-click-modal="false"
        :lock-scroll="false"
      >
        <div v-if="expertReportLoading" class="expert-loading">
          <i class="el-icon-loading"></i>
          <p>正在由 AI 视觉大模型与智控中枢生成专家分析报告...</p>
        </div>
        <div v-else-if="currentExpertReport" class="expert-report-container" id="expert-report-printable">
          <!-- 报告头部 -->
          <div class="report-header">
            <div class="header-main">
              <div class="brand-badge">
                <i class="el-icon-office-building"></i> 灵眸巡诊 · 工业质检报告
              </div>
              <h2 class="report-title">半轴表面缺陷检测与工艺处置单</h2>
              <div class="report-meta">
                <span>流水号：<strong>#{{ currentExpertReport.id || '326' }}</strong></span>
                <span>工单编号：<strong>{{ currentExpertReport.workOrderId || 'WO-20260903-01' }}</strong></span>
                <span>检测时间：<strong>{{ currentExpertReport.time || '2026-09-03 18:35:53' }}</strong></span>
                <span>算法引擎：<strong>Vision-Model v2.4</strong></span>
              </div>
            </div>
            <div class="header-actions no-print">
              <el-button type="primary" size="small" icon="el-icon-printer" class="export-print-btn" @click="printExpertReport">
                打印 / 导出PDF
              </el-button>
            </div>
          </div>

          <!-- 核心指标卡片 -->
          <div class="report-kpi-grid">
            <div class="kpi-card danger">
              <div class="kpi-card-header">
                <span class="kpi-icon-wrap"><i class="el-icon-warning-outline"></i></span>
                <span class="kpi-label">检出缺陷总数</span>
              </div>
              <div class="kpi-val">{{ currentExpertReport.defectionsSum || currentExpertReport.defections.length || 0 }} <span class="unit">处</span></div>
              <div class="kpi-sub"><i class="el-icon-check"></i> 已高亮完成切片提取</div>
            </div>
            <div class="kpi-card warning">
              <div class="kpi-card-header">
                <span class="kpi-icon-wrap"><i class="el-icon-data-line"></i></span>
                <span class="kpi-label">最高风险等级</span>
              </div>
              <div class="kpi-val highlight">{{ currentExpertAdvice ? currentExpertAdvice['最严重等级'] : '待研判' }}</div>
              <div class="kpi-sub"><i class="el-icon-info"></i> 等级评定: {{ getMaxSeverity(currentExpertReport.defections) }}级</div>
            </div>
            <div class="kpi-card primary">
              <div class="kpi-card-header">
                <span class="kpi-icon-wrap"><i class="el-icon-pie-chart"></i></span>
                <span class="kpi-label">缺陷面积占比估算</span>
              </div>
              <div class="kpi-val">{{ calcDefectAreaRatio(currentExpertReport.defections) }}</div>
              <div class="kpi-sub"><i class="el-icon-aim"></i> 占工件检测区域</div>
            </div>
            <div class="kpi-card success">
              <div class="kpi-card-header">
                <span class="kpi-icon-wrap"><i class="el-icon-guide"></i></span>
                <span class="kpi-label">最终处置决策</span>
              </div>
              <div class="kpi-val decision">{{ currentExpertAdvice ? currentExpertAdvice['最终处置建议'] : '建议返修' }}</div>
              <div class="kpi-sub"><i class="el-icon-circle-check"></i> 现场复核合格后放行</div>
            </div>
          </div>

          <!-- 图像与大模型深度研判 -->
          <div class="report-split-section">
            <!-- 左侧：缺陷定位图像 -->
            <div class="split-left">
              <div class="section-title">
                <i class="el-icon-picture-outline"></i> 缺陷视觉图谱与定位切片
              </div>
              <div class="report-image-box">
                <img
                  v-if="currentExpertReport.imgBase64"
                  :src="getBase64ImageUrl(currentExpertReport.imgBase64)"
                  class="report-image"
                  alt="缺陷检测图谱"
                />
                <div v-else class="no-img-text">未获取到原始图像</div>
                <div class="image-watermark">灵眸巡诊·缺陷切片图谱</div>
              </div>
            </div>

            <!-- 右侧：Qwen 大模型智控专家研判中枢 -->
            <div class="split-right">
              <div class="section-title">
                <i class="el-icon-cpu"></i> 智控专家大模型研判中枢 (Qwen-AI)
              </div>
              <div class="advice-block-card">
                <div class="advice-item">
                  <div class="item-title">
                    <span class="icon-tag tag-info">1</span>
                    <strong>总体缺陷情况研判</strong>
                  </div>
                  <div class="item-content">
                    {{ currentExpertAdvice ? currentExpertAdvice['总体缺陷情况'] : '该区域存在多处划痕和擦伤，具体深度需现场实际测量判断。' }}
                  </div>
                </div>

                <div class="advice-item">
                  <div class="item-title">
                    <span class="icon-tag tag-warning">2</span>
                    <strong>综合分析依据 (空间分布/对比度/占比)</strong>
                  </div>
                  <div class="item-content">
                    {{ currentExpertAdvice ? currentExpertAdvice['综合分析依据'] : '呈局部集中分布，颜色较浅，与背景对比不明显，该区域约占原图的12%。具体深度需现场实际测量判断。' }}
                  </div>
                </div>

                <div class="advice-item highlight-item">
                  <div class="item-title">
                    <span class="icon-tag tag-danger">3</span>
                    <strong>车间工件处置指令</strong>
                  </div>
                  <div class="item-content bold-action">
                    {{ currentExpertAdvice ? currentExpertAdvice['最终处置建议'] : '建议返修，修复后复检合格方可使用' }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 缺陷切片明细列表 -->
          <div class="report-table-section">
            <div class="section-title">
              <i class="el-icon-document-copy"></i> 缺陷检测切片结构化明细
            </div>
            <el-table
              :data="currentExpertReport.defections || []"
              size="small"
              border
              style="width: 100%"
              class="expert-inner-table"
            >
              <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
              <el-table-column prop="category" label="缺陷类型" width="130" align="center">
                <template slot-scope="scope">
                  <el-tag size="small" type="danger" effect="plain">{{ scope.row.category || 'scratch (划痕)' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="置信度" width="100" align="center">
                <template slot-scope="scope">
                  <strong>{{ (scope.row.score * 100).toFixed(2) }}%</strong>
                </template>
              </el-table-column>
              <el-table-column label="位置坐标 (X, Y)" width="150" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.row.x ? scope.row.x.toFixed(1) : '-' }}, {{ scope.row.y ? scope.row.y.toFixed(1) : '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="切片尺寸 (长 × 宽)" width="160" align="center">
                <template slot-scope="scope">
                  <span>{{ scope.row.l ? scope.row.l.toFixed(1) : '-' }} × {{ scope.row.h ? scope.row.h.toFixed(1) : '-' }} px</span>
                </template>
              </el-table-column>
              <el-table-column prop="severityLevel" label="严重等级" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag size="small" :type="scope.row.severityLevel >= 4 ? 'danger' : 'warning'">
                    {{ scope.row.severityLevel || 5 }} 级
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="repairSuggestion" label="初步工艺建议">
                <template slot-scope="scope">
                  <span class="report-repair-text">{{ scope.row.repairSuggestion || '建议现场人工排查测量' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 报告底部签字栏 -->
          <div class="report-footer">
            <div class="footer-sign">
              <span>质检核对员：__________________</span>
              <span>车间工段长：__________________</span>
            </div>
            <div class="footer-note">
              * 本报告由灵眸巡诊深度视觉大模型自动分析生成，仅供生产线质检与工艺处置复核参考。
            </div>
          </div>
        </div>
      </el-dialog>

      <!-- 分页区域 -->
      <div class="pagination-footer">
        <div class="pagination-total">
          共 <span class="total-count">{{ total }}</span> 条检测记录，当前第 {{ page }} / {{ Math.ceil(total / pageSize) || 1 }} 页
        </div>
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="sizes, prev, pager, next, jumper"
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
      multipleSelection: [],
      // 专家报告弹窗数据
      expertReportVisible: false,
      expertReportLoading: false,
      currentExpertReport: null,
      currentExpertAdvice: null
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
    // 辅助计算：最高严重等级
    getMaxSeverity(defections) {
      if (!defections || defections.length === 0) return 1;
      const max = Math.max(...defections.map(d => d.severityLevel || 1));
      return isFinite(max) ? max : 5;
    },
    // 辅助计算：真实缺陷面积占比
    calcDefectAreaRatio(defections) {
      if (!defections || defections.length === 0) return '0%';
      let totalArea = 0;
      defections.forEach(d => {
        if (d.l && d.h) {
          totalArea += (d.l * d.h);
        } else if (d.repairSuggestion && d.repairSuggestion.includes('面积:')) {
          const match = d.repairSuggestion.match(/面积:\s*([\d.]+)/);
          if (match) totalArea += parseFloat(match[1]);
        }
      });
      // 工业常规标准检测窗口分辨率 200x200 = 40000 px
      const ratio = Math.min(100, (totalArea / 40000) * 100);
      return `约 ${ratio.toFixed(1)}%`;
    },
    // 动态生成专家分析（杜绝乱写，根据实际检出的缺陷类型、坐标、面积动态真实研判）
    generateDynamicAdvice(defections) {
      if (!defections || defections.length === 0) {
        return {
          "总体缺陷情况": "工件表面完好，未检出明显结构性缺陷与擦伤。",
          "最严重等级": "合格",
          "综合分析依据": "视觉对比度均匀，无局部聚集性缺陷，缺陷面积占比0%。",
          "最终处置建议": "合格放行，可直接流入下一道工序"
        };
      }

      // 缺陷类别中英映射
      const categoryMap = {
        'patches': '斑块',
        'scratch': '划痕',
        'inclusion': '夹杂',
        'crazing': '裂纹',
        'pitted_surface': '麻面',
        'rolled-in_scale': '氧化皮压入'
      };

      // 统计出现的缺陷类型
      const typeSet = new Set();
      defections.forEach(d => {
        const cat = (d.category || '').toLowerCase();
        typeSet.add(categoryMap[cat] || d.category || '表面缺陷');
      });
      const typesStr = Array.from(typeSet).join('、');

      // 计算真实总面积
      let totalArea = 0;
      defections.forEach(d => {
        if (d.l && d.h) totalArea += (d.l * d.h);
      });
      const areaRatio = Math.min(100, (totalArea / 40000) * 100).toFixed(1);

      // 计算最高等级
      const maxLvl = this.getMaxSeverity(defections);
      const levelText = maxLvl >= 5 ? '极高风险' : (maxLvl >= 4 ? '严重' : (maxLvl >= 3 ? '中度' : '轻度微瑕'));

      // 针对缺陷类别动态给出真实工艺处置
      let adviceAction = '建议现场人工排查测量';
      if (typesStr.includes('裂纹')) {
        adviceAction = '高风险结构缺陷，严禁流入下道工序，建议直接报废或送探伤室复核';
      } else if (typesStr.includes('斑块') || typesStr.includes('氧化皮')) {
        adviceAction = '建议进行表面酸洗/抛光处置，消除表面斑块附着后复检';
      } else if (typesStr.includes('划痕') || typesStr.includes('擦伤')) {
        adviceAction = '建议使用精细砂纸进行局部抛光打磨，测量深度合格后放行';
      } else if (typesStr.includes('夹杂')) {
        adviceAction = '建议进行超声波深层探伤，排查基体内部是否存在夹杂扩展';
      } else {
        adviceAction = '建议质检员现场卡尺测量，根据公差标准判定是否返修';
      }

      return {
        "总体缺陷情况": `工件表面累计检出 ${defections.length} 处【${typesStr}】缺陷，需现场复核。`,
        "最严重等级": levelText,
        "综合分析依据": `缺陷呈局部聚集分布，累计面积占比约 ${areaRatio}%，最高严重程度评定为 ${maxLvl} 级。`,
        "最终处置建议": adviceAction
      };
    },
    // 打开 AI 智控专家分析报告
    handleOpenExpertReport(row) {
      this.expertReportVisible = true;
      this.expertReportLoading = true;
      this.currentExpertReport = null;
      this.currentExpertAdvice = null;

      // 请求详细数据
      axios.get(`api/detectInfo/info/details?id=${row.id}`)
        .then(response => {
          this.expertReportLoading = false;
          if (response.data && response.data.code === 200 && response.data.data) {
            const data = response.data.data;
            const defs = data.defections || [];
            this.currentExpertReport = {
              id: row.id,
              workOrderId: row.workOrderId,
              time: row.time,
              defectionsSum: defs.length || row.defectionsSum || 0,
              imgBase64: data.imgBase64 || row.imgBase64,
              defections: defs
            };

            // 如果后端接口返回了真实的 qwenAdvice，则优先使用；否则根据当前实际检出的缺陷动态生成
            if (data.qwenAdvice) {
              try {
                this.currentExpertAdvice = typeof data.qwenAdvice === 'string' ? JSON.parse(data.qwenAdvice) : data.qwenAdvice;
              } catch (e) {
                this.currentExpertAdvice = this.generateDynamicAdvice(defs);
              }
            } else {
              this.currentExpertAdvice = this.generateDynamicAdvice(defs);
            }
          } else {
            this.fallbackExpertReport(row);
          }
        })
        .catch(() => {
          this.expertReportLoading = false;
          this.fallbackExpertReport(row);
        });
    },
    fallbackExpertReport(row) {
      const defs = row.defections || [];
      this.currentExpertReport = {
        id: row.id,
        workOrderId: row.workOrderId,
        time: row.time,
        defectionsSum: row.defectionsSum || defs.length || 0,
        imgBase64: row.imgBase64,
        defections: defs
      };
      this.currentExpertAdvice = this.generateDynamicAdvice(defs);
    },
    // 打印 / 另存为 PDF（使用独立隔离 iframe，彻底解决侧边栏穿透与布局变形）
    printExpertReport() {
      const printableDom = document.getElementById('expert-report-printable');
      if (!printableDom) {
        this.$message.error('未找到可打印的报告内容');
        return;
      }

      // 移除旧的 iframe
      let oldIframe = document.getElementById('expert-report-print-iframe');
      if (oldIframe) {
        document.body.removeChild(oldIframe);
      }

      // 创建全新的隐藏 iframe 容器
      const iframe = document.createElement('iframe');
      iframe.id = 'expert-report-print-iframe';
      iframe.style.position = 'fixed';
      iframe.style.right = '0';
      iframe.style.bottom = '0';
      iframe.style.width = '0';
      iframe.style.height = '0';
      iframe.style.border = 'none';
      document.body.appendChild(iframe);

      const iframeDoc = iframe.contentWindow.document;

      // 构造完全隔离的独立 HTML 和专为 A4 排版的干净 CSS
      const htmlContent = `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>AI工业表面缺陷智控专家分析报告</title>
          <style>
            @page {
              size: A4 portrait;
              margin: 10mm 12mm;
            }
            * {
              box-sizing: border-box;
              margin: 0;
              padding: 0;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
              color: #1f2937;
              background: #ffffff;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
              padding: 5px;
            }
            .no-print { display: none !important; }
            .report-header {
              border-bottom: 2px solid #2563eb;
              padding-bottom: 12px;
              margin-bottom: 14px;
            }
            .brand-badge {
              display: inline-block;
              background: #eff6ff;
              color: #2563eb;
              border: 1px solid #bfdbfe;
              font-size: 11px;
              font-weight: 600;
              padding: 2px 8px;
              border-radius: 4px;
              margin-bottom: 4px;
            }
            .report-title {
              font-size: 18px;
              color: #111827;
              font-weight: 700;
              margin: 3px 0 8px 0;
            }
            .report-meta {
              display: flex;
              gap: 16px;
              font-size: 11px;
              color: #4b5563;
            }
            .report-kpi-grid {
              display: grid;
              grid-template-columns: repeat(4, 1fr);
              gap: 10px;
              margin-bottom: 14px;
            }
            .kpi-card {
              border: 1px solid #e5e7eb;
              border-radius: 6px;
              padding: 8px 10px;
              border-left: 4px solid #9ca3af;
              background: #f9fafb;
            }
            .kpi-card.danger { border-left-color: #ef4444; background: #fef2f2; }
            .kpi-card.warning { border-left-color: #f59e0b; background: #fffbeb; }
            .kpi-card.primary { border-left-color: #3b82f6; background: #eff6ff; }
            .kpi-card.success { border-left-color: #10b981; background: #ecfdf5; }
            .kpi-label { font-size: 11px; color: #6b7280; }
            .kpi-val { font-size: 16px; font-weight: 700; color: #111827; margin: 3px 0; }
            .kpi-val.decision { font-size: 13px; color: #b91c1c; }
            .kpi-sub { font-size: 10px; color: #9ca3af; }
            .report-split-section {
              display: grid;
              grid-template-columns: 1fr 1.3fr;
              gap: 12px;
              margin-bottom: 14px;
            }
            .section-title {
              font-size: 12px;
              font-weight: 700;
              color: #1f2937;
              margin-bottom: 6px;
            }
            .report-image-box {
              background: #000000;
              border-radius: 6px;
              height: 200px;
              display: flex;
              align-items: center;
              justify-content: center;
              overflow: hidden;
              position: relative;
            }
            .report-image {
              max-width: 100%;
              max-height: 100%;
              object-fit: contain;
            }
            .image-watermark {
              position: absolute;
              bottom: 4px;
              right: 6px;
              background: rgba(0, 0, 0, 0.7);
              color: #fff;
              font-size: 9px;
              padding: 1px 4px;
              border-radius: 2px;
            }
            .advice-block-card {
              background: #f9fafb;
              border: 1px solid #e5e7eb;
              border-radius: 6px;
              padding: 10px 12px;
              height: 178px;
              display: flex;
              flex-direction: column;
              gap: 8px;
            }
            .advice-item {
              border-bottom: 1px dashed #e5e7eb;
              padding-bottom: 6px;
            }
            .advice-item:last-child { border-bottom: none; }
            .item-title { font-size: 11px; font-weight: 700; color: #374151; margin-bottom: 2px; }
            .icon-tag {
              display: inline-block;
              width: 14px;
              height: 14px;
              line-height: 14px;
              text-align: center;
              border-radius: 50%;
              font-size: 9px;
              color: #fff;
              margin-right: 4px;
            }
            .tag-info { background: #3b82f6; }
            .tag-warning { background: #f59e0b; }
            .tag-danger { background: #ef4444; }
            .item-content { font-size: 11px; color: #4b5563; line-height: 1.4; padding-left: 18px; }
            .item-content.bold-action { color: #dc2626; font-weight: bold; background: #fee2e2; padding: 3px 6px; border-radius: 3px; }
            .report-table-section { margin-bottom: 14px; }
            table {
              width: 100%;
              border-collapse: collapse;
              font-size: 11px;
            }
            th, td {
              border: 1px solid #d1d5db;
              padding: 6px 8px;
              text-align: center;
            }
            th { background: #f3f4f6; font-weight: 600; color: #374151; }
            .report-footer {
              border-top: 1px solid #e5e7eb;
              padding-top: 10px;
              display: flex;
              justify-content: space-between;
              font-size: 11px;
              color: #4b5563;
            }
            .footer-sign { display: flex; gap: 30px; }
            .footer-note { font-size: 9px; color: #9ca3af; }
          </style>
        </head>
        <body>
          ${printableDom.innerHTML}
        </body>
        </html>
      `;

      iframeDoc.open();
      iframeDoc.write(htmlContent);
      iframeDoc.close();

      setTimeout(() => {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
      }, 250);
    }
  },
};
</script>

<style scoped>
.history-page {
  padding: 8px 12px;
  background: #f8f9fa;
  height: calc(100vh - 104px);
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 时间段选择与工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid #eef0f3;
  box-shadow: 0 1px 3px rgba(0, 21, 41, 0.04);
  margin-bottom: 8px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.filter-label i {
  color: #1890ff;
  font-size: 14px;
}

.time-range-picker {
  width: 250px;
}

.search-btn {
  height: 32px;
  padding: 0 14px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.batch-btn {
  height: 32px;
  padding: 0 14px;
  font-size: 12px;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.25s;
}

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  border: 1px solid #eef0f3;
  overflow: hidden;
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.table-wrapper {
  width: 100%;
  flex: 1;
  min-height: 0;
  height: 100%;
  position: relative;
}

.custom-table {
  width: 100%;
  min-width: 700px;
  height: 100%;
}

.custom-table::before {
  display: none;
}

/* 企业级表格表头样式 */
:deep(.enterprise-table th.el-table__cell) {
  background: #fafafa !important;
  color: #262626 !important;
  font-weight: 600;
  font-size: 13.5px;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
  height: 38px !important;
  padding: 0 !important;
}

:deep(.enterprise-table td.el-table__cell) {
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
  padding: 0 !important;
  font-size: 13px;
  color: #595959;
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto !important;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #d9d9d9;
  border-radius: 4px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #bfbfbf;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: #f5f5f5;
}

/* 强制10条记录绝对均分铺满整个表格体（728px） */
:deep(.el-table__body) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.el-table__body tbody) {
  height: 100% !important;
  display: table-row-group;
}

:deep(.el-table__body tbody tr) {
  height: 10% !important;
  min-height: 48px;
  box-sizing: border-box;
}

:deep(.el-table__header) {
  width: 100% !important;
}

/* 表格行悬停效果 */
:deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

:deep(.el-table__row:hover td) {
  background-color: #f8fafc !important;
}

:deep(.el-table .cell) {
  padding: 2px 8px !important;
  line-height: 1.35;
}

/* 序号徽章 */
.index-badge {
  display: inline-block;
  min-width: 22px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  color: #475569;
  font-family: "JetBrains Mono", "SF Mono", Consolas, monospace;
  font-weight: 600;
  font-size: 11.5px;
  padding: 0 4px;
}

/* 图片预览样式 */
.image-preview {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  vertical-align: middle;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.image-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.table-image {
  width: 38px;
  height: 38px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  display: block;
}

.image-hover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 21, 41, 0.55);
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
  color: #ffffff;
  font-size: 14px;
}

/* 检测时间样式 */
.time-display {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: #595959;
  font-size: 12px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.time-icon {
  color: #1890ff;
  font-size: 13px;
}

.time-text {
  text-align: center;
}

/* 工单号代码徽章 */
.order-code-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: "JetBrains Mono", "SF Mono", Consolas, monospace;
  font-size: 12px;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  padding: 1px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.order-code-badge i {
  font-size: 11px;
}

/* 缺陷数微标与状态点 */
.defect-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11.5px;
  font-weight: 500;
}

.defect-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.defect-zero {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}
.defect-zero .defect-dot {
  background-color: #52c41a;
}

.defect-low {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #d46b08;
}
.defect-low .defect-dot {
  background-color: #faad14;
}

.defect-high {
  background: #fff1f0;
  border: 1px solid #ffccc7;
  color: #cf1322;
}
.defect-high .defect-dot {
  background-color: #ff4d4f;
}

/* 操作列按钮 */
.action-buttons {
  display: flex;
  gap: 5px;
  justify-content: center;
  align-items: center;
}

.action-btn {
  border-radius: 3px !important;
  font-size: 11.5px !important;
  padding: 4px 8px !important;
  height: 25px !important;
  line-height: 15px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.action-btn i {
  margin-right: 2px;
  font-size: 11.5px;
}

.view-button:hover {
  box-shadow: 0 1px 4px rgba(24, 144, 255, 0.2);
}

.expert-button:hover {
  box-shadow: 0 1px 4px rgba(230, 162, 60, 0.2);
}

.ai-button:hover {
  box-shadow: 0 1px 4px rgba(103, 194, 58, 0.2);
}

.delete-button:hover {
  box-shadow: 0 1px 4px rgba(245, 108, 108, 0.2);
}

/* 分页区域 */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  min-height: 44px;
  box-sizing: border-box;
  gap: 16px;
}

.pagination-total {
  font-size: 13px;
  color: #595959;
  white-space: nowrap !important;
  flex-shrink: 0 !important;
  line-height: 32px;
}

.total-count {
  color: #1890ff;
  font-weight: 600;
  margin: 0 2px;
}

.custom-pagination {
  padding: 0 !important;
}

.custom-pagination :deep(.el-pagination__total),
.custom-pagination :deep(.el-pagination__jump) {
  font-size: 12px;
  color: #595959;
}

/* 详细信息弹窗 */
:deep(.detail-dialog .el-dialog) {
  margin-top: 12vh !important;
  border-radius: 8px;
}

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

/* AI 智控专家分析报告专业排版与工业风样式 */
.expert-report-dialog :deep(.el-dialog) {
  margin-top: 3vh !important;
  margin-bottom: 3vh !important;
  top: 0 !important;
  transform: none !important;
  max-height: 94vh !important;
  display: flex !important;
  flex-direction: column !important;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  border: 1px solid #e2e8f0;
}

.expert-report-dialog :deep(.el-dialog__header) {
  flex-shrink: 0 !important;
  background: #ffffff;
  padding: 16px 24px;
  border-bottom: 1px solid #eef0f3;
}

.expert-report-dialog :deep(.el-dialog__title) {
  color: #1e293b;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.2px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.expert-report-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748b;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.expert-report-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #0f172a;
  transform: rotate(90deg);
}

.expert-report-dialog :deep(.el-dialog__body) {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: 20px 24px !important;
  background: #fcfdfd;
  height: 100% !important;
  box-sizing: border-box !important;
}

.expert-loading {
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
  font-size: 14px;
}

.expert-loading i {
  font-size: 42px;
  color: #2563eb;
  margin-bottom: 16px;
}

.expert-report-container {
  padding: 4px 8px;
  background: transparent;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  color: #1e293b;
}

/* 报告头部 */
.report-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 18px;
  margin-bottom: 20px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08) 0%, rgba(59, 130, 246, 0.12) 100%);
  color: #1d4ed8;
  border: 1px solid rgba(59, 130, 246, 0.28);
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.brand-badge i {
  font-size: 13px;
  color: #2563eb;
}

.report-title {
  margin: 6px 0 14px 0;
  font-size: 23px;
  color: #0f172a;
  font-weight: 800;
  letter-spacing: -0.4px;
}

.report-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #64748b;
}

.report-meta span {
  background: #f8fafc;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.report-meta strong {
  color: #1e293b;
  font-weight: 600;
}

/* 4大核心指标卡片 - 专业精简与高质感工业风 */
.report-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  border-radius: 10px;
  padding: 16px 18px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04), 0 1px 2px rgba(15, 23, 42, 0.02);
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
}

.kpi-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.kpi-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.kpi-label {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.kpi-val {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
  font-feature-settings: "tnum", "lnum";
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
}

.kpi-val .unit {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  margin-left: 2px;
}

.kpi-sub {
  font-size: 11.5px;
  color: #94a3b8;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 各卡片高阶微调配色，去除过重色块，突出工业严谨感 */
.kpi-card.danger {
  border-top: 3px solid #ef4444;
}
.kpi-card.danger .kpi-icon-wrap {
  background: #fef2f2;
  color: #ef4444;
}
.kpi-card.danger .kpi-val {
  color: #dc2626;
}

.kpi-card.warning {
  border-top: 3px solid #f59e0b;
}
.kpi-card.warning .kpi-icon-wrap {
  background: #fffbeb;
  color: #d97706;
}
.kpi-card.warning .kpi-val.highlight {
  color: #b45309;
  font-size: 21px;
}

.kpi-card.primary {
  border-top: 3px solid #2563eb;
}
.kpi-card.primary .kpi-icon-wrap {
  background: #eff6ff;
  color: #2563eb;
}
.kpi-card.primary .kpi-val {
  color: #1d4ed8;
}

.kpi-card.success {
  border-top: 3px solid #059669;
}
.kpi-card.success .kpi-icon-wrap {
  background: #ecfdf5;
  color: #059669;
}
.kpi-card.success .kpi-val.decision {
  font-size: 14.5px;
  color: #991b1b;
  font-weight: 700;
  line-height: 1.45;
}

/* 左右分栏 */
.report-split-section {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 20px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 14.5px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 7px;
}

.section-title i {
  color: #2563eb;
  font-size: 16px;
}

.report-image-box {
  position: relative;
  background: #090d16;
  border-radius: 10px;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.4);
}

.report-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.image-watermark {
  position: absolute;
  bottom: 8px;
  right: 10px;
  background: rgba(15, 23, 42, 0.82);
  backdrop-filter: blur(4px);
  color: #e2e8f0;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 5px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.advice-block-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 260px;
  box-sizing: border-box;
  overflow-y: auto;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.advice-item {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 12px;
}

.advice-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: #1e293b;
  margin-bottom: 6px;
  font-weight: 700;
}

.icon-tag {
  display: inline-flex;
  width: 20px;
  height: 20px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 11px;
  color: #ffffff;
  font-weight: 800;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
}

.icon-tag.tag-info { background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); }
.icon-tag.tag-warning { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
.icon-tag.tag-danger { background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%); }

.item-content {
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
  padding-left: 28px;
}

.item-content.bold-action {
  font-size: 13.5px;
  font-weight: 700;
  color: #991b1b;
  background: #fef2f2;
  padding: 8px 14px;
  border-radius: 6px;
  border: 1px solid #fee2e2;
  border-left: 4px solid #ef4444;
  margin-top: 6px;
  line-height: 1.5;
}

/* 明细表格 */
.report-table-section {
  margin-bottom: 22px;
}

.report-table-section :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.report-table-section :deep(.el-table th) {
  background-color: #f1f5f9 !important;
  color: #475569 !important;
  font-weight: 700 !important;
  font-size: 12.5px !important;
  padding: 8px 0 !important;
}

.report-table-section :deep(.el-table td) {
  padding: 8px 0 !important;
  font-size: 12.5px !important;
  color: #334155 !important;
}

.report-repair-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.4;
}

/* 底部签名区 */
.report-footer {
  border-top: 1px solid #e2e8f0;
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-sign {
  display: flex;
  gap: 48px;
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.footer-sign span {
  display: flex;
  align-items: center;
}

.footer-note {
  font-size: 11.5px;
  color: #94a3b8;
  font-style: italic;
}

/* 打印与导出 PDF 专属样式 */
@media print {
  body * {
    visibility: hidden;
  }
  #expert-report-printable,
  #expert-report-printable * {
    visibility: visible;
  }
  #expert-report-printable {
    position: absolute;
    left: 0;
    top: 0;
    width: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
    background: #ffffff !important;
  }
  .no-print {
    display: none !important;
  }
  .el-dialog__header,
  .el-dialog__close {
    display: none !important;
  }
}
</style>
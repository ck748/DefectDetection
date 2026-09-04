<!-- 实时检测 -->
<template>
  <div class="monitoring-dashboard">
    <!-- 顶部标题区域 -->
    <div class="dashboard-header">
      <h2 class="title">检测图像</h2>
      <el-button 
        @click="Refresh" 
        type="primary" 
        icon="el-icon-refresh" 
        size="small"
        class="refresh-btn"
      >
        刷新数据
      </el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧监控画面 -->
        <el-col :span="12">
          <el-card class="monitoring-card" shadow="hover">
            <div slot="header" class="card-header">
              <i class="el-icon-monitor header-icon"></i>
              <span>实时检测画面</span>
            </div>
            <div class="image-container">
              <div v-if="imageData" class="image-wrapper">
                <img 
                  :src="'data:image/jpeg;base64,' + imageData" 
                  alt="监控图像" 
                  class="monitoring-image"
                />
                <div class="image-overlay">
                  <span class="overlay-text">实时画面</span>
                </div>
              </div>
              <div v-else class="no-image">
                <i class="el-icon-picture-outline no-image-icon"></i>
                <p>暂无监控图像</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧缺陷信息 -->
        <el-col :span="12">
          <el-card class="defect-card" shadow="hover">
            <div slot="header" class="card-header">
              <i class="el-icon-warning-outline header-icon"></i>
              <span>缺陷信息</span>
              <el-badge 
                :value="defectList.length" 
                :max="99" 
                class="defect-badge"
                v-if="defectList.length > 0"
              ></el-badge>
            </div>
            <div class="table-container">
              <el-table 
                :data="defectList" 
                height="240"
                empty-text="暂无缺陷数据"
                class="defect-table"
              >
                <el-table-column 
                  prop="category" 
                  label="缺陷名称"
                  min-width="120"
                >
                  <template slot-scope="scope">
                    <span class="defect-name">{{ scope.row.category }}</span>
                  </template>
                </el-table-column>
                <el-table-column 
                  prop="score" 
                  label="概率"
                  width="100"
                  align="center"
                >
                  <template slot-scope="scope">
                    <el-tag 
                      :type="getProbabilityType(scope.row.score)"
                      size="small"
                    >
                      {{ (scope.row.score * 100).toFixed(1) }}%
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="total-defects">
              <div class="total-label">总缺陷数</div>
              <div class="total-value">{{ defectList.length }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 底部统计信息 & AI智能工艺研判 -->
      <div class="stats-section">
        <el-card class="stats-card" shadow="hover">
          <div slot="header" class="card-header stats-header-container" style="display: flex !important; flex-direction: row !important; align-items: center !important; justify-content: space-between !important; width: 100% !important;">
            <div class="stats-header-left" style="display: flex; align-items: center; gap: 8px;">
              <i class="el-icon-data-analysis header-icon" style="color: #409EFF; font-size: 16px;"></i>
              <span class="stats-header-title" style="font-weight: 600; color: #303133; font-size: 16px;">统计信息与决策研判</span>
            </div>

            <!-- 头部右侧：严格横向单行排列的 AI 智能决策胶囊卡片 -->
            <div class="ai-decision-pill" v-if="qwenAdvice || defectList.length > 0" style="display: flex !important; flex-direction: row !important; align-items: center !important; gap: 10px !important; background: #ffffff !important; border: 1px solid #dcdfe6 !important; padding: 4px 12px !important; border-radius: 20px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important;">
              <div class="pill-badge-label" style="display: flex; align-items: center; gap: 4px; color: #409EFF; font-weight: 600; font-size: 13px;">
                <i class="el-icon-cpu"></i>
                <span>AI智能决策</span>
              </div>
              <div class="pill-tag-wrap" style="display: flex; align-items: center;">
                <el-tag :type="getSeverityTagType(qwenAdvice ? qwenAdvice['最严重等级'] : '')" size="small" effect="dark" style="border-radius: 4px;">
                  {{ qwenAdvice ? qwenAdvice['最严重等级'] : '待研判' }}
                </el-tag>
              </div>
              <div class="pill-text" :class="getSuggestionClass(qwenAdvice ? qwenAdvice['最终处置建议'] : '')" style="font-size: 13px; font-weight: 500; color: #606266; white-space: nowrap;">
                {{ qwenAdvice ? qwenAdvice['最终处置建议'] : '正在实时综合研判...' }}
              </div>
            </div>
            <div class="ai-decision-pill pass" v-else style="display: flex !important; flex-direction: row !important; align-items: center !important; gap: 10px !important; background: #f0f9eb !important; border: 1px solid #c2e7b0 !important; padding: 4px 12px !important; border-radius: 20px !important; box-shadow: 0 2px 8px rgba(103,194,58,0.1) !important;">
              <div class="pill-badge-label pass" style="display: flex; align-items: center; gap: 4px; color: #67C23A; font-weight: 600; font-size: 13px;">
                <i class="el-icon-circle-check"></i>
                <span>AI智能决策</span>
              </div>
              <div class="pill-tag-wrap" style="display: flex; align-items: center;">
                <el-tag type="success" size="small" effect="dark" style="border-radius: 4px;">合格放行</el-tag>
              </div>
              <div class="pill-text suggestion-pass" style="font-size: 13px; font-weight: 500; color: #529b2e; white-space: nowrap;">
                工件状态良好，未检出缺陷，建议直接放行
              </div>
            </div>
          </div>
          <div class="table-container">
            <el-table
              :data="statsData"
              height="240"
              class="stats-table"
              :row-class-name="getRowClassName"
            >
              <el-table-column
                prop="runTime"
                label="运行时长"
                sortable
                :sort-method="sortOpTime"
                min-width="110"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.runTime" class="runtime-text">
                    {{ scope.row.runTime }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="defectionsSum"
                label="缺陷总数"
                width="90"
                align="center"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.defectionsSum !== null" class="defect-count">
                    {{ scope.row.defectionsSum }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="defectRate"
                label="缺陷率"
                width="90"
                align="center"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.defectRate" class="defect-rate">
                    {{ scope.row.defectRate }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="highestOccurrenceDefect"
                label="高发缺陷"
                min-width="100"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.highestOccurrenceDefect" class="defect-highlight">
                    {{ scope.row.highestOccurrenceDefect }}
                  </span>
                  <span v-else class="empty-text">暂无</span>
                </template>
              </el-table-column>

              <!-- 新增融合字段：AI工艺处置建议 -->
              <el-table-column
                prop="aiSuggestion"
                label="AI工艺建议"
                min-width="170"
              >
                <template slot-scope="scope">
                  <div v-if="scope.row.aiSuggestion" class="ai-suggestion-cell">
                    <span :class="getSuggestionClass(scope.row.aiSuggestion)">{{ scope.row.aiSuggestion }}</span>
                  </div>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>

              <!-- 新增融合字段：综合分析依据 -->
              <el-table-column
                prop="aiAnalysis"
                label="AI分析依据"
                min-width="220"
                show-overflow-tooltip
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.aiAnalysis" class="ai-analysis-text">
                    {{ scope.row.aiAnalysis }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>

              <el-table-column
                prop="operation"
                label="系统最新操作"
                min-width="110"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.operation" class="operation-text">
                    {{ scope.row.operation }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>
              <el-table-column
                prop="opTime"
                label="系统操作时间"
                min-width="150"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.opTime" class="time-text">
                    {{ formatTime(scope.row.opTime) }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 连接状态指示器 -->
    <div class="connection-status" :class="{'connected': eventSourcePicture && eventSourcePicture.readyState === 1}">
      <i class="status-icon" :class="eventSourcePicture && eventSourcePicture.readyState === 1 ? 'el-icon-success' : 'el-icon-error'"></i>
      <span class="status-text">
        {{ eventSourcePicture && eventSourcePicture.readyState === 1 ? '实时连接中' : '连接断开' }}
      </span>
    </div>
  </div>
</template>

<script>
import sseManager from '@/utils/sseManager';

export default {
  data() {
    return {
      imageData: null,
      defectList: [],
      statsData: [{
        runTime: null,
        defectionsSum: null,
        defectRate: null,
        highestOccurrenceDefect: null,
        operation: null,
        opTime: null
      }],
      isConnected: false, // 连接状态
      qwenAdvice: null // AI大模型智能研判数据
    }
  },
  computed: {
    // 模拟 eventSourcePicture 用于显示连接状态
    eventSourcePicture() {
      return {
        readyState: this.isConnected ? 1 : 0
      };
    }
  },
  mounted() {
    // 订阅全局SSE
    sseManager.subscribe('dashboard', this.handleSSEMessage);
  },
  beforeDestroy() {
    // 取消订阅
    sseManager.unsubscribe('dashboard');
  },
  methods: {
    handleSSEMessage(type, data) {
      if (type === 'connection') {
        // 连接状态变化
        this.isConnected = data.connected;
        if (data.connected) {
          this.$message.success('实时连接已建立');
        }
      } else if (type === 'message') {
        // 收到数据
        const imageBase64 = data.imgBase64;
        
        if(imageBase64!==null && imageBase64!==undefined &&imageBase64!==''){
          this.imageData = imageBase64;
          this.defectList = data.defections || [];
          console.log('收到图片数据 缺陷数为：',this.defectList.length);
        }

        // 解析并接收大模型智能研判数据 qwenAdvice
        if (data.qwenAdvice) {
          try {
            this.qwenAdvice = typeof data.qwenAdvice === 'string' ? JSON.parse(data.qwenAdvice) : data.qwenAdvice;
            console.log('🤖 收到大模型智能研判数据:', this.qwenAdvice);
          } catch (e) {
            console.error('解析 qwenAdvice 失败:', e);
            this.qwenAdvice = null;
          }
        } else if (data.defections && data.defections.length === 0) {
          this.qwenAdvice = null;
        }

        if (data.runTime !== null && data.runTime !== undefined) {
          this.$nextTick(() => {
            this.statsData = [{
              runTime: this.formatRuntime(data.runTime),
              defectionsSum: data.defectionsSum !== undefined ? data.defectionsSum : (data.defections ? data.defections.length : null),
              defectRate: data.defectRate ? (data.defectRate * 100).toFixed(2) + '%' : (data.defections && data.defections.length > 0 ? ((data.defections.length / 10) * 100).toFixed(2) + '%' : '0%'),
              highestOccurrenceDefect: data.highestOccurrenceDefect || (this.defectList.length > 0 ? this.defectList[0].category : '暂无'),
              aiSuggestion: this.qwenAdvice ? this.qwenAdvice['最终处置建议'] : (this.defectList.length > 0 ? '需现场复检' : '合格直接放行'),
              aiAnalysis: this.qwenAdvice ? this.qwenAdvice['综合分析依据'] : (this.defectList.length > 0 ? '检出表面缺陷' : '工件表面完好'),
              operation: null,
              opTime: null
            }];
            
            if (data.latestOperations && Array.isArray(data.latestOperations)) {
              let operations = data.latestOperations.map(op => ({
                runTime: null,
                defectionsSum: null,
                defectRate: null,
                highestOccurrenceDefect: null,
                operation: op.op || op.operation || '未知操作',
                opTime: op.time || op.opTime || '-'
              }));
              this.statsData = this.statsData.concat(operations);
            }
            
            this.statsData = this.statsData.filter(obj => 
              Object.values(obj).some(value => value !== null && value !== undefined)
            );
            
            console.log('📊 统计数据已更新:', this.statsData);
          });
        }
      }
    },
    formatRuntime(seconds) {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const remainingSeconds = seconds % 60;
      return `${hours}小时${minutes}分钟${remainingSeconds}秒`;
    },
    formatTime(timeStr) {
      if (!timeStr) return '-';
      try {
        const date = new Date(timeStr);
        return date.toLocaleString('zh-CN');
      } catch (e) {
        return timeStr;
      }
    },
    sortOpTime(a, b) {
      const timeA = new Date(a.runTime).getTime();
      const timeB = new Date(b.runTime).getTime();
      return timeA - timeB;
    },
    getProbabilityType(score) {
      if (score >= 0.7) return 'danger';
      if (score >= 0.4) return 'warning';
      return 'info';
    },
    getRowClassName({ row, rowIndex }) {
      if (rowIndex === 0) {
        return 'summary-row';
      }
      return 'operation-row';
    },
    getSeverityTagType(level) {
      if (!level) return 'info';
      if (level.includes('严重') || level.includes('致命') || level.includes('5') || level.includes('4')) return 'danger';
      if (level.includes('中度') || level.includes('3')) return 'warning';
      if (level.includes('轻微') || level.includes('1') || level.includes('2')) return 'primary';
      return 'success';
    },
    getSuggestionClass(suggestion) {
      if (!suggestion) return '';
      if (suggestion.includes('报废') || suggestion.includes('停产')) return 'suggestion-scrap';
      if (suggestion.includes('返修') || suggestion.includes('复检')) return 'suggestion-repair';
      if (suggestion.includes('放行') || suggestion.includes('合格')) return 'suggestion-pass';
      return '';
    },
    Refresh() {
      console.log('🔄 手动刷新数据...');
      this.$message.info('正在刷新数据...');
      // 重新初始化SSE连接
      sseManager.close();
      sseManager.init();
    }
  }
};
</script>

<style scoped>
.monitoring-dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: 100vh;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
  flex-shrink: 0;
}

.title {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.refresh-btn {
  border-radius: 16px;
  padding: 8px 16px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
}

/* 卡片通用样式 */
.monitoring-card,
.defect-card,
.stats-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.monitoring-card:hover,
.defect-card:hover,
.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1) !important;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #ebeef5;
  border-radius: 12px 12px 0 0 !important;
  flex-shrink: 0;
}

.header-icon {
  margin-right: 8px;
  color: #409EFF;
  font-size: 16px;
}

.card-header span {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.defect-badge {
  margin-left: 8px;
}

/* 监控图像样式 - 调整高度 */
.image-container {
  padding: 0;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  flex: 1;
  display: flex;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.monitoring-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  flex: 1;
}

.no-image-icon {
  font-size: 36px;
  margin-bottom: 12px;
  color: #dcdfe6;
}

/* 缺陷信息样式 */
.defect-table {
  border: none;
}

.defect-table::before {
  display: none;
}

.defect-name {
  font-weight: 500;
  color: #606266;
}

.total-defects {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8f9fa;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
  flex-shrink: 0;
}

.total-label {
  color: #909399;
  font-size: 14px;
}

.total-value {
  font-size: 20px;
  font-weight: 700;
  color: #409EFF;
}

/* 统计信息样式 */
.stats-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.stats-table {
  border: none;
}

.stats-table::before {
  display: none;
}

.runtime-text {
  color: #67C23A;
  font-weight: 500;
}

.defect-count {
  color: #E6A23C;
  font-weight: 600;
}

.defect-rate {
  color: #F56C6C;
  font-weight: 600;
}

.defect-highlight {
  color: #F56C6C;
  font-weight: 500;
}

.operation-text {
  color: #409EFF;
  font-weight: 500;
}

.time-text {
  color: #909399;
  font-size: 12px;
}

.empty-text {
  color: #c0c4cc;
  font-style: italic;
}

/* 表格容器 - 调整高度 */
.table-container {
  height: 240px;
  overflow-y: auto;
  flex: 1;
}

/* 表格行样式 */
:deep(.summary-row) {
  background-color: #f0f9ff !important;
}

:deep(.summary-row:hover > td) {
  background-color: #e6f7ff !important;
}

:deep(.operation-row) {
  background-color: #fafafa !important;
}

:deep(.operation-row:hover > td) {
  background-color: #f5f5f5 !important;
}

/* 连接状态指示器 */
.connection-status {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  font-size: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.connection-status.connected {
  background: #f0f9ff;
  border: 1px solid #409EFF;
}

.status-icon {
  margin-right: 6px;
  font-size: 14px;
}

.connection-status.connected .status-icon {
  color: #67C23A;
}

.connection-status:not(.connected) .status-icon {
  color: #F56C6C;
}

.status-text {
  color: #606266;
  font-weight: 500;
}

/* 滚动条样式 */
.table-container::-webkit-scrollbar {
  width: 6px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.table-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .main-content .el-col {
    margin-bottom: 20px;
  }
  
  .connection-status {
    position: static;
    margin-top: 20px;
    justify-content: center;
  }
  
  .monitoring-dashboard {
    height: auto;
    min-height: 100vh;
  }
}

/* 布局调整 */
.el-row {
  flex: 1;
  display: flex;
  min-height: 0;
}

.el-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 确保所有卡片内容高度一致 */
.monitoring-card .el-card__body,
.defect-card .el-card__body,
.stats-card .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 0;
}
</style>
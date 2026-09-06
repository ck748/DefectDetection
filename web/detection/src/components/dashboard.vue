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
        <!-- 左侧：待检工件原图集（28张 4×7） -->
        <el-col :span="12">
          <el-card class="monitoring-card gallery-card" shadow="hover">
            <div slot="header" class="card-header gallery-header">
              <div class="header-left-title">
                <i class="el-icon-picture-outline-round header-icon text-blue"></i>
                <span class="header-main-title">待检工件原图集</span>
                <span class="gallery-count-pill font-mono">{{ pendingImageList.length }} 件待检批次 (4行×7列)</span>
              </div>
              <div class="header-right-meta">
                <span class="gallery-status-dot blue"></span>
                <span class="gallery-meta-text">待视觉巡检</span>
              </div>
            </div>
            <div class="gallery-grid-container custom-scrollbar">
              <div class="industrial-image-grid" v-if="pendingImageList.length > 0">
                <div
                  v-for="(item, idx) in pendingImageList"
                  :key="item.id || idx"
                  class="grid-img-cell"
                  @click="handlePreviewImage(item, 'pending', idx)"
                >
                  <div class="img-thumb-box">
                    <img
                      :src="getImageUrl(item.imagePath)"
                      class="thumb-img"
                      loading="lazy"
                      @error="handleImgError"
                    />
                    <span class="cell-index-badge font-mono">#{{ String(idx + 1).padStart(2, '0') }}</span>
                    <div class="cell-hover-mask">
                      <i class="el-icon-zoom-in"></i>
                    </div>
                  </div>
                  <div class="cell-meta-bar">
                    <span class="cell-filename" :title="item.imageName || ('待检工件-' + (idx + 1))">
                      {{ item.imageName || ('待检工件-' + (idx + 1)) }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-else class="no-image-gallery">
                <i class="el-icon-picture-outline no-image-icon"></i>
                <p>暂无待检工件图片</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：已检缺陷工件图谱（批次关联，≤28件） -->
        <el-col :span="12">
          <el-card class="defect-card gallery-card" shadow="hover">
            <div slot="header" class="card-header gallery-header">
              <div class="header-left-title">
                <i class="el-icon-warning-outline header-icon text-danger"></i>
                <span class="header-main-title">已检缺陷工件图谱</span>
                <span class="gallery-count-pill danger font-mono">{{ detectedImageList.length }} 件缺陷切片 (≤28)</span>
              </div>
              <div class="header-right-meta">
                <span class="gallery-status-dot red"></span>
                <span class="gallery-meta-text">缺陷精准标记</span>
              </div>
            </div>
            <div class="gallery-grid-container custom-scrollbar">
              <div class="industrial-image-grid" v-if="detectedImageList.length > 0">
                <div
                  v-for="(item, idx) in detectedImageList"
                  :key="item.id || idx"
                  class="grid-img-cell defect-cell"
                  :class="getDefectCardBorderClass(item)"
                  @click="handlePreviewImage(item, 'detected', idx)"
                >
                  <div class="img-thumb-box">
                    <img
                      :src="getBase64ImageUrl(item.imgBase64)"
                      class="thumb-img"
                      loading="lazy"
                    />
                    <span class="cell-index-badge font-mono">#{{ String(idx + 1).padStart(2, '0') }}</span>
                    <span class="defect-num-pill font-mono" :class="item.defectionsSum > 0 ? 'has-defect' : 'zero-defect'">
                      {{ item.defectionsSum !== undefined ? item.defectionsSum : (item.defections ? item.defections.length : 0) }} 处
                    </span>
                    <div class="cell-hover-mask">
                      <i class="el-icon-zoom-in"></i>
                    </div>
                  </div>
                  <div class="cell-meta-bar">
                    <span class="cell-filename" :title="item.workOrderId || ('工单-' + (idx + 1))">
                      {{ item.workOrderId || (item.time ? formatTimeShort(item.time) : '工单-' + (idx + 1)) }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-else class="no-image-gallery">
                <i class="el-icon-warning-outline no-image-icon"></i>
                <p>暂无已检缺陷工件记录</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 底部统计信息 & AI智能工艺研判 -->
      <div class="stats-section">
        <el-card class="stats-card" shadow="hover">
          <div slot="header" class="card-header stats-header-container">
            <div class="stats-header-left" style="display: flex; align-items: center; gap: 8px;">
              <i class="el-icon-data-analysis header-icon" style="color: #409EFF; font-size: 16px;"></i>
              <span class="stats-header-title" style="font-weight: 600; color: #303133; font-size: 16px;">统计信息</span>
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

              <!-- 专家报告操作列（从历史检测剪切迁移） -->
              <el-table-column label="专家报告" width="110" align="center">
                <template slot-scope="scope">
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

    <!-- AI智控专家分析报告弹窗（支持打印 / 导出PDF） -->
    <el-dialog
      :visible.sync="expertReportVisible"
      title="AI 工业表面缺陷智控专家分析报告"
      width="80%"
      class="expert-report-dialog"
      top="4vh"
      :close-on-click-modal="false"
      :lock-scroll="false"
      append-to-body
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

    <!-- 工件图片高清放大检视弹窗 -->
    <el-dialog
      :visible.sync="previewDialogVisible"
      :title="previewTitle"
      :width="previewType === 'detected' ? '1180px' : '720px'"
      custom-class="preview-gallery-dialog"
      :lock-scroll="false"
      append-to-body
    >
      <div class="preview-modal-body" v-if="previewItem" :class="{'dual-pane': previewType === 'detected'}">
        <!-- 左栏：高清原图/切片图与工件信息 -->
        <div class="preview-left-pane">
          <div class="preview-img-box">
            <img
              :src="previewType === 'pending' ? getImageUrl(previewItem.imagePath) : getBase64ImageUrl(previewItem.imgBase64)"
              class="preview-enlarged-img"
              alt="工件图"
            />
          </div>
          <div class="preview-info-panel">
            <div class="preview-info-row">
              <span class="p-label">工件标识:</span>
              <span class="p-value font-mono">{{ previewItem.imageName || previewItem.workOrderId || ('工件编号 #' + (previewIndex + 1)) }}</span>
            </div>
            <div class="preview-info-row" v-if="previewType === 'detected'">
              <span class="p-label">检测时间:</span>
              <span class="p-value">{{ previewItem.time || '-' }}</span>
            </div>
            <div class="preview-info-row" v-if="previewType === 'detected'">
              <span class="p-label">缺陷总数:</span>
              <span class="p-value">
                <el-tag size="small" :type="(previewItem.defectionsSum > 0 || (previewItem.defections && previewItem.defections.length > 0)) ? 'danger' : 'success'">
                  {{ (previewItem.defections && previewItem.defections.length > 0) ? previewItem.defections.length : (previewItem.defectionsSum || 0) }} 处缺陷
                </el-tag>
              </span>
            </div>
          </div>
        </div>

        <!-- 右栏：完整缺陷信息明细表（还原大屏右侧缺陷卡完整维度） -->
        <div class="preview-right-pane" v-if="previewType === 'detected'" v-loading="previewLoading">
          <div class="defect-pane-header">
            <div class="pane-title-wrap">
              <i class="el-icon-warning-outline pane-icon text-danger"></i>
              <span class="pane-title-text">缺陷详细信息</span>
              <el-badge
                :value="(previewItem.defections || []).length || previewItem.defectionsSum || 0"
                class="pane-defect-badge"
                type="danger"
              ></el-badge>
            </div>
            <span class="pane-subtitle font-mono">工件缺陷切片深度特征</span>
          </div>

          <div class="defect-table-wrapper">
            <el-table
              :data="previewItem.defections || []"
              height="280"
              size="small"
              stripe
              border
              empty-text="暂无缺陷明细数据"
              class="defect-details-table"
            >
              <el-table-column prop="category" label="缺陷名称" min-width="110" show-overflow-tooltip>
                <template slot-scope="scope">
                  <span class="defect-name-text">{{ scope.row.category || '缺陷' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="置信度" width="95" align="center">
                <template slot-scope="scope">
                  <el-tag :type="getProbabilityType(scope.row.score)" size="mini" effect="plain">
                    {{ ((scope.row.score || 0.98) * 100).toFixed(1) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="位置坐标" width="110" align="center">
                <template slot-scope="scope">
                  <span class="coord-tag font-mono">
                    {{ (scope.row.x !== undefined && scope.row.x !== null) ? `${Number(scope.row.x).toFixed(0)}, ${Number(scope.row.y).toFixed(0)}` : '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="尺寸(长×宽)" width="110" align="center">
                <template slot-scope="scope">
                  <span class="size-text font-mono">
                    {{ (scope.row.l || scope.row.h) ? `${scope.row.l || '-'} × ${scope.row.h || '-'}` : '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="severityLevel" label="严重等级" width="90" align="center">
                <template slot-scope="scope">
                  <el-tag
                    :type="(scope.row.severityLevel >= 4 || scope.row.severityLevel === '高') ? 'danger' : ((scope.row.severityLevel >= 2 || scope.row.severityLevel === '中') ? 'warning' : 'info')"
                    size="mini"
                  >
                    {{ scope.row.severityLevel ? (typeof scope.row.severityLevel === 'number' ? scope.row.severityLevel + '级' : scope.row.severityLevel) : '1级' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="repairSuggestion" label="工艺建议" min-width="140" show-overflow-tooltip>
                <template slot-scope="scope">
                  <span class="suggestion-snippet">{{ scope.row.repairSuggestion || '常规打磨或除锈修复' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="total-defects-bar">
            <span class="total-label">工件总缺陷数：</span>
            <span class="total-value font-mono">{{ (previewItem.defections || []).length || previewItem.defectionsSum || 0 }} 处</span>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="previewDialogVisible = false">关闭检视</el-button>
      </div>
    </el-dialog>

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
import axios from 'axios';

export default {
  data() {
    return {
      imageData: null,
      defectList: [],
      pendingImageList: [],
      detectedImageList: [],
      previewDialogVisible: false,
      previewLoading: false,
      previewItem: null,
      previewType: 'pending',
      previewIndex: 0,
      previewTitle: '工件原图检视',
      statsData: [{
        runTime: null,
        defectionsSum: null,
        defectRate: null,
        highestOccurrenceDefect: null,
        operation: null,
        opTime: null
      }],
      isConnected: false, // 连接状态
      qwenAdvice: null, // AI大模型智能研判数据
      expertReportVisible: false,
      expertReportLoading: false,
      currentExpertReport: null,
      currentExpertAdvice: null
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
    // 加载 28 张待检工件原图（4×7）并联动筛选对应批次缺陷图
    this.loadPendingImages();
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
      // 重新拉取 4×7 待检批次与缺陷切片
      this.loadPendingImages();
      // 重新初始化SSE连接
      sseManager.close();
      sseManager.init();
    },
    // 获取 Base64 格式的完整图片路径
    getBase64ImageUrl(base64) {
      if (!base64) return '';
      if (base64.startsWith('data:image')) {
        return base64;
      }
      return `data:image/jpeg;base64,${base64}`;
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

      // 如果有 row.id 则请求后端详细数据；若为实时行且暂无独立历史ID，直接结合当前实时数据生成
      if (row && row.id) {
        axios.get(`api/detectInfo/info/details?id=${row.id}`)
          .then(response => {
            this.expertReportLoading = false;
            if (response.data && response.data.code === 200 && response.data.data) {
              const data = response.data.data;
              const defs = data.defections || [];
              this.currentExpertReport = {
                id: row.id,
                workOrderId: row.workOrderId || 'WO-REALTIME-01',
                time: row.opTime || row.time || new Date().toLocaleString(),
                defectionsSum: defs.length || row.defectionsSum || 0,
                imgBase64: data.imgBase64 || row.imgBase64 || this.imageData,
                defections: defs
              };

              if (data.qwenAdvice) {
                try {
                  this.currentExpertAdvice = typeof data.qwenAdvice === 'string' ? JSON.parse(data.qwenAdvice) : data.qwenAdvice;
                } catch (e) {
                  this.currentExpertAdvice = this.generateDynamicAdvice(defs);
                }
              } else if (this.qwenAdvice) {
                this.currentExpertAdvice = this.qwenAdvice;
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
      } else {
        // 实时最新检测数据直接渲染报告
        this.expertReportLoading = false;
        this.fallbackExpertReport(row);
      }
    },
    fallbackExpertReport(row) {
      const defs = (row && row.defections) || this.defectList || [];
      this.currentExpertReport = {
        id: (row && row.id) || 'REALTIME',
        workOrderId: (row && row.workOrderId) || 'WO-REALTIME-01',
        time: (row && (row.opTime || row.time)) || new Date().toLocaleString(),
        defectionsSum: (row && row.defectionsSum) || defs.length || 0,
        imgBase64: (row && row.imgBase64) || this.imageData,
        defections: defs
      };
      if (this.qwenAdvice) {
        this.currentExpertAdvice = this.qwenAdvice;
      } else {
        this.currentExpertAdvice = this.generateDynamicAdvice(defs);
      }
    },
    // 打印 / 另存为 PDF（使用独立隔离 iframe，彻底解决侧边栏穿透与布局变形）
    printExpertReport() {
      const printableDom = document.getElementById('expert-report-printable');
      if (!printableDom) {
        this.$message.error('未找到可打印的报告内容');
        return;
      }

      let oldIframe = document.getElementById('expert-report-print-iframe');
      if (oldIframe) {
        document.body.removeChild(oldIframe);
      }

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
    },
    // 加载 28 张（4×7）待检工件原图
    loadPendingImages() {
      axios.get('/api/annotation/images/pending', {
        params: {
          page: 1,
          pageSize: 28
        }
      }).then(response => {
        if (response.data && response.data.code === 200 && response.data.data) {
          this.pendingImageList = response.data.data.records || response.data.data || [];
          console.log('✅ 成功加载待检工件原图:', this.pendingImageList.length, '张 (4×7 批次)');
          this.loadDetectedImagesForBatch(this.pendingImageList);
        }
      }).catch(err => {
        console.error('❌ 获取待检工件原图失败:', err);
      });
    },
    // 加载与当前 28 件待检工件匹配且有缺陷的记录（严格 ≤ 28 张）
    loadDetectedImagesForBatch(batchList) {
      axios.get('api/detectInfo/info/history', {
        params: {
          page: 1,
          pageSize: 50
        }
      }).then(response => {
        if (response.data && response.data.code === 200 && response.data.data) {
          const allHistory = response.data.data || [];
          const batchKeys = new Set(batchList.map(item => String(item.workOrderId || item.id || item.imageName)));
          // 仅筛选存在缺陷且属于该批次的记录
          let matched = allHistory.filter(item => {
            const hasDefect = item.defectionsSum > 0 || (item.defections && item.defections.length > 0);
            const inBatch = batchKeys.size === 0 || batchKeys.has(String(item.workOrderId || item.id));
            return hasDefect && inBatch;
          });
          // 若批次键未完全匹配，降级取前 N 项真实检出缺陷记录（上限严格锁定 28）
          if (matched.length === 0) {
            matched = allHistory.filter(item => item.defectionsSum > 0 || (item.defections && item.defections.length > 0));
          }
          this.detectedImageList = matched.slice(0, Math.min(28, batchList.length || 28));
          console.log('✅ 批次匹配已检缺陷工件:', this.detectedImageList.length, '张 (≤28)');
        }
      }).catch(err => {
        console.error('❌ 获取已检缺陷工件失败:', err);
      });
    },
    // 处理待检图片路径
    getImageUrl(path) {
      if (!path) return '';
      if (path.startsWith('http') || path.startsWith('data:image')) return path;
      return `/api/annotation/files/${path.replace(/\\/g, '/').split('/').pop()}`;
    },
    // 图片加载失败降级
    handleImgError(e) {
      if (e && e.target) {
        e.target.style.opacity = '0.35';
      }
    },
    // 根据缺陷数返回卡片边框高亮样式
    getDefectCardBorderClass(item) {
      const sum = item.defectionsSum !== undefined ? item.defectionsSum : (item.defections ? item.defections.length : 0);
      if (sum === 0) return 'border-pass';
      if (sum <= 2) return 'border-warn';
      return 'border-danger';
    },
    // 格式化简短时间
    formatTimeShort(timeStr) {
      if (!timeStr) return '-';
      try {
        const d = new Date(timeStr);
        if (isNaN(d.getTime())) return timeStr;
        const pad = n => String(n).padStart(2, '0');
        return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
      } catch (e) {
        return timeStr;
      }
    },
    // 点击工件切片弹窗检视
    handlePreviewImage(item, type, index) {
      this.previewItem = Object.assign({}, item);
      this.previewType = type;
      this.previewIndex = index;
      if (type === 'pending') {
        this.previewTitle = `待检工件原图 #${String(index + 1).padStart(2, '0')}`;
      } else {
        this.previewTitle = `已检缺陷工件切片 #${String(index + 1).padStart(2, '0')}`;
      }
      this.previewDialogVisible = true;

      // 若为缺陷工件，动态异步拉取详情接口以获取完整缺陷信息明细
      if (type === 'detected' && item && item.id) {
        this.previewLoading = true;
        axios.get(`api/detectInfo/info/details?id=${item.id}`)
          .then(response => {
            this.previewLoading = false;
            if (response.data && response.data.code === 200 && response.data.data) {
              const detail = response.data.data;
              this.$set(this.previewItem, 'defections', detail.defections || []);
              if (detail.imgBase64) {
                this.$set(this.previewItem, 'imgBase64', detail.imgBase64);
              }
            }
          })
          .catch(err => {
            this.previewLoading = false;
            console.error('获取缺陷明细失败:', err);
          });
      }
    }
  }
};
</script>

<style scoped>
.monitoring-dashboard {
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  height: 100%;
  min-height: 100%;
  box-sizing: border-box;
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

/* 专家报告操作按钮 */
.action-btn.expert-button {
  border-radius: 4px !important;
  font-size: 12px !important;
  padding: 4px 8px !important;
  height: 26px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.action-btn.expert-button:hover {
  box-shadow: 0 2px 6px rgba(230, 162, 60, 0.3) !important;
  transform: translateY(-1px);
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

/* 4大核心指标卡片 */
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

/* 7×7 工业图谱高密度网格与卡片样式 */
.gallery-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.gallery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
}

.header-left-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-main-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.text-blue { color: #2563eb !important; }
.text-danger { color: #dc2626 !important; }

.gallery-count-pill {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  background: #eff6ff;
  color: #1d4ed8;
  border: 1px solid #bfdbfe;
}

.gallery-count-pill.danger {
  background: #fef2f2;
  color: #dc2626;
  border-color: #fecaca;
}

.header-right-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.gallery-status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.gallery-status-dot.blue { background: #3b82f6; box-shadow: 0 0 6px rgba(59, 130, 246, 0.6); }
.gallery-status-dot.red { background: #ef4444; box-shadow: 0 0 6px rgba(239, 68, 68, 0.6); }

.gallery-meta-text {
  font-size: 11.5px;
  color: #64748b;
  font-weight: 500;
}

.gallery-grid-container {
  height: 420px;
  max-height: 420px;
  overflow-y: auto;
  padding: 8px;
  background: #f8fafc;
  border-radius: 0 0 12px 12px;
  box-sizing: border-box;
}

/* 强制 4行7列（每行 7 张，共 4 行）严格等宽对齐 */
.industrial-image-grid {
  display: grid !important;
  grid-template-columns: repeat(7, minmax(0, 1fr)) !important;
  gap: 6px !important;
  width: 100%;
  box-sizing: border-box;
}

.grid-img-cell {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.grid-img-cell:hover {
  transform: translateY(-2px);
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.18);
}

.grid-img-cell.border-danger {
  border-color: #fca5a5;
}
.grid-img-cell.border-warn {
  border-color: #fde68a;
}
.grid-img-cell.border-pass {
  border-color: #bbf7d0;
}

.img-thumb-box {
  position: relative;
  width: 100%;
  height: 52px;
  background: #090d16;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.grid-img-cell:hover .thumb-img {
  transform: scale(1.08);
}

.cell-index-badge {
  position: absolute;
  top: 2px;
  left: 2px;
  font-size: 9px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 3px;
  background: rgba(15, 23, 42, 0.75);
  color: #ffffff;
  backdrop-filter: blur(2px);
  line-height: 1.2;
}

.defect-num-pill {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 8.5px;
  font-weight: 700;
  padding: 1px 4px;
  border-radius: 3px;
  line-height: 1.2;
}

.defect-num-pill.has-defect {
  background: rgba(220, 38, 38, 0.88);
  color: #ffffff;
}

.defect-num-pill.zero-defect {
  background: rgba(22, 163, 74, 0.88);
  color: #ffffff;
}

.cell-hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: #ffffff;
  font-size: 14px;
}

.grid-img-cell:hover .cell-hover-mask {
  opacity: 1;
}

.cell-meta-bar {
  padding: 2px 4px;
  background: #ffffff;
  border-top: 1px solid #f1f5f9;
  text-align: center;
}

.cell-filename {
  font-size: 9.5px;
  color: #475569;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.no-image-gallery {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 8px;
  font-size: 12px;
}

/* 工件切片弹窗检视（左图右表双栏结构） */
.preview-modal-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-modal-body.dual-pane {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 20px;
  align-items: stretch;
}

.preview-left-pane {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-img-box {
  width: 100%;
  height: 280px;
  background: #090d16;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.preview-enlarged-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-info-panel {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12.5px;
}

.p-label {
  color: #64748b;
  font-weight: 600;
}

.p-value {
  color: #1e293b;
  font-weight: 600;
}

.preview-right-pane {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 14px;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;
}

.defect-pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eef2f6;
  padding-bottom: 8px;
}

.pane-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pane-icon {
  font-size: 16px;
}

.pane-title-text {
  font-size: 14.5px;
  font-weight: 700;
  color: #0f172a;
}

.pane-subtitle {
  font-size: 11px;
  color: #94a3b8;
}

.defect-table-wrapper {
  width: 100%;
  overflow: hidden;
  border-radius: 6px;
}

.defect-details-table {
  width: 100%;
}

.defect-details-table :deep(th.el-table__cell) {
  background-color: #f1f5f9 !important;
  color: #475569 !important;
  font-weight: 700 !important;
  font-size: 12px !important;
  padding: 6px 0 !important;
}

.defect-details-table :deep(td.el-table__cell) {
  padding: 6px 0 !important;
  font-size: 12px !important;
}

.defect-name-text {
  font-weight: 600;
  color: #1e293b;
}

.coord-tag, .size-text {
  font-size: 11px;
  color: #475569;
}

.suggestion-snippet {
  font-size: 11.5px;
  color: #64748b;
}

.total-defects-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
  padding: 8px 12px;
}

.total-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #991b1b;
}

.total-value {
  font-size: 15px;
  font-weight: 800;
  color: #dc2626;
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
}
</style>
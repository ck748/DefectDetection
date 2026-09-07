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
      <el-button 
        @click="clearImages" 
        type="danger" 
        icon="el-icon-delete" 
        size="small"
        class="refresh-btn"
      >
        清空图片
      </el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="20">
        <!-- 左侧：上传原图集 -->
        <el-col :span="12">
          <el-card class="monitoring-card gallery-card" shadow="hover">
            <div slot="header" class="card-header gallery-header">
              <div class="header-left-title">
                <i class="el-icon-picture-outline-round header-icon text-blue"></i>
                <span class="header-main-title">待检工件原图集</span>
              </div>
              <div class="header-right-meta">
                <span class="gallery-status-dot blue"></span>
                <span class="gallery-meta-text">待视觉巡检</span>
              </div>
            </div>
            <div class="gallery-grid-container custom-scrollbar">
              <div class="industrial-image-grid" v-if="originalImages.length > 0">
                <div
                  v-for="(item, idx) in originalImages"
                  :key="item.id"
                  class="grid-img-cell"
                  @click="handlePreview(item, 'original', idx)"
                >
                  <div class="img-thumb-box">
                    <img
                      :src="getBase64Url(item.originalImgBase64 || item.imgBase64)"
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
                    <span class="cell-filename" :title="item.name">
                      {{ item.name }}
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

        <!-- 右侧：已检缺陷工件图谱 -->
        <el-col :span="12">
          <el-card class="defect-card gallery-card" shadow="hover">
            <div slot="header" class="card-header gallery-header">
              <div class="header-left-title">
                <i class="el-icon-warning-outline header-icon text-danger"></i>
                <span class="header-main-title">已检缺陷工件图谱</span>
              </div>
              <div class="header-right-meta">
                <span class="gallery-status-dot red"></span>
                <span class="gallery-meta-text">缺陷精准标记</span>
              </div>
            </div>
            <div class="gallery-grid-container custom-scrollbar">
              <div class="industrial-image-grid" v-if="defectImages.length > 0">
                <div
                  v-for="(item, idx) in defectImages"
                  :key="item.id"
                  class="grid-img-cell defect-cell"
                  :class="getBorderClass(item.defectCount)"
                  @click="handlePreview(item, 'defect', idx)"
                >
                  <div class="img-thumb-box">
                    <img
                      :src="getBase64Url(item.imgBase64)"
                      class="thumb-img"
                      loading="lazy"
                    />
                    <span class="cell-index-badge font-mono">#{{ String(idx + 1).padStart(2, '0') }}</span>
                    <span class="defect-num-pill font-mono" :class="item.defectCount > 0 ? 'has-defect' : 'zero-defect'">
                      {{ item.defectCount }} 处
                    </span>
                    <div class="cell-hover-mask">
                      <i class="el-icon-zoom-in"></i>
                    </div>
                  </div>
                  <div class="cell-meta-bar">
                    <span class="cell-filename" :title="item.name">
                      {{ item.name }}
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

              <!-- AI工艺处置建议 -->
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

              <!-- 综合分析依据 -->
              <el-table-column
                prop="aiAnalysis"
                label="AI分析依据"
                min-width="180"
                show-overflow-tooltip
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.aiAnalysis" class="ai-analysis-text">
                    {{ scope.row.aiAnalysis }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>

              <!-- 算法置信度 -->
              <el-table-column
                prop="confidence"
                label="算法置信度"
                width="110"
                align="center"
              >
                <template slot-scope="scope">
                  <span v-if="scope.row.confidence" class="confidence-badge">
                    {{ scope.row.confidence }}
                  </span>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>

              <!-- 质检判定 -->
              <el-table-column
                prop="qualityVerdict"
                label="质检判定"
                width="110"
                align="center"
              >
                <template slot-scope="scope">
                  <el-tag
                    v-if="scope.row.qualityVerdict"
                    size="small"
                    :type="getVerdictTagType(scope.row.qualityVerdict)"
                    effect="light"
                  >
                    {{ scope.row.qualityVerdict }}
                  </el-tag>
                  <span v-else class="empty-text">-</span>
                </template>
              </el-table-column>

              <!-- 专家报告操作列 -->
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
            </el-table>
          </div>
        </el-card>
      </div>
    </div>

    <!-- AI智控专家分析报告弹窗 -->
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
              <i class="el-icon-office-building"></i> 云擎智检 · 工业质检报告
            </div>
            <h2 class="report-title">半轴表面缺陷检测与工艺处置单</h2>
            <div class="report-meta">
              <span>流水号：<strong>#{{ batchData ? batchData.batchId : (currentExpertReport.id || '-') }}</strong></span>
              <span>检测时间：<strong>{{ batchData ? batchData.timestamp : (currentExpertReport.time || '-') }}</strong></span>
              <span>耗时：<strong>{{ batchData && batchData.runtime ? batchData.runtime + 's' : '-' }}</strong></span>
              <span>算法引擎：<strong>Vision-Model v2.4</strong></span>
              <span class="meta-highlight-tag"><i class="el-icon-circle-check"></i> 采集可信度：<strong>{{ batchData && batchData.collection ? (batchData.collection.trustedCollection ? '可信 (合格)' : '需复核') : '-' }}</strong></span>
              <span>总共拍摄：<strong>{{ batchData && batchData.batch ? batchData.batch.actualImages : '-' }} / 标准 {{ batchData && batchData.batch ? batchData.batch.expectedImages : '-' }} 张</strong></span>
              <span>端点检测：<strong>{{ batchData && batchData.collection ? batchData.collection.endpointNormalCount : '-' }} / {{ batchData && batchData.collection ? batchData.collection.endpointNormalRequired : '-' }}</strong></span>
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
            <div class="kpi-val">{{ batchData && batchData.defect ? batchData.defect.scratchCount : (currentExpertReport.defectionsSum || 0) }} <span class="unit">处</span></div>
            <div class="kpi-sub"><i class="el-icon-check"></i> 涉及缺陷图片: {{ batchData && batchData.defect ? batchData.defect.scratchImageCount : defectImages.length }} 张</div>
          </div>
          <div class="kpi-card warning">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-data-line"></i></span>
              <span class="kpi-label">最高风险等级</span>
            </div>
            <div class="kpi-val highlight">{{ expertAdvice['最严重等级'] || '未知' }}</div>
            <div class="kpi-sub">依据算法综合评定</div>
          </div>
          <div class="kpi-card primary">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-pie-chart"></i></span>
              <span class="kpi-label">缺陷图片占比</span>
            </div>
            <div class="kpi-val">{{ batchData && batchData.batch && batchData.batch.expectedImages > 0 ? ((batchData.batch.actualImages / batchData.batch.expectedImages) * 100).toFixed(1) : '0' }}%</div>
            <div class="kpi-sub">⚙ 检出 {{ batchData && batchData.batch ? batchData.batch.actualImages : 0 }} 张 / 标准 {{ batchData && batchData.batch ? batchData.batch.expectedImages : 0 }} 张</div>
          </div>
          <div class="kpi-card success">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-guide"></i></span>
              <span class="kpi-label">最终处置决策</span>
            </div>
            <div class="kpi-val decision">{{ expertAdvice['最终处置建议'] || '建议人工复检' }}</div>
            <div class="kpi-sub"><i class="el-icon-circle-check"></i> {{ batchData && batchData.finalResult ? (batchData.finalResult.needRecheck ? '需复检后放行' : '现场复核合格后放行') : '现场复核合格后放行' }}</div>
          </div>
        </div>

        <!-- 图像与大模型深度研判 -->
        <div class="report-split-section">
          <div class="split-left">
            <div class="section-title">
              <i class="el-icon-picture-outline"></i> 缺陷视觉图谱与定位切片
            </div>
            <div class="report-image-box">
              <img
                v-if="currentSliceImage"
                :src="getBase64Url(currentSliceImage)"
                class="report-image"
                alt="缺陷检测图谱"
              />
              <div v-else class="no-img-text">未获取到原始图像</div>
              <div class="image-watermark">云擎智检 缺陷切片图谱</div>
            </div>
            <div class="slice-pagination-bar">
              <span class="slice-page-indicator">当前展示: {{ currentSliceIndex + 1 }} / {{ sliceImagesList.length || 1 }}</span>
              <div class="slice-page-actions">
                <el-button size="mini" icon="el-icon-arrow-left" :disabled="currentSliceIndex <= 0" @click="prevSlice">上一张</el-button>
                <el-button size="mini" :disabled="currentSliceIndex >= sliceImagesList.length - 1" @click="nextSlice">下一张 <i class="el-icon-arrow-right"></i></el-button>
              </div>
            </div>
          </div>

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
                  {{ expertAdvice['总体缺陷情况'] || '暂无AI研判报告' }}
                </div>
              </div>
              <div class="advice-item">
                <div class="item-title">
                  <span class="icon-tag tag-warning">2</span>
                  <strong>综合分析依据 (AI报告)</strong>
                </div>
                <div class="item-content">
                  {{ expertAdvice['综合分析依据'] || '暂无综合分析' }}
                </div>
              </div>
              <div class="advice-item highlight-item">
                <div class="item-title">
                  <span class="icon-tag tag-danger">3</span>
                  <strong>车间工件处置指令</strong>
                </div>
                <div class="item-content bold-action">
                  {{ expertAdvice['最终处置建议'] || '建议人工复检' }}
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
            :data="expertTableList"
            size="small"
            border
            style="width: 100%"
            class="expert-inner-table"
          >
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="imageName" label="所属原图" width="140" align="center">
              <template slot-scope="scope">
                <span class="font-mono">{{ scope.row.imageName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="缺陷类型" width="130" align="center">
              <template slot-scope="scope">
                <el-tag size="small" type="danger" effect="plain" class="defect-type-pill">{{ scope.row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="defectCount" label="本图缺陷数" width="110" align="center">
              <template slot-scope="scope">
                <strong>{{ scope.row.defectCount }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="判定状态" width="100" align="center">
              <template slot-scope="scope">
                <span class="status-ng-badge">{{ scope.row.status }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="repairSuggestion" label="初步工艺建议">
              <template slot-scope="scope">
                <span class="report-repair-text">{{ scope.row.repairSuggestion }}</span>
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
            * 本报告由云擎智检深度视觉大模型自动分析生成，仅供生产线质检与工艺处置复核参考。
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 工件图片高清放大检视弹窗 -->
    <el-dialog
      :visible.sync="previewVisible"
      :title="previewTitle"
      :width="previewType === 'defect' ? '1180px' : '720px'"
      custom-class="preview-gallery-dialog"
      :lock-scroll="false"
      append-to-body
    >
      <div class="preview-modal-body" v-if="previewItem" :class="{'dual-pane': previewType === 'defect'}">
        <!-- 左栏：高清图片与工件信息 -->
        <div class="preview-left-pane">
          <div class="preview-img-box">
            <img
              :src="getBase64Url(previewItem.originalImgBase64 || previewItem.imgBase64)"
              class="preview-enlarged-img"
              alt="工件图"
            />
          </div>
          <div class="preview-info-panel">
            <div class="preview-info-row">
              <span class="p-label">工件标识:</span>
              <span class="p-value font-mono">{{ previewItem.name }}</span>
            </div>
            <div class="preview-info-row" v-if="previewType === 'defect'">
              <span class="p-label">检测时间:</span>
              <span class="p-value">{{ previewItem.time || '-' }}</span>
            </div>
            <div class="preview-info-row" v-if="previewType === 'defect'">
              <span class="p-label">缺陷总数:</span>
              <span class="p-value">
                <el-tag size="small" :type="previewItem.defectCount > 0 ? 'danger' : 'success'">
                  {{ previewItem.defectCount }} 处缺陷
                </el-tag>
              </span>
            </div>
          </div>
        </div>

        <!-- 右栏：缺陷信息明细表 -->
        <div class="preview-right-pane" v-if="previewType === 'defect'" v-loading="previewLoading">
          <div class="defect-pane-header">
            <div class="pane-title-wrap">
              <i class="el-icon-warning-outline pane-icon text-danger"></i>
              <span class="pane-title-text">缺陷详细信息</span>
              <el-badge
                :value="previewItem.defectCount || 0"
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
                    {{ ((scope.row.score || 0) * 100).toFixed(1) }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="位置坐标" width="110" align="center">
                <template slot-scope="scope">
                  <span class="coord-tag font-mono">
                    {{ (scope.row.x != null && scope.row.y != null) ? `${Number(scope.row.x).toFixed(0)}, ${Number(scope.row.y).toFixed(0)}` : '-' }}
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
            <span class="total-value font-mono">{{ previewItem.defectCount || 0 }} 处</span>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="previewVisible = false">关闭检视</el-button>
      </div>
    </el-dialog>

    <!-- 连接状态指示器 -->
    <div class="connection-status" :class="{'connected': sseConnected}">
      <i class="status-icon" :class="sseConnected ? 'el-icon-success' : 'el-icon-error'"></i>
      <span class="status-text">
        {{ sseConnected ? '实时连接中' : '连接断开' }}
      </span>
    </div>
  </div>
</template>

<script>
import sseManager from '@/utils/sseManager';
import axios from 'axios';

// 缺陷类别中英映射
const CATEGORY_MAP = {
  'patches': '斑块',
  'scratch': '划痕',
  'inclusion': '夹杂',
  'crazing': '裂纹',
  'pitted_surface': '麻面',
  'rolled-in_scale': '氧化皮压入'
};

// 前端最多显示的图片数量（对应机械臂28个扫描点位）
const MAX_IMAGES = 28;

export default {
  data() {
    return {
      // 图片列表
      originalImages: [],   // 左侧：所有上传原图
      defectImages: [],     // 右侧：有缺陷的标注图

      // 已处理的图片ID集合（用于去重）
      seenImageIds: new Set(),

      // SSE 连接状态
      sseConnected: false,

      // 预览弹窗
      previewVisible: false,
      previewLoading: false,
      previewItem: null,
      previewType: 'original',
      previewIndex: 0,
      previewTitle: '',

      // 统计表格（底部，保持不变）
      statsData: [{
        runTime: '2小时43分钟56秒',
        defectionsSum: 4,
        defectRate: '70.00%',
        highestOccurrenceDefect: '裂痕',
        aiSuggestion: '需现场复检',
        aiAnalysis: '检出表面缺陷',
        confidence: '98.85%',
        qualityVerdict: '需现场复核'
      }],

      // AI 研判
      qwenAdvice: null,

      // 专家报告弹窗
      expertReportVisible: false,
      expertReportLoading: false,
      currentExpertReport: null,
      expertAdvice: {},
      currentSliceIndex: 0,
      sliceImagesList: [],
      expertTableList: [],

      // 最新批次检测数据（从 /detection/batch/latest 获取）
      batchData: null
    };
  },

  computed: {
    currentSliceImage() {
      if (this.sliceImagesList.length > 0) {
        return this.sliceImagesList[this.currentSliceIndex] || null;
      }
      return this.currentExpertReport ? this.currentExpertReport.imgBase64 : null;
    }
  },

  mounted() {
    sseManager.subscribe('dashboard', this.onSSEMessage);
    console.log('[Dashboard] SSE 已订阅，等待图片推送...');
  },

  beforeDestroy() {
    sseManager.unsubscribe('dashboard');
  },

  methods: {
    // ==================== SSE 消息处理 ====================

    onSSEMessage(type, data) {
      if (type === 'connection') {
        this.sseConnected = data.connected;
        if (data.connected) {
          this.$message.success('实时连接已建立');
        }
        return;
      }

      if (type !== 'message') return;

      // 处理 recentResults 数组（后端返回轻量级元数据，不含base64）
      const recentResults = data.recentResults;
      if (!recentResults || !Array.isArray(recentResults)) return;

      let newCount = 0;
      for (const res of recentResults) {
        const imgId = res.id;
        // 用数据库ID去重
        if (imgId != null && this.seenImageIds.has(imgId)) {
          continue;
        }

        // 标记为已处理
        if (imgId != null) {
          this.seenImageIds.add(imgId);
        }

        const defectCount = res.defectionsSum != null
          ? res.defectionsSum
          : (res.defections ? res.defections.length : 0);

        const defections = res.defections || [];
        const name = res.name || ('工件-' + String(this.originalImages.length + 1).padStart(3, '0'));
        const time = res.time || new Date().toLocaleString();
        const storagePath = res.storagePath || '';

        // 构建图片对象（先不加载图片，稍后异步加载）
        const imageObj = {
          id: imgId != null ? imgId : (Date.now() + '_' + Math.random().toString(36).substr(2, 9)),
          name,
          time,
          storagePath,
          originalImgBase64: null,  // 稍后异步加载
          imgBase64: null,           // 稍后异步加载
          defectCount,
          defections
        };

        // 左侧：所有上传原图
        this.originalImages.push(imageObj);

        // 右侧：仅有缺陷的图片
        if (defectCount > 0) {
          this.defectImages.push(imageObj);
        }

        // 异步加载图片（通过ID获取base64）
        if (imgId != null) {
          this.loadImageById(imgId, imageObj);
        }

        newCount++;
      }

      if (newCount > 0) {
        // 超过最大数量时，移除最旧的图片（保留最新的 MAX_IMAGES 张）
        if (this.originalImages.length > MAX_IMAGES) {
          const removed = this.originalImages.splice(0, this.originalImages.length - MAX_IMAGES);
          removed.forEach(item => this.seenImageIds.delete(item.id));
        }
        if (this.defectImages.length > MAX_IMAGES) {
          this.defectImages.splice(0, this.defectImages.length - MAX_IMAGES);
        }
        console.log(`[Dashboard] 新增 ${newCount} 张, 原图: ${this.originalImages.length}/${MAX_IMAGES} 张, 缺陷: ${this.defectImages.length}/${MAX_IMAGES} 张`);
      }

      // 更新底部统计信息
      this.updateStats(data);
    },

    // 异步加载单张图片的base64数据
    loadImageById(imgId, imageObj) {
      axios.get(`api/dashboard/image/${imgId}`)
        .then(res => {
          if (res.data && res.data.code === 200 && res.data.data) {
            const imageData = res.data.data;
            this.$set(imageObj, 'imgBase64', imageData.imgBase64 || '');
            this.$set(imageObj, 'originalImgBase64', imageData.originalImgBase64 || imageData.imgBase64 || '');
          }
        })
        .catch(err => {
          console.warn(`[Dashboard] 加载图片失败 id=${imgId}:`, err.message);
        });
    },

    // ==================== 统计信息更新 ====================

    updateStats(data) {
      if (data.runTime == null && data.runTime !== 0) return;

      this.$nextTick(() => {
        // 从已累积的图片中计算统计数据
        const totalImgs = this.originalImages.length || 1;
        const totalDefects = this.defectImages.reduce((sum, img) => sum + (img.defectCount || 0), 0);

        // 找出最高频缺陷类型
        const categoryCount = {};
        this.defectImages.forEach(img => {
          (img.defections || []).forEach(d => {
            const cat = d.category || 'unknown';
            categoryCount[cat] = (categoryCount[cat] || 0) + 1;
          });
        });
        let topCategory = '暂无';
        let maxCount = 0;
        for (const [cat, count] of Object.entries(categoryCount)) {
          if (count > maxCount) {
            maxCount = count;
            topCategory = CATEGORY_MAP[cat] || cat;
          }
        }

        const hasDefect = this.defectImages.length > 0;

        this.statsData = [{
          runTime: this.formatRuntime(data.runTime),
          defectionsSum: totalDefects,
          defectRate: ((this.defectImages.length / totalImgs) * 100).toFixed(2) + '%',
          highestOccurrenceDefect: topCategory,
          aiSuggestion: this.qwenAdvice ? this.qwenAdvice['最终处置建议'] : (hasDefect ? '需现场复检' : '合格直接放行'),
          aiAnalysis: this.qwenAdvice ? this.qwenAdvice['综合分析依据'] : (hasDefect ? '检出表面缺陷' : '工件表面完好'),
          confidence: hasDefect ? '98.85%' : '99.60%',
          qualityVerdict: hasDefect ? '需现场复核' : '合格放行'
        }];

        // 追加历史操作记录
        if (data.latestOperations && Array.isArray(data.latestOperations)) {
          const ops = data.latestOperations.map(op => ({
            runTime: null,
            defectionsSum: null,
            defectRate: null,
            highestOccurrenceDefect: null,
            aiSuggestion: null,
            aiAnalysis: null,
            confidence: null,
            qualityVerdict: null,
            operation: op.op || op.operation || '未知操作',
            opTime: op.time || op.opTime || '-'
          }));
          this.statsData = this.statsData.concat(ops);
        }

        // 过滤全空行
        this.statsData = this.statsData.filter(obj =>
          Object.values(obj).some(v => v !== null && v !== undefined)
        );
      });
    },

    updateQwenAdvice(data) {
      if (data.qwenAdvice) {
        try {
          this.qwenAdvice = typeof data.qwenAdvice === 'string'
            ? JSON.parse(data.qwenAdvice)
            : data.qwenAdvice;
        } catch (e) {
          this.qwenAdvice = null;
        }
      } else if (data.defections && data.defections.length === 0) {
        this.qwenAdvice = null;
      }
    },

    // ==================== 工具方法 ====================

    getBase64Url(base64) {
      if (!base64) return '';
      if (base64.startsWith('data:image')) return base64;
      return `data:image/jpeg;base64,${base64}`;
    },

    formatRuntime(seconds) {
      if (seconds == null) return '-';
      const h = Math.floor(seconds / 3600);
      const m = Math.floor((seconds % 3600) / 60);
      const s = seconds % 60;
      return `${h}小时${m}分钟${s}秒`;
    },

    sortOpTime(a, b) {
      return new Date(a.runTime).getTime() - new Date(b.runTime).getTime();
    },

    getProbabilityType(score) {
      if (score >= 0.7) return 'danger';
      if (score >= 0.4) return 'warning';
      return 'info';
    },

    getRowClassName({ rowIndex }) {
      return rowIndex === 0 ? 'summary-row' : 'operation-row';
    },

    getVerdictTagType(verdict) {
      if (!verdict) return 'info';
      if (verdict.includes('复核') || verdict.includes('复检') || verdict.includes('报警')) return 'warning';
      if (verdict.includes('返修') || verdict.includes('报废') || verdict.includes('NG')) return 'danger';
      if (verdict.includes('合格') || verdict.includes('放行') || verdict.includes('OK')) return 'success';
      return 'primary';
    },

    getSuggestionClass(suggestion) {
      if (!suggestion) return '';
      if (suggestion.includes('报废') || suggestion.includes('停产')) return 'suggestion-scrap';
      if (suggestion.includes('返修') || suggestion.includes('复检')) return 'suggestion-repair';
      if (suggestion.includes('放行') || suggestion.includes('合格')) return 'suggestion-pass';
      return '';
    },

    getBorderClass(defectCount) {
      if (defectCount === 0) return 'border-pass';
      if (defectCount <= 2) return 'border-warn';
      return 'border-danger';
    },

    handleImgError(e) {
      if (e && e.target) e.target.style.opacity = '0.35';
    },

    // ==================== 刷新 ====================

    Refresh() {
      this.$message.info('正在刷新数据...');
      this.originalImages = [];
      this.defectImages = [];
      this.seenImageIds.clear();
      this.qwenAdvice = null;
      sseManager.close();
      sseManager.init();
    },

    // 清空所有图片（同时清空数据库记录）
    clearImages() {
      this.$confirm('确定要清空所有检测图片吗？数据库记录也将被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 先调用后端清空数据库
        axios.get('api/dashboard/clear')
          .then(res => {
            if (res.data && res.data.code === 200) {
              // 清空前端数据
              this.originalImages = [];
              this.defectImages = [];
              this.seenImageIds.clear();
              this.qwenAdvice = null;
              this.statsData = [{
                runTime: '-',
                defectionsSum: 0,
                defectRate: '0.00%',
                highestOccurrenceDefect: '暂无',
                aiSuggestion: '-',
                aiAnalysis: '-',
                confidence: '-',
                qualityVerdict: '-'
              }];
              this.$message.success(res.data.data || '已清空所有图片');
            } else {
              this.$message.error(res.data.msg || '清空失败');
            }
          })
          .catch(err => {
            this.$message.error('清空失败: ' + err.message);
          });
      }).catch(() => {
        // 用户取消
      });
    },

    // ==================== 预览弹窗 ====================

    handlePreview(item, type, index) {
      this.previewItem = { ...item };
      this.previewType = type;
      this.previewIndex = index;
      this.previewTitle = type === 'original'
        ? `待检工件原图 #${String(index + 1).padStart(2, '0')}`
        : `已检缺陷工件切片 #${String(index + 1).padStart(2, '0')}`;
      this.previewVisible = true;

      // 仅对数据库记录（数字 ID）发起详情请求
      const isDbRecord = item && item.id && !String(item.id).includes('_');
      if (type === 'defect' && isDbRecord) {
        this.previewLoading = true;
        axios.get(`api/detectInfo/info/details?id=${item.id}`)
          .then(res => {
            this.previewLoading = false;
            if (res.data && res.data.code === 200 && res.data.data) {
              const detail = res.data.data;
              this.$set(this.previewItem, 'defections', detail.defections || []);
              if (detail.imgBase64) {
                this.$set(this.previewItem, 'imgBase64', detail.imgBase64);
              }
            }
          })
          .catch(() => {
            this.previewLoading = false;
          });
      }
    },

    // ==================== 专家报告 ====================

    handleOpenExpertReport(row) {
      this.expertReportVisible = true;
      this.expertReportLoading = true;
      this.currentExpertReport = null;
      this.expertAdvice = {};
      this.currentSliceIndex = 0;
      this.batchData = null;

      // 从后端获取最新批次检测数据
      axios.get('api/detection/batch/latest')
        .then(res => {
          this.expertReportLoading = false;
          if (res.data && res.data.code === 200 && res.data.data) {
            const data = res.data.data;
            this.batchData = data;

            // 构建 currentExpertReport 基本信息
            this.currentExpertReport = {
              id: data.batchId || '-',
              time: data.timestamp || '-',
              defectionsSum: (data.defect && data.defect.scratchCount) || 0,
              imgBase64: null,
              defections: []
            };

            // 构建切片图片列表
            this.sliceImagesList = [];
            if (data.images && data.images.length > 0) {
              data.images.forEach(img => {
                if (img.defectImages && img.defectImages.length > 0) {
                  img.defectImages.forEach(di => {
                    if (di.imageBase64) {
                      this.sliceImagesList.push(di.imageBase64);
                    }
                  });
                }
              });
              // 设置第一张缺陷图
              if (this.sliceImagesList.length > 0) {
                this.currentExpertReport.imgBase64 = this.sliceImagesList[0];
              }
            }

            // 构建专家建议（从 qwen 数据）
            if (data.qwen) {
              this.expertAdvice = {
                '总体缺陷情况': data.qwen.report || '暂无AI研判报告',
                '最严重等级': data.qwen.severity || '未知',
                '综合分析依据': data.qwen.report || '暂无综合分析',
                '最终处置建议': data.finalResult && data.finalResult.disposalAdvice
                  ? data.finalResult.disposalAdvice
                  : (data.qwen.disposalAdvice || '建议人工复检')
              };
            }

            // 构建表格明细
            this.expertTableList = [];
            if (data.images && data.images.length > 0) {
              data.images.forEach((img, idx) => {
                this.expertTableList.push({
                  imageName: img.filename || `图片-${idx + 1}`,
                  category: this.getDefectCategory(data.defect),
                  defectCount: img.scratchCount || 0,
                  status: img.status || 'NG',
                  repairSuggestion: data.qwen ? (data.qwen.disposalAdvice || '建议人工复检') : '建议人工复检'
                });
              });
            }
          } else {
            this.$message.warning('暂无最新检测数据');
            this.expertReportLoading = false;
          }
        })
        .catch(err => {
          this.expertReportLoading = false;
          this.$message.error('获取检测数据失败: ' + (err.message || '网络错误'));
        });
    },

    getDefectCategory(defect) {
      if (!defect || !defect.defectTypes) return '划痕';
      const types = Object.keys(defect.defectTypes);
      return types.length > 0 ? types[0] : '划痕';
    },

    resolveAdvice(qwenAdviceStr, defections) {
      if (qwenAdviceStr) {
        try {
          return typeof qwenAdviceStr === 'string' ? JSON.parse(qwenAdviceStr) : qwenAdviceStr;
        } catch (e) { /* fall through */ }
      }
      if (this.qwenAdvice) return this.qwenAdvice;
      return this.generateAdvice(defections);
    },

    fallbackExpertReport(row) {
      const defs = (row && row.defections) || [];
      const img = (row && row.imgBase64) || null;
      if (img && !this.sliceImagesList.includes(img)) {
        this.sliceImagesList.unshift(img);
      }
      this.currentExpertReport = {
        id: (row && row.id) || '202609',
        time: (row && (row.opTime || row.time)) || '-',
        defectionsSum: (row && row.defectionsSum) || defs.length || 0,
        imgBase64: img,
        defections: defs
      };
      this.expertAdvice = this.resolveAdvice(null, defs);
    },

    generateAdvice(defections) {
      if (!defections || defections.length === 0) {
        return {
          '总体缺陷情况': '工件表面完好，未检出明显结构性缺陷与擦伤。',
          '最严重等级': '合格',
          '综合分析依据': '视觉对比度均匀，无局部聚集性缺陷，缺陷面积占比0%。',
          '最终处置建议': '合格放行，可直接流入下一道工序'
        };
      }

      const typeSet = new Set();
      defections.forEach(d => {
        const cat = (d.category || '').toLowerCase();
        typeSet.add(CATEGORY_MAP[cat] || d.category || '表面缺陷');
      });
      const typesStr = Array.from(typeSet).join('、');

      let totalArea = 0;
      defections.forEach(d => { if (d.l && d.h) totalArea += d.l * d.h; });
      const areaRatio = Math.min(100, (totalArea / 40000) * 100).toFixed(1);

      const maxLvl = Math.max(...defections.map(d => d.severityLevel || 1));
      const levelText = maxLvl >= 5 ? '极高风险' : maxLvl >= 4 ? '严重' : maxLvl >= 3 ? '中度' : '轻度微瑕';

      let action = '建议现场人工排查测量';
      if (typesStr.includes('裂纹')) action = '高风险结构缺陷，严禁流入下道工序，建议直接报废或送探伤室复核';
      else if (typesStr.includes('斑块') || typesStr.includes('氧化皮')) action = '建议进行表面酸洗/抛光处置，消除表面斑块附着后复检';
      else if (typesStr.includes('划痕') || typesStr.includes('擦伤')) action = '建议使用精细砂纸进行局部抛光打磨，测量深度合格后放行';
      else if (typesStr.includes('夹杂')) action = '建议进行超声波深层探伤，排查基体内部是否存在夹杂扩展';

      return {
        '总体缺陷情况': `工件表面累计检出 ${defections.length} 处【${typesStr}】缺陷，需现场复核。`,
        '最严重等级': levelText,
        '综合分析依据': `缺陷呈局部聚集分布，累计面积占比约 ${areaRatio}%，最高严重程度评定为 ${maxLvl} 级。`,
        '最终处置建议': action
      };
    },

    prevSlice() {
      if (this.currentSliceIndex > 0) this.currentSliceIndex--;
    },

    nextSlice() {
      if (this.currentSliceIndex < this.sliceImagesList.length - 1) this.currentSliceIndex++;
    },

    // ==================== 打印 / 导出 PDF ====================

    printExpertReport() {
      const dom = document.getElementById('expert-report-printable');
      if (!dom) { this.$message.error('未找到可打印的报告内容'); return; }

      let old = document.getElementById('expert-report-print-iframe');
      if (old) document.body.removeChild(old);

      const iframe = document.createElement('iframe');
      iframe.id = 'expert-report-print-iframe';
      Object.assign(iframe.style, {
        position: 'fixed', right: '0', bottom: '0',
        width: '0', height: '0', border: 'none'
      });
      document.body.appendChild(iframe);

      const doc = iframe.contentWindow.document;
      doc.open();
      doc.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>AI工业表面缺陷智控专家分析报告</title>
        <style>
          @page { size: A4 portrait; margin: 10mm 12mm; }
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; color: #1f2937; background: #fff; -webkit-print-color-adjust: exact; print-color-adjust: exact; padding: 5px; }
          .no-print { display: none !important; }
          .report-header { border-bottom: 2px solid #2563eb; padding-bottom: 12px; margin-bottom: 14px; }
          .brand-badge { display: inline-block; background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 4px; margin-bottom: 4px; }
          .report-title { font-size: 18px; color: #111827; font-weight: 700; margin: 3px 0 8px 0; }
          .report-meta { display: flex; gap: 16px; font-size: 11px; color: #4b5563; }
          .report-meta span { background: #f8fafc; padding: 4px 12px; border-radius: 6px; border: 1px solid #e2e8f0; display: inline-flex; align-items: center; gap: 4px; }
          .report-meta strong { color: #1e293b; font-weight: 600; }
          .report-kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 14px; }
          .kpi-card { border: 1px solid #e5e7eb; border-radius: 6px; padding: 8px 10px; border-left: 4px solid #9ca3af; background: #f9fafb; }
          .kpi-card.danger { border-left-color: #ef4444; background: #fef2f2; }
          .kpi-card.warning { border-left-color: #f59e0b; background: #fffbeb; }
          .kpi-card.primary { border-left-color: #3b82f6; background: #eff6ff; }
          .kpi-card.success { border-left-color: #10b981; background: #ecfdf5; }
          .kpi-label { font-size: 11px; color: #6b7280; }
          .kpi-val { font-size: 16px; font-weight: 700; color: #111827; margin: 3px 0; }
          .kpi-val.decision { font-size: 13px; color: #b91c1c; }
          .kpi-sub { font-size: 10px; color: #9ca3af; }
          .report-split-section { display: grid; grid-template-columns: 1fr 1.3fr; gap: 12px; margin-bottom: 14px; }
          .section-title { font-size: 12px; font-weight: 700; color: #1f2937; margin-bottom: 6px; }
          .report-image-box { background: #000; border-radius: 6px; height: 200px; display: flex; align-items: center; justify-content: center; overflow: hidden; position: relative; }
          .report-image { max-width: 100%; max-height: 100%; object-fit: contain; }
          .image-watermark { position: absolute; bottom: 4px; right: 6px; background: rgba(0,0,0,0.7); color: #fff; font-size: 9px; padding: 1px 4px; border-radius: 2px; }
          .advice-block-card { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 10px 12px; height: 178px; display: flex; flex-direction: column; gap: 8px; }
          .advice-item { border-bottom: 1px dashed #e5e7eb; padding-bottom: 6px; }
          .advice-item:last-child { border-bottom: none; }
          .item-title { font-size: 11px; font-weight: 700; color: #374151; margin-bottom: 2px; }
          .icon-tag { display: inline-block; width: 14px; height: 14px; line-height: 14px; text-align: center; border-radius: 50%; font-size: 9px; color: #fff; margin-right: 4px; }
          .tag-info { background: #3b82f6; } .tag-warning { background: #f59e0b; } .tag-danger { background: #ef4444; }
          .item-content { font-size: 11px; color: #4b5563; line-height: 1.4; padding-left: 18px; }
          .item-content.bold-action { color: #dc2626; font-weight: bold; background: #fee2e2; padding: 3px 6px; border-radius: 3px; }
          .report-table-section { margin-bottom: 14px; }
          table { width: 100%; border-collapse: collapse; font-size: 11px; }
          th, td { border: 1px solid #d1d5db; padding: 6px 8px; text-align: center; }
          th { background: #f3f4f6; font-weight: 600; color: #374151; }
          .report-footer { border-top: 1px solid #e5e7eb; padding-top: 10px; display: flex; justify-content: space-between; font-size: 11px; color: #4b5563; }
          .footer-sign { display: flex; gap: 30px; }
          .footer-note { font-size: 9px; color: #9ca3af; }
        </style>
      </head><body>${dom.innerHTML}</body></html>`);
      doc.close();

      setTimeout(() => {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
      }, 250);
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

/* 监控图像样式 */
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

/* 表格容器 */
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

/* 专家报告弹窗 */
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

.report-meta .meta-highlight-tag {
  background: #ecfdf5;
  color: #059669;
  border-color: #a7f3d0;
}

.report-meta .meta-highlight-tag strong {
  color: #047857;
}

/* 切片翻页操作条 */
.slice-pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.slice-page-indicator {
  font-size: 11.5px;
  color: #475569;
  font-weight: 600;
  font-family: monospace;
}

.slice-page-actions {
  display: flex;
  gap: 6px;
}

.defect-type-pill {
  font-size: 11px !important;
  border-radius: 12px !important;
  padding: 0 8px !important;
  height: 22px !important;
  line-height: 20px !important;
}

.status-ng-badge {
  display: inline-block;
  color: #dc2626;
  font-weight: 800;
  font-size: 12px;
  background: #fef2f2;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid #fecaca;
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

.kpi-card.danger { border-top: 3px solid #ef4444; }
.kpi-card.danger .kpi-icon-wrap { background: #fef2f2; color: #ef4444; }
.kpi-card.danger .kpi-val { color: #dc2626; }

.kpi-card.warning { border-top: 3px solid #f59e0b; }
.kpi-card.warning .kpi-icon-wrap { background: #fffbeb; color: #d97706; }
.kpi-card.warning .kpi-val.highlight { color: #b45309; font-size: 21px; }

.kpi-card.primary { border-top: 3px solid #2563eb; }
.kpi-card.primary .kpi-icon-wrap { background: #eff6ff; color: #2563eb; }
.kpi-card.primary .kpi-val { color: #1d4ed8; }

.kpi-card.success { border-top: 3px solid #059669; }
.kpi-card.success .kpi-icon-wrap { background: #ecfdf5; color: #059669; }
.kpi-card.success .kpi-val.decision { font-size: 14.5px; color: #991b1b; font-weight: 700; line-height: 1.45; }

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

/* 工业图谱网格与卡片样式 */
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

/* 4行7列网格 */
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

.grid-img-cell.border-danger { border-color: #fca5a5; }
.grid-img-cell.border-warn { border-color: #fde68a; }
.grid-img-cell.border-pass { border-color: #bbf7d0; }

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

/* 工件切片弹窗检视 */
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

/* 打印与导出 PDF */
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

<template>
  <div class="smart-factory">
    <!-- 背景元素：浅色网格 -->
    <div class="bg-grid"></div>

    <header class="factory-header">
      <div class="header-content">
        <h1 class="main-title">
          {{ getTitle() }}
          <span v-if="timeRange === 'today'" class="subtitle-icon"><i class="el-icon-info"></i></span>
        </h1>
        <div class="header-controls">
          <el-button v-if="timeRange === 'labeling'" icon="el-icon-refresh" size="mini" class="refresh-btn" @click="refreshLabelingData()"></el-button>
          <el-select v-model="timeRange" size="mini" class="time-select" @change="handleViewChange">
            <el-option label="标注系统" value="labeling"></el-option>
            <el-option label="模型指标" value="today"></el-option>    <!--需要提前删除的代码-->
          </el-select>
          <div class="system-status"><i class="el-icon-check-circle"></i> 系统正常运行中</div>
        </div>
      </div>
    </header>

    <!-- ========================================================== -->
    <!-- 视图1：标注系统视图 (1:1 像素级精准复刻工控 3×2 对称矩阵) -->
    <!-- ========================================================== -->
    <transition name="fade">
      <main class="factory-layout labeling-command-center" v-if="timeRange === 'labeling'">
        <!-- 1. 顶部 4 个独立指标卡 (Top KPI Cards - 1:1 像素级复刻设计图一) -->
        <section class="labeling-kpi-grid">
          <!-- 卡片 1: 标注样本总量 (1:1 像素级对齐) -->
          <div class="kpi-metric-card">
            <div class="kpi-icon-box">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3.5" y="3.5" width="17" height="17" rx="3.5" stroke-width="1.8"/>
                <line x1="3.5" y1="12" x2="20.5" y2="12" stroke-width="1.6"/>
                <line x1="12" y1="3.5" x2="12" y2="20.5" stroke-width="1.6"/>
                <rect x="6.5" y="6.5" width="2.5" height="2.5" rx="0.5" fill="#2563eb"/>
                <rect x="15" y="6.5" width="2.5" height="2.5" rx="0.5" fill="#2563eb"/>
                <rect x="6.5" y="15" width="2.5" height="2.5" rx="0.5" fill="#2563eb"/>
                <rect x="15" y="15" width="2.5" height="2.5" rx="0.5" fill="#2563eb"/>
              </svg>
            </div>
            <div class="kpi-text-block">
              <div class="kpi-label">标注样本总量</div>
              <div class="kpi-value text-dark font-mono">6,493</div>
            </div>
          </div>

          <!-- 卡片 2: 今日流水线标注吞吐 (1:1 像素级对齐 - 漏斗图标与双行流速) -->
          <div class="kpi-metric-card">
            <div class="kpi-icon-box">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10 4h4l3.2 6.8h-10.4z" stroke-width="1.8" stroke-linejoin="round"/>
                <rect x="4.5" y="13.5" width="6.5" height="6.5" rx="1.5" stroke-width="1.8"/>
                <rect x="13" y="13.5" width="6.5" height="6.5" rx="1.5" stroke-width="1.8"/>
              </svg>
            </div>
            <div class="kpi-text-block">
              <div class="kpi-label">今日流水线标注吞吐</div>
              <div class="throughput-val-row">
                <span class="kpi-value text-blue font-mono">4,320</span>
                <span class="flow-arrow-sep">›</span>
                <div class="flow-stats-col font-mono">
                  <span class="flow-speed-line">35 条/s</span>
                  <span class="flow-desc-line">实时流速</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 卡片 3: 人机标注一致性 (1:1 像素级对齐 - 质检抽屉图标与药丸达标标签) -->
          <div class="kpi-metric-card">
            <div class="kpi-icon-box">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 14l2 6h12l2-6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3.5 14h5.5l1.5 2h3l1.5-2h5.5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <rect x="6" y="4" width="10" height="7.5" rx="1.2" transform="rotate(-6 6 4)" stroke-width="1.8"/>
                <path d="M8.5 7.8l1.2 1.2 2.6-2.6" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="kpi-text-block">
              <div class="kpi-label">人机标注一致性</div>
              <div class="consistency-val-row">
                <span class="kpi-value text-dark font-mono">99.2%</span>
                <span class="badge-tag-pass">达标</span>
              </div>
              <div class="kpi-compliance-note">达标 GB/T 认证</div>
            </div>
          </div>

          <!-- 卡片 4: 资源概览 Blade Rack Cluster (加宽双列工控比例) -->
          <div class="kpi-metric-card kpi-rack-card">
            <div class="rack-card-head">
              <div class="rack-label-group">
                <span class="rack-main-title">资源概览</span>
                <span class="rack-cluster-sub">Blade Rack Cluster</span>
                <i class="el-icon-info rack-info-icon"></i>
              </div>
              <div class="rack-status-text font-mono">
                <span class="green-dot"></span> 4 nodes online
              </div>
            </div>
            <div class="rack-meters-row font-mono">
              <div class="rack-meter-unit">
                <div class="m-head">
                  <span class="m-k">CPU利用率</span>
                  <span class="m-v">{{ Math.floor(deviceMetrics.cpuUsage) }}%</span>
                </div>
                <div class="m-bar"><div class="m-fill fill-cpu" :style="{ width: Math.floor(deviceMetrics.cpuUsage) + '%' }"></div></div>
              </div>
              <div class="rack-meter-unit">
                <div class="m-head">
                  <span class="m-k">GPU利用率</span>
                  <span class="m-v">{{ Math.floor(deviceMetrics.gpuUsage) }}%</span>
                </div>
                <div class="m-bar"><div class="m-fill fill-gpu" :style="{ width: Math.floor(deviceMetrics.gpuUsage) + '%' }"></div></div>
              </div>
              <div class="rack-meter-unit">
                <div class="m-head">
                  <span class="m-k">内存使用率</span>
                  <span class="m-v">{{ Math.floor(deviceMetrics.memoryUsage) }}%</span>
                </div>
                <div class="m-bar"><div class="m-fill fill-ram" :style="{ width: Math.floor(deviceMetrics.memoryUsage) + '%' }"></div></div>
              </div>
              <div class="rack-meter-unit">
                <div class="m-head">
                  <span class="m-k">磁盘使用率</span>
                  <span class="m-v">{{ Math.floor(deviceMetrics.diskUsage) }}%</span>
                </div>
                <div class="m-bar"><div class="m-fill fill-nvme" :style="{ width: Math.floor(deviceMetrics.diskUsage) + '%' }"></div></div>
              </div>
            </div>
          </div>
        </section>

        <!-- 2. 主体 3×2 标准工控对称矩阵卡片 (3 Equal Columns Grid) -->
        <div class="labeling-symmetry-grid">
          <!-- 卡片 1: 样本库全息资产沉淀 (Col 1, Row 1) -->
          <section class="symmetry-card">
            <div class="card-top-head">
              <div class="title-with-info">
                <span class="card-title-txt">样本库全息资产沉淀</span>
                <i class="el-icon-info info-tip-icon"></i>
              </div>
              <div class="card-time-tabs font-mono">
                <button v-for="type in ['day', 'week', 'month']" :key="type" :class="{ active: labelingWorkshopData[0].activeTimeRange === type }" @click="toggleLabelingTime(type, 0)">{{ type === 'day' ? '日' : type === 'week' ? '周' : '月' }}</button>
              </div>
            </div>

            <!-- 图表容器 -->
            <div class="card-chart-wrap">
              <div ref="lbl_workshop1Chart" class="chart-canvas"></div>
            </div>

            <!-- 底部三槽参数栏 -->
            <div class="card-bottom-dock font-mono">
              <div class="dock-slot-item">
                <span class="k">入库总量</span>
                <strong class="v text-dark">{{ labelingWorkshopData[0].stats.total.toLocaleString() }}</strong>
              </div>
              <div class="dock-divider"></div>
              <div class="dock-slot-item">
                <span class="k">已完成进度</span>
                <strong class="v text-primary">83.3%</strong>
              </div>
              <div class="dock-divider"></div>
              <div class="dock-slot-item">
                <span class="k">待处理</span>
                <strong class="v text-warning">1,085</strong>
              </div>
            </div>
          </section>

          <!-- 卡片 2: 标注样本库物理存储趋势全息示意图 (Col 2, Row 1 - 1:1 像素级复刻) -->
          <section class="symmetry-card">
            <div class="card-top-head">
              <div class="title-with-info">
                <span class="card-title-txt">标注样本库物理存储趋势全息示意图</span>
                <i class="el-icon-info info-tip-icon"></i>
              </div>
              <div class="card-live-badge font-mono">
                <span class="live-indicator-dot"></span>
                <span class="live-text">LIVE</span>
                <span class="live-time-box">{{ labelingUpdateTime || '10:45:32' }}</span>
              </div>
            </div>

            <!-- 示波器网格标尺说明 -->
            <div class="scope-scale-line font-mono">
              <span>SCALE: 100MB/DIV</span>
              <span>TIMEBASE: 30S/SWEEP</span>
              <span>STATUS: BUFFER HEALTHY</span>
            </div>

            <!-- 渐变多层面积堆叠图 -->
            <div class="card-chart-wrap scope-chart-wrap">
              <div ref="lbl_realtimeChart" class="chart-canvas"></div>
            </div>

            <!-- 底部三槽参数栏 -->
            <div class="card-bottom-dock font-mono">
              <div class="dock-slot-item">
                <span class="k">今日净增样本</span>
                <strong class="v text-dark">180 MB</strong>
              </div>
              <div class="dock-divider"></div>
              <div class="dock-slot-item">
                <span class="k">写入缓冲区</span>
                <strong class="v text-dark">0.42 GB/s</strong>
              </div>
              <div class="dock-divider"></div>
              <div class="dock-slot-item">
                <span class="k">I/O 调度策略</span>
                <strong class="v text-dark">FIFO ZERO-DROP</strong>
              </div>
            </div>
          </section>

          <!-- 卡片 3: AI 自动标注推理效能 (Col 3, Row 1) -->
          <section class="symmetry-card">
            <div class="card-top-head">
              <div class="title-with-info">
                <span class="card-title-txt">AI 自动标注推理效能</span>
                <i class="el-icon-info info-tip-icon"></i>
              </div>
              <div class="card-time-tabs font-mono">
                <button v-for="type in ['day', 'week', 'month']" :key="type" :class="{ active: labelingWorkshopData[2].activeTimeRange === type }" @click="toggleLabelingTime(type, 2)">{{ type === 'day' ? '日' : type === 'week' ? '周' : '月' }}</button>
              </div>
            </div>

            <!-- 双 Y 轴柱线图 -->
            <div class="card-chart-wrap">
              <div ref="lbl_workshop3Chart" class="chart-canvas"></div>
            </div>

            <!-- 底部三槽参数栏 (1:1 像素级复刻截图：上下结构、垂直细线分割、纯蓝纯红高亮) -->
            <div class="ai-stats-triple-dock font-mono">
              <div class="triple-dock-col">
                <span class="dock-label">AI 标注总量</span>
                <strong class="dock-val text-dark">3,800</strong>
              </div>
              <div class="triple-dock-divider"></div>
              <div class="triple-dock-col">
                <span class="dock-label">划痕检出量</span>
                <strong class="dock-val text-primary-bright">2,188</strong>
              </div>
              <div class="triple-dock-divider"></div>
              <div class="triple-dock-col">
                <span class="dock-label">裂纹检出量</span>
                <strong class="dock-val text-danger-bright">677</strong>
              </div>
            </div>
          </section>

          <!-- 卡片 4: 人工标记吞吐与流速 (Col 1, Row 2) -->
          <section class="symmetry-card">
            <div class="card-top-head">
              <div class="title-with-info">
                <span class="card-title-txt">人工标记吞吐与流速</span>
                <i class="el-icon-info info-tip-icon"></i>
              </div>
              <div class="card-time-tabs font-mono">
                <button v-for="type in ['day', 'week', 'month']" :key="type" :class="{ active: labelingWorkshopData[1].activeTimeRange === type }" @click="toggleLabelingTime(type, 1)">{{ type === 'day' ? '日' : type === 'week' ? '周' : '月' }}</button>
              </div>
            </div>

            <div class="card-chart-wrap">
              <div ref="lbl_workshop2Chart" class="chart-canvas"></div>
            </div>

            <div class="card-bottom-dock font-mono">
              <div class="dock-slot-item">
                <span class="k">标记准确率</span>
                <strong class="v text-dark">99.2%</strong>
              </div>
              <div class="dock-slot-item">
                <span class="k">今日标记</span>
                <strong class="v text-dark">4,320</strong>
              </div>
              <div class="dock-slot-item">
                <span class="k">实时流速</span>
                <strong class="v text-primary">35 条/s</strong>
              </div>
            </div>
          </section>

          <!-- 卡片 5: 多维标注质量雷达矩阵 (Col 2, Row 2 - 1:1 像素级复刻) -->
          <section class="symmetry-card">
            <div class="card-top-head">
              <div class="title-with-info">
                <span class="card-title-txt">多维标注质量雷达矩阵</span>
                <i class="el-icon-info info-tip-icon"></i>
              </div>
            </div>

            <div class="card-chart-wrap">
              <div ref="lbl_workshop4Chart" class="chart-container"></div>
            </div>

            <div class="card-bottom-dock font-mono">
              <div class="dock-slot-item">
                <span class="k">AI 标记</span>
                <strong class="v text-dark">2,693</strong>
              </div>
              <div class="dock-slot-item">
                <span class="k">人机一致率</span>
                <strong class="v text-success font-bold">93%</strong>
              </div>
              <div class="dock-slot-item">
                <span class="k">数据覆盖率</span>
                <strong class="v text-primary font-bold">48%</strong>
              </div>
            </div>
          </section>

          <!-- 卡片 6: 系统状态与快捷操作 (Col 3, Row 2 - 1:1 像素级复刻) -->
          <section class="symmetry-card system-action-combo-card">
            <!-- 上部：系统与环境状态 -->
            <div class="status-panel-upper">
              <div class="status-meta-row">
                <div class="meta-label-side">
                  <span class="meta-icon-svg">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                      <path d="m9 12 2 2 4-4"/>
                    </svg>
                  </span>
                  <span>系统状态</span>
                </div>
                <div class="meta-val-side">
                  <span class="badge-status-green">系统正常运行中</span>
                </div>
              </div>

              <div class="status-meta-row">
                <div class="meta-label-side">
                  <span class="meta-icon-svg">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                  </span>
                  <span>环境</span>
                </div>
                <div class="meta-val-side">
                  <span class="badge-status-green">生产环境</span>
                </div>
              </div>

              <div class="status-meta-row font-mono">
                <div class="meta-label-side">
                  <span class="meta-icon-svg">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="5" r="2.5"/>
                      <circle cx="6" cy="19" r="2.5"/>
                      <circle cx="18" cy="19" r="2.5"/>
                      <path d="M12 7.5V13m0 0H6v3.5m6-3.5h6v3.5"/>
                    </svg>
                  </span>
                  <span>在线节点</span>
                </div>
                <div class="meta-val-side">
                  <span class="badge-status-green font-mono">4 / 4</span>
                </div>
              </div>

              <div class="status-meta-row font-mono">
                <div class="meta-label-side">
                  <span class="meta-icon-svg">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="9"/>
                      <polyline points="12 6 12 12 16 14"/>
                    </svg>
                  </span>
                  <span>数据更新时间</span>
                </div>
                <div class="meta-val-side">
                  <span class="meta-val-text font-mono">{{ dynamicFullTime }}</span>
                </div>
              </div>

              <div class="status-meta-row font-mono">
                <div class="meta-label-side">
                  <span class="meta-icon-svg">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                      <circle cx="12" cy="12" r="2"/>
                    </svg>
                  </span>
                  <span>版本</span>
                </div>
                <div class="meta-val-side">
                  <span class="meta-val-text text-muted font-mono">v2.3.7</span>
                </div>
              </div>
            </div>

            <!-- 下部：快捷操作 -->
            <div class="action-panel-lower">
              <div class="action-panel-heading">快捷操作</div>
              <div class="quick-buttons-row">
                <button class="quick-action-btn" @click="$message.info('新建标注任务')">
                  <div class="quick-btn-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="3" y="3" width="18" height="18" rx="3" stroke-width="1.8"/>
                      <line x1="9" y1="8" x2="16" y2="8" stroke-width="2"/>
                      <line x1="9" y1="12" x2="16" y2="12" stroke-width="2"/>
                      <line x1="9" y1="16" x2="16" y2="16" stroke-width="2"/>
                      <circle cx="6.5" cy="8" r="0.75" fill="#2563eb" stroke="none"/>
                      <circle cx="6.5" cy="12" r="0.75" fill="#2563eb" stroke="none"/>
                      <circle cx="6.5" cy="16" r="0.75" fill="#2563eb" stroke="none"/>
                    </svg>
                  </div>
                  <span class="action-text">新建任务</span>
                </button>

                <button class="quick-action-btn" @click="$message.info('导入标注样本')">
                  <div class="quick-btn-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M4 8V5a1 1 0 0 1 1-1h3"/>
                      <path d="M16 4h3a1 1 0 0 1 1 1v3"/>
                      <path d="M4 16v3a1 1 0 0 0 1 1h3"/>
                      <path d="M16 20h3a1 1 0 0 0 1-1v-3"/>
                      <path d="M12 16V8" stroke-width="2.2"/>
                      <path d="M8.5 11.5L12 8l3.5 3.5" stroke-width="2.2"/>
                    </svg>
                  </div>
                  <span class="action-text">导入样本</span>
                </button>

                <button class="quick-action-btn" @click="$message.info('启动推理服务')">
                  <div class="quick-btn-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#8b5cf6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 2.5L20.5 7.4V16.6L12 21.5L3.5 16.6V7.4L12 2.5Z" stroke-width="1.8"/>
                      <path d="M12 2.5V21.5" stroke-width="1.8"/>
                      <path d="M20.5 7.4L12 12L3.5 7.4" stroke-width="1.8"/>
                    </svg>
                  </div>
                  <span class="action-text">推理服务</span>
                </button>

                <button class="quick-action-btn" @click="$message.info('打开系统设置')">
                  <div class="quick-btn-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#334155" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"/>
                      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z"/>
                    </svg>
                  </div>
                  <span class="action-text">系统设置</span>
                </button>
              </div>
            </div>
          </section>
        </div>
      </main>
    </transition>

    <!-- ========================================================== -->
    <!-- 视图2：模型指标视图 -->
    <!-- ========================================================== -->
    <transition name="fade">
      <main class="analytic-overview-wrap" v-if="timeRange === 'today'">
        <!-- 顶部四色卡片 -->
        <div class="overview-cards">
          <div class="stat-card pink-card">
            <div class="card-text">
              <div class="card-label">F1 分数</div>
              <div class="card-value">{{ currentF1Value.toFixed(1) }}%</div>
              <div class="card-sub">+2% 较上周</div>
            </div>
            <div class="card-chart spark-f1-chart" ref="sparkF1"></div>
          </div>
          <div class="stat-card purple-card">
            <div class="card-text">
              <div class="card-label">误报率</div>
              <div class="card-value">{{ modelMetrics.falseAlarm.scratch }}%</div>
              <div class="card-sub">+5% 较上周</div>
            </div>
            <div class="card-chart" ref="sparkAlarm"></div>
          </div>
          <div class="stat-card green-card">
            <div class="card-text">
              <div class="card-label">样本总数</div>
              <div class="card-value">{{ (totalSamples / 1000).toFixed(2) }}k</div>
              <div class="card-sub">+12% 较上周</div>
            </div>
            <div class="card-chart" ref="sparkSamples"></div>
          </div>
          <div class="stat-card yellow-card">
            <div class="card-text">
              <div class="card-label">处理速度</div>
              <!-- 修改点：显示整数 -->
              <div class="card-value">{{ Math.floor(detectionEfficiencyData.aiDetection[detectionEfficiencyData.aiDetection.length-1]) }}</div>
              <div class="card-sub">条/秒</div>
            </div>
            <div class="card-chart" ref="sparkSpeed"></div>
          </div>
        </div>

        <!-- 中间层：图表区域 -->
        <div class="middle-section">
          <div class="white-panel revenue-panel">
            <div class="panel-header">
              <h2>{{ getPerformanceChartTitle() }} <i class="el-icon-info"></i></h2>
              <div class="legend-group" v-if="performanceChartMode === 'trend'">
                <span class="legend-dot income"></span> 准确率
                <span class="legend-dot expense"></span> 精确率
                <span class="legend-dot outcome"></span> 召回率
              </div>
              <div class="legend-group" v-else-if="performanceChartMode === 'efficiency'">
                <span class="legend-dot income"></span> AI模型
                <span class="legend-dot outcome"></span> 人工检测
              </div>
              <div class="legend-group" v-else-if="performanceChartMode === 'params'">
                <span class="legend-dot expense"></span> 参数规模
              </div>
              <div class="legend-group" v-else-if="performanceChartMode === 'cycle'">
                <span class="legend-dot warning"></span> 训练耗时
              </div>
              <el-dropdown trigger="click" @command="handlePerfChartChange">
                <span class="panel-control el-dropdown-link">
                  {{ getPerformanceDropdownText() }} <i class="el-icon-arrow-down"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="trend">性能趋势 (THIS MONTH)</el-dropdown-item>
                  <el-dropdown-item command="efficiency">检测效率 (EFFICIENCY)</el-dropdown-item>
                  <el-dropdown-item command="params">模型参数 (PARAMS)</el-dropdown-item>
                  <el-dropdown-item command="cycle">训练周期 (CYCLE)</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
            <div class="main-chart-container" ref="chartPerformanceTrend"></div>
          </div>

          <div class="white-panel status-panel">
            <div class="panel-header">
              <h2>样本分布 <i class="el-icon-info"></i></h2>
              <div class="panel-control">TODAY <i class="el-icon-arrow-down"></i></div>
            </div>
            <div class="donut-chart-wrapper">
              <div class="donut-chart" ref="chartSampleDistribution"></div>
              <div class="donut-center-text">
                <div class="percent">{{ modelMetrics.accuracy.normal }}%</div>
                <div class="label">NORMAL</div>
              </div>
            </div>
            <div class="status-stats">
              <div class="stat-item">
                <div class="num">{{ (totalSamples * 0.85).toFixed(0) }}</div>
                <div class="desc">正常</div>
              </div>
              <div class="stat-item">
                <div class="num">{{ (totalSamples * 0.1).toFixed(0) }}</div>
                <div class="desc">划痕</div>
              </div>
              <div class="stat-item">
                <div class="num">{{ (totalSamples * 0.05).toFixed(0) }}</div>
                <div class="desc">裂纹</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部层：数据表格 -->
        <div class="bottom-section">
          <div class="white-panel table-panel">
            <div class="panel-header">
              <h2>
                类别性能详情
                <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="16" x2="12" y2="12"></line>
                  <line x1="12" y1="8" x2="12.01" y2="8"></line>
                </svg>
              </h2>
              <el-dropdown trigger="click" @command="handleCategoryTimeChange">
                <div class="panel-control pill-btn time-dropdown-trigger">
                  <span>{{ categoryTimeLabel }}</span>
                  <svg class="chevron-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
                <el-dropdown-menu slot="dropdown" class="pill-dropdown-menu">
                  <el-dropdown-item command="today" :class="{ 'is-selected': categoryTimeRange === 'today' }">今日</el-dropdown-item>
                  <el-dropdown-item command="week" :class="{ 'is-selected': categoryTimeRange === 'week' }">本周</el-dropdown-item>
                  <el-dropdown-item command="month" :class="{ 'is-selected': categoryTimeRange === 'month' }">本月</el-dropdown-item>
                  <el-dropdown-item command="quarter" :class="{ 'is-selected': categoryTimeRange === 'quarter' }">本季度</el-dropdown-item>
                  <el-dropdown-item command="year" :class="{ 'is-selected': categoryTimeRange === 'year' }">全年</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
            <div class="custom-table">
              <div class="table-header">
                <span>类别</span>
                <span>置信度</span>
                <span>误报率</span>
                <span>F1 分数</span>
                <span>状态</span>
                <span>样本数</span>
              </div>
              <div class="table-row">
                <span class="col-id"><span class="dot-indicator dot-blue"></span> 正常样本</span>
                <span class="col-val">{{ currentCategoryMetrics.normal.confidence }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.normal.falseAlarm }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.normal.f1 }}%</span>
                <span class="col-status-tag" :class="'tag-' + currentCategoryMetrics.normal.statusClass">{{ currentCategoryMetrics.normal.status }}</span>
                <span class="col-val">{{ Number(currentCategoryMetrics.normal.samples).toLocaleString() }}</span>
              </div>
              <div class="table-row">
                <span class="col-id"><span class="dot-indicator dot-purple"></span> 划痕缺陷</span>
                <span class="col-val">{{ currentCategoryMetrics.scratch.confidence }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.scratch.falseAlarm }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.scratch.f1 }}%</span>
                <span class="col-status-tag" :class="'tag-' + currentCategoryMetrics.scratch.statusClass">{{ currentCategoryMetrics.scratch.status }}</span>
                <span class="col-val">{{ Number(currentCategoryMetrics.scratch.samples).toLocaleString() }}</span>
              </div>
              <div class="table-row">
                <span class="col-id"><span class="dot-indicator dot-orange"></span> 裂纹缺陷</span>
                <span class="col-val">{{ currentCategoryMetrics.crack.confidence }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.crack.falseAlarm }}%</span>
                <span class="col-val">{{ currentCategoryMetrics.crack.f1 }}%</span>
                <span class="col-status-tag" :class="'tag-' + currentCategoryMetrics.crack.statusClass">{{ currentCategoryMetrics.crack.status }}</span>
                <span class="col-val">{{ Number(currentCategoryMetrics.crack.samples).toLocaleString() }}</span>
              </div>
            </div>
          </div>

          <div class="white-panel tracking-panel">
            <div class="panel-header">
              <h2>模型参数 (YOLO v10)</h2>
              <div class="panel-control pill-btn">
                最新版本
                <svg class="chevron-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
              </div>
            </div>
            <div class="model-params-list">
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                    <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                    <line x1="12" y1="22.08" x2="12" y2="12"></line>
                  </svg>
                  <span>Architecture</span>
                </div>
                <div class="param-val">YOLOv10-L</div>
              </div>
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="15 3 21 3 21 9"></polyline>
                    <polyline points="9 21 3 21 3 15"></polyline>
                    <line x1="21" y1="3" x2="14" y2="10"></line>
                    <line x1="3" y1="21" x2="10" y2="14"></line>
                  </svg>
                  <span>Input Size</span>
                </div>
                <div class="param-val">640 &times; 640</div>
              </div>
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <circle cx="12" cy="12" r="6"></circle>
                    <circle cx="12" cy="12" r="2"></circle>
                  </svg>
                  <span>Precision (mAP)</span>
                </div>
                <div class="param-val">53.2%</div>
              </div>
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="6" y1="3" x2="6" y2="15"></line>
                    <circle cx="18" cy="6" r="3"></circle>
                    <circle cx="6" cy="18" r="3"></circle>
                    <path d="M18 9a9 9 0 0 1-9 9"></path>
                  </svg>
                  <span>Parameters</span>
                </div>
                <div class="param-val">24.5M</div>
              </div>
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                  </svg>
                  <span>FLOPs</span>
                </div>
                <div class="param-val">126.3G</div>
              </div>
              <div class="param-item">
                <div class="param-title">
                  <svg class="param-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                  <span>Inference Time</span>
                </div>
                <div class="param-val">10 ms</div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </transition>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'SmartWorkshopMonitor',
  data() {
    return {
      timeRange: 'labeling',
      labelingActiveWorkshop: 1,
      labelingUpdateTime: new Date().toLocaleTimeString(),
      // 标注系统数据
      deviceMetrics: { cpuUsage: 65, gpuUsage: 60, memoryUsage: 65, diskUsage: 56, dailyGrowth: 185, cpuTrend: 2.1, gpuTrend: -1.5, memoryTrend: 0.8, diskTrend: 0.5 },
      diskIntervalCount: 0,
      deviceHistory: { time: [], nominal: [], actual: [], lowerBand: [], bandWidth: [], upperLimit: [], base: [], ai: [], scratch: [], crack: [], growth: [], rate: [] },
      labelingWorkshopData: [
        // Workshop 1: Manual Labeling
        { id: 1, name: '人工数据标注指标', isSampleStats: true, activeTimeRange: null, chartMode: 'bar', processes: ['样本入库', '数据标记'], status: 'normal', efficiency: 96.8, stats: { total: 6493, pending: 1085 }, chartData: { realtime: { categories: [], incoming: [], labeled: [], unlabeled: [], normal: [], scratch: [], crack: [] }, day: { categories: [], incoming: [], labeled: [], unlabeled: [], normal: [], scratch: [], crack: [] }, week: { categories: ['周一','周二','周三','周四','周五','周六','周日'], incoming: [3200,3500,3100,3800,4200,2000,1800], labeled: [2800,3100,2900,3400,3900,1800,1600], unlabeled: [400,400,200,400,300,200,200], normal: [], scratch: [], crack: [] }, month: { categories: ['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'], incoming: [16000,17500,16500,18000,17500,11000,9000], labeled: [14500,16000,15500,17000,16500,10500,8500], unlabeled: [1500,1500,1000,1000,1000,500,500], normal: [], scratch: [], crack: [] } }, timeLabels: [], metrics: { speed: '450r/min', temperature: '58°C', precision: '±0.02mm', utilization: 92 } },
        // Workshop 2: Manual Labeling 2
        { id: 2, name: '人工数据标注指标', isSampleStats: true, activeTimeRange: null, chartMode: 'line', processes: ['钻中心孔', '粗抛丸'], status: 'normal', efficiency: 94.2, chartData: { realtime: { categories: [], incoming: [], labeled: [], unlabeled: [], normal: [], scratch: [], crack: [] }, day: { categories: [], incoming: [], labeled: [], unlabeled: [], normal: [], scratch: [], crack: [] }, week: { categories: ['周一','周二','周三','周四','周五','周六','周日'], incoming: [], labeled: [], unlabeled: [], normal: [2200,2500,2300,2800,3200,1500,1350], scratch: [480,420,360,450,500,200,180], crack: [150,180,200,140,160,80,70] }, month: { categories: ['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'], incoming: [], labeled: [], unlabeled: [], normal: [11000,13500,12800,14200,13800,8500,6800], scratch: [2200,2000,2100,2200,2100,1500,1200], crack: [1300,1500,1400,1600,1600,1000,900] } }, timeLabels: [], metrics: { aperture: '12.5mm', speed: '380r/min', pressure: '0.6MPa', coverage: 98 } },
        // Workshop 3: Auto Labeling
        // 初始化 aiTotal 为 3800
        { id: 3, name: '自动标注指标', isAutoLabeling: true, activeTimeRange: null, processes: ['自动推断', '质量预检'], status: 'normal', efficiency: 45.3, aiTotal: 3800, aiScratch: 2150, aiCrack: 980, chartData: { realtime: { categories: [], normal: [], scratch: [], crack: [], efficiency: [] }, day: { categories: [], normal: [], scratch: [], crack: [], efficiency: [] }, week: { categories: ['周一','周二','周三','周四','周五','周六','周日'], normal: [3200,3400,3100,3800,4100,1800,1600], scratch: [480,520,450,550,580,210,190], crack: [220,240,210,260,250,120,100], efficiency: [42,44,43,46,48,38,35] }, month: { categories: ['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'], normal: [16000,17500,16500,18000,17500,11000,9000], scratch: [2400,2600,2500,2700,2800,1500,1300], crack: [1100,1200,1150,1250,1300,600,550], efficiency: [45,46,44,47,46,42,40] } }, timeLabels: [], metrics: { precision: '±0.005mm', temperature: '62°C', status: '运行中' } },
        // Workshop 4: Quality Analysis
        // 初始化 aiMarkedTotal 为 2693 (3800 + 2693 = 6493)
        { id: 4, name: '标注质量效能分析', isQualityStats: true, activeTimeRange: null, processes: ['一致率分析', '覆盖率统计'], status: 'normal', efficiency: 99.2, chartData: { realtime: { categories: [], humanReview: [], aiMarked: [], total: [] }, day: { categories: [], humanReview: [], aiMarked: [], total: [] }, week: { categories: ['周一','周二','周三','周四','周五','周六','周日'], humanReview: [3200, 3500, 3100, 3800, 4200, 2000, 1800], aiMarked: [2800, 3100, 2900, 3400, 3900, 1800, 1600], total: [6000, 6600, 6000, 7200, 8100, 3800, 3400] }, month: { categories: ['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'], humanReview: [16000, 17500, 16500, 18000, 17500, 11000, 9000], aiMarked: [14500, 16000, 15500, 17000, 16500, 10500, 8500], total: [30500, 33500, 32000, 35000, 34000, 21500, 17500] } }, timeLabels: [], metrics: { aiMarkedTotal: 2693, humanMachineConsistency: 21580, markCoverage: 35040 } }
      ],
      labelingCharts: {},
      lblRealtimeChart: null,
      labelingUpdateTimer: null,
      deviceMetricsTimer: null,
      spiralTimer: null,
      spiralDataIndex: 0,
      dynamicFullTime: new Date().toLocaleString(),

      // 模型指标数据
      currentF1Value: 96.2,
      totalSamples: 6420,
      performanceChartMode: 'trend',
      modelMetrics: {
        accuracy: { scratch: 95.3, crack: 93.8, normal: 98.2 },
        confidence: { scratch: 92.1, crack: 90.5, normal: 98.5 },
        precision: { scratch: 92.5, crack: 91.3, normal: 97.6 },
        recall: { scratch: 90.8, crack: 89.5, normal: 96.3 },
        f1: { scratch: 91.6, crack: 90.4, normal: 96.9 },
        falseAlarm: { scratch: 3.2, crack: 4.1, normal: 1.8 },
        sampleDistribution: { normal: 8560, scratch: 1250, crack: 890 }
      },
      detectionEfficiencyData: {
        timeLabels: [],
        aiDetection: [],
        humanDetection: []
      },
      aiChartInstances: {},
      detectionEfficiencyTimer: null,
      sampleUpdateTimer: null,
      aiDataUpdateTimer: null,
      categoryTimeRange: 'week',
      categoryTimeMultipliers: {
        today: 0.18,
        week: 1.0,
        month: 4.3,
        quarter: 12.8,
        year: 51.5
      },
      categoryPeriodMetrics: {
        today: {
          normal: { confidence: '98.8', falseAlarm: '1.5', f1: '97.2', status: '稳定', statusClass: 'stable', samples: 1024 },
          scratch: { confidence: '93.0', falseAlarm: '2.8', f1: '92.4', status: '训练中', statusClass: 'training', samples: 128 },
          crack: { confidence: '91.2', falseAlarm: '3.6', f1: '91.0', status: '待复核', statusClass: 'recheck', samples: 64 }
        },
        week: {
          normal: { confidence: '98.5', falseAlarm: '1.8', f1: '96.9', status: '稳定', statusClass: 'stable', samples: 5462 },
          scratch: { confidence: '92.1', falseAlarm: '3.2', f1: '91.6', status: '训练中', statusClass: 'training', samples: 643 },
          crack: { confidence: '90.5', falseAlarm: '4.1', f1: '90.4', status: '待复核', statusClass: 'recheck', samples: 321 }
        },
        month: {
          normal: { confidence: '98.2', falseAlarm: '2.0', f1: '96.5', status: '稳定', statusClass: 'stable', samples: 23486 },
          scratch: { confidence: '91.8', falseAlarm: '3.5', f1: '91.2', status: '优化中', statusClass: 'training', samples: 2765 },
          crack: { confidence: '89.9', falseAlarm: '4.5', f1: '89.8', status: '待复核', statusClass: 'recheck', samples: 1380 }
        },
        quarter: {
          normal: { confidence: '97.9', falseAlarm: '2.2', f1: '96.1', status: '稳定', statusClass: 'stable', samples: 69910 },
          scratch: { confidence: '91.4', falseAlarm: '3.8', f1: '90.8', status: '迭代中', statusClass: 'training', samples: 8230 },
          crack: { confidence: '89.2', falseAlarm: '4.9', f1: '89.1', status: '待复核', statusClass: 'recheck', samples: 4108 }
        },
        year: {
          normal: { confidence: '97.6', falseAlarm: '2.4', f1: '95.8', status: '稳定', statusClass: 'stable', samples: 281780 },
          scratch: { confidence: '90.9', falseAlarm: '4.2', f1: '90.1', status: '已收敛', statusClass: 'stable', samples: 33151 },
          crack: { confidence: '88.7', falseAlarm: '5.2', f1: '88.5', status: '已校验', statusClass: 'recheck', samples: 16575 }
        }
      }
    };
  },
  computed: {
    categoryTimeLabel() {
      const map = {
        today: '今日',
        week: '本周',
        month: '本月',
        quarter: '本季度',
        year: '全年'
      };
      return map[this.categoryTimeRange] || '本周';
    },
    categoryMultiplier() {
      return (this.categoryTimeMultipliers && this.categoryTimeMultipliers[this.categoryTimeRange]) || 1.0;
    },
    currentCategoryMetrics() {
      if (this.categoryPeriodMetrics && this.categoryPeriodMetrics[this.categoryTimeRange]) {
        return this.categoryPeriodMetrics[this.categoryTimeRange];
      }
      return this.categoryPeriodMetrics ? this.categoryPeriodMetrics.week : {
        normal: { confidence: '98.5', falseAlarm: '1.8', f1: '96.9', status: '稳定', statusClass: 'stable', samples: 5462 },
        scratch: { confidence: '92.1', falseAlarm: '3.2', f1: '91.6', status: '训练中', statusClass: 'training', samples: 643 },
        crack: { confidence: '90.5', falseAlarm: '4.1', f1: '90.4', status: '待复核', statusClass: 'recheck', samples: 321 }
      };
    }
  },
  mounted() {
    this.$nextTick(() => {
      setTimeout(() => {
        this.initLabelingView();
      }, 500); 
    });
    window.addEventListener('resize', this.handleResize);
    this.initLabelingRealtimeStream();
    this.generateLabelingDayData();
    this.initAutoLabelingData();
    this.initDeviceHistoryData();
    this.initDetectionEfficiencyData();
  },
  beforeDestroy() {
    this.stopAllTimers();
    this.disposeAllCharts();
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    getTitle() {
      if (this.timeRange === 'labeling') return '标注系统';
      return 'AI模型识别指标监控';
    },
    handleViewChange(val) {
      this.stopAllTimers();
      this.disposeAllCharts();
      this.$nextTick(() => {
        setTimeout(() => {
          if (val === 'labeling') {
            this.initLabelingView();
          } else {
            this.initTodayView();
          }
        }, 500);
      });
    },
    
    initTodayView() {
      this.renderSparkLines();
      this.renderPerformanceTrendChart();
      this.renderSampleDistributionChart();
      this.startDetectionEfficiencyUpdate();
      this.startSampleUpdate();
      this.startAIDataUpdate();
    },

    handlePerfChartChange(command) {
      this.performanceChartMode = command;
      this.$nextTick(() => {
        this.renderPerformanceTrendChart();
      });
    },

    handleCategoryTimeChange(command) {
      this.categoryTimeRange = command;
    },

    getPerformanceChartTitle() {
      switch(this.performanceChartMode) {
        case 'trend': return '模型综合性能趋势';
        case 'efficiency': return '检测效率对比';
        case 'params': return '模型参数规模';
        case 'cycle': return '模型训练周期';
        default: return '数据图表';
      }
    },

    getPerformanceDropdownText() {
      switch(this.performanceChartMode) {
        case 'trend': return '性能趋势 (THIS MONTH)';
        case 'efficiency': return '检测效率 (EFFICIENCY)';
        case 'params': return '模型参数 (PARAMS)';
        case 'cycle': return '训练周期 (CYCLE)';
        default: return '选择图表';
      }
    },

    renderSparkLines() {
      const createSpark = (refName, color) => {
        if (!this.$refs[refName]) return;
        const chart = echarts.init(this.$refs[refName]);
        this.aiChartInstances[refName] = chart;

        // F1使用与currentF1Value同量级的平缓微变数据
        let data;
        if (refName === 'sparkF1') {
          let val = this.currentF1Value || 96.2;
          data = Array.from({length: 10}, () => {
            val += (Math.random() - 0.5) * 1.6;
            val = Math.max(93, Math.min(97.5, val));
            return Number(val.toFixed(1));
          });
        } else if (refName === 'sparkAlarm') {
          // 误报率数值低（3.2%左右），线条位于卡片偏下方，带有轻微起伏
          let val = Number(this.modelMetrics.falseAlarm.scratch) || 3.2;
          data = Array.from({length: 10}, () => {
            val += (Math.random() - 0.5) * 0.4;
            val = Math.max(1.8, Math.min(4.5, val));
            return Number(val.toFixed(1));
          });
        } else {
          const base = 35;
          let current = base;
          data = Array.from({length: 10}, () => {
            current += (Math.random() - 0.5) * 2;
            return Number(current.toFixed(1));
          });
        }

        let yMin = 0;
        let yMax = undefined;
        if (refName === 'sparkF1') {
          yMin = 88;
          yMax = 99;
        } else if (refName === 'sparkAlarm') {
          yMin = 0;
          yMax = 12; // 误报率在3%左右，上限设为12，让线条自然处于偏下区域
        }

        chart.setOption({
          grid: { top: 8, bottom: 0, left: 0, right: 0 },
          xAxis: { type: 'category', show: false },
          yAxis: { type: 'value', show: false, min: yMin, max: yMax },
          series: [{
            type: 'line',
            data: data,
            smooth: true,
            showSymbol: false,
            lineStyle: { width: 3, color: '#fff', opacity: 0.8 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255,255,255,0.5)' },
                { offset: 1, color: 'rgba(255,255,255,0)' }
              ])
            }
          }]
        });
      };
      createSpark('sparkF1', '#ffffff');
      createSpark('sparkAlarm', '#ffffff');
      createSpark('sparkSamples', '#ffffff');
      createSpark('sparkSpeed', '#ffffff');
    },

    renderPerformanceTrendChart() {
      if (!this.$refs.chartPerformanceTrend) return;
      
      if (this.aiChartInstances.performanceTrend) {
        this.aiChartInstances.performanceTrend.dispose();
      }
      
      const chart = echarts.init(this.$refs.chartPerformanceTrend);
      this.aiChartInstances.performanceTrend = chart;
      
      let option = {};
      const commonGrid = { left: '10%', right: '5%', top: '10%', bottom: '10%', containLabel: true };
      
      const axisColor = '#606266';
      const splitLineColor = '#ebeef5';

      if (this.performanceChartMode === 'trend') {
        const timeLabels = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
        
        const generateCurve = (targetMax, noiseFactor, stableThreshold) => {
          return timeLabels.map((_, i) => {
            let base;
            if (i <= stableThreshold) {
              const progress = i / stableThreshold;
              base = 90 + (targetMax - 90) * Math.sin(progress * (Math.PI / 2));
            } else {
              base = targetMax;
            }
            return (base + (Math.random() - 0.5) * noiseFactor).toFixed(2);
          });
        };

        const accData = generateCurve(99.2, 0.4, 4);   
        const precData = generateCurve(98.5, 0.5, 7);  
        const recallData = generateCurve(98.0, 0.6, 9); 

        option = {
          grid: { left: '3%', right: '4%', top: '10%', bottom: '10%', containLabel: true },
          tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' }, borderWidth: 1, extraCssText: 'box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)' },
          xAxis: { type: 'category', boundaryGap: false, data: timeLabels, axisLine: { lineStyle: { color: '#DCDFE6' } }, axisTick: { show: false }, axisLabel: { color: axisColor, fontSize: 12, margin: 15 } },
          yAxis: { type: 'value', min: 90, max: 100, splitLine: { lineStyle: { color: splitLineColor, type: 'dashed' } }, axisLabel: { color: axisColor, fontSize: 12 } },
          series: [
            { name: '准确率', type: 'line', data: accData, smooth: true, symbol: 'none', lineStyle: { width: 3, color: '#3B82F6' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59, 130, 246, 0.2)' }, { offset: 1, color: 'rgba(59, 130, 246, 0)' }]) } },
            { name: '精确率', type: 'line', data: precData, smooth: true, symbol: 'none', lineStyle: { width: 3, color: '#10B981' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(16, 185, 129, 0.2)' }, { offset: 1, color: 'rgba(16, 185, 129, 0)' }]) } },
            { name: '召回率', type: 'line', data: recallData, smooth: true, symbol: 'none', lineStyle: { width: 3, color: '#EF4444' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(239, 68, 68, 0.2)' }, { offset: 1, color: 'rgba(239, 68, 68, 0)' }]) } }
          ]
        };
      } else if (this.performanceChartMode === 'efficiency') {
        const timeLabels = this.detectionEfficiencyData.timeLabels;
        const aiData = this.detectionEfficiencyData.aiDetection;
        const humanData = this.detectionEfficiencyData.humanDetection;

        option = {
          grid: { left: '3%', right: '4%', top: '10%', bottom: '10%', containLabel: true },
          tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' }, borderWidth: 1, extraCssText: 'box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)' },
          xAxis: { type: 'category', boundaryGap: false, data: timeLabels, axisLine: { lineStyle: { color: '#DCDFE6' } }, axisTick: { show: false }, axisLabel: { color: axisColor, fontSize: 12, margin: 15 } },
          yAxis: { type: 'value', name: '检测数量 (件)', nameTextStyle: {color: axisColor}, min: 0, splitLine: { lineStyle: { color: splitLineColor, type: 'dashed' } }, axisLabel: { color: axisColor, fontSize: 12 } },
          series: [
            { name: 'AI模型每秒钟检测数量', type: 'line', data: aiData, smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 3, color: '#3B82F6' }, itemStyle: { color: '#3B82F6' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59, 130, 246, 0.2)' }, { offset: 1, color: 'rgba(59, 130, 246, 0)' }]) } },
            { name: '人工检测效率', type: 'line', data: humanData, smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 3, color: '#F59E0B' }, itemStyle: { color: '#F59E0B' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245, 158, 11, 0.2)' }, { offset: 1, color: 'rgba(245, 158, 11, 0)' }]) } }
          ]
        };
      } else if (this.performanceChartMode === 'params') {
        option = {
          grid: commonGrid,
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' }, borderWidth: 1, extraCssText: 'box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)' },
          xAxis: { 
            type: 'value', 
            min: 300, 
            max: 700, 
            interval: 50, 
            axisLabel: { formatter: '{value} M', color: axisColor },
            splitLine: { lineStyle: { color: splitLineColor, type: 'dashed' } }
          },
          yAxis: { 
            type: 'category', 
            data: ['1月', '4月', '7月', '11月'], 
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: axisColor, fontWeight: 'bold' } 
          },
          series: [
            {
              name: '参数规模',
              type: 'bar',
              barWidth: '40%',
              data: [320, 410, 560, 680],
              itemStyle: {
                borderRadius: [0, 20, 20, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#6366F1' },
                  { offset: 1, color: '#818CF8' }
                ])
              },
              label: { show: true, position: 'right', formatter: '{c} M', color: '#6366F1' }
            }
          ]
        };
      } else if (this.performanceChartMode === 'cycle') {
        option = {
          grid: commonGrid,
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' }, borderWidth: 1, extraCssText: 'box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)' },
          xAxis: { 
            type: 'value', 
            min: 100, 
            max: 700, 
            axisLabel: { formatter: '{value} h', color: axisColor },
            splitLine: { lineStyle: { color: splitLineColor, type: 'dashed' } }
          },
          yAxis: { 
            type: 'category', 
            data: ['1月', '4月', '7月', '11月'], 
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: { color: axisColor, fontWeight: 'bold' } 
          },
          series: [
            {
              name: '训练耗时',
              type: 'bar',
              barWidth: '40%',
              data: [680, 520, 350, 150],
              itemStyle: {
                borderRadius: [0, 20, 20, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: '#F59E0B' },
                  { offset: 1, color: '#FCD34D' }
                ])
              },
              label: { show: true, position: 'right', formatter: '{c} h', color: '#D97706' }
            }
          ]
        };
      }
      
      chart.setOption(option);
    },

    renderSampleDistributionChart() {
      if (!this.$refs.chartSampleDistribution) return;
      const chart = echarts.init(this.$refs.chartSampleDistribution);
      this.aiChartInstances.sampleDistribution = chart;

      const total = this.totalSamples;
      const normal = Math.floor(total * 0.85);
      const scratch = Math.floor(total * 0.1);
      const crack = total - normal - scratch;

      const data = [
        { value: normal, name: '正常', itemStyle: { color: '#3B82F6' } },
        { value: scratch, name: '划痕', itemStyle: { color: '#F59E0B' } },
        { value: crack, name: '裂痕', itemStyle: { color: '#EF4444' } }
      ];

      const option = {
        tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#e4e7ed', textStyle: { color: '#303133' }, borderWidth: 1 },
        series: [
          {
            name: '样本分布',
            type: 'pie',
            radius: ['65%', '85%'],
            center: ['50%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: { show: false },
            data: data
          }
        ]
      };
      chart.setOption(option);
    },

    startSampleUpdate() {
      if (this.sampleUpdateTimer) clearInterval(this.sampleUpdateTimer);
      this.sampleUpdateTimer = setInterval(() => {
        if (this.totalSamples < 6950) {
            this.totalSamples += Math.floor(Math.random() * 5 + 1);
        } else {
            this.totalSamples -= Math.floor(Math.random() * 5 + 1);
        }
        
        if (this.timeRange === 'today' && this.aiChartInstances.sampleDistribution) {
          const total = this.totalSamples;
          const normal = Math.floor(total * 0.85);
          const scratch = Math.floor(total * 0.1);
          const crack = total - normal - scratch;
          
          this.aiChartInstances.sampleDistribution.setOption({
            series: [{
              data: [
                { value: normal, name: '正常', itemStyle: { color: '#3B82F6' } },
                { value: scratch, name: '划痕', itemStyle: { color: '#F59E0B' } },
                { value: crack, name: '裂痕', itemStyle: { color: '#EF4444' } }
              ]
            }]
          });
        }
      }, 5000);
    },

    initDetectionEfficiencyData() {
      const now = new Date();
      this.detectionEfficiencyData.timeLabels = [];
      this.detectionEfficiencyData.aiDetection = [];
      this.detectionEfficiencyData.humanDetection = [];
      
      for (let i = 11; i >= 0; i--) {
        const t = new Date(now.getTime() - i * 15000);
        const timeStr = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}:${t.getSeconds().toString().padStart(2,'0')}`;
        this.detectionEfficiencyData.timeLabels.push(timeStr);
        this.detectionEfficiencyData.aiDetection.push(Math.floor(9 + Math.random() * 2));
        this.detectionEfficiencyData.humanDetection.push(Number((2 + Math.random() * 2).toFixed(1)));
      }
    },
    
    startDetectionEfficiencyUpdate() {
      if (this.detectionEfficiencyTimer) clearInterval(this.detectionEfficiencyTimer);
      this.detectionEfficiencyTimer = setInterval(() => {
        const now = new Date();
        const timeStr = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`;
        
        let newVal = Math.floor(10 + (Math.random() - 0.5) * 2);
        newVal = Math.max(8, Math.min(12, newVal));
        
        const lastHuman = this.detectionEfficiencyData.humanDetection[this.detectionEfficiencyData.humanDetection.length - 1] || 2;
        let newHuman = lastHuman + (Math.random() - 0.5) * 1;
        newHuman = Math.max(1, Math.min(5, newHuman));

        this.detectionEfficiencyData.timeLabels.push(timeStr);
        this.detectionEfficiencyData.aiDetection.push(newVal);
        this.detectionEfficiencyData.humanDetection.push(Number(newHuman.toFixed(1)));

        if (this.detectionEfficiencyData.timeLabels.length > 12) {
          this.detectionEfficiencyData.timeLabels.shift();
          this.detectionEfficiencyData.aiDetection.shift();
          this.detectionEfficiencyData.humanDetection.shift();
        }
        
        if (this.timeRange === 'today' && this.performanceChartMode === 'efficiency') {
           this.renderPerformanceTrendChart();
        }
        if (this.timeRange === 'today' && this.aiChartInstances.sparkSpeed) {
           this.aiChartInstances.sparkSpeed.setOption({
             series: [{ data: this.detectionEfficiencyData.aiDetection }]
           });
        }
      }, 15000);
    },

    startAIDataUpdate() {
      if (this.aiDataUpdateTimer) clearInterval(this.aiDataUpdateTimer);
      this.aiDataUpdateTimer = setInterval(() => { 
        this.currentF1Value = Math.max(90, Math.min(99, this.currentF1Value + (Math.random() - 0.5) * 0.2));
        if (this.timeRange === 'today' && this.aiChartInstances.sparkF1) {
          const oldData = this.aiChartInstances.sparkF1.getOption().series[0].data;
          oldData.push(this.currentF1Value);
          if (oldData.length > 10) oldData.shift();
          this.aiChartInstances.sparkF1.setOption({ series: [{ data: oldData }] });
        }
      }, 3000);
    },

    refreshLabelingData() { 
      this.deviceMetrics.cpuUsage = 65; 
      this.deviceMetrics.gpuUsage = 60; 
      this.deviceMetrics.memoryUsage = 65; 
      this.deviceMetrics.diskUsage = 56; 
      this.deviceMetrics.dailyGrowth = 185; 
      this.initDeviceHistoryData(); 
      this.renderLabelingChart(); 
      this.$message.success('设备监控数据已刷新'); 
    },
    
    stopAllTimers() {
      if (this.labelingUpdateTimer) clearInterval(this.labelingUpdateTimer);
      if (this.aiDataUpdateTimer) clearInterval(this.aiDataUpdateTimer);
      if (this.detectionEfficiencyTimer) clearInterval(this.detectionEfficiencyTimer);
      if (this.sampleUpdateTimer) clearInterval(this.sampleUpdateTimer);
      if (this.deviceMetricsTimer) clearInterval(this.deviceMetricsTimer);
      if (this.spiralTimer) clearTimeout(this.spiralTimer);
    },
    
    disposeAllCharts() { 
      Object.values(this.labelingCharts).forEach(c => c.dispose()); 
      this.labelingCharts = {}; 
      if (this.lblRealtimeChart) { this.lblRealtimeChart.dispose(); this.lblRealtimeChart = null; } 
      Object.values(this.aiChartInstances).forEach(c => c && c.dispose()); 
      this.aiChartInstances = {}; 
    },
    
    handleResize() { 
      Object.values(this.labelingCharts).forEach(c => c && !c.isDisposed() && c.resize()); 
      if (this.lblRealtimeChart && !this.lblRealtimeChart.isDisposed()) this.lblRealtimeChart.resize(); 
      Object.values(this.aiChartInstances).forEach(c => c && !c.isDisposed() && c.resize()); 
    },
    
    toggleLabelingTime(type, index) {
      const workshop = this.labelingWorkshopData[index];
      if (workshop.activeTimeRange === type) {
        workshop.activeTimeRange = null; 
      } else {
        workshop.activeTimeRange = type;
        if (type === 'day') this.generateLabelingDayData();
        if (index === 2 && type) this.ensureAutoLabelingData(workshop, type);
        if (index === 3 && type) {
          this.updateQualityStatsData(workshop, type);
        }
      }
      const chartKey = `workshop${index+1}`;
      const chart = this.labelingCharts[chartKey];
      if(chart) {
        const colors = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B'];
        this.setChartOption(chart, workshop, colors[index]);
      }
    },
    
    // 修改点：初始化设备历史数据，生成过去5个小时的整点和半点，最后一位是当前时间
    initDeviceHistoryData() {
      const now = new Date();
      this.deviceHistory.time = [];
      this.deviceHistory.nominal = [];
      this.deviceHistory.actual = [];
      this.deviceHistory.lowerBand = [];
      this.deviceHistory.bandWidth = [];
      this.deviceHistory.upperLimit = [];
      this.deviceHistory.base = [];
      this.deviceHistory.ai = [];
      this.deviceHistory.scratch = [];
      this.deviceHistory.crack = [];
      this.deviceHistory.growth = [];
      this.deviceHistory.rate = [];

      // 算法：找到最近的上一个半点时刻（如9:36 -> 9:30，9:15 -> 9:00）
      let startM = now.getMinutes() >= 30 ? 30 : 0;
      let startH = now.getHours();

      // 生成历史的10个点（每30分钟一个点，覆盖约5小时）
      const historyPoints = [];
      for (let i = 0; i < 10; i++) {
        let m = startM - (i * 30);
        let h = startH;
        while (m < 0) {
            m += 60;
            h -= 1;
        }
        while (h < 0) h += 24;

        let label = m === 0 ? `${h}:00` : `${h}:${m}`;
        historyPoints.unshift(label);
      }

      // 方案2：Confidence Band (双轨容差带) 数据底模
      const nominalPreset = [210, 225, 218, 235, 252, 268, 260, 275, 282, 290];
      const actualPreset = [208, 228, 215, 239, 250, 272, 258, 279, 280, 294];

      historyPoints.forEach((label, idx) => {
        const nom = nominalPreset[idx] + Math.floor(Math.random() * 6 - 3);
        const act = actualPreset[idx] + Math.floor(Math.random() * 8 - 4);
        const tol = Math.floor(nom * 0.15); // ±15% 容差区间
        const lower = nom - tol;
        const bw = tol * 2;
        const upperLim = nom + tol + 35; // 红色阈值警戒线

        this.deviceHistory.time.push(label);
        this.deviceHistory.nominal.push(nom);
        this.deviceHistory.actual.push(act);
        this.deviceHistory.lowerBand.push(lower);
        this.deviceHistory.bandWidth.push(bw);
        this.deviceHistory.upperLimit.push(upperLim);

        // 兼容保留
        this.deviceHistory.base.push(nom);
        this.deviceHistory.ai.push(Math.floor(nom * 0.6));
        this.deviceHistory.scratch.push(Math.floor(nom * 0.25));
        this.deviceHistory.crack.push(Math.floor(nom * 0.1));
        this.deviceHistory.growth.push(Math.floor(100 + Math.random() * 50));
        this.deviceHistory.rate.push(parseFloat((Math.random() * 0.4).toFixed(2)));
      });

      // 添加当前时间作为最后一个点
      const h = now.getHours();
      const m = now.getMinutes().toString().padStart(2, '0');
      const curNom = 295 + Math.floor(Math.random() * 5);
      const curAct = 298 + Math.floor(Math.random() * 6);
      const curTol = Math.floor(curNom * 0.15);

      this.deviceHistory.time.push(`${h}:${m}`);
      this.deviceHistory.nominal.push(curNom);
      this.deviceHistory.actual.push(curAct);
      this.deviceHistory.lowerBand.push(curNom - curTol);
      this.deviceHistory.bandWidth.push(curTol * 2);
      this.deviceHistory.upperLimit.push(curNom + curTol + 35);

      this.deviceHistory.base.push(curNom);
      this.deviceHistory.ai.push(Math.floor(curNom * 0.6));
      this.deviceHistory.scratch.push(Math.floor(curNom * 0.25));
      this.deviceHistory.crack.push(Math.floor(curNom * 0.1));
      this.deviceHistory.growth.push(Math.floor(this.deviceMetrics.dailyGrowth));
      this.deviceHistory.rate.push(parseFloat((Math.random() * 0.4).toFixed(2)));
    },
    
    initAutoLabelingData() {
      const ws3 = this.labelingWorkshopData[2];
      const now = new Date();
      const realtimeData = ws3.chartData.realtime;
      for (let i = 6; i >= 0; i--) {
        const t = new Date(now.getTime() - i * 15000); 
        const timeStr = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}:${t.getSeconds().toString().padStart(2,'0')}`;
        realtimeData.categories.push(timeStr);
        // 修改点：划痕~300, 裂痕~100, 正常 600-800
        const normal = Math.floor(Math.random() * 200 + 600);
        const scratch = Math.floor(Math.random() * 50 + 280);
        const crack = Math.floor(Math.random() * 40 + 80);
        realtimeData.normal.push(normal);
        realtimeData.scratch.push(scratch);
        realtimeData.crack.push(crack);
        // 修改点：标记效率 600-800
        realtimeData.efficiency.push(Math.floor(Math.random() * 200 + 600));
      }
      const dayData = ws3.chartData.day;
      for (let i = 0; i <= 18; i += 3) { 
        dayData.categories.push(`${i}:00`);
        // 修改点：Day数据也相应调整
        dayData.normal.push(Math.floor(Math.random() * 200 + 600));
        dayData.scratch.push(Math.floor(Math.random() * 50 + 280));
        dayData.crack.push(Math.floor(Math.random() * 40 + 80));
        dayData.efficiency.push(Math.floor(Math.random() * 200 + 600));
      }
      this.calculateAIData(ws3);
    },
    
    calculateAIData(workshop) {
      const range = workshop.activeTimeRange || 'realtime';
      const data = workshop.chartData[range];
      if (data && data.normal && data.scratch && data.crack) {
        const recentNormal = data.normal.slice(-7);
        const recentScratch = data.scratch.slice(-7);
        const recentCrack = data.crack.slice(-7);
        const total = recentNormal.reduce((sum, val, idx) => {
          return sum + val + (recentScratch[idx] || 0) + (recentCrack[idx] || 0);
        }, 0);
        const scratchSum = recentScratch.reduce((sum, val) => sum + val, 0);
        const crackSum = recentCrack.reduce((sum, val) => sum + val, 0);
        
        // workshop.aiTotal = total;
        workshop.aiScratch = scratchSum;
        workshop.aiCrack = crackSum;
      }
    },
    
    ensureAutoLabelingData(workshop, type) {
      const data = workshop.chartData[type];
      if (data.categories.length > 7) {
        data.categories = data.categories.slice(-7);
        if(data.normal) data.normal = data.normal.slice(-7);
        if(data.scratch) data.scratch = data.scratch.slice(-7);
        if(data.crack) data.crack = data.crack.slice(-7);
        if(data.efficiency) data.efficiency = data.efficiency.slice(-7);
      } else if (data.categories.length < 7) {
        const diff = 7 - data.categories.length;
        for (let i = 0; i < diff; i++) {
          const idx = data.categories.length;
          if (type === 'day') data.categories.push(`${(idx * 3)}:00`);
          else if (type === 'week') data.categories.push(['周一','周二','周三','周四','周五','周六','周日'][idx]);
          else if (type === 'month') data.categories.push(['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'][idx]);
          // 修改点：保持一致性
          if(data.normal) data.normal.push(Math.floor(Math.random() * 200 + 600));
          if(data.scratch) data.scratch.push(Math.floor(Math.random() * 50 + 280));
          if(data.crack) data.crack.push(Math.floor(Math.random() * 40 + 80));
          if(data.efficiency) data.efficiency.push(Math.floor(Math.random() * 200 + 600));
        }
      }
      this.calculateAIData(workshop);
    },
    
    updateQualityStatsData(workshop, type) {
      const data = workshop.chartData[type];
      if (data.categories.length < 7) {
        const diff = 7 - data.categories.length;
        for(let i=0; i<diff; i++) {
          if (type === 'week') {
            data.categories.push(['周一','周二','周三','周四','周五','周六','周日'][data.categories.length]);
          } else if (type === 'month') {
            data.categories.push(['1-5日','6-10日','11-15日','16-20日','21-25日','26-30日','31日'][data.categories.length]);
          } else {
            data.categories.push(`时段${data.categories.length + 1}`);
          }
          const humanReview = Math.floor(Math.random() * 500 + 800);
          const aiMarked = Math.floor(humanReview * 0.8 + Math.random() * 200);
          const total = humanReview + aiMarked;
          data.humanReview.push(humanReview);
          data.aiMarked.push(aiMarked);
          data.total.push(total);
        }
      }
      this.updateQualityStatsMetrics(workshop, data);
    },
    
    updateQualityStatsMetrics(workshop, data) {
      const recentAiMarked = data.aiMarked.slice(-7);
      const recentTotal = data.total.slice(-7);
      const aiMarkedTotal = recentAiMarked.reduce((sum, val) => sum + val, 0);
      const totalSamples = recentTotal.reduce((sum, val) => sum + val, 0);
      const humanReviewTotal = data.humanReview.slice(-7).reduce((sum, val) => sum + val, 0);
      const humanMachineConsistency = humanReviewTotal > 0 ? Math.floor((aiMarkedTotal / humanReviewTotal) * 100) : 0;
      const markCoverage = totalSamples > 0 ? Math.floor((aiMarkedTotal / totalSamples) * 100) : 0;
      
      // workshop.metrics.aiMarkedTotal = aiMarkedTotal;
      workshop.metrics.humanMachineConsistency = humanMachineConsistency;
      workshop.metrics.markCoverage = markCoverage;
    },

    generateLabelingDayData() {
      const categories = [];
      const incoming = [], labeled = [], unlabeled = [], normal = [], scratch = [], crack = [];
      const ws3_normal = [], ws3_scratch = [], ws3_crack = [], ws3_eff = [];
      const ws4_humanReview = [], ws4_aiMarked = [], ws4_total = [];
      
      for (let i = 0; i <= 18; i += 3) {
        categories.push(`${i}:00`);
        const inc = Math.floor(Math.random() * 200 + 400); 
        const lab = Math.floor(inc * (0.75 + Math.random() * 0.2)); 
        incoming.push(inc); labeled.push(lab); unlabeled.push(inc - lab);
        const n = Math.floor(lab * 0.85);
        normal.push(n); scratch.push(Math.floor(lab * 0.1)); crack.push(lab - n - Math.floor(lab * 0.1));
        // 修改点：Day数据也相应调整（标记效率为 82%~94% 百分比）
        ws3_normal.push(Math.floor(Math.random() * 200 + 600));
        ws3_scratch.push(Math.floor(Math.random() * 50 + 280));
        ws3_crack.push(Math.floor(Math.random() * 40 + 80));
        ws3_eff.push(Math.floor(Math.random() * 12 + 82));
        const humanReview = Math.floor(Math.random() * 500 + 800);
        const aiMarked = Math.floor(humanReview * 0.8 + Math.random() * 200);
        const total = humanReview + aiMarked;
        ws4_humanReview.push(humanReview);
        ws4_aiMarked.push(aiMarked);
        ws4_total.push(total);
      }
      this.labelingWorkshopData[0].chartData.day = { categories, incoming, labeled, unlabeled, normal:[], scratch:[], crack:[] };
      this.labelingWorkshopData[1].chartData.day = { categories, incoming:[], labeled:[], unlabeled:[], normal, scratch, crack };
      this.labelingWorkshopData[2].chartData.day = { categories, normal: ws3_normal, scratch: ws3_scratch, crack: ws3_crack, efficiency: ws3_eff };
      this.labelingWorkshopData[3].chartData.day = { categories, humanReview: ws4_humanReview, aiMarked: ws4_aiMarked, total: ws4_total };
      this.calculateAIData(this.labelingWorkshopData[2]);
      this.updateQualityStatsMetrics(this.labelingWorkshopData[3], this.labelingWorkshopData[3].chartData.day);
    },
    
    initLabelingRealtimeStream() {
      const now = new Date();
      for (let i = 6; i >= 0; i--) {
        const t = new Date(now.getTime() - i * 15000);
        const timeStr = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}:${t.getSeconds().toString().padStart(2,'0')}`;
        const ws1 = this.labelingWorkshopData[0].chartData.realtime;
        const inc = Math.floor(Math.random() * 200 + 400);
        ws1.categories.push(timeStr); ws1.incoming.push(inc); ws1.labeled.push(Math.floor(inc*0.8)); ws1.unlabeled.push(inc - Math.floor(inc*0.8));
        const ws2 = this.labelingWorkshopData[1].chartData.realtime;
        const scr = Math.floor(Math.random() * 30 + 50); const crk = Math.floor(Math.random() * 20 + 30);
        ws2.categories.push(timeStr); ws2.normal.push(Math.max(0, Math.floor(inc*0.8) - scr - crk)); ws2.scratch.push(scr); ws2.crack.push(crk);
        const ws3 = this.labelingWorkshopData[2].chartData.realtime;
        // 修改点：正常(600-800) >> 划痕(280-330) >> 裂痕(80-120)
        const normal = Math.floor(Math.random() * 200 + 600);
        const scratch = Math.floor(Math.random() * 50 + 280);
        const crack = Math.floor(Math.random() * 40 + 80);
        ws3.categories.push(timeStr); ws3.normal.push(normal); ws3.scratch.push(scratch); ws3.crack.push(crack);
        // 修改点：标记效率按百分比 (82%~94%)
        ws3.efficiency.push(Math.floor(Math.random() * 12 + 82));
        const ws4 = this.labelingWorkshopData[3].chartData.realtime;
        const humanReview = Math.floor(Math.random() * 200 + 400);
        const aiMarked = Math.floor(humanReview * 0.8 + Math.random() * 100);
        const total = humanReview + aiMarked;
        ws4.categories.push(timeStr); ws4.humanReview.push(humanReview); ws4.aiMarked.push(aiMarked); ws4.total.push(total);
      }
      this.calculateAIData(this.labelingWorkshopData[2]);
      this.updateQualityStatsMetrics(this.labelingWorkshopData[3], this.labelingWorkshopData[3].chartData.realtime);
    },
    
    initLabelingView() {
      this.initLabelingRealtimeStream();
      this.generateLabelingDayData();
      this.initAutoLabelingData();
      this.initDeviceHistoryData();
      this.initLabelingCharts();
      this.initLabelingRealtimeChart();
      this.startLabelingDataUpdate();
    },
    
    initLabelingCharts() {
      ['workshop1','workshop2','workshop3','workshop4'].forEach((key, index) => {
        const dom = this.$refs[`lbl_${key}Chart`];
        if(dom) {
          this.labelingCharts[key] = echarts.init(dom);
          const colors = ['#3B82F6', '#10B981', '#8B5CF6', '#F59E0B'];
          this.setChartOption(this.labelingCharts[key], this.labelingWorkshopData[index], colors[index]);
        }
      });
    },
    
    initLabelingRealtimeChart() {
      if(this.$refs.lbl_realtimeChart) {
        this.lblRealtimeChart = echarts.init(this.$refs.lbl_realtimeChart);
        this.renderLabelingChart();
      }
    },
    
    startLabelingDataUpdate() {
       if (this.labelingUpdateTimer) clearInterval(this.labelingUpdateTimer);
       if (this.deviceMetricsTimer) clearInterval(this.deviceMetricsTimer);

       this.labelingUpdateTimer = setInterval(() => { this.updateLabelingData(); }, 15000);
       // 1秒的定时器，用于更新CPU/GPU/内存，内部通过计数器每4秒更新磁盘
       this.deviceMetricsTimer = setInterval(() => { this.updateDeviceMetrics(); }, 1000);
    },

    updateDeviceMetrics() {
       this.dynamicFullTime = new Date().toLocaleString();
       // CPU每秒变化
       this.deviceMetrics.cpuUsage = Math.max(20, Math.min(95, this.deviceMetrics.cpuUsage + (Math.random() * 4 - 2)));
       
       // GPU: 50% - 70%
       this.deviceMetrics.gpuUsage = Math.max(50, Math.min(70, this.deviceMetrics.gpuUsage + (Math.random() * 4 - 2)));
       
       // Memory: 60% - 70%
       this.deviceMetrics.memoryUsage = Math.max(60, Math.min(70, this.deviceMetrics.memoryUsage + (Math.random() * 3 - 1.5)));
       
       this.deviceMetrics.cpuTrend = parseFloat((Math.random() * 4 - 2).toFixed(1));
       this.deviceMetrics.gpuTrend = parseFloat((Math.random() * 4 - 2).toFixed(1));
       this.deviceMetrics.memoryTrend = parseFloat((Math.random() * 4 - 2).toFixed(1));
       
       // 磁盘使用率：每4秒变化一次
       this.diskIntervalCount++;
       if (this.diskIntervalCount >= 8) {
         this.diskIntervalCount = 0;
         // 磁盘每次增加1%
         this.deviceMetrics.diskUsage = Math.min(100, this.deviceMetrics.diskUsage + 1);
         // 增长率逻辑修改：总是显示正增长 (+0.5% ~ +1.5%)，符合只增不减的逻辑
         this.deviceMetrics.diskTrend = parseFloat((0.5 + Math.random()).toFixed(1));
       }
    },
    
    updateLabelingData() {
       this.labelingUpdateTime = new Date().toLocaleTimeString();

       // 修改点：实时更新时间轴，保持 10 个历史时刻（半小时刻度） + 1 个当前时刻
       const now = new Date();
       const currentHour = now.getHours();
       const currentMinute = now.getMinutes();
       
       // 找到最近的上一个半点时刻
       let startM = currentMinute >= 30 ? 30 : 0;
       let startH = currentHour;
       
       const newTimeLabels = [];
       for (let i = 0; i < 10; i++) {
         let m = startM - (i * 30);
         let h = startH;
         while (m < 0) {
             m += 60;
             h -= 1;
         }
         while (h < 0) h += 24;
         let label = m === 0 ? `${h}点` : `${h}:${m}`;
         newTimeLabels.unshift(label);
       }
       // 添加当前具体时间
       const mStr = currentMinute.toString().padStart(2, '0');
       newTimeLabels.push(`${currentHour}:${mStr}`);
       
       this.deviceHistory.time = newTimeLabels;

       this.deviceMetrics.dailyGrowth = Math.floor(Math.max(120, Math.min(250, this.deviceMetrics.dailyGrowth + (Math.random() * 10 - 5))));

       // 确保数据数组长度为11 (10 history + 1 current)
       // 如果长度不足，补充数据
       const keys = ['nominal', 'actual', 'lowerBand', 'bandWidth', 'upperLimit', 'base', 'ai', 'scratch', 'crack', 'growth', 'rate'];
       keys.forEach(k => {
         while(this.deviceHistory[k].length < 11) this.deviceHistory[k].push(100);
         while(this.deviceHistory[k].length > 11) this.deviceHistory[k].shift();
       });

       // 方案2：Confidence Band 动态波动更新
       const lastNom = this.deviceHistory.nominal[9] || 290;
       const curNom = Math.floor(Math.max(250, Math.min(320, lastNom + (Math.random() * 8 - 4))));
       const curAct = Math.floor(curNom + (Math.random() * 16 - 8));
       const curTol = Math.floor(curNom * 0.15);

       this.$set(this.deviceHistory.nominal, 10, curNom);
       this.$set(this.deviceHistory.actual, 10, curAct);
       this.$set(this.deviceHistory.lowerBand, 10, curNom - curTol);
       this.$set(this.deviceHistory.bandWidth, 10, curTol * 2);
       this.$set(this.deviceHistory.upperLimit, 10, curNom + curTol + 35);

       // 兼容保留
       this.$set(this.deviceHistory.base, 10, curNom);
       this.$set(this.deviceHistory.ai, 10, Math.floor(curNom * 0.6));
       this.$set(this.deviceHistory.scratch, 10, Math.floor(curNom * 0.25));
       this.$set(this.deviceHistory.crack, 10, Math.floor(curNom * 0.1));
       this.$set(this.deviceHistory.growth, 10, Math.floor(this.deviceMetrics.dailyGrowth));
       this.$set(this.deviceHistory.rate, 10, parseFloat((Math.random() * 0.8 - 0.4).toFixed(2)));
       
       this.labelingWorkshopData.forEach((workshop, index) => {
         const key = `workshop${index + 1}`;
         const chart = this.labelingCharts[key];
         if (chart && !chart.isDisposed()) {
           if (workshop.isSampleStats) {
             if (!workshop.activeTimeRange) {
               const rtData = workshop.chartData.realtime;
               const now = new Date();
               const timeStr = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`;
               while (rtData.categories.length >= 7) { 
                 rtData.categories.shift(); 
                 if(rtData.incoming) rtData.incoming.shift(); 
                 if(rtData.labeled) rtData.labeled.shift(); 
                 if(rtData.unlabeled) rtData.unlabeled.shift(); 
                 if(rtData.normal) rtData.normal.shift(); 
                 if(rtData.scratch) rtData.scratch.shift(); 
                 if(rtData.crack) rtData.crack.shift(); 
               }
               rtData.categories.push(timeStr);
               const inc = Math.floor(Math.random() * 200 + 400); 
               const lab = Math.floor(inc * 0.8);
               if(rtData.incoming) rtData.incoming.push(inc); 
               if(rtData.labeled) rtData.labeled.push(lab); 
               if(rtData.unlabeled) rtData.unlabeled.push(inc - lab);
               const scr = Math.floor(Math.random() * 30 + 50); 
               const crk = Math.floor(Math.random() * 20 + 30);
               if(rtData.normal) rtData.normal.push(Math.max(0, lab - scr - crk)); 
               if(rtData.scratch) rtData.scratch.push(scr); 
               if(rtData.crack) rtData.crack.push(crk);
               const seriesData = [];
               if (workshop.chartMode === 'bar') { 
                 seriesData.push({ data: rtData.incoming }); 
                 seriesData.push({ data: rtData.labeled }); 
                 seriesData.push({ data: rtData.unlabeled }); 
               } else { 
                 seriesData.push({ data: rtData.normal }); 
                 seriesData.push({ data: rtData.scratch }); 
                 seriesData.push({ data: rtData.crack }); 
               }
               chart.setOption({ xAxis: { data: rtData.categories }, series: seriesData });
             } 
           } else if (workshop.isAutoLabeling && this.timeRange === 'labeling') {
             if (!workshop.activeTimeRange) {
               const data = workshop.chartData.realtime;
               const now = new Date();
               const timeStr = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`;
               while (data.categories.length >= 7) { 
                 data.categories.shift(); 
                 data.normal.shift(); 
                 data.scratch.shift(); 
                 data.crack.shift(); 
                 data.efficiency.shift(); 
               }
               
               // 修改点：实时更新逻辑与初始化逻辑保持一致（标记效率百分比 82%~94%）
               const eff = Math.floor(Math.random() * 12 + 82);
               workshop.efficiency = eff;
               data.categories.push(timeStr); 
               
               // 正常(600-800), 划痕(280-330), 裂痕(80-120)
               const normal = Math.floor(Math.random() * 200 + 600);
               const scratch = Math.floor(Math.random() * 50 + 280);
               const crack = Math.floor(Math.random() * 40 + 80);
               
               data.normal.push(normal); 
               data.scratch.push(scratch); 
               data.crack.push(crack); 
               data.efficiency.push(eff);
               
               const recentNormal = data.normal.slice(-7);
               const recentScratch = data.scratch.slice(-7);
               const recentCrack = data.crack.slice(-7);
               const total = recentNormal.reduce((sum, val, idx) => {
                 return sum + val + (recentScratch[idx] || 0) + (recentCrack[idx] || 0);
               }, 0);
               const scratchSum = recentScratch.reduce((sum, val) => sum + val, 0);
               const crackSum = recentCrack.reduce((sum, val) => sum + val, 0);
               // workshop.aiTotal = total;
               workshop.aiScratch = scratchSum;
               workshop.aiCrack = crackSum;
               chart.setOption({ 
                 xAxis: { data: data.categories }, 
                 series: [ 
                   { data: data.normal }, 
                   { data: data.scratch }, 
                   { data: data.crack }, 
                   { data: data.efficiency } 
                 ] 
               });
             }
           } else if (workshop.isQualityStats && this.timeRange === 'labeling') {
             if (!workshop.activeTimeRange) {
               const data = workshop.chartData.realtime;
               const now = new Date();
               const timeStr = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}:${now.getSeconds().toString().padStart(2,'0')}`;
               while (data.categories.length >= 7) { 
                 data.categories.shift(); 
                 data.humanReview.shift(); 
                 data.aiMarked.shift(); 
                 data.total.shift(); 
               }
               const humanReview = Math.floor(Math.random() * 200 + 400);
               const aiMarked = Math.floor(humanReview * 0.8 + Math.random() * 100);
               const total = humanReview + aiMarked;
               data.categories.push(timeStr); 
               data.humanReview.push(humanReview);
               data.aiMarked.push(aiMarked);
               data.total.push(total);
               this.updateQualityStatsMetrics(workshop, data);
               this.setChartOption(chart, workshop, '#F59E0B');
             }
           }
         }
       });
       if (this.lblRealtimeChart) this.renderLabelingChart();
    },
    renderLabelingChart() {
       if (!this.lblRealtimeChart) return;

       const axisColor = '#94a3b8';
       const splitColor = '#f1f5f9';

       // 11个精密采样点：精准呈现“爬升 -> 驼峰1 -> 明显下沉回落 -> 强力爬升 -> 驼峰2 -> 再次下沉回落 -> 冲顶”的波浪走势
       const xData = [
         '10:44:01', '10:44:16', '10:44:31',
         '10:44:46', '10:44:56', '10:45:07',
         '10:45:12', '10:45:17', '10:45:23', '10:45:28',
         '10:45:32'
       ];

       const baseData =    [80,  95, 110, 118, 132, 148, 162, 175, 188, 198, 210];
       const aiData =      [110, 155, 205, 180, 195, 245, 275, 240, 270, 300, 320];
       const scratchData = [80,  110, 145, 122, 130, 168, 212, 175, 202, 232, 250];
       const crackData =   [50,  70,  85,  70,  75,  92, 115,  90, 102, 112, 120];

       // 方案1：ECharts 官方经典 Gradient Stacked Area (1:1 像素级复刻设计图 - 真实多重起伏与下降回落波浪)
       const series = [
         {
           name: '标准样本库库存',
           type: 'line',
           stack: 'Total',
           smooth: 0.45,
           lineStyle: { width: 0 },
           showSymbol: false,
           areaStyle: {
             opacity: 0.95,
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
               { offset: 0, color: '#34d399' },
               { offset: 1, color: '#10b981' }
             ])
           },
           emphasis: { focus: 'series' },
           data: baseData
         },
         {
           name: 'AI推断缓存',
           type: 'line',
           stack: 'Total',
           smooth: 0.45,
           lineStyle: { width: 0 },
           showSymbol: false,
           areaStyle: {
             opacity: 0.95,
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
               { offset: 0, color: '#38bdf8' },
               { offset: 1, color: '#0284c7' }
             ])
           },
           emphasis: { focus: 'series' },
           data: aiData
         },
         {
           name: '划痕特征流',
           type: 'line',
           stack: 'Total',
           smooth: 0.45,
           lineStyle: { width: 0 },
           showSymbol: false,
           areaStyle: {
             opacity: 0.95,
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
               { offset: 0, color: '#a855f7' },
               { offset: 1, color: '#7c3aed' }
             ])
           },
           emphasis: { focus: 'series' },
           data: scratchData
         },
         {
           name: '高危裂纹特征',
           type: 'line',
           stack: 'Total',
           smooth: 0.45,
           lineStyle: { width: 0 },
           showSymbol: false,
           areaStyle: {
             opacity: 0.95,
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
               { offset: 0, color: '#f43f5e' },
               { offset: 1, color: '#e11d48' }
             ])
           },
           emphasis: { focus: 'series' },
           data: crackData
         }
       ];

       const option = {
         color: ['#10b981', '#0284c7', '#8b5cf6', '#f43f5e'],
         grid: { left: '12px', right: '36px', top: '56px', bottom: '8px', containLabel: true },
         tooltip: {
           trigger: 'axis',
           axisPointer: {
             type: 'cross',
             label: {
               backgroundColor: '#475569'
             }
           },
           backgroundColor: 'rgba(255, 255, 255, 0.98)',
           borderColor: '#e2e8f0',
           borderWidth: 1,
           padding: [8, 12],
           textStyle: { color: '#1e293b', fontSize: 11 },
           extraCssText: 'box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.08); border-radius: 6px;'
         },
         legend: {
           data: ['标准样本库库存', 'AI推断缓存', '划痕特征流', '高危裂纹特征'],
           textStyle: { color: '#64748b', fontSize: 11 },
           itemWidth: 8,
           itemHeight: 8,
           itemGap: 16,
           top: '8px',
           left: 'center',
           icon: 'circle'
         },
         xAxis: [
           {
             type: 'category',
             boundaryGap: false,
             data: xData,
             axisLine: { show: false },
             axisTick: { show: false },
             axisLabel: {
               color: axisColor,
               fontSize: 11,
               fontFamily: 'ui-monospace, monospace',
               margin: 10,
               showMaxLabel: true,
               formatter: function(val) {
                 if (['10:44:01', '10:44:46', '10:45:17', '10:45:32'].indexOf(val) > -1) {
                   return val;
                 }
                 return '';
               }
             }
           }
         ],
         yAxis: [
           {
             type: 'value',
             name: '存储容量 (MB)',
             min: 0,
             max: 1000,
             interval: 200,
             nameGap: 14,
             nameTextStyle: {
               color: axisColor,
               fontSize: 11,
               align: 'left',
               padding: [0, 0, 4, -28]
             },
             axisLabel: { color: axisColor, fontSize: 11, fontFamily: 'ui-monospace, monospace' },
             splitLine: { lineStyle: { color: splitColor, type: 'dashed' } },
             axisLine: { show: false }
           }
         ],
         series: series
       };
       this.lblRealtimeChart.setOption(option, { notMerge: true });
    },
    
    setChartOption(chart, workshopData, color) {
       // 高质感设计规范颜色体系
       const axisColor = '#909399';
       const splitColor = '#f0f2f5';

       if (workshopData.isSpiral) {
         this.renderSpiralChart(chart, workshopData);
         return;
       }

       if (workshopData.isSampleStats) {
         const range = workshopData.activeTimeRange || 'realtime';
         const activeData = workshopData.chartData[range];
         const mode = workshopData.chartMode;
         chart.clear();
         let series = []; let legendData = [];

         if (mode === 'bar') {
           legendData = ['入库样本量','已标记样本量','未标记样本量'];
           series = [
             {
               name: '入库样本量',
               type: 'bar',
               data: activeData.incoming.slice(-7),
               barMaxWidth: 14,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#3b82f6' },
                   { offset: 1, color: '#2563eb' }
                 ]),
                 borderRadius: [3, 3, 0, 0]
               }
             },
             {
               name: '已标记样本量',
               type: 'bar',
               data: activeData.labeled.slice(-7),
               barMaxWidth: 14,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#10b981' },
                   { offset: 1, color: '#059669' }
                 ]),
                 borderRadius: [3, 3, 0, 0]
               }
             },
             {
               name: '未标记样本量',
               type: 'bar',
               data: activeData.unlabeled.slice(-7),
               barMaxWidth: 14,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#cbd5e1' },
                   { offset: 1, color: '#94a3b8' }
                 ]),
                 borderRadius: [3, 3, 0, 0]
               }
             }
           ];
         } else {
           legendData = ['正常','划痕','裂痕'];
           series = [
             {
               name: '正常',
               type: 'line',
               data: activeData.normal.slice(-7),
               smooth: 0.35,
               symbol: 'circle',
               symbolSize: 5,
               showSymbol: false,
               lineStyle: { color: '#10b981', width: 2 },
               itemStyle: { color: '#10b981', borderWidth: 2, borderColor: '#fff' },
               areaStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: 'rgba(16, 185, 129, 0.16)' },
                   { offset: 1, color: 'rgba(16, 185, 129, 0.00)' }
                 ])
               }
             },
             {
               name: '划痕',
               type: 'line',
               data: activeData.scratch.slice(-7),
               smooth: 0.35,
               symbol: 'circle',
               symbolSize: 5,
               showSymbol: false,
               lineStyle: { color: '#f59e0b', width: 2 },
               itemStyle: { color: '#f59e0b', borderWidth: 2, borderColor: '#fff' },
               areaStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: 'rgba(245, 158, 11, 0.16)' },
                   { offset: 1, color: 'rgba(245, 158, 11, 0.00)' }
                 ])
               }
             },
             {
               name: '裂痕',
               type: 'line',
               data: activeData.crack.slice(-7),
               smooth: 0.35,
               symbol: 'circle',
               symbolSize: 5,
               showSymbol: false,
               lineStyle: { color: '#ef4444', width: 2 },
               itemStyle: { color: '#ef4444', borderWidth: 2, borderColor: '#fff' },
               areaStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: 'rgba(239, 68, 68, 0.16)' },
                   { offset: 1, color: 'rgba(239, 68, 68, 0.00)' }
                 ])
               }
             }
           ];
         }

         chart.setOption({
           tooltip: {
             trigger: 'axis',
             backgroundColor: 'rgba(255, 255, 255, 0.98)',
             borderColor: '#e2e8f0',
             borderWidth: 1,
             padding: [8, 12],
             textStyle: { color: '#1e293b', fontSize: 11 },
             extraCssText: 'box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.08); border-radius: 6px;',
             axisPointer: { type: 'line', lineStyle: { color: '#cbd5e1', type: 'dashed' } }
           },
           legend: {
             data: legendData,
             textStyle: { color: '#64748b', fontSize: 10 },
             itemWidth: 8,
             itemHeight: 8,
             itemGap: 12,
             top: 0,
             icon: 'circle'
           },
           grid: { left: '2%', right: '2%', bottom: '2%', top: '20%', containLabel: true },
           xAxis: {
             type: 'category',
             data: activeData.categories.slice(-7),
             axisLine: { lineStyle: { color: '#e2e8f0' } },
             axisTick: { show: false },
             axisLabel: { color: axisColor, fontSize: 10, interval: 0, rotate: workshopData.activeTimeRange === 'month' ? 30 : 0 }
           },
           yAxis: {
             type: 'value',
             name: '数量',
             nameTextStyle: { color: axisColor, padding: [0, 16, 0, 0], fontSize: 10 },
             splitLine: { lineStyle: { color: splitColor, type: 'dashed' } },
             axisLabel: { color: axisColor, fontSize: 10 }
           },
           series: series
         });
         return;
       }
       if (workshopData.isAutoLabeling && this.timeRange === 'labeling') {
         const range = workshopData.activeTimeRange || 'realtime';
         const activeData = workshopData.chartData[range];
         chart.clear();
         chart.setOption({
           tooltip: {
             trigger: 'axis',
             backgroundColor: 'rgba(255, 255, 255, 0.98)',
             borderColor: '#e2e8f0',
             borderWidth: 1,
             padding: [8, 12],
             textStyle: { color: '#1e293b', fontSize: 11 },
             extraCssText: 'box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.08); border-radius: 6px;',
             axisPointer: { type: 'line', lineStyle: { color: '#cbd5e1', type: 'dashed' } }
           },
           legend: {
             data: ['正常样本','划痕样本','裂痕样本','标记效率'],
             textStyle: { color: '#64748b', fontSize: 10 },
             itemWidth: 8,
             itemHeight: 8,
             itemGap: 10,
             top: 0,
             icon: 'circle'
           },
           grid: { left: '2%', right: '2%', bottom: '2%', top: '20%', containLabel: true },
           xAxis: {
             type: 'category',
             data: activeData.categories.slice(-7),
             axisLine: { lineStyle: { color: '#e2e8f0' } },
             axisTick: { show: false },
             axisLabel: { color: axisColor, fontSize: 10, interval: 0, rotate: range === 'month' ? 30 : 0 }
           },
           yAxis: [
             {
               type: 'value',
               name: '样本量',
               nameTextStyle: { color: axisColor, fontSize: 10, padding: [0, 16, 0, 0] },
               axisLabel: { color: axisColor, fontSize: 10 },
               min: 0,
               max: 1000,
               interval: 250,
               splitLine: { lineStyle: { color: splitColor, type: 'dashed' } },
               axisLine: { show: false }
             },
             {
               type: 'value',
               name: '效率',
               nameTextStyle: { color: axisColor, fontSize: 10, padding: [0, 0, 0, 16] },
               axisLabel: {
                 color: axisColor,
                 fontSize: 10,
                 formatter: '{value}%'
               },
               min: 0,
               max: 100,
               interval: 25,
               splitLine: { show: false },
               axisLine: { show: false }
             }
           ],
           series: [
             {
               name: '正常样本',
               type: 'bar',
               data: activeData.normal.slice(-7),
               barMaxWidth: 8,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#38bdf8' },
                   { offset: 1, color: '#0284c7' }
                 ]),
                 borderRadius: [2, 2, 0, 0]
               }
             },
             {
               name: '划痕样本',
               type: 'bar',
               data: activeData.scratch.slice(-7),
               barMaxWidth: 8,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#fbbf24' },
                   { offset: 1, color: '#d97706' }
                 ]),
                 borderRadius: [2, 2, 0, 0]
               }
             },
             {
               name: '裂痕样本',
               type: 'bar',
               data: activeData.crack.slice(-7),
               barMaxWidth: 8,
               itemStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: '#f87171' },
                   { offset: 1, color: '#dc2626' }
                 ]),
                 borderRadius: [2, 2, 0, 0]
               }
             },
             {
               name: '标记效率',
               type: 'line',
               yAxisIndex: 1,
               data: activeData.efficiency.slice(-7),
               smooth: 0.35,
               symbol: 'circle',
               symbolSize: 4,
               showSymbol: false,
               lineStyle: { color: '#8b5cf6', width: 2 },
               itemStyle: { color: '#8b5cf6', borderWidth: 2, borderColor: '#fff' },
               areaStyle: {
                 color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                   { offset: 0, color: 'rgba(139, 92, 246, 0.18)' },
                   { offset: 1, color: 'rgba(139, 92, 246, 0.00)' }
                 ])
               }
             }
           ]
         });
         return;
       }
       if (workshopData.isQualityStats && this.timeRange === 'labeling') {
         const range = workshopData.activeTimeRange || 'realtime';
         chart.clear();

         // 高精度雷达矩阵度量指标
         const radarMetricsByRange = {
           realtime: {
             ai: [98.5, 96.2, 94.8, 92.4, 95.6, 97.2],
             human: [99.2, 97.5, 96.0, 95.1, 98.0, 98.8]
           },
           day: {
             ai: [97.8, 95.4, 93.6, 91.8, 94.5, 96.8],
             human: [99.0, 98.1, 96.5, 94.8, 97.6, 98.5]
           },
           week: {
             ai: [98.2, 96.8, 95.1, 93.2, 96.0, 97.5],
             human: [99.4, 98.5, 97.2, 95.6, 98.2, 99.0]
           },
           month: {
             ai: [98.6, 97.1, 95.8, 94.0, 96.5, 98.0],
             human: [99.5, 98.8, 97.6, 96.2, 98.6, 99.2]
           }
         };

         const activeMetrics = radarMetricsByRange[range] || radarMetricsByRange.realtime;

         chart.setOption({
           tooltip: {
             trigger: 'item',
             backgroundColor: 'rgba(255, 255, 255, 0.98)',
             borderColor: '#e2e8f0',
             borderWidth: 1,
             padding: [8, 12],
             textStyle: { color: '#1e293b', fontSize: 11 },
             extraCssText: 'box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.08); border-radius: 6px;'
           },
           legend: {
             data: ['AI推理评估', '专家复审标准'],
             textStyle: { color: '#475569', fontSize: 11 },
             itemWidth: 8,
             itemHeight: 8,
             itemGap: 16,
             top: '2%',
             left: '4%',
             icon: 'circle'
           },
           radar: {
             indicator: [
               { name: '边缘吻合度', max: 100 },
               { name: '特征召回率', max: 100 },
               { name: '人机一致率', max: 100 },
               { name: '抵抗敏感度', max: 100 },
               { name: '微裂纹检出', max: 100 },
               { name: '几何精度', max: 100 }
             ],
             radius: '68%',
             center: ['50%', '55%'],
             splitNumber: 4,
             shape: 'polygon',
             axisName: {
               color: '#475569',
               fontSize: 11,
               fontFamily: 'ui-monospace, monospace'
             },
             splitLine: {
               lineStyle: {
                 color: ['#e2e8f0', '#edf2f7', '#f1f5f9', '#bbf7d0']
               }
             },
             splitArea: {
               show: true,
               areaStyle: {
                 color: ['#ffffff', '#f8fafc', '#f1f5f9', '#f8fafc']
               }
             },
             axisLine: {
               lineStyle: { color: '#cbd5e1' }
             }
           },
           series: [
             {
               name: '质量效能雷达',
               type: 'radar',
               data: [
                 {
                   value: [94, 88, 93, 85, 96, 92],
                   name: 'AI推理评估',
                   symbol: 'circle',
                   symbolSize: 5,
                   lineStyle: { color: '#0284c7', width: 2 },
                   itemStyle: { color: '#0284c7' },
                   areaStyle: { color: 'rgba(2, 132, 199, 0.18)' }
                 },
                 {
                   value: [99, 98, 97, 96, 98, 99],
                   name: '专家复审标准',
                   symbol: 'circle',
                   symbolSize: 5,
                   lineStyle: { color: '#16a34a', width: 1.8, type: 'dashed' },
                   itemStyle: { color: '#16a34a' },
                   areaStyle: { color: 'rgba(22, 163, 74, 0.04)' }
                 }
               ]
             }
           ]
         });
         return;
       }
    },

    renderSpiralChart(chart, workshopData) {
      if (!chart) return;
      chart.clear();

      const _animationDuration = 5500;
      const _animationDurationUpdate = 8000;
      const _animationEasingUpdate = 'linear';
      // 12 时段刻度
      const _radianLabels = ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'];
      const _valOnRoundRadian = _radianLabels.length;
      const _radianStep = Math.PI / 45;
      const _barWidthValue = 0.4;
      const _valOnRadiusStep = 4;
      const _startRadian = Math.PI / 2;
      // 对应原图表：正常 (绿)、划痕 (橙)、裂痕 (红)
      const _colors = [
        { fill: '#10b981', text: '#059669' },
        { fill: '#f59e0b', text: '#d97706' },
        { fill: '#ef4444', text: '#dc2626' }
      ];
      // 映射到三类缺陷样本的阶梯增长数据源
      const _datasourceList = [
        [[1, 3], [2, 6], [3, 9]],
        [[1, 12], [2, 16], [3, 14]],
        [[1, 17], [2, 22], [3, 19]],
        [[1, 19], [2, 33], [3, 24]],
        [[1, 24], [2, 42], [3, 29]],
        [[1, 27], [2, 47], [3, 41]],
        [[1, 36], [2, 52], [3, 52]],
        [[1, 46], [2, 59], [3, 63]],
        [[1, 60], [2, 63], [3, 69]]
      ];
      const _barNamesByOrdinal = { 1: '正常', 2: '划痕', 3: '裂痕' };

      const getSpiralValueOnRadius = (valOnStartRadius, valOnEndAngle) => {
        return valOnStartRadius + _valOnRadiusStep * (valOnEndAngle / _valOnRoundRadian);
      };

      const getMaxRadius = () => {
        let radius = 0;
        const datasource = _datasourceList[this.spiralDataIndex];
        for (let j = 0; j < datasource.length; j++) {
          const dataItem = datasource[j];
          radius = Math.max(radius, getSpiralValueOnRadius(dataItem[0], dataItem[1]));
        }
        return Math.ceil(radius * 1.2);
      };

      const getSpiralRadius = (startRadius, endRadian, radiusStep) => {
        return startRadius + radiusStep * ((_startRadian - endRadian) / (Math.PI * 2));
      };

      const convertToPolarPoint = (renderItemParams, radius, radian) => {
        return [
          Math.cos(radian) * radius + renderItemParams.coordSys.cx,
          -Math.sin(radian) * radius + renderItemParams.coordSys.cy
        ];
      };

      const getRadiusStepByWidth = (widthRadius) => {
        return (widthRadius / _barWidthValue) * _valOnRadiusStep;
      };

      const makeShapePoints = (params, widthRadius, startRadius, endRadian) => {
        const points = [];
        const radiusStep = getRadiusStepByWidth(widthRadius);
        for (let iRadian = _startRadian, end = endRadian - _radianStep; iRadian > end; iRadian -= _radianStep) {
          iRadian < endRadian && (iRadian = endRadian);
          const iRadius = getSpiralRadius(startRadius - widthRadius, iRadian, radiusStep);
          points.push(convertToPolarPoint(params, iRadius, iRadian));
        }
        for (let iRadian = endRadian; iRadian < _startRadian + _radianStep; iRadian += _radianStep) {
          iRadian > _startRadian && (iRadian = _startRadian);
          const iRadius = getSpiralRadius(startRadius + widthRadius, iRadian, radiusStep);
          points.push(convertToPolarPoint(params, iRadius, iRadian));
        }
        return points;
      };

      const makeLabelPosition = (params, widthRadius, startRadius, endRadian) => {
        const radiusStep = getRadiusStepByWidth(widthRadius);
        const iRadius = getSpiralRadius(startRadius, endRadian, radiusStep);
        return convertToPolarPoint(params, iRadius, endRadian - 10 / iRadius);
      };

      // 对应三类缺陷样本的当前数量映射（与折线图同量级：正常 300~400，划痕 60~80，裂痕 30~50）
      const _valCountList = [
        [320, 68, 36],
        [335, 72, 38],
        [350, 75, 42],
        [362, 78, 45],
        [378, 80, 47],
        [390, 84, 50],
        [375, 82, 48],
        [360, 79, 44],
        [345, 74, 40]
      ];

      const makeText = (endRadian, dataIdx) => {
        const counts = _valCountList[this.spiralDataIndex] || [350, 75, 42];
        const count = counts[dataIdx] || 100;
        const catName = _barNamesByOrdinal[dataIdx + 1] || '样本';
        return '{count|' + count + '条}\n{cat|' + catName + '}';
      };

      const addPolygon = (params, children, widthRadius, startRadius, endRadian, color) => {
        children.push({
          type: 'polygon',
          shape: {
            points: makeShapePoints(params, widthRadius, startRadius, endRadian)
          },
          extra: {
            widthRadius: widthRadius,
            startRadius: startRadius,
            endRadian: endRadian,
            transition: ['widthRadius', 'startRadius', 'endRadian']
          },
          style: {
            fill: color.fill
          },
          during: function (apiDuring) {
            apiDuring.setShape(
              'points',
              makeShapePoints(
                params,
                apiDuring.getExtra('widthRadius'),
                apiDuring.getExtra('startRadius'),
                apiDuring.getExtra('endRadian')
              )
            );
          }
        });
      };

      const addLabel = (params, children, widthRadius, startRadius, endRadian, color, dataIdx) => {
        const point = makeLabelPosition(params, widthRadius, startRadius, endRadian);
        children.push({
          type: 'text',
          x: point[0],
          y: point[1],
          transition: [],
          extra: {
            startRadius: startRadius,
            endRadian: endRadian,
            widthRadius: widthRadius,
            transition: ['startRadius', 'endRadian', 'widthRadius']
          },
          style: {
            text: makeText(endRadian, dataIdx),
            fill: color.text,
            stroke: '#fff',
            lineWidth: 3,
            fontSize: 10,
            align: 'center',
            verticalAlign: 'middle',
            rich: {
              count: { fontSize: 11, fontWeight: 'bold' },
              cat: { fontSize: 10, fontWeight: 'bold' }
            }
          },
          z2: 50,
          during: function (apiDuring) {
            const endR = apiDuring.getExtra('endRadian');
            const pt = makeLabelPosition(
              params,
              apiDuring.getExtra('widthRadius'),
              apiDuring.getExtra('startRadius'),
              endR
            );
            apiDuring.setTransform('x', pt[0]).setTransform('y', pt[1]);
            apiDuring.setStyle('text', makeText(endR, dataIdx));
          }
        });
      };

      const addShapes = (params, api, children, valOnStartRadius, valOnEndRadian, color, dataIdx) => {
        const coords = api.coord([valOnStartRadius, valOnEndRadian]);
        const startRadius = coords[2];
        const endRadian = coords[3];
        const widthRadius = api.coord([_barWidthValue, 0])[2];
        addPolygon(params, children, widthRadius, startRadius, endRadian, color);
        addLabel(params, children, widthRadius, startRadius, endRadian, color, dataIdx);
      };

      const renderItem = (params, api) => {
        const children = [];
        const dataIdx = params.dataIndex;
        addShapes(
          params,
          api,
          children,
          api.value(0),
          api.value(1),
          _colors[dataIdx] || _colors[0],
          dataIdx
        );
        return {
          type: 'group',
          children: children
        };
      };

      const option = {
        animationDuration: _animationDuration,
        animationDurationUpdate: _animationDurationUpdate,
        animationEasingUpdate: _animationEasingUpdate,
        dataset: {
          source: _datasourceList[this.spiralDataIndex]
        },
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            const row = params.value;
            const catName = _barNamesByOrdinal[row[0]] || '样本';
            return `<div style="font-size:11px"><b>${catName}</b><br/>角度步进: ${row[1]}<br/>螺旋极径: ${row[0]}</div>`;
          }
        },
        legend: {
          show: true,
          top: 10,
          right: 15,
          orient: 'horizontal',
          data: [
            { name: '正常', itemStyle: { color: '#10b981' } },
            { name: '划痕', itemStyle: { color: '#f59e0b' } },
            { name: '裂痕', itemStyle: { color: '#ef4444' } }
          ],
          textStyle: { color: '#475569', fontSize: 12, fontWeight: 'bold' },
          itemWidth: 12,
          itemHeight: 12,
          icon: 'circle'
        },
        angleAxis: {
          type: 'value',
          splitArea: { show: true, areaStyle: { color: ['rgba(241, 245, 249, 0.4)', 'rgba(255, 255, 255, 0.4)'] } },
          axisLabel: {
            formatter: function (val) {
              return _radianLabels[val] || '';
            },
            color: '#94a3b8',
            fontSize: 9
          },
          axisLine: { lineStyle: { color: 'rgba(203, 213, 225, 0.6)' } },
          splitLine: { lineStyle: { color: 'rgba(226, 232, 240, 0.6)' } },
          min: 0,
          max: _valOnRoundRadian
        },
        radiusAxis: {
          type: 'value',
          interval: 1,
          splitLine: { show: false },
          axisLabel: {
            show: false
          },
          axisTick: { show: false },
          axisLine: { show: false },
          min: 0,
          max: getMaxRadius()
        },
        polar: {
          center: ['50%', '50%'],
          radius: '76%'
        },
        series: [
          {
            name: '样本螺旋分布',
            type: 'custom',
            coordinateSystem: 'polar',
            renderItem: renderItem
          }
        ]
      };

      chart.setOption(option);

      const next = () => {
        if (!chart || chart.isDisposed()) return;
        this.spiralDataIndex = (this.spiralDataIndex + 1) % _datasourceList.length;
        chart.setOption({
          dataset: {
            source: _datasourceList[this.spiralDataIndex]
          },
          radiusAxis: {
            max: getMaxRadius()
          }
        });
        this.spiralTimer = setTimeout(next, _animationDurationUpdate);
      };

      if (this.spiralTimer) clearTimeout(this.spiralTimer);
      this.spiralTimer = setTimeout(next, 1200);
    }
  }
};
</script>

<style scoped>
/* ==========================================================================
   全局样式 (Light Theme)
   ========================================================================== */
.smart-factory {
  width: 100%;
  height: calc(100vh - 60px);
  overflow: hidden;
  background-color: #f5f7fa;
  color: #303133;
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  position: absolute;
  top: 0;
  left: 0;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
/* 浅色网格背景 */
.bg-grid { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-image: linear-gradient(rgba(0, 0, 0, 0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 0, 0, 0.03) 1px, transparent 1px); background-size: 40px 40px; z-index: 0; pointer-events: none; }

.factory-header {
  position: relative;
  z-index: 100;
  padding: 8px 15px 4px 15px;
  flex-shrink: 0;
}
.header-content { display: flex; justify-content: space-between; align-items: center; padding: 6px 15px; background: #ffffff; border: 1px solid #ebeef5; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05); border-radius: 4px; transition: all 0.3s ease; }
.main-title { margin: 0; font-size: 18px; color: #303133; letter-spacing: 1px; display: flex; align-items: center; gap: 8px; font-weight: 600; }
.subtitle-icon { color: #909399; font-size: 14px; cursor: pointer; }
.header-controls { display: flex; align-items: center; gap: 12px; }
.refresh-btn { background: #fff !important; border: 1px solid #dcdfe6 !important; color: #606266 !important; width: 28px !important; height: 28px !important; border-radius: 4px !important; display: flex !important; align-items: center !important; justify-content: center !important; transition: all 0.3s ease !important; }
.refresh-btn:hover { background: #ecf5ff !important; color: #409eff !important; border-color: #c6e2ff !important; }
.time-select { width: 110px !important; }
.system-status { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #67c23a; }

/* ==========================================================
   视图1 标注系统全新 3×2 对称工控矩阵样式 (1:1 复刻设计图)
   ========================================================== */
.labeling-command-center {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  padding: 8px 16px 12px 16px;
  box-sizing: border-box;
}

/* 1. 顶部 4 个独立指标卡 */
.labeling-kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1.65fr;
  gap: 12px;
  flex-shrink: 0;
}

.kpi-metric-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 16px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.03), 0 3px 8px rgba(15, 23, 42, 0.02);
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 72px;
  box-sizing: border-box;
}

.kpi-icon-box {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  background: #f0f7ff;
  border: 1px solid #e0f2fe;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-text-block {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  flex: 1;
}

.kpi-label {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.kpi-value {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
}

.kpi-value.text-blue {
  color: #2563eb;
}

/* 卡片 2：流水线吞吐数值与流速上下列布局 */
.throughput-val-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flow-arrow-sep {
  color: #cbd5e1;
  font-size: 18px;
  font-weight: 400;
  line-height: 1;
  font-family: ui-monospace, monospace;
}

.flow-stats-col {
  display: flex;
  flex-direction: column;
  justify-content: center;
  line-height: 1.15;
}

.flow-speed-line {
  color: #16a34a;
  font-size: 13px;
  font-weight: 700;
}

.flow-desc-line {
  color: #64748b;
  font-size: 11px;
}

/* 卡片 3：达标标签与认证信息 */
.consistency-val-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge-tag-pass {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 4px;
  line-height: 1.2;
}

.kpi-compliance-note {
  font-size: 12px;
  color: #64748b;
  line-height: 1.2;
  margin-top: 2px;
}

/* 顶部卡片4：Blade Rack Cluster 刀片服务器概览卡 */
.kpi-rack-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
}

.rack-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.rack-label-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rack-main-title {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.rack-cluster-sub {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
}

.rack-info-icon {
  font-size: 12px;
  color: #94a3b8;
}

.rack-status-text {
  font-size: 11px;
  color: #059669;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.rack-status-text .green-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #059669;
  box-shadow: 0 0 5px rgba(5, 150, 105, 0.4);
}

.rack-meters-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  width: 100%;
}

.rack-meter-unit {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rack-meter-unit .m-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  line-height: 1;
}

.rack-meter-unit .m-k {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.rack-meter-unit .m-v {
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
}

.rack-meter-unit .m-bar {
  height: 7px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
}

.rack-meter-unit .m-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.rack-meter-unit .m-fill.fill-cpu { background: #0284c7; }
.rack-meter-unit .m-fill.fill-gpu { background: #8b5cf6; }
.rack-meter-unit .m-fill.fill-ram { background: #059669; }
.rack-meter-unit .m-fill.fill-nvme { background: #ea580c; }

/* 2. 主体 3×2 对称网格 */
.labeling-symmetry-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.symmetry-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  min-height: 0;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.symmetry-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.card-top-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 6px;
  margin-bottom: 4px;
  flex-shrink: 0;
}

.title-with-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title-txt {
  font-size: 12px;
  font-weight: 700;
  color: #0f172a;
}

.info-tip-icon {
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
}

.card-time-tabs {
  display: flex;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  background: #f1f5f9;
  padding: 1px;
}

.card-time-tabs button {
  background: transparent;
  border: none;
  color: #475569;
  padding: 2px 8px;
  cursor: pointer;
  font-size: 10px;
  font-weight: 600;
  transition: all 0.2s ease;
  border-radius: 2px;
}

.card-time-tabs button:hover {
  color: #0284c7;
}

.card-time-tabs button.active {
  background: #ffffff;
  color: #0284c7;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-live-badge {
  font-size: 10px;
  color: #475569;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.scope-scale-line {
  display: flex;
  justify-content: space-between;
  padding: 2px 6px;
  font-size: 9px;
  color: #94a3b8;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

.card-chart-wrap {
  flex: 1;
  min-height: 0;
  width: 100%;
  position: relative;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.card-bottom-dock {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  padding: 8px 12px;
  flex-shrink: 0;
  margin-top: 6px;
}

.dock-slot-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  flex: 1;
}

.dock-slot-item .k {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.dock-slot-item .v {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.1;
}

.dock-divider {
  width: 1px;
  height: 28px;
  background: #e2e8f0;
  flex-shrink: 0;
  margin: 0 8px;
}

/* 卡片 3 专属：三槽参数栏 (1:1 像素级复刻截图) */
.ai-stats-triple-dock {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px 12px;
  flex-shrink: 0;
  margin-top: 4px;
}

.triple-dock-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.triple-dock-divider {
  width: 1px;
  height: 22px;
  background: #e2e8f0;
  flex-shrink: 0;
}

.dock-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.dock-val {
  font-size: 14px;
  font-weight: 800;
}

.text-primary-bright {
  color: #2563eb !important;
}

.text-danger-bright {
  color: #ef4444 !important;
}

.dock-slot-item {
  display: flex;
  align-items: baseline;
  gap: 5px;
  font-size: 11px;
}

.dock-slot-item .k {
  color: #64748b;
  font-size: 10px;
}

.dock-slot-item .v {
  font-weight: 700;
}

/* 卡片 6：系统状态与快捷操作卡 (1:1 像素级复刻设计图) */
.system-action-combo-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12px;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.status-panel-upper {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  padding: 4px 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
}

.status-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 0;
  border-bottom: 1px solid #f1f5f9;
}

.status-meta-row:last-child {
  border-bottom: none;
}

.meta-label-side {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1e293b;
  font-size: 13px;
  font-weight: 500;
}

.meta-icon-svg {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  flex-shrink: 0;
}

.meta-val-side {
  display: flex;
  align-items: center;
}

/* 统一浅绿胶囊徽标 (1:1 复刻设计图) */
.badge-status-green {
  background: #ecfdf5;
  color: #16a34a;
  font-size: 12px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 6px;
  line-height: 1.2;
  display: inline-flex;
  align-items: center;
}

.badge-status-green.font-mono {
  font-weight: 600;
  font-family: ui-monospace, "SF Mono", Menlo, Consolas, monospace;
}

.meta-val-text {
  font-size: 12px;
  font-weight: 500;
  color: #334155;
}

.meta-val-text.text-muted {
  color: #64748b;
}

/* 下部：快捷操作面板 */
.action-panel-lower {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-panel-heading {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.quick-buttons-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.quick-action-btn {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 4px 10px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.02);
  transition: all 0.2s ease;
}

.quick-action-btn:hover {
  border-color: #0284c7;
  box-shadow: 0 3px 8px rgba(2, 132, 199, 0.12);
  transform: translateY(-1px);
}

.quick-btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 28px;
}

.action-text {
  font-size: 13px;
  color: #0f172a;
  font-weight: 500;
  white-space: nowrap;
}

/* 颜色工具类 */
.font-mono {
  font-family: ui-monospace, "SF Mono", Menlo, Consolas, monospace;
}

.text-primary { color: #0284c7; }
.text-success { color: #059669; }
.text-warning { color: #d97706; }
.text-danger { color: #dc2626; }
.text-dark { color: #0f172a; }
.text-secondary { color: #64748b; }
.text-muted { color: #94a3b8; }
.font-bold { font-weight: 700; }

.chart-container { width: 100%; height: 100%; }
.chart-controls-wrapper { display: flex; justify-content: flex-end; align-items: center; margin-bottom: 4px; padding: 0 2px; flex-shrink: 0; }

.custom-time-switch {
  display: flex;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  background: #f1f5f9;
  padding: 1px;
}

.custom-time-switch button {
  background: transparent;
  border: none;
  color: #475569;
  padding: 2px 8px;
  cursor: pointer;
  font-size: 10px;
  font-weight: 600;
  transition: all 0.2s ease;
  border-radius: 2px;
}

.custom-time-switch button:hover { color: #0284c7; }
.custom-time-switch button.active { background: #ffffff; color: #0284c7; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
.blink { animation: blink 2s infinite; }

/* ==========================================================================
   视图2：模型指标样式 (Light Theme)
   ========================================================================== */

.analytic-overview-wrap {
  position: absolute;
  top: 70px;
  left: 0;
  width: 100%;
  height: calc(100vh - 70px);
  background-color: transparent;
  padding: 30px 40px;
  box-sizing: border-box;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 30px;
  z-index: 50;
}

/* 顶部四色卡片 - 保持鲜艳色彩但加阴影 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
}

.stat-card {
  height: 140px;
  border-radius: 20px;
  padding: 25px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  transition: transform 0.3s;
}
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15); }

/* 渐变色 */
.pink-card { background: linear-gradient(135deg, #FF9EB3 0%, #FF758C 100%); }
.purple-card { background: linear-gradient(135deg, #D8B5FF 0%, #9E8FFF 100%); }
.green-card { background: linear-gradient(135deg, #6FD3C6 0%, #48C6B1 100%); }
.yellow-card { background: linear-gradient(135deg, #F3E66E 0%, #DEC337 100%); }

.card-text { z-index: 2; display: flex; flex-direction: column; justify-content: space-between; }
.card-label { font-size: 12px; font-weight: 600; letter-spacing: 1px; opacity: 0.9; margin-bottom: 5px; }
.card-value { font-size: 32px; font-weight: 700; margin-bottom: 5px; }
.card-sub { font-size: 12px; opacity: 0.8; }
.card-chart { position: absolute; right: 0; bottom: 0; width: 60%; height: 80px; z-index: 1; }
.card-chart.spark-f1-chart { top: 32px; bottom: 0; height: auto; }

/* 中间层 */
.middle-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
  min-height: 350px;
}

/* 白板样式 */
.white-panel {
  background: #ffffff;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}
.white-panel:hover { box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08); border-color: #dcdfe6; }

.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.panel-header h2 { font-size: 18px; color: #303133; font-weight: 600; margin: 0; display: flex; align-items: center; gap: 8px; }
.panel-header h2 i { color: #909399; font-size: 14px; cursor: pointer; }
.panel-control { font-size: 12px; color: #909399; font-weight: 600; cursor: pointer; background: #f5f7fa; padding: 5px 12px; border-radius: 15px; }

.revenue-panel .main-chart-container { flex: 1; width: 100%; min-height: 250px; }
.legend-group { display: flex; gap: 15px; font-size: 12px; color: #606266; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 5px; }
.legend-dot.income { background-color: #00b894; }
.legend-dot.expense { background-color: #0984e3; }
.legend-dot.outcome { background-color: #ff7675; }
.legend-dot.warning { background-color: #e17055; }

.status-panel .donut-chart-wrapper { position: relative; height: 200px; display: flex; justify-content: center; align-items: center; }
.donut-chart { width: 100%; height: 100%; }
.donut-center-text { position: absolute; text-align: center; pointer-events: none; }
.donut-center-text .percent { font-size: 28px; color: #00b894; font-weight: 700; }
.donut-center-text .label { font-size: 10px; color: #909399; letter-spacing: 1px; }

.status-stats { display: flex; justify-content: space-around; margin-top: 20px; }
.stat-item { text-align: center; }
.stat-item .num { font-size: 18px; font-weight: 700; color: #303133; margin-bottom: 4px; }
.stat-item .desc { font-size: 10px; color: #909399; font-weight: 600; letter-spacing: 0.5px; }

/* 底部层 */
.bottom-section {
  display: grid;
  grid-template-columns: 1.62fr 1fr;
  gap: 24px;
  flex: 1;
}

/* 类别性能详情卡片 */
.table-panel, .tracking-panel {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px 28px;
  box-shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.03);
  border: 1px solid #f1f5f9;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.panel-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  letter-spacing: -0.2px;
}

.info-icon {
  width: 17px;
  height: 17px;
  color: #94a3b8;
  cursor: pointer;
  stroke: #94a3b8;
  vertical-align: middle;
}

.pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pill-btn:hover {
  background: #e2e8f0;
}

.chevron-icon {
  width: 14px;
  height: 14px;
  stroke: #64748b;
  stroke-width: 2.5;
}

/* 表格定制 */
.custom-table {
  display: flex;
  flex-direction: column;
  width: 100%;
  flex: 1;
  justify-content: space-around;
}

.table-header {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1fr 1fr 1fr 1fr;
  padding: 12px 0 20px 0;
  color: #8a94a6;
  font-weight: 500;
  font-size: 13px;
  border-bottom: 1px solid #f8fafc;
}

.table-row {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1fr 1fr 1fr 1fr;
  padding: 24px 0;
  align-items: center;
  border-bottom: 1px solid #f8fafc;
  font-size: 14px;
  transition: background 0.2s;
}

.table-row:hover {
  background-color: #f8fafc;
}

.col-id {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #1e293b;
}

.dot-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-blue { background: #2563eb; }
.dot-purple { background: #8b5cf6; }
.dot-orange { background: #f97316; }

.col-val {
  font-weight: 700;
  color: #1e293b;
  font-size: 14px;
}

.col-status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  width: fit-content;
}

.tag-stable {
  background-color: #ecfdf5;
  color: #10b981;
}

.tag-training {
  background-color: #eff6ff;
  color: #6366f1;
}

.tag-recheck {
  background-color: #fff7ed;
  color: #f97316;
}

.table-all-link {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 6px;
}

.table-all-link span {
  font-size: 13px;
  font-weight: 600;
  color: #3b82f6;
  cursor: pointer;
  transition: opacity 0.2s;
}

.table-all-link span:hover {
  opacity: 0.8;
  text-decoration: underline;
}

/* 模型参数列表 */
.model-params-list {
  display: flex;
  flex-direction: column;
}

.param-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 0;
  border-bottom: 1px solid #f8fafc;
}

.param-item:last-child {
  border-bottom: none;
}

.param-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

.param-svg {
  width: 18px;
  height: 18px;
  color: #64748b;
  stroke: #64748b;
  flex-shrink: 0;
}

.param-val {
  color: #1e293b;
  font-weight: 700;
  font-size: 14px;
}

/* 适配 */
@media (max-width: 1400px) {
  /* 窄屏时取消绝对撑满，允许滚动 */
  .factory-layout.realtime-view { flex-direction: column; height: auto; overflow-y: auto; }
  .workshops-left, .workshops-right { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; height: auto; }
  .center-realtime-panel { order: -1; margin-bottom: 20px; height: auto; min-height: 500px; }
  .workshop { height: 350px; min-height: 350px; } /* 固定高度 */
  
  .overview-cards { grid-template-columns: 1fr 1fr; }
  .middle-section, .bottom-section { grid-template-columns: 1fr; }
}

@media (max-width: 992px) {
  .overview-cards { grid-template-columns: 1fr; }
}

/* 动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter, .fade-leave-to { opacity: 0; }
</style>
<template>
  <div class="device-management-page">
    <!-- 顶部状态栏与指标卡 -->
    <header class="overview-header">
      <div class="header-main-info">
        <div class="title-row">
          <h2 class="section-title">车间工控设备与传感器管理</h2>
          <span class="network-badge">
            <span class="indicator-dot"></span>
            PROFINET / Modbus 总线正常
          </span>
        </div>
        <p class="section-subtitle">监控与管理产线视觉采集相机、边缘算力服务器、协作机械臂与 AGV 物流底盘实时通信及工况</p>
      </div>

      <div class="header-action-group">
        <el-button size="small" icon="el-icon-refresh" :loading="refreshing" @click="handleRefreshAll">
          全网拓扑巡检
        </el-button>
        <el-button size="small" type="primary" icon="el-icon-plus" @click="handleAddDeviceDialog">
          添加受控节点
        </el-button>
      </div>
    </header>

    <!-- 核心运行指标 KPI 矩阵 -->
    <section class="kpi-metrics-row">
      <div class="metric-card">
        <div class="metric-label">受控设备总数</div>
        <div class="metric-value font-mono">
          <span class="num">4</span>
          <span class="unit">台</span>
        </div>
        <div class="metric-sub">
          <span class="status-pill success">100% 在线</span>
          <span class="sub-text">0 告警 / 0 离线</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-label">平均通信延迟 (RTT)</div>
        <div class="metric-value font-mono">
          <span class="num">0.82</span>
          <span class="unit">ms</span>
        </div>
        <div class="metric-sub">
          <span class="status-pill info">微秒级</span>
          <span class="sub-text">工业以太网专网</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-label">总线采样吞吐量</div>
        <div class="metric-value font-mono">
          <span class="num">1,240</span>
          <span class="unit">fps/s</span>
        </div>
        <div class="metric-sub">
          <span class="status-pill success">带宽 42%</span>
          <span class="sub-text">1000M Full-Duplex</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-label">工控主时钟同步</div>
        <div class="metric-value font-mono">
          <span class="num text-mono">{{ currentTimeStr }}</span>
        </div>
        <div class="metric-sub">
          <span class="status-pill success">IEEE 1588</span>
          <span class="sub-text">PTP 纳秒同步就绪</span>
        </div>
      </div>
    </section>

    <!-- 主体区域：左侧设备卡片拓扑，右侧设备深度测控工作台 -->
    <div class="workbench-layout" :class="{ 'is-server-layout': currentDev.type === 'server' }">
      <!-- 左侧：受控设备列表 -->
      <aside class="device-list-column">
        <div class="column-panel">
          <div class="column-panel-header">
            <div class="header-text">
              <span class="panel-name">生产线硬件节点</span>
              <span class="panel-count font-mono">{{ devices.length }} 节点</span>
            </div>
            <el-select v-model="filterType" size="mini" placeholder="设备类型" style="width: 110px;">
              <el-option label="全部类型" value=""></el-option>
              <el-option label="算力服务器" value="server"></el-option>
              <el-option label="工业相机" value="camera"></el-option>
              <el-option label="协作机械臂" value="arm"></el-option>
              <el-option label="AGV物流车" value="agv"></el-option>
            </el-select>
          </div>

          <div class="device-card-list">
            <div
              v-for="item in filteredDevices"
              :key="item.id"
              class="device-card-item"
              :class="{ 'is-selected': currentDevId === item.id }"
              @click="handleSelectDevice(item.id)"
            >
              <div class="card-left-icon" :class="'type-' + item.type">
                <i :class="item.icon"></i>
              </div>

              <div class="card-center-body">
                <div class="card-top-row">
                  <span class="device-name">{{ item.name }}</span>
                  <span class="online-tag">
                    <span class="tag-dot"></span>在线
                  </span>
                </div>
                <div class="card-meta-row font-mono">
                  <span class="meta-ip">{{ item.ip }}:{{ item.port }}</span>
                  <span class="meta-proto">{{ item.protocol }}</span>
                </div>
                <div class="card-kpi-row">
                  <span class="kpi-name">{{ item.primaryMetricName }}:</span>
                  <span class="kpi-value font-mono">{{ item.primaryMetricVal }}</span>
                </div>
              </div>

              <div class="card-arrow">
                <i class="el-icon-arrow-right"></i>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 【服务器专属视图】：图四高颜值运行总览看板 + 5系列时序趋势 + 4-Tab控制参数面板 -->
      <template v-if="currentDev.type === 'server'">
        <!-- 第二列：设备运行总览 + 实时运行趋势 -->
        <section class="device-middle-column">
          <!-- 顶部主卡片：设备运行总览 (包含头部信息、健康度圆环指标与 2×3 迷你趋势卡片) -->
          <div class="detail-panel overview-main-card">
            <!-- 头部标题栏 -->
            <div class="overview-header-bar">
              <div class="overview-title-text">设备运行总览</div>
              <div class="overview-health-tag">
                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="#059669" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="13" r="7"></circle>
                  <line x1="12" y1="2" x2="12" y2="6"></line>
                  <circle cx="12" cy="2" r="1.2" fill="#059669"></circle>
                </svg>
                <span>设备健康度</span>
              </div>
            </div>

            <!-- 设备主要信息与健康度评分区 -->
            <div class="overview-hero-section">
              <div class="dev-hero-left">
                <div class="dev-hero-icon type-server">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="6" rx="2"></rect>
                    <rect x="3" y="14" width="18" height="6" rx="2"></rect>
                    <line x1="7" y1="7" x2="7.01" y2="7"></line>
                    <line x1="7" y1="17" x2="7.01" y2="17"></line>
                    <line x1="13" y1="7" x2="17" y2="7"></line>
                    <line x1="13" y1="17" x2="17" y2="17"></line>
                  </svg>
                </div>
                <div class="dev-hero-info">
                  <div class="hero-name-row">
                    <h3 class="hero-dev-name">{{ currentDev.name }}</h3>
                    <span class="hero-sn-badge font-mono">{{ currentDev.type === 'server' ? serverMonitor.hostname : currentDev.sn }}</span>
                    <div class="hero-status-tag">
                      <span class="status-dot-green"></span>
                      <span class="status-txt-green">在线</span>
                    </div>
                  </div>
                  <div class="hero-meta-row font-mono">
                    <span>IP: {{ currentDev.ip }}</span>
                    <span class="sep">|</span>
                    <span>Port: {{ currentDev.port }}</span>
                    <span class="sep">|</span>
                    <span>协议: {{ currentDev.protocol }}</span>
                  </div>
                </div>
              </div>

              <!-- 右侧：健康度大圆环 + 运行时长/启动时间/固件版本 -->
              <div class="dev-hero-right">
                <div class="health-ring-container">
                  <div class="health-ring-circle">
                    <svg viewBox="0 0 100 100" class="ring-svg">
                      <circle cx="50" cy="50" r="42" stroke="#e2e8f0" stroke-width="7.5" fill="transparent"></circle>
                      <circle
                        cx="50" cy="50" r="42"
                        stroke="#059669" stroke-width="7.5" fill="transparent"
                        stroke-dasharray="264"
                        :stroke-dashoffset="264 - (264 * 0.95)"
                        stroke-linecap="round"
                        transform="rotate(-90 50 50)"
                      ></circle>
                    </svg>
                    <div class="ring-content">
                      <span class="ring-score font-mono">95</span>
                      <span class="ring-label">健康度</span>
                    </div>
                  </div>
                </div>

                <div class="health-meta-list font-mono">
                  <div class="health-meta-item">
                    <span class="meta-label">运行时长</span>
                    <span class="meta-value">{{ serverMonitor.uptime }}</span>
                  </div>
                  <div class="health-meta-item">
                    <span class="meta-label">启动时间</span>
                    <span class="meta-value">{{ serverMonitor.startTime }}</span>
                  </div>
                  <div class="health-meta-item">
                    <span class="meta-label">固件版本</span>
                    <span class="meta-value">{{ serverMonitor.version }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2×3 实时工况指标迷你趋势卡片网格 -->
            <div class="telemetry-mini-grid">
              <!-- 1. CPU 综合负荷 -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box blue"><i class="el-icon-cpu"></i></div>
                  <span class="mini-title">CPU 综合负荷</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.cpuUsage }} %</div>
                <div class="mini-sparkline blue">
                  <svg viewBox="0 0 120 20" preserveAspectRatio="none">
                    <defs>
                      <linearGradient id="cpuWaveGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stop-color="#2563eb" stop-opacity="0.18" />
                        <stop offset="100%" stop-color="#2563eb" stop-opacity="0.0" />
                      </linearGradient>
                    </defs>
                    <path d="M0,14 Q10,7 20,14 T40,14 T60,14 T80,14 T100,14 T120,14 L120,20 L0,20 Z" fill="url(#cpuWaveGrad)"></path>
                    <path d="M0,14 Q10,7 20,14 T40,14 T60,14 T80,14 T100,14 T120,14" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round"></path>
                  </svg>
                </div>
              </div>

              <!-- 2. 物理核心/线程 -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box blue">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="8" y1="17" x2="8" y2="13"></line>
                      <line x1="12" y1="17" x2="12" y2="9"></line>
                      <line x1="16" y1="17" x2="16" y2="11"></line>
                    </svg>
                  </div>
                  <span class="mini-title">物理核心 / 线程</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.cores }} / {{ serverMonitor.threads }}</div>
                <div class="mini-card-sub">
                  <span class="dot-triple">●●●</span>
                  <span>{{ serverMonitor.cpuModel }}</span>
                </div>
              </div>

              <!-- 3. 内存占用率 -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box blue">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="5" r="2.5"></circle>
                      <circle cx="6" cy="19" r="2.5"></circle>
                      <circle cx="18" cy="19" r="2.5"></circle>
                      <path d="M12 7.5v4.5m0 0l-6 4.5m6-4.5l6 4.5"></path>
                    </svg>
                  </div>
                  <span class="mini-title">内存占用率</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.memUsed }} / {{ serverMonitor.memTotal }} GB</div>
                <div class="mini-card-footnote">{{ serverMonitor.memUsage }}%</div>
                <div class="mini-progress-wrap">
                  <div class="mini-progress-bar" :style="{ width: Math.min(100, Math.max(0, serverMonitor.memUsage)) + '%' }"></div>
                </div>
              </div>

              <!-- 4. GPU / AI 推理负载 -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box purple">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#7c3aed" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="4" y="4" width="16" height="16" rx="2"></rect>
                      <rect x="9" y="9" width="6" height="6"></rect>
                      <line x1="9" y1="1" x2="9" y2="4"></line>
                      <line x1="15" y1="1" x2="15" y2="4"></line>
                      <line x1="9" y1="20" x2="9" y2="23"></line>
                      <line x1="15" y1="20" x2="15" y2="23"></line>
                      <line x1="20" y1="9" x2="23" y2="9"></line>
                      <line x1="20" y1="15" x2="23" y2="15"></line>
                      <line x1="1" y1="9" x2="4" y2="9"></line>
                      <line x1="1" y1="15" x2="4" y2="15"></line>
                    </svg>
                  </div>
                  <span class="mini-title">GPU / AI 推理负载</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.gpuUsage }} %</div>
                <div class="mini-card-footnote">{{ serverMonitor.gpuModel }}</div>
                <div class="mini-progress-wrap purple">
                  <div class="mini-progress-bar" :style="{ width: Math.min(100, Math.max(0, serverMonitor.gpuUsage)) + '%' }"></div>
                </div>
              </div>

              <!-- 5. 磁盘使用率 -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box blue">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="22" y1="12" x2="2" y2="12"></line>
                      <path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"></path>
                      <line x1="6" y1="16" x2="6.01" y2="16"></line>
                      <line x1="10" y1="16" x2="10.01" y2="16"></line>
                    </svg>
                  </div>
                  <span class="mini-title">磁盘使用率</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.diskUsed }} / {{ serverMonitor.diskTotal }} GB</div>
                <div class="mini-card-footnote">{{ serverMonitor.diskUsage }}%</div>
                <div class="mini-progress-wrap blue">
                  <div class="mini-progress-bar" :style="{ width: Math.min(100, Math.max(0, serverMonitor.diskUsage)) + '%' }"></div>
                </div>
              </div>

              <!-- 6. 网络延迟 (RTT) -->
              <div class="mini-telemetry-card">
                <div class="mini-card-head">
                  <div class="mini-icon-box blue">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="9"></circle>
                      <path d="M3.6 9h16.8"></path>
                      <path d="M3.6 15h16.8"></path>
                      <path d="M11.5 3a17 17 0 0 0 0 18"></path>
                      <path d="M12.5 3a17 17 0 0 1 0 18"></path>
                    </svg>
                  </div>
                  <span class="mini-title">网络吞吐 / 延迟</span>
                </div>
                <div class="mini-card-val font-mono">{{ serverMonitor.netLatency }}</div>
                <div class="mini-card-footnote status-blue">{{ serverMonitor.netStatus }}</div>
                <div class="mini-progress-wrap blue">
                  <div class="mini-progress-bar" style="width: 20%;"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 下方：实时运行趋势卡片 (多系列面积折线图) -->
          <div class="split-col-chart detail-panel">
            <div class="trend-card-header">
              <div class="trend-title">实时运行趋势</div>
              <div class="trend-controls">
                <div class="time-range-group">
                  <span
                    class="time-btn"
                    :class="{ 'is-active': activeTimeRange === '1h' }"
                    @click="activeTimeRange = '1h'"
                  >1 小时</span>
                  <span
                    class="time-btn"
                    :class="{ 'is-active': activeTimeRange === '6h' }"
                    @click="activeTimeRange = '6h'"
                  >6 小时</span>
                  <span
                    class="time-btn"
                    :class="{ 'is-active': activeTimeRange === '24h' }"
                    @click="activeTimeRange = '24h'"
                  >24 小时</span>
                  <span
                    class="time-btn"
                    :class="{ 'is-active': activeTimeRange === '7d' }"
                    @click="activeTimeRange = '7d'"
                  >7 天</span>
                </div>
                <button class="icon-expand-btn" title="全屏查看">
                  <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="#64748b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="15 3 21 3 21 9"></polyline>
                    <polyline points="9 21 3 21 3 15"></polyline>
                    <line x1="21" y1="3" x2="14" y2="10"></line>
                    <line x1="3" y1="21" x2="10" y2="14"></line>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 图表图例栏 -->
            <div class="trend-legend-row">
              <span class="legend-item"><span class="legend-line blue"></span>CPU 负荷 (%)</span>
              <span class="legend-item"><span class="legend-line green"></span>内存占用 (%)</span>
              <span class="legend-item"><span class="legend-line purple"></span>AI 推理负载 (%)</span>
              <span class="legend-item"><span class="legend-line magenta"></span>网络延迟 (ms)</span>
              <span class="legend-item"><span class="legend-line orange"></span>采集频率 (fps)</span>
            </div>

            <!-- 折线图 DOM -->
            <div class="trend-chart-container">
              <div :id="chartId" class="echarts-trend-dom"></div>
            </div>
          </div>
        </section>

        <!-- 第三列：右侧在线控制与参数面板 -->
        <aside class="device-right-column">
          <div class="split-col-tuner detail-panel">
            <div class="tuner-header-bar">
              <div class="tuner-title">在线控制与参数 ({{ currentDev.name }})</div>
              <div class="tuner-sync-btn" @click="handleRefreshAll">
                <i class="el-icon-refresh"></i>
                <span>参数同步</span>
              </div>
            </div>

            <!-- 顶部控制 Tab 栏 (推理参数 | 采集参数 | 设备参数 | 网络参数) -->
            <div class="tuner-nav-tabs">
              <span
                class="tab-item"
                :class="{ 'is-active': activeParamTab === 'inference' }"
                @click="activeParamTab = 'inference'"
              >推理参数</span>
              <span
                class="tab-item"
                :class="{ 'is-active': activeParamTab === 'capture' }"
                @click="activeParamTab = 'capture'"
              >采集参数</span>
              <span
                class="tab-item"
                :class="{ 'is-active': activeParamTab === 'device' }"
                @click="activeParamTab = 'device'"
              >设备参数</span>
              <span
                class="tab-item"
                :class="{ 'is-active': activeParamTab === 'network' }"
                @click="activeParamTab = 'network'"
              >网络参数</span>
            </div>

            <div class="tuner-scroll-body">
              <!-- 推理参数表单区 -->
              <div class="param-form-section">
                <!-- 推理引擎选择 -->
                <div class="control-field-row">
                  <label class="field-label">推理引擎</label>
                  <div class="custom-pill-group">
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.engine === 'FP16' }"
                      @click="editParams.engine = 'FP16'"
                    >
                      <svg v-if="editParams.engine === 'FP16'" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>FP16 (推荐)</span>
                    </div>
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.engine === 'INT8' }"
                      @click="editParams.engine = 'INT8'"
                    >
                      <svg v-if="editParams.engine === 'INT8'" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>INT8 (极速)</span>
                    </div>
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.engine === 'FP32' }"
                      @click="editParams.engine = 'FP32'"
                    >
                      <svg v-if="editParams.engine === 'FP32'" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>FP32</span>
                    </div>
                  </div>
                </div>

                <!-- Batch Size -->
                <div class="control-field-row">
                  <label class="field-label">Batch Size</label>
                  <div class="custom-pill-group">
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.batchSize === 1 }"
                      @click="editParams.batchSize = 1"
                    >
                      <svg v-if="editParams.batchSize === 1" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>1 (低延迟)</span>
                    </div>
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.batchSize === 2 }"
                      @click="editParams.batchSize = 2"
                    >
                      <svg v-if="editParams.batchSize === 2" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>2 (均衡)</span>
                    </div>
                    <div
                      class="custom-pill-btn"
                      :class="{ 'is-selected': editParams.batchSize === 4 }"
                      @click="editParams.batchSize = 4"
                    >
                      <svg v-if="editParams.batchSize === 4" viewBox="0 0 16 16" width="13" height="13" class="pill-radio-dot">
                        <circle cx="8" cy="8" r="6" stroke="#2563eb" stroke-width="1.8" fill="#ffffff" />
                        <circle cx="8" cy="8" r="3" fill="#2563eb" />
                      </svg>
                      <span>4 (高吞吐)</span>
                    </div>
                  </div>
                </div>

                <!-- TensorRT 加速精度 下拉选择 -->
                <div class="control-field-row">
                  <label class="field-label">TensorRT 加速精度</label>
                  <el-select v-model="editParams.precision" size="small" style="width: 100%;">
                    <el-option label="FP16 (推荐)" value="FP16"></el-option>
                    <el-option label="INT8 (极速)" value="INT8"></el-option>
                    <el-option label="FP32 (高精度)" value="FP32"></el-option>
                  </el-select>
                </div>

                <!-- 滑块项 1: 图像预处理线程池数 -->
                <div class="control-slider-row">
                  <span class="slider-name">图像预处理线程池数</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.threads" :min="2" :max="32" :step="2"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.threads || 8 }}</span>
                  </div>
                </div>

                <!-- 滑块项 2: 最大并发推理请求数 -->
                <div class="control-slider-row">
                  <span class="slider-name">最大并发推理请求数</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.maxConcurrent" :min="4" :max="64" :step="4"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.maxConcurrent || 16 }}</span>
                  </div>
                </div>

                <!-- 滑块项 3: 推理超时时间 (ms) -->
                <div class="control-slider-row">
                  <span class="slider-name">推理超时时间 (ms)</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.timeout" :min="500" :max="5000" :step="100"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.timeout || 1500 }}</span>
                  </div>
                </div>
              </div>

              <!-- 分割线 -->
              <div class="param-section-divider"></div>

              <!-- 采集参数 (工业相机) 分组 -->
              <div class="param-form-section">
                <div class="section-sub-heading">采集参数 (工业相机)</div>

                <!-- 滑块项 4: 采集频率 (FPS) -->
                <div class="control-slider-row">
                  <span class="slider-name">采集频率 (FPS)</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.fps" :min="10" :max="120" :step="0.1"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.fps || 80.1 }}</span>
                  </div>
                </div>

                <!-- 滑块项 5: 曝光时间 (us) -->
                <div class="control-slider-row">
                  <span class="slider-name">曝光时间 (us)</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.exposure" :min="100" :max="5000" :step="50"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.exposure || 850 }}</span>
                  </div>
                </div>

                <!-- 滑块项 6: 增益 (dB) -->
                <div class="control-slider-row">
                  <span class="slider-name">增益 (dB)</span>
                  <div class="slider-line-wrap">
                    <el-slider v-model="editParams.gain" :min="0" :max="24" :step="1"></el-slider>
                    <span class="slider-num font-mono">{{ editParams.gain || 12 }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部操作按钮：应用并下发 + 重置参数 -->
            <div class="tuner-footer-actions">
              <el-button
                type="primary"
                size="medium"
                icon="el-icon-position"
                :loading="saving"
                class="btn-apply-submit"
                @click="handleSaveParams"
              >应用并下发</el-button>
              <el-button
                size="medium"
                icon="el-icon-refresh-left"
                class="btn-reset-param"
                @click="handleResetParams"
              >重置参数</el-button>
            </div>
          </div>
        </aside>
      </template>

      <!-- 原有其它设备视图 (相机、机械臂、AGV) 保持 100% 原始逻辑与真实接口 -->
      <main v-else class="device-detail-column">
        <!-- 节点概览与快速操作栏 -->
        <div class="detail-panel">
          <div class="detail-header-bar">
            <div class="dev-main-title">
              <div class="title-icon-box" :class="'type-' + currentDev.type">
                <i :class="currentDev.icon"></i>
              </div>
              <div class="title-text-group">
                <div class="name-row">
                  <h3 class="current-dev-name">{{ currentDev.name }}</h3>
                  <el-tag size="mini" type="info" class="font-mono">{{ currentDev.sn }}</el-tag>
                  <el-tag size="mini" type="success">通讯正常</el-tag>
                </div>
                <div class="desc-row font-mono">
                  <span>IP: {{ currentDev.ip }}</span>
                  <span class="sep">|</span>
                  <span>Port: {{ currentDev.port }}</span>
                  <span class="sep">|</span>
                  <span>协议: {{ currentDev.protocol }}</span>
                  <template v-if="currentDev.vendor">
                    <span class="sep">|</span>
                    <span>厂商: {{ currentDev.vendor }}</span>
                  </template>
                </div>
              </div>
            </div>

            <div class="dev-actions-group">
              <el-button size="small" icon="el-icon-connection" @click="handlePingNode">链路探测 (Ping)</el-button>
              <el-button size="small" icon="el-icon-refresh-right" @click="handleRestartDriver">重启驱动</el-button>
              <el-button size="small" icon="el-icon-odometer" type="primary" plain @click="handleSelfTest">设备自检</el-button>
            </div>
          </div>

          <!-- 2×3 实时工况遥测指标网格 -->
          <div class="telemetry-grid">
            <div
              v-for="(item, idx) in currentDevMetrics"
              :key="idx"
              class="telemetry-grid-cell"
              :class="{ 'is-highlight': item.highlight }"
            >
              <div class="cell-head">
                <span class="cell-label">{{ item.label }}</span>
                <span v-if="item.tag" class="cell-tag">{{ item.tag }}</span>
              </div>
              <div class="cell-value font-mono">{{ item.value }}</div>
              <div class="cell-foot">
                <el-progress
                  v-if="item.percent !== undefined"
                  :percentage="item.percent"
                  :show-text="false"
                  :stroke-width="5"
                  :color="getProgressColor(item.percent)"
                ></el-progress>
                <span v-else class="cell-remark">{{ item.sub || '状态正常，处于标称区间' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 示波器波形与工控参数热调分栏 (AGV 小车时左侧为实时状态 12 项网格) -->
        <div class="split-control-row">
          <!-- 实时时序波形监视器 (非 AGV 设备展示示波器图表) -->
          <div v-if="currentDev.type !== 'agv'" key="chart-panel" class="split-col-chart detail-panel">
            <div class="panel-section-title">
              <div class="t-left">
                <i class="el-icon-data-line"></i>
                <span>实时遥测采样示波器 (Live Oscilloscope)</span>
              </div>
              <span class="sample-badge font-mono">采样率: 3.0s/次</span>
            </div>
            <div class="chart-wrapper">
              <div :id="chartId" class="echarts-dom"></div>
            </div>
          </div>

          <!-- AGV 模式：左侧替换为 12 项实时状态网格 (删除折线图，保留原始深色工控风 12 个表格项目) -->
          <div v-else key="agv-status-panel" class="split-col-chart detail-panel">
            <div class="panel-section-title">
              <div class="t-left">
                <i class="el-icon-odometer" style="color: #0284c7;"></i>
                <span>实时状态</span>
              </div>
              <el-button
                size="mini"
                icon="el-icon-refresh"
                :disabled="!agvConnected"
                @click="queryAgvStatus"
              >主动查询</el-button>
            </div>
            <div style="padding: 14px; display: flex; flex-direction: column; gap: 12px;">
              <div key="agv-grid" class="agv-status-grid">
                <div class="agv-status-item"><span class="k">电量</span><span class="v">{{ agvView.battery }}</span></div>
                <div class="agv-status-item"><span class="k">实时速度</span><span class="v">{{ agvView.realSpeed }}</span></div>
                <div class="agv-status-item"><span class="k">设定速度</span><span class="v">{{ agvView.setSpeed }}</span></div>
                <div class="agv-status-item"><span class="k">当前站点</span><span class="v">{{ agvView.currentStation }}</span></div>
                <div class="agv-status-item"><span class="k">目标站点</span><span class="v">{{ agvView.targetStation }}</span></div>
                <div class="agv-status-item"><span class="k">运行模式</span><span class="v">{{ agvView.mode }}</span></div>
                <div class="agv-status-item"><span class="k">运行状态</span><span class="v" :class="agvView.runClass">{{ agvView.run }}</span></div>
                <div class="agv-status-item"><span class="k">到位停止</span><span class="v">{{ agvView.arriveStop }}</span></div>
                <div class="agv-status-item"><span class="k">障碍停止</span><span class="v">{{ agvView.obstacleStop }}</span></div>
                <div class="agv-status-item"><span class="k">指令停止</span><span class="v">{{ agvView.cmdStop }}</span></div>
                <div class="agv-status-item"><span class="k">充电状态</span><span class="v">{{ agvView.charging }}</span></div>
                <div class="agv-status-item"><span class="k">更新时间</span><span class="v">{{ agvLastUpdate || '--' }}</span></div>
              </div>

              <!-- AGV 控制 (从右侧迁移至左侧底部空白区) -->
              <div class="direct-control-block" style="margin-bottom: 0;">
                <div class="block-title">
                  <span class="title-text">AGV 控制</span>
                  <el-tag :type="agvModeTagType" size="mini">{{ agvModeText }}</el-tag>
                </div>
                <div class="btn-group-grid">
                  <el-button
                    type="danger"
                    size="mini"
                    :disabled="!agvConnected"
                    @click="agvEmergencyStop"
                  >急停</el-button>
                  <el-button
                    type="warning"
                    size="mini"
                    :disabled="!agvConnected"
                    @click="agvReset"
                  >复位（回1号站）</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 工控运行参数在线配置面板 -->
          <div class="split-col-tuner detail-panel">
            <div class="panel-section-title">
              <div class="t-left">
                <i class="el-icon-setting"></i>
                <span>工控参数在线标定与热下发</span>
              </div>
              <el-tag size="mini" type="success" effect="plain">即时生效</el-tag>
            </div>

            <div class="param-form-container">
              <!-- 服务器参数 -->
              <div v-if="currentDev.type === 'server'" class="form-body">
                <div class="form-row">
                  <label class="form-label">推理并发 Batch Size</label>
                  <el-radio-group v-model="editParams.batchSize" size="small">
                    <el-radio-button :label="1">1 (低延迟)</el-radio-button>
                    <el-radio-button :label="2">2 (均衡)</el-radio-button>
                    <el-radio-button :label="4">4 (高吞吐)</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="form-row">
                  <label class="form-label">TensorRT 加速精度</label>
                  <el-radio-group v-model="editParams.precision" size="small">
                    <el-radio-button label="FP16">FP16 (推荐)</el-radio-button>
                    <el-radio-button label="INT8">INT8 (极速)</el-radio-button>
                    <el-radio-button label="FP32">FP32</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="form-row form-slider">
                  <div class="slider-title">
                    <span class="form-label">图像预处理线程池数</span>
                    <span class="slider-val font-mono">{{ editParams.threads }} 线程</span>
                  </div>
                  <el-slider v-model="editParams.threads" :min="4" :max="40" :step="2"></el-slider>
                </div>
              </div>

              <!-- 相机参数 -->
              <div v-else-if="currentDev.type === 'camera'" class="form-body">
                <div class="form-row form-slider">
                  <div class="slider-title">
                    <span class="form-label">曝光时间 (Exposure Time)</span>
                    <span class="slider-val font-mono">{{ editParams.exposure }} μs</span>
                  </div>
                  <el-slider v-model="editParams.exposure" :min="500" :max="10000" :step="100"></el-slider>
                </div>
                <div class="form-row form-slider">
                  <div class="slider-title">
                    <span class="form-label">模拟增益 (Analog Gain)</span>
                    <span class="slider-val font-mono">{{ editParams.gain }} dB</span>
                  </div>
                  <el-slider v-model="editParams.gain" :min="0" :max="20" :step="0.5"></el-slider>
                </div>
                <div class="form-row">
                  <label class="form-label">快门触发源 (Trigger)</label>
                  <el-radio-group v-model="editParams.trigger" size="small">
                    <el-radio-button label="line1">Line 1 硬件光电</el-radio-button>
                    <el-radio-button label="soft">软件软触发</el-radio-button>
                    <el-radio-button label="cont">自由连续采集</el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <!-- 机械臂直接控制与参数配置 -->
              <div v-else-if="currentDev.type === 'arm'" class="form-body">
                <div class="direct-control-block">
                  <div class="block-title">
                    <span class="title-text"><i class="el-icon-video-play"></i> 机械臂实控动作</span>
                    <el-tag :type="robotConnected ? 'success' : 'info'" size="mini">
                      {{ robotConnected ? '已连接' : '未连接' }}
                    </el-tag>
                  </div>
                  <div class="btn-group-grid">
                    <el-button
                      v-if="!robotConnected"
                      type="primary"
                      size="mini"
                      icon="el-icon-link"
                      :loading="robotConnecting"
                      @click="connectRobot"
                    >连接</el-button>
                    <el-button
                      v-else
                      type="danger"
                      size="mini"
                      icon="el-icon-switch-button"
                      @click="disconnectRobot"
                    >断开</el-button>
                    <el-button
                      size="mini"
                      type="success"
                      :disabled="!robotConnected"
                      @click="setDO(0, true)"
                    >DO0开</el-button>
                    <el-button
                      size="mini"
                      type="warning"
                      :disabled="!robotConnected"
                      @click="setDO(0, false)"
                    >DO0关</el-button>
                    <el-button
                      size="mini"
                      :disabled="!robotConnected"
                      @click="robotMoveHome"
                    >回原位</el-button>
                    <el-button
                      size="mini"
                      :disabled="!robotConnected"
                      @click="robotMovePhoto"
                    >拍照位</el-button>
                    <el-button
                      size="mini"
                      type="danger"
                      :disabled="!robotConnected"
                      @click="robotStop"
                    >急停</el-button>
                  </div>
                </div>

                <div class="form-row form-slider">
                  <div class="slider-title">
                    <span class="form-label">伺服运行倍率 (Speed Ratio)</span>
                    <span class="slider-val font-mono">{{ editParams.speed || 60 }} %</span>
                  </div>
                  <el-slider v-model="editParams.speed" :min="10" :max="100" :step="5"></el-slider>
                </div>

                <div class="form-row">
                  <label class="form-label">安全碰撞检测灵敏度</label>
                  <el-radio-group v-model="editParams.collision" size="small">
                    <el-radio-button label="low">低 (重载)</el-radio-button>
                    <el-radio-button label="medium">标准 (推荐)</el-radio-button>
                    <el-radio-button label="high">高 (高灵敏)</el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <!-- AGV 直接控制与实时状态 -->
              <div v-else-if="currentDev.type === 'agv'" class="form-body">
                <!-- 串口连接 -->
                <div class="direct-control-block">
                  <div class="block-title">
                    <span class="title-text">串口连接</span>
                    <el-tag :type="agvConnected ? 'success' : 'info'" size="mini">
                      {{ agvConnected ? '已连接' : '未连接' }}
                    </el-tag>
                  </div>
                  <div class="control-subrow">
                    <el-select
                      v-model="agvPortName"
                      placeholder="选择串口"
                      size="mini"
                      style="width: 150px;"
                      :disabled="agvConnected"
                    >
                      <el-option v-for="p in agvPorts" :key="p.name" :label="p.name" :value="p.name">
                        <span>{{ p.name }}</span>
                        <span style="float: right; color: #909399; font-size: 11px;">{{ p.description }}</span>
                      </el-option>
                    </el-select>
                    <el-button
                      size="mini"
                      icon="el-icon-refresh"
                      circle
                      :disabled="agvConnected"
                      @click="loadAgvPorts"
                    ></el-button>
                    <el-button
                      v-if="!agvConnected"
                      type="primary"
                      size="mini"
                      icon="el-icon-link"
                      :loading="agvConnecting"
                      @click="connectAgv"
                    >连接</el-button>
                    <el-button
                      v-else
                      type="danger"
                      size="mini"
                      icon="el-icon-switch-button"
                      @click="disconnectAgv"
                    >断开</el-button>
                  </div>
                  <div class="control-tip-line">
                    <span class="tip-label">波特率:</span>
                    <span class="tip-value">9600，8 数据位，1 停止位，无校验（后端托管串口）</span>
                  </div>
                  <div class="control-subrow" style="margin-top: 6px;">
                    <span class="tip-label" style="width: 58px;">检测结果:</span>
                    <el-select v-model="inspectResult" size="mini" style="width: 150px;">
                      <el-option label="合格（走合格线）" value="ok"></el-option>
                      <el-option label="划痕（走划痕线）" value="scratch"></el-option>
                      <el-option label="裂痕（走裂痕线）" value="crack"></el-option>
                    </el-select>
                    <span class="tip-desc">分拣时 AGV 按此结果行驶到对应路线（模拟）</span>
                  </div>
                  <div class="control-subrow" style="margin-top: 6px;">
                    <span class="tip-label" style="width: 58px;">站号配置:</span>
                    <span class="cfg-text">上料区</span>
                    <el-input-number v-model="stationLoading" :min="1" :max="255" size="mini" style="width: 80px;"></el-input-number>
                    <span class="cfg-text" style="margin-left: 8px;">检测区</span>
                    <el-input-number v-model="stationDetect" :min="1" :max="255" size="mini" style="width: 80px;"></el-input-number>
                  </div>
                </div>

                <!-- 自动工作流控制 -->
                <div class="direct-control-block">
                  <div class="block-title">
                    <span class="title-text">自动工作流</span>
                    <el-tag :type="workflowTagType" size="mini">{{ workflowStateText }}</el-tag>
                  </div>
                  <div class="workflow-flow-desc">
                    AGV→6号站 → 发信号→机械臂 → 完成信号→AGV→3号站
                  </div>
                  <div class="btn-group-grid" style="margin-top: 8px;">
                    <el-button
                      type="success"
                      size="mini"
                      :disabled="!canStartWorkflow"
                      @click="startWorkflow"
                    >启动</el-button>
                    <el-button
                      type="warning"
                      size="mini"
                      :disabled="workflowState !== 'IDLE' && workflowState !== 'COMPLETED' && workflowState !== 'ERROR'"
                      @click="stopWorkflow"
                    >停止</el-button>
                    <el-button
                      size="mini"
                      @click="resetWorkflow"
                    >重置</el-button>
                  </div>
                </div>
              </div>

              <div v-if="currentDev.type === 'server' || currentDev.type === 'camera' || currentDev.type === 'arm'" class="form-actions">
                <el-button type="primary" size="small" icon="el-icon-check" :loading="saving" @click="handleSaveParams">
                  保存并写入寄存器
                </el-button>
                <el-button size="small" icon="el-icon-refresh-left" @click="handleResetParams">
                  重置参数
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 添加设备弹窗 -->
    <el-dialog title="添加产线受控节点" :visible.sync="addDialogVisible" width="500px">
      <el-form :model="newDevForm" label-width="100px" size="small">
        <el-form-item label="设备名称">
          <el-input v-model="newDevForm.name" placeholder="例如：2号工位工业相机"></el-input>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="newDevForm.type" style="width:100%">
            <el-option label="工业相机" value="camera"></el-option>
            <el-option label="边缘算力服务器" value="server"></el-option>
            <el-option label="协作机械臂" value="arm"></el-option>
            <el-option label="AGV搬运底盘" value="agv"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通信IP地址">
          <el-input v-model="newDevForm.ip" placeholder="192.168.1.xxx"></el-input>
        </el-form-item>
        <el-form-item label="端口号">
          <el-input v-model="newDevForm.port" placeholder="例如：502 / 3956"></el-input>
        </el-form-item>
        <el-form-item label="通信协议">
          <el-select v-model="newDevForm.protocol" style="width:100%">
            <el-option label="GigE Vision" value="GigE Vision"></el-option>
            <el-option label="Modbus-TCP" value="Modbus-TCP"></el-option>
            <el-option label="HTTP / REST" value="HTTP / REST"></el-option>
            <el-option label="UR-RT / TCP" value="UR-RT / TCP"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="addDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="confirmAddDevice">确认添加</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from 'axios';

export default {
  name: 'DeviceManagement',
  data() {
    return {
      currentTimeStr: '',
      clockTimer: null,
      pollTimer: null,
      refreshing: false,
      saving: false,
      filterType: '',
      currentDevId: 'dev_server_01',
      chartId: 'device-oscilloscope-chart',
      chartInstance: null,
      addDialogVisible: false,
      activeTimeRange: '1h',
      activeParamTab: 'inference',

      // 机械臂实控状态
      robotConnected: false,
      robotConnecting: false,

      // AGV 实控状态
      agvConnected: false,
      agvConnecting: false,
      agvPorts: [],
      agvPortName: '',
      agvStatus: null,
      agvLastUpdate: '',
      agvTargetStationInput: 6,
      agvPollTimer: null,
      inspectResult: 'ok',
      stationLoading: 1,
      stationDetect: 2,

      // 自动工作流状态
      workflowState: 'IDLE',
      workflowPollTimer: null,

      // 服务器实控监控状态 (真实接口联动 + 兜底保护)
      serverMonitor: {
        hostname: 'SRV-LAB-40C-251G',
        uptime: '12 天 4 小时',
        startTime: '2026-08-24 08:15:22',
        version: 'v2.1.3-build20260824',
        cpuUsage: 1.3,
        cores: 40,
        threads: 80,
        cpuModel: '双路 Xeon Platinum',
        memUsed: 5.5,
        memTotal: 251.8,
        memUsage: 21.8,
        gpuUsage: 23,
        gpuModel: 'FP16 TensorRT',
        diskUsed: 13.2,
        diskTotal: 218.5,
        diskUsage: 6.0,
        netLatency: '0.82 ms',
        netStatus: '微秒级',
        bytesSent: 1.15,
        bytesRecv: 0.55
      },
      serverMonitorTimer: null,

      newDevForm: {
        name: '',
        type: 'camera',
        ip: '192.168.1.105',
        port: 3956,
        protocol: 'GigE Vision'
      },
      editParams: {},
      chartHistory: {
        server: [1.2, 1.5, 1.1, 1.8, 1.2, 1.4, 1.2],
        camera: [30.2, 30.4, 30.1, 30.5, 30.3, 30.4, 30.4],
        arm: [37.2, 37.4, 37.5, 37.6, 37.6, 37.7, 37.6],
        agv: [0.82, 0.84, 0.85, 0.83, 0.84, 0.86, 0.84]
      },
      devices: [
        {
          id: 'dev_server_01',
          name: '边缘算力服务器',
          type: 'server',
          typeName: '算力节点',
          icon: 'el-icon-cpu',
          ip: '192.168.1.3',
          port: 8088,
          protocol: 'HTTP / REST',
          sn: 'SRV-LAB-40C-251G',
          vendor: 'Inspur / Intel Xeon Dual',
          primaryMetricName: 'CPU负荷',
          primaryMetricVal: '1.2 %',
          params: { batchSize: 1, precision: 'FP16', threads: 16 }
        },
        {
          id: 'dev_cam_01',
          name: '工业相机 YG-CO100100-W',
          type: 'camera',
          typeName: '视觉传感器',
          icon: 'el-icon-camera',
          ip: '192.168.1.103',
          port: 3956,
          protocol: 'GigE Vision',
          sn: 'CAM-CIM-C10C-01',
          vendor: '',
          primaryMetricName: '采集帧率',
          primaryMetricVal: '30.4 FPS',
          params: { exposure: 3500, gain: 4.0, trigger: 'line1' }
        },
        {
          id: 'dev_arm_01',
          name: '机械臂 DUX-1A3M',
          type: 'arm',
          typeName: '协作机械臂',
          icon: 'el-icon-connection',
          ip: '192.168.1.102',
          port: 30003,
          protocol: 'UR-RT / TCP',
          sn: 'ARM-CNC-6DOF-001',
          vendor: '',
          primaryMetricName: '电机温升',
          primaryMetricVal: '37.6 °C',
          params: { speed: 60, force: 25, collision: 'medium' }
        },
        {
          id: 'dev_agv_01',
          name: 'AGV小车 SLAM-500',
          type: 'agv',
          typeName: '自主底盘',
          icon: 'el-icon-truck',
          ip: '192.168.1.101',
          port: 502,
          protocol: 'Modbus-TCP',
          sn: 'AGV-SLAM-500K-01',
          vendor: '',
          primaryMetricName: '动力电池',
          primaryMetricVal: '88 %',
          params: { maxSpeed: 1.2, safeDist: 0.8, turnSpeed: 45 }
        }
      ],
      busLogs: [
        { time: '16:42:01', source: 'SERVER', type: 'server', protocol: 'HTTP/REST', addr: '192.168.1.3:8088', msg: 'GET /api/status -> CPU 40 Cores online, TensorRT-FP16 ready' },
        { time: '16:42:02', source: 'CAMERA', type: 'camera', protocol: 'GigE Vision', addr: '192.168.1.103:3956', msg: 'Stream packet recv: Frame #142857 (4024x3036, 12MB, 0 loss)' },
        { time: '16:42:03', source: 'ROBOT', type: 'arm', protocol: 'UR-RT TCP', addr: '192.168.1.102:30003', msg: 'Feedback cycle 125Hz: Tool pose [X420, Y120, Z370, Rz0.01]' },
        { time: '16:42:04', source: 'AGV', type: 'agv', protocol: 'Modbus-TCP', addr: '192.168.1.101:502', msg: 'Read Holding Registers [0x0010-0x0018]: Battery=88%, Pose=Station_A3' },
        { time: '16:42:05', source: 'GATEWAY', type: 'sys', protocol: 'PROFINET', addr: '192.168.1.1:102', msg: 'Cyclic I/O data exchange acknowledged, Jitter < 0.05ms' }
      ]
    };
  },
  computed: {
    filteredDevices() {
      if (!this.filterType) return this.devices;
      return this.devices.filter(d => d.type === this.filterType);
    },
    currentDev() {
      return this.devices.find(d => d.id === this.currentDevId) || this.devices[0];
    },
    currentDevMetrics() {
      const dev = this.currentDev;
      if (!dev) return [];

      if (dev.type === 'server') {
        const lastVal = this.chartHistory.server[this.chartHistory.server.length - 1] || 1.2;
        return [
          { label: 'CPU 综合负荷', value: lastVal + ' %', highlight: true, percent: lastVal * 6, tag: '稳定' },
          { label: '物理核心/线程', value: '40 核 / 80 线程', sub: '双路 Xeon Platinum 算力' },
          { label: '内存占用率', value: '5.5 / 251.8 GB', highlight: true, percent: 14 },
          { label: '持久化磁盘可用', value: '13.2 / 218.5 GB', percent: 8 },
          { label: 'AI 推理精度', value: dev.params.precision + ' TensorRT', highlight: true, sub: 'YOLOv8 缺陷检测模型' },
          { label: '连续无故障运行', value: '128 小时 42 分', sub: '系统运行平稳' }
        ];
      } else if (dev.type === 'camera') {
        return [
          { label: '相机类型', value: '工业面阵相机', highlight: true, sub: '高分辨率工业成像' },
          { label: '传感器类型', value: 'CMOS 卷帘快门', highlight: true, sub: '高灵敏度感光元件' },
          { label: '分辨率 / 像素', value: '1200 万像素', highlight: true, sub: '4024 × 3036 标称输出' }
        ];
      } else if (dev.type === 'arm') {
        return [
          { label: '工作半径', value: '1000 mm', highlight: true, sub: '最大作业包络范围' },
          { label: '重复定位精度', value: '±0.02 mm', highlight: true, sub: '高精度伺服闭环' },
          { label: '防护等级', value: 'IP54', highlight: true, sub: '工业级防尘防溅水' }
        ];
      } else if (dev.type === 'agv') {
        return [
          { label: '导航方式', value: 'SLAM 激光导航', highlight: true, sub: '高精自主建图与定位' },
          { label: '最大载重', value: '100 kg', highlight: true, sub: '工业级重载搬运底盘' },
          { label: '最大速度', value: '1.5 m/s', highlight: true, sub: '全向平稳巡航行驶' }
        ];
      }
      return [];
    },
    agvModeText() {
      if (!this.agvStatus || this.agvStatus.mode === undefined) return '-';
      return this.modeText(this.agvStatus.mode);
    },
    agvModeTagType() {
      if (!this.agvStatus || this.agvStatus.mode === undefined) return 'info';
      return this.agvStatus.mode === 0 ? 'success' : this.agvStatus.mode === 2 ? 'warning' : 'info';
    },
    canStartWorkflow() {
      return this.agvConnected && this.robotConnected &&
        (this.workflowState === 'IDLE' || this.workflowState === 'COMPLETED' || this.workflowState === 'ERROR');
    },
    workflowStateText() {
      const map = {
        IDLE: '空闲',
        AGV_TO_STATION6: 'AGV→6号站',
        SIGNAL_ROBOT: '已发信号→等待机械臂响应',
        WAIT_ROBOT_DONE: '机械臂动作中→等待完成信号',
        AGV_TO_STATION3: '收到完成信号→AGV→3号站',
        COMPLETED: '已完成',
        ERROR: '异常'
      };
      return map[this.workflowState] || this.workflowState;
    },
    workflowTagType() {
      const map = {
        IDLE: 'info',
        AGV_TO_STATION6: '',
        SIGNAL_ROBOT: 'warning',
        WAIT_ROBOT_DONE: 'warning',
        AGV_TO_STATION3: '',
        COMPLETED: 'success',
        ERROR: 'danger'
      };
      return map[this.workflowState] || 'info';
    },
    agvView() {
      const s = this.agvStatus;
      const d = '--';
      return {
        battery: s ? s.battery + '%' : d,
        realSpeed: s ? s.realSpeed + ' 米/小时' : d,
        setSpeed: s ? s.setSpeed + ' 米/小时' : d,
        currentStation: s ? (s.currentStation || d) : d,
        targetStation: s ? (s.targetStation || d) : d,
        mode: s ? this.modeText(s.mode) : d,
        run: s ? this.runText(s) : d,
        runClass: s ? (s.stopFlag ? 'warn' : 'ok') : '',
        arriveStop: s ? (s.arriveStop ? '是' : '否') : d,
        obstacleStop: s ? (s.obstacleStop ? '是' : '否') : d,
        cmdStop: s ? (s.cmdStop ? '是' : '否') : d,
        charging: s ? (s.charging === 1 ? '充电中' : '未充电') : d
      };
    }
  },
  mounted() {
    this.updateClock();
    this.clockTimer = setInterval(this.updateClock, 1000);

    this.syncEditParams();
    this.$nextTick(() => {
      this.initChart();
    });
    this.startHeartbeat();

    // 加载硬件实控初始状态
    this.loadAgvPorts();
    this.fetchAgvStatus();
    this.startAgvStatusPolling();

    // 启动服务器底层监控遥测轮询
    this.startServerMonitorPolling();
  },
  beforeDestroy() {
    if (this.clockTimer) clearInterval(this.clockTimer);
    if (this.pollTimer) clearInterval(this.pollTimer);
    this.stopAgvStatusPolling();
    this.stopWorkflowPolling();
    this.stopServerMonitorPolling();
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
  },
  methods: {
    updateClock() {
      const d = new Date();
      const pad = n => String(n).padStart(2, '0');
      this.currentTimeStr = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    },
    handleSelectDevice(id) {
      this.currentDevId = id;
      this.syncEditParams();
      this.$nextTick(() => {
        if (this.currentDev && this.currentDev.type !== 'agv') {
          this.initChart();
        } else {
          if (this.chartInstance) {
            this.chartInstance.dispose();
            this.chartInstance = null;
          }
        }
      });
    },
    syncEditParams() {
      if (this.currentDev) {
        this.editParams = JSON.parse(JSON.stringify(this.currentDev.params));
      }
    },
    startHeartbeat() {
      this.pollTimer = setInterval(() => {
        this.tickTelemetry();
      }, 3000);
    },
    tickTelemetry() {
      const sVal = parseFloat((1.0 + Math.random() * 0.8).toFixed(1));
      this.chartHistory.server.shift();
      this.chartHistory.server.push(sVal);

      const cVal = parseFloat((30.0 + Math.random() * 0.6).toFixed(1));
      this.chartHistory.camera.shift();
      this.chartHistory.camera.push(cVal);

      const aVal = parseFloat((37.2 + Math.random() * 0.6).toFixed(1));
      this.chartHistory.arm.shift();
      this.chartHistory.arm.push(aVal);

      const agvVal = parseFloat((0.8 + Math.random() * 0.08).toFixed(2));
      this.chartHistory.agv.shift();
      this.chartHistory.agv.push(agvVal);

      const devSrv = this.devices.find(d => d.type === 'server');
      if (devSrv) devSrv.primaryMetricVal = sVal + ' %';
      const devCam = this.devices.find(d => d.type === 'camera');
      if (devCam) devCam.primaryMetricVal = cVal + ' FPS';
      const devArm = this.devices.find(d => d.type === 'arm');
      if (devArm) devArm.primaryMetricVal = aVal + ' °C';
      const devAgv = this.devices.find(d => d.type === 'agv');
      if (devAgv) devAgv.primaryMetricVal = '88 %';

      if (this.currentDev && this.currentDev.type !== 'agv') {
        this.updateChart();
      }
    },
    initChart() {
      if (this.currentDev && this.currentDev.type === 'agv') {
        if (this.chartInstance) {
          this.chartInstance.dispose();
          this.chartInstance = null;
        }
        return;
      }
      const dom = document.getElementById(this.chartId);
      if (!dom) return;

      if (this.chartInstance) {
        this.chartInstance.dispose();
        this.chartInstance = null;
      }

      this.chartInstance = echarts.init(dom);
      this.updateChart();

      if (!this._resizeHandler) {
        this._resizeHandler = () => {
          if (this.chartInstance) this.chartInstance.resize();
        };
        window.addEventListener('resize', this._resizeHandler);
      }
    },
    updateChart() {
      if (!this.currentDev || this.currentDev.type === 'agv') {
        if (this.chartInstance) {
          this.chartInstance.dispose();
          this.chartInstance = null;
        }
        return;
      }
      if (!this.chartInstance) return;

      if (this.currentDev.type === 'server') {
        const timeLabels = [
          '19:30', '19:32', '19:34', '19:36', '19:38',
          '19:40', '19:42', '19:44', '19:46', '19:48',
          '19:50', '19:52', '19:54', '19:56', '19:58',
          '20:00', '20:02', '20:04', '20:06', '20:08',
          '20:10', '20:12', '20:14', '20:16', '20:18', '20:20'
        ];

        const option = {
          grid: {
            left: '3%',
            right: '5%',
            top: '12%',
            bottom: '8%',
            containLabel: true
          },
          tooltip: {
            trigger: 'axis',
            backgroundColor: '#1e293b',
            borderColor: '#334155',
            textStyle: { color: '#f8fafc', fontSize: 12 }
          },
          xAxis: {
            type: 'category',
            data: timeLabels,
            boundaryGap: false,
            axisLine: { lineStyle: { color: '#e2e8f0' } },
            axisLabel: {
              color: '#64748b',
              fontSize: 11,
              interval: (index) => index % 5 === 0
            }
          },
          yAxis: [
            {
              type: 'value',
              name: '百分比 (%)',
              nameTextStyle: { color: '#94a3b8', fontSize: 11, padding: [0, 0, 4, -10] },
              min: 0,
              max: 100,
              interval: 25,
              splitLine: { lineStyle: { color: '#f1f5f9', type: 'solid' } },
              axisLabel: { color: '#64748b', fontSize: 11 }
            },
            {
              type: 'value',
              name: '帧率 / 延迟',
              nameTextStyle: { color: '#94a3b8', fontSize: 11, padding: [0, -10, 4, 0] },
              min: 0,
              max: 160,
              interval: 40,
              splitLine: { show: false },
              axisLabel: { color: '#64748b', fontSize: 11 }
            }
          ],
          series: [
            {
              name: 'CPU 负荷 (%)',
              type: 'line',
              smooth: 0.25,
              showSymbol: true,
              symbol: 'circle',
              symbolSize: 4,
              itemStyle: { color: '#2563eb', borderColor: '#ffffff', borderWidth: 1 },
              lineStyle: { width: 1.8, color: '#2563eb' },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(37, 99, 235, 0.16)' },
                  { offset: 1, color: 'rgba(37, 99, 235, 0.01)' }
                ])
              },
              data: [72, 68, 70, 65, 74, 69, 71, 64, 58, 62, 66, 60, 65, 68, 72, 75, 71, 67, 63, 68, 64, 69, 72, 66, 63, 62]
            },
            {
              name: '内存占用 (%)',
              type: 'line',
              smooth: 0.25,
              showSymbol: true,
              symbol: 'circle',
              symbolSize: 4,
              itemStyle: { color: '#059669', borderColor: '#ffffff', borderWidth: 1 },
              lineStyle: { width: 1.8, color: '#059669' },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(5, 150, 105, 0.14)' },
                  { offset: 1, color: 'rgba(5, 150, 105, 0.01)' }
                ])
              },
              data: [35, 34, 32, 30, 33, 38, 42, 40, 36, 34, 37, 41, 44, 46, 43, 39, 37, 35, 38, 40, 37, 36, 38, 41, 39, 38]
            },
            {
              name: 'AI 推理负载 (%)',
              type: 'line',
              smooth: 0.25,
              showSymbol: true,
              symbol: 'circle',
              symbolSize: 4,
              itemStyle: { color: '#7c3aed', borderColor: '#ffffff', borderWidth: 1 },
              lineStyle: { width: 1.6, color: '#7c3aed' },
              data: [20, 21, 23, 24, 22, 21, 23, 25, 24, 22, 20, 22, 24, 25, 23, 21, 22, 24, 25, 23, 22, 24, 25, 23, 22, 22]
            },
            {
              name: '网络延迟 (ms)',
              type: 'line',
              yAxisIndex: 1,
              smooth: 0.25,
              showSymbol: true,
              symbol: 'circle',
              symbolSize: 4,
              itemStyle: { color: '#c026d3', borderColor: '#ffffff', borderWidth: 1 },
              lineStyle: { width: 1.6, color: '#c026d3' },
              data: [12, 13, 15, 16, 14, 13, 15, 17, 14, 15, 13, 14, 16, 18, 15, 13, 14, 16, 15, 13, 14, 16, 17, 14, 15, 15]
            },
            {
              name: '采集频率 (fps)',
              type: 'line',
              yAxisIndex: 1,
              smooth: 0.25,
              showSymbol: true,
              symbol: 'circle',
              symbolSize: 4,
              itemStyle: { color: '#ea580c', borderColor: '#ffffff', borderWidth: 1 },
              lineStyle: { width: 1.6, color: '#ea580c' },
              data: [22, 24, 27, 28, 26, 25, 28, 29, 27, 26, 23, 25, 28, 30, 27, 25, 26, 29, 28, 27, 26, 28, 29, 27, 26, 26]
            }
          ]
        };

        this.chartInstance.setOption(option);
        return;
      }

      const type = this.currentDev ? this.currentDev.type : 'server';
      const dataList = this.chartHistory[type] || [1, 2, 1, 2, 1, 2, 1];

      let label = 'CPU 负载率 (%)';
      let color = '#2563eb';
      let areaColor = 'rgba(37, 99, 235, 0.12)';

      if (type === 'camera') {
        label = '采集帧率 (FPS)';
        color = '#059669';
        areaColor = 'rgba(5, 150, 105, 0.12)';
      } else if (type === 'arm') {
        label = '电机温度 (°C)';
        color = '#d97706';
        areaColor = 'rgba(217, 119, 6, 0.12)';
      } else if (type === 'agv') {
        label = '行进速度 (m/s)';
        color = '#7c3aed';
        areaColor = 'rgba(124, 58, 237, 0.12)';
      }

      const option = {
        grid: {
          left: '2%',
          right: '3%',
          top: '16%',
          bottom: '10%',
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#1e293b',
          borderColor: '#334155',
          textStyle: { color: '#f8fafc', fontSize: 12 },
          formatter: `{b}<br/><span style="color:${color}; font-weight:bold;">● ${label}: {c}</span>`
        },
        xAxis: {
          type: 'category',
          data: ['T-6', 'T-5', 'T-4', 'T-3', 'T-2', 'T-1', 'Now (0.0s)'],
          axisLine: { lineStyle: { color: '#cbd5e1' } },
          axisLabel: { color: '#64748b', fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
          axisLabel: { color: '#64748b', fontSize: 11 }
        },
        series: [
          {
            name: label,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            itemStyle: { color: color },
            lineStyle: { width: 2.5, color: color },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: areaColor },
                { offset: 1, color: 'rgba(255, 255, 255, 0.01)' }
              ])
            },
            data: dataList
          }
        ]
      };

      this.chartInstance.setOption(option);
    },
    getProgressColor(p) {
      if (p > 85) return '#ef4444';
      if (p > 60) return '#f59e0b';
      return '#2563eb';
    },
    handleRefreshAll() {
      this.refreshing = true;
      setTimeout(() => {
        this.refreshing = false;
        this.tickTelemetry();
        this.$message.success('产线受控节点拓扑与数据同步完成，各通信链路畅通');
        this.appendBusLog('SYS', 'GATEWAY', 'PROFINET', '192.168.1.1:102', '全网拓扑巡检完成: 4/4 节点应答正常');
      }, 400);
    },
    handlePingNode() {
      if (!this.currentDev) return;
      this.$message.info(`正在探测 ${this.currentDev.ip} ...`);
      setTimeout(() => {
        this.$message.success(`[${this.currentDev.name}] 链路通畅 (RTT: 0.65ms, 丢包率: 0%)`);
        this.appendBusLog(this.currentDev.type, this.currentDev.name, this.currentDev.protocol, `${this.currentDev.ip}:${this.currentDev.port}`, 'ICMP Ping probe ACK (RTT 0.65ms)');
      }, 250);
    },
    handleRestartDriver() {
      if (!this.currentDev) return;
      this.$message.info(`正在重启 ${this.currentDev.name} 驱动服务...`);
      setTimeout(() => {
        this.$message.success(`[${this.currentDev.name}] 驱动服务热重启就绪`);
        this.appendBusLog(this.currentDev.type, this.currentDev.name, this.currentDev.protocol, `${this.currentDev.ip}:${this.currentDev.port}`, 'Driver service restarted successfully');
      }, 350);
    },
    handleSelfTest() {
      this.$message.info('正在执行全总线健康自检...');
      setTimeout(() => {
        this.$message.success('设备自检通过：各项工况指标均处于标称区间');
        this.appendBusLog('SYS', 'SELF-TEST', 'DIAG', '127.0.0.1:0', 'Health self-test passed (0 errors, 0 warnings)');
      }, 300);
    },
    handleSaveParams() {
      this.saving = true;
      setTimeout(() => {
        this.saving = false;
        if (this.currentDev) {
          this.currentDev.params = JSON.parse(JSON.stringify(this.editParams));
          this.appendBusLog(this.currentDev.type, this.currentDev.name, this.currentDev.protocol, `${this.currentDev.ip}:${this.currentDev.port}`, `Write Parameters to registers [OK]`);
        }
        this.$message.success('工控参数已下发至硬件寄存器并立即热生效');
      }, 350);
    },
    handleResetParams() {
      this.syncEditParams();
      this.$message.info('已恢复为当前设备标称运行参数');
    },
    handleAddDeviceDialog() {
      this.addDialogVisible = true;
    },
    confirmAddDevice() {
      if (!this.newDevForm.name) {
        this.$message.warning('请输入设备名称');
        return;
      }
      const newId = 'dev_custom_' + Date.now();
      const typeIcons = {
        camera: 'el-icon-camera',
        server: 'el-icon-cpu',
        arm: 'el-icon-connection',
        agv: 'el-icon-truck'
      };
      this.devices.push({
        id: newId,
        name: this.newDevForm.name,
        type: this.newDevForm.type,
        typeName: '扩展节点',
        icon: typeIcons[this.newDevForm.type] || 'el-icon-setting',
        ip: this.newDevForm.ip,
        port: Number(this.newDevForm.port),
        protocol: this.newDevForm.protocol,
        sn: 'EXT-' + Math.floor(Math.random() * 9000 + 1000),
        vendor: 'Standard Industrial',
        primaryMetricName: '运行状态',
        primaryMetricVal: 'ONLINE',
        params: {}
      });
      this.addDialogVisible = false;
      this.$message.success('新增设备节点成功并加入总线轮询');
      this.appendBusLog(this.newDevForm.type, this.newDevForm.name, this.newDevForm.protocol, `${this.newDevForm.ip}:${this.newDevForm.port}`, 'New node registered into bus polling queue');
    },
    appendBusLog(type, source, protocol, addr, msg) {
      const now = new Date();
      const pad = n => String(n).padStart(2, '0');
      const timeStr = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
      this.busLogs.unshift({
        time: timeStr,
        type,
        source,
        protocol,
        addr,
        msg
      });
      if (this.busLogs.length > 30) this.busLogs.pop();
    },

    // ==================== 机械臂实控方法 ====================
    async connectRobot() {
      this.robotConnecting = true;
      try {
        const res = await axios.post('api/aubo/connect');
        if (res.data.code === 200) {
          this.robotConnected = true;
          this.$message.success('机械臂已连接');
          this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Connect controller [SUCCESS]');
        } else {
          this.$message.error(res.data.message || '连接失败');
        }
      } catch (e) {
        this.$message.error('机械臂连接失败：请确认控制器IP(192.168.1.6)可达');
      } finally {
        this.robotConnecting = false;
      }
    },
    async disconnectRobot() {
      try {
        await axios.post('api/aubo/disconnect');
      } catch (e) { /* ignore */ }
      this.robotConnected = false;
      this.$message.info('机械臂连接已断开');
      this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Disconnect controller [CLOSED]');
    },
    async setDO(index, value) {
      try {
        const res = await axios.post('api/aubo/setDO', { index, value });
        if (res.data.code === 200) {
          this.$message.success(`DO${index} = ${value ? '开' : '关'}`);
          this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, `Set DO${index} output -> ${value ? 'HIGH' : 'LOW'}`);
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('DO 设置失败');
      }
    },
    async robotMoveHome() {
      try {
        const res = await axios.post('api/aubo/photo/moveToHome');
        if (res.data.code === 200) {
          this.$message.success('机械臂已回原位');
          this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Motion Cmd: MoveToHome executed');
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('移动失败');
      }
    },
    async robotMovePhoto() {
      try {
        const res = await axios.post('api/aubo/photo/moveToPosition');
        if (res.data.code === 200) {
          this.$message.success('机械臂已到拍照位');
          this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Motion Cmd: MoveToPhotoPosition executed');
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('移动失败');
      }
    },
    async robotStop() {
      try {
        const res = await axios.post('api/aubo/stop');
        if (res.data.code === 200) {
          this.$message.warning('机械臂已发送急停');
          this.appendBusLog('arm', 'AUBO-ROBOT', 'UR-RT / TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Safety Emergency Stop triggered!');
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('急停失败');
      }
    },

    // ==================== AGV 实控方法 ====================
    async loadAgvPorts() {
      try {
        const res = await axios.get('api/agv/ports');
        if (res.data.code === 200) {
          this.agvPorts = res.data.data || [];
          if (!this.agvPortName && this.agvPorts.length > 0) {
            this.agvPortName = this.agvPorts[0].name;
          }
        }
      } catch (e) {
        this.agvPorts = [];
      }
    },
    async fetchAgvStatus() {
      try {
        const res = await axios.get('api/agv/status');
        if (res.data.code === 200) {
          const d = res.data.data || {};
          this.agvConnected = !!d.connected;
          if (d.portName) this.agvPortName = d.portName;
          if (d.status) {
            this.agvStatus = d.status;
            this.agvLastUpdate = d.lastStatusTime ? new Date(d.lastStatusTime).toLocaleTimeString() : '';
            // 更新 AGV 卡片指标
            const agvDev = this.devices.find(item => item.type === 'agv');
            if (agvDev && d.status.battery !== undefined) {
              agvDev.primaryMetricVal = `${d.status.battery} %`;
            }
          }
        }
      } catch (e) { /* ignore */ }
    },
    startAgvStatusPolling() {
      this.stopAgvStatusPolling();
      this.agvPollTimer = setInterval(() => {
        if (this.agvConnected) {
          this.fetchAgvStatus();
        }
      }, 1500);
    },
    stopAgvStatusPolling() {
      if (this.agvPollTimer) {
        clearInterval(this.agvPollTimer);
        this.agvPollTimer = null;
      }
    },
    async connectAgv() {
      if (!this.agvPortName) {
        this.$message.warning('请先选择串口');
        return;
      }
      this.agvConnecting = true;
      try {
        const res = await axios.post('api/agv/connect', { portName: this.agvPortName });
        if (res.data.code === 200) {
          this.agvConnected = true;
          this.startAgvStatusPolling();
          this.$message.success('AGV 串口已连接');
          this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, `Serial ${this.agvPortName} connected`);
        } else {
          this.$message.error(res.data.message || '连接失败');
        }
      } catch (e) {
        this.$message.error('AGV 串口连接失败：后端服务未启动或异常');
      } finally {
        this.agvConnecting = false;
      }
    },
    async disconnectAgv() {
      try {
        await axios.post('api/agv/disconnect');
      } catch (e) { /* ignore */ }
      this.agvConnected = false;
      this.agvStatus = null;
      this.stopAgvStatusPolling();
      this.$message.info('AGV 串口已断开');
      this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Serial port disconnected');
    },
    async sendAgvCmd(sub, p1, p2) {
      try {
        const res = await axios.post('api/agv/command', { sub, p1, p2 });
        return res.data.code === 200;
      } catch (e) {
        this.$message.error('指令发送失败：后端服务未启动或串口未连接');
        return false;
      }
    },
    async agvEmergencyStop() {
      try {
        await axios.post('api/agv/command', { sub: 0x9D, p1: 0, p2: 0x00 });
        this.$message.warning('AGV 急停指令已下发');
        this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Emergency Stop (0x9D 0x00)');
      } catch (e) {
        this.$message.error('急停指令发送失败');
      }
    },
    async agvReset() {
      try {
        const res = await axios.post('api/agv/command', { sub: 0x9D, p1: 1, p2: 0x00 });
        if (res.data.code === 200) {
          this.$message.success('AGV 复位：正在返回 1 号站');
          this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Reset to Station 1 (0x9D 0x01)');
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('复位指令发送失败');
      }
    },
    async agvGoStation(station) {
      if (!station) return;
      const ok = await this.sendAgvCmd(0x9D, station, 0x00);
      if (ok) {
        this.$message.success(`AGV 目标站点 ${station} 已下发`);
        this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, `Navigate to Station ${station} (0x9D)`);
      }
    },
    modeText(m) {
      return ['普通模式', '站点编辑', '站点召回'][m] || '-';
    },
    runText(s) {
      if (!s.stopFlag) return '行驶中';
      if (s.obstacleStop) return '障碍停止';
      if (s.arriveStop) return '到站停止';
      if (s.cmdStop) return '指令停止';
      return '停止';
    },
    queryAgvStatus() {
      axios.post('api/agv/command', { query: true })
        .then(() => {
          this.$message.success('状态查询指令已发送');
          this.appendBusLog('agv', 'AGV-CHASSIS', 'Modbus-TCP', `${this.currentDev.ip}:${this.currentDev.port}`, 'Query AGV Status (query: true)');
        })
        .catch(() => this.$message.warning('查询失败，串口可能未连接'));
    },

    // ==================== 自动工作流控制 ====================
    async startWorkflow() {
      try {
        const res = await axios.post('api/workflow/start');
        if (res.data.code === 200) {
          this.$message.success('工作流已启动');
          this.appendBusLog('agv', 'WORKFLOW-COORDINATOR', 'RPC / Bus', 'Coordinator', 'Auto Workflow Started');
          this.startWorkflowPolling();
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('启动失败');
      }
    },
    async stopWorkflow() {
      try {
        await axios.post('api/workflow/stop');
        this.$message.success('工作流已停止');
        this.appendBusLog('agv', 'WORKFLOW-COORDINATOR', 'RPC / Bus', 'Coordinator', 'Auto Workflow Stopped');
        this.stopWorkflowPolling();
      } catch (e) { /* ignore */ }
    },
    async resetWorkflow() {
      try {
        await axios.post('api/workflow/reset');
        this.workflowState = 'IDLE';
        this.$message.success('工作流已重置');
        this.appendBusLog('agv', 'WORKFLOW-COORDINATOR', 'RPC / Bus', 'Coordinator', 'Auto Workflow Reset');
      } catch (e) { /* ignore */ }
    },
    startWorkflowPolling() {
      this.stopWorkflowPolling();
      this.workflowPollTimer = setInterval(() => this.fetchWorkflowStatus(), 1000);
    },
    stopWorkflowPolling() {
      if (this.workflowPollTimer) {
        clearInterval(this.workflowPollTimer);
        this.workflowPollTimer = null;
      }
    },
    async fetchWorkflowStatus() {
      try {
        const res = await axios.get('api/workflow/status');
        if (res.data.code === 200 && res.data.data) {
          this.workflowState = res.data.data.state;
          if (this.workflowState === 'COMPLETED' || this.workflowState === 'ERROR') {
            this.stopWorkflowPolling();
          }
        }
      } catch (e) { /* ignore */ }
    },
    async fetchServerMonitor() {
      try {
        const res = await axios.get('api/server/monitor/info');
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          if (d.system) {
            if (d.system.hostname) this.serverMonitor.hostname = d.system.hostname;
            if (d.system.release) this.serverMonitor.version = d.system.release;
            if (d.system.uptime !== undefined && d.system.uptime !== null) {
              const up = Number(d.system.uptime);
              const days = Math.floor(up / 86400);
              const hours = Math.floor((up % 86400) / 3600);
              const mins = Math.floor((up % 3600) / 60);
              if (days > 0) {
                this.serverMonitor.uptime = `${days} 天 ${hours} 小时`;
              } else if (hours > 0) {
                this.serverMonitor.uptime = `${hours} 小时 ${mins} 分钟`;
              } else {
                this.serverMonitor.uptime = `${mins} 分钟`;
              }
            }
          }
          if (d.cpu) {
            if (d.cpu.usage !== undefined) this.serverMonitor.cpuUsage = Number(d.cpu.usage).toFixed(1);
            if (d.cpu.cores) this.serverMonitor.cores = d.cpu.cores;
            if (d.cpu.threads) this.serverMonitor.threads = d.cpu.threads;
            if (d.cpu.cores >= 20) this.serverMonitor.cpuModel = 'Intel Xeon (双路)';
          }
          if (d.memory) {
            if (d.memory.used !== undefined) this.serverMonitor.memUsed = Number(d.memory.used).toFixed(1);
            if (d.memory.total !== undefined) this.serverMonitor.memTotal = Number(d.memory.total).toFixed(1);
            if (d.memory.usage !== undefined) this.serverMonitor.memUsage = Number(d.memory.usage).toFixed(1);
          }
          if (d.disk) {
            if (d.disk.used !== undefined) this.serverMonitor.diskUsed = Number(d.disk.used).toFixed(1);
            if (d.disk.total !== undefined) this.serverMonitor.diskTotal = Number(d.disk.total).toFixed(1);
            if (d.disk.usage !== undefined) this.serverMonitor.diskUsage = Number(d.disk.usage).toFixed(1);
          }
          if (d.network) {
            if (d.network.bytesSent !== undefined) this.serverMonitor.bytesSent = d.network.bytesSent;
            if (d.network.bytesRecv !== undefined) this.serverMonitor.bytesRecv = d.network.bytesRecv;
            this.serverMonitor.netStatus = `↑${d.network.bytesSent}MB ↓${d.network.bytesRecv}MB`;
          }
          const srvDev = this.devices.find(dev => dev.type === 'server');
          if (srvDev) {
            if (d.system && d.system.hostname) srvDev.sn = d.system.hostname;
            if (d.cpu && d.cpu.usage !== undefined) srvDev.primaryMetricVal = `${Number(d.cpu.usage).toFixed(1)} %`;
          }
        }
      } catch (e) {
        // 保持优雅降级，不阻断前端交互
      }
    },
    startServerMonitorPolling() {
      this.fetchServerMonitor();
      if (this.serverMonitorTimer) clearInterval(this.serverMonitorTimer);
      this.serverMonitorTimer = setInterval(() => {
        this.fetchServerMonitor();
      }, 3000);
    },
    stopServerMonitorPolling() {
      if (this.serverMonitorTimer) {
        clearInterval(this.serverMonitorTimer);
        this.serverMonitorTimer = null;
      }
    }
  }
};
</script>

<style scoped>
/* ================= 全局页面与排版 (Clean Swiss Industrial) ================= */
.device-management-page {
  width: 100%;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #1e293b;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.font-mono {
  font-family: 'Roboto Mono', 'SF Mono', Consolas, Monaco, monospace;
}

/* ================= 1. 顶部状态栏 ================= */
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.network-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #059669;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 3px 10px;
  border-radius: 4px;
}

.indicator-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #059669;
}

.section-subtitle {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.header-action-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* ================= 2. 核心 KPI 指标卡行 ================= */
.kpi-metrics-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.metric-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.06);
  border-color: #cbd5e1;
}

.metric-label {
  font-size: 11.5px;
  font-weight: 600;
  color: #64748b;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-value .num {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.1;
}

.metric-value .unit {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.metric-value .text-mono {
  font-size: 20px;
  color: #2563eb;
}

.metric-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

.status-pill {
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10.5px;
  font-weight: 600;
}

.status-pill.success {
  background: #ecfdf5;
  color: #059669;
}

.status-pill.info {
  background: #eff6ff;
  color: #2563eb;
}

/* ================= 3. 主体分栏工作台 ================= */
.workbench-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
  align-items: stretch;
}

.workbench-layout.is-server-layout {
  grid-template-columns: 340px minmax(560px, 1fr) 350px;
  gap: 12px;
}

.device-middle-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  height: 100%;
}

.device-right-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100%;
}

/* ================= 图四：服务器专属运行总览卡片 ================= */
.overview-main-card {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.overview-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-title-text {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.overview-health-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  font-weight: 500;
  color: #059669;
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 1px 8px;
  border-radius: 6px;
}

.overview-hero-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 2px;
}

.dev-hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dev-hero-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background-color: #eff6ff;
  color: #2563eb;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dev-hero-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.hero-dev-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
}

.hero-sn-badge {
  font-size: 11px;
  color: #64748b;
  background: #f1f5f9;
  padding: 1px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

.hero-status-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 1px 6px;
  border-radius: 10px;
  white-space: nowrap;
}

.status-dot-green {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: #059669;
}

.status-txt-green {
  font-size: 10.5px;
  color: #059669;
  font-weight: 600;
}

.hero-meta-row {
  font-size: 11px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.hero-meta-row .sep {
  color: #cbd5e1;
}

.dev-hero-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  white-space: nowrap;
}

.health-ring-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.health-ring-circle {
  position: relative;
  width: 56px;
  height: 56px;
}

.ring-svg {
  width: 100%;
  height: 100%;
}

.ring-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ring-score {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.ring-label {
  font-size: 8.5px;
  color: #64748b;
  margin-top: 1px;
}

.health-meta-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex-shrink: 0;
}

.health-meta-item {
  font-size: 10.5px;
  display: flex;
  gap: 6px;
  white-space: nowrap;
}

.health-meta-item .meta-label {
  color: #64748b;
  width: 50px;
  white-space: nowrap;
  flex-shrink: 0;
}

.health-meta-item .meta-value {
  color: #334155;
  font-weight: 500;
  white-space: nowrap;
}

/* 2×3 遥测迷你网格 (图四指标设计) */
.telemetry-mini-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.mini-telemetry-card {
  background-color: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
  min-height: 68px;
  box-sizing: border-box;
}

.mini-card-head {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-icon-box {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.mini-icon-box.blue { background: #eff6ff; color: #2563eb; }
.mini-icon-box.green { background: #ecfdf5; color: #059669; }
.mini-icon-box.sky { background: #f0f9ff; color: #0284c7; }
.mini-icon-box.purple { background: #f5f3ff; color: #7c3aed; }

.mini-title {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
}

.mini-card-val {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.mini-sparkline {
  margin-top: 1px;
  height: 12px;
  width: 100%;
}

.mini-sparkline svg {
  width: 100%;
  height: 100%;
}

.mini-card-sub {
  font-size: 10px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 3px;
}

.dot-triple {
  color: #10b981;
  font-size: 8px;
  letter-spacing: -1px;
}

.mini-progress-wrap {
  width: 100%;
  height: 3px;
  background-color: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-top: 2px;
}

.mini-progress-bar {
  height: 100%;
  background-color: #0284c7;
  border-radius: 2px;
}

.mini-progress-wrap.purple .mini-progress-bar {
  background-color: #7c3aed;
}

.mini-progress-wrap.blue .mini-progress-bar {
  background-color: #2563eb;
}

.mini-card-footnote {
  font-size: 9.5px;
  color: #94a3b8;
  margin-top: 1px;
}

.mini-card-footnote.status-blue {
  color: #2563eb;
  font-weight: 500;
}

/* 图四趋势折线图卡片 */
.trend-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px 4px 14px;
  flex-shrink: 0;
}

.trend-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.trend-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-range-group {
  display: flex;
  background-color: #f1f5f9;
  border-radius: 5px;
  padding: 1px;
  gap: 1px;
}

.time-btn {
  font-size: 10.5px;
  color: #64748b;
  padding: 2px 6px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.time-btn.is-active {
  background-color: #2563eb;
  color: #ffffff;
  font-weight: 600;
}

.icon-expand-btn {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.trend-legend-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 2px 14px 4px 14px;
  flex-shrink: 0;
}

.legend-item {
  font-size: 10.5px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-line {
  width: 12px;
  height: 2.5px;
  border-radius: 2px;
}

.legend-line.blue { background-color: #2563eb; }
.legend-line.green { background-color: #059669; }
.legend-line.purple { background-color: #7c3aed; }
.legend-line.magenta { background-color: #c026d3; }
.legend-line.orange { background-color: #ea580c; }

.trend-chart-container {
  padding: 0 8px 6px 8px;
  flex: 1;
  min-height: 0;
  height: 230px;
  position: relative;
}

.echarts-trend-dom {
  width: 100%;
  height: 100%;
}

/* 图四右侧在线控制与参数面板 */
.split-col-tuner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.tuner-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px 6px 14px;
  flex-shrink: 0;
}

.tuner-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.tuner-sync-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  color: #059669;
  cursor: pointer;
  user-select: none;
}

.tuner-sync-btn:hover {
  color: #047857;
}

.tuner-nav-tabs {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 14px;
  gap: 16px;
  flex-shrink: 0;
}

.tab-item {
  font-size: 12px;
  color: #64748b;
  padding: 6px 0;
  cursor: pointer;
  position: relative;
  font-weight: 500;
}

.tab-item.is-active {
  color: #2563eb;
  font-weight: 600;
}

.tab-item.is-active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #2563eb;
  border-radius: 2px;
}

.tuner-scroll-body {
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.param-form-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-sub-heading {
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
  margin-top: 2px;
  margin-bottom: 1px;
}

.control-field-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: 11.5px;
  font-weight: 600;
  color: #475569;
}

.custom-pill-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.custom-pill-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 5px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #334155;
  font-size: 11.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.custom-pill-btn:hover {
  border-color: #cbd5e1;
  background-color: #f8fafc;
}

.custom-pill-btn.is-selected {
  border-color: #bfdbfe;
  background-color: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.pill-radio-dot {
  flex-shrink: 0;
}

.param-section-divider {
  height: 1px;
  background-color: #f1f5f9;
  margin: 2px 0;
}

.control-slider-row {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.slider-name {
  font-size: 11.5px;
  color: #475569;
}

.slider-line-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.slider-line-wrap ::v-deep .el-slider {
  flex: 1;
}

.slider-line-wrap ::v-deep .el-slider__runway {
  margin: 10px 0;
  height: 4px;
}

.slider-line-wrap ::v-deep .el-slider__bar {
  height: 4px;
}

.slider-line-wrap ::v-deep .el-slider__button {
  width: 12px;
  height: 12px;
}

.slider-num {
  width: 40px;
  text-align: right;
  font-size: 11.5px;
  color: #1e293b;
  font-weight: 600;
}

.tuner-footer-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.btn-apply-submit {
  flex: 1.6;
  background-color: #2563eb;
  border-color: #2563eb;
  font-weight: 600;
}

.btn-reset-param {
  flex: 1;
}

.column-panel, .detail-panel {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.device-list-column {
  display: flex;
  flex-direction: column;
}

.device-list-column .column-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 左侧卡片列表 */
.column-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 8px 8px 0 0;
  flex-shrink: 0;
}

.header-text {
  display: flex;
  align-items: center;
  gap: 6px;
}

.panel-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.panel-count {
  font-size: 10.5px;
  color: #64748b;
  background: #e2e8f0;
  padding: 1px 6px;
  border-radius: 3px;
}

.device-card-list {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  overflow-y: auto;
}

.device-card-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  flex: 1;
  min-height: 0;
}

.device-card-item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
  transform: translateY(-1px);
}

.device-card-item.is-selected {
  border-color: #2563eb;
  background: #eff6ff;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.08);
}

.card-left-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.card-left-icon.type-server { background: #eff6ff; color: #2563eb; }
.card-left-icon.type-camera { background: #ecfdf5; color: #059669; }
.card-left-icon.type-arm { background: #fffbeb; color: #d97706; }
.card-left-icon.type-agv { background: #f5f3ff; color: #7c3aed; }

.card-center-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.card-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-name {
  font-size: 12.5px;
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.online-tag {
  font-size: 10.5px;
  color: #059669;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #059669;
}

.card-meta-row {
  font-size: 10.5px;
  color: #64748b;
  display: flex;
  justify-content: space-between;
}

.card-kpi-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  margin-top: 1px;
}

.kpi-name {
  color: #64748b;
}

.kpi-value {
  font-weight: 700;
  color: #2563eb;
}

.card-arrow {
  color: #94a3b8;
  font-size: 12px;
}

/* 右侧工作台 */
.device-detail-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid #e2e8f0;
}

.dev-main-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.title-icon-box.type-server { background: #eff6ff; color: #2563eb; }
.title-icon-box.type-camera { background: #ecfdf5; color: #059669; }
.title-icon-box.type-arm { background: #fffbeb; color: #d97706; }
.title-icon-box.type-agv { background: #f5f3ff; color: #7c3aed; }

.title-text-group {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-dev-name {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.desc-row {
  font-size: 11px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.desc-row .sep {
  color: #cbd5e1;
}

.dev-actions-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 2×3 遥测网格 */
.telemetry-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 18px;
}

.telemetry-grid-cell {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.telemetry-grid-cell.is-highlight {
  border-color: #bfdbfe;
  background: #f0f7ff;
}

.cell-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cell-label {
  font-size: 11.5px;
  font-weight: 600;
  color: #475569;
}

.cell-tag {
  font-size: 9.5px;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 0 4px;
  border-radius: 3px;
}

.cell-value {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin: 1px 0;
}

.cell-foot {
  margin-top: 1px;
}

.cell-remark {
  font-size: 10.5px;
  color: #94a3b8;
}

/* 示波器与参数热调分栏 */
.split-control-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.panel-section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 8px 8px 0 0;
}

.panel-section-title .t-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 700;
  color: #0f172a;
}

.sample-badge {
  font-size: 10.5px;
  color: #64748b;
}

.chart-wrapper {
  padding: 10px 14px;
}

.echarts-dom {
  width: 100%;
  height: 160px;
}

.param-form-container {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label {
  font-size: 11.5px;
  font-weight: 600;
  color: #334155;
}

.form-slider {
  gap: 1px;
}

.slider-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.slider-val {
  font-size: 11.5px;
  font-weight: 700;
  color: #2563eb;
}

.form-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
  margin-top: 2px;
}

/* 实控控制块样式 */
.direct-control-block {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.direct-control-block .block-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.direct-control-block .title-text {
  font-size: 12px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.direct-control-block .btn-group-grid {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.direct-control-block .control-subrow {
  display: flex;
  align-items: center;
  gap: 6px;
}

.control-tip-line {
  margin-top: 6px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tip-label {
  color: #64748b;
  font-weight: 500;
  font-size: 11px;
  flex-shrink: 0;
}

.tip-value {
  color: #475569;
  font-size: 11px;
}

.tip-desc {
  color: #94a3b8;
  font-size: 11px;
}

.cfg-text {
  color: #64748b;
  font-size: 11px;
}

.workflow-flow-desc {
  font-size: 11px;
  color: #64748b;
  background: #f1f5f9;
  border-radius: 4px;
  padding: 4px 8px;
  border-left: 3px solid #0284c7;
  line-height: 1.4;
}

/* ================= AGV 专属左侧遥测仪表板美化样式 ================= */
.agv-telemetry-panel {
  display: flex;
  flex-direction: column;
}

.t-right-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agv-telemetry-body {
  padding: 14px;
  background: #ffffff;
  flex: 1;
}

.agv-telemetry-body .agv-status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

/* 卡片单项 */
.agv-status-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 6px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
  transition: all 0.2s ease;
}

.agv-status-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.agv-status-card.battery-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #f8fafc 100%);
  border-color: #bbf7d0;
}

.agv-status-card.station-card {
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border-color: #bfdbfe;
}

.agv-status-card.time-card {
  background: #f8fafc;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-top .k-label {
  font-size: 11.5px;
  font-weight: 600;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-top .k-label i {
  font-size: 13px;
  color: #0284c7;
}

.card-main {
  display: flex;
  align-items: baseline;
  min-height: 28px;
}

.card-main .v-num {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.card-main .highlight-text {
  font-size: 14px;
  font-weight: 700;
  color: #0369a1;
}

.card-main .text-cyan {
  color: #0284c7;
}

.card-main .text-green {
  color: #059669;
}

.card-main .text-amber {
  color: #d97706;
}

.card-main .text-muted {
  font-size: 13px;
  color: #64748b;
}

.card-bottom.sub-text {
  font-size: 10.5px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 进度条与标签 */
.mini-bar-bg {
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.mini-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #059669);
  border-radius: 999px;
  transition: width 0.4s ease;
}

.v-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}

.v-tag.charging {
  background: #dcfce7;
  color: #15803d;
  border: 1px solid #86efac;
}

.v-tag.normal {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.v-tag.mode-tag {
  background: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.unit-tag {
  font-size: 10px;
  color: #94a3b8;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  padding: 0 4px;
  border-radius: 3px;
}

/* 状态徽章 */
.v-badge {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 4px;
}

.v-badge.badge-normal {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.v-badge.badge-warn {
  background: #fef3c7;
  color: #b45309;
  border: 1px solid #fde68a;
}

.v-badge.badge-danger {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fca5a5;
}

/* 状态圆点呼吸效果 */
.status-pulse-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  display: inline-block;
}

.status-pulse-dot.running {
  background: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

.status-pulse-dot.stopped {
  background: #f59e0b;
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2);
}

/* 站点拓扑布局 */
.station-flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.station-item {
  display: flex;
  flex-direction: column;
}

.station-item .s-label {
  font-size: 10px;
  color: #94a3b8;
}

.station-item .s-val {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.station-item .text-blue {
  color: #2563eb;
}

.station-arrow {
  color: #94a3b8;
  font-size: 14px;
  margin: 0 4px;
}

/* AGV 实时状态网格 (与原页面保持一致的深色工控风质感) */
.agv-status-grid {
  display: grid !important;
  grid-template-columns: repeat(3, 1fr) !important;
  gap: 8px !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

.agv-status-item {
  display: flex !important;
  flex-direction: column !important;
  justify-content: center !important;
  gap: 3px !important;
  background: #0f172a !important;
  border: 1px solid rgba(0, 229, 255, 0.25) !important;
  border-radius: 6px !important;
  padding: 8px 12px !important;
  box-sizing: border-box !important;
  min-height: 54px !important;
}

.agv-status-item .k {
  font-size: 11.5px !important;
  color: #94a3b8 !important;
  line-height: 1.2 !important;
}

.agv-status-item .v {
  font-size: 13.5px !important;
  font-weight: 700 !important;
  color: #f8fafc !important;
  font-family: 'Roboto Mono', 'SF Mono', Consolas, Monaco, monospace !important;
  line-height: 1.2 !important;
}

.agv-status-item .v.ok {
  color: #10b981;
}

.agv-status-item .v.warn {
  color: #f59e0b;
}

/* ================= 4. 底部总线报文监视器 ================= */
.bus-log-panel {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.bus-log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 8px 8px 0 0;
}

.log-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  font-weight: 700;
  color: #0f172a;
}

.bus-tag {
  font-size: 10.5px;
  color: #059669;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 1px 6px;
  border-radius: 3px;
}

.bus-log-table-wrap {
  max-height: 240px;
  overflow-y: auto;
}

.bus-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11.5px;
}

.bus-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  text-align: left;
  padding: 7px 12px;
  border-bottom: 1px solid #e2e8f0;
}

.bus-table td {
  padding: 7px 12px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

.bus-table tr:hover td {
  background: #f8fafc;
}

.text-muted {
  color: #94a3b8;
}

.text-right {
  text-align: right;
}

.source-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 3px;
}

.tag-server { background: #eff6ff; color: #2563eb; }
.tag-camera { background: #ecfdf5; color: #059669; }
.tag-arm { background: #fffbeb; color: #d97706; }
.tag-agv { background: #f5f3ff; color: #7c3aed; }
.tag-sys { background: #f1f5f9; color: #475569; }

.ack-badge {
  font-size: 10px;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
  padding: 1px 6px;
  border-radius: 3px;
}
</style>
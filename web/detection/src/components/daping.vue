<template>
  <div class="smart-factory">
    <!-- 背景网格与科技感效果 -->
    <div class="bg-grid"></div>
    <div class="bg-glow"></div>

    <!-- 顶部导航 -->
    <header class="factory-header">
      <div class="header-content">
        <h1 class="main-title">智能生产监控中心</h1>
        <div class="header-controls">
          <el-button 
            icon="el-icon-refresh" 
            size="mini" 
            class="refresh-btn"
            @click="refreshData"
          ></el-button>
          <el-select v-model="timeRange" size="mini" class="time-select">
            <el-option label="实时监控" value="realtime"></el-option>
            <el-option label="今日数据" value="today"></el-option>
            <el-option label="本周趋势" value="week"></el-option>
          </el-select>
          <div class="system-status">
            <i class="el-icon-check-circle"></i> 系统正常运行中
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="factory-layout">
      <!-- 左边车间区域 -->
      <div class="workshops-left">
        <!-- 一车间 -->
        <section class="workshop workshop-1" :class="{ active: activeWorkshop === 1 }">
          <div class="workshop-header">
            <h2 class="workshop-title">一车间 <span class="workshop-desc">切割下料 · 压花键</span></h2>
            <div class="workshop-status">
              <span class="status-indicator online"></span>
              <span class="status-text">正常生产</span>
            </div>
          </div>

          <!-- 设备状态面板 -->
          <div class="equipment-panel">
            <div class="equipment-status">
              <div class="status-item">
                <span class="status-label">总状态</span>
                <span class="status-value normal">正常</span>
              </div>
              <div class="status-item">
                <span class="status-label">运行设备</span>
                <span class="status-value">12/12</span>
              </div>
              <div class="status-item">
                <span class="status-label">生产效率</span>
                <span class="status-value">{{ workshopData[0].efficiency.toFixed(1) }}%</span>
              </div>
            </div>

            <!-- 核心设备可视化 -->
            <div class="equipment-visual">
              <div class="machine machine-1">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">压花键设备</div>
                  <div class="machine-metrics">
                    <div class="metric">速度: {{ workshopData[0].metrics.speed }}</div>
                    <div class="metric">温度: {{ workshopData[0].metrics.temperature }}</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
              <div class="machine machine-2">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">切割设备</div>
                  <div class="machine-metrics">
                    <div class="metric">精度: {{ workshopData[0].metrics.precision }}</div>
                    <div class="metric">利用率: {{ workshopData[0].metrics.utilization }}%</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
            </div>
          </div>

          <!-- 生产数据图表 -->
          <div class="production-chart">
            <div ref="workshop1Chart" class="chart-container"></div>
          </div>

          <!-- 溯源信息 -->
          <div class="traceability-section">
            <div class="trace-header">
              <h3>区块链溯源</h3>
              <el-button 
                size="mini" 
                class="view-details"
                @click="showTraceDetails(1)"
              >
                查看详情
              </el-button>
            </div>
            <div class="trace-metrics">
              <div class="trace-metric">
                <div class="metric-label">上链率</div>
                <div class="metric-value">100%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">溯源成功率</div>
                <div class="metric-value">99.7%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">最新区块</div>
                <div class="metric-value hash-value">{{ workshopData[0].latestBlock }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 二车间 -->
        <section class="workshop workshop-2" :class="{ active: activeWorkshop === 2 }">
          <div class="workshop-header">
            <h2 class="workshop-title">二车间 <span class="workshop-desc">钻中心孔 · 粗抛丸</span></h2>
            <div class="workshop-status">
              <span class="status-indicator online"></span>
              <span class="status-text">正常生产</span>
            </div>
          </div>

          <div class="equipment-panel">
            <div class="equipment-status">
              <div class="status-item">
                <span class="status-label">总状态</span>
                <span class="status-value normal">正常</span>
              </div>
              <div class="status-item">
                <span class="status-label">运行设备</span>
                <span class="status-value">8/8</span>
              </div>
              <div class="status-item">
                <span class="status-label">生产效率</span>
                <span class="status-value">{{ workshopData[1].efficiency.toFixed(1) }}%</span>
              </div>
            </div>

            <div class="equipment-visual">
              <div class="machine machine-1">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">钻中心孔设备</div>
                  <div class="machine-metrics">
                    <div class="metric">孔径: {{ workshopData[1].metrics.aperture }}</div>
                    <div class="metric">速度: {{ workshopData[1].metrics.speed }}</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
              <div class="machine machine-2">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">粗抛丸设备</div>
                  <div class="machine-metrics">
                    <div class="metric">压力: {{ workshopData[1].metrics.pressure }}</div>
                    <div class="metric">覆盖率: {{ workshopData[1].metrics.coverage }}%</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
            </div>
          </div>

          <div class="production-chart">
            <div ref="workshop2Chart" class="chart-container"></div>
          </div>

          <div class="traceability-section">
            <div class="trace-header">
              <h3>区块链溯源</h3>
              <el-button 
                size="mini" 
                class="view-details"
                @click="showTraceDetails(2)"
              >
                查看详情
              </el-button>
            </div>
            <div class="trace-metrics">
              <div class="trace-metric">
                <div class="metric-label">上链率</div>
                <div class="metric-value">100%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">溯源成功率</div>
                <div class="metric-value">99.5%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">最新区块</div>
                <div class="metric-value hash-value">{{ workshopData[1].latestBlock }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 中间实时数据区域 -->
      <div class="center-realtime-panel">
        <div class="realtime-header">
          <h2><i class="el-icon-data-analysis"></i> 实时生产数据监控</h2>
          <div class="update-time">最后更新: {{ updateTime }}</div>
        </div>
        
        <!-- 核心指标卡片 -->
        <div class="core-metrics">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="metric-content">
              <div class="metric-label">当前产量</div>
              <div class="metric-value">{{ dynamicData.production.toLocaleString() }}</div>
              <div class="metric-unit">件</div>
              <div class="metric-trend">
                <span class="trend-up">↑ 12.5%</span> 较昨日
              </div>
            </div>
          </div>
          
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-check"></i>
            </div>
            <div class="metric-content">
              <div class="metric-label">综合合格率</div>
              <div class="metric-value">{{ dynamicData.qualityRate }}</div>
              <div class="metric-unit">%</div>
              <div class="metric-trend">
                <span class="trend-up">↑ 0.8%</span> 较上周
              </div>
            </div>
          </div>
          
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="metric-content">
              <div class="metric-label">设备综合利用率</div>
              <div class="metric-value">{{ dynamicData.equipmentUsage }}</div>
              <div class="metric-unit">%</div>
              <div class="metric-trend">
                <span class="trend-down">↓ 1.2%</span> 较昨日
              </div>
            </div>
          </div>
          
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-timer"></i>
            </div>
            <div class="metric-content">
              <div class="metric-label">平均生产周期</div>
              <div class="metric-value">28.4</div>
              <div class="metric-unit">分钟</div>
              <div class="metric-trend">
                <span class="trend-up">↓ 5.2%</span> 较上周
              </div>
            </div>
          </div>
        </div>
        
        <!-- 实时趋势图表 -->
        <div class="realtime-chart-section">
          <div class="chart-header">
            <h3>生产效率实时趋势</h3>
            <div class="chart-controls">
              <el-radio-group v-model="chartType" size="mini">
                <el-radio-button label="line">折线图</el-radio-button>
                <el-radio-button label="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="realtimeChart" class="realtime-chart-container"></div>
        </div>
        
        <!-- 生产进度 -->
        <div class="production-progress">
          <h3>今日生产进度</h3>
          <div class="progress-grid">
            <div class="progress-item">
              <div class="progress-info">
                <span class="progress-label">计划产量</span>
                <span class="progress-value">15,000 件</span>
              </div>
              <el-progress 
                :percentage="85" 
                :stroke-width="10"
                color="#00f2ff"
              />
            </div>
            <div class="progress-item">
              <div class="progress-info">
                <span class="progress-label">已完成</span>
                <span class="progress-value">12,847 件</span>
              </div>
              <el-progress 
                :percentage="85.6" 
                :stroke-width="10"
                color="#00ff99"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右边车间区域 -->
      <div class="workshops-right">
        <!-- 三车间 -->
        <section class="workshop workshop-3" :class="{ active: activeWorkshop === 3 }">
          <div class="workshop-header">
            <h2 class="workshop-title">三车间 <span class="workshop-desc">精车杆 · 精校</span></h2>
            <div class="workshop-status">
              <span class="status-indicator warning"></span>
              <span class="status-text">需关注</span>
            </div>
          </div>

          <div class="equipment-panel">
            <div class="equipment-status">
              <div class="status-item">
                <span class="status-label">总状态</span>
                <span class="status-value warning">需关注</span>
              </div>
              <div class="status-item">
                <span class="status-label">运行设备</span>
                <span class="status-value">7/8</span>
              </div>
              <div class="status-item">
                <span class="status-label">生产效率</span>
                <span class="status-value">{{ workshopData[2].efficiency.toFixed(1) }}%</span>
              </div>
            </div>

            <div class="equipment-visual">
              <div class="machine machine-1">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">精车杆设备</div>
                  <div class="machine-metrics">
                    <div class="metric">精度: {{ workshopData[2].metrics.precision }}</div>
                    <div class="metric">温度: {{ workshopData[2].metrics.temperature }}</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
              <div class="machine machine-2">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">精校设备</div>
                  <div class="machine-metrics">
                    <div class="metric">状态: {{ workshopData[2].metrics.status }}</div>
                    <div class="metric">预计: 1小时后恢复</div>
                  </div>
                </div>
                <div class="machine-status offline"></div>
              </div>
            </div>
          </div>

          <div class="production-chart">
            <div ref="workshop3Chart" class="chart-container"></div>
          </div>

          <div class="traceability-section">
            <div class="trace-header">
              <h3>区块链溯源</h3>
              <el-button 
                size="mini" 
                class="view-details"
                @click="showTraceDetails(3)"
              >
                查看详情
              </el-button>
            </div>
            <div class="trace-metrics">
              <div class="trace-metric">
                <div class="metric-label">上链率</div>
                <div class="metric-value">98.2%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">溯源成功率</div>
                <div class="metric-value">99.1%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">最新区块</div>
                <div class="metric-value hash-value">{{ workshopData[2].latestBlock }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 四车间 -->
        <section class="workshop workshop-4" :class="{ active: activeWorkshop === 4 }">
          <div class="workshop-header">
            <h2 class="workshop-title">四车间 <span class="workshop-desc">探伤 · 包装</span></h2>
            <div class="workshop-status">
              <span class="status-indicator online"></span>
              <span class="status-text">正常生产</span>
            </div>
          </div>

          <div class="equipment-panel">
            <div class="equipment-status">
              <div class="status-item">
                <span class="status-label">总状态</span>
                <span class="status-value normal">正常</span>
              </div>
              <div class="status-item">
                <span class="status-label">运行设备</span>
                <span class="status-value">5/5</span>
              </div>
              <div class="status-item">
                <span class="status-label">合格率</span>
                <span class="status-value">{{ workshopData[3].efficiency.toFixed(1) }}%</span>
              </div>
            </div>

            <div class="equipment-visual">
              <div class="machine machine-1">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">超声波探伤</div>
                  <div class="machine-metrics">
                    <div class="metric">灵敏度: {{ workshopData[3].metrics.sensitivity }}</div>
                    <div class="metric">检测速度: {{ workshopData[3].metrics.speed }}</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
              <div class="machine machine-2">
                <div class="machine-icon">
                  <i class="el-icon-cog"></i>
                </div>
                <div class="machine-info">
                  <div class="machine-name">自动包装线</div>
                  <div class="machine-metrics">
                    <div class="metric">速度: {{ workshopData[3].metrics.packSpeed }}</div>
                    <div class="metric">完成率: {{ workshopData[3].metrics.completionRate }}%</div>
                  </div>
                </div>
                <div class="machine-status online"></div>
              </div>
            </div>
          </div>

          <div class="production-chart">
            <div ref="workshop4Chart" class="chart-container"></div>
          </div>

          <div class="traceability-section">
            <div class="trace-header">
              <h3>区块链溯源</h3>
              <el-button 
                size="mini" 
                class="view-details"
                @click="showTraceDetails(4)"
              >
                查看详情
              </el-button>
            </div>
            <div class="trace-metrics">
              <div class="trace-metric">
                <div class="metric-label">上链率</div>
                <div class="metric-value">100%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">溯源成功率</div>
                <div class="metric-value">99.9%</div>
              </div>
              <div class="trace-metric">
                <div class="metric-label">最新区块</div>
                <div class="metric-value hash-value">{{ workshopData[3].latestBlock }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- 溯源详情弹窗 -->
    <el-dialog 
      title="车间区块链溯源详情" 
      :visible.sync="traceDialogVisible" 
      width="70%" 
      class="trace-dialog"
      custom-class="custom-dialog"
    >
      <div class="trace-detail-content" v-if="currentTraceData">
        <div class="trace-header-info">
          <div class="trace-workshop">{{ currentTraceData.workshop }} - 区块链数据链</div>
          <div class="trace-stats">
            <div class="stat-item">
              <span class="stat-label">区块总数</span>
              <span class="stat-value">{{ currentTraceData.totalBlocks }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">数据完整率</span>
              <span class="stat-value">{{ currentTraceData.integrity }}%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">最后同步</span>
              <span class="stat-value">{{ formatTime(currentTraceData.lastSync) }}</span>
            </div>
          </div>
        </div>

        <!-- 区块链可视化 -->
        <div class="blockchain-visual">
          <div class="chain-container">
            <div class="block" v-for="(block, index) in currentTraceData.blocks" :key="index">
              <div class="block-header">
                <div class="block-number">#{{ block.number }}</div>
                <div class="block-hash">{{ block.hash.substring(0, 16) }}...</div>
              </div>
              <div class="block-body">
                <div class="block-data">
                  <div class="data-item">产品ID: {{ block.productId }}</div>
                  <div class="data-item">工序: {{ block.process }}</div>
                  <div class="data-item">操作员: {{ block.operator }}</div>
                </div>
                <div class="block-timestamp">{{ formatTime(block.timestamp) }}</div>
              </div>
              <div class="block-footer" :class="block.valid ? 'valid' : 'invalid'">
                <i class="el-icon-check" v-if="block.valid"></i>
                <i class="el-icon-close" v-else></i>
                {{ block.valid ? '数据有效' : '验证失败' }}
              </div>
            </div>
            <div class="chain-connector" v-for="i in currentTraceData.blocks.length - 1" :key="'conn' + i"></div>
          </div>
        </div>

        <!-- 溯源查询 -->
        <div class="trace-query">
          <h4>产品溯源查询</h4>
          <div class="query-form">
            <el-input 
              v-model="queryProductId" 
              placeholder="输入产品编号" 
              size="small"
              class="product-input"
            ></el-input>
            <el-button 
              type="primary" 
              size="small"
              @click="queryTrace()"
            >
              立即查询
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'SmartWorkshopMonitor',
  data() {
    return {
      // 基础状态
      timeRange: 'realtime',
      activeWorkshop: 1,
      traceDialogVisible: false,
      currentTraceData: null,
      queryProductId: '',
      chartType: 'line',
      updateTime: new Date().toLocaleTimeString(),
      
      // 动态数据
      dynamicData: {
        production: 12847,
        qualityRate: 98.6,
        equipmentUsage: 92.3
      },
      
      // 车间数据
      workshopData: [
        {
          id: 1,
          name: '一车间',
          processes: ['切割下料', '压花键'],
          status: 'normal',
          onlineCount: 12,
          totalCount: 12,
          efficiency: 96.8,
          chartData: [45, 52, 49, 63, 58, 67, 72, 68],
          latestBlock: '0x7d3f8a1b4...',
          timeLabels: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
          metrics: {
            speed: '450r/min',
            temperature: '58°C',
            precision: '±0.02mm',
            utilization: 92
          }
        },
        {
          id: 2,
          name: '二车间',
          processes: ['钻中心孔', '粗抛丸'],
          status: 'normal',
          onlineCount: 8,
          totalCount: 8,
          efficiency: 94.2,
          chartData: [38, 42, 45, 41, 44, 48, 46, 50],
          latestBlock: '0x6c2e7b0a3...',
          timeLabels: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
          metrics: {
            aperture: '12.5mm',
            speed: '380r/min',
            pressure: '0.6MPa',
            coverage: 98
          }
        },
        {
          id: 3,
          name: '三车间',
          processes: ['精车杆', '精校'],
          status: 'warning',
          onlineCount: 7,
          totalCount: 8,
          efficiency: 89.3,
          chartData: [52, 55, 48, 42, 46, 50, 45, 49],
          latestBlock: '0x5b1d6a2e7...',
          timeLabels: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
          metrics: {
            precision: '±0.005mm',
            temperature: '62°C',
            status: '维护中'
          }
        },
        {
          id: 4,
          name: '四车间',
          processes: ['探伤', '包装'],
          status: 'normal',
          onlineCount: 5,
          totalCount: 5,
          efficiency: 99.2,
          chartData: [28, 32, 35, 30, 33, 36, 34, 38],
          latestBlock: '0x4a0c5b1d6...',
          timeLabels: ['8:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'],
          metrics: {
            sensitivity: '0.5mm',
            speed: '3m/min',
            packSpeed: '12件/分钟',
            completionRate: 97
          }
        }
      ],
      
      // 图表实例
      charts: {},
      realtimeChart: null,
      
      // 定时器
      dataUpdateTimer: null
    };
  },
  mounted() {
    this.initCharts();
    this.initRealtimeChart();
    window.addEventListener('resize', this.handleResize);
    this.currentTraceData = this.generateTraceData(1);
    this.startDataUpdate();
  },
  beforeDestroy() {
    Object.values(this.charts).forEach(chart => chart.dispose());
    if (this.realtimeChart) {
      this.realtimeChart.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
    if (this.dataUpdateTimer) {
      clearInterval(this.dataUpdateTimer);
    }
  },
  methods: {
    initCharts() {
      const chart1 = echarts.init(this.$refs.workshop1Chart);
      this.charts.workshop1 = chart1;
      this.setChartOption(chart1, this.workshopData[0], '#00f2ff');
      
      const chart2 = echarts.init(this.$refs.workshop2Chart);
      this.charts.workshop2 = chart2;
      this.setChartOption(chart2, this.workshopData[1], '#ff9966');
      
      const chart3 = echarts.init(this.$refs.workshop3Chart);
      this.charts.workshop3 = chart3;
      this.setChartOption(chart3, this.workshopData[2], '#ffcc00');
      
      const chart4 = echarts.init(this.$refs.workshop4Chart);
      this.charts.workshop4 = chart4;
      this.setChartOption(chart4, this.workshopData[3], '#00ff99');
    },
    initRealtimeChart() {
      this.realtimeChart = echarts.init(this.$refs.realtimeChart);
      const timeLabels = [];
      const efficiencyData = [];
      const qualityData = [];
      for (let i = 0; i < 24; i++) {
        const hour = i.toString().padStart(2, '0');
        timeLabels.push(`${hour}:00`);
        efficiencyData.push(Math.floor(Math.random() * 20) + 80);
        qualityData.push(Math.floor(Math.random() * 15) + 85);
      }
      this.updateRealtimeChart(timeLabels, efficiencyData, qualityData);
    },
    updateRealtimeChart(timeLabels, efficiencyData, qualityData) {
      const option = {
        grid: { left: '5%', right: '5%', top: '15%', bottom: '15%', containLabel: true },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(10, 16, 40, 0.9)',
          borderColor: '#1890ff',
          textStyle: { color: '#ffffff' },
          axisPointer: { type: 'cross', label: { backgroundColor: '#1890ff' } }
        },
        legend: {
          data: ['生产效率', '产品质量'],
          textStyle: { color: 'rgba(255, 255, 255, 0.7)' },
          top: '5%'
        },
        xAxis: {
          type: 'category',
          data: timeLabels,
          axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
          axisLabel: { color: 'rgba(255, 255, 255, 0.6)', fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          min: 70, max: 100,
          axisLine: { show: false },
          axisLabel: { color: 'rgba(255, 255, 255, 0.6)', fontSize: 10, formatter: '{value}%' },
          splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.05)', type: 'dashed' } }
        },
        series: [
          {
            name: '生产效率', type: this.chartType, data: efficiencyData, smooth: true, symbol: 'circle', symbolSize: 4,
            lineStyle: { width: 3, color: '#00f2ff' }, itemStyle: { color: '#00f2ff' },
            areaStyle: this.chartType === 'line' ? { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0, 242, 255, 0.3)' }, { offset: 1, color: 'rgba(0, 242, 255, 0.05)' }]) } : null
          },
          {
            name: '产品质量', type: this.chartType, data: qualityData, smooth: true, symbol: 'circle', symbolSize: 4,
            lineStyle: { width: 3, color: '#00ff99' }, itemStyle: { color: '#00ff99' },
            areaStyle: this.chartType === 'line' ? { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0, 255, 153, 0.3)' }, { offset: 1, color: 'rgba(0, 255, 153, 0.05)' }]) } : null
          }
        ]
      };
      this.realtimeChart.setOption(option);
    },
    setChartOption(chart, workshopData, color) {
      chart.setOption({
        grid: { left: '5%', right: '5%', top: '10%', bottom: '10%', containLabel: true },
        xAxis: {
          type: 'category', data: workshopData.timeLabels,
          axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
          axisLabel: { color: 'rgba(255, 255, 255, 0.7)', fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          min: workshopData.status === 'warning' ? 30 : 50,
          max: workshopData.status === 'warning' ? 80 : 90,
          axisLine: { show: false },
          axisLabel: { color: 'rgba(255, 255, 255, 0.5)', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.05)' } }
        },
        series: [{
          type: 'line', data: workshopData.chartData, smooth: true, symbol: 'circle', symbolSize: 5,
          lineStyle: { width: 2, color: color },
          itemStyle: { color: color, borderColor: '#0f1736', borderWidth: 2 },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: color + '40' }, { offset: 1, color: color + '00' }]) }
        }]
      });
    },
    handleResize() {
      Object.values(this.charts).forEach(chart => { if (chart && !chart.isDisposed()) chart.resize(); });
      if (this.realtimeChart && !this.realtimeChart.isDisposed()) this.realtimeChart.resize();
    },
    refreshData() {
      this.workshopData.forEach(workshop => {
        workshop.chartData = workshop.chartData.map(val => Math.max(30, Math.min(90, val + (Math.random() - 0.5) * 10)));
      });
      this.workshopData.forEach((workshop, index) => {
        const chartKey = `workshop${index + 1}`;
        const color = ['#00f2ff', '#ff9966', '#ffcc00', '#00ff99'][index];
        this.setChartOption(this.charts[chartKey], workshop, color);
      });
      const timeLabels = [], efficiencyData = [], qualityData = [];
      for (let i = 0; i < 24; i++) {
        const hour = i.toString().padStart(2, '0');
        timeLabels.push(`${hour}:00`);
        efficiencyData.push(Math.floor(Math.random() * 20) + 80);
        qualityData.push(Math.floor(Math.random() * 15) + 85);
      }
      this.updateRealtimeChart(timeLabels, efficiencyData, qualityData);
      this.$message.success('数据已刷新');
    },
    startDataUpdate() {
      this.updateData();
      this.dataUpdateTimer = setInterval(() => { this.updateData(); }, 1500);
    },
    updateData() {
      this.updateTime = new Date().toLocaleTimeString();
      this.updateDynamicData();
      this.updateWorkshopCharts();
      if (this.realtimeChart) {
        const option = this.realtimeChart.getOption();
        const efficiencyData = option.series[0].data;
        const qualityData = option.series[1].data;
        const timeLabels = option.xAxis[0].data;
        efficiencyData.shift(); efficiencyData.push(Math.floor(Math.random() * 20) + 80);
        qualityData.shift(); qualityData.push(Math.floor(Math.random() * 15) + 85);
        const now = new Date();
        timeLabels.shift(); timeLabels.push(`${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`);
        this.realtimeChart.setOption({ xAxis: { data: timeLabels }, series: [{ data: efficiencyData }, { data: qualityData }] });
      }
    },
    updateDynamicData() {
      this.dynamicData.production = Math.floor(this.dynamicData.production + Math.random() * 3);
      this.dynamicData.qualityRate = Math.min(100, Math.max(95, this.dynamicData.qualityRate + (Math.random() * 0.4 - 0.2))).toFixed(1);
      this.dynamicData.equipmentUsage = Math.min(100, Math.max(85, this.dynamicData.equipmentUsage + (Math.random() * 0.6 - 0.3))).toFixed(1);
    },
    updateWorkshopCharts() {
      this.workshopData.forEach((workshop, index) => {
        const chartKey = `workshop${index + 1}`;
        const chart = this.charts[chartKey];
        if (chart && !chart.isDisposed()) {
          const currentData = workshop.chartData;
          const timeLabels = workshop.timeLabels;
          let newValue;
          if (workshop.status === 'normal') {
            const lastValue = currentData[currentData.length - 1] || 70;
            newValue = Math.max(60, Math.min(90, lastValue + (Math.random() * 10 - 5)));
          } else {
            const lastValue = currentData[currentData.length - 1] || 50;
            newValue = Math.max(40, Math.min(70, lastValue + (Math.random() * 16 - 8)));
          }
          currentData.shift(); currentData.push(newValue);
          timeLabels.shift();
          const now = new Date();
          timeLabels.push(`${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`);
          workshop.efficiency = newValue;
          chart.setOption({ xAxis: { data: timeLabels }, series: [{ data: currentData }] });
        }
      });
    },
    showTraceDetails(workshopId) {
      this.activeWorkshop = workshopId;
      this.currentTraceData = this.generateTraceData(workshopId);
      this.traceDialogVisible = true;
    },
    generateTraceData(workshopId) {
      const workshop = this.workshopData.find(w => w.id === workshopId);
      const blocks = [];
      for (let i = 0; i < 5; i++) {
        const now = Date.now() - (4 - i) * 3600000;
        blocks.push({
          number: 18472 + i,
          hash: `0x${Math.random().toString(16).substr(2, 32)}`,
          productId: `P20241129${workshopId}${i + 100}`,
          process: workshop.processes[i % workshop.processes.length],
          operator: `操作员${Math.floor(Math.random() * 10) + 1}`,
          timestamp: now,
          valid: Math.random() > 0.1
        });
      }
      return {
        workshop: workshop.name,
        totalBlocks: 1287 + workshopId * 32,
        integrity: 99 + (Math.random() * 1 - 0.2),
        lastSync: Date.now() - Math.random() * 60000,
        blocksRate: 24 + Math.random() * 10,
        blocks: blocks
      };
    },
    queryTrace() {
      if (!this.queryProductId) return;
      this.$message.success(`找到产品 ${this.queryProductId} 的溯源记录`);
    },
    formatTime(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleTimeString();
    }
  },
  watch: {
    chartType() {
      this.initRealtimeChart();
    }
  }
};
</script>

<style scoped>
/* 基础样式 - 关键修改 */
.smart-factory {
  width: 100%;
  height: 100vh; /* 强制高度等于视口高度 */
  overflow-y: auto; /* 在容器内部开启滚动 */
  overflow-x: hidden; /* 禁止横向滚动 */
  background-color: #0a1028;
  color: #fff;
  font-family: 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  position: relative;
  padding: 20px;
  box-sizing: border-box;
}

/* 背景效果 - 修改为 fixed 防止滚动时背景跑偏 */
.bg-grid {
  position: fixed; /* 改为固定定位 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 242, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 242, 255, 0.05) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 0;
  pointer-events: none;
}

.bg-glow {
  position: fixed; /* 改为固定定位 */
  top: 50%;
  left: 50%;
  width: 1200px;
  height: 800px;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(0, 100, 255, 0.1) 0%, rgba(10, 16, 40, 0) 70%);
  z-index: 0;
  pointer-events: none;
}

/* 头部样式 */
.factory-header {
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(15, 23, 54, 0.6);
  border: 1px solid rgba(0, 242, 255, 0.2);
  border-radius: 4px;
  backdrop-filter: blur(10px);
}

.main-title {
  margin: 0;
  font-size: 22px;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.3);
  letter-spacing: 1px;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.refresh-btn {
  background: transparent !important;
  border: 1px solid rgba(0, 242, 255, 0.5) !important;
  color: #00f2ff !important;
  width: 30px !important;
  height: 30px !important;
  border-radius: 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s ease !important;
}

.refresh-btn:hover {
  background: rgba(0, 242, 255, 0.1) !important;
  border-color: #00f2ff !important;
  box-shadow: 0 0 10px rgba(0, 242, 255, 0.3) !important;
}

.time-select {
  background: rgba(0, 0, 0, 0.3) !important;
  border: 1px solid rgba(0, 242, 255, 0.3) !important;
  color: #fff !important;
  width: 120px !important;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #00ff99;
}

/* 主布局样式 - 修改高度控制 */
.factory-layout {
  display: flex;
  gap: 20px;
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 120px); /* 保证最小高度 */
  height: auto; /* 允许撑开高度 */
  padding-bottom: 20px; /* 底部留白，防止内容贴边 */
}

/* 左右车间区域 */
.workshops-left,
.workshops-right {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 中间实时数据区域 */
.center-realtime-panel {
  flex: 1.6;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: rgba(15, 23, 54, 0.6);
  border: 1px solid rgba(0, 242, 255, 0.2);
  border-radius: 6px;
  padding: 20px;
  backdrop-filter: blur(5px);
}

.realtime-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.realtime-header h2 {
  margin: 0;
  font-size: 18px;
  color: #00f2ff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.update-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

/* 核心指标卡片 */
.core-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.metric-card {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 242, 255, 0.1);
  border-radius: 6px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.metric-card:hover {
  border-color: rgba(0, 242, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: rgba(0, 242, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #00f2ff;
}

.metric-content {
  flex: 1;
}

.metric-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 5px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 2px;
}

.metric-unit {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 5px;
}

.metric-trend {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.trend-up {
  color: #00ff99;
  font-weight: 500;
}

.trend-down {
  color: #ff6666;
  font-weight: 500;
}

/* 实时图表区域 */
.realtime-chart-section {
  flex: 1;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-header h3 {
  margin: 0;
  font-size: 14px;
  color: #fff;
}

.realtime-chart-container {
  flex: 1;
  min-height: 0;
}

/* 生产进度 */
.production-progress {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  padding: 15px;
}

.production-progress h3 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #fff;
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.progress-item {
  background: rgba(15, 23, 54, 0.4);
  border-radius: 4px;
  padding: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.progress-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.progress-value {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}

/* 车间面板样式 */
.workshop {
  background: rgba(15, 23, 54, 0.4);
  border: 1px solid rgba(0, 242, 255, 0.2);
  border-radius: 6px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  position: relative;
  overflow: hidden;
}

.workshop::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 242, 255, 0.5), transparent);
}

.workshop::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(0, 242, 255, 0.5), transparent);
}

.workshop.active {
  background: rgba(15, 23, 54, 0.6);
  border-color: rgba(0, 242, 255, 0.4);
  box-shadow: 0 0 20px rgba(0, 242, 255, 0.1);
}

.workshop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.workshop-title {
  margin: 0;
  font-size: 18px;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.workshop-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: normal;
  margin-top: 3px;
}

.workshop-status {
  display: flex;
  align-items: center;
  gap: 5px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator.online {
  background-color: #00ff99;
  box-shadow: 0 0 10px rgba(0, 255, 153, 0.5);
}

.status-indicator.warning {
  background-color: #ffcc00;
  box-shadow: 0 0 10px rgba(255, 204, 0, 0.5);
}

.status-indicator.offline {
  background-color: #ff6666;
  box-shadow: 0 0 10px rgba(255, 102, 102, 0.5);
}

.status-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

/* 设备面板 */
.equipment-panel {
  margin-bottom: 15px;
}

.equipment-status {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.status-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 3px;
}

.status-value {
  font-size: 14px;
  font-weight: 500;
}

.status-value.normal {
  color: #00ff99;
}

.status-value.warning {
  color: #ffcc00;
}

.equipment-visual {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.machine {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  border-left: 2px solid transparent;
  transition: all 0.2s ease;
}

.workshop-1 .machine {
  border-left-color: #00f2ff;
}

.workshop-2 .machine {
  border-left-color: #ff9966;
}

.workshop-3 .machine {
  border-left-color: #ffcc00;
}

.workshop-4 .machine {
  border-left-color: #00ff99;
}

.machine-icon {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f2ff;
  margin-right: 10px;
}

.machine-info {
  flex: 1;
}

.machine-name {
  font-size: 13px;
  margin-bottom: 2px;
}

.machine-metrics {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.machine-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* 生产图表 */
.production-chart {
  flex: 1;
  margin-bottom: 15px;
  min-height: 0;
  position: relative;
}

.chart-container {
  width: 100%;
  height: 100%;
  min-height: 100px;
}

/* 溯源区域 */
.traceability-section {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 10px;
  border: 1px solid rgba(0, 242, 255, 0.1);
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.trace-header h3 {
  margin: 0;
  font-size: 14px;
  color: #00f2ff;
}

.view-details {
  background: transparent !important;
  border: 1px solid rgba(0, 242, 255, 0.5) !important;
  color: #00f2ff !important;
  font-size: 12px !important;
  padding: 2px 8px !important;
  border-radius: 3px !important;
}

.trace-metrics {
  display: flex;
  justify-content: space-between;
}

.trace-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.metric-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 3px;
}

.metric-value {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.hash-value {
  font-family: monospace;
  font-size: 12px;
  color: #00f2ff;
}

/* 溯源详情弹窗 */
.trace-dialog {
  background: rgba(10, 16, 40, 0.95) !important;
  border: 1px solid rgba(0, 242, 255, 0.3) !important;
  color: #fff !important;
}

.custom-dialog .el-dialog__header {
  border-bottom: 1px solid rgba(0, 242, 255, 0.1);
}

.custom-dialog .el-dialog__title {
  color: #00f2ff !important;
  font-size: 18px !important;
}

.custom-dialog .el-dialog__close {
  color: rgba(255, 255, 255, 0.5) !important;
}

.trace-detail-content {
  padding: 10px 0;
}

.trace-header-info {
  margin-bottom: 20px;
}

.trace-workshop {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #fff;
}

.trace-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.stat-value {
  font-size: 13px;
  color: #00f2ff;
}

/* 区块链可视化 */
.blockchain-visual {
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.chain-container {
  display: flex;
  gap: 15px;
  padding: 10px 0;
  min-width: max-content;
}

.block {
  width: 200px;
  background: rgba(15, 23, 54, 0.8);
  border: 1px solid rgba(0, 242, 255, 0.3);
  border-radius: 6px;
  padding: 10px;
  position: relative;
}

.block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 242, 255, 0.5), transparent);
}

.block-header {
  margin-bottom: 8px;
}

.block-number {
  font-size: 14px;
  font-weight: 500;
  color: #00f2ff;
  margin-bottom: 3px;
}

.block-hash {
  font-size: 11px;
  font-family: monospace;
  color: rgba(255, 255, 255, 0.6);
}

.block-body {
  margin-bottom: 8px;
  padding: 5px 0;
  border-top: 1px dashed rgba(255, 255, 255, 0.1);
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

.block-data {
  margin-bottom: 5px;
}

.data-item {
  font-size: 12px;
  margin-bottom: 2px;
}

.block-timestamp {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.block-footer {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.block-footer.valid {
  color: #00ff99;
}

.block-footer.invalid {
  color: #ff6666;
}

.chain-connector {
  align-self: center;
  width: 20px;
  height: 1px;
  background: linear-gradient(90deg, rgba(0, 242, 255, 0.5), rgba(0, 242, 255, 0.2));
  position: relative;
}

.chain-connector::after {
  content: '';
  position: absolute;
  top: -3px;
  left: 100%;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #00f2ff;
}

/* 溯源查询 */
.trace-query h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #00f2ff;
}

.query-form {
  display: flex;
  gap: 10px;
}

.product-input {
  background: rgba(0, 0, 0, 0.3) !important;
  border: 1px solid rgba(0, 242, 255, 0.3) !important;
  color: #fff !important;
  width: 200px !important;
}

/* 响应式调整 */
@media (max-width: 1400px) {
  .factory-layout {
    flex-direction: column;
    height: auto;
    overflow-y: auto;
  }
  
  .workshops-left,
  .workshops-right {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .center-realtime-panel {
    order: -1;
    margin-bottom: 20px;
  }
}
</style>
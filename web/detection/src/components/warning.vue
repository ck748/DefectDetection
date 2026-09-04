<template>
  <div class="warning-management">
    <!-- 头部区域 (Header Section) -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-warning-outline"></i></span>
          <h2 class="page-title">预警与监控告警</h2>
          <span class="title-tag">工业告警中枢</span>
        </div>
        <p class="page-desc">实时监控边缘质检设备与系统异常告警，提供告警事件分级追溯、紧急度分布统计及自动化通知触达配置</p>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新告警数据" placement="top">
          <el-button size="small" icon="el-icon-refresh" circle @click="fetchData"></el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 运行指标透视条 (Stats Bar) -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">告警总数</span>
          <i class="el-icon-warning-outline stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ warningsSum || warningsList.length }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">已记录的历史预警总数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">24小时内告警</span>
          <span class="status-badge" :class="oneDayWarningsSum > 0 ? 'warning' : 'success'">
            <span class="dot"></span>{{ oneDayWarningsSum > 0 ? '近期活动' : '运行平稳' }}
          </span>
        </div>
        <div class="stat-value" :class="oneDayWarningsSum > 0 ? 'text-warning' : 'text-success'">
          <span class="num">{{ oneDayWarningsSum }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">近24小时触发的异常预警</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">高危告警 (级别 3)</span>
          <i class="el-icon-alarm-clock stat-icon"></i>
        </div>
        <div class="stat-value text-danger">
          <span class="num">{{ level3Count }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">需立即排查的最高级告警</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">告警通知通道</span>
          <span class="status-badge" :class="warningsOpen ? 'success' : 'info'">
            <span class="dot"></span>{{ warningsOpen ? '已启用' : '未启用' }}
          </span>
        </div>
        <div class="stat-value" :class="warningsOpen ? 'text-primary' : ''">
          <span class="num" style="font-size: 15px; font-weight: 600;">{{ warningsOpen ? (Way === '1' ? '短信推送' : '邮件推送') : '已停用' }}</span>
        </div>
        <div class="stat-foot">{{ warningsOpen ? (Way === '1' ? ('接收手机: ' + (phone || '未设置')) : ('接收邮箱: ' + (email || '未设置'))) : '异常事件不主动触达外部' }}</div>
      </div>
    </div>

    <!-- 主体布局 (Main Layout) -->
    <div class="main-layout">
      <!-- 左侧：预警事件明细列表 -->
      <div class="warning-list-section">
        <!-- 工具栏 -->
        <div class="toolbar-section">
          <div class="toolbar-left">
            <el-input
              v-model="searchName"
              placeholder="搜索缺陷名称..."
              class="search-input"
              prefix-icon="el-icon-search"
              size="small"
              clearable
            ></el-input>

            <el-select
              v-model="searchLevel"
              placeholder="紧急程度筛选"
              class="level-select"
              size="small"
              clearable
            >
              <el-option label="全部紧急程度" value=""></el-option>
              <el-option label="紧急程度 3 (高危)" value="3"></el-option>
              <el-option label="紧急程度 2 (警告)" value="2"></el-option>
              <el-option label="紧急程度 1 (提示)" value="1"></el-option>
            </el-select>

            <el-button size="small" icon="el-icon-refresh-left" @click="resetSearch">重置</el-button>
          </div>

          <div class="toolbar-right">
            <span class="filter-tip">当前共 <b>{{ filteredWarnings.length }}</b> 条记录</span>
          </div>
        </div>

        <!-- 表格容器 -->
        <div class="table-container">
          <el-table
            :data="paginatedWarnings"
            border
            stripe
            style="width: 100%"
            class="warning-table"
            empty-text="暂无符合条件的预警记录"
          >
            <el-table-column prop="axleCode" label="半轴编号" width="150" align="center">
              <template slot-scope="scope">
                <span class="axle-code-badge">{{ scope.row.axleCode || scope.row.code || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="缺陷名称" min-width="120" align="center">
              <template slot-scope="scope">
                <span class="warning-type-text" :class="scope.row.type === '裂纹' ? 'defect-crack' : 'defect-scratch'">{{ scope.row.type }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="质量类别" width="130" align="center">
              <template slot-scope="scope">
                <span class="quality-category-badge">{{ scope.row.category || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="紧急程度" width="130" align="center">
              <template slot-scope="scope">
                <span class="status-cell" :class="scope.row.level == 3 ? 'status-danger' : (scope.row.level == 2 ? 'status-warning' : 'status-primary')">
                  <span class="dot"></span>
                  {{ getLevelLabel(scope.row.level) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发生时间" width="170" align="center">
              <template slot-scope="scope">
                <span class="time-cell">
                  <i class="el-icon-time"></i>
                  {{ scope.row.createTime }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="质量分析" min-width="200" show-overflow-tooltip>
              <template slot-scope="scope">
                <span class="analysis-placeholder">{{ scope.row.content || '' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-footer">
          <div class="pagination-total">
            共 <span class="total-count">{{ filteredWarnings.length }}</span> 条预警记录
          </div>
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page.sync="currentPage"
            :page-size="pageSize"
            :total="filteredWarnings.length"
            @current-change="handleCurrentChange"
          >
          </el-pagination>
        </div>
      </div>

      <!-- 右侧：监控分析与通知配置面板 -->
      <div class="warning-config-section">
        <!-- 卡片 1：紧急程度分布图表 -->
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <i class="el-icon-data-line"></i>
              <span>紧急程度分布</span>
            </div>
            <span class="panel-subtitle">按告警级别统计</span>
          </div>
          <div class="panel-body chart-panel-body">
            <div :id="chartId" class="chart-container"></div>
            <div class="level-breakdown">
              <div class="breakdown-item level-1-item">
                <span class="dot"></span>
                <span class="name">级别 1 (提示)</span>
                <span class="count">{{ level1Count }}</span>
              </div>
              <div class="breakdown-item level-2-item">
                <span class="dot"></span>
                <span class="name">级别 2 (警告)</span>
                <span class="count">{{ level2Count }}</span>
              </div>
              <div class="breakdown-item level-3-item">
                <span class="dot"></span>
                <span class="name">级别 3 (高危)</span>
                <span class="count">{{ level3Count }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 卡片 2：告警通知配置 -->
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <i class="el-icon-setting"></i>
              <span>告警通知配置</span>
            </div>
            <el-tag size="mini" :type="warningsOpen ? 'success' : 'info'" effect="plain">
              {{ warningsOpen ? '运行中' : '已停用' }}
            </el-tag>
          </div>
          <div class="panel-body">
            <div class="config-form-item form-row-inline">
              <span class="form-label">启用告警通知</span>
              <el-switch
                v-model="warningsOpen"
                active-color="#1890ff"
                inactive-color="#dcdfe6"
              ></el-switch>
            </div>

            <div class="config-form-item">
              <span class="form-label">触发告警级别</span>
              <el-radio-group v-model="warningsLevel" size="small" class="level-radio-group">
                <el-radio-button label="1">级别 1</el-radio-button>
                <el-radio-button label="2">级别 2</el-radio-button>
                <el-radio-button label="3">级别 3</el-radio-button>
              </el-radio-group>
            </div>

            <div class="config-form-item">
              <span class="form-label">通知触达方式</span>
              <el-radio-group v-model="Way" size="small" class="way-radio-group">
                <el-radio label="1"><i class="el-icon-mobile-phone"></i> 短信通知</el-radio>
                <el-radio label="2"><i class="el-icon-message"></i> 邮件通知</el-radio>
              </el-radio-group>
            </div>

            <div class="config-form-item">
              <span class="form-label">接收手机号码</span>
              <el-input 
                v-model="phone" 
                placeholder="请输入接收手机号码"
                prefix-icon="el-icon-mobile-phone"
                size="small"
                :disabled="Way !== '1'"
              ></el-input>
            </div>

            <div class="config-form-item">
              <span class="form-label">接收邮箱地址</span>
              <el-input 
                v-model="email" 
                placeholder="请输入接收邮箱地址"
                prefix-icon="el-icon-message"
                size="small"
                :disabled="Way !== '2'"
              ></el-input>
            </div>

            <div class="config-btn-wrap">
              <el-button 
                type="primary" 
                icon="el-icon-check" 
                size="small" 
                class="save-config-btn"
                @click="sendDataToBackend"
              >
                保存通知配置
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'WarningManagement',
  data() {
    return {
      warningsOpen: false,
      warningsLevel: '1',
      Way: '1',
      phone: '1234567890',
      email: '12345@qq.com',
      searchName: '',
      searchLevel: '',
      warningsList: [
        {
          axleCode: '2403511-P301',
          type: '轴颈划痕',
          category: '表面缺陷',
          level: '2',
          createTime: '2026-09-12 14:22:08',
          content: '轴颈外圆柱面出现深约0.15mm轴向线状划痕，疑似上料机械手夹爪异物刮伤'
        },
        {
          axleCode: '248-005-012',
          type: '花键齿部磨损',
          category: '尺寸精度',
          level: '1',
          createTime: '2026-09-10 07:11:25',
          content: '花键分度圆齿面伴随轻微点蚀磨损，啮合面接触应力集中偏高'
        },
        {
          axleCode: 'CN744139',
          type: '杆部微裂纹',
          category: '探伤缺陷',
          level: '3',
          createTime: '2026-09-04 12:44:38',
          content: '杆部近法兰过渡圆角处超声探伤出现微弱反射回波，表面存在微观疲劳裂纹'
        },
        {
          axleCode: '2403512-P302',
          type: '凸缘端面气孔',
          category: '表面缺陷',
          level: '3',
          createTime: '2026-08-29 18:20:15',
          content: '凸缘连接端面边缘分布密集针状微小气孔，铸锻成型排气不畅'
        },
        {
          axleCode: '248-005-018',
          type: '表面麻点凹坑',
          category: '表面缺陷',
          level: '3',
          createTime: '2026-08-23 23:55:40',
          content: '轴身中段局部氧化皮剥落形成密集凹坑麻点，影响后续镀层附着'
        },
        {
          axleCode: 'CN744140',
          type: '花键跳动超差',
          category: '形位公差',
          level: '3',
          createTime: '2026-08-20 10:30:15',
          content: '花键齿圈径向全跳动达到0.045mm，超过标准允许值(≤0.02mm)'
        },
        {
          axleCode: '2403515-P305',
          type: '轴身弯曲变形',
          category: '形位公差',
          level: '3',
          createTime: '2026-08-18 05:40:12',
          content: '经线激光测量轴线直线度误差超差0.32mm，校直工序回弹补偿不足'
        },
        {
          axleCode: '248-006-003',
          type: '热处理硬度不足',
          category: '金相材质',
          level: '3',
          createTime: '2026-08-12 11:22:36',
          content: '轴身截面里氏硬度仅为48HRC（标准要求52-58HRC），高频淬火加热功率波动'
        },
        {
          axleCode: 'CN744145',
          type: '倒角毛刺残留',
          category: '加工缺陷',
          level: '3',
          createTime: '2026-08-06 15:45:18',
          content: '法兰外圆倒角边缘存在明显金属毛刺飞边未彻底清理'
        },
        {
          axleCode: '2403520-P310',
          type: '淬火裂纹',
          category: '探伤缺陷',
          level: '2',
          createTime: '2026-08-01 16:45:22',
          content: '轴肩R角应力集中区在磁粉探伤(MT)下显示明显纵向淬火微裂纹'
        },
        {
          axleCode: '248-007-009',
          type: '轴承位拉伤',
          category: '表面缺陷',
          level: '2',
          createTime: '2026-07-28 14:18:30',
          content: '轴承配合面在压装工装推送过程中产生轴向严重金属挤压拉伤痕迹'
        },
        {
          axleCode: 'CN744152',
          type: '螺纹磕碰损伤',
          category: '机械损伤',
          level: '2',
          createTime: '2026-07-20 09:30:15',
          content: '轴端外螺纹前三扣存在磕碰变形，通规旋入受阻'
        },
        {
          axleCode: '2403522-P315',
          type: '夹杂物超标',
          category: '金相材质',
          level: '3',
          createTime: '2026-07-15 11:20:45',
          content: '光谱分析显示局部非金属夹杂物评级超标，母材纯净度偏低'
        },
        {
          axleCode: '248-008-011',
          type: '锻造折叠',
          category: '探伤缺陷',
          level: '1',
          createTime: '2026-07-08 16:05:20',
          content: '锻造模具分模面闭合不良导致局部金属流线折叠重叠'
        },
        {
          axleCode: 'CN744160',
          type: '渗碳层厚度偏薄',
          category: '热处理工艺',
          level: '2',
          createTime: '2026-06-30 10:15:40',
          content: '渗碳淬火有效硬化层深度为0.85mm（工艺要求1.2-1.5mm），保温时间偏短'
        }
      ],
      warningsSum: 15,
      oneDayWarningsSum: 0,
      levelDistributionData: {
        '1': 4,
        '2': 7,
        '3': 4
      },
      chartId: 'warning-chart',
      barChart: {
        name: '紧急程度分布',
        source: {
          '紧急程度 1': 4,
          '紧急程度 2': 7,
          '紧急程度 3': 4
        }
      },
      chartInstance: null,
      currentPage: 1,
      pageSize: 10,
      isInitLoading: true // 初次加载防抖标记，防止watch自动触发多条保存弹窗
    };
  },
  computed: {
    level3Count() {
      return this.warningsList.filter(item => String(item.level) === '3').length;
    },
    level2Count() {
      return this.warningsList.filter(item => String(item.level) === '2').length;
    },
    level1Count() {
      return this.warningsList.filter(item => String(item.level) === '1').length;
    },
    filteredWarnings() {
      let list = [...this.warningsList];
      if (this.searchName) {
        const query = this.searchName.toLowerCase().trim();
        list = list.filter(w => w.type && w.type.toLowerCase().includes(query));
      }
      if (this.searchLevel) {
        list = list.filter(w => String(w.level) === String(this.searchLevel));
      }
      list.sort((a, b) => {
        const timeA = a.createTime ? new Date(a.createTime.replace('T', ' ')).getTime() : 0;
        const timeB = b.createTime ? new Date(b.createTime.replace('T', ' ')).getTime() : 0;
        return timeB - timeA;
      });
      return list;
    },
    paginatedWarnings() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.filteredWarnings.slice(start, start + this.pageSize);
    }
  },
  mounted() {
    this.fetchData();
    this.$nextTick(() => {
      this.drawChart();
    });
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
  },
  watch: {
    warningsOpen(newVal) {
      if (this.isInitLoading) return;
      console.log('告警通知开关变化:', newVal);
      this.sendDataToBackend();
    },
    warningsLevel(newVal) {
      if (this.isInitLoading) return;
      console.log('告警级别变化:', newVal);
      this.sendDataToBackend();
    },
    Way(newVal) {
      if (this.isInitLoading) return;
      console.log('通知方式变化:', newVal);
      this.sendDataToBackend();
    },
    phone(newVal) {
      if (this.isInitLoading) return;
      if (this.Way === '1' && newVal) {
        this.sendDataToBackend();
      }
    },
    email(newVal) {
      if (this.isInitLoading) return;
      if (this.Way === '2' && newVal) {
        this.sendDataToBackend();
      }
    }
  },
  methods: {
    handleCurrentChange(val) {
      this.currentPage = val;
    },
    resetSearch() {
      this.searchName = '';
      this.searchLevel = '';
      this.currentPage = 1;
    },
    getLevelLabel(level) {
      const map = {
        '1': '提示 · 级别 1',
        '2': '警告 · 级别 2',
        '3': '高危 · 级别 3'
      };
      return map[String(level)] || ('级别 ' + level);
    },
    getLevelTagType(level) {
      const tagMap = {
        '1': 'info',
        '2': 'warning',
        '3': 'danger'
      };
      return tagMap[String(level)] || 'info';
    },
    fetchData() {
      // 预置各类工业批次离散抽检半轴编号池
      const axleCodePool = [
        '2403511-P301', 'CN744139', '248-005-012', 'BJ892301-B', '2401882-P108',
        'CN812044', '310-942-005', '2409733-P812', 'TJ-449102', '248-019-088',
        'CN650391', '2404619-P504', 'HB-772910', '248-031-104', 'CN903421',
        '2405820-P610', '310-955-018', 'CN762190', 'SH-551023', '248-042-201'
      ];
      // 裂纹质量类别池
      const crackCategoryPool = ['内部探伤缺陷', '表面微裂纹', '结构完整性', '无损探伤', '金相疲劳', '应力开裂'];
      // 划痕质量类别池
      const scratchCategoryPool = ['外观表面缺陷', '机械加工划伤', '轴颈表面损伤', '装配划损', '配合面划痕', '搬运磕划'];

      this.$request.get('/api/detectInfo/warnings/load').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          const data = res.data;
          if (data) {
            if (data.warningsList && Array.isArray(data.warningsList) && data.warningsList.length > 0) {
              // 映射与格式化列表数据，确保未改数据库前前端能正确预览对应半轴抽检信息
              this.warningsList = data.warningsList.map((item, idx) => {
                const isCrack = String(item.level) === '3' || (String(item.level) === '2' && idx % 2 === 1);
                const defectType = isCrack ? '裂纹' : '划痕';
                const catPool = isCrack ? crackCategoryPool : scratchCategoryPool;
                const catIndex = (idx * 3 + (item.id || idx)) % catPool.length;
                const codeIndex = (idx + (item.id || 0)) % axleCodePool.length;
                const codeSuffix = idx >= axleCodePool.length ? '-' + (idx + 1) : '';

                return {
                  id: item.id || idx,
                  axleCode: item.axleCode || item.code || (axleCodePool[codeIndex] + codeSuffix),
                  type: defectType,
                  category: item.category || catPool[catIndex],
                  level: String(item.level || (isCrack ? '2' : '1')),
                  createTime: item.createTime || item.create_time || '2026-09-02 10:00:00',
                  content: '' // 留空，预留对接后续AI质检专家报告
                };
              });
            }
            if (data.warningsSum !== undefined) this.warningsSum = data.warningsSum;
            if (data.oneDayWarningsSum !== undefined) this.oneDayWarningsSum = data.oneDayWarningsSum;

            if (data.warningsOpen !== undefined) this.warningsOpen = Boolean(data.warningsOpen);
            if (data.warningsLevel !== undefined) this.warningsLevel = String(data.warningsLevel);
            if (data.phoneWay) this.Way = '1';
            else if (data.emailWay) this.Way = '2';
            if (data.phone) this.phone = data.phone;
            if (data.email) this.email = data.email;

            if (data.barChart && data.barChart.source) {
              const src = data.barChart.source;
              this.barChart.source = {
                '紧急程度 1': src['1'] || src[1] || 0,
                '紧急程度 2': src['2'] || src[2] || 0,
                '紧急程度 3': src['3'] || src[3] || 0
              };
            } else {
              const levelCount = { 1: 0, 2: 0, 3: 0 };
              this.warningsList.forEach(w => {
                if (levelCount[w.level] !== undefined) levelCount[w.level]++;
              });
              this.barChart.source = {
                '紧急程度 1': levelCount[1],
                '紧急程度 2': levelCount[2],
                '紧急程度 3': levelCount[3]
              };
            }

            this.$nextTick(() => {
              this.drawChart();
            });
          }
        } else {
          console.warn('获取告警信息异常:', res.msg);
        }
      }).catch(err => {
        console.error('连接后端告警接口失败:', err);
      });
    },
    drawChart() {
      const chartDom = document.getElementById(this.chartId);
      if (!chartDom) return;

      if (this.chartInstance) {
        this.chartInstance.dispose();
      }

      const xAxisData = ['紧急程度 1', '紧急程度 2', '紧急程度 3'];
      const seriesData = [
        this.barChart.source['紧急程度 1'] !== undefined ? this.barChart.source['紧急程度 1'] : this.level1Count,
        this.barChart.source['紧急程度 2'] !== undefined ? this.barChart.source['紧急程度 2'] : this.level2Count,
        this.barChart.source['紧急程度 3'] !== undefined ? this.barChart.source['紧急程度 3'] : this.level3Count
      ];

      // 高阶工业科技色彩体系：纵向微质感渐变 + 柔和光晕 + 悬浮高亮反馈
      const colorConfigs = [
        {
          start: '#1890ff',
          end: '#36cfc9',
          shadow: 'rgba(24, 144, 255, 0.28)',
          dot: '#1890ff'
        },
        {
          start: '#fa8c16',
          end: '#ffd666',
          shadow: 'rgba(250, 140, 22, 0.28)',
          dot: '#fa8c16'
        },
        {
          start: '#f5222d',
          end: '#ff7875',
          shadow: 'rgba(245, 34, 45, 0.28)',
          dot: '#f5222d'
        }
      ];

      const option = {
        grid: {
          left: '3%',
          right: '4%',
          bottom: '6%',
          top: '16%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLine: {
            lineStyle: { color: '#f0f2f5' }
          },
          axisTick: { show: false },
          axisLabel: {
            color: '#606266',
            fontSize: 12,
            fontWeight: 500,
            margin: 10
          }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: {
            color: '#909399',
            fontSize: 11
          },
          splitLine: {
            lineStyle: {
              color: '#f0f2f5',
              type: 'dashed'
            }
          }
        },
        series: [{
          type: 'bar',
          barWidth: 26,
          showBackground: false,
          label: {
            show: true,
            position: 'top',
            distance: 6,
            color: '#1f2d3d',
            fontSize: 13,
            fontWeight: 'bold',
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
          },
          data: seriesData.map((val, idx) => ({
            value: val,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                { offset: 0, color: colorConfigs[idx].start },
                { offset: 1, color: colorConfigs[idx].end }
              ]),
              borderRadius: [8, 8, 2, 2],
              shadowColor: colorConfigs[idx].shadow,
              shadowBlur: 8,
              shadowOffsetY: 4
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 14,
                shadowOffsetY: 6,
                shadowColor: colorConfigs[idx].shadow
              }
            }
          }))
        }],
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(24, 30, 42, 0.94)',
          padding: [8, 12],
          borderRadius: 8,
          borderWidth: 0,
          boxShadow: '0 6px 16px rgba(0, 0, 0, 0.2)',
          axisPointer: {
            type: 'shadow',
            shadowStyle: {
              color: 'rgba(24, 144, 255, 0.04)'
            }
          },
          formatter: function(params) {
            const p = params[0];
            const cfg = colorConfigs[p.dataIndex] || colorConfigs[0];
            return '<div style="font-size:12px;font-weight:600;color:#fff;margin-bottom:4px;">' + p.name + '</div>' +
                   '<div style="font-size:12px;color:#e4e7ed;display:flex;align-items:center;gap:6px;">' +
                     '<span style="display:inline-block;width:7px;height:7px;border-radius:50%;background:' + cfg.dot + ';"></span>' +
                     '告警数量: <b style="color:#fff;font-size:13px;margin-left:4px;">' + p.value + '</b> 条' +
                   '</div>';
          }
        }
      };

      const myChart = echarts.init(chartDom);
      this.chartInstance = myChart;
      myChart.setOption(option);

      window.addEventListener('resize', () => {
        if (myChart && !myChart.isDisposed()) {
          myChart.resize();
        }
      });
    },
    sendDataToBackend() {
      const data = {
        warningsOpen: this.warningsOpen,
        warningsLevel: Number(this.warningsLevel),
        phoneWay: this.Way === '1',
        emailWay: this.Way === '2',
        phone: this.phone,
        email: this.email
      };
      console.log('发送配置数据到后端:', data);

      this.$request.put('/api/detectInfo/warnings/set', data)
        .then(res => {
          if (res.code === 200 || res.code === 1 || res.code === '200') {
            this.$message.success('告警配置保存成功');
          }
        })
        .catch(error => {
          console.error('配置保存失败:', error);
          this.$message.error('配置保存失败');
        });
    }
  }
};
</script>

<style scoped>
.warning-management {
  width: 100%;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #303133;
  display: flex;
  flex-direction: column;
}

/* 页面顶部标题与操作栏 (Header Section) */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 6px;
  margin-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  background: #e6f7ff;
  border-radius: 6px;
  color: #1890ff;
  font-size: 15px;
}

.page-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1f2d3d;
}

.title-tag {
  font-size: 11px;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 3px;
  padding: 1px 6px;
}

.page-desc {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 运行指标透视条 (Stats Bar) */
.stats-bar {
  display: flex;
  align-items: center;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-radius: 6px;
  padding: 6px 16px;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.stat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 12px;
}

.stat-label {
  font-size: 12px;
  font-weight: 500;
  color: #4b5563;
}

.stat-icon {
  font-size: 14px;
  color: #9ca3af;
}

.status-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-badge.success {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.success .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #52c41a;
}

.status-badge.warning {
  background: #fffbe6;
  color: #fa8c16;
  border: 1px solid #ffe58f;
}

.status-badge.warning .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #fa8c16;
}

.status-badge.info {
  background: #f5f5f5;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
}

.status-badge.info .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #8c8c8c;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin: 0;
}

.stat-value .num {
  font-size: 50px;
  font-weight: 800;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #111827;
  line-height: 1;
  letter-spacing: -1.5px;
}

.stat-value .unit {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  margin-left: 2px;
}

.stat-value.text-success .num {
  color: #52c41a;
}

.stat-value.text-warning .num {
  color: #fa8c16;
}

.stat-value.text-danger .num {
  color: #ff4d4f;
}

.stat-value.text-primary .num {
  color: #1890ff;
}

.stat-foot {
  font-size: 11px;
  color: #9ca3af;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: #e5e7eb;
  margin: 0 16px;
}

/* 主体布局 (Main Layout) */
.main-layout {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

/* 左侧预警列表区域 */
.warning-list-section {
  flex: 1;
  min-width: 0;
  background: #ffffff;
  border: 1px solid #eef0f3;
  border-radius: 6px;
  padding: 10px 14px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* 工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.level-select {
  width: 140px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.filter-tip {
  font-size: 12px;
  color: #8c8c8c;
}

.filter-tip b {
  color: #1890ff;
}

/* 表格区域 */
.table-container {
  flex: 1;
  width: 100%;
  overflow: hidden;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

::v-deep .el-table {
  width: 100% !important;
  font-size: 14px !important;
}

::v-deep .el-table th.el-table__cell {
  background-color: #fafafa !important;
  color: #1f2d3d !important;
  font-weight: 600;
  font-size: 14px;
  padding: 14.5px 0 !important;
  border-bottom: 1px solid #ebeef5;
}

::v-deep .el-table td.el-table__cell {
  padding: 13.5px 0 !important;
  font-size: 14px;
  color: #374151;
  border-bottom: 1px solid #f0f2f5;
}

.axle-code-badge {
  font-weight: 600;
  font-family: 'Roboto Mono', Consolas, Monaco, monospace;
  color: #1f2937;
}

.quality-category-badge {
  color: #4b5563;
  font-size: 13.5px;
}

.warning-type-text {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
}

.time-cell {
  font-size: 13px;
  color: #4b5563;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.analysis-placeholder {
  font-size: 13.5px;
  color: #6b7280;
}

.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.status-cell .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-danger {
  background: #fff1f0;
  color: #cf1322;
  border: 1px solid #ffa39e;
}
.status-danger .dot {
  background: #f5222d;
}

.status-warning {
  background: #fffbe6;
  color: #d46b08;
  border: 1px solid #ffe58f;
}
.status-warning .dot {
  background: #fa8c16;
}

.status-primary {
  background: #e6f7ff;
  color: #096dd9;
  border: 1px solid #91d5ff;
}
.status-primary .dot {
  background: #1890ff;
}

/* 分页 */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  padding-bottom: 2px;
  margin-top: auto;
  border-top: 1px solid #f0f0f0;
}

.pagination-total {
  font-size: 13px;
  color: #595959;
}

.total-count {
  font-weight: 600;
  color: #1f2d3d;
}

/* 右侧监控分析与配置面板 */
.warning-config-section {
  width: 350px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-card {
  background: #ffffff;
  border: 1px solid #eef0f3;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.2s;
}

.panel-card:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 14px;
  background: #fafbfc;
  border-bottom: 1px solid #eef0f3;
}

.panel-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.panel-title-wrap i {
  color: #1890ff;
  font-size: 15px;
}

.panel-subtitle {
  font-size: 11px;
  color: #8c8c8c;
}

.panel-body {
  padding: 14px 16px;
}

.chart-panel-body {
  padding: 12px 14px;
}

.chart-container {
  width: 100%;
  height: 180px;
}

.level-breakdown {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.breakdown-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px 6px;
  border-radius: 4px;
  font-size: 11px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  transition: all 0.2s ease;
}

.breakdown-item .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.level-1-item {
  background: #f0f9ff;
  border-color: #e0f2fe;
  color: #0369a1;
}
.level-1-item .dot { background: #0284c7; }
.level-1-item .count { color: #0284c7; font-weight: 700; }

.level-2-item {
  background: #fffbeb;
  border-color: #fef3c7;
  color: #b45309;
}
.level-2-item .dot { background: #d97706; }
.level-2-item .count { color: #d97706; font-weight: 700; }

.level-3-item {
  background: #fef2f2;
  border-color: #fee2e2;
  color: #b91c1c;
}
.level-3-item .dot { background: #dc2626; }
.level-3-item .count { color: #dc2626; font-weight: 700; }

/* 表单项 */
.config-form-item {
  margin-bottom: 12px;
}

.config-form-item:last-of-type {
  margin-bottom: 14px;
}

.form-label {
  display: block;
  font-size: 12px;
  color: #595959;
  margin-bottom: 6px;
  font-weight: 500;
}

.form-row-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.form-row-inline .form-label {
  margin-bottom: 0;
}

.level-radio-group, .way-radio-group {
  width: 100%;
}

.config-btn-wrap {
  padding-top: 4px;
}

.save-config-btn {
  width: 100%;
}
</style>
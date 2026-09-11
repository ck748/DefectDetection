<template>
  <div class="feature-checklist-container">
    <!-- 页面顶部标题与操作栏 -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-circle-check"></i></span>
          <h2 class="page-title">系统功能核对与验收台</h2>
          <span class="title-tag">竞赛验收 · 实时核验</span>
        </div>
        <p class="page-desc">对系统各项业务模块、质检流程与智能运检功能进行逐项核验，点击功能卡片即可切换完成状态并实时统计核对进度</p>
      </div>
      <div class="header-right">
        <el-button size="small" icon="el-icon-refresh-left" @click="resetAllStatus">重置所有状态</el-button>
        <el-button size="small" type="primary" icon="el-icon-check" @click="checkAllStatus">一键全部完成</el-button>
      </div>
    </div>

    <!-- 指标统计条 (Stats Bar) -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">总核对功能项</span>
          <i class="el-icon-document-checked stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ checklist.length }}</span>
          <span class="unit">项</span>
        </div>
        <div class="stat-foot">系统覆盖的核心功能检查清单</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">已核对完成</span>
          <span class="status-badge success">
            <span class="dot"></span>已验收
          </span>
        </div>
        <div class="stat-value text-success">
          <span class="num">{{ completedCount }}</span>
          <span class="unit">项</span>
        </div>
        <div class="stat-foot">已点击确认完成的检查点</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">待核对项</span>
          <span class="status-badge" :class="pendingCount > 0 ? 'warning' : 'info'">
            <span class="dot"></span>{{ pendingCount > 0 ? '待确认' : '已清空' }}
          </span>
        </div>
        <div class="stat-value" :class="pendingCount > 0 ? 'text-warning' : ''">
          <span class="num">{{ pendingCount }}</span>
          <span class="unit">项</span>
        </div>
        <div class="stat-foot">仍处于灰色未完成状态</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">核对完成率</span>
          <span class="status-badge" :class="progressPercent === 100 ? 'success' : 'primary'">
            <span class="dot"></span>{{ progressPercent === 100 ? '全部就绪' : '核验中' }}
          </span>
        </div>
        <div class="stat-value text-primary">
          <span class="num">{{ progressPercent }}</span>
          <span class="unit">%</span>
        </div>
        <div class="stat-foot">
          <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="6" :status="progressPercent === 100 ? 'success' : undefined" style="margin-top: 4px;"></el-progress>
        </div>
      </div>
    </div>

    <!-- 工具与筛选栏 -->
    <div class="toolbar-section">
      <div class="toolbar-left">
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button label="all">全部项 ({{ checklist.length }})</el-radio-button>
          <el-radio-button label="pending">待核对 ({{ pendingCount }})</el-radio-button>
          <el-radio-button label="completed">已完成 ({{ completedCount }})</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索功能编号或名称..."
          prefix-icon="el-icon-search"
          size="small"
          clearable
          style="width: 240px; margin-left: 12px;"
        ></el-input>
      </div>
      <div class="toolbar-right">
        <span class="filter-tip">点击卡片可快速切换完成状态</span>
      </div>
    </div>

    <!-- 功能清单列表卡片 -->
    <div class="checklist-grid">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="check-item-card"
        :class="{ 'is-completed': item.completed }"
        @click="toggleItem(item)"
      >
        <!-- 序号徽标 -->
        <div class="item-index-badge">
          <span>{{ item.id < 10 ? '0' + item.id : item.id }}</span>
        </div>

        <!-- 核心内容 -->
        <div class="item-content">
          <div class="item-header-row">
            <span class="item-module-tag">{{ item.module }}</span>
            <h3 class="item-title">{{ item.title }}</h3>
          </div>
          <p class="item-desc">{{ item.description }}</p>
        </div>

        <!-- 右侧状态与对勾标识 -->
        <div class="item-status-col">
          <div v-if="item.completed" class="completed-badge-wrap">
            <div class="success-icon-box">
              <i class="el-icon-check"></i>
            </div>
            <span class="status-text-success">已核验</span>
          </div>
          <div v-else class="pending-badge-wrap">
            <div class="pending-circle"></div>
            <span class="status-text-pending">待核对</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FeatureChecklist',
  data() {
    return {
      filterType: 'all',
      searchKeyword: '',
      checklist: [
        { id: 1, module: '移动运载', title: 'AGV小车外观结构与轮组完好性核验', description: '核验AGV搬运小车车体机械结构、底盘轮组、防撞缓冲条及急停按钮完好无损、无异物附着。', completed: false },
        { id: 2, module: '状态指示', title: '运载小车三色状态指示灯与声光报警测试', description: '核验AGV小车红黄绿三色状态指示灯发光正常，声光报警器与避障提示蜂鸣功能就绪。', completed: false },
        { id: 3, module: '无线测控', title: '车载工业无线通信与总线信号接收核验', description: '验证无线AP与车载工业Wi-Fi通信链路稳定，报文丢包率极低，遥测与控制指令响应实时。', completed: false },
        { id: 4, module: '准入确认', title: '移动运载单元综合硬件自检与准入确认', description: 'AGV小车动力电池电量、机械结构、电气系统及导航定位全部自检合格，进入待命就绪状态。', completed: false },
        { id: 5, module: '视觉感知', title: '六轴机械臂、工业相机及高亮偏振光源点检', description: '确认协作机械臂本体就绪，GigE千兆网口工业相机图像采集正常，高亮偏振光源照射均匀稳定。', completed: false },
        { id: 6, module: '日志归档', title: '灵巡移动底盘点检数据归档与日志登记', description: '汇总灵巡智能移动底盘前序全部硬件自检数据，生成标准化开机点检日志并写入系统数据库。', completed: false },
        { id: 7, module: '系统启动', title: '灵眸表面缺陷智能质检系统初始化与算法自检', description: '启动灵眸质检上位机主控程序，完成AI视觉感知算法模型加载、显存预热及通信接口握手。', completed: false },
        { id: 8, module: '安全复位', title: '六轴协作机械臂原点校准与安全待命复位', description: '调度机械臂执行回原点运动控制指令，各关节平稳回零至初始安全位姿，伺服抱闸锁定到位。', completed: false },
        { id: 9, module: '电气回路', title: '视觉检测回路供电与传感器线缆紧固核验', description: '核验24V工业直流稳压电源输出稳定，频闪光源驱动器连接可靠，相机千兆以太网线屏蔽良好无松脱。', completed: false },
        { id: 10, module: '治具点检', title: '光学检测工位标定台面与工装夹具清洁度核验', description: '检查工件旋转检测台、光学暗箱箱体、标定靶标及机械夹具表面清洁无油污，无结构性磕碰破损。', completed: false },
        { id: 11, module: '示教测试', title: '手持示教器点动调试与关节运动插补验证', description: '通过手持示教器执行关节微动点动与直线插补轨迹试运行，验证机械臂运动平滑性与限位保护逻辑。', completed: false },
        { id: 12, module: '工序协同', title: '灵眸质检系统开机自检回执确认与工序记录', description: '工控上位机与边缘计算节点完成通信握手，系统确认收到灵眸开机自检合格回执并归档记录。', completed: false },
        { id: 13, module: '机柜巡检', title: '工控机柜与主控电箱安全开箱巡查', description: '遵循安全用电规程规范开启工业计算控制柜前门，对柜内核心电气元件、导轨及散热风道开展巡查。', completed: false },
        { id: 14, module: '工业网络', title: '边缘服务器、工业交换机及路由网络功能核验', description: '核验边缘AI算力服务器、PROFINET工业以太网交换机及工业路由器网络端口连通性与路由转发状态。', completed: false },
        { id: 15, module: '运行监测', title: '计算节点硬件状态指示与交换机端口指示灯核验', description: '确认服务器主板、硬盘及存储阵列指示灯无告警红灯，工业交换机各网口Link/Act绿灯常亮闪烁。', completed: false },
        { id: 16, module: '电气安全', title: '柜内低压配电端子排、接地线与屏蔽线路全面核查', description: '全面核验电箱内接线端子排紧固度、系统保护接地电阻（＜4Ω）及各类传感器屏蔽线接地规范性。', completed: false },
        { id: 17, module: '点检归档', title: '基础设施全维度点检完成确认与数据归档', description: '车间工控硬件、电气与工业网络基础设施全部核验合格，生成综合点检合格凭证并录入系统。', completed: false },
        { id: 18, module: '全闭环协同', title: '灵眸巡诊「运-检-诊-分」全流程自动化闭环联动', description: '启动AGV智能运载、全周光学采集、AI大模型缺陷诊断与合格/缺陷品自动分流全闭环协同工作流。', completed: false },
        { id: 19, module: '现场导引', title: '产线功能区域布局与定置管理现场导引', description: '明确上料待检区、360°旋转光学检测站、合格品暂存库及缺陷品黄色防错隔离区功能划定与路线。', completed: false },
        { id: 20, module: '生产运行', title: '启动批次半轴自动化连续在线质检运行', description: '产线正式进入自动化连续质检作业模式，实时处理全周采集图像并下发质量分级与工艺处置决策。', completed: false }
      ]
    };
  },
  computed: {
    completedCount() {
      return this.checklist.filter(item => item.completed).length;
    },
    pendingCount() {
      return this.checklist.filter(item => !item.completed).length;
    },
    progressPercent() {
      if (this.checklist.length === 0) return 0;
      return Math.round((this.completedCount / this.checklist.length) * 100);
    },
    filteredList() {
      return this.checklist.filter(item => {
        if (this.filterType === 'pending' && item.completed) return false;
        if (this.filterType === 'completed' && !item.completed) return false;
        if (this.searchKeyword) {
          const kw = this.searchKeyword.toLowerCase().trim();
          const matchTitle = item.title.toLowerCase().includes(kw);
          const matchDesc = item.description.toLowerCase().includes(kw);
          const matchModule = item.module.toLowerCase().includes(kw);
          const matchId = String(item.id).includes(kw);
          return matchTitle || matchDesc || matchModule || matchId;
        }
        return true;
      });
    }
  },
  created() {
    this.loadSavedStatus();
  },
  methods: {
    loadSavedStatus() {
      try {
        const saved = localStorage.getItem('defect_feature_checklist_v1');
        if (saved) {
          const parsed = JSON.parse(saved);
          if (Array.isArray(parsed)) {
            parsed.forEach(savedItem => {
              const target = this.checklist.find(i => i.id === savedItem.id);
              if (target) {
                target.completed = !!savedItem.completed;
              }
            });
          }
        }
      } catch (e) {
        console.error('加载核对状态失败', e);
      }
    },
    saveStatus() {
      try {
        const state = this.checklist.map(i => ({ id: i.id, completed: i.completed }));
        localStorage.setItem('defect_feature_checklist_v1', JSON.stringify(state));
      } catch (e) {
        console.error('保存核对状态失败', e);
      }
    },
    toggleItem(item) {
      item.completed = !item.completed;
      this.saveStatus();
      if (item.completed) {
        this.$message({
          message: `功能点 ${item.id < 10 ? '0' + item.id : item.id} 已完成核验`,
          type: 'success',
          duration: 1500
        });
      }
    },
    resetAllStatus() {
      this.$confirm('确定要重置所有功能核对状态为待核对吗？', '提示', {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.checklist.forEach(i => (i.completed = false));
        this.saveStatus();
        this.$message.info('已重置所有功能核对状态');
      }).catch(() => {});
    },
    checkAllStatus() {
      this.$confirm('确定要将所有功能项标记为已完成吗？', '提示', {
        confirmButtonText: '全部完成',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        this.checklist.forEach(i => (i.completed = true));
        this.saveStatus();
        this.$message.success('已全部标记为完成状态');
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.feature-checklist-container {
  width: 100%;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #303133;
}

/* 顶部标题与操作栏 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
  width: 28px;
  height: 28px;
  background: #e6f7ff;
  border-radius: 6px;
  color: #1890ff;
  font-size: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.title-tag {
  font-size: 11px;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 3px;
  padding: 1px 7px;
}

.page-desc {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 指标统计条 (Stats Bar) */
.stats-bar {
  display: flex;
  align-items: center;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-radius: 6px;
  padding: 14px 20px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 12px;
}

.stat-label {
  font-size: 12px;
  color: #595959;
}

.stat-icon {
  font-size: 14px;
  color: #bfbfbf;
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

.status-badge.primary {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-badge.primary .dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #1890ff;
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
}

.stat-value .num {
  font-size: 22px;
  font-weight: 700;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1f2937;
  line-height: 1.2;
}

.stat-value .unit {
  font-size: 12px;
  color: #8c8c8c;
}

.stat-value.text-success .num {
  color: #52c41a;
}

.stat-value.text-warning .num {
  color: #fa8c16;
}

.stat-value.text-primary .num {
  color: #1890ff;
}

.stat-foot {
  font-size: 11px;
  color: #8c8c8c;
}

.stat-divider {
  width: 1px;
  height: 44px;
  background: #e2e8f0;
  margin: 0 20px;
}

/* 工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.filter-tip {
  font-size: 12px;
  color: #8c8c8c;
}

/* 清单卡片网格 */
.checklist-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 功能卡片基础样式（默认未完成灰色） */
.check-item-card {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: #fbfcfd;
  border: 1px solid #e8ecf0;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.check-item-card:hover {
  border-color: #cbd5e1;
  background: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.04);
}

/* 序号徽标 */
.item-index-badge {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #edf2f7;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  margin-right: 18px;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

/* 内容区 */
.item-content {
  flex: 1;
  min-width: 0;
}

.item-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.item-module-tag {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
  font-weight: 500;
}

.item-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  transition: color 0.25s ease;
}

.item-desc {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
  transition: color 0.25s ease;
}

/* 右侧状态区 */
.item-status-col {
  margin-left: 20px;
  flex-shrink: 0;
}

.pending-badge-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.pending-circle {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #94a3b8;
}

.status-text-pending {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
}

/* 点击后已完成高亮状态 (变亮 + 绿色对勾) */
.check-item-card.is-completed {
  background: #f6ffed;
  border-color: #b7eb8f;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.08);
}

.check-item-card.is-completed:hover {
  background: #efffde;
  border-color: #95de64;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.15);
}

.check-item-card.is-completed .item-index-badge {
  background: #52c41a;
  color: #ffffff;
  box-shadow: 0 2px 6px rgba(82, 196, 26, 0.3);
}

.check-item-card.is-completed .item-module-tag {
  background: #d9f7be;
  color: #237804;
  border-color: #b7eb8f;
}

.check-item-card.is-completed .item-title {
  color: #1f2d3d;
}

.check-item-card.is-completed .item-desc {
  color: #595959;
}

.completed-badge-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 20px;
  background: #d9f7be;
  border: 1px solid #b7eb8f;
}

.success-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #52c41a;
  color: #ffffff;
  font-size: 11px;
  font-weight: bold;
}

.status-text-success {
  font-size: 12px;
  color: #237804;
  font-weight: 600;
}
</style>

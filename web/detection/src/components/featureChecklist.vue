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
        { id: 1, module: '数据监控', title: '数据大屏实时看板与产线核心运行指标呈现', description: '核验数据大屏各项质检节拍、合格率、缺陷分类雷达图及设备在线状态渲染。', completed: false },
        { id: 2, module: '数据标注', title: '工件缺陷样本图像导入与矩形标注框交互', description: '核验图像上传、画框标注、缺陷类型选择及标注结果保存交互。', completed: false },
        { id: 3, module: '标注管理', title: '历史标注数据集版本导出与标注记录回溯', description: '核验已标注数据集历史列表、条件检索以及导出标注文件功能。', completed: false },
        { id: 4, module: '实时检测', title: '边缘端工件缺陷图像实时采集与模型推理解析', description: '核验摄像头或本地工件检测图像流加载、AI 模型推理与缺陷框覆盖显示。', completed: false },
        { id: 5, module: '历史检测', title: '工件缺陷历史检测记录多维检索与明细追溯', description: '核验历史检测记录表格、分页、置信度展示与大图详情查看。', completed: false },
        { id: 6, module: '预警运维', title: '系统异常事件分级监控与紧急度统计图表', description: '核验预警信息列表、紧急程度筛选、指标统计条与 ECharts 分布图表。', completed: false },
        { id: 7, module: '告警通知', title: '异常事件通知触达配置（短信/邮件）与联调', description: '核验告警总开关、触发级别、手机/邮箱输入框保存及后台联动配置。', completed: false },
        { id: 8, module: '审计安全', title: '系统操作审计日志记录与时间多维筛选', description: '核验日志管理中登录、检测、参数变动等操作日志的分页检索与日志导出。', completed: false },
        { id: 9, module: '凭证管理', title: 'API 调用凭证密钥生成、状态启停与权限鉴权', description: '核验 API 密钥新建、密钥复制、禁用/启用状态切换与鉴权响应。', completed: false },
        { id: 10, module: '权限维护', title: '系统操作密钥维护与安全授权人员信息管理', description: '核验人员列表管理、新增运维人员、密钥修改及多级安全控制。', completed: false },
        { id: 11, module: '智能质检', title: '360° 全周质检工作台模型加载与缺陷空间定位', description: '核验质检工作台工件模型多角度自旋渲染与缺陷表面标记。', completed: false },
        { id: 12, module: '运检控制', title: 'AGV 工装联调控制台硬件指令通信与点动控制', description: '核验 AGV 导航指令下发、工装夹具升降/旋转状态同步与急停控制。', completed: false },
        { id: 13, module: '数字孪生', title: '产线态势孪生大屏 3D 工位实时三维联动渲染', description: '核验 3D 数字孪生产线模型、产线设备运行状态映射与实时交互大屏。', completed: false },
        { id: 14, module: '算法调优', title: '缺陷识别模型置信度阈值调优与动态热更新', description: '核验模型权重快速切换、推理置信度阈值滑动调整与识别结果对比。', completed: false },
        { id: 15, module: '工业互联', title: '边缘端工控机与 PLC 交互状态心跳与急停断路', description: '核验 PLC 寄存器通信状态监控、心跳延迟检测与安全光栅联锁。', completed: false },
        { id: 16, module: '工件追溯', title: '工件唯一码扫码录入与全生命周期防重核验', description: '核验工件条码/二维码解析、重复扫码预警与全工艺流程追溯链路。', completed: false },
        { id: 17, module: '声光联动', title: '严重告警现场声光报警器触发与急停处置', description: '核验当高危级别 3 告警触发时，声光报警装置与中控界面的联动指示。', completed: false },
        { id: 18, module: '报告导出', title: '批次工件质检报告自动化生成与 PDF/Excel 导出', description: '核验按批次汇总质检合格率、缺陷分布统计图表并生成报告文件。', completed: false },
        { id: 19, module: '系统鉴权', title: '多角色权限控制、Token 认证与会话超时保护', description: '核验管理员/操作员登录鉴权、路由守卫与会话失效自动重定向。', completed: false },
        { id: 20, module: '容灾同步', title: '边缘端与云端质检数据同步备份与断网续传', description: '核验网络离线时本地缓存质检数据并在网络恢复后自动增量同步。', completed: false }
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

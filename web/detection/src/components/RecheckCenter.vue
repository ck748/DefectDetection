<template>
  <div class="recheck-container">
    <!-- 头部统计与态势 -->
    <div class="recheck-header">
      <div class="header-left">
        <span class="pulse-amber"></span>
        <span class="title-text">疑难件智能复检与协同仲裁中心</span>
        <el-tag type="warning" size="small" effect="dark">低/中置信度件自主闭环流转</el-tag>
      </div>
      <div class="header-stats">
        <div class="stat-badge">
          <span class="s-label">待复检件数</span>
          <span class="s-num text-amber">{{ recheckQueue.length }}</span>
        </div>
        <div class="stat-badge">
          <span class="s-label">复检准确率跃升</span>
          <span class="s-num text-emerald">+24.6%</span>
        </div>
        <div class="stat-badge">
          <span class="s-label">AI与人工协同率</span>
          <span class="s-num text-blue">98.2%</span>
        </div>
      </div>
    </div>

    <!-- 主体：两栏布局（左侧疑难工件流转队列，右侧初复检对比视窗与仲裁） -->
    <div class="recheck-main">
      <!-- 左栏：待复检工件队列 -->
      <div class="queue-panel">
        <div class="panel-header">
          <i class="el-icon-timer"></i> 待复检工件任务池
          <el-badge :value="recheckQueue.length" class="badge-item"></el-badge>
        </div>
        <div class="queue-list">
          <div
            v-for="(item, idx) in recheckQueue"
            :key="idx"
            class="queue-card"
            :class="{ active: currentItem.id === item.id }"
            @click="selectItem(item)"
          >
            <div class="card-line1">
              <span class="item-id">{{ item.id }}</span>
              <el-tag size="mini" :type="item.statusTag">{{ item.status }}</el-tag>
            </div>
            <div class="card-line2">
              <span>初检置信度: <b class="text-amber">{{ item.initialConf }}%</b></span>
              <span>疑难角度: <b>{{ item.angle }}°</b></span>
            </div>
            <div class="card-reason">
              <i class="el-icon-info"></i> {{ item.reason }}
            </div>
          </div>
        </div>

        <div class="queue-actions">
          <el-button type="primary" size="small" icon="el-icon-refresh" style="width: 100%" @click="batchRecheck">
            一键调度 AGV 批量二次复拍
          </el-button>
        </div>
      </div>

      <!-- 右栏：初检 vs 二次复测 双视窗对比与裁决 -->
      <div class="compare-panel">
        <div class="panel-header">
          <div class="title-with-tag">
            <i class="el-icon-data-analysis"></i> 初检 / 复检 多视角深度诊断比对
            <span class="target-sub">[ 当前工件: {{ currentItem.id }} | 疑似缺陷: {{ currentItem.suspectDefect }} ]</span>
          </div>
          <div class="header-tags">
            <el-tag size="mini" type="info">补偿光源: +15% 偏振</el-tag>
            <el-tag size="mini" type="info">滚轮步进微调: ±2°</el-tag>
          </div>
        </div>

        <!-- 双屏并排比对 -->
        <div class="dual-viewport">
          <!-- 初检视窗 -->
          <div class="viewport-box">
            <div class="vp-label initial">
              <span>初检原图 (一次全周采图)</span>
              <span class="vp-score">初检置信: {{ currentItem.initialConf }}%</span>
            </div>
            <div class="vp-img-wrap">
              <img :src="currentItem.imgInitial" class="vp-img" alt="初检">
              <div class="doubt-box" :style="currentItem.initialBox">
                <span class="doubt-tip">初检疑似 ({{ currentItem.initialConf }}%)</span>
              </div>
            </div>
            <div class="vp-telemetry">
              <span>曝光: 0.05ms</span>
              <span>状态: 弱反射光斑干扰</span>
            </div>
          </div>

          <!-- 复检二次增强视窗 -->
          <div class="viewport-box">
            <div class="vp-label recheck">
              <span>二次复检视窗 (AGV原位角度微调+环形光补偿)</span>
              <span class="vp-score text-emerald">确诊置信: {{ currentItem.recheckConf }}%</span>
            </div>
            <div class="vp-img-wrap">
              <img :src="currentItem.imgRecheck" class="vp-img" alt="复检">
              <div class="confirmed-box" :style="currentItem.recheckBox">
                <span class="confirmed-tip">已确诊: {{ currentItem.confirmedDefect }} ({{ currentItem.recheckConf }}%)</span>
              </div>
            </div>
            <div class="vp-telemetry">
              <span>曝光: 0.08ms</span>
              <span class="text-emerald">状态: 特征边缘锐度提升32%</span>
            </div>
          </div>
        </div>

        <!-- 判定结果雷达与协同决策操作区 -->
        <div class="decision-section">
          <div class="decision-metrics">
            <div class="d-metric">
              <div class="dm-label">初复检特征一致性:</div>
              <el-progress :percentage="currentItem.consistency" status="success"></el-progress>
            </div>
            <div class="d-metric">
              <div class="dm-label">AI综合复判结论:</div>
              <div class="dm-verdict">
                <span class="badge-verdict" :class="currentItem.isDefect ? 'danger' : 'success'">
                  {{ currentItem.isDefect ? '判定次品 (' + currentItem.confirmedDefect + ')' : '判定良品 (合格无瑕)' }}
                </span>
                <span class="dm-note">算法建议: {{ currentItem.advice }}</span>
              </div>
            </div>
          </div>

          <!-- 协同仲裁动作条 -->
          <div class="decision-toolbar">
            <span class="tb-title"><i class="el-icon-s-custom"></i> 质检工程师协同仲裁:</span>
            <el-button type="success" size="small" icon="el-icon-check" @click="handleArbitrate('ok')">
              认可并放行 (归入合格品)
            </el-button>
            <el-button type="danger" size="small" icon="el-icon-close" @click="handleArbitrate('defect')">
              核定次品 (AGV分流缺陷区)
            </el-button>
            <el-button type="warning" size="small" icon="el-icon-document-add" @click="handleArbitrate('active_learning')">
              打标并送入【主动学习样本库】
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RecheckCenter',
  data() {
    return {
      currentItem: {},
      recheckQueue: [
        {
          id: 'HX-20260829-08',
          angle: 150,
          initialConf: 72.4,
          recheckConf: 96.1,
          status: '复检完成待仲裁',
          statusTag: 'warning',
          suspectDefect: '微小气孔/麻坑',
          confirmedDefect: '浅表油污点 (非缺陷)',
          reason: '反射强反光导致模型置信度不足75%',
          isDefect: false,
          consistency: 92,
          advice: '表面轻微油斑已被偏振光滤除，判定良品',
          imgInitial: require('../assets/8.jpg'),
          imgRecheck: require('../assets/8.jpg'),
          initialBox: { left: '48%', top: '40%', width: '18%', height: '18%' },
          recheckBox: { left: '49%', top: '41%', width: '18%', height: '18%' }
        },
        {
          id: 'HX-20260829-12',
          angle: 210,
          initialConf: 65.8,
          recheckConf: 94.3,
          status: 'AGV二次采图中',
          statusTag: 'info',
          suspectDefect: '微细纵向裂纹',
          confirmedDefect: '淬火微裂纹',
          reason: '花键边缘阴影混淆',
          isDefect: true,
          consistency: 86,
          advice: '二次定点曝光确认为真实裂纹，需剔除',
          imgInitial: require('../assets/8.jpg'),
          imgRecheck: require('../assets/8.jpg'),
          initialBox: { left: '30%', top: '35%', width: '22%', height: '16%' },
          recheckBox: { left: '31%', top: '36%', width: '22%', height: '16%' }
        },
        {
          id: 'HX-20260829-15',
          angle: 90,
          initialConf: 68.2,
          recheckConf: 91.0,
          status: '排队中',
          statusTag: 'info',
          suspectDefect: '擦伤/碰伤',
          confirmedDefect: '机械擦伤',
          reason: '旋转角过渡边缘区域',
          isDefect: true,
          consistency: 79,
          advice: '确诊轻微擦碰，分流至打磨返修区',
          imgInitial: require('../assets/8.jpg'),
          imgRecheck: require('../assets/8.jpg'),
          initialBox: { left: '55%', top: '50%', width: '15%', height: '20%' },
          recheckBox: { left: '55%', top: '50%', width: '15%', height: '20%' }
        }
      ]
    }
  },
  created() {
    this.currentItem = this.recheckQueue[0]
  },
  methods: {
    selectItem(item) {
      this.currentItem = item
    },
    batchRecheck() {
      this.$message.success('已联动 AGV 调度控制中心，正在规划二次定点复检航线！')
    },
    handleArbitrate(type) {
      if (type === 'ok') {
        this.$message.success(`工件 ${this.currentItem.id} 已仲裁判定为合格，AGV 将自动引导入合格品料仓`)
      } else if (type === 'defect') {
        this.$message.error(`工件 ${this.currentItem.id} 仲裁确诊次品，分流指令已发送至 AGV`)
      } else if (type === 'active_learning') {
        this.$message.warning(`工件 ${this.currentItem.id} 特征样本已自动归档至模型主动学习工场，供下一代权重微调！`)
        this.$router.push('/active-learning')
      }
    }
  }
}
</script>

<style scoped>
.recheck-container {
  padding: 16px;
  background-color: #0b1325;
  color: #e2e8f0;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

.recheck-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #132238;
  padding: 12px 20px;
  border-radius: 8px;
  border: 1px solid #1e3a8a;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pulse-amber {
  width: 10px;
  height: 10px;
  background-color: #f59e0b;
  border-radius: 50%;
  box-shadow: 0 0 8px #f59e0b;
}

.title-text {
  font-size: 16px;
  font-weight: bold;
  color: #fbbf24;
}

.header-stats {
  display: flex;
  gap: 24px;
}

.stat-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.s-label {
  font-size: 11px;
  color: #94a3b8;
}

.s-num {
  font-size: 16px;
  font-weight: bold;
}

.text-amber { color: #f59e0b; }
.text-emerald { color: #10b981; }
.text-blue { color: #38bdf8; }

.recheck-main {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.queue-panel {
  background: #111e36;
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
}

.panel-header {
  font-size: 14px;
  font-weight: 600;
  color: #38bdf8;
  border-bottom: 1px solid #1e293b;
  padding-bottom: 10px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.queue-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.queue-card {
  background: #172744;
  border: 1px solid #23385c;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.queue-card:hover, .queue-card.active {
  border-color: #f59e0b;
  background: #1c3055;
}

.card-line1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.item-id {
  font-weight: bold;
  font-size: 13px;
  color: #f1f5f9;
}

.card-line2 {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.card-reason {
  font-size: 11px;
  color: #cbd5e1;
  background: rgba(15, 23, 42, 0.4);
  padding: 4px 6px;
  border-radius: 4px;
}

.queue-actions {
  margin-top: 12px;
}

/* 右侧对比面板 */
.compare-panel {
  background: #111e36;
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 14px;
  display: flex;
  flex-direction: column;
}

.title-with-tag {
  display: flex;
  align-items: center;
  gap: 8px;
}

.target-sub {
  font-size: 12px;
  color: #94a3b8;
  font-weight: normal;
}

.dual-viewport {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.viewport-box {
  background: #070e1c;
  border: 1px solid #1e293b;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.vp-label {
  padding: 8px 12px;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  background: #132238;
  border-bottom: 1px solid #1e293b;
}

.vp-label.initial {
  border-left: 4px solid #f59e0b;
}

.vp-label.recheck {
  border-left: 4px solid #10b981;
}

.vp-img-wrap {
  height: 320px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
}

.vp-img {
  max-width: 95%;
  max-height: 90%;
  object-fit: contain;
}

.doubt-box {
  position: absolute;
  border: 2px dashed #f59e0b;
  background: rgba(245, 158, 11, 0.2);
}

.doubt-tip {
  position: absolute;
  top: -20px;
  left: 0;
  background: #f59e0b;
  color: #000;
  font-size: 10px;
  padding: 1px 4px;
  font-weight: bold;
  white-space: nowrap;
}

.confirmed-box {
  position: absolute;
  border: 2px solid #10b981;
  background: rgba(16, 185, 129, 0.2);
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
}

.confirmed-tip {
  position: absolute;
  top: -20px;
  left: 0;
  background: #10b981;
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  font-weight: bold;
  white-space: nowrap;
}

.vp-telemetry {
  display: flex;
  justify-content: space-between;
  padding: 6px 12px;
  background: #0f172a;
  font-size: 11px;
  color: #94a3b8;
  border-top: 1px solid #1e293b;
}

/* 判定与协同仲裁 */
.decision-section {
  background: #172744;
  border: 1px solid #23385c;
  border-radius: 6px;
  padding: 14px;
}

.decision-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 14px;
}

.dm-label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 6px;
}

.dm-verdict {
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge-verdict {
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 13px;
}

.badge-verdict.success {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
  border: 1px solid #10b981;
}

.badge-verdict.danger {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border: 1px solid #ef4444;
}

.dm-note {
  font-size: 12px;
  color: #cbd5e1;
}

.decision-toolbar {
  border-top: 1px solid #23385c;
  padding-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.tb-title {
  font-size: 13px;
  color: #38bdf8;
  font-weight: bold;
}
</style>

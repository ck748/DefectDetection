<template>
  <div class="omni-workspace-clean">
    <!-- 1. 顶部工件身份与终审裁决战术看板 (Tactical Command Deck) -->
    <header class="clean-hero-card">
      <div class="hero-left-meta">
        <div class="title-meta-row">
          <div class="station-chip">
            <span class="station-dot"></span>
            <span class="station-text">全周质检工位 02</span>
            <span class="station-tag font-mono">ST-02A</span>
          </div>
          <h1 class="part-main-title">{{ workpieceMeta.model }}</h1>
          <span class="part-sn-badge font-mono">{{ workpieceMeta.sampleSerial }}</span>
          <div class="sensor-online-badge font-mono">
            <span class="pulse-ring"></span>
            <span>IIoT SENSOR ACTIVE</span>
          </div>
        </div>

        <div class="meta-pills-row">
          <div class="info-pill">
            <span class="p-k">生产批次</span>
            <span class="p-v font-mono">{{ workpieceMeta.batchCode }}</span>
            <span class="p-sub">({{ workpieceMeta.batchSeq }})</span>
          </div>
          <div class="info-pill">
            <span class="p-k">径向跳动量</span>
            <span class="p-v font-mono text-success">{{ workpieceMeta.runout }}</span>
            <span class="p-sub">{{ workpieceMeta.runoutStandard }}</span>
          </div>
          <div class="info-pill">
            <span class="p-k">气动自锁</span>
            <span class="p-v font-mono text-success">{{ workpieceMeta.lockStatus }}</span>
          </div>
          <div class="info-pill">
            <span class="p-k">工装机构</span>
            <span class="p-v font-medium">{{ workpieceMeta.carrier }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：AI 终审结论全息警示看板 -->
      <div class="hero-decision-card" :class="finalDecision.type">
        <div class="decision-glow-bg"></div>
        <div class="decision-content">
          <div class="decision-header">
            <div class="badge-wrap">
              <span class="decision-badge">AI 综合判定</span>
              <span class="live-dot"></span>
            </div>
            <span class="confidence-rate font-mono">置信度 <strong>96.72%</strong></span>
          </div>
          <div class="decision-main-text">
            <i class="el-icon-warning-outline alarm-icon"></i>
            <span>{{ finalDecision.text }}</span>
          </div>
          <div class="decision-footer-dest">
            <span class="dest-label">柔性分流调度指令:</span>
            <strong class="dest-target font-mono">{{ finalDecision.dest }}</strong>
          </div>
        </div>
      </div>
    </header>

    <!-- 2. 主体工作区：双翼工控布局 -->
    <main class="clean-grid-layout">
      <!-- 左翼：360° 旋转微距成像与柱面空间展开图谱 (视觉证据) -->
      <section class="layout-column left-wing">
        <!-- 360° 微距视觉检测卡片 -->
        <div class="industrial-white-card">
          <div class="card-head-bar">
            <div class="head-title">
              <span class="icon-block"><i class="el-icon-camera"></i></span>
              <span class="caption">360° 旋转采图与定点显微检测视窗</span>
              <span class="phase-indicator-pill font-mono">
                <span class="dot-live"></span>
                当前观测相位: <strong>{{ currentStep.angle }}.00°</strong>
              </span>
            </div>
            <div class="head-actions">
              <el-button-group size="mini">
                <el-button
                  :type="showBbox ? 'primary' : 'default'"
                  icon="el-icon-aim"
                  class="action-btn"
                  @click="showBbox = !showBbox"
                >缺陷标定</el-button>
                <el-button
                  :type="autoPlaying ? 'danger' : 'default'"
                  :icon="autoPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"
                  class="action-btn"
                  @click="toggleAutoPlay"
                >{{ autoPlaying ? '暂停轮播' : '自动巡检' }}</el-button>
              </el-button-group>
            </div>
          </div>

          <!-- 12 相位采样定点微动选择轨道 (精确刻度转盘风格) -->
          <div class="phase-stepper-strip">
            <div
              v-for="(item, idx) in angleSteps"
              :key="idx"
              class="stepper-node"
              :class="{
                'is-active': currentAngleIdx === idx,
                'is-defect': item.hasDefect
              }"
              @click="selectAngle(idx)"
            >
              <div class="node-degree font-mono">{{ item.angle }}°</div>
              <div class="node-indicator-bar">
                <span class="dot-indicator"></span>
              </div>
              <div class="node-tag-txt font-mono">
                {{ item.hasDefect ? '异常' : '良品' }}
              </div>
              <div class="node-active-bar" v-if="currentAngleIdx === idx"></div>
            </div>
          </div>

          <!-- 嵌入式精密光学暗室观测台 (工业测控视窗) -->
          <div class="optical-stage-container">
            <!-- 激光准星标尺与 HUD 网格 -->
            <div class="laser-cross-reticle">
              <div class="axis-line x-axis"></div>
              <div class="axis-line y-axis"></div>
              <div class="center-aim-circle"></div>
              <div class="center-aim-dot"></div>
              <div class="dimension-readout font-mono">
                <span class="lbl">POS-X:</span> <strong>{{ currentStep.locationMm || 145.2 }}mm</strong>
                <span class="divider">|</span>
                <span class="lbl">RADIUS:</span> <strong>30.00mm</strong>
                <span class="divider">|</span>
                <span class="lbl">PHASE:</span> <strong>{{ currentStep.angle }}°</strong>
              </div>
            </div>

            <!-- 背景毫米级坐标网格标尺 -->
            <div class="ruler-grid-overlay">
              <div class="scale-ruler-marks font-mono">
                <span>0mm</span><span>150mm</span><span>300mm</span><span>450mm</span><span>600mm</span><span>750mm</span><span>820mm</span>
              </div>
            </div>

            <!-- 全息激光扫描扫掠线 -->
            <div class="laser-scan-sweeper"></div>

            <!-- 传动半轴真实机械结构装配体矢量拟真 (高拟真精研轴) -->
            <div class="mechanical-shaft-graphic">
              <!-- 左侧法兰盘与装配孔 -->
              <div class="shaft-component flange-wheel">
                <div class="flange-chamfer-top"></div>
                <span class="mounting-hole"></span>
                <span class="mounting-hole"></span>
                <span class="mounting-hole"></span>
                <div class="flange-chamfer-bottom"></div>
              </div>

              <!-- 左侧花键齿段 -->
              <div class="shaft-component spline-section left-spline">
                <div class="spline-teeth-mask"></div>
              </div>

              <!-- 主轴精磨轴颈段 (淬火镜面反光与表面纹理) -->
              <div class="shaft-component main-journal">
                <div class="machining-grain"></div>
                <div class="reflection-glare"></div>
                <div class="reflection-bottom-glare"></div>

                <!-- 缺陷高精度 HUD 瞄准定位框 -->
                <div
                  v-if="currentStep.hasDefect && showBbox"
                  class="defect-hud-bounding"
                  :style="currentStep.bboxStyle"
                >
                  <div class="bracket corner-tl"></div>
                  <div class="bracket corner-tr"></div>
                  <div class="bracket corner-bl"></div>
                  <div class="bracket corner-br"></div>
                  <div class="scanning-line"></div>
                  <div class="hud-defect-badge">
                    <span class="pulse-alarm-dot"></span>
                    <span class="name">{{ currentStep.defectName }}</span>
                    <span class="conf font-mono">{{ currentStep.confidence }}%</span>
                  </div>
                </div>
              </div>

              <!-- 阶梯倒角段 -->
              <div class="shaft-component step-shoulder">
                <div class="shoulder-bevel"></div>
              </div>

              <!-- 右侧传动花键段 -->
              <div class="shaft-component spline-section right-spline">
                <div class="spline-teeth-mask"></div>
              </div>
            </div>

            <!-- 视窗底端嵌入式参数 HUD -->
            <div class="stage-dock-footer font-mono">
              <div class="hud-param-item">
                <i class="el-icon-view icon-accent"></i> 面阵相机: <strong>{{ workpieceMeta.camera }}</strong>
              </div>
              <div class="hud-param-item" v-if="currentStep.hasDefect">
                <i class="el-icon-warning text-danger"></i> 缺陷测距:
                <strong class="text-danger">{{ currentStep.locationMm }} mm</strong>
                <span class="split-line">/</span> 面积:
                <strong class="text-danger">{{ currentStep.pixelArea }} px²</strong>
              </div>
              <div class="hud-param-item" v-else>
                <i class="el-icon-circle-check text-success"></i> 结构完整度:
                <strong class="text-success">{{ currentStep.confidence }}% 合格</strong>
              </div>
              <div class="hud-param-item">
                <i class="el-icon-refresh icon-accent"></i> 伺服相位: <strong>{{ currentStep.angle }}.00°</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- 柱面展开 360° 时空缺陷热力散点工程图谱卡片 -->
        <div class="industrial-white-card">
          <div class="card-head-bar">
            <div class="head-title">
              <span class="icon-block"><i class="el-icon-data-analysis"></i></span>
              <span class="caption">半轴 820mm 全柱面展开缺陷空间图谱</span>
            </div>
            <div class="chart-legend-row font-mono">
              <span class="legend-unit"><span class="dot ok-dot"></span>良品点位采样</span>
              <span class="legend-unit"><span class="dot ng-dot"></span>检出缺陷 (微裂纹/表面麻坑)</span>
            </div>
          </div>
          <div class="chart-drawing-box" ref="cylinderChart"></div>
        </div>
      </section>

      <!-- 右翼：诊断雷达、自动化流水线与数据台账工作坞 (决策与控制) -->
      <section class="layout-column right-wing">
        <!-- 12 相位极坐标雷达置信度分布卡片 -->
        <div class="industrial-white-card">
          <div class="card-head-bar">
            <div class="head-title">
              <span class="icon-block"><i class="el-icon-pie-chart"></i></span>
              <span class="caption">12 相位极坐标缺陷置信度分布雷达</span>
            </div>
            <div class="radar-tip-tag font-mono">
              <span class="pulse-point"></span> 角度相位缺陷敏感度响应
            </div>
          </div>
          <div class="radar-drawing-box" ref="radarChart"></div>
        </div>

        <!-- 自动化运检协同分流流水线 -->
        <div class="industrial-white-card">
          <div class="card-head-bar">
            <div class="head-title">
              <span class="icon-block"><i class="el-icon-guide"></i></span>
              <span class="caption">自动化运检协同分流流水线</span>
            </div>
            <div class="flow-status-chip font-mono">
              <span class="pulse-live-dot"></span>
              STEP 03 / RUNNING
            </div>
          </div>
          <div class="flow-steps-deck">
            <div
              v-for="(st, idx) in workflowSteps"
              :key="idx"
              class="flow-step-cell"
              :class="{
                'step-done': dispatchStepActive > idx,
                'step-active': dispatchStepActive === idx
              }"
            >
              <div class="step-num-icon font-mono">
                <i v-if="dispatchStepActive > idx" class="el-icon-check"></i>
                <span v-else>{{ idx + 1 }}</span>
              </div>
              <div class="step-content">
                <div class="step-name">{{ st.name }}</div>
                <div class="step-desc">{{ st.sub }}</div>
              </div>
              <div class="step-connector" v-if="idx < workflowSteps.length - 1">
                <span class="conn-line"></span>
                <i class="el-icon-arrow-right conn-arrow"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 底层硬件信号与台账档案工作坞 -->
        <div class="industrial-white-card dock-tabs-container">
          <el-tabs v-model="activeTabName" class="engineering-tabs">
            <!-- PLC 寄存器映射表 -->
            <el-tab-pane label="PLC 寄存器映射表" name="inspection">
              <div class="dock-table-wrapper">
                <table class="engineering-table">
                  <thead>
                    <tr>
                      <th width="85">地址</th>
                      <th>变量功能描述</th>
                      <th width="85">类型</th>
                      <th width="115">实时读数</th>
                      <th width="80" style="text-align:center;">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(reg, i) in plcRegisters" :key="i">
                      <td class="font-mono font-bold text-primary">{{ reg.address }}</td>
                      <td class="text-secondary font-medium">{{ reg.name }}</td>
                      <td><span class="type-pill font-mono">{{ reg.type }}</span></td>
                      <td class="font-mono font-bold" :class="reg.valueClass">{{ reg.value }}</td>
                      <td style="text-align:center;">
                        <span class="status-chip" :class="reg.statusType">{{ reg.status }}</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </el-tab-pane>

            <!-- 运检执行日志 -->
            <el-tab-pane label="运检执行时间线" name="timeline">
              <div class="dock-table-wrapper timeline-flow-list">
                <div v-for="(log, i) in plcLogs" :key="i" class="timeline-row" :class="log.type">
                  <div class="time-col font-mono">{{ log.time }}</div>
                  <div class="axis-col">
                    <span class="axis-dot"></span>
                    <span class="axis-track" v-if="i < plcLogs.length - 1"></span>
                  </div>
                  <div class="body-col">
                    <div class="log-heading">{{ log.title }}</div>
                    <div class="log-sub">{{ log.desc }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 一物一码数字追溯台账 -->
            <el-tab-pane label="一物一码追溯台账" name="archive">
              <div class="dock-table-wrapper">
                <table class="engineering-table">
                  <thead>
                    <tr>
                      <th>流水批次号</th>
                      <th>节拍耗时</th>
                      <th>跳动偏差</th>
                      <th>检出缺陷</th>
                      <th>终审结论</th>
                      <th>分流流向</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(rec, i) in archiveRecords" :key="i">
                      <td class="font-mono font-bold text-primary">{{ rec.batchId }}</td>
                      <td class="font-mono">{{ rec.cycleTime }}s</td>
                      <td class="font-mono">{{ rec.runout }}</td>
                      <td>
                        <span v-if="rec.defects.length === 0" class="text-success font-bold">无缺陷</span>
                        <span v-else class="text-danger font-bold">{{ rec.defects.join('/') }}</span>
                      </td>
                      <td>
                        <span class="status-chip" :class="rec.decisionType">{{ rec.finalDecision }}</span>
                      </td>
                      <td class="text-muted">{{ rec.targetBin }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'OmniInspection',
  data() {
    return {
      activeTabName: 'inspection',
      showBbox: true,
      autoPlaying: false,
      autoPlayTimer: null,
      currentAngleIdx: 2, // 默认聚焦 60° 淬火微裂纹点位
      dispatchStepActive: 2, // 当前处于第3步：AI 裁决完成阶段

      workflowSteps: [
        { name: '到站自锁', sub: '激光SLAM精准停靠' },
        { name: '旋转采图', sub: '12相位频闪曝光' },
        { name: 'AI裁决', sub: '多模型综合判定' },
        { name: '道岔分流', sub: 'B3柔性道岔到位' }
      ],

      finalDecision: {
        type: 'danger',
        text: '次品件 (微裂纹/麻坑)',
        dest: '道岔 B3 缺陷分流区'
      },

      workpieceMeta: {
        model: 'HX-820 轿车传动半轴',
        camera: 'BASLER 4K 面阵 / 同轴环形光源',
        carrier: 'AGV-02 自适应双主动滚轮',
        lockStatus: '0.42 MPa 气动自锁就绪',
        batchCode: 'HX-20260829-08',
        batchSeq: '第 12 批次',
        sampleSerial: '#9824',
        runout: '±0.014 mm',
        runoutStandard: '符合 GB/T 1184-K 级'
      },

      angleSteps: [
        { angle: 0, hasDefect: false, confidence: 99.2, defectName: '' },
        { angle: 30, hasDefect: false, confidence: 98.6, defectName: '' },
        {
          angle: 60,
          hasDefect: true,
          confidence: 94.8,
          defectName: '淬火微裂纹',
          pixelArea: 1420,
          locationMm: 145.2,
          bboxStyle: { left: '26%', top: '22%', width: '15%', height: '56%' }
        },
        { angle: 90, hasDefect: false, confidence: 98.9, defectName: '' },
        { angle: 120, hasDefect: false, confidence: 97.5, defectName: '' },
        {
          angle: 150,
          hasDefect: true,
          confidence: 78.6,
          defectName: '表面麻坑',
          pixelArea: 430,
          locationMm: 380.0,
          bboxStyle: { left: '52%', top: '30%', width: '12%', height: '40%' }
        },
        { angle: 180, hasDefect: false, confidence: 99.4, defectName: '' },
        { angle: 210, hasDefect: false, confidence: 98.3, defectName: '' },
        { angle: 240, hasDefect: false, confidence: 97.9, defectName: '' },
        { angle: 270, hasDefect: false, confidence: 99.1, defectName: '' },
        { angle: 300, hasDefect: false, confidence: 98.4, defectName: '' },
        { angle: 330, hasDefect: false, confidence: 98.7, defectName: '' }
      ],

      plcRegisters: [
        { address: 'D1002', name: 'AGV载具到站自锁光电信号', type: 'BOOL', value: 'TRUE', valueClass: 'text-success', status: '正常', statusType: 'success' },
        { address: 'D1004', name: '伺服滚轮当前旋转相位角', type: 'INT', value: '60.00°', valueClass: 'text-primary', status: '就绪', statusType: 'primary' },
        { address: 'D1006', name: '4K相机硬件外触发信号', type: 'BOOL', value: 'HIGH', valueClass: 'text-success', status: '触发', statusType: 'success' },
        { address: 'D1008', name: '气动抱闸闭锁气压值', type: 'REAL', value: '0.42 MPa', valueClass: 'text-success', status: '合格', statusType: 'success' },
        { address: 'D1010', name: 'AI质检综合决策输出字', type: 'WORD', value: '0x0002 (NG)', valueClass: 'text-danger', status: '报警', statusType: 'danger' },
        { address: 'D1012', name: '道岔B3换向机构锁止到位', type: 'BOOL', value: 'TRUE', valueClass: 'text-success', status: '到位', statusType: 'success' }
      ],

      plcLogs: [
        { time: '14:28:10.120', title: 'AGV-02 载具进入质检工位', desc: '激光SLAM精准停靠，气压自锁完成 (0.42MPa)', type: 'primary' },
        { time: '14:28:10.850', title: '伺服主动双滚轮启动旋转采图', desc: '12 相位定点频闪采样硬件触发', type: 'primary' },
        { time: '14:28:15.670', title: 'AI 边缘多模型推断完毕', desc: '检出 60° 淬火裂纹及 150° 麻坑，评定为次品', type: 'danger' },
        { time: '14:28:15.820', title: '下达柔性分流调度至道岔 B3', desc: '伺服锁止到位信号确认回传', type: 'success' }
      ],

      archiveRecords: [
        { batchId: 'HX-20260829-08', cycleTime: '4.82', runout: '±0.014mm', defects: ['淬火裂纹', '表面麻坑'], finalDecision: '次品', decisionType: 'danger', targetBin: '道岔 B3 缺陷区' },
        { batchId: 'HX-20260829-07', cycleTime: '4.68', runout: '±0.008mm', defects: [], finalDecision: '合格良品', decisionType: 'success', targetBin: 'A1 高位立体仓' },
        { batchId: 'HX-20260829-06', cycleTime: '4.75', runout: '±0.011mm', defects: [], finalDecision: '合格良品', decisionType: 'success', targetBin: 'A1 高位立体仓' },
        { batchId: 'HX-20260829-05', cycleTime: '4.91', runout: '±0.016mm', defects: ['微痕疑似(52%)'], finalDecision: '疑难复测', decisionType: 'warning', targetBin: '人工复检中心' }
      ],

      cylinderChartInstance: null,
      radarChartInstance: null
    }
  },
  computed: {
    currentStep() {
      return this.angleSteps[this.currentAngleIdx] || this.angleSteps[0]
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initCylinderChart()
      this.initRadarChart()
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeDestroy() {
    if (this.autoPlayTimer) {
      clearInterval(this.autoPlayTimer)
    }
    window.removeEventListener('resize', this.handleResize)
    if (this.cylinderChartInstance) {
      this.cylinderChartInstance.dispose()
    }
    if (this.radarChartInstance) {
      this.radarChartInstance.dispose()
    }
  },
  methods: {
    selectAngle(idx) {
      this.currentAngleIdx = idx
      this.plcRegisters[1].value = `${this.angleSteps[idx].angle}.00°`
      this.renderCharts()
    },
    toggleAutoPlay() {
      this.autoPlaying = !this.autoPlaying
      if (this.autoPlaying) {
        this.autoPlayTimer = setInterval(() => {
          const nextIdx = (this.currentAngleIdx + 1) % this.angleSteps.length
          this.selectAngle(nextIdx)
        }, 1800)
      } else {
        if (this.autoPlayTimer) {
          clearInterval(this.autoPlayTimer)
          this.autoPlayTimer = null
        }
      }
    },
    initCylinderChart() {
      if (!this.$refs.cylinderChart) return
      this.cylinderChartInstance = echarts.init(this.$refs.cylinderChart)
      this.renderCylinderOption()
    },
    renderCylinderOption() {
      if (!this.cylinderChartInstance) return

      const goodPoints = []
      const defectPoints = []

      this.angleSteps.forEach((step) => {
        if (step.hasDefect) {
          defectPoints.push([step.locationMm, step.angle, step.defectName, step.confidence])
        } else {
          const xPos = 60 + (step.angle * 2.1)
          goodPoints.push([xPos, step.angle, '良品采样点', step.confidence])
        }
      })

      const option = {
        backgroundColor: 'transparent',
        grid: {
          top: 30,
          left: 55,
          right: 35,
          bottom: 28
        },
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(15, 23, 42, 0.95)',
          borderColor: '#38bdf8',
          borderWidth: 1,
          padding: [8, 12],
          textStyle: { color: '#f8fafc', fontSize: 12 },
          extraCssText: 'box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25); border-radius: 6px; backdrop-filter: blur(8px);',
          formatter: (param) => {
            const d = param.data
            const isDefect = d[2] !== '良品采样点'
            const badgeColor = isDefect ? '#ef4444' : '#10b981'
            return `<div style="font-family:ui-monospace, monospace; line-height:1.6;">
              <div style="font-weight:700; color:${badgeColor}; border-bottom:1px solid rgba(255,255,255,0.15); padding-bottom:3px; margin-bottom:4px;">
                ● ${d[2]}
              </div>
              <div style="color:#94a3b8; font-size:11px;">轴向定位: <strong style="color:#f8fafc;">${d[0]} mm</strong></div>
              <div style="color:#94a3b8; font-size:11px;">展开相位: <strong style="color:#38bdf8;">${d[1]}°</strong></div>
              <div style="color:#94a3b8; font-size:11px;">质检置信: <strong style="color:#f8fafc;">${d[3]}%</strong></div>
            </div>`
          }
        },
        xAxis: {
          type: 'value',
          min: 0,
          max: 820,
          name: '轴长 (mm)',
          nameTextStyle: { color: '#64748b', fontSize: 11, padding: [0, 0, 0, 8] },
          axisLine: { lineStyle: { color: '#cbd5e1' } },
          axisLabel: { color: '#64748b', fontSize: 11, fontFamily: 'ui-monospace, monospace' },
          splitLine: { lineStyle: { color: '#e2e8f0', type: 'dashed' } }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 360,
          interval: 90,
          name: '展开相位',
          nameTextStyle: { color: '#64748b', fontSize: 11 },
          axisLine: { lineStyle: { color: '#cbd5e1' } },
          axisLabel: { color: '#64748b', fontSize: 11, formatter: '{value}°', fontFamily: 'ui-monospace, monospace' },
          splitLine: { lineStyle: { color: '#e2e8f0' } }
        },
        series: [
          {
            name: '良品采样',
            type: 'scatter',
            symbolSize: 8,
            data: goodPoints,
            itemStyle: {
              color: '#059669',
              shadowColor: 'rgba(5, 150, 105, 0.4)',
              shadowBlur: 6
            }
          },
          {
            name: '检出缺陷',
            type: 'scatter',
            symbol: 'pin',
            symbolSize: 32,
            data: defectPoints,
            itemStyle: {
              color: '#ef4444',
              shadowColor: 'rgba(239, 68, 68, 0.5)',
              shadowBlur: 10
            },
            label: {
              show: true,
              formatter: '{@[2]}',
              position: 'top',
              color: '#ffffff',
              fontSize: 11,
              fontWeight: 'bold',
              backgroundColor: '#dc2626',
              padding: [3, 8],
              borderRadius: 4,
              shadowColor: 'rgba(220, 38, 38, 0.35)',
              shadowBlur: 6
            }
          }
        ]
      }

      this.cylinderChartInstance.setOption(option)
    },
    initRadarChart() {
      if (!this.$refs.radarChart) return
      this.radarChartInstance = echarts.init(this.$refs.radarChart)
      this.renderRadarOption()
    },
    renderRadarOption() {
      if (!this.radarChartInstance) return

      const indicators = this.angleSteps.map(s => ({
        name: `${s.angle}°`,
        max: 100
      }))

      const confidenceData = this.angleSteps.map(s => s.confidence)
      const defectSeverityData = this.angleSteps.map(s => s.hasDefect ? 92 : 15)

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(15, 23, 42, 0.95)',
          borderColor: '#0284c7',
          borderWidth: 1,
          padding: [8, 12],
          textStyle: { color: '#f8fafc', fontSize: 12 },
          extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.25); border-radius: 6px; backdrop-filter: blur(8px);'
        },
        radar: {
          indicator: indicators,
          radius: '64%',
          center: ['50%', '52%'],
          splitNumber: 4,
          shape: 'polygon',
          axisName: {
            color: '#475569',
            fontSize: 11,
            fontWeight: 600,
            fontFamily: 'ui-monospace, monospace'
          },
          splitLine: { lineStyle: { color: '#cbd5e1' } },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['#ffffff', '#f8fafc', '#f1f5f9', '#e2e8f0']
            }
          },
          axisLine: { lineStyle: { color: '#cbd5e1' } }
        },
        series: [
          {
            type: 'radar',
            data: [
              {
                value: confidenceData,
                name: '健康置信度 (%)',
                symbol: 'circle',
                symbolSize: 4,
                lineStyle: { color: '#0284c7', width: 2 },
                itemStyle: { color: '#0284c7' },
                areaStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(2, 132, 199, 0.35)' },
                    { offset: 1, color: 'rgba(2, 132, 199, 0.05)' }
                  ])
                }
              },
              {
                value: defectSeverityData,
                name: '缺陷激化响应',
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { color: '#ef4444', width: 2, type: 'dashed' },
                itemStyle: { color: '#ef4444' },
                areaStyle: {
                  color: 'rgba(239, 68, 68, 0.2)'
                }
              }
            ]
          }
        ]
      }

      this.radarChartInstance.setOption(option)
    },
    renderCharts() {
      this.renderCylinderOption()
      this.renderRadarOption()
    },
    handleResize() {
      if (this.cylinderChartInstance) {
        this.cylinderChartInstance.resize()
      }
      if (this.radarChartInstance) {
        this.radarChartInstance.resize()
      }
    }
  }
}
</script>

<style scoped>
/* ================= 全局工业基底规范 ================= */
.omni-workspace-clean {
  width: 100%;
  min-height: calc(100vh - 64px);
  box-sizing: border-box;
  background-color: #f1f5f9;
  color: #0f172a;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Microsoft YaHei", sans-serif;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.font-mono {
  font-family: "JetBrains Mono", "SF Mono", Consolas, Menlo, Monaco, monospace !important;
}

/* ================= 1. 顶部工件看板 (Tactical Command Deck) ================= */
.clean-hero-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 16px -2px rgba(15, 23, 42, 0.05), 0 2px 4px -1px rgba(15, 23, 42, 0.03);
  flex-wrap: wrap;
  gap: 16px;
  position: relative;
  overflow: hidden;
}

.clean-hero-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #0284c7 0%, #38bdf8 100%);
}

.hero-left-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.title-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.station-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  color: #334155;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
}

.station-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 8px #10b981;
}

.station-tag {
  color: #0284c7;
  font-weight: 700;
  margin-left: 2px;
}

.part-main-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.part-sn-badge {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  color: #0284c7;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}

.sensor-online-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 2px 8px;
  border-radius: 9999px;
}

.pulse-ring {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
  animation: pulse-dot 2s infinite ease-in-out;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.85); }
}

.meta-pills-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.info-pill {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 4px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.info-pill:hover {
  border-color: #cbd5e1;
  background: #ffffff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}

.info-pill .p-k {
  color: #64748b;
  font-size: 11px;
}

.info-pill .p-v {
  font-weight: 700;
  color: #1e293b;
}

.info-pill .p-sub {
  font-size: 11px;
  color: #94a3b8;
}

/* 最终裁决卡片 (警报全息发光晶体) */
.hero-decision-card {
  position: relative;
  background: linear-gradient(135deg, #fff1f2 0%, #ffe4e6 100%);
  border: 1px solid #fecdd3;
  border-radius: 8px;
  padding: 12px 18px;
  min-width: 290px;
  box-shadow: 0 4px 14px rgba(225, 29, 72, 0.08);
  overflow: hidden;
}

.hero-decision-card::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #f43f5e, #e11d48);
}

.decision-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.decision-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.badge-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.decision-badge {
  font-size: 11px;
  font-weight: 800;
  color: #be123c;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e11d48;
  box-shadow: 0 0 6px #e11d48;
  animation: pulse-dot 1.2s infinite ease-in-out;
}

.confidence-rate {
  font-size: 11px;
  color: #9f1239;
}

.confidence-rate strong {
  font-weight: 800;
}

.decision-main-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 800;
  color: #be123c;
}

.alarm-icon {
  font-size: 18px;
  animation: shake-alarm 3s infinite ease-in-out;
}

@keyframes shake-alarm {
  0%, 90%, 100% { transform: rotate(0deg); }
  92% { transform: rotate(-8deg); }
  94% { transform: rotate(8deg); }
  96% { transform: rotate(-5deg); }
  98% { transform: rotate(5deg); }
}

.decision-footer-dest {
  font-size: 12px;
  color: #64748b;
  border-top: 1px dashed rgba(225, 29, 72, 0.25);
  padding-top: 5px;
  margin-top: 3px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dest-target {
  color: #0284c7;
  font-weight: 700;
}

/* ================= 2. 主体双翼网格 ================= */
.clean-grid-layout {
  display: grid;
  grid-template-columns: 1.28fr 0.92fr;
  gap: 16px;
  flex: 1;
}

@media (max-width: 1400px) {
  .clean-grid-layout {
    grid-template-columns: 1fr;
  }
}

.layout-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 工业白色卡片规范 (Titanium Precision) */
.industrial-white-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  box-shadow: 0 4px 16px -2px rgba(15, 23, 42, 0.04), 0 2px 4px -1px rgba(15, 23, 42, 0.02);
  display: flex;
  flex-direction: column;
  gap: 14px;
  transition: all 0.2s;
}

.card-head-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 12px;
}

.head-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-block {
  width: 26px;
  height: 26px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0284c7;
  font-size: 14px;
}

.caption {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.phase-indicator-pill {
  display: flex;
  align-items: center;
  gap: 5px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #334155;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.phase-indicator-pill strong {
  color: #0284c7;
}

.dot-live {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #0284c7;
  box-shadow: 0 0 5px #0284c7;
}

.action-btn {
  font-size: 12px !important;
  font-weight: 600 !important;
}

/* ================= 12 相位采样定点微动选择轨道 ================= */
.phase-stepper-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 6px;
  gap: 4px;
  overflow-x: auto;
}

.stepper-node {
  position: relative;
  flex: 1;
  min-width: 44px;
  height: 44px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.18s ease-in-out;
  user-select: none;
}

.stepper-node:hover {
  border-color: #38bdf8;
  background: #f0f9ff;
  transform: translateY(-1px);
}

.stepper-node.is-active {
  background: #0284c7;
  border-color: #0284c7;
  box-shadow: 0 3px 10px rgba(2, 132, 199, 0.3);
}

.stepper-node.is-active .node-degree {
  color: #ffffff;
  font-weight: 800;
}

.stepper-node.is-active .node-tag-txt {
  color: #bae6fd;
}

.stepper-node.is-active .dot-indicator {
  background-color: #ffffff;
  box-shadow: 0 0 6px #ffffff;
}

.node-active-bar {
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 2px;
  background: #ffffff;
  border-radius: 2px 2px 0 0;
}

.node-degree {
  font-size: 11px;
  color: #475569;
  font-weight: 600;
}

.node-indicator-bar {
  display: flex;
  align-items: center;
  margin: 2px 0;
}

.dot-indicator {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: #10b981;
}

.stepper-node.is-defect .dot-indicator {
  background-color: #ef4444;
  box-shadow: 0 0 6px #ef4444;
}

.stepper-node.is-active.is-defect .dot-indicator {
  background-color: #fee2e2;
  box-shadow: 0 0 6px #ffffff;
}

.node-tag-txt {
  font-size: 9px;
  color: #94a3b8;
}

.stepper-node.is-defect .node-tag-txt {
  color: #ef4444;
  font-weight: 700;
}

/* ================= 嵌入式精密光学暗室显微视窗 ================= */
.optical-stage-container {
  position: relative;
  height: 290px;
  background: radial-gradient(circle at 50% 50%, #1e293b 0%, #090d16 100%);
  border: 1px solid #1e293b;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 3px 12px rgba(0, 0, 0, 0.7);
}

/* 激光准星与测量 HUD */
.laser-cross-reticle {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 6;
}

.laser-cross-reticle .x-axis {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(56, 189, 248, 0.35);
  box-shadow: 0 0 4px rgba(56, 189, 248, 0.45);
}

.laser-cross-reticle .y-axis {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 1px;
  background: rgba(56, 189, 248, 0.35);
  box-shadow: 0 0 4px rgba(56, 189, 248, 0.45);
}

.center-aim-circle {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 32px;
  height: 32px;
  transform: translate(-50%, -50%);
  border: 1px dashed rgba(56, 189, 248, 0.6);
  border-radius: 50%;
}

.center-aim-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  transform: translate(-50%, -50%);
  background: #38bdf8;
  border-radius: 50%;
  box-shadow: 0 0 6px #38bdf8;
}

.dimension-readout {
  position: absolute;
  top: 10px;
  left: 12px;
  font-size: 11px;
  color: #38bdf8;
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(56, 189, 248, 0.35);
  padding: 3px 10px;
  border-radius: 4px;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  gap: 6px;
}

.dimension-readout .lbl {
  color: #94a3b8;
  font-size: 10px;
}

.dimension-readout .divider {
  color: #334155;
  margin: 0 2px;
}

.ruler-grid-overlay {
  position: absolute;
  inset: 0;
  background-size: 26px 26px;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  pointer-events: none;
}

.scale-ruler-marks {
  display: flex;
  justify-content: space-between;
  padding: 6px 14px;
  font-size: 9px;
  color: rgba(148, 163, 184, 0.45);
}

/* 全息扫掠光束 */
.laser-scan-sweeper {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 80px;
  background: linear-gradient(90deg, transparent 0%, rgba(56, 189, 248, 0.08) 50%, rgba(56, 189, 248, 0.25) 100%);
  border-right: 1.5px solid rgba(56, 189, 248, 0.75);
  box-shadow: 0 0 16px rgba(56, 189, 248, 0.4);
  pointer-events: none;
  animation: laser-sweep 4s infinite linear;
}

@keyframes laser-sweep {
  0% { left: -10%; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { left: 110%; opacity: 0; }
}

/* 机械传动半轴高拟真装配体 */
.mechanical-shaft-graphic {
  width: 90%;
  height: 140px;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 3;
  filter: drop-shadow(0 14px 20px rgba(0, 0, 0, 0.75));
}

.flange-wheel {
  width: 22px;
  height: 106px;
  background: linear-gradient(180deg, #e2e8f0 0%, #94a3b8 25%, #475569 60%, #1e293b 100%);
  border: 1px solid #cbd5e1;
  border-radius: 6px 0 0 6px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
  padding: 10px 0;
  box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.5), inset 0 -2px 4px rgba(0, 0, 0, 0.6);
}

.mounting-hole {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0b0f19;
  border: 1px solid #64748b;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.8);
}

.spline-section {
  width: 50px;
  height: 52px;
  background: linear-gradient(180deg, #94a3b8 0%, #64748b 40%, #334155 80%, #0f172a 100%);
  border-top: 1.5px solid #cbd5e1;
  border-bottom: 1.5px solid #0f172a;
  position: relative;
}

.spline-teeth-mask {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    90deg,
    transparent 0px,
    transparent 4px,
    rgba(11, 15, 25, 0.7) 4px,
    rgba(11, 15, 25, 0.7) 7px
  );
}

.main-journal {
  flex: 1;
  height: 64px;
  background: linear-gradient(
    180deg,
    #e2e8f0 0%,
    #ffffff 18%,
    #cbd5e1 40%,
    #94a3b8 65%,
    #475569 88%,
    #1e293b 100%
  );
  border-top: 2px solid #ffffff;
  border-bottom: 2px solid #0f172a;
  position: relative;
  box-shadow: inset 0 3px 8px rgba(255, 255, 255, 0.8), inset 0 -4px 10px rgba(0, 0, 0, 0.7);
}

.machining-grain {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    90deg,
    transparent 0px,
    transparent 8px,
    rgba(255, 255, 255, 0.06) 8px,
    rgba(0, 0, 0, 0.08) 10px
  );
}

.reflection-glare {
  position: absolute;
  top: 16%;
  left: 0;
  right: 0;
  height: 5px;
  background: rgba(255, 255, 255, 0.85);
  filter: blur(1px);
}

.reflection-bottom-glare {
  position: absolute;
  bottom: 20%;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(148, 163, 184, 0.35);
  filter: blur(1px);
}

.step-shoulder {
  width: 24px;
  height: 56px;
  background: linear-gradient(180deg, #94a3b8 0%, #64748b 50%, #1e293b 100%);
  border-top: 1px solid #cbd5e1;
  border-bottom: 1px solid #1e293b;
}

/* 缺陷瞄准 HUD 框 (军工级靶向标定框) */
.defect-hud-bounding {
  position: absolute;
  border: 1px dashed #ef4444;
  background: rgba(239, 68, 68, 0.22);
  z-index: 10;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.4);
}

.bracket { position: absolute; width: 6px; height: 6px; border-color: #ef4444; }
.corner-tl { top: -2px; left: -2px; border-top: 2px solid; border-left: 2px solid; }
.corner-tr { top: -2px; right: -2px; border-top: 2px solid; border-right: 2px solid; }
.corner-bl { bottom: -2px; left: -2px; border-bottom: 2px solid; border-left: 2px solid; }
.corner-br { bottom: -2px; right: -2px; border-bottom: 2px solid; border-right: 2px solid; }

.scanning-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, transparent, #ef4444, transparent);
  box-shadow: 0 0 6px #ef4444;
  animation: scan-loop 2s infinite linear;
}

@keyframes scan-loop {
  0% { left: 0%; opacity: 0.9; }
  100% { left: 100%; opacity: 0.9; }
}

.hud-defect-badge {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  background: #dc2626;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.5);
}

.pulse-alarm-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 4px #ffffff;
  animation: pulse-dot 1s infinite ease-in-out;
}

.stage-dock-footer {
  position: absolute;
  bottom: 10px;
  left: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  background: rgba(15, 23, 42, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 6px 14px;
  border-radius: 6px;
  backdrop-filter: blur(6px);
}

.stage-dock-footer strong {
  color: #f8fafc;
}

.icon-accent {
  color: #38bdf8;
}

.split-line {
  color: #475569;
  margin: 0 4px;
}

/* ================= 柱面展开图与雷达图 ================= */
.chart-drawing-box {
  width: 100%;
  height: 185px;
}

.chart-legend-row {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 11px;
  color: #64748b;
}

.legend-unit {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-unit .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.dot.ok-dot {
  background-color: #059669;
  box-shadow: 0 0 5px #059669;
}

.dot.ng-dot {
  background-color: #ef4444;
  box-shadow: 0 0 5px #ef4444;
}

.radar-drawing-box {
  width: 100%;
  height: 185px;
}

.radar-tip-tag {
  font-size: 11px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.pulse-point {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #0284c7;
  box-shadow: 0 0 6px #0284c7;
}

/* ================= 自动化协同流水线 ================= */
.flow-status-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  font-weight: 700;
  color: #0284c7;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  padding: 2px 8px;
  border-radius: 9999px;
}

.pulse-live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0284c7;
  box-shadow: 0 0 6px #0284c7;
  animation: pulse-dot 1.5s infinite ease-in-out;
}

.flow-steps-deck {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
}

.flow-step-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  opacity: 0.5;
  transition: all 0.25s;
}

.flow-step-cell.step-done,
.flow-step-cell.step-active {
  opacity: 1;
}

.step-num-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #e2e8f0;
  border: 1.5px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #64748b;
  transition: all 0.2s;
}

.step-done .step-num-icon {
  background: #10b981;
  border-color: #10b981;
  color: #ffffff;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.4);
}

.step-active .step-num-icon {
  background: #0284c7;
  border-color: #0284c7;
  color: #ffffff;
  box-shadow: 0 0 10px rgba(2, 132, 199, 0.45);
}

.step-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.step-desc {
  font-size: 11px;
  color: #64748b;
}

.step-connector {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #cbd5e1;
  font-size: 12px;
  margin-left: 6px;
}

.conn-line {
  width: 14px;
  height: 1.5px;
  background: #e2e8f0;
}

/* ================= 底层工作坞表格 (Clean Tab & Table) ================= */
.dock-tabs-container {
  padding-bottom: 10px;
}

::v-deep .engineering-tabs .el-tabs__header {
  margin-bottom: 10px;
}

::v-deep .engineering-tabs .el-tabs__item {
  color: #64748b !important;
  font-size: 13px !important;
  height: 34px !important;
  line-height: 34px !important;
  font-weight: 600;
}

::v-deep .engineering-tabs .el-tabs__item.is-active {
  color: #0284c7 !important;
  font-weight: 800 !important;
}

::v-deep .engineering-tabs .el-tabs__active-bar {
  background-color: #0284c7 !important;
  height: 2px !important;
}

.dock-table-wrapper {
  max-height: 180px;
  overflow-y: auto;
}

.engineering-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  text-align: left;
}

.engineering-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
  padding: 8px 12px;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 1;
  font-size: 11px;
  letter-spacing: 0.02em;
}

.engineering-table td {
  padding: 7px 12px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  transition: background 0.15s;
}

.engineering-table tr:hover td {
  background: #f8fafc;
}

.type-pill {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.status-chip {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-chip.success {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.status-chip.primary {
  background: #f0f9ff;
  color: #0284c7;
  border: 1px solid #bae6fd;
}

.status-chip.danger {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.status-chip.warning {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

/* 时间线 */
.timeline-flow-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 6px 8px;
}

.timeline-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 12px;
}

.time-col {
  width: 95px;
  color: #64748b;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.axis-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 10px;
  flex-shrink: 0;
  padding-top: 4px;
}

.axis-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #0284c7;
  box-shadow: 0 0 6px #0284c7;
}

.timeline-row.danger .axis-dot {
  background: #ef4444;
  box-shadow: 0 0 6px #ef4444;
}

.timeline-row.success .axis-dot {
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
}

.axis-track {
  width: 1.5px;
  height: 28px;
  background: #e2e8f0;
  margin-top: 4px;
}

.body-col {
  flex: 1;
}

.log-heading {
  color: #0f172a;
  font-weight: 700;
  font-size: 12px;
}

.log-sub {
  color: #64748b;
  font-size: 11px;
  margin-top: 2px;
}

/* 文本工具类 */
.text-success { color: #059669 !important; }
.text-danger { color: #dc2626 !important; }
.text-primary { color: #0284c7 !important; }
.text-secondary { color: #475569 !important; }
.text-muted { color: #94a3b8 !important; }
.font-bold { font-weight: 700; }
.font-medium { font-weight: 500; }
</style>

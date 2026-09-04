<template>
  <div class="archive-container">
    <!-- 顶部检索与概览 -->
    <div class="archive-header">
      <div class="search-box">
        <span class="pulse-emerald"></span>
        <span class="title-text">汽车半轴“一物一码”质量数字档案系统</span>
        <el-input
          v-model="searchId"
          placeholder="输入半轴条码 / 二维码序列号 (例如: HX-20260829-08)"
          size="small"
          style="width: 320px; margin-left: 16px;"
          clearable
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch">溯源查询</el-button>
        </el-input>
      </div>

      <div class="quick-export">
        <el-button type="primary" size="small" icon="el-icon-printer" @click="exportCertificate">
          导出半轴电子合格证 (PDF/印鉴)
        </el-button>
      </div>
    </div>

    <!-- 核心两栏：左侧工件身份+时间链路，右侧360°缺陷空间展开拓扑+质量特征雷达 -->
    <div class="archive-body">
      <!-- 左列：基本信息与流转时序卡 -->
      <div class="body-left">
        <!-- 身份卡片 -->
        <div class="card-panel">
          <div class="panel-title"><i class="el-icon-postcard"></i> 半轴唯一数字身份 (Digital Passport)</div>
          <div class="passport-grid">
            <div class="p-item">
              <span class="p-k">条码序列号:</span>
              <span class="p-v highlight">{{ currentRecord.serialId }}</span>
            </div>
            <div class="p-item">
              <span class="p-k">半轴规格配方:</span>
              <span class="p-v">{{ currentRecord.modelSpec }}</span>
            </div>
            <div class="p-item">
              <span class="p-k">钢材批次号:</span>
              <span class="p-v">40Cr-2026B-98</span>
            </div>
            <div class="p-item">
              <span class="p-k">总长度 / 杆径:</span>
              <span class="p-v">{{ currentRecord.dimensions }}</span>
            </div>
            <div class="p-item">
              <span class="p-k">运检AGV编号:</span>
              <span class="p-v">{{ currentRecord.agvId }}</span>
            </div>
            <div class="p-item">
              <span class="p-k">综合质检结论:</span>
              <el-tag size="mini" :type="currentRecord.verdictTag" effect="dark">{{ currentRecord.verdict }}</el-tag>
            </div>
          </div>
        </div>

        <!-- 生产与检测时序链路 -->
        <div class="card-panel" style="margin-top: 16px;">
          <div class="panel-title"><i class="el-icon-time"></i> 全生命周期质量追溯链路</div>
          <div class="trace-timeline">
            <el-timeline>
              <el-timeline-item
                v-for="(act, index) in currentRecord.timeline"
                :key="index"
                :timestamp="act.time"
                :color="act.color"
              >
                <div class="act-title">{{ act.node }}</div>
                <div class="act-desc">{{ act.desc }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>

      <!-- 右列：全周缺陷时空分布图 (横轴长度，纵轴旋转角度) + 关键几何精度 -->
      <div class="body-right">
        <!-- 360° 圆周柱面缺陷立体展开图 -->
        <div class="card-panel">
          <div class="panel-title flex-between">
            <span><i class="el-icon-c-scale-to-original"></i> 360° 轴表面缺陷空间坐标展开图谱</span>
            <span class="sub-tip">横轴: 轴向长度 (0~820mm) | 纵轴: 旋转角度 (0°~360°)</span>
          </div>

          <div class="cylinder-surface-map">
            <!-- 纵轴角度刻度 -->
            <div class="y-angle-marks">
              <span>360°</span>
              <span>270°</span>
              <span>180°</span>
              <span>90°</span>
              <span>0°</span>
            </div>
            <!-- 柱面展开画布网格 -->
            <div class="surface-grid">
              <!-- 模拟划痕/缺陷区域 -->
              <div
                v-for="(def, idx) in currentRecord.defects"
                :key="idx"
                class="defect-feature-mark"
                :style="{
                  left: (def.locMm / 820) * 100 + '%',
                  top: (def.angle / 360) * 100 + '%'
                }"
              >
                <div class="def-halo"></div>
                <div class="def-tooltip">
                  <b>{{ def.type }}</b> [{{ def.angle }}°, {{ def.locMm }}mm]
                  <br>置信度: {{ def.conf }}% | 面积: {{ def.area }}px²
                </div>
              </div>

              <!-- 轴体结构分段刻度线 -->
              <div class="shaft-segments">
                <div class="seg seg-1">法兰盘端 (0~120mm)</div>
                <div class="seg seg-2">轴杆身部 (120~710mm)</div>
                <div class="seg seg-3">花键端 (710~820mm)</div>
              </div>
            </div>
          </div>

          <!-- 缺陷明细清单表格 -->
          <div class="defect-table-box" style="margin-top: 16px;">
            <el-table
              :data="currentRecord.defects"
              size="mini"
              style="width: 100%"
              empty-text="全周无瑕疵，品质极佳"
            >
              <el-table-column prop="type" label="缺陷类型" width="130">
                <template slot-scope="scope">
                  <el-tag size="mini" type="danger">{{ scope.row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="angle" label="旋转角度" width="100">
                <template slot-scope="scope">{{ scope.row.angle }}°</template>
              </el-table-column>
              <el-table-column prop="locMm" label="轴向坐标" width="110">
                <template slot-scope="scope">{{ scope.row.locMm }} mm</template>
              </el-table-column>
              <el-table-column prop="conf" label="AI识别置信度" width="120">
                <template slot-scope="scope">{{ scope.row.conf }}%</template>
              </el-table-column>
              <el-table-column prop="area" label="像素面积 (px²)" width="120"></el-table-column>
              <el-table-column prop="level" label="危害分级">
                <template slot-scope="scope">
                  <span :class="scope.row.level === '严重' ? 'text-red' : 'text-amber'">
                    {{ scope.row.level }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 几何形位公差遥测 -->
        <div class="card-panel" style="margin-top: 16px;">
          <div class="panel-title"><i class="el-icon-odometer"></i> 形位公差与装配配合遥测</div>
          <div class="geo-specs-grid">
            <div class="geo-item">
              <span class="g-k">径向圆跳动:</span>
              <span class="g-v text-emerald">0.015 mm (标定: ≤0.03)</span>
            </div>
            <div class="geo-item">
              <span class="g-k">花键同轴度:</span>
              <span class="g-v text-emerald">0.018 mm (标定: ≤0.04)</span>
            </div>
            <div class="geo-item">
              <span class="g-k">表面粗糙度 Ra:</span>
              <span class="g-v text-emerald">0.78 μm (标定: ≤1.6)</span>
            </div>
            <div class="geo-item">
              <span class="g-k">淬火硬化层深度:</span>
              <span class="g-v text-cyan">3.2 mm (合格)</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QualityArchive',
  data() {
    return {
      searchId: 'HX-20260829-08',
      currentRecord: {
        serialId: 'HX-20260829-08',
        modelSpec: '乘用车高端驱动半轴 (HX-820)',
        dimensions: '820 mm / Φ34.5 mm',
        agvId: 'AGV-02 (载具#3工装)',
        verdict: '次品 (待返工)',
        verdictTag: 'danger',
        timeline: [
          { time: '14:20:12', node: '上料区装夹', desc: '半轴进入 AGV-02 顶部双主动滚轮托盘', color: '#10b981' },
          { time: '14:21:05', node: '自主转运完成', desc: 'AGV 精准停靠至视觉检测工位 (偏差0.02mm)', color: '#10b981' },
          { time: '14:21:30', node: '360°间歇旋转全量采图', desc: '完成 0°~330° 共12组多角度高清成像', color: '#10b981' },
          { time: '14:21:45', node: 'AI模型推理与初检', desc: '检出 60° 处存在划伤，150° 存在疑似气孔', color: '#f59e0b' },
          { time: '14:22:10', node: '疑难件复检仲裁', desc: '经二次偏振复检确诊划伤瑕疵，判定次品', color: '#ef4444' },
          { time: '14:22:50', node: 'AGV柔性分流完毕', desc: 'AGV 自主引导卸料至【缺陷件暂存区】', color: '#38bdf8' }
        ],
        defects: [
          { type: '表面机械划痕', angle: 60, locMm: 145.2, conf: 94.7, area: 1420, level: '严重' },
          { type: '轻度磕碰凹坑', angle: 150, locMm: 280.0, conf: 81.2, area: 420, level: '轻度' }
        ]
      }
    }
  },
  methods: {
    handleSearch() {
      if (!this.searchId) {
        this.$message.warning('请输入半轴唯一编号！')
        return
      }
      this.$message.success(`已成功调取半轴 ${this.searchId} 的全维度数字档案`)
    },
    exportCertificate() {
      this.$message.success('电子质量追溯证书生成完毕，已触发下载！')
    }
  }
}
</script>

<style scoped>
.archive-container {
  padding: 16px;
  background-color: #0b1325;
  color: #e2e8f0;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #132238;
  padding: 12px 20px;
  border-radius: 8px;
  border: 1px solid #1e3a8a;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
}

.pulse-emerald {
  width: 10px;
  height: 10px;
  background-color: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 8px #10b981;
}

.title-text {
  font-size: 15px;
  font-weight: bold;
  color: #38bdf8;
  margin-left: 10px;
}

.archive-body {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
}

.card-panel {
  background: #111e36;
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 14px;
}

.panel-title {
  font-size: 13px;
  font-weight: bold;
  color: #38bdf8;
  border-bottom: 1px solid #1e293b;
  padding-bottom: 8px;
  margin-bottom: 12px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-tip {
  font-size: 11px;
  color: #94a3b8;
  font-weight: normal;
}

/* 身份网格 */
.passport-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.p-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  background: #172744;
  padding: 6px 10px;
  border-radius: 4px;
}

.p-k {
  color: #94a3b8;
}

.p-v {
  font-weight: bold;
  color: #f1f5f9;
}

.highlight {
  color: #38bdf8 !important;
}

/* 时间线 */
.trace-timeline {
  max-height: 440px;
  overflow-y: auto;
  padding-left: 6px;
}

.act-title {
  font-size: 12px;
  font-weight: bold;
  color: #f1f5f9;
}

.act-desc {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

/* 360 柱面展开图 */
.cylinder-surface-map {
  display: flex;
  height: 220px;
  background: #09101f;
  border: 1px solid #1e293b;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
}

.y-angle-marks {
  width: 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px 4px;
  font-size: 10px;
  color: #64748b;
  text-align: right;
  border-right: 1px dashed #1e293b;
}

.surface-grid {
  flex: 1;
  position: relative;
  background-image: linear-gradient(rgba(30, 41, 59, 0.5) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(30, 41, 59, 0.5) 1px, transparent 1px);
  background-size: 25px 25px;
}

.shaft-segments {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 24px;
  display: flex;
  border-top: 1px solid #1e293b;
  background: rgba(15, 23, 42, 0.8);
  font-size: 10px;
  color: #94a3b8;
  text-align: center;
  line-height: 24px;
}

.seg-1 { width: 14.6%; border-right: 1px dashed #334155; }
.seg-2 { width: 72%; border-right: 1px dashed #334155; }
.seg-3 { width: 13.4%; }

.defect-feature-mark {
  position: absolute;
  width: 16px;
  height: 16px;
  transform: translate(-50%, -50%);
  cursor: pointer;
}

.def-halo {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #ef4444;
  box-shadow: 0 0 10px #ef4444;
  animation: beat 1.2s infinite;
}

@keyframes beat {
  0% { transform: scale(0.9); opacity: 1; }
  50% { transform: scale(1.6); opacity: 0.5; }
  100% { transform: scale(0.9); opacity: 1; }
}

.def-tooltip {
  display: none;
  position: absolute;
  bottom: 22px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid #ef4444;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 11px;
  color: #f1f5f9;
  white-space: nowrap;
  z-index: 10;
}

.defect-feature-mark:hover .def-tooltip {
  display: block;
}

/* 几何公差网格 */
.geo-specs-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.geo-item {
  background: #172744;
  padding: 10px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

.g-k {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.g-v {
  font-size: 12px;
  font-weight: bold;
}

.text-emerald { color: #10b981; }
.text-cyan { color: #06b6d4; }
.text-red { color: #ef4444; }
.text-amber { color: #f59e0b; }
</style>

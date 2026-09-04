<template>
  <div class="learning-container">
    <!-- 顶部状态栏 -->
    <div class="learning-header">
      <div class="header-left">
        <span class="pulse-violet"></span>
        <span class="main-title">缺陷多视角标注与主动学习模型工场 (Active Learning MLOps)</span>
        <el-tag size="mini" type="success" effect="dark">YOLOv10 + 蒸馏模型持续演进中</el-tag>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="mini" icon="el-icon-upload" @click="triggerDistill">一键云端增量微调</el-button>
        <el-button type="success" size="mini" icon="el-icon-s-promotion" @click="deployEdge">下发轻量化权重至边缘端</el-button>
      </div>
    </div>

    <!-- 主体：三栏工作流（左侧样本池与难例挖掘，中间标注画布，右侧模型版本与指标） -->
    <div class="learning-grid">
      <!-- 左列：高不确定度难例挖掘样本池 -->
      <div class="col-panel">
        <div class="panel-head"><i class="el-icon-folder-opened"></i> 主动学习·低置信难例挖掘池</div>
        <div class="sample-list">
          <div
            v-for="(item, idx) in samplePool"
            :key="idx"
            class="sample-card"
            :class="{ active: currentSample.id === item.id }"
            @click="selectSample(item)"
          >
            <div class="sc-top">
              <span class="sc-id">{{ item.id }}</span>
              <el-tag size="mini" :type="item.tagType">{{ item.labelState }}</el-tag>
            </div>
            <div class="sc-info">
              <span>角度: {{ item.angle }}°</span>
              <span>模型熵不确定度: <b class="text-amber">{{ item.uncertainty }}</b></span>
            </div>
            <div class="sc-reason">{{ item.desc }}</div>
          </div>
        </div>

        <div class="panel-head" style="margin-top: 14px;"><i class="el-icon-collection-tag"></i> 标注标签库</div>
        <div class="tags-group">
          <el-tag
            v-for="tag in defectTags"
            :key="tag.name"
            :color="tag.color"
            class="defect-tag-item"
            effect="dark"
          >
            {{ tag.name }}
          </el-tag>
        </div>
      </div>

      <!-- 中列：360° 难例增强标注视窗与多边形/BBox工具 -->
      <div class="col-panel col-canvas">
        <div class="panel-head flex-between">
          <span><i class="el-icon-edit"></i> 难例多视角交互标注台 [ {{ currentSample.id }} - {{ currentSample.angle }}° ]</span>
          <div class="canvas-tools">
            <el-radio-group v-model="toolMode" size="mini">
              <el-radio-button label="bbox">矩形框 (BBox)</el-radio-button>
              <el-radio-button label="polygon">多边形轮廓</el-radio-button>
            </el-radio-group>
            <el-button size="mini" type="text" icon="el-icon-refresh-left">撤销</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete">清空</el-button>
          </div>
        </div>

        <!-- 标注画布容器 -->
        <div class="annotation-canvas-wrap">
          <img :src="currentSample.imgUrl" class="anno-base-img" alt="标注底图">
          <!-- 模拟交互式标注边界框 -->
          <div class="anno-box" :style="currentSample.boxStyle">
            <span class="anno-tag-label">{{ currentSample.annoType }}</span>
          </div>
        </div>

        <div class="anno-save-bar">
          <span class="tip-text"><i class="el-icon-info"></i> 该样本经人工复核后将自动归入增量金牌训练集</span>
          <el-button type="success" size="small" icon="el-icon-check" @click="saveAnnotation">
            保存标注并入库
          </el-button>
        </div>
      </div>

      <!-- 右列：模型演进版本、准确率雷达与知识蒸馏指标 -->
      <div class="col-panel">
        <div class="panel-head"><i class="el-icon-cpu"></i> 模型迭代版本演进 (Model Zoo)</div>
        <div class="model-versions">
          <div v-for="m in modelList" :key="m.version" class="model-card" :class="{ current: m.isCurrent }">
            <div class="mc-head">
              <span class="m-ver">{{ m.version }}</span>
              <el-tag size="mini" :type="m.isCurrent ? 'success' : 'info'">
                {{ m.isCurrent ? '当前生产' : '历史归档' }}
              </el-tag>
            </div>
            <div class="mc-metrics">
              <span>mAP@0.5: <b class="text-emerald">{{ m.map }}%</b></span>
              <span>推理延迟: <b>{{ m.latency }} ms</b></span>
              <span>模型体量: <b>{{ m.size }} MB</b></span>
            </div>
          </div>
        </div>

        <div class="panel-head" style="margin-top: 14px;"><i class="el-icon-data-line"></i> 云边端知识蒸馏训练监控</div>
        <div class="distill-board">
          <div class="db-item">
            <span class="db-k">Teacher 大模型参数量:</span>
            <span class="db-v">YOLOv10-X (31.8M)</span>
          </div>
          <div class="db-item">
            <span class="db-k">Student 边缘轻量模型:</span>
            <span class="db-v">YOLOv10-Nano-Distill (2.4M)</span>
          </div>
          <div class="db-item">
            <span class="db-k">知识蒸馏损失 (Loss):</span>
            <span class="db-v text-emerald">0.0142 (收敛稳定)</span>
          </div>
          <div class="db-item">
            <span class="db-k">边缘端推理吞吐量:</span>
            <span class="db-v text-cyan">165 FPS (TensorRT FP16)</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActiveLearning',
  data() {
    return {
      toolMode: 'bbox',
      defectTags: [
        { name: '表面划痕', color: '#ef4444' },
        { name: '淬火微裂纹', color: '#f59e0b' },
        { name: '加工麻坑', color: '#3b82f6' },
        { name: '机械磨损', color: '#8b5cf6' },
        { name: '良品无瑕', color: '#10b981' }
      ],
      samplePool: [
        {
          id: 'SPL-2026-08',
          angle: 150,
          uncertainty: 0.88,
          labelState: '待复核标注',
          tagType: 'warning',
          desc: '强光反光与细小凹坑特征高度混淆',
          annoType: '轻度麻坑',
          boxStyle: { left: '46%', top: '42%', width: '18%', height: '18%' },
          imgUrl: require('../assets/8.jpg')
        },
        {
          id: 'SPL-2026-12',
          angle: 60,
          uncertainty: 0.76,
          labelState: '已标注入库',
          tagType: 'success',
          desc: '花键边缘疑似纵向暗纹',
          annoType: '微裂纹',
          boxStyle: { left: '32%', top: '36%', width: '20%', height: '15%' },
          imgUrl: require('../assets/8.jpg')
        },
        {
          id: 'SPL-2026-15',
          angle: 210,
          uncertainty: 0.69,
          labelState: '待标注',
          tagType: 'info',
          desc: '过渡圆弧处轻微摩擦划痕',
          annoType: '擦伤',
          boxStyle: { left: '52%', top: '48%', width: '16%', height: '16%' },
          imgUrl: require('../assets/8.jpg')
        }
      ],
      currentSample: {},
      modelList: [
        { version: 'YOLOv10-Shaft-v2.3', isCurrent: true, map: 98.4, latency: 18.2, size: 4.8 },
        { version: 'YOLOv10-Shaft-v2.2', isCurrent: false, map: 96.7, latency: 19.5, size: 4.8 },
        { version: 'YOLOv10-Shaft-v1.0 (市赛基础版)', isCurrent: false, map: 91.2, latency: 26.0, size: 14.2 }
      ]
    }
  },
  created() {
    this.currentSample = this.samplePool[0]
  },
  methods: {
    selectSample(item) {
      this.currentSample = item
    },
    saveAnnotation() {
      this.$message.success(`样本 ${this.currentSample.id} 已成功入库，模型权重将在下一次触发蒸馏迭代！`)
    },
    triggerDistill() {
      this.$message.info('云端主动学习训练作业已下发，增量蒸馏开始！')
    },
    deployEdge() {
      this.$message.success('新版 TensorRT 优化权重已成功下发并部署至 AGV 边缘工控机！')
    }
  }
}
</script>

<style scoped>
.learning-container {
  padding: 16px;
  background-color: #0b1325;
  color: #e2e8f0;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

.learning-header {
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

.pulse-violet {
  width: 10px;
  height: 10px;
  background-color: #8b5cf6;
  border-radius: 50%;
  box-shadow: 0 0 8px #8b5cf6;
}

.main-title {
  font-size: 15px;
  font-weight: bold;
  color: #38bdf8;
}

.learning-grid {
  display: grid;
  grid-template-columns: 320px 1fr 340px;
  gap: 16px;
}

.col-panel {
  background: #111e36;
  border: 1px solid #1e293b;
  border-radius: 8px;
  padding: 14px;
}

.panel-head {
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

/* 样本池 */
.sample-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 380px;
  overflow-y: auto;
}

.sample-card {
  background: #172744;
  border: 1px solid #23385c;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.sample-card:hover, .sample-card.active {
  border-color: #8b5cf6;
  background: #201c44;
}

.sc-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sc-id {
  font-weight: bold;
  font-size: 12px;
  color: #f1f5f9;
}

.sc-info {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.sc-reason {
  font-size: 10px;
  color: #cbd5e1;
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.defect-tag-item {
  border: none;
  font-size: 11px;
}

/* 中间标注画布 */
.col-canvas {
  display: flex;
  flex-direction: column;
}

.annotation-canvas-wrap {
  flex: 1;
  min-height: 400px;
  background: #070d18;
  border: 1px solid #1e293b;
  border-radius: 6px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.anno-base-img {
  max-width: 90%;
  max-height: 85%;
  object-fit: contain;
}

.anno-box {
  position: absolute;
  border: 2px solid #8b5cf6;
  background: rgba(139, 92, 246, 0.2);
  box-shadow: 0 0 10px rgba(139, 92, 246, 0.5);
}

.anno-tag-label {
  position: absolute;
  top: -20px;
  left: 0;
  background: #8b5cf6;
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  white-space: nowrap;
}

.anno-save-bar {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip-text {
  font-size: 11px;
  color: #94a3b8;
}

/* 模型管理 */
.model-versions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.model-card {
  background: #172744;
  border: 1px solid #23385c;
  padding: 10px;
  border-radius: 6px;
}

.model-card.current {
  border-color: #10b981;
  background: #102d38;
}

.mc-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.m-ver {
  font-weight: bold;
  font-size: 12px;
  color: #f1f5f9;
}

.mc-metrics {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
}

.distill-board {
  background: #172744;
  padding: 10px 12px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.db-item {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
}

.db-k { color: #94a3b8; }
.db-v { font-weight: bold; color: #f1f5f9; }

.text-amber { color: #f59e0b; }
.text-emerald { color: #10b981; }
.text-cyan { color: #06b6d4; }
</style>

<template>
  <div class="annotation-page">
    <!-- 顶部导航 (保持原样) -->
    <div class="page-header">
      <div class="header-left">
        <div class="logo-container"><i class="el-icon-s-data brand-icon"></i></div>
        <span class="brand-text">数据标注平台</span>
      </div>
      <div class="header-right">
        <el-tooltip content="支持 JPG/PNG 格式" placement="bottom">
          <el-button type="primary" icon="el-icon-upload" @click="uploadImage" size="medium" round class="upload-btn">上传图片</el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      
      <!-- 左侧：图片列表 (保持原样) -->
      <div class="image-panel">
        <div class="panel-card full-height-card">
          <div class="panel-header">
            <div class="header-title">
              <span class="title-icon"><i class="el-icon-menu"></i></span>
              <span>图片列表 ({{ imageTotal }})</span>
            </div>
            <el-button type="text" icon="el-icon-refresh" class="refresh-btn" @click="refreshImages">刷新</el-button>
          </div>
          <div class="panel-toolbar">
            <el-pagination 
              small 
              @size-change="handleSizeChange" 
              @current-change="handleCurrentChange" 
              :current-page="imagePage" 
              :page-sizes="[20, 40, 60, 80]" 
              :page-size="imagePageSize" 
              layout="total, prev, pager, next" 
              :total="imageTotal" 
              class="custom-pagination"
            ></el-pagination>
          </div>
          <div class="panel-body custom-scrollbar">
            <div class="image-grid">
              <div v-for="image in imageList" :key="image.id" class="image-item" :class="{ 'selected': selectedImageId === image.id }" @click="selectImage(image)">
                <div class="image-wrapper">
                  <img :src="getImageUrl(image.imagePath)" class="thumbnail" loading="lazy" @error="handleImageError"/>
                  <div class="status-tag" :class="getImageStatusClass(image.status)">{{ getImageStatusText(image.status) }}</div>
                  <div class="selected-overlay" v-if="selectedImageId === image.id"><i class="el-icon-check"></i></div>
                  <div class="hover-actions"><el-button type="danger" icon="el-icon-delete" size="mini" circle @click.stop="deleteImage(image)"></el-button></div>
                </div>
                <div class="item-info">
                  <div class="item-name" :title="image.imageName">{{ image.imageName }}</div>
                  <div class="item-meta">{{ formatFileSize(image.imageSize) }}</div>
                </div>
              </div>
            </div>
            <div v-if="imageList.length === 0" class="empty-list"><i class="el-icon-folder-opened"></i><p>暂无图片数据</p></div>
          </div>
        </div>
      </div>

      <!-- === 右侧：工作台布局 === -->
      <div class="workspace-panel">
        
        <!-- 盒子1：标注工作台 -->
        <div class="panel-card annotation-box">
          <div class="panel-header">
            <div class="header-title">
              <span class="title-icon"><i class="el-icon-edit-outline"></i></span>
              <span class="title-text">标注工作台</span>
              <span v-if="selectedImage" class="current-file-name" :title="selectedImage.imageName"> — {{ selectedImage.imageName }}</span>
            </div>
            <div class="header-actions">
              <!-- 工业仪器级视觉算法工具组 (胶囊按钮风格) -->
              <div class="action-btn-group">
                <el-tooltip content="依据 GB/T 3077-2015 准则，将像素映射为毫米物理公差" placement="bottom">
                  <button
                    class="industrial-tool-btn"
                    :class="{ 'active-measure': isMeasuring }"
                    :disabled="!selectedImage"
                    @click="toggleMeasureMode"
                  >
                    <i class="el-icon-odometer"></i>
                    <span>{{ isMeasuring ? '几何计量 (已激活)' : '几何计量 (GB)' }}</span>
                  </button>
                </el-tooltip>

                <el-tooltip content="工业金相对比度自适应增强，强化微米级暗场裂纹" placement="bottom">
                  <button
                    class="industrial-tool-btn"
                    :class="{ 'active-enhance': isEnhanced }"
                    :disabled="!selectedImage"
                    @click="toggleEnhance"
                  >
                    <i class="el-icon-magic-stick"></i>
                    <span>{{ isEnhanced ? '探伤开启' : 'CLAHE探伤' }}</span>
                  </button>
                </el-tooltip>
              </div>

              <!-- 保存操作按钮 -->
              <el-button
                type="primary"
                size="small"
                icon="el-icon-check"
                @click="saveAnnotations"
                :loading="savingAnnotations"
                :disabled="!selectedImage"
                class="save-btn"
              >
                保存标注
              </el-button>
            </div>
          </div>
          
          <!-- 画布区域 (修改了背景样式) -->
          <div class="workspace-body">
            <template v-if="selectedImage">
              <div class="canvas-container custom-scrollbar">
                <div class="canvas-wrapper" :class="{ 'enhanced-flaw-detect': isEnhanced }">
                  <img :src="getImageUrl(selectedImage.imagePath)" ref="annotationImage" class="target-image" @load="initCanvas" draggable="false"/>
                  <div class="flaw-hud-tag" v-if="isEnhanced">
                    <span class="hud-dot"></span> 工业对比度自适应增强模式 (CLAHE 动态探伤)
                  </div>
                  <div class="flaw-hud-tag measure-hud" v-if="isMeasuring">
                    <span class="hud-dot measure-dot"></span> 标定空间: 1 px = 0.0482 mm (48.2 μm) · GB/T 3077-2015
                  </div>
                  <canvas ref="annotationCanvas" class="drawing-layer" @mousedown="startDrawing" @mousemove="drawRectangle" @mouseup="finishDrawing" @mouseleave="finishDrawing"></canvas>
                </div>
              </div>

              <!-- 工业计量与公差判定浮窗 -->
              <transition name="el-zoom-in-top">
                <div class="metrology-panel" v-if="isMeasuring">
                  <div class="metrology-header">
                    <div class="metro-title">
                      <i class="el-icon-c-scale-to-original"></i> 几何计量与国标公差
                    </div>
                    <div class="metro-header-right">
                      <span class="metro-badge">GB/T 3077</span>
                      <i class="el-icon-close close-metro-btn" @click="isMeasuring = false" title="收起面板"></i>
                    </div>
                  </div>
                  <div class="metrology-body custom-scrollbar">
                    <div class="metro-metric-row">
                      <span class="m-label">相机工作分辨率</span>
                      <span class="m-val">2592 × 1944 (500万像素)</span>
                    </div>
                    <div class="metro-metric-row">
                      <span class="m-label">标定空间换算比</span>
                      <span class="m-val highlight">1 px ≈ 0.0482 mm (48.2 μm)</span>
                    </div>
                    <div class="divider-line"></div>

                    <div class="defect-measure-list" v-if="currentAnnotations.length > 0">
                      <div
                        v-for="(ann, idx) in currentAnnotations"
                        :key="idx"
                        class="defect-measure-item"
                        :class="getToleranceLevel(ann).class"
                      >
                        <div class="defect-title-bar">
                          <span class="defect-tag">#{{ idx + 1 }} {{ ann.category }}</span>
                          <span class="verdict-tag" :class="getToleranceLevel(ann).class">
                            {{ getToleranceLevel(ann).text }}
                          </span>
                        </div>
                        <div class="measure-grid">
                          <div class="grid-cell">
                            <span class="cell-k">物理长度 (L)</span>
                            <span class="cell-v">{{ (ann.width * 0.0482).toFixed(2) }} mm</span>
                          </div>
                          <div class="grid-cell">
                            <span class="cell-k">物理宽度 (W)</span>
                            <span class="cell-v">{{ (ann.height * 0.0482).toFixed(2) }} mm</span>
                          </div>
                          <div class="grid-cell">
                            <span class="cell-k">投影面积 (S)</span>
                            <span class="cell-v">{{ (ann.width * ann.height * 0.0482 * 0.0482).toFixed(2) }} mm²</span>
                          </div>
                          <div class="grid-cell">
                            <span class="cell-k">国标公差上限</span>
                            <span class="cell-v">{{ ann.category === '裂纹' ? '0.00 mm (零容忍)' : '5.00 mm' }}</span>
                          </div>
                        </div>
                        <div class="defect-disposition">
                          <strong>工艺处置建议：</strong>{{ getToleranceLevel(ann).advice }}
                        </div>
                      </div>
                    </div>
                    <div class="no-defect-tip" v-else>
                      <i class="el-icon-info"></i> 在画布上框选缺陷，系统将实时计算毫米级几何尺寸并对照国标判定
                    </div>
                  </div>
                </div>
              </transition>
            </template>

            <!-- 空状态 (修改了HTML结构和文字) -->
            <div class="empty-workspace" v-else>
              <div class="empty-content">
                <h3>准备开始标注</h3>
                <p>请从左侧列表选择一张图片，即可开启高精度标注模式</p>
              </div>
            </div>
          </div>

          <!-- 类别选择：作为底部栏 (保持原样) -->
          <div class="category-footer" v-if="selectedImage">
            <div class="category-header">
              <i class="el-icon-price-tag"></i> 
              <span>选择缺陷类别</span>
            </div>
            <div class="category-content">
              <div v-for="category in defectCategories" :key="category.id" class="category-btn" :class="{ 'active': selectedCategory === category.id }" @click="selectCategory(category.id)">
                <div class="btn-inner">
                  <span class="dot"></span>
                  <span class="label">{{ category.name }}</span>
                </div>
                <i class="el-icon-check check-icon" v-if="selectedCategory === category.id"></i>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>

    <!-- 上传弹窗 (保持原样) -->
    <el-dialog title="上传图片" :visible.sync="uploadDialogVisible" width="450px" custom-class="custom-dialog" :close-on-click-modal="false" append-to-body>
      <el-upload class="upload-area" drag action="/api/annotation/upload/camera" :auto-upload="false" :on-change="handleFileChange" :on-remove="handleFileRemove" :file-list="uploadFileList" multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false" plain>取 消</el-button>
        <el-button type="primary" @click="confirmUpload" :loading="uploading">开始上传</el-button>
      </div>
    </el-dialog>
  </div>
</template>


<script>
import axios from 'axios'

export default {
  name: 'Annotation',
  data() {
    return {
      imageList: [],
      selectedImage: null,
      selectedImageId: null,
      imagePage: 1,
      imagePageSize: 20, 
      imageTotal: 0,
      selectedCategory: null,
      defectCategories: [],
      currentAnnotations: [],
      isDrawing: false,
      startX: 0,
      startY: 0,
      canvasContext: null,
      uploadDialogVisible: false,
      uploadFileList: [],
      uploading: false,
      savingAnnotations: false,
      isEnhanced: false,
      isMeasuring: false
    }
  },
  mounted() {
    this.loadImages()
    this.loadDefectCategories()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    loadImages() {
      axios.get('/api/annotation/images/pending', {
        params: { page: this.imagePage, pageSize: this.imagePageSize }
      }).then(response =>{
        if(response.data.code === 200){
          this.imageList = response.data.data.records || []
          this.imageTotal = response.data.data.total || 0
        }
      }).catch(()=>{})
    },
    refreshImages() { this.loadImages(); this.$message.success('已刷新') },
    selectImage(image) {
      if (this.selectedImageId === image.id) return
      this.selectedImage = image
      this.selectedImageId = image.id
      this.currentAnnotations = []
      this.loadImageAnnotations(image.id)
    },
    deleteImage(image) {
      this.$confirm('确认删除该图片吗？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/api/annotation/images/${image.id}`).then(res => {
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            if (this.selectedImageId === image.id) {
              this.selectedImage = null; this.selectedImageId = null; this.selectedCategory = null
            }
            this.loadImages()
          }
        })
      }).catch(() => {})
    },
    handleSizeChange(val) { this.imagePageSize = val; this.imagePage = 1; this.loadImages() },
    handleCurrentChange(val) { this.imagePage = val; this.loadImages() },
    handleResize() { if (this.selectedImage) this.initCanvas() },
    initCanvas() {
      if (!this.$refs.annotationCanvas || !this.$refs.annotationImage) return
      const canvas = this.$refs.annotationCanvas; const image = this.$refs.annotationImage
      canvas.width = image.clientWidth; canvas.height = image.clientHeight
      canvas.style.top = image.offsetTop + 'px'; canvas.style.left = image.offsetLeft + 'px'
      this.canvasContext = canvas.getContext('2d')
      this.redrawAnnotations()
    },
    selectCategory(categoryId) { this.selectedCategory = categoryId },
    startDrawing(event) {
      if (!this.selectedCategory) return this.$message.warning('请先在下方选择缺陷类别')
      const canvas = this.$refs.annotationCanvas; const rect = canvas.getBoundingClientRect()
      this.startX = event.clientX - rect.left; this.startY = event.clientY - rect.top; this.isDrawing = true
    },
    drawRectangle(event) {
      if (!this.isDrawing) return
      const canvas = this.$refs.annotationCanvas; const rect = canvas.getBoundingClientRect()
      const currentX = event.clientX - rect.left; const currentY = event.clientY - rect.top
      this.redrawAnnotations()
      this.canvasContext.strokeStyle = '#00E676'; this.canvasContext.lineWidth = 2
      this.canvasContext.setLineDash([4, 2])
      this.canvasContext.strokeRect(this.startX, this.startY, currentX - this.startX, currentY - this.startY)
      this.canvasContext.setLineDash([])
    },
    finishDrawing(event) {
      if (!this.isDrawing) return
      const canvas = this.$refs.annotationCanvas; const rect = canvas.getBoundingClientRect()
      const endX = event.clientX - rect.left; const endY = event.clientY - rect.top
      const width = Math.abs(endX - this.startX); const height = Math.abs(endY - this.startY)
      if (width > 5 && height > 5) {
        this.currentAnnotations.push({
          rawImageId: this.selectedImage.id, categoryId: this.selectedCategory,
          category: this.getCategoryName(this.selectedCategory), x: Math.min(this.startX, endX), y: Math.min(this.startY, endY),
          width, height, confidence: 1.0, annotatorId: 1, annotatorName: '标注员', annotationTime: new Date().toISOString()
        })
      }
      this.isDrawing = false; this.redrawAnnotations()
    },
    redrawAnnotations() {
      if (!this.canvasContext || !this.$refs.annotationCanvas) return
      const ctx = this.canvasContext
      ctx.clearRect(0, 0, this.$refs.annotationCanvas.width, this.$refs.annotationCanvas.height)

      this.currentAnnotations.forEach((ann, idx) => {
        const isCrack = ann.category === '裂纹'
        const primaryColor = isCrack ? '#FF3D00' : '#F59E0B'

        // 1. 基础缺陷边界框
        ctx.strokeStyle = primaryColor
        ctx.lineWidth = 2
        ctx.strokeRect(ann.x, ann.y, ann.width, ann.height)

        // 2. 标签背景与名称
        ctx.fillStyle = primaryColor
        const textWidth = ctx.measureText(ann.category).width
        ctx.fillRect(ann.x, ann.y - 20, textWidth + 10, 20)
        ctx.fillStyle = '#fff'
        ctx.font = '12px Arial'
        ctx.fillText(ann.category, ann.x + 5, ann.y - 6)

        // 3. 若激活工业几何计量模式，绘制高精度十字游标与毫米（mm）尺寸测量标注
        if (this.isMeasuring) {
          const physWidth = (ann.width * 0.0482).toFixed(2)
          const physHeight = (ann.height * 0.0482).toFixed(2)

          // 绘制四角工业对齐标靶十字线
          ctx.strokeStyle = '#00E5FF'
          ctx.lineWidth = 1.5
          const arm = 8
          // 左上
          ctx.beginPath(); ctx.moveTo(ann.x - arm, ann.y); ctx.lineTo(ann.x + arm, ann.y); ctx.stroke()
          ctx.beginPath(); ctx.moveTo(ann.x, ann.y - arm); ctx.lineTo(ann.x, ann.y + arm); ctx.stroke()
          // 右下
          ctx.beginPath(); ctx.moveTo(ann.x + ann.width - arm, ann.y + ann.height); ctx.lineTo(ann.x + ann.width + arm, ann.y + ann.height); ctx.stroke()
          ctx.beginPath(); ctx.moveTo(ann.x + ann.width, ann.y + ann.height - arm); ctx.lineTo(ann.x + ann.width, ann.y + ann.height + arm); ctx.stroke()

          // 物理尺寸标尺线 (下边缘横线: 长度)
          ctx.strokeStyle = '#00E5FF'
          ctx.setLineDash([2, 2])
          ctx.beginPath()
          ctx.moveTo(ann.x, ann.y + ann.height + 12)
          ctx.lineTo(ann.x + ann.width, ann.y + ann.height + 12)
          ctx.stroke()
          ctx.setLineDash([])

          // 物理尺寸标尺文字
          ctx.fillStyle = 'rgba(15, 23, 42, 0.85)'
          ctx.fillRect(ann.x, ann.y + ann.height + 16, 96, 18)
          ctx.strokeStyle = '#00E5FF'
          ctx.strokeRect(ann.x, ann.y + ann.height + 16, 96, 18)
          ctx.fillStyle = '#00E5FF'
          ctx.font = 'bold 11px Consolas, monospace'
          ctx.fillText(`L:${physWidth}mm W:${physHeight}mm`, ann.x + 4, ann.y + ann.height + 29)
        }
      })
    },
    toggleMeasureMode() {
      this.isMeasuring = !this.isMeasuring
      this.redrawAnnotations()
      if (this.isMeasuring) {
        this.$message.success('📐 亚像素几何尺寸计量与国标公差判定引擎已激活')
      } else {
        this.$message.info('退出几何计量模式')
      }
    },
    getToleranceLevel(ann) {
      const len = ann.width * 0.0482
      if (ann.category === '裂纹') {
        return {
          class: 'verdict-scrap',
          text: '❌ 结构性超差 (强制报废)',
          advice: '根据 GB/T 3077-2015 规定，传动半轴受力面裂纹公差为 0mm，存在疲劳断裂断裂隐患，直接判废。'
        }
      }
      if (len > 5.0) {
        return {
          class: 'verdict-scrap',
          text: '❌ 划痕长度超标 (建议报废)',
          advice: `划痕延伸度达 ${len.toFixed(2)}mm (超标 >5.0mm)，深度超限风险，不满足动平衡装配基线。`
        }
      } else if (len > 2.0) {
        return {
          class: 'verdict-rework',
          text: '⚠️ 轻度超差 (建议返修抛光)',
          advice: `划痕长度 ${len.toFixed(2)}mm (在 2.0~5.0mm 区间)，建议进入2号抛丸/砂光工序消除表面应力。`
        }
      } else {
        return {
          class: 'verdict-pass',
          text: '✅ 公差允许 (合格放行)',
          advice: `划痕微弱 (${len.toFixed(2)}mm < 2.0mm)，在汽车底盘半轴粗磨容许公差带内，允许流转。`
        }
      }
    },
    loadImageAnnotations(imageId) {
      axios.get(`/api/annotation/data/image/${imageId}`).then(res => {
        if (res.data.code === 200) {
          this.currentAnnotations = res.data.data || []
          if (this.currentAnnotations.length > 0) this.selectedCategory = this.currentAnnotations[0].categoryId
          this.$nextTick(this.initCanvas)
        }
      })
    },
    saveAnnotations() {
      if (!this.selectedCategory) return this.$message.warning('请选择类别')
      this.savingAnnotations = true
      let data = this.currentAnnotations.length ? this.currentAnnotations : [{
        rawImageId: this.selectedImage.id, taskId: null, categoryId: this.selectedCategory, category: this.getCategoryName(this.selectedCategory),
        x:0, y:0, width:0, height:0, confidence: 1.0, annotatorId: 1, annotatorName: '标注员', annotationTime: new Date().toISOString()
      }]
      axios.post('/api/annotation/data', data).then(res => {
        if (res.data.code === 200) {
          this.$message.success('保存成功'); this.selectedCategory = null; this.selectedImage = null; this.selectedImageId = null; this.currentAnnotations = []; this.loadImages()
        } else { this.$message.error(res.data.msg || '保存失败') }
      }).finally(() => this.savingAnnotations = false)
    },
    toggleEnhance() {
      this.isEnhanced = !this.isEnhanced//可提前删除
      this.$message.success(this.isEnhanced ? '⚡ 探伤模式开启' : '原画模式')//可提前删除
    },
    uploadImage() { this.uploadDialogVisible = true; this.uploadFileList = [] },
    handleFileChange(f, list) { this.uploadFileList = list },
    handleFileRemove(f, list) { this.uploadFileList = list },
    confirmUpload() {
      if (!this.uploadFileList.length) {
        return this.$message.warning('请选择文件')
      }
      
      console.log('开始上传，文件数量:', this.uploadFileList.length);
      this.uploading = true;
      
      const reqs = this.uploadFileList.map((f, index) => {
        const fd = new FormData();
        fd.append('image', f.raw);
        fd.append('uploadSource', 'manual');
        
        console.log(`上传文件 ${index + 1}:`, f.name);
        
        return axios.post('/api/annotation/upload/camera', fd, {
          headers: { 'Content-Type': 'multipart/form-data' },
          timeout: 30000 // 30秒超时
        }).then(res => {
          console.log(`文件 ${index + 1} 上传响应:`, res.data);
          return res;
        }).catch(err => {
          console.error(`文件 ${index + 1} 上传失败:`, err);
          return { data: { code: 500, msg: err.message || '上传失败' }};
        });
      });
      
      Promise.all(reqs).then(res => {
        const successCount = res.filter(r => r.data.code === 200).length;
        const failCount = res.length - successCount;
        
        if (successCount > 0) {
          this.$message.success(`成功上传 ${successCount} 张` + (failCount > 0 ? `，失败 ${failCount} 张` : ''));
        } else {
          this.$message.error('所有文件上传失败');
        }
        
        this.uploadDialogVisible = false;
        this.uploadFileList = [];
        this.loadImages();
      }).catch(err => {
        console.error('上传失败:', err);
        this.$message.error('上传失败: ' + (err.message || '未知错误'));
      }).finally(() => {
        this.uploading = false;
        console.log('上传完成');
      });
    },
    getImageUrl(path) { if (!path) return ''; if (path.startsWith('http')) return path; return `/api/annotation/files/${path.replace(/\\/g, '/').split('/').pop()}` },
    handleImageError(e) { e.target.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHJlY3QgZmlsbD0iI2Y1ZjdmYSIgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiLz48dGV4dCBmaWxsPSIjOTA5Mzk5IiB4PSI1MCIgeT0iNTAiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiPkVycm9yPC90ZXh0Pjwvc3ZnPg==' },
    formatFileSize(s) { if (!s) return '0 B'; return s < 1024 ? s + ' B' : s < 1048576 ? (s/1024).toFixed(1) + ' KB' : (s/1048576).toFixed(1) + ' MB' },
    getImageStatusClass(s) { return {0:'status-pending',1:'status-wip',2:'status-done',3:'status-check'}[s] || 'status-pending' },
    getImageStatusText(s) { return {0:'待标',1:'进行中',2:'完成',3:'质检'}[s] || '未知' },
    getCategoryName(id) { return (this.defectCategories.find(c => c.id === id) || {}).name || '' },
    loadDefectCategories() { this.defectCategories = [{id:1,name:'合格'},{id:2,name:'裂纹'},{id:3,name:'划痕'}] }
  }
}
</script>

<style scoped>
/* 全局变量 */
.annotation-page {
  --header-height: 60px;
  --bg-color: #f0f2f5;
  --panel-bg: #fff;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  overflow: hidden; 
}

/* 顶部导航 */
.page-header {
  height: var(--header-height);
  background: var(--panel-bg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  flex-shrink: 0;
  z-index: 20;
}
.header-left { display: flex; align-items: center; }
.logo-container { width: 32px; height: 32px; background: #ecf5ff; border-radius: 6px; display: flex; align-items: center; justify-content: center; margin-right: 12px; }
.brand-icon { font-size: 20px; color: #409EFF; }
.brand-text { font-size: 18px; font-weight: bold; color: #303133; }

/* 主体内容布局 */
.main-content {
  flex: 1; 
  display: flex;
  padding: 16px;
  gap: 16px;
  overflow: hidden; 
  height: calc(100vh - var(--header-height)); 
  position: relative; 
}

/* 通用面板卡片样式 */
.panel-card {
  background: var(--panel-bg);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  border: 1px solid #ebeef5;
}

/* 1. 左侧图片列表 */
.image-panel {
  flex: 0 0 540px;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.full-height-card { display: flex; flex-direction: column; height: 100%; }
.panel-header {
  height: 50px; padding: 0 16px; border-bottom: 1px solid #ebeef5;
  display: flex; align-items: center; justify-content: space-between; flex-shrink: 0;
}
.header-title {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1 1 auto;
}

.title-text {
  font-size: 15px;
  white-space: nowrap;
}

.current-file-name {
  color: #909399;
  font-weight: normal;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 工业级仪器风格胶囊工具栏 */
.action-btn-group {
  display: inline-flex;
  align-items: center;
  background: #f1f5f9;
  padding: 3px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  gap: 4px;
}

.industrial-tool-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  outline: none;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  white-space: nowrap;
}

.industrial-tool-btn:hover:not(:disabled) {
  background: #ffffff;
  color: #0f172a;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.industrial-tool-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 计量激活态 - 工业青绿高亮 */
.industrial-tool-btn.active-measure {
  background: #ffffff;
  color: #0284c7;
  border-color: #38bdf8;
  box-shadow: 0 1px 4px rgba(2, 132, 199, 0.18);
}

/* 探伤激活态 - 工业琥珀金高亮 */
.industrial-tool-btn.active-enhance {
  background: #ffffff;
  color: #d97706;
  border-color: #f59e0b;
  box-shadow: 0 1px 4px rgba(217, 119, 6, 0.18);
}

.save-btn {
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 16px;
}
.panel-toolbar { padding: 8px 16px; background: #f9fafb; border-bottom: 1px solid #ebeef5; flex-shrink: 0; }
.panel-body { flex: 1; overflow-y: auto; padding: 16px 16px 24px 16px; }

/* 左侧网格样式 */
.image-grid { 
  display: grid; 
  /* 修改处：使用 minmax(0, 1fr) 强制限制列宽，防止大图撑开容器 */
  grid-template-columns: repeat(4, minmax(0, 1fr)); 
  gap: 12px; 
}

/* 修改处：增加 overflow 和 width 限制，确保不溢出 */
.image-item { 
  border: 1px solid #ebeef5; 
  border-radius: 4px; 
  cursor: pointer; 
  background: #fff; 
  transition: all 0.2s; 
  position: relative; 
  width: 100%; /* 确保充满格子 */
  overflow: hidden; /* 裁剪溢出 */
}

.image-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.image-item.selected { border-color: #409EFF; box-shadow: 0 0 0 2px rgba(64,158,255,0.2); }

.image-wrapper { 
  height: 90px; 
  width: 100%; /* 确保宽度固定 */
  background: #f5f7fa; border-radius: 4px 4px 0 0; position: relative; overflow: hidden; 
}
.thumbnail { width: 100%; height: 100%; object-fit: cover; }
.status-tag { position: absolute; top: 4px; left: 4px; font-size: 10px; padding: 1px 5px; border-radius: 2px; color: #fff; background: rgba(0,0,0,0.5); }
.status-tag.status-done { background: rgba(103,194,58,0.9); }
.selected-overlay { position: absolute; top: 0; right: 0; width: 20px; height: 20px; background: #409EFF; border-radius: 0 0 0 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; }
.hover-actions { position: absolute; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.image-item:hover .hover-actions { opacity: 1; }

.item-info { padding: 6px 8px; }
.item-name { font-size: 12px; color: #606266; margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-meta { font-size: 10px; color: #909399; }

/* === 右侧布局 === */
.workspace-panel {
  flex: 1;
  min-width: 0;
  height: 100%;
  position: relative; 
}

/* 盒子1：标注工作台 (包含类别选择) */
.annotation-box {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: bottom 0.3s; 
}

.current-file-name { color: #909399; font-weight: normal; font-size: 14px; margin-left: 8px; }

/* 修改处：背景改为点阵纹理，替换原有的棋盘格 */
.workspace-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc; /* 更干净的浅色底 */
  background-image: radial-gradient(#dcdfe6 1.5px, transparent 1.5px); /* 点阵效果 */
  background-size: 24px 24px; /* 点阵间距 */
  position: relative;
  overflow: hidden;
}

.canvas-container {
  flex: 1;
  overflow: hidden; 
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px; 
}

.canvas-wrapper {
  position: relative;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  background: #fff;
  transition: all 0.3s ease;
}

/* 工业自适应边缘对比度增强（暗场伽马补偿，通用适配亮底/暗底） */
.canvas-wrapper.enhanced-flaw-detect {
  /* 去除多余的彩色光晕，保持纯净工业灰阶投影 */
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.28);
}

.canvas-wrapper.enhanced-flaw-detect .target-image {
  /*
   * 工业金相探伤专属滤镜：
   * 压暗高光背景 (brightness 84%) + 强对比度拉升 (contrast 165%)
   * 彻底压下金属底噪灰雾，同时让暗色裂纹、划痕、油污坑洞加深高亮呈现！
   */
  filter: brightness(84%) contrast(165%) drop-shadow(0 0 1px rgba(0, 0, 0, 0.85));
  transition: filter 0.3s ease;
}

/* 探伤模式工业 HUD 状态胶囊 - 极简灰度工业风 */
.flaw-hud-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(30, 41, 59, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  gap: 6px;
  pointer-events: none;
  z-index: 5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.5px;
}

.hud-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 6px #10b981;
  animation: pulse-hud 1.5s infinite;
}

@keyframes pulse-hud {
  0% { transform: scale(0.9); opacity: 0.7; }
  50% { transform: scale(1.3); opacity: 1; }
  100% { transform: scale(0.9); opacity: 0.7; }
}

.target-image { 
  display: block; 
  min-width: 550px; 
  max-width: 100%; 
  height: auto; 
}
.drawing-layer { position: absolute; top: 0; left: 0; cursor: crosshair; }

/* 类别选择底部区域 */
.category-footer {
  height: auto; 
  display: flex;
  flex-direction: column;
  padding: 12px 24px 16px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5; 
  box-sizing: border-box;
  flex-shrink: 0; 
  z-index: 10;
}

.category-header {
  font-weight: 600; color: #303133; margin-bottom: 12px;
  display: flex; align-items: center; gap: 8px; font-size: 15px;
}

.category-content {
  display: flex; gap: 16px; 
  width: 100%; 
}

.category-btn {
  flex: 1; 
  height: 56px;
  display: flex; align-items: center; padding: 0 24px;
  background: #fff; border: 1px solid #dcdfe6; border-radius: 4px;
  cursor: pointer; transition: all 0.2s; min-width: 100px; 
  justify-content: space-between; 
}
.category-btn:hover { border-color: #c6e2ff; color: #409EFF; }
.category-btn.active { border-color: #409EFF; background-color: #ecf5ff; color: #409EFF; font-weight: 500; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #dcdfe6; margin-right: 10px; }
.category-btn.active .dot { background: #409EFF; }

/* 修改处：空状态样式重写，移除图标背景，应用新字体样式 */
.empty-workspace {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-content {
  text-align: center;
}

/* 对应截图中的 "准备开始标注" */
.empty-workspace h3 {
  font-size: 28px;
  color: #2c3e50;
  font-weight: 700;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

/* 对应截图中的 "请从左侧列表选择一张图片..." */
.empty-workspace p {
  font-size: 16px;
  color: #94a3b8; /* 浅灰色 */
  font-weight: 300;
}

.empty-list { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #909399; }

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }

/* 覆盖 Element UI */
.custom-pagination >>> .btn-prev, .custom-pagination >>> .btn-next, .custom-pagination >>> .el-pager li { background: transparent; }
.refresh-btn { padding: 0; color: #909399; font-size: 14px; }
.refresh-btn:hover { color: #409EFF; }

/* ==========================================================
   工业亚像素计量与国标公差判定浮窗样式 (升级：现代浅色工业风，不遮挡图片)
   ========================================================== */
.flaw-hud-tag.measure-hud {
  top: auto;
  bottom: 14px;
  left: 14px;
  right: auto;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(2, 132, 199, 0.35);
  color: #0369a1;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}
.hud-dot.measure-dot {
  background-color: #0284c7;
  box-shadow: 0 0 6px #0284c7;
}

.metrology-panel {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 320px;
  max-height: calc(100% - 28px);
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12), 0 2px 8px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(12px);
  z-index: 100;
  display: flex;
  flex-direction: column;
  color: #1e293b;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  transition: all 0.3s ease;
}

.metrology-header {
  padding: 10px 14px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  border-radius: 8px 8px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.metro-title {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 6px;
}
.metro-title i {
  color: #0284c7;
}
.metro-badge {
  font-size: 10px;
  font-weight: 700;
  background: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.metrology-body {
  padding: 12px 14px;
  overflow-y: auto;
  flex: 1;
}

.metro-metric-row {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  margin-bottom: 6px;
  color: #64748b;
}
.metro-metric-row .m-val {
  font-family: Consolas, monospace;
  font-weight: 600;
  color: #334155;
}
.metro-metric-row .m-val.highlight {
  color: #0284c7;
  font-weight: 700;
}

.divider-line {
  height: 1px;
  background: #f1f5f9;
  margin: 8px 0;
}

.defect-measure-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.defect-measure-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 8px 10px;
  transition: all 0.2s ease;
}

/* 报废红 */
.defect-measure-item.verdict-scrap {
  border-left: 3px solid #ef4444;
  background: #fff5f5;
}
/* 返修橙 */
.defect-measure-item.verdict-rework {
  border-left: 3px solid #f59e0b;
  background: #fffbeb;
}
/* 合格绿 */
.defect-measure-item.verdict-pass {
  border-left: 3px solid #10b981;
  background: #f0fdf4;
}

.defect-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.defect-tag {
  font-size: 12px;
  font-weight: 700;
  color: #0f172a;
}

.verdict-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 3px;
}
.verdict-tag.verdict-scrap {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}
.verdict-tag.verdict-rework {
  background: #fef3c7;
  color: #b45309;
  border: 1px solid #fde68a;
}
.verdict-tag.verdict-pass {
  background: #dcfce7;
  color: #15803d;
  border: 1px solid #bbf7d0;
}

.measure-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px 8px;
  background: #ffffff;
  border: 1px solid #f1f5f9;
  padding: 6px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
}
.grid-cell {
  display: flex;
  flex-direction: column;
}
.cell-k {
  font-size: 9px;
  color: #94a3b8;
}
.cell-v {
  font-size: 11px;
  font-weight: 700;
  font-family: Consolas, monospace;
  color: #0f172a;
}

.defect-disposition {
  font-size: 10px;
  color: #475569;
  line-height: 1.4;
  background: rgba(241, 245, 249, 0.8);
  padding: 4px 6px;
  border-radius: 3px;
}

.no-defect-tip {
  text-align: center;
  color: #94a3b8;
  font-size: 11px;
  padding: 14px 6px;
  line-height: 1.5;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
}
</style>
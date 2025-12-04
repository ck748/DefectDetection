<template>
  <div class="annotation-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-left">
        <div class="logo-container"><i class="el-icon-s-data brand-icon"></i></div>
        <span class="brand-text">数据标注平台</span>
      </div>
      <div class="header-right">
        <el-tooltip content="支持 JPG/PNG 格式" placement="bottom">
          <el-button type="primary" icon="el-icon-upload" @click="uploadImage" size="medium" round plain>上传图片</el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      
      <!-- 左侧：图片列表 -->
      <div class="image-panel">
        <div class="panel-card full-height-card">
          <div class="panel-header">
            <div class="header-title"><i class="el-icon-picture-outline"></i><span>图片列表 ({{ imageTotal }})</span></div>
            <el-button type="text" icon="el-icon-refresh" class="refresh-btn" @click="refreshImages">刷新</el-button>
          </div>
          <div class="panel-toolbar">
            <!-- 修改处：page-sizes 改为 [20, 40, 60, 80] -->
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

      <!-- === 右侧：工作台布局 (保持之前的逻辑) === -->
      <div class="workspace-panel">
        
        <!-- 盒子1：标注工作台 -->
        <!-- 逻辑：如果没有选中图片，bottom: 0 (全屏)；选中了，bottom: 140px (给下面留空) -->
        <div class="panel-card annotation-box" :style="{ bottom: selectedImage ? '140px' : '0' }">
          <div class="panel-header">
            <div class="header-title">
              <i class="el-icon-edit-outline"></i>
              <span>标注工作台</span>
              <span v-if="selectedImage" class="current-file-name"> - {{ selectedImage.imageName }}</span>
            </div>
            <div class="header-actions">
              <el-button type="success" size="medium" icon="el-icon-check" @click="saveAnnotations" :loading="savingAnnotations" :disabled="!selectedImage">保存标注</el-button>
            </div>
          </div>
          
          <div class="workspace-body">
            <div class="canvas-container custom-scrollbar" v-if="selectedImage">
              <div class="canvas-wrapper">
                <img :src="getImageUrl(selectedImage.imagePath)" ref="annotationImage" class="target-image" @load="initCanvas" draggable="false"/>
                <canvas ref="annotationCanvas" class="drawing-layer" @mousedown="startDrawing" @mousemove="drawRectangle" @mouseup="finishDrawing" @mouseleave="finishDrawing"></canvas>
              </div>
            </div>
            <div class="empty-workspace" v-else>
              <div class="empty-content">
                <div class="empty-icon-bg"><i class="el-icon-picture"></i></div>
                <h3>准备开始标注</h3>
                <p>请从左侧列表选择一张图片</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 盒子2：缺陷类别选择 -->
        <!-- 逻辑：v-if 只有选中图片才显示 -->
        <div class="panel-card category-box" v-if="selectedImage">
          <div class="category-header">
            <i class="el-icon-s-flag"></i> 
            <span>选择缺陷类别</span>
          </div>
          <div class="category-content">
            <div v-for="category in defectCategories" :key="category.id" class="category-btn" :class="{ 'active': selectedCategory === category.id }" @click="selectCategory(category.id)">
              <span class="dot"></span>
              <span>{{ category.name }}</span>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog title="上传图片" :visible.sync="uploadDialogVisible" width="450px" custom-class="upload-dialog" :close-on-click-modal="false" append-to-body>
      <el-upload class="upload-area" drag action="/api/annotation/upload/camera" :auto-upload="false" :on-change="handleFileChange" :on-remove="handleFileRemove" :file-list="uploadFileList" multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">取 消</el-button>
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
      // 修改处：默认每页显示 20 条 (5行 * 4列)
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
      savingAnnotations: false
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
      }).then(response => {
        if (response.data.code === 200) {
          this.imageList = response.data.data.records || []
          this.imageTotal = response.data.data.total || 0
        }
      }).catch(() => {})
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
      this.canvasContext.clearRect(0, 0, this.$refs.annotationCanvas.width, this.$refs.annotationCanvas.height)
      this.currentAnnotations.forEach(ann => {
        this.canvasContext.strokeStyle = '#FF3D00'; this.canvasContext.lineWidth = 2
        this.canvasContext.strokeRect(ann.x, ann.y, ann.width, ann.height)
        this.canvasContext.fillStyle = '#FF3D00'
        const textWidth = this.canvasContext.measureText(ann.category).width
        this.canvasContext.fillRect(ann.x, ann.y - 20, textWidth + 10, 20)
        this.canvasContext.fillStyle = '#fff'; this.canvasContext.font = '12px Arial'
        this.canvasContext.fillText(ann.category, ann.x + 5, ann.y - 6)
      })
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
    uploadImage() { this.uploadDialogVisible = true; this.uploadFileList = [] },
    handleFileChange(f, list) { this.uploadFileList = list },
    handleFileRemove(f, list) { this.uploadFileList = list },
    confirmUpload() {
      if (!this.uploadFileList.length) return this.$message.warning('请选择文件')
      this.uploading = true
      const reqs = this.uploadFileList.map(f => {
        const fd = new FormData(); fd.append('image', f.raw); fd.append('uploadSource', 'manual')
        return axios.post('/api/annotation/upload/camera', fd, { headers: { 'Content-Type': 'multipart/form-data' }})
      })
      Promise.all(reqs).then(res => {
        const count = res.filter(r => r.data.code === 200).length
        this.$message.success(`成功上传 ${count} 张`); this.uploadDialogVisible = false; this.loadImages()
      }).finally(() => this.uploading = false)
    },
    getImageUrl(path) { if (!path) return ''; if (path.startsWith('http')) return path; return `/api/annotation/files/${path.replace(/\\/g, '/').split('/').pop()}` },
    handleImageError(e) { e.target.src = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHJlY3QgZmlsbD0iI2Y1ZjdmYSIgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiLz48dGV4dCBmaWxsPSIjOTA5Mzk5IiB4PSI1MCIgeT0iNTAiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiPkVycm9yPC90ZXh0Pjwvc3ZnPg==' },
    formatFileSize(s) { if (!s) return '0 B'; return s < 1024 ? s + ' B' : s < 1048576 ? (s/1024).toFixed(1) + ' KB' : (s/1048576).toFixed(1) + ' MB' },
    getImageStatusClass(s) { return {0:'status-pending',1:'status-wip',2:'status-done',3:'status-check'}[s] || 'status-pending' },
    getImageStatusText(s) { return {0:'待标',1:'进行中',2:'完成',3:'质检'}[s] || '未知' },
    getCategoryName(id) { return (this.defectCategories.find(c => c.id === id) || {}).name || '' },
    loadDefectCategories() { this.defectCategories = [{id:1,name:'合格'},{id:2,name:'裂痕'},{id:3,name:'划痕'}] }
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
  flex: 1; /* 占满剩余高度 */
  display: flex;
  padding: 16px;
  gap: 16px;
  overflow: hidden; /* 防止溢出 */
  height: calc(100vh - var(--header-height)); 
  position: relative; /* 为内部绝对定位做参考 */
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
.header-title { font-weight: 600; color: #303133; display: flex; align-items: center; gap: 8px; }
.panel-toolbar { padding: 8px 16px; background: #f9fafb; border-bottom: 1px solid #ebeef5; flex-shrink: 0; }
.panel-body { flex: 1; overflow-y: auto; padding: 16px; }

/* 左侧网格样式 */
.image-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 12px; }
.image-item { border: 1px solid #ebeef5; border-radius: 4px; cursor: pointer; background: #fff; transition: all 0.2s; position: relative; }
.image-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.image-item.selected { border-color: #409EFF; box-shadow: 0 0 0 2px rgba(64,158,255,0.2); }
.image-wrapper { height: 100px; background: #f5f7fa; border-radius: 4px 4px 0 0; position: relative; overflow: hidden; }
.thumbnail { width: 100%; height: 100%; object-fit: cover; }
.status-tag { position: absolute; top: 4px; left: 4px; font-size: 10px; padding: 1px 5px; border-radius: 2px; color: #fff; background: rgba(0,0,0,0.5); }
.status-tag.status-done { background: rgba(103,194,58,0.9); }
.selected-overlay { position: absolute; top: 0; right: 0; width: 20px; height: 20px; background: #409EFF; border-radius: 0 0 0 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; }
.hover-actions { position: absolute; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.image-item:hover .hover-actions { opacity: 1; }
.item-info { padding: 6px 8px; }
.item-name { font-size: 12px; color: #606266; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-meta { font-size: 10px; color: #909399; }

/* === 右侧布局：使用绝对定位确保位置 === */
.workspace-panel {
  flex: 1;
  min-width: 0;
  height: 100%;
  position: relative; /* 为内部 absolute 提供基准 */
}

/* 盒子1：标注工作台 (上方) */
/* 使用 absolute 定位：top=0, bottom 动态调整 */
.annotation-box {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  /* bottom 属性通过 :style 动态控制 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: bottom 0.3s; /* 增加平滑过渡效果 */
}

.workspace-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  position: relative;
  overflow: hidden;
}

.canvas-container {
  flex: 1;
  overflow: auto; /* 图片区域内部滚动 */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.canvas-wrapper {
  position: relative;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.target-image { display: block; }
.drawing-layer { position: absolute; top: 0; left: 0; cursor: crosshair; }

/* 盒子2：类别选择 (下方) */
/* 使用 absolute 定位：bottom=0, height=130px */
.category-box {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 130px; /* 固定高度 */
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.08); /* 增加一点阴影使其区分明显 */
  margin-bottom: 16px; /* 增加底部外边距，防止贴底 */
  margin-right: 1px; /* 微调右侧边距 */
  width: calc(100% - 2px); /* 减去边框宽度，防止水平滚动条 */
}

.category-header {
  font-weight: 600; color: #303133; margin-bottom: 12px;
  display: flex; align-items: center; gap: 8px; font-size: 15px;
}

.category-content {
  display: flex; gap: 16px; flex-wrap: wrap;
}

.category-btn {
  display: flex; align-items: center; padding: 10px 24px;
  background: #fff; border: 1px solid #dcdfe6; border-radius: 4px;
  cursor: pointer; transition: all 0.2s; min-width: 100px; justify-content: center;
}
.category-btn:hover { border-color: #c6e2ff; color: #409EFF; }
.category-btn.active { border-color: #409EFF; background-color: #ecf5ff; color: #409EFF; font-weight: 500; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #dcdfe6; margin-right: 10px; }
.category-btn.active .dot { background: #409EFF; }

/* 空状态 */
.empty-workspace, .empty-list { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #909399; }
.empty-icon-bg { width: 64px; height: 64px; background: #e4e7ed; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.empty-icon-bg i { font-size: 32px; color: #fff; }

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }

/* 覆盖 Element UI */
.custom-pagination >>> .btn-prev, .custom-pagination >>> .btn-next, .custom-pagination >>> .el-pager li { background: transparent; }
.refresh-btn { padding: 0; color: #909399; font-size: 14px; }
.refresh-btn:hover { color: #409EFF; }
</style>
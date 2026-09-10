<template>
  <div class="camera-watch-container">
    <!-- 头部控制栏 -->
    <div class="watch-header">
      <div class="header-left">
        <div class="title-wrap">
          <div class="title-icon">
            <i class="el-icon-camera"></i>
          </div>
          <div class="title-text-group">
            <div class="title-row">
              <h2 class="page-title">目录自动监听与智能识别</h2>
              <span class="status-badge" :class="running ? 'is-active' : 'is-stopped'">
                <span class="status-dot"></span>
                {{ running ? '实时监听中' : '监听已停止' }}
              </span>
            </div>
            <p class="page-desc">实时监听指定目录，捕获新图片后前端自动调用 AI 模型进行半轴缺陷检测与红框渲染</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button
          v-if="!running"
          type="primary"
          icon="el-icon-video-play"
          size="medium"
          class="action-btn btn-start"
          @click="startWatch"
        >
          启动目录监听
        </el-button>
        <el-button
          v-else
          type="danger"
          icon="el-icon-video-pause"
          size="medium"
          class="action-btn btn-stop"
          @click="stopWatch"
        >
          停止监听
        </el-button>
        <el-button
          icon="el-icon-delete"
          type="danger"
          plain
          size="medium"
          class="action-btn btn-clear"
          :disabled="imageList.length === 0"
          @click="handleClearImages"
        >
          清空列表
        </el-button>
      </div>
    </div>

    <!-- 监听配置与状态卡片 -->
    <div class="config-card">
      <div class="config-bar">
        <div class="config-item path-picker-box">
          <span class="config-label"><i class="el-icon-folder-opened"></i> 监听目录：</span>
          <el-input
            v-model="watchPath"
            size="small"
            class="path-input"
            placeholder="请输入要监听的本地绝对路径"
          ></el-input>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-check"
            class="apply-btn"
            @click="applyWatchPath"
          >
            应用/切换目录
          </el-button>
        </div>
        <div class="config-item stat-info">
          <span class="config-label"><i class="el-icon-picture"></i> 已捕获图片：</span>
          <span class="count-highlight">{{ imageList.length }}</span>
          <span class="count-unit">张</span>
        </div>
        <div class="config-item stat-info" v-if="aiDetecting">
          <el-tag type="warning" size="small"><i class="el-icon-loading"></i> AI正在分析中...</el-tag>
        </div>
      </div>
    </div>

    <!-- 核心展示区域：当前选中画面 & 捕获流水队列 -->
    <div class="content-body">
      <!-- 左侧：选中的单张大图展示卡片 -->
      <div class="latest-card-wrap">
        <div class="panel-card latest-card">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-picture-outline title-panel-icon"></i>
              <span>{{ isCurrentNewest ? '最新捕获画面' : '当前选中画面' }}</span>
              <span class="panel-badge-pill" v-if="currentDisplayItem && currentDisplayItem.annotatedBase64">已AI标注</span>
              <span class="panel-badge-pill" v-else>大图预览</span>
            </div>
            <div class="header-tools" v-if="currentDisplayItem">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-cpu"
                :loading="aiDetecting"
                @click="triggerAiDetect(currentDisplayItem)"
              >
                手动重检
              </el-button>
              <span class="latest-time"><i class="el-icon-time"></i> {{ currentDisplayItem.createTime }}</span>
              <el-button
                type="text"
                icon="el-icon-delete"
                class="del-btn-text"
                @click="handleDeleteImage(currentDisplayItem)"
              >
                删除此图
              </el-button>
            </div>
          </div>
          <div class="panel-content">
            <div class="latest-image-box" v-if="currentDisplayItem">
              <div class="image-preview-wrapper">
                <!-- 优先展示 AI 识别后的标注图 Base64，否则展示原图 URL -->
                <el-image
                  :src="currentDisplayItem.annotatedBase64 || formatImageUrl(currentDisplayItem.imgUrl)"
                  :preview-src-list="[currentDisplayItem.annotatedBase64 || formatImageUrl(currentDisplayItem.imgUrl)]"
                  fit="contain"
                  class="main-preview-img"
                >
                  <div slot="placeholder" class="image-slot">
                    <i class="el-icon-loading"></i> 图片加载中...
                  </div>
                </el-image>
                <div class="zoom-hint">
                  <i class="el-icon-zoom-in"></i> 点击查看高清大图
                </div>
              </div>

              <div class="image-meta-info">
                <div class="meta-row">
                  <span class="meta-label">文件名称</span>
                  <span class="meta-val file-name" :title="currentDisplayItem.fileName">{{ currentDisplayItem.fileName }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">AI 识别状态</span>
                  <span class="meta-val" :class="currentDisplayItem.isQualified ? 'status-ok' : 'status-warn'">
                    <i :class="currentDisplayItem.isQualified ? 'el-icon-circle-check' : 'el-icon-warning-outline'"></i>
                    {{ currentDisplayItem.aiStatus || '待识别' }}
                  </span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">置信度</span>
                  <span class="meta-val">{{ currentDisplayItem.confidence ? (currentDisplayItem.confidence * 100).toFixed(1) + '%' : '-' }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">抓拍时间</span>
                  <span class="meta-val">{{ currentDisplayItem.createTime }}</span>
                </div>
              </div>
            </div>
            <div class="empty-holder" v-else>
              <div class="empty-icon-wrap">
                <i class="el-icon-picture"></i>
              </div>
              <p class="empty-main-text">暂无捕获图片</p>
              <span class="sub-tip">请在监听目录中放入半轴图片即可实时呈现并自动识别</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：捕获图像流流水队列 -->
      <div class="stream-list-wrap">
        <div class="panel-card stream-card">
          <div class="panel-header">
            <div class="panel-title">
              <i class="el-icon-collection title-panel-icon"></i>
              <span>捕获流水队列</span>
              <span class="panel-badge-count">{{ imageList.length }}</span>
            </div>
            <el-button type="text" icon="el-icon-refresh" class="refresh-btn" size="mini" @click="fetchStatus">
              手动刷新
            </el-button>
          </div>
          <div class="panel-content">
            <div class="stream-grid" v-if="imageList.length > 0">
              <div
                v-for="(item, index) in imageList"
                :key="item.id || index"
                class="stream-item"
                :class="{
                  'is-newest': index === 0,
                  'is-selected': currentDisplayItem && currentDisplayItem.id === item.id
                }"
                @click="handleSelectItem(item)"
              >
                <div class="badge-tag" v-if="index === 0">最新</div>
                <div class="badge-annotated" v-if="item.annotatedBase64">已识别</div>
                <div class="item-actions">
                  <el-tooltip content="删除此图片" placement="top">
                    <span class="delete-icon" @click.stop="handleDeleteImage(item)">
                      <i class="el-icon-delete"></i>
                    </span>
                  </el-tooltip>
                </div>

                <div class="thumb-img-wrapper">
                  <img
                    :src="item.annotatedBase64 || formatImageUrl(item.imgUrl)"
                    class="thumb-img"
                    alt="半轴图片"
                    loading="lazy"
                  />
                  <div class="select-overlay" v-if="currentDisplayItem && currentDisplayItem.id === item.id">
                    <i class="el-icon-check check-icon"></i>
                  </div>
                </div>

                <div class="thumb-info">
                  <span class="thumb-name" :title="item.fileName">{{ item.fileName }}</span>
                  <span class="thumb-time">{{ item.createTime }}</span>
                </div>
              </div>
            </div>
            <div class="empty-holder" v-else>
              <div class="empty-icon-wrap">
                <i class="el-icon-picture-outline"></i>
              </div>
              <p class="empty-main-text">队列为空</p>
              <span class="sub-tip">等待抓拍图片推送...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

function toFullImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  return url.startsWith('/') ? url : '/' + url;
}

export default {
  name: 'CameraWatch',
  data() {
    return {
      running: false,
      watchPath: '/root/desc/cmzj-main/mijia-watcher/image',
      imageList: [],
      selectedItem: null,
      pollTimer: null,
      aiDetecting: false,
      aiCache: {} // 缓存每张图的 AI 识别结果，避免重复请求
    };
  },
  computed: {
    currentDisplayItem() {
      if (this.selectedItem) {
        const found = this.imageList.find(item => item.id === this.selectedItem.id);
        if (found) return found;
      }
      return this.imageList.length > 0 ? this.imageList[0] : null;
    },
    isCurrentNewest() {
      if (!this.currentDisplayItem || this.imageList.length === 0) return false;
      return this.currentDisplayItem.id === this.imageList[0].id;
    }
  },
  watch: {
    currentDisplayItem: {
      handler(newItem) {
        if (newItem && !newItem.annotatedBase64) {
          this.triggerAiDetect(newItem);
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.fetchStatus();
    this.pollTimer = setInterval(() => {
      if (this.running) {
        this.fetchStatus();
      }
    }, 1500);
  },
  beforeDestroy() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  },
  methods: {
    handleSelectItem(item) {
      this.selectedItem = item;
    },
    formatImageUrl(url) {
      return toFullImageUrl(url);
    },
    // 前端直接调用 AI 识别接口 http://192.168.1.3:9001/Qualified
    async triggerAiDetect(item) {
      if (!item || !item.imgUrl) return;
      if (this.aiCache[item.id]) {
        this.$set(item, 'annotatedBase64', this.aiCache[item.id].annotatedBase64);
        this.$set(item, 'aiStatus', this.aiCache[item.id].aiStatus);
        this.$set(item, 'confidence', this.aiCache[item.id].confidence);
        this.$set(item, 'isQualified', this.aiCache[item.id].isQualified);
        return;
      }

      this.aiDetecting = true;
      try {
        const fullUrl = this.formatImageUrl(item.imgUrl);
        // 1. 获取图片 Blob 对象
        const imageRes = await axios.get(fullUrl, { responseType: 'blob' });
        const blob = imageRes.data;

        // 2. 构造 FormData 发送给 AI 接口
        const formData = new FormData();
        formData.append('file', blob, item.fileName || 'image.jpg');

        const aiRes = await axios.post('http://192.168.1.3:9001/Qualified', formData, {
          timeout: 10000
        });

        const data = aiRes.data || {};
        let annotatedBase64 = data.image_base64 || data.image;
        if (annotatedBase64 && !annotatedBase64.startsWith('data:image')) {
          annotatedBase64 = 'data:image/jpeg;base64,' + annotatedBase64;
        }

        const isQualified = data.is_qualified !== undefined ? data.is_qualified : (data.status === '合格');
        const aiStatus = data.status || (isQualified ? '合格' : '不合格');
        const confidence = data.confidence || 0.95;

        const resultData = {
          annotatedBase64,
          aiStatus,
          confidence,
          isQualified
        };

        this.aiCache[item.id] = resultData;
        this.$set(item, 'annotatedBase64', annotatedBase64);
        this.$set(item, 'aiStatus', aiStatus);
        this.$set(item, 'confidence', confidence);
        this.$set(item, 'isQualified', isQualified);
      } catch (e) {
        console.error('前端请求 AI 视觉推理失败:', e);
        this.$set(item, 'aiStatus', '识别异常');
      } finally {
        this.aiDetecting = false;
      }
    },
    fetchStatus() {
      this.$request.get('/api/cameraWatch/status').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          const data = res.data || {};
          this.running = Boolean(data.running);
          if (data.watchDir) this.watchPath = data.watchDir;
          if (Array.isArray(data.list)) {
            const newList = data.list.map(item => {
              if (this.aiCache[item.id]) {
                return { ...item, ...this.aiCache[item.id] };
              }
              return item;
            });

            const isDifferent =
              newList.length !== this.imageList.length ||
              (newList.length > 0 && this.imageList.length > 0 && newList[0].id !== this.imageList[0].id);
            if (isDifferent) {
              this.imageList = newList;
            }
          }
        }
      }).catch(err => {
        console.warn('获取摄像头监听状态失败:', err);
      });
    },
    applyWatchPath() {
      if (!this.watchPath || !this.watchPath.trim()) {
        this.$message.warning('请输入有效的监听目录路径');
        return;
      }
      this.startWatch();
    },
    startWatch() {
      this.$request.post('/api/cameraWatch/start', { watchPath: this.watchPath }).then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          this.$message.success(res.msg || res.message || '目录监听启动成功！');
          this.running = true;
          this.fetchStatus();
        } else {
          this.$message.error(res.msg || res.message || '启动监听失败');
        }
      }).catch(err => {
        this.$message.error('请求后端启动监听失败');
      });
    },
    stopWatch() {
      this.$request.post('/api/cameraWatch/stop').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          this.$message.info('目录监听已停止');
          this.running = false;
        } else {
          this.$message.error('停止监听失败');
        }
      }).catch(err => {
        this.$message.error('停止监听失败');
      });
    },
    handleDeleteImage(item) {
      if (!item || !item.id) return;
      this.$confirm(
        `确定要删除图片【${item.fileName}】吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.$request.post('/api/cameraWatch/delete', {
          id: item.id,
          deleteSourceFile: true
        }).then(res => {
          if (res.code === 200 || res.code === 1 || res.code === '200') {
            this.$message.success(res.msg || '删除成功');
            if (this.selectedItem && this.selectedItem.id === item.id) {
              this.selectedItem = null;
            }
            delete this.aiCache[item.id];
            this.fetchStatus();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        }).catch(err => {
          this.$message.error('删除请求失败');
        });
      }).catch(() => {});
    },
    handleClearImages() {
      this.$confirm(
        '确定清空全部已捕获的图片吗？',
        '清空确认',
        {
          confirmButtonText: '全部删除',
          cancelButtonText: '取消',
          type: 'danger'
        }
      ).then(() => {
        this.$request.post('/api/cameraWatch/clear', {
          deletePhysical: true
        }).then(res => {
          if (res.code === 200 || res.code === 1 || res.code === '200') {
            this.$message.success(res.msg || '已清空捕获列表');
            this.imageList = [];
            this.selectedItem = null;
            this.aiCache = {};
          } else {
            this.$message.error(res.msg || '清空失败');
          }
        }).catch(err => {
          this.$message.error('清空请求失败');
        });
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.camera-watch-container {
  padding: 20px 24px;
  background: #f0f4f8;
  height: calc(100vh - 84px);
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* 顶部控制栏 */
.watch-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  padding: 18px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.3);
}

.title-text-group {
  display: flex;
  flex-direction: column;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 19px;
  font-weight: 700;
  margin: 0;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
}

.status-badge.is-active {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.status-badge.is-stopped {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #cbd5e1;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}

.status-badge.is-active .status-dot {
  box-shadow: 0 0 0 0 rgba(5, 150, 105, 0.6);
  animation: pulse-dot 1.6s infinite;
}

@keyframes pulse-dot {
  0% { box-shadow: 0 0 0 0 rgba(5, 150, 105, 0.6); }
  70% { box-shadow: 0 0 0 6px rgba(5, 150, 105, 0); }
  100% { box-shadow: 0 0 0 0 rgba(5, 150, 105, 0); }
}

.page-desc {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-btn {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s;
}

/* 监听配置卡片 */
.config-card {
  flex-shrink: 0;
  background: #ffffff;
  padding: 14px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
}

.config-bar {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
}

.config-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #334155;
}

.config-label {
  font-weight: 600;
  color: #475569;
  margin-right: 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.path-input {
  width: 380px;
}

.path-input >>> .el-input__inner {
  border-radius: 6px;
  border-color: #cbd5e1;
}

.apply-btn {
  margin-left: 8px;
  border-radius: 6px;
}

.count-highlight {
  font-size: 20px;
  font-weight: 800;
  color: #1890ff;
  margin: 0 4px;
}

.count-unit {
  color: #64748b;
  font-size: 13px;
}

/* 核心布局 */
.content-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 18px;
}

.latest-card-wrap,
.stream-list-wrap {
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-card {
  height: 100%;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #fafbfc;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.title-panel-icon {
  font-size: 16px;
  color: #1890ff;
}

.panel-badge-pill {
  font-size: 11px;
  background: #eff6ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
  border: 1px solid #bfdbfe;
}

.panel-badge-count {
  font-size: 12px;
  background: #e2e8f0;
  color: #334155;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 700;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.latest-time {
  font-size: 12px;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.del-btn-text {
  color: #ef4444;
  padding: 0;
  font-size: 13px;
  font-weight: 600;
}
.del-btn-text:hover {
  color: #dc2626;
}

.refresh-btn {
  color: #1890ff;
  font-weight: 600;
  padding: 0;
}

.panel-content {
  flex: 1;
  min-height: 0;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
}

/* 左侧大图展示区 */
.latest-image-box {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.image-preview-wrapper {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 380px;
  background: #0f172a;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.4);
}

.main-preview-img {
  width: 100%;
  height: 100%;
  cursor: zoom-in;
}

.zoom-hint {
  position: absolute;
  bottom: 10px;
  right: 12px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(4px);
  color: #f8fafc;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 6px;
  pointer-events: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.image-meta-info {
  flex-shrink: 0;
  background: #f8fafc;
  padding: 14px 18px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 20px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.meta-label {
  color: #64748b;
  font-weight: 500;
}

.meta-val {
  color: #0f172a;
  font-weight: 600;
}

.meta-val.file-name {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-val.status-ok {
  color: #059669;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-val.status-warn {
  color: #d97706;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 右侧流水队列网格 */
.stream-grid {
  flex: 1;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  align-content: start;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
}

.stream-grid::-webkit-scrollbar {
  width: 5px;
}
.stream-grid::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.stream-item {
  position: relative;
  background: #ffffff;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  user-select: none;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.stream-item:hover {
  border-color: #93c5fd;
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.08);
}

.stream-item.is-selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.2), 0 6px 14px rgba(24, 144, 255, 0.15);
}

.stream-item:hover .item-actions {
  opacity: 1;
}

.badge-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  z-index: 2;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.badge-annotated {
  position: absolute;
  bottom: 40px;
  left: 6px;
  z-index: 2;
  background: rgba(5, 150, 105, 0.85);
  backdrop-filter: blur(2px);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 1px 5px;
  border-radius: 4px;
}

.item-actions {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.2s;
}

.delete-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  color: #fff;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.15s;
}

.delete-icon:hover {
  background: #ef4444;
}

.thumb-img-wrapper {
  position: relative;
  width: 100%;
  height: 105px;
  background: #0f172a;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}

.stream-item:hover .thumb-img {
  transform: scale(1.04);
}

.select-overlay {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #1890ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.thumb-info {
  padding: 8px 10px;
  background: #ffffff;
}

.thumb-name {
  display: block;
  font-size: 11px;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.thumb-time {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 2px;
  display: block;
}

/* 空状态 */
.empty-holder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 70px 20px;
  color: #94a3b8;
}

.empty-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #94a3b8;
  margin-bottom: 14px;
}

.empty-main-text {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  margin: 0;
}

.sub-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  text-align: center;
  max-width: 320px;
}
</style>

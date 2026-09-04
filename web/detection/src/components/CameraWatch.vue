<template>
  <div class="camera-watch-container">
    <!-- 头部控制栏 -->
    <div class="watch-header">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-video-camera"></i></span>
          <h2 class="page-title">小米摄像头自动监听与媒体流</h2>
          <el-tag size="small" :type="running ? 'success' : 'info'" effect="dark">
            <i :class="running ? 'el-icon-loading' : 'el-icon-video-pause'"></i>
            {{ running ? '实时监听中' : '监听已停止' }}
          </el-tag>
        </div>
        <p class="page-desc">实时监听指定本地目录，当摄像头产生新半轴图像或视频后，后端自动捕获并无感推送到本界面</p>
      </div>
      <div class="header-right">
        <el-button
          v-if="!running"
          type="primary"
          icon="el-icon-video-play"
          size="medium"
          @click="startWatch"
        >
          启动目录监听
        </el-button>
        <el-button
          v-else
          type="danger"
          icon="el-icon-video-pause"
          size="medium"
          @click="stopWatch"
        >
          停止监听
        </el-button>
        <el-button
          icon="el-icon-delete"
          type="danger"
          plain
          size="medium"
          :disabled="imageList.length === 0"
          @click="handleClearImages"
        >
          清空列表
        </el-button>
      </div>
    </div>

    <!-- 监听配置与状态卡片 -->
    <el-card shadow="never" class="config-card">
      <div class="config-bar">
        <div class="config-item path-picker-box">
          <span class="label">当前监听目录：</span>
          <el-input
            v-model="watchPath"
            size="small"
            style="width: 380px;"
            placeholder="请输入要监听的本地绝对路径"
          ></el-input>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-check"
            style="margin-left: 8px;"
            @click="applyWatchPath"
          >
            应用/切换目录
          </el-button>
        </div>
        <div class="config-item stat-info">
          <span class="label">已捕获媒体数：</span>
          <span class="count-highlight">{{ imageList.length }}</span> 个
        </div>
        <div class="config-item tip-info" v-if="running">
          <span class="pulse-dot"></span>
          <span class="tip-text">正在守护目录，出现新图片或视频将毫秒级推送显示...</span>
        </div>
      </div>
    </el-card>

    <!-- 核心展示区域：当前选中/最新画面 & 捕获流水队列 -->
    <div class="content-body">
      <!-- 左侧：选中的单张大图/视频展示卡片 -->
      <div class="latest-card-wrap">
        <el-card shadow="hover" class="latest-card">
          <div slot="header" class="card-title-bar">
            <span>
              <i :class="isVideoItem(currentDisplayItem) ? 'el-icon-video-camera' : 'el-icon-picture-outline'"></i>
              {{ isCurrentNewest ? '最新捕获画面' : '当前选中画面' }}
              <el-tag size="mini" :type="isVideoItem(currentDisplayItem) ? 'warning' : 'primary'" style="margin-left: 8px;">
                {{ isVideoItem(currentDisplayItem) ? '视频文件' : '图片文件' }}
              </el-tag>
            </span>
            <div class="header-tools" v-if="currentDisplayItem">
              <span class="latest-time">{{ currentDisplayItem.createTime }}</span>
              <el-button
                type="text"
                icon="el-icon-delete"
                class="del-btn-text"
                @click="handleDeleteImage(currentDisplayItem)"
              >
                删除此文件
              </el-button>
            </div>
          </div>
          <div class="latest-image-box" v-if="currentDisplayItem">
            <!-- 视频播放区 -->
            <div class="video-container" v-if="isVideoItem(currentDisplayItem)">
              <video
                ref="mainVideo"
                :src="formatImageUrl(currentDisplayItem.fileUrl || currentDisplayItem.imgUrl)"
                controls
                playsinline
                preload="auto"
                class="main-preview-video"
              ></video>
            </div>
            <!-- 图片大图展示（点击左侧大图才弹全屏预览） -->
            <el-image
              v-else
              :src="formatImageUrl(currentDisplayItem.imgUrl)"
              :preview-src-list="[formatImageUrl(currentDisplayItem.imgUrl)]"
              fit="contain"
              class="main-preview-img"
            >
              <div slot="placeholder" class="image-slot">
                <i class="el-icon-loading"></i> 加载中...
              </div>
            </el-image>

            <div class="image-meta-info">
              <div class="meta-row">
                <span class="meta-label">文件名称：</span>
                <span class="meta-val">{{ currentDisplayItem.fileName }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">文件大小：</span>
                <span class="meta-val">{{ currentDisplayItem.fileSize }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">捕获时间：</span>
                <span class="meta-val">{{ currentDisplayItem.createTime }}</span>
              </div>
            </div>
          </div>
          <div class="empty-holder" v-else>
            <i class="el-icon-camera empty-icon"></i>
            <p>暂无捕获媒体</p>
            <span class="sub-tip">请在小米摄像头目录「{{ watchPath }}」中放入图片或视频即可在此呈现</span>
          </div>
        </el-card>
      </div>

      <!-- 右侧：捕获图像/视频流流水队列 -->
      <div class="stream-list-wrap">
        <el-card shadow="never" class="stream-card">
          <div slot="header" class="card-title-bar">
            <span><i class="el-icon-collection"></i> 捕获流水队列 ({{ imageList.length }})</span>
            <el-button type="text" icon="el-icon-refresh" size="mini" @click="fetchStatus">手动刷新</el-button>
          </div>
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
              <div class="media-type-tag" v-if="isVideoItem(item)">
                <i class="el-icon-video-play"></i> 视频
              </div>
              <div class="item-actions">
                <el-tooltip content="删除此项" placement="top">
                  <span class="delete-icon" @click.stop="handleDeleteImage(item)">
                    <i class="el-icon-delete"></i>
                  </span>
                </el-tooltip>
              </div>

              <!-- 视频项缩略图/占位 -->
              <div class="thumb-video-box" v-if="isVideoItem(item)">
                <video
                  :src="formatImageUrl(item.fileUrl || item.imgUrl)"
                  class="thumb-video"
                  preload="metadata"
                  muted
                ></video>
                <div class="video-play-overlay">
                  <i class="el-icon-video-play play-icon"></i>
                </div>
              </div>

              <!-- 图片项缩略图（点击仅选中，不配置 preview-src-list） -->
              <img
                v-else
                :src="formatImageUrl(item.imgUrl)"
                class="thumb-img"
                alt="thumb"
              />

              <div class="thumb-info">
                <span class="thumb-name" :title="item.fileName">{{ item.fileName }}</span>
                <span class="thumb-time">{{ item.createTime }}</span>
              </div>
            </div>
          </div>
          <div class="empty-holder" v-else>
            <i class="el-icon-picture-outline empty-icon"></i>
            <p>队列为空</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
function toFullImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  const cleanUrl = url.startsWith('/') ? url : '/' + url;
  return 'http://localhost:8081' + cleanUrl;
}

export default {
  name: 'CameraWatch',
  data() {
    return {
      running: false,
      watchPath: 'D:\\QQ\\小米摄像头图片存储',
      imageList: [],
      selectedItem: null,
      pollTimer: null
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
  mounted() {
    this.fetchStatus();
    this.pollTimer = setInterval(() => {
      if (this.running) {
        this.fetchStatus();
      }
    }, 1000);
  },
  beforeDestroy() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  },
  methods: {
    isVideoItem(item) {
      if (!item) return false;
      if (item.fileType === 'video') return true;
      const name = (item.fileName || item.imgUrl || '').toLowerCase();
      return (
        name.endsWith('.mp4') ||
        name.endsWith('.avi') ||
        name.endsWith('.mov') ||
        name.endsWith('.mkv') ||
        name.endsWith('.flv') ||
        name.endsWith('.webm') ||
        name.endsWith('.wmv')
      );
    },
    handleSelectItem(item) {
      this.selectedItem = item;
    },
    formatImageUrl(url) {
      if (!url) return '';
      // 如果是视频，使用专用的 HTTP 206 流式分片接口，确保任何浏览器均可无缝缓冲与拖动进度条
      const fileName = url.substring(url.lastIndexOf('/') + 1);
      const lower = fileName.toLowerCase();
      if (
        lower.endsWith('.mp4') ||
        lower.endsWith('.avi') ||
        lower.endsWith('.mov') ||
        lower.endsWith('.mkv') ||
        lower.endsWith('.flv') ||
        lower.endsWith('.webm') ||
        lower.endsWith('.wmv')
      ) {
        return 'http://localhost:8081/detectInfo/cameraWatch/mediaStream?fileName=' + encodeURIComponent(fileName);
      }
      return toFullImageUrl(url);
    },
    fetchStatus() {
      this.$request.get('/api/detectInfo/cameraWatch/status').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          const data = res.data || {};
          this.running = Boolean(data.running);
          if (data.watchDir) this.watchPath = data.watchDir;
          if (Array.isArray(data.list)) {
            // 增量比对更新，避免列表引用频繁变更导致视频组件重新挂载被打断
            const newList = data.list;
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
      this.$request.post('/api/detectInfo/cameraWatch/start', { watchPath: this.watchPath }).then(res => {
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
      this.$request.post('/api/detectInfo/cameraWatch/stop').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          this.$message.info('目录监听已停止');
          this.running = false;
        }
      }).catch(err => {
        this.$message.error('停止监听失败');
      });
    },
    handleDeleteImage(item) {
      if (!item || !item.id) return;
      const isVid = this.isVideoItem(item);
      this.$confirm(
        `确定要删除${isVid ? '视频' : '图片'}【${item.fileName}】吗？<br><small style="color:#e6a23c;">提示：将同时删除系统上传缓存及本地硬盘文件</small>`,
        '删除确认',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.$request.post('/api/detectInfo/cameraWatch/delete', {
          id: item.id,
          deleteSourceFile: true
        }).then(res => {
          if (res.code === 200 || res.code === 1 || res.code === '200') {
            this.$message.success(res.msg || '删除成功');
            if (this.selectedItem && this.selectedItem.id === item.id) {
              this.selectedItem = null;
            }
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
        '确定清空全部已捕获的媒体文件吗？<br><small style="color:#f56c6c;">将同时清理系统缓存及本地已捕获物理文件！</small>',
        '清空确认',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '全部删除',
          cancelButtonText: '取消',
          type: 'danger'
        }
      ).then(() => {
        this.$request.post('/api/detectInfo/cameraWatch/clear', {
          deletePhysical: true
        }).then(res => {
          if (res.code === 200 || res.code === 1 || res.code === '200') {
            this.$message.success(res.msg || '已清空捕获列表');
            this.imageList = [];
            this.selectedItem = null;
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
  padding: 18px 22px;
  background: #f4f6f9;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

.watch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  margin-bottom: 14px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #e6f7ff;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #1e293b;
}

.page-desc {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0 0;
}

.config-card {
  margin-bottom: 14px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.config-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.config-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #334155;
}

.config-item .label {
  font-weight: 500;
  margin-right: 6px;
}

.count-highlight {
  font-size: 18px;
  font-weight: 700;
  color: #1890ff;
  margin: 0 4px;
}

.tip-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #52c41a;
  font-size: 12px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
  box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.6);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.6);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(82, 196, 26, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0);
  }
}

.content-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.latest-card-wrap, .stream-list-wrap {
  min-width: 0;
}

.card-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.header-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.del-btn-text {
  color: #f56c6c;
  padding: 0;
  font-size: 13px;
}
.del-btn-text:hover {
  color: #d9363e;
}

.latest-time {
  font-size: 12px;
  color: #64748b;
  font-weight: normal;
}

.latest-image-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.video-container {
  width: 100%;
  height: 380px;
  background: #0f172a;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-preview-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  outline: none;
}

.main-preview-img {
  width: 100%;
  height: 380px;
  background: #0f172a;
  border-radius: 6px;
  overflow: hidden;
  cursor: zoom-in;
}

.image-meta-info {
  width: 100%;
  margin-top: 14px;
  background: #f8fafc;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

.meta-row:last-child {
  margin-bottom: 0;
}

.meta-label {
  color: #64748b;
}

.meta-val {
  color: #1e293b;
  font-weight: 500;
}

.stream-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 12px;
  max-height: 480px;
  overflow-y: auto;
  padding-right: 4px;
}

.stream-item {
  position: relative;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.2s;
  cursor: pointer;
  user-select: none;
}

.stream-item:hover {
  border-color: #60a5fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.stream-item.is-selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.stream-item:hover .item-actions {
  opacity: 1;
}

.badge-tag {
  position: absolute;
  top: 4px;
  left: 4px;
  z-index: 2;
  background: #1890ff;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
}

.media-type-tag {
  position: absolute;
  top: 4px;
  left: 4px;
  z-index: 2;
  background: #e6a23c;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
}

.badge-tag + .media-type-tag {
  left: 38px;
}

.item-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.2s;
}

.delete-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.delete-icon:hover {
  background: #f56c6c;
}

.thumb-img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  background: #1e293b;
  display: block;
}

.thumb-video-box {
  width: 100%;
  height: 100px;
  background: #0f172a;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.thumb-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  font-size: 28px;
  color: rgba(255,255,255,0.9);
}

.thumb-info {
  padding: 6px 8px;
}

.thumb-name {
  display: block;
  font-size: 11px;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.thumb-time {
  font-size: 10px;
  color: #94a3b8;
}

.empty-holder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #94a3b8;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #cbd5e1;
}

.sub-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
}
</style>

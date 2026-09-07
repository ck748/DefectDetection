<template>
  <div class="home-container">
    <el-container class="app-wrapper">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
        <!-- 侧边栏背景图独立滤镜层：仅将饱和度精准调节为 66%，不压暗亮度与色温 -->
        <div class="sidebar-bg-layer"></div>

        <!-- Logo 区域 -->
        <div class="logo-container" :class="{ 'collapse': isCollapse }">
          <transition name="sidebarLogoFade">
            <router-link v-if="isCollapse" key="collapse" class="sidebar-logo-link" to="/">
              <img src="../assets/logo.png" class="sidebar-logo">
            </router-link>
            <router-link v-else key="expand" class="sidebar-logo-link" to="/">
              <img src="../assets/logo.png" class="sidebar-logo">
              <h1 class="sidebar-title">{{ title }}</h1>
            </router-link>
          </transition>
        </div>

        <!-- 菜单区域 -->
        <el-scrollbar wrap-class="scrollbar-wrapper">
          <el-menu :collapse="isCollapse" :unique-opened="false" :collapse-transition="false" mode="vertical" router
            :default-active="$route.path" background-color="transparent" text-color="rgba(255, 255, 255, 0.75)"
            active-text-color="#ffffff" class="el-menu-vertical el-menu-vertical-demo"
            @open="handleOpen" @close="handleClose">
            <!-- 预警信息 -->
            <el-menu-item index="/warning">
              <i class="el-icon-warning-outline"></i>
              <span slot="title">预警信息</span>
            </el-menu-item>

            <!-- 设备管理 -->
            <el-menu-item index="/device-manager">
              <i class="el-icon-cpu"></i>
              <span slot="title">设备管理</span>
            </el-menu-item>

            <!-- 车间检测 -->
            <el-menu-item index="/agv">
              <i class="el-icon-truck"></i>
              <span slot="title">车间检测</span>
            </el-menu-item>

            <!-- 监测信息 -->
            <el-submenu index="info">
              <template slot="title">
                <i class="el-icon-data-line"></i>
                <span slot="title">检测系统</span>
              </template>
              <el-menu-item index="/dashboard">
                <i class="el-icon-monitor"></i>
                <span slot="title">实时检测</span>
              </el-menu-item>
              <el-menu-item index="/info">
                <i class="el-icon-tickets"></i>
                <span slot="title">历史检测</span>
              </el-menu-item>
            </el-submenu>

            <!-- 【国赛2.0】AGV运检一体化升级系统 -->
            <el-submenu index="national-2026">
              <template slot="title">
                <i class="el-icon-medal"></i>
                <span slot="title">智能管家</span>
              </template>
              <el-menu-item index="/camera-watch">
                <i class="el-icon-video-camera"></i>
                <span slot="title">摄像头目录监听</span>
              </el-menu-item>
              <el-menu-item index="/feature-checklist">
                <i class="el-icon-circle-check"></i>
                <span slot="title">功能核对</span>
              </el-menu-item>
              <el-menu-item index="/six-s-manager">
                <i class="el-icon-s-custom"></i>
                <span slot="title">6S管家</span>
              </el-menu-item>
            </el-submenu>

            <!-- 数据标注子菜单 -->
            <el-submenu index="annotation-manager">
              <template slot="title">
                <i class="el-icon-edit-outline"></i>
                <span slot="title">标注系统</span>
              </template>
              <el-menu-item index="/annotation">
                <i class="el-icon-edit"></i>
                <span slot="title">数据标注</span>
              </el-menu-item>
              <el-menu-item index="/history-annotation">
                <i class="el-icon-time"></i>
                <span slot="title">历史标注</span>
              </el-menu-item>
            </el-submenu>

            <!-- 系统管理 -->
            <el-submenu index="sysmanager">
              <template slot="title">
                <i class="el-icon-setting"></i>
                <span slot="title">系统管理</span>
              </template>
              <el-menu-item index="/log">
                <i class="el-icon-notebook-2"></i>
                <span slot="title">日志管理</span>
              </el-menu-item>
              <el-menu-item index="/apimanager">
                <i class="el-icon-key"></i>
                <span slot="title">API管理</span>
              </el-menu-item>
              <el-menu-item index="/pwdmanager">
                <i class="el-icon-lock"></i>
                <span slot="title">人员管理</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container class="main-container" :class="{ 'is-collapse': isCollapse }">
        <!-- 头部 (高 64px 清爽极简现代工业风，白底无背景图) -->
        <el-header class="navbar" height="64px">
          <div class="navbar-left">
            <!-- 顶部折叠按钮 -->
            <div class="hamburger-container" @click="handleCollapse">
              <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
            </div>

            <!-- 固定的工业标题与标语区域 -->
            <div class="header-title-zone">
              <div class="header-main-title">工业设备智能管控中心</div>
              <div class="header-sub-slogan">全生命周期监测 · AGV 智能运维 · 数据驱动高效生产</div>
            </div>
          </div>

          <div class="navbar-right">
            <!-- 搜索框 -->
            <div class="header-search-box">
              <i class="el-icon-search search-icon"></i>
              <input
                type="text"
                v-model="navSearchText"
                placeholder="搜索设备名称 / SN / IP 地址..."
                class="search-input"
              />
            </div>

            <!-- 实时时间 -->
            <div class="header-time-display">
              <i class="el-icon-date time-cal-icon"></i>
              <span>{{ currentTimeStr }}</span>
            </div>

            <!-- 报警消息铃铛 -->
            <div class="header-notice-btn" @click="$router.push('/warning')">
              <el-badge :value="3" class="notice-badge">
                <i class="el-icon-bell notice-icon"></i>
              </el-badge>
            </div>

            <!-- 帮助图标 -->
            <div class="header-help-btn" title="系统帮助与操作指引">
              <i class="el-icon-question help-icon"></i>
            </div>

            <!-- 分割线 -->
            <div class="header-divider"></div>

            <!-- 用户信息下拉菜单 -->
            <el-dropdown class="avatar-container" trigger="click">
              <div class="avatar-wrapper">
                <div class="user-avatar-circle">
                  <i class="el-icon-user-solid"></i>
                </div>
                <span class="user-name">{{ user.name || 'admin1' }}</span>
                <i class="el-icon-caret-bottom user-caret" />
              </div>
              <el-dropdown-menu slot="dropdown" class="user-dropdown">
                <router-link to="/person">
                  <el-dropdown-item>
                    <i class="el-icon-user"></i> 个人中心
                  </el-dropdown-item>
                </router-link>
                <router-link to="/password">
                  <el-dropdown-item>
                    <i class="el-icon-lock"></i> 修改密码
                  </el-dropdown-item>
                </router-link>
                <el-dropdown-item divided @click.native="logout">
                  <span style="display:block;"><i class="el-icon-switch-button"></i> 退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="app-main" :class="{ 'is-fullscreen-view': $route.meta && $route.meta.isFullLayout }">
          <transition name="fade-transform" mode="out-in">
            <div class="main-content-view" :class="{ 'flush-view': $route.meta && $route.meta.isFullLayout }">
              <keep-alive>
                <router-view @update:user="updateUser" :key="$route.meta && $route.meta.noCache ? $route.fullPath : undefined" />
              </keep-alive>
            </div>
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      isCollapse: false,
      user: JSON.parse(localStorage.getItem('useradmin') || '{}'),
      title: '智检控系统',
      defaultAvatar: require('../assets/头像.jpg'),
      navSearchText: '',
      currentTimeStr: '',
      timer: null
    }
  },
  mounted() {
    this.title = '灵眸巡诊';
    if (this.$route.path === '/') {
      this.$router.replace('/daping');
    }
    this.updateCurrentTime();
    this.timer = setInterval(this.updateCurrentTime, 1000);
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  },
  methods: {
    updateCurrentTime() {
      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, '0');
      const d = String(now.getDate()).padStart(2, '0');
      const hh = String(now.getHours()).padStart(2, '0');
      const mm = String(now.getMinutes()).padStart(2, '0');
      const ss = String(now.getSeconds()).padStart(2, '0');
      this.currentTimeStr = `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
    },
    updateUser(user) {
      this.user = JSON.parse(JSON.stringify(user));
    },
    logout() {
      this.$confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('useradmin');
        this.$router.push('/login');
        this.$message.success('退出成功');
      });
    },
    handleFull() {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen();
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
      }
    },
    handleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    }
  }
}
</script>

<style scoped>
/* ================= 全局容器 ================= */
.home-container {
  height: 100vh;
  width: 100vw;
  background-color: #ffffff;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  box-sizing: border-box;
}

.app-wrapper {
  height: 100%;
  width: 100%;
  display: flex !important;
  flex-direction: row !important;
  overflow: hidden;
  position: relative;
}

/* ================= 侧边栏 (Sidebar) ================= */
.sidebar-container {
  /* 调整色温至 11200K（高冷蓝调科技色温：hue-rotate(-12deg)），仅展示右侧区域，隐藏机械臂 */
  background: #001529 url('../assets/1.png') no-repeat right bottom;
  background-size: auto 135%;
  filter: hue-rotate(-12deg);
  height: 100%;
  font-size: 0px;
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: none !important;
  border-right: none !important;
  outline: none;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative !important;
  z-index: 10;
}

/* Logo 区域 */
.logo-container {
  position: relative;
  width: 100%;
  height: 96px;
  line-height: 96px;
  background: transparent;
  text-align: center;
  overflow: hidden;
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.sidebar-logo-link {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  padding: 0 16px;
  box-sizing: border-box;
}

.sidebar-logo {
  width: 44px;
  height: 44px;
  vertical-align: middle;
  margin-right: 14px;
  object-fit: contain;
  transition: all 0.3s;
  flex-shrink: 0;
}

.logo-container.collapse .sidebar-logo-link {
  width: 64px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-container.collapse .sidebar-logo {
  margin: 0 !important;
  width: 38px;
  height: 38px;
  display: block;
}

.sidebar-title {
  display: inline-block;
  margin: 0;
  color: #fff;
  font-weight: 700;
  font-size: 22px;
  letter-spacing: 1.5px;
  line-height: 96px;
  vertical-align: middle;
  white-space: nowrap;
  text-shadow: 0 0 12px rgba(64, 169, 255, 0.7);
}

/* 滚动条区域 */
.el-scrollbar {
  flex: 1;
}

::v-deep .el-scrollbar__wrap {
  overflow-x: hidden !important;
  overflow-y: auto !important;
  margin-right: -17px !important;
}

::v-deep .el-scrollbar__wrap::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
}

/* 菜单整体样式 */
::v-deep .el-menu {
  border: none !important;
  background-color: transparent !important;
  width: 100% !important;
}

/* 菜单项基础样式 */
::v-deep .el-menu-item,
::v-deep .el-submenu__title {
  height: 50px;
  line-height: 50px;
  margin: 0 !important;
  border-radius: 0 !important;
  width: 100% !important;
  box-sizing: border-box !important;
  transition: all 0.3s;
}

/* 折叠状态下菜单项与图标水平居中对齐 */
::v-deep .el-menu--collapse {
  width: 64px !important;
}

::v-deep .el-menu--collapse .el-menu-item,
::v-deep .el-menu--collapse .el-submenu__title {
  margin: 4px auto !important;
  padding: 0 !important;
  width: 48px !important;
  text-align: center !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

::v-deep .el-menu--collapse .el-menu-item i,
::v-deep .el-menu--collapse .el-submenu__title i {
  margin: 0 !important;
  padding: 0 !important;
  font-size: 18px !important;
  width: auto !important;
  text-align: center !important;
  display: inline-block !important;
}

::v-deep .el-menu--collapse .el-submenu__title .el-submenu__icon-arrow {
  display: none !important;
}

/* 图标与文字间距 */
::v-deep .el-menu-item i,
::v-deep .el-submenu__title i {
  margin-right: 10px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.65);
}

/* 鼠标悬停 */
::v-deep .el-menu-item:hover,
::v-deep .el-submenu__title:hover {
  background-color: rgba(255, 255, 255, 0.05) !important;
  color: #fff !important;
}

::v-deep .el-menu-item:hover i,
::v-deep .el-submenu__title:hover i {
  color: #fff !important;
}

/* 选中状态 (Active) - 蓝色圆角矩形 */
::v-deep .el-menu-item.is-active {
  background-color: #1890ff !important;
  color: #fff !important;
}

::v-deep .el-menu-item.is-active i {
  color: #fff !important;
}

/* 子菜单样式 */
::v-deep .el-submenu .el-menu-item {
  background-color: #000c17 !important;
}

::v-deep .el-submenu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

::v-deep .el-submenu .el-menu-item.is-active {
  background-color: #1890ff !important;
  color: #fff !important;
}

/* ================= 主内容布局 (Main Layout) ================= */
.main-container {
  height: 100vh;
  max-height: 100vh;
  flex: 1;
  min-width: 0;
  margin-left: 0 !important;
  background-color: #07152b;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.main-container.is-collapse {
  margin-left: 0 !important;
}

/* ================= 头部导航 (Navbar) - 权威管控中心大顶栏 ================= */
.navbar {
  height: 78px !important;
  width: 100%;
  background-color: #ffffff !important;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.03);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 26px;
  z-index: 99;
  position: relative;
  top: auto;
  border-bottom: 1px solid #f1f5f9;
  overflow: visible;
  flex-shrink: 0;
  box-sizing: border-box;
  min-width: max-content;
}

.navbar-left {
  display: flex;
  align-items: center;
  height: 100%;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
  white-space: nowrap;
}

/* 汉堡折叠按钮 - 适配大顶栏 */
.hamburger-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  width: 40px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  margin-right: 18px;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.hamburger-container:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.hamburger-container i {
  font-size: 20px;
  color: #334155;
}

.hamburger-container:hover i {
  color: #0284c7;
}

/* 标题与标语区域 - 大字号权威排版 */
.header-title-zone {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-shrink: 0;
  white-space: nowrap;
}

.header-main-title {
  color: #0f172a;
  font-weight: 800;
  font-size: 22px;
  letter-spacing: -0.3px;
  line-height: 1.25;
  white-space: nowrap;
  flex-shrink: 0;
}

.header-sub-slogan {
  font-size: 13.5px;
  color: #64748b;
  font-weight: 500;
  margin-top: 5px;
  letter-spacing: 0.5px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* 右侧区域容器 */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  white-space: nowrap;
}

/* 胶囊搜索框 - 宽适大气 */
.header-search-box {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 8px;
  padding: 0 14px;
  height: 38px;
  width: 260px;
  border: 1px solid #d1d5db;
  transition: all 0.2s ease;
}

.header-search-box:hover {
  border-color: #9ca3af;
}

.header-search-box:focus-within {
  background: #ffffff;
  border-color: #9ca3af;
  box-shadow: 0 0 0 2px rgba(156, 163, 175, 0.15);
  width: 280px;
}

.search-icon {
  font-size: 15px;
  color: #9ca3af;
  margin-right: 8px;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: #6b7280;
  width: 100%;
}

.search-input::placeholder {
  color: #9ca3af;
  font-size: 13px;
}

/* 实时时间显示 */
.header-time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  font-weight: 500;
  color: #6b7280;
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
}

.time-cal-icon {
  font-size: 16px;
  color: #9ca3af;
}

/* 单铃铛通知按钮 */
.header-notice-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  transition: all 0.2s;
}

.header-notice-btn:hover {
  background: #f1f5f9;
}

.notice-icon {
  font-size: 20px;
  color: #475569;
  transition: color 0.2s;
}

.header-notice-btn:hover .notice-icon {
  color: #0284c7;
}

::v-deep .notice-badge .el-badge__content {
  background-color: #ef4444;
  border: 1.5px solid #ffffff;
  font-weight: 700;
  font-size: 11px;
  height: 16px;
  line-height: 14px;
  padding: 0 5px;
  top: -2px;
  right: 2px;
}

/* 帮助问号按钮 */
.header-help-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  transition: all 0.2s;
}

.header-help-btn:hover {
  background: #f1f5f9;
}

.help-icon {
  font-size: 20px;
  color: #475569;
}

.header-help-btn:hover .help-icon {
  color: #0284c7;
}

/* 分割线 */
.header-divider {
  width: 1px;
  height: 22px;
  background: #e2e8f0;
  margin: 0 4px;
}

/* 用户信息区 */
.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 8px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.avatar-wrapper:hover {
  background: #f1f5f9;
}

.user-avatar-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 16px;
}

.user-name {
  margin: 0 8px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  letter-spacing: 0.2px;
}

.user-caret {
  color: #94a3b8;
  font-size: 13px;
}

/* ================= 内容区 (Content) - 零间隙无缝全平铺 ================= */
.app-main {
  padding: 0 !important;
  width: 100%;
  position: relative;
  overflow: hidden !important;
  box-sizing: border-box;
  flex: 1; /* 自动撑满剩余视口高度 */
  display: flex;
  flex-direction: column;
  height: calc(100vh - 78px);
  max-height: calc(100vh - 78px);
  background-color: #ffffff;
}

.app-main.is-fullscreen-view {
  padding: 0 !important;
  overflow: hidden !important;
}

.main-content-view {
  background: #ffffff;
  padding: 0;
  border-radius: 0 !important;
  height: 100%;
  max-height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  box-shadow: none !important;
  box-sizing: border-box;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

.main-content-view.flush-view {
  background: transparent !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  min-height: 100% !important;
}

/* ================= 动画效果 ================= */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ================= 全局弹窗响应式居中（联动侧边栏展开/收起） ================= */
::v-deep .main-container .el-dialog__wrapper {
  left: 220px;
  transition: left 0.3s cubic-bezier(0.2, 0, 0, 1);
}

::v-deep .main-container.is-collapse .el-dialog__wrapper {
  left: 0;
}

::v-deep .main-container .el-dialog {
  margin: 0 auto !important;
  top: 42% !important;
  transform: translateY(-50%) !important;
}
</style>
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
              <img src="../assets/软件学院院徽.png" class="sidebar-logo">
            </router-link>
            <router-link v-else key="expand" class="sidebar-logo-link" to="/">
              <img src="../assets/软件学院院徽.png" class="sidebar-logo">
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

            <!-- AGV控制与车间检测 -->
            <el-menu-item index="/agv">
              <i class="el-icon-truck"></i>
              <span slot="title">AGV控制与车间检测</span>
            </el-menu-item>

            <!-- 新增：数据标注子菜单 -->
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

              <el-menu-item index="/daping">
                <i class="el-icon-data-analysis"></i>
                <span slot="title">数据大屏</span>
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
                <span slot="title">密钥管理</span>
              </el-menu-item>
            </el-submenu>

                        <!-- 【国赛2.0】AGV运检一体化升级系统 -->
            <el-submenu index="national-2026">
              <template slot="title">
                <i class="el-icon-medal"></i>
                <span slot="title">运检系统</span>
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
              <el-menu-item index="/omni-inspection">
                <i class="el-icon-odometer"></i>
                <span slot="title">质检工作台</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container class="main-container" :class="{ 'is-collapse': isCollapse }">
        <!-- 头部 (高度由 120px 适度缩短为 96px，上下边距更加紧凑精致) -->
        <el-header class="navbar" height="96px">
          <!-- 导航栏背景图独立层：精准应用 11200K 色温 (hue-rotate(-12deg)) 与 66% 饱和度 (saturate(0.66)) -->
          <div class="navbar-bg-layer"></div>

          <div class="navbar-left">
            <!-- 顶部折叠按钮 -->
            <div class="hamburger-container" @click="handleCollapse">
              <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
            </div>

            <!-- 固定的工业标题与标语区域 -->
            <div class="header-title-zone">
              <div class="header-main-title-row">
                <span class="header-main-title">工业设备智能管控中心</span>
                <span class="header-tag-badge">工业总线集成</span>
              </div>
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
              {{ currentTimeStr }}
            </div>

            <!-- 报警消息铃铛 (保留1个带气泡角标) -->
            <div class="header-notice-btn" @click="$router.push('/warning')">
              <el-badge :value="3" class="notice-badge">
                <i class="el-icon-bell notice-icon"></i>
              </el-badge>
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
                <i class="el-icon-arrow-down" />
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
              <router-view @update:user="updateUser" />
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
    this.title = '云擎智检';
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
  background-color: #f0f2f5;
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
  /* 调整色温至 11200K（高冷蓝调科技色温：hue-rotate(-12deg)），饱和度已恢复原始 100% */
  background: #001529 url('../assets/1.png') no-repeat bottom center;
  background-size: 100% 100%;
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
  height: 64px;
  line-height: 64px;
  background: rgba(0, 21, 41, 0.85);
  backdrop-filter: blur(4px);
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-logo-link {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.sidebar-logo {
  width: 32px;
  height: 32px;
  vertical-align: middle;
  margin-right: 12px;
  transition: all 0.3s;
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
  width: 32px;
  height: 32px;
  display: block;
}

.sidebar-title {
  display: inline-block;
  margin: 0;
  color: #fff;
  font-weight: 600;
  line-height: 50px;
  font-size: 18px;
  letter-spacing: 1px;
  vertical-align: middle;
  white-space: nowrap;
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
  margin: 4px 8px !important;
  border-radius: 4px;
  width: auto;
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
  flex: 1;
  min-width: 0;
  margin-left: 0 !important;
  background-color: #07152b;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  overflow-y: auto !important;
  box-sizing: border-box;
}

.main-container.is-collapse {
  margin-left: 0 !important;
}

/* ================= 头部导航 (Navbar) ================= */
.navbar {
  height: 120px;
  width: 100%;
  background-color: #07152b;
  box-shadow: 0 4px 16px rgba(0, 15, 30, 0.45);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  z-index: 99;
  position: relative;
  top: auto;
  border-bottom: 1px solid rgba(24, 144, 255, 0.35);
  overflow: visible;
  flex-shrink: 0;
  box-sizing: border-box;
  min-width: max-content;
}

/* 顶部导航背景图独立滤镜层：色温 11200K (hue-rotate(-12deg))，饱和度已恢复原始 100% */
.navbar-bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('../assets/2.png') no-repeat right -40px;
  background-size: 100% 333%;
  background-position: right -40px;
  filter: hue-rotate(-12deg);
  pointer-events: none;
  z-index: 1;
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

/* 汉堡折叠按钮（已缩小尺寸） */
.hamburger-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  width: 38px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 6px;
  cursor: pointer;
  margin-right: 18px;
  transition: all 0.3s;
  backdrop-filter: blur(4px);
  flex-shrink: 0;
}

.hamburger-container:hover {
  background: rgba(24, 144, 255, 0.35);
  border-color: #40a9ff;
}

.hamburger-container i {
  font-size: 18px;
  color: #ffffff;
}

/* 标题与标语区域 */
.header-title-zone {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-shrink: 0;
  white-space: nowrap;
}

.header-main-title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
  flex-shrink: 0;
}

.header-main-title {
  color: #ffffff;
  font-weight: 700;
  font-size: 28px;
  letter-spacing: 1.2px;
  line-height: 38px;
  text-shadow: 0 0 16px rgba(64, 169, 255, 0.9);
  white-space: nowrap;
  flex-shrink: 0;
}

.header-tag-badge {
  display: inline-block;
  padding: 4px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: 1px solid rgba(145, 213, 255, 0.6);
  border-radius: 6px;
  box-shadow: 0 3px 10px rgba(24, 144, 255, 0.5);
  letter-spacing: 0.8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.header-sub-slogan {
  font-size: 15px;
  color: #ffffff;
  font-weight: 500;
  margin-top: 12px;
  letter-spacing: 1.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.9), 0 0 10px rgba(24, 144, 255, 0.6);
  white-space: nowrap;
  flex-shrink: 0;
}

/* 右侧区域容器 (1:1 还原参考图) */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  white-space: nowrap;
}

/* 胶囊搜索框 */
.header-search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 0 16px;
  height: 42px;
  width: 270px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
}

.header-search-box:focus-within {
  background: #ffffff;
  border-color: #40a9ff;
  box-shadow: 0 0 12px rgba(64, 169, 255, 0.6);
  width: 290px;
}

.search-icon {
  font-size: 17px;
  color: #595959;
  margin-right: 8px;
}

.search-input {
  border: none;
  background: transparent;
  outline: none;
  font-size: 13.5px;
  color: #333333;
  width: 100%;
}

.search-input::placeholder {
  color: #8c8c8c;
}

/* 实时时间显示 */
.header-time-display {
  font-size: 15px;
  font-weight: 500;
  color: #ffffff;
  letter-spacing: 0.8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8), 0 0 8px rgba(24, 144, 255, 0.4);
  font-family: 'Roboto Mono', Consolas, Monaco, monospace;
}

/* 单铃铛通知按钮 */
.header-notice-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px 6px;
  transition: all 0.3s;
}

.notice-icon {
  font-size: 24px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.6));
  transition: transform 0.3s ease;
}

.header-notice-btn:hover .notice-icon {
  transform: scale(1.15) rotate(15deg);
  color: #40a9ff;
}

::v-deep .notice-badge .el-badge__content {
  background-color: #ff4d4f;
  border: 1.5px solid #ffffff;
  font-weight: 700;
  font-size: 11px;
  height: 18px;
  line-height: 16px;
  padding: 0 5px;
  top: -2px;
  right: 2px;
}

/* 分割线 */
.header-divider {
  width: 1px;
  height: 28px;
  background: rgba(255, 255, 255, 0.25);
  margin: 0 2px;
}

/* 用户胶囊下拉栏 */
.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 14px 4px 6px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
  white-space: nowrap;
}

.avatar-wrapper:hover {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(64, 169, 255, 0.7);
  box-shadow: 0 0 10px rgba(64, 169, 255, 0.4);
}

.user-avatar-circle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 19px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.user-name {
  margin: 0 10px 0 8px;
  font-size: 14.5px;
  font-weight: 500;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.avatar-wrapper .el-icon-arrow-down {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  font-weight: bold;
}

/* ================= 内容区 (Content) - 零间隙无缝全平铺 ================= */
.app-main {
  padding: 0 !important;
  width: 100%;
  position: relative;
  overflow: visible !important; /* 彻底取消 el-main 的内部滚动 */
  box-sizing: border-box;
  flex: 1; /* 自动撑满剩余视口高度 */
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
  background-color: #f0f2f5;
}

.app-main.is-fullscreen-view {
  padding: 0 !important;
  overflow: visible !important;
}

.main-content-view {
  background: #fff;
  padding: 9px 11px;
  border-radius: 0 !important;
  min-height: 100%;
  flex: 1;
  box-shadow: none !important;
  box-sizing: border-box;
  width: 100%;
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
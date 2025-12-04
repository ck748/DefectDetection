<template>
  <div class="home-container">
    <el-container class="app-wrapper">
      <!-- 侧边栏 -->
      <el-aside
        :width="isCollapse ? '64px' : '220px'"
        class="sidebar-container"
      >
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
          <!-- 
            核心修改：
            1. 删除了 :default-openeds 属性，解决了“一点就全展开”和“默认不收起”的问题。
            2. unique-opened="false" 保持不变，保证各个菜单展开独立。
          -->
          <el-menu
            :collapse="isCollapse"
            :unique-opened="false"
            :collapse-transition="false"
            mode="vertical"
            router
            :default-active="$route.path"
            background-color="#001529"
            text-color="rgba(255, 255, 255, 0.65)"
            active-text-color="#ffffff"
            class="el-menu-vertical"
          >
            <!-- 数据大屏 -->
            <el-menu-item index="/daping">
              <i class="el-icon-data-analysis"></i>
              <span slot="title">数据大屏</span>
            </el-menu-item>

            <!-- 概要信息 -->
            <el-menu-item index="/dashboard">
              <i class="el-icon-monitor"></i>
              <span slot="title">概要信息</span>
            </el-menu-item>
           
            <!-- 监测信息 -->
            <el-submenu index="info">
              <template slot="title">
                <i class="el-icon-data-line"></i>
                <span>监测信息</span>
              </template>
              <el-menu-item index="/info">
                <i class="el-icon-tickets"></i>
                <span>历史检测</span>
              </el-menu-item>
              <el-menu-item index="/annotation">
                <i class="el-icon-edit-outline"></i>
                <span>数据标注</span>
              </el-menu-item>
              <el-menu-item index="/charts">
                <i class="el-icon-pie-chart"></i>
                <span>检测报表</span>
              </el-menu-item>
              <el-menu-item index="/history-carousel">
                <i class="el-icon-picture"></i>
                <span>历史轮播</span>
              </el-menu-item>
              <el-menu-item index="/warning">
                <i class="el-icon-warning-outline"></i>
                <span>预警信息</span>
              </el-menu-item>
            </el-submenu>
           
            <!-- 链上管理 -->
            <!-- <el-submenu index="chainmanager">
              <template slot="title">
                <i class="el-icon-connection"></i>
                <span>链上管理</span>
              </template>
              <el-menu-item index="/production">
                <i class="el-icon-cpu"></i>
                <span>生产管理</span>
              </el-menu-item>
              <el-menu-item index="/assembly">
                <i class="el-icon-set-up"></i>
                <span>组装管理</span>
              </el-menu-item>
            </el-submenu> -->
           
            <!-- 系统管理 -->
            <el-submenu index="sysmanager">
              <template slot="title">
                <i class="el-icon-setting"></i>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/log">
                <i class="el-icon-notebook-2"></i>
                <span>日志管理</span>
              </el-menu-item>
              <el-menu-item index="/apimanager">
                <i class="el-icon-key"></i>
                <span>API管理</span>
              </el-menu-item>
              <el-menu-item index="/pwdmanager">
                <i class="el-icon-lock"></i>
                <span>密钥管理</span>
              </el-menu-item>
            </el-submenu>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container class="main-container" :class="{ 'is-collapse': isCollapse }">
        <!-- 头部 -->
        <el-header class="navbar">
          <div class="navbar-left">
            <!-- 顶部折叠按钮 -->
            <div class="hamburger-container" @click="handleCollapse">
               <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
            </div>

            <!-- 面包屑 -->
            <el-breadcrumb class="app-breadcrumb" separator="/">
              <transition-group name="breadcrumb">
                <el-breadcrumb-item key="home" :to="{ path: '/' }">
                   <span class="breadcrumb-item-text">主页</span>
                </el-breadcrumb-item>
                <el-breadcrumb-item key="current">
                   <span class="breadcrumb-item-text active">{{ $route.meta.name || $route.name }}</span>
                </el-breadcrumb-item>
              </transition-group>
            </el-breadcrumb>
          </div>

          <div class="navbar-right">
            <el-tooltip content="全屏切换" effect="dark" placement="bottom">
              <div class="right-menu-item hover-effect" @click="handleFull">
                <i class="el-icon-full-screen"></i>
              </div>
            </el-tooltip>

            <el-dropdown class="avatar-container" trigger="click">
              <div class="avatar-wrapper">
                <el-avatar
                  :size="32"
                  :src="user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                  class="user-avatar"
                ></el-avatar>
                <span class="user-name">{{ user.name || '管理员' }}</span>
                <i class="el-icon-caret-bottom" />
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
        <el-main class="app-main">
          <transition name="fade-transform" mode="out-in">
            <div class="main-content-view">
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
      title: '智检控系统'
    }
  },
  mounted() {
    this.title = '智检控系统';
    if (this.$route.path === '/') {
      this.$router.replace('/daping');
    }
  },
  methods: {
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
    }
  }
}
</script>

<style scoped>
/* ================= 全局容器 ================= */
.home-container {
  height: 100vh;
  width: 100%;
  background-color: #f0f2f5;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.app-wrapper {
  height: 100%;
  width: 100%;
}

/* ================= 侧边栏 (Sidebar) ================= */
.sidebar-container {
  background-color: #001529; /* 原始深蓝黑 */
  height: 100%;
  position: fixed;
  font-size: 0px;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  display: flex;
  flex-direction: column;
}

/* Logo 区域 */
.logo-container {
  position: relative;
  width: 100%;
  height: 64px;
  line-height: 64px;
  background: #002140; 
  text-align: center;
  overflow: hidden;
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

.logo-container.collapse .sidebar-logo {
  margin-right: 0;
  width: 28px;
  height: 28px;
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
  overflow-x: hidden;
  overflow-y: auto;
}
::v-deep .el-scrollbar__wrap::-webkit-scrollbar {
  width: 6px;
  background-color: #001529;
}
::v-deep .el-scrollbar__wrap::-webkit-scrollbar-thumb {
  background-color: #304156;
  border-radius: 10px;
}

/* 菜单整体样式 */
::v-deep .el-menu {
  border: none;
  width: 100% !important;
}

/* 菜单项基础样式 */
::v-deep .el-menu-item,
::v-deep .el-submenu__title {
  height: 50px;
  line-height: 50px;
  margin: 4px 10px !important;
  border-radius: 4px;
  width: auto;
  transition: all 0.3s;
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
  min-height: 100vh;
  transition: margin-left 0.3s;
  margin-left: 220px;
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.main-container.is-collapse {
  margin-left: 64px;
}

/* ================= 头部导航 (Navbar) ================= */
.navbar {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  z-index: 9;
  position: sticky;
  top: 0;
}

.navbar-left {
  display: flex;
  align-items: center;
  height: 100%;
}

/* 汉堡按钮 */
.hamburger-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  width: 36px;
  background: #f7f8fa; 
  border-radius: 6px; 
  cursor: pointer;
  margin-right: 16px; 
  transition: all 0.3s;
}
.hamburger-container:hover {
  background: #eef0f3;
}
.hamburger-container i {
  font-size: 18px;
  color: #5a5e66;
}

/* 面包屑 */
.navbar-left .app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 64px;
}
.breadcrumb-item-text {
  color: #606266;
  font-weight: 400;
  transition: color 0.3s;
}
.breadcrumb-item-text.active {
  color: #303133;
  font-weight: 600;
}
::v-deep .el-breadcrumb__inner a:hover .breadcrumb-item-text {
  color: #1890ff;
}

/* 右侧菜单 */
.navbar-right {
  display: flex;
  align-items: center;
}

.right-menu-item {
  padding: 0 12px;
  cursor: pointer;
  color: #5a5e66;
  font-size: 20px;
  transition: all 0.3s;
  border-radius: 4px;
}
.right-menu-item:hover {
  color: #1890ff;
  background-color: rgba(0,0,0,0.025);
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}
.avatar-wrapper:hover {
  background: rgba(0,0,0,0.025);
}
.user-avatar {
  border: 1px solid #e6e6e6;
}
.user-name {
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* ================= 内容区 (Content) ================= */
.app-main {
  padding: 20px;
  width: 100%;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

.main-content-view {
  background: #fff;
  padding: 24px; 
  border-radius: 4px;
  min-height: calc(100vh - 124px); 
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
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
</style>
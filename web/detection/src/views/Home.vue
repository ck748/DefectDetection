<template>
  <div class="home-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="asideWidth" class="sidebar" :class="{ 'sidebar-collapsed': isCollapse }">
        <div class="logo-section">
          <img src="../assets/软件学院院徽.png" alt="院徽" class="logo-img"/>
          <transition name="fade">
            <span class="logo-title" v-show="!isCollapse">{{ title }}</span>
          </transition>
        </div>

        <el-menu 
          :default-openeds="['info', 'sysmanager']" 
          :collapse="isCollapse"  
          :collapse-transition="false" 
          router 
          class="sidebar-menu"
          :default-active="$route.path"
          background-color="#1a2a3a"
          text-color="#b0bec5"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/dashboard" class="menu-item">
            <i class="el-icon-monitor menu-icon"></i>
            <span slot="title" class="menu-text">概要信息</span>
          </el-menu-item>
          
          <el-submenu index="info">
            <template slot="title">
              <i class="el-icon-data-line menu-icon"></i>
              <span class="menu-text">监测信息</span>
            </template>
            <el-menu-item index="/info" class="submenu-item">
              <i class="el-icon-tickets menu-sub-icon"></i>
              <span>历史检测</span>
            </el-menu-item>
            <el-menu-item index="/charts" class="submenu-item">
              <i class="el-icon-data-analysis menu-sub-icon"></i>
              <span>监测报表</span>
            </el-menu-item>
            <el-menu-item index="/warning" class="submenu-item">
              <i class="el-icon-warning menu-sub-icon"></i>
              <span>异常管理</span>
            </el-menu-item>
          </el-submenu>
          
          <el-menu-item index="/log" class="menu-item">
            <i class="el-icon-notebook-2 menu-icon"></i>
            <span slot="title" class="menu-text">日志管理</span>
          </el-menu-item>
          
          <el-submenu index="sysmanager">
            <template slot="title">
              <i class="el-icon-setting menu-icon"></i>
              <span class="menu-text">系统管理</span>
            </template>
            <el-menu-item index="/apimanager" class="submenu-item">
              <i class="el-icon-key menu-sub-icon"></i>
              <span>API管理</span>
            </el-menu-item>
            <el-menu-item index="/pwdmanager" class="submenu-item">
              <i class="el-icon-lock menu-sub-icon"></i>
              <span>密钥管理</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
        
        <!-- 侧边栏底部收缩按钮 -->
        <div class="sidebar-footer" @click="handleCollapse">
          <i :class="collapseIcon" class="collapse-btn"></i>
          <transition name="fade">
            <span v-show="!isCollapse" class="collapse-text">收起菜单</span>
          </transition>
        </div>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container class="main-container">
        <!-- 头部区域 -->
        <el-header class="main-header">
          <div class="header-left">
            <i :class="collapseIcon" class="header-collapse-icon" @click="handleCollapse"></i>
            <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">主页</el-breadcrumb-item>
              <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-tooltip content="全屏" placement="bottom">
              <i class="el-icon-full-screen header-icon" @click="handleFull"></i>
            </el-tooltip>
            
            <el-dropdown class="user-dropdown" trigger="click">
              <div class="user-info">
                <el-avatar 
                  :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                  :size="36"
                  class="user-avatar"
                ></el-avatar>
                <div class="user-details">
                  <span class="user-name">{{ user.name || '用户' }}</span>
                  <span class="user-role">管理员</span>
                </div>
                <i class="el-icon-arrow-down dropdown-arrow"></i>
              </div>
              <el-dropdown-menu slot="dropdown" class="user-menu">
                <el-dropdown-item @click.native="$router.push('/person')" class="menu-item">
                  <i class="el-icon-user"></i>
                  <span>个人信息</span>
                </el-dropdown-item>
                <el-dropdown-item @click.native="$router.push('/password')" class="menu-item">
                  <i class="el-icon-edit"></i>
                  <span>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click.native="logout" class="menu-item logout-item">
                  <i class="el-icon-switch-button"></i>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主体内容区域 -->
        <el-main class="main-content">
          <div class="content-wrapper">
            <router-view @update:user="updateUser" />
          </div>
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
      asideWidth: '240px',
      collapseIcon: 'el-icon-s-fold',
      user: JSON.parse(localStorage.getItem('useradmin') || '{}'),
      title: '智检控系统'
    }
  },
  mounted() {
    this.title = '智检控系统';
  },
  methods: {
    updateUser(user) {
      this.user = JSON.parse(JSON.stringify(user));
    },
    logout() {
      this.$confirm('确定要退出登录吗？', '提示', {
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
      this.asideWidth = this.isCollapse ? '64px' : '240px';
      this.collapseIcon = this.isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold';
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}

/* 侧边栏样式 - 重新设计配色 */
.sidebar {
  background: linear-gradient(180deg, #1a2a3a 0%, #2d3e50 100%);
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-collapsed {
  width: 64px !important;
}

.logo-section {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
}

.logo-img {
  width: 36px;
  height: 36px;
  transition: all 0.3s ease;
  filter: brightness(0) invert(1);
}

.logo-title {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  white-space: nowrap;
  transition: all 0.3s ease;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 菜单样式 - 优化配色 */
.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent !important;
  padding: 16px 8px;
}

.sidebar-menu .menu-item {
  height: 48px !important;
  line-height: 48px !important;
  margin: 4px 0;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #b0bec5 !important;
  position: relative;
  overflow: hidden;
}

.sidebar-menu .menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 0;
  background: linear-gradient(90deg, rgba(74, 144, 226, 0.1), transparent);
  transition: width 0.3s ease;
}

.sidebar-menu .menu-item:hover::before {
  width: 100%;
}

.sidebar-menu .menu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff !important;
  transform: translateX(4px);
}

.sidebar-menu .menu-item.is-active {
  background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%) !important;
  color: #ffffff !important;
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.4);
  transform: translateX(0);
}

.sidebar-menu .menu-item.is-active::before {
  display: none;
}

.menu-icon {
  font-size: 18px;
  color: inherit;
  margin-right: 12px;
  transition: all 0.3s ease;
}

.menu-text {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 子菜单样式 */
.sidebar-menu .el-submenu__title {
  height: 48px !important;
  line-height: 48px !important;
  margin: 4px 0;
  border-radius: 10px;
  color: #b0bec5 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.sidebar-menu .el-submenu__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 0;
  background: linear-gradient(90deg, rgba(74, 144, 226, 0.1), transparent);
  transition: width 0.3s ease;
}

.sidebar-menu .el-submenu__title:hover::before {
  width: 100%;
}

.sidebar-menu .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff !important;
  transform: translateX(4px);
}

.sidebar-menu .el-submenu.is-opened .el-submenu__title {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

.sidebar-menu .el-submenu .el-menu {
  background: rgba(0, 0, 0, 0.25) !important;
  border-radius: 8px;
  margin: 8px 0;
  padding: 4px;
}

.submenu-item {
  height: 40px !important;
  line-height: 40px !important;
  margin: 2px 0;
  border-radius: 8px;
  color: #90a4ae !important;
  transition: all 0.3s ease;
  min-width: 0 !important;
  position: relative;
}

.submenu-item::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: currentColor;
  border-radius: 50%;
  opacity: 0;
  transition: all 0.3s ease;
}

.submenu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff !important;
  padding-left: 20px;
}

.submenu-item:hover::before {
  opacity: 1;
  left: 12px;
}

.submenu-item.is-active {
  background: rgba(74, 144, 226, 0.2) !important;
  color: #4a90e2 !important;
  border-left: 3px solid #4a90e2;
  padding-left: 20px;
}

.submenu-item.is-active::before {
  opacity: 1;
  left: 12px;
  background: #4a90e2;
}

.menu-sub-icon {
  font-size: 14px;
  margin-right: 8px;
  color: inherit;
  transition: all 0.3s ease;
}

/* 侧边栏底部 */
.sidebar-footer {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  gap: 8px;
  padding: 0 16px;
  backdrop-filter: blur(10px);
}

.sidebar-footer:hover {
  background: rgba(255, 255, 255, 0.08);
}

.collapse-btn {
  font-size: 18px;
  color: #90a4ae;
  transition: all 0.3s ease;
}

.collapse-text {
  font-size: 12px;
  color: #90a4ae;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.sidebar-footer:hover .collapse-btn,
.sidebar-footer:hover .collapse-text {
  color: #ffffff;
}

.sidebar-footer:hover .collapse-btn {
  transform: scale(1.1) rotate(180deg);
}

/* 主内容区域 */
.main-container {
  background: #f5f7fa;
  min-height: 100vh;
}

.main-header {
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid #eaeef2;
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-collapse-icon {
  font-size: 20px;
  color: #5a6270;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
}

.header-collapse-icon:hover {
  background: #4a90e2;
  color: #ffffff;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb ::v-deep .el-breadcrumb__inner {
  color: #6b7280;
  font-weight: 500;
  transition: color 0.3s ease;
}

.breadcrumb ::v-deep .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #4a90e2;
  font-weight: 600;
}

.breadcrumb ::v-deep .el-breadcrumb__separator {
  color: #9ca3af;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
}

.header-icon:hover {
  background: #4a90e2;
  color: #ffffff;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

/* 用户信息下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
  border: 1px solid transparent;
}

.user-info:hover {
  background: #ffffff;
  border-color: #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.user-avatar {
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info:hover .user-avatar {
  border-color: #4a90e2;
  transform: scale(1.05);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.2;
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 12px;
  margin-top: 2px;
}

.dropdown-arrow {
  font-size: 12px;
  color: #9ca3af;
  transition: transform 0.3s ease;
}

.user-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
  color: #4a90e2;
}

.user-menu {
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border: 1px solid #e5e7eb;
  padding: 8px;
}

.user-menu .menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: #4b5563;
}

.user-menu .menu-item:hover {
  background: #f3f4f6;
  color: #4a90e2;
  transform: translateX(4px);
}

.user-menu .logout-item {
  color: #ef4444;
}

.user-menu .logout-item:hover {
  background: #fef2f2;
  color: #ef4444;
}

.user-menu .logout-item i {
  color: #ef4444;
}

/* 主内容区域 */
.main-content {
  padding: 0;
  background: #f5f7fa;
}

.content-wrapper {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #ffffff;
  margin: 16px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
}

/* 动画效果 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }
  
  .main-container {
    margin-left: 64px;
  }
  
  .main-header {
    padding: 0 16px;
  }
  
  .content-wrapper {
    padding: 16px;
    margin: 8px;
    border-radius: 12px;
  }
  
  .user-details {
    display: none;
  }
  
  .collapse-text {
    display: none;
  }
}

/* 滚动条美化 */
.sidebar-menu ::v-deep .el-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu ::v-deep .el-menu::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.sidebar-menu ::v-deep .el-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.sidebar-menu ::v-deep .el-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
<template>
  <div id="app">
    <router-view/>
    <!-- 6S管家 AI智能体 (全页面常驻悬浮球+抽屉) -->
    <SixSButler />
  </div>
</template>

<script>
import sseManager from '@/utils/sseManager';
import SixSButler from '@/components/SixSButler.vue';

export default {
  name: 'App',
  components: {
    SixSButler
  },
  mounted() {
    // 应用启动时初始化全局SSE连接
    sseManager.init();
  },
  beforeDestroy() {
    // 应用关闭时关闭SSE连接
    sseManager.close();
  }
}
</script>

<style>
/* 全局重置浏览器默认外边距与内边距，彻底消除四周白边与滚动条 */
html, body {
  margin: 0 !important;
  padding: 0 !important;
  width: 100%;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

/* 确保根容器完全撑满视口 */
#app {
  position: relative;
  width: 100%;
  height: 100%;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden;
}
</style>

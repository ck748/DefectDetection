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
/* 确保根容器支持定位 */
#app {
  position: relative;
  min-height: 100vh;
}
</style>

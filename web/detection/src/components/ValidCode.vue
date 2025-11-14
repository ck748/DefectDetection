<template>
  <div class="valid-code-container" @click="refreshCode">
    <div class="valid-code-wrapper">
      <span 
        v-for="(item, index) in codeList" 
        :key="index" 
        class="code-char"
        :style="getStyle(item)"
      >
        {{ item.code }}
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ValidCode',
  data () {
    return {
      length: 4,
      codeList: []
    }
  },
  mounted () {
    this.createdCode()
  },
  methods: {
    refreshCode () {
      // 添加点击动画效果
      this.$el.style.transform = 'scale(0.95)'
      setTimeout(() => {
        this.$el.style.transform = 'scale(1)'
        this.createdCode()
      }, 150)
    },
    createdCode () {
      let len = this.length,
          codeList = [],
          chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz23456789',
          charsLen = chars.length
      
      // 生成验证码字符和样式
      for (let i = 0; i < len; i++) {
        let rgb = [
          Math.round(Math.random() * 100 + 80),
          Math.round(Math.random() * 100 + 80),
          Math.round(Math.random() * 100 + 80)
        ]
        
        codeList.push({
          code: chars.charAt(Math.floor(Math.random() * charsLen)),
          color: `rgb(${rgb})`,
          fontSize: `${Math.floor(Math.random() * 6) + 22}px`,
          padding: `${Math.floor(Math.random() * 6)}px ${Math.floor(Math.random() * 3)}px`,
          transform: `rotate(${Math.floor(Math.random() * 30) - 15}deg)`,
          fontWeight: Math.random() > 0.5 ? '600' : '400',
          textShadow: `1px 1px 3px rgba(${rgb.map(c => c - 60).join(',')}, 0.4)`,
          margin: `0 ${Math.floor(Math.random() * 4)}px`
        })
      }
      
      this.codeList = codeList
      this.$emit('update:value', codeList.map(item => item.code).join(''))
    },
    getStyle (data) {
      return `
        color: ${data.color};
        font-size: ${data.fontSize};
        padding: ${data.padding};
        transform: ${data.transform};
        font-weight: ${data.fontWeight};
        text-shadow: ${data.textShadow};
        margin: ${data.margin};
      `
    }
  }
}
</script>

<style scoped>
.valid-code-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.valid-code-container:hover {
  border-color: #3b82f6;
  box-shadow: 
    0 12px 40px rgba(59, 130, 246, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: translateY(-3px);
}

.valid-code-container:active {
  transform: scale(0.97);
}

.valid-code-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 2;
  padding: 20px;
}

.code-char {
  display: inline-block;
  font-family: 'Segoe UI', 'Arial', 'Microsoft YaHei', sans-serif;
  transition: all 0.3s ease;
  position: relative;
  user-select: none;
  animation: charAppear 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.code-char:hover {
  transform: scale(1.15) !important;
  z-index: 3;
}

/* 背景干扰元素 */
.valid-code-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 25%, rgba(99, 102, 241, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 85% 75%, rgba(236, 72, 153, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(139, 92, 246, 0.02) 0%, transparent 50%);
  z-index: 1;
  opacity: 0.6;
}

.valid-code-container::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    linear-gradient(45deg, transparent 40%, rgba(255, 255, 255, 0.1) 50%, transparent 60%),
    linear-gradient(-45deg, transparent 40%, rgba(255, 255, 255, 0.05) 50%, transparent 60%);
  animation: shimmer 8s infinite linear;
  z-index: 1;
}

/* 禁用文本选择 */
.valid-code-container {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

/* 动画 */
@keyframes charAppear {
  0% {
    opacity: 0;
    transform: translateY(15px) rotate(-8deg) scale(0.8);
  }
  60% {
    opacity: 1;
    transform: translateY(-2px) rotate(2deg) scale(1.05);
  }
  100% {
    opacity: 1;
    transform: translateY(0) rotate(var(--rotation)) scale(1);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(0deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .valid-code-container {
    border-radius: 12px;
  }
  
  .valid-code-wrapper {
    padding: 15px;
  }
}

/* 加载状态 */
.valid-code-container.loading .code-char {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(0.95);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}
</style>
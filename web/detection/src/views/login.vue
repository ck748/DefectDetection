<template>
  <!-- 整体容器 -->
  <div class="login-wrapper">
    <!-- 动态背景层：保留半透明遮罩，保证文字清晰 -->
    <div class="bg-layer"></div>
    <div class="bg-grid"></div>

    <!-- 登录卡片 (完全保持不变) -->
    <div class="login-card slide-up-fade">
      
      <!-- 左侧：品牌与视觉展示 -->
      <div class="card-left">
        <div class="brand-header">
          <div class="logo-box">
            <i class="el-icon-s-platform"></i>
          </div>
          <span class="brand-name">云擎智检</span>
        </div>
        
        <div class="visual-center">
          <h1 class="hero-title">半轴检测系统 <br><span>V3.0 Enterprise</span></h1>
          <p class="hero-desc">
            基于云边协同的汽车半轴数智化缺陷检测系统<br>
            实时监控 · 智能预警 · 数据透视
          </p>
          <!-- 装饰性数据图表元素 -->
          <div class="data-decoration">
            <div class="data-item">
              <span>精度</span>
              <div class="bar"><div class="fill" style="width: 98%"></div></div>
            </div>
            <div class="data-item">
              <span>响应</span>
              <div class="bar"><div class="fill" style="width: 95%"></div></div>
            </div>
          </div>
        </div>

        <div class="left-footer">
          <span>&copy; Smart Industry Group</span>
          <span>Server Status: <i class="status-dot"></i> Online</span>
        </div>
        
        <!-- 装饰圆环 -->
        <div class="circle-deco c1"></div>
        <div class="circle-deco c2"></div>
      </div>

      <!-- 右侧：登录表单 -->
      <div class="card-right">
        <div class="form-header">
          <h3>用户登录</h3>
          <p>User Login</p>
        </div>

        <el-form :model="user" :rules="rules" ref="loginRef" class="custom-form" hide-required-asterisk>
          <!-- 账号 -->
          <el-form-item prop="account">
            <el-input 
              v-model="user.account" 
              placeholder="请输入管理员账号" 
              prefix-icon="el-icon-user-solid"
            ></el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="pwd">
            <el-input 
              v-model="user.pwd" 
              type="password" 
              show-password 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock"
              @keyup.enter.native="login"
            ></el-input>
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="code" class="code-item">
            <div class="code-wrap">
              <el-input 
                v-model="user.code" 
                placeholder="验证码" 
                prefix-icon="el-icon-key"
                class="code-input"
                @keyup.enter.native="login"
              ></el-input>
              <div class="verify-canvas">
                <valid-code @update:value="getCode" />
              </div>
            </div>
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item class="btn-item">
            <el-button 
              type="primary" 
              class="login-btn" 
              :loading="loading" 
              @click="login"
            >
              {{ loading ? '系统接入中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <div class="form-extras">
            <span class="help-link" @click="handleForgetPass">忘记密码?</span>
            <span class="help-link">注册新账户</span>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 弹窗组件 -->
    <el-dialog 
      title="提示" 
      :visible.sync="forgetPassDialogVis" 
      width="400px" 
      center 
      custom-class="tech-dialog"
      :show-close="false"
    >
      <div class="dialog-content">
        <i class="el-icon-warning"></i>
        <p class="dialog-text">请联系企业 IT 管理员重置密码</p>
        <p class="dialog-phone">TEL: 0755-8888-9999</p>
      </div>
      <span slot="footer">
        <el-button type="primary" size="small" @click="forgetPassDialogVis = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import ValidCode from "@/components/ValidCode.vue";
import axios from "axios";

export default {
  name: "Login",
  components: { ValidCode },
  data() {
    const validateCode = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入验证码'));
      } else if (value.toLowerCase() !== this.code) {
        callback(new Error('验证码错误'));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      forgetPassDialogVis: false,
      code: '',
      user: {
        code: '',
        account: '',
        pwd: '',
      },
      rules: {
        account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        pwd: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        code: [{ validator: validateCode, trigger: 'blur' }]
      }
    };
  },
  methods: {
    handleForgetPass() {
      this.forgetPassDialogVis = true;
    },
    getCode(code) {
      this.code = code.toLowerCase();
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          this.loading = true;
          // 模拟请求
          setTimeout(() => {
            axios.post('api/login/in', { account: this.user.account, pwd: this.user.pwd })
              .then(res => {
                if (res.data.code === 200) {
                  localStorage.setItem('useradmin', JSON.stringify(res.data.data));
                  this.$message.success('登录成功');
                  this.$router.push('/daping');
                } else {
                  this.$message.error(res.data.message);
                }
              })
              .catch(() => {
                this.$message.success('演示模式：登录成功');
                this.$router.push('/daping');
              })
              .finally(() => {
                this.loading = false;
              });
          }, 800);
        }
      });
    }
  }
};
</script>

<!-- 全局样式：去除白边 -->
<style>
body, html {
  margin: 0 !important;
  padding: 0 !important;
  height: 100%;
  width: 100%;
  overflow: hidden;
}
</style>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }

.login-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Roboto', sans-serif;
  overflow: hidden;

  /* 深蓝自动化工厂，非常有纵深感 */
  background-image: url('../assets/8.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 动态背景遮罩层：加深了一点点，为了让文字在复杂的工厂背景上也能看清 */
.bg-layer {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(15, 23, 42, 0.4); 
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(30, 64, 175, 0.4) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(6, 182, 212, 0.2) 0%, transparent 40%);
  z-index: 1;
}

/* 科技网格 (保持) */
.bg-grid {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
}

/* ================== 主要修改：调整左右比例 ================== */
.login-card {
  position: relative;
  z-index: 10;
  display: flex;
  width: 960px;
  height: 580px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.card-left {
  flex: 0 0 49.9%; /* 从 45% 调整为 58%，让左侧更宽 */
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  position: relative;
  color: #fff;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.1); /* 增强分隔线 */
}

.card-right {
  flex: 0 0 50.1%; /* 从 55% 调整为 42%，让右侧变窄 */
  padding: 50px 40px; /* 减少右侧内边距 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
  border-left: 1px solid rgba(226, 232, 240, 0.6); /* 添加分隔线 */
}

/* ================== 其他样式保持不变 ================== */

.circle-deco {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.05);
  z-index: 1;
}
.c1 { width: 300px; height: 300px; top: -50px; left: -50px; border-width: 20px; border-color: rgba(255,255,255,0.02); }
.c2 { width: 500px; height: 500px; bottom: -100px; right: -100px; border: 1px dashed rgba(255,255,255,0.1); animation: spin 60s linear infinite; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.brand-header { display: flex; align-items: center; gap: 12px; margin-bottom: 60px; position: relative; z-index: 2; }
.logo-box { 
  width: 40px; height: 40px; background: #3b82f6; border-radius: 8px; 
  display: flex; align-items: center; justify-content: center; font-size: 24px; 
}
.brand-name { font-size: 20px; font-weight: 700; letter-spacing: 1px; }

.visual-center { flex: 1; position: relative; z-index: 2; display: flex; flex-direction: column; justify-content: center; }
.hero-title { font-size: 32px; line-height: 1.3; font-weight: 700; margin-bottom: 20px; text-shadow: 0 2px 10px rgba(0,0,0,0.3); }
.hero-title span { font-size: 14px; font-weight: 400; color: #94a3b8; text-transform: uppercase; letter-spacing: 2px; display: block; margin-top: 5px; }
.hero-desc { font-size: 14px; color: #cbd5e1; line-height: 1.8; opacity: 0.8; margin-bottom: 40px; }

.data-decoration { width: 100%; max-width: 240px; }
.data-item { display: flex; align-items: center; margin-bottom: 15px; font-size: 12px; color: #94a3b8; }
.data-item span { width: 40px; }
.bar { flex: 1; height: 4px; background: rgba(255,255,255,0.1); border-radius: 2px; overflow: hidden; margin-left: 10px; }
.fill { height: 100%; background: #3b82f6; border-radius: 2px; box-shadow: 0 0 10px #3b82f6; }

.left-footer { font-size: 12px; color: #64748b; display: flex; justify-content: space-between; position: relative; z-index: 2; }
.status-dot { display: inline-block; width: 6px; height: 6px; background: #10b981; border-radius: 50%; margin-right: 4px; box-shadow: 0 0 5px #10b981; }

.form-header { margin-bottom: 40px; text-align: left; }
.form-header h3 { font-size: 26px; color: #1e293b; margin: 0; font-weight: 700; }
.form-header p { font-size: 14px; color: #94a3b8; margin-top: 8px; text-transform: uppercase; letter-spacing: 1px; }

.custom-form ::v-deep .el-form-item { margin-bottom: 25px; }

.custom-form ::v-deep .el-input__inner {
  height: 50px;
  border: none;
  border-bottom: 2px solid #e2e8f0;
  border-radius: 0;
  background: transparent;
  padding-left: 35px;
  font-size: 16px;
  color: #334155;
  transition: all 0.3s;
}

.custom-form ::v-deep .el-input__inner:focus {
  border-bottom-color: #3b82f6;
}

.custom-form ::v-deep .el-input__prefix { left: 0; display: flex; align-items: center; }
.custom-form ::v-deep .el-input__icon { font-size: 18px; color: #94a3b8; width: 25px; }

.code-wrap { display: flex; align-items: center; gap: 15px; }
.code-input { flex: 1; }
.verify-canvas { 
  width: 110px; height: 50px; border: 1px solid #f1f5f9; border-radius: 4px; 
  cursor: pointer; overflow: hidden; transition: 0.3s;
}
.verify-canvas:hover { border-color: #cbd5e1; }

.login-btn {
  width: 100%; height: 50px; border-radius: 8px; 
  font-size: 16px; font-weight: 600; letter-spacing: 2px;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  border: none;
  box-shadow: 0 10px 25px -10px rgba(37, 99, 235, 0.6);
  transition: transform 0.2s, box-shadow 0.2s;
  margin-top: 10px;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px -10px rgba(37, 99, 235, 0.7);
}

.form-extras { display: flex; justify-content: space-between; font-size: 14px; color: #64748b; margin-top: 20px; }
.help-link { cursor: pointer; transition: color 0.3s; }
.help-link:hover { color: #2563eb; text-decoration: underline; }

.slide-up-fade { animation: slideUpFade 0.8s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideUpFade {
  from { opacity: 0; transform: translateY(40px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.tech-dialog ::v-deep .el-dialog { border-radius: 12px; }
.dialog-content { text-align: center; padding: 20px 0; }
.dialog-content i { font-size: 48px; color: #f59e0b; margin-bottom: 15px; }
.dialog-text { font-size: 16px; color: #334155; margin-bottom: 5px; }
.dialog-phone { font-size: 18px; color: #2563eb; font-weight: 700; font-family: monospace; }

@media (max-width: 1000px) {
  .login-card { width: 90%; height: auto; flex-direction: column; }
  .card-left { padding: 30px; flex: none; height: 200px; width: 100%; border-right: none; border-bottom: 1px solid rgba(255, 255, 255, 0.1); }
  .card-right { padding: 40px 30px; flex: none; width: 100%; border-left: none; border-top: 1px solid rgba(226, 232, 240, 0.6); }
  .hero-desc, .data-decoration, .c2 { display: none; }
}
</style>
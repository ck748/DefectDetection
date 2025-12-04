<template>
  <!-- 还原了背景图片的容器 -->
  <div class="login-container">
    
    <!-- 登录卡片主体（保持刚才的高级设计） -->
    <div class="login-box slide-in">
      <!-- 左侧：品牌视觉区 -->
      <div class="login-left">
        <div class="brand-content">
          <div class="logo-area">
            <i class="el-icon-s-data logo-icon"></i>
            <span class="logo-text">AIS</span>
          </div>
          <div class="title-group">
            <h1 class="main-title">中轴检测系统</h1>
            <div class="sub-title">AXIS INSPECTION SYSTEM</div>
            <p class="desc-text">
              <i class="el-icon-check"></i> 纳米级精度控制<br>
              <i class="el-icon-check"></i> 实时数据分析<br>
              <i class="el-icon-check"></i> 智能预警平台
            </p>
          </div>
          <div class="copyright">© 2023 Smart Industry v3.0</div>
        </div>
        <!-- 左侧装饰性背景图层 (保持混合模式) -->
        <div class="left-bg-overlay"></div>
      </div>

      <!-- 右侧：登录表单区 -->
      <div class="login-right">
        <div class="form-wrapper">
          <div class="welcome-header">
            <h3>欢迎回来</h3>
            <p>请登录您的管理账号</p>
          </div>

          <el-form :model="user" :rules="rules" ref="loginRef" class="login-form">
            <!-- 账号 -->
            <el-form-item prop="account">
              <div class="input-group">
                <el-input 
                  v-model="user.account" 
                  placeholder="请输入账号" 
                  prefix-icon="el-icon-user"
                  class="custom-input"
                ></el-input>
              </div>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="pwd">
              <div class="input-group">
                <el-input 
                  v-model="user.pwd" 
                  type="password" 
                  show-password 
                  placeholder="请输入密码" 
                  prefix-icon="el-icon-lock"
                  class="custom-input"
                  @keyup.enter.native="login"
                ></el-input>
              </div>
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="code">
              <div class="code-row">
                <el-input 
                  v-model="user.code" 
                  placeholder="验证码" 
                  prefix-icon="el-icon-key"
                  class="custom-input code-input"
                  @keyup.enter.native="login"
                ></el-input>
                <div class="verify-box">
                  <valid-code @update:value="getCode" />
                </div>
              </div>
            </el-form-item>

            <!-- 登录按钮 -->
            <el-form-item>
              <el-button 
                type="primary" 
                class="submit-btn" 
                :loading="loading" 
                @click="login"
              >
                {{ loading ? '正在接入...' : '立即登录' }}
                <i class="el-icon-right" v-if="!loading"></i>
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <span class="link-text" @click="handleForgetPass">忘记密码？</span>
              <span class="divider">|</span>
              <span class="link-text">联系管理员</span>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 弹窗组件 -->
    <el-dialog 
      title="安全提示" 
      :visible.sync="forgetPassDialogVis" 
      width="360px" 
      center 
      custom-class="custom-dialog"
    >
      <div class="dialog-body">
        <div class="icon-box"><i class="el-icon-warning-outline"></i></div>
        <p>为保障数据安全，密码重置需人工审核。</p>
        <p class="highlight">请联系 IT 部门：800-888-9999</p>
      </div>
      <span slot="footer">
        <el-button type="primary" size="medium" round @click="forgetPassDialogVis = false">知道了</el-button>
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
        account: 'admin1',
        pwd: '123456',
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
                  this.$router.push('/dashboard');
                } else {
                  this.$message.error(res.data.message);
                }
              })
              .catch(() => {
                // 仅用于演示，实际请删除
                this.$message.warning('模拟登录成功');
                this.$router.push('/dashboard');
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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;800&display=swap');

/* ================== 核心布局 ================== */
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  
  /* --- 关键修改：换回您的背景图片 --- */
  background-image: url('../assets/背景图片1.jpg'); 
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

/* --- 背景遮罩层：确保背景图不会太花哨影响文字阅读 --- */
.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5); /* 黑色半透明遮罩 */
  backdrop-filter: blur(4px); /* 适度的高斯模糊，增加高级感 */
  z-index: 1;
}

/* 登录主体卡片 */
.login-box {
  width: 1000px;
  height: 600px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5); /* 阴影稍微加深一点，适配深色背景 */
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 10; /* 确保在遮罩层之上 */
}

/* ================== 左侧品牌区 (保持不变) ================== */
.login-left {
  flex: 0 0 42%;
  background: #0f172a;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 40px;
  color: #fff;
  overflow: hidden;
}

.left-bg-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: url('../assets/2.png'); 
  background-size: cover;
  background-position: center;
  opacity: 0.2;
  mix-blend-mode: luminosity; 
  z-index: 0;
  transition: transform 10s linear;
}
.login-left:hover .left-bg-overlay {
  transform: scale(1.1);
}

.brand-content { position: relative; z-index: 2; }

.logo-area { display: flex; align-items: center; margin-bottom: 40px; opacity: 0.9; }
.logo-icon { font-size: 32px; margin-right: 10px; color: #38bdf8; }
.logo-text { font-size: 24px; font-weight: 800; letter-spacing: 2px; }

.main-title {
  font-size: 42px; font-weight: 900; margin: 0; line-height: 1.2;
  background: linear-gradient(to right, #ffffff, #94a3b8);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.sub-title { font-size: 14px; letter-spacing: 4px; color: #64748b; margin: 10px 0 40px; font-weight: 600; }
.desc-text { font-size: 15px; color: #94a3b8; line-height: 2.2; }
.desc-text i { color: #38bdf8; margin-right: 8px; }
.copyright { position: absolute; bottom: 30px; left: 40px; font-size: 12px; color: #475569; }

/* ================== 右侧表单区 (保持不变) ================== */
.login-right {
  flex: 1;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.form-wrapper { width: 100%; max-width: 380px; padding: 0 20px; }
.welcome-header { margin-bottom: 40px; }
.welcome-header h3 { font-size: 28px; color: #1e293b; font-weight: 700; margin: 0 0 10px 0; }
.welcome-header p { color: #94a3b8; font-size: 15px; }

/* 输入框样式 */
.login-form ::v-deep .el-input__inner {
  height: 52px; line-height: 52px;
  background-color: #f8fafc;
  border: 1px solid transparent;
  border-radius: 12px; color: #334155; padding-left: 45px; font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.login-form ::v-deep .el-input__inner:focus {
  background-color: #ffffff; border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}
.login-form ::v-deep .el-input__prefix { left: 12px; height: 100%; display: flex; align-items: center; }
.login-form ::v-deep .el-input__icon { font-size: 18px; color: #94a3b8; transition: color 0.3s; }
.login-form ::v-deep .el-input__inner:focus ~ .el-input__prefix .el-input__icon { color: #3b82f6; }

/* 验证码 */
.code-row { display: flex; gap: 15px; }
.code-input { flex: 1; }
.verify-box {
  width: 120px; height: 52px; border-radius: 12px; overflow: hidden;
  cursor: pointer; border: 1px solid #f1f5f9; transition: all 0.3s;
}
.verify-box:hover { border-color: #cbd5e1; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }

/* 按钮 */
.submit-btn {
  width: 100%; height: 54px; border-radius: 12px; font-size: 16px; font-weight: 600;
  letter-spacing: 1px; background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border: none; box-shadow: 0 8px 20px -6px rgba(37, 99, 235, 0.5); margin-top: 10px;
  transition: all 0.3s ease;
}
.submit-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 25px -8px rgba(37, 99, 235, 0.6); }
.submit-btn:active { transform: scale(0.98); }

/* 底部 */
.form-footer { text-align: center; margin-top: 25px; font-size: 14px; color: #94a3b8; }
.link-text { cursor: pointer; transition: color 0.3s; }
.link-text:hover { color: #2563eb; }
.divider { margin: 0 10px; color: #e2e8f0; }

/* 弹窗 */
.custom-dialog ::v-deep .el-dialog { border-radius: 16px; }
.dialog-body { text-align: center; padding: 10px 0; }
.icon-box { font-size: 48px; color: #f59e0b; margin-bottom: 15px; }
.highlight { font-weight: bold; color: #1e293b; margin-top: 5px; font-size: 16px; }

/* 动画 */
.slide-in { animation: slideIn 0.8s cubic-bezier(0.2, 0.8, 0.2, 1); }
@keyframes slideIn { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }

/* 移动端适配 */
@media (max-width: 960px) {
  .login-box { width: 90%; height: auto; flex-direction: column; }
  .login-left { padding: 40px; flex: none; height: 200px; }
  .main-title { font-size: 28px; }
  .desc-text, .copyright { display: none; }
  .login-right { padding: 40px 20px; }
}
</style>
<template>
  <div id="myDiv" class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="image-overlay"></div>
        <img src="../assets/123123.jpg" alt="钢铁检测系统" class="login-image">
        <div class="welcome-text">
          <h2>钢铁检测系统</h2>
          <p>专业 · 精准 · 高效</p>
        </div>
      </div>
      <div class="login-right">
        <div class="login-form-container">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请输入您的账号和密码</p>
          </div>
          <el-form :model="user" style="width: 100%" :rules="rules" ref="loginRef">
            <el-form-item prop="account">
              <el-input 
                prefix-icon="el-icon-user" 
                size="medium" 
                placeholder="请输入账号" 
                v-model="user.account"
                class="custom-input"
              >
              </el-input>
            </el-form-item>
            <el-form-item prop="pwd">
              <el-input 
                prefix-icon="el-icon-lock" 
                size="medium" 
                show-password 
                placeholder="请输入密码" 
                v-model="user.pwd"
                class="custom-input"
                @keyup.enter.native="login"
              ></el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-container">
                <el-input 
                  placeholder="请输入验证码" 
                  prefix-icon="el-icon-circle-check" 
                  size="medium" 
                  v-model="user.code"
                  class="code-input"
                  @keyup.enter.native="login"
                ></el-input>
                <div class="valid-code-wrapper">
                  <valid-code @update:value="getCode" />
                </div>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                @click="login"
                :loading="loading"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
            <div class="form-footer">
              <span class="forget-password" @click="handleForgetPass">忘记密码？</span>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <el-dialog 
      title="忘记密码" 
      :visible.sync="forgetPassDialogVis" 
      width="400px"
      center
      class="forget-dialog"
    >
      <div class="dialog-content">
        <i class="el-icon-info dialog-icon"></i>
        <p>请咨询系统管理员获取密码重置帮助</p>
        <p class="contact-info">联系电话：123-4567-8910</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="forgetPassDialogVis = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import ValidCode from "@/components/ValidCode.vue";
import axios from "axios";

export default {
  name: "Login",
  components: {
    ValidCode
  },
  data() {
    // 验证码校验
    const validateCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入验证码'))
      } else if (value.toLowerCase() !== this.code) {
        callback(new Error('验证码错误'))
      } else {
        callback()
      }
    }

    return {
      loading: false,
      forgetUserForm: {},
      forgetPassDialogVis: false,
      code: '',
      user: {
        code: '',
        account: 'admin1',
        pwd: '123456',
        id: 1,
        name: 'admin',
        phoneNumber: '123456789',
        email: '10@qq.com'
      },
      rules: {
        account: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        pwd: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        code: [
          { validator: validateCode, trigger: 'blur' }
        ],
      }
    }
  },
  mounted() {
    const myDiv = document.getElementById('myDiv');
    myDiv.style.backgroundImage = `url(${require('@/assets/背景图片1.jpg')})`;
    localStorage.setItem('useradmin1', JSON.stringify(this.user))
  },
  methods: {
    handleForgetPass() {
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    getCode(code) {
      this.code = code.toLowerCase();
      console.log("验证码:", this.code)
    },
    login() {
      const user = this.user;
      const account = user.account;
      const pwd = user.pwd;
      
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          this.loading = true;
          console.log("验证码:", this.code)
          axios.post('api/login/in', { account: account, pwd: pwd })
            .then(res => {
              console.log("响应代码:", res.data.code)
              if (res.data.code === 200) {
                localStorage.setItem('useradmin1', JSON.stringify(this.user))
                this.$message.success('登录成功')
                localStorage.setItem("useradmin", JSON.stringify(res.data.data))
                this.$router.push('/dashboard')
              } else {
                this.$message.error(res.data.message)
              }
            })
            .catch(error => {
              this.$message.error('登录失败，请稍后重试')
              console.error('登录错误:', error)
            })
            .finally(() => {
              this.loading = false;
            })
        } else {
          this.$message.error("输入格式错误")
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
}

.login-wrapper {
  display: flex;
  width: 900px;
  height: 550px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

/* 左侧图片区域 */
.login-left {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.login-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.login-left:hover .login-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3) 0%, rgba(25, 118, 210, 0.6) 100%);
  z-index: 1;
}

.welcome-text {
  position: absolute;
  bottom: 60px;
  left: 40px;
  color: white;
  z-index: 2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.welcome-text h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #ffffff 0%, #e3f2fd 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-text p {
  font-size: 16px;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* 右侧表单区域 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px;
}

.login-form-container {
  width: 100%;
  max-width: 320px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a2a3a;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #1a2a3a 0%, #4a90e2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.form-header p {
  color: #6b7280;
  font-size: 14px;
}

/* 修复输入框样式 - 解决图标和内容重叠问题 */
.custom-input {
  border-radius: 10px;
}

.custom-input ::v-deep .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 12px 15px 12px 45px; /* 增加左侧内边距为图标留出空间 */
  height: 48px;
  transition: all 0.3s ease;
  background: #f8fafc;
  font-size: 14px;
}

.custom-input ::v-deep .el-input__inner:focus {
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
  background: #ffffff;
}

.custom-input ::v-deep .el-input__prefix {
  left: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
}

.custom-input ::v-deep .el-icon-user,
.custom-input ::v-deep .el-icon-lock {
  color: #6b7280;
  font-size: 18px;
}

/* 验证码输入框单独处理 */
.code-input {
  flex: 1;
}

.code-input ::v-deep .el-input__inner {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 12px 15px 12px 45px; /* 同样增加左侧内边距 */
  height: 48px;
  background: #f8fafc;
  font-size: 14px;
}

.code-input ::v-deep .el-input__prefix {
  left: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
}

.code-input ::v-deep .el-icon-circle-check {
  color: #6b7280;
  font-size: 18px;
}

/* 验证码区域 */
.code-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.valid-code-wrapper {
  flex-shrink: 0;
  height: 48px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);
  transition: all 0.3s ease;
  margin-top: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
}

.forget-password {
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s ease;
  text-decoration: none;
}

.forget-password:hover {
  color: #4a90e2;
  text-decoration: underline;
}

/* 忘记密码弹窗 */
.forget-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.forget-dialog ::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f8fafc 0%, #e5e7eb 100%);
  padding: 20px;
  text-align: center;
}

.forget-dialog ::v-deep .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #1a2a3a;
}

.dialog-content {
  text-align: center;
  padding: 30px 20px;
}

.dialog-icon {
  font-size: 48px;
  color: #4a90e2;
  margin-bottom: 20px;
}

.dialog-content p {
  color: #6b7280;
  margin-bottom: 10px;
  line-height: 1.6;
}

.contact-info {
  color: #1a2a3a !important;
  font-weight: 600;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.forget-dialog ::v-deep .el-button {
  padding: 12px 40px;
  border-radius: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    width: 90%;
    height: auto;
  }
  
  .login-left {
    height: 200px;
  }
  
  .welcome-text {
    bottom: 20px;
    left: 20px;
  }
  
  .welcome-text h2 {
    font-size: 24px;
  }
  
  .login-right {
    padding: 30px 20px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-form-container {
  animation: fadeInUp 0.6s ease;
}
</style>
<template>
  <div class="password-container">
    <el-card class="password-card">
      <!-- 上半部分保持图1设计 -->
      <div class="header-section">
        <div style="margin: 15px; text-align: center">
          <img src="../assets/软件学院院徽.png" height="124" width="125"/>
        </div>
        <div style="border: #000c17; font-size: 20px; text-align: center; margin-bottom: 20px">密码修改</div>
      </div>
      
      <!-- 下半部分按照图2设计 -->
      <el-form ref="formRef" :model="user" :rules="rules" label-width="100px" class="password-form">
        <el-form-item label="原始密码" prop="pwd">
          <el-input 
            show-password 
            v-model="pwd" 
            placeholder="......"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPwd">
          <el-input 
            show-password 
            v-model="user.newPwd" 
            placeholder="请输入新密码"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input 
            show-password 
            v-model="user.confirmPwd" 
            placeholder="请再次输入新密码"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="update" 
            class="submit-btn"
            size="medium"
          >
            确认修改
          </el-button>
          <el-button 
            @click="$router.back()" 
            class="cancel-btn"
            size="medium"
          >
            返回
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Password",
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请确认密码'))
      } else if (value !== this.user.newPwd) {
        callback(new Error('确认密码需和新密码一致'))
      } else {
        callback()
      }
    }

    return {
      pwd: null,
      user: JSON.parse(localStorage.getItem('useradmin') || '{}'),
      user1: JSON.parse(localStorage.getItem('useradmin1') || '{}'),
      rules: {
        pwd: [
          { required: true, message: '请输入原始密码', trigger: 'blur' },
        ],
        newPwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPwd: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  created() {
    this.pwd = this.user1.pwd;
  },
  methods: {
    update() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.user.pwd = this.user.newPwd;
          // 保存当前的用户信息到数据库
          this.$request.put('/user/savePwd', this.user, {
            params: {
              oldPwd: this.pwd,
              newPwd: this.user.newPwd
            }
          }).then(res => {
            if (res.code === '200') {
              // 成功更新
              this.$message.success('密码修改成功，请重新登录')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>
.password-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 改为顶部对齐 */
  background-color: #f5f7fa;
  padding: 20px;
  box-sizing: border-box;
  padding-top: 10vh; /* 向上移动，只保留10%的顶部间距 */
}

.password-card {
  width: 50%;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: none;
  margin: 0 auto; /* 水平居中 */
}

.header-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  margin: -20px -20px 20px -20px;
  padding: 20px 0;
  border-radius: 8px 8px 0 0;
}

.password-form {
  padding: 0 30px 30px;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  width: 120px;
  height: 40px;
  font-size: 14px;
  border-radius: 4px;
  margin-right: 15px;
}

.cancel-btn {
  width: 120px;
  height: 40px;
  font-size: 14px;
  border-radius: 4px;
}

/* 表单样式优化 */
::v-deep .el-form-item__label {
  font-weight: bold;
  color: #606266;
  font-size: 14px;
}

::v-deep .el-input__inner {
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.2s;
  height: 40px;
  line-height: 40px;
}

::v-deep .el-input__inner:focus {
  border-color: #409eff;
  outline: none;
}

::v-deep .el-form-item {
  margin-bottom: 22px;
}

::v-deep .el-input__inner::placeholder {
  color: #c0c4cc;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .password-card {
    width: 90%;
  }
  
  .password-form {
    padding: 0 20px 20px;
  }
  
  .password-container {
    padding-top: 5vh; /* 移动端调整顶部间距 */
  }
}
</style>
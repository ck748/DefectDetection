<template>
  <div class="person-container">
    <el-card class="person-card">
      <div class="header-section">
        <div class="logo-section">
          <img src="../assets/软件学院院徽.png" alt="软件学院院徽" class="logo"/>
        </div>
        <div class="title">钢铁检测管理系统个人中心</div>
      </div>
      
      <el-form ref="formRef" :model="user" label-width="100px" class="person-form">
        <el-form-item label="账号" prop="account">
          <el-input 
            v-model="user.account" 
            placeholder="账号"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input 
            v-model="user.name" 
            placeholder="姓名"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input 
            v-model="user.phoneNumber" 
            placeholder="11位手机号"
            size="medium"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <div class="email-input-group">
            <el-input 
              v-model="user.email" 
              placeholder="邮箱"
              size="medium"
            ></el-input>
            <el-tooltip content="密码修改请到密码修改界面进行" placement="top-end">
              <i class="el-icon-info info-icon"></i>
            </el-tooltip>
          </div>
        </el-form-item>
        
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="update" 
            class="save-btn"
            size="medium"
          >
            保存
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Person",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('useradmin1') || '{}')
    }
  },
  methods: {
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/user/saveInfo', null,{
        params:{
          account:this.user.account,
          phoneNumber:this.user.phoneNumber,
          name:this.user.name,
          email:this.user.email
        }
      }).then(res => {
        if (res.code === '200') {
          // 成功更新
          this.$message.success('保存成功')
          // 更新浏览器缓存里的用户信息
          localStorage.setItem('useradmin1', JSON.stringify(this.user))
          // 触发父级的数据更新
          this.$emit('update:user', this.user)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>

<style scoped>
.person-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: #f5f7fa;
  padding: 20px;
  box-sizing: border-box;
  padding-top: 10vh;
}

.person-card {
  width: 50%;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: none;
  margin: 0 auto;
}

.header-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  margin: -20px -20px 20px -20px;
  padding: 20px 0;
  border-radius: 8px 8px 0 0;
}

.logo-section {
  text-align: center;
  margin-bottom: 10px;
}

.logo {
  height: 124px;
  width: 125px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin: 0;
}

.person-form {
  padding: 0 30px 30px;
}

.email-input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-icon {
  color: #409eff;
  font-size: 18px;
  cursor: pointer;
  flex-shrink: 0;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
}

.save-btn {
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
  .person-card {
    width: 90%;
  }
  
  .person-form {
    padding: 0 20px 20px;
  }
  
  .person-container {
    padding-top: 5vh;
  }
  
  .email-input-group {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-icon {
    align-self: flex-end;
    margin-top: 5px;
  }
}
</style>
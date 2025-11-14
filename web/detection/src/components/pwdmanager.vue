<template>
  <div class="operator-management">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">主页</el-breadcrumb-item>
        <el-breadcrumb-item>操作秘钥管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <div class="operation-left">
        <el-button type="primary" icon="el-icon-plus" @click="AddMore">添加</el-button>
        <el-button type="danger" icon="el-icon-delete" @click="deleteMore">批量删除</el-button>
      </div>
      
      <div class="operation-right">
        <div class="batch-set">
          <el-input 
            maxlength="6" 
            show-word-limit 
            ref="loginkeyword" 
            prop="loginkeyword" 
            :rules="rules" 
            v-model="loginkeyword" 
            placeholder="批量设置登入密码(6位)" 
            style="width: 200px;"
          ></el-input>
          <el-button type="success" icon="el-icon-setting" @click="makeLoginPwd">设置</el-button>
        </div>
        
        <el-input 
          v-model="keyword" 
          placeholder="输入姓名或工号进行搜索" 
          style="width: 200px;"
          @input="search" 
          prefix-icon="el-icon-search"
          clearable
        ></el-input>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-card>
        <el-table 
          ref="list" 
          :data="filteroperators" 
          style="width: 100%"
          stripe
          v-loading="loading"
          @selection-change="handleSelectionChange"
          :border="false"
        >
          <el-table-column type="selection" width="50" align="center"></el-table-column>
          <el-table-column prop="name" label="姓名" width="120" align="center"></el-table-column>
          <el-table-column prop="jobId" label="工号" width="120" align="center"></el-table-column>
          <el-table-column label="登入密码" width="150" align="center">
            <template slot-scope="scope">
              <div class="password-input">
                <el-input 
                  show-password 
                  v-model="scope.row.loginPwd" 
                  size="small"
                ></el-input>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作密码" width="150" align="center">
            <template slot-scope="scope">
              <div class="password-input">
                <el-input 
                  show-password 
                  v-model="scope.row.opPwd" 
                  size="small"
                ></el-input>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="entryTime" label="入职时间" width="160" align="center">
            <template slot-scope="scope">
              {{ formatDate(scope.row.entryTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" width="200" align="left" show-overflow-tooltip></el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button 
                  @click="editOperator(scope.row)" 
                  type="primary" 
                  size="mini" 
                  icon="el-icon-edit"
                  class="edit-btn"
                >编辑</el-button>
                <el-button 
                  @click="deleteOperator(scope.row)" 
                  type="danger" 
                  size="mini" 
                  icon="el-icon-delete"
                  class="delete-btn"
                >删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            background
          ></el-pagination>
        </div>
      </el-card>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog 
      :visible.sync="showEditDialog" 
      title="编辑操作人员信息"
      width="600px"
      center
    >
      <el-form :model="editForm" :rules="editFormRules" label-width="100px" ref="editForm">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="工号" prop="jobId">
          <el-input v-model="editForm.jobId" placeholder="请输入工号"></el-input>
        </el-form-item>
        <el-form-item label="登入密码" prop="loginPwd">
          <el-input v-model="editForm.loginPwd" show-password placeholder="请输入登入密码"></el-input>
        </el-form-item>
        <el-form-item label="操作密码" prop="opPwd">
          <el-input v-model="editForm.opPwd" show-password placeholder="请输入操作密码"></el-input>
        </el-form-item>
        <el-form-item label="入职时间" prop="createTime">
          <el-date-picker
            v-model="editForm.createTime"
            type="date"
            placeholder="选择入职时间"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="editForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注信息"
            maxlength="255"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeEditDialog">取 消</el-button>
        <el-button type="primary" @click="saveEdit">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 添加对话框 -->
    <el-dialog 
      :visible.sync="showAddDialog"  
      title="操作人员信息添加"
      width="600px"
      center
    >
      <el-form :rules="editFormRules" :model="addForm" label-width="100px" ref="addForm">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="工号" prop="jobId">
          <el-input v-model="addForm.jobId" placeholder="请输入工号"></el-input>
        </el-form-item>
        <el-form-item label="登入密码" prop="loginPwd">
          <el-input v-model="addForm.loginPwd" show-password placeholder="请输入登入密码"></el-input>
        </el-form-item>
        <el-form-item label="操作密码" prop="opPwd">
          <el-input v-model="addForm.opPwd" show-password placeholder="请输入操作密码"></el-input>
        </el-form-item>
        <el-form-item label="入职时间" prop="createTime">
          <el-date-picker
            v-model="addForm.createTime"
            type="date"
            placeholder="选择入职时间"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="addForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注信息"
            maxlength="255"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showAddDialog = false">取 消</el-button>
        <el-button type="primary" @click="AddMoreTo">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";
import dayjs from 'dayjs';

export default {
  data() {
    return {
      user: JSON.parse(localStorage.getItem('useradmin1') || '{}'),
      page: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      showAddDialog: false,
      addForm: {
        name: null,
        jobId: null,
        loginPwd: null,
        opPwd: null,
        showLoginPwd: false,
        showOpPwd: false,
        createTime: new Date(),
        remark: null,
        createName: null
      },
      loginkeyword: null,
      keyword: null,
      operators: [],
      selectedOperators: [],
      editForm: {
        id: null,
        name: null,
        jobId: null,
        loginPwd: null,
        opPwd: null,
        showLoginPwd: false,
        showOpPwd: false,
        createTime: new Date(),
        remark: null,
        createName: null
      },
      showEditDialog: false,
      mainid: 0,
      rules: {
        loginkeyword: [
          { required: true, message: '请输入修改的操作密码', trigger: 'blur' },
          { min: 6, max: 6, message: '操作密码固定长度为6', trigger: 'blur' }
        ]
      },
      editFormRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        jobId: [
          { required: true, message: '请输入工号', trigger: 'blur' },
          { pattern: /^\d{1,5}$/, message: '工号格式不正确，长度不超过5位数字', trigger: 'blur' }
        ],
        loginPwd: [
          { required: true, message: '请输入登入密码', trigger: 'blur' }
        ],
        opPwd: [
          { required: true, message: '请输入操作密码', trigger: 'blur' },
          { min: 6, max: 6, message: '操作密码固定长度为6', trigger: 'blur' }
        ],
        createTime: [{ required: true, message: '请选择入职时间', trigger: 'change' }],
        remark: [
          { max: 255, message: '备注最长255个字符', trigger: 'blur' }
        ]
      }
    };
  },
  mounted() {
    this.fetchOperator();
  },
  computed: {
    filteroperators() {
      if (!this.keyword) {
        return this.operators;
      } else {
        let keyword1 = this.keyword.toString().toLowerCase();
        return this.operators.filter(operator => {
          return (
            (operator.name && typeof operator.name === 'string' && operator.name.toLowerCase().includes(keyword1)) ||
            (operator.jobId && operator.jobId.toString().toLowerCase().includes(keyword1))
          );
        });
      }
    }
  },
  methods: {
    // 格式化日期显示
    formatDate(date) {
      if (!date) return '';
      // 如果日期包含时间部分，只显示日期部分
      if (date.includes(' ')) {
        return date.split(' ')[0];
      }
      return date;
    },
    
    handleSelectionChange(selection) {
      this.selectedOperators = selection;
    },
    
    makeLoginPwd() {
      if (!this.loginkeyword || this.loginkeyword.length !== 6) {
        this.$message.warning('请输入6位登入密码');
        return;
      }

      this.$confirm('确定要进行批量设置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.loading = true;
        const loginPwd = this.loginkeyword;
        axios.put('api/sysManage/key/batchPwd', null, {
          params: {
            loginPwd: loginPwd,
            page: this.page,
            pageSize: this.pageSize
          }
        })
        .then(response => {
          this.loading = false;
          if (response.data.code === 200) {
            this.$message.success("设置成功");
            this.fetchOperator();
          } else {
            this.$message.error("设置失败");
          }
        })
        .catch(error => {
          this.loading = false;
          this.$message.error("设置失败");
        });
      }).catch(() => {
        this.$message.info("取消设置");
      });
    },
    
    AddMore() {
      this.showAddDialog = true;
      this.addForm = {
        name: null,
        jobId: null,
        loginPwd: null,
        opPwd: null,
        showLoginPwd: false,
        showOpPwd: false,
        createTime: new Date(),
        remark: null,
        createName: null
      };
    },
    
    AddMoreTo() {
      this.$refs.addForm.validate((valid) => {
        if (valid) {
          let createTime = this.addForm.createTime;
          if (createTime && createTime.length === 10) {
            this.addForm.createTime = createTime + " 00:00:00";
          }

          axios.post('api/sysManage/key/add', this.addForm)
          .then(response => {
            if (response.data.code === 200) {
              this.$message.success("添加成功");
              this.showAddDialog = false;
              this.fetchOperator();
            } else {
              this.$message.error(response.data.message);
            }
          })
          .catch(error => {
            console.error(error);
            this.$message.error("添加数据失败");
          });
        }
      });
    },
    
    search() {
      this.keyword = this.keyword ? this.keyword.trim() : '';
    },
    
    fetchOperator() {
      this.loading = true;
      axios.get("api/sysManage/key/info", {
        params: {
          page: this.page,
          pageSize: this.pageSize
        }
      })
      .then(response => {
        this.loading = false;
        if (response.data.code === 200) {
          this.total = response.data.data[0].totals;
          this.operators = response.data.data;
          this.operators.forEach(operator => {
            operator.showLoginPwd = false;
            operator.showOpPwd = false;
          });
        }
      })
      .catch(error => {
        this.loading = false;
        console.error(error);
        this.$message.error("获取数据失败");
      });
    },
    
    editOperator(row) {
      this.editForm = { ...row };
      this.mainid = row.id;
      this.showEditDialog = true;
    },
    
    saveEdit() {
      this.$refs.editForm.validate((valid) => {
        if (valid) {
          let createTime = this.editForm.createTime;
          if (createTime && createTime.length === 10) {
            this.editForm.createTime = createTime + " 00:00:00";
          }

          axios.put("api/sysManage/key/update", this.editForm, {
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then(response => {
            if (response.data.code === 200) {
              this.$message.success("修改成功");
              this.showEditDialog = false;
              this.fetchOperator();
            } else {
              this.$message.error("编辑失败");
            }
          })
          .catch(error => {
            console.error('错误信息为', error);
            this.$message.error("编辑失败");
          });
        }
      });
    },
    
    closeEditDialog() {
      this.showEditDialog = false;
    },
    
    deleteOperator(row) {
      this.$confirm('确定要删除该操作员吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const id = row.id;
        axios.delete('api/sysManage/key/delete', {
          data: [id]
        })
        .then(response => {
          if (response.data.code === 200) {
            this.$message.success("删除成功");
            this.fetchOperator();
          } else {
            this.$message.error("删除失败");
          }
        })
        .catch((error) => {
          console.log(error);
          this.$message.error("删除失败");
        });
      }).catch(() => {
        this.$message.info("取消删除");
      });
    },
    
    deleteMore() {
      if (this.selectedOperators.length === 0) {
        this.$message.warning("请先勾选要删除的数据");
        return;
      }

      this.$confirm("确认删除选中的操作员吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.loading = true;
        const selectedIds = this.selectedOperators.map(operator => operator.id);
        axios.delete('api/sysManage/key/delete', { data: selectedIds })
        .then(response => {
          this.loading = false;
          if (response.data.code === 200) {
            this.$message.success("批量删除成功");
            this.fetchOperator();
            this.selectedOperators = [];
          } else {
            this.$message.error(response.data.message);
          }
        }).catch(error => {
          this.loading = false;
          this.$message.error("批量删除失败");
        });
      }).catch(() => {
        this.$message.info("取消删除");
      });
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchOperator();
    },
    
    handleCurrentChange(val) {
      this.page = val;
      this.fetchOperator();
    }
  }
};
</script>

<style scoped>
.operator-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 0 10px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.operation-left {
  display: flex;
  gap: 10px;
}

.operation-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.batch-set {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.password-input {
  display: flex;
  justify-content: center;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
  align-items: center;
}

.edit-btn,
.delete-btn {
  margin: 0;
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  background-color: #409eff;
  border-color: #409eff;
  transform: translateY(-1px);
}

.delete-btn:hover {
  background-color: #f56c6c;
  border-color: #f56c6c;
  transform: translateY(-1px);
}

/* 完全移除表格所有列之间的边框线 */
::v-deep .el-table {
  border: none;
}

::v-deep .el-table th,
::v-deep .el-table td {
  border-right: none !important;
  border-bottom: 1px solid #ebeef5;
}

::v-deep .el-table--border {
  border: none;
}

::v-deep .el-table--border th,
::v-deep .el-table--border td {
  border-right: none !important;
}

::v-deep .el-table th.gutter {
  border-right: none !important;
}

::v-deep .el-table th {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
  border-bottom: 2px solid #e8e8e8;
}

::v-deep .el-table .cell {
  padding: 12px 8px;
  border-right: none !important;
}

::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: #fafafa;
}

::v-deep .el-table__body tr:hover > td {
  background-color: #f5f7fa !important;
}

::v-deep .el-dialog__header {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  border-radius: 8px 8px 0 0;
}

::v-deep .el-dialog__title {
  color: #303133;
  font-weight: 600;
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

::v-deep .el-input__inner,
::v-deep .el-textarea__inner {
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: border-color 0.3s;
}

::v-deep .el-input__inner:focus,
::v-deep .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

@media (max-width: 768px) {
  .operation-bar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .operation-right {
    flex-direction: column;
    gap: 10px;
  }
  
  .batch-set {
    width: 100%;
  }
  
  .batch-set .el-input {
    flex: 1;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
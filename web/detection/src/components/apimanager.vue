<template>
  <div class="api-management">
    <div class="header">
      <h1><i class="el-icon-s-management"></i> API管理</h1>
      <div class="operation-buttons">
        <el-button type="primary" @click="handleAdd" icon="el-icon-plus">添加</el-button>
        <el-button type="primary" @click="handleAddmore" icon="el-icon-document-add">批量添加</el-button>
        <el-button type="danger" @click="deleteMore" icon="el-icon-delete">批量删除</el-button>
        <span v-if="selectedRows.length > 0" class="selection-count">已选择 {{ selectedRows.length }} 项</span>
      </div>
    </div>
    
    <div class="table-container">
      <el-table 
          :data="tableData" 
          style="width: 100%" 
          @selection-change="handleSelectionChange"
          stripe>
        <el-table-column type="selection" width="50" align="center"></el-table-column>
        <el-table-column type="index" label="编号" width="80" align="center"></el-table-column>
        <el-table-column prop="createName" label="创建者" width="100" align="center"></el-table-column>
        <el-table-column prop="apiKey" label="API密钥" min-width="180">
          <template slot-scope="scope">
            <span class="api-key">{{ scope.row.apiKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expirationDate" label="到期日期" width="120" align="center">
          <template slot-scope="scope">
            <span>{{ calculateExpirationDate(scope.row.validityPeriod) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validityPeriod" label="有效期(天)" width="110" align="center">
          <template slot-scope="scope">
            <span>{{ calculateExpirationDate1(scope.row.validityPeriod) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validityTimes" label="有效次数" width="100" align="center">
          <template slot-scope="scope">
            <span>{{ calculateExpirationDate2(scope.row.validityTimes) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="permissionLevel" label="权限等级" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.permissionLevel === '1'" type="primary" class="permission-tag">权限1</el-tag>
            <el-tag v-else-if="scope.row.permissionLevel === '2'" type="success" class="permission-tag">权限2</el-tag>
            <el-tag v-else-if="scope.row.permissionLevel === '3'" type="warning" class="permission-tag">权限3</el-tag>
            <el-tag v-else type="info" class="permission-tag">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 1" class="status-badge status-enabled">启用</span>
            <span v-else class="status-badge status-disabled">禁用</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button size="mini" type="danger" @click="handleDelete(scope.$index, scope.row)" icon="el-icon-delete">删除</el-button>
              <el-button size="mini" type="warning" @click="handleedit(scope.$index, scope.row)" icon="el-icon-edit">修改</el-button>
              <el-button size="mini" type="primary" @click="handleCopy(scope.$index, scope.row)" icon="el-icon-document-copy">复制</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page"
            :page-sizes="[10, 20, 30, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </div>

    <!-- 添加数据对话框 -->
    <el-dialog title="添加API密钥" :visible.sync="dialogVisible" width="500px" @closed="resetForm('formData')">
      <el-form ref="formData" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="有效期(天)" prop="validityPeriod">
          <el-input v-model.number="formData.validityPeriod" placeholder="输入有效期天数，-1表示无限期" type="number"></el-input>
        </el-form-item>
        <el-form-item label="有效次数" prop="validityTimes">
          <el-input v-model.number="formData.validityTimes" placeholder="输入有效次数，-1表示无限次" type="number"></el-input>
        </el-form-item>
        <el-form-item label="权限等级" prop="permissionLevel">
          <el-select v-model="formData.permissionLevel" placeholder="请选择权限等级">
            <el-option label="权限1" value="1"></el-option>
            <el-option label="权限2" value="2"></el-option>
            <el-option label="权限3" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model.number="formData.status" placeholder="请选择状态">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addData">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 批量添加数据对话框 -->
    <el-dialog title="批量添加API密钥" :visible.sync="moreAdddialogVisible" width="500px" @closed="resetForm('formDataMore')">
      <el-form ref="formDataMore" :rules="formRules" :model="formDataMore" label-width="100px">
        <el-form-item label="数量" prop="num">
          <el-input v-model.number="formDataMore.num" placeholder="请输入要生成的API密钥数量" type="number"></el-input>
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validityPeriod">
          <el-input v-model.number="formDataMore.validityPeriod" placeholder="输入有效期天数，-1表示无限期" type="number"></el-input>
        </el-form-item>
        <el-form-item label="有效次数" prop="validityTimes">
          <el-input v-model.number="formDataMore.validityTimes" placeholder="输入有效次数，-1表示无限次" type="number"></el-input>
        </el-form-item>
        <el-form-item label="权限等级" prop="permissionLevel">
          <el-select v-model="formDataMore.permissionLevel" placeholder="请选择权限等级">
            <el-option label="权限1" value="1"></el-option>
            <el-option label="权限2" value="2"></el-option>
            <el-option label="权限3" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model.number="formDataMore.status" placeholder="请选择状态">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formDataMore.remark" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="moreAdddialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addDatamore">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑数据对话框 -->
    <el-dialog title="编辑API密钥" :visible.sync="EditdialogVisible" width="500px" @closed="resetForm('EditformData')">
      <el-form ref="EditformData" :model="EditformData" :rules="formRules" label-width="100px">
        <el-form-item label="有效期(天)" prop="validityPeriod">
          <el-input v-model.number="EditformData.validityPeriod" placeholder="输入有效期天数，-1表示无限期" type="number"></el-input>
        </el-form-item>
        <el-form-item label="有效次数" prop="validityTimes">
          <el-input v-model.number="EditformData.validityTimes" placeholder="输入有效次数，-1表示无限次" type="number"></el-input>
        </el-form-item>
        <el-form-item label="权限等级" prop="permissionLevel">
          <el-select v-model="EditformData.permissionLevel" placeholder="请选择权限等级">
            <el-option label="权限1" value="1"></el-option>
            <el-option label="权限2" value="2"></el-option>
            <el-option label="权限3" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model.number="EditformData.status" placeholder="请选择状态">
            <el-option label="禁用" :value="0"></el-option>
            <el-option label="启用" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="EditformData.remark" placeholder="请输入备注信息"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="EditdialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="EditData">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ApiManagement',
  data() {
    return {
      page: 1,
      pageSize: 10,
      total: 2,
      user: JSON.parse(localStorage.getItem('useradmin') || '{"name": "管理员"}'),
      formRules: {
        num: [
          { required: true, message: '数量不能为空', trigger: 'blur' },
        ],
        validityPeriod: [
          { required: true, message: '有效期不能为空', trigger: 'blur' },
        ],
        validityTimes: [
          { required: true, message: '有效次数不能为空', trigger: 'blur' },
        ],
        permissionLevel: [
          { required: true, message: '权限等级不能为空', trigger: 'change' },
        ],
        status: [
          { required: true, message: '状态不能为空', trigger: 'change' },
        ]
      },
      dialogVisible: false,
      moreAdddialogVisible: false,
      EditdialogVisible: false,
      tableData: [
        {
          id: 1,
          createName: '管理员1',
          apiKey: 'APTRET1',
          validityPeriod: -1,
          validityTimes: -1,
          permissionLevel: '1',
          status: 1,
          remark: ''
        },
        {
          id: 2,
          createName: '管理员2',
          apiKey: 'APTRET2',
          validityPeriod: -1,
          validityTimes: -1,
          permissionLevel: '2',
          status: 1,
          remark: ''
        },
        {
          id: 3,
          createName: '管理员3',
          apiKey: 'APTRET3',
          validityPeriod: 90,
          validityTimes: 500,
          permissionLevel: '3',
          status: 0,
          remark: '测试权限3显示'
        }
      ],
      formData: {
        validityPeriod: '',
        validityTimes: '',
        permissionLevel: '1',
        status: 1,
        remark: '',
        createName: '',
      },
      formDataMore: {
        num: null,
        validityPeriod: '',
        validityTimes: '',
        permissionLevel: '1',
        status: 1,
        remark: '',
        createName: '',
      },
      selectedRows: [],
      EditdialogVisible: false,
      currentEditIndex: -1,
      EditformData: {
        id: null,
        validityPeriod: '',
        validityTimes: '',
        permissionLevel: '1',
        status: 1,
        remark: '',
        createName: '',
      },
    };
  },
  mounted() {
    this.fetchData();
  },
  methods: {
    handleedit(index, row){
      this.EditformData = Object.assign({}, row);
      this.currentEditIndex = index;
      this.EditdialogVisible = true;
    },
    async EditData() {
      try {
        this.$refs['EditformData'].validate(async (valid) => {
          if (valid) {
            // 模拟API调用
            const response = await axios.patch(`api/sysManage/api/update`, this.EditformData);
            if (response.data.code === 200) {
              // 更新本地数据
              this.tableData[this.currentEditIndex] = Object.assign({}, this.EditformData);
              this.EditdialogVisible = false;
              this.$message.success("保存编辑数据成功");
              this.fetchData();
            } else {
              this.$message.error("保存编辑数据失败");
            }
          } else {
            this.$message.warning('请填写完整信息！');
          }
        });
      } catch (error) {
        console.error('编辑错误:', error);
        this.$message.error("保存编辑数据失败: " + error.message);
      }
    },
    calculateExpirationDate(validityPeriod) {
      if (validityPeriod < 0) {
        return '无限制';
      } else {
        const today = new Date();
        const expirationDate = new Date(today.getTime() + validityPeriod * 24 * 60 * 60 * 1000);
        return expirationDate.toISOString().slice(0, 10);
      }
    },
    calculateExpirationDate1(validityPeriod) {
      if (validityPeriod < 0) {
        return '无限制';
      } else {
        return validityPeriod;
      }
    },
    calculateExpirationDate2(validityTimes) {
      if (validityTimes < 0) {
        return '无限制';
      } else {
        return validityTimes;
      }
    },
    fetchData() {
      // 模拟数据获取
      this.total = this.tableData.length;
    },
    handleDelete(index, row) {
      this.$confirm('此操作将永久删除该API密钥, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        try {
          // 模拟API调用
          axios.delete("api/sysManage/api/delete", {
            data: [row.id]
          })
          .then(response => {
            if (response.data.code === 200) {
              this.tableData.splice(index, 1);
              this.fetchData();
              this.$message.success("删除成功");
            } else {
              this.$message.error("删除失败: " + (response.data.message || '未知错误'));
            }
          })
          .catch(error => {
            console.error('删除错误:', error);
            this.$message.error("删除失败: " + (error.response?.data?.message || error.message));
          });
        } catch (error) {
          console.error('删除异常:', error);
          this.$message.error("删除失败: " + error.message);
        }
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },
    handleCopy(index, row) {
      if (!row) {
        this.$message.warning("没有找到要复制的项");
        return;
      }
      const apiKey = row.apiKey;
      
      // 创建临时输入框
      const input = document.createElement('input');
      input.value = apiKey;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      
      this.$message.success("已成功复制到剪贴板");
    },
    handleAdd(){
      this.dialogVisible = true;
    },
    addData() {
      this.$refs['formData'].validate((valid) => {
        if (valid) {
          let name = this.user.name;
          this.formData.createName = name;
          
          // 生成新的API密钥
          const newApiKey = 'API_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
          
          // 创建新数据
          const newData = {
            id: this.tableData.length + 1,
            createName: this.formData.createName,
            apiKey: newApiKey,
            validityPeriod: this.formData.validityPeriod,
            validityTimes: this.formData.validityTimes,
            permissionLevel: this.formData.permissionLevel,
            status: this.formData.status,
            remark: this.formData.remark
          };
          
          // 模拟API调用
          axios.post('api/sysManage/api/add', newData)
          .then(response => {
            if (response.data.code === 200) {
              // 添加到表格数据
              this.tableData.push(newData);
              this.fetchData();
              this.$message.success("添加成功");
              this.dialogVisible = false;
              this.resetForm('formData');
            } else {
              this.$message.error("添加失败");
            }
          })
          .catch(error => {
            console.error('添加错误:', error);
            this.$message.error("添加失败: " + error.message);
          });
        } else {
          this.$message.warning('请填写完整信息！');
        }
      });
    },
    handleAddmore(){
      this.moreAdddialogVisible = true;
    },
    addDatamore() {
      this.$refs['formDataMore'].validate((valid) => {
        if (valid) {
          let name = this.user.name;
          this.formDataMore.createName = name;
          
          const newItems = [];
          for (let i = 0; i < this.formDataMore.num; i++) {
            const newApiKey = 'BATCH_API_' + Date.now() + '_' + i + '_' + Math.random().toString(36).substr(2, 6);
            newItems.push({
              id: this.tableData.length + i + 1,
              createName: this.formDataMore.createName,
              apiKey: newApiKey,
              validityPeriod: this.formDataMore.validityPeriod,
              validityTimes: this.formDataMore.validityTimes,
              permissionLevel: this.formDataMore.permissionLevel,
              status: this.formDataMore.status,
              remark: this.formDataMore.remark
            });
          }
          
          // 模拟API调用
          axios.post('api/sysManage/api/add', newItems)
          .then(response => {
            if (response.data.code === 200) {
              // 批量添加到表格数据
              this.tableData.push(...newItems);
              this.fetchData();
              this.moreAdddialogVisible = false;
              this.$message.success(`批量添加成功，共添加 ${this.formDataMore.num} 条数据`);
              this.resetForm('formDataMore');
            } else {
              this.$message.error("批量添加失败");
            }
          })
          .catch(error => {
            console.error('批量添加错误:', error);
            this.$message.error("批量添加失败: " + error.message);
          });
        } else {
          this.$message.warning('请填写完整信息！');
        }
      });
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    deleteMore() {
      if (this.selectedRows.length === 0) {
        this.$message.warning("请先勾选要删除的数据");
        return;
      }
      const ids = this.selectedRows.map(item => item.id);
      this.$confirm(`此操作将永久删除选中的 ${this.selectedRows.length} 个API密钥, 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete("api/sysManage/api/delete", {
          data: ids
        })
        .then(response => {
          if (response.data.code === 200) {
            // 从表格数据中移除选中的项
            this.tableData = this.tableData.filter(item => !ids.includes(item.id));
            this.fetchData();
            this.selectedRows = [];
            this.$message.success("批量删除成功");
          } else {
            this.$message.error("批量删除失败");
          }
        })
        .catch(error => {
          console.error(error);
          this.$message.error("批量删除失败");
        });
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchData();
    },
    handleCurrentChange(val) {
      this.page = val;
      this.fetchData();
    },
    resetForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
      if (formName === 'formData') {
        this.formData = {
          validityPeriod: '',
          validityTimes: '',
          permissionLevel: '1',
          status: 1,
          remark: '',
          createName: '',
        };
      } else if (formName === 'formDataMore') {
        this.formDataMore = {
          num: null,
          validityPeriod: '',
          validityTimes: '',
          permissionLevel: '1',
          status: 1,
          remark: '',
          createName: '',
        };
      }
    }
  }
};
</script>

<style scoped>
.api-management {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.header {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-weight: 600;
  font-size: 24px;
  color: #303133;
  display: flex;
  align-items: center;
}

.header h1 i {
  margin-right: 12px;
  font-size: 28px;
  color: #409EFF;
}

.operation-buttons {
  display: flex;
  gap: 15px;
  align-items: center;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-enabled {
  background-color: #f0f9ff;
  color: #409EFF;
}

.status-disabled {
  background-color: #fef0f0;
  color: #F56C6C;
}

.api-key {
  font-family: 'Courier New', monospace;
  background: #f5f7fa;
  padding: 6px 10px;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
  font-weight: 500;
  color: #409EFF;
}

.permission-tag {
  border-radius: 4px;
  font-weight: 500;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.selection-count {
  background: #409EFF;
  color: white;
  padding: 5px 12px;
  border-radius: 12px;
  font-size: 14px;
  margin-left: 15px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  flex-shrink: 0;
  min-width: 60px;
}

/* 移除横向滚动条 */
.el-table__body-wrapper::-webkit-scrollbar {
  display: none;
}

.el-table__body-wrapper {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
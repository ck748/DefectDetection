<template>
  <div class="operator-management">
    <!-- 头部区域 -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-lock"></i></span>
          <h2 class="page-title">操作密钥与人员管理</h2>
          <span class="title-tag">操作员权限</span>
        </div>
        <p class="page-desc">管理产线质检人员凭证、工控上位机操作密码与系统登录凭据</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="medium" icon="el-icon-plus" @click="AddMore">添加人员</el-button>
        <el-button
          type="danger"
          plain
          size="medium"
          icon="el-icon-delete"
          :disabled="selectedOperators.length === 0"
          @click="deleteMore"
        >
          批量删除<span v-if="selectedOperators.length > 0"> ({{ selectedOperators.length }})</span>
        </el-button>
      </div>
    </div>

    <!-- 运行指标透视条 (Stats Bar) -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">操作员总数</span>
          <i class="el-icon-user stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ totalOperators }}</span>
          <span class="unit">人</span>
        </div>
        <div class="stat-foot">已登记的质检操作档案</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">已配操作密码</span>
          <span class="status-badge success"><span class="dot"></span>已授权</span>
        </div>
        <div class="stat-value text-success">
          <span class="num">{{ hasOpPwdCount }}</span>
          <span class="unit">人</span>
        </div>
        <div class="stat-foot">具备上位机机台操作权</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">已配登录密码</span>
          <i class="el-icon-key stat-icon"></i>
        </div>
        <div class="stat-value text-primary">
          <span class="num">{{ hasLoginPwdCount }}</span>
          <span class="unit">人</span>
        </div>
        <div class="stat-foot">支持 Web 与车间登录</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">当前勾选人员</span>
          <i class="el-icon-check stat-icon"></i>
        </div>
        <div class="stat-value text-warning">
          <span class="num">{{ selectedCount }}</span>
          <span class="unit">人</span>
        </div>
        <div class="stat-foot">批量密码或删除目标</div>
      </div>
    </div>

    <!-- 内容区域 (Content Section - 对应红框3自适应充满) -->
    <div class="content-box">
      <!-- 筛选与操作工具栏 -->
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-input
            v-model="keyword"
            size="small"
            placeholder="搜索操作人员姓名 / 工号..."
            prefix-icon="el-icon-search"
            clearable
            class="search-input"
            @input="search"
            @clear="search"
          ></el-input>
          <el-button size="small" icon="el-icon-refresh-left" @click="keyword = ''; search()">重置</el-button>
        </div>
        <div class="toolbar-right">
          <div class="batch-set-box">
            <el-input
              v-model="loginkeyword"
              maxlength="6"
              show-password
              placeholder="批量设置登入密码(6位)"
              size="small"
              autocomplete="new-password"
              name="batch_operator_new_password_no_autofill"
              class="pwd-input"
            ></el-input>
            <el-button
              type="primary"
              plain
              size="small"
              icon="el-icon-setting"
              :disabled="!loginkeyword || loginkeyword.length !== 6 || selectedOperators.length === 0"
              @click="makeLoginPwd"
            >应用设置</el-button>
          </div>
          <el-tooltip content="刷新人员数据" placement="top">
            <el-button size="small" icon="el-icon-refresh" circle @click="fetchOperator"></el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table
          :height="tableHeight"
          ref="list"
          :data="filteroperators"
          v-loading="loading"
          element-loading-text="正在加载操作员档案..."
          stripe
          style="width: 100%"
          class="enterprise-table"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" align="center"></el-table-column>
          <el-table-column label="序号" width="60" align="center">
            <template slot-scope="scope">
              <span>{{ (page - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" align="center">
            <template slot-scope="scope">
              <span class="user-badge"><i class="el-icon-user"></i> {{ scope.row.name || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="jobId" label="工号" width="120" align="center">
            <template slot-scope="scope">
              <span class="code-badge">{{ scope.row.jobId || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="登入密码" width="150" align="center">
            <template slot-scope="scope">
              <div class="password-cell">
                <el-input size="small" show-password v-model="scope.row.loginPwd" placeholder="未设置"></el-input>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作密码" width="150" align="center">
            <template slot-scope="scope">
              <div class="password-cell">
                <el-input size="small" show-password v-model="scope.row.opPwd" placeholder="未设置"></el-input>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="入职时间" width="170" align="center">
            <template slot-scope="scope">
              <span class="time-text"><i class="el-icon-time"></i> {{ formatDateTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注说明" min-width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <span class="remark-text">{{ scope.row.remark || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template slot-scope="scope">
              <div class="action-links">
                <el-button type="text" size="small" icon="el-icon-edit" @click="editOperator(scope.row)">编辑</el-button>
                <el-popconfirm
                  title="确定删除此操作人员档案？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  icon="el-icon-warning"
                  icon-color="#f56c6c"
                  @confirm="deleteOperator(scope.row)"
                >
                  <el-button slot="reference" type="text" size="small" class="delete-btn" icon="el-icon-delete">删除</el-button>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页区域 -->
      <div class="pagination-footer">
        <div class="pagination-total">
          共 <span class="total-count">{{ displayTotal }}</span> 名操作人员，当前第 {{ page }} / {{ Math.ceil(displayTotal / pageSize) || 1 }} 页
        </div>
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="page"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="displayTotal"
        ></el-pagination>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      :visible.sync="showEditDialog"
      title="编辑操作人员信息"
      width="560px"
      custom-class="enterprise-dialog"
      :close-on-click-modal="false"
      @close="closeEditDialog"
    >
      <div class="dialog-tip-banner">
        <i class="el-icon-info"></i>
        <span>修改产线操作人员的基础资料、系统登入密码与上位机机台操作密码</span>
      </div>
      <el-form :model="editForm" :rules="editFormRules" label-width="90px" ref="editForm" size="small">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入操作人员姓名"></el-input>
        </el-form-item>
        <el-form-item label="工号" prop="jobId">
          <el-input v-model="editForm.jobId" placeholder="请输入工号 (不超过5位数字)"></el-input>
        </el-form-item>
        <el-form-item label="登入密码" prop="loginPwd">
          <el-input v-model="editForm.loginPwd" show-password placeholder="请输入Web/系统登入密码"></el-input>
        </el-form-item>
        <el-form-item label="操作密码" prop="opPwd">
          <el-input v-model="editForm.opPwd" show-password maxlength="6" placeholder="请输入6位操作密码"></el-input>
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
        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注说明"
            maxlength="255"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="closeEditDialog">取 消</el-button>
        <el-button size="small" type="primary" @click="saveEdit">保 存</el-button>
      </span>
    </el-dialog>

    <!-- 添加对话框 -->
    <el-dialog
      :visible.sync="showAddDialog"
      title="新增操作人员档案"
      width="560px"
      custom-class="enterprise-dialog"
      :close-on-click-modal="false"
      @close="showAddDialog = false"
    >
      <div class="dialog-tip-banner">
        <i class="el-icon-info"></i>
        <span>登记新入职或新授权的产线质检人员及机台操作凭证</span>
      </div>
      <el-form :rules="editFormRules" :model="addForm" label-width="90px" ref="addForm" size="small">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入操作人员姓名"></el-input>
        </el-form-item>
        <el-form-item label="工号" prop="jobId">
          <el-input v-model="addForm.jobId" placeholder="请输入工号 (不超过5位数字)"></el-input>
        </el-form-item>
        <el-form-item label="登入密码" prop="loginPwd">
          <el-input v-model="addForm.loginPwd" show-password placeholder="请输入Web/系统登入密码"></el-input>
        </el-form-item>
        <el-form-item label="操作密码" prop="opPwd">
          <el-input v-model="addForm.opPwd" show-password maxlength="6" placeholder="请输入6位操作密码"></el-input>
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
        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注说明"
            maxlength="255"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="showAddDialog = false">取 消</el-button>
        <el-button size="small" type="primary" @click="AddMoreTo">创 建</el-button>
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
      tableHeight: 380,
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
      loginkeyword: '',
      keyword: '',
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
      tableHeight: 400, // 添加表格高度控制
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
    this.keyword = '';
    this.loginkeyword = '';
    this.fetchOperator();
    setTimeout(() => {
      this.keyword = '';
      this.loginkeyword = '';
    }, 150);
    this.$nextTick(() => {
      setTimeout(() => {
        this.calculateTableHeight();
      }, 100);
    });
    window.addEventListener('resize', this.calculateTableHeight);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calculateTableHeight);
  },
  computed: {
    totalOperators() {
      return this.total || (Array.isArray(this.operators) ? this.operators.length : 0);
    },
    hasOpPwdCount() {
      if (!Array.isArray(this.operators)) return 0;
      return this.operators.filter(i => !!i.opPwd).length;
    },
    hasLoginPwdCount() {
      if (!Array.isArray(this.operators)) return 0;
      return this.operators.filter(i => !!i.loginPwd).length;
    },
    selectedCount() {
      return Array.isArray(this.selectedOperators) ? this.selectedOperators.length : 0;
    },
    filteroperators() {
      const kw = (this.keyword || '').toString().trim().toLowerCase();
      if (!kw) {
        return this.operators;
      }
      return this.operators.filter(operator => {
        const name = (operator.name || '').toString().toLowerCase();
        const jobId = (operator.jobId !== undefined && operator.jobId !== null ? operator.jobId : '').toString().toLowerCase();
        return name.includes(kw) || jobId.includes(kw);
      });
    },
    displayTotal() {
      if (!this.keyword || !this.keyword.trim()) {
        return this.total;
      }
      return this.filteroperators.length;
    }
  },
  methods: {
    // 计算表格高度 - 自适应充满内容卡片区域
    calculateTableHeight() {
      this.$nextTick(() => {
        const container = this.$el ? this.$el.querySelector('.table-container') : document.querySelector('.table-container');
        if (container && container.clientHeight > 100) {
          this.tableHeight = container.clientHeight;
        } else {
          const vh = window.innerHeight || 800;
          this.tableHeight = Math.max(vh - 440, 280);
        }
      });
    },
    
    // 格式化日期时间显示 - 确保在一行显示完整时间
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      
      // 处理 ISO 格式的时间，如 "2025-11-09T15:27:28"
      if (dateTime.includes('T')) {
        const datePart = dateTime.split('T')[0];
        const timePart = dateTime.split('T')[1];
        // 显示完整的日期和时间在一行，替换T为空格
        return `${datePart} ${timePart}`;
      }
      
      // 处理其他格式的时间
      return dateTime;
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
        
        // 修复：直接更新前端数据，确保用户立即看到变化
        this.filteroperators.forEach(row => {
          row.loginPwd = loginPwd;
        });
        
        // 同时发送请求到后端
        axios.put('api/sysManage/key/batchPwd', null, {
          params: {
            loginPwd: loginPwd,
            page: this.page,
            pageSize: this.pageSize
          }
        })
        .then(response => {
          this.loading = false;
          if (response.data && (response.data.code === 200 || response.data.code === 0)) {
            this.$message.success("设置成功");
            this.fetchOperator();
          } else {
            this.$message.error((response.data && response.data.message) || "设置失败");
          }
        })
        .catch(error => {
          this.loading = false;
          console.error(error);
          this.$message.error("设置失败，请检查网络或登录状态");
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
          let submitData = { ...this.addForm };
          let ct = submitData.createTime;
          if (ct) {
            if (typeof ct === 'string' && ct.length === 10) {
              submitData.createTime = ct + "T00:00:00";
            } else if (typeof ct === 'string' && ct.includes(' ')) {
              submitData.createTime = ct.replace(' ', 'T');
            } else if (ct instanceof Date) {
              submitData.createTime = dayjs(ct).format('YYYY-MM-DDTHH:mm:ss');
            }
          }

          axios.post('api/sysManage/key/add', submitData)
          .then(response => {
            if (response.data && (response.data.code === 200 || response.data.code === 0)) {
              this.$message.success("添加成功");
              this.showAddDialog = false;
              this.fetchOperator();
            } else {
              this.$message.error((response.data && response.data.message) || "添加失败");
            }
          })
          .catch(error => {
            console.error(error);
            const msg = error.response && error.response.data && error.response.data.message
              ? error.response.data.message
              : "添加请求失败，请检查网络或登录状态";
            this.$message.error(msg);
          });
        }
      });
    },
    
    search() {
      this.page = 1;
    },
    
    fetchOperator() {
      this.loading = true;
      axios.get('api/sysManage/key/info', {
        params: {
          page: this.page,
          pageSize: this.pageSize
        }
      })
      .then(response => {
        this.loading = false;
        if (response.data && (response.data.code === 200 || response.data.code === 0)) {
          const data = response.data.data;
          if (Array.isArray(data) && data.length > 0) {
            this.total = data[0].totals !== undefined ? data[0].totals : data.length;
            this.operators = data;
            this.operators.forEach(operator => {
              operator.showLoginPwd = false;
              operator.showOpPwd = false;
              // 恢复原来的逻辑：确保入职时间字段正确显示
              if (operator.createTime && operator.createTime.includes(' ')) {
                operator.createTime = operator.createTime.split(' ')[0];
              }
            });
          } else {
            this.total = 0;
            this.operators = [];
          }
          
          // 数据加载完成后重新计算高度
          this.$nextTick(() => {
            this.calculateTableHeight();
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
      // 确保编辑对话框中的入职时间正确显示
      if (this.editForm.createTime && this.editForm.createTime.includes(' ')) {
        this.editForm.createTime = this.editForm.createTime.split(' ')[0];
      }
      this.showEditDialog = true;
    },
    
    saveEdit() {
      this.$refs.editForm.validate((valid) => {
        if (valid) {
          let submitData = { ...this.editForm };
          let ct = submitData.createTime;
          if (ct) {
            if (typeof ct === 'string' && ct.length === 10) {
              submitData.createTime = ct + "T00:00:00";
            } else if (typeof ct === 'string' && ct.includes(' ')) {
              submitData.createTime = ct.replace(' ', 'T');
            } else if (ct instanceof Date) {
              submitData.createTime = dayjs(ct).format('YYYY-MM-DDTHH:mm:ss');
            }
          }

          axios.put("api/sysManage/key/update", submitData, {
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then(response => {
            if (response.data && (response.data.code === 200 || response.data.code === 0)) {
              this.$message.success("修改成功");
              this.showEditDialog = false;
              this.fetchOperator();
            } else {
              this.$message.error((response.data && response.data.message) || "修改失败");
            }
          })
          .catch(error => {
            console.error('错误信息为', error);
            this.$message.error("修改失败，请检查网络或登录状态");
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
          if (response.data && (response.data.code === 200 || response.data.code === 0)) {
            this.$message.success("删除成功");
            this.fetchOperator();
          } else {
            this.$message.error((response.data && response.data.message) || "删除失败");
          }
        })
        .catch((error) => {
          console.log(error);
          this.$message.error("删除失败，请检查网络或登录状态");
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
          if (response.data && (response.data.code === 200 || response.data.code === 0)) {
            this.$message.success("批量删除成功");
            this.fetchOperator();
            this.selectedOperators = [];
          } else {
            this.$message.error((response.data && response.data.message) || "批量删除失败");
          }
        }).catch(error => {
          this.loading = false;
          console.error(error);
          this.$message.error("批量删除失败，请检查网络或登录状态");
        });
      }).catch(() => {
        this.$message.info("取消删除");
      });
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.page = 1;
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
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  overflow: hidden;
  background: #f0f2f5;
}

/* 页面顶部标题与操作栏 - 对应红框 1 放大 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 8px;
  padding: 18px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #e6f7ff;
  border-radius: 8px;
  color: #1890ff;
  font-size: 22px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  letter-spacing: -0.3px;
}

.title-tag {
  font-size: 13px;
  font-weight: 500;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 3px 10px;
}

.page-desc {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right .el-button {
  font-size: 14px;
  padding: 10px 18px;
  border-radius: 6px;
  font-weight: 500;
}

/* 运行指标透视条 (Stats Bar) - 对应红框 2 放大 */
.stats-bar {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 16px 24px;
  margin-bottom: 8px;
  min-height: 88px;
  flex-shrink: 0;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #4e5969;
}

.stat-meta .stat-icon {
  font-size: 18px;
  color: #86909c;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 12px;
}

.status-badge.success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.status-badge.success .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 4px;
}

.stat-value .num {
  font-size: 32px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.stat-value .unit {
  font-size: 14px;
  font-weight: 500;
  color: #86909c;
}

.text-success .num {
  color: #52c41a !important;
}

.text-primary .num {
  color: #1890ff !important;
}

.text-warning .num {
  color: #fa8c16 !important;
}

.stat-foot {
  font-size: 12.5px;
  color: #86909c;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 52px;
  background: #e5e6eb;
  margin: 0 28px;
}

/* 内容区域整体大卡片 - 对应红框 3 充满剩余视口 */
.content-box {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  padding: 16px 20px;
  box-sizing: border-box;
  overflow: hidden;
}

/* 筛选与操作工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 260px;
}

::v-deep .search-input input::-webkit-search-cancel-button,
::v-deep .search-input input::-webkit-search-decoration {
  -webkit-appearance: none !important;
  display: none !important;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.batch-set-box {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 3px 6px;
}

.pwd-input {
  width: 190px;
}

/* 表格区域 */
.table-container {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

::v-deep .el-table {
  width: 100% !important;
  height: 100%;
}

::v-deep .el-table th.el-table__cell {
  background-color: #fafafa !important;
  color: #262626 !important;
  font-weight: 600;
  font-size: 14px;
  padding: 8px 0 !important;
  border-bottom: 1px solid #f0f0f0;
}

::v-deep .el-table td.el-table__cell {
  padding: 6px 0 !important;
  font-size: 14px;
  color: #595959;
  border-bottom: 1px solid #f0f0f0;
}

::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #fafbfc;
}

::v-deep .el-table__body-wrapper {
  overflow-y: auto !important;
  overflow-x: hidden !important;
}

.user-badge {
  color: #262626;
  font-weight: 500;
  font-size: 14px;
}

.user-badge i {
  color: #1890ff;
  margin-right: 4px;
}

.code-badge {
  font-family: "JetBrains Mono", Consolas, monospace;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #e8e8e8;
  font-size: 13px;
}

.password-cell {
  padding: 0 4px;
}

.password-cell ::v-deep .el-input__inner {
  height: 28px;
  line-height: 28px;
  font-size: 13px;
}

.time-text {
  font-size: 13px;
  color: #8c8c8c;
}

.time-text i {
  margin-right: 3px;
}

.remark-text {
  font-size: 13px;
  color: #595959;
}

.action-links {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  white-space: nowrap;
}

.action-links .el-button--text {
  padding: 0;
  font-size: 14px;
  margin: 0;
}

.delete-btn {
  color: #f56c6c !important;
}

.delete-btn:hover {
  color: #ff7875 !important;
}

/* 分页区域 */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 10px 0 0;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.pagination-total {
  font-size: 13px;
  color: #8c8c8c;
}

.total-count {
  color: #1890ff;
  font-weight: 600;
}

/* 对话框 */
.dialog-tip-banner {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 7px 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #0050b3;
}

.dialog-tip-banner i {
  font-size: 15px;
  color: #1890ff;
}
</style>


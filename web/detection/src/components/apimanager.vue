<template>
  <div class="api-management">
    <!-- Header -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-key"></i></span>
          <h2 class="page-title">API 凭据管理</h2>
          <span class="title-tag">工业端点鉴权</span>
        </div>
        <p class="page-desc">管理边缘质检工位、相机采集终端及算法推理中台的安全访问凭证与调用配额</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新建凭证</el-button>
        <el-button size="small" icon="el-icon-document-add" @click="handleAddmore">批量生成</el-button>
        <el-button
          type="danger"
          plain
          size="small"
          icon="el-icon-delete"
          :disabled="selectedRows.length === 0"
          @click="deleteMore"
        >
          批量删除<span v-if="selectedRows.length > 0"> ({{ selectedRows.length }})</span>
        </el-button>
      </div>
    </div>

    <!-- 集成式指标统计条 (Stat Group) -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">凭据总数</span>
          <i class="el-icon-collection-tag stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ total || tableData.length }}</span>
          <span class="unit">个</span>
        </div>
        <div class="stat-foot">已登记的通信鉴权 Key</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">正常运行</span>
          <span class="status-badge success"><span class="dot"></span>启用中</span>
        </div>
        <div class="stat-value text-success">
          <span class="num">{{ activeCount }}</span>
          <span class="unit">个</span>
        </div>
        <div class="stat-foot">实时接收工位请求</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">长期有效</span>
          <i class="el-icon-date stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ permanentCount }}</span>
          <span class="unit">个</span>
        </div>
        <div class="stat-foot">固定机台专线凭据</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">高级调度</span>
          <i class="el-icon-lock stat-icon"></i>
        </div>
        <div class="stat-value text-warning">
          <span class="num">{{ highPermCount }}</span>
          <span class="unit">个</span>
        </div>
        <div class="stat-foot">核心调度控制权限</div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar-section">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          size="small"
          placeholder="搜索 API 密钥 / 创建人 / 备注..."
          prefix-icon="el-icon-search"
          clearable
          class="search-input"
          @input="onFilterChange"
          @clear="onFilterChange"
        ></el-input>
        <el-select v-model="filterStatus" size="small" placeholder="运行状态" clearable class="filter-select" @change="onFilterChange">
          <el-option label="全部状态" value=""></el-option>
          <el-option label="正常启用" :value="1"></el-option>
          <el-option label="已停用" :value="0"></el-option>
        </el-select>
        <el-select v-model="filterLevel" size="small" placeholder="权限等级" clearable class="filter-select" @change="onFilterChange">
          <el-option label="全部等级" value=""></el-option>
          <el-option label="权限1 (基础读取)" value="1"></el-option>
          <el-option label="权限2 (标准质检)" value="2"></el-option>
          <el-option label="权限3 (系统调度)" value="3"></el-option>
        </el-select>
        <el-button size="small" icon="el-icon-refresh-left" @click="resetFilters">重置</el-button>
      </div>
      <div class="toolbar-right">
        <el-tooltip content="刷新列表" placement="top">
          <el-button size="small" icon="el-icon-refresh" circle @click="fetchData"></el-button>
        </el-tooltip>
      </div>
    </div>

﻿    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        :height="tableHeight"
        v-loading="loading"
        element-loading-text="正在加载数据..."
        element-loading-spinner="el-icon-loading"
        :data="filteredData"
        size="small"
        stripe
        style="width: 100%;"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" align="center"></el-table-column>
        <el-table-column label="序号" width="60" align="center">
          <template slot-scope="scope">
            <span>{{ (page - 1) * pageSize + scope.$index + 1 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="创建人" prop="createName" width="100">
          <template slot-scope="scope">
            <span class="user-badge"><i class="el-icon-user"></i> {{ scope.row.createName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="API 密钥 (Secret Key)" min-width="210">
          <template slot-scope="scope">
            <div class="key-cell">
              <span class="key-text">{{ isKeyVisible(scope.$index) ? scope.row.apiKey : maskApiKey(scope.row.apiKey) }}</span>
              <el-tooltip :content="isKeyVisible(scope.$index) ? '隐藏密钥' : '显示完整密钥'" placement="top">
                <i
                  :class="isKeyVisible(scope.$index) ? 'el-icon-view is-visible' : 'el-icon-view'"
                  class="key-action-icon"
                  @click="toggleKeyVisible(scope.$index)"
                ></i>
              </el-tooltip>
              <el-tooltip content="复制密钥" placement="top">
                <i class="el-icon-document-copy key-action-icon copy-icon" @click="copyApiKey(scope.row.apiKey)"></i>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="到期日期" prop="expirationDate" width="120" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.validityPeriod < 0 || scope.row.expirationDate === '无限期'" class="text-secondary">无限期</span>
            <span v-else :class="{ 'text-expired': isExpired(scope.row.expirationDate) }">
              {{ scope.row.expirationDate }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="有效期" prop="validityPeriod" width="95" align="center">
          <template slot-scope="scope">
            <el-tag :type="getValidityPeriodType(scope.row.validityPeriod)" size="mini" effect="plain">
              {{ scope.row.validityPeriod < 0 ? '无限期' : scope.row.validityPeriod + ' 天' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="有效次数" prop="validityTimes" width="95" align="center">
          <template slot-scope="scope">
            <el-tag :type="getValidityTimesType(scope.row.validityTimes)" size="mini" effect="plain">
              {{ scope.row.validityTimes < 0 ? '无限次' : scope.row.validityTimes + ' 次' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="权限等级" prop="permissionLevel" width="95" align="center">
          <template slot-scope="scope">
            <el-tag :type="getPermissionLevelType(scope.row.permissionLevel)" size="mini" effect="light">
              权限{{ scope.row.permissionLevel }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="运行状态" prop="status" width="90" align="center">
          <template slot-scope="scope">
            <span class="status-badge" :class="scope.row.status === 1 ? 'status-active' : 'status-disabled'">
              <span class="status-dot"></span>
              <span class="status-text">{{ scope.row.status === 1 ? '启用' : '停用' }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column label="备注说明" prop="remark" min-width="110" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="remark-text">{{ scope.row.remark || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center">
          <template slot-scope="scope">
            <div class="action-links">
              <el-button type="text" size="small" icon="el-icon-edit" @click="handleedit(scope.$index)">修改</el-button>
              <el-popconfirm
                title="确定注销并删除此 API 凭证？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                icon="el-icon-warning"
                icon-color="#f56c6c"
                @confirm="handleDelete(scope.$index, scope.row)"
              >
                <el-button slot="reference" type="text" size="small" class="delete-btn" icon="el-icon-delete">删除</el-button>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页栏 -->
    <div class="pagination-footer">
      <div class="pagination-total">
        共 <span class="total-count">{{ total }}</span> 条凭据记录，当前第 {{ page }} / {{ Math.ceil(total / pageSize) || 1 }} 页
      </div>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      ></el-pagination>
    </div>

﻿    <!-- 新增弹窗 -->
    <el-dialog title="新建 API 凭证" :visible.sync="dialogVisible" width="560px" :close-on-click-modal="false">
      <div class="dialog-tip-banner">
        <i class="el-icon-info"></i>
        <span>创建的 API 凭证将用于工业相机、边缘工位及第三方系统接入本系统的通信鉴权。</span>
      </div>
      <el-form :model="formData" :rules="formRules" ref="apiForm" label-width="110px" size="small">
        <el-form-item label="创建者账号">
          <el-input v-model="formData.createName" disabled></el-input>
        </el-form-item>
        <el-form-item label="权限等级" prop="permissionLevel">
          <el-select v-model="formData.permissionLevel" placeholder="请选择权限等级" style="width: 100%;">
            <el-option v-for="item in permissionOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="有效期 (天)" prop="validityPeriod">
          <el-input-number v-model="formData.validityPeriod" :min="-1" :max="3650" style="width: 100%;" placeholder="-1 表示无限期"></el-input-number>
        </el-form-item>
        <el-form-item label="预计到期时间">
          <span class="preview-date">{{ calculateExpirationDate(formData.validityPeriod) }}</span>
        </el-form-item>
        <el-form-item label="有效调用次数" prop="validityTimes">
          <el-input-number v-model="formData.validityTimes" :min="-1" style="width: 100%;" placeholder="-1 表示无限次"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input type="textarea" :rows="2" v-model="formData.remark" placeholder="填写凭证使用工位或所属产线设备"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="addData">确认创建</el-button>
      </div>
    </el-dialog>

    <!-- 批量生成弹窗 -->
    <el-dialog title="批量生成 API 凭证" :visible.sync="moreAdddialogVisible" width="560px" :close-on-click-modal="false">
      <div class="dialog-tip-banner">
        <i class="el-icon-info"></i>
        <span>系统将根据配置的参数一次性批量生成多个独立的 API 通信密钥。</span>
      </div>
      <el-form :model="formDataMore" :rules="formRulesMore" ref="apiFormMore" label-width="110px" size="small">
        <el-form-item label="创建者账号">
          <el-input v-model="formDataMore.createName" disabled></el-input>
        </el-form-item>
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="formDataMore.count" :min="1" :max="100" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="权限等级" prop="permissionLevel">
          <el-select v-model="formDataMore.permissionLevel" placeholder="请选择权限等级" style="width: 100%;">
            <el-option v-for="item in permissionOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="有效期 (天)" prop="validityPeriod">
          <el-input-number v-model="formDataMore.validityPeriod" :min="-1" :max="3650" style="width: 100%;" placeholder="-1 表示无限期"></el-input-number>
        </el-form-item>
        <el-form-item label="预计到期时间">
          <span class="preview-date">{{ calculateExpirationDate1(formDataMore.validityPeriod) }}</span>
        </el-form-item>
        <el-form-item label="有效调用次数" prop="validityTimes">
          <el-input-number v-model="formDataMore.validityTimes" :min="-1" style="width: 100%;" placeholder="-1 表示无限次"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formDataMore.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input type="textarea" :rows="2" v-model="formDataMore.remark" placeholder="批量生成统一备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="moreAdddialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="addDatamore">确认批量生成</el-button>
      </div>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog title="配置 API 凭证" :visible.sync="EditdialogVisible" width="560px" :close-on-click-modal="false">
      <el-form :model="EditformData" label-width="110px" size="small">
        <el-form-item label="凭证编号">
          <el-input v-model="EditformData.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="API 密钥">
          <el-input v-model="EditformData.apiKey" disabled></el-input>
        </el-form-item>
        <el-form-item label="权限等级">
          <el-select v-model="EditformData.permissionLevel" placeholder="请选择权限等级" style="width: 100%;">
            <el-option v-for="item in permissionOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="有效期 (天)">
          <el-input-number v-model="EditformData.validityPeriod" :min="-1" :max="3650" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="预计到期时间">
          <span class="preview-date">{{ calculateExpirationDate2(EditformData.validityPeriod) }}</span>
        </el-form-item>
        <el-form-item label="有效调用次数">
          <el-input-number v-model="EditformData.validityTimes" :min="-1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="运行状态">
          <el-radio-group v-model="EditformData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input type="textarea" :rows="2" v-model="EditformData.remark" placeholder="备注说明"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="EditdialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="EditData">保存配置</el-button>
      </div>
    </el-dialog>
  </div>
</template>

﻿<script>
import axios from 'axios';

export default {
  name: 'ApiManager',
  data() {
    return {
      tableHeight: 380,
      loading: false,
      page: 1,
      pageSize: 10,
      total: 0,
      user: JSON.parse(localStorage.getItem('useradmin') || '{}'),
      searchKeyword: '',
      filterStatus: '',
      filterLevel: '',
      visibleKeyIndices: {},
      permissionOptions: [
        { label: '权限1 (基础读取)', value: '1' },
        { label: '权限2 (标准质检)', value: '2' },
        { label: '权限3 (系统调度)', value: '3' }
      ],
      formRules: {
        permissionLevel: [{ required: true, message: '请选择权限等级', trigger: 'change' }],
        validityPeriod: [{ required: true, message: '请输入有效期天数', trigger: 'blur' }],
        validityTimes: [{ required: true, message: '请输入有效次数', trigger: 'blur' }]
      },
      formRulesMore: {
        count: [{ required: true, message: '请输入生成数量', trigger: 'blur' }],
        permissionLevel: [{ required: true, message: '请选择权限等级', trigger: 'change' }],
        validityPeriod: [{ required: true, message: '请输入有效期天数', trigger: 'blur' }],
        validityTimes: [{ required: true, message: '请输入有效次数', trigger: 'blur' }]
      },
      dialogVisible: false,
      moreAdddialogVisible: false,
      EditdialogVisible: false,
      tableData: [],
      selectedRows: [],
      formData: {
        createName: '',
        permissionLevel: '1',
        validityPeriod: 30,
        validityTimes: 1000,
        status: 1,
        remark: ''
      },
      formDataMore: {
        createName: '',
        count: 5,
        permissionLevel: '1',
        validityPeriod: 30,
        validityTimes: 1000,
        status: 1,
        remark: ''
      },
      EditformData: {
        id: '',
        apiKey: '',
        permissionLevel: '1',
        validityPeriod: 30,
        validityTimes: 1000,
        status: 1,
        remark: ''
      }
    };
  },
  computed: {
    activeCount() {
      return this.tableData.filter(item => item.status === 1).length;
    },
    permanentCount() {
      return this.tableData.filter(item => item.validityPeriod < 0 || item.expirationDate === '无限期').length;
    },
    highPermCount() {
      return this.tableData.filter(item => Number(item.permissionLevel) >= 2).length;
    },
    filteredData() {
      return this.tableData.filter(item => {
        const matchKeyword = !this.searchKeyword ||
          (item.apiKey && item.apiKey.toLowerCase().includes(this.searchKeyword.toLowerCase())) ||
          (item.createName && item.createName.toLowerCase().includes(this.searchKeyword.toLowerCase())) ||
          (item.remark && item.remark.toLowerCase().includes(this.searchKeyword.toLowerCase()));
        const matchStatus = this.filterStatus === '' || this.filterStatus === null || item.status === this.filterStatus;
        const matchLevel = this.filterLevel === '' || this.filterLevel === null || String(item.permissionLevel) === String(this.filterLevel);
        return matchKeyword && matchStatus && matchLevel;
      });
    }
  },
  mounted() {
    this.formData.createName = this.user.userName || 'admin1';
    this.formDataMore.createName = this.user.userName || 'admin1';
    this.fetchData();
    this.$nextTick(() => {
      setTimeout(() => {
        this.calculateTableHeight();
      }, 100);
    });
    window.addEventListener('resize', this.calculateTableHeight);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.calculateTableHeight);
  },
  methods: {
    calculateTableHeight() {
      const vh = window.innerHeight || 800;
      this.tableHeight = Math.max(vh - 410, 260);
    },
    fetchData() {
      this.loading = true;
      axios.get('api/sysManage/api/info', {
        params: {
          page: this.page,
          pageSize: this.pageSize
        }
      })
      .then(response => {
        if (response.data.code === 200) {
          const data = response.data.data;
          if (Array.isArray(data) && data.length > 0) {
            this.tableData = data;
            this.total = data[0].totals || data.length;
          } else {
            this.tableData = [];
            this.total = 0;
          }
        } else {
          this.$message.error(response.data.message || '获取数据失败');
          this.tableData = [];
          this.total = 0;
        }
      })
      .catch(error => {
        console.error('获取 API 凭证错误:', error);
        this.$message.error('获取数据失败，请检查网络连接');
        this.tableData = [];
        this.total = 0;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    maskApiKey(key) {
      if (!key) return '';
      if (key.length <= 10) return '••••••••';
      return key.slice(0, 6) + '••••••••' + key.slice(-4);
    },
    isKeyVisible(index) {
      return !!this.visibleKeyIndices[index];
    },
    toggleKeyVisible(index) {
      this.$set(this.visibleKeyIndices, index, !this.visibleKeyIndices[index]);
    },
    copyApiKey(key) {
      if (!key) return;
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(key).then(() => {
          this.$message.success('API 密钥已成功复制到剪贴板');
        }).catch(() => {
          this.fallbackCopy(key);
        });
      } else {
        this.fallbackCopy(key);
      }
    },
    fallbackCopy(key) {
      const textarea = document.createElement('textarea');
      textarea.value = key;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.$message.success('API 密钥已复制到剪贴板');
    },
    isExpired(dateStr) {
      if (!dateStr || dateStr === '无限期') return false;
      return new Date(dateStr) < new Date();
    },
    getValidityPeriodType(val) {
      if (val < 0) return 'success';
      if (val <= 7) return 'danger';
      if (val <= 30) return 'warning';
      return 'info';
    },
    getValidityTimesType(val) {
      if (val < 0) return 'success';
      if (val <= 100) return 'warning';
      return 'info';
    },
    getPermissionLevelType(val) {
      const map = { 1: 'info', 2: 'primary', 3: 'warning' };
      return map[val] || 'info';
    },
    resetFilters() {
      this.searchKeyword = '';
      this.filterStatus = '';
      this.filterLevel = '';
    },
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    handleAdd() {
      this.formData = {
        createName: this.user.userName || 'admin1',
        permissionLevel: '1',
        validityPeriod: 30,
        validityTimes: 1000,
        status: 1,
        remark: ''
      };
      this.dialogVisible = true;
    },
    addData() {
      this.$refs.apiForm.validate(valid => {
        if (!valid) return;
        const submitData = {
          ...this.formData,
          num: 1,
          expirationDate: this.calculateExpirationDate(this.formData.validityPeriod)
        };
        axios.post('api/sysManage/api/add', submitData)
          .then(res => {
            if (res.data.code === 200) {
              this.$message.success('API 凭证创建成功');
              this.dialogVisible = false;
              this.fetchData();
            } else {
              this.$message.error(res.data.message || '创建失败');
            }
          })
          .catch(() => {
            this.$message.error('请求失败，请稍后重试');
          });
      });
    },
    handleAddmore() {
      this.formDataMore = {
        createName: this.user.userName || 'admin1',
        count: 5,
        permissionLevel: '1',
        validityPeriod: 30,
        validityTimes: 1000,
        status: 1,
        remark: ''
      };
      this.moreAdddialogVisible = true;
    },
    addDatamore() {
      this.$refs.apiFormMore.validate(valid => {
        if (!valid) return;
        const submitData = {
          ...this.formDataMore,
          num: Number(this.formDataMore.count || this.formDataMore.num || 1),
          expirationDate: this.calculateExpirationDate1(this.formDataMore.validityPeriod)
        };
        axios.post('api/sysManage/api/add', submitData)
          .then(res => {
            if (res.data.code === 200) {
              this.$message.success('批量生成成功');
              this.moreAdddialogVisible = false;
              this.fetchData();
            } else {
              this.$message.error(res.data.message || '批量生成失败');
            }
          })
          .catch(() => {
            this.$message.error('请求失败，请稍后重试');
          });
      });
    },
    handleedit(index) {
      const row = this.filteredData[index];
      this.EditformData = {
        id: row.id,
        apiKey: row.apiKey,
        permissionLevel: String(row.permissionLevel || '1'),
        validityPeriod: row.validityPeriod,
        validityTimes: row.validityTimes,
        status: row.status,
        remark: row.remark || ''
      };
      this.EditdialogVisible = true;
    },
    EditData() {
      const submitData = {
        ...this.EditformData,
        expirationDate: this.calculateExpirationDate2(this.EditformData.validityPeriod)
      };
      axios.patch('/api/sysManage/api/update', submitData)
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success('配置已保存');
            this.EditdialogVisible = false;
            this.fetchData();
          } else {
            this.$message.error(res.data.message || '更新失败');
          }
        })
        .catch(() => {
          this.$message.error('请求失败，请稍后重试');
        });
    },
    handleDelete(index, row) {
      axios.delete('api/sysManage/api/delete', {
        data: [row.id]
      })
      .then(res => {
        if (res.data.code === 200) {
          this.$message.success('删除成功');
          if (this.tableData.length === 1 && this.page > 1) {
            this.page--;
          }
          this.fetchData();
        } else {
          this.$message.error(res.data.message || '删除失败');
        }
      })
      .catch(() => {
        this.$message.error('删除失败，请稍后重试');
      });
    },
    deleteMore() {
      if (this.selectedRows.length === 0) return;
      this.$confirm(`确定注销并删除选中的 ${this.selectedRows.length} 个 API 凭证？`, '批量删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const ids = this.selectedRows.map(item => item.id);
        axios.delete('api/sysManage/api/delete', {
          data: ids
        })
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success('批量删除成功');
            this.fetchData();
          } else {
            this.$message.error(res.data.message || '批量删除失败');
          }
        })
        .catch(() => {
          this.$message.error('批量删除失败');
        });
      }).catch(() => {});
    },
    calculateExpirationDate(validityPeriod) {
      if (validityPeriod < 0) return '无限期';
      const d = new Date();
      d.setDate(d.getDate() + Number(validityPeriod));
      return d.toISOString().slice(0, 10);
    },
    calculateExpirationDate1(validityPeriod) {
      if (validityPeriod < 0) return '无限期';
      const d = new Date();
      d.setDate(d.getDate() + Number(validityPeriod));
      return d.toISOString().slice(0, 10);
    },
    calculateExpirationDate2(validityPeriod) {
      if (validityPeriod < 0) return '无限期';
      const d = new Date();
      d.setDate(d.getDate() + Number(validityPeriod));
      return d.toISOString().slice(0, 10);
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.page = 1;
      this.fetchData();
    },
    onFilterChange() {
      this.page = 1;
    },
    handleCurrentChange(val) {
      this.page = val;
      this.fetchData();
    }
  }
};
</script>

﻿<style scoped>
.api-management {
  width: 100%;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #303133;
}

/* 页面头部栏 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  width: 28px;
  height: 28px;
  background: #e6f7ff;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  font-size: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
  letter-spacing: -0.2px;
}

.title-tag {
  font-size: 12px;
  font-weight: 500;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 3px;
  padding: 1px 7px;
}

.page-desc {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: #8c8c8c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 集成式指标统计条 */
.stats-bar {
  display: flex;
  align-items: center;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 8px 18px;
  margin-bottom: 12px;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: #e8e8e8;
  margin: 0 18px;
}

.stat-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.stat-icon {
  font-size: 14px;
  color: #bfbfbf;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value .num {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  line-height: 1.2;
}

.stat-value .unit {
  font-size: 12px;
  color: #8c8c8c;
}

.stat-value.text-success .num {
  color: #52c41a;
}

.stat-value.text-warning .num {
  color: #fa8c16;
}

.stat-foot {
  font-size: 12px;
  color: #bfbfbf;
  margin-top: 2px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
}

.status-badge.success {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
}

/* 工具栏区域 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.filter-select {
  width: 140px;
}

/* 表格容器 */
.table-container {
  width: 100%;
  overflow: hidden;
}

::v-deep .el-table {
  width: 100% !important;
}

::v-deep .el-table th.el-table__cell {
  background-color: #fafafa !important;
  color: #262626 !important;
  font-weight: 600;
  font-size: 13px;
  padding: 6px 0 !important;
  border-bottom: 1px solid #f0f0f0;
}

::v-deep .el-table td.el-table__cell {
  padding: 5px 0 !important;
  font-size: 13px;
  color: #595959;
  border-bottom: 1px solid #f0f0f0;
}

::v-deep .el-table__body-wrapper {
  overflow-y: auto !important;
  overflow-x: hidden !important;
}

/* 单元格元素 */
.user-badge {
  color: #595959;
  font-size: 13px;
}

.user-badge i {
  color: #8c8c8c;
  margin-right: 4px;
}

.key-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: "JetBrains Mono", "SF Mono", Consolas, Monaco, monospace;
  font-size: 12.5px;
  color: #1f2937;
  background: #f8fafc;
  padding: 3px 8px;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

.key-text {
  letter-spacing: 0.5px;
}

.key-action-icon {
  font-size: 14px;
  color: #8c8c8c;
  cursor: pointer;
  margin-left: 3px;
  transition: color 0.15s;
}

.key-action-icon:hover {
  color: #1890ff;
}

.key-action-icon.copy-icon:hover {
  color: #52c41a;
}

.key-action-icon.is-visible {
  color: #1890ff;
}

.text-secondary {
  color: #8c8c8c;
}

.text-expired {
  color: #f5222d;
  font-weight: 500;
}

.remark-text {
  color: #8c8c8c;
  font-size: 13px;
}

/* 运行状态指示 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-active .status-dot {
  background-color: #52c41a;
}

.status-active .status-text {
  color: #389e0d;
}

.status-disabled .status-dot {
  background-color: #bfbfbf;
}

.status-disabled .status-text {
  color: #8c8c8c;
}

/* 操作列链接 */
.action-links {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  white-space: nowrap;
}

.action-links .el-button--text {
  padding: 0;
  font-size: 13px;
  margin: 0;
}

.delete-btn {
  color: #f56c6c !important;
}

.delete-btn:hover {
  color: #ff7875 !important;
}

/* 分页栏 */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding: 6px 0 2px;
}

.pagination-total {
  font-size: 13px;
  color: #8c8c8c;
}

.total-count {
  color: #1890ff;
  font-weight: 600;
}

/* 弹窗样式 */
.dialog-tip-banner {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 8px 12px;
  margin-bottom: 18px;
  font-size: 13px;
  color: #0050b3;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-date {
  font-size: 13px;
  font-weight: 500;
  color: #1890ff;
  background: #f0f7ff;
  padding: 2px 8px;
  border-radius: 3px;
}
</style>

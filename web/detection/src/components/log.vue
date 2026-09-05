<template>
  <div class="log-management">
    <!-- 头部区域 -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-document"></i></span>
          <h2 class="page-title">系统操作日志</h2>
          <span class="title-tag">审计追溯</span>
        </div>
        <p class="page-desc">全维度记录系统接口调用、推理服务流转与关键操作审计轨迹</p>
      </div>
      <div class="header-right">
        <el-tooltip content="刷新日志数据" placement="top">
          <el-button size="small" icon="el-icon-refresh" circle @click="fetchData"></el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 运行指标透视条 (Stats Bar) -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">日志总数</span>
          <i class="el-icon-document-copy stat-icon"></i>
        </div>
        <div class="stat-value">
          <span class="num">{{ total || tableData.length }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">已记录全量审计数据</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">正常执行</span>
          <span class="status-badge success"><span class="dot"></span>正常</span>
        </div>
        <div class="stat-value text-success">
          <span class="num">{{ successCount }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">接口调用与执行成功</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">告警与异常</span>
          <i class="el-icon-warning-outline stat-icon"></i>
        </div>
        <div class="stat-value text-warning">
          <span class="num">{{ warnCount }}</span>
          <span class="unit">条</span>
        </div>
        <div class="stat-foot">需审计关注的事件</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-meta">
          <span class="stat-label">活跃终端 IP</span>
          <i class="el-icon-monitor stat-icon"></i>
        </div>
        <div class="stat-value text-primary">
          <span class="num">{{ uniqueIpCount }}</span>
          <span class="unit">个</span>
        </div>
        <div class="stat-foot">发起调用的边缘机台</div>
      </div>
    </div>

    <!-- 内容区域 (Content Section - 对应红框3自适应充满) -->
    <div class="content-box">
      <!-- 筛选工具栏 -->
      <div class="toolbar-section" ref="filterRef">
        <div class="toolbar-left">
          <span class="filter-label">时间范围：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            size="small"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            class="time-range-picker"
            @change="mainTime"
            clearable
          ></el-date-picker>
          <el-button size="small" type="primary" icon="el-icon-search" @click="mainTime">查询</el-button>
          <el-button size="small" icon="el-icon-refresh-left" @click="resetTimeFilter">重置</el-button>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="searchName"
            size="small"
            placeholder="检索操作主体 / 工号..."
            prefix-icon="el-icon-search"
            clearable
            class="search-input"
            @keyup.enter.native="search"
          ></el-input>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table
          :height="tableHeight"
          :data="filteredtableData"
          v-loading="loading"
          element-loading-text="正在加载系统日志..."
          stripe
          style="width: 100%"
          class="enterprise-table"
        >
          <el-table-column label="序号" width="65" align="center">
            <template slot-scope="scope">
              <span>{{ (page - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="mainRole" label="操作主体" min-width="110">
            <template slot-scope="scope">
              <span class="user-badge"><i class="el-icon-user"></i> {{ scope.row.mainRole || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="工号/账号" min-width="110">
            <template slot-scope="scope">
              <span class="code-badge">{{ scope.row.operatorName || scope.row.label || scope.row.operator || scope.row.userId || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="终端 IP" min-width="135">
            <template slot-scope="scope">
              <span class="ip-tag"><i class="el-icon-location-information"></i> {{ scope.row.clientIp || scope.row.ip || '127.0.0.1' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="资源类型" width="125" align="center">
            <template slot-scope="scope">
              <el-tag :type="getResourceTagType(scope.row.resourceType || scope.row.type)" size="small" effect="plain" class="res-tag">
                {{ scope.row.resourceType || scope.row.type || '系统' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作动作" min-width="110">
            <template slot-scope="scope">
              <span class="op-text">{{ scope.row.op || scope.row.operation || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="执行状态" width="95" align="center">
            <template slot-scope="scope">
              <div class="status-cell" :class="'status-' + (getStatusTagType(scope.row.status) || 'info')">
                <span class="status-dot"></span>
                <span class="status-text">{{ scope.row.status || '成功' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作详情" min-width="220" show-overflow-tooltip>
            <template slot-scope="scope">
              <span class="detail-text">{{ scope.row.details || scope.row.target || scope.row.detail || scope.row.op || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="time" label="记录时间" width="165" align="center">
            <template slot-scope="scope">
              <span class="time-text"><i class="el-icon-time"></i> {{ scope.row.time }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页区域 -->
      <div class="pagination-footer">
        <div class="pagination-total">
          共 <span class="total-count">{{ total }}</span> 条日志记录，当前第 {{ page }} / {{ Math.ceil(total / pageSize) || 1 }} 页
        </div>
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="page"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import moment from "moment/moment";

export default {
  data() {
    return {
      searchName: '',
      tableData: [],
      page: 1,
      pageSize: 10,
      total: 0,
      dateRange: [],
      dateL: '',
      dateR: '',
      loading: false,
      tableHeight: 400,
      pickerOptions: {
        shortcuts: [
          {
            text: '最近一周',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'week').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一月',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'month').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          },
          {
            text: '最近一年',
            onClick(picker) {
              const end = moment().endOf('day');
              const start = moment(end).subtract(1, 'year').startOf('day');
              picker.$emit('pick', [start, end]);
            }
          }
        ],
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
    };
  },
  created() {
    this.fetchData();
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
    successCount() {
      if (!Array.isArray(this.tableData)) return 0;
      return this.tableData.filter(i => i.status === '成功' || i.status === 'SUCCESS' || i.status == 1).length;
    },
    warnCount() {
      if (!Array.isArray(this.tableData)) return 0;
      return this.tableData.filter(i => i.status === '告警' || i.status === '失败' || i.status === 'FAILED' || i.status === 'WARNING' || i.status == 0).length;
    },
    uniqueIpCount() {
      if (!Array.isArray(this.tableData) || this.tableData.length === 0) return 0;
      const ips = new Set(this.tableData.map(i => i.ip).filter(Boolean));
      return ips.size;
    },

    filteredtableData() {
      if (!this.searchName) {
        return this.tableData;
      } else {
        const searchName = this.searchName.toLowerCase();
        return this.tableData.filter(log => {
          return (
            log.mainRole.toLowerCase().includes(searchName) ||
            log.label.toLowerCase().includes(searchName)
          );
        });
      }
    }
  },
  methods: {
    resetTimeFilter() {
      this.dateRange = [];
      this.dateL = '';
      this.dateR = '';
      this.fetchData();
    },

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
    search() {
      this.searchName = this.searchName.trim();
    },
    mainTime() {
      if (this.dateRange && this.dateRange.length === 2) {
        this.page = 1;
        this.fetchData();
      } else {
        this.$message.warning("请选择完整的时间范围");
      }
    },
    fetchData() {
      this.loading = true;
      const params = {
        page: this.page,
        pageSize: this.pageSize
      };
      if (this.dateRange && this.dateRange.length === 2) {
        const start = moment(this.dateRange[0]).startOf("day").format("YYYY-MM-DDTHH:mm:ss");
        const end = moment(this.dateRange[1]).endOf("day").format("YYYY-MM-DDTHH:mm:ss");
        params.dateRange = `${start},${end}`;
      }
      axios.get("api/log/info", { params })
        .then(response => {
          this.loading = false;
          if (response.data && response.data.code === 200) {
            const resData = response.data.data;
            if (Array.isArray(resData) && resData.length > 0) {
              this.tableData = resData;
              this.total = resData[0].totals || resData.length;
            } else {
              this.tableData = [];
              this.total = 0;
            }
            this.$message.success("查询到日志信息");
            this.$nextTick(() => {
              this.calculateTableHeight();
            });
          } else {
            this.$message.error("日志信息获取失败");
          }
        })
        .catch(err => {
          this.loading = false;
          console.error("获取数据失败", err);
          this.$message.error("未能查询到日志信息");
        });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.page = 1; // 重置到第一页
      this.fetchData();
    },
    handleCurrentChange(val) {
      this.page = val;
      this.fetchData();
    },
    getResourceTagType(type) {
      const map = {
        'AI质检模型': 'danger',
        '缺陷复核': 'warning',
        '产线硬件': 'info',
        'AGV运检': 'success',
        '排产工单': '',
        'API密钥': 'warning',
        '系统安全': 'danger'
      };
      return map[type] || 'info';
    },
    getStatusTagType(status) {
      if (!status || status === '成功' || status === 'SUCCESS') return 'success';
      if (status === '告警' || status === 'WARNING') return 'warning';
      if (status === '失败' || status === 'FAILED') return 'danger';
      return 'info';
    }
  }
}
</script>

<style scoped>
.log-management {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  overflow: hidden;
  background: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #303133;
}

/* 顶部标题栏 - 对应红框 1 放大 */
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

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 40px;
  height: 40px;
  background: #e6f7ff;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
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
  padding-right: 12px;
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

.stat-value.text-success .num {
  color: #52c41a !important;
}

.stat-value.text-warning .num {
  color: #fa8c16 !important;
}

.stat-value.text-primary .num {
  color: #1890ff !important;
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

/* 筛选工具栏 */
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
  gap: 10px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: #595959;
}

.time-range-picker {
  width: 260px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 260px;
}

/* 表格容器与样式 */
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
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.user-badge i {
  color: #1890ff;
}

.code-badge {
  font-family: "JetBrains Mono", "SF Mono", Consolas, monospace;
  font-size: 13px;
  color: #1f2937;
  background: #f8fafc;
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid #e2e8f0;
}

.ip-tag {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  color: #595959;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.ip-tag i {
  color: #1890ff;
}

.res-tag {
  font-size: 12px;
}

.op-text {
  font-size: 14px;
  color: #262626;
}

.detail-text {
  font-size: 13px;
  color: #595959;
}

.time-text {
  font-size: 13px;
  color: #8c8c8c;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 状态展示 */
.status-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.status-success .status-dot {
  background-color: #52c41a;
}
.status-success .status-text {
  color: #389e0d;
}

.status-warning .status-dot {
  background-color: #faad14;
}
.status-warning .status-text {
  color: #d46b08;
}

.status-danger .status-dot {
  background-color: #ff4d4f;
}
.status-danger .status-text {
  color: #cf1322;
}

.status-info .status-dot {
  background-color: #d9d9d9;
}
.status-info .status-text {
  color: #8c8c8c;
}

/* 分页 */
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
</style>
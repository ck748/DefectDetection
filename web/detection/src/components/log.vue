<template>
  <div class="log-container">
    <!-- 顶部筛选区域 -->
    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">时间段:</span>
        <div class="date-picker-group">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :picker-options="pickerOptions"
            class="date-picker"
          ></el-date-picker>
          <el-button type="primary" class="confirm-btn" @click="mainTime">确认时间段</el-button>
        </div>
      </div>
      
      <div class="search-group">
        <el-input 
          v-model="searchName" 
          placeholder="搜索主体或工号" 
          class="search-input" 
          prefix-icon="el-icon-search" 
          @input="search"
          clearable
        ></el-input>
      </div>
    </div>
    
    <!-- 表格区域 -->
    <div class="table-section">
      <el-card class="table-card">
        <el-table 
        :data="filteredtableData" 
        style="width: 100%"
        stripe
        v-loading="loading"
        element-loading-text="数据加载中..."
        element-loading-spinner="el-icon-loading"
>
        <el-table-column type="index" label="编号" width="100" align="center" header-align="center"></el-table-column>
        <el-table-column prop="mainRole" label="主体" min-width="120" align="center" header-align="center"></el-table-column>
        <el-table-column prop="label" label="工号或账号" min-width="150" align="center" header-align="center"></el-table-column>
        <el-table-column prop="op" label="操作" min-width="180" align="center" header-align="center"></el-table-column>
        <el-table-column prop="time" label="时间" min-width="160" align="center" header-align="center"></el-table-column>
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
          >
          </el-pagination>
        </div>
      </el-card>
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
          return time.getTime() > Date.now(); // 禁止选择未来日期
        }
      },
    };
  },
  created() {
    this.fetchData();
  },
  computed: {
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
    search() {
      this.searchName = this.searchName.trim();
    },
    mainTime() {
      console.log("这里是时间段哦", this.dateRange);
      if (this.dateRange && this.dateRange.length === 2) {
        let startTime = new Date(this.dateRange[0]); // 将开始时间字符串转换为日期对象
        let endTime = new Date(this.dateRange[1]); // 将结束时间字符串转换为日期对象
        startTime.setHours(23, 59, 59, 999); // 将开始时间设置为当天的23:59:59
        endTime.setHours(23, 59, 59, 999); // 将结束时间设置为当天的23:59:59
        this.dateL = startTime.getTime(); // 获取开始时间的时间戳
        this.dateR = endTime.getTime(); // 获取结束时间的时间戳
        console.log("开始时间的时间戳：", this.dateL);
        console.log("结束时间的时间戳：", this.dateR);
        this.fetchData();
      } else {
        this.$message.warning('请选择完整的时间范围');
      }
    },
    fetchData() {
      this.loading = true;
      axios.get('api/log/info', {
        params: {
          page: this.page,
          pageSize: this.pageSize,
          dateL: this.dateL,
          dateR: this.dateR
        }
      })  // 发起 GET 请求获取数据
        .then(response => {
          this.loading = false;
          if (response.data.code === 200) {
            this.tableData = response.data.data;  // 将获取的数据填充到tableData中
            console.log("22222222222", response.data.data)
            this.total = this.tableData[0].totals;
            console.log("1111111111111111111", this.total)
            this.$message({
              message: '查询到日志信息',
              type: 'success'
            });
          } else {
            this.$message.error("日志信息获取失败")
          }
        })
        .catch(error => {
          this.loading = false;
          console.error('获取数据失败', error);
          this.$message({
            message: '未能查询到日志信息',
            type: 'error'
          });
        });
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.fetchData();
    },
    handleCurrentChange(val) {
      this.page = val;
      this.fetchData();
    }
  }
}
</script>

<style scoped>
.log-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.filter-group {
  display: flex;
  align-items: center;
}

.filter-label {
  font-weight: 500;
  color: #606266;
  margin-right: 12px;
  font-size: 14px;
}

.date-picker-group {
  display: flex;
  align-items: center;
}

.date-picker {
  width: 280px;
  margin-right: 12px;
}

.confirm-btn {
  height: 40px;
  padding: 0 20px;
}

.search-group {
  display: flex;
}

.search-input {
  width: 240px;
}

.table-section {
  margin-top: 16px;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: none;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表格样式优化 */
.el-table {
  font-size: 14px;
}

.el-table th {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-group {
    margin-top: 16px;
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  .date-picker-group {
    flex-direction: column;
    width: 100%;
  }
  
  .date-picker {
    width: 100%;
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .confirm-btn {
    width: 100%;
  }
}
</style>
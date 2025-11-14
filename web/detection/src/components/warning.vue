<template>
  <div class="warning-management-container">
    <div class="main-layout">
      <!-- 左侧预警信息列表 -->
      <div class="warning-list-section">
        <div class="section-header">
          <div class="title-wrapper">
            <i class="el-icon-warning-outline title-icon"></i>
            <h1 class="section-title">预警信息</h1>
          </div>
        </div>
        
        <div class="search-toolbar">
          <el-input 
            v-model="searchName" 
            placeholder="搜索预警名称" 
            class="search-input"
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
          <el-select 
            v-model="searchLevel" 
            placeholder="紧急程度筛选" 
            class="level-select"
            clearable
          >
            <el-option label="全部" :value="null"></el-option>
            <el-option label="紧急程度 3" value="3"></el-option>
            <el-option label="紧急程度 2" value="2"></el-option>
            <el-option label="紧急程度 1" value="1"></el-option>
          </el-select>
        </div>

        <div class="table-container">
          <el-table 
            :data="filteredWarnings" 
            border 
            stripe
            style="width: 100%"
            class="warning-table"
            empty-text="暂无预警信息"
          >
            <el-table-column prop="type" label="预警名称" min-width="120"></el-table-column>
            <el-table-column prop="level" label="紧急程度" width="100" align="center">
              <template slot-scope="scope">
                <el-tag 
                  :type="getLevelTagType(scope.row.level)"
                  effect="dark"
                >
                  {{ scope.row.level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发生时间" width="160" align="center"></el-table-column>
            <el-table-column prop="content" label="预警信息" min-width="180" show-overflow-tooltip></el-table-column>
          </el-table>
        </div>

        <div class="statistics-footer">
          <div class="total-count">
            <i class="el-icon-document"></i>
            总报警数: <span class="count-number">{{ warningsList.length }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧统计和设置 -->
      <div class="right-panel">
        <!-- 告警统计卡片 -->
        <div class="stat-card">
          <el-card shadow="hover" class="stats-card">
            <div slot="header" class="card-header">
              <i class="el-icon-data-analysis header-icon"></i>
              <span class="header-text">告警统计</span>
            </div>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-label">告警总数</div>
                <div class="stat-value">{{ warningsSum }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">24小时内告警数</div>
                <div class="stat-value warning-count">{{ oneDayWarningsSum }}</div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 图表区域 -->
        <div class="chart-section">
          <el-card shadow="hover" class="chart-card">
            <div slot="header" class="card-header">
              <i class="el-icon-data-line header-icon"></i>
              <span class="header-text">紧急程度分布</span>
            </div>
            <div :id="chartId" class="chart-container"></div>
          </el-card>
        </div>

        <!-- 告警通知设置 -->
        <div class="settings-section">
          <el-card shadow="hover" class="settings-card">
            <div slot="header" class="card-header">
              <i class="el-icon-setting header-icon"></i>
              <span class="header-text">告警通知设置</span>
            </div>
            <div class="settings-content">
              <div class="setting-row">
                <div class="setting-item compact">
                  <label class="setting-label">告警通知</label>
                  <el-radio-group v-model="warningsOpen" class="setting-control">
                    <el-radio :label="true" class="custom-radio">启用</el-radio>
                    <el-radio :label="false" class="custom-radio">关闭</el-radio>
                  </el-radio-group>
                </div>

                <div class="setting-item compact">
                  <label class="setting-label">告警级别</label>
                  <el-radio-group v-model="warningsLevel" class="setting-control">
                    <el-radio label="1" class="custom-radio">1</el-radio>
                    <el-radio label="2" class="custom-radio">2</el-radio>
                    <el-radio label="3" class="custom-radio">3</el-radio>
                  </el-radio-group>
                </div>
              </div>

              <div class="setting-row">
                <div class="setting-item compact">
                  <label class="setting-label">通知方式</label>
                  <el-radio-group v-model="Way" class="setting-control">
                    <el-radio label="1" class="custom-radio">手机</el-radio>
                    <el-radio label="2" class="custom-radio">邮箱</el-radio>
                  </el-radio-group>
                </div>
              </div>

              <div class="setting-row">
                <div class="setting-item compact">
                  <label class="setting-label">手机号码</label>
                  <el-input 
                    v-model="phone" 
                    placeholder="请输入手机号码"
                    class="setting-input compact-input"
                    :disabled="Way !== '1'"
                    size="small"
                  ></el-input>
                </div>

                <div class="setting-item compact">
                  <label class="setting-label">邮箱地址</label>
                  <el-input 
                    v-model="email" 
                    placeholder="请输入邮箱地址"
                    class="setting-input compact-input"
                    :disabled="Way !== '2'"
                    size="small"
                  ></el-input>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';

export default {
  data() {
    return {
      warningsOpen: "true",
      warningsLevel: "1",
      Way: "1",
      phone: '',
      email: '',
      searchName: '',
      searchLevel: '',
      warningsList: [],
      warningsSum: 0,
      oneDayWarningsSum: 0,
      levelDistributionData: {},
      notificationLevel: 0,
      notificationMethod: '',
      phoneNumber: '',
      emailAddress: '',
      chartId: 'myChart',
      barChart: {
        title: {
          text: '紧急程度分布'
        },
        tooltip: {},
        xAxis: {
          data: []
        },
        yAxis: {},
        series: [{
          name: '紧急程度分布',
          type: 'bar',
          data: []
        }]
      },
    };
  },
  computed: {
    filteredWarnings() {
      let searchLevel = this.searchLevel;
      if (searchLevel === "null") {
        return this.warningsList;
      }
      if (!this.searchName && !searchLevel) {
        return this.warningsList;
      }
      console.log("this.seratch",this.searchName);
      return this.warningsList.filter(warning => {
        const levelMatch = !searchLevel || warning.level == searchLevel;
        const nameMatch = !this.searchName || (!!warning.type && warning.type.includes(this.searchName.trim().toLowerCase()));
        return nameMatch && levelMatch;
      });
    },
  },
  mounted() {
      this.fetchData();
  },
  watch: {
    warningsOpen(newVal, oldVal) {
      console.log('告警通知开关变化:', newVal);
    },
    warningsLevel(newVal, oldVal) {
      console.log('告警级别变化:', newVal);
    },
    Way(newVal, oldVal) {
      console.log('通知方式变化:', newVal);
    },
    phone(newVal, oldVal) {
      console.log('手机变化:', newVal);
    },
    email(newVal, oldVal) {
      console.log('邮箱变化:', newVal);
    }
  },
  methods:{
    getLevelTagType(level) {
      const typeMap = {
        '1': 'success',
        '2': 'warning',
        '3': 'danger'
      };
      return typeMap[level] || 'info';
    },
    
    fetchData(){
      axios.get('api/detectInfo/warnings/load')
          .then(response => {
            if(response.data.code === 200){
              console.log("11111111111",response.data.data.warningsOpen)
              if(response.data.data.warningsOpen === false){
                this.warningsOpen=false
              }else{
                this.warningsOpen=true
              }
              this.warningsList = response.data.data.warningsList;
              this.warningsSum = response.data.data.warningsSum;
              this.oneDayWarningsSum = response.data.data.oneDayWarningsSum;
              this.warningsLevel = response.data.data.warningsLevel;
              this.phoneWay = response.data.data.phoneWay;
              this.emailWay=response.data.data.emailWay;
              if(this.phoneWay === true){
                this.Way=1;
              }else if(this.emailWay === true){
                this.Way=2
              }else {
                this.Way=0;
              }
              ;
              this.phone = response.data.data.phone;
              this.email = response.data.data.email;
              this.barChart = response.data.data.barChart;
              console.log("1111111112222222222222222",this.warningsOpen,this.warningsLevel,this.Way,this.barChartOptions);
              this.drawChart();
            }else{
              this.$message.error(response.data.message);
            }
          })
          .catch(error => {
            console.error(error);
          });
    },
    drawChart() {
      const xAxisData = [];
      const seriesData = [];
      for (const key in this.barChart.source) {
        if (this.barChart.source.hasOwnProperty(key)) {
          xAxisData.push(key);
          seriesData.push(this.barChart.source[key]);
        }
      }
      const option = {
        title: {
          text: this.barChart.name,
          textStyle: {
            color: '#333',
            fontSize: 14,
            fontWeight: 'normal'
          },
          left: 'center'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLine: {
            lineStyle: {
              color: '#dcdfe6'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#dcdfe6'
            }
          },
          axisLabel: {
            color: '#606266',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0',
              type: 'dashed'
            }
          }
        },
        series: [{
          type: 'bar',
          data: seriesData,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ]),
            borderRadius: [4, 4, 0, 0]
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' }
              ])
            }
          }
        }],
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        }
      };
      const chartDom = document.getElementById(this.chartId);
      const myChart = echarts.init(chartDom);
      myChart.setOption(option);
      
      // 响应式调整
      window.addEventListener('resize', () => {
        myChart.resize();
      });
    },
    sendDataToBackend() {
      const data = {
        warningsOpen: this.warningsOpen,
        warningsLevel: this.warningsLevel,
        Way: this.Way,
        phone: this.phone,
        email: this.email
      };
      axios.put('http://localhost:3000/warningdata', data)
          .then(response => {
            console.log('Data sent to backend successfully:', response.data);
          })
          .catch(error => {
            console.error('Error sending data to backend:', error);
          });
    }
  }
};
</script>

<style scoped>
.warning-management-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 80vh;
}

.main-layout {
  display: flex;
  gap: 20px;
  height: 75vh;
}

/* 左侧预警信息列表 */
.warning-list-section {
  flex: 1;
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  padding: 24px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 20px;
}

.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 24px;
  color: #409eff;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input,
.level-select {
  flex: 1;
}

.table-container {
  flex: 1;
  overflow: hidden;
}

.warning-table {
  height: 100%;
}

.warning-table ::v-deep .el-table__header-wrapper {
  background: #f8f9fa;
}

.warning-table ::v-deep .el-table th {
  background: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

.statistics-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.total-count {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.count-number {
  color: #409eff;
  font-weight: 600;
}

/* 右侧面板 - 压缩高度 */
.right-panel {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 16px; /* 减少间距 */
}

/* 卡片通用样式 */
.stat-card,
.chart-section,
.settings-section {
  width: 100%;
}

.stats-card,
.chart-card,
.settings-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none !important;
  padding: 12px 16px 0; /* 减少内边距 */
}

.header-icon {
  font-size: 16px;
  color: #409eff;
}

.header-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 统计卡片 - 压缩高度 */
.stats-content {
  padding: 12px 16px; /* 减少内边距 */
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0; /* 减少内边距 */
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  font-size: 16px; /* 减小字体 */
  font-weight: 600;
  color: #303133;
}

.warning-count {
  color: #e6a23c;
}

/* 图表区域 - 压缩高度 */
.chart-container {
  width: 100%;
  height: 160px; /* 减小高度 */
  padding: 0 16px 12px; /* 减少内边距 */
}

/* 设置区域 - 压缩高度 */
.settings-content {
  padding: 12px 16px; /* 减少内边距 */
}

.setting-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px; /* 减少间距 */
}

.setting-row:last-child {
  margin-bottom: 0;
}

.setting-item.compact {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px; /* 减少间距 */
}

.setting-label {
  font-size: 13px; /* 减小字体 */
  color: #606266;
  font-weight: 500;
  margin-bottom: 2px;
}

.setting-control {
  display: flex;
  gap: 12px; /* 减少间距 */
}

.custom-radio ::v-deep .el-radio__label {
  font-size: 13px; /* 减小字体 */
}

.setting-input.compact-input ::v-deep .el-input__inner {
  border-radius: 6px;
  height: 32px; /* 减小输入框高度 */
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-layout {
    flex-direction: column;
    height: auto;
  }
  
  .right-panel {
    width: 100%;
    flex-direction: row;
  }
  
  .stat-card,
  .chart-section,
  .settings-section {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .warning-management-container {
    padding: 10px;
  }
  
  .main-layout {
    gap: 15px;
  }
  
  .warning-list-section,
  .stats-card,
  .chart-card,
  .settings-card {
    padding: 16px;
  }
  
  .right-panel {
    flex-direction: column;
  }
  
  .search-toolbar {
    flex-direction: column;
  }
  
  .setting-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
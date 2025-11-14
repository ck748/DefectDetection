<template>
  <div class="dashboard-container">
    <!-- 顶部控制面板 -->
    <div class="control-panel">
      <div class="control-group">
        <label class="control-label">N值:</label>
        <el-input 
          v-model="N" 
          type="text" 
          placeholder="请输入n的值" 
          @blur="setN" 
          class="control-input"
          size="small"
        ></el-input>
      </div>
      <div class="control-group">
        <label class="control-label">工单号:</label>
        <el-input 
          v-model="order" 
          type="text" 
          placeholder="请输入工单号" 
          @blur="setOrder" 
          class="control-input"
          size="small"
        ></el-input>
      </div>
      <div class="control-group">
        <label class="control-label">时间范围:</label>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :picker-options="pickerOptions"
          @change="maintime"
          size="small"
          style="width: 240px;"
        ></el-date-picker>
      </div>
    </div>

    <!-- 图表展示区域 - 只保留4个图表 -->
    <div class="charts-grid">
      <div class="chart-row">
        <div class="chart-card">
          <div class="chart-title">{{ getChartTitle(0) }}</div>
          <div :id="chartIds[0]" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-title">{{ getChartTitle(1) }}</div>
          <div :id="chartIds[1]" class="chart-container"></div>
        </div>
      </div>
      <div class="chart-row">
        <div class="chart-card">
          <div class="chart-title">{{ getChartTitle(2) }}</div>
          <div :id="chartIds[2]" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div class="chart-title">{{ getChartTitle(3) }}</div>
          <div :id="chartIds[3]" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- 底部信息栏 -->
    <div class="footer-info">
      <span class="refresh-time">上次刷新时间：{{ lastRefreshTime }}</span>
      <el-button 
        @click="refresh" 
        type="primary" 
        size="small" 
        icon="el-icon-refresh"
        class="refresh-btn"
      >刷新</el-button>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from "axios";
import moment from 'moment';

export default {
  data() {
    return {
      dateRange: [],
      dateL: null,
      dateR: null,
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
      charts: [],
      // 只保留4个图表ID
      chartIds: ['chart1', 'chart2', 'chart3', 'chart4'],
      N: null,
      startTime: '',
      endTime: '',
      order: '',
      lastRefreshTime: '--'
    };
  },
  computed: {
    isBothInputsFilled() {
      return this.startTime !== '' && this.endTime !== '';
    }
  },
  mounted() {
    this.fetchChartsData();
    this.updateRefreshTime();
  },
  methods: {
    updateRefreshTime() {
      this.lastRefreshTime = moment().format('YYYY-MM-DD HH:mm:ss');
    },
    getChartTitle(index) {
      if (this.charts[index]) {
        return this.charts[index].name;
      }
      return `图表 ${index + 1}`;
    },
    setN() {
      axios.put("api/detectInfo/charts/set", null, {
        params: {
          N: this.N,
          order: this.order
        }
      })
      .then(response => {
        if (response.data.code === 200) {
          this.charts = response.data.data;
          this.renderCharts();
          this.$message.success("N设置成功");
        } else {
          this.$message.error("N设置失败");
        }
      })
      .catch(error => {
        this.$message.error("N设置失败");
      });
    },
    setOrder() {
      axios.put("api/detectInfo/charts/set", null, {
        params: {
          N: this.N,
          order: this.order
        }
      })
      .then(response => {
        if (response.data.code === 200) {
          this.charts = response.data.data;
          this.renderCharts();
          this.$message.success("工单号设置成功");
        } else {
          this.$message.error("工单号设置失败");
        }
      })
      .catch(error => {
        this.$message.error("工单号设置失败");
      });
    },
    maintime() {
      console.log("这里是时间段哦", this.dateRange);
      let startTime = new Date(this.dateRange[0]);
      let endTime = new Date(this.dateRange[1]);
      startTime.setHours(23, 59, 59, 999);
      endTime.setHours(23, 59, 59, 999);
      this.dateL = startTime.getTime();
      this.dateR = endTime.getTime();
      console.log("开始时间的时间戳：", this.dateL);
      console.log("结束时间的时间戳：", this.dateR);
      axios.put("api/detectInfo/charts/set")
      .then(response => {
        if (response.data.code === 200) {
          this.$message.success("时间段设置成功");
        } else {
          this.$message.error("时间段设置失败");
        }
      })
      .catch(error => {
        this.$message.error("时间段设置失败");
      });
    },
    fetchChartsData() {
      fetch('api/detectInfo/charts/load')
      .then(response => {
        if (response.status === 200) {
          return response.json();
        } else {
          throw new Error('状态码非200，无法获取数据');
        }
      })
      .then(data => {
        this.charts = data.data;
        // 如果数据多于4个，只取前4个
        if (this.charts.length > 4) {
          this.charts = this.charts.slice(0, 4);
        }
        this.renderCharts();
        this.updateRefreshTime();
        this.$message({
          message: '查询到报表信息',
          type: 'success'
        });
      })
      .catch(error => {
        console.error(error);
        this.$message({
          message: '未能查询到报表信息',
          type: 'error'
        });
      });
    },
    refresh() {
      this.fetchChartsData();
    },
    renderCharts() {
      // 销毁现有图表实例
      this.charts.forEach((chart, index) => {
        const chartDom = document.getElementById(this.chartIds[index]);
        if (chartDom) {
          const existingChart = echarts.getInstanceByDom(chartDom);
          if (existingChart) {
            existingChart.dispose();
          }
        }
      });
      
      // 渲染新图表
      for (let i = 0; i < this.charts.length; i++) {
        this.renderChart(this.charts[i], this.chartIds[i]);
      }
    },
    renderChart(chart, containerId) {
      const chartDom = document.getElementById(containerId);
      if (!chartDom) return;
      
      const myChart = echarts.init(chartDom);
      let option = {};
      switch(chart.type) {
        case '折线图':
          option = this.createLineOption(chart);
          break;
        case '柱状图':
          option = this.createBarOption(chart);
          break;
        case '环形图':
          option = this.createPieOption(chart);
          break;
        case '热力图':
          option = this.createHeatmapOption(chart);
          break;
      }
      
      // 添加导出配置选项
      option.toolbox = {
        feature: {
          saveAsImage: {
            title: '保存为图片',
            pixelRatio: 2
          },
          dataView: {
            title: '数据视图',
            readOnly: true
          }
        },
        right: 10,
        top: 10
      };
      
      myChart.setOption(option);
      
      // 响应窗口大小变化
      window.addEventListener('resize', () => {
        myChart.resize();
      });
    },
    createLineOption(chart) {
      const xAxisData = [];
      const seriesData = [];
      for (const key in chart.source) {
        if (chart.source.hasOwnProperty(key)) {
          xAxisData.push(key);
          seriesData.push(chart.source[key]);
        }
      }
      
      return {
        title: {
          text: chart.name,
          left: 'center',
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#f5f5f5'
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            return `${params[0].name}: ${params[0].value}`;
          }
        },
        series: [{
          type: 'line',
          data: seriesData,
          smooth: true,
          lineStyle: {
            color: '#5470c6',
            width: 3
          },
          itemStyle: {
            color: '#5470c6'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
              { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
            ])
          }
        }]
      };
    },
    createBarOption(chart) {
      const xAxisData = [];
      const seriesData = [];
      for (const key in chart.source) {
        if (chart.source.hasOwnProperty(key)) {
          xAxisData.push(key);
          seriesData.push(chart.source[key]);
        }
      }

      return {
        title: {
          text: chart.name,
          left: 'center',
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: xAxisData,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          },
          axisLabel: {
            interval: 0,
            rotate: xAxisData.length > 5 ? 30 : 0
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#f5f5f5'
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            return `${params[0].name}: ${params[0].value}`;
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
            ])
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
        }]
      };
    },
    createPieOption(chart) {
      const legendData = Object.keys(chart.source);
      const seriesData = [];
      for (const key in chart.source) {
        if (chart.source.hasOwnProperty(key)) {
          seriesData.push({ name: key, value: chart.source[key] });
        }
      }

      return {
        title: {
          text: chart.name,
          left: 'center',
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        legend: {
          data: legendData,
          top: '10%',
          type: 'scroll'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'], // 环形图，内半径40%，外半径70%
          center: ['50%', '60%'],
          data: seriesData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            }
          },
          label: {
            formatter: '{b}: {d}%'
          },
          // 添加环形图样式
          itemStyle: {
            borderWidth: 2,
            borderColor: '#fff'
          }
        }]
      };
    },
    createHeatmapOption(chart) {
      return {
        title: {
          text: chart.name,
          left: 'center',
          textStyle: {
            fontSize: 14,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          position: 'top'
        },
        xAxis: {
          type: 'category',
          data: chart.source.xAxisData,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          }
        },
        yAxis: {
          type: 'category',
          data: chart.source.yAxisData,
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          }
        },
        visualMap: {
          min: 0,
          max: 100,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '5%',
          padding: [10, 20],
          inRange: {
            color: ['#e0f3ff', '#0066cc']
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '15%',
          containLabel: true
        },
        series: [{
          name: chart.name,
          type: 'heatmap',
          data: chart.source.seriesData,
          label: {
            show: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      };
    }
  }
};
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.control-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.control-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.control-input {
  width: 200px;
}

.charts-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-row {
  display: flex;
  gap: 20px;
}

.chart-card {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.chart-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.chart-title {
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 12px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.refresh-time {
  font-size: 14px;
  color: #909399;
}

.refresh-btn {
  padding: 8px 16px;
}

@media (max-width: 1200px) {
  .chart-row {
    flex-direction: column;
  }
  
  .control-panel {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
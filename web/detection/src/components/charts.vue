<template>
  <div class="smart-inspection-system">
    <div class="system-header">
      <div class="header-decoration"></div>
      <h1 class="system-title">智检控系统</h1>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <div class="main-content">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="score-card tech-border-card" shadow="hover">
            <div class="score-title">
              <i class="el-icon-s-check score-icon"></i>
              安全评分
            </div>
            <div class="score-body">
              <div class="level-box">
                <div class="level-shield">
                  <span class="level-text">{{ securityLevel }}</span>
                </div>
                <div class="level-label">安全等级</div>
              </div>
              <div class="circle-box">
                <div class="circle">
                  <svg viewBox="0 0 100 100">
                    <defs>
                      <linearGradient id="gradStroke" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stop-color="#00f0ff" />
                        <stop offset="60%" stop-color="#4ea7ff" />
                        <stop offset="100%" stop-color="#9b7bff" />
                      </linearGradient>
                    </defs>
                    <circle class="circle-bg" cx="50" cy="50" r="42" />
                    <circle 
                      class="circle-progress" 
                      :stroke-dasharray="dashArray" 
                      cx="50" cy="50" r="42" 
                      stroke="url(#gradStroke)" 
                    />
                  </svg>
                  <div class="circle-score">{{ securityScore }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="defect-info-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="header-title-wrapper">
                <i class="el-icon-warning header-icon"></i>
                <span class="header-title">缺陷信息</span>
                <el-badge 
                  v-if="defectList.length > 0" 
                  :value="defectList.length" 
                  :max="99" 
                  class="defect-badge"
                ></el-badge>
              </div>
            </div>
            <div class="defect-info-content">
              <div class="defect-list-scroll">
                <div class="defect-item" v-for="(defect, index) in defectList" :key="index">
                  <div class="defect-name">
                    <span class="defect-icon">●</span>
                    {{ defect.category || '未知缺陷' }}
                  </div>
                  <div class="defect-percentage">{{ (defect.score * 100).toFixed(1) }}%</div>
                </div>
                <div v-if="defectList.length === 0" class="no-defects">
                  <i class="el-icon-success no-defects-icon"></i>
                  <span class="no-defects-text">暂无缺陷</span>
                </div>
              </div>
              <div class="total-defects">
                <div class="total-title">总缺陷数</div>
                <div class="total-count">{{ defectList.length }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="defect-bar-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="header-title-wrapper">
                <i class="el-icon-data-analysis header-icon"></i>
                <span class="header-title">缺陷分布</span>
              </div>
            </div>
            <div class="chart-section">
              <div id="defectBarChart" class="defect-bar-chart"></div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="monitoring-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="monitoring-header">
                <div class="header-title-wrapper">
                  <i class="el-icon-video-camera header-icon"></i>
                  <span class="header-title">历史轮播</span>
                </div>
              </div>
            </div>
            <div class="image-container">
              <div v-if="currentImageData" class="image-wrapper">
                <img 
                  :src="getBase64ImageUrl(currentImageData)" 
                  alt="监控图像" 
                  class="monitoring-image"
                />
                <div class="image-overlay">
                  <span class="overlay-text">历史轮播</span>
                  <span class="carousel-info">
                    {{ currentCarouselIndex + 1 }}/{{ historyImages.length }}
                  </span>
                </div>
                <div class="hud-lines">
                  <span class="hud-line hud-line-1"></span>
                  <span class="hud-line hud-line-2"></span>
                </div>
                <div v-if="historyImages.length > 1" class="carousel-controls">
                  <el-button @click="prevImage" icon="el-icon-arrow-left" circle size="mini" class="carousel-arrow"></el-button>
                  <el-button @click="nextImage" icon="el-icon-arrow-right" circle size="mini" class="carousel-arrow"></el-button>
                </div>
              </div>
              <div v-else class="no-image">
                <i class="el-icon-picture-outline no-image-icon"></i>
                <p>暂无监控图像</p>
              </div>
            </div>
          </el-card>

          <el-card class="stats-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="header-title-wrapper">
                <i class="el-icon-s-data header-icon"></i>
                <span class="header-title">图片详细信息</span>
              </div>
            </div>
            <div class="table-container">
              <el-table 
                :data="statsData" 
                height="100%"
                class="stats-table"
                :row-class-name="getRowClassName"
                :cell-class-name="getCellClassName"
                empty-text="暂无图片信息"
              >
                <el-table-column prop="score" label="精确度" align="center">
                  <template slot-scope="scope"><span class="defect-count">{{ scope.row.score || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="l" label="横向长度" align="center">
                  <template slot-scope="scope"><span class="defect-rate">{{ scope.row.l || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="h" label="纵向长度" align="center">
                  <template slot-scope="scope"><span class="defect-highlight">{{ scope.row.h || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="x" label="横坐标" align="center">
                  <template slot-scope="scope"><span class="operation-text">{{ scope.row.x || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="y" label="纵坐标" align="center">
                  <template slot-scope="scope"><span class="operation-text">{{ scope.row.y || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="category" label="缺陷名称" align="center">
                  <template slot-scope="scope"><span class="defect-name-text">{{ scope.row.category || '-' }}</span></template>
                </el-table-column>
                <el-table-column prop="categoryId" label="类别编号" align="center">
                  <template slot-scope="scope"><span class="category-id">{{ scope.row.categoryId || '-' }}</span></template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="analysis-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="header-title-wrapper">
                <i class="el-icon-pie-chart header-icon"></i>
                <span class="header-title">缺陷占比分析</span>
              </div>
            </div>
            <div class="chart-section">
              <div id="pieChart" class="pie-chart"></div>
            </div>
          </el-card>

          <el-card class="bar-chart-card tech-border-card" shadow="hover">
            <div slot="header" class="card-header">
              <div class="header-title-wrapper">
                <i class="el-icon-s-marketing header-icon"></i>
                <span class="header-title">缺陷数量统计</span>
              </div>
            </div>
            <div class="chart-section">
              <div id="barChart" class="bar-chart"></div>
            </div>
          </el-card>

          <div class="status-footer tech-border-card">
            <div class="refresh-status">
              <div class="status-item">
                <i class="status-icon" :class="eventSourcePicture && eventSourcePicture.readyState === 1 ? 'el-icon-success' : 'el-icon-error'"></i>
                <span class="status-text">{{ eventSourcePicture && eventSourcePicture.readyState === 1 ? '实时连接中' : '连接断开' }}</span>
              </div>
              <div class="status-item">
                <i class="el-icon-refresh refresh-icon"></i>
                <span class="refresh-text">上次刷新: {{ lastRefreshTime }}</span>
              </div>
              <div class="status-item">
                <i class="el-icon-timer refresh-icon"></i>
                <span class="refresh-text">轮播间隔: 1秒</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog :visible.sync="dialogVisible" title="详细信息" width="65%" class="detail-dialog" :append-to-body="true">
      <el-card class="detail-card">
        <div class="detail-content">
          <div class="image-area">
            <img :src="getBase64ImageUrl(dialogImageUrl)" class="detail-image" alt="详细图片"/>
          </div>
          <div class="info-area">
            <div class="info-item"><span class="info-label">缺陷名称：</span><span class="info-value">{{ tableDataShow.category || '0' }}</span></div>
            <div class="info-item"><span class="info-label">精确度：</span><span class="info-value">{{ tableDataShow.score || '0' }}</span></div>
            <div class="info-item"><span class="info-label">横向长度：</span><span class="info-value">{{ tableDataShow.l || '0' }}</span></div>
            <div class="info-item"><span class="info-label">纵向长度：</span><span class="info-value">{{ tableDataShow.h || '0' }}</span></div>
            <div class="info-item"><span class="info-label">坐标(X,Y)：</span><span class="info-value">{{ tableDataShow.x || 0 }}, {{ tableDataShow.y || 0 }}</span></div>
          </div>
        </div>
      </el-card>
    </el-dialog>

    <el-dialog :visible.sync="dialogVisibleimg" title="放大的图片" width="80%" class="image-dialog" :center="true" :append-to-body="true">
      <div class="image-modal">
        <img :src="getBase64ImageUrl(dialogImageUrl)" class="enlarged-image" alt="放大图片"/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from "axios";
import moment from 'moment';
import sseManager from '@/utils/sseManager';

export default {
  data() {
    return {
      // 监控相关数据
      imageData: null,
      currentImageData: null, // 当前显示的图片数据
      defectList: [],
      statsData: [{
        score: null,
        l: null,
        h: null,
        x: null,
        y: null,
        category: null,
        categoryId: null
      }],
      isConnected: false, // 连接状态
      
      // 图表相关数据
      charts: [],
      lastRefreshTime: '--',
      pieChart: null,
      barChart: null,
      defectBarChart: null,
      currentTime: '',

      // --- 新增：安全评分数据（用于左上角显示）
      securityScore: 95,   // 默认值 95（可由外部接口覆盖）
      securityLevel: '优', // 等级（良/优/差等），同样可由后端设置
      dashArray: '0 0',

      // --- 新增：历史图片轮播相关数据 ---
      historyImages: [], // 存储历史图片数据
      currentCarouselIndex: 0, // 当前轮播索引
      carouselTimer: null, // 轮播定时器
      page: 1, // 分页页码
      pageSize: 20, // 每页数量，获取更多图片用于轮播

      // --- 新增：详细信息弹窗相关数据 ---
      dialogVisible: false,
      dialogVisibleimg: false,
      dialogImageUrl: null,
      tableDataShow: {
        "score": 0.01,
        "l": 0.01,
        "h": 0.01,
        "x": 0.01,
        "y": 0.01,
        "category": "裂缝1",
        "categoryId": 1,
      },
    }
  },
  computed: {
    // 模拟 eventSourcePicture 用于显示连接状态
    eventSourcePicture() {
      return {
        readyState: this.isConnected ? 1 : 0
      };
    }
  },
  mounted() {
    // 订阅全局SSE
    sseManager.subscribe('charts', this.handleSSEMessage);
    this.fetchChartsData();
    this.updateRefreshTime();
    this.updateCurrentTime();
    this.updateCircle(); // 初始化圆环显示
    
    // 自动开始轮播
    this.startAutoCarousel();
    
    // 更新时间
    this._timeTicker = setInterval(() => {
      this.updateCurrentTime();
    }, 1000);
    
    this.$nextTick(() => {
      setTimeout(() => {
        this.renderPieChart();
        this.renderBarChart();
        this.renderDefectBarChart();
      }, 500);
    });

    // 全局 resize -> 统一处理，避免重复监听堆积
    this._onResize = () => {
      if (this.pieChart) this.pieChart.resize();
      if (this.barChart) this.barChart.resize();
      if (this.defectBarChart) this.defectBarChart.resize();
    };
    window.addEventListener('resize', this._onResize);
  },
  beforeDestroy() {
    // 取消订阅
    sseManager.unsubscribe('charts');
    if (this.pieChart) {
      this.pieChart.dispose();
    }
    if (this.barChart) {
      this.barChart.dispose();
    }
    if (this.defectBarChart) {
      this.defectBarChart.dispose();
    }
    if (this._timeTicker) clearInterval(this._timeTicker);
    if (this._onResize) window.removeEventListener('resize', this._onResize);
    this.stopCarousel(); // 清理轮播定时器
  },
  methods: {
    // 监控相关方法
    handleSSEMessage(type, data) {
      if (type === 'connection') {
        // 连接状态变化
        this.isConnected = data.connected;
        if (data.connected) {
          if (this.$message && this.$message.success) this.$message.success('实时连接已建立');
        }
      } else if (type === 'message') {
        // 收到数据
        const imageBase64 = data.imgBase64;
        
        if (imageBase64 !== null && imageBase64 !== undefined && imageBase64 !== '') {
          this.imageData = imageBase64;
          // 更新安全评分和等级（模拟实时变化）
          this.updateSecurityInfo(data);
        }
        
        if (data.defections && Array.isArray(data.defections)) {
          this.defectList = data.defections;
          // 实时更新图表数据
          this.updateChartsWithDefects(data.defections);
        }
        
        // 更新统计信息为实时数据
        if (data.defections && data.defections[0]) {
          this.updateStatsWithDefectDetails(data.defections[0]);
        }
      }
    },

    // 更新安全评分和等级信息
    updateSecurityInfo(data) {
      // 根据缺陷情况动态计算安全评分
      const defectCount = data.defections ? data.defections.length : 0;
      const baseScore = 100;
      const penalty = defectCount * 5; // 每个缺陷扣5分
      this.securityScore = Math.max(40, baseScore - penalty);
      
      // 根据分数确定安全等级
      if (this.securityScore >= 90) {
        this.securityLevel = '优';
      } else if (this.securityScore >= 70) {
        this.securityLevel = '良';
      } else if (this.securityScore >= 60) {
        this.securityLevel = '中';
      } else {
        this.securityLevel = '差';
      }
      
      this.updateCircle();
    },

    // 根据缺陷数据更新图表
    updateChartsWithDefects(defects) {
      if (!defects || defects.length === 0) return;
      
      // 统计缺陷类型
      const defectCounts = {};
      defects.forEach(defect => {
        const category = defect.category || '未知缺陷';
        defectCounts[category] = (defectCounts[category] || 0) + 1;
      });
      
      // 更新饼图数据
      if (this.pieChart) {
        const pieData = Object.keys(defectCounts).map(category => ({
          name: category,
          value: defectCounts[category]
        }));
        
        this.pieChart.setOption({
          series: [{
            data: pieData
          }]
        });
      }
      
      // 更新柱状图数据
      if (this.barChart) {
        const barData = Object.keys(defectCounts).map(category => ({
          name: category,
          value: defectCounts[category]
        }));
        
        this.barChart.setOption({
          yAxis: {
            data: barData.map(item => item.name)
          },
          series: [{
            data: barData.map(item => ({
              value: item.value,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: this.getBarColor(0) },
                  { offset: 1, color: this.getBarColor(0, true) }
                ])
              }
            }))
          }]
        });
      }
      
      // 更新缺陷分布图
      if (this.defectBarChart) {
        const defectTypes = Object.keys(defectCounts);
        const seriesData = [{
          name: '当前',
          type: 'bar',
          data: defectTypes.map(type => defectCounts[type])
        }];
        
        this.defectBarChart.setOption({
          xAxis: {
            data: defectTypes
          },
          series: seriesData
        });
      }
    },

    // --- 新增：历史图片轮播相关方法 ---
    
    // 获取历史图片数据
    async fetchHistoryImages() {
      try {
        const response = await axios.get('api/detectInfo/info/history', {
          params: {
            page: this.page,
            pageSize: this.pageSize
          }
        });
        
        if (response.data.code === 200 && response.data.data) {
          // 过滤出有图片的数据
          const imagesWithData = response.data.data.filter(item => 
            item.imgBase64 && item.imgBase64 !== null && item.imgBase64 !== ''
          );
          
          if (imagesWithData.length > 0) {
            this.historyImages = imagesWithData;
            this.currentCarouselIndex = 0;
            this.currentImageData = this.historyImages[0].imgBase64;
            
            // 更新缺陷信息为第一张图片的数据
            if (this.historyImages[0].defections) {
              this.defectList = this.historyImages[0].defections;
              this.updateChartsWithDefects(this.historyImages[0].defections);
            }
            
            // 更新统计信息为第一张图片的详细信息
            await this.updateStatsWithCurrentImage();
            
            return true;
          } else {
            this.$message.warning('未找到可用的历史图片');
            return false;
          }
        } else {
          this.$message.error('获取历史数据失败');
          return false;
        }
      } catch (error) {
        console.error('获取历史图片失败:', error);
        this.$message.error('获取历史图片失败');
        return false;
      }
    },

    // 自动开始轮播
    async startAutoCarousel() {
      const success = await this.fetchHistoryImages();
      if (success && this.historyImages.length > 0) {
        this.startCarousel();
        this.$message.success('已开启历史图片轮播模式');
      } else {
        this.$message.warning('无法开启轮播模式，没有可用的历史图片');
      }
    },

    // 开始轮播
    startCarousel() {
      this.stopCarousel(); // 先停止现有的定时器
      this.carouselTimer = setInterval(() => {
        this.nextImage();
      }, 1000); // 1秒切换一次
    },

    // 停止轮播
    stopCarousel() {
      if (this.carouselTimer) {
        clearInterval(this.carouselTimer);
        this.carouselTimer = null;
      }
    },

    // 下一张图片
    async nextImage() {
      if (this.historyImages.length === 0) return;
      
      this.currentCarouselIndex = (this.currentCarouselIndex + 1) % this.historyImages.length;
      this.currentImageData = this.historyImages[this.currentCarouselIndex].imgBase64;
      
      // 更新当前图片的缺陷信息
      if (this.historyImages[this.currentCarouselIndex].defections) {
        this.defectList = this.historyImages[this.currentCarouselIndex].defections;
        this.updateChartsWithDefects(this.historyImages[this.currentCarouselIndex].defections);
        this.updateSecurityInfo(this.historyImages[this.currentCarouselIndex]);
      }
      
      // 更新统计信息为当前图片的详细信息
      await this.updateStatsWithCurrentImage();
    },

    // 上一张图片
    async prevImage() {
      if (this.historyImages.length === 0) return;
      
      this.currentCarouselIndex = this.currentCarouselIndex === 0 
        ? this.historyImages.length - 1 
        : this.currentCarouselIndex - 1;
      this.currentImageData = this.historyImages[this.currentCarouselIndex].imgBase64;
      
      // 更新当前图片的缺陷信息
      if (this.historyImages[this.currentCarouselIndex].defections) {
        this.defectList = this.historyImages[this.currentCarouselIndex].defections;
        this.updateChartsWithDefects(this.historyImages[this.currentCarouselIndex].defections);
        this.updateSecurityInfo(this.historyImages[this.currentCarouselIndex]);
      }
      
      // 更新统计信息为当前图片的详细信息
      await this.updateStatsWithCurrentImage();
    },

    // 更新统计信息为当前图片的详细信息
    async updateStatsWithCurrentImage() {
      const currentImage = this.historyImages[this.currentCarouselIndex];
      if (!currentImage) return;
      
      // 获取当前图片的详细信息
      await this.fetchImageDetails(currentImage.id);
      
      // 更新统计表格数据
      this.statsData = [this.tableDataShow];
    },

    // 使用缺陷详情更新统计信息
    updateStatsWithDefectDetails(defect) {
      if (defect) {
        this.statsData = [{
          score: defect.score || 0,
          l: defect.l || 0,
          h: defect.h || 0,
          x: defect.x || 0,
          y: defect.y || 0,
          category: defect.category || '未知',
          categoryId: defect.categoryId || 0
        }];
      }
    },

    // 获取图片详细信息
    async fetchImageDetails(id) {
      try {
        const response = await fetch(`api/detectInfo/info/details?id=${id}`);
        const data = await response.json();
        
        if (data.code === 200) {
          if (data.data.defections && data.data.defections[0]) {
            this.tableDataShow = data.data.defections[0];
          } else {
            // 如果没有缺陷信息，重置为默认值
            this.tableDataShow = {
              score: '0',
              l: '0',
              h: '0',
              x: '0',
              y: '0',
              category: '无缺陷',
              categoryId: '0'
            };
          }
          this.dialogImageUrl = data.data.imgBase64;
        } else {
          console.error('获取图片详情失败:', data);
          // 如果获取详情失败，使用默认值
          this.tableDataShow = {
            score: '0',
            l: '0',
            h: '0',
            x: '0',
            y: '0',
            category: '未知',
            categoryId: '0'
          };
        }
      } catch (error) {
        console.error('获取图片详情请求失败:', error);
        // 如果请求失败，使用默认值
        this.tableDataShow = {
          score: '0',
          l: '0',
          h: '0',
          x: '0',
          y: '0',
          category: '未知',
          categoryId: '0'
        };
      }
    },

    // Base64图片URL处理
    getBase64ImageUrl(base64Data) {
      return `data:image/jpeg;base64,${base64Data}`;
    },

    formatTime(timeStr) {
      if (!timeStr) return '-';
      try {
        const date = new Date(timeStr);
        return date.toLocaleString('zh-CN');
      } catch (e) {
        return timeStr;
      }
    },
    formatTimeSingleLine(timeStr) {
      if (!timeStr) return '-';
      try {
        const date = new Date(timeStr);
        // 使用空格分隔而不是换行
        return date.toLocaleString('zh-CN').replace(/\//g, '-').replace(/:/g, ':');
      } catch (e) {
        return timeStr;
      }
    },
    getRowClassName({ row, rowIndex }) {
      if (rowIndex === 0) {
        return 'summary-row';
      }
      return 'operation-row';
    },
    getCellClassName({ row, column, rowIndex, columnIndex }) {
      // 为第一行（汇总行）添加特殊样式
      if (rowIndex === 0) {
        return 'summary-cell';
      }
      return '';
    },
    updateCurrentTime() {
      this.currentTime = moment().format('YYYY-MM-DD HH:mm:ss dddd');
    },
    
    // 图表相关方法
    updateRefreshTime() {
      this.lastRefreshTime = moment().format('YYYY-MM-DD HH:mm:ss');
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
        this.renderPieChart();
        this.renderBarChart();
        this.renderDefectBarChart();
        this.updateRefreshTime();
        if (this.$message && this.$message.success) this.$message.success('查询到报表信息');
      })
      .catch(error => {
        console.error(error);
        if (this.$message && this.$message.error) this.$message.error('未能查询到报表信息');
      });
    },
    renderPieChart() {
      const chartDom = document.getElementById('pieChart');
      if (!chartDom) return;
      
      if (this.pieChart) {
        this.pieChart.dispose();
      }
      
      this.pieChart = echarts.init(chartDom);
      
      // 环形图数据（默认演示数据，实际由 charts/load 返回填充时请自行替换）
      const pieData = [
        { name: '划痕', value: 35 },
        { name: '点染', value: 28 },
        { name: '氧化', value: 20 },
        { name: '缺失', value: 15 },
        { name: '刻痕', value: 10 }
      ];
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: params => {
            return `<div style="color:#cfeeff;font-size:13px">
                      <div style="font-weight:700">${params.name}</div>
                      <div style="margin-top:6px">${params.value} ( ${params.percent}% )</div>
                    </div>`;
          },
          backgroundColor: 'rgba(3,18,40,0.92)',
          borderColor: 'rgba(0,200,255,0.12)',
          borderWidth: 1,
          textStyle: { color: '#cfeeff' }
        },
        legend: { show: false },
        series: [{
          name: '缺陷占比',
          type: 'pie',
          radius: ['36%', '68%'],
          center: ['50%', '50%'],
          avoidLabelOverlap: false,
          roseType: false,
          clockwise: true,
          hoverOffset: 8,
          startAngle: 120,
          data: pieData.map((d, i) => ({
            value: d.value,
            name: d.name,
            itemStyle: {
              borderColor: '#071a2a',
              borderWidth: 2,
              shadowColor: 'rgba(0, 0, 0, 0.6)',
              shadowBlur: 8
            }
          })),
          label: {
            show: true,
            formatter: '{b}\n{d}%',
            color: '#cfeeff',
            fontSize: 11,
            align: 'center'
          },
          labelLine: {
            length: 8,
            length2: 6,
            smooth: true
          },
          emphasis: {
            scale: true,
            scaleSize: 8,
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(0, 180, 255, 0.3)'
            }
          },
          color: [
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#00e5ff' }, { offset: 1, color: '#0087ff' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#7b61ff' }, { offset: 1, color: '#4b2bff' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f6a2ff' }, { offset: 1, color: '#f04a87' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#3ad3ff' }, { offset: 1, color: '#00a6ff' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#6ef5b8' }, { offset: 1, color: '#28c7a9' }])
          ]
        }]
      };
      
      this.pieChart.setOption(option);
      // gentle appear animation
      this.pieChart.setOption({ series: [{ animationDuration: 900, animationEasing: 'cubicOut' }] });
    },
    renderBarChart() {
      const chartDom = document.getElementById('barChart');
      if (!chartDom) return;
      
      if (this.barChart) {
        this.barChart.dispose();
      }
      
      this.barChart = echarts.init(chartDom);
      
      // 柱状图数据 - 参考意向企业行业分类的样式
      const defectData = [
        { name: '划痕', value: 35 },
        { name: '点染', value: 28 },
        { name: '氧化', value: 20 },
        { name: '缺失', value: 15 },
        { name: '刻痕', value: 10 }
      ];
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: { 
            type: 'shadow',
            shadowStyle: {
              color: 'rgba(0, 150, 255, 0.1)'
            }
          },
          formatter: (params) => {
            return `<div style="color:#cfeeff;font-size:13px;padding:8px;background:rgba(3,18,40,0.95);border:1px solid rgba(0,200,255,0.2);border-radius:4px;">
                      <div style="font-weight:700;margin-bottom:4px;">${params[0].name}</div>
                      <div>数量: <b style="color:#a8e9ff;">${params[0].value}</b></div>
                    </div>`;
          },
          backgroundColor: 'transparent',
          borderWidth: 0
        },
        grid: {
          left: '20%',
          right: '5%',
          bottom: '15%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLine: { 
            show: false
          },
          axisLabel: { 
            color: '#9fcffb', 
            fontSize: 11 
          },
          splitLine: { 
            show: false
          }
        },
        yAxis: {
          type: 'category',
          inverse: true,
          data: defectData.map(item => item.name),
          axisLine: { 
            show: false
          },
          axisTick: { 
            show: false
          },
          axisLabel: { 
            color: '#cfeeff', 
            fontSize: 12,
            margin: 8
          }
        },
        series: [
          {
            name: '缺陷数量',
            type: 'bar',
            data: defectData.map((item, index) => ({
              value: item.value,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: this.getBarColor(index) },
                  { offset: 1, color: this.getBarColor(index, true) }
                ]),
                borderRadius: [0, 4, 4, 0],
                borderWidth: 0
              }
            })),
            barWidth: '45%',
            barGap: '60%',
            barCategoryGap: '40%',
            label: {
              show: true,
              position: 'right',
              formatter: '{c}',
              color: '#e8fbff',
              fontWeight: 700,
              fontSize: 12,
              textShadowColor: 'rgba(0,0,0,0.8)',
              textShadowBlur: 4
            },
            emphasis: {
              itemStyle: {
                shadowColor: 'rgba(0, 200, 255, 0.8)',
                shadowBlur: 15
              }
            }
          }
        ]
      };
      
      this.barChart.setOption(option);
      this.barChart.setOption({ 
        series: [{ 
          animationDuration: 1200, 
          animationEasing: 'elasticOut',
          animationDelay: function (idx) {
            return idx * 150;
          }
        }] 
      });
    },

    // 获取柱状图颜色 - 参考意向企业行业分类的样式
    getBarColor(index, isEnd = false) {
      const colors = [
        { start: '#00f0ff', end: '#0077ff' },
        { start: '#8b6bff', end: '#5a2eff' },
        { start: '#ff9fcf', end: '#ff5b9b' },
        { start: '#38f3ff', end: '#00a2ff' },
        { start: '#8ef6c6', end: '#2bb89a' }
      ];
      const colorSet = colors[index % colors.length];
      return isEnd ? colorSet.end : colorSet.start;
    },

    // ========== 这里是被美化的缺陷分布图（左下） ==========
    renderDefectBarChart() {
      const chartDom = document.getElementById('defectBarChart');
      if (!chartDom) return;
      
      if (this.defectBarChart) {
        this.defectBarChart.dispose();
      }
      
      this.defectBarChart = echarts.init(chartDom);
      
      // 簇状柱形图数据 - 包含所有缺陷类型（保留原始数据顺序与值）
      const defectTypes = ['划痕', '点染', '氧化', '缺失', '刻痕'];
      const seriesData = [
        {
          name: '本周',
          type: 'bar',
          data: [35, 28, 20, 15, 10]
        },
        {
          name: '上周',
          type: 'bar',
          data: [30, 25, 18, 12, 8]
        }
      ];
      
      // 新的科技风样式 option（仅影响视觉，不改接口/数据）
      const option = {
        backgroundColor: 'transparent',
        tooltip: { 
          trigger: 'axis', 
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(3,18,40,0.95)',
          borderColor: 'rgba(0,200,255,0.14)',
          borderWidth: 1,
          textStyle: { color: '#cfeeff' }
        },
        legend: {
          data: ['本周', '上周'],
          textStyle: { color: '#bcdcff', fontSize: 12 },
          right: 10,
          itemWidth: 14,
          itemHeight: 8,
          inactiveColor: '#3b5364'
        },
        grid: {
          left: '12%',
          right: '6%',
          bottom: '16%',
          top: '12%',
          containLabel: true
        },

        // xAxis 使用类目轴，配合科技风刻度与底部发光线
        xAxis: {
          type: 'category',
          data: defectTypes,
          axisLine: { 
            lineStyle: { 
              color: 'rgba(100,180,255,0.14)',
              width: 1
            } 
          },
          axisLabel: { color: '#cbeaff', fontSize: 12 },
          axisTick: { show: false }
        },

        // yAxis 加入淡色虚线网格（科技感）
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisLabel: { color: '#9fcffb' },
          splitLine: { 
            lineStyle: { 
              color: 'rgba(8,30,60,0.08)', 
              type: 'dashed' 
            } 
          }
        },

        series: seriesData.map((series, index) => ({
          ...series,
          barWidth: '36%',
          // 科技风渐变 + 内外发光 - 本周用蓝色，上周用紫色
          itemStyle: {
            borderRadius: 8,
            color: index === 0 ? 
              // 本周 - 蓝色渐变
              new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#00f0ff' }, 
                { offset: 0.5, color: '#0099ff' },
                { offset: 1, color: '#0066ff' }
              ]) :
              // 上周 - 紫色渐变
              new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#b066ff' }, 
                { offset: 0.5, color: '#8a2fff' },
                { offset: 1, color: '#6a00ff' }
              ]),
            // 柱体外发光（霓虹感）
            shadowColor: index === 0 ? 'rgba(0,160,255,0.22)' : 'rgba(120,60,255,0.18)',
            shadowBlur: 18,
            // 边框微妙高亮
            borderColor: 'rgba(255,255,255,0.02)',
            borderWidth: 1
          },
          emphasis: {
            itemStyle: { 
              shadowBlur: 26, 
              shadowColor: index === 0 ? 'rgba(0,160,255,0.28)' : 'rgba(120,60,255,0.24)' 
            }
          },
          // 顶部数值样式（发光+粗体）
          label: {
            show: true,
            position: 'top',
            color: '#e6faff',
            fontWeight: 700,
            fontSize: 12,
            textBorderColor: 'rgba(0,0,0,0.25)',
            textBorderWidth: 2
          },
          // 微妙的动画错开（营造流动感）
          animationDelay: (idx) => idx * 80 + (index * 60),
          animationEasing: 'cubicOut'
        }))
      };
      
      // 使用 setOption 并加入 appearance 动画
      this.defectBarChart.setOption(option);
      this.defectBarChart.setOption({ series: [{ animationDuration: 900, animationEasing: 'cubicOut' }] });
    },

    // ===== 左上角安全评分圆环支持函数 =====
    updateCircle() {
      // 计算 SVG 圆环 stroke-dasharray
      const r = 42;
      const circumference = 2 * Math.PI * r;
      let percent = 0;
      if (typeof this.securityScore === 'number' && !isNaN(this.securityScore)) {
        percent = Math.max(0, Math.min(100, this.securityScore)) / 100;
      }
      const dash = (circumference * percent).toFixed(2);
      this.dashArray = `${dash} ${circumference.toFixed(2)}`;
    }
  }
};
</script>

<script>
import * as echarts from 'echarts';
import axios from "axios";
import moment from 'moment';

export default {
  data() {
    return {
      // 监控相关数据
      imageData: null,
      currentImageData: null, // 当前显示的图片数据
      defectList: [],
      statsData: [{
        score: null, l: null, h: null, x: null, y: null, category: null, categoryId: null
      }],
      eventSourcePicture: null,
      
      // 图表相关数据
      charts: [],
      lastRefreshTime: '--',
      pieChart: null,
      barChart: null,
      defectBarChart: null,
      currentTime: '',

      // 左上角数据
      securityScore: 95,
      securityLevel: '优',
      dashArray: '0 0',

      // 轮播数据
      historyImages: [],
      currentCarouselIndex: 0,
      carouselTimer: null,
      page: 1,
      pageSize: 20,

      // 弹窗数据
      dialogVisible: false,
      dialogVisibleimg: false,
      dialogImageUrl: null,
      tableDataShow: {},
    }
  },
  mounted() {
    // 订阅全局SSE
    sseManager.subscribe('charts', this.handleSSEMessage);
    this.fetchChartsData();
    this.updateRefreshTime();
    this.updateCurrentTime();
    this.updateCircle();
    
    this.startAutoCarousel();
    
    this._timeTicker = setInterval(() => {
      this.updateCurrentTime();
    }, 1000);
    
    this.$nextTick(() => {
      setTimeout(() => {
        this.renderPieChart();
        this.renderBarChart();
        this.renderDefectBarChart();
      }, 500);
    });

    this._onResize = () => {
      if (this.pieChart) this.pieChart.resize();
      if (this.barChart) this.barChart.resize();
      if (this.defectBarChart) this.defectBarChart.resize();
    };
    window.addEventListener('resize', this._onResize);
  },
  beforeDestroy() {
    // 取消订阅
    sseManager.unsubscribe('charts');
    if (this.pieChart) this.pieChart.dispose();
    if (this.barChart) this.barChart.dispose();
    if (this.defectBarChart) this.defectBarChart.dispose();
    if (this._timeTicker) clearInterval(this._timeTicker);
    if (this._onResize) window.removeEventListener('resize', this._onResize);
    this.stopCarousel();
  },
  methods: {
    // --- 核心逻辑保留原有代码，仅展示图表美化部分 ---

    updateSecurityInfo(data) {
      const defectCount = data.defections ? data.defections.length : 0;
      const baseScore = 100;
      const penalty = defectCount * 5;
      this.securityScore = Math.max(40, baseScore - penalty);
      
      if (this.securityScore >= 90) this.securityLevel = '优';
      else if (this.securityScore >= 70) this.securityLevel = '良';
      else if (this.securityScore >= 60) this.securityLevel = '中';
      else this.securityLevel = '差';
      
      this.updateCircle();
    },

    updateChartsWithDefects(defects) {
      if (!defects || defects.length === 0) return;
      const defectCounts = {};
      defects.forEach(defect => {
        const category = defect.category || '未知缺陷';
        defectCounts[category] = (defectCounts[category] || 0) + 1;
      });
      
      // 更新逻辑同原代码，此处省略重复...
      if (this.pieChart) { /* ... */ }
    },

    async fetchHistoryImages() {
      // 保持原有逻辑不变
       try {
        const response = await axios.get('api/detectInfo/info/history', {
          params: { page: this.page, pageSize: this.pageSize }
        });
        if (response.data.code === 200 && response.data.data) {
          const imagesWithData = response.data.data.filter(item => item.imgBase64);
          if (imagesWithData.length > 0) {
            this.historyImages = imagesWithData;
            this.currentCarouselIndex = 0;
            this.currentImageData = this.historyImages[0].imgBase64;
            if (this.historyImages[0].defections) {
              this.defectList = this.historyImages[0].defections;
              this.updateChartsWithDefects(this.historyImages[0].defections);
            }
            await this.updateStatsWithCurrentImage();
            return true;
          }
        }
        return false;
      } catch (error) {
        console.error(error);
        return false;
      }
    },

    startAutoCarousel() { this.fetchHistoryImages().then(ok => { if(ok) this.startCarousel(); }); },
    startCarousel() { this.stopCarousel(); this.carouselTimer = setInterval(() => { this.nextImage(); }, 1000); },
    stopCarousel() { if (this.carouselTimer) { clearInterval(this.carouselTimer); this.carouselTimer = null; } },

    async nextImage() {
      if (this.historyImages.length === 0) return;
      this.currentCarouselIndex = (this.currentCarouselIndex + 1) % this.historyImages.length;
      this.updateCarouselState();
    },
    async prevImage() {
      if (this.historyImages.length === 0) return;
      this.currentCarouselIndex = this.currentCarouselIndex === 0 ? this.historyImages.length - 1 : this.currentCarouselIndex - 1;
      this.updateCarouselState();
    },
    async updateCarouselState() {
      const current = this.historyImages[this.currentCarouselIndex];
      this.currentImageData = current.imgBase64;
      if (current.defections) {
        this.defectList = current.defections;
        this.updateChartsWithDefects(current.defections);
        this.updateSecurityInfo(current);
      }
      await this.updateStatsWithCurrentImage();
    },
    async updateStatsWithCurrentImage() {
      const current = this.historyImages[this.currentCarouselIndex];
      if (current) await this.fetchImageDetails(current.id);
      this.statsData = [this.tableDataShow];
    },
    updateStatsWithDefectDetails(defect) {
      if (defect) this.statsData = [{ ...defect }];
    },
    async fetchImageDetails(id) {
      // 保持原有逻辑
      try {
        const response = await fetch(`api/detectInfo/info/details?id=${id}`);
        const data = await response.json();
        if (data.code === 200) {
          this.tableDataShow = (data.data.defections && data.data.defections[0]) ? data.data.defections[0] : {};
          this.dialogImageUrl = data.data.imgBase64;
        }
      } catch(e) { console.error(e); }
    },
    
    getBase64ImageUrl(base64Data) { return `data:image/jpeg;base64,${base64Data}`; },
    getRowClassName({ rowIndex }) { return rowIndex === 0 ? 'summary-row' : 'operation-row'; },
    getCellClassName({ rowIndex }) { return rowIndex === 0 ? 'summary-cell' : ''; },
    
    updateCurrentTime() { this.currentTime = moment().format('YYYY-MM-DD HH:mm:ss dddd'); },
    updateRefreshTime() { this.lastRefreshTime = moment().format('YYYY-MM-DD HH:mm:ss'); },
    fetchChartsData() { /* 保持原有请求逻辑 */ },

    // ===== ECharts 渲染配置 (美化版) =====
    renderPieChart() {
      const chartDom = document.getElementById('pieChart');
      if (!chartDom) return;
      if (this.pieChart) this.pieChart.dispose();
      this.pieChart = echarts.init(chartDom);
      
      const pieData = [
        { name: '划痕', value: 35 }, { name: '点染', value: 28 },
        { name: '氧化', value: 20 }, { name: '缺失', value: 15 }, { name: '刻痕', value: 10 }
      ];
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item' },
        legend: { show: false },
        series: [{
          name: '缺陷占比',
          type: 'pie',
          radius: ['36%', '68%'],
          center: ['50%', '50%'],
          itemStyle: {
            borderColor: '#071a2a', borderWidth: 2,
            shadowColor: 'rgba(0, 0, 0, 0.5)', shadowBlur: 10
          },
          label: {
            show: true, formatter: '{b}\n{d}%', color: '#cfeeff', fontSize: 11
          },
          labelLine: { length: 10, length2: 8 },
          data: pieData,
          color: ['#00e5ff', '#7b61ff', '#f6a2ff', '#3ad3ff', '#6ef5b8']
        }]
      };
      this.pieChart.setOption(option);
    },
    renderBarChart() {
      const chartDom = document.getElementById('barChart');
      if (!chartDom) return;
      if (this.barChart) this.barChart.dispose();
      this.barChart = echarts.init(chartDom);
      
      const defectData = [
        { name: '划痕', value: 35 }, { name: '点染', value: 28 },
        { name: '氧化', value: 20 }, { name: '缺失', value: 15 }, { name: '刻痕', value: 10 }
      ];
      
      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '20%', right: '12%', bottom: '10%', top: '10%', containLabel: true },
        xAxis: { show: false },
        yAxis: {
          type: 'category', inverse: true, data: defectData.map(i => i.name),
          axisLine: { show: false }, axisTick: { show: false },
          axisLabel: { color: '#cfeeff', fontSize: 12 }
        },
        series: [{
          type: 'bar',
          data: defectData.map(item => ({
            value: item.value,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#00f0ff' }, { offset: 1, color: '#0077ff' }
              ]),
              borderRadius: [0, 4, 4, 0]
            }
          })),
          barWidth: 12,
          label: { show: true, position: 'right', color: '#fff' }
        }]
      };
      this.barChart.setOption(option);
    },
    renderDefectBarChart() {
      const chartDom = document.getElementById('defectBarChart');
      if (!chartDom) return;
      if (this.defectBarChart) this.defectBarChart.dispose();
      this.defectBarChart = echarts.init(chartDom);
      
      const defectTypes = ['划痕', '点染', '氧化', '缺失', '刻痕'];
      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: {
          data: ['本周', '上周'], textStyle: { color: '#bcdcff' }, right: 10, top: 0
        },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: {
          type: 'category', data: defectTypes,
          axisLine: { lineStyle: { color: 'rgba(100,180,255,0.14)' } },
          axisLabel: { color: '#cbeaff' }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)', type: 'dashed' } },
          axisLabel: { color: '#9fcffb' }
        },
        series: [
          {
            name: '本周', type: 'bar', data: [35, 28, 20, 15, 10],
            itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset:0, color:'#00f0ff'},{offset:1, color:'#0066ff'}]) }
          },
          {
            name: '上周', type: 'bar', data: [30, 25, 18, 12, 8],
            itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset:0, color:'#b066ff'},{offset:1, color:'#6a00ff'}]) }
          }
        ]
      };
      this.defectBarChart.setOption(option);
    },
    updateCircle() {
      const r = 42;
      const circumference = 2 * Math.PI * r;
      let percent = Math.max(0, Math.min(100, this.securityScore || 0)) / 100;
      const dash = (circumference * percent).toFixed(2);
      this.dashArray = `${dash} ${circumference.toFixed(2)}`;
    }
  }
};
</script>

<style scoped>
/* =========== 全局大屏基调 ============ */
.smart-inspection-system {
  padding: 20px;
  min-height: 100vh;
  box-sizing: border-box;
  background: radial-gradient(1200px 500px at 10% 10%, rgba(20,80,150,0.25), transparent 10%),
              linear-gradient(180deg, #082548 0%, #0a2c5a 45%, #071c42 100%);
  color: #e6f6ff;
  font-family: "Microsoft YaHei", Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 系统标题 */
.system-header {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 6px 12px;
  border-bottom: 1px solid rgba(0, 242, 255, 0.1);
  background: linear-gradient(90deg, rgba(0,20,40,0.5), transparent);
}

.system-title {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #00f0ff, #4ea7ff 60%, #9b7bff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
  margin: 0;
}

.current-time {
  font-size: 14px;
  color: #bfeaff;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(0,200,255,0.3);
  padding: 6px 16px;
  border-radius: 20px;
  font-family: 'Consolas', monospace;
}

/* ===================== 科技风边框样式 (复用类) ===================== */
.tech-border-card {
  position: relative;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(12,30,58,0.65), rgba(10,24,46,0.55));
  border: 1px solid rgba(50,180,255,0.2);
  box-shadow: 0 8px 30px rgba(2,40,80,0.4), inset 0 0 15px rgba(0, 200, 255, 0.05);
  margin-bottom: 14px;
  overflow: hidden;
}
.tech-border-card::before {
  content: ""; position: absolute; top: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  opacity: 0.5;
}

/* ===================== 通用头部样式 ===================== */
.card-header {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.header-title-wrapper {
  display: flex; align-items: center; gap: 8px; color: #4ea2ff; font-weight: 700; font-size: 16px;
}
.header-icon { color: #00f0ff; font-size: 18px; text-shadow: 0 0 5px #00f0ff; }
.header-title { color: #e6f6ff; letter-spacing: 1px; }

/* ===================== 左侧组件样式 ===================== */
/* 安全评分 */
.score-card {
  padding: 12px 16px; height: 180px; box-sizing: border-box;
  display: flex; flex-direction: column;
}
.score-title {
  display: flex; align-items: center; gap: 8px; color: #4ea2ff; font-weight: 700; font-size: 16px; margin-bottom: 10px;
}
.score-icon { color: #00f0ff; font-size: 18px; }
.score-body {
  display: flex; justify-content: space-around; align-items: center; flex: 1;
}
.level-box { display: flex; flex-direction: column; align-items: center; }
.level-shield {
  width: 70px; height: 80px;
  background: linear-gradient(180deg, #103060, #081830);
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 8px;
  display: flex; justify-content: center; align-items: center;
  box-shadow: 0 0 15px rgba(0,100,255,0.2);
}
.level-text { font-size: 36px; font-weight: 900; color: #00f0ff; text-shadow: 0 0 10px rgba(0,240,255,0.5); }
.level-label { font-size: 12px; color: #8cbde5; margin-top: 5px; }

/* 圆环 */
.circle-box { width: 100px; height: 100px; position: relative; }
.circle { width: 100%; height: 100%; }
svg { transform: rotate(-90deg); width: 100%; height: 100%; }
.circle-bg { fill: none; stroke: rgba(255,255,255,0.1); stroke-width: 8; }
.circle-progress { fill: none; stroke-width: 8; stroke-linecap: round; transition: stroke-dasharray 1s ease; }
.circle-score {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  color: #fff; font-size: 24px; font-weight: 800;
}

/* 缺陷列表 */
.defect-info-card { height: 300px; display: flex; flex-direction: column; }
.defect-info-content { flex: 1; display: flex; flex-direction: column; padding: 10px; }
.defect-list-scroll { flex: 1; overflow-y: auto; padding-right: 5px; }
.defect-item {
  display: flex; justify-content: space-between; padding: 8px;
  background: rgba(0,0,0,0.2); margin-bottom: 6px; border-radius: 4px;
  border-left: 2px solid transparent;
}
.defect-item:hover { background: rgba(0,200,255,0.1); border-left-color: #00f0ff; }
.defect-name { color: #bfeaff; font-size: 13px; display: flex; align-items: center; gap: 5px; }
.defect-icon { color: #ff4d4f; font-size: 12px; }
.defect-percentage { color: #ffd666; font-weight: bold; }

.total-defects {
  margin-top: 10px; padding: 10px; background: rgba(0,100,255,0.1); border-radius: 4px;
  display: flex; justify-content: space-between; align-items: center;
  border: 1px solid rgba(0,200,255,0.2);
}
.total-title { color: #e6f6ff; font-size: 13px; }
.total-count { color: #00f0ff; font-size: 20px; font-weight: 800; }

/* 缺陷分布图 */
.defect-bar-card { height: 280px; }
.defect-bar-chart { width: 100%; height: 220px; }

/* ===================== 中间监控样式 ===================== */
.monitoring-card { margin-bottom: 14px; }
.image-container { padding: 15px; height: 360px; box-sizing: border-box; }
.image-wrapper {
  width: 100%; height: 100%; background: #000; border-radius: 6px; border: 1px solid rgba(0,200,255,0.2);
  position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.monitoring-image { max-width: 100%; max-height: 100%; object-fit: contain; }
.image-overlay {
  position: absolute; top: 10px; right: 10px; background: rgba(0,0,0,0.7);
  padding: 5px 10px; border-radius: 4px; border: 1px solid #00f0ff; color: #fff; font-size: 12px;
  display: flex; flex-direction: column; align-items: flex-end;
}
.carousel-info { color: #ffd666; font-weight: bold; }

/* HUD装饰线 */
.hud-lines .hud-line {
  position: absolute; height: 1px; background: linear-gradient(90deg, transparent, #00f0ff, transparent); opacity: 0.5;
}
.hud-line-1 { top: 20%; left: 10%; width: 80%; }
.hud-line-2 { bottom: 20%; left: 10%; width: 80%; }

.carousel-controls {
  position: absolute; bottom: 10px; display: flex; gap: 15px; z-index: 10;
}
.carousel-arrow {
  background: rgba(0,0,0,0.5) !important; border: 1px solid #00f0ff !important; color: #00f0ff !important;
}
.carousel-arrow:hover { background: #00f0ff !important; color: #000 !important; }

/* ===================== 详细表格样式 ===================== */
.stats-card { height: 400px; display: flex; flex-direction: column; }
.table-container { flex: 1; overflow: hidden; padding: 10px; }
.stats-table { width: 100%; background: transparent; }

/* 深度选择器修改ElementUI表格样式 */
:deep(.el-table), :deep(.el-table__expanded-cell) { background-color: transparent; }
:deep(.el-table tr), :deep(.el-table th) { background-color: transparent; }
:deep(.el-table td), :deep(.el-table th.is-leaf) { border-bottom: 1px solid rgba(255,255,255,0.05); }
:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) { background-color: rgba(0, 200, 255, 0.1); }
:deep(.el-table th) { color: #00f0ff; }
:deep(.el-table) { color: #e6f6ff; }
:deep(.summary-row) { background: rgba(0, 200, 255, 0.05); font-weight: bold; }
.summary-cell { color: #ffd666; }
.defect-count { color: #ff9900; font-weight: bold; }
.defect-rate { color: #ff4d4f; }
.defect-highlight { color: #f56c6c; }
.operation-text { color: #409eff; }

/* ===================== 右侧样式 ===================== */
.analysis-card { height: 280px; }
.pie-chart { width: 100%; height: 220px; }

.bar-chart-card { height: 260px; }
.bar-chart { width: 100%; height: 200px; }

.status-footer {
  padding: 15px; background: rgba(0,0,0,0.2); border-radius: 8px;
  border: 1px solid rgba(0,200,255,0.1);
}
.status-item { display: flex; align-items: center; gap: 10px; margin-bottom: 5px; font-size: 13px; }
.status-icon { font-size: 16px; }
.el-icon-success { color: #67c23a; }
.el-icon-error { color: #f56c6c; }
.refresh-icon { color: #409eff; }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.2); border-radius: 3px; }
::-webkit-scrollbar-track { background: rgba(0,0,0,0.1); }
</style>
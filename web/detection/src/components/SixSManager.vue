<template>
  <div class="sixs-dashboard-hub">
    <!-- 1. 顶部：企业级极简工控 KPI 概览条 (4 核心指标卡) -->
    <div class="top-kpi-bar">
      <div class="kpi-card">
        <div class="kpi-icon-box bg-blue">
          <i class="el-icon-medal"></i>
        </div>
        <div class="kpi-info">
          <div class="kpi-label">6S 综合健康度</div>
          <div class="kpi-val-row">
            <span class="kpi-number text-blue font-mono">98.5</span>
            <span class="kpi-unit">分</span>
            <span class="kpi-custom-badge badge-blue">A+ 卓越</span>
          </div>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-box bg-green">
          <i class="el-icon-connection"></i>
        </div>
        <div class="kpi-info">
          <div class="kpi-label">工控总线与联锁</div>
          <div class="kpi-val-row">
            <span class="kpi-status-text text-green">PROFINET 在线</span>
            <span class="kpi-custom-badge badge-green">正常</span>
          </div>
        </div>
      </div>

      <div class="kpi-card">
        <div class="kpi-icon-box bg-amber">
          <i class="el-icon-document-checked"></i>
        </div>
        <div class="kpi-info">
          <div class="kpi-label">今日点检闭环率</div>
          <div class="kpi-val-row">
            <span class="kpi-number text-amber font-mono">100</span>
            <span class="kpi-unit">%</span>
            <span class="kpi-custom-badge badge-amber">6/6 工位</span>
          </div>
        </div>
      </div>

      <div class="kpi-card action-kpi-card">
        <div class="kpi-action-btns">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-refresh-right"
            :loading="checking"
            @click="runAutoCheck"
            class="action-btn-primary"
          >
            {{ checking ? '全车间巡检中...' : '一键 6S 智能巡检' }}
          </el-button>
          <el-button
            size="small"
            icon="el-icon-download"
            @click="exportReport"
            class="action-btn-outline"
            plain
          >
            导出巡检合规简报
          </el-button>
        </div>
      </div>
    </div>

    <!-- 2. 中部：6S 精益管理执行阶段与工位标准 -->
    <div class="sixs-step-flow-card">
      <div class="flow-header">
        <div class="flow-title">
          <div class="flow-title-icon-badge">
            <i class="el-icon-cpu"></i>
          </div>
          <span class="flow-title-text">6S 精益管理执行阶段与工位标准</span>
        </div>
        <div class="flow-detail-link" @click="viewDetailAction">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="flow-steps-grid">
        <div
          v-for="(item, idx) in sixSItems"
          :key="idx"
          class="step-item-card"
          @click="selectCard(item)"
        >
          <!-- 顶部行：左侧序号钢蓝方块 + 阶段名称，右侧蓝色分数 -->
          <div class="card-top-row">
            <div class="card-left-title-box">
              <span class="step-num-badge font-mono">{{ idx + 1 }}</span>
              <span class="step-title-text">{{ item.name }}</span>
            </div>
            <div class="card-score-text font-mono">{{ item.score }}分</div>
          </div>

          <!-- 中间行：说明规范文本 -->
          <div class="card-desc-text" :title="item.desc">{{ item.desc }}</div>

          <!-- 底部行：右对齐状态标签 -->
          <div class="card-bottom-row">
            <span class="status-outline-tag" :class="'tag-' + item.tagType">{{ item.status }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 底部：左右工作区 (左侧 62% AI 核心工作台，右侧 38% 雷达 + 点检表) -->
    <div class="bottom-dual-layout">
      <!-- 左翼：AI 质检专家与工控调度工作台 (全屏视觉重心) -->
      <div class="left-ai-column">
        <div class="ai-workbench-card">
          <!-- 1. 顶部标题栏 -->
          <div class="ai-wb-header">
            <div class="ai-wb-title-box">
              <div class="ai-wb-icon-badge">
                <i class="el-icon-service"></i>
              </div>
              <div class="ai-wb-title-info">
                <span class="ai-wb-title-text">6S 智能巡检专家工作台</span>
                <span class="ai-wb-status-badge">AI 联锁就绪</span>
              </div>
            </div>
            <div class="ai-wb-actions">
              <button class="ai-clear-btn" @click="clearHistory">
                <i class="el-icon-delete"></i>
                <span>清空对话</span>
              </button>
            </div>
          </div>

          <!-- 2. 快捷指令胶囊栏 -->
          <div class="ai-wb-quick-strip">
            <div class="ai-quick-label">
              <i class="el-icon-magic-stick"></i>
              <span>快捷指令:</span>
            </div>
            <div class="ai-quick-btn-group">
              <button
                v-for="(chip, cIdx) in quickChips"
                :key="cIdx"
                class="ai-quick-pill"
                :class="'chip-' + chip.btnType"
                @click="sendQuickQuestion(chip.cmd)"
              >
                <i :class="chip.icon"></i>
                <span>{{ chip.label }}</span>
              </button>
            </div>
          </div>

          <!-- 3. AI 对话主舞台 (浅蓝沉浸式工作区) -->
          <div class="ai-chat-stage" ref="pageChatBox">
            <div
              v-for="(msg, idx) in messageList"
              :key="idx"
              class="ai-msg-row"
              :class="msg.role"
            >
              <div class="ai-avatar-box" v-if="msg.role === 'assistant'">
                <img :src="logoImg" alt="6S管家" />
              </div>
              <div class="ai-avatar-box user-avatar-box" v-else>
                <img :src="currentUserAvatar" alt="用户" />
              </div>

              <div class="ai-msg-main">
                <div class="ai-msg-meta font-mono">
                  <span class="ai-sender-name">{{ msg.role === 'assistant' ? '6S 数字化管家' : currentUserName }}</span>
                  <span class="ai-msg-time">{{ msg.time }}</span>
                </div>
                <div class="ai-msg-bubble" :class="msg.role">
                  <div class="markdown-render" v-html="formatMessage(msg.content)"></div>
                </div>
              </div>
            </div>

            <!-- 思考中动态 -->
            <div class="ai-msg-row assistant" v-if="isThinking">
              <div class="ai-avatar-box">
                <img :src="logoImg" alt="6S管家" />
              </div>
              <div class="ai-msg-main">
                <div class="ai-msg-meta font-mono">
                  <span class="ai-sender-name">6S 数字化管家</span>
                  <span class="ai-msg-time">THINKING</span>
                </div>
                <div class="ai-msg-bubble assistant thinking-bubble">
                  <i class="el-icon-loading"></i>
                  <span>{{ thinkingText }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 4. 底部智能输入与调度区 -->
          <div class="ai-wb-compose-area">
            <div class="ai-compose-input-wrapper">
              <el-input
                type="textarea"
                :rows="2"
                v-model="inputQuestion"
                placeholder="向 6S 管家提问，如：半轴标定台整顿标准、工业相机镜片清洁防错、机械臂安全复位..."
                resize="none"
                class="ai-copilot-input"
                @keydown.enter.native.exact.prevent="handleSend"
              ></el-input>
            </div>
            <div class="ai-compose-bottom-bar">
              <div class="ai-kbd-tips font-mono">
                <kbd class="kbd-badge">Enter</kbd> <span>发送</span> <span class="kbd-divider">/</span> <kbd class="kbd-badge">Shift</kbd>+<kbd class="kbd-badge">Enter</kbd> <span>换行</span>
              </div>
              <button
                class="ai-send-primary-btn"
                :disabled="isThinking || !inputQuestion.trim()"
                @click="handleSend"
              >
                <i class="el-icon-position"></i>
                <span>发送指令</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右翼：数据监测与分析区 (雷达健康度 + 点检流水表格) -->
      <div class="right-data-column">
        <!-- 雷达图容器 -->
        <div class="analysis-card radar-card">
          <div class="card-header-clean">
            <div class="header-left">
              <div class="header-icon-box bg-blue-subtle">
                <i class="el-icon-pie-chart text-blue"></i>
              </div>
              <span class="header-title-text">6S 车间工位健康度雷达</span>
            </div>
            <div class="header-right font-mono">
              <span class="rate-label">达成率:</span>
              <span class="rate-value font-mono">98.5%</span>
            </div>
          </div>
          <div class="radar-chart-stage" ref="radarChart"></div>
        </div>

        <!-- 点检流水表容器 -->
        <div class="analysis-card table-card">
          <div class="card-header-clean">
            <div class="header-left">
              <div class="header-icon-box bg-green-subtle">
                <i class="el-icon-tickets text-green"></i>
              </div>
              <span class="header-title-text">车间 6S 点检与标准执行流水</span>
            </div>
            <span class="kpi-custom-badge badge-green">实时校验</span>
          </div>
          <div class="table-container">
            <el-table
              :data="checkTableData"
              size="small"
              stripe
              style="width: 100%"
              class="styled-sixs-table"
            >
              <el-table-column label="分类" width="75" align="center">
                <template slot-scope="{ row }">
                  <span class="mini-status-tag" :class="'tag-' + row.tagType">{{ row.category }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="station" label="受检工位" width="120" show-overflow-tooltip>
                <template slot-scope="{ row }">
                  <span class="station-cell-text">{{ row.station }}</span>
                </template>
              </el-table-column>
              <el-table-column label="点检与执行标准" min-width="190" show-overflow-tooltip>
                <template slot-scope="{ row }">
                  <div class="table-primary-item">{{ row.item }}</div>
                  <div class="table-sub-std">{{ row.standard }}</div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="{ row }">
                  <span class="status-pass-pill">
                    <span class="status-dot"></span> {{ row.status }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" align="center">
                <template slot-scope="{ row }">
                  <el-button type="text" size="mini" class="guide-action-link" @click="handleDetail(row)">指引</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'SixSManager',
  data() {
    return {
      logoImg: require('@/assets/logo.png'),
      defaultAvatar: require('@/assets/头像.jpg'),
      checking: false,
      activeCard: 'seiri',
      inputQuestion: '',
      isThinking: false,
      thinkingText: '正在调阅 6S 管理标准规范与工位数据...',
      typingTimer: null,
      isTyping: false,
      radarChartInstance: null,
      sixSItems: [
        { name: '整理 (Seiri)', tagType: 'primary', desc: '区分要与不要 清除非必需品', score: 98, status: '达标', icon: 'el-icon-sort', prompt: '请给出当前半轴质检工位的【整理(Seiri)】执行要点与不要物清理规范。', type: 'seiri' },
        { name: '整顿 (Seiton)', tagType: 'success', desc: '三定管理 减震卡槽定置定位', score: 99, status: '优秀', icon: 'el-icon-menu', prompt: '半轴缺陷标定区与合格品库房的【整顿(Seiton)】三定管理要求是什么？', type: 'seiton' },
        { name: '清扫 (Seiso)', tagType: 'warning', desc: '相机镜头除尘 无水乙醇擦拭', score: 97, status: '达标', icon: 'el-icon-brush', prompt: '光学检测相机镜头与半轴转台的每日【清扫(Seiso)】防尘标准是？', type: 'seiso' },
        { name: '清洁 (Seiketsu)', tagType: 'success', desc: '制度化维持 班前班后点检', score: 100, status: '满分', icon: 'el-icon-circle-check', prompt: '如何通过看板与日常点检制度维持检测系统的【清洁(Seiketsu)】状态？', type: 'seiketsu' },
        { name: '素养 (Shitsuke)', tagType: 'info', desc: '严守操作规程 佩戴无尘手套', score: 98, status: '达标', icon: 'el-icon-user', prompt: '质检员在系统操作与样本标定过程中的【素养(Shitsuke)】行为准则。', type: 'shitsuke' },
        { name: '安全 (Safety)', tagType: 'danger', desc: 'AGV避障与安全光栅联锁锁定', score: 100, status: '满分', icon: 'el-icon-warning-outline', prompt: 'AGV运检协同与机械手旋转上下料中的【安全(Safety)】防碰与急停规程。', type: 'safety' }
      ],
      quickChips: [
        { label: '机械臂复位', icon: 'el-icon-refresh', btnType: 'primary', cmd: '请你将机械臂复位' },
        { label: '分拣小车归位', icon: 'el-icon-position', btnType: 'warning', cmd: '请将分拣小车归位' },
        { label: '生成自检评分', icon: 'el-icon-document', btnType: 'success', cmd: '生成当前页面的6S自检评分报告' },
        { label: '相机镜头保养', icon: 'el-icon-camera', btnType: 'info', cmd: '检测相机镜头清洁保养规程' },
        { label: 'AGV安全避障', icon: 'el-icon-warning-outline', btnType: 'danger', cmd: 'AGV运检协同与安全避障规范' }
      ],
      messageList: [
        {
          role: 'assistant',
          content: '您好！欢迎进入 **6S 数字化智能管家** 🦾。\n本工作台已接入车间工业物联网总线与 6S 精益管控体系，支持：\n1. **6S 现场执行规范智能问答**（整理、整顿、清扫、清洁、素养、安全）\n2. **工位自动化协同控制**（支持下发机械臂原点复位、AGV 分拣小车归位）\n3. **光学感知设备维护标准及自检报告输出**\n\n请点击上方快捷指令或直接输入问题咨询！',
          time: this.getNowTime()
        }
      ],
      checkTableData: [
        { category: '整理', tagType: 'primary', station: '1号全周质检站', item: '红牌作战与待检品区分离', standard: '非检验半轴及杂物4小时内清理', status: '正常' },
        { category: '整顿', tagType: 'success', station: '2号标定工作台', item: '标定工具定置定位与EVA卡槽', standard: '量具100%归槽，缺陷样件上锁', status: '正常' },
        { category: '清扫', tagType: 'warning', station: '光学成像检测箱', item: '工业相机镜头与偏振滤镜擦拭', standard: '每日班前使用无水乙醇单向擦拭', status: '正常' },
        { category: '清洁', tagType: 'success', station: '主控机柜与看板', item: '点检卡常态化打卡记录', standard: '班前5分钟确认，班后10分钟维持', status: '正常' },
        { category: '素养', tagType: 'info', station: '质检操作工位', item: '防静电服与无尘手套佩戴', standard: '严禁裸手直接接触精磨半轴表面', status: '正常' },
        { category: '安全', tagType: 'danger', station: 'AGV运检交互区', item: '激光雷达避障与安全光栅联锁', standard: '0.6米急停触发，光栅遮断0.1s制动', status: '正常' }
      ]
    };
  },
  computed: {
    currentUser() {
      try {
        return JSON.parse(localStorage.getItem('useradmin') || '{}');
      } catch (e) {
        return {};
      }
    },
    currentUserName() {
      return this.currentUser.name || this.currentUser.username || '管理员';
    },
    currentUserAvatar() {
      return this.currentUser.avatar || this.defaultAvatar;
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initRadarChart();
      this.scrollToBottom();
      window.addEventListener('resize', this.handleResize);
    });
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.radarChartInstance) {
      this.radarChartInstance.dispose();
    }
    if (this.typingTimer) {
      clearInterval(this.typingTimer);
    }
  },
  methods: {
    getNowTime() {
      const d = new Date();
      return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
    },
    selectCard(item) {
      this.activeCard = item.type;
      this.sendQuickQuestion(item.prompt);
    },
    sendQuickQuestion(text) {
      this.inputQuestion = text;
      this.handleSend();
    },
    clearHistory() {
      this.messageList = [
        {
          role: 'assistant',
          content: '对话记录已清空。我是您的 **6S管家**，随时为您提供现场规范指导与控制联动。',
          time: this.getNowTime()
        }
      ];
    },
    handleSend() {
      const q = (this.inputQuestion || '').trim();
      if (!q || this.isThinking || this.isTyping) return;

      this.messageList.push({
        role: 'user',
        content: q,
        time: this.getNowTime()
      });
      this.inputQuestion = '';

      const isResetArm = q.includes('机械臂复位') || q.includes('复位机械臂') || q.includes('将机械臂复位');
      const isResetCart = q.includes('小车归位') || q.includes('分拣小车归位') || q.includes('小车复位') || q.includes('归位分拣小车');

      if (isResetArm) {
        this.thinkingText = '正在连接工控PLC总线，下发机械臂六轴原点复位校准指令...';
      } else if (isResetCart) {
        this.thinkingText = '正在调度AGV小车导航系统，下发分拣小车原点归位指令...';
      } else {
        this.thinkingText = '正在调阅 6S 精益管理规范与当前工位传感数据...';
      }

      this.isThinking = true;
      this.$nextTick(() => this.scrollToBottom());

      setTimeout(() => {
        const fullReply = this.generate6SAnswer(q);
        this.isThinking = false;

        const assistantMsg = {
          role: 'assistant',
          content: '',
          time: this.getNowTime()
        };
        this.messageList.push(assistantMsg);
        this.startTypewriter(assistantMsg, fullReply);
      }, 1000);
    },
    startTypewriter(msgObj, fullText) {
      this.isTyping = true;
      let currentIndex = 0;
      const totalLen = fullText.length;
      if (this.typingTimer) {
        clearInterval(this.typingTimer);
      }

      this.typingTimer = setInterval(() => {
        if (currentIndex < totalLen) {
          const step = Math.min(2, totalLen - currentIndex);
          currentIndex += step;
          msgObj.content = fullText.slice(0, currentIndex);
          this.$nextTick(() => this.scrollToBottom());
        } else {
          clearInterval(this.typingTimer);
          this.typingTimer = null;
          this.isTyping = false;
          msgObj.content = fullText;
          this.$nextTick(() => this.scrollToBottom());
        }
      }, 16);
    },
    generate6SAnswer(query) {
      if (query.includes('机械臂复位') || query.includes('复位机械臂') || query.includes('将机械臂复位')) {
        return `好的，已通过工控总线为全周检测工位下发指令：**机械臂六轴已平稳复位至初始原点**，伺服抱闸锁定正常，处于待命就绪状态！🦾`;
      }
      if (query.includes('小车归位') || query.includes('分拣小车归位') || query.includes('小车复位') || query.includes('归位分拣小车') || query.includes('AGV小车归位')) {
        return `好的，已为车间分拣单元下发调度指令：**分拣小车已安全调度归位**，随时准备下一批次缺陷品转运！🛺`;
      }
      if (query.includes('整理') || query.includes('Seiri')) {
        return `### 📌 【6S·整理 (Seiri)】半轴质检工位实施规范：\n1. **红牌作战机制**：对连续 3 批次未检/无法标定半轴挂设红牌，4小时内移至待查隔离区；\n2. **要与不要分类**：工作台上严禁摆放私人水杯、非检验图纸、已失效标定工具；\n3. **空间释放**：检测机柜周围 1.2 米内禁止堆叠闲置纸箱，保障机柜散热与巡检通道畅通。`;
      }
      if (query.includes('整顿') || query.includes('Seiton')) {
        return `### 📌 【6S·整顿 (Seiton)】定置定位管理标准：\n1. **定置三要素**：\n   - **定点**：卡尺/测头必须放置于专用 EVA 减震定位槽；\n   - **定容**：合格品放入绿色周转箱，缺陷品放入黄色锁扣防错周转箱；\n   - **定量**：暂存工位半轴堆叠上限为 6 件，杜绝超载碰伤。\n2. **可视化标识**：地坪黄色警戒线 100mm 规范施划，目视化率达到 100%。`;
      }
      if (query.includes('清扫') || query.includes('Seiso') || query.includes('清洁规程') || query.includes('相机')) {
        return `### 📌 【6S·清扫 (Seiso)】光学感知与机械台点检规程：\n1. **工业相机与光源清扫**：\n   - 每日开班前使用无水乙醇配合专用镜头纸顺时针单向擦拭；\n   - 严禁使用普通棉纱擦拭高精度光学滤镜。\n2. **转台与导轨除屑**：使用工业吸尘器清理金属毛刺，严禁用高压气枪直接吹扫轴承间隙。\n3. **清扫即点检**：清扫过程中同步检查螺栓有无松动、气压表指针是否在 0.6±0.05 MPa 正常区间。`;
      }
      if (query.includes('清洁') || query.includes('Seiketsu')) {
        return `### 📌 【6S·清洁 (Seiketsu)】长效常态化机制：\n1. 坚持前 3S（整理、整顿、清扫）的成果标准化；\n2. 每日实行 **「班前5分钟确认，班后10分钟维持」** 责任包干制；\n3. 质检系统已开启自动巡检日志，每周五下午生成 6S 数字化综合诊断红黑榜。`;
      }
      if (query.includes('素养') || query.includes('Shitsuke')) {
        return `### 📌 【6S·素养 (Shitsuke)】质检人员行为规程：\n1. 严格遵守半轴外观缺陷判定基准（GB/T 38885）；\n2. 严禁未经授权修改 AI 缺陷识别置信度阈值（当前阈值锁定 ≥0.85）；\n3. 作业过程穿戴防静电服与丁腈无尘手套，严禁裸手接触精加工半轴表面。`;
      }
      if (query.includes('安全') || query.includes('Safety') || query.includes('AGV')) {
        return `### 📌 【6S·安全 (Safety)】智能运检联锁安全防线：\n1. **AGV激光避障**：AGV 行进路径 1.5 米内感应减速，0.6 米内触发硬级联急停；\n2. **机械臂联锁**：全周质检工作站安全光栅遮断时，伺服主轴 0.1s 内制动锁定；\n3. **用电与接地**：大功率高频光源与计算服务器外壳接地电阻需 ＜4Ω，杜绝静电击穿。`;
      }
      if (query.includes('报告') || query.includes('自检') || query.includes('评分')) {
        return `### 📊 【当前车间 6S 数字化自检诊断简报】\n* **评定等级**：**A级·卓越 (98.5分)**\n* **明细指标达成情况**：\n  - 整理(Seiri)：98.0%（通道通畅无积压）\n  - 整顿(Seiton)：99.0%（量具与样本定置定位率100%）\n  - 清扫(Seiso)：97.0%（相机镜头通透无油污积尘）\n  - 清洁(Seiketsu)：100.0%（标准化看板与点检执行规范）\n  - 素养(Shitsuke)：98.0%（作业人员严守安全与品质规范）\n  - 安全(Safety)：100.0%（安全光栅、急停与避障联锁零隐患）\n* **管家行动建议**：建议 14:30 针对 2 号标定工位开展例行气吹维护。`;
      }
      return `收到关于「**${query}**」的咨询。\n根据当前车间 6S 规范与工位执行标准：\n1. 请确认现场物料与工具已严格执行 **定点、定容、定量**；\n2. 如检测相机有微弱反光或模糊，请先执行 **6S 清扫标准（无水乙醇擦拭）**；\n3. 需深入调阅某一细项标准，可直接点击上方 6S 卡片或快捷指令！`;
    },
    formatMessage(text) {
      if (!text) return '';
      return text
        .replace(/### (.*?)\n/g, '<div class="md-heading"><i class="el-icon-guide"></i> $1</div>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br/>');
    },
    scrollToBottom() {
      const box = this.$refs.pageChatBox;
      if (box) {
        box.scrollTop = box.scrollHeight;
      }
    },
    initRadarChart() {
      if (!this.$refs.radarChart) return;
      this.radarChartInstance = echarts.init(this.$refs.radarChart);
      const option = {
        tooltip: {
          trigger: 'item',
          backgroundColor: '#fff',
          borderColor: 'rgba(35, 136, 232, 0.3)',
          borderWidth: 1,
          padding: [8, 12],
          textStyle: { color: '#1C3047', fontSize: 12 },
          extraCssText: 'box-shadow: 0 6px 16px rgba(50, 110, 165, 0.08); border-radius: 8px;',
          formatter: (params) => {
            let str = `<div style="font-weight:700;margin-bottom:6px;color:#2388e8">${params.seriesName}</div>`;
            const indicators = ['整理', '整顿', '清扫', '清洁', '素养', '安全'];
            params.value.forEach((v, i) => {
              str += `<div style="display:flex;justify-content:space-between;gap:16px;font-size:11.5px;line-height:1.6;">
                <span style="color:#64748b">${indicators[i]}:</span>
                <span style="font-weight:700;color:#2388e8">${v}分</span>
              </div>`;
            });
            return str;
          }
        },
        radar: {
          indicator: [
            { name: '整理 (Seiri)', max: 100 },
            { name: '整顿 (Seiton)', max: 100 },
            { name: '清扫 (Seiso)', max: 100 },
            { name: '清洁 (Seiketsu)', max: 100 },
            { name: '素养 (Shitsuke)', max: 100 },
            { name: '安全 (Safety)', max: 100 }
          ],
          radius: '62%',
          center: ['50%', '52%'],
          splitNumber: 4,
          axisName: {
            color: '#64748b',
            fontWeight: 600,
            fontSize: 11.5
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(35, 136, 232, 0.16)'
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(35, 136, 232, 0.02)', 'rgba(35, 136, 232, 0.04)', 'rgba(35, 136, 232, 0.02)', 'rgba(35, 136, 232, 0.06)']
            }
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(35, 136, 232, 0.18)'
            }
          }
        },
        series: [
          {
            name: '车间6S健康度',
            type: 'radar',
            data: [
              {
                value: [98, 99, 97, 100, 98, 100],
                name: '实测评分',
                symbol: 'circle',
                symbolSize: 5,
                itemStyle: {
                  color: '#2388e8',
                  borderColor: '#fff',
                  borderWidth: 2
                },
                lineStyle: {
                  width: 2.2,
                  color: '#2388e8'
                },
                areaStyle: {
                  color: 'rgba(35, 136, 232, 0.28)'
                }
              },
              {
                value: [90, 90, 90, 90, 90, 95],
                name: '基准目标线',
                symbol: 'none',
                lineStyle: {
                  type: 'dashed',
                  width: 1.2,
                  color: '#94a3b8'
                }
              }
            ]
          }
        ]
      };
      this.radarChartInstance.setOption(option);
    },
    handleResize() {
      if (this.radarChartInstance) {
        this.radarChartInstance.resize();
      }
    },
    viewDetailAction() {
      this.sendQuickQuestion('请生成 6S 精益管理执行阶段与工位标准详情报告');
    },
    runAutoCheck() {
      this.checking = true;
      this.$message.info('正在联动车间各传感器，开始全自动 6S 智能巡检...');
      setTimeout(() => {
        this.checking = false;
        this.$message.success('全车间 6S 智能巡检完成！6大工位点检无异常，综合评级 A+ 级 (98.5分)。');
      }, 1200);
    },
    exportReport() {
      this.$message.success('已生成《车间6S数字化精益合规巡检诊断简报》并导出！');
    },
    handleDetail(row) {
      this.sendQuickQuestion(`请详细说明【${row.station}】在【${row.category}】方面的标准要求及操作指导。`);
    }
  }
};
</script>

<style scoped>
/* ================= 1. 全局设计系统 Token 与重置 ================= */
.font-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.text-blue { color: #2388e8 !important; }
.text-green { color: #48bb78 !important; }
.text-amber { color: #f59e0b !important; }

.sixs-dashboard-hub {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: transparent;
  box-sizing: border-box;
}

/* ================= 2. 统一微胶囊与状态标签 ================= */
.kpi-custom-badge {
  font-size: 11.5px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 8px !important;
  line-height: 1.3;
  display: inline-flex;
  align-items: center;
}

.badge-blue {
  background: #eaf4ff;
  color: #1d72b8;
  border: 1px solid rgba(35, 136, 232, 0.2);
}

.badge-green {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid rgba(72, 187, 120, 0.2);
}

.badge-amber {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

/* ================= 3. 顶部 4 列 KPI 紧凑概览条 ================= */
.top-kpi-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.kpi-card {
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(244, 249, 255, 0.94));
  border-radius: 18px !important;
  border: 1px solid rgba(180, 210, 238, 0.35);
  padding: 16px 20px;
  min-height: 106px;
  height: 106px;
  display: flex;
  align-items: center;
  box-shadow: 0 10px 26px rgba(50, 110, 165, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  box-sizing: border-box;
}

.kpi-card::after {
  content: "";
  position: absolute;
  right: -25px;
  bottom: -30px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(35, 136, 232, 0.06) 0%, rgba(35, 136, 232, 0) 70%);
  pointer-events: none;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(50, 110, 165, 0.1);
  border-color: rgba(35, 136, 232, 0.35);
}

.kpi-icon-box {
  flex: 0 0 54px;
  width: 54px;
  height: 54px;
  margin-right: 16px;
  border-radius: 14px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.kpi-icon-box.bg-blue {
  background: #eaf4ff;
  color: #2388e8;
  border: 1px solid rgba(35, 136, 232, 0.18);
}

.kpi-icon-box.bg-green {
  background: #f0fdf4;
  color: #48bb78;
  border: 1px solid rgba(72, 187, 120, 0.18);
}

.kpi-icon-box.bg-amber {
  background: #fffbeb;
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.18);
}

.kpi-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 14px;
  color: #4e647a;
  margin-bottom: 4px;
  font-weight: 600;
  letter-spacing: 0.1px;
}

.kpi-val-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  position: relative;
}

.kpi-number {
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.5px;
}

.kpi-unit {
  font-size: 13.5px;
  color: #8a9aaf;
  font-weight: 600;
}

.kpi-status-text {
  font-size: 19px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.2px;
}

.kpi-val-row .kpi-custom-badge {
  margin-left: auto;
  align-self: center;
}

.action-kpi-card {
  padding: 12px 18px;
}

.kpi-action-btns {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1;
}

.action-btn-primary {
  width: 100%;
  height: 38px;
  margin: 0 !important;
  background: linear-gradient(135deg, #2588ea 0%, #1577d7 100%) !important;
  border: none !important;
  border-radius: 10px !important;
  color: #ffffff !important;
  font-size: 13.5px !important;
  font-weight: 600 !important;
  box-shadow: 0 6px 14px rgba(34, 132, 225, 0.22) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.2s ease;
}

.action-btn-primary:hover {
  background: linear-gradient(135deg, #3b9bff 0%, #1985ee 100%) !important;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(34, 132, 225, 0.3) !important;
}

.action-btn-outline {
  width: 100%;
  height: 34px;
  margin: 0 !important;
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(180, 210, 238, 0.5) !important;
  border-radius: 10px !important;
  color: #4e647a !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.2s ease;
}

.action-btn-outline:hover {
  border-color: #2388e8 !important;
  color: #2388e8 !important;
  background: #ffffff !important;
  box-shadow: 0 4px 10px rgba(35, 136, 232, 0.08);
}

/* ================= 4. 中部 6S 流程步骤连贯卡片 ================= */
.sixs-step-flow-card {
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
}

.flow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.flow-title {
  font-size: 15.5px;
  font-weight: 700;
  color: #1c3047;
  display: flex;
  align-items: center;
  gap: 8px;
}

.flow-title-icon-badge {
  width: 24px;
  height: 24px;
  border-radius: 7px;
  background: #eaf4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.flow-title-icon-badge i {
  color: #2388e8;
  font-size: 14px;
}

.flow-title-text {
  font-size: 15.5px;
  font-weight: 700;
  color: #1c3047;
}

.flow-detail-link {
  font-size: 13px;
  color: #2388e8;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.flow-detail-link:hover {
  opacity: 0.8;
}

.flow-steps-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.step-item-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(246, 250, 255, 0.92));
  border: 1px solid rgba(180, 210, 238, 0.35);
  border-radius: 14px !important;
  padding: 12px 14px 10px 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 108px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(50, 110, 165, 0.03);
  box-sizing: border-box;
}

.step-item-card:hover {
  transform: translateY(-2px);
  border-color: rgba(35, 136, 232, 0.4);
  box-shadow: 0 8px 20px rgba(50, 110, 165, 0.08);
}

.card-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.card-left-title-box {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.step-num-badge {
  width: 22px;
  height: 22px;
  border-radius: 6px !important;
  background: #64748b;
  color: #ffffff;
  font-size: 12.5px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title-text {
  font-size: 13.5px;
  font-weight: 700;
  color: #1c3047;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-score-text {
  font-size: 13.5px;
  font-weight: 800;
  color: #2388e8;
  flex-shrink: 0;
}

.card-desc-text {
  font-size: 11.5px;
  color: #8a9aaf;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 4px 0 6px 0;
}

.card-bottom-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.status-outline-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 6px !important;
  line-height: 1.4;
  display: inline-block;
}

.tag-primary {
  background: #eaf4ff;
  color: #2388e8;
  border: 1px solid rgba(35, 136, 232, 0.25);
}

.tag-success {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid rgba(72, 187, 120, 0.25);
}

.tag-warning {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid rgba(245, 158, 11, 0.25);
}

.tag-danger {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.25);
}

.tag-info {
  background: #f8fafc;
  color: #475569;
  border: 1px solid #cbd5e1;
}

/* ================= 5. 底部左右工作区 (左 62% 主工作台 / 右 38% 分析区) ================= */
.bottom-dual-layout {
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 16px;
}

/* ================= 左翼：AI 工作台 (全页视觉中心) ================= */
.ai-workbench-card {
  height: 610px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(244, 250, 255, 0.96));
  border: 1px solid rgba(180, 215, 245, 0.45);
  border-radius: 22px !important;
  box-shadow: 0 12px 36px rgba(50, 110, 165, 0.07);
  display: flex;
  flex-direction: column;
  padding: 18px 20px;
  box-sizing: border-box;
  overflow: hidden;
  position: relative;
}

.ai-wb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(226, 238, 250, 0.8);
  flex-shrink: 0;
}

.ai-wb-title-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-wb-icon-badge {
  width: 44px;
  height: 44px;
  border-radius: 13px !important;
  background: #eaf4ff;
  border: 1px solid rgba(35, 136, 232, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-wb-icon-badge i {
  color: #2388e8;
  font-size: 22px;
}

.ai-wb-title-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-wb-title-text {
  font-size: 16.5px;
  font-weight: 700;
  color: #1c3047;
  letter-spacing: 0.2px;
}

.ai-wb-status-badge {
  font-size: 11.5px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px !important;
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid rgba(72, 187, 120, 0.25);
}

.ai-wb-actions {
  display: flex;
  align-items: center;
}

.ai-clear-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #8a9aaf;
  font-size: 12.5px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px !important;
  transition: all 0.2s;
}

.ai-clear-btn:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.08);
}

.ai-wb-quick-strip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  flex-shrink: 0;
}

.ai-quick-label {
  font-size: 12.5px;
  font-weight: 700;
  color: #4e647a;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-quick-btn-group {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}

.ai-quick-btn-group::-webkit-scrollbar {
  display: none;
}

.ai-quick-pill {
  border: 1px solid #d9d9d9;
  background: #ffffff;
  border-radius: 8px !important;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.ai-quick-pill:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
}

.ai-quick-pill.chip-primary { color: #2388e8; border-color: rgba(35, 136, 232, 0.28); background: #eaf4ff; }
.ai-quick-pill.chip-primary:hover { background: #d6ebff; border-color: #2388e8; }

.ai-quick-pill.chip-warning { color: #d97706; border-color: rgba(245, 158, 11, 0.28); background: #fffbeb; }
.ai-quick-pill.chip-warning:hover { background: #fef3c7; border-color: #f59e0b; }

.ai-quick-pill.chip-success { color: #16a34a; border-color: rgba(72, 187, 120, 0.28); background: #f0fdf4; }
.ai-quick-pill.chip-success:hover { background: #dcfce7; border-color: #16a34a; }

.ai-quick-pill.chip-info { color: #475569; border-color: #cbd5e1; background: #f8fafc; }
.ai-quick-pill.chip-info:hover { background: #f1f5f9; border-color: #94a3b8; }

.ai-quick-pill.chip-danger { color: #dc2626; border-color: rgba(239, 68, 68, 0.28); background: #fef2f2; }
.ai-quick-pill.chip-danger:hover { background: #fee2e2; border-color: #dc2626; }

/* AI 对话主舞台 */
.ai-chat-stage {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f1f7fe 0%, #eaf3fd 100%);
  border-radius: 16px !important;
  border: 1px solid rgba(195, 222, 248, 0.45);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-msg-row {
  display: flex;
  gap: 12px;
  max-width: 94%;
}

.ai-msg-row.assistant {
  align-self: flex-start;
}

.ai-msg-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-avatar-box {
  width: 38px;
  height: 38px;
  border-radius: 12px !important;
  background: #ffffff;
  border: 1px solid rgba(195, 222, 248, 0.6);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.ai-avatar-box img {
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.ai-avatar-box.user-avatar-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ai-msg-main {
  display: flex;
  flex-direction: column;
}

.ai-msg-meta {
  font-size: 11px;
  color: #8a9aaf;
  margin-bottom: 4px;
  display: flex;
  gap: 6px;
}

.ai-msg-row.user .ai-msg-meta {
  justify-content: flex-end;
}

.ai-msg-bubble {
  padding: 12px 16px;
  border-radius: 14px !important;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.ai-msg-bubble.assistant {
  background: #ffffff;
  color: #1c3047;
  border: 1px solid rgba(219, 234, 254, 0.7);
  box-shadow: 0 4px 14px rgba(70, 130, 180, 0.05);
}

.ai-msg-bubble.user {
  background: linear-gradient(135deg, #2388e8 0%, #1572cf 100%);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(35, 136, 232, 0.25);
}

.thinking-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4e647a;
}

.ai-wb-compose-area {
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.ai-compose-input-wrapper {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(180, 210, 238, 0.45);
  border-radius: 14px !important;
  padding: 2px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.ai-copilot-input ::v-deep .el-textarea__inner {
  font-size: 13px;
  padding: 8px 10px;
  border: none !important;
  border-radius: 12px !important;
  background: transparent !important;
  color: #1c3047;
}

.ai-copilot-input ::v-deep .el-textarea__inner::placeholder {
  color: #8a9aaf;
}

.ai-compose-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ai-kbd-tips {
  font-size: 11px;
  color: #8a9aaf;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kbd-badge {
  background: #ffffff;
  color: #4e647a;
  padding: 2px 5px;
  border-radius: 4px !important;
  border: 1px solid #cbd5e1;
  font-size: 10px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.kbd-divider {
  color: #cbd5e1;
  margin: 0 2px;
}

.ai-send-primary-btn {
  height: 38px;
  padding: 0 18px;
  border: none;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #2588ea 0%, #1577d7 100%);
  color: #ffffff;
  font-size: 13.5px;
  font-weight: 600;
  box-shadow: 0 6px 14px rgba(34, 132, 225, 0.22);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.ai-send-primary-btn:hover {
  background: linear-gradient(135deg, #3b9bff 0%, #1985ee 100%);
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(34, 132, 225, 0.3);
}

.ai-send-primary-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* ================= 6. 右翼：数据分析区 (雷达与点检流水) ================= */
.analysis-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(246, 250, 255, 0.94));
  border-radius: 20px !important;
  border: 1px solid rgba(180, 210, 238, 0.35);
  box-shadow: 0 10px 28px rgba(50, 110, 165, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.card-header-clean {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(226, 238, 250, 0.7);
  background: transparent;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon-box {
  width: 24px;
  height: 24px;
  border-radius: 7px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-blue-subtle {
  background: #eaf4ff;
}

.bg-green-subtle {
  background: #f0fdf4;
}

.header-title-text {
  font-size: 14px;
  font-weight: 700;
  color: #1c3047;
}

.header-right {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.rate-label {
  font-size: 12px;
  color: #8a9aaf;
}

.rate-value {
  font-size: 14px;
  font-weight: 800;
  color: #16a34a;
}

.radar-card {
  height: 240px;
  margin-bottom: 14px;
}

.radar-chart-stage {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.table-card {
  height: 266px;
}

.table-container {
  flex: 1;
  min-height: 0;
  padding: 8px 12px;
  overflow-y: auto;
}

.styled-sixs-table {
  background: transparent !important;
}

.styled-sixs-table ::v-deep tr {
  background: transparent !important;
}

.styled-sixs-table ::v-deep th.el-table__cell {
  background-color: rgba(244, 249, 255, 0.8) !important;
  color: #4e647a !important;
  font-weight: 700;
  font-size: 12px;
  padding: 6px 0 !important;
  border-bottom: 1px solid rgba(226, 238, 250, 0.8) !important;
}

.styled-sixs-table ::v-deep td.el-table__cell {
  padding: 6px 0 !important;
  font-size: 12px;
  border-bottom: 1px solid rgba(235, 243, 250, 0.6) !important;
}

.station-cell-text {
  font-weight: 600;
  color: #1c3047;
}

.table-primary-item {
  font-weight: 600;
  color: #1c3047;
  line-height: 1.3;
}

.table-sub-std {
  font-size: 10.5px;
  color: #8a9aaf;
  margin-top: 2px;
}

.mini-status-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 5px !important;
  display: inline-block;
}

.status-pass-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid rgba(72, 187, 120, 0.25);
  padding: 1px 7px;
  border-radius: 6px !important;
  font-weight: 600;
  font-size: 11px;
}

.status-dot {
  width: 5px;
  height: 5px;
  border-radius: 50% !important;
  background: #16a34a;
}

.guide-action-link {
  color: #2388e8;
  font-weight: 600;
}

.guide-action-link:hover {
  color: #1577d7;
  text-decoration: underline;
}
</style>

<template>
  <div class="six-s-butler-wrapper">
    <!-- 悬浮球 (打开抽屉时自动平滑隐藏，关闭抽屉时重新显示) -->
    <transition name="ball-fade">
      <div
        v-show="!drawerVisible"
        ref="floatBall"
        class="butler-ball"
        :class="{ 'is-dragging': isDragging, 'pulse-animate': !drawerVisible }"
        :style="ballStyle"
        @mousedown="startDrag"
        @click="toggleDrawer"
        title="6S管家 - 工业质检AI专家"
      >
        <!-- 核心发光球体 -->
        <div class="ball-inner">
          <!-- 相对路径引入的高清机器人 Logo，100% 正圆充满，禁用原生图片拖拽残影 -->
          <img
            :src="logoImg"
            class="butler-logo-img"
            alt="6S管家"
            draggable="false"
            @dragstart.prevent
          />
        </div>
        <!-- 徽标提示 -->
        <div class="ball-badge">6S管家</div>
      </div>
    </transition>

    <!-- 侧边栏抽屉 (深度匹配日志管理/API管理/密钥管理纯白清爽现代卡片风格) -->
    <el-drawer
      :visible.sync="drawerVisible"
      direction="rtl"
      size="440px"
      custom-class="six-s-drawer-light"
      :with-header="false"
      :modal-append-to-body="true"
      :append-to-body="true"
      @close="onDrawerClose"
    >
      <div class="drawer-container-light">
        <!-- 头部 (纯白现代卡片顶栏) -->
        <div class="drawer-header-light">
          <div class="header-left">
            <div class="mini-logo-box">
              <img :src="logoImg" class="mini-logo" alt="logo" />
            </div>
            <div class="header-text">
              <div class="title-wrap">
                <span class="title">6S管家 · AI 智能体</span>
                <span class="title-tag">质检现场巡检</span>
              </div>
              <div class="sub-title">车间现场 6S 合规规范 · 缺陷工艺排查诊断专家</div>
            </div>
          </div>
          <div class="header-actions">
            <i class="el-icon-close close-btn" @click="drawerVisible = false"></i>
          </div>
        </div>

        <!-- 6S 状态感知栏 (白底高质感卡片) -->
        <div class="context-banner-light">
          <div class="context-item">
            <span class="label">当前所处区域：</span>
            <span class="context-tag route-tag">{{ currentPageName }}</span>
          </div>
          <div class="context-item">
            <span class="label">6S现场评级：</span>
            <span class="context-tag grade-tag"><span class="dot"></span>优秀 (A级·98.5分)</span>
          </div>
        </div>

        <!-- 6S 标准六大快捷卡片 (白底悬浮微投影现代网格) -->
        <div class="six-s-grid-light">
          <div
            v-for="(item, idx) in sixSItems"
            :key="idx"
            class="six-s-card-light"
            :class="item.type"
            @click="sendQuickQuestion(item.prompt)"
          >
            <div class="card-pinyin">{{ item.en }}</div>
            <div class="card-name">{{ item.name }}</div>
            <div class="card-desc">{{ item.desc }}</div>
          </div>
        </div>

        <!-- 对话聊天区 (浅灰高雅工作台背景) -->
        <div class="chat-container-light" ref="chatBox">
          <div
            v-for="(msg, idx) in messageList"
            :key="idx"
            class="chat-bubble-wrapper"
            :class="msg.role"
          >
            <!-- 助手头像 -->
            <div class="avatar" v-if="msg.role === 'assistant'">
              <img :src="logoImg" class="avatar-logo" alt="6S" />
            </div>
            <!-- 用户头像 (动态同步当前登录账号头像) -->
            <div class="avatar user-avatar" v-else>
              <img :src="currentUserAvatar" class="avatar-logo" alt="用户头像" />
            </div>

            <!-- 消息主体 -->
            <div class="bubble-content">
              <div class="sender-name">{{ msg.role === 'assistant' ? '6S管家' : currentUserName }}</div>
              <div class="msg-text-light" v-html="formatMessage(msg.content)"></div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- 思考中状态 -->
          <div class="chat-bubble-wrapper assistant" v-if="isThinking">
            <div class="avatar">
              <img :src="logoImg" class="avatar-logo" alt="6S" />
            </div>
            <div class="bubble-content">
              <div class="sender-name">6S管家</div>
              <div class="msg-text-light thinking">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
                {{ thinkingText }}
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷问答 Chips (白底药丸微标签) -->
        <div class="quick-chips-light">
          <div
            class="chip-item-light"
            v-for="(chip, idx) in currentChips"
            :key="idx"
            @click="sendQuickQuestion(chip)"
          >
            <i class="el-icon-chat-dot-round chip-icon"></i> {{ chip }}
          </div>
        </div>

        <!-- 底部输入框 (纯白现代操作栏) -->
        <div class="drawer-footer-light">
          <el-input
            v-model="inputQuestion"
            type="textarea"
            :rows="2"
            placeholder="向6S管家提问，如：工位整顿标准、划痕缺陷清洁规范..."
            resize="none"
            class="custom-textarea"
            @keyup.enter.native="handleSend"
          />
          <div class="footer-bar">
            <span class="hint">Enter 发送 / Shift + Enter 换行</span>
            <el-button type="primary" size="small" :loading="isThinking" @click="handleSend" icon="el-icon-s-promotion">
              咨询管家
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: 'SixSButler',
  data() {
    return {
      // 相对路径引入 Logo
      logoImg: require('@/assets/logo.png'),
      defaultAvatar: require('@/assets/头像.jpg'),
      drawerVisible: false,
      isDragging: false,
      hasDragged: false,
      startPos: { x: 0, y: 0 },
      // 默认停靠右下角
      position: {
        x: window.innerWidth - 86,
        y: window.innerHeight - 150
      },
      inputQuestion: '',
      isThinking: false,
      thinkingText: '正在调阅 6S 管理标准规范与当前工位数据...',
      sixSItems: [
        { name: '整理', en: 'SEIRI', desc: '区分要与不要 腾出空间', prompt: '请给出当前半轴质检工位的【整理(Seiri)】执行要点与不要物清理规范。', type: 'seiri' },
        { name: '整顿', en: 'SEITON', desc: '定容定量定位 标识清晰', prompt: '半轴缺陷标定区与合格品库房的【整顿(Seiton)】三定管理要求是什么？', type: 'seiton' },
        { name: '清扫', en: 'SEISO', desc: '扫除工位垃圾 查漏防错', prompt: '光学检测相机镜头与半轴转台的每日【清扫(Seiso)】防尘标准是？', type: 'seiso' },
        { name: '清洁', en: 'SEIKETSU', desc: '制度化常态化 维持3S', prompt: '如何通过看板与日常点检制度维持检测系统的【清洁(Seiketsu)】状态？', type: 'seiketsu' },
        { name: '素养', en: 'SHITSUKE', desc: '养成良好习惯 严守规程', prompt: '质检员在系统操作与样本标定过程中的【素养(Shitsuke)】行为准则。', type: 'shitsuke' },
        { name: '安全', en: 'SAFETY', desc: '预防设备事故 消除隐患', prompt: 'AGV运检协同与机械手旋转上下料中的【安全(Safety)】防碰与急停规程。', type: 'safety' }
      ],
      messageList: [
        {
          role: 'assistant',
          content: '您好！我是您的 **6S管家 AI智能体** 🦾。\n我已实时连接当前半轴质检数字化车间，随时为您提供 **现场6S合规咨询**、**缺陷工艺排查标准** 及 **工位安全防差错指导**。请问有什么可以协助您？',
          time: this.getNowTime()
        }
      ],
      quickChips: [
        '请你将机械臂复位',
        '请将分拣小车归位',
        '半轴划痕缺陷工位6S应急处置',
        '生成当前页面的6S自检评分报告',
        '检测相机镜头清洁保养规程'
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
    },
    ballStyle() {
      return {
        left: `${this.position.x}px`,
        top: `${this.position.y}px`
      };
    },
    currentPageName() {
      const metaName = this.$route && this.$route.meta && this.$route.meta.name;
      return metaName || '质检管控系统';
    },
    currentChips() {
      return this.quickChips;
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('mousemove', this.onDrag);
    window.addEventListener('mouseup', this.stopDrag);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    window.removeEventListener('mousemove', this.onDrag);
    window.removeEventListener('mouseup', this.stopDrag);
  },
  methods: {
    getNowTime() {
      const d = new Date();
      return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
    },
    handleResize() {
      const maxX = window.innerWidth - 70;
      const maxY = window.innerHeight - 70;
      if (this.position.x > maxX) this.position.x = maxX;
      if (this.position.y > maxY) this.position.y = maxY;
    },
    startDrag(e) {
      this.isDragging = true;
      this.hasDragged = false;
      this.startPos = {
        x: e.clientX - this.position.x,
        y: e.clientY - this.position.y
      };
    },
    onDrag(e) {
      if (!this.isDragging) return;
      this.hasDragged = true;
      let newX = e.clientX - this.startPos.x;
      let newY = e.clientY - this.startPos.y;

      const maxX = window.innerWidth - 68;
      const maxY = window.innerHeight - 68;
      if (newX < 10) newX = 10;
      if (newX > maxX) newX = maxX;
      if (newY < 10) newY = 10;
      if (newY > maxY) newY = maxY;

      this.position.x = newX;
      this.position.y = newY;
    },
    stopDrag() {
      if (this.isDragging) {
        this.isDragging = false;
      }
    },
    toggleDrawer() {
      if (this.hasDragged) {
        this.hasDragged = false;
        return;
      }
      this.drawerVisible = !this.drawerVisible;
      if (this.drawerVisible) {
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    sendQuickQuestion(text) {
      this.inputQuestion = text;
      this.handleSend();
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

      // 判断是否为机械臂复位 / 分拣小车归位等工控控制指令
      const isResetArm = q.includes('机械臂复位') || q.includes('复位机械臂') || q.includes('将机械臂复位');
      const isResetCart = q.includes('小车归位') || q.includes('分拣小车归位') || q.includes('小车复位') || q.includes('归位分拣小车');

      if (isResetArm) {
        this.thinkingText = '正在连接工控PLC总线，下发机械臂六轴原点复位校准指令...';
      } else if (isResetCart) {
        this.thinkingText = '正在调度AGV小车导航系统，下发分拣小车原点归位指令...';
      } else {
        this.thinkingText = '正在调阅 6S 管理标准规范与当前工位数据...';
      }

      this.isThinking = true;
      this.$nextTick(() => this.scrollToBottom());

      // 1. 深度思考与假装操作 3000ms (3秒左右)
      setTimeout(() => {
        const fullReply = this.generate6SAnswer(q);
        this.isThinking = false;

        // 2. 插入一条空的助手消息，开启打字机效果逐字呈现
        const assistantMsg = {
          role: 'assistant',
          content: '',
          time: this.getNowTime()
        };
        this.messageList.push(assistantMsg);
        this.startTypewriter(assistantMsg, fullReply);
      }, 3000);
    },
    startTypewriter(msgObj, fullText) {
      this.isTyping = true;
      let currentIndex = 0;
      const totalLen = fullText.length;
      // 打字步长与间隔：每 22ms 输出 1~2 个字符，自然丝滑
      if (this.typingTimer) {
        clearInterval(this.typingTimer);
      }

      this.typingTimer = setInterval(() => {
        if (currentIndex < totalLen) {
          // 动态打字步长（遇换行或标点微调节奏）
          const step = Math.min(2, totalLen - currentIndex);
          currentIndex += step;
          msgObj.content = fullText.slice(0, currentIndex);
          this.$nextTick(() => this.scrollToBottom());
        } else {
          // 打字完成
          clearInterval(this.typingTimer);
          this.typingTimer = null;
          this.isTyping = false;
          msgObj.content = fullText;
          this.$nextTick(() => this.scrollToBottom());
        }
      }, 22);
    },
    onDrawerClose() {
      // 抽屉关闭时悬浮球自动显现
      this.drawerVisible = false;
    },
    generate6SAnswer(query) {
      // 响应机械臂复位指令（语气自然亲和、智能管家化）
      if (query.includes('机械臂复位') || query.includes('复位机械臂') || query.includes('将机械臂复位')) {
        return `好的，已为您将机械臂复位至初始原点，各轴伺服状态正常，系统待命就绪！🦾`;
      }
      // 响应分拣小车/AGV小车归位指令
      if (query.includes('小车归位') || query.includes('分拣小车归位') || query.includes('小车复位') || query.includes('归位分拣小车') || query.includes('AGV小车归位')) {
        return `好的，已为您将分拣小车调度归位，当前位置标定正常，随时可执行下一批次工单配送！🛺`;
      }
      if (query.includes('整理') || query.includes('Seiri')) {
        return `### 📌 【6S·整理 (Seiri)】半轴质检工位实施规范：\n1. **红牌作战**：对连续 3 批次未检/无法标定半轴挂设红牌，4小时内移至待查隔离区；\n2. **要与不要分类**：工作台上严禁摆放私人水杯、非检验图纸、已失效标定工具；\n3. **空间释放**：检测机柜周围 1.2 米内禁止堆叠闲置纸箱，保障机柜散热与巡检通道畅通。`;
      }
      if (query.includes('整顿') || query.includes('Seiton')) {
        return `### 📌 【6S·整顿 (Seiton)】定置定位管理标准：\n1. **定置三要素**：\n   - 定点：卡尺/测头必须放置于专用 EVA 减震定位槽；\n   - 定容：合格品放入绿色周转箱，缺陷品放入黄色锁扣防错周转箱；\n   - 定量：暂存工位半轴堆叠上限为 6 件，杜绝超载碰伤。\n2. **可视化标识**：地坪黄色警戒线 100mm 规范施划，目视化率达到 100%。`;
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
        return `### 📊 【当前页面 6S 数字化自检诊断简报】\n* **受检工位**：${this.currentPageName} (ID: ST-04)\n* **当前得分**：**98.5分 (优+)**\n* **明细评估**：\n  - 整理(Seiri)：10/10（物料区分明确）\n  - 整顿(Seiton)：9.5/10（缺陷样本定置箱已上锁）\n  - 清扫(Seiso)：10/10（镜头透光率无衰减）\n  - 清洁(Seiketsu)：10/10（点检规程执行中）\n  - 素养(Shitsuke)：9.5/10（操作日志全量留痕）\n  - 安全(Safety)：10/10（光栅与急停状态正常）\n* **管家建议**：建议 14:00 准时对 2 号工位进行例行镜头气吹维护。`;
      }
      return `收到关于「**${query}**」的咨询。\n根据车间 6S 规范与当前【${this.currentPageName}】工序标准：\n1. 请确认现场物料已严格执行 **定点、定容、定量**；\n2. 如遇突发缺陷波动，请先对检测工装转台进行 **6S 清扫除屑**，并一键提交异常预警；\n3. 需深入调阅具体标准细则，可直接点击上方 6S 卡片或直接提问！`;
    },
    formatMessage(text) {
      if (!text) return '';
      let html = text
        .replace(/### (.*?)\n/g, '<div class="md-h3">$1</div>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br/>');
      return html;
    },
    scrollToBottom() {
      const box = this.$refs.chatBox;
      if (box) {
        box.scrollTop = box.scrollHeight;
      }
    }
  }
};
</script>

<style scoped>
/* 悬浮球容器 */
.six-s-butler-wrapper {
  position: relative;
  z-index: 99999;
}

/* 核心球体样式 (现代质感白蓝光圈圆球) */
.butler-ball {
  position: fixed;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  cursor: pointer;
  user-select: none;
  touch-action: none;
  z-index: 99999;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.butler-ball:hover {
  transform: scale(1.1);
}

.butler-ball.is-dragging {
  cursor: grabbing;
  transform: scale(1.15);
}

/* 纯白晶莹圆球底衬 */
.ball-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.35), 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 2px solid #1890ff;
  position: relative;
  overflow: hidden;
  background: #ffffff;
}

/* 机器人图片 Logo：充满圆形容器，50% 纯正圆角无黑边 */
.butler-logo-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

/* 悬浮呼吸动画 */
.pulse-animate {
  animation: floatPulse 3.6s ease-in-out infinite;
}

@keyframes floatPulse {
  0% {
    transform: translateY(0);
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.45);
  }
  50% {
    transform: translateY(-5px);
    box-shadow: 0 0 0 8px rgba(24, 144, 255, 0);
  }
  100% {
    transform: translateY(0);
    box-shadow: 0 0 0 0 rgba(24, 144, 255, 0);
  }
}

/* 底部徽标 (天蓝现代徽标) */
.ball-badge {
  position: absolute;
  bottom: -6px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #ffffff;
  font-size: 10px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 10px;
  border: 1.5px solid #ffffff;
  white-space: nowrap;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.4);
  pointer-events: none;
  z-index: 2;
}

/* 抽屉样式 (纯净浅灰背景 + 现代白卡片风格) */
::v-deep .six-s-drawer-light {
  background: #f8fafc !important;
  color: #1f2937;
  border-left: 1px solid #e2e8f0;
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.08);
}

.drawer-container-light {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  color: #1f2937;
  box-sizing: border-box;
}

/* 抽屉顶部栏 (与日志/API/密钥管理页头风格完全一致) */
.drawer-header-light {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  background: #ffffff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mini-logo-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f7ff;
  border: 1.5px solid #91d5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.15);
}

.mini-logo {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-wrap .title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.title-wrap .title-tag {
  font-size: 11px;
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.header-text .sub-title {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 3px;
}

.close-btn {
  font-size: 18px;
  color: #8c8c8c;
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px;
  border-radius: 4px;
}

.close-btn:hover {
  color: #1890ff;
  background: #f0f7ff;
}

/* 感知栏 (白底纯净微卡片) */
.context-banner-light {
  padding: 10px 18px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.context-item {
  display: flex;
  align-items: center;
}

.context-item .label {
  color: #595959;
  margin-right: 6px;
}

.context-tag.route-tag {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.context-tag.grade-tag {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.context-tag.grade-tag .dot {
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  display: inline-block;
}

/* 6S 标准快捷导航卡片 (现代白底悬浮卡片) */
.six-s-grid-light {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 18px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.six-s-card-light {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.six-s-card-light:hover {
  transform: translateY(-2px);
  background: #ffffff;
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.card-pinyin {
  font-size: 10px;
  font-weight: 700;
  color: #1890ff;
  letter-spacing: 0.5px;
}

.card-name {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
  margin: 3px 0 2px 0;
}

.card-desc {
  font-size: 11px;
  color: #8c8c8c;
  transform: scale(0.92);
  white-space: nowrap;
}

/* 对话区 (浅灰高雅工作台背景) */
.chat-container-light {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.chat-bubble-wrapper {
  display: flex;
  gap: 10px;
  max-width: 92%;
}

.chat-bubble-wrapper.assistant {
  align-self: flex-start;
}

.chat-bubble-wrapper.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ffffff;
  border: 1.5px solid #91d5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.15);
  overflow: hidden;
}

.avatar-logo {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar.user-avatar {
  background: #1890ff;
  color: #ffffff;
  border: none;
  font-size: 18px;
}

.bubble-content {
  display: flex;
  flex-direction: column;
}

.chat-bubble-wrapper.user .bubble-content {
  align-items: flex-end;
}

.sender-name {
  font-size: 11px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

/* 助手回复 (纯白卡片 + 现代浅阴影) */
.msg-text-light {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  border-radius: 10px;
  border-top-left-radius: 2px;
  font-size: 13px;
  line-height: 1.65;
  color: #262626;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 用户提问 (品牌天蓝胶囊气泡) */
.chat-bubble-wrapper.user .msg-text-light {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  border-radius: 10px;
  border-top-right-radius: 2px;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
}

.msg-text-light ::v-deep .md-h3 {
  font-size: 13px;
  font-weight: 700;
  color: #1890ff;
  margin: 4px 0 6px 0;
}

.msg-time {
  font-size: 10px;
  color: #bfbfbf;
  margin-top: 4px;
}

/* 思考中动画 */
.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8c8c8c;
}

.thinking .dot {
  width: 5px;
  height: 5px;
  background: #1890ff;
  border-radius: 50%;
  animation: dotPulse 1.2s infinite ease-in-out;
}

.thinking .dot:nth-child(2) { animation-delay: 0.2s; }
.thinking .dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 100% { transform: scale(0.8); opacity: 0.4; }
  50% { transform: scale(1.4); opacity: 1; }
}

/* 快捷提问 chips (白底微药丸) */
.quick-chips-light {
  padding: 10px 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #ffffff;
  border-top: 1px solid #ebeef5;
}

.chip-item-light {
  font-size: 12px;
  background: #f8fafc;
  color: #595959;
  padding: 5px 12px;
  border-radius: 14px;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.chip-item-light .chip-icon {
  color: #1890ff;
}

.chip-item-light:hover {
  background: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

/* 底部输入框 (纯白操作底栏) */
.drawer-footer-light {
  padding: 14px 18px;
  background: #ffffff;
  border-top: 1px solid #ebeef5;
}

.drawer-footer-light ::v-deep .el-textarea__inner {
  background: #f8fafc;
  border: 1px solid #d9d9d9;
  color: #262626;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s;
}

.drawer-footer-light ::v-deep .el-textarea__inner:focus {
  background: #ffffff;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.footer-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.footer-bar .hint {
  font-size: 11px;
  color: #8c8c8c;
}

.footer-bar ::v-deep .el-button--primary {
  background: #1890ff;
  border-color: #1890ff;
  font-weight: 500;
}

.footer-bar ::v-deep .el-button--primary:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}
</style>

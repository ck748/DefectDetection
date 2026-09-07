<template>
  <div class="agv-page">
    <div class="agv-grid">
      <!-- 左上：串口连接 + AGV 控制（合并） + 运动控制 -->
      <div class="area-left">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>串口连接 & AGV 控制</span>
            <el-tag :type="connected ? 'success' : 'info'" size="small">{{ connected ? '已连接' : '未连接' }}</el-tag>
          </div>
          <el-form label-width="70px">
            <el-form-item label="串口">
              <el-select v-model="portName" placeholder="选择串口" style="width: 190px;" :disabled="connected">
                <el-option v-for="p in ports" :key="p.name" :label="p.name" :value="p.name">
                  <span>{{ p.name }}</span>
                  <span style="float: right; color: #909399; font-size: 12px;">{{ p.description }}</span>
                </el-option>
              </el-select>
              <el-button icon="el-icon-refresh" circle style="margin-left: 6px;" :disabled="connected" @click="loadPorts"></el-button>
            </el-form-item>
            <el-form-item label="操作">
              <el-button v-if="!connected" type="primary" icon="el-icon-link" :loading="connecting" @click="connect">连接</el-button>
              <el-button v-else type="danger" icon="el-icon-switch-button" @click="disconnect">断开</el-button>
              <span v-if="connected" class="muted" style="margin-left: 8px;">{{ portName }}</span>
            </el-form-item>
            <el-form-item label="波特率">
              <span class="muted">9600，8 数据位，1 停止位，无校验（后端托管串口）</span>
            </el-form-item>
          </el-form>
          <!-- AGV 控制按钮 -->
          <div class="agv-btns" style="margin-top: 8px;">
            <el-button type="danger" :disabled="!connected" @click="agvEmergencyStop">急停</el-button>
            <el-button type="warning" :disabled="!connected" @click="agvReset">复位（回1号站）</el-button>
          </div>
        </el-card>

        <!-- 机械臂 + 工作流（合并卡片） -->
        <el-card shadow="hover" class="card-robot">
          <div slot="header" class="card-header">
            <span>机械臂 & 工作流</span>
            <el-tag :type="robotConnected ? 'success' : 'info'" size="small">{{ robotConnected ? '已连接' : '未连接' }}</el-tag>
          </div>
          <div class="robot-row">
            <div class="robot-section">
              <div class="section-title">机械臂</div>
              <div class="robot-btns">
                <el-button v-if="!robotConnected" type="primary" icon="el-icon-link" :loading="robotConnecting" @click="connectRobot">连接</el-button>
                <el-button v-else type="danger" icon="el-icon-switch-button" @click="disconnectRobot">断开</el-button>
                <el-button :disabled="!robotConnected" @click="robotMoveHome">回原位</el-button>
                <el-button :disabled="!robotConnected" @click="robotMovePhoto">拍照位</el-button>
                <el-button type="danger" :disabled="!robotConnected" @click="robotStop">急停</el-button>
              </div>
            </div>
            <el-divider style="margin: 6px 0;"></el-divider>
            <div class="robot-section">
              <div class="section-title">自动工作流 <el-tag :type="workflowTagType" size="small" style="margin-left:4px;">{{ workflowStateText }}</el-tag></div>
              <div class="muted hint" style="margin-bottom:6px;"></div>
              <div class="workflow-btns">
                <el-button type="success" :disabled="!canStartWorkflow" @click="startWorkflow">启动</el-button>
                <el-button type="warning" :disabled="workflowState !== 'IDLE' && workflowState !== 'COMPLETED' && workflowState !== 'ERROR'" @click="stopWorkflow">停止</el-button>
                <el-button @click="resetWorkflow">重置</el-button>
              </div>
            </div>
          </div>
        </el-card>

      </div>

      <!-- 右上：项目流程图 -->
      <el-card shadow="hover" class="card-flow">
        <div slot="header" class="card-header">
          <span>项目流程</span>
          <el-button icon="el-icon-refresh-left" @click="resetFlow">重置流程</el-button>
        </div>
        <div class="flow-wrap">
          <div ref="agvViewer" class="agv-viewer" :class="steps[2].state">
            <div class="viewer-tag">检测数字孪生</div>
          </div>
          <div class="flow-row">
            <template v-for="(s, i) in steps">
              <div :key="'n' + i" class="flow-node" :class="[s.state, { disabled: !canClick(i) }]" @click="clickStep(i)">
                <div class="node-circle"><i :class="stepIcon(i)"></i></div>
                <div class="node-label">{{ s.label }}</div>
                <div class="node-state">{{ stateText(s.state) }}</div>
              </div>
              <div :key="'a' + i" v-if="i < steps.length - 1" class="flow-arrow" :class="{ lit: s.state === 'done' }"></div>
            </template>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as THREE from 'three';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import axios from 'axios';
import gsap from 'gsap';

export default {
  name: 'AgvControl',
  data() {
    return {
      portName: '',
      ports: [],
      statusPollTimer: null,
      connected: false,
      connecting: false,
      stationLoading: 1,
      stationDetect: 2,
      // 检测结果: ok=合格 / ng=缺陷, 决定分拣时 AGV 走哪条地面路线
      inspectResult: 'ok',
      steps: [
        { label: '到达上料区', state: 'pending' },
        { label: '到达检测区', state: 'pending' },
        { label: '检测中', state: 'pending' },
        { label: '检测完成', state: 'pending' },
        { label: '分拣已完成', state: 'pending' }
      ],
      pendingStep: -1,
      pendingStation: null,
      status: null,
      lastUpdate: '',
      agvMode: null,  // AGV当前工作模式：0=普通(基础) 1=站点编辑 2=站点召回
      agvSpeed: 500,  // AGV设定速度(米/小时)
      // 机械臂控制数据
      robotConnected: false,
      robotConnecting: false,
      workflowState: 'IDLE',
      previousWorkflowState: 'IDLE',
      workflowPollTimer: null,
      // 机械臂控制数据
      // 每一节均支持: 转台左右 / 肘部前伸下压与左右 / 头部下压与左右 / 相机前伸与左右平移
      robotAngles: {
        turntableYaw: 0,   // 转台左右(绕 Y)
        elbowPitch: 0,     // 肘部向前/下压(绕 X, 负值为前压)
        elbowYaw: 0,       // 肘部左右(绕 Y)
        elbowRoll: 0,      // 肘部侧倾(绕 Z)
        headPitch: 0,      // 头部低头/下压(绕 X, 负值为下压)
        headYaw: 0,        // 头部左右(绕 Y)
        cameraX: 0,        // 相机左右平移(米)
        cameraOut: 0       // 相机向前伸出(米)
      }
    };
  },
  computed: {
    canControl() {
      return this.connected;
    },
    canStartWorkflow() {
      return this.connected && this.robotConnected &&
        (this.workflowState === 'IDLE' || this.workflowState === 'COMPLETED' || this.workflowState === 'ERROR');
    },
    workflowStateText() {
      const map = {
        IDLE: '空闲',
        AGV_TO_STATION6: 'AGV→6号站',
        SCANNING: '28点位扫描中',
        AGV_TO_STATION3: '扫描完成→AGV→3号站',
        COMPLETED: '已完成',
        ERROR: '异常'
      };
      return map[this.workflowState] || this.workflowState;
    },
    workflowTagType() {
      const map = {
        IDLE: 'info',
        AGV_TO_STATION6: '',
        SCANNING: 'warning',
        AGV_TO_STATION3: '',
        COMPLETED: 'success',
        ERROR: 'danger'
      };
      return map[this.workflowState] || 'info';
    },
    view() {
      const s = this.status;
      const d = '--';
      return {
        battery: s ? s.battery + '%' : d,
        realSpeed: s ? s.realSpeed + ' 米/小时' : d,
        setSpeed: s ? s.setSpeed + ' 米/小时' : d,
        currentStation: s ? (s.currentStation || d) : d,
        targetStation: s ? (s.targetStation || d) : d,
        mode: s ? this.modeText(s.mode) : d,
        run: s ? this.runText(s) : d,
        runClass: s ? (s.stopFlag ? 'warn' : 'ok') : '',
        arriveStop: s ? (s.arriveStop ? '是' : '否') : d,
        obstacleStop: s ? (s.obstacleStop ? '是' : '否') : d,
        cmdStop: s ? (s.cmdStop ? '是' : '否') : d,
        charging: s ? (s.charging === 1 ? '充电中' : '未充电') : d
      };
    }
  },
  mounted() {
    this.initAgv();
    this.loadPorts();
    this.fetchStatus();
    this.startStatusPolling();
  },
  beforeDestroy() {
    this.stopStatusPolling();
    this.stopWorkflowPolling();
    this.disposeAgv();
  },
  methods: {
    stepIcon(i) {
      return ['el-icon-box', 'el-icon-view', 'el-icon-loading', 'el-icon-circle-check', 'el-icon-finished'][i];
    },
    stateText(state) {
      return { pending: '未开始', active: '进行中', done: '已完成' }[state];
    },
    modeText(m) {
      return ['普通模式', '站点编辑', '站点召回'][m] || '-';
    },
    runText(s) {
      if (!s.stopFlag) return '行驶中';
      if (s.obstacleStop) return '障碍停止';
      if (s.arriveStop) return '到站停止';
      if (s.cmdStop) return '指令停止';
      return '停止';
    },
    canClick(i) {
      return true;
    },

    async loadPorts() {
      try {
        const res = await axios.get('api/agv/ports');
        if (res.data.code === 200) {
          this.ports = res.data.data || [];
          if (!this.portName && this.ports.length === 1) {
            this.portName = this.ports[0].name;
          }
        }
      } catch (e) {
        this.ports = [];
        this.$message.warning('无法获取串口列表，请确认后端服务已启动');
      }
    },
    async fetchStatus() {
      try {
        const res = await axios.get('api/agv/status');
        if (res.data.code === 200) {
          const d = res.data.data || {};
          this.connected = !!d.connected;
          if (d.portName) this.portName = d.portName;
          if (d.status) {
            this.status = d.status;
            this.lastUpdate = d.lastStatusTime ? new Date(d.lastStatusTime).toLocaleTimeString() : '';
          }
        }
      } catch (e) { /* 后端未启动 */ }
    },
    startStatusPolling() {
      this.stopStatusPolling();
      this.statusPollTimer = setInterval(() => {
        if (this.connected) {
          this.fetchStatus();
        }
      }, 1000);
    },
    stopStatusPolling() {
      if (this.statusPollTimer) {
        clearInterval(this.statusPollTimer);
        this.statusPollTimer = null;
      }
    },

    async connect() {
      if (!this.portName) {
        this.$message.warning('请先选择串口');
        return;
      }
      this.connecting = true;
      try {
        const res = await axios.post('api/agv/connect', { portName: this.portName });
        if (res.data.code === 200) {
          this.connected = true;
          this.startStatusPolling();
          this.$message.success('串口已连接');
        } else {
          this.$message.error(res.data.message || '连接失败');
        }
      } catch (e) {
        this.$message.error('连接失败：后端服务未启动或请求异常');
      } finally {
        this.connecting = false;
      }
    },
    async disconnect() {
      try {
        await axios.post('api/agv/disconnect');
      } catch (e) { /* ignore */ }
      this.connected = false;
      this.status = null;
      this.lastUpdate = '';
      this.stopStatusPolling();
    },

    async sendCmd(sub, p1, p2) {
      try {
        console.log(`[AGV] 发送指令: sub=0x${sub.toString(16).padStart(2,'0')} p1=0x${p1.toString(16).padStart(2,'0')} p2=0x${p2.toString(16).padStart(2,'0')}`);
        const res = await axios.post('api/agv/command', { sub, p1, p2 });
        console.log('[AGV] 响应:', res.data);
        return res.data.code === 200;
      } catch (e) {
        console.error('[AGV] 指令发送异常:', e);
        this.$message.error('指令发送失败：后端服务未启动或串口未连接');
        return false;
      }
    },
    queryStatus() {
      axios.post('api/agv/command', { query: true })
        .catch(() => this.$message.warning('查询失败，串口可能未连接'));
    },

    /** AGV 站点导航（sub=0x9D） */
    async agvGoStation(station) {
      const ok = await this.sendCmd(0x9D, station, 0x00);
      if (ok) this.$message.success(`已发送目标站点 ${station}`);
    },

    /** AGV 急停 */
    async agvEmergencyStop() {
      try {
        const res = await axios.post('api/agv/command', { sub: 0x9D, p1: 0, p2: 0x00 });
        // 急停：发送停止指令
        this.$message.warning('AGV 急停指令已发送');
      } catch (e) {
        this.$message.error('急停指令发送失败');
      }
    },

    /** AGV 复位（回 1 号站） */
    async agvReset() {
      try {
        const res = await axios.post('api/agv/command', { sub: 0x9D, p1: 1, p2: 0x00 });
        if (res.data.code === 200) {
          this.$message.success('AGV 正在返回 1 号站');
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('复位指令发送失败');
      }
    },

    clickStep(i) {
      if (!this.canClick(i)) return;
      for (let j = 0; j < i; j++) {
        if (this.steps[j].state !== 'done') this.steps[j].state = 'done';
      }
      const step = this.steps[i];
      
      // 联动 3D 模型：控制 AGV 行驶到对应站点
      if (this._agvModel) {
        const pos = this._agvModel.position;
        gsap.killTweensOf(pos);
        if (i === 1) {
          // "到达检测区"：沿共用线移动到机械臂正前方(机械臂在 X:0)
          gsap.to(pos, {
            x: 0,
            z: 1.5,
            duration: 2.5,
            ease: 'power1.inOut'
          });
        } else if (i === 3) {
          // "检测完成"：移动到分岔口前(X: 1.2)
          gsap.to(pos, {
            x: 1.2,
            z: 1.5,
            duration: 1.5,
            ease: 'power1.inOut'
          });
        } else if (i === 4) {
          // "分拣已完成"：按检测结果走 合格线(直行) / 划痕线 / 裂痕线
          if (this.inspectResult === 'scratch') {
            const tl = gsap.timeline();
            tl.to(pos, { x: 3.8, z: 0.15, duration: 1.5, ease: 'power1.inOut' })
              .to(pos, { x: 7.0, z: -0.35, duration: 1.8, ease: 'power1.inOut' });
          } else if (this.inspectResult === 'crack') {
            const tl = gsap.timeline();
            tl.to(pos, { x: 3.8, z: 2.85, duration: 1.5, ease: 'power1.inOut' })
              .to(pos, { x: 7.0, z: 3.35, duration: 1.8, ease: 'power1.inOut' });
          } else {
            gsap.to(pos, { x: 7.0, z: 1.5, duration: 2.5, ease: 'power1.inOut' });
          }
        }
      }

      if (i === 0 || i === 1) {
        // 到达上料区 / 到达检测区：仅更新流程状态
        step.state = 'active';
      } else if (i === 2) {
        // "检测中"：启动机械臂检测动作(左右俯身 + 相机左右移动拍照)
        step.state = 'active';
        this.runInfiniteInspectionAnimation(true);
      } else if (i === 3) {
        // "检测完成"：停止机械臂并复位
        this.stopInspection();
        step.state = 'done';
      } else if (i === 4) {
        // "分拣已完成"
        step.state = 'done';
      }
    },

    // 兼容旧调用：只做左右拍照的循环动作
    runInspectionAnimation() {
      this.runInfiniteInspectionAnimation(true);
    },

    /**
     * "检测中"扫描控制器 - 可随时重触发。
     * 轨迹(每轮只执行一次下压):
     *   复位 → 转台转到左侧起点 → 下压一次(elbowPitch + headPitch + cameraOut)
     *   → 保持下压, 从左往右停靠 7 次(含第 1 次)并拍照
     *   → 到右端后返回, 右端第 1 次已算一次, 再停靠 6 次并拍照
     *   → 抬起复位, 准备下一轮
     * @param {boolean} loop
     */
    runInfiniteInspectionAnimation(loop = false) {
      // 每次触发都先彻底停掉上一轮,再开新一轮
      this.stopInspection();
      this._scanning = true;
      this._scanLoop = !!loop;

      // 左右扫描停靠点: 共 7 次(含左端第 1 次), 从最左到最右均匀分布
      const photoCount = 7;
      const stops = [];
      for (let i = 0; i < photoCount; i++) {
        const t = photoCount === 1 ? 0 : i / (photoCount - 1);
        stops.push({
          yaw: 28 - t * 56,        // 转台角度: 28°(最左) → -28°(最右)
          camX: -0.15 + t * 0.30,  // 相机平移: -0.15(最左) → 0.15(最右)
          side: 'p' + (i + 1)
        });
      }
      // 从当前站位连续扫到下一站位, 到位后停留拍照
      const sweep = (tl, next) => {
        tl.to(this.robotAngles, {
          turntableYaw: next.yaw,
          cameraX: next.camX,
          duration: 0.8,
          ease: 'power1.inOut'
        })
        .add(() => this.onPhoto(next.side))
        .to({}, { duration: 0.45 });
      };
      const run = () => {
        if (!this._scanning) return;
        const tl = gsap.timeline({
          onComplete: () => { if (this._scanLoop && this._scanning) run(); }
        });
        this._scanTl = tl;

        // 0) 复位到初始姿态
        tl.to(this.robotAngles, {
          turntableYaw: 0, elbowPitch: 0, elbowYaw: 0, elbowRoll: 0,
          headPitch: 0, headYaw: 0, cameraX: 0, cameraOut: 0,
          duration: 0.4, ease: 'power2.out'
        })
        // 1) 转台转到左侧站位（扫描起点）
        .to(this.robotAngles, {
          turntableYaw: stops[0].yaw,
          cameraX: stops[0].camX,
          duration: 0.8,
          ease: 'power1.inOut'
        })
        // 2) 执行一次下压; 下压后保持到两次往返扫描完成再抬起
        .to(this.robotAngles, {
          elbowPitch: 10, headPitch: 10, cameraOut: 0.22,
          duration: 0.9, ease: 'power2.out'
        })
        .add(() => this.onPhoto(stops[0].side))
        .to({}, { duration: 0.4 });
        // 3) 保持下压, 从左往右停靠并拍照: p1(已拍) → p2 ... → p7(右端)
        for (let i = 1; i < stops.length; i++) {
          sweep(tl, stops[i]);
        }
        // 4) 到右端后返回: p7 第 1 次已算一次, 再停靠 6 次: p6 → ... → p1
        for (let i = stops.length - 2; i >= 0; i--) {
          sweep(tl, stops[i]);
        }
        // 5) 抬起复位并回中, 准备下一轮
        tl.to(this.robotAngles, {
          turntableYaw: 0, elbowPitch: 0, headPitch: 0, cameraX: 0, cameraOut: 0,
          duration: 1.0, ease: 'power2.inOut'
        });
      };
      run();
    },

    onPhoto(side) {
      // 在这里接你的真实拍照逻辑：后端拍照指令 / 前端截图 / 事件通知
      const sideText = {
        p1: '第1次停靠(最左)',
        p2: '第2次停靠',
        p3: '第3次停靠',
        p4: '第4次停靠',
        p5: '第5次停靠',
        p6: '第6次停靠',
        p7: '第7次停靠(最右)'
      }[side] || side;
      console.log(`拍照：${sideText}`);
      this.$emit('photo', side);
    },

    stopInspection() {
      this._scanning = false;
      if (this._scanTl) {
        this._scanTl.kill();
        this._scanTl = null;
      }
      gsap.killTweensOf(this.robotAngles);
      Object.assign(this.robotAngles, {
        turntableYaw: 0, elbowPitch: 0, elbowYaw: 0, elbowRoll: 0,
        headPitch: 0, headYaw: 0, cameraX: 0, cameraOut: 0
      });
    },

    completeStep(i) {
      if (i >= 0 && i < this.steps.length) this.steps[i].state = 'done';
      this.pendingStep = -1;
      this.pendingStation = null;
    },

    // ==================== 机械臂控制 ====================
    async connectRobot() {
      this.robotConnecting = true;
      try {
        const res = await axios.post('api/aubo/connect');
        if (res.data.code === 200) {
          this.robotConnected = true;
          this.$message.success('机械臂已连接');
        } else {
          this.$message.error(res.data.message || '连接失败');
        }
      } catch (e) {
        this.$message.error('机械臂连接失败：请确认控制器IP(192.168.1.6)可达');
      } finally {
        this.robotConnecting = false;
      }
    },
    async disconnectRobot() {
      try {
        await axios.post('api/aubo/disconnect');
      } catch (e) { /* ignore */ }
      this.robotConnected = false;
    },
    async setDO(index, value) {
      try {
        const res = await axios.post('api/aubo/setDO', { index, value });
        if (res.data.code === 200) {
          this.$message.success(`DO${index} = ${value ? '开' : '关'}`);
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) {
        this.$message.error('DO 设置失败');
      }
    },
    async robotMoveHome() {
      try {
        const res = await axios.post('api/aubo/photo/moveToHome');
        res.data.code === 200 ? this.$message.success('已回原位') : this.$message.error(res.data.message);
      } catch (e) { this.$message.error('移动失败'); }
    },
    async robotMovePhoto() {
      try {
        const res = await axios.post('api/aubo/photo/moveToPosition');
        res.data.code === 200 ? this.$message.success('已到拍照位') : this.$message.error(res.data.message);
      } catch (e) { this.$message.error('移动失败'); }
    },
    async robotStop() {
      try {
        const res = await axios.post('api/aubo/stop');
        res.data.code === 200 ? this.$message.success('已发送急停') : this.$message.error(res.data.message);
      } catch (e) { this.$message.error('急停失败'); }
    },

    // ==================== 工作流控制 ====================
    async startWorkflow() {
      try {
        const res = await axios.post('api/workflow/start');
        if (res.data.code === 200) {
          this.$message.success('工作流已启动');
          // 重置前序状态，确保首次状态变迁能被检测到
          this.previousWorkflowState = 'IDLE';
          this.startWorkflowPolling();
        } else {
          this.$message.error(res.data.message);
        }
      } catch (e) { this.$message.error('启动失败'); }
    },
    async stopWorkflow() {
      try {
        await axios.post('api/workflow/stop');
        this.$message.success('工作流已停止');
        this.stopWorkflowPolling();
        this.stopInspection();
      } catch (e) { /* ignore */ }
    },
    async resetWorkflow() {
      try {
        await axios.post('api/workflow/reset');
        this.workflowState = 'IDLE';
        this.previousWorkflowState = 'IDLE';
        this.resetFlow();
        this.$message.success('工作流已重置');
      } catch (e) { /* ignore */ }
    },
    startWorkflowPolling() {
      this.stopWorkflowPolling();
      this.workflowPollTimer = setInterval(() => this.fetchWorkflowStatus(), 1000);
    },
    stopWorkflowPolling() {
      if (this.workflowPollTimer) {
        clearInterval(this.workflowPollTimer);
        this.workflowPollTimer = null;
      }
    },
    async fetchWorkflowStatus() {
      try {
        const res = await axios.get('api/workflow/status');
        if (res.data.code === 200 && res.data.data) {
          const newState = res.data.data.state;
          this.workflowState = newState;
          // 检测状态变迁 → 联动数字孪生
          if (newState !== this.previousWorkflowState) {
            this.syncDigitalTwin(newState);
            this.previousWorkflowState = newState;
          }
          if (newState === 'COMPLETED' || newState === 'ERROR') {
            this.stopWorkflowPolling();
          }
        }
      } catch (e) { /* ignore */ }
    },

    /**
     * 根据工作流状态自动驱动数字孪生 3D 场景
     * 状态变迁时自动：移动 AGV 模型 / 启停机械臂扫描 / 更新流程步骤
     */
    syncDigitalTwin(state) {
      console.log('[DigitalTwin] 状态变迁 →', state);
      switch (state) {
        case 'AGV_TO_STATION6': {
          // AGV 从当前位置驶向检测区(机械臂正前方 X:0)
          this.animateAgvTo(0, 1.5, 6);
          // 更新流程步骤: 到达上料区→完成, 到达检测区→进行中
          this.setStepsDone(0);
          this.setStepActive(1);
          break;
        }
        case 'SCANNING': {
          // AGV 停在检测区，机械臂开始扫描动画
          this.animateAgvTo(0, 1.5, 0);
          this.setStepsDone(1);
          this.setStepActive(2);
          this.runInfiniteInspectionAnimation(true);
          break;
        }
        case 'AGV_TO_STATION3': {
          // 扫描完成，机械臂复位，AGV 直接驶向合格区
          this.stopInspection();
          this.setStepsDone(2);
          this.setStepActive(3);
          this.animateAgvToSortZone();
          break;
        }
        case 'COMPLETED': {
          // 流程完成，标记步骤
          this.setStepsDone(3);
          this.setStepActive(4);
          setTimeout(() => this.setStepDone(4), 1500);
          break;
        }
        case 'ERROR': {
          // 异常：停止所有动画
          this.stopInspection();
          break;
        }
        case 'IDLE': {
          // 空闲：不做额外操作（由 resetFlow 处理复位）
          break;
        }
      }
    },

    /** AGV 模型平滑移动到目标位置 */
    animateAgvTo(x, z, duration) {
      if (!this._agvModel) return;
      const pos = this._agvModel.position;
      gsap.killTweensOf(pos);
      gsap.to(pos, {
        x, z,
        duration: duration || 3,
        ease: 'power1.inOut'
      });
    },

    /** AGV 按检测结果驶向对应分拣区 */
    animateAgvToSortZone() {
      if (!this._agvModel) return;
      const pos = this._agvModel.position;
      gsap.killTweensOf(pos);
      if (this.inspectResult === 'scratch') {
        const tl = gsap.timeline();
        tl.to(pos, { x: 3.8, z: 0.15, duration: 1.5, ease: 'power1.inOut' })
          .to(pos, { x: 7.0, z: -0.35, duration: 1.8, ease: 'power1.inOut' });
      } else if (this.inspectResult === 'crack') {
        const tl = gsap.timeline();
        tl.to(pos, { x: 3.8, z: 2.85, duration: 1.5, ease: 'power1.inOut' })
          .to(pos, { x: 7.0, z: 3.35, duration: 1.8, ease: 'power1.inOut' });
      } else {
        gsap.to(pos, { x: 7.0, z: 1.5, duration: 2.5, ease: 'power1.inOut' });
      }
    },

    /** 将指定索引及之前的步骤标记为 done */
    setStepsDone(upToIndex) {
      for (let j = 0; j <= upToIndex && j < this.steps.length; j++) {
        this.steps[j].state = 'done';
      }
    },
    /** 将指定步骤标记为 active */
    setStepActive(index) {
      if (index >= 0 && index < this.steps.length) {
        this.steps[index].state = 'active';
      }
    },
    /** 将指定步骤标记为 done */
    setStepDone(index) {
      if (index >= 0 && index < this.steps.length) {
        this.steps[index].state = 'done';
      }
    },

    resetFlow() {
      this.steps.forEach(s => { s.state = 'pending'; });
      this.pendingStep = -1;
      this.pendingStation = null;
      this.previousWorkflowState = 'IDLE';
      if (this._jointNodes) this.stopInspection();
      
      // 流程重置时，让 AGV 回到起点 (X: -7)
      if (this._agvModel) {
        gsap.to(this._agvModel.position, {
          x: -7,
          z: 1.5,
          duration: 1.5,
          ease: 'power1.inOut'
        });
      }
    },

    initAgv() {
      const el = this.$refs.agvViewer;
      if (!el || !el.clientWidth) return;

      const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
      renderer.setPixelRatio(window.devicePixelRatio || 1);
      renderer.setSize(el.clientWidth, el.clientHeight);
      renderer.outputColorSpace = THREE.SRGBColorSpace;
      renderer.toneMapping = THREE.ACESFilmicToneMapping;
      el.appendChild(renderer.domElement);

      const scene = new THREE.Scene();
      const camera = new THREE.PerspectiveCamera(40, el.clientWidth / el.clientHeight, 0.1, 100);
      // 调整相机位置，以便更好地俯视观察整个检测设备和机械臂 (拉近并调整视角以放大画面)
      camera.position.set(0, 11, 10);
      camera.lookAt(0, 0, 0);

      const controls = new OrbitControls(camera, renderer.domElement);
      controls.enableDamping = true;

      scene.add(new THREE.AmbientLight(0xffffff, 2.5)); // 增强环境光，让整体更亮
      const dirLight = new THREE.DirectionalLight(0xffffff, 3.5); // 增强平行光
      dirLight.position.set(5, 10, 7.5);
      scene.add(dirLight);

      // 1. 添加统一颜色的实体地面(放大铺满整个可视区域, 避免露出外围深色地面)
      const floorGeo = new THREE.PlaneGeometry(60, 60);
      const floorMat = new THREE.MeshStandardMaterial({ color: 0x4a5568, roughness: 0.7 }); // 改为亮灰蓝色
      const floor = new THREE.Mesh(floorGeo, floorMat);
      floor.rotation.x = -Math.PI / 2;
      scene.add(floor);

      // 2. 叠加科技感网格
      const grid = new THREE.GridHelper(20, 20, 0x00e5ff, 0x2a3542);
      grid.position.y = 0.01; // 稍微抬高防止 Z-fighting
      grid.material.opacity = 0.15;
      grid.material.transparent = true;
      scene.add(grid);
      scene.add(this.buildIndustrialScene());

      // 3. 添加 AGV 运行轨迹：上料/检测共用引导线 → 卸货区分 合格线 / 划痕线 / 裂痕线
      // 履带主体(亮蓝色地板轨道, 不受光照影响)
      // 缩短总长，使其不过度超出。设长度为 15.5，中心点 x 向左平移以匹配缩短量。
      const trackGeo = new THREE.BoxGeometry(15.5, 0.02, 0.4); 
      const trackMat = new THREE.MeshBasicMaterial({ color: 0x42a5f5 });
      const track = new THREE.Mesh(trackGeo, trackMat);
      track.position.set(-0.45, 0.02, 1.5);
      scene.add(track);

      // 放置一段引导线的辅助函数
      const tape = (len, color, x, z, angleY) => {
        const m = new THREE.Mesh(
          new THREE.BoxGeometry(len, 0.025, 0.05),
          new THREE.MeshBasicMaterial({ color })
        );
        m.position.set(x, 0.026, z);
        if (angleY) m.rotation.y = angleY;
        scene.add(m);
      };
      // 共用引导线: 上料区(X:-7) → 检测区(X:0) → 分岔口(X:1.2)
      tape(8.2, 0xffd700, -2.9, 1.5);
      // 合格线(绿): 分岔口直行(X:1.2起点) → 合格区(X:6.5)
      // 长度 L = 6.5 - 1.2 = 5.3，中心点 = 1.2 + 5.3/2 = 3.85
      tape(5.3, 0x67c23a, 3.85, 1.5);
      // 划痕线/裂痕线: 分岔口分别拐向两条支线, 每段下面垫蓝色地板轨道条
      const laneSeg = (x1, z1, x2, z2, color) => {
        const dx = x2 - x1, dz = z2 - z1;
        const len = Math.sqrt(dx * dx + dz * dz) + 0.15; // 增加长度以防止拼接处留空
        const ax = -Math.atan2(dz, dx);
        // 亮蓝色地板轨道(不受光照影响)
        const base = new THREE.Mesh(
          new THREE.BoxGeometry(len, 0.02, 0.55),
          new THREE.MeshBasicMaterial({ color: 0x42a5f5 })
        );
        base.position.set((x1 + x2) / 2, 0.02, (z1 + z2) / 2);
        base.rotation.y = ax;
        scene.add(base);
        // 上面的引导色带
        tape(len, color, (x1 + x2) / 2, (z1 + z2) / 2, ax);
      };
      // 划痕线(橙) - 向左分支
      laneSeg(1.2, 1.5, 3.8, 0.15, 0xe6a23c);
      laneSeg(3.8, 0.15, 7.0, -0.35, 0xe6a23c);
      // 裂痕线(红) - 向右分支
      laneSeg(1.2, 1.5, 3.8, 2.85, 0xf56c6c);
      laneSeg(3.8, 2.85, 7.0, 3.35, 0xf56c6c);

      // 末端卸货区域块(划痕区 / 合格区 / 裂痕区)
      const zone = (x, z, color) => {
        const m = new THREE.Mesh(
          new THREE.BoxGeometry(0.9, 0.015, 0.8),
          new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 0.45 })
        );
        m.position.set(x, 0.033, z);
        scene.add(m);
      };
      zone(7.0, -0.35, 0xe6a23c); // 划痕区
      zone(7.0, 1.5, 0x67c23a);   // 合格区
      zone(7.0, 3.35, 0xf56c6c);  // 裂痕区

      // 地面文字标牌(合格区 / 划痕区 / 裂痕区)
      const labelTexture = (text, color) => {
        const cv = document.createElement('canvas');
        cv.width = 512; cv.height = 128;
        const ctx = cv.getContext('2d');
        ctx.fillStyle = 'rgba(10, 16, 24, 0.82)';
        ctx.fillRect(4, 4, cv.width - 8, cv.height - 8);
        ctx.strokeStyle = color;
        ctx.lineWidth = 6;
        ctx.strokeRect(4, 4, cv.width - 8, cv.height - 8);
        ctx.font = 'bold 58px "Microsoft YaHei", sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = color;
        ctx.fillText(text, cv.width / 2, cv.height / 2 + 2);
        const tex = new THREE.CanvasTexture(cv);
        tex.colorSpace = THREE.SRGBColorSpace;
        return tex;
      };
      const floorLabel = (text, color, x, z, w = 0.9, h = 0.225) => {
        const mat = new THREE.MeshBasicMaterial({
          map: labelTexture(text, color),
          transparent: true,
          depthWrite: false,
          side: THREE.DoubleSide
        });
        const mesh = new THREE.Mesh(new THREE.PlaneGeometry(w, h), mat);
        mesh.position.set(x, 0.05, z);
        mesh.rotation.x = Math.PI / 2;
        scene.add(mesh);
      };
      floorLabel('划痕区', '#E6A23C', 7.0, -0.35);
      floorLabel('合格区', '#67C23A', 7.0, 1.5);
      floorLabel('裂纹区', '#F56C6C', 7.0, 3.35);

      // 使用普通变量存储 Three.js 核心对象及关节节点，避免 Vue 响应式污染
      let model = null;
      let robotArm = null;
      this._jointNodes = {};

      const loader = new GLTFLoader();

      // 加载 AGV 小车模型
      loader.load('/model/base_basic_pbr.glb', (gltf) => {
        model = gltf.scene;
        this._agvModel = model; // 绑定到实例上以便进行动画控制
        
        // 初始位置：放置在起点 (X: -7)
        model.position.set(-7, 0.03, 1.5);
        
        model.scale.set(2, 2, 2); // 放大模型的尺寸 (之前是 0.8)
        
        // 向右旋转90度，让车头朝向右侧行驶方向
        model.rotation.y = -Math.PI / 30;
        
        scene.add(model);
      }, undefined, (error) => {
        console.error('Error loading device model:', error);
      });

      // 加载机械臂模型 (Blender 已重建好父子关节的 jixie_jointed.glb)
      // 层级: ROOT -> J_Turntable -> J_Elbow -> J_Head_A -> J_Camera
      loader.load('/model/jixie_jointed.glb', (gltf) => {
        robotArm = gltf.scene;
        this._jointNodes = {};

        robotArm.traverse((child) => {
          if (child.name === 'J_Turntable') {
            this._jointNodes.j1 = child;      // 转台
            child.userData.baseQuat = child.quaternion.clone();
          } else if (child.name === 'J_Elbow') {
            this._jointNodes.j2 = child;      // 肘部
            child.userData.baseQuat = child.quaternion.clone();
          } else if (child.name === 'J_Head_A') {
            this._jointNodes.headA = child;   // 头部
            child.userData.baseQuat = child.quaternion.clone();
          } else if (child.name === 'J_Camera') {
            this._jointNodes.camera = child;  // 相机挂点(左右平移 + 前后伸出)
            child.userData.baseX = child.position.x;
            child.userData.baseY = child.position.y;
            child.userData.baseZ = child.position.z;
          }
        });

        console.log("=== 绑定的关节节点 ===", this._jointNodes);

        // 将机械臂放置在场景原点
        robotArm.position.set(0, 0, 0);
        robotArm.scale.set(1.5, 1.5, 1);

        // 父子结构修复: 转台旋转只应带动上方的机械臂本体,
        // 转台下方直接挂着的立柱/外壳盒子(root00 / Part_Turntable_Column 等)
        // 属于固定底座, 不应跟着转, 统一改挂到 ROOT / Arm_Root 固定节点下
        let armRoot = robotArm;
        robotArm.traverse((obj) => {
          if (obj.name === 'ROOT' || obj.name === 'Arm_Root') armRoot = obj;
        });
        robotArm.updateMatrixWorld(true);
        const staticParts = [];
        const turntable = this._jointNodes.j1;
        if (turntable) {
          turntable.children.forEach((child) => {
            if (!child.isMesh) return;
            child.geometry.computeBoundingBox();
            const worldBox = child.geometry.boundingBox
              .clone()
              .applyMatrix4(child.matrixWorld);
            const isStaticHousing =
              child.name === 'root00' ||
              child.name === 'Part_Turntable_Column' ||
              worldBox.min.y < 1.0;
            if (isStaticHousing) staticParts.push(child);
          });
        }
        staticParts.forEach((part) => {
          armRoot.attach(part); // 保持世界位姿不变, 从转台改挂到固定底座
          console.log('机械臂父子结构修复：', part.name, '已从 J_Turntable 移到固定底座');
        });

        scene.add(robotArm);
      }, undefined, (error) => {
        console.error('Error loading robot arm model:', error);
      });

      this.agvThree = { renderer, scene, camera, controls, el };

      this.agvResizeObserver = new ResizeObserver(() => {
        const w = el.clientWidth, h = el.clientHeight;
        if (!w || !h) return;
        renderer.setSize(w, h);
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
      });
      this.agvResizeObserver.observe(el);

      const animate = () => {
        this.agvRaf = requestAnimationFrame(animate);
        controls.update();

        // 每一帧将 Vue 响应式状态（角度/位移）同步到真实关节节点
        if (this._jointNodes) {
          const J = this._jointNodes;
          // 1) 转台左右: 在基础姿态上绕本地 Y 叠加偏航
          if (J.j1) {
            J.j1.quaternion
              .copy(J.j1.userData.baseQuat)
              .multiply(
                new THREE.Quaternion().setFromAxisAngle(
                  new THREE.Vector3(0, 1, 0),
                  THREE.MathUtils.degToRad(this.robotAngles.turntableYaw)
                )
              );
          }
          // 2) 肘部: 前伸/下压绕 X, 左右绕 Y, 侧倾绕 Z
          if (J.j2) {
            J.j2.quaternion
              .copy(J.j2.userData.baseQuat)
              .multiply(
                new THREE.Quaternion().setFromEuler(
                  new THREE.Euler(
                    THREE.MathUtils.degToRad(this.robotAngles.elbowPitch),
                    THREE.MathUtils.degToRad(this.robotAngles.elbowYaw),
                    -THREE.MathUtils.degToRad(this.robotAngles.elbowRoll),
                    'XYZ'
                  )
                )
              );
          }
          // 3) 头部: 下压绕 X, 左右绕 Y
          if (J.headA) {
            J.headA.quaternion
              .copy(J.headA.userData.baseQuat)
              .multiply(
                new THREE.Quaternion().setFromEuler(
                  new THREE.Euler(
                    THREE.MathUtils.degToRad(this.robotAngles.headPitch),
                    THREE.MathUtils.degToRad(this.robotAngles.headYaw),
                    0,
                    'XYZ'
                  )
                )
              );
          }
          // 4) 相机挂点: 左右平移 cameraX(米) + 向前伸出 cameraOut(米)
          if (J.camera) {
            const c = J.camera;
            c.position.set(
              c.userData.baseX + this.robotAngles.cameraX,
              c.userData.baseY,
              c.userData.baseZ - this.robotAngles.cameraOut
            );
          }
        }

        renderer.render(scene, camera);
      };
      animate();
    },
    buildIndustrialScene() {
      const group = new THREE.Group();

      const metal = new THREE.MeshStandardMaterial({ color: 0x37424f, roughness: 0.55, metalness: 0.8 });
      const orange = new THREE.MeshStandardMaterial({ color: 0xcc6a2b, roughness: 0.6, metalness: 0.1 });
      const crate = new THREE.MeshStandardMaterial({ color: 0x8a6a44, roughness: 0.9 });
      const beltMat = new THREE.MeshStandardMaterial({ color: 0x11161d, roughness: 0.95 });
      const cyanMat = new THREE.MeshStandardMaterial({ color: 0x00e5ff, emissive: 0x00e5ff, emissiveIntensity: 0.85 });

      const box = (w, h, d, mat, x, y, z) => {
        const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
        m.position.set(x, y, z);
        group.add(m);
        return m;
      };
      // 深色工业地面(与上方主地面同色, 保证整体颜色统一)
      const ground = new THREE.Mesh(new THREE.PlaneGeometry(60, 60), new THREE.MeshStandardMaterial({ color: 0x4a5568, roughness: 0.95 }));
      ground.rotation.x = -Math.PI / 2;
      ground.position.y = -0.02;
      group.add(ground);

      // 左侧方形箱体/货架
      box(0.5, 0.9, 0.5, orange, -6.2, 0.45, -0.4);
      box(0.5, 0.9, 0.5, metal, -6.2, 0.45, 0.35);
      box(1.8, 1.1, 0.5, metal, -5.6, 0.55, -2.5);
      box(0.9, 0.5, 0.4, crate, -6.0, 1.45, -2.5);
      box(1.2, 0.5, 0.4, orange, -5.4, 1.72, -2.5);

      // 工业射灯
      const spot1 = new THREE.SpotLight(0xcfe8ff, 260, 45, 0.55, 0.6);
      spot1.position.set(0, 4.5, 1.0);
      spot1.target.position.set(0, 0.6, 0);
      group.add(spot1);
      group.add(spot1.target);
      const spot2 = new THREE.SpotLight(0xffe2b8, 180, 35, 0.6, 0.7);
      spot2.position.set(2.4, 4.3, -0.8);
      spot2.target.position.set(0.4, 0.5, 0.4);
      group.add(spot2);
      group.add(spot2.target);

      return group;
    },

    disposeAgv() {
      cancelAnimationFrame(this.agvRaf);
      if (this.agvResizeObserver) this.agvResizeObserver.disconnect();
      if (this.agvThree) {
        const { renderer, scene, el } = this.agvThree;
        scene.traverse(obj => {
          if (obj.geometry) obj.geometry.dispose();
          if (obj.material) obj.material.dispose();
        });
        renderer.dispose();
        if (renderer.domElement && renderer.domElement.parentNode === el) el.removeChild(renderer.domElement);
        this.agvThree = null;
        this._jointNodes = null;
      }
    }
  }
};
</script>

<style scoped>
.agv-page {
  padding: 10px;
  height: calc(100vh - 120px);
  box-sizing: border-box;
  overflow: hidden;
  background-color: #0c1017;
  background-image:
    radial-gradient(ellipse at 50% -10%, rgba(0, 114, 179, .28) 0%, transparent 60%),
    linear-gradient(rgba(0, 229, 255, .035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 229, 255, .035) 1px, transparent 1px);
  background-size: 100% 100%, 34px 34px, 34px 34px;
}
.agv-grid {
  display: grid;
  gap: 10px;
  height: 100%;
  min-height: 0;
  min-width: 0;
  grid-template-columns: 370px minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr);
  grid-template-areas: "left flow";
}
.area-left { grid-area: left; display: flex; flex-direction: column; gap: 10px; min-height: 0; overflow-y: auto; overflow-x: hidden; }
.card-flow { grid-area: flow; display: flex; flex-direction: column; min-width: 0; min-height: 0; overflow: hidden; }
.card-flow ::v-deep .el-card__body { flex: 1; min-height: 0; overflow: hidden; }
.card-status { flex: 0 1 auto; min-height: 0; display: flex; flex-direction: column; }
.card-status ::v-deep .el-card__body { flex: 1; overflow: auto; }

.area-left ::v-deep .el-card__header { padding: 8px 12px; }
.area-left ::v-deep .el-card__body { padding: 8px 12px 10px; }
.area-left .card-header { font-size: 15px; }
.area-left .card-header > span::before { height: 13px; }

.agv-page ::v-deep .el-card {
  background: linear-gradient(180deg, rgba(23, 30, 40, .95), rgba(15, 20, 28, .95));
  border: 1px solid rgba(0, 229, 255, .16);
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, .35);
  color: #dbe4ee;
}
.agv-page ::v-deep .el-card__header { border-bottom: 1px solid rgba(0, 229, 255, .12); }
.agv-page ::v-deep .el-card__header .el-button--default {
  background: rgba(0, 229, 255, .06);
  border-color: rgba(0, 229, 255, .35);
  color: #9fd8e8;
}

.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; font-size: 16px; color: #e8f1fa; }
.card-header > span::before {
  content: ''; display: inline-block; width: 4px; height: 16px; margin-right: 8px;
  background: #00e5ff; border-radius: 2px; vertical-align: -2px;
  box-shadow: 0 0 8px rgba(0, 229, 255, .8);
}
.muted { color: #8fa0b3; font-size: 12px; }
.hint { margin-top: 2px; font-size: 11px; line-height: 14px; color: #75879c; }
.agv-page ::v-deep .el-form-item { margin-bottom: 5px; }
.agv-page ::v-deep .el-form-item__label { font-size: 13px; line-height: 30px; color: #aeb9c7; }
.agv-page ::v-deep .el-form-item__content { line-height: 30px; }
.agv-page ::v-deep .el-input__inner {
  font-size: 13px;
  background: #0c121b;
  border-color: #2a3542;
  color: #e8f1fa;
}
.agv-page ::v-deep .el-input__inner::placeholder { color: #5c6b7d; }
.agv-page ::v-deep .el-input-number__decrease,
.agv-page ::v-deep .el-input-number__increase {
  background: #131b26;
  border-color: #2a3542;
  color: #aeb9c7;
}
.agv-page ::v-deep .el-button { font-size: 13px; }

/* 流程图 */
.flow-wrap { height: 100%; min-height: 0; display: flex; flex-direction: column; justify-content: space-between; gap: 8px; padding: 2px 0 6px; box-sizing: border-box; overflow: auto; }
.agv-viewer {
  flex: 1; min-height: 160px; width: 100%;
  position: relative; border-radius: 12px; overflow: hidden;
  background-image: url('https://core-normal.traeapi.us/api/ide/v1/text_to_image?prompt=High-angle%20isometric%203D%20render%20of%20a%20bright%20and%20clean%20modern%20industrial%20assembly%20line%20factory%20environment%2C%20well-lit%20with%20daylight%20and%20bright%20overhead%20LEDs%2C%20white%20and%20light%20gray%20epoxy%20floor%2C%20yellow%20safety%20lines%20and%20conveyor%20belt%20tracks%20running%20across%20the%20floor%2C%20bright%20metallic%20structures%2C%20no%20robots%2C%20no%20machinery%20blocking%20the%20center%2C%20highly%20detailed%2C%20photorealistic%2C%208k%20resolution%2C%20unreal%20engine%205%20render%20style&image_size=landscape_16_9');
  background-size: cover;
  background-position: center;
  box-shadow: inset 0 0 34px rgba(34, 211, 238, .12);
  transition: box-shadow .3s, filter .3s;
}
.agv-viewer ::v-deep canvas { display: block; outline: none; }
.agv-viewer.active { box-shadow: inset 0 0 34px rgba(34, 211, 238, .25), 0 0 14px rgba(64, 158, 255, .55); }
.agv-viewer.done { box-shadow: inset 0 0 34px rgba(103, 194, 58, .18), 0 0 12px rgba(103, 194, 58, .5); }
.viewer-tag {
  position: absolute; top: 12px; left: 14px; z-index: 2;
  font-size: 14px; letter-spacing: 2px; color: #7fd4ec;
  text-shadow: 0 0 10px rgba(0, 229, 255, .6);
  pointer-events: none;
}

.flow-row { display: flex; align-items: flex-start; justify-content: center; padding: 0 10px; }
.flow-node {
  display: flex; flex-direction: column; align-items: center;
  width: 120px; flex-shrink: 0;
  cursor: pointer; user-select: none;
}
.flow-node.disabled { cursor: not-allowed; opacity: .45; }
.flow-node:hover .node-circle { border-color: #00e5ff; box-shadow: 0 0 12px rgba(0, 229, 255, .45); }
.flow-node.active:hover .node-circle { border-color: #409EFF; box-shadow: 0 0 12px rgba(64, 158, 255, .6); }
.flow-node.done:hover .node-circle { border-color: #85ce61; box-shadow: 0 0 12px rgba(103, 194, 58, .8); }
.flow-node.disabled:hover .node-circle { border-color: #3a4656; box-shadow: none; }
.node-circle {
  width: 64px; height: 64px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; border: 4px solid #3a4656; color: #8fa0b3; background: rgba(255, 255, 255, .04);
  transition: all .3s;
}
.flow-node.active .node-circle { border-color: #409EFF; color: #409EFF; background: rgba(64, 158, 255, .12); animation: pulse 1.2s infinite; }
.flow-node.done .node-circle { border-color: #67C23A; color: #fff; background: #67C23A; box-shadow: 0 0 14px rgba(103, 194, 58, .7); }
.node-label { margin-top: 8px; font-weight: 600; color: #dbe4ee; font-size: 16px; }
.flow-node.pending .node-label { color: #8fa0b3; }
.node-state { font-size: 14px; margin-top: 2px; color: #8fa0b3; }
.flow-node.active .node-state { color: #409EFF; }
.flow-node.done .node-state { color: #67C23A; }
.flow-arrow { flex: 1; min-width: 18px; height: 4px; background: #33404f; margin-top: 32px; position: relative; }
.flow-arrow::after { content: ''; position: absolute; right: -2px; top: -6px; border: 8px solid transparent; border-left-color: #33404f; }
.flow-arrow.lit { background: #67C23A; }
.flow-arrow.lit::after { border-left-color: #67C23A; }

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, .5); }
  70% { box-shadow: 0 0 0 12px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

/* 实时状态 */
.status-grid { display: grid; grid-template-columns: repeat(3, 1fr); grid-auto-rows: auto; align-content: start; gap: 6px; }
.status-item {
  display: flex; flex-direction: column; justify-content: center; gap: 2px;
  background: rgba(0, 229, 255, .05);
  border: 1px solid rgba(0, 229, 255, .10);
  border-radius: 6px; padding: 5px 7px;
}
.status-item .k { font-size: 12px; color: #8fa0b3; }
.status-item .v { font-size: 13px; font-weight: 600; color: #eaf3fc; }
.status-item .v.ok { color: #67C23A; }
.status-item .v.warn { color: #E6A23C; }

/* 站号配置 */
.station-cfg { display: flex; align-items: center; gap: 6px; font-weight: normal; }
.station-cfg .el-input-number { width: 78px; }

/* AGV 控制 */
.card-move ::v-deep .el-card__header { padding: 8px 12px; }
.card-move ::v-deep .el-card__body { padding: 10px 12px 12px; }
.station-btns { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.station-btns .el-button { min-width: 100px; font-weight: 600; font-size: 14px; padding: 10px 16px; }
.agv-btns { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.agv-btns .el-button { min-width: 140px; font-weight: 600; font-size: 15px; padding: 12px 20px; }

/* 机械臂 + 工作流合并卡片 */
.card-robot ::v-deep .el-card__header { padding: 6px 12px; }
.card-robot ::v-deep .el-card__body { padding: 8px 12px 10px; }
.robot-row { display: flex; flex-direction: column; gap: 6px; }
.robot-section { display: flex; flex-direction: column; gap: 4px; }
.section-title { font-size: 13px; font-weight: 600; color: #aeb9c7; }
.robot-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.robot-btns .el-button { min-width: 90px; font-weight: 600; font-size: 14px; padding: 10px 16px; }

/* 工作流控制 */
.card-workflow ::v-deep .el-card__header { padding: 8px 12px; }
.card-workflow ::v-deep .el-card__body { padding: 10px 12px 12px; }
.workflow-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.workflow-btns .el-button { min-width: 90px; font-weight: 600; font-size: 14px; padding: 10px 16px; }

/* 左侧滚动条 */
.area-left::-webkit-scrollbar { width: 4px; }
.area-left::-webkit-scrollbar-thumb { background: rgba(0, 229, 255, .2); border-radius: 2px; }
.area-left::-webkit-scrollbar-track { background: transparent; }
</style>

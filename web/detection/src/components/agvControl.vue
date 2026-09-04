<template>
  <div class="agv-page">
    <div class="agv-grid">
      <!-- 左上：串口连接 + 运动控制 -->
      <div class="area-left">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>串口连接</span>
            <el-tag :type="connected ? 'success' : 'info'" size="mini">{{ connected ? '已连接' : '未连接' }}</el-tag>
          </div>
          <el-form label-width="70px" size="small">
            <el-form-item label="串口">
              <el-select v-model="portName" placeholder="选择串口" size="small" style="width: 190px;" :disabled="connected">
                <el-option v-for="p in ports" :key="p.name" :label="p.name" :value="p.name">
                  <span>{{ p.name }}</span>
                  <span style="float: right; color: #909399; font-size: 12px;">{{ p.description }}</span>
                </el-option>
              </el-select>
              <el-button size="mini" icon="el-icon-refresh" circle style="margin-left: 6px;" :disabled="connected" @click="loadPorts"></el-button>
            </el-form-item>
            <el-form-item label="操作">
              <el-button v-if="!connected" type="primary" icon="el-icon-link" :loading="connecting" @click="connect">连接</el-button>
              <el-button v-else type="danger" icon="el-icon-switch-button" @click="disconnect">断开</el-button>
              <span v-if="connected" class="muted" style="margin-left: 8px;">{{ portName }}</span>
            </el-form-item>
            <el-form-item label="波特率">
              <span class="muted">9600，8 数据位，1 停止位，无校验（后端托管串口）</span>
            </el-form-item>
            <el-form-item label="检测结果">
              <el-select v-model="inspectResult" size="small" style="width: 160px;">
                <el-option label="合格（走合格线）" value="ok"></el-option>
                <el-option label="划痕（走划痕线）" value="scratch"></el-option>
                <el-option label="裂痕（走裂痕线）" value="crack"></el-option>
              </el-select>
              <div class="muted hint">分拣时 AGV 按此结果行驶到对应路线（模拟）</div>
            </el-form-item>
            <el-form-item label="站号配置">
              <div class="station-cfg">
                <span class="muted">上料区</span>
                <el-input-number v-model="stationLoading" :min="1" :max="255" size="mini"></el-input-number>
                <span class="muted">检测区</span>
                <el-input-number v-model="stationDetect" :min="1" :max="255" size="mini"></el-input-number>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- AGV 控制 -->
        <el-card shadow="hover" class="card-move">
          <div slot="header" class="card-header">
            <span>AGV 控制</span>
            <el-tag :type="agvMode === 0 ? 'success' : agvMode === 2 ? 'warning' : 'info'" size="mini">{{ modeText(agvMode) }}</el-tag>
          </div>
          <div class="agv-btns">
            <el-button type="danger" size="small" :disabled="!connected" @click="agvEmergencyStop">急停</el-button>
            <el-button type="warning" size="small" :disabled="!connected" @click="agvReset">复位（回1号站）</el-button>
          </div>
        </el-card>

        <!-- 机械臂 + 工作流（合并卡片） -->
        <el-card shadow="hover" class="card-robot">
          <div slot="header" class="card-header">
            <span>机械臂 & 工作流</span>
            <el-tag :type="robotConnected ? 'success' : 'info'" size="mini">{{ robotConnected ? '已连接' : '未连接' }}</el-tag>
          </div>
          <div class="robot-row">
            <div class="robot-section">
              <div class="section-title">机械臂</div>
              <div class="robot-btns">
                <el-button v-if="!robotConnected" type="primary" size="mini" icon="el-icon-link" :loading="robotConnecting" @click="connectRobot">连接</el-button>
                <el-button v-else type="danger" size="mini" icon="el-icon-switch-button" @click="disconnectRobot">断开</el-button>
                <el-button size="mini" type="success" :disabled="!robotConnected" @click="setDO(0, true)">DO0开</el-button>
                <el-button size="mini" type="warning" :disabled="!robotConnected" @click="setDO(0, false)">DO0关</el-button>
                <el-button size="mini" :disabled="!robotConnected" @click="robotMoveHome">回原位</el-button>
                <el-button size="mini" :disabled="!robotConnected" @click="robotMovePhoto">拍照位</el-button>
                <el-button size="mini" type="danger" :disabled="!robotConnected" @click="robotStop">急停</el-button>
              </div>
            </div>
            <el-divider style="margin: 6px 0;"></el-divider>
            <div class="robot-section">
              <div class="section-title">自动工作流 <el-tag :type="workflowTagType" size="mini" style="margin-left:4px;">{{ workflowStateText }}</el-tag></div>
              <div class="muted hint" style="margin-bottom:6px;">AGV→6号站 → 发信号→机械臂 → 完成信号→AGV→3号站</div>
              <div class="workflow-btns">
                <el-button type="success" size="mini" :disabled="!canStartWorkflow" @click="startWorkflow">启动</el-button>
                <el-button type="warning" size="mini" :disabled="workflowState !== 'IDLE' && workflowState !== 'COMPLETED' && workflowState !== 'ERROR'" @click="stopWorkflow">停止</el-button>
                <el-button size="mini" @click="resetWorkflow">重置</el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 左下：实时状态 -->
        <el-card shadow="hover" class="card-status">
          <div slot="header" class="card-header">
            <span>实时状态</span>
            <el-button size="mini" icon="el-icon-refresh" :disabled="!connected" @click="queryStatus">主动查询</el-button>
          </div>
          <div class="status-grid">
            <div class="status-item"><span class="k">电量</span><span class="v">{{ view.battery }}</span></div>
            <div class="status-item"><span class="k">实时速度</span><span class="v">{{ view.realSpeed }}</span></div>
            <div class="status-item"><span class="k">设定速度</span><span class="v">{{ view.setSpeed }}</span></div>
            <div class="status-item"><span class="k">当前站点</span><span class="v">{{ view.currentStation }}</span></div>
            <div class="status-item"><span class="k">目标站点</span><span class="v">{{ view.targetStation }}</span></div>
            <div class="status-item"><span class="k">运行模式</span><span class="v">{{ view.mode }}</span></div>
            <div class="status-item"><span class="k">运行状态</span><span class="v" :class="view.runClass">{{ view.run }}</span></div>
            <div class="status-item"><span class="k">到位停止</span><span class="v">{{ view.arriveStop }}</span></div>
            <div class="status-item"><span class="k">障碍停止</span><span class="v">{{ view.obstacleStop }}</span></div>
            <div class="status-item"><span class="k">指令停止</span><span class="v">{{ view.cmdStop }}</span></div>
            <div class="status-item"><span class="k">充电状态</span><span class="v">{{ view.charging }}</span></div>
            <div class="status-item"><span class="k">更新时间</span><span class="v">{{ lastUpdate || '--' }}</span></div>
          </div>
        </el-card>
      </div>

      <!-- 右上：项目流程图 -->
      <el-card shadow="hover" class="card-flow">
        <div slot="header" class="card-header">
          <span>项目流程</span>
          <el-button size="mini" icon="el-icon-refresh-left" @click="resetFlow">重置流程</el-button>
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
      workflowPollTimer: null,
      // 只使用 elbow(旋转)和 cameraX(摄像头左右平移),其余保持 0
      robotAngles: {
        turntable: 0,
        elbow: 0,
        headA: 0,
        headB: 0,
        headC: 0,
        headD: 0,
        headE: 0,
        cameraX: 0,
        cameraOut: 0
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
        SIGNAL_ROBOT: '已发信号→等待机械臂响应',
        WAIT_ROBOT_DONE: '机械臂动作中→等待完成信号',
        AGV_TO_STATION3: '收到完成信号→AGV→3号站',
        COMPLETED: '已完成',
        ERROR: '异常'
      };
      return map[this.workflowState] || this.workflowState;
    },
    workflowTagType() {
      const map = {
        IDLE: 'info',
        AGV_TO_STATION6: '',
        SIGNAL_ROBOT: 'warning',
        WAIT_ROBOT_DONE: 'warning',
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
      return !((i === 0 || i === 1) && !this.canControl);
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
            tl.to(pos, { x: 3.8, z: 2.0, duration: 1.5, ease: 'power1.inOut' })
              .to(pos, { x: 7.0, z: 2.3, duration: 1.6, ease: 'power1.inOut' });
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
        // 到达上料区 / 到达检测区：向 AGV 下发对应站点
        const station = i === 0 ? this.stationLoading : this.stationDetect;
        this.sendCmd(0x9D, station, 0x00);
        step.state = 'active';
        this.pendingStep = i;
        this.pendingStation = station;
        this.$message.success(`已发送目标站点 ${station}`);
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
     * “检测中”扫描控制器 - 可随时重触发。
     * 动作(只驱动 elbow + cameraX 两个真实关节)：
     *   1) 肘部左右俯身(elbow: -28°~+28°)
     *   2) 相机挂点左右平移(cameraX: -0.18~+0.18 米)
     *   3) 在 左/中/右 三个站位触发拍照事件
     * @param {boolean} loop
     */
    runInfiniteInspectionAnimation(loop = false) {
      // 每次触发都先彻底停掉上一轮,再开新一轮
      this.stopInspection();
      this._scanning = true;
      this._scanLoop = !!loop;
      const run = () => {
        if (!this._scanning) return;
        const tl = gsap.timeline({
          onComplete: () => { if (this._scanLoop && this._scanning) run(); }
        });
        this._scanTl = tl;

        // 0) 复位到初始姿态
        tl.to(this.robotAngles, {
          turntable: 0, elbow: 0, headA: 0, headB: 0, headC: 0, headD: 0, headE: 0,
          duration: 0.5, ease: 'power2.out'
        })
        // 1) 向左俯身 + 相机左移 → 左侧站位拍照
        .to(this.robotAngles, { elbow: -28, cameraX: -0.18, duration: 1.4, ease: 'power1.inOut' })
        .add(() => this.onPhoto('left'))
        .to({}, { duration: 0.8 })
        // 2) 扫回中间 → 中间站位拍照
        .to(this.robotAngles, { elbow: -6, cameraX: 0, duration: 1.0, ease: 'power1.inOut' })
        .add(() => this.onPhoto('mid'))
        .to({}, { duration: 0.8 })
        // 3) 向右俯身 + 相机右移 → 右侧站位拍照
        .to(this.robotAngles, { elbow: 28, cameraX: 0.18, duration: 1.2, ease: 'power1.inOut' })
        .add(() => this.onPhoto('right'))
        .to({}, { duration: 0.8 })
        // 4) 回中复位, 准备下一轮
        .to(this.robotAngles, {
          turntable: 0, elbow: 0, cameraX: 0,
          duration: 1.4, ease: 'power2.inOut'
        });
      };
      run();
    },

    onPhoto(side) {
      // 在这里接你的真实拍照逻辑：后端拍照指令 / 前端截图 / 事件通知
      const sideText = { left: '左侧', mid: '中间', right: '右侧' }[side] || side;
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
        turntable: 0, elbow: 0, cameraX: 0,
        headA: 0, headB: 0, headC: 0, headD: 0, headE: 0
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
      } catch (e) { /* ignore */ }
    },
    async resetWorkflow() {
      try {
        await axios.post('api/workflow/reset');
        this.workflowState = 'IDLE';
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
          this.workflowState = res.data.data.state;
          if (this.workflowState === 'COMPLETED' || this.workflowState === 'ERROR') {
            this.stopWorkflowPolling();
          }
        }
      } catch (e) { /* ignore */ }
    },
    resetFlow() {
      this.steps.forEach(s => { s.state = 'pending'; });
      this.pendingStep = -1;
      this.pendingStation = null;
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

      // 1. 添加实体地面 (调亮地面颜色)
      const floorGeo = new THREE.PlaneGeometry(20, 10);
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
      // 履带主体
      const trackGeo = new THREE.BoxGeometry(16, 0.02, 0.4); 
      const trackMat = new THREE.MeshStandardMaterial({ color: 0x222222, roughness: 0.8 });
      const track = new THREE.Mesh(trackGeo, trackMat);
      track.position.set(0, 0.02, 1.5);
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
      // 合格线(绿): 分岔口直行 → 合格区(X:7)
      tape(5.8, 0x67c23a, 4.1, 1.5);
      // 划痕线/裂痕线: 分岔口分别拐向两条支线, 每段下面垫黑色地板轨道条
      const laneSeg = (x1, z1, x2, z2, color) => {
        const dx = x2 - x1, dz = z2 - z1;
        const len = Math.sqrt(dx * dx + dz * dz);
        const ax = -Math.atan2(dz, dx);
        // 黑色地板轨道
        const base = new THREE.Mesh(
          new THREE.BoxGeometry(len, 0.02, 0.55),
          new THREE.MeshStandardMaterial({ color: 0x1d242e, roughness: 0.9 })
        );
        base.position.set((x1 + x2) / 2, 0.02, (z1 + z2) / 2);
        base.rotation.y = ax;
        scene.add(base);
        // 上面的引导色带
        tape(len, color, (x1 + x2) / 2, (z1 + z2) / 2, ax);
      };
      // 划痕线(橙)
      laneSeg(1.2, 1.5, 3.8, 2.0, 0xe6a23c);
      laneSeg(3.8, 2.0, 7.0, 2.3, 0xe6a23c);
      // 裂痕线(红)
      laneSeg(1.2, 1.5, 3.8, 2.85, 0xf56c6c);
      laneSeg(3.8, 2.85, 7.0, 3.35, 0xf56c6c);

      // 末端卸货区域块(合格区 / 划痕区 / 裂痕区)
      const zone = (x, z, color) => {
        const m = new THREE.Mesh(
          new THREE.BoxGeometry(0.9, 0.015, 0.8),
          new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 0.45 })
        );
        m.position.set(x, 0.033, z);
        scene.add(m);
      };
      zone(7.0, 1.5, 0x67c23a);   // 合格区
      zone(7.0, 2.3, 0xe6a23c);   // 划痕区
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
      const floorLabel = (text, color, x, z) => {
        const mat = new THREE.MeshBasicMaterial({
          map: labelTexture(text, color),
          transparent: true,
          depthWrite: false,
          side: THREE.DoubleSide
        });
        const mesh = new THREE.Mesh(new THREE.PlaneGeometry(1.7, 0.42), mat);
        mesh.position.set(x, 0.05, z);
        mesh.rotation.x = Math.PI / 2;
        scene.add(mesh);
      };
      floorLabel('合格区', '#67C23A', 8.6, 1.5);
      floorLabel('划痕区', '#E6A23C', 8.6, 2.3);
      floorLabel('裂痕区', '#F56C6C', 8.6, 3.35);

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
        
        model.scale.set(0.8, 0.8, 0.8); // 放大模型的尺寸 (之前是 0.5)
        
        // 向右旋转90度，让车头朝向右侧行驶方向
        model.rotation.y = Math.PI / 2;
        
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
          } else if (child.name === 'J_Elbow') {
            this._jointNodes.j2 = child;      // 肘部
          } else if (child.name === 'J_Head_A') {
            this._jointNodes.headA = child;   // 头部
          } else if (child.name === 'J_Camera') {
            this._jointNodes.camera = child;  // 相机挂点(左右平移)
            child.userData.baseX = child.position.x;
            child.userData.baseY = child.position.y;
            child.userData.baseZ = child.position.z;
          }
        });

        console.log("=== 绑定的关节节点 ===", this._jointNodes);

        // 将机械臂放置在场景原点
        robotArm.position.set(0, 0, 0);
        robotArm.scale.set(1, 1, 1);
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
          // 1) 转台/立柱偏航：绕 Y（扫描时保持 0）
          if (J.j1) {
            J.j1.rotation.y = THREE.MathUtils.degToRad(this.robotAngles.turntable);
          }
          // 2) 肘部左右俯身：绕 Z（负号与 Blender Y 轴旋转方向保持一致）
          if (J.j2) {
            J.j2.rotation.z = -THREE.MathUtils.degToRad(this.robotAngles.elbow);
          }
          // 3) 头部微调偏航(默认 0)
          if (J.headA) {
            J.headA.rotation.y = THREE.MathUtils.degToRad(this.robotAngles.headA);
          }
          // 4) 相机挂点: 在基础位置做左右平移 cameraX(米)
          if (J.camera) {
            const c = J.camera;
            c.position.set(
              c.userData.baseX + this.robotAngles.cameraX,
              c.userData.baseY,
              c.userData.baseZ
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
      const darkMetal = new THREE.MeshStandardMaterial({ color: 0x1f2730, roughness: 0.7, metalness: 0.65 });
      const orange = new THREE.MeshStandardMaterial({ color: 0xcc6a2b, roughness: 0.6, metalness: 0.1 });
      const crate = new THREE.MeshStandardMaterial({ color: 0x8a6a44, roughness: 0.9 });
      const beltMat = new THREE.MeshStandardMaterial({ color: 0x11161d, roughness: 0.95 });
      const cyanMat = new THREE.MeshStandardMaterial({ color: 0x00e5ff, emissive: 0x00e5ff, emissiveIntensity: 0.85 });
      const greenMat = new THREE.MeshStandardMaterial({ color: 0x67ff8a, emissive: 0x2fcf5f, emissiveIntensity: 0.7 });
      const redMat = new THREE.MeshStandardMaterial({ color: 0xff5555, emissive: 0xff2222, emissiveIntensity: 0.7 });

      // 黄黑警示条纹贴图
      const cv = document.createElement('canvas');
      cv.width = 256; cv.height = 64;
      const ctx = cv.getContext('2d');
      ctx.fillStyle = '#e8b32a';
      ctx.fillRect(0, 0, 256, 64);
      ctx.fillStyle = '#181c22';
      for (let i = -64; i < 320; i += 64) {
        ctx.beginPath();
        ctx.moveTo(i, 0); ctx.lineTo(i + 32, 0); ctx.lineTo(i - 32, 64); ctx.lineTo(i - 64, 64);
        ctx.closePath(); ctx.fill();
      }
      const hazardTex = new THREE.CanvasTexture(cv);
      hazardTex.wrapS = THREE.RepeatWrapping;
      hazardTex.colorSpace = THREE.SRGBColorSpace;
      const hazard = new THREE.MeshStandardMaterial({ map: hazardTex, roughness: 0.55, metalness: 0.25 });

      const box = (w, h, d, mat, x, y, z) => {
        const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
        m.position.set(x, y, z);
        group.add(m);
        return m;
      };
      const cyl = (rt, rb, h, mat, x, y, z) => {
        const m = new THREE.Mesh(new THREE.CylinderGeometry(rt, rb, h, 16), mat);
        m.position.set(x, y, z);
        group.add(m);
        return m;
      };

      // 深色工业地面
      const ground = new THREE.Mesh(new THREE.PlaneGeometry(30, 30), new THREE.MeshStandardMaterial({ color: 0x12171e, roughness: 0.95 }));
      ground.rotation.x = -Math.PI / 2;
      ground.position.y = -0.02;
      group.add(ground);

      // 龙门架 + 警示柱脚 + 指示灯
      const corners = [[-5.6, -1.2], [5.6, -1.2], [-5.6, 5.6], [5.6, 5.6]];
      corners.forEach(([px, pz], idx) => {
        box(0.3, 4.0, 0.3, darkMetal, px, 2.0, pz);
        box(0.44, 0.2, 0.44, hazard, px, 0.1, pz);
        const lamp = new THREE.Mesh(new THREE.SphereGeometry(0.07, 12, 12), idx % 2 ? greenMat : redMat);
        lamp.position.set(px, 4.08, pz);
        group.add(lamp);
      });
      box(12.4, 0.26, 0.26, metal, 0, 4.06, -1.2);
      box(12.4, 0.26, 0.26, metal, 0, 4.06, 5.6);
      box(0.26, 0.26, 7.6, metal, -5.6, 4.06, 2.2);
      box(0.26, 0.26, 7.6, metal, 5.6, 4.06, 2.2);

      // 左侧油桶/货架
      cyl(0.28, 0.28, 0.9, orange, -6.2, 0.45, -0.4);
      cyl(0.28, 0.28, 0.9, metal, -6.2, 0.45, 0.35);
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
.area-left { grid-area: left; display: flex; flex-direction: column; gap: 8px; min-height: 0; overflow-y: auto; overflow-x: hidden; padding-right: 2px; }
.card-flow { grid-area: flow; display: flex; flex-direction: column; min-width: 0; min-height: 0; overflow: hidden; }
.card-flow ::v-deep .el-card__body { flex: 1; min-height: 0; overflow: hidden; }
.card-status { flex: 0 1 auto; min-height: 0; display: flex; flex-direction: column; }
.card-status ::v-deep .el-card__body { flex: 1; overflow: auto; }

.area-left ::v-deep .el-card__header { padding: 6px 12px; }
.area-left ::v-deep .el-card__body { padding: 6px 12px 8px; }
.area-left .card-header { font-size: 14px; }
.area-left .card-header > span::before { height: 12px; }

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
.agv-page ::v-deep .el-form-item { margin-bottom: 3px; }
.agv-page ::v-deep .el-form-item__label { font-size: 13px; line-height: 26px; color: #aeb9c7; }
.agv-page ::v-deep .el-form-item__content { line-height: 26px; }
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
  width: 96px; flex-shrink: 0;
  cursor: pointer; user-select: none;
}
.flow-node.disabled { cursor: not-allowed; opacity: .45; }
.flow-node:hover .node-circle { border-color: #00e5ff; box-shadow: 0 0 12px rgba(0, 229, 255, .45); }
.flow-node.active:hover .node-circle { border-color: #409EFF; box-shadow: 0 0 12px rgba(64, 158, 255, .6); }
.flow-node.done:hover .node-circle { border-color: #85ce61; box-shadow: 0 0 12px rgba(103, 194, 58, .8); }
.flow-node.disabled:hover .node-circle { border-color: #3a4656; box-shadow: none; }
.node-circle {
  width: 50px; height: 50px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; border: 3px solid #3a4656; color: #8fa0b3; background: rgba(255, 255, 255, .04);
  transition: all .3s;
}
.flow-node.active .node-circle { border-color: #409EFF; color: #409EFF; background: rgba(64, 158, 255, .12); animation: pulse 1.2s infinite; }
.flow-node.done .node-circle { border-color: #67C23A; color: #fff; background: #67C23A; box-shadow: 0 0 14px rgba(103, 194, 58, .7); }
.node-label { margin-top: 5px; font-weight: 600; color: #dbe4ee; font-size: 14px; }
.flow-node.pending .node-label { color: #8fa0b3; }
.node-state { font-size: 12px; margin-top: 1px; color: #8fa0b3; }
.flow-node.active .node-state { color: #409EFF; }
.flow-node.done .node-state { color: #67C23A; }
.flow-arrow { flex: 1; min-width: 18px; height: 3px; background: #33404f; margin-top: 25px; position: relative; }
.flow-arrow::after { content: ''; position: absolute; right: -2px; top: -5px; border: 6px solid transparent; border-left-color: #33404f; }
.flow-arrow.lit { background: #67C23A; }
.flow-arrow.lit::after { border-left-color: #67C23A; }

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, .5); }
  70% { box-shadow: 0 0 0 12px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

/* 实时状态 */
.status-grid { display: grid; grid-template-columns: repeat(3, 1fr); grid-auto-rows: auto; align-content: start; gap: 4px; }
.status-item {
  display: flex; flex-direction: column; justify-content: center; gap: 1px;
  background: rgba(0, 229, 255, .05);
  border: 1px solid rgba(0, 229, 255, .10);
  border-radius: 6px; padding: 3px 6px;
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
.agv-btns { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.agv-btns .el-button { min-width: 120px; font-weight: 600; }

/* 机械臂 + 工作流合并卡片 */
.card-robot ::v-deep .el-card__header { padding: 6px 12px; }
.card-robot ::v-deep .el-card__body { padding: 6px 12px 8px; }
.robot-row { display: flex; flex-direction: column; gap: 4px; }
.robot-section { display: flex; flex-direction: column; gap: 4px; }
.section-title { font-size: 13px; font-weight: 600; color: #aeb9c7; }
.robot-btns { display: flex; gap: 4px; flex-wrap: wrap; }
.robot-btns .el-button { font-size: 12px; padding: 5px 8px; }

/* 工作流控制 */
.card-workflow ::v-deep .el-card__header { padding: 8px 12px; }
.card-workflow ::v-deep .el-card__body { padding: 10px 12px 12px; }
.workflow-btns { display: flex; gap: 6px; flex-wrap: wrap; }
.workflow-btns .el-button { font-size: 12px; padding: 5px 10px; }

/* 左侧滚动条 */
.area-left::-webkit-scrollbar { width: 4px; }
.area-left::-webkit-scrollbar-thumb { background: rgba(0, 229, 255, .2); border-radius: 2px; }
.area-left::-webkit-scrollbar-track { background: transparent; }
</style>

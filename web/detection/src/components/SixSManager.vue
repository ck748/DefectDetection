<template>
  <div class="sixs-dashboard-hub">
    <!-- 1. 顶部全局统一标准卡片 Header（与系统日志、API、密钥管理高度样式完全对齐） -->
    <div class="header-section">
      <div class="header-left">
        <div class="title-wrap">
          <span class="title-icon"><i class="el-icon-cpu"></i></span>
          <h2 class="page-title">6S 精益管理执行阶段与工位标准</h2>
          <span class="title-tag">精益管控</span>
        </div>
        <p class="page-desc">全维度规范车间整理、整顿、清扫、清洁、素养与安全标准化巡检与工位联锁调度</p>
      </div>
      <div class="header-right">
        <div class="inspection-stage-group">
          <el-button
            size="small"
            :type="currentInspectionStage === 'pre' ? 'primary' : ''"
            :class="['stage-check-btn', { 'is-active': currentInspectionStage === 'pre' }]"
            @click="handleStageCheck('pre')"
          >
            <i class="el-icon-time"></i> 事前检查
          </el-button>
          <el-button
            size="small"
            :type="currentInspectionStage === 'in' ? 'primary' : ''"
            :class="['stage-check-btn', { 'is-active': currentInspectionStage === 'in' }]"
            @click="handleStageCheck('in')"
          >
            <i class="el-icon-video-play"></i> 事中检查
          </el-button>
          <el-button
            size="small"
            :type="currentInspectionStage === 'post' ? 'primary' : ''"
            :class="['stage-check-btn', { 'is-active': currentInspectionStage === 'post' }]"
            @click="handleStageCheck('post')"
          >
            <i class="el-icon-circle-check"></i> 事后检查
          </el-button>
        </div>
        <el-button
          type="success"
          size="small"
          icon="el-icon-camera"
          :loading="capturing"
          @click="handleVmCapture"
          class="header-act-btn-capture"
        >
          {{ capturing ? '正在拍照中...' : '触发拍照存D盘' }}
        </el-button>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-refresh-right"
          :loading="checking"
          @click="runAutoCheck"
          class="header-act-btn-primary"
        >
          {{ checking ? '巡检中...' : '智能巡诊实时信息' }}
        </el-button>
      </div>
    </div>

    <!-- 2. 6S 执行阶段与工位标准 6 阶段微卡片网格 -->
    <div class="flow-steps-grid">
        <div
          v-for="(item, idx) in sixSItems"
          :key="idx"
          class="step-item-card"
          :class="'step-card-' + item.type"
          @click="selectCard(item)"
        >
          <!-- 顶部行：左侧序号钢蓝方块 + 阶段名称 -->
          <div class="card-top-row">
            <div class="card-left-title-box">
              <span class="step-num-badge font-mono">{{ idx + 1 }}</span>
              <span class="step-title-text">{{ item.name }}</span>
            </div>
          </div>

          <!-- 中间行：说明规范文本 -->
          <div class="card-desc-text" :title="item.desc">{{ item.desc }}</div>
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
                <i class="el-icon-cpu"></i>
              </div>
              <div class="ai-wb-title-info">
                <span class="ai-wb-title-text">6S 智能巡检专家工作台</span>
                <span class="ai-wb-status-badge font-mono">
                  <span class="badge-dot"></span>AI 联锁就绪
                </span>
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
                <img :src="logoImg" alt="灵鉴" />
              </div>
              <div class="ai-avatar-box user-avatar-box" v-else>
                <img :src="currentUserAvatar" alt="用户" />
              </div>

              <div class="ai-msg-main">
                <div class="ai-msg-meta font-mono">
                  <span class="ai-sender-name">{{ msg.role === 'assistant' ? '灵鉴' : currentUserName }}</span>
                  <span class="ai-msg-time">{{ msg.time }}</span>
                </div>
                <div class="ai-msg-bubble" :class="msg.role">
                  <div class="markdown-render" v-html="formatMessage(msg.content)"></div>

                  <!-- 收到相机抓拍推送时渲染图片卡片 -->
                  <div v-if="msg.imageCard" class="watch-image-push-card" style="margin-top:10px;padding:8px;background:rgba(255,255,255,0.06);border-radius:8px;border:1px solid rgba(255,255,255,0.12);">
                    <div style="font-size:12px;color:#94a3b8;margin-bottom:6px;display:flex;justify-content:space-between;">
                      <span><i class="el-icon-picture-outline"></i> {{ msg.imageCard.fileName }}</span>
                      <span class="font-mono">{{ msg.imageCard.fileSize }}</span>
                    </div>
                    <div style="border-radius:6px;overflow:hidden;max-height:220px;background:#000;">
                      <el-image
                        :src="formatImageUrl(msg.imageCard.webUrl)"
                        :preview-src-list="[formatImageUrl(msg.imageCard.webUrl)]"
                        fit="contain"
                        style="width:100%;height:180px;display:block;"
                      >
                        <div slot="error" style="display:flex;align-items:center;justify-content:center;height:100%;color:#94a3b8;font-size:12px;">
                          <i class="el-icon-picture-outline"></i> 无法加载图片
                        </div>
                      </el-image>
                    </div>
                  </div>

                  <!-- 事前检查机械臂复位专属交互问答按钮组 -->
                  <div v-if="msg.interactive === 'reset_arm' && !isTyping" class="ai-interactive-actions">
                    <div v-if="!msg.userChoice" class="action-btn-group">
                      <button class="choice-btn yes-btn" @click="handleArmResetChoice('yes', msg)">
                        <i class="el-icon-check"></i> 是（立即复位）
                      </button>
                      <button class="choice-btn no-btn" @click="handleArmResetChoice('no', msg)">
                        <i class="el-icon-close"></i> 否（暂不复位）
                      </button>
                    </div>
                    <div v-else class="action-done-status font-mono">
                      <span v-if="msg.userChoice === 'yes'" class="done-tag yes">
                        <i class="el-icon-circle-check"></i> 已调度工控接口执行复位
                      </span>
                      <span v-else class="done-tag no">
                        <i class="el-icon-info"></i> 已选择暂不复位
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 思考中动态 -->
            <div class="ai-msg-row assistant" v-if="isThinking">
              <div class="ai-avatar-box">
                <img :src="logoImg" alt="灵鉴" />
              </div>
              <div class="ai-msg-main">
                <div class="ai-msg-meta font-mono">
                  <span class="ai-sender-name">灵鉴</span>
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
                placeholder="向灵鉴提问，如：半轴标定台整顿标准、工业相机镜片清洁防错、机械臂安全复位..."
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

      <!-- 右翼：数据监测与分析区 (6S 车间工位健康度雷达) -->
      <div class="right-data-column">
        <!-- 雷达图容器 -->
        <div class="analysis-card radar-card">
          <div class="card-header-clean">
            <div class="header-left">
              <span class="header-title-text">6S 车间工位健康度雷达</span>
            </div>
            <div class="header-right font-mono">
              <span class="rate-label">达成率:</span>
              <span class="rate-value font-mono">94.7%</span>
            </div>
          </div>
          <div class="radar-chart-stage" ref="radarChart"></div>
        </div>
      </div>
    </div>

    <!-- 智能巡诊实时分析报告弹窗卡片 (现代工业浅色高精风 + 简洁白话工况报告) -->
    <el-dialog
      :visible.sync="reportDialogVisible"
      width="1040px"
      custom-class="sixs-report-dialog light-precision-modal"
      top="4vh"
      :close-on-click-modal="true"
      :show-close="false"
      :lock-scroll="false"
    >
      <!-- 浅色工业标题栏 -->
      <div slot="title" class="report-light-header">
        <div class="light-header-left">
          <div class="light-shield-badge">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="light-title-wrap">
            <div class="light-title-row">
              <h3 class="light-title-text">车间 6S 智能巡检现场诊断报告</h3>
              <span class="light-live-tag font-mono">
                <span class="live-dot-red"></span>实时检测
              </span>
              <span class="light-sec-code font-mono">6S-REPORT-0906</span>
            </div>
            <div class="light-subtitle-row">
              <span>车间工位设备复位、断电及桌面整洁实时巡查结果</span>
            </div>
          </div>
        </div>

        <div class="light-header-right">
          <div class="light-clock-pill font-mono">
            <i class="el-icon-time"></i>
            <span>{{ reportTime }}</span>
          </div>
          <button class="light-close-btn" @click="reportDialogVisible = false" title="关闭报告">
            <i class="el-icon-close"></i>
          </button>
        </div>
      </div>

      <div class="report-light-content">
        <!-- 0. 报告来源溯源横幅 (联动专家报告) -->
        <div class="report-source-provenance-bar">
          <div class="provenance-left">
            <i class="el-icon-link"></i>
            <span class="prov-label">报告来源：</span>
            <span class="prov-text">来自【<strong>{{ currentExpertPackage ? currentExpertPackage.workOrderId : '半轴总成-全周巡检' }}</strong>】检测全过程质检与工序专家报告</span>
          </div>
          <div class="provenance-right">
            <el-button
              size="mini"
              type="primary"
              icon="el-icon-document"
              class="view-origin-btn"
              @click="openExpertReportDetail"
            >
              查看专家报告原件
            </el-button>
          </div>
        </div>

        <!-- 1. 顶部三联状态卡片 -->
        <div class="light-bento-row">
          <!-- Bento 1: 综合评分 -->
          <div class="bento-light-card kpi-score-card">
            <div class="bento-light-header">
              <span class="bento-light-label"><i class="el-icon-pie-chart"></i> 6S 现场合规评分</span>
              <span class="bento-light-tag danger font-mono">需整改</span>
            </div>
            <div class="score-main-flex">
              <div class="score-number-box">
                <span class="score-num font-mono">94.7</span>
                <span class="score-unit font-mono">/ 100分</span>
              </div>
              <div class="score-delta-wrap">
                <div class="delta-bar-shell">
                  <div class="delta-bar-fill" style="width: 94.7%;"></div>
                </div>
                <div class="delta-meta font-mono">
                  <span>合格基准 ≥ 95.0分</span>
                  <span class="text-danger">差距 0.3 分</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Bento 2: 问题统计 -->
          <div class="bento-light-card kpi-hazard-card">
            <div class="bento-light-header">
              <span class="bento-light-label"><i class="el-icon-warning"></i> 现场问题统计</span>
              <span class="bento-light-tag warn font-mono">共 3 项待处理</span>
            </div>
            <div class="hazard-stat-row font-mono">
              <div class="hazard-block danger">
                <span class="hz-count">2</span>
                <span class="hz-label">设备未复位</span>
              </div>
              <div class="hazard-block blue">
                <span class="hz-count">1</span>
                <span class="hz-label">桌面不整洁</span>
              </div>
            </div>
          </div>

          <!-- Bento 3: 远程总线状态 -->
          <div class="bento-light-card kpi-bus-card">
            <div class="bento-light-header">
              <span class="bento-light-label"><i class="el-icon-connection"></i> 远程总线控制</span>
              <span class="bento-light-tag success font-mono">PROFINET 在线</span>
            </div>
            <div class="bus-status-body">
              <div class="interlock-pill font-mono">
                <span class="pulse-cyan"></span>
                <span>工控总线正常 · 支持一键远程整改</span>
              </div>
              <div class="bus-meta-sub font-mono">
                <span>响应延迟 0.8ms</span>
                <span class="dot-sep">•</span>
                <span>支持机械臂、AGV 小车快速复位</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 分类筛选栏 -->
        <div class="light-filter-bar">
          <div class="filter-pills-group">
            <button
              class="filter-pill-btn"
              :class="{ 'is-active': reportFilterTab === 'all' }"
              @click="reportFilterTab = 'all'"
            >
              <i class="el-icon-menu"></i>
              <span>全部现场问题</span>
              <span class="pill-badge">3</span>
            </button>
            <button
              class="filter-pill-btn danger"
              :class="{ 'is-active': reportFilterTab === 'reset' }"
              @click="reportFilterTab = 'reset'"
            >
              <i class="el-icon-warning"></i>
              <span>设备未复位</span>
              <span class="pill-badge danger">2</span>
            </button>
            <button
              class="filter-pill-btn cyan"
              :class="{ 'is-active': reportFilterTab === 'clean' }"
              @click="reportFilterTab = 'clean'"
            >
              <i class="el-icon-brush"></i>
              <span>台面与手套</span>
              <span class="pill-badge cyan">1</span>
            </button>
          </div>

          <div class="filter-tip-right">
            <i class="el-icon-info"></i>
            <span>提示：可点击卡片按钮单项调度处置</span>
          </div>
        </div>

        <!-- 3. 详细现场问题列表 (工业精简风、客观清晰、自适应排版) -->
        <div class="light-issues-stream custom-light-scroll">
          <!-- 1. 机械臂未复位 -->
          <div v-show="reportFilterTab === 'all' || reportFilterTab === 'reset'" class="light-issue-card border-danger">
            <div class="card-glow-edge red"></div>
            <div class="card-inner-shell">
              <div class="issue-meta-row">
                <div class="meta-left-tags">
                  <span class="light-tag-pill red font-mono"><i class="el-icon-error"></i> 机械臂未归位</span>
                </div>
                <div class="meta-right-state">
                  <span class="status-dot red"></span>
                  <span class="state-txt red">未回安全原点</span>
                </div>
              </div>

              <div class="issue-title-block">
                <h4 class="issue-heading">机械臂 灵眸-CB-iS 未归位</h4>
              </div>

              <div class="issue-detail-narrative">
                <strong>现场情况：</strong>检测结束 未恢复原位 且通电发热。
              </div>

              <div class="issue-action-dock">
                <div class="dock-left-guide">
                  <span class="guide-lead"><i class="el-icon-s-operation"></i> 整改措施:</span>
                  <span>调度机械臂平稳复位至待命原点，锁定抱闸。</span>
                </div>
                <el-button size="mini" type="danger" plain class="dock-act-btn" @click="sendQuickQuestion('请你将机械臂复位')">
                  调度机械臂复位
                </el-button>
              </div>
            </div>
          </div>

          <!-- 2. AGV小车未复位 -->
          <div v-show="reportFilterTab === 'all' || reportFilterTab === 'reset'" class="light-issue-card border-danger">
            <div class="card-glow-edge red"></div>
            <div class="card-inner-shell">
              <div class="issue-meta-row">
                <div class="meta-left-tags">
                  <span class="light-tag-pill red font-mono"><i class="el-icon-error"></i> 小车未归位</span>
                </div>
                <div class="meta-right-state">
                  <span class="status-dot red"></span>
                  <span class="state-txt red">未回充电桩</span>
                </div>
              </div>

              <div class="issue-title-block">
                <h4 class="issue-heading">灵巡LN-IA 搬运小车未归位</h4>
              </div>

              <div class="issue-detail-narrative">
                <strong>现场情况：</strong>检测结束 AGV小车未归位，停留在主干通道中间。
              </div>

              <div class="issue-action-dock">
                <div class="dock-left-guide">
                  <span class="guide-lead"><i class="el-icon-s-operation"></i> 整改措施:</span>
                  <span>下发返航指令，调度AGV小车归位。</span>
                </div>
                <el-button size="mini" type="danger" plain class="dock-act-btn" @click="sendQuickQuestion('请将分拣小车归位')">
                  调度 AGV 小车归位
                </el-button>
              </div>
            </div>
          </div>

          <!-- 3. 标定台桌面整洁与手套用具 -->
          <div v-show="reportFilterTab === 'all' || reportFilterTab === 'clean'" class="light-issue-card border-blue">
            <div class="card-glow-edge cyan"></div>
            <div class="card-inner-shell">
              <div class="issue-meta-row">
                <div class="meta-left-tags">
                  <span class="light-tag-pill cyan font-mono"><i class="el-icon-info"></i> 桌面不整洁</span>
                </div>
                <div class="meta-right-state">
                  <span class="status-dot cyan"></span>
                  <span class="state-txt cyan">用具未摆放整齐</span>
                </div>
              </div>

              <div class="issue-title-block">
                <h4 class="issue-heading">工作台面不整洁 手套用具未摆放整齐</h4>
              </div>

              <div class="issue-detail-narrative">
                <strong>现场情况：</strong>检测结束 工作台面杂乱，劳保手套与工位用具随意摆放未归位。
              </div>

              <div class="issue-action-dock">
                <div class="dock-left-guide">
                  <span class="guide-lead"><i class="el-icon-s-operation"></i> 整改措施:</span>
                  <span>整理清洁工作台面，将劳保手套及工件用具规范定置摆放。</span>
                </div>
                <el-button size="mini" type="primary" plain class="dock-act-btn" @click="sendQuickQuestion('半轴缺陷标定区与合格品库房的整顿三定管理要求')">
                  查看整顿规范
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 4. 底部数据汇总 -->
        <div class="light-bottom-audit-grid font-mono">
          <div class="audit-col">
            <span class="audit-k"><i class="el-icon-odometer"></i> 受检工位</span>
            <div class="audit-v-row">
              <span class="audit-v">12 / 12</span>
              <span class="audit-sub">全部在线接入</span>
            </div>
          </div>

          <div class="audit-col">
            <span class="audit-k"><i class="el-icon-circle-check"></i> 6S 达标项</span>
            <div class="audit-v-row">
              <span class="audit-v text-emerald">18 项</span>
              <span class="audit-sub">点检符合标准</span>
            </div>
          </div>

          <div class="audit-col">
            <span class="audit-k"><i class="el-icon-warning"></i> 待整改项</span>
            <div class="audit-v-row">
              <span class="audit-v text-crimson">3 项</span>
              <span class="audit-sub">2项复位 / 1项桌面整洁</span>
            </div>
          </div>

          <div class="audit-col">
            <span class="audit-k"><i class="el-icon-timer"></i> 预计整改耗时</span>
            <div class="audit-v-row">
              <span class="audit-v text-cyan">约 1 分钟</span>
              <span class="audit-sub">支持一键自动处理</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. 底部操作栏 -->
      <div slot="footer" class="report-light-footer">
        <div class="light-footer-left">
          <span class="bus-beacon-dot"></span>
          <span class="bus-status-txt">车间工控总线正常连接 (支持一键联动整改)</span>
        </div>

        <div class="light-footer-right">
          <el-button size="medium" class="light-ghost-btn" @click="reportDialogVisible = false">
            关闭报告
          </el-button>
          <el-button size="medium" type="primary" plain class="light-recheck-btn" :loading="checking" @click="handleRecheck">
            <i class="el-icon-refresh-right"></i> 重新巡检
          </el-button>
          <el-button size="medium" type="danger" class="light-hero-exec-btn" @click="handleAutoFixAll">
            <i class="el-icon-video-play"></i> 一键执行整改
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- AI 工业表面缺陷智控专家分析报告原件弹窗 -->
    <el-dialog
      :visible.sync="expertDetailVisible"
      title="AI 工业表面缺陷智控专家分析报告 (原件)"
      width="80%"
      class="expert-report-dialog"
      top="3vh"
      :close-on-click-modal="false"
      :lock-scroll="false"
    >
      <div v-if="expertReportLoading" class="expert-loading">
        <i class="el-icon-loading"></i>
        <p>正在由 AI 视觉大模型与智控中枢生成专家分析报告...</p>
      </div>
      <div v-else-if="currentExpertReport" class="expert-report-container" id="expert-report-printable-sixs">
        <!-- 报告头部 -->
        <div class="report-header">
          <div class="header-main">
            <div class="brand-badge">
              <i class="el-icon-office-building"></i> 灵眸巡诊 · 工业质检报告
            </div>
            <h2 class="report-title">半轴表面缺陷检测与工艺处置单</h2>
            <div class="report-meta">
              <span>流水号：<strong>#{{ currentExpertReport.id || '202609' }}</strong></span>
              <span>检测时间：<strong>{{ currentExpertReport.time || '2026-9-3 18:58:19' }}</strong></span>
              <span>耗时：<strong>{{ currentExpertReport.runtime ? (currentExpertReport.runtime + 's') : '12.5s' }}</strong></span>
              <span>算法引擎：<strong>{{ currentExpertReport.algorithmEngine || 'Vision-Model v2.4' }}</strong></span>
              <span class="meta-highlight-tag">
                <i :class="(currentExpertReport.actualImages >= 28 || !currentExpertReport.actualImages) ? 'el-icon-circle-check' : 'el-icon-warning-outline'"></i>
                采集可信度：<strong>{{ currentExpertReport.reliabilityStatus || ((currentExpertReport.actualImages >= 28 || !currentExpertReport.actualImages) ? '可信 (合格)' : '需复检 (异常)') }}</strong>
              </span>
              <span>总共拍摄：<strong>{{ currentExpertReport.actualImages || 28 }} / 标准 {{ currentExpertReport.standardImages || 28 }} 张</strong></span>
              <span>端点检测：<strong>{{ currentExpertReport.endpointDetected || 4 }} / {{ currentExpertReport.endpointStandard || 4 }}</strong></span>
            </div>
          </div>
          <div class="header-actions no-print">
            <el-button type="primary" size="small" icon="el-icon-printer" class="export-print-btn" @click="printExpertReport">
              打印 / 导出PDF
            </el-button>
          </div>
        </div>

        <!-- 核心指标卡片 -->
        <div class="report-kpi-grid">
          <div class="kpi-card danger">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-warning-outline"></i></span>
              <span class="kpi-label">检出缺陷总数</span>
            </div>
            <div class="kpi-val">{{ currentExpertReport.defectionsSum || 4 }} <span class="unit">处</span></div>
            <div class="kpi-sub"><i class="el-icon-check"></i> 涉及缺陷图片: {{ expertDefectImagesCount }} 张</div>
          </div>
          <div class="kpi-card warning">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-data-line"></i></span>
              <span class="kpi-label">最高风险等级</span>
            </div>
            <div class="kpi-val highlight">{{ currentExpertAdvice && currentExpertAdvice['最严重等级'] ? currentExpertAdvice['最严重等级'] : '严重' }}</div>
            <div class="kpi-sub">依据算法综合评定</div>
          </div>
          <div class="kpi-card primary">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-pie-chart"></i></span>
              <span class="kpi-label">缺陷图片占比</span>
            </div>
            <div class="kpi-val">{{ ((expertDefectImagesCount / 28) * 100).toFixed(1) }}%</div>
            <div class="kpi-sub">⚙ 检出 {{ expertDefectImagesCount }} 张 / 实拍 28 张</div>
          </div>
          <div class="kpi-card success">
            <div class="kpi-card-header">
              <span class="kpi-icon-wrap"><i class="el-icon-guide"></i></span>
              <span class="kpi-label">最终处置决策</span>
            </div>
            <div class="kpi-val decision">{{ currentExpertAdvice && currentExpertAdvice['最终处置建议'] ? currentExpertAdvice['最终处置建议'] : '建议质检员现场卡尺测量，根据公差标准判定是否返修' }}</div>
            <div class="kpi-sub"><i class="el-icon-circle-check"></i> 现场复核合格后放行</div>
          </div>
        </div>

        <!-- 图像与大模型深度研判 -->
        <div class="report-split-section">
          <!-- 左侧：缺陷定位图像及切片翻页操作条 -->
          <div class="split-left">
            <div class="section-title">
              <i class="el-icon-picture-outline"></i> 缺陷视觉图谱与定位切片
            </div>
            <div class="report-image-box">
              <img
                v-if="currentSliceImage"
                :src="getBase64ImageUrl(currentSliceImage)"
                class="report-image"
                alt="缺陷检测图谱"
              />
              <div v-else class="no-img-text">未获取到原始图像</div>
              <div class="image-watermark">灵眸巡诊 缺陷切片图谱</div>
            </div>
            <div class="slice-pagination-bar">
              <span class="slice-page-indicator">当前展示: {{ currentSliceIndex + 1 }} / {{ (sliceImagesList && sliceImagesList.length) || 1 }}</span>
              <div class="slice-page-actions">
                <el-button size="mini" icon="el-icon-arrow-left" :disabled="currentSliceIndex <= 0" @click="prevSliceImage">上一张</el-button>
                <el-button size="mini" :disabled="currentSliceIndex >= sliceImagesList.length - 1" @click="nextSliceImage">下一张 <i class="el-icon-arrow-right"></i></el-button>
              </div>
            </div>
          </div>

          <!-- 右侧：Qwen 大模型智控专家研判中枢 -->
          <div class="split-right">
            <div class="section-title">
              <i class="el-icon-cpu"></i> 智控专家大模型研判中枢 (Qwen-AI)
            </div>
            <div class="advice-block-card">
              <div class="advice-item">
                <div class="item-title">
                  <span class="icon-tag tag-info">1</span>
                  <strong>总体缺陷情况研判</strong>
                </div>
                <div class="item-content">
                  {{ currentExpertAdvice && currentExpertAdvice['总体缺陷情况'] ? currentExpertAdvice['总体缺陷情况'] : `工件表面累计检出 ${currentExpertReport.defectionsSum || 4} 处异常，当前状态：COMPLETED。` }}
                </div>
              </div>

              <div class="advice-item">
                <div class="item-title">
                  <span class="icon-tag tag-warning">2</span>
                  <strong>综合分析依据 (AI报告)</strong>
                </div>
                <div class="item-content">
                  {{ currentExpertAdvice && currentExpertAdvice['综合分析依据'] ? currentExpertAdvice['综合分析依据'] : '缺陷呈局部聚集分布，累计面积占比约 27.4%，最高严重程度评定为 4 级。' }}
                </div>
              </div>

              <div class="advice-item highlight-item">
                <div class="item-title">
                  <span class="icon-tag tag-danger">3</span>
                  <strong>车间工件处置指令</strong>
                </div>
                <div class="item-content bold-action">
                  {{ currentExpertAdvice && currentExpertAdvice['最终处置建议'] ? currentExpertAdvice['最终处置建议'] : '建议质检员现场卡尺测量，根据公差标准判定是否返修' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 缺陷切片明细列表 -->
        <div class="report-table-section">
          <div class="section-title">
            <i class="el-icon-document-copy"></i> 缺陷检测切片结构化明细
          </div>
          <el-table
            :data="expertTableList"
            size="small"
            border
            style="width: 100%"
            class="expert-inner-table"
          >
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="imageName" label="所属原图" width="140" align="center">
              <template slot-scope="scope">
                <span class="font-mono">{{ scope.row.imageName || 'mock_01.jpg' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="缺陷类型" width="130" align="center">
              <template slot-scope="scope">
                <el-tag size="small" type="danger" effect="plain" class="defect-type-pill">{{ scope.row.category || '划痕/裂痕' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="defectCount" label="本图缺陷数" width="110" align="center">
              <template slot-scope="scope">
                <strong>{{ scope.row.defectCount || 2 }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="判定状态" width="100" align="center">
              <template slot-scope="scope">
                <span class="status-ng-badge">{{ scope.row.status || 'NG' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="repairSuggestion" label="初步工艺建议">
              <template slot-scope="scope">
                <span class="report-repair-text">{{ scope.row.repairSuggestion || '建议质检员现场卡尺测量，根据公差标准判定是否返修' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 报告底部签字栏 -->
        <div class="report-footer">
          <div class="footer-sign">
            <span>质检核对员：__________________</span>
            <span>车间工段长：__________________</span>
          </div>
          <div class="footer-note">
            * 本报告由灵眸巡诊深度视觉大模型自动分析生成，仅供生产线质检与工艺处置复核参考。
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from 'axios';

export default {
  name: 'SixSManager',
  data() {
    return {
      logoImg: require('@/assets/logo.7f766218.png'),
      defaultAvatar: require('@/assets/头像.jpg'),
      checking: false,
      capturing: false,
      reportDialogVisible: false,
      reportFilterTab: 'all',
      reportTime: '',
      currentInspectionStage: '',
      currentExpertPackage: null,
      expertDetailVisible: false,
      expertReportLoading: false,
      currentExpertReport: null,
      currentExpertAdvice: null,
      currentSliceIndex: 0,
      sliceImagesList: [],
      expertTableList: [],
      expertDefectImagesCount: 13,
      activeCard: 'seiri',
      inputQuestion: '',
      isThinking: false,
      thinkingText: '正在调阅 6S 管理标准规范与工位数据...',
      typingTimer: null,
      isTyping: false,
      radarChartInstance: null,
      watchPollTimer: null,
      lastKnownImageId: null,
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
          content: '您好！我是灵鉴 🦾。\n本工作台已接入车间工业物联网总线与 6S 精益管控体系，支持：\n1. **6S 现场执行规范智能问答**（整理、整顿、清扫、清洁、素养、安全）\n2. **工位自动化协同控制**（支持下发机械臂原点复位、AGV 分拣小车归位）\n3. **光学感知设备维护标准及自检报告输出**\n\n请点击上方快捷指令或直接输入问题咨询！',
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
    currentSliceImage() {
      if (this.sliceImagesList && this.sliceImagesList.length > 0) {
        return this.sliceImagesList[this.currentSliceIndex] || (this.currentExpertReport ? this.currentExpertReport.imgBase64 : '');
      }
      return this.currentExpertReport ? this.currentExpertReport.imgBase64 : '';
    },
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
      this.initWatchImageListener();
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
    if (this.watchPollTimer) {
      clearInterval(this.watchPollTimer);
      this.watchPollTimer = null;
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
    handleStageCheck(stage) {
      this.currentInspectionStage = stage;
      const stageMap = {
        pre: {
          name: '事前检查 (开机准入点检)',
          query: '请执行工位【事前检查】：检查机械臂、分拣小车是否归位，半轴是否定置归位，桌面是否规整，人员是否规范佩戴安全帽。'
        },
        in: {
          name: '事中检查 (过程合规巡检)',
          query: '请执行工位【事中检查】：检查算法置信度阈值、人员劳保手套与安全帽规范佩戴、缺陷检测操作与判定过程是否规范。'
        },
        post: {
          name: '事后检查 (班后维护与归位)',
          query: '请执行工位【事后检查】：检查机械臂与分拣小车是否复位、半轴是否分拣成功、现场工位与桌面是否整齐、辅机是否安全断电。'
        }
      };
      const target = stageMap[stage];
      if (target) {
        this.$message.success(`已切换至「${target.name}」标准化核对模式`);
        this.sendQuickQuestion(target.query);
      }
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

        const isPreCheck = q.includes('事前检查') || q.includes('班前点检') || q.includes('开机准入');
        const isPostCheck = q.includes('事后检查') || q.includes('班后维护') || q.includes('停机归整');

        const assistantMsg = {
          role: 'assistant',
          content: '',
          time: this.getNowTime(),
          interactive: (isPreCheck || isPostCheck) ? 'reset_arm' : null,
          userChoice: null
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
    async handleArmResetChoice(choice, msgObj) {
      if (!msgObj || msgObj.userChoice) return;
      this.$set(msgObj, 'userChoice', choice);

      if (choice === 'no') {
        // 点击“否”：不产生硬件动作，仅记录选择
        this.messageList.push({
          role: 'user',
          content: '否',
          time: this.getNowTime()
        });
        this.$nextTick(() => this.scrollToBottom());
      } else if (choice === 'yes') {
        // 点击“是”：调用车间检测页面同款回原位接口 api/aubo/photo/moveToHome
        this.messageList.push({
          role: 'user',
          content: '是，请将机械臂复位',
          time: this.getNowTime()
        });
        this.isThinking = true;
        this.thinkingText = '正在下发工控指令，调度机械臂平稳复位至原点...';
        this.$nextTick(() => this.scrollToBottom());

        try {
          const res = await axios.post('api/aubo/photo/moveToHome');
          this.isThinking = false;
          if (res.data && res.data.code === 200) {
            this.$message.success('机械臂已回原位');
            const doneMsg = {
              role: 'assistant',
              content: '好的，已通过工控总线为全周检测工位下发指令：**机械臂六轴已平稳复位至初始原点**，伺服抱闸锁定正常，处于待命就绪状态！🦾',
              time: this.getNowTime()
            };
            this.messageList.push(doneMsg);
            this.startTypewriter(doneMsg, doneMsg.content);
          } else {
            const errTip = (res.data && res.data.message) || '机械臂回原位响应异常';
            this.$message.error(errTip);
            this.messageList.push({
              role: 'assistant',
              content: `机械臂复位指令执行未成功：${errTip}，请检查工控总线连接状态。`,
              time: this.getNowTime()
            });
          }
        } catch (e) {
          this.isThinking = false;
          this.$message.error('调度机械臂移动失败，请确认工控网络已连接');
          this.messageList.push({
            role: 'assistant',
            content: '工控通信失败：未能成功连接到机械臂控制器，请检查网络或总线设置。',
            time: this.getNowTime()
          });
          this.$nextTick(() => this.scrollToBottom());
        }
      }
    },
    async handleVmCapture() {
      if (this.capturing) return;
      this.capturing = true;
      try {
        // 1. 联动自动开启/确保服务器端目录监听已启动（相当于联动点击了目录监听页面的【启动目录监听】）
        try {
          await axios.post('api/cameraWatch/start');
        } catch (watchErr) {
          console.warn('启动目录监听提示:', watchErr);
        }

        // 2. 触发 VisionMaster 拍照并存盘
        const res = await axios.post('api/vmCamera/trigger', { timeout: 5000 });
        this.capturing = false;
        if (res.data && res.data.code === 200) {
          this.$message.success('拍照成功！图片已自动存入 D 盘，正在同步入库推送...');
        } else {
          const msg = (res.data && res.data.message) || '触发拍照未成功';
          this.$message.warning(msg);
        }
      } catch (err) {
        this.capturing = false;
        this.$message.error('请求拍照接口失败，请检查后端服务');
      }
    },
    formatImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      return 'http://127.0.0.1:8081' + (url.startsWith('/') ? url : '/' + url);
    },
    initWatchImageListener() {
      // 1. 初始化先拉取一次最新记录，记录最后一张图片的 ID
      axios.get('api/cameraWatch/status').then(res => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const list = res.data.data.list;
          if (Array.isArray(list) && list.length > 0) {
            this.lastKnownImageId = list[0].id;
          }
        }
      }).catch(() => {});

      // 2. 启动轻量定时轮询，一旦检测到目录监听产生新图片，6S管家自动把照片以卡片形式发给用户
      this.watchPollTimer = setInterval(() => {
        axios.get('api/cameraWatch/status').then(res => {
          if (res.data && res.data.code === 200 && res.data.data) {
            const list = res.data.data.list;
            if (Array.isArray(list) && list.length > 0) {
              const newest = list[0];
              if (this.lastKnownImageId !== null && newest.id !== this.lastKnownImageId) {
                this.lastKnownImageId = newest.id;
                // 6S 管家主动发送包含图片的消息
                this.messageList.push({
                  role: 'assistant',
                  content: `📷 **感知到新工件抓拍图片**：\n已自动捕获并同步入库，文件名称为 \`${newest.fileName}\`，点击下方缩略图可查看大图：`,
                  time: this.getNowTime(),
                  imageCard: newest
                });
                this.$nextTick(() => this.scrollToBottom());
              } else if (this.lastKnownImageId === null) {
                this.lastKnownImageId = newest.id;
              }
            }
          }
        }).catch(() => {});
      }, 1500);
    },
    generate6SAnswer(query) {
      // 1. 最高优先级：三阶段综合点检（事前/事中/事后检查）
      if (query.includes('事前检查') || query.includes('班前点检') || query.includes('开机准入')) {
        return `### 🕒 【6S·事前检查】班前开机与工位准入 5 大合规核验：\n1. ⚠️ **机械臂归位检查**：<span style="color:#ef4444;font-weight:700;">【未复位】</span> 全周检测机械臂未回原点待命位，需复位就绪。\n2. 🛺 **分拣小车归位检查**：<span style="color:#16a34a;font-weight:700;">【正常】</span> AGV 分拣小车处于规定标定待命点，避障传感器常开。\n3. ⚙️ **半轴定置归位检查**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 待检半轴已定置放置于固定工位，摆放规范整齐。\n4. 🧹 **桌面规整检查**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 工作台面规整整洁，劳保手套与工位用具已定置。\n5. 👷‍♂️ **人员安全帽与劳保合规**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 进入作业区人员已 100% 正确佩戴安全帽与劳保手套。\n\n🤖 **管家提示**：检测到机械臂未复位，是否立即让机械臂复位？`;
      }
      if (query.includes('事中检查') || query.includes('过程巡检') || query.includes('过程合规')) {
        return `### ⚡ 【6S·事中检查】生产作业与缺陷质检过程 4 大合规核验：\n1. 🎯 **算法置信度阈值监控**：<span style="color:#16a34a;font-weight:700;">【正常】</span> AI 深度视觉识别算法置信度阈值锁定 ≥0.85，运行状态稳定。\n2. 🧤 **劳保手套佩戴规范**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 质检人员全程规范佩戴劳保手套，严禁裸手接触精加工工件。\n3. 👷‍♂️ **作业人员安全帽规范**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 作业区人员 100% 正确佩戴安全帽并扣紧下颚带。\n4. 🔍 **缺陷检测过程与判定规范**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 严格执行半轴全周旋转检测流程，合格品与缺陷品定置分流合规。`;
      }
      if (query.includes('事后检查') || query.includes('班后维护') || query.includes('停机归整')) {
        return `### 🏁 【6S·事后检查】班后维护与停机归整 4 大合规核验：\n1. ⚠️ **机械臂复位检查**：<span style="color:#ef4444;font-weight:700;">【未复位】</span> 全周检测机械臂停机后未回归待命原点。\n2. 🛺 **小车复位检查**：<span style="color:#16a34a;font-weight:700;">【正常】</span> AGV 分拣小车已安全调度复位至指定充电待命点。\n3. ⚙️ **半轴分拣结果确认**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 批次半轴已全部完成检测并按类别入库，无滞留混料。\n4. 🧹 **工位整齐与台面维持**：<span style="color:#16a34a;font-weight:700;">【正常】</span> 现场工作台面清洁整齐、手套用具定置归位，完成 6S 数字化点检交接。\n\n🤖 **管家提示**：检测到机械臂未复位，是否立即让机械臂复位？`;
      }

      // 2. 硬件与控制指令
      if (query.includes('机械臂复位') || query.includes('复位机械臂') || query.includes('将机械臂复位')) {
        return `好的，已通过工控总线为全周检测工位下发指令：**机械臂六轴已平稳复位至初始原点**，伺服抱闸锁定正常，处于待命就绪状态！🦾`;
      }
      if (query.includes('小车归位') || query.includes('分拣小车归位') || query.includes('小车复位') || query.includes('归位分拣小车') || query.includes('AGV小车归位')) {
        return `好的，已为车间分拣单元下发调度指令：**分拣小车已安全调度归位**，随时准备下一批次缺陷品转运！🛺`;
      }

      // 3. 6S 单项标准规范
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
        return `### 📌 【6S·素养 (Shitsuke)】质检人员行为规程：\n1. 严格遵守半轴外观缺陷判定基准（GB/T 38885）；\n2. 严禁未经授权修改 AI 缺陷识别置信度阈值（当前阈值锁定 ≥0.85）；\n3. 作业过程穿戴劳保手套，严禁裸手接触精加工半轴表面。`;
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

      // 6S 当前实测与演进多轮拟合曲线 (非满分、合理梯次实测值，综合平均匹配 94.7分)
      const targetScore = [96, 94, 95, 93, 97, 93];
      const baseScore = [32, 36, 28, 30, 42, 38];
      const totalRounds = 26;

      const seriesList = [];
      for (let i = 1; i <= totalRounds; i++) {
        const t = i / totalRounds;
        const v1 = Math.round(baseScore[0] + (targetScore[0] - baseScore[0]) * Math.pow(t, 0.85));
        const v2 = Math.round(baseScore[1] + (targetScore[1] - baseScore[1]) * Math.pow(t, 1.25));
        const v3 = Math.round(baseScore[2] + (targetScore[2] - baseScore[2]) * Math.pow(t, 1.45));
        const v4 = Math.round(baseScore[3] + (targetScore[3] - baseScore[3]) * Math.pow(t, 0.9));
        const v5 = Math.round(baseScore[4] + (targetScore[4] - baseScore[4]) * Math.pow(t, 1.15));
        const v6 = Math.round(baseScore[5] + (targetScore[5] - baseScore[5]) * Math.pow(t, 1.05));

        const isLatest = (i === totalRounds);
        seriesList.push({
          type: 'radar',
          symbol: isLatest ? 'circle' : 'none',
          symbolSize: isLatest ? 5 : 0,
          lineStyle: {
            width: isLatest ? 2 : 1,
            opacity: isLatest ? 1 : 0.85
          },
          emphasis: {
            lineStyle: {
              width: 2.5
            },
            areaStyle: {
              color: 'rgba(234, 179, 8, 0.25)'
            }
          },
          data: [
            {
              value: [v1, v2, v3, v4, v5, v6],
              name: isLatest ? '最新巡检实测' : `巡检批次 #${i}`
            }
          ]
        });
      }

      const option = {
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(255, 255, 255, 0.96)',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          padding: [10, 14],
          textStyle: { color: '#1e293b', fontSize: 12 },
          extraCssText: 'box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.08); border-radius: 10px; backdrop-filter: blur(8px);',
          formatter: (params) => {
            let str = `<div style="font-weight:700;margin-bottom:8px;color:#0f172a;font-size:13px;border-bottom:1px solid #f1f5f9;padding-bottom:5px;display:flex;align-items:center;justify-content:space-between;">
              <span>${params.name || '6S 巡检实测'}</span>
              <span style="font-size:11px;color:#0284c7;background:#f0f9ff;padding:1px 6px;border-radius:4px;font-weight:600;">达成率 94.7%</span>
            </div>`;
            const indicators = ['整理', '整顿', '清扫', '清洁', '素养', '安全'];
            params.value.forEach((v, idx) => {
              str += `<div style="display:flex;justify-content:space-between;gap:18px;font-size:12px;line-height:1.75;">
                <span style="color:#64748b">${indicators[idx]}</span>
                <span style="font-weight:700;color:#0284c7;font-family:monospace;">${v} <span style="font-size:10.5px;color:#94a3b8;font-weight:normal;">分</span></span>
              </div>`;
            });
            return str;
          }
        },
        visualMap: {
          top: 'middle',
          right: 12,
          min: 0,
          max: 100,
          formatter: '{value}分',
          itemWidth: 12,
          itemHeight: 130,
          textStyle: {
            color: '#64748b',
            fontSize: 11
          },
          inRange: {
            color: ['#ef4444', '#f97316', '#eab308', '#22c55e', '#06b6d4']
          },
          calculable: true
        },
        radar: {
          indicator: [
            { name: '整理', max: 100 },
            { name: '整顿', max: 100 },
            { name: '清扫', max: 100 },
            { name: '清洁', max: 100 },
            { name: '素养', max: 100 },
            { name: '安全', max: 100 }
          ],
          radius: '56%',
          center: ['46%', '52%'],
          splitNumber: 4,
          shape: 'polygon',
          axisName: {
            color: '#1e293b',
            fontWeight: 700,
            fontSize: 14
          },
          splitLine: {
            lineStyle: {
              color: [
                'rgba(226, 232, 240, 0.7)',
                'rgba(226, 232, 240, 0.8)',
                'rgba(203, 213, 225, 0.9)',
                'rgba(203, 213, 225, 0.95)'
              ],
              width: 1
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: [
                'rgba(248, 250, 252, 0.4)',
                'rgba(241, 245, 249, 0.4)',
                'rgba(248, 250, 252, 0.45)',
                'rgba(241, 245, 249, 0.55)'
              ]
            }
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(203, 213, 225, 0.75)',
              width: 1
            }
          }
        },
        series: seriesList
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
      this.$message.info('正在联动车间各传感器，执行全车间智能巡诊实时探测...');
      setTimeout(() => {
        this.checking = false;
        const now = new Date();
        const pad = n => String(n).padStart(2, '0');
        this.reportTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
        this.reportDialogVisible = true;
        this.$message.warning('智能巡诊巡检完成：现场发现 4 项工况及 6S 异常隐患，已生成分析报告！');
      }, 800);
    },
    handleRecheck() {
      this.runAutoCheck();
    },
    handleAutoFixAll() {
      this.$confirm('是否立即联动工控总线执行一键整改（机械臂原点复位、AGV返航寻充、关闭闲置辅机电源）？', '工控协同整改', {
        confirmButtonText: '立即执行',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('整改指令已下发至 PLC 及 AGV 调度系统！机械臂正在复位，AGV正在归位。');
        setTimeout(() => {
          this.reportDialogVisible = false;
        }, 1200);
      });
    },
    exportReport() {
      this.$message.success('已生成《车间6S数字化精益合规巡检诊断简报》并导出！');
    },
    handleDetail(row) {
      this.sendQuickQuestion(`请详细说明【${row.station}】在【${row.category}】方面的标准要求及操作指导。`);
    },
    // 打开 AI 智控专家分析报告原件弹窗
    openExpertReportDetail() {
      this.reportDialogVisible = false; // 关闭上一层巡检诊断弹窗，避免多层弹窗 DOM 堆叠导致定位紊乱与顶部遮挡
      this.expertDetailVisible = true;
      this.expertReportLoading = true;
      this.currentSliceIndex = 0;

      // 读取最新专家报告与全过程工况数据
      let pkg = null;
      try {
        const stored = localStorage.getItem('LATEST_6S_EXPERT_PACKAGE');
        if (stored) {
          pkg = JSON.parse(stored);
        }
      } catch (e) {
        console.error('读取 6S 专家报告缓存失败', e);
      }

      setTimeout(() => {
        if (pkg) {
          this.currentExpertPackage = pkg;
          this.expertDefectImagesCount = pkg.defectImagesCount || 13;
          this.expertTableList = pkg.expertTableList || [];
          this.sliceImagesList = (pkg.expertTableList || []).map(i => i.imgBase64 || pkg.imgBase64).filter(Boolean);
          if (this.sliceImagesList.length === 0 && pkg.imgBase64) {
            this.sliceImagesList = [pkg.imgBase64];
          }

          this.currentExpertReport = {
            id: pkg.reportId || '202609',
            workOrderId: pkg.workOrderId || '半轴总成-全周巡检',
            time: pkg.checkTime || '2026-9-3 18:58:19',
            defectionsSum: pkg.totalDefects || 26,
            imgBase64: pkg.imgBase64 || '',
            defections: []
          };

          this.currentExpertAdvice = {
            '总体缺陷情况': `工件表面累计检出 ${pkg.totalDefects || 26} 处异常，当前状态：COMPLETED。`,
            '最严重等级': pkg.maxSeverity || '严重',
            '综合分析依据': '缺陷呈局部聚集分布，累计面积占比约 27.4%，最高严重程度评定为 4 级。',
            '最终处置建议': pkg.decision || '建议质检员现场卡尺测量，根据公差标准判定是否返修'
          };
        } else {
          // 兜底默认值
          this.expertDefectImagesCount = 13;
          this.expertTableList = [
            { imageName: '工件切片_01.jpg', category: '夹杂', defectCount: 4, status: 'NG', repairSuggestion: '表面夹杂异物，建议超声波探伤并评估深度' },
            { imageName: '工件切片_02.jpg', category: '划痕', defectCount: 2, status: 'NG', repairSuggestion: '建议局部砂纸抛光打磨后复检' },
            { imageName: '工件切片_03.jpg', category: '裂纹', defectCount: 1, status: 'NG', repairSuggestion: '高风险结构裂纹，建议直接送探伤复检或报废处置' }
          ];
          this.currentExpertReport = {
            id: '202609',
            workOrderId: '半轴总成-全周巡检',
            time: '2026-9-3 18:58:19',
            defectionsSum: 26,
            imgBase64: '',
            defections: []
          };
          this.currentExpertAdvice = {
            '总体缺陷情况': '工件表面累计检出 26 处异常，当前状态：COMPLETED。',
            '最严重等级': '严重',
            '综合分析依据': '缺陷呈局部聚集分布，累计面积占比约 27.4%，最高严重程度评定为 4 级。',
            '最终处置建议': '建议质检员现场卡尺测量，根据公差标准判定是否返修'
          };
        }
        this.expertReportLoading = false;
      }, 350);
    },
    getBase64ImageUrl(base64) {
      if (!base64) return '';
      if (base64.startsWith('data:image')) {
        return base64;
      }
      return `data:image/jpeg;base64,${base64}`;
    },
    prevSliceImage() {
      if (this.currentSliceIndex > 0) {
        this.currentSliceIndex--;
      }
    },
    nextSliceImage() {
      if (this.sliceImagesList && this.currentSliceIndex < this.sliceImagesList.length - 1) {
        this.currentSliceIndex++;
      }
    },
    printExpertReport() {
      const printableDom = document.getElementById('expert-report-printable-sixs');
      if (!printableDom) {
        this.$message.error('未找到可打印的报告内容');
        return;
      }

      let oldIframe = document.getElementById('expert-report-print-iframe-sixs');
      if (oldIframe) {
        document.body.removeChild(oldIframe);
      }

      const iframe = document.createElement('iframe');
      iframe.id = 'expert-report-print-iframe-sixs';
      iframe.style.position = 'fixed';
      iframe.style.right = '0';
      iframe.style.bottom = '0';
      iframe.style.width = '0';
      iframe.style.height = '0';
      iframe.style.border = 'none';
      document.body.appendChild(iframe);

      const iframeDoc = iframe.contentWindow.document;

      const htmlContent = `
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>AI工业表面缺陷智控专家分析报告</title>
          <style>
            @page {
              size: A4 portrait;
              margin: 10mm 12mm;
            }
            * {
              box-sizing: border-box;
              margin: 0;
              padding: 0;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
              color: #1f2937;
              background: #ffffff;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
              padding: 5px;
            }
            .no-print { display: none !important; }
            .report-header {
              border-bottom: 2px solid #2563eb;
              padding-bottom: 12px;
              margin-bottom: 14px;
            }
            .brand-badge {
              display: inline-block;
              background: #eff6ff;
              color: #2563eb;
              border: 1px solid #bfdbfe;
              font-size: 11px;
              font-weight: 600;
              padding: 2px 8px;
              border-radius: 4px;
              margin-bottom: 4px;
            }
            .report-title {
              font-size: 18px;
              color: #111827;
              font-weight: 700;
              margin: 3px 0 8px 0;
            }
            .report-meta {
              display: flex;
              gap: 16px;
              font-size: 11px;
              color: #4b5563;
            }
            .report-kpi-grid {
              display: grid;
              grid-template-columns: repeat(4, 1fr);
              gap: 10px;
              margin-bottom: 14px;
            }
            .kpi-card {
              border: 1px solid #e5e7eb;
              border-radius: 6px;
              padding: 8px 10px;
              border-left: 4px solid #9ca3af;
              background: #f9fafb;
            }
            .kpi-card.danger { border-left-color: #ef4444; background: #fef2f2; }
            .kpi-card.warning { border-left-color: #f59e0b; background: #fffbeb; }
            .kpi-card.primary { border-left-color: #3b82f6; background: #eff6ff; }
            .kpi-card.success { border-left-color: #10b981; background: #ecfdf5; }
            .kpi-label { font-size: 11px; color: #6b7280; }
            .kpi-val { font-size: 16px; font-weight: 700; color: #111827; margin: 3px 0; }
            .kpi-val.decision { font-size: 13px; color: #b91c1c; }
            .kpi-sub { font-size: 10px; color: #9ca3af; }
            .report-split-section {
              display: grid;
              grid-template-columns: 1fr 1.3fr;
              gap: 12px;
              margin-bottom: 14px;
            }
            .section-title {
              font-size: 12px;
              font-weight: 700;
              color: #1f2937;
              margin-bottom: 6px;
            }
            .report-image-box {
              background: #000000;
              border-radius: 6px;
              height: 200px;
              display: flex;
              align-items: center;
              justify-content: center;
              overflow: hidden;
              position: relative;
            }
            .report-image {
              max-width: 100%;
              max-height: 100%;
              object-fit: contain;
            }
            .image-watermark {
              position: absolute;
              bottom: 4px;
              right: 6px;
              background: rgba(0, 0, 0, 0.7);
              color: #fff;
              font-size: 9px;
              padding: 1px 4px;
              border-radius: 2px;
            }
            .advice-block-card {
              background: #f9fafb;
              border: 1px solid #e5e7eb;
              border-radius: 6px;
              padding: 10px 12px;
              height: 178px;
              display: flex;
              flex-direction: column;
              gap: 8px;
            }
            .advice-item {
              border-bottom: 1px dashed #e5e7eb;
              padding-bottom: 6px;
            }
            .advice-item:last-child { border-bottom: none; }
            .item-title { font-size: 11px; font-weight: 700; color: #374151; margin-bottom: 2px; }
            .icon-tag {
              display: inline-block;
              width: 14px;
              height: 14px;
              line-height: 14px;
              text-align: center;
              border-radius: 50%;
              font-size: 9px;
              color: #fff;
              margin-right: 4px;
            }
            .tag-info { background: #3b82f6; }
            .tag-warning { background: #f59e0b; }
            .tag-danger { background: #ef4444; }
            .item-content { font-size: 11px; color: #4b5563; line-height: 1.4; padding-left: 18px; }
            .item-content.bold-action { color: #dc2626; font-weight: bold; background: #fee2e2; padding: 3px 6px; border-radius: 3px; }
            .report-table-section { margin-bottom: 14px; }
            table {
              width: 100%;
              border-collapse: collapse;
              font-size: 11px;
            }
            th, td {
              border: 1px solid #d1d5db;
              padding: 6px 8px;
              text-align: center;
            }
            th { background: #f3f4f6; font-weight: 600; color: #374151; }
            .report-footer {
              border-top: 1px solid #e5e7eb;
              padding-top: 10px;
              display: flex;
              justify-content: space-between;
              font-size: 11px;
              color: #4b5563;
            }
            .footer-sign { display: flex; gap: 30px; }
            .footer-note { font-size: 9px; color: #9ca3af; }
          </style>
        </head>
        <body>
          ${printableDom.innerHTML}
        </body>
        </html>
      `;

      iframeDoc.open();
      iframeDoc.write(htmlContent);
      iframeDoc.close();

      setTimeout(() => {
        iframe.contentWindow.focus();
        iframe.contentWindow.print();
      }, 250);
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

/* ================= 3. 顶部 4 列 KPI 紧凑概览条 (已弃用归档，避免样式污染) ================= */
.legacy-top-kpi-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.legacy-kpi-card {
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

/* ================= 3. 顶部全局统一标准卡片 Header（与系统日志、API、密钥管理高度样式完全对齐） ================= */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 8px !important;
  padding: 18px 24px !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05) !important;
  border: 1px solid #ebeef5 !important;
  box-sizing: border-box !important;
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
  border-radius: 8px !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  font-size: 22px;
  flex-shrink: 0;
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
  border-radius: 4px !important;
  padding: 3px 10px;
  line-height: 1.3;
}

.page-desc {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-act-btn-primary {
  height: 32px !important;
  padding: 0 15px !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #2588ea 0%, #1577d7 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #ffffff !important;
  box-shadow: 0 2px 6px rgba(34, 132, 225, 0.2) !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 5px !important;
  transition: all 0.2s ease !important;
}

.header-act-btn-primary:hover {
  background: linear-gradient(135deg, #3b9bff 0%, #1985ee 100%) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(34, 132, 225, 0.3) !important;
}

.header-act-btn-outline {
  height: 32px !important;
  padding: 0 15px !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  background: #ffffff !important;
  border: 1px solid rgba(180, 210, 238, 0.8) !important;
  border-radius: 8px !important;
  color: #4e647a !important;
  display: inline-flex !important;
  align-items: center !important;
  gap: 5px !important;
  transition: all 0.2s ease !important;
}

.header-act-btn-outline:hover {
  border-color: #2388e8 !important;
  color: #2388e8 !important;
  box-shadow: 0 2px 6px rgba(35, 136, 232, 0.1) !important;
}

/* ================= 4. 中部 6S 流程步骤连贯卡片 ================= */
.flow-steps-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.step-item-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px !important;
  padding: 12px 14px 10px 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 110px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.step-item-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.85;
  transition: opacity 0.25s ease;
}

.step-item-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.step-item-card:hover::before {
  opacity: 1;
}

/* 1. 整理 (Seiri) - 科技深蓝 */
.step-card-seiri {
  background: linear-gradient(145deg, #ffffff 0%, #f0f7ff 100%);
  border-color: #dbeafe;
}
.step-card-seiri::before { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }
.step-card-seiri:hover { border-color: #93c5fd; box-shadow: 0 8px 20px rgba(37, 99, 235, 0.12); }
.step-card-seiri .step-num-badge { background: linear-gradient(135deg, #3b82f6, #1d4ed8); box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3); }
.step-card-seiri .card-score-text { color: #1d4ed8; }

/* 2. 整顿 (Seiton) - 精益翠绿 */
.step-card-seiton {
  background: linear-gradient(145deg, #ffffff 0%, #f0fdf4 100%);
  border-color: #dcfce7;
}
.step-card-seiton::before { background: linear-gradient(90deg, #10b981, #059669); }
.step-card-seiton:hover { border-color: #86efac; box-shadow: 0 8px 20px rgba(5, 150, 105, 0.12); }
.step-card-seiton .step-num-badge { background: linear-gradient(135deg, #10b981, #059669); box-shadow: 0 2px 6px rgba(5, 150, 105, 0.3); }
.step-card-seiton .card-score-text { color: #059669; }

/* 3. 清扫 (Seiso) - 琥珀明金 */
.step-card-seiso {
  background: linear-gradient(145deg, #ffffff 0%, #fffbeb 100%);
  border-color: #fef3c7;
}
.step-card-seiso::before { background: linear-gradient(90deg, #f59e0b, #d97706); }
.step-card-seiso:hover { border-color: #fde68a; box-shadow: 0 8px 20px rgba(217, 119, 6, 0.12); }
.step-card-seiso .step-num-badge { background: linear-gradient(135deg, #f59e0b, #d97706); box-shadow: 0 2px 6px rgba(217, 119, 6, 0.3); }
.step-card-seiso .card-score-text { color: #d97706; }

/* 4. 清洁 (Seiketsu) - 碧波海青 */
.step-card-seiketsu {
  background: linear-gradient(145deg, #ffffff 0%, #ecfeff 100%);
  border-color: #cffafe;
}
.step-card-seiketsu::before { background: linear-gradient(90deg, #06b6d4, #0891b2); }
.step-card-seiketsu:hover { border-color: #67e8f9; box-shadow: 0 8px 20px rgba(8, 145, 178, 0.12); }
.step-card-seiketsu .step-num-badge { background: linear-gradient(135deg, #06b6d4, #0891b2); box-shadow: 0 2px 6px rgba(8, 145, 178, 0.3); }
.step-card-seiketsu .card-score-text { color: #0891b2; }

/* 5. 素养 (Shitsuke) - 质感靛紫 */
.step-card-shitsuke {
  background: linear-gradient(145deg, #ffffff 0%, #f5f3ff 100%);
  border-color: #ede9fe;
}
.step-card-shitsuke::before { background: linear-gradient(90deg, #6366f1, #4f46e5); }
.step-card-shitsuke:hover { border-color: #c4b5fd; box-shadow: 0 8px 20px rgba(99, 102, 241, 0.12); }
.step-card-shitsuke .step-num-badge { background: linear-gradient(135deg, #6366f1, #4f46e5); box-shadow: 0 2px 6px rgba(99, 102, 241, 0.3); }
.step-card-shitsuke .card-score-text { color: #4f46e5; }

/* 6. 安全 (Safety) - 警戒红盾 */
.step-card-safety {
  background: linear-gradient(145deg, #ffffff 0%, #fef2f2 100%);
  border-color: #fee2e2;
}
.step-card-safety::before { background: linear-gradient(90deg, #ef4444, #dc2626); }
.step-card-safety:hover { border-color: #fca5a5; box-shadow: 0 8px 20px rgba(220, 38, 38, 0.12); }
.step-card-safety .step-num-badge { background: linear-gradient(135deg, #ef4444, #dc2626); box-shadow: 0 2px 6px rgba(220, 38, 38, 0.3); }
.step-card-safety .card-score-text { color: #dc2626; }

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
  width: 26px;
  height: 26px;
  border-radius: 6px !important;
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title-text {
  font-size: 16px;
  font-weight: 700;
  color: #1c3047;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-score-text {
  font-size: 14.5px;
  font-weight: 800;
  color: #2388e8;
  flex-shrink: 0;
}

.card-desc-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 6px 0 2px 0;
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
  align-items: stretch;
  height: 640px;
  min-height: 640px;
  max-height: 640px;
}

.left-ai-column {
  height: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ================= 左翼：AI 工作台 (全页视觉中心) ================= */
.ai-workbench-card {
  flex: 1;
  height: 100%;
  max-height: 100%;
  min-height: 0;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 18px !important;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.04);
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
  overflow-x: hidden;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f1f7fe 0%, #eaf3fd 100%);
  border-radius: 16px !important;
  border: 1px solid rgba(195, 222, 248, 0.45);
  display: flex;
  flex-direction: column;
  gap: 12px;
  scrollbar-width: thin;
  scrollbar-color: rgba(35, 136, 232, 0.35) transparent;
}

.ai-chat-stage::-webkit-scrollbar {
  width: 6px;
}

.ai-chat-stage::-webkit-scrollbar-track {
  background: transparent;
}

.ai-chat-stage::-webkit-scrollbar-thumb {
  background: rgba(35, 136, 232, 0.28);
  border-radius: 4px;
}

.ai-chat-stage::-webkit-scrollbar-thumb:hover {
  background: rgba(35, 136, 232, 0.55);
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
  border-radius: 50% !important;
  background: transparent;
  border: none;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: none;
}

.ai-avatar-box img {
  width: 100%;
  height: 100%;
  border-radius: 50% !important;
  object-fit: cover;
}

.ai-avatar-box.user-avatar-box {
  border-radius: 50% !important;
  border: 1px solid rgba(195, 222, 248, 0.8);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.ai-avatar-box.user-avatar-box img {
  width: 100%;
  height: 100%;
  border-radius: 50% !important;
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
.right-data-column {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 14px;
}

.analysis-card {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border-radius: 18px !important;
  border: 1px solid #e1ebf5;
  box-shadow: 0 6px 20px rgba(28, 48, 71, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
  transition: all 0.25s ease;
}

.analysis-card:hover {
  border-color: #cbdbe8;
  box-shadow: 0 10px 24px rgba(35, 136, 232, 0.07);
}

.card-header-clean {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #edf3f9;
  background: #fbfdff;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon-box {
  width: 28px;
  height: 28px;
  border-radius: 8px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.bg-blue-subtle {
  background: #eaf4ff;
}

.bg-green-subtle {
  background: #f0fdf4;
}

.header-title-text {
  font-size: 17px;
  font-weight: 700;
  color: #1c3047;
  letter-spacing: -0.2px;
}

.header-right {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.rate-label {
  font-size: 13px;
  color: #8a9aaf;
}

.rate-value {
  font-size: 16px;
  font-weight: 800;
  color: #16a34a;
}

.radar-card {
  flex: 1;
  height: 100%;
  margin-bottom: 0;
}

.radar-chart-stage {
  flex: 1;
  width: 100%;
  min-height: 0;
}

.table-card {
  flex: 1;
  min-height: 0;
  height: auto;
}

.table-container {
  flex: 1;
  min-height: 0;
  padding: 6px 12px 10px;
  overflow-y: auto;
}

.styled-sixs-table {
  background: transparent !important;
}

.styled-sixs-table ::v-deep tr {
  background: transparent !important;
}

.styled-sixs-table ::v-deep th.el-table__cell {
  background-color: #f6f9fc !important;
  color: #334155 !important;
  font-weight: 700;
  font-size: 12px;
  padding: 7px 0 !important;
  border-bottom: 1px solid #e8eff5 !important;
}

.styled-sixs-table ::v-deep td.el-table__cell {
  padding: 6px 0 !important;
  font-size: 12px;
  border-bottom: 1px solid #f0f4f8 !important;
  color: #334155;
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

/* ================= 7. 智能巡诊现场深度诊断报告弹窗 (现代高端浅色精工风格) ================= */
::v-deep .sixs-report-dialog.light-precision-modal {
  margin-top: 5vh !important;
  margin-bottom: 5vh !important;
  border-radius: 18px !important;
  overflow: hidden !important;
  background: #ffffff !important;
  border: 1px solid rgba(180, 210, 238, 0.45) !important;
  box-shadow: 0 25px 50px -12px rgba(15, 23, 42, 0.22), 0 0 0 1px rgba(226, 232, 240, 0.8) !important;
}

::v-deep .sixs-report-dialog.light-precision-modal .el-dialog__header {
  padding: 0 !important;
  border-bottom: none !important;
  background: transparent !important;
}

::v-deep .sixs-report-dialog.light-precision-modal .el-dialog__body {
  padding: 18px 22px !important;
  background: #ffffff !important;
}

::v-deep .sixs-report-dialog.light-precision-modal .el-dialog__footer {
  padding: 12px 22px !important;
  background: #f8fafc !important;
  border-top: 1px solid #e2e8f0 !important;
}

/* 顶部浅色标题栏 */
.report-light-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 22px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
}

.light-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.light-shield-badge {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #dc2626;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.15);
}

.light-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.light-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.light-title-text {
  margin: 0;
  font-size: 16.5px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.2px;
}

.light-live-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 8px;
  border-radius: 6px;
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #dc2626;
  font-size: 11px;
  font-weight: 700;
}

.live-dot-red {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #dc2626;
  animation: pulse-dot-red 1.5s infinite ease-in-out;
}

@keyframes pulse-dot-red {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.3; transform: scale(0.8); }
}

.light-sec-code {
  font-size: 11px;
  color: #64748b;
  border: 1px solid #cbd5e1;
  background: #ffffff;
  padding: 2px 6px;
  border-radius: 4px;
}

.light-subtitle-row {
  font-size: 10.5px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
  letter-spacing: 0.3px;
}

.sub-sep {
  color: #cbd5e1;
}

.light-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.light-clock-pill {
  font-size: 11.5px;
  color: #0369a1;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  padding: 4px 10px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 600;
}

.light-close-btn {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s ease;
}

.light-close-btn:hover {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
  transform: rotate(90deg);
}

/* 浅色内容区 */
.report-light-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 1. 顶部三联 Bento 卡片 (浅色微拟物) */
.light-bento-row {
  display: grid;
  grid-template-columns: 1.15fr 1fr 1fr;
  gap: 12px;
}

.bento-light-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.03);
}

.bento-light-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.bento-light-label {
  font-size: 11.5px;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 700;
}

.bento-light-tag {
  font-size: 10.5px;
  padding: 2px 7px;
  border-radius: 4px;
  font-weight: 700;
}

.bento-light-tag.danger {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.bento-light-tag.warn {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fde68a;
}

.bento-light-tag.success {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.score-main-flex {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-number-box {
  display: flex;
  align-items: baseline;
  gap: 4px;
  flex-shrink: 0;
}

.score-num {
  font-size: 26px;
  font-weight: 900;
  color: #dc2626;
  line-height: 1;
}

.score-unit {
  font-size: 11.5px;
  color: #94a3b8;
  font-weight: 600;
}

.score-delta-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.delta-bar-shell {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #e2e8f0;
  overflow: hidden;
}

.delta-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ef4444 0%, #f97316 100%);
  border-radius: 3px;
}

.delta-meta {
  display: flex;
  justify-content: space-between;
  font-size: 10.5px;
  color: #64748b;
  font-weight: 500;
}

.text-danger { color: #dc2626 !important; font-weight: 700; }

.hazard-stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.hazard-block {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.hazard-block.danger .hz-count { color: #dc2626; }
.hazard-block.amber .hz-count { color: #d97706; }
.hazard-block.blue .hz-count { color: #0284c7; }

.hz-count {
  font-size: 16px;
  font-weight: 800;
  line-height: 1.1;
}

.hz-label {
  font-size: 9.5px;
  color: #64748b;
  white-space: nowrap;
}

.bus-status-body {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.interlock-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #0369a1;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 600;
}

.pulse-cyan {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #0284c7;
  animation: pulse-dot-blue 1.5s infinite;
}

@keyframes pulse-dot-blue {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.bus-meta-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  color: #64748b;
}

.dot-sep { color: #cbd5e1; }

/* 2. 分类筛选 Pills 交互栏 */
.light-filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
}

.filter-pills-group {
  display: flex;
  gap: 8px;
}

.filter-pill-btn {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.filter-pill-btn:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.filter-pill-btn.is-active {
  background: #e0f2fe;
  border-color: #38bdf8;
  color: #0284c7;
  box-shadow: 0 1px 4px rgba(2, 132, 199, 0.15);
}

.filter-pill-btn.danger.is-active {
  background: #fee2e2;
  border-color: #f87171;
  color: #dc2626;
  box-shadow: 0 1px 4px rgba(220, 38, 38, 0.15);
}

.filter-pill-btn.amber.is-active {
  background: #fef3c7;
  border-color: #f59e0b;
  color: #d97706;
  box-shadow: 0 1px 4px rgba(217, 119, 6, 0.15);
}

.filter-pill-btn.cyan.is-active {
  background: #e0f2fe;
  border-color: #38bdf8;
  color: #0284c7;
  box-shadow: 0 1px 4px rgba(2, 132, 199, 0.15);
}

.pill-badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

.pill-badge.danger { background: #fee2e2; color: #dc2626; border-color: #fca5a5; }
.pill-badge.amber { background: #fef3c7; color: #d97706; border-color: #fde68a; }
.pill-badge.cyan { background: #e0f2fe; color: #0284c7; border-color: #bae6fd; }

.filter-tip-right {
  font-size: 11px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 3. 现场详细隐患卡片流 (浅色工控卡，白话通俗、高度自适应防止字被截断) */
.light-issues-stream {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
  padding: 2px 6px 6px 2px;
}

.custom-light-scroll::-webkit-scrollbar {
  width: 5px;
}

.custom-light-scroll::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.custom-light-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.custom-light-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.light-issue-card {
  position: relative;
  flex-shrink: 0 !important; /* 关键：彻底防止卡片被 flexbox 压扁挤压 */
  width: 100% !important;
  box-sizing: border-box !important;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 16px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04);
}

.light-issue-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.card-glow-edge {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.card-glow-edge.red {
  background: #ef4444;
}

.card-glow-edge.amber {
  background: #f59e0b;
}

.card-glow-edge.cyan {
  background: #0284c7;
}

.card-inner-shell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.issue-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-left-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.light-tag-pill {
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 4px;
}

.light-tag-pill.red {
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.light-tag-pill.amber {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fde68a;
}

.light-tag-pill.cyan {
  background: #e0f2fe;
  color: #0284c7;
  border: 1px solid #bae6fd;
}

.light-tag-category {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}

.light-station-tag {
  font-size: 11px;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 3px;
  font-weight: 600;
}

.light-dev-code {
  font-size: 10px;
  color: #64748b;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  padding: 1px 5px;
  border-radius: 3px;
}

.meta-right-state {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 600;
}

.status-dot.red { background: #dc2626; }
.status-dot.amber { background: #d97706; }
.status-dot.cyan { background: #0284c7; }

.state-txt.red { color: #dc2626; font-weight: 700; }
.state-txt.amber { color: #d97706; font-weight: 700; }
.state-txt.cyan { color: #0284c7; font-weight: 700; }

.issue-title-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.issue-heading {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.1px;
}

.telemetry-capsule {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 10.5px;
  width: fit-content;
}

.cap-k { color: #64748b; }
.cap-v { color: #0369a1; font-weight: 600; }

.issue-detail-narrative {
  margin: 0;
  font-size: 12px;
  color: #475569;
  line-height: 1.6;
}

.issue-detail-narrative code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  background: #f1f5f9;
  color: #0369a1;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 11px;
}

.issue-action-dock {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  padding: 6px 10px;
  margin-top: 2px;
  gap: 12px;
}

.dock-left-guide {
  font-size: 11px;
  color: #166534;
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.guide-lead {
  color: #15803d;
  font-weight: 700;
  white-space: nowrap;
}

.dock-act-btn {
  font-size: 11px !important;
  padding: 5px 10px !important;
  border-radius: 6px !important;
  white-space: nowrap !important;
  font-weight: 600 !important;
}

/* 4. 底部数据审计四联指标栏 */
.light-bottom-audit-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 14px;
}

.audit-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.audit-k {
  font-size: 10.5px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.audit-v-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.audit-v {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.audit-sub {
  font-size: 10px;
  color: #64748b;
}

.text-emerald { color: #16a34a !important; }
.text-crimson { color: #dc2626 !important; }
.text-cyan { color: #0284c7 !important; }

/* 5. 底部浅色操作栏 */
.report-light-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.light-footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bus-beacon-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #16a34a;
  box-shadow: 0 0 6px #16a34a;
}

.bus-status-txt {
  font-size: 11px;
  color: #64748b;
}

.light-footer-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.light-ghost-btn {
  background: #ffffff !important;
  border: 1px solid #cbd5e1 !important;
  color: #475569 !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
}

.light-ghost-btn:hover {
  background: #f1f5f9 !important;
  color: #0f172a !important;
}

.light-recheck-btn {
  border-color: #93c5fd !important;
  color: #0284c7 !important;
  background: #f0f9ff !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
}

.light-recheck-btn:hover {
  background: #e0f2fe !important;
  border-color: #38bdf8 !important;
}

.light-hero-exec-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  border: none !important;
  color: #ffffff !important;
  border-radius: 8px !important;
  font-weight: 700 !important;
  box-shadow: 0 3px 10px rgba(220, 38, 38, 0.25) !important;
  transition: all 0.2s ease;
}

.light-hero-exec-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(220, 38, 38, 0.35) !important;
}

/* ================= 8. AI 智控专家分析报告原件专属排版样式 (与预警信息/实时检测页面 1:1 完全对齐) ================= */
.expert-report-dialog :deep(.el-dialog) {
  margin-top: 3vh !important;
  margin-bottom: 3vh !important;
  top: 0 !important;
  transform: none !important;
  max-height: 94vh !important;
  display: flex !important;
  flex-direction: column !important;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.25);
  border: 1px solid #e2e8f0;
}

.expert-report-dialog :deep(.el-dialog__header) {
  flex-shrink: 0 !important;
  background: #ffffff;
  padding: 16px 24px;
  border-bottom: 1px solid #eef0f3;
}

.expert-report-dialog :deep(.el-dialog__title) {
  color: #1e293b;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -0.2px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.expert-report-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748b;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.expert-report-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #0f172a;
  transform: rotate(90deg);
}

.expert-report-dialog :deep(.el-dialog__body) {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: 20px 24px !important;
  background: #fcfdfd;
  height: 100% !important;
  box-sizing: border-box !important;
}

.expert-loading {
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
  font-size: 14px;
}

.expert-loading i {
  font-size: 42px;
  color: #2563eb;
  margin-bottom: 16px;
}

.expert-report-container {
  padding: 4px 8px;
  background: transparent;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
  color: #1e293b;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 18px;
  margin-bottom: 20px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08) 0%, rgba(59, 130, 246, 0.12) 100%);
  color: #1d4ed8;
  border: 1px solid rgba(59, 130, 246, 0.28);
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.report-title {
  margin: 6px 0 14px 0;
  font-size: 23px;
  color: #0f172a;
  font-weight: 800;
  letter-spacing: -0.4px;
}

.report-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #64748b;
}

.report-meta span {
  background: #f8fafc;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.report-meta strong {
  color: #1e293b;
  font-weight: 600;
}

.report-meta .meta-highlight-tag {
  background: #ecfdf5;
  color: #059669;
  border-color: #a7f3d0;
}

.report-meta .meta-highlight-tag strong {
  color: #047857;
}

/* 4大核心指标卡片 4列水平网格 */
.report-kpi-grid {
  display: grid !important;
  grid-template-columns: repeat(4, 1fr) !important;
  gap: 16px !important;
  margin-bottom: 24px !important;
}

.kpi-card {
  border-radius: 10px;
  padding: 16px 18px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04), 0 1px 2px rgba(15, 23, 42, 0.02);
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
}

.kpi-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.kpi-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.kpi-label {
  font-size: 12.5px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.kpi-val {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.kpi-val .unit {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  margin-left: 2px;
}

.kpi-sub {
  font-size: 11.5px;
  color: #94a3b8;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-card.danger { border-top: 3px solid #ef4444; }
.kpi-card.danger .kpi-icon-wrap { background: #fef2f2; color: #ef4444; }
.kpi-card.danger .kpi-val { color: #dc2626; }

.kpi-card.warning { border-top: 3px solid #f59e0b; }
.kpi-card.warning .kpi-icon-wrap { background: #fffbeb; color: #d97706; }
.kpi-card.warning .kpi-val.highlight { color: #b45309; font-size: 21px; }

.kpi-card.primary { border-top: 3px solid #2563eb; }
.kpi-card.primary .kpi-icon-wrap { background: #eff6ff; color: #2563eb; }
.kpi-card.primary .kpi-val { color: #1d4ed8; }

.kpi-card.success { border-top: 3px solid #059669; }
.kpi-card.success .kpi-icon-wrap { background: #ecfdf5; color: #059669; }
.kpi-card.success .kpi-val.decision { font-size: 14.5px; color: #991b1b; font-weight: 700; line-height: 1.45; }

/* 左右分栏 */
.report-split-section {
  display: grid !important;
  grid-template-columns: 1fr 1.2fr !important;
  gap: 20px !important;
  margin-bottom: 24px !important;
}

.section-title {
  font-size: 14.5px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 7px;
}

.section-title i {
  color: #2563eb;
  font-size: 16px;
}

.report-image-box {
  position: relative;
  background: #090d16;
  border-radius: 10px;
  height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.4);
}

.report-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.image-watermark {
  position: absolute;
  bottom: 8px;
  right: 10px;
  background: rgba(15, 23, 42, 0.82);
  backdrop-filter: blur(4px);
  color: #e2e8f0;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 5px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.slice-pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.slice-page-indicator {
  font-size: 11.5px;
  color: #475569;
  font-weight: 600;
  font-family: monospace;
}

.slice-page-actions {
  display: flex;
  gap: 6px;
}

.advice-block-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 260px;
  box-sizing: border-box;
  overflow-y: auto;
}

.advice-item {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 12px;
}

.advice-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: #1e293b;
  margin-bottom: 6px;
  font-weight: 700;
}

.icon-tag {
  display: inline-flex;
  width: 20px;
  height: 20px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 11px;
  color: #ffffff;
  font-weight: 800;
}

.icon-tag.tag-info { background: #3b82f6; }
.icon-tag.tag-warning { background: #f59e0b; }
.icon-tag.tag-danger { background: #ef4444; }

.item-content {
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
  padding-left: 28px;
}

.item-content.bold-action {
  font-size: 13.5px;
  font-weight: 700;
  color: #991b1b;
  background: #fef2f2;
  padding: 8px 14px;
  border-radius: 6px;
  border: 1px solid #fee2e2;
  border-left: 4px solid #ef4444;
  margin-top: 6px;
  line-height: 1.5;
}

.report-table-section {
  margin-bottom: 22px;
}

.report-table-section :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.report-table-section :deep(.el-table th) {
  background-color: #f1f5f9 !important;
  color: #475569 !important;
  font-weight: 700 !important;
  font-size: 12.5px !important;
}

.report-footer {
  border-top: 1px solid #e2e8f0;
  padding-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-sign {
  display: flex;
  gap: 48px;
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.footer-note {
  font-size: 11.5px;
  color: #94a3b8;
  font-style: italic;
}
</style>

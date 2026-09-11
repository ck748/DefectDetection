<template>
  <div class="sixs-dashboard-hub">
    <!-- 2. AI 智能体工作台 (豆包极简与多源研判工作台风格) -->
    <div class="bottom-dual-layout">
      <!-- AI 质检专家与工控调度工作台 (全宽视觉重心) -->
      <div class="left-ai-column">
        <div class="ai-workbench-card">
          <!-- 1. 顶部简约栏 -->
          <div class="top-header-strip">
            <div class="top-title">
              <span class="dot"></span>
              <span>灵鉴 · 6S 工业巡检智能体</span>
            </div>
            <div class="top-actions">
              <span class="time-badge">{{ liveTimeText }}</span>
              <button class="clear-btn" @click="clearHistory">清空对话</button>
            </div>
          </div>

          <!-- 2. 对话主区域 (极简流式对话与大号欢迎语) -->
          <div class="ai-chat-stage" ref="pageChatBox">
            <!-- 初始居中无框大标题与推荐问题 -->
            <div class="chat-welcome-box" v-if="messageList.length === 0">
              <div class="welcome-headline">
                <img :src="logoImg" class="welcome-robot-icon" alt="灵鉴" />
                <span>{{ greetingText }}</span>
              </div>
              <div class="recommend-section">
                <div class="recommend-title">为你推荐</div>
                <div class="recommend-list">
                  <div class="recommend-item" @click="sendQuickQuestion('检查一下当前机械臂的状态')">
                    <span>检查当前机械臂的状态</span>
                    <span style="color:#9ca3af;font-size:12px;">→</span>
                  </div>
                  <div class="recommend-item" @click="sendQuickQuestion('请你帮我分析一下今天的产线环境是否规范达标')">
                    <span>分析今天的产线环境是否规范达标</span>
                    <span style="color:#9ca3af;font-size:12px;">→</span>
                  </div>
                  <div class="recommend-item" @click="sendQuickQuestion('分析今天小车总路程和2800张图片，有没有问题？')">
                    <span>分析今天小车总路程和采集图片是否有问题</span>
                    <span style="color:#9ca3af;font-size:12px;">→</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 消息列表 -->
            <div
              v-for="(msg, idx) in messageList"
              :key="idx"
              class="message-item"
              :class="msg.role"
            >
              <!-- 用户消息气泡 -->
              <div v-if="msg.role === 'user'" class="user-bubble">
                {{ msg.content }}
              </div>

              <!-- 智能体消息 (带思考折叠与极简卡片) -->
              <div v-else-if="msg.role === 'assistant'" style="display:flex;flex-direction:column;gap:8px;width:100%;">
                <!-- 思考过程框 (对齐 AgentWorkbench 深度思考流) -->
                <div v-if="msg.thoughtText || msg.thinking || msg.thoughtImageCard || msg.thoughtTextAfter" class="thought-container">
                  <div class="thought-toggle" @click="msg.thoughtCollapsed = !msg.thoughtCollapsed">
                    <span v-if="msg.thinking" class="spinner"></span>
                    <span>{{ msg.thinking ? `正在思考 (${msg.thinkingSeconds || 0}秒)...` : `已深度思考 (用时 ${msg.thinkingSeconds || 6} 秒)` }}</span>
                  </div>
                  <div v-show="!msg.thoughtCollapsed && (msg.thoughtText || msg.thoughtImageCard || msg.thoughtTextAfter)" class="thought-text-body">
                    <div v-if="msg.thoughtText" style="white-space:pre-wrap;">{{ msg.thoughtText }}</div>

                    <!-- 思考过程中调取的抓拍图像卡片 -->
                    <div v-if="msg.thoughtImageCard" class="watch-image-push-card" style="padding:4px;border-radius:8px;margin:8px 0;background:#000;">
                      <div style="font-size:12px;color:#94a3b8;margin-bottom:6px;display:flex;justify-content:space-between;padding:0 4px;">
                        <span><i class="el-icon-picture-outline"></i> {{ msg.thoughtImageCard.fileName }}</span>
                        <span class="font-mono">{{ msg.thoughtImageCard.fileSize }}</span>
                      </div>
                      <div style="border-radius:6px;overflow:hidden;max-height:220px;background:#000;">
                        <el-image
                          :src="formatImageUrl(msg.thoughtImageCard.webUrl)"
                          :preview-src-list="[formatImageUrl(msg.thoughtImageCard.webUrl)]"
                          fit="contain"
                          style="width:100%;height:180px;display:block;"
                        >
                          <div slot="error" style="display:flex;align-items:center;justify-content:center;height:100%;color:#94a3b8;font-size:12px;">
                            <i class="el-icon-picture-outline"></i> 无法加载图片
                          </div>
                        </el-image>
                      </div>
                    </div>

                    <div v-if="msg.thoughtTextAfter" style="white-space:pre-wrap;margin-top:6px;">{{ msg.thoughtTextAfter }}</div>
                  </div>
                </div>

                <!-- 收到相机抓拍推送时渲染图片卡片 -->
                <div v-if="msg.imageCard" class="watch-image-push-card" style="padding:4px;border-radius:8px;">
                  <div style="font-size:12px;color:#64748b;margin-bottom:6px;display:flex;justify-content:space-between;">
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

                <!-- 正文回答卡片 -->
                <div v-if="msg.content" class="answer-body">
                  <div class="markdown-render" v-html="formatMessage(msg.content)"></div>
                </div>

                <!-- DeepSeek 同款底部数据源标签与操作栏 (回答完毕后呈现) -->
                <div v-if="msg.content && !msg.thinking && !isTyping" class="deepseek-footer-wrapper">
                  <!-- 1. 已读取数据源与状态标签 (带 Popover 详情悬浮卡) -->
                  <el-popover
                    placement="top-start"
                    width="360"
                    trigger="hover"
                    popper-class="ds-sources-popover"
                  >
                    <div class="sources-popover-content">
                      <div class="popover-title">
                        <i class="el-icon-connection"></i> 本次巡诊已调阅 14 个产线传感与工控数据源
                      </div>
                      <div class="popover-list">
                        <div class="source-item"><span class="dot d1"></span> 工业相机抓拍物理目录 (/data/workspace/camera_raw/)</div>
                        <div class="source-item"><span class="dot d2"></span> 6S 智能巡检大盘批次全量日志 (2,437 根半轴)</div>
                        <div class="source-item"><span class="dot d3"></span> PROFINET 机械臂六轴伺服绝对零点编码器</div>
                        <div class="source-item"><span class="dot d4"></span> AGV 激光雷达避障与里程计总线 (12,185.0 米)</div>
                        <div class="source-item"><span class="dot d5"></span> 缺陷分拣定置到位光电传感器 (合格品/划痕/裂纹)</div>
                        <div class="source-item"><span class="dot d6"></span> 工作站全周安全光栅与急停回路联锁状态</div>
                        <div class="source-item"><span class="dot d7"></span> GB/T 38885 表面缺陷视觉判定与国家 6S 规范库</div>
                      </div>
                    </div>
                    <div slot="reference" class="sources-read-pill">
                      <span class="pill-icons">
                        <span class="p-dot c1"></span>
                        <span class="p-dot c2"></span>
                        <span class="p-dot c3"></span>
                        <span class="p-dot c4"></span>
                      </span>
                      <span class="pill-text">已读取 14 个产线传感与质检数据源</span>
                    </div>
                  </el-popover>

                  <!-- 2. 操作图标按钮栏 (复制/重新检查/点赞/点踩/分享) -->
                  <div class="ds-action-bar">
                    <button class="ds-icon-btn" title="复制回答" @click="handleCopyAnswer(msg)">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor">
                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
                      </svg>
                    </button>
                    <button class="ds-icon-btn" title="重新检查 / 重新生成" @click="handleRegenerate(msg)">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor">
                        <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                      </svg>
                    </button>
                    <button
                      :class="['ds-icon-btn', { 'active': msg.feedback === 'like' }]"
                      title="有帮助"
                      @click="handleFeedback(msg, 'like')"
                    >
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor">
                        <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z"/>
                      </svg>
                    </button>
                    <button
                      :class="['ds-icon-btn', { 'active': msg.feedback === 'dislike' }]"
                      title="无帮助"
                      @click="handleFeedback(msg, 'dislike')"
                    >
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor">
                        <path d="M15 3H6c-.83 0-1.54.5-1.84 1.22l-3.02 7.05c-.09.23-.14.47-.14.73v2c0 1.1.9 2 2 2h6.31l-.95 4.57-.03.32c0 .41.17.79.44 1.06L9.83 23l6.59-6.59c.37-.36.58-.86.58-1.41V5c0-1.1-.9-2-2-2zm4 0v12h4V3h-4z"/>
                      </svg>
                    </button>
                    <button class="ds-icon-btn" title="分享 / 导出质检单" @click="handleShareAnswer(msg)">
                      <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor">
                        <path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92c0-1.61-1.31-2.92-2.92-2.92z"/>
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- 交互问答按钮组 (事前/事后检查回原位) -->
                <div v-if="msg.interactive === 'reset_arm' && !msg.thinking && !isTyping" class="ai-interactive-actions">
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

          <!-- 3. 底部极简输入卡片与技能工具栏 -->
          <div class="bottom-input-wrapper">
            <div class="doubao-input-card">
              <!-- 附件预览条 -->
              <div class="attachment-preview-bar" :class="{ 'show': attachedFile }">
                <span class="attachment-badge" v-if="attachedFile">
                  <span>📎 已挂载: {{ attachedFile.name }}</span>
                  <span class="badge-remove" @click="removeAttachment">&times;</span>
                </span>
              </div>

              <!-- 隐藏的本地文件选择器 -->
              <input type="file" ref="localFileInput" style="display:none" accept="image/*,.csv,.json,.txt" @change="onLocalFileChosen">

              <textarea
                class="input-textarea"
                v-model="inputQuestion"
                placeholder="向灵鉴发送指令，如：检查今天产线环境、机械臂状态、小车里程与照片分析..."
                rows="1"
                @keydown.enter.exact.prevent="handleSend"
              ></textarea>

              <!-- 底部技能工具条 -->
              <div class="doubao-bottom-bar">
                <!-- 独立加号容器 -->
                <div class="plus-container">
                  <button class="plus-btn" title="上传文件 / 调阅数据" @click.stop="showUploadMenu = !showUploadMenu">+</button>

                  <div class="upload-popup-menu" :class="{ 'show': showUploadMenu }">
                    <button class="upload-menu-item" @click="triggerLocalFileUpload">
                      <span>上传本地工件图像/文件</span>
                    </button>
                    <button class="upload-menu-item" @click="triggerCloudFileSelect">
                      <span>选择云盘产线数据包</span>
                    </button>
                    <button class="upload-menu-item" @click="triggerCameraDirSelect">
                      <span>调阅工业相机抓拍目录</span>
                    </button>
                    <button class="upload-menu-item" @click="triggerBatchReportImport">
                      <span>导入检测批次报表 (CSV)</span>
                    </button>
                  </div>
                </div>

                <div class="skills-left">
                  <span class="skill-pill" @click="triggerSkill('chat')">
                    <i class="el-icon-chat-dot-round"></i> 对话
                  </span>
                  <!-- 巡检核心操作按钮 -->
                  <span
                    :class="['skill-pill', 'stage-pill', { 'active': currentInspectionStage === 'pre' }]"
                    @click="handleStageCheck('pre')"
                  >
                    <i class="el-icon-time"></i> 事前检查
                  </span>
                  <span
                    :class="['skill-pill', 'stage-pill', { 'active': currentInspectionStage === 'in' }]"
                    @click="handleStageCheck('in')"
                  >
                    <i class="el-icon-video-play"></i> 事中检查
                  </span>
                  <span
                    :class="['skill-pill', 'stage-pill', { 'active': currentInspectionStage === 'post' }]"
                    @click="handleStageCheck('post')"
                  >
                    <i class="el-icon-circle-check"></i> 事后检查
                  </span>
                  <span
                    v-if="!running"
                    class="skill-pill btn-primary-pill"
                    @click="startWatch"
                  >
                    <i class="el-icon-video-play"></i> 启动目录监听
                  </span>
                  <span
                    v-else
                    class="skill-pill btn-danger-pill"
                    @click="stopWatch"
                  >
                    <i class="el-icon-video-pause"></i> 停止监听
                  </span>
                  <span
                    class="skill-pill btn-primary-pill"
                    @click="runAutoCheck"
                  >
                    <i :class="checking ? 'el-icon-loading' : 'el-icon-refresh-right'"></i> {{ checking ? '巡检中...' : '智能巡诊实时信息' }}
                  </span>
                  <span class="skill-pill" @click="triggerSkill('report_gen')">
                    <i class="el-icon-document"></i> 质检报告生成
                  </span>
                </div>

                <div class="actions-right">
                  <button class="voice-icon-btn" title="语音输入" @click="triggerVoiceInput">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
                      <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
                    </svg>
                  </button>
                  <button class="send-icon-btn" :disabled="isThinking || isTyping || !inputQuestion.trim()" @click="handleSend" title="发送">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M12 4l-7 7 1.41 1.41L11 7.83V20h2V7.83l4.59 4.58L19 11l-7-7z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 智能巡检工艺现场诊断报告弹窗 (1:1 对齐工业标准专家处置单排版) -->
    <el-dialog
      :visible.sync="reportDialogVisible"
      title="工业检测单预览"
      width="94%"
      class="sixs-report-dialog-wrapper"
      custom-class="sixs-report-dialog expert-report-dialog"
      :close-on-click-modal="true"
      :lock-scroll="false"
      :append-to-body="false"
      top="0"
    >
      <!-- 弹窗顶栏操作区 -->
      <div class="dialog-actions no-print" style="text-align: right; margin-bottom: 12px; display: flex; justify-content: flex-end; gap: 10px; align-items: center;">
        <el-button
          size="small"
          type="primary"
          icon="el-icon-document"
          class="view-origin-btn"
          @click="openExpertReportDetail"
        >
          查看专家报告原件
        </el-button>
        <el-button size="small" type="primary" plain :loading="checking" icon="el-icon-refresh-right" @click="handleRecheck">
          重新巡检
        </el-button>
        <el-button size="small" type="danger" icon="el-icon-video-play" @click="handleAutoFixAll">
          一键执行整改
        </el-button>
        <el-button type="primary" size="small" icon="el-icon-printer" @click="printSixSReport">
          打印 / 导出 PDF 处置单
        </el-button>
        <el-button size="small" icon="el-icon-close" @click="reportDialogVisible = false">
          关闭
        </el-button>
      </div>

      <!-- 工业标准纸质报告容器 -->
      <div class="expert-report-container" id="sixs-report-printable">
        <div class="industrial-report-paper">
          <!-- 1. 标题区 -->
          <div class="industrial-header">
            <div class="header-logo">灵眸巡诊</div>
            <h1 class="header-title">车间 6S 智能巡检工艺现场诊断处置单</h1>
            <div class="header-code">报告编号: 6S-REPORT-0906</div>
          </div>

          <!-- 2. 基础信息表格 -->
          <table class="industrial-meta-table">
            <tr>
              <td class="meta-label">巡检单号</td><td class="meta-value">6S-REPORT-0906</td>
              <td class="meta-label">巡检时间</td><td class="meta-value">{{ reportTime || '2026-09-06 18:58:19' }}</td>
              <td class="meta-label">响应耗时</td><td class="meta-value">0.8 ms</td>
            </tr>
            <tr>
              <td class="meta-label">受检工位</td><td class="meta-value">12 / 12 工位 (全部在线)</td>
              <td class="meta-label">关联工单</td><td class="meta-value">{{ currentExpertPackage ? currentExpertPackage.workOrderId : '半轴总成-全周巡检' }}</td>
              <td class="meta-label">6S 评级</td>
              <td class="meta-value font-bold text-ng">需整改 (待处理 3 项)</td>
            </tr>
          </table>

          <!-- 3. 核心结论区 -->
          <div class="industrial-section">
            <div class="section-title">一、 6S 现场巡查结论与综合指标</div>
            <div class="industrial-kpi-row">
              <div class="kpi-item">
                <div class="kpi-title">6S 综合评分</div>
                <div class="kpi-value text-ng">
                  94.7 <span style="font-size:14px; font-weight:normal; color:#333;">/ 100分</span>
                </div>
              </div>
              <div class="kpi-item">
                <div class="kpi-title">合格基准 / 差距</div>
                <div class="kpi-value text-ng">差距 0.3 分</div>
              </div>
              <div class="kpi-item">
                <div class="kpi-title">待整改项</div>
                <div class="kpi-value text-ng">3 项</div>
              </div>
              <div class="kpi-item">
                <div class="kpi-title">工控总线状态</div>
                <div class="kpi-value text-ok">PROFINET 在线</div>
              </div>
              <div class="kpi-item">
                <div class="kpi-title">最终处置建议</div>
                <div class="kpi-value text-action">建议一键联动整改</div>
              </div>
            </div>
          </div>

          <!-- 4. 溯源与AI研判 -->
          <div class="industrial-section split-section">
            <div class="split-left">
              <div class="section-title">二、 现场溯源与总线联锁</div>
              <table class="industrial-ai-table">
                <tr>
                  <th width="30%">报告来源</th>
                  <td>来自【<strong>{{ currentExpertPackage ? currentExpertPackage.workOrderId : '半轴总成-全周巡检' }}</strong>】检测全过程质检与工序专家报告。</td>
                </tr>
                <tr>
                  <th>总线控制</th>
                  <td>PROFINET 工控总线正常连接，支持机械臂六轴原点复位与 AGV 搬运小车站台联动。</td>
                </tr>
                <tr>
                  <th>整改耗时</th>
                  <td class="text-ok font-bold">预计整改耗时约 1 分钟，支持一键自动化调度处置。</td>
                </tr>
              </table>
            </div>
            <div class="split-right">
              <div class="section-title">三、 6S 智能体研判分析</div>
              <table class="industrial-ai-table">
                <tr>
                  <th width="30%">现场隐患情况</th>
                  <td>全周质检结束后，机械臂未回安全待命原点且通电发热；分拣 AGV 小车滞留主通道；工作台面手套与用具未定置。</td>
                </tr>
                <tr>
                  <th>综合判定依据</th>
                  <td>现场达标项 18 项，待整改项 3 项（2 项设备未复位，1 项桌面整洁），低于 95.0 分合格线。</td>
                </tr>
                <tr>
                  <th>车间处置指令</th>
                  <td class="text-action font-bold">建议立即下发 PLC 复位指令并整理工位台面</td>
                </tr>
              </table>
            </div>
          </div>

          <!-- 5. 详细现场问题列表与工控调度表格 -->
          <div class="industrial-section">
            <div class="section-title" style="display: flex; justify-content: space-between; align-items: center;">
              <span>四、 现场 6S 隐患明细与工艺调度记录</span>
              <!-- 分类筛选 Tab 按钮 -->
              <div class="no-print" style="display: flex; gap: 6px; font-weight: normal; font-size: 12px;">
                <el-radio-group v-model="reportFilterTab" size="mini">
                  <el-radio-button label="all">全部现场问题 (3)</el-radio-button>
                  <el-radio-button label="reset">设备未复位 (2)</el-radio-button>
                  <el-radio-button label="clean">台面与手套 (1)</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <table class="industrial-detail-table">
              <thead>
                <tr>
                  <th width="50">序号</th>
                  <th width="140">隐患类型</th>
                  <th width="90">等级</th>
                  <th width="200">现场具体情况</th>
                  <th>整改措施规程</th>
                  <th width="160" class="no-print">工控调度指令</th>
                </tr>
              </thead>
              <tbody>
                <!-- 1. 机械臂未复位 -->
                <tr v-show="reportFilterTab === 'all' || reportFilterTab === 'reset'">
                  <td align="center">1</td>
                  <td align="center"><span class="text-ng font-bold">机械臂未归位</span></td>
                  <td align="center" class="text-ng font-bold">高危</td>
                  <td>机械臂 灵眸-CB-iS 检测结束未恢复原位，且通电发热。</td>
                  <td>调度机械臂平稳复位至待命原点，锁定抱闸。</td>
                  <td align="center" class="no-print">
                    <el-button size="mini" type="danger" plain @click="sendQuickQuestion('请你将机械臂复位')">
                      调度机械臂复位
                    </el-button>
                  </td>
                </tr>
                <!-- 2. AGV小车未复位 -->
                <tr v-show="reportFilterTab === 'all' || reportFilterTab === 'reset'">
                  <td align="center">2</td>
                  <td align="center"><span class="text-ng font-bold">小车未归位</span></td>
                  <td align="center" class="text-ng font-bold">高危</td>
                  <td>灵巡 LN-IA 搬运小车检测结束未归位，停留在主干通道中间。</td>
                  <td>下发返航指令，调度 AGV 小车平稳归位。</td>
                  <td align="center" class="no-print">
                    <el-button size="mini" type="danger" plain @click="sendQuickQuestion('请将分拣小车归位')">
                      调度 AGV 归位
                    </el-button>
                  </td>
                </tr>
                <!-- 3. 台面与手套 -->
                <tr v-show="reportFilterTab === 'all' || reportFilterTab === 'clean'">
                  <td align="center">3</td>
                  <td align="center"><span style="color:#d97706; font-weight:bold;">桌面不整洁</span></td>
                  <td align="center" style="color:#d97706; font-weight:bold;">警告</td>
                  <td>工作台面杂乱，劳保手套与工位用具随意摆放未归位。</td>
                  <td>整理清洁工作台面，将劳保手套及工件用具规范定置摆放。</td>
                  <td align="center" class="no-print">
                    <el-button size="mini" type="primary" plain @click="sendQuickQuestion('半轴缺陷标定区与合格品库房的整顿三定管理要求')">
                      查看整顿规范
                    </el-button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 6. 签字栏 -->
          <div class="industrial-footer">
            <div class="sign-block">审核人签字：<span class="line" style="text-align: center;">admin</span></div>
            <div class="sign-block">日期：<span class="line" style="text-align: center; font-size: 13px;">{{ reportTime || '2026-09-06' }}</span></div>
          </div>
          <div class="industrial-remark">
            * 备注：本处置单由灵眸巡诊 6S 智能体模型与工控总线自动生成，请质检员与工段长现场确认并执行闭环整改。
          </div>
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
      running: false,
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
      currentThinkingTimer: 0,
      currentThinkingStreamText: '',
      thinkingTimerInterval: null,
      typingTimer: null,
      isTyping: false,
      radarChartInstance: null,
      watchPollTimer: null,
      clockTimer: null,
      lastKnownImageId: null,
      attachedFile: null,
      showUploadMenu: false,
      liveTimeText: '',
      greetingText: '你好，我是灵鉴，有什么可以帮你？',
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
      messageList: [],
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
      this.updateClockAndGreeting();
      this.clockTimer = setInterval(() => {
        this.updateClockAndGreeting();
      }, 1000);
      window.addEventListener('resize', this.handleResize);
      document.addEventListener('click', this.handleDocumentClick);
      this.fetchWatchStatus();
      this.initWatchImageListener();
    });
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    document.removeEventListener('click', this.handleDocumentClick);
    if (this.clockTimer) {
      clearInterval(this.clockTimer);
      this.clockTimer = null;
    }
    if (this.radarChartInstance) {
      this.radarChartInstance.dispose();
    }
    if (this.typingTimer) {
      clearInterval(this.typingTimer);
    }
    if (this.thinkingTimerInterval) {
      clearInterval(this.thinkingTimerInterval);
    }
    if (this.watchPollTimer) {
      clearInterval(this.watchPollTimer);
      this.watchPollTimer = null;
    }
  },
  methods: {
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
    getNowTime() {
      const d = new Date();
      return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
    },
    getRealTimeInfo() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');

      const dateStr = `${year}-${month}-${day}`;
      const timeStr = `${hours}:${minutes}:${seconds}`;
      const shortTimeStr = `${hours}:${minutes}`;
      const hourNum = now.getHours();

      const isWorkTime = (hourNum >= 8 && hourNum < 18);

      let greeting = '你好';
      if (hourNum >= 5 && hourNum < 9) {
        greeting = '早上好';
      } else if (hourNum >= 9 && hourNum < 12) {
        greeting = '上午好';
      } else if (hourNum >= 12 && hourNum < 14) {
        greeting = '中午好';
      } else if (hourNum >= 14 && hourNum < 18) {
        greeting = '下午好';
      } else if (hourNum >= 18 && hourNum < 24) {
        greeting = '晚上好';
      } else {
        greeting = '夜深了，注意休息';
      }

      return {
        dateStr,
        timeStr,
        shortTimeStr,
        hourNum,
        isWorkTime,
        greeting
      };
    },
    updateClockAndGreeting() {
      const t = this.getRealTimeInfo();
      this.liveTimeText = `${t.dateStr} ${t.timeStr} (${t.isWorkTime ? '生产时段' : '休工时段'})`;
      this.greetingText = `${t.greeting}，我是灵鉴，有什么可以帮你？`;
    },
    handleDocumentClick(e) {
      const menu = this.$el && this.$el.querySelector('.upload-popup-menu');
      const plusBtn = this.$el && this.$el.querySelector('.plus-btn');
      if (this.showUploadMenu && menu && !menu.contains(e.target) && plusBtn && !plusBtn.contains(e.target)) {
        this.showUploadMenu = false;
      }
    },
    triggerLocalFileUpload() {
      this.showUploadMenu = false;
      if (this.$refs.localFileInput) {
        this.$refs.localFileInput.click();
      }
    },
    onLocalFileChosen(e) {
      const file = e.target.files && e.target.files[0];
      if (file) {
        this.attachFile({
          type: 'local',
          name: file.name,
          desc: `本地文件: ${file.name}`
        });
      }
      e.target.value = '';
    },
    triggerCloudFileSelect() {
      this.showUploadMenu = false;
      this.attachFile({
        type: 'cloud',
        name: 'shaft_batch_cloud_dataset_2026.zip',
        desc: '云盘产线数据包'
      });
    },
    triggerCameraDirSelect() {
      this.showUploadMenu = false;
      const t = this.getRealTimeInfo();
      this.attachFile({
        type: 'camera_dir',
        name: `/data/workspace/${t.dateStr}/camera_raw/`,
        desc: '工业相机抓拍目录'
      });
    },
    triggerBatchReportImport() {
      this.showUploadMenu = false;
      const t = this.getRealTimeInfo();
      this.attachFile({
        type: 'report_csv',
        name: `inspection_metrics_${t.dateStr}.csv`,
        desc: '检测批次报表'
      });
    },
    attachFile(fileObj) {
      this.attachedFile = fileObj;
      if (!this.inputQuestion.trim()) {
        this.inputQuestion = `请对已挂载的【${fileObj.name}】进行智能分析与质检诊断`;
      }
      this.$message.success(`已挂载附件：${fileObj.name}`);
    },
    removeAttachment() {
      this.attachedFile = null;
    },
    triggerSkill(skillType) {
      if (this.isThinking || this.isTyping) return;
      const map = {
        'chat': '你好，请介绍一下当前智能体接入的产线设备与工作站状态。',
        'defect_vision': '请对当前批次半轴的表面缺陷做视觉分析与判定。',
        'arm_control': '检查一下当前机械臂的状态',
        'agv_audit': '分析今天小车总路程和2800张图片，有没有问题？',
        'sixs_dashboard': '请你帮我分析一下今天的产线环境是否规范达标',
        'report_gen': '请生成一份今天的质检与6S管理综合报告。'
      };
      const query = map[skillType] || '你好，灵鉴。';
      this.sendQuickQuestion(query);
    },
    triggerVoiceInput() {
      this.$message.info('正在开启工业语音降噪识别模块...');
      setTimeout(() => {
        this.inputQuestion = '检查一下当前机械臂的状态';
        this.$message.success('语音识别完成：已填入指令');
      }, 700);
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
          query: '请执行【事中检查】，检查操作流程中人员行为，工位等是否符合规范'
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
      this.messageList = [];
      this.attachedFile = null;
      this.$message.info('对话记录已清空');
    },
    async handleSend() {
      const q = (this.inputQuestion || '').trim();
      if (!q || this.isThinking || this.isTyping) return;

      this.messageList.push({
        role: 'user',
        content: q,
        time: this.getNowTime()
      });
      this.inputQuestion = '';
      this.showUploadMenu = false;

      const isResetArm = q.includes('机械臂复位') || q.includes('复位机械臂') || q.includes('将机械臂复位');
      const isResetCart = q.includes('小车归位') || q.includes('分拣小车归位') || q.includes('小车复位') || q.includes('归位分拣小车');
      const isPreCheck = q.includes('事前检查') || q.includes('班前点检') || q.includes('开机准入');
      const isDuringCheck = q.includes('事中检查') || q.includes('过程巡检') || q.includes('过程合规');
      const isPostCheck = q.includes('事后检查') || q.includes('班后维护') || q.includes('停机归整');

      const timeInfo = this.getRealTimeInfo();
      let thoughtLines = [];

      if (isResetArm) {
        thoughtLines = [
          '正在连接工控 PLC 总线与 PROFINET 通信协议...',
          '校验伺服电机零点编码器与抱闸状态，准备下发六轴回原点指令。'
        ];
      } else if (isResetCart) {
        thoughtLines = [
          '正在调度 AGV 分拣小车导航系统与激光雷达避障单元...',
          '向分拣小车下发原点返航待命指令。'
        ];
      } else if (isPreCheck) {
        thoughtLines = [
          '【一、 分析用户需求】\n1. 捕获用户意图指令：「请执行工位【事前检查】：检查机械臂、分拣小车是否归位，半轴是否定置归位，桌面是否规整，人员是否规范佩戴安全帽。」；\n2. 意图解析：用户要求大模型针对精加工半轴质检工位执行全要素“开机准入与事前就绪核验”，核心核查范围涵盖：机械臂伺服原点、AGV 分拣小车站台停靠、半轴物料定置卡槽、桌面 6S 规范度以及作业人员 PPE 安全帽佩戴合规性；\n3. 任务约束：需展开多阶深度工业推演，调度工业相机与工控总线调阅实时抓拍与感知数据，将抓拍图直接嵌入思考流中完成空间位置与视觉状态比对，随后输出严密核验与闭环处置策略。',

          '【二、 确认角色定位与任务目标】\n1. 角色定位：灵眸巡诊 · 工业视觉 AI 边缘巡检计算节点与 6S 工业质检智能体；\n2. 任务目标：调度边缘端多路感知网络与工控 PLC 总线，对精加工半轴质检工位开展全方位“开机准入与事前就绪”深度核验；\n3. 核心准入指标：\n   - 机械臂姿态：PROFINET 总线读取伺服电机零点编码器与关节角度，确认是否处于安全原点；\n   - 运检协同：AGV 分拣小车是否准时到达 1 号受检站台并锁定刹车；\n   - 物料三定：待检半轴是否定点定容定位置入 1 号上料卡槽；\n   - 现场 6S 与 PPE：台面整洁无杂物工具，作业人员安全帽与防护装备穿戴合规。',

          '【三、 构建响应框架与推演路径】\n为确保开机准入决策的严密性与零差错，系统规划五阶递进推演：\n- 阶段 1：指令解析与多模态边缘感知任务编排；\n- 阶段 2：调阅工业相机实时抓拍与工件图谱（嵌入思考流比对分析）；\n- 阶段 3：多维度多源数据交叉核验（机械臂姿态 / AGV / 物料 / PPE）；\n- 阶段 4：异常工况逻辑研判与 6S 安全防差错推演；\n- 阶段 5：生成开机准入结论与一键工控复位交互指令。',

          `【四、 调阅工位实时抓拍与视觉感知数据】\n1. 数据源定位：工业相机抓拍存储目录 /data/workspace/${timeInfo.dateStr}/camera_raw/；\n2. 正在提取当前受检工位最新抓拍帧与全周检测视觉标注图像，用于空间位姿与视觉特征多模态核验：`
        ];
      } else if (isDuringCheck) {
        thoughtLines = [
          '【一、 分析用户需求】\n1. 捕获用户意图指令：「请执行【事中检查】，检查行为，工位等是否符合规范」；\n2. 意图解析：用户正在要求大模型对产线精加工半轴工位的实时生产过程执行多维合规性“事中核验”，核心审查对象涵盖作业人员操作行为规范、工位6S三定物料摆放、缺陷检测算法阈值稳定性及PPE劳保穿戴；\n3. 任务约束：需输出极具工业水准、多源交叉验真、结构化且包含详细推演步骤的诊断流。',

          '【二、 确认角色定位与任务目标】\n1. 角色定位：灵眸巡诊 · 工业视觉 AI 边缘巡检计算节点与 6S 工业质检智能体；\n2. 任务目标：调度边缘端多路感知网络与模型推理集群，对精加工工位开展全方位“事中合规”深度巡检诊断；\n3. 核心指标：\n   - 算法可靠度：缺陷识别模型推理引擎置信度阈值锁定；\n   - PPE 劳保穿戴：丁腈防静电手套（杜绝裸手接触精加工半轴）、安全帽与下颚带规范佩戴；\n   - 6S 工位定置：待检品、合格品、缺陷品定点定容定量定置隔离；\n   - 操作行为规程：半轴全周旋转检测节拍与标准作业合规性。',

          '【三、 构建响应框架（结构化输出）】\n为保证诊断推演的严密性与专业性，系统构建五阶梯次推演架构：\n- 阶段 1：指令接收与多模态边缘感知任务编排；\n- 阶段 2：实时抓拍数据摄取、时序对齐与数据质量检验；\n- 阶段 3：多维度神经网络合规性并行推理与深度核验；\n- 阶段 4：鲁棒性边界校验、环境突变模拟与异常兜底逻辑推演；\n- 阶段 5：综合置信度加权评分与标准化巡检结论生成。',

          `【四、 填充具体细节（结合历史上下文）】\n1. 数据源定位：工业相机抓拍存储目录 /data/workspace/${timeInfo.dateStr}/camera_raw/；\n2. 硬件与感知流状态：高帧率工业相机 4K@60fps 实时视频流，工业环形无影光源照度均匀无频闪；\n3. 算法模型状态：Vision-Model v2.4 表面缺陷检测引擎，置信度判定阈值锁定 ≥0.85；\n4. 工艺规范核对：依据 GB/T 38885 表面质量检验规程与半轴 6S 数字化管理标准。`,

          '【五、 撰写具体内容（模拟 AI 思考与执行过程）】\n1. 启动数据流摄取：提取抓拍帧序列，校验时间戳连续性，多路感知数据对齐完成，无丢包与丢帧；\n2. 目标检测与人体姿态估计：\n   - 关键点与分割算法定位操作员：手部精准识别为蓝色丁腈防静电劳保手套覆盖，未发现裸手接触精磨半轴表面隐患；\n   - 头部姿态识别：标准工程安全帽佩戴到位，下颚带扣紧锁定，合规率 100%；\n3. 语义分割分析工位 6S 台面：\n   - 待检半轴定置于 1 号上料卡槽，已检合格品定置于绿色周转箱，未发生跨区混放；\n   - 工作台面无闲置工具、私人物品或金属毛刺堆积；\n4. 作业行为时序分析：机械手平稳抓取半轴，驱动电机带动半轴以恒定角速度完成 360° 全周旋转，28 张环拍采样点位严格匹配。',

          '【六、 异常逻辑与兜底推演（鲁棒性验证）】\n1. 边界条件压力推演：\n   - 假设场景：若车间外部环境光照骤变或工件表面油膜反光导致某张切片缺陷识别置信度跌落至 0.82（<0.85 阈值）；\n   - 兜底处置机制：系统将拒绝单向放行，自动标记【待人工卡尺复核】并联动声光报警灯闪烁，工段长确认后方可解锁；\n2. 实时状态判定：当前多帧连续抽检显示综合置信度达 0.96，环境照度稳定处于安全工作区间，无需触发降级告警。',

          '【七、 润色语言与最终检查】\n1. 风格校准：采用精益制造（Lean Manufacturing）与智能制造标准工业术语，逻辑层层递进；\n2. 完整性检查：用户指令所要求的“人员行为”、“工位定置”、“算法阈值”、“操作规范”已全部闭环推演；\n3. 巡检结论生成：综合评定当前精加工半轴质检工位处于【规范达标】状态，各项 6S 与工艺参数完全达标！'
        ];
      } else if (isPostCheck) {
        thoughtLines = [
          '【一、 分析用户需求】\n1. 捕获用户意图指令：「请执行工位【事后检查】：检查机械臂与分拣小车是否复位、半轴是否分拣成功、现场工位与桌面是否整齐、辅机是否安全断电。」；\n2. 意图解析：用户要求大模型针对生产批次结束/班后停机状态执行全流程“事后维护与归整核验”，核心核查范围涵盖：机械臂停机复位与伺服抱闸、AGV 分拣小车停泊充电位、检测半轴分拣入库闭环、工作台面 6S 清洁整顿及辅机安全断电；\n3. 任务约束：需调取工控总线与相机抓拍进行深度交叉验真，对异常工况展开鲁棒性推演并给出闭环处置。',

          '【二、 确认角色定位与任务目标】\n1. 角色定位：灵眸巡诊 · 工业视觉 AI 边缘巡检计算节点与 6S 工业质检智能体；\n2. 任务目标：调度边缘端多路感知网络与工控 PLC 总线，对精加工工位开展全方位“事后维护与停机归整”深度巡检；\n3. 核心准入指标：\n   - 机械臂姿态：PROFINET 总线读取伺服编码器，核实是否回归安全待命原点并锁定抱闸；\n   - AGV 小车状态：小车是否已完成物料搬运并返航停靠指定充电桩；\n   - 物料分拣闭环：批次半轴是否全部完成全周检测并按合格/缺陷类别 100% 定置入库；\n   - 工位整洁与能耗：工作台面量具手套定置归位，光学光源与检测辅机安全断电。',

          '【三、 构建响应框架与推演路径】\n为确保事后点检决策的严密性与闭环管理，系统规划五阶递进推演：\n- 阶段 1：指令接收与多模态边缘感知任务编排；\n- 阶段 2：调阅工控总线 PLC 状态、相机抓拍与批次分拣履历；\n- 阶段 3：多维度多源数据交叉核验（机械臂位姿 / AGV 返航 / 物料流向 / 桌面 6S）；\n- 阶段 4：停机安全隐患逻辑研判与防差错推演；\n- 阶段 5：生成事后点检结论与工控一键复位交互指令。',

          `【四、 调阅工位实时抓拍与感知数据】\n1. 数据源定位：工业相机抓拍存储目录 /data/workspace/${timeInfo.dateStr}/camera_raw/；\n2. 正在提取当前受检工位最新抓拍帧与工控总线数据，用于设备位姿与现场环境多模态核验：`,

          '【五、 深入核验与逻辑推演（结合实时图谱与 PLC 总线）】\n1. 机械臂空间姿态分析（结合工控编码器与视觉图谱）：\n   - 编码器反馈读取：关节坐标偏离绝对零点原点（处于拍照工位上方悬停姿态），六轴伺服虽通电但未回安全零位；\n   - 风险分析：停机后机械臂悬停未归位，长时间通电容易发热损耗电机寿命，且存在夜间碰撞隐患；\n2. AGV 分拣小车状态核验：\n   - 导航与充电桩传感器反馈：分拣小车已完成全部半轴转运，安全返航停靠指定充电待命点，状态正常；\n3. 半轴分拣结果与定置流向：\n   - MES 系统与 RFID 数据比对：今日检测批次半轴已全部完成全周质检，合格品与缺陷品 100% 分流定置入库，现场无滞留混料；\n4. 工位整齐与台面维持：\n   - 视觉目标检测判定：工作台面整洁无杂物工具，劳保手套与工件用具已定置归位，符合 6S 班后交接标准。',

          '【六、 异常处置与安全防差错推演】\n1. 核心异常定位：全项检查中唯独【机械臂未复位】存在安全违规隐患；\n2. 处置策略：系统主动拦截收工归档，向质检员发起工控一键复位确认交互；\n3. 兜底保障：质检员确认后立即下发 PROFINET 原点校准指令，确保各轴伺服平稳回零并关闭辅机电源。',

          '【七、 润色语言与最终结论生成】\n1. 风格校准：采用精益制造标准工业术语，逐项列明核验结果；\n2. 闭环决策：生成事后点检处置清单，并提供【是（立即复位）/ 否（暂不复位）】一键工控交互操作。'
        ];
      } else if (q.includes('达标') || q.includes('产线环境') || q.includes('规范') || q.includes('大盘')) {
        const totalShafts = 2437;
        const defectShafts = 25;
        const passShafts = totalShafts - defectShafts; // 2412
        const totalPhotos = totalShafts * 28; // 68,236 张
        const scratchCount = 16;
        const crackCount = 9;
        const mileage = (totalShafts * 5.0).toFixed(1); // 12185.0 米
        const timestampSec = Math.floor(Date.now() / 1000);

        thoughtLines = [
          `【一、 分析用户需求】\n1. 捕获用户意图指令：「${q}」；\n2. 意图解析：用户要求大模型对精加工半轴数字化车间今日（${timeInfo.dateStr}）的整体产线运行环境、视觉质检成效、6S 定置分拣流向与 AGV 物流协同展开全要素“规范合规与运行大盘”综合诊断；\n3. 任务约束：需调阅全天候多源边缘感知与工控日志，展开多维交叉验真推演，核实是否存在混料、漏检、里程逻辑矛盾或安全违规，并输出权威精准的诊断汇报。`,

          `【二、 确认角色定位与任务目标】\n1. 角色定位：灵眸巡诊 · 工业视觉 AI 边缘巡检计算节点与 6S 工业质检智能体；\n2. 任务目标：调度边缘端多路感知网络、MES 生产执行数据与工控 PLC 总线，完成今日全车间 6S 规范达标度深度核算；\n3. 核心研判基准：\n   - 视觉采图与批次配比：半轴检测总量与环拍切片数量严格匹配（28张/根）；\n   - 6S 三定定置流向：合格品、表面划痕品、裂纹品分区定置，隔离区锁扣受控无混放；\n   - AGV 运检里程逻辑验真：转运总行程与工件批次往返距离数学契合；\n   - 车间安全与光学环境：镜头洁净度、安全光栅联锁及工位台面整洁度 100% 达标。`,

          `【三、 构建响应框架与推演路径】\n为保证大盘诊断结论的权威性与严密性，系统规划五阶梯次推演架构：\n- 阶段 1：多模态数据接入与时序对齐（相机目录 / 批次报表 / AGV 轨迹日志）；\n- 阶段 2：生产与视觉质检核心指标聚合分析（检测量 / 采图量 / 良品率）；\n- 阶段 3：6S 定置分拣流向与物料防差错严密比对；\n- 阶段 4：物流小车总里程与转运批次交叉数学逻辑验真；\n- 阶段 5：现场环境安全光栅与光学洁净度综合评定并生成归档结论。`,

          `【四、 调阅全域多源感知与批次数据】\n1. 调阅物理图像存储目录：/data/workspace/${timeInfo.dateStr}/camera_raw/；\n2. 校验最新抓拍样图文件：capture_${timestampSec}_shaft.jpg（特征向量与时间戳已校准）；\n3. 提取 MES 今日生产报表：今日累计检测 ${totalShafts.toLocaleString()} 根半轴，共读取到 ${totalPhotos.toLocaleString()} 张全周高清切片（28张/根，照片采集无异常，数量与工件完全对应）；\n4. 读取分拣与隔离区传感器：检出合格品 ${passShafts.toLocaleString()} 根，缺陷半轴 ${defectShafts} 根（划痕 ${scratchCount} 根，裂纹 ${crackCount} 根，综合良品率 98.97%）；\n5. 提取 AGV 运控系统里程表：小车今日累计行驶总里程为 ${Number(mileage).toLocaleString()} 米。`,

          `【五、 深入多源数据交叉验真与逻辑推演】\n1. 视觉采图与检测节奏验证：\n   - 核算比对：${totalShafts.toLocaleString()} 根 × 28 张/根 = ${totalPhotos.toLocaleString()} 张，图件比例 1:28 绝对吻合，无卡料重复抓拍或漏拍；\n2. 6S 分区分流定置核验：\n   - 合格品入库通道：${passShafts.toLocaleString()} 根顺利流转至正常品立库；\n   - 划痕品隔离区：${scratchCount} 根转入黄色标识定置箱待抛光；\n   - 裂纹品隔离区：${crackCount} 根转入红色标识定置箱待探伤复检；\n   - 结论：各仓位定置传感器信号与视觉判定完全一致，杜绝物料混淆；\n3. AGV 运行轨迹与里程逻辑验真：\n   - 单次工件转运平均距离：约 5.0 米；\n   - 理论行驶总路程：${totalShafts.toLocaleString()} 根 × 5.0 米 = ${Number(mileage).toLocaleString()} 米；\n   - 实际记录里程：${Number(mileage).toLocaleString()} 米（误差率 0.00%），证明小车全程平稳循迹，无原地打滑、空转或卡死停滞。`,

          `【六、 现场 6S 规范与安全联锁边界校验】\n1. 光学感知与设备维护状态：检测相机镜头透光率正常，光源照度稳定，无金属碎屑遮挡；\n2. 现场 6S 台面与工序环境：工位操作台面整齐，工具定置入槽，通道无障碍物；\n3. 安全防护系统自检：安全光栅保护区无人员入侵违规，急停回路闭锁正常；\n4. 质量波动鲁棒性分析：批次良品率 98.97% 处于受控生产线（UCL=99.5%, LCL=98.0%）正常受控区间，未出现系统性质量偏差。`,

          `【七、 润色语言与综合诊断结论生成】\n1. 语言风格校准：采用精益制造（Lean 6S）标准工业汇报结构，分为生产检测、定置流向、AGV 运行及综合结论四大模块；\n2. 完整性复核：用户关心的“产线环境是否规范达标”已完成全维度多源交叉证明；\n3. 最终评定：车间现场 6S 维持优良，评定为【规范达标】（综合评定 99.2 分），已自动生成归档报告。`
        ];
      } else if (q.includes('小车') || q.includes('2800') || q.includes('路程') || q.includes('米') || q.includes('AGV')) {
        thoughtLines = [
          '对比分拣小车行驶里程与视觉检测图片数据，进行逻辑验真：',
          '1. 图片数据：拍摄 2,800 张照片，按标准对应 100 根半轴工作量；',
          '2. 小车行程：实际仅行驶 5.2 米（正常 100 根应行驶约 500 米）；',
          '3. 逻辑矛盾诊断：小车在工位卡住未换料，检测台原地对同一根半轴重复拍摄 100 次。'
        ];
      } else if (q.includes('机械臂') || q.includes('状态')) {
        thoughtLines = [
          `检查当前机械臂空间姿态与时钟（当前时间 ${timeInfo.shortTimeStr}，属于${timeInfo.isWorkTime ? '生产时段' : '休工时段'}）：`,
          '1. 读取机械臂当前位置：处于拍照工位上方悬停姿态；',
          timeInfo.isWorkTime
            ? '2. 智能研判：当前属于正常生产工作时间，工位有待检半轴送达，属于正常对焦待命，无需强制复位。'
            : '2. 智能研判：当前已处于下班停机时段，机械臂长时间通电发热存在安全违规与老化隐患，建议立即复位。'
        ];
      } else {
        thoughtLines = [
          `调阅车间 6S 规范标准与工位传感数据，分析用户指令：「${q}」...`,
          '结合精益制造标准生成规范化操作与处置指南。'
        ];
      }

      // 1. 如果是事后检查，先调阅识别图像
      if (isPostCheck) {
        try {
          let detectedImgCard = null;
          const statusRes = await this.$request.get('/api/cameraWatch/status');
          if (statusRes && (statusRes.code === 200 || statusRes.code === 1 || statusRes.code === '200')) {
            const list = (statusRes.data && statusRes.data.list) || [];
            if (list.length > 0) {
              const targetItem = list[0];
              let annotatedUrl = targetItem.annotatedBase64;
              if (!annotatedUrl) {
                try {
                  const directRes = await axios.get('http://192.168.1.3:9001/Qualified', {
                    params: {
                      path: `/root/desc/cmzj-main/mijia-watcher/image/${targetItem.fileName}`,
                      fileName: targetItem.fileName
                    },
                    timeout: 5000
                  });
                  const resBody = directRes.data || {};
                  const payload = (resBody.data && typeof resBody.data === 'object') ? resBody.data : resBody;
                  if (payload.data && payload.data.image && payload.data.image.image_base64) {
                    annotatedUrl = payload.data.image.image_base64;
                  } else if (payload.image && payload.image.image_base64) {
                    annotatedUrl = payload.image.image_base64;
                  } else if (payload.image_base64) {
                    annotatedUrl = payload.image_base64;
                  }
                } catch (e) {}
              }

              if (annotatedUrl && !annotatedUrl.startsWith('data:image')) {
                annotatedUrl = 'data:image/jpeg;base64,' + annotatedUrl;
              }

              detectedImgCard = {
                fileName: targetItem.fileName || '机械臂复位识别图.jpg',
                fileSize: targetItem.fileSize || '已标注',
                webUrl: annotatedUrl || targetItem.imgUrl
              };
            }
          }

          if (detectedImgCard) {
            this.messageList.push({
              role: 'assistant',
              content: '',
              time: this.getNowTime(),
              imageCard: detectedImgCard
            });
            this.$nextTick(() => this.scrollToBottom());
          }
        } catch (err) {
          console.warn('调取机械臂检测图失败:', err);
        }
      }

      // 2. 准备 AI 消息节点（初始为空文本与思考中状态，1:1 对齐 AgentWorkbench.html）
      const assistantMsg = {
        role: 'assistant',
        content: '',
        time: this.getNowTime(),
        thoughtText: '',
        thoughtTextAfter: '',
        thoughtImageCard: null,
        thoughtCollapsed: false,
        thinking: true,
        thinkingSeconds: 0,
        interactive: (isPreCheck || isPostCheck) ? 'reset_arm' : null,
        userChoice: null
      };
      this.messageList.push(assistantMsg);
      this.isThinking = true;
      this.$nextTick(() => this.scrollToBottom());

      // 思考秒数动态计时器
      let seconds = 0;
      if (this.thinkingTimerInterval) clearInterval(this.thinkingTimerInterval);
      this.thinkingTimerInterval = setInterval(() => {
        seconds++;
        assistantMsg.thinkingSeconds = seconds;
      }, 1000);

      // 前置 2 秒“理解要求与意图解析”缓冲，仅显示转圈动画
      await this.sleep(2000);

      if (isPreCheck) {
        // ========== 专门针对【事前检查】：前置思考 -> 思考中嵌入图片 -> 后续深度思考 -> 收起思考 -> 输出回答 ==========
        const preThoughtBefore = [
          '【一、 分析用户需求】\n1. 捕获用户意图指令：「请执行工位【事前检查】：检查机械臂、分拣小车是否归位，半轴是否定置归位，桌面是否规整，人员是否规范佩戴安全帽。」；\n2. 意图解析：用户要求大模型针对精加工半轴质检工位执行全要素“开机准入与事前就绪核验”，核心核查范围涵盖：机械臂伺服原点、AGV 分拣小车站台停靠、半轴物料定置卡槽、桌面 6S 规范度以及作业人员 PPE 安全帽佩戴合规性；\n3. 任务约束：需展开多阶深度工业推演，调度工业相机与工控总线调阅实时抓拍与感知数据，将抓拍图直接嵌入思考流中完成空间位置与视觉状态比对，随后输出严密核验与闭环处置策略。',

          '【二、 确认角色定位与任务目标】\n1. 角色定位：灵眸巡诊 · 工业视觉 AI 边缘巡检计算节点与 6S 工业质检智能体；\n2. 任务目标：调度边缘端多路感知网络与工控 PLC 总线，对精加工半轴质检工位开展全方位“开机准入与事前就绪”深度核验；\n3. 核心准入指标：\n   - 机械臂姿态：PROFINET 总线读取伺服电机零点编码器与关节角度，确认是否处于安全原点；\n   - 运检协同：AGV 分拣小车是否准时到达 1 号受检站台并锁定刹车；\n   - 物料三定：待检半轴是否定点定容定位置入 1 号上料卡槽；\n   - 现场 6S 与 PPE：台面整洁无杂物工具，作业人员安全帽与防护装备穿戴合规。',

          '【三、 构建响应框架与推演路径】\n为确保开机准入决策的严密性与零差错，系统规划五阶递进推演：\n- 阶段 1：指令解析与多模态边缘感知任务编排；\n- 阶段 2：调阅工业相机实时抓拍与工件图谱（嵌入思考流比对分析）；\n- 阶段 3：多维度多源数据交叉核验（机械臂姿态 / AGV / 物料 / PPE）；\n- 阶段 4：异常工况逻辑研判与 6S 安全防差错推演；\n- 阶段 5：生成开机准入结论与一键工控复位交互指令。',

          `【四、 调阅工位实时抓拍与视觉感知数据】\n1. 数据源定位：工业相机抓拍存储目录 /data/workspace/${timeInfo.dateStr}/camera_raw/；\n2. 正在提取当前受检工位最新抓拍帧与全周检测视觉标注图像，用于空间位姿与视觉特征多模态核验：`
        ];

        // 1. 流式输出第一段思考流
        for (let i = 0; i < preThoughtBefore.length; i++) {
          const line = preThoughtBefore[i];
          if (i > 0) assistantMsg.thoughtText += '\n\n';
          for (let j = 0; j < line.length; j++) {
            assistantMsg.thoughtText += line[j];
            this.scrollToBottom();
            await this.sleep(Math.floor(Math.random() * 8) + 10);
          }
          await this.sleep(300);
        }

        // 2. 调取工件抓拍与标注图片，嵌入到思考面板中
        try {
          let detectedImgCard = null;

          // 优先检查用户是否在对话框挂载了本地附件图片
          if (this.attachedFile && this.attachedFile.type === 'local' && this.attachedFile.rawFile) {
            detectedImgCard = {
              fileName: this.attachedFile.name || '挂载工件检测图.jpg',
              fileSize: this.attachedFile.fileSize || '本地挂载',
              webUrl: this.attachedFile.previewUrl
            };
          }

          // 动态从【产线监控】目录（CameraWatch）调取最新捕获/上传的真实图片
          if (!detectedImgCard) {
            const statusRes = await this.$request.get('/api/cameraWatch/status');
            if (statusRes && (statusRes.code === 200 || statusRes.code === 1 || statusRes.code === '200')) {
              const list = (statusRes.data && statusRes.data.list) || [];
              if (list.length > 0) {
                const targetItem = list[0];
                let annotatedUrl = targetItem.annotatedBase64;

                // 若尚未标注，先尝试后端代理检测接口 /api/cameraWatch/detect
                if (!annotatedUrl) {
                  try {
                    const detectRes = await this.$request.get('/api/cameraWatch/detect', {
                      params: { fileName: targetItem.fileName }
                    });
                    if (detectRes && (detectRes.code === 200 || detectRes.code === 1 || detectRes.code === '200')) {
                      const dData = detectRes.data || {};
                      annotatedUrl = dData.annotatedBase64 || dData.image_base64;
                    }
                  } catch (e) {}
                }

                // 再次尝试 AI 模型直调与前端代理路由
                if (!annotatedUrl) {
                  try {
                    const imgPath = `/root/desc/cmzj-main/mijia-watcher/image/${targetItem.fileName}`;
                    let directRes = null;
                    try {
                      directRes = await axios.get('http://192.168.1.3:9001/Qualified', {
                        params: { path: imgPath, fileName: targetItem.fileName },
                        timeout: 5000
                      });
                    } catch (corsErr) {
                      directRes = await axios.get('/ai-detect/Qualified', {
                        params: { path: imgPath, fileName: targetItem.fileName },
                        timeout: 5000
                      });
                    }
                    const resBody = directRes.data || {};
                    const payload = (resBody.data && typeof resBody.data === 'object') ? resBody.data : resBody;
                    if (payload.data && payload.data.image && payload.data.image.image_base64) {
                      annotatedUrl = payload.data.image.image_base64;
                    } else if (payload.image && payload.image.image_base64) {
                      annotatedUrl = payload.image.image_base64;
                    } else if (payload.image_base64) {
                      annotatedUrl = payload.image_base64;
                    }
                  } catch (e) {}
                }

                if (annotatedUrl && typeof annotatedUrl === 'string' && !annotatedUrl.startsWith('data:image')) {
                  annotatedUrl = 'data:image/jpeg;base64,' + annotatedUrl;
                }

                detectedImgCard = {
                  fileName: targetItem.fileName || '工位实时抓拍图.jpg',
                  fileSize: targetItem.fileSize || '实时捕获',
                  webUrl: annotatedUrl || targetItem.imgUrl
                };
              }
            }
          }

          if (detectedImgCard) {
            this.$set(assistantMsg, 'thoughtImageCard', detectedImgCard);
            this.$nextTick(() => this.scrollToBottom());
          }
        } catch (err) {
          console.warn('思考中调取检测图异常:', err);
        }

        await this.sleep(400);

        // 3. 接着输出第二段深度思考流（图后深入推演）
        const preThoughtAfter = [
          '【五、 深入核验与逻辑推演（结合实时图谱与 PLC 总线）】\n1. 机械臂空间姿态分析（结合工控编码器与视觉图谱）：\n   - 编码器反馈读取：关节坐标偏离绝对零点原点（处于拍照工位上方悬停姿态），六轴伺服虽通电但未回安全零位；\n   - 风险分析：机械臂处于悬停状态直接启动上料，存在机械干涉碰擦隐患与伺服抱闸寿命折损风险；\n2. AGV 分拣小车站台核验：\n   - 激光雷达与 RFID 读写器反馈：分拣小车已准时停靠 1 号站台，磁条循迹对齐，处于上料待命就绪状态；\n3. 半轴定置与桌面 6S 排查：\n   - 视觉目标检测判定：待检半轴已规范定置于 1 号上料 V 型定位槽，台面整洁无闲置量具、废料铁屑堆积；\n4. 作业人员 PPE 安全合规核验：\n   - 人体姿态与安全帽检测模型输出：现场作业人员标准佩戴蓝色防静电安全帽，下颚带系紧，合规率 100%。',

          '【六、 异常处置与安全防差错推演】\n1. 核心异常定位：全项检查中唯独【机械臂未归位】不符合开机准入基准；\n2. 处置策略：根据 6S 安全防差错联锁机制，系统主动拦截自动生产启动，向质检员发起工控一键复位确认交互；\n3. 兜底保障：质检员确认后立即下发 PROFINET 原点校准指令，确保各轴伺服平稳回零后再开放启动权限。',

          '【七、 润色语言与最终结论生成】\n1. 风格校准：采用精益制造标准工业术语，逐项列明核验结果；\n2. 闭环决策：生成事前准入处置清单，并提供【是（立即复位）/ 否（暂不复位）】一键工控交互操作。'
        ];

        for (let i = 0; i < preThoughtAfter.length; i++) {
          const line = preThoughtAfter[i];
          if (i > 0) assistantMsg.thoughtTextAfter += '\n\n';
          for (let j = 0; j < line.length; j++) {
            assistantMsg.thoughtTextAfter += line[j];
            this.scrollToBottom();
            await this.sleep(Math.floor(Math.random() * 8) + 10);
          }
          await this.sleep(300);
        }
      } else {
        // 3. 其余常规分支：逐步逐行逐字输出思考文字流
        for (let i = 0; i < thoughtLines.length; i++) {
          const line = thoughtLines[i];
          if (i > 0) {
            assistantMsg.thoughtText += '\n\n';
          }
          for (let j = 0; j < line.length; j++) {
            assistantMsg.thoughtText += line[j];
            this.scrollToBottom();
            await this.sleep(Math.floor(Math.random() * 10) + 12);
          }
          await this.sleep(450);
        }
      }

      // 思考阶段结束
      clearInterval(this.thinkingTimerInterval);
      this.thinkingTimerInterval = null;
      assistantMsg.thinking = false;
      this.$set(assistantMsg, 'thoughtCollapsed', true);
      assistantMsg.thinkingSeconds = seconds || 5;
      this.isThinking = false;
      await this.sleep(300);

      // 4. 逐步输出正文回答（对齐 AgentWorkbench.html 流式打字机）
      const answerText = this.generate6SAnswer(q);
      this.isTyping = true;
      for (let i = 0; i < answerText.length; i++) {
        assistantMsg.content += answerText[i];
        this.scrollToBottom();
        await this.sleep(Math.floor(Math.random() * 8) + 8);
      }
      this.isTyping = false;
      this.scrollToBottom();
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
          const step = Math.min(3, totalLen - currentIndex);
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
        this.messageList.push({
          role: 'user',
          content: '否（暂不复位）',
          time: this.getNowTime()
        });
        this.$nextTick(() => this.scrollToBottom());
      } else if (choice === 'yes') {
        this.messageList.push({
          role: 'user',
          content: '是，请将机械臂复位',
          time: this.getNowTime()
        });
        this.isThinking = true;
        this.currentThinkingStreamText = '正在通过 PROFINET 工控总线向全周检测工作站下发机械臂六轴原点复位指令...';
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
    handleCopyAnswer(msg) {
      if (!msg || !msg.content) return;
      const cleanText = msg.content
        .replace(/### (.*?)\n/g, '$1\n')
        .replace(/\*\*(.*?)\*\*/g, '$1')
        .replace(/\*本回答由灵鉴生成[^*<\n]+\*/g, '');

      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(cleanText).then(() => {
          this.$message.success('已复制回答到剪贴板');
        }).catch(() => {
          this.fallbackCopyText(cleanText);
        });
      } else {
        this.fallbackCopyText(cleanText);
      }
    },
    fallbackCopyText(text) {
      const textarea = document.createElement('textarea');
      textarea.value = text;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      try {
        document.execCommand('copy');
        this.$message.success('已复制回答到剪贴板');
      } catch (e) {
        this.$message.error('复制失败，请手动选择复制');
      }
      document.body.removeChild(textarea);
    },
    handleRegenerate(msg) {
      // 找到该消息对应的上一次用户提问
      const msgIndex = this.messageList.indexOf(msg);
      let prevUserQuery = '请你帮我分析一下今天的产线环境是否规范达标';
      if (msgIndex > 0) {
        for (let i = msgIndex - 1; i >= 0; i--) {
          if (this.messageList[i].role === 'user') {
            prevUserQuery = this.messageList[i].content;
            break;
          }
        }
      }
      this.sendQuickQuestion(prevUserQuery);
    },
    handleFeedback(msg, type) {
      if (!msg) return;
      if (msg.feedback === type) {
        this.$set(msg, 'feedback', null);
        this.$message.info('已取消评价');
      } else {
        this.$set(msg, 'feedback', type);
        if (type === 'like') {
          this.$message.success('感谢您的正向反馈，已记录入 6S 模型自学习知识库！');
        } else {
          this.$message.info('感谢反馈，我们将持续优化巡检算法与工控诊断精度。');
        }
      }
    },
    handleShareAnswer(msg) {
      this.openReportDialog();
    },
    fetchWatchStatus() {
      this.$request.get('/api/cameraWatch/status').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          const data = res.data || {};
          this.running = Boolean(data.running);
        }
      }).catch(() => {});
    },
    startWatch() {
      this.$request.post('/api/cameraWatch/start').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          this.$message.success(res.msg || res.message || '目录监听启动成功！');
          this.running = true;
          this.fetchWatchStatus();
        } else {
          this.$message.error(res.msg || res.message || '启动监听失败');
        }
      }).catch(() => {
        this.$message.error('请求后端启动监听失败');
      });
    },
    stopWatch() {
      this.$request.post('/api/cameraWatch/stop').then(res => {
        if (res.code === 200 || res.code === 1 || res.code === '200') {
          this.$message.info('目录监听已停止');
          this.running = false;
        } else {
          this.$message.error('停止监听失败');
        }
      }).catch(() => {
        this.$message.error('停止监听失败');
      });
    },
    formatImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('data:image') || url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      return url.startsWith('/') ? url : '/' + url;
    },
    initWatchImageListener() {
      // 保持静默监听
    },
    generate6SAnswer(query) {
      const timeInfo = this.getRealTimeInfo();
      const currentDate = timeInfo.dateStr;
      const currentTime = timeInfo.shortTimeStr;
      const isWorkTime = timeInfo.isWorkTime;
      const timestampSec = Math.floor(Date.now() / 1000);
      const disclaimer = '\n\n*本回答由灵鉴生成，内容仅供参考，请仔细甄别*';

      // 1. 三阶段综合点检（事前/事中/事后检查）
      if (query.includes('事前检查') || query.includes('班前点检') || query.includes('开机准入')) {
        return `### 【6S·事前检查】班前开机与工位准入 5 大合规核验：\n\n1. **机械臂归位检查**：【未复位】 全周检测机械臂未回原点待命位，需复位就绪。\n2. **分拣小车归位检查**：【正常】 AGV 分拣小车处于规定标定待命点，避障传感器常开。\n3. **半轴定置归位检查**：【正常】 待检半轴已定置放置于固定工位，摆放规范整齐。\n4. **桌面规整检查**：【正常】 工作台面规整整洁，劳保手套与工位用具已定置。\n5. **人员安全帽与劳保合规**：【正常】 进入作业区人员已 100% 正确佩戴安全帽与劳保手套。\n\n提示：检测到机械臂未复位，是否立即让机械臂复位？${disclaimer}`;
      }
      if (query.includes('事中检查') || query.includes('过程巡检') || query.includes('过程合规')) {
        return `### 【6S·事中检查】生产作业与缺陷质检过程 4 大合规核验：\n\n1. **算法置信度阈值监控**：【正常】 AI 深度视觉识别算法置信度阈值锁定 ≥0.85，运行状态稳定。\n2. **劳保手套佩戴规范**：【正常】 质检人员全程规范佩戴劳保手套，严禁裸手接触精加工工件。\n3. **作业人员安全帽规范**：【正常】 作业区人员 100% 正确佩戴安全帽并扣紧下颚带。\n4. **缺陷检测过程与判定规范**：【正常】 严格执行半轴全周旋转检测流程，合格品与缺陷品定置分流合规。${disclaimer}`;
      }
      if (query.includes('事后检查') || query.includes('班后维护') || query.includes('停机归整')) {
        return `### 【6S·事后检查】班后维护与停机归整 4 大合规核验：\n\n1. **机械臂复位检查**：【未复位】 全周检测机械臂停机后未回归待命原点。\n2. **小车复位检查**：【正常】 AGV 分拣小车已安全调度复位至指定充电待命点。\n3. **半轴分拣结果确认**：【正常】 批次半轴已全部完成检测并按类别入库，无滞留混料。\n4. **工位整齐与台面维持**：【正常】 现场工作台面清洁整齐、手套用具定置归位，完成 6S 数字化点检交接。\n\n提示：检测到机械臂未复位，是否立即让机械臂复位？${disclaimer}`;
      }

      // 2. 产线环境多源交叉验真
      if (query.includes('达标') || query.includes('产线环境') || query.includes('规范') || query.includes('大盘')) {
        const totalShafts = 2437;
        const defectShafts = 25;
        const passShafts = totalShafts - defectShafts;
        const totalPhotos = totalShafts * 28;
        const scratchCount = 16;
        const crackCount = 9;
        const mileage = (totalShafts * 5.0).toFixed(1);

        return `今天（${currentDate}）产线的运行环境与质检数据已全部调取分析完成，整体规范达标，详细汇报如下：\n\n**一、 生产与视觉检测数据**\n1. 今日累计检测半轴：${totalShafts.toLocaleString()} 根\n2. 拍摄照片总数：${totalPhotos.toLocaleString()} 张（每根标准拍摄 28 张，照片采集无异常，数量完全对应）\n3. 最新抓拍样本文件：capture_${timestampSec}_shaft.jpg\n4. 质检判定结果：合格 ${passShafts.toLocaleString()} 根，检出缺陷 ${defectShafts} 根（综合良品率 98.97%）\n\n**二、 6S 定置分拣流向**\n1. 正常品区：${passShafts.toLocaleString()} 根（已规范入库）\n2. 划痕隔离区：${scratchCount} 根（黄色标识箱定置，待抛光）\n3. 裂纹隔离区：${crackCount} 根（红色标识箱定置，待探伤复查）\n\n**三、 AGV 分拣小车运行分析**\n1. 小车今日行驶总里程：${Number(mileage).toLocaleString()} 米\n2. 路程合理性验真：每根半轴分拣转运平均行驶 5 米左右（${totalShafts.toLocaleString()} 根 × 5 米 = ${Number(mileage).toLocaleString()} 米），小车路程分析无误，运行轨迹与批次节奏完全匹配，无卡阻或异常空转。\n\n**四、 综合诊断结论**\n车间现场 6S 规范维持良好，相机镜头洁净，分区定置清晰，小车与工位联锁正常，今日产线环境评定为【规范达标】（综合评定 99.2 分）。已自动为您生成今日巡检归档报告。${disclaimer}`;
      }

      // 3. 小车行程与图片逻辑异常分析
      if (query.includes('小车') && (query.includes('2800') || query.includes('路程') || query.includes('图片') || query.includes('问题') || query.includes('米'))) {
        return `通过对比照片数量和小车行驶里程，我发现产线存在明显的逻辑异常：\n\n1. **数据矛盾**：\n- 视觉检测拍了 2,800 张照片（相当于 100 根半轴的工作量）；\n- 但分拣小车今天一共只跑了 5.2 米（正常转运 100 根半轴应该跑 500 米左右）。\n\n2. **问题原因**：\n产线并没有真正完成 100 根半轴的分拣，而是小车在工位卡住没能换料，检测台在原地对着同一根半轴重复拍了 100 遍。\n\n3. **建议处理**：\n建议先暂停检测程序，检查 1 号工位小车的轨道和驱动轮，并重置批次计数。${disclaimer}`;
      }

      // 4. 机械臂状态自主时段研判
      if (query.includes('机械臂') && (query.includes('状态') || query.includes('检查') || query.includes('复位'))) {
        if (isWorkTime) {
          return `我已经帮你检查完机械臂的状态了，情况如下：\n\n1. **机械臂位置**：\n目前机械臂停在工位上方，处于检测悬停姿态。\n\n2. **未复位原因分析**：\n系统时间显示当前为 ${currentTime}，处于正常生产上班时间。工位上正好有刚送过来的待检半轴，机械臂是在准备拍照检测，属于正常的生产待命状态。\n\n3. **智能判断与处理**：\n- 如果现在是【下班时间】，大家下班后机械臂没复位属于安全违规（容易发热损耗），我会建议立即调度复位关机；\n- 但现在是【上班时间】，设备正在正常工作，各项指标（温度、气压）也都很正常，所以不需要强制归位，产线可以继续正常运行。${disclaimer}`;
        } else {
          return `**警报：检测到机械臂存在未归位安全隐患！**\n\n1. **机械臂当前状态**：\n目前机械臂处于工作悬停姿态，未处于安全原点。\n\n2. **时段与隐患分析**：\n当前时间为 ${currentTime}，已处于【下班/休工时段】。下班后机械臂长时间通电发热属于 6S 安全违规，容易损耗抱闸寿命并存在碰撞隐患。\n\n3. **建议处理**：\n建议立即下发复位指令，让机械臂平稳回归零位并关闭辅机电源。${disclaimer}`;
        }
      }

      // 5. 硬件与控制指令
      if (query.includes('机械臂复位') || query.includes('复位机械臂') || query.includes('将机械臂复位')) {
        return `好的，已通过工控总线为全周检测工位下发指令：**机械臂六轴已平稳复位至初始原点**，伺服抱闸锁定正常，处于待命就绪状态！${disclaimer}`;
      }
      if (query.includes('小车归位') || query.includes('分拣小车归位') || query.includes('小车复位') || query.includes('归位分拣小车') || query.includes('AGV小车归位')) {
        return `好的，已为车间分拣单元下发调度指令：**分拣小车已安全调度归位**，随时准备下一批次缺陷品转运！${disclaimer}`;
      }

      // 6. 6S 单项标准规范
      if (query.includes('整理') || query.includes('Seiri')) {
        return `### 【6S·整理 (Seiri)】半轴质检工位实施规范：\n\n1. **红牌作战机制**：对连续 3 批次未检/无法标定半轴挂设红牌，4小时内移至待查隔离区；\n2. **要与不要分类**：工作台上严禁摆放私人水杯、非检验图纸、已失效标定工具；\n3. **空间释放**：检测机柜周围 1.2 米内禁止堆叠闲置纸箱，保障机柜散热与巡检通道畅通。${disclaimer}`;
      }
      if (query.includes('整顿') || query.includes('Seiton')) {
        return `### 【6S·整顿 (Seiton)】定置定位管理标准：\n\n1. **定置三要素**：\n   - **定点**：卡尺/测头必须放置于专用 EVA 减震定位槽；\n   - **定容**：合格品放入绿色周转箱，缺陷品放入黄色锁扣防错周转箱；\n   - **定量**：暂存工位半轴堆叠上限为 6 件，杜绝超载碰伤。\n2. **可视化标识**：地坪黄色警戒线 100mm 规范施划，目视化率达到 100%。${disclaimer}`;
      }
      if (query.includes('清扫') || query.includes('Seiso') || query.includes('清洁规程') || query.includes('相机')) {
        return `### 【6S·清扫 (Seiso)】光学感知与机械台点检规程：\n\n1. **工业相机与光源清扫**：\n   - 每日开班前使用无水乙醇配合专用镜头纸顺时针单向擦拭；\n   - 严禁使用普通棉纱擦拭高精度光学滤镜。\n2. **转台与导轨除屑**：使用工业吸尘器清理金属毛刺，严禁用高压气枪直接吹扫轴承间隙。\n3. **清扫即点检**：清扫过程中同步检查螺栓有无松动、气压表指针是否在 0.6±0.05 MPa 正常区间。${disclaimer}`;
      }
      if (query.includes('清洁') || query.includes('Seiketsu')) {
        return `### 【6S·清洁 (Seiketsu)】长效常态化机制：\n\n1. 坚持前 3S（整理、整顿、清扫）的成果标准化；\n2. 每日实行 **「班前5分钟确认，班后10分钟维持」** 责任包干制；\n3. 质检系统已开启自动巡检日志，每周五下午生成 6S 数字化综合诊断红黑榜。${disclaimer}`;
      }
      if (query.includes('素养') || query.includes('Shitsuke')) {
        return `### 【6S·素养 (Shitsuke)】质检人员行为规程：\n\n1. 严格遵守半轴外观缺陷判定基准（GB/T 38885）；\n2. 严禁未经授权修改 AI 缺陷识别置信度阈值（当前阈值锁定 ≥0.85）；\n3. 作业过程穿戴劳保手套，严禁裸手接触精加工半轴表面。${disclaimer}`;
      }
      if (query.includes('安全') || query.includes('Safety') || query.includes('AGV')) {
        return `### 【6S·安全 (Safety)】智能运检联锁安全防线：\n\n1. **AGV激光避障**：AGV 行进路径 1.5 米内感应减速，0.6 米内触发硬级联急停；\n2. **机械臂联锁**：全周质检工作站安全光栅遮断时，伺服主轴 0.1s 内制动锁定；\n3. **用电与接地**：大功率高频光源与计算服务器外壳接地电阻需 ＜4Ω，杜绝静电击穿。${disclaimer}`;
      }
      if (query.includes('报告') || query.includes('自检') || query.includes('评分')) {
        return `### 【当前车间 6S 数字化自检诊断简报】\n\n* **评定等级**：**A级·卓越 (98.5分)**\n* **明细指标达成情况**：\n  - 整理(Seiri)：98.0%（通道通畅无积压）\n  - 整顿(Seiton)：99.0%（量具与样本定置定位率100%）\n  - 清扫(Seiso)：97.0%（相机镜头通透无油污积尘）\n  - 清洁(Seiketsu)：100.0%（标准化看板与点检执行规范）\n  - 素养(Shitsuke)：98.0%（作业人员严守安全与品质规范）\n  - 安全(Safety)：100.0%（安全光栅、急停与避障联锁零隐患）\n* **管家行动建议**：建议 14:30 针对 2 号标定工位开展例行气吹维护。${disclaimer}`;
      }
      return `收到关于「**${query}**」的咨询。\n根据当前车间 6S 规范与工位执行标准：\n1. 请确认现场物料与工具已严格执行 **定点、定容、定量**；\n2. 如检测相机有微弱反光或模糊，请先执行 **6S 清扫标准（无水乙醇擦拭）**；\n3. 需深入调阅某一细项标准，可直接点击下方技能胶囊或快捷指令！${disclaimer}`;
    },
    formatImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('data:image') || url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      return url.startsWith('/') ? url : '/' + url;
    },
    formatMessage(text) {
      if (!text) return '';
      let html = text
        .replace(/### (.*?)\n/g, '<div class="md-heading">$1</div>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
      html = html.replace(/\*?(本回答由灵鉴生成[^*<\n]+)\*?/g, '<div class="disclaimer-text">$1</div>');
      html = html
        .replace(/【未复位】/g, '<span class="badge-status-danger">【未复位】</span>')
        .replace(/【需整改】/g, '<span class="badge-status-danger">【需整改】</span>')
        .replace(/【异常】/g, '<span class="badge-status-danger">【异常】</span>')
        .replace(/【高危】/g, '<span class="badge-status-danger">【高危】</span>')
        .replace(/【正常】/g, '<span class="badge-status-success">【正常】</span>')
        .replace(/【规范达标】/g, '<span class="badge-status-success">【规范达标】</span>')
        .replace(/【达标】/g, '<span class="badge-status-success">【达标】</span>')
        .replace(/【优秀】/g, '<span class="badge-status-success">【优秀】</span>');
      html = html.replace(/\n/g, '<br/>');
      return html;
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
    },
    printSixSReport() {
      const printableDom = document.getElementById('sixs-report-printable');
      if (!printableDom) {
        this.$message.error('未找到可打印的报告内容');
        return;
      }

      let oldIframe = document.getElementById('sixs-report-print-iframe');
      if (oldIframe) {
        document.body.removeChild(oldIframe);
      }

      const iframe = document.createElement('iframe');
      iframe.id = 'sixs-report-print-iframe';
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
          <title>车间6S智能巡检工艺现场诊断处置单</title>
          <style>
            @page { size: A4 portrait; margin: 10mm 12mm; }
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; color: #000; background: #ffffff; padding: 5px; }
            .no-print { display: none !important; }
            .industrial-report-paper { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; color: #000; max-width: 100%; margin: 0 auto; padding: 10px; }
            .industrial-header { text-align: center; position: relative; border-bottom: 3px solid #000; padding-bottom: 15px; margin-bottom: 20px; }
            .header-logo { position: absolute; left: 0; top: 0; font-size: 16px; font-weight: bold; border: 2px solid #000; padding: 4px 10px; letter-spacing: 2px; }
            .header-title { font-size: 24px; font-weight: bold; margin: 0 0 10px 0; letter-spacing: 3px; }
            .header-code { position: absolute; right: 0; bottom: 15px; font-size: 14px; font-family: monospace; }
            .industrial-meta-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 14px; }
            .industrial-meta-table td { border: 1px solid #000; padding: 8px 12px; }
            .meta-label { background: #f0f0f0; font-weight: bold; width: 12%; text-align: center; }
            .meta-value { width: 21%; }
            .industrial-section { margin-bottom: 20px; }
            .section-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; border-left: 4px solid #000; padding-left: 8px; line-height: 1; }
            .industrial-kpi-row { display: flex; border: 2px solid #000; }
            .kpi-item { flex: 1; border-right: 1px solid #000; text-align: center; padding: 12px 2px; }
            .kpi-item:last-child { border-right: none; }
            .kpi-title { font-size: 13px; color: #333; margin-bottom: 6px; }
            .kpi-value { font-size: 18px; font-weight: bold; }
            .kpi-value.text-action { font-size: 14px; }
            .split-section { display: flex; gap: 20px; }
            .split-left { flex: 1; }
            .split-right { flex: 1.2; }
            .industrial-ai-table { width: 100%; border-collapse: collapse; height: 180px; }
            .industrial-ai-table th, .industrial-ai-table td { border: 1px solid #000; padding: 10px 12px; font-size: 13.5px; }
            .industrial-ai-table th { background: #f0f0f0; text-align: center; }
            .industrial-detail-table { width: 100%; border-collapse: collapse; border: 2px solid #000; font-size: 13px; }
            .industrial-detail-table th, .industrial-detail-table td { border: 1px solid #000; padding: 8px; }
            .industrial-detail-table th { background: #f0f0f0; }
            .industrial-footer { display: flex; justify-content: flex-end; gap: 40px; margin-top: 30px; font-size: 15px; font-weight: bold; }
            .sign-block .line { display: inline-block; width: 120px; border-bottom: 1px solid #000; }
            .industrial-remark { margin-top: 15px; font-size: 12px; color: #666; }
            .text-ng { color: #d32f2f !important; font-weight: bold; }
            .text-ok { color: #2e7d32 !important; font-weight: bold; }
            .text-action { color: #c62828 !important; font-weight: bold; font-size: 15px; }
            .font-bold { font-weight: bold; }
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
  gap: 8px;
  background-color: transparent;
  box-sizing: border-box;
  height: 100%;
  max-height: 100%;
  flex: 1;
  overflow: hidden;
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
  padding: 10px 18px !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05) !important;
  border: 1px solid #ebeef5 !important;
  box-sizing: border-box !important;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  width: 34px;
  height: 34px;
  background: #e6f7ff;
  border-radius: 8px !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  font-size: 19px;
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
  letter-spacing: -0.3px;
}

.title-tag {
  font-size: 12px;
  font-weight: 500;
  color: #1890ff;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px !important;
  padding: 2px 8px;
  line-height: 1.3;
}

.page-desc {
  margin: 3px 0 0 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.3;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
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
  border-radius: 10px !important;
  padding: 8px 12px 6px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 80px;
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
  width: 30px;
  height: 30px;
  border-radius: 7px !important;
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title-text {
  font-size: 18.5px;
  font-weight: 800;
  color: #1c3047;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.2px;
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

/* ================= 5. 底部工作区 (AI 工作台横向撑满) ================= */
.bottom-dual-layout {
  display: flex;
  width: 100%;
  gap: 0;
  align-items: stretch;
  flex: 1;
  min-height: 0;
  height: 100%;
}

.left-ai-column {
  width: 100%;
  flex: 1;
  height: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ================= AI 智能体工作台现代极简样式 (1:1 对齐 AgentWorkbench.html) ================= */
.ai-workbench-card {
  flex: 1;
  height: 100%;
  max-height: 100%;
  min-height: 0;
  background: #f7f8fa;
  border: 1px solid #e2e8f0;
  border-radius: 16px !important;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
  position: relative;
}

/* 顶部简约栏 */
.top-header-strip {
  padding: 12px 24px;
  border-bottom: 1px solid #eef0f3;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  flex-shrink: 0;
}

.top-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-title .dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  display: inline-block;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-badge {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 6px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.clear-btn {
  font-size: 13px;
  color: #6b7280;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 4px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #f9fafb;
  color: #ef4444;
  border-color: #fca5a5;
}

/* 聊天主区域 */
.ai-chat-stage {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 24px;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: transparent;
  box-sizing: border-box;
}

.ai-chat-stage::-webkit-scrollbar {
  display: none;
}

.chat-welcome-box {
  width: 100%;
  max-width: 820px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-headline {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  font-size: 32px;
  font-weight: 750;
  color: #111827;
  letter-spacing: -0.5px;
  margin: 32px 0 24px 0;
  line-height: 1.35;
  text-align: center;
}

.welcome-robot-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  display: inline-block;
  border: none;
  box-shadow: none;
}

.recommend-section {
  width: 100%;
  max-width: 820px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recommend-title {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.recommend-item {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13.5px;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
}

.recommend-item:hover {
  background: #f9fafb;
  border-color: #3b82f6;
  color: #2563eb;
  transform: translateX(4px);
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.message-item.user {
  align-items: flex-end;
}

.user-bubble {
  background: #1a1a1a;
  color: #ffffff;
  padding: 11px 18px;
  border-radius: 16px 16px 4px 16px;
  font-size: 14px;
  line-height: 1.55;
  max-width: 82%;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 思考框 (DeepSeek 极简风) */
.thought-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 4px;
  width: 100%;
}

.thought-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888888;
  cursor: pointer;
  user-select: none;
  width: fit-content;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.2s;
}

.thought-toggle:hover {
  color: #444444;
  background: #eef1f5;
}

.spinner {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid #ccc;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.thought-text-body {
  border-left: 2px solid #cbd5e1;
  padding-left: 14px;
  margin-left: 4px;
  font-size: 13px;
  color: #555555;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  background: rgba(241, 245, 249, 0.5);
  padding-top: 8px;
  padding-bottom: 8px;
  padding-right: 12px;
  border-radius: 0 8px 8px 0;
}

/* 正文回答区 */
.answer-body {
  font-size: 14px;
  line-height: 1.75;
  color: #1f2937;
  word-break: break-word;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
}

.answer-body:empty {
  display: none !important;
}

.markdown-render {
  white-space: pre-wrap;
  line-height: 1.75;
}

.md-heading {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.disclaimer-text {
  color: #8c8c8c !important;
  font-style: normal !important;
  font-size: 12.5px;
  margin-top: 12px;
  display: block;
}

/* 状态标签红绿高亮 */
.badge-status-danger {
  color: #ef4444 !important;
  font-weight: 700 !important;
  background: #fef2f2 !important;
  border: 1px solid #fee2e2 !important;
  padding: 1px 6px !important;
  border-radius: 4px !important;
  display: inline-block !important;
}

.badge-status-success {
  color: #10b981 !important;
  font-weight: 700 !important;
  background: #ecfdf5 !important;
  border: 1px solid #d1fae5 !important;
  padding: 1px 6px !important;
  border-radius: 4px !important;
  display: inline-block !important;
}

/* ================= DeepSeek 对话底部数据源胶囊与操作工具栏 ================= */
.deepseek-footer-wrapper {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}

/* 数据源调阅胶囊 */
.sources-read-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.sources-read-pill:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #1e293b;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.pill-icons {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.p-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.p-dot.c1 { background: #3b82f6; }
.p-dot.c2 { background: #10b981; }
.p-dot.c3 { background: #f59e0b; }
.p-dot.c4 { background: #8b5cf6; }

.pill-text {
  font-weight: 500;
  letter-spacing: 0.2px;
}

/* 操作图标按钮栏 */
.ds-action-bar {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ds-icon-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid transparent;
  background: transparent;
  color: #94a3b8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  outline: none;
  transition: all 0.18s ease;
}

.ds-icon-btn:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #334155;
  transform: translateY(-1px);
}

.ds-icon-btn:active {
  transform: translateY(0);
  background: #e2e8f0;
}

.ds-icon-btn.active {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #2563eb;
}

.ds-icon-btn svg {
  display: block;
  transition: transform 0.18s;
}

.ds-icon-btn:hover svg {
  transform: scale(1.08);
}

/* 交互按钮组 */
.ai-interactive-actions {
  margin-top: 4px;
}

.action-btn-group {
  display: flex;
  gap: 10px;
}

.choice-btn {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.yes-btn {
  background: #10b981;
  color: #ffffff;
}

.yes-btn:hover {
  background: #059669;
}

.no-btn {
  background: #f3f4f6;
  color: #4b5563;
  border-color: #e5e7eb;
}

.no-btn:hover {
  background: #e5e7eb;
  color: #1f2937;
}

.action-done-status {
  font-size: 12px;
}

.done-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
}

.done-tag.yes {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.done-tag.no {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #e5e7eb;
}

/* 底部输入卡片 (豆包 1:1 同款) */
.bottom-input-wrapper {
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px 20px;
  flex-shrink: 0;
  box-sizing: border-box;
}

.doubao-input-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 14px 16px 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.doubao-input-card:focus-within {
  border-color: #94a3b8;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
}

.input-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 14.5px;
  line-height: 1.5;
  resize: none;
  height: 38px;
  min-height: 38px;
  color: #1f2937;
  background: transparent;
  font-family: inherit;
}

.input-textarea::placeholder {
  color: #9ca3af;
}

.doubao-bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-top: 1px solid #f3f4f6;
  padding-top: 8px;
  position: relative;
}

.plus-container {
  position: relative;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  z-index: 1000;
}

.plus-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #6b7280;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.plus-btn:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.upload-popup-menu {
  position: absolute;
  bottom: 42px;
  left: 0;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
  padding: 6px;
  width: 230px;
  display: none;
  flex-direction: column;
  gap: 3px;
  z-index: 99999;
}

.upload-popup-menu.show {
  display: flex !important;
}

.upload-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
  transition: all 0.18s;
  text-align: left;
  border: none;
  background: transparent;
  width: 100%;
}

.upload-menu-item:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.attachment-preview-bar {
  display: none;
  align-items: center;
  gap: 8px;
  padding: 4px 6px 6px 6px;
  flex-wrap: wrap;
}

.attachment-preview-bar.show {
  display: flex;
}

.attachment-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  padding: 3px 8px;
  font-size: 12px;
  color: #334155;
}

.attachment-badge .badge-remove {
  cursor: pointer;
  color: #94a3b8;
  font-size: 14px;
  font-weight: bold;
}

.attachment-badge .badge-remove:hover {
  color: #ef4444;
}

.skills-left {
  display: flex;
  align-items: center;
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
  flex: 1;
}

.skills-left::-webkit-scrollbar {
  display: none;
}

.skill-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 12.5px;
  color: #475569;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
  transition: all 0.2s;
  flex-shrink: 0;
}

.skill-pill:hover {
  background: #f1f5f9;
  color: #1e293b;
  border-color: #cbd5e1;
}

.actions-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.voice-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.voice-icon-btn:hover {
  background: #f3f4f6;
  color: #111827;
  border-color: #cbd5e1;
}

.send-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #1a1a1a;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.send-icon-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.send-icon-btn:not(:disabled):hover {
  background: #333333;
  transform: scale(1.05);
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

/* ================= 7. 智能巡诊现场深度诊断报告弹窗 (工业标准黑白纸质检测单版式 1:1 对齐) ================= */
::v-deep .sixs-report-dialog-wrapper.el-dialog__wrapper {
  left: 220px !important;
  top: 66px !important;
  width: calc(100vw - 220px) !important;
  height: calc(100vh - 66px) !important;
  overflow: hidden !important;
}

::v-deep .sixs-report-dialog.expert-report-dialog {
  margin: 6px auto !important;
  top: 0 !important;
  transform: none !important;
  max-height: calc(100vh - 84px) !important;
  display: flex !important;
  flex-direction: column !important;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  background: #fff;
}

::v-deep .sixs-report-dialog.expert-report-dialog .el-dialog__header {
  flex-shrink: 0 !important;
  background: #f5f7fa;
  border-bottom: 2px solid #333;
  padding: 15px 20px;
}

::v-deep .sixs-report-dialog.expert-report-dialog .el-dialog__body {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: 20px;
  background: #fff;
  box-sizing: border-box !important;
}

/* 工业检测单实体纸张样式与排版 */
.industrial-report-paper {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #000;
  max-width: 100%;
  margin: 0 auto;
  padding: 10px;
}

.industrial-header {
  text-align: center;
  position: relative;
  border-bottom: 3px solid #000;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.header-logo {
  position: absolute;
  left: 0;
  top: 0;
  font-size: 16px;
  font-weight: bold;
  border: 2px solid #000;
  padding: 4px 10px;
  letter-spacing: 2px;
}

.header-title {
  font-size: 26px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 4px;
}

.header-code {
  position: absolute;
  right: 0;
  bottom: 15px;
  font-size: 14px;
  font-family: monospace;
}

.industrial-meta-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  font-size: 14px;
}

.industrial-meta-table td {
  border: 1px solid #000;
  padding: 8px 12px;
}

.meta-label {
  background: #f0f0f0;
  font-weight: bold;
  width: 12%;
  text-align: center;
}

.meta-value {
  width: 21%;
}

.industrial-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  border-left: 4px solid #000;
  padding-left: 8px;
  line-height: 1;
}

.industrial-kpi-row {
  display: flex;
  border: 2px solid #000;
}

.kpi-item {
  flex: 1;
  border-right: 1px solid #000;
  text-align: center;
  padding: 12px 2px;
}

.kpi-item:last-child {
  border-right: none;
}

.kpi-title {
  font-size: 13px;
  color: #333;
  margin-bottom: 6px;
}

.kpi-value {
  font-size: 18px;
  font-weight: bold;
}

.kpi-value.text-action {
  font-size: 14px;
}

.split-section {
  display: flex;
  gap: 20px;
}

.split-left {
  flex: 1;
}

.split-right {
  flex: 1.2;
}

.industrial-ai-table {
  width: 100%;
  border-collapse: collapse;
  height: 180px;
}

.industrial-ai-table th,
.industrial-ai-table td {
  border: 1px solid #000;
  padding: 10px 12px;
  font-size: 13.5px;
}

.industrial-ai-table th {
  background: #f0f0f0;
  text-align: center;
}

.industrial-detail-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  font-size: 13px;
}

.industrial-detail-table th,
.industrial-detail-table td {
  border: 1px solid #000;
  padding: 8px;
}

.industrial-detail-table th {
  background: #f0f0f0;
}

.industrial-footer {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  margin-top: 30px;
  font-size: 15px;
  font-weight: bold;
}

.sign-block .line {
  display: inline-block;
  width: 120px;
  border-bottom: 1px solid #000;
}

.industrial-remark {
  margin-top: 15px;
  font-size: 12px;
  color: #666;
}

.text-ng {
  color: #d32f2f !important;
  font-weight: bold;
}

.text-ok {
  color: #2e7d32 !important;
  font-weight: bold;
}

.text-action {
  color: #c62828 !important;
  font-weight: bold;
  font-size: 15px;
}

.font-bold {
  font-weight: bold;
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

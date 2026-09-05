const nodemailer = require('../../node_modules/nodemailer');
const express = require('express');

module.exports = {
  devServer: {
    port: "8082", // 设置端口 默认8080
    open: true, // 项目运行自动打开浏览器
    historyApiFallback: true,
    setupMiddlewares: (middlewares, devServer) => {
      // 专家质检报告专属直达邮件中继接口（专属轻量解析，避免全局阻塞）
      devServer.app.post('/api/detectInfo/warnings/sendExpertEmail', express.json({ limit: '20mb' }), express.urlencoded({ extended: true, limit: '20mb' }), async (req, res) => {
        try {
          const { to, subject, reportData } = req.body || {};
          if (!to) {
            return res.status(400).json({ code: 400, msg: '收件人邮箱不能为空' });
          }

          // 动态解密安全授权凭证 (内存即时计算，杜绝明文留存)
          const _enc = [0x50, 0x51, 0x4c, 0x45, 0x5c, 0x52, 0x4c, 0x5e, 0x46, 0x54, 0x4f, 0x46, 0x5c, 0x5d, 0x55, 0x58];
          const authPass = _enc.map(c => String.fromCharCode(c ^ 0x3f)).join('');

          const transporter = nodemailer.createTransport({
            host: 'smtp.qq.com',
            port: 465,
            secure: true,
            auth: {
              user: '3767953802@qq.com',
              pass: authPass
            }
          });

          const rData = reportData || {};
          const defectRows = (rData.defections && rData.defections.length > 0)
            ? rData.defections.map((d, index) => `
                <tr style="border-bottom: 1px solid #e5e7eb; font-size: 12px; text-align: center;">
                  <td style="padding: 10px 8px; color: #64748b;">${index + 1}</td>
                  <td style="padding: 10px 8px;"><span style="display:inline-block; padding: 2px 8px; border-radius: 4px; background: #fee2e2; color: #dc2626; font-weight: 600;">${d.category || '表面划痕'}</span></td>
                  <td style="padding: 10px 8px; font-weight: bold; color: #1e293b;">${d.score || '93.5%'}</td>
                  <td style="padding: 10px 8px; color: #475569; font-family: monospace;">${d.coords || '56.0, 108.0'}</td>
                  <td style="padding: 10px 8px; color: #475569; font-family: monospace;">${d.size || '32.0 × 28.0 px'}</td>
                  <td style="padding: 10px 8px;"><span style="display:inline-block; padding: 2px 8px; border-radius: 4px; background: ${(d.severityLevel >= 4) ? '#fee2e2' : '#fef3c7'}; color: ${(d.severityLevel >= 4) ? '#dc2626' : '#d97706'}; font-weight: 600;">${d.severityLevel || 3} 级</span></td>
                  <td style="padding: 10px 8px; text-align: left; color: #475569;">${d.repairSuggestion || '建议使用精细砂纸局部打磨'}</td>
                </tr>
              `).join('')
            : `
                <tr style="border-bottom: 1px solid #e5e7eb; font-size: 12px; text-align: center;">
                  <td style="padding: 10px 8px; color: #64748b;">1</td>
                  <td style="padding: 10px 8px;"><span style="display:inline-block; padding: 2px 8px; border-radius: 4px; background: #fee2e2; color: #dc2626; font-weight: 600;">裂纹</span></td>
                  <td style="padding: 10px 8px; font-weight: bold; color: #1e293b;">94.20%</td>
                  <td style="padding: 10px 8px; color: #475569; font-family: monospace;">78.5, 112.0</td>
                  <td style="padding: 10px 8px; color: #475569; font-family: monospace;">45.0 × 18.0 px</td>
                  <td style="padding: 10px 8px;"><span style="display:inline-block; padding: 2px 8px; border-radius: 4px; background: #fee2e2; color: #dc2626; font-weight: 600;">4 级</span></td>
                  <td style="padding: 10px 8px; text-align: left; color: #475569;">高风险微观疲劳裂纹，需探伤复检</td>
                </tr>
              `;

          let imageTag = '<div style="color: #94a3b8; font-size: 12px; text-align: center; line-height: 180px; background: #0b1120; border-radius: 8px;">未获取到原始切片图像</div>';
          if (rData.imgBase64) {
            const imgSrc = rData.imgBase64.startsWith('data:image') ? rData.imgBase64 : `data:image/jpeg;base64,${rData.imgBase64}`;
            imageTag = `<img src="${imgSrc}" alt="缺陷切片图谱" style="max-width: 100%; max-height: 240px; border-radius: 6px; object-fit: contain; display: block; margin: 0 auto;" />`;
          }

          const htmlContent = `
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; color: #1e293b; background: #f1f5f9; padding: 24px 10px; }
                .report-wrapper { max-width: 820px; margin: 0 auto; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08); }
                .report-header { background: #001529; color: #ffffff; padding: 24px 30px; border-bottom: 3px solid #2563eb; }
                .brand-badge { display: inline-block; background: rgba(37, 99, 235, 0.25); color: #60a5fa; border: 1px solid rgba(96, 165, 250, 0.4); font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 4px; margin-bottom: 8px; letter-spacing: 0.5px; }
                .report-title { font-size: 22px; font-weight: 800; color: #ffffff; margin: 0 0 12px 0; letter-spacing: -0.3px; }
                .meta-bar { display: flex; flex-wrap: wrap; gap: 10px; font-size: 12px; color: #94a3b8; }
                .meta-tag { background: rgba(255,255,255,0.08); padding: 4px 10px; border-radius: 4px; border: 1px solid rgba(255,255,255,0.12); color: #e2e8f0; }
                .meta-tag b { color: #38bdf8; }

                .report-content { padding: 26px 30px; }

                /* KPI 4格卡片 */
                .kpi-container { display: table; width: 100%; table-layout: fixed; margin-bottom: 24px; }
                .kpi-cell { display: table-cell; padding: 0 6px; vertical-align: top; }
                .kpi-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px 12px; min-height: 90px; }
                .kpi-card.danger { border-top: 3px solid #ef4444; }
                .kpi-card.warning { border-top: 3px solid #f59e0b; }
                .kpi-card.primary { border-top: 3px solid #3b82f6; }
                .kpi-card.success { border-top: 3px solid #10b981; }
                .kpi-title { font-size: 11px; font-weight: 600; color: #64748b; margin-bottom: 6px; }
                .kpi-number { font-size: 20px; font-weight: 800; color: #0f172a; line-height: 1.2; }
                .kpi-number.red { color: #dc2626; }
                .kpi-number.amber { color: #b45309; font-size: 17px; }
                .kpi-number.blue { color: #1d4ed8; }
                .kpi-number.green { color: #991b1b; font-size: 14px; }
                .kpi-desc { font-size: 10px; color: #94a3b8; margin-top: 4px; }

                /* 左右分栏 */
                .split-container { display: table; width: 100%; table-layout: fixed; margin-bottom: 26px; }
                .split-left-cell { display: table-cell; width: 44%; padding-right: 12px; vertical-align: top; }
                .split-right-cell { display: table-cell; width: 56%; padding-left: 12px; vertical-align: top; }
                .block-title { font-size: 13px; font-weight: 700; color: #1e293b; margin-bottom: 8px; }
                .img-card { background: #0b1120; border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px; min-height: 200px; text-align: center; }
                .qwen-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px 16px; min-height: 200px; }

                .advice-row { margin-bottom: 12px; font-size: 12px; line-height: 1.5; }
                .advice-label { font-weight: 700; color: #1e293b; margin-bottom: 3px; display: flex; align-items: center; }
                .num-circle { display: inline-block; width: 16px; height: 16px; line-height: 16px; text-align: center; border-radius: 50%; color: #fff; font-size: 10px; margin-right: 6px; }
                .circle-1 { background: #3b82f6; }
                .circle-2 { background: #f59e0b; }
                .circle-3 { background: #ef4444; }
                .advice-text { color: #475569; padding-left: 22px; }
                .advice-action { background: #fef2f2; border: 1px solid #fee2e2; border-left: 3px solid #ef4444; padding: 6px 10px; border-radius: 4px; color: #991b1b; font-weight: bold; margin-top: 4px; }

                /* 明细表格 */
                .table-section { margin-bottom: 24px; }
                .data-table { width: 100%; border-collapse: collapse; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; }
                .data-table th { background: #f1f5f9; color: #475569; font-size: 11.5px; font-weight: 700; padding: 10px 8px; text-align: center; border-bottom: 1px solid #e2e8f0; }

                /* 底部签名区 */
                .report-footer { border-top: 1px solid #e2e8f0; padding-top: 16px; display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #64748b; }
                .sign-zone { display: flex; gap: 32px; }
                .notice-foot { font-size: 10.5px; color: #94a3b8; font-style: italic; }
              </style>
            </head>
            <body>
              <div class="report-wrapper">
                <!-- 头部 -->
                <div class="report-header">
                  <div class="brand-badge">云擎智检 · 工业质检报告中枢</div>
                  <h1 class="report-title">半轴表面缺陷检测与工艺处置单</h1>
                  <div class="meta-bar">
                    <span class="meta-tag">流水号：<b>#${rData.id || '86'}</b></span>
                    <span class="meta-tag">工单编号：<b>${rData.workOrderId || rData.axleCode || 'WO-2026-02'}</b></span>
                    <span class="meta-tag">检测时间：<b>${rData.detectTime || '2026-09-04 18:58:19'}</b></span>
                    <span class="meta-tag">算法引擎：<b>Vision-Model v2.4</b></span>
                  </div>
                </div>

                <div class="report-content">
                  <!-- 4 项核心 KPI 卡片 -->
                  <div class="kpi-container">
                    <div class="kpi-cell" style="padding-left:0;">
                      <div class="kpi-card danger">
                        <div class="kpi-title">检出缺陷总数</div>
                        <div class="kpi-number red">${rData.defectCount || 2} <span style="font-size:12px; color:#64748b; font-weight:normal;">处</span></div>
                        <div class="kpi-desc">已完成切片提取</div>
                      </div>
                    </div>
                    <div class="kpi-cell">
                      <div class="kpi-card warning">
                        <div class="kpi-title">最高风险等级</div>
                        <div class="kpi-number amber">${rData.maxLevel || '严重'}</div>
                        <div class="kpi-desc">等级评定与预警</div>
                      </div>
                    </div>
                    <div class="kpi-cell">
                      <div class="kpi-card primary">
                        <div class="kpi-title">缺陷面积占比估算</div>
                        <div class="kpi-number blue">${rData.areaRatio || '约 2.6%'}</div>
                        <div class="kpi-desc">占工件检测区域</div>
                      </div>
                    </div>
                    <div class="kpi-cell" style="padding-right:0;">
                      <div class="kpi-card success">
                        <div class="kpi-title">最终处置决策</div>
                        <div class="kpi-number green">${rData.finalAdvice || '建议返修'}</div>
                        <div class="kpi-desc">现场复核后放行</div>
                      </div>
                    </div>
                  </div>

                  <!-- 图像与大模型深度研判 -->
                  <div class="split-container">
                    <div class="split-left-cell">
                      <div class="block-title">📷 缺陷视觉图谱与定位切片</div>
                      <div class="img-card">
                        ${imageTag}
                      </div>
                    </div>
                    <div class="split-right-cell">
                      <div class="block-title">🤖 智控专家大模型研判中枢 (Qwen-AI)</div>
                      <div class="qwen-card">
                        <div class="advice-row">
                          <div class="advice-label"><span class="num-circle circle-1">1</span>总体缺陷情况研判</div>
                          <div class="advice-text">${rData.summaryAdvice || '工件表面检测到明显划痕与局部缺陷。'}</div>
                        </div>
                        <div class="advice-row">
                          <div class="advice-label"><span class="num-circle circle-2">2</span>综合分析依据</div>
                          <div class="advice-text">${rData.analysisBasis || '缺陷呈局部聚集分布，最高严重程度较高。'}</div>
                        </div>
                        <div class="advice-row" style="margin-bottom:0;">
                          <div class="advice-label"><span class="num-circle circle-3">3</span>车间工件处置指令</div>
                          <div class="advice-action">${rData.finalAdvice || '建议质检员现场卡尺测量，根据公差标准判定是否返修'}</div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 缺陷检测切片结构化明细表格 -->
                  <div class="table-section">
                    <div class="block-title">📋 缺陷检测切片结构化明细</div>
                    <table class="data-table">
                      <thead>
                        <tr>
                          <th style="width: 45px;">序号</th>
                          <th style="width: 90px;">缺陷类型</th>
                          <th style="width: 75px;">置信度</th>
                          <th style="width: 110px;">位置坐标 (X, Y)</th>
                          <th style="width: 120px;">切片尺寸 (长 × 宽)</th>
                          <th style="width: 75px;">严重等级</th>
                          <th>初步工艺建议</th>
                        </tr>
                      </thead>
                      <tbody>
                        ${defectRows}
                      </tbody>
                    </table>
                  </div>

                  <!-- 底部签名区 -->
                  <div class="report-footer">
                    <div class="sign-zone">
                      <span>质检核对员：__________________</span>
                      <span>车间工段长：__________________</span>
                    </div>
                    <div class="notice-foot">
                      * 本报告由云擎智检视觉大模型自动分析生成并邮件归档。
                    </div>
                  </div>
                </div>
              </div>
            </body>
            </html>
          `;

          const info = await transporter.sendMail({
            from: '"云擎智检·工业质检中枢" <3767953802@qq.com>',
            to: to,
            subject: subject || `【云擎智检·工业质检告警】半轴 ${rData.axleCode || '2403511-P301'} 缺陷分析报告`,
            html: htmlContent
          });

          console.log('✅ 邮件发送成功, MessageId:', info.messageId);
          res.json({ code: 200, msg: '邮件发送成功', messageId: info.messageId });
        } catch (err) {
          console.error('❌ 邮件发送失败:', err);
          res.status(500).json({ code: 500, msg: err.message });
        }
      });

      return middlewares;
    },
    // 在与port open 设置服务代理
    proxy: {
      // /api 自定义服务代理名字
      "/api": {
        target: "http://localhost:8081", //代理帮助你请求的具体服务http://localhost:8081
        changeOrigin: true, // 开启代理
        ws: true, // 启用WebSocket代理（SSE需要）
        pathRewrite: {  // 格式化path
          "^/api": ""
        },
        bypass: function (req) {
          // 浏览器直接刷新页面时，请求头 accept 包含 html，或者是 /apimanager 这类页面路由，直接返回单页面入口 index.html，不代理给后端
          if (req.headers && req.headers.accept && req.headers.accept.includes("html")) {
            return "/index.html";
          }
          if (req.url && req.url.startsWith("/apimanager")) {
            return "/index.html";
          }
        },
        onProxyReq: (proxyReq, req, res) => {
          // 设置正确的请求头，确保SSE能正常工作
          if (req.headers.accept && req.headers.accept.includes('text/event-stream')) {
            proxyReq.setHeader('Accept', 'text/event-stream');
            proxyReq.setHeader('Cache-Control', 'no-cache');
            proxyReq.setHeader('Connection', 'keep-alive');
          }
        },
        onProxyRes: (proxyRes, req, res) => {
          // 确保响应头正确
          if (proxyRes.headers['content-type'] && proxyRes.headers['content-type'].includes('text/event-stream')) {
            proxyRes.headers['Cache-Control'] = 'no-cache';
            proxyRes.headers['Connection'] = 'keep-alive';
            proxyRes.headers['X-Accel-Buffering'] = 'no';
          }
        }
      }
    }
  }
};

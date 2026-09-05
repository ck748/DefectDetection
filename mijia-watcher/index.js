const chokidar = require('chokidar');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');
const config = require('./config');

// 本地防重处理集合（已成功上传的文件不再重复触发）
const processedFiles = new Set();

/**
 * 判断是否为目标图片文件
 */
function isImageFile(filename) {
  if (!filename) return false;
  const ext = path.extname(filename).toLowerCase();
  return config.imageExtensions.includes(ext);
}

/**
 * 执行 HTTP POST 将本地图片上传给服务器存储目录并入库
 */
async function uploadImageToServer(filePath, retryCount = 0) {
  const filename = path.basename(filePath);

  try {
    if (!fs.existsSync(filePath)) {
      console.warn(`⚠️ 文件不存在或已被移动: ${filePath}`);
      return;
    }

    const stats = fs.statSync(filePath);
    if (stats.size <= 0) {
      console.warn(`⚠️ 文件大小为 0，跳过: ${filename}`);
      return;
    }

    console.log(`📤 [${new Date().toLocaleTimeString()}] 正在上传米家抓拍图片: ${filename} (${(stats.size / 1024).toFixed(1)} KB)`);

    const formData = new FormData();
    formData.append('image', fs.createReadStream(filePath), {
      filename: filename,
      contentType: 'image/' + path.extname(filename).slice(1).toLowerCase()
    });
    formData.append('fileName', filename);

    const response = await axios.post(config.serverUploadUrl, formData, {
      headers: { ...formData.getHeaders() },
      timeout: 30000,
      maxContentLength: Infinity,
      maxBodyLength: Infinity
    });

    if (response.data && (response.data.code === 200 || response.data.code === '200' || response.data.code === 1)) {
      processedFiles.add(filename);
      console.log(`✅ [上传成功] 图片已存盘至服务器当前目录并入库: ${filename}`);
      console.log(`🎉 [全链路完成] 前端监控界面已实时呈现该画面！\n`);
    } else {
      throw new Error(response.data ? response.data.message || response.data.msg : '未知服务器响应');
    }
  } catch (error) {
    console.error(`❌ [上传失败] ${filename} 错误: ${error.message}`);

    if (retryCount < config.maxRetries) {
      const nextRetry = retryCount + 1;
      console.log(`🔄 [${new Date().toLocaleTimeString()}] 等待 ${config.retryDelay / 1000} 秒后进行第 ${nextRetry}/${config.maxRetries} 次重试...`);
      await new Promise(resolve => setTimeout(resolve, config.retryDelay));
      return uploadImageToServer(filePath, nextRetry);
    } else {
      console.error(`💥 [终止] ${filename} 已达最大重试次数 (${config.maxRetries})，取消上传。\n`);
    }
  }
}

/**
 * 处理检测到的新文件
 */
async function handleNewFile(filePath) {
  const filename = path.basename(filePath);

  // 1. 忽略隐藏文件或已处理文件
  if (filename.startsWith('.') || processedFiles.has(filename)) {
    return;
  }

  // 2. 后缀校验
  if (!isImageFile(filename)) {
    return;
  }

  console.log(`\n🔔 [${new Date().toLocaleTimeString()}] Windows D盘根目录捕获到新照片: ${filename}`);

  // 3. 额外等待 300ms 保证第三方软件写入释放文件锁
  await new Promise(resolve => setTimeout(resolve, 300));

  // 4. 发起自动同步上传
  await uploadImageToServer(filePath);
}

/**
 * 启动本地监听守护进程
 */
function startWatcher() {
  console.log('=====================================================');
  console.log('🚀 米家APP Windows本地照片监听 & 自动同步上传守护进程');
  console.log('=====================================================');
  console.log(`📁 本地监听目标: ${config.watchFolder} (深度: 仅根目录单层)`);
  console.log(`🌐 服务器同步端点: ${config.serverUploadUrl}`);
  console.log(`🖼️  监听图片格式: ${config.imageExtensions.join(', ')}`);
  console.log('=====================================================\n');

  if (!fs.existsSync(config.watchFolder)) {
    console.error(`❌ 本地监听目录不存在: ${config.watchFolder}`);
    process.exit(1);
  }

  // 创建监听器（depth: 0 仅监听 D:\ 根目录下产生的文件，避免遍历整个 D 盘所有子目录）
  const watcher = chokidar.watch(config.watchFolder, {
    ignored: /(^[/\\])\../, // 忽略隐藏文件
    persistent: true,
    ignoreInitial: true,    // 仅监听新增，不重复推送历史存量
    depth: config.depth !== undefined ? config.depth : 0,
    awaitWriteFinish: {
      stabilityThreshold: config.stabilityThreshold || 1000,
      pollInterval: config.pollInterval || 100
    }
  });

  watcher
    .on('add', filePath => {
      handleNewFile(filePath);
    })
    .on('error', error => {
      console.error(`❌ 本地文件监听异常:`, error);
    })
    .on('ready', () => {
      console.log(`👀 本地监听已就绪！正在实时守候 D:\\ 根目录...\n📸 米家APP拍照存入 D:\\ 即可全自动流转！\n`);
    });

  // 安全退出信号处理
  process.on('SIGINT', () => {
    console.log('\n🛑 正在停止本地监听进程...');
    watcher.close().then(() => {
      console.log('✅ 本地监听已安全退出');
      process.exit(0);
    });
  });
}

// 启动
startWatcher();

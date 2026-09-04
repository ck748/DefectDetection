/**
 * Image Watcher Service
 * 自动监听目录中的新增图片文件并上传到后端检测接口
 */

const chokidar = require('chokidar');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');
const config = require('./config');

// 统计信息
const stats = {
  processed: 0,
  success: 0,
  failed: 0
};

// 正在处理的文件集合
const processingFiles = new Set();

/**
 * 格式化时间戳
 */
function getTimestamp() {
  const now = new Date();
  return now.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
}

/**
 * 日志输出
 */
function log(message, emoji = 'ℹ️') {
  console.log(`[${getTimestamp()}] ${emoji} ${message}`);
}

/**
 * 检查文件是否为图片格式
 */
function isImageFile(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  return config.imageExtensions.includes(ext);
}

/**
 * 确保目录存在
 */
function ensureDir(dir) {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
    log(`创建目录: ${dir}`, '📁');
  }
}

/**
 * 上传图片到后端
 */
async function uploadImage(filePath) {
  const fileName = path.basename(filePath);
  
  try {
    log(`正在上传: ${fileName}`, '📤');
    const startTime = Date.now();
    
    // 读取文件
    const fileStream = fs.createReadStream(filePath);
    
    // 创建FormData
    const formData = new FormData();
    formData.append('img', fileStream, fileName);
    
    // 发送POST请求
    const response = await axios.post(config.uploadUrl, formData, {
      headers: formData.getHeaders(),
      timeout: config.uploadTimeout
    });
    
    const duration = ((Date.now() - startTime) / 1000).toFixed(2);
    log(`上传成功: ${fileName} (耗时: ${duration}s)`, '✅');
    
    return { success: true, response };
  } catch (error) {
    const errorMsg = error.response 
      ? `HTTP ${error.response.status}: ${error.response.statusText}`
      : error.message;
    
    log(`上传失败: ${fileName}`, '❌');
    log(`错误详情: ${errorMsg}`, '  ');
    
    return { success: false, error: errorMsg };
  }
}

/**
 * 移动文件到目标目录
 */
function moveFile(sourcePath, targetDir) {
  const fileName = path.basename(sourcePath);
  
  // 首先检查源文件是否存在
  if (!fs.existsSync(sourcePath)) {
    log(`源文件不存在，无法移动: ${fileName}`, '⚠️');
    return false;
  }
  
  let targetPath = path.join(targetDir, fileName);
  
  // 处理文件名冲突
  if (fs.existsSync(targetPath)) {
    const ext = path.extname(fileName);
    const nameWithoutExt = path.basename(fileName, ext);
    const timestamp = Date.now();
    const newFileName = `${nameWithoutExt}_${timestamp}${ext}`;
    targetPath = path.join(targetDir, newFileName);
    log(`文件名冲突，重命名为: ${newFileName}`, '⚠️');
  }
  
  try {
    fs.renameSync(sourcePath, targetPath);
    const relPath = path.relative(path.join(__dirname, '..'), targetPath);
    log(`已移动到: ${relPath}`, '📦');
    return true;
  } catch (error) {
    // 如果重命名失败（可能跨文件系统），尝试复制+删除
    try {
      // 再次检查源文件是否存在
      if (!fs.existsSync(sourcePath)) {
        log(`源文件已不存在，无法复制: ${fileName}`, '⚠️');
        return false;
      }
      
      fs.copyFileSync(sourcePath, targetPath);
      fs.unlinkSync(sourcePath);
      const relPath = path.relative(path.join(__dirname, '..'), targetPath);
      log(`已移动到: ${relPath}`, '📦');
      return true;
    } catch (copyError) {
      log(`移动文件失败: ${copyError.message}`, '❌');
      return false;
    }
  }
}

/**
 * 处理新增的图片文件
 */
async function processImageFile(filePath) {
  const fileName = path.basename(filePath);
  
  // 避免重复处理
  if (processingFiles.has(filePath)) {
    return;
  }
  
  processingFiles.add(filePath);
  stats.processed++;
  
  try {
    // 等待文件写入完成
    await new Promise(resolve => setTimeout(resolve, config.fileStableDelay));
    
    // 检查文件是否仍然存在
    if (!fs.existsSync(filePath)) {
      log(`文件已不存在（可能已被其他程序处理）: ${fileName}`, '⚠️');
      processingFiles.delete(filePath);
      return;
    }
    
    // 上传图片
    const result = await uploadImage(filePath);
    
    // 再次检查文件是否存在（上传过程中可能被删除）
    if (!fs.existsSync(filePath)) {
      log(`文件已不存在（上传后消失）: ${fileName}`, '⚠️');
      processingFiles.delete(filePath);
      // 虽然文件不存在，但上传成功了，计入成功数
      if (result.success) {
        stats.success++;
      }
      return;
    }
    
    // 移动文件
    const targetDir = result.success 
      ? path.resolve(__dirname, config.processedDir)
      : path.resolve(__dirname, config.failedDir);
    
    const moved = moveFile(filePath, targetDir);
    
    if (moved) {
      if (result.success) {
        stats.success++;
      } else {
        stats.failed++;
      }
    } else {
      // 移动失败但上传成功，仍然计入成功
      if (result.success) {
        stats.success++;
        log(`上传成功但文件移动失败（文件可能已被删除）: ${fileName}`, 'ℹ️');
      } else {
        stats.failed++;
      }
    }
    
  } catch (error) {
    log(`处理文件时出错: ${fileName} - ${error.message}`, '❌');
    stats.failed++;
  } finally {
    processingFiles.delete(filePath);
  }
}

/**
 * 健康检查：测试后端连接
 */
async function healthCheck() {
  try {
    // 尝试访问实际的上传接口（使用HEAD请求）
    await axios.head(config.uploadUrl, { timeout: 5000 });
    log('后端服务连接正常', '✅');
    return true;
  } catch (error) {
    // 如果是HEAD不支持，尝试OPTIONS
    try {
      await axios.options(config.uploadUrl, { timeout: 5000 });
      log('后端服务连接正常', '✅');
      return true;
    } catch (error2) {
      log('后端服务暂时不可用，将在文件到达时重试', '⚠️');
      return false;
    }
  }
}

/**
 * 启动服务
 */
async function start() {
  console.log('\n🚀 图片监听服务已启动\n');
  
  // 解析监听目录的绝对路径
  const watchPath = path.resolve(__dirname, config.watchDir);
  const processedPath = path.resolve(__dirname, config.processedDir);
  const failedPath = path.resolve(__dirname, config.failedDir);
  
  // 确保必要的目录存在
  ensureDir(watchPath);
  ensureDir(processedPath);
  ensureDir(failedPath);
  
  // 显示配置信息
  log(`监听目录: ${watchPath}`, '📁');
  log(`上传接口: ${config.uploadUrl}`, '🔗');
  log(`支持格式: ${config.imageExtensions.join(', ')}`, '📷');
  log('后端服务将在文件上传时自动连接', 'ℹ️');
  console.log('');
  
  // 创建文件监听器
  const watcher = chokidar.watch(watchPath, {
    ignored: [
      /(^|[\/\\])\../, // 忽略隐藏文件
      '**/processed/**', // 忽略processed子目录
      '**/failed/**'     // 忽略failed子目录
    ],
    persistent: true,
    ignoreInitial: true, // 忽略初始文件，只监听新增
    awaitWriteFinish: {
      stabilityThreshold: config.fileStableDelay,
      pollInterval: 100
    }
  });
  
  // 监听文件添加事件
  watcher.on('add', (filePath) => {
    // 只处理根目录下的文件，忽略子目录中的文件
    const relativePath = path.relative(watchPath, filePath);
    const isInSubDir = relativePath.includes(path.sep);
    
    if (!isInSubDir && isImageFile(filePath)) {
      const fileName = path.basename(filePath);
      log(`检测到新图片: ${fileName}`, '🖼️');
      processImageFile(filePath);
    }
  });
  
  // 监听器错误
  watcher.on('error', (error) => {
    log(`监听器错误: ${error.message}`, '❌');
  });
  
  log('等待新图片...', '⏳');
  
  // 优雅退出
  setupGracefulShutdown(watcher);
}

/**
 * 设置优雅退出
 */
function setupGracefulShutdown(watcher) {
  let isShuttingDown = false;
  
  const shutdown = async (signal) => {
    if (isShuttingDown) return;
    isShuttingDown = true;
    
    console.log('\n');
    log(`接收到${signal}信号，正在停止服务...`, '⏹️');
    
    // 等待正在处理的文件完成（最多10秒）
    if (processingFiles.size > 0) {
      log(`等待${processingFiles.size}个文件处理完成...`, '⏳');
      const maxWait = 10000;
      const startWait = Date.now();
      
      while (processingFiles.size > 0 && (Date.now() - startWait) < maxWait) {
        await new Promise(resolve => setTimeout(resolve, 500));
      }
      
      if (processingFiles.size > 0) {
        log('强制退出，部分文件可能未完整处理', '⚠️');
      }
    }
    
    // 关闭监听器
    await watcher.close();
    
    // 输出统计信息
    console.log('');
    log('服务已停止', '✅');
    log(`统计: 处理 ${stats.processed} 个文件, 成功 ${stats.success}, 失败 ${stats.failed}`, '📊');
    console.log('');
    
    process.exit(0);
  };
  
  process.on('SIGINT', () => shutdown('SIGINT'));
  process.on('SIGTERM', () => shutdown('SIGTERM'));
}

// 启动服务
start().catch((error) => {
  console.error('服务启动失败:', error);
  process.exit(1);
});

/**
 * PC端摄像头文件夹监听上传脚本
 * 功能: 监听本地摄像头文件夹,发现新图片后自动上传到服务器
 */

const chokidar = require('chokidar');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// ==================== 配置区 ====================
const CONFIG = {
  // PC本地监听的文件夹路径 (摄像头保存图片的位置)
  watchFolder: 'C:/Users/LENOVO/Desktop/v2/cmzj-main/cmzj-main/defectDetection/uploads/camera',
  
  // 服务器地址 (根据实际服务器IP修改)
  serverUrl: 'http://192.168.1.3:8081/detect/img',  // 正确的API接口
  
  // 支持的图片格式
  imageExtensions: ['.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp'],
  
  // 上传失败后的重试次数
  maxRetries: 3,
  
  // 重试间隔(毫秒)
  retryDelay: 2000
};
// ===============================================

// 已处理文件集合(避免重复上传)
const processedFiles = new Set();

/**
 * 判断是否为图片文件
 */
function isImageFile(filename) {
  const ext = path.extname(filename).toLowerCase();
  return CONFIG.imageExtensions.includes(ext);
}

/**
 * 上传图片到服务器
 */
async function uploadImage(filePath, retryCount = 0) {
  const filename = path.basename(filePath);
  
  try {
    console.log(`📤 [${new Date().toLocaleTimeString()}] 正在上传: ${filename}`);
    
    // 检查文件是否存在
    if (!fs.existsSync(filePath)) {
      console.error(`❌ 文件不存在: ${filePath}`);
      return;
    }
    
    // 创建表单数据
    const formData = new FormData();
    formData.append('img', fs.createReadStream(filePath), {
      filename: filename,
      contentType: 'image/' + path.extname(filename).slice(1)
    });
    
    // 发送到服务器
    const response = await axios.post(CONFIG.serverUrl, formData, {
      headers: {
        ...formData.getHeaders()
      },
      timeout: 30000,  // 30秒超时
      maxContentLength: Infinity,
      maxBodyLength: Infinity
    });
    
    // 检查响应
    if (response.data && response.data.code === 200) {
      console.log(`✅ [${new Date().toLocaleTimeString()}] 上传成功: ${filename}`);
      console.log(`   服务器响应: ${response.data.msg || '检测完成'}`);
      
      // 标记为已处理
      processedFiles.add(filename);
    } else {
      console.error(`❌ 上传失败: ${filename}, 服务器返回:`, response.data);
    }
    
  } catch (error) {
    console.error(`❌ 上传出错: ${filename}`);
    
    if (error.response) {
      // 服务器返回错误
      console.error(`   HTTP状态: ${error.response.status}`);
      console.error(`   错误信息: ${error.response.data?.msg || error.message}`);
    } else if (error.request) {
      // 请求发送失败
      console.error(`   网络错误: 无法连接到服务器 ${CONFIG.serverUrl}`);
    } else {
      console.error(`   错误: ${error.message}`);
    }
    
    // 重试逻辑
    if (retryCount < CONFIG.maxRetries) {
      console.log(`🔄 [${new Date().toLocaleTimeString()}] ${CONFIG.retryDelay/1000}秒后重试 (${retryCount + 1}/${CONFIG.maxRetries})...`);
      await new Promise(resolve => setTimeout(resolve, CONFIG.retryDelay));
      return uploadImage(filePath, retryCount + 1);
    } else {
      console.error(`❌ 上传失败,已达最大重试次数: ${filename}`);
      // 可以选择移动到失败文件夹
      // moveToFailedFolder(filePath);
    }
  }
}

/**
 * 处理新文件
 */
async function handleNewFile(filePath) {
  const filename = path.basename(filePath);
  
  // 避免重复处理
  if (processedFiles.has(filename)) {
    return;
  }
  
  // 检查是否为图片
  if (!isImageFile(filename)) {
    console.log(`⏭️  跳过非图片文件: ${filename}`);
    return;
  }
  
  // 等待文件写入完成(某些摄像头软件会分块写入)
  await new Promise(resolve => setTimeout(resolve, 500));
  
  // 上传到服务器
  await uploadImage(filePath);
}

/**
 * 启动文件监听
 */
function startWatcher() {
  console.log('🚀 ==========================================');
  console.log('🚀 PC端摄像头文件夹监听服务已启动');
  console.log('🚀 ==========================================');
  console.log(`📁 监听文件夹: ${CONFIG.watchFolder}`);
  console.log(`🌐 服务器地址: ${CONFIG.serverUrl}`);
  console.log(`📷 支持格式: ${CONFIG.imageExtensions.join(', ')}`);
  console.log('🚀 ==========================================\n');
  
  // 检查监听文件夹是否存在
  if (!fs.existsSync(CONFIG.watchFolder)) {
    console.error(`❌ 监听文件夹不存在: ${CONFIG.watchFolder}`);
    console.error(`请在脚本开头的CONFIG.watchFolder中配置正确的路径`);
    process.exit(1);
  }
  
  // 创建监听器
  const watcher = chokidar.watch(CONFIG.watchFolder, {
    ignored: /(^|[\/\\])\../, // 忽略隐藏文件
    persistent: true,
    ignoreInitial: true,  // 忽略初始文件,只监听新增
    awaitWriteFinish: {   // 等待文件写入完成
      stabilityThreshold: 2000,
      pollInterval: 100
    }
  });
  
  // 监听新文件
  watcher
    .on('add', filePath => {
      console.log(`\n🔔 [${new Date().toLocaleTimeString()}] 检测到新文件: ${path.basename(filePath)}`);
      handleNewFile(filePath);
    })
    .on('error', error => {
      console.error(`❌ 监听器错误:`, error);
    })
    .on('ready', () => {
      console.log(`👀 正在监听文件夹,等待新图片...\n`);
    });
  
  // 优雅退出
  process.on('SIGINT', () => {
    console.log('\n\n🛑 收到退出信号,正在关闭监听器...');
    watcher.close();
    console.log('✅ 监听器已关闭');
    process.exit(0);
  });
}

// 启动服务
startWatcher();

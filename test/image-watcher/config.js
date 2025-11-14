/**
 * Image Watcher 配置文件
 */

module.exports = {
  // 监听目录（相对于image-watcher目录）
  watchDir: './watchDir',
  
  // 后端上传接口URL
  uploadUrl: 'http://localhost:8081/detect/img',
  
  // 支持的图片格式（扩展名，小写）
  imageExtensions: ['.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp'],
  
  // 处理后文件的目标目录
  processedDir: './watchDir/processed',
  failedDir: './watchDir/failed',
  
  // 上传超时时间（毫秒）
  uploadTimeout: 30000,
  
  // 文件稳定等待时间（毫秒）- 避免文件正在写入时就开始上传
  fileStableDelay: 500
};

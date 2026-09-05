/**
 * 米家本地拍照文件夹监听客户端配置文件
 */
const path = require('path');

module.exports = {
  // 1. Windows 本地米家拍照存放目录（直接写死为 D 盘根目录）
  // 监听 D 盘根目录下生成的所有抓拍图片
  watchFolder: 'D:\\',

  // 是否仅监听根目录单层（避免深度递归扫描 D 盘所有子文件夹损耗性能，true = 仅根目录）
  depth: 0,

  // 2. 远程质检服务器上传接口（默认 192.168.1.3:8081）
  serverUploadUrl: 'http://192.168.1.3:8081/cameraWatch/upload',

  // 备用本地开发调试上传接口（本地联调时可切为 8081）
  // serverUploadUrl: 'http://127.0.0.1:8081/cameraWatch/upload',

  // 3. 支持的图片文件后缀格式
  imageExtensions: ['.jpg', '.jpeg', '.png', '.bmp'],

  // 4. 防抖等待文件写入完成阈值（米家相机写入较大图片时通常为流式分块写入，需等待文件写完）
  stabilityThreshold: 1000,
  pollInterval: 100,

  // 5. 失败重试次数与间隔(ms)
  maxRetries: 3,
  retryDelay: 1500
};

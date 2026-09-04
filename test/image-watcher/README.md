# Image Watcher Service

自动监听 `watchDir` 目录中的新增图片文件，并将其上传到后端检测接口。

## 功能特性

- ✅ 自动监听目录中的图片文件变化
- ✅ 支持多种图片格式：jpg, jpeg, png, bmp, gif, webp
- ✅ 自动上传到后端检测接口
- ✅ 成功/失败文件分类管理
- ✅ 清晰的日志输出（带emoji图标）
- ✅ 优雅退出（Ctrl+C）
- ✅ 统计信息（处理文件数、成功率）

## 快速开始

### 1. 安装依赖

```bash
cd test/image-watcher
npm install
```

### 2. 启动服务

```bash
npm start
```

或直接运行：

```bash
node index.js
```

### 3. 使用服务

启动后，将图片文件复制到 `/watchDir` 目录：

```bash
# 在另一个终端窗口
cp your-image.jpg /watchDir/
```

服务会自动：
1. 检测新图片文件
2. 上传到后端 `http://localhost:8081/detect/img`
3. 成功后移动到 `watchDir/processed/`
4. 失败则移动到 `watchDir/failed/`

## 配置说明

编辑 `config.js` 可修改以下配置：

```javascript
module.exports = {
  // 监听目录
  watchDir: './watchDir',
  
  // 后端上传接口URL
  uploadUrl: 'http://localhost:8081/detect/img',
  
  // 支持的图片格式
  imageExtensions: ['.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp'],
  
  // 上传超时时间（毫秒）
  uploadTimeout: 30000
};
```

## 日志示例

```
🚀 图片监听服务已启动

[2025-11-03 22:30:00] 📁 监听目录: C:\...\watchDir
[2025-11-03 22:30:00] 🔗 上传接口: http://localhost:8081/detect/img
[2025-11-03 22:30:00] 📷 支持格式: .jpg, .jpeg, .png, .bmp, .gif, .webp

[2025-11-03 22:30:00] ✅ 后端服务连接正常

[2025-11-03 22:30:00] ⏳ 等待新图片...
[2025-11-03 22:30:15] 🖼️  检测到新图片: sample.jpg
[2025-11-03 22:30:16] 📤 正在上传: sample.jpg
[2025-11-03 22:30:17] ✅ 上传成功: sample.jpg (耗时: 1.2s)
[2025-11-03 22:30:17] 📦 已移动到: watchDir/processed/sample.jpg
```

## 停止服务

按 `Ctrl+C` 优雅退出：

```
[2025-11-03 22:35:00] ⏹️  接收到SIGINT信号，正在停止服务...
[2025-11-03 22:35:01] ✅ 服务已停止
[2025-11-03 22:35:01] 📊 统计: 处理 10 个文件, 成功 9, 失败 1
```

## 目录结构

```
test/
├── image-watcher/
│   ├── node_modules/      # 依赖包（npm install后生成）
│   ├── config.js          # 配置文件
│   ├── index.js           # 主程序
│   ├── package.json       # 项目配置
│   ├── .gitignore         # Git忽略文件
│   └── README.md          # 本文档
├── watchDir/               # 监听目录（放入测试图片）
│   ├── processed/         # 成功上传的图片
│   └── failed/            # 上传失败的图片
```

## 故障排查

### Q1: 上传失败 "connect ECONNREFUSED"
**A:** 后端服务未启动或端口不正确
- 确保后端服务运行在 `http://localhost:8081`
- 检查 `config.js` 中的 `uploadUrl` 配置

### Q2: 文件没有被检测到
**A:** 
- 确认文件扩展名在支持列表中
- 检查文件是否放入了正确的监听目录
- 查看控制台是否有错误日志

### Q3: 文件移动失败
**A:** 
- 检查文件权限
- 确保 `processed/` 和 `failed/` 目录存在
- 服务会自动创建这些目录

### Q4: 服务无法启动
**A:** 
- 确保已运行 `npm install` 安装依赖
- 检查 Node.js 版本（需要 18.x+）
- 查看错误信息

## 依赖包

- `chokidar` - 跨平台文件监听
- `axios` - HTTP客户端
- `form-data` - 文件上传

## 开发说明

### 修改配置
直接编辑 `config.js` 文件，重启服务生效。

### 调试
在 `index.js` 中添加更多日志输出，或使用 Node.js 调试工具：

```bash
node --inspect index.js
```


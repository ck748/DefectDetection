# 摄像头上传接口测试工具

测试 `/annotation/upload/camera/auto` 接口

## 📋 测试接口信息

- **接口路径**: `POST /annotation/upload/camera/auto`
- **参数**:
  - `image` (MultipartFile): 图片文件
  - `deviceId` (Integer, 可选): 设备ID，默认为0
- **返回**:
  ```json
  {
    "code": 1,
    "msg": "上传成功",
    "data": 123  // 图片ID
  }
  ```

### 1. 安装依赖
```bash
cd test
npm install
```

### 2. 准备测试图片

**方法一：手动准备**
将任意图片重命名为 `test-image.jpg`，放在 `test` 目录下。

### 3. 运行测试

确保后端服务已启动（localhost:8081），然后：

```bash
cd test
node test-camera-upload.js single

# 或使用npm脚本
npm test
```
### 单次测试
```bash
node test-camera-upload.js single
# 或： npm test
```
上传一次测试图片，查看详细响应信息。

### 批量测试（模拟连续拍照）
```bash
# 默认: 5次，间隔2秒
node test-camera-upload.js batch
# 或： npm run test:batch

# 自定义: 10次，间隔1秒
node test-camera-upload.js batch 10 1000
```

### 性能测试
```bash
node test-camera-upload.js performance
# 或： npm run test:performance
```
连续上传10次，统计平均耗时、最快、最慢时间。

## 🔧 配置修改

修改 `test-camera-upload.js` 中的配置：

```javascript
const config = {
  baseUrl: 'http://localhost:8081',  // 后端地址
  endpoint: '/annotation/upload/camera/auto',  // 接口路径
  deviceId: 1,  // 默认设备ID
  testImagePath: path.join(__dirname, 'test-image.jpg')  // 测试图片路径
};
```

## 📁 生成的文件

测试成功后会生成：
- **数据库记录**: `raw_image` 表中新增记录
- **图片文件**: `uploads/camera/camera_{deviceId}_{timestamp}.jpg`
- **后端日志**: 包含上传成功的日志信息


/**
 * 摄像头上传接口测试脚本
 * 测试 POST /annotation/upload/camera/auto 接口
 */

const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// 配置
const config = {
  baseUrl: 'http://localhost:8081',
  endpoint: '/annotation/upload/camera/auto',
  deviceId: 1,
  // 测试图片路径（请修改为实际存在的图片路径）
  testImagePath: path.join(__dirname, 'test-image.jpg')
};

/**
 * 创建测试图片（如果不存在）
 */
function createTestImage() {
  if (!fs.existsSync(config.testImagePath)) {
    console.log('⚠️  测试图片不存在，请将一张图片命名为 test-image.jpg 放在 test 目录下');
    console.log(`   期望路径: ${config.testImagePath}`);
    return false;
  }
  return true;
}

/**
 * 测试摄像头上传接口
 */
async function testCameraUpload() {
  console.log('\n🚀 开始测试摄像头上传接口\n');
  console.log('配置信息:');
  console.log(`  - 接口地址: ${config.baseUrl}${config.endpoint}`);
  console.log(`  - 设备ID: ${config.deviceId}`);
  console.log(`  - 测试图片: ${config.testImagePath}`);
  console.log('');

  // 检查测试图片
  if (!createTestImage()) {
    return;
  }

  try {
    // 准备FormData
    const formData = new FormData();
    const imageStream = fs.createReadStream(config.testImagePath);
    formData.append('image', imageStream, path.basename(config.testImagePath));

    const url = `${config.baseUrl}${config.endpoint}?deviceId=${config.deviceId}`;
    
    console.log('📤 正在上传图片...');
    const startTime = Date.now();

    // 发送请求
    const response = await axios.post(url, formData, {
      headers: {
        ...formData.getHeaders()
      },
      timeout: 30000 // 30秒超时
    });

    const duration = ((Date.now() - startTime) / 1000).toFixed(2);

    // 输出结果
    console.log('\n✅ 上传成功!');
    console.log(`⏱️  耗时: ${duration}秒`);
    console.log('\n📋 响应数据:');
    console.log(JSON.stringify(response.data, null, 2));

    // 验证响应（兼容两种格式）
    const isSuccess = response.data.code === 1 || response.data.code === 200;
    const imageId = response.data.data;
    const message = response.data.msg || response.data.message;
    
    if (isSuccess && imageId) {
      console.log('\n✅ 接口测试通过');
      console.log(`   - 图片ID: ${imageId}`);
      console.log(`   - 状态: ${message}`);
    } else {
      console.log('\n⚠️  接口返回失败');
      console.log(`   - 错误信息: ${message}`);
    }

  } catch (error) {
    console.log('\n❌ 测试失败');
    
    if (error.response) {
      // 服务器返回错误
      console.log(`   - HTTP状态码: ${error.response.status}`);
      console.log(`   - 错误信息: ${JSON.stringify(error.response.data, null, 2)}`);
    } else if (error.request) {
      // 请求发出但没有响应
      console.log('   - 错误: 服务器无响应');
      console.log('   - 请检查后端服务是否启动 (http://localhost:8081)');
    } else {
      // 其他错误
      console.log(`   - 错误: ${error.message}`);
    }
  }
}

/**
 * 批量测试（模拟连续拍照）
 */
async function batchTest(count = 5, interval = 2000) {
  console.log(`\n🔁 批量测试: 将上传 ${count} 次，间隔 ${interval}ms\n`);
  
  if (!createTestImage()) {
    return;
  }

  for (let i = 1; i <= count; i++) {
    console.log(`\n--- 第 ${i}/${count} 次上传 ---`);
    await testCameraUpload();
    
    if (i < count) {
      console.log(`\n⏳ 等待 ${interval}ms...\n`);
      await new Promise(resolve => setTimeout(resolve, interval));
    }
  }
  
  console.log('\n✅ 批量测试完成\n');
}

/**
 * 性能测试
 */
async function performanceTest() {
  console.log('\n⚡ 性能测试: 测试上传速度\n');
  
  if (!createTestImage()) {
    return;
  }

  const testCount = 10;
  const results = [];

  for (let i = 1; i <= testCount; i++) {
    try {
      const formData = new FormData();
      const imageStream = fs.createReadStream(config.testImagePath);
      formData.append('image', imageStream, path.basename(config.testImagePath));

      const url = `${config.baseUrl}${config.endpoint}?deviceId=${config.deviceId}`;
      const startTime = Date.now();

      const response = await axios.post(url, formData, {
        headers: { ...formData.getHeaders() },
        timeout: 30000
      });

      const duration = Date.now() - startTime;
      results.push(duration);
      
      console.log(`✓ 第${i}次: ${duration}ms`);
    } catch (error) {
      console.log(`✗ 第${i}次: 失败`);
    }
  }

  if (results.length > 0) {
    const avg = (results.reduce((a, b) => a + b, 0) / results.length).toFixed(2);
    const min = Math.min(...results);
    const max = Math.max(...results);

    console.log('\n📊 性能统计:');
    console.log(`   - 成功次数: ${results.length}/${testCount}`);
    console.log(`   - 平均耗时: ${avg}ms`);
    console.log(`   - 最快: ${min}ms`);
    console.log(`   - 最慢: ${max}ms`);
  }
}

// 主函数
async function main() {
  const args = process.argv.slice(2);
  const command = args[0] || 'single';

  console.log('');
  console.log('═══════════════════════════════════════');
  console.log('  摄像头上传接口测试工具');
  console.log('═══════════════════════════════════════');

  switch (command) {
    case 'single':
      await testCameraUpload();
      break;
    
    case 'batch':
      const count = parseInt(args[1]) || 5;
      const interval = parseInt(args[2]) || 2000;
      await batchTest(count, interval);
      break;
    
    case 'performance':
      await performanceTest();
      break;
    
    default:
      console.log('\n使用方法:');
      console.log('  node test-camera-upload.js single           # 单次测试');
      console.log('  node test-camera-upload.js batch [次数] [间隔ms]  # 批量测试');
      console.log('  node test-camera-upload.js performance      # 性能测试');
  }

  console.log('\n═══════════════════════════════════════\n');
}

// 运行
main().catch(error => {
  console.error('程序错误:', error);
  process.exit(1);
});

#!/bin/bash
# Ollama AI 服务诊断脚本

echo "======================================"
echo "🔍 Ollama AI 服务诊断"
echo "======================================"
echo ""

# 1. 检查 Ollama 进程
echo "1️⃣ 检查 Ollama 进程状态:"
if ps aux | grep -v grep | grep ollama > /dev/null; then
    echo "✅ Ollama 进程运行中"
    ps aux | grep -v grep | grep ollama
else
    echo "❌ Ollama 进程未运行"
    echo "   解决方案: 执行 'ollama serve &' 启动服务"
fi
echo ""

# 2. 检查端口监听
echo "2️⃣ 检查端口 11434 监听状态:"
if netstat -tulnp 2>/dev/null | grep 11434 > /dev/null || ss -tulnp 2>/dev/null | grep 11434 > /dev/null; then
    echo "✅ 端口 11434 正在监听"
    netstat -tulnp 2>/dev/null | grep 11434 || ss -tulnp 2>/dev/null | grep 11434
else
    echo "❌ 端口 11434 未监听"
    echo "   解决方案: 确保 Ollama 服务正常运行"
fi
echo ""

# 3. 测试 API 连接
echo "3️⃣ 测试 Ollama API 连接:"
if curl -s --connect-timeout 5 http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "✅ API 连接成功"
    echo "   已安装的模型列表:"
    curl -s http://localhost:11434/api/tags | python3 -m json.tool 2>/dev/null || curl -s http://localhost:11434/api/tags
else
    echo "❌ API 连接失败"
    echo "   解决方案: 检查 Ollama 是否正确启动"
fi
echo ""

# 4. 检查模型是否存在
echo "4️⃣ 检查模型 qwen2.5:7b 是否存在:"
if curl -s http://localhost:11434/api/tags 2>/dev/null | grep -q "qwen2.5:7b"; then
    echo "✅ 模型 qwen2.5:7b 已安装"
else
    echo "❌ 模型 qwen2.5:7b 未找到"
    echo "   解决方案: 执行 'ollama pull qwen2.5:7b' 下载模型"
    echo "   当前已安装模型:"
    curl -s http://localhost:11434/api/tags 2>/dev/null | grep '"name"' || echo "   无模型"
fi
echo ""

# 5. 检查应用配置
echo "5️⃣ 检查应用 AI 配置:"
if [ -f /root/desc/cmzj-main/defectDetection/src/main/resources/application.yaml ]; then
    echo "✅ 配置文件存在"
    echo "   当前 AI 配置:"
    cat /root/desc/cmzj-main/defectDetection/src/main/resources/application.yaml | grep -A 8 "^ai:"
else
    echo "❌ 配置文件不存在"
fi
echo ""

# 6. 测试完整的聊天接口
echo "6️⃣ 测试聊天接口:"
RESPONSE=$(curl -s --connect-timeout 10 -X POST http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen2.5:7b",
    "messages": [{"role": "user", "content": "你好"}],
    "stream": false
  }' 2>&1)

if echo "$RESPONSE" | grep -q "content"; then
    echo "✅ 聊天接口测试成功"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
else
    echo "❌ 聊天接口测试失败"
    echo "   错误信息: $RESPONSE"
fi
echo ""

# 7. 检查后端日志
echo "7️⃣ 检查后端日志中的 AI 错误:"
if [ -f /root/desc/cmzj-main/logs/backend.log ]; then
    echo "   最近的 AI 相关日志:"
    tail -50 /root/desc/cmzj-main/logs/backend.log | grep -i "ai\|ollama" | tail -10
else
    echo "❌ 后端日志文件不存在"
fi
echo ""

echo "======================================"
echo "🎯 诊断总结"
echo "======================================"
echo "如果发现问题，请按照上述提示的解决方案处理"
echo "如果所有检查都通过，但仍然失败，请检查:"
echo "  1. 防火墙设置"
echo "  2. SELinux 配置"
echo "  3. 后端应用的完整错误日志"
echo ""

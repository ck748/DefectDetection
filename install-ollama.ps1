# Ollama本地大模型自动部署脚本
# 运行方式: 右键此文件 -> "使用PowerShell运行"
# 或在PowerShell中: .\install-ollama.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   Ollama本地大模型自动部署工具   " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否以管理员权限运行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "⚠️  建议以管理员权限运行此脚本" -ForegroundColor Yellow
    Write-Host "   右键PowerShell -> '以管理员身份运行'" -ForegroundColor Yellow
    Write-Host ""
}

# 步骤1: 检查Ollama是否已安装
Write-Host "📋 步骤1: 检查Ollama安装状态..." -ForegroundColor Green
$ollamaInstalled = $false
try {
    $version = ollama --version 2>$null
    if ($version) {
        Write-Host "✅ Ollama已安装: $version" -ForegroundColor Green
        $ollamaInstalled = $true
    }
} catch {
    Write-Host "❌ Ollama未安装" -ForegroundColor Red
}

# 步骤2: 如果未安装，提供下载链接
if (-not $ollamaInstalled) {
    Write-Host ""
    Write-Host "📥 请按以下步骤安装Ollama:" -ForegroundColor Yellow
    Write-Host "   1. 访问: https://ollama.com/download" -ForegroundColor White
    Write-Host "   2. 下载Windows版本 (OllamaSetup.exe)" -ForegroundColor White
    Write-Host "   3. 双击安装文件完成安装" -ForegroundColor White
    Write-Host "   4. 安装完成后重新运行此脚本" -ForegroundColor White
    Write-Host ""
    
    # 询问是否打开浏览器
    $openBrowser = Read-Host "是否现在打开下载页面? (Y/N)"
    if ($openBrowser -eq "Y" -or $openBrowser -eq "y") {
        Start-Process "https://ollama.com/download"
    }
    
    Write-Host ""
    Write-Host "按任意键退出..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

Write-Host ""

# 步骤3: 检查Ollama服务状态
Write-Host "📋 步骤2: 检查Ollama服务状态..." -ForegroundColor Green
$ollamaRunning = $false
try {
    $response = Invoke-WebRequest -Uri "http://localhost:11434" -TimeoutSec 2 -ErrorAction SilentlyContinue
    $ollamaRunning = $true
    Write-Host "✅ Ollama服务正在运行" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Ollama服务未运行" -ForegroundColor Yellow
    Write-Host "   正在启动Ollama服务..." -ForegroundColor Yellow
    
    # 尝试启动Ollama服务
    Start-Process "ollama" -ArgumentList "serve" -WindowStyle Hidden
    Start-Sleep -Seconds 3
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:11434" -TimeoutSec 2 -ErrorAction SilentlyContinue
        Write-Host "✅ Ollama服务已启动" -ForegroundColor Green
        $ollamaRunning = $true
    } catch {
        Write-Host "❌ 无法启动Ollama服务，请手动运行: ollama serve" -ForegroundColor Red
    }
}

Write-Host ""

# 步骤4: 列出已安装的模型
Write-Host "📋 步骤3: 检查已安装的模型..." -ForegroundColor Green
$modelsList = ollama list 2>$null
Write-Host $modelsList

$hasQwen = $modelsList -match "qwen2.5"
$hasLlama = $modelsList -match "llama"

if (-not $hasQwen -and -not $hasLlama) {
    Write-Host ""
    Write-Host "❌ 未检测到推荐模型" -ForegroundColor Red
    Write-Host ""
    
    # 步骤5: 下载推荐模型
    Write-Host "📥 推荐下载以下模型之一:" -ForegroundColor Yellow
    Write-Host "   1. qwen2.5:7b    (4.7GB, 需要8GB内存, 中文优秀) ⭐推荐" -ForegroundColor White
    Write-Host "   2. qwen2.5:3b    (2GB,   需要4GB内存, 轻量级)" -ForegroundColor White
    Write-Host "   3. qwen2.5:14b   (9GB,   需要16GB内存, 更强大)" -ForegroundColor White
    Write-Host "   4. llama3.1:8b   (4.7GB, 需要8GB内存, Meta模型)" -ForegroundColor White
    Write-Host "   5. 跳过，稍后手动下载" -ForegroundColor White
    Write-Host ""
    
    $choice = Read-Host "请选择 (1-5)"
    
    $modelToDownload = ""
    switch ($choice) {
        "1" { $modelToDownload = "qwen2.5:7b" }
        "2" { $modelToDownload = "qwen2.5:3b" }
        "3" { $modelToDownload = "qwen2.5:14b" }
        "4" { $modelToDownload = "llama3.1:8b" }
        "5" { 
            Write-Host "⏭️  跳过下载，稍后可手动运行: ollama pull qwen2.5:7b" -ForegroundColor Yellow
        }
        default {
            Write-Host "❌ 无效选择，使用默认模型: qwen2.5:7b" -ForegroundColor Yellow
            $modelToDownload = "qwen2.5:7b"
        }
    }
    
    if ($modelToDownload) {
        Write-Host ""
        Write-Host "⏬ 正在下载模型: $modelToDownload ..." -ForegroundColor Cyan
        Write-Host "   这可能需要10-30分钟，请耐心等待..." -ForegroundColor Gray
        Write-Host ""
        
        ollama pull $modelToDownload
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ 模型下载成功!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ 模型下载失败，请检查网络连接" -ForegroundColor Red
            Write-Host "   稍后可手动运行: ollama pull $modelToDownload" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "✅ 检测到已安装的模型" -ForegroundColor Green
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "          配置检查完成            " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 步骤6: 测试模型
Write-Host "📋 是否测试模型? (Y/N)" -ForegroundColor Green
$testModel = Read-Host
if ($testModel -eq "Y" -or $testModel -eq "y") {
    Write-Host ""
    Write-Host "🧪 启动模型测试..." -ForegroundColor Cyan
    Write-Host "   输入 '/bye' 退出测试" -ForegroundColor Gray
    Write-Host ""
    
    # 获取第一个可用模型
    $firstModel = (ollama list | Select-Object -Skip 1 -First 1).Split()[0]
    if ($firstModel) {
        ollama run $firstModel
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "         部署指南总结             " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ 下一步操作:" -ForegroundColor Green
Write-Host "   1. 确保Ollama服务运行: ollama serve" -ForegroundColor White
Write-Host "   2. 启动后端服务: cd defectDetection && mvn spring-boot:run" -ForegroundColor White
Write-Host "   3. 打开前端页面测试AI分析功能" -ForegroundColor White
Write-Host ""
Write-Host "📖 详细文档: 本地大模型部署指南.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔧 常用命令:" -ForegroundColor Yellow
Write-Host "   ollama list              - 查看已安装模型" -ForegroundColor White
Write-Host "   ollama pull qwen2.5:7b   - 下载新模型" -ForegroundColor White
Write-Host "   ollama run qwen2.5:7b    - 运行模型测试" -ForegroundColor White
Write-Host "   ollama rm qwen2.5:7b     - 删除模型" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

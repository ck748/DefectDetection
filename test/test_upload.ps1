# 测试图片上传脚本
# 使用说明：将测试图片放在同目录下，命名为test_image.jpg

$imagePath = "test_image.jpg"
$url = "http://localhost:8081/detect/img"

# 检查文件是否存在
if (-Not (Test-Path $imagePath)) {
    Write-Host "错误: 找不到测试图片 $imagePath" -ForegroundColor Red
    Write-Host "请在当前目录放置一张测试图片并命名为 test_image.jpg" -ForegroundColor Yellow
    exit
}

Write-Host "正在上传图片到检测接口..." -ForegroundColor Green

# 创建multipart/form-data请求
$fileBytes = [System.IO.File]::ReadAllBytes($imagePath)
$fileName = [System.IO.Path]::GetFileName($imagePath)

# 使用 curl 上传（需要安装 curl）
curl.exe -X POST $url `
    -F "img=@$imagePath" `
    -H "Content-Type: multipart/form-data"

Write-Host "`n上传完成！请查看Web界面的实时更新" -ForegroundColor Green
Write-Host "Web界面地址: http://localhost:8080/dashboard" -ForegroundColor Cyan

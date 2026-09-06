@echo off
chcp 65001 >nul
echo ============================================
echo   缺陷检测系统 - 一键启动
echo ============================================
echo.

echo [1/3] 启动 Java 后端...
cd /d "%~dp0defectDetection"
start "DefectDetection" cmd /k "mvn spring-boot:run"

echo [2/3] 等待 Java 后端启动（约 15 秒）...
timeout /t 15 /nobreak >nul

echo [3/3] 启动 TCP Server 和连接设备...
curl -s -X POST http://localhost:8081/vmCamera/start
echo.
curl -s -X POST http://localhost:8081/aubo/connect -H "Content-Type: application/json" -d "{}"
echo.

echo.
echo ============================================
echo   启动完成！
echo   - Java 后端: http://localhost:8081
echo   - VM TCP:    127.0.0.1:8888
echo   - 机械臂:    192.168.1.6:30002
echo ============================================
echo.
echo 请按任意键关闭此窗口...
pause >nul

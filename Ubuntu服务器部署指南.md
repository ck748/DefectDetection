# Ubuntu 服务器部署指南

## 📋 目录
- [系统要求](#系统要求)
- [一、环境准备](#一环境准备)
- [二、数据库部署](#二数据库部署)
- [三、后端部署](#三后端部署)
- [四、AI检测服务部署](#四ai检测服务部署)
- [五、前端部署](#五前端部署)
- [六、Nginx配置](#六nginx配置)
- [七、开机自启动配置](#七开机自启动配置)
- [八、防火墙配置](#八防火墙配置)
- [九、验证部署](#九验证部署)
- [十、常见问题](#十常见问题)

---

## 系统要求

- **操作系统**: Ubuntu 20.04 LTS 或更高版本
- **内存**: 至少 4GB RAM (推荐 8GB)
- **硬盘**: 至少 20GB 可用空间
- **网络**: 可访问互联网用于下载依赖

---

## 一、环境准备

### 1.1 更新系统
```bash
sudo apt update
sudo apt upgrade -y
```

### 1.2 安装 Java 17
```bash
# 安装 OpenJDK 17
sudo apt install openjdk-17-jdk -y

# 验证安装
java -version
# 应该显示: openjdk version "17.x.x"
```

### 1.3 安装 Maven
```bash
# 安装 Maven
sudo apt install maven -y

# 验证安装
mvn -version
```

### 1.4 安装 MySQL 8
```bash
# 安装 MySQL
sudo apt install mysql-server -y

# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 配置 MySQL 安全设置
sudo mysql_secure_installation
```

### 1.5 安装 Python 3 和 pip
```bash
# Ubuntu 通常预装 Python3
python3 --version

# 安装 pip
sudo apt install python3-pip -y

# 安装 Python 虚拟环境工具
sudo apt install python3-venv -y
```

### 1.6 安装 Node.js 和 npm
```bash
# 安装 Node.js 16.x LTS
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install nodejs -y

# 验证安装
node -v
npm -v
```

### 1.7 安装 Nginx
```bash
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

---

## 二、数据库部署

### 2.1 配置 MySQL
```bash
# 登录 MySQL
sudo mysql -u root -p

# 创建数据库和用户
CREATE DATABASE defect_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'defect_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON defect_detection.* TO 'defect_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2.2 导入数据库表结构
```bash
# 上传项目文件到服务器后，执行
mysql -u defect_user -p defect_detection < /path/to/sql/建表语句.sql
```

### 2.3 导入测试数据（可选）
```bash
mysql -u defect_user -p defect_detection < /path/to/sql/测试数据.sql
```

---

## 三、后端部署

### 3.1 上传项目到服务器
```bash
# 创建项目目录
sudo mkdir -p /root/desc/cmzj-main
sudo chown root:root /root/desc/cmzj-main

# 使用 scp 或其他方式上传项目文件
# 从本地执行:
# scp -r c:\Users\LENOVO\Desktop\v2\cmzj-main\cmzj-main/* root@server_ip:/root/desc/cmzj-main/
```

### 3.2 修改配置文件
```bash
cd /root/desc/cmzj-main/defectDetection/src/main/resources
nano application.yaml
```

修改以下配置:
```yaml
spring:
  datasource:
    username: defect_user
    password: your_secure_password
    url: jdbc:mysql://localhost:3306/defect_detection?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&connectionCollation=utf8mb4_unicode_ci

server:
  port: 8081

# AI模型配置 - 根据需要选择
ai:
  mode: local  # 或使用 cloud
  local:
    url: http://localhost:11434/v1/chat/completions
    model: qwen2.5:7b
```

### 3.3 编译打包
```bash
cd /root/desc/cmzj-main/defectDetection

# 清理并打包
mvn clean package -DskipTests

# 打包成功后,jar包位于: target/defectDetection-0.0.1-SNAPSHOT.jar
```

### 3.4 创建启动脚本
```bash
nano /root/desc/cmzj-main/start-backend.sh
```

添加以下内容:
```bash
#!/bin/bash
cd /root/desc/cmzj-main/defectDetection
nohup java -Djava.awt.headless=true -jar target/defectDetection-0.0.1-SNAPSHOT.jar \
  --server.port=8081 \
  > /root/desc/cmzj-main/logs/backend.log 2>&1 &
echo $! > /root/desc/cmzj-main/backend.pid
echo "后端服务已启动,PID: $(cat /root/desc/cmzj-main/backend.pid)"
```

```bash
# 创建日志目录
mkdir -p /root/desc/cmzj-main/logs

# 添加执行权限
chmod +x /root/desc/cmzj-main/start-backend.sh
```

### 3.5 创建停止脚本
```bash
nano /root/desc/cmzj-main/stop-backend.sh
```

添加以下内容:
```bash
#!/bin/bash
if [ -f /root/desc/cmzj-main/backend.pid ]; then
  PID=$(cat /root/desc/cmzj-main/backend.pid)
  kill $PID
  rm /root/desc/cmzj-main/backend.pid
  echo "后端服务已停止"
else
  echo "后端服务未运行"
fi
```

```bash
chmod +x /root/desc/cmzj-main/stop-backend.sh
```

---

## 四、AI检测服务部署

### 4.1 安装 Python 依赖
```bash
cd /root/desc/cmzj-main/predict

# 创建虚拟环境
python3 -m venv venv

# 激活虚拟环境
source venv/bin/activate

# 安装依赖
pip install flask opencv-python-headless numpy ultralytics
```

### 4.2 上传模型文件
```bash
# 确保 v10best.pt 模型文件已上传到 /root/desc/cmzj-main/predict/ 目录
# 从本地执行:
# scp v10best.pt root@server_ip:/root/desc/cmzj-main/predict/
```

### 4.3 创建启动脚本
```bash
nano /root/desc/cmzj-main/start-ai.sh
```

添加以下内容:
```bash
#!/bin/bash
cd /root/desc/cmzj-main/predict
source venv/bin/activate
nohup python3 main.py > /root/desc/cmzj-main/logs/ai.log 2>&1 &
echo $! > /root/desc/cmzj-main/ai.pid
echo "AI检测服务已启动,PID: $(cat /root/desc/cmzj-main/ai.pid)"
```

```bash
chmod +x /root/desc/cmzj-main/start-ai.sh
```

### 4.4 创建停止脚本
```bash
nano /root/desc/cmzj-main/stop-ai.sh
```

添加以下内容:
```bash
#!/bin/bash
if [ -f /root/desc/cmzj-main/ai.pid ]; then
  PID=$(cat /root/desc/cmzj-main/ai.pid)
  kill $PID
  rm /root/desc/cmzj-main/ai.pid
  echo "AI检测服务已停止"
else
  echo "AI检测服务未运行"
fi
```

```bash
chmod +x /root/desc/cmzj-main/stop-ai.sh
```

---

## 五、前端部署

### 5.1 修改前端配置
```bash
cd /root/desc/cmzj-main/web/detection

# 修改环境变量
nano .env
```

修改为服务器IP或域名:
```env
VUE_APP_BASE_API=http://your-server-ip:8080/detect
```

或者如果使用 Nginx 反向代理（推荐）:
```env
VUE_APP_BASE_API=/api
```

### 5.2 构建前端
```bash
cd /root/desc/cmzj-main/web/detection

# 安装依赖
npm install

# 构建生产版本
npm run build

# 构建完成后,dist 目录包含生产文件
```

### 5.3 配置前端静态文件目录
```bash
# 将构建好的文件复制到 Nginx 目录
sudo mkdir -p /var/www/defect-detection
sudo cp -r dist/* /var/www/defect-detection/
sudo chown -R www-data:www-data /var/www/defect-detection
```

---

## 六、Nginx配置

### 6.1 创建 Nginx 配置文件
```bash
sudo nano /etc/nginx/sites-available/defect-detection
```

添加以下内容:
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或服务器IP

    # 前端静态文件
    location / {
        root /var/www/defect-detection;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # SSE支持
        proxy_buffering off;
        proxy_cache off;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
    }

    # AI检测服务代理
    location /detect/ {
        proxy_pass http://localhost:8090/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # 增加超时时间（AI推理可能需要较长时间）
        proxy_read_timeout 300s;
        proxy_connect_timeout 300s;
        proxy_send_timeout 300s;
        
        # 增加body大小限制（上传图片）
        client_max_body_size 50M;
    }

    # Swagger UI
    location /swagger-ui.html {
        proxy_pass http://localhost:8081/swagger-ui.html;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 日志
    access_log /var/log/nginx/defect-detection-access.log;
    error_log /var/log/nginx/defect-detection-error.log;
}
```

### 6.2 启用站点配置
```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/defect-detection /etc/nginx/sites-enabled/

# 删除默认配置（可选）
sudo rm /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

---

## 七、开机自启动配置

### 7.1 创建后端服务文件
```bash
sudo nano /etc/systemd/system/defect-backend.service
```

添加以下内容:
```ini
[Unit]
Description=Defect Detection Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/root/desc/cmzj-main/defectDetection
ExecStart=/usr/bin/java -Djava.awt.headless=true -jar /root/desc/cmzj-main/defectDetection/target/defectDetection-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/root/desc/cmzj-main/logs/backend.log
StandardError=append:/root/desc/cmzj-main/logs/backend-error.log

[Install]
WantedBy=multi-user.target
```

### 7.2 创建AI检测服务文件
```bash
sudo nano /etc/systemd/system/defect-ai.service
```

添加以下内容:
```ini
[Unit]
Description=Defect Detection AI Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/desc/cmzj-main/predict
ExecStart=/root/desc/cmzj-main/predict/venv/bin/python3 /root/desc/cmzj-main/predict/main.py
Restart=on-failure
RestartSec=10
StandardOutput=append:/root/desc/cmzj-main/logs/ai.log
StandardError=append:/root/desc/cmzj-main/logs/ai-error.log

[Install]
WantedBy=multi-user.target
```

### 7.3 启用并启动服务
```bash
# 重新加载 systemd
sudo systemctl daemon-reload

# 启用开机自启
sudo systemctl enable defect-backend
sudo systemctl enable defect-ai

# 启动服务
sudo systemctl start defect-backend
sudo systemctl start defect-ai

# 查看服务状态
sudo systemctl status defect-backend
sudo systemctl status defect-ai
```

---

## 八、防火墙配置

### 8.1 配置 UFW 防火墙
```bash
# 启用防火墙
sudo ufw enable

# 允许 SSH
sudo ufw allow ssh

# 允许 HTTP
sudo ufw allow 80/tcp

# 允许 HTTPS（如果配置SSL）
sudo ufw allow 443/tcp

# 查看防火墙状态
sudo ufw status
```

### 8.2 内部端口说明
以下端口仅用于内部服务通信，无需对外开放:
- **8081**: Spring Boot 后端
- **8090**: Python AI 检测服务
- **3306**: MySQL 数据库
- **11434**: Ollama (如果使用本地AI模型)

---

## 九、验证部署

### 9.1 检查服务状态
```bash
# 检查后端服务
sudo systemctl status defect-backend
curl http://localhost:8081/swagger-ui.html

# 检查AI服务
sudo systemctl status defect-ai
curl http://localhost:8090/test

# 检查Nginx
sudo systemctl status nginx
curl http://localhost
```

### 9.2 查看日志
```bash
# 后端日志
tail -f /root/desc/cmzj-main/logs/backend.log

# AI服务日志
tail -f /root/desc/cmzj-main/logs/ai.log

# Nginx日志
sudo tail -f /var/log/nginx/defect-detection-access.log
sudo tail -f /var/log/nginx/defect-detection-error.log
```

### 9.3 访问系统
在浏览器中访问: `http://your-server-ip`

---

## 十、常见问题

### 10.1 后端无法启动
```bash
# 检查Java版本
java -version  # 必须是Java 17

# 检查端口占用
sudo netstat -tulpn | grep 8081

# 查看详细错误日志
tail -100 /root/desc/cmzj-main/logs/backend.log
```

### 10.2 数据库连接失败
```bash
# 检查MySQL服务
sudo systemctl status mysql

# 测试数据库连接
mysql -u defect_user -p -h localhost defect_detection

# 检查防火墙规则
sudo ufw status
```

### 10.3 AI服务无法启动
```bash
# 检查Python依赖
source /root/desc/cmzj-main/predict/venv/bin/activate
pip list

# 检查模型文件
ls -lh /root/desc/cmzj-main/predict/v10best.pt

# 重新安装依赖
pip install --upgrade ultralytics opencv-python-headless
```

### 10.4 前端无法访问
```bash
# 检查Nginx配置
sudo nginx -t

# 检查静态文件
ls -lh /var/www/defect-detection/

# 重启Nginx
sudo systemctl restart nginx
```

### 10.5 图片上传失败
```bash
# 检查Nginx文件大小限制
# 在 /etc/nginx/sites-available/defect-detection 中添加:
# client_max_body_size 50M;

# 检查后端配置
# application.yaml 中确认:
# spring.servlet.multipart.max-file-size: 50MB
```

---

## 十一、可选配置

### 11.1 安装 Ollama (本地AI模型)
```bash
# 下载并安装Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 下载模型
ollama pull qwen2.5:7b

# 验证运行
ollama list
curl http://localhost:11434/v1/chat/completions
```

### 11.2 配置 SSL 证书 (HTTPS)
```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 11.3 配置日志轮转
```bash
sudo nano /etc/logrotate.d/defect-detection
```

添加以下内容:
```
/root/desc/cmzj-main/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0640 root root
}
```

---

## 十二、维护命令

### 12.1 服务管理
```bash
# 启动所有服务
sudo systemctl start defect-backend defect-ai nginx

# 停止所有服务
sudo systemctl stop defect-backend defect-ai nginx

# 重启所有服务
sudo systemctl restart defect-backend defect-ai nginx

# 查看所有服务状态
sudo systemctl status defect-backend defect-ai nginx
```

### 12.2 备份数据库
```bash
# 创建备份目录
mkdir -p /root/desc/cmzj-main/backups

# 备份数据库
mysqldump -u defect_user -p defect_detection > /root/desc/cmzj-main/backups/db_backup_$(date +%Y%m%d_%H%M%S).sql

# 定时备份(添加到crontab)
crontab -e
# 添加: 0 2 * * * mysqldump -u defect_user -p'password' defect_detection > /root/desc/cmzj-main/backups/db_backup_$(date +\%Y\%m\%d).sql
```

### 12.3 更新部署
```bash
# 1. 停止服务
sudo systemctl stop defect-backend defect-ai

# 2. 备份当前版本
cp -r /root/desc/cmzj-main /root/desc/cmzj-main.bak

# 3. 上传新版本文件

# 4. 重新构建后端
cd /root/desc/cmzj-main/defectDetection
mvn clean package -DskipTests

# 5. 重新构建前端
cd /root/desc/cmzj-main/web/detection
npm install
npm run build
sudo cp -r dist/* /var/www/defect-detection/

# 6. 重启服务
sudo systemctl start defect-backend defect-ai
sudo systemctl restart nginx
```

---

## 📞 支持

如遇到问题,请检查:
1. 日志文件: `/root/desc/cmzj-main/logs/`
2. Nginx日志: `/var/log/nginx/`
3. 系统日志: `journalctl -u defect-backend -f`

---

**部署完成! 🎉**

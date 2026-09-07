# 灵眸巡诊·缺陷检测系统 Docker 镜像打包与全生命周期运维部署手册

---

## 目录
1. [工作记录与实战排错纪实](#1-工作记录与实战排错纪实)
2. [工程产物清单与文件架构](#2-工程产物清单与文件架构)
3. [首次服务器全量部署流程（包含数据库导入）](#3-首次服务器全量部署流程包含数据库导入)
4. [日常二次开发与增量热更新指南（小改动免重打全量包）](#4-日常二次开发与增量热更新指南小改动免重打全量包)
5. [系统日常运维与常用管理指令](#5-系统日常运维与常用管理指令)
6. [数据持久化与数据库备份/恢复](#6-数据持久化与数据库备份恢复)
7. [网络安全组、端口映射与排坑避障总结](#7-网络安全组端口映射与排坑避障总结)

---

## 1. 工作记录与实战排错纪实

### 📅 工作时间：2026-09-06
### 🎯 目标任务：
将 Windows 本地运行的“灵眸巡诊·缺陷检测系统”（Java 17 Spring Boot + Vue 前端 + MySQL 8.0）容器化，打包为离线 Docker 镜像并部署到 Linux 服务器上，实现在任意客户端浏览器流畅访问与交互。

---

### 🛠️ 关键问题排查与攻关全记录（踩坑与填坑复盘）：

#### 踩坑 1：国内 Docker 镜像源 403 / 阻断导致本地无法打包
* **现象**：本地执行 `docker build` 时，因 Docker Hub 官方源及 DaoCloud 等第三方加速源失效，提示 `403 Forbidden` 或超时失败。
* **解决**：本地开启代理客户端（Sparkle）的 **虚拟网卡（TUN 模式）**，基础镜像无缝切换为官方维护且极为轻量的 `eclipse-temurin:17-jdk-alpine`，成功拉取并构建后端与前端镜像。

#### 踩坑 2：服务器首次访问报 `502 Bad Gateway`（后端容器启动崩溃）
* **现象**：前端 Nginx 正常启动，但访问接口全部报 `502 Bad Gateway`。查看后端日志（`docker logs defect-backend`）捕获到异常：
  ```text
  java.nio.file.NoSuchFileException: src/main/resources/config.json
  Caused by: java.lang.NullPointerException: Cannot invoke "...JSONObject.getString" because "jsonObject" is null
  at com.ggbond.defectdetection.software.common.ConfigProperties.loadProperties
  ```
* **根因定位**：原代码在 `ConfigProperties.java` 中写死了本地源码相对路径 `src/main/resources/config.json`。在 IDEA 本地运行时正常，但在 Docker 容器运行在 `/app` 根目录下时该文件路径不存在，返回 null 引发空指针崩溃。
* **代码修复**：
  - 在 `ConfigProperties.java` 中重构配置加载器，优先读取挂载目录 `config/config.json`，其次通过 `ClassPathResource` 读取 jar 包内的配置流，并添加全局默认非空保底对象，彻底杜绝 NPE。
  - 重新编译 Maven 并导出独立后端增量包 `backend.tar`（~150MB），服务器上平滑热更新后成功启动！

#### 踩坑 3：全新 MySQL 容器无表结构导致登录无反应
* **现象**：后端启动成功后，在登录页输入账号密码提示异常或无法登录。
* **根因定位**：Docker 编排中新生成的 MySQL 8.0 是一个全新的空数据库，没有创建表结构和默认管理员账号。
* **解决**：从本地数据库转储完整 `defect_detection.sql`（含表结构与数据），通过管道命令 `docker exec -i defect-mysql mysql -uroot -proot defect_detection < defect_detection.sql` 一键注入容器，系统完全恢复正常。

---

## 2. 工程产物清单与文件架构

在项目根目录下已准备好的离线与构建资产：

```text
DefectDetection/
├── defect-images.tar                # [全量离线包] 722 MB（包含后端、前端 Nginx、MySQL 8.0 三个镜像）
├── backend.tar                      # [增量更新包] 150 MB（仅后端最新修复版镜像）
├── docker-compose.yml               # [编排文件] 容器网络、端口映射、健康检查与持久化卷定义
├── DOCKER_DEPLOY_GUIDE.md           # [本手册] 完整部署与运维手册
│
├── defectDetection/                 # 后端工程
│   ├── Dockerfile                   # 后端 Java 17 Alpine 镜像构建配置（已注入 Headless 模式与时区）
│   └── target/*.jar                 # 编译完成的最新后端可执行 jar 包
│
└── web/detection/                   # 前端工程
    ├── Dockerfile                   # 前端 Nginx 容器构建配置
    ├── nginx.conf                   # 前端路由转发、SSE/WebSocket 穿透与 Gzip 压缩配置
    └── dist/                        # 编译完成的前端生产静态资源
```

---

## 3. 首次服务器全量部署流程（包含数据库导入）

### 第一步：上传核心文件至服务器
使用 `Xftp`、`FinalShell` 或 `scp` 将以下文件上传至服务器目录（如 `/home/defect_system/`）：
1. `defect-images.tar`（或 `backend.tar`）
2. `docker-compose.yml`
3. 本地导出的数据库脚本 `defect_detection.sql`

---

### 第二步：导入镜像并一键启动容器
在服务器终端中执行：
```bash
cd /home/defect_system

# 1. 导入离线镜像（耗时约 10~20 秒）
docker load -i defect-images.tar

# 2. 后台一键启动前后端与数据库容器
docker compose up -d

# 3. 检查容器运行状态（确保三者均处于 Up 状态）
docker compose ps
```

---

### 第三步：注入初始数据库表结构与数据
```bash
# 将本地转储的 SQL 文件一键导入到 MySQL 容器
docker exec -i defect-mysql mysql -uroot -proot defect_detection < defect_detection.sql
```
*提示 `[Warning] Using a password...` 为正常安全提示，命令返回下一行即代表导入完成！*

🌐 **打开浏览器访问**：
直接在浏览器输入 `http://<服务器IP>:<映射端口>`（例如：`http://192.168.1.3:8080`），输入账号密码即可登录使用！

---

## 4. 日常二次开发与增量热更新指南（小改动免重打全量包）

后续开发中如对前端或后端代码进行了调整，**切勿重复打包 700MB 的全量镜像包**，使用增量更新：

---

### 场景 A：只修改了前端页面（UI 美化、页面文案等）

#### 方案 1：独立导出前端镜像（推荐，仅约 30MB）
1. 本地打包：
   ```powershell
   cd web/detection; npm run build; cd ../..
   docker build -t defect-frontend:latest ./web/detection
   docker save -o frontend.tar defect-frontend:latest
   ```
2. 服务器更新（秒级生效）：
   上传 `frontend.tar` 到服务器执行：
   ```bash
   docker load -i frontend.tar
   docker compose up -d --no-deps frontend
   ```

#### 方案 2：静态资源挂载（免镜像构建，覆盖即更新）
在服务器 `docker-compose.yml` 中配置前端挂载：
```yaml
frontend:
  image: defect-frontend:latest
  volumes:
    - ./dist:/usr/share/nginx/html
```
本地执行 `npm run build` 后，只需用 Xftp 把本地 `dist/` 文件夹上传覆盖服务器的 `dist/` 目录，浏览器按 `Ctrl + F5` 强制刷新立即生效。

---

### 场景 B：只修改了后端 Java 代码（Controller、Service 等）

1. 本地重新编译并导出后端镜像（约 150MB）：
   ```powershell
   cd defectDetection; mvn clean package -DskipTests; cd ..
   docker build -t defect-backend:latest ./defectDetection
   docker save -o backend.tar defect-backend:latest
   ```
2. 服务器平滑重启后端：
   上传 `backend.tar` 到服务器执行：
   ```bash
   docker load -i backend.tar
   docker compose up -d --no-deps backend
   ```

---

## 5. 系统日常运维与常用管理指令

在服务器 `/home/defect_system` 目录下执行：

```bash
# 1. 查看容器运行状态与资源占用
docker compose ps
docker stats

# 2. 查看实时日志（排查问题利器）
docker logs -f defect-backend      # 查看后端实时运行日志
docker logs -f defect-frontend     # 查看前端 Nginx 访问与代理日志
docker logs -f defect-mysql        # 查看数据库日志

# 3. 容器重启与生命周期管理
docker compose restart             # 重启所有服务
docker compose restart backend     # 仅重启后端
docker compose stop                # 暂停所有服务（数据完好保留）
docker compose down                # 停止并清理网络容器（持久化数据保留）
```

---

## 6. 数据持久化与数据库备份/恢复

所有业务数据均实时保存在宿主机当前目录的 `./data` 文件夹中，**即使容器被销毁重建，数据也绝不丢失**：

- 📷 **缺陷图片与切片**：`./data/uploads/`
- 📝 **后端应用运行日志**：`./data/logs/`
- 💾 **MySQL 数据库数据文件**：`./data/mysql/`

### 常用数据库备份与还原命令：

```bash
# 1. 将服务器容器内的数据库导出备份为 sql 文件
docker exec -i defect-mysql mysqldump -uroot -proot defect_detection > backup_$(date +%Y%m%d).sql

# 2. 还原 sql 备份到数据库
docker exec -i defect-mysql mysql -uroot -proot defect_detection < backup_20260906.sql
```

---

## 7. 网络安全组、端口映射与排坑避障总结

1. **端口冲突与映射调整**：
   - 若服务器宿主机 `80` 或 `3306` 端口被原有服务占用，可按需在 `docker-compose.yml` 中调整宿主机映射端口（如将前端修改为 `8080:80`，将 MySQL 修改为 `13306:3306`）。
   - **注意**：容器内部通信不受宿主机端口映射影响，后端与 MySQL 在内部虚拟网络依然使用标准端口通信。

2. **云服务器/局域网防火墙放行**：
   - 必须放行前端访问端口（如 `80` 或 `8080`）。
   - 外部 Navicat 远程连接数据库时需放行对应端口（如 `13306`）。

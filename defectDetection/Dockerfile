# 基础运行环境：官方维护的 Eclipse Temurin Java 17 Alpine 镜像（轻量稳定）
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 安装 tzdata 配置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 设置无头模式（适配无 GUI 服务器）
ENV JAVA_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8"

# 复制打包生成的 jar 包
COPY target/*.jar app.jar

# 暴露后端端口
EXPOSE 8081

# 声明持久化挂载卷
VOLUME ["/app/uploads", "/app/logs"]

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

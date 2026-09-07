# 阶段 1: 构建产物
FROM nginx:alpine

# 拷贝前端静态资源
COPY dist /usr/share/nginx/html

# 拷贝 Nginx 反向代理配置
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

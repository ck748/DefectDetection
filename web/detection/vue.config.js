module.exports={
  devServer:{
    port:"8082", // 设置端口 默认8080
    open:true, // 项目运行自动打开浏览器
    historyApiFallback: true,
    // 在与port open 设置服务代理
    proxy:{
      // /api 自定义服务代理名字
      "/api":{
        target:"http://localhost:8081", //代理帮助你请求的具体服务http://localhost:8081
        changeOrigin:true, // 开启代理
        ws: true, // 启用WebSocket代理（SSE需要）
        pathRewrite:{  // 格式化path
          "^/api":""
        },
        bypass: function (req) {
          // 浏览器直接刷新页面时，请求头 accept 包含 html，或者是 /apimanager 这类页面路由，直接返回单页面入口 index.html，不代理给后端
          if (req.headers && req.headers.accept && req.headers.accept.includes("html")) {
            return "/index.html";
          }
          if (req.url && req.url.startsWith("/apimanager")) {
            return "/index.html";
          }
        },
        onProxyReq: (proxyReq, req, res) => {
          // 设置正确的请求头，确保SSE能正常工作
          if (req.headers.accept && req.headers.accept.includes('text/event-stream')) {
            proxyReq.setHeader('Accept', 'text/event-stream');
            proxyReq.setHeader('Cache-Control', 'no-cache');
            proxyReq.setHeader('Connection', 'keep-alive');
          }
        },
        onProxyRes: (proxyRes, req, res) => {
          // 确保响应头正确
          if (proxyRes.headers['content-type'] && proxyRes.headers['content-type'].includes('text/event-stream')) {
            proxyRes.headers['Cache-Control'] = 'no-cache';
            proxyRes.headers['Connection'] = 'keep-alive';
            proxyRes.headers['X-Accel-Buffering'] = 'no';
          }
        }
      }
    }
  }
}

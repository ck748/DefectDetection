# 仓库贡献指南

## 项目结构与模块组织

这是一个基于 Vue.js 2.x 的缺陷检测 Web 应用程序。项目遵循标准的 Vue CLI 结构：

- **`src/`** - 主要源代码目录
  - **`components/`** - 可复用的 Vue 组件（检测、仪表板、图表等）
  - **`views/`** - 页面级组件（主页、登录）
  - **`router/`** - Vue Router 路由配置
  - **`assets/`** - 静态资源（图片、图标、样式）
  - **`data/`** - 数据文件和后端服务器逻辑
- **`public/`** - 静态公共文件（index.html、favicon）
- **`package.json`** - 项目依赖和脚本配置

## 构建、测试和开发命令

首次运行前安装依赖：
```bash
npm install
```

启动开发服务器（带热重载，运行在 8080 端口）：
```bash
npm run serve
```

构建生产版本（输出到 `dist/` 文件夹）：
```bash
npm run build
```

应用程序在开发模式下会将 API 请求代理到 `http://localhost:8081`。

## 编码风格与命名规范

- **缩进规则**：使用 2 个空格（禁止使用制表符）
- **组件命名**：组件文件使用 PascalCase 命名（例如：`HelloWorld.vue`）
- **路由命名**：路由路径使用 kebab-case 命名（例如：`/picture_detection`）
- **语言**：注释和 UI 文本使用中文
- **框架**：Vue 2.x 配合 Vue Router 3.x
- **UI 库**：使用 Element UI 和 Ant Design Vue 组件库

## 测试指南

本项目目前未包含测试框架。在添加测试时：
- 考虑使用 Jest 或 Mocha 进行单元测试
- 使用 Vue Test Utils 进行组件测试
- 测试文件应放置在组件旁边，使用 `.spec.js` 或 `.test.js` 后缀
- 专注于核心检测和数据处理逻辑的有效覆盖

## 架构概览

- **前端**：Vue.js 2.x 单页应用（SPA），使用 Vue Router 进行导航
- **UI 组件**：Element UI + Ant Design Vue
- **图表**：ECharts 用于数据可视化
- **后端 API**：Express 服务器（参见 `src/data/server.js`）
- **身份验证**：基于 Token 的认证，使用 localStorage 持久化

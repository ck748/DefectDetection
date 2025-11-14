# Repository Guidelines

## Project Structure & Module Organization

This is a Vue.js 2.x web application for defect detection. The project follows a standard Vue CLI structure:

- **`src/`** - Main source code directory
  - **`components/`** - Reusable Vue components (detection, dashboard, charts, etc.)
  - **`views/`** - Page-level components (Home, login)
  - **`router/`** - Vue Router configuration
  - **`assets/`** - Static assets (images, icons, styles)
  - **`data/`** - Data files and backend server logic
- **`public/`** - Static public files (index.html, favicon)
- **`package.json`** - Project dependencies and scripts

## Build, Test, and Development Commands

Install dependencies before first run:
```bash
npm install
```

Start development server with hot-reload (runs on port 8080):
```bash
npm run serve
```

Build for production (outputs to `dist/` folder):
```bash
npm run build
```

The application proxies API requests to `http://localhost:8081` in development mode.

## Coding Style & Naming Conventions

- **Indentation**: Use 2 spaces (no tabs)
- **Component Naming**: Use PascalCase for component files (e.g., `HelloWorld.vue`)
- **Route Naming**: Use kebab-case for route paths (e.g., `/picture_detection`)
- **Language**: Use Chinese for comments and UI text
- **Framework**: Vue 2.x with Vue Router 3.x
- **UI Libraries**: Element UI and Ant Design Vue for components

## Testing Guidelines

This project does not currently include a testing framework. When adding tests:
- Consider using Jest or Mocha for unit tests
- Use Vue Test Utils for component testing
- Place test files adjacent to components with `.spec.js` or `.test.js` suffix
- Aim for meaningful coverage of core detection and data processing logic

## Architecture Overview

- **Frontend**: Vue.js 2.x SPA with Vue Router for navigation
- **UI Components**: Element UI + Ant Design Vue
- **Charts**: ECharts for data visualization
- **Backend API**: Express server (see `src/data/server.js`)
- **Authentication**: Token-based with localStorage persistence

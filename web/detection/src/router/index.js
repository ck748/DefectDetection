import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from "@/views/Home.vue";

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
    return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'Manager',
        component: () => import('../views/login.vue'),
        redirect: '/login',  // 重定向到主页
    },
    { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/login.vue') },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        children: [
            { path: '/daping', name: 'Daping', meta: { name: '数据大屏' }, component: () => import('../components/platform.vue') }, 
            { path: '/picture_detection', name: 'Picture_detection', meta: { name: '图片检测' }, component: () => import('../components/picture_detection.vue') },
            { path: '/dashboard', name: 'Dashboard', meta: { name: '概要信息' }, component: () => import('../components/dashboard.vue') },
            { path: '/info', name: 'Info', meta: { name: '历史检测' }, component: () => import('../components/info.vue') },
            { path: '/annotation', name: 'Annotation', meta: { name: '数据标注' }, component: () => import('../components/annotation.vue') },
            { path: '/history-annotation', name: 'HistoryAnnotation', meta: { name: '历史标注' }, component: () => import('../components/biaozhu.vue') },
            
            { path: '/warning', name: 'Warning', meta: { name: '异常管理' }, component: () => import('../components/warning.vue') },
            { path: '/log', name: 'Log', meta: { name: '日志管理' }, component: () => import('../components/log.vue') },
            { path: '/apimanager', name: 'apiManager', meta: { name: 'api管理' }, component: () => import('../components/apimanager.vue') },
            { path: '/pwdmanager', name: 'pwdManager', meta: { name: '操作秘钥管理' }, component: () => import('../components/pwdmanager.vue') },
            { path: '/camera', name: 'Camera', meta: { name: '摄像头监控' }, component: () => import('../components/camera.vue') },
            { path: '/camera-watch', name: 'CameraWatch', meta: { name: '摄像头目录监听' }, component: () => import('../components/CameraWatch.vue') },
            { path: '/person', name: 'Person', meta: { name: '个人中心' }, component: () => import('../components/person.vue') },
            { path: '/password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../components/password.vue') },

            // 国赛2.0 升级核心页面
            { path: '/omni-inspection', name: 'OmniInspection', meta: { name: '360°全周质检', isFullLayout: true }, component: () => import('../components/OmniInspection.vue') },
            { path: '/feature-checklist', name: 'FeatureChecklist', meta: { name: '功能核对' }, component: () => import('../components/featureChecklist.vue') },
            { path: '/six-s-manager', name: 'SixSManager', meta: { name: '6S管家' }, component: () => import('../components/SixSManager.vue') },

            // 同学新增页面：AGV控制
            { path: '/agv', name: 'Agv', meta: { name: 'AGV控制' }, component: () => import('../components/agvControl.vue') },
            // 设备管理页面
            { path: '/device-manager', name: 'DeviceManager', meta: { name: '设备管理' }, component: () => import('../components/deviceManager.vue') },
        ]
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

router.beforeEach((to, from, next) => {
    // to 是到达的路由信息
    // from 是离开的路由信息
    // next 是帮助我们跳转路由的函数
    let user = JSON.parse(localStorage.getItem('useradmin') || '{}')
    
    // 修复：移除多余的 next() 调用
    if (!user || Object.keys(user).length === 0) {
        // 如果用户不存在，跳转到登录页或403页面
        if (to.path !== '/login') {
            next('/login')
        } else {
            next()
        }
    } else {
        next()
    }
})

export default router

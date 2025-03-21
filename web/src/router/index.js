import { createRouter, createWebHistory } from 'vue-router';
import MainLayout from '../components/MainLayout.vue';
import Login from "@/components/Login";
import MachineStatus from '@/components/MachineStatus.vue';
import MachineSettings from '@/components/MachineSettings.vue'

// 动态导入组件，提升性能
const OrderPage = () => import('../components/OrderPage.vue');
const ButtonsPage = () => import('../components/ButtonsPage.vue');
const SalesStatistics = () => import('../components/SalesStatistics.vue');
const CleaningOperation = () => import('../components/CleaningOperation.vue');
const AutomaticOperation = () => import('../components/AutomaticOperation.vue');
const ManualOperation = () => import('../components/ManualOperation.vue');
const AlarmSettings = () => import('../components/AlarmSettings.vue');
const PortionSettings = () => import('../components/PortionSettings.vue');
const OrderingSettings = () => import('../components/OrderingSettings.vue');
const PriceSettings = () => import('../components/PriceSettings.vue');
const SetAccount = () => import('@/components/SetAccount.vue');

const routes = [
    { path: '/login', name: 'Login', component: Login },
    {
        path: '/',
        component: MainLayout,
        children: [
            { path: '', name: 'OrderPage', component: OrderPage }, // 默认首页
            { path: 'buttons', name: 'ButtonsPage', component: ButtonsPage },
            { path: 'salesStatistics', name: 'SalesStatistics', component: SalesStatistics },
            { path: 'cleaningOperation', name: 'CleaningOperation', component: CleaningOperation },
            { path: 'automaticOperation', name: 'AutomaticOperation', component: AutomaticOperation },
            { path: 'manualOperation', name: 'ManualOperation', component: ManualOperation },
            { path: 'alarmSettings', name: 'AlarmSettings', component: AlarmSettings },
            { path: 'portionSettings', name: 'PortionSettings', component: PortionSettings },
            { path: 'orderingSettings', name: 'OrderingSettings', component: OrderingSettings },
            { path: 'priceSettings', name: 'PriceSettings', component: PriceSettings },
            { path: 'setAccount', name: 'SetAccount', component: SetAccount },
            { path: 'machineStatus', name: 'MachineStatus', component: MachineStatus },
            { path: 'machineSettings', name: 'MachineSettings', component: MachineSettings }
        ]
    }
];

// 假设使用 localStorage 存储用户的登录状态
const isAuthenticated = () => {
    return !!localStorage.getItem('userToken'); // 假设通过 token 判断是否登录
};

const router = createRouter({
    history: createWebHistory(), // 使用 HTML5 History 模式，去掉路径中的 # 号
    routes
});

// 全局路由守卫
router.beforeEach((to, from, next) => {
    const publicPages = ['OrderPage', 'Login'];
    const authRequired = !publicPages.includes(to.name);
    const loggedIn = !!localStorage.getItem('userToken');

    if (authRequired && !loggedIn) {
        return next('/login');
    }
    next();
});

export default router;

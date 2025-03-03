import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './assets/styles/global.css';

// 显式定义feature flag
window.__VUE_PROD_HYDRATION_MISMATCH_DETAILS__ = true;

// Configure Vue feature flags
const app = createApp(App);

app.use(router)
   .use(ElementPlus)
   .mount('#app');

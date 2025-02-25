<template>
  <div>
    <el-menu
        :default-active="activeIndex"
        class="custom-menu"
        @select="handleSelect"
        :background-color="'#FFA500'"
        :text-color="'#FFFFFF'"
        :active-text-color="'#FF4500'"
    >
      <!-- 菜单组 1 -->
      <el-menu-item-group title="返回点餐">
        <el-menu-item index="1" @click="goTo('/')">
          点餐
        </el-menu-item>
      </el-menu-item-group>

      <!-- 菜单组 2 -->
      <el-menu-item-group title="设置页面">
        <el-menu-item index="2" @click="goTo('/machineStatus')">
          机器状态
        </el-menu-item>
        <el-menu-item index="3" @click="goTo('/machineSettings')">
          机器设置
        </el-menu-item>
        <el-menu-item index="4" @click="goTo('salesStatistics')">
          销售统计
        </el-menu-item>
       
      </el-menu-item-group>
    </el-menu>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'Menu',
  data() {
    return {
      activeIndex: this.getActiveIndex(),
      menuBackgroundColor: '#eeeeee',  // 默认颜色，之后会被替换
      menuTextColor: '#333333',        // 默认颜色，之后会被替换
      menuActiveTextColor: '#ffffff',  // 默认颜色，之后会被替换
    };
  },
  mounted() {
    // 获取 CSS 变量的值
    const rootStyles = getComputedStyle(document.documentElement);
    this.menuBackgroundColor = rootStyles.getPropertyValue('--gray').trim();
    this.menuTextColor = rootStyles.getPropertyValue('--gray').trim();
    this.menuActiveTextColor = rootStyles.getPropertyValue('--silver').trim();
  },
  setup() {
    const router = useRouter();
    const goTo = (path) => {
      router.push(path);
    };
    return { goTo };
  },
  methods: {
    handleSelect(key) {
      this.activeIndex = key; // 更新选中项
      switch (key) {
        case '1':
          this.goTo('/');
          break;
        case '2':
          this.goTo('/machineStatus');
          break;
        case '3':
          this.goTo('/machineSettings');
          break;
        case '4':
          this.goTo('salesStatistics');
          break;
        case '9':
          this.goTo('buttons');
          break;
        default:
          this.goTo('/');
      }
    },
    getActiveIndex() {
      const path = this.$route.path;
      switch (path) {
        case '/':
          return '1';
        case '/machineStatus':
          return '2';
        case '/machineSettings':
          return '3';
        case '/salesStatistics':
          return '4';
        case '/buttons':
          return '9';
        default:
          return '1';
      }
    }
  },
  watch: {
    '$route'(to) {
      this.activeIndex = this.getActiveIndex();
    }
  }
};
</script>

<style scoped>
.custom-menu {
  width: 200px;
  border-right: none;
}

/* 菜单项的基本样式 */
:deep(.el-menu-item) {
  background-color: var(--gray) !important;
  color: var(--deep-blue) !important;
  transition: all 0.3s ease;
}

/* 菜单项悬停效果 */
:deep(.el-menu-item:hover) {
  background-color: var(--silver) !important;
  color: var(--deep-blue) !important;
}

/* 选中菜单项的样式 */
:deep(.el-menu-item.is-active) {
  background-color: var(--deep-blue) !important;
  color: var(--silver) !important;
}

/* 菜单组标题样式 */
:deep(.el-menu-item-group__title) {
  padding: 10px 0;
  color: var(--deep-blue);
  font-weight: bold;
  font-size: 16px;
}

/* 整个菜单的背景色 */
.custom-menu {
  background-color: var(--gray) !important;
}
</style>

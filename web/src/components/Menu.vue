<template>
  <div>
    <el-menu
        :default-active="activeIndex"
        class="custom-menu"
        @select="handleSelect"
        :background-color="menuBackgroundColor"
        :text-color="menuTextColor"
        :active-text-color="menuActiveTextColor"
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
        <el-menu-item index="3" @click="goTo('salesStatistics')">
          销售统计
        </el-menu-item>
        <el-menu-item index="4" @click="goTo('alarmSettings')">
          报警设置
        </el-menu-item>
        <el-menu-item index="5" @click="goTo('cleaningOperation')">
          清洗操作
        </el-menu-item>
        <el-menu-item index="6" @click="goTo('automaticOperation')">
          自动运行
        </el-menu-item>
        <el-menu-item index="7" @click="goTo('manualOperation')">
          手动操作
        </el-menu-item>
        <el-menu-item index="8" @click="goTo('buttons')">
          配料分发
        </el-menu-item>
        <el-menu-item index="9" @click="goTo('setAccount')">
          系统设置
        </el-menu-item>
        <el-menu-item index="10" @click="goTo('portionSettings')">
          份量设置
        </el-menu-item>
        <el-menu-item index="11" @click="goTo('priceSettings')">
          价格设置
        </el-menu-item>
        <el-menu-item index="12" @click="goTo('/machineSettings')">
          机器设置
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
          this.goTo('salesStatistics');
          break;
        case '4':
          this.goTo('alarmSettings');
          break;
        case '5':
          this.goTo('cleaningOperation');
          break;
        case '6':
          this.goTo('automaticOperation');
          break;
        case '7':
          this.goTo('manualOperation');
          break;
        case '8':
          this.goTo('buttons');
          break;
        case '9':
          this.goTo('setAccount');
          break;
        case '10':
          this.goTo('portionSettings');
          break;
        case '11':
          this.goTo('priceSettings');
          break;
        case '12':
          this.goTo('/machineSettings');
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
        case '/salesStatistics':
          return '3';
        case '/alarmSettings':
          return '4';
        case '/cleaningOperation':
          return '5';
        case '/automaticOperation':
          return '6';
        case '/manualOperation':
          return '7';
        case '/buttons':
          return '8';
        case '/setAccount':
          return '9';
        case '/portionSettings':
          return '10';
        case '/priceSettings':
          return '11';
        case '/machineSettings':
          return '12';
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

.custom-menu  {
  background-color: #eeeeee;
  color: #333333;
  transition: background-color 0.3s, color 0.3s;
  cursor: pointer; /* 确保整个菜单项看起来是可以点击的 */
}

.custom-menu :hover {
  background-color: #e0e0e0;
}

.custom-menu {
  background-color: var(--deep-blue) !important;
  color: var(--silver) !important;
}
</style>

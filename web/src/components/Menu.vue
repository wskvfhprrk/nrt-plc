<template>
  <div>
    <el-menu
        :default-active="activeIndex"
        class="custom-menu"
        @select="handleSelect"
        :background-color="'#FFFFFF'"
        :text-color="'#333333'"
        :active-text-color="'#FFFFFF'"
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
        <el-menu-item index="5" @click="goTo('/setAccount')">
          账户设置
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
    };
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
      this.goTo(key);
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
        case '/setAccount':
          return '5';
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
}

.el-menu-vertical {
  background-color: #ffffff; /* 背景颜色 */
}

.el-menu-item {
  font-size: 16px; /* 字体大小 */
  color: #333; /* 默认字体颜色 */
  border-radius: 8px; /* 圆角 */
}

.el-menu-item:hover {
  background-color: #f0f0f0; /* 悬停时背景颜色 */
}

.el-menu-item.is-active {
  background-color: #007bff; /* 选中项背景颜色 */
  color: white; /* 选中项字体颜色 */
  border-radius: 8px; /* 圆角 */
}
</style>

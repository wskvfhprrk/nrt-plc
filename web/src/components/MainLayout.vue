<!-- components/MainLayout.vue -->
<template>
  <el-container>
    <el-header v-if="!isShow"><Head /></el-header>
    <el-container>
      <!-- 只有不是 /orderPage 的路由才显示菜单 -->
      <el-aside v-if="!isShow" width="200px">
        <Menu />
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import Menu from '@/components/Menu.vue';
import Head from '@/components/Head.vue';

export default {
  name: 'MainLayout',
  components: {
    Menu,
    Head
  },
  computed: {
    // 判断当前路由是否展示菜单和项部时才展示
    isShow() {
      return this.$route.path === '/';
    }
  }
}
</script>

<style scoped>
el-container{
  background-color: var(--beige);
}
</style>

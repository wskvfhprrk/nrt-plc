<template>
  <div class="container">
    <!-- Robot Operations -->
    <div class="button-group">
      <h3 class="section-title">机器人操作</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup1" :key="`btn1-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Door Control -->
    <div class="button-group">
      <h3 class="section-title">门锁控制</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup2" :key="`btn2-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Food Processing -->
    <div class="button-group">
      <h3 class="section-title">餐品处理</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup3" :key="`btn3-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Service Control -->
    <div class="button-group">
      <h3 class="section-title">出餐控制</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup4" :key="`btn4-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Reserved Group 5 -->
    <div class="button-group" v-if="buttonsGroup5.length > 0">
      <h3 class="section-title">预留组5</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup5" :key="`btn5-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Reserved Group 6 -->
    <div class="button-group" v-if="buttonsGroup6.length > 0">
      <h3 class="section-title">预留组6</h3>
      <div class="button-columns">
        <div v-for="(btn, index) in buttonsGroup6" :key="`btn6-${index}`" class="button-column">
          <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
            {{ btn.name }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Fixed Buttons -->
    <div class="fixed-buttons">
      <el-button type="success" class="reset-button" @click="resetSystem">复位</el-button>
      <el-button type="danger" class="emergency-button" @click="emergencyStop">急停</el-button>
    </div>

    <!-- Dialog for Input Parameters -->
    <el-dialog title="输入参数" v-model="dialogVisible" :before-close="handleClose">
      <el-input v-model="parameter" placeholder="请输入参数"></el-input>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitParameter">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';


export default {
  name: 'ButtonsPage',
  data() {
    return {
      buttonsGroup1: [
        {id: 1, name: "机器人取粉丝1"},
        {id: 2, name: "机器人取粉丝2"},
        {id: 3, name: "机器人取粉丝3"},
        {id: 4, name: "机器人取粉丝4"},
        {id: 5, name: "机器人取粉丝5"},
        {id: 6, name: "机器人取粉丝6"},
        {id: 7, name: "机器人放碗"},
        {id: 8, name: "机器人出汤"},
        {id: 9, name: "机器人口汤牛肉"},
        {id: 10, name: "机器人倒牛肉"}
      ],
      buttonsGroup2: [
        {id: 11, name: "打开门锁1"},
        {id: 12, name: "打开门锁2"},
        {id: 13, name: "打开门锁3"},
        {id: 14, name: "打开门锁4"},
        {id: 15, name: "打开门锁5"},
        {id: 16, name: "打开门锁6"},
        {id: 17, name: "打开门锁7"}
      ],
      buttonsGroup3: [
        {id: 18, name: "切牛肉测试"},
        {id: 19, name: "汤加热"},
        {id: 20, name: "称重测试"}
      ],
      buttonsGroup4: [
        {id: 21, name: "出碗测试"},
        {id: 22, name: "打开出餐口"},
        {id: 23, name: "关闭出餐口"}
      ],
      buttonsGroup5: [],
      buttonsGroup6: [],
      dialogVisible: false,
      parameter: '',
      currentButtonId: null,
      currentButtonName: ''
    };
  },
  computed: {
    buttonStyle() {
      return {
        width: '200px',  // 所有按钮宽度设置为200px
      };
    }
  },
  methods: {
    async sendRequest(url) {
      try {
        const response = await axios.get(url);
        this.$message.success(`操作成功：${response.data}`);
        console.log(response.data);
      } catch (error) {
        this.$message.error('操作失败');
        console.error(error);
      }
    },
    openDialog(id, name) {
      if (name.includes('（') && name.includes('）')) {
        this.dialogVisible = true;
      } else {
        this.dialogVisible = false;
        this.sendRequest(`buttonAction/${id}`);
      }
      this.currentButtonId = id;
      this.currentButtonName = name;
    },
    handleClose() {
      this.dialogVisible = false;
    },
    submitParameter() {
      const url = `buttonAction/${this.currentButtonId}?number=${this.parameter}`;
      this.sendRequest(url);
      this.dialogVisible = false;
      this.parameter = '';
    },
    emergencyStop() {
      this.sendRequest(`buttonAction/emergencyStop`);
    },
    resetSystem() {
      this.sendRequest(`buttonAction/reset`);
    }
  }
};
</script>

<style scoped>
.container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
  grid-gap: 20px;
  padding: 20px;
  height: 100vh;
  box-sizing: border-box;
}

.button-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #ddd;
  padding: 10px;
  box-sizing: border-box;
  border-radius: 12px; /* 圆角设置 */
  overflow: hidden;    /* 防止子元素溢出父容器的圆角 */
}

.button-columns {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-title {
  color: red;
  background-color: white;
  padding: 5px 10px;
  border-radius: 5px;
  text-align: center;
  margin-bottom: 10px;
}

.el-button {
  margin: 5px 0;
}

.fixed-buttons {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.reset-button {
  background-color: green;
  color: white;
  border: none;
  margin-bottom: 10px;
}

.emergency-button {
  background-color: red;
  color: white;
  border: none;
}
</style>

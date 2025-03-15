<template>
  <div class="container">
    <div class="button-layout">
      <!-- 模式切换 -->
      <div class="button-group">
        <h3 class="section-title">模式切换</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in modeButtons" :key="`mode-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 柜灯控制 -->
      <div class="button-group">
        <h3 class="section-title">柜灯控制</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in cabinetLightButtons" :key="`light-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 取料操作 -->
      <div class="button-group">
        <h3 class="section-title">取料操作</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in materialButtons" :key="`material-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 切肉设置 -->
      <div class="button-group">
        <h3 class="section-title">切肉设置</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in meatCutButtons" :key="`meatcut-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 汤品操作 -->
      <div class="button-group">
        <h3 class="section-title">汤品操作</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in soupButtons" :key="`soup-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 餐具操作 -->
      <div class="button-group">
        <h3 class="section-title">餐具操作</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in tablewareButtons" :key="`tableware-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 消毒操作 -->
      <div class="button-group">
        <h3 class="section-title">消毒操作</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in disinfectButtons" :key="`disinfect-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 称重设置 -->
      <div class="button-group">
        <h3 class="section-title">称重设置</h3>
        <div class="button-columns">
          <div v-for="(btn, index) in weightButtons" :key="`weight-${index}`" class="button-column">
            <el-button type="primary" :style="buttonStyle" @click="openDialog(btn.id, btn.name)">
              {{ btn.name }}
            </el-button>
          </div>
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
      <template >
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
      // 模式切换
      modeButtons: [
        {id: 0, name: "切换为自动控制"}
      ],
      // 柜灯控制
      cabinetLightButtons: [
        {id: 23, name: "打开柜灯"},
        {id: 24, name: "关闭柜灯"}
      ],
      // 取料操作
      materialButtons: [
        {id: 1, name: "机器人取1号粉丝"},
        {id: 2, name: "机器人取2号粉丝"},
        {id: 3, name: "机器人取3号粉丝"},
        {id: 4, name: "机器人取4号粉丝"},
        {id: 5, name: "机器人取5号粉丝"},
        {id: 6, name: "机器人取6号粉丝"}
      ],
      // 切肉设置
      meatCutButtons: [
        {id: 7, name: "切肉设置价格1"},
        {id: 8, name: "切肉设置价格2"},
        {id: 9, name: "切肉设置价格3"},
        {id: 10, name: "切肉设置价格4"},
        {id: 11, name: "切肉设置价格5"}
      ],
      // 汤品操作
      soupButtons: [
        {id: 12, name: "加汤"},
        {id: 13, name: "汤加热到设置温度"}
      ],
      // 餐具操作
      tablewareButtons: [
        {id: 14, name: "机器人取碗"},
        {id: 15, name: "机器人放碗"},
        {id: 16, name: "机器人出餐"}
      ],
      // 消毒操作
      disinfectButtons: [
        {id: 17, name: "打开蒸汽消毒"}
      ],
      // 称重设置
      weightButtons: [
        {id: 18, name: "称重设置1"},
        {id: 19, name: "称重设置2"},
        {id: 20, name: "称重设置3"},
        {id: 21, name: "称重设置4"},
        {id: 22, name: "称重设置5"}
      ],
      dialogVisible: false,
      parameter: '',
      currentButtonId: null,
      currentButtonName: ''
    };
  },
  computed: {
    buttonStyle() {
      return {
        width: '50%',  // 按钮宽度从100%改为50%
      };
    }
  },
  methods: {
    async sendRequest(url) {
      try {
        const response = await axios.get(`${url}?isNewOrder=false`);
        this.$message.success(`操作成功：${response.data}`);
        console.log(response.data);
      } catch (error) {
        this.$message.error('操作失败');
        console.error(error);
      }
    },
    async sendManualCommand(commandId, commandName, params = {}) {
      try {
        // 构建请求参数
        const requestParams = {
          commandId,
          commandName,
          isNewOrder: false,
          ...params
        };
        
        // 发送请求到后端
        const response = await axios.post('/machines/manual-command', requestParams);
        
        if (response.data.code === 200) {
          this.$message.success(`${commandName}操作成功`);
          console.log('手动指令执行成功:', response.data);
          return response.data;
        } else {
          this.$message.error(`${commandName}操作失败: ${response.data.msg}`);
          console.error('手动指令执行失败:', response.data);
          return response.data;
        }
      } catch (error) {
        this.$message.error(`${commandName}操作失败: ${error.message}`);
        console.error('发送手动指令时出错:', error);
        throw error;
      }
    },
    openDialog(id, name) {
      this.currentButtonId = id;
      this.currentButtonName = name;
      
      if (name.includes('（') && name.includes('）')) {
        this.dialogVisible = true;
      } else {
        this.dialogVisible = false;
        // 使用新的手动指令方法
        this.sendManualCommand(id, name);
      }
    },
    handleClose() {
      this.dialogVisible = false;
    },
    submitParameter() {
      // 使用新的手动指令方法，并传递参数
      this.sendManualCommand(this.currentButtonId, this.currentButtonName, {
        number: this.parameter
      });
      
      this.dialogVisible = false;
      this.parameter = '';
    },
    emergencyStop() {
      this.sendManualCommand('emergencyStop', '急停');
    },
    resetSystem() {
      this.sendManualCommand('reset', '复位');
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 20px;
  min-height: 100vh;
  box-sizing: border-box;
}

.button-layout {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 20px;
  margin-bottom: 60px; /* 为底部固定按钮留出空间 */
}

.button-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #ddd;
  padding: 15px;
  box-sizing: border-box;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  height: fit-content;
  width: 50%;
  margin-left: auto;
  margin-right: auto;
}

.section-title {
  color: #f56c6c;
  font-weight: bold;
  text-align: center;
  margin-bottom: 15px;
  width: 100%;
}

.button-columns {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.button-column {
  width: 100%;
  margin-bottom: 10px;
  display: flex;
  justify-content: center;
}


.fixed-buttons {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 100;
}

.reset-button {
  background-color: #67c23a;
  color: white;
  border: none;
  margin-bottom: 10px;
  width: 80px;
  height: 40px;
}

.emergency-button {
  background-color: #f56c6c;
  color: white;
  border: none;
  width: 80px;
  height: 40px;
}

/* 媒体查询，确保在小屏幕上也能正确显示 */
@media screen and (max-width: 768px) {
  .button-layout {
    grid-template-columns: 1fr;
  }
}
</style>

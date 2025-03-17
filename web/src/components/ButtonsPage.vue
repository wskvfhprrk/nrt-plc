<template>
  <div class="container">
    <!-- 顶部控制按钮区域 -->
    <div class="top-controls">
      <!-- 自动控制按钮 -->
      <el-button type="success" class="auto-control-btn" @click="openDialog(0, '切换为自动控制')">
        切换为自动控制
      </el-button>
      
      <!-- 灯光控制按钮 -->
      <div class="light-controls">
        <el-button type="primary" class="light-btn" @click="openDialog(32, '打开灯')">
          打开灯
        </el-button>
        <el-button type="warning" class="light-btn" @click="openDialog(33, '关闭灯')">
          关闭灯
        </el-button>
      </div>
    </div>

    <div class="button-layout">
      <!-- 左侧按钮组 -->
      <div class="button-column">
        <el-button v-for="btn in leftButtons" 
                   :key="btn.id" 
                   type="primary" 
                   class="control-btn"
                   @click="openDialog(btn.id, btn.name)">
          {{ btn.name }}
        </el-button>
      </div>

      <!-- 右侧按钮组 -->
      <div class="button-column">
        <el-button v-for="btn in rightButtons" 
                   :key="btn.id" 
                   type="primary" 
                   class="control-btn"
                   @click="openDialog(btn.id, btn.name)">
          {{ btn.name }}
        </el-button>
      </div>
    </div>

    <!-- 固定按钮 -->
    <div class="fixed-buttons">
      <el-button type="success" class="reset-button" @click="resetSystem">复位</el-button>
      <el-button type="danger" class="emergency-button" @click="emergencyStop">急停</el-button>
    </div>

    <!-- 参数输入对话框 -->
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
      leftButtons: [
        {id: 1, name: "1号粉丝粉丝"},
        {id: 2, name: "2号粉丝粉丝"},
        {id: 3, name: "3号粉丝粉丝"},
        {id: 4, name: "4号粉丝粉丝"},
        {id: 5, name: "5号粉丝粉丝"},
        {id: 6, name: "6号粉丝粉丝"},
        {id: 7, name: "机器人取碗1"},
        {id: 8, name: "机器人取碗2"},
        {id: 9, name: "机器人取碗3"},
        {id: 10, name: "机器人取碗4"},
        {id: 11, name: "机器人取碗5"},
        {id: 12, name: "机器人取碗6"},
        {id: 13, name: "切肉设置位1"},
        {id: 14, name: "切肉设置位2"},
        {id: 15, name: "切肉设置位3"},
        {id: 16, name: "切肉设置位4"},
        {id: 17, name: "切肉设置位5"},
        {id: 18, name: "抽汤到液位"},
        {id: 19, name: "上升汤盖"},
        {id: 20, name: "下降汤盖"},
        {id: 21, name: "汤加热到设置温度"},
        {id: 22, name: "出碗机出碗"}
      ],
      rightButtons: [
        {id: 23, name: "机器人取碗"},
        {id: 24, name: "机器人出餐"},
        {id: 25, name: "机器人汤牛肉"},
        {id: 26, name: "机器人倒牛肉"},
        {id: 27, name: "称重设置1"},
        {id: 28, name: "称重设置2"},
        {id: 29, name: "称重设置3"},
        {id: 30, name: "称重设置4"},
        {id: 31, name: "称重设置5"},
        {id: 32, name: "打开餐口"},
        {id: 33, name: "关闭餐口"},
        {id: 34, name: "打开门1"},
        {id: 35, name: "打开门2"},
        {id: 36, name: "打开门3"},
        {id: 37, name: "打开门4"},
        {id: 38, name: "打开门5"},
        {id: 39, name: "打开门6"},
        {id: 40, name: "打开蒸汽消毒"},
        {id: 41, name: "关闭蒸汽消毒"},
        {id: 42, name: "打开水泵"},
        {id: 43, name: "关闭水泵"}
      ],
      dialogVisible: false,
      parameter: '',
      currentButtonId: null,
      currentButtonName: ''
    };
  },
  methods: {
    async sendManualCommand(commandId, commandName, params = {}) {
      try {
        const requestParams = {
          commandId,
          commandName,
          isNewOrder: false,
          ...params
        };
        
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
      
      // 处理开关灯
      if (name === "打开灯") {
        this.sendManualCommand("lightOn", name);
        return;
      } else if (name === "关闭灯") {
        this.sendManualCommand("lightOff", name);
        return;
      }
      
      // 处理其他手动指令
      if (name.includes('（') && name.includes('）')) {
        this.dialogVisible = true;
      } else {
        this.dialogVisible = false;
        this.sendManualCommand(id, name);
      }
    },
    handleClose() {
      this.dialogVisible = false;
    },
    submitParameter() {
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

.top-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
  width: 100%;
}

.auto-control-btn {
  width: 100%;
  max-width: 500px;
  height: 50px;
  background-color: #67c23a;
  color: white;
  border: none;
  font-size: 18px;
  font-weight: bold;
}

.light-controls {
  display: flex;
  gap: 20px;
  justify-content: center;
  width: 100%;
  max-width: 500px;
}

.light-btn {
  flex: 1;
  height: 40px;
}

.button-layout {
  display: flex;
  justify-content: space-between;
  margin-bottom: 60px;
  gap: 20px;
  width: 100%;
}

.button-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.control-btn {
  width: 100%;
  height: 40px;
}


.fixed-buttons {
  position: fixed;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 100;
}

.reset-button {
  background-color: #67c23a;
  color: white;
  border: none;
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

/* 媒体查询 */
@media screen and (max-width: 768px) {
  .button-layout {
    flex-direction: column;
  }
  
  .button-column {
    width: 100%;
  }
  
  .auto-control-btn, .light-controls {
    width: 100%;
  }
}
</style>

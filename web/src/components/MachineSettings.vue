<template>
  <div class="machine-settings">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>机器设置</span>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px" class="settings-form">
        <!-- 温度设置 -->
        <el-form-item label="汤温设置">
          <el-row :gutter="20">
            <el-col :span="11">
              <el-input-number 
                v-model="form.minTemp" 
                :min="60" 
                :max="90" 
                label="最低温度">
              </el-input-number>
              <span class="unit">℃</span>
            </el-col>
            <el-col :span="11">
              <el-input-number 
                v-model="form.maxTemp" 
                :min="80" 
                :max="100" 
                label="最高温度">
              </el-input-number>
              <span class="unit">℃</span>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 份量设置 -->
        <el-form-item label="份量设置">
          <el-row :gutter="20">
            <el-col :span="11">
              <el-input-number 
                v-model="form.noodleWeight" 
                :min="50" 
                :max="200" 
                label="粉条重量">
              </el-input-number>
              <span class="unit">g</span>
            </el-col>
            <el-col :span="11">
              <el-input-number 
                v-model="form.soupVolume" 
                :min="200" 
                :max="500" 
                label="汤量">
              </el-input-number>
              <span class="unit">ml</span>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 时间设置 -->
        <el-form-item label="时间设置">
          <el-row :gutter="20">
            <el-col :span="11">
              <el-input-number 
                v-model="form.cleaningInterval" 
                :min="30" 
                :max="240" 
                label="清洗间隔">
              </el-input-number>
              <span class="unit">分钟</span>
            </el-col>
            <el-col :span="11">
              <el-input-number 
                v-model="form.cookingTime" 
                :min="1" 
                :max="10" 
                label="制作时间">
              </el-input-number>
              <span class="unit">分钟</span>
            </el-col>
          </el-row>
        </el-form-item>

        <!-- 其他设置 -->
        <el-form-item label="自动清洗">
          <el-switch v-model="form.autoClean"></el-switch>
        </el-form-item>

        <el-form-item label="夜间模式">
          <el-switch v-model="form.nightMode"></el-switch>
        </el-form-item>

        <el-form-item label="运行状态">
          <el-select v-model="form.status" placeholder="请选择运行状态">
            <el-option label="正常运行" value="running"></el-option>
            <el-option label="待机模式" value="standby"></el-option>
            <el-option label="停止运行" value="stopped"></el-option>
          </el-select>
        </el-form-item>

        <!-- 按钮组 -->
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button @click="resetSettings">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'; // 确保您已安装 axios

export default {
  name: 'MachineSettings',
  data() {
    return {
      form: {
        minTemp: 80,
        maxTemp: 95,
        noodleWeight: 100,
        soupVolume: 300,
        cleaningInterval: 60,
        cookingTime: 3,
        autoClean: true,
        nightMode: false,
        status: 'running'
      }
    }
  },
  created() {
    this.loadSettings(); // 组件创建时加载设置
  },
  methods: {
    loadSettings() {
      axios.get('/machine-settings/get')
        .then(response => {
          this.form = response.data.data; // 更新表单数据
        })
        .catch(error => {
          this.$message({
            message: '加载设置失败',
            type: 'error'
          });
        });
    },
    saveSettings() {
      axios.post('/machine-settings/save', this.form)
        .then(response => {
          this.$message({
            message: response.data.message,
            type: 'success'
          });
        })
        .catch(error => {
          this.$message({
            message: '保存设置失败',
            type: 'error'
          });
        });
    },
    resetSettings() {
      // 重置表单
      this.form = {
        minTemp: 80,
        maxTemp: 95,
        noodleWeight: 100,
        soupVolume: 300,
        cleaningInterval: 60,
        cookingTime: 3,
        autoClean: true,
        nightMode: false,
        status: 'running'
      };
      this.$message({
        message: '设置已重置',
        type: 'info'
      });
    }
  }
}
</script>

<style scoped>
.machine-settings {
  padding: 20px;
}

.settings-form {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unit {
  margin-left: 5px;
  color: #909399;
}

.el-row {
  margin-bottom: 10px;
}

.el-input-number {
  width: 120px;
}
</style> 
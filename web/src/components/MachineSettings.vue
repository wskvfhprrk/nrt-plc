<template>
  <div class="machine-settings-container">
      
    <el-form :model="form" label-width="180px" class="settings-form">
      <h4>机器设置</h4>  
      
      <!-- 自动清洗 -->
      <el-form-item label="自动清洗">
        <el-switch v-model="form.autoClean" active-text="开启" inactive-text="关闭"></el-switch>
      </el-form-item>

      <!-- 夜间模式 -->
      <el-form-item label="夜间模式">
        <el-switch v-model="form.nightMode" active-text="开启" inactive-text="关闭"></el-switch>
      </el-form-item>

      <!-- 开门锁通电时间 -->
      <el-form-item label="开门锁通电时间">
        <el-input-number v-model="form.openLockTime" :min="0" :max="120" />
        <span>毫秒</span>
        <span style="color: red;">（一般为50，不要随意更改，否则烧坏锁）</span>
      </el-form-item>

      <!-- 汤温度设置 -->
      <el-form-item label="汤最高温度">
        <el-input-number v-model="form.soupMaxTemperature" :min="0" :max="100" />
        <span>℃（出汤温度）</span>
      </el-form-item>
      <el-form-item label="汤最低温度">
        <el-input-number v-model="form.soupMinTemperature" :min="0" :max="100" />
        <span>℃（保温温度）</span>
      </el-form-item>

      <!-- 汤数量 -->
      <el-form-item label="汤数量">
        <el-input-number v-model="form.soupQuantity" :min="0" />
        <span>脉冲（出汤量多少值）</span>
      </el-form-item>

      <!-- 排油烟风扇通风的时间 -->
      <el-form-item label="排油烟风扇通风的时间">
        <el-input-number v-model="form.fanVentilationTime" :min="0" />
        <span>秒</span>
      </el-form-item>

      <!-- 电柜风扇通风的温度值 -->
      <el-form-item label="电柜风扇通风的温度值">
        <el-input-number v-model="form.electricalBoxFanTemp" :min="0" :max="100" />
        <span>℃（电箱到达温度会启动风扇）</span>
      </el-form-item>

      <!-- 电柜风扇通风的湿度值 -->
      <el-form-item label="电柜风扇通风的湿度值">
        <el-input-number v-model="form.electricalBoxFanHumidity" :min="0" :max="100" />
        <span>%（电箱到达湿度会启动风扇）</span>
      </el-form-item>

      <!-- 价格设置 -->
      <h4>价格设置</h4>
      <el-form-item label="价格1">
        <el-input-number v-model="form.price1" :min="0" />
        <span>份（价格1的份量）</span>
      </el-form-item>
      <el-form-item label="价格2">
        <el-input-number v-model="form.price2" :min="0" />
        <span>份（价格2的份量）</span>
      </el-form-item>
      <el-form-item label="价格3">
        <el-input-number v-model="form.price3" :min="0" />
        <span>份（价格3的份量）</span>
      </el-form-item>
      <el-form-item label="价格4">
        <el-input-number v-model="form.price4" :min="0" />
        <span>份（价格4的份量）</span>
      </el-form-item>
      <el-form-item label="价格5">
        <el-input-number v-model="form.price5" :min="0" />
        <span>份（价格5的份量）</span>
      </el-form-item>

      <!-- 配料设置 -->
      <h4>配料设置</h4>
      <el-form-item label="配料1重量">
        <el-input-number v-model="form.ingredient1Weight" :min="0" />
        <span>g（0为不用称重 ）</span>
      </el-form-item>
      <el-form-item label="配料2重量">
        <el-input-number v-model="form.ingredient2Weight" :min="0" />
        <span>g（0为不用称重 ）</span>
      </el-form-item>
      <el-form-item label="配料3重量">
        <el-input-number v-model="form.ingredient3Weight" :min="0" />
        <span>g（0为不用称重 ）</span>
      </el-form-item>
      <el-form-item label="配料4重量">
        <el-input-number v-model="form.ingredient4Weight" :min="0" />
        <span>g（0为不用称重 ）</span>
      </el-form-item>
      <el-form-item label="配料5重量">
        <el-input-number v-model="form.ingredient5Weight" :min="0" />
        <span>g（0为不用称重 ）</span>
      </el-form-item>

      <!-- 按钮组 -->
      <el-form-item>
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
        <el-button @click="resetSettings">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios'; // 确保您已安装 axios

export default {
  name: 'MachineSettings',
  data() {
    return {
      form: {       
        autoClean: false, // 默认自动清洗关闭
        nightMode: false, // 默认夜间模式关闭
        openLockTime: 50,
        soupMaxTemperature: 90, // 汤最高温度
        soupMinTemperature: 70, // 汤最低温度
        soupQuantity: 10,
        fanVentilationTime: 60,
        electricalBoxFanTemp: 40,
        electricalBoxFanHumidity: 60,
        price1: 10,
        price2: 15,
        price3: 20,
        price4: 25,
        price5: 30,
        ingredient1Weight: 100,
        ingredient2Weight: 150,
        ingredient3Weight: 200,
        ingredient4Weight: 250,
        ingredient5Weight: 300,
      }
    }
  },
  created() {
    this.loadSettings(); // 组件创建时加载设置
  },
  methods: {
    loadSettings() {
      axios.get('/machines/get')
        .then(response => {
          if (response.data.code === 200) {
            const backendData = response.data.data;
            // 映射后台数据到表单
            this.form = {
              ...this.form, // 保留原有默认值
              autoClean: backendData.autoClean,
              nightMode: backendData.nightMode,
              soupMaxTemperature: backendData.maxTemp,
              soupMinTemperature: backendData.minTemp,
              soupQuantity: backendData.soupVolume,
              // 其他字段保持默认值
              electricalBoxFanTemp: backendData.electricalBoxTemp,
              electricalBoxFanHumidity: backendData.electricalBoxHumidity
            };
          } else {
            this.$message.error(response.data.message);
          }
        })
        .catch(error => {
          this.$message.error(error.response?.data?.message || '系统错误');
        });
    },
    saveSettings() {
      axios.post('/machines/save', this.form)
        .then(response => {
          if (response.data.code === 200) {
            this.$message({
              message: response.data.message,
              type: 'success'
            });
          } else {
            this.$message.error(response.data.message);
          }
        })
        .catch(error => {
          this.$message.error(error.response?.data?.message || '系统错误');
        });
    },
    resetSettings() {
      this.loadSettings(); // 从后台获取默认设置
      this.$message({
        message: '设置已重置',
        type: 'info'
      });
    }
  }
}
</script>

<style scoped>
.machine-settings-container {
  padding: 20px;
  display: flex;
  justify-content: center; /* 居中 */
}

.settings-form {
  width: 80%; /* 设置宽度为当前的一半 */
}

.settings-form h4 {
  margin-top: 100px;
}
</style> 
<template>
  <div class="machine-status-container" style="margin-top: 10px;">
    <h3>机器运行状态</h3>
    <!-- 机器状态区域 -->
    <el-card class="status-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <span class="label">机器状态：</span>
            <el-tag :type="machineData.basicStatus?.machineStatus === '运行中' ? 'success' : 'danger'">
              {{ machineData.basicStatus?.machineStatus || '未知' }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">牛肉汤温度：</span>
            <span class="value" :style="{ color: machineData.basicStatus.currentTemperature > 80 ? 'red' : 'black' }">{{ machineData.basicStatus.currentTemperature }}°C</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">菜重量：</span>
            <span class="value">{{ machineData.basicStatus.currentWeight }} g</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">运行时间：</span>
            <span class="value">{{ machineData.basicStatus.uptime }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 机器人状态区域 -->
    <el-card class="robot-status-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="status-item">
            <span class="label">机器人状态：</span>
            <el-tag :type="getRobotStatusType(machineData.robotStatus.robotStatus)">
              {{ machineData.robotStatus.robotStatus }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="status-item">
            <span class="label">机器人运行程序：</span>
            <span class="program-value">{{ machineData.robotStatus.currentProgram }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 电箱状态区域 -->
    <el-card class="electrical-box-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="status-item">
            <span class="label">电箱温度：</span>
            <span class="value">{{ machineData.electricalBoxStatus.temperature }}°C</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <span class="label">电箱湿度：</span>
            <span class="value">{{ machineData.electricalBoxStatus.humidity }}%</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <span class="label">电箱状态：</span>
            <el-tag :type="getElectricalBoxStatusType()">
              {{ getElectricalBoxStatus() }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 设备输入点状态 -->
    <el-card class="components-card">
      <h4>设备输入点状态</h4>
      <div v-if="machineData.inputPoints.length > 0" style="display: flex; justify-content: space-between;">
        <el-table :data="machineData.inputPoints.slice(0, Math.ceil(machineData.inputPoints.length / 2))" style="width: 48%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="pointName" label="点位" />
          <el-table-column prop="status" label="状态值">
            <template #default="{ row }">
              <el-tag :type="row.status === '打开' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-table :data="machineData.inputPoints.slice(Math.ceil(machineData.inputPoints.length / 2))" style="width: 48%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="pointName" label="点位" />
          <el-table-column prop="status" label="状态值">
            <template #default="{ row }">
              <el-tag :type="row.status === '打开' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else>没有输入点数据</div>
    </el-card>

    <!-- 设备输出点状态 -->
    <el-card class="components-card">
      <h4>设备输出点状态</h4>
      <div v-if="machineData.outputPoints.length > 0" style="display: flex; justify-content: space-between;">
        <el-table :data="machineData.outputPoints.slice(0, Math.ceil(machineData.outputPoints.length / 2))" style="width: 48%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="pointName" label="点位" />
          <el-table-column prop="status" label="状态值">
            <template #default="{ row }">
              <el-tag :type="row.status === '打开' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-table :data="machineData.outputPoints.slice(Math.ceil(machineData.outputPoints.length / 2))" style="width: 48%">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="pointName" label="点位" />
          <el-table-column prop="status" label="状态值">
            <template #default="{ row }">
              <el-tag :type="row.status === '打开' ? 'success' : 'danger'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else>没有输出点数据</div>
    </el-card>

    <!-- 报警信息模块 -->
    <el-card class="alerts-card">
      <h4>报警信息</h4>
      <el-table :data="machineData.alerts" style="width: 100%">
        <el-table-column prop="time" label="时间" />
        <el-table-column prop="level" label="报警级别">
          <template #default="{ row }">
            <el-tag :type="getAlertLevelType(row.level)">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="报警信息" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.resolved ? 'success' : 'warning'">
              {{ row.resolved ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="!row.resolved" @click="resetAlert(row)" type="danger" size="small">复位</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'MachineStatus',
  data() {
    return {
      machineData: {
        basicStatus: {
          machineStatus: '未知',
          currentTemperature: 0,
          currentWeight: 0,
          uptime: ''
        },
        robotStatus: {
          robotStatus: '未知',
          currentProgram: ''
        },
        electricalBoxStatus: {
          temperature: 0,
          humidity: 0
        },
        inputPoints: [],
        outputPoints: [],
        alerts: []
      }
    }
  },
  methods: {
    getRobotStatusType(status) {
      switch (status) {
        case '未使能':
          return 'info';
        case '空闲':
          return 'success';
        case '程序运行状态':
          return 'success';
        case '暂停状态':
          return 'warning';
        case '程序结束':
          return 'danger';
        case '碰撞':
          return 'danger';
        case '机械劈运动中':
          return 'warning';
        default:
          return 'info';
      }
    },
    getAlertLevelType(level) {
      switch (level) {
        case '一级':
          return 'danger';
        case '二级':
          return 'warning';
        default:
          return 'info';
      }
    },
    async resetAlert(row) {
      try {
        const response = await fetch('/machines/alerts/reset', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ time: row.time })
        });
        
        if (response.ok) {
          row.resolved = true;
          this.$message.success('复位成功');
        } else {
          this.$message.error('复位失败');
        }
      } catch (error) {
        console.error('复位请求失败:', error);
        this.$message.error('网络请求失败');
      }
    },
    getElectricalBoxStatus() {
      if (this.machineData.electricalBoxStatus.temperature > 40) {
        return '过热';
      }
      if (this.machineData.electricalBoxStatus.humidity > 90) {
        return '过潮';
      }
      return '正常';
    },
    getElectricalBoxStatusType() {
      const status = this.getElectricalBoxStatus();
      switch (status) {
        case '过热':
        case '过潮':
          return 'danger';
        case '正常':
          return 'success';
        default:
          return 'info';
      }
    },
    async fetchMachineStatus() {
      try {
        const response = await fetch('/machines/status');
        const result = await response.json();
        
        // 添加数据兼容处理
        const backendData = result.data || {};
        
        console.log('Backend data:', backendData);
        
        // 分离输入和输出点
        const inputPoints = backendData.inputPoints || [];
        const outputPoints = backendData.outputPoints || [];

        this.machineData = {
          ...this.machineData,
          basicStatus: {
            ...this.machineData.basicStatus,
            machineStatus: backendData.machineStatus || '未知',
            currentTemperature: backendData.temperature || 0,
            currentWeight: backendData.noodleWeight || 0,
            uptime: `${backendData.cleaningInterval || 0}分钟`
          },
          robotStatus: {
            ...this.machineData.robotStatus,
            robotStatus: this.translateStatus(backendData.status) || '待实现',
            currentProgram: backendData.currentProgram || '待实现'
          },
          electricalBoxStatus: {
            ...this.machineData.electricalBoxStatus,
            temperature: backendData.temperature || 0,
            humidity: backendData.humidity || 0
          },
          inputPoints: inputPoints,
          outputPoints: outputPoints,
          alerts: backendData.alerts || [] // 处理报警信息
        };

        console.log('Merged data:', this.machineData);
      } catch (error) {
        console.error('获取机器状态失败:', error);
        // 可添加重试机制
      }
    },
    translateStatus(status) {
      const statusMap = {
        'running': '运行中',
        'standby': '待机中',
        'stopped': '已停止'
      };
      return statusMap[status] || status;
    }
  },
  mounted() {
    this.fetchMachineStatus();
    // 设置定时刷新
    setInterval(() => {
      console.log('Fetching machine status...');
      this.fetchMachineStatus();
    }, 5000); // 每5秒刷新一次
  }
};
</script>

<style scoped>
.machine-status-container {
  padding: 20px;
}

.status-card,
.robot-status-card,
.electrical-box-card,
.components-card,
.alerts-card {
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
}

.program-item {
  justify-content: space-between;
}

.label {
  margin-right: 10px;
  font-weight: bold;
}

.value {
  color: #409EFF;
}

.program-value {
  color: #409EFF;
}

h3 {
  margin-bottom: 20px;
  text-align: center;
}

h4 {
  margin-bottom: 15px;
}
</style> 
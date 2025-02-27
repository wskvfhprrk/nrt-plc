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

    <!-- 人工区域状态 -->
    <el-card class="manual-status-card">      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="status-item">
            <span class="label">自动清洗：</span>
            <el-tag :type="machineData.robotStatus.autoClean ? 'success' : 'info'">
              {{ machineData.robotStatus.autoClean ? '已开启' : '已关闭' }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="status-item">
            <span class="label">夜间模式：</span>
            <el-tag :type="machineData.robotStatus.nightMode ? 'success' : 'info'">
              {{ machineData.robotStatus.nightMode ? '已开启' : '已关闭' }}
            </el-tag>
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
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
        <h4 style="margin: 0;">报警信息</h4>
        <el-button 
          type="danger" 
          size="small" 
          @click="clearAllAlerts"
          :disabled="!machineData.alerts.length"
        >
          清除报警信息
        </el-button>
      </div>
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
      currentTime: '',
      machineData: {
        basicStatus: {
          machineStatus: '未知',
          currentTemperature: 0,
          currentWeight: 0,
          uptime: ''
        },
        robotStatus: {
          robotStatus: '未知',
          currentProgram: '',
          autoClean: false,
          nightMode: false
        },
        electricalBoxStatus: {
          temperature: 0,
          humidity: 0,
          status: 0
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
        const response = await fetch(`/machines/alerts/reset?alertId=${row.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          }
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
      const status = this.machineData.electricalBoxStatus.status;
      switch (status) {
        case 0:
          return '正常';
        case 1:
          return '过热';
        case 2:
          return '过潮';
        default:
          return '未知';
      }
    },
    getElectricalBoxStatusType() {
      const status = this.machineData.electricalBoxStatus.status;
      switch (status) {
        case 0:
          return 'success';
        case 1:
        case 2:
          return 'danger';
        default:
          return 'info';
      }
    },
    formatRuntime(hours) {
      if (!hours && hours !== 0) return '未知';
      
      const days = Math.floor(hours / 24);
      const remainingHours = Math.floor(hours % 24);
      
      if (days > 0) {
        return `${days}天${remainingHours}小时`;
      }
      return `${remainingHours}小时`;
    },
    async fetchMachineStatus() {
      try {
        const response = await fetch('/machines/status');
        const result = await response.json();
        const backendData = result.data || {};
        
        // 添加调试日志
        console.log('后端原始数据:', {
          temp: backendData.electricalBoxTemp,
          humidity: backendData.electricalBoxHumidity
        });

        const alerts = (backendData.alerts || []).map(alert => ({
          ...alert,
          time: this.formatDateTime(alert.timestamp)
        }));

        this.machineData = {
          ...this.machineData,
          basicStatus: {
            ...this.machineData.basicStatus,
            machineStatus: backendData.machineStatus || '未知',
            currentTemperature: backendData.temperature || 0,
            currentWeight: backendData.weight || 0,
            uptime: this.formatRuntime(backendData.runtime)
          },
          robotStatus: {
            ...this.machineData.robotStatus,
            robotStatus: this.translateStatus(backendData.robotStatus) || '待实现',
            currentProgram: backendData.currentProgram || '待实现',
            autoClean: backendData.autoClean || false,
            nightMode: backendData.nightMode || false
          },
          electricalBoxStatus: {
            ...this.machineData.electricalBoxStatus,
            temperature: backendData.electricalBoxTemp || 0,
            humidity: backendData.electricalBoxHumidity || 0,
            status: backendData.electricalBoxStatus || 0
          },
          inputPoints: backendData.inputPoints || [],
          outputPoints: backendData.outputPoints || [],
          alerts: alerts
        };

        // 添加调试日志
        console.log('更新后的数据:', this.machineData.electricalBoxStatus);

      } catch (error) {
        console.error('获取机器状态失败:', error);
      }
    },
    translateStatus(status) {
      const statusMap = {
        'running': '运行中',
        'standby': '待机中',
        'stopped': '已停止'
      };
      return statusMap[status] || status;
    },
    updateCurrentTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      
      this.currentTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    formatDateTime(timestamp) {
      if (!timestamp) return '未知时间';
      
      try {
        // 后台传来的格式已经是 "2023-10-01 10:00" 这样的格式
        // 我们只需要验证一下格式是否正确，如果正确就直接返回
        if (typeof timestamp === 'string' && timestamp.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
          return timestamp; // 如果格式正确，直接返回
        }
        
        // 如果格式不正确，尝试其他格式化方式
        const date = new Date(timestamp);
        if (!isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          const hours = String(date.getHours()).padStart(2, '0');
          const minutes = String(date.getMinutes()).padStart(2, '0');
          
          return `${year}-${month}-${day} ${hours}:${minutes}`;
        }
        
        return timestamp; // 如果都不成功，返回原始值
      } catch (error) {
        console.error('时间格式化失败:', error);
        return timestamp; // 发生错误时返回原始值
      }
    },
    async clearAllAlerts() {
      try {
        const response = await fetch('/machines/alerts/clear', {
          method: 'DELETE'
        });
        
        if (response.ok) {
          this.machineData.alerts = [];
          this.$message({
            type: 'success',
            message: '报警信息已清除'
          });
        } else {
          this.$message.error('清除报警信息失败');
        }
      } catch (error) {
        console.error('清除报警信息失败:', error);
        this.$message.error('网络请求失败');
      }
    }
  },
  mounted() {
    this.fetchMachineStatus();
    // 设置定时刷新机器状态
    setInterval(() => {
      this.fetchMachineStatus();
    }, 5000);
    
    // 设置时间更新
    this.updateCurrentTime(); // 初始化时间
    setInterval(() => {
      this.updateCurrentTime();
    }, 1000); // 每秒更新一次时间
  }
};
</script>

<style scoped>
.machine-status-container {
  padding: 20px;
}

.status-card,
.robot-status-card,
.manual-status-card,
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

.alerts-card h4 {
  margin: 0;
}
</style> 
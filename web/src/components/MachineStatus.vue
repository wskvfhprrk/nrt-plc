<template>
  <div class="machine-status-container" style="margin-top: 10px;">
    <h3>机器运行状态</h3>
    <!-- 机器状态区域 -->
    <el-card class="status-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <span class="label">机器状态：</span>
            <el-tag :type="machineData.machineStatus === '运行中' ? 'success' : 'danger'">
              {{ machineData.machineStatus }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">牛肉汤温度：</span>
            <span class="value" :style="{ color: machineData.currentTemperature > 80 ? 'red' : 'black' }">{{ machineData.currentTemperature }}°C</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">菜重量：</span>
            <span class="value">{{ machineData.currentWeight }} g</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <span class="label">运行时间：</span>
            <span class="value">{{ machineData.uptime }}</span>
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
            <el-tag :type="getRobotStatusType(machineData.robotStatus)">
              {{ machineData.robotStatus }}
            </el-tag>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="status-item">
            <span class="label">机器人运行程序：</span>
            <span class="program-value">{{ machineData.currentProgram }}</span>
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
            <span class="value">{{ machineData.electricalBoxTemperature }}°C</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <span class="label">电箱湿度：</span>
            <span class="value">{{ machineData.electricalBoxHumidity }}%</span>
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
      <div style="display: flex; justify-content: space-between;">
        <el-table :data="machineData.inputPointsStatusLeft" style="width: 48%">
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

        <el-table :data="machineData.inputPointsStatusRight" style="width: 48%">
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
    </el-card>

    <!-- 设备输出点状态 -->
    <el-card class="components-card">
      <h4>设备输出点状态</h4>
      <div style="display: flex; justify-content: space-between;">
        <el-table :data="machineData.outputPointsStatusLeft" style="width: 48%">
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

        <el-table :data="machineData.outputPointsStatusRight" style="width: 48%">
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
    </el-card>

    <!-- 报警信息模块 -->
    <el-card class="alerts-card">
      <h4>报警信息</h4>
      <el-table :data="machineData.alerts" style="width: 100%">
        <el-table-column prop="time" label="时间" />
        <el-table-column prop="level" label="报警级别">
          <template #default="{ row }">
            <el-tag :type="row.level === '一级' ? 'danger' : ''">
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
            <el-button v-if="!row.resolved" @click="resetAlert(row)" type="danger" size="mini">复位</el-button>
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
        machineStatus: '运行中',
        currentTemperature: 85,
        uptime: '12小时34分钟',
        robotStatus: '运行中',
        currentProgram: '取粉丝',
        inputPointsStatusLeft: [
          { name: '粉丝到位传感器', pointName: 'V1.0', status: '打开' },
          { name: '粉丝气缸1', pointName: 'V1.1', status: '关闭' },
          { name: '粉丝气缸2', pointName: 'V1.2', status: '打开' },
          { name: '粉丝气缸3', pointName: 'V1.3', status: '关闭' },
          { name: '粉丝气缸4', pointName: 'V1.4', status: '打开' },
          { name: '粉丝气缸5', pointName: 'V1.5', status: '关闭' },
          { name: '粉丝气缸6', pointName: 'V1.6', status: '打开' },
          { name: '出碗检测', pointName: 'V1.7', status: '关闭' },
          { name: '是否有碗', pointName: 'V2.0', status: '打开' },
          { name: '碗报警', pointName: 'V2.1', status: '关闭' },
          { name: '出碗电机', pointName: 'V2.2', status: '打开' }
        ],
        inputPointsStatusRight: [
          { name: '做汤机气缸', pointName: 'V2.3', status: '关闭' },
          { name: '流量计', pointName: 'V2.4', status: '打开' },
          { name: '出餐口气缸', pointName: 'V2.5', status: '关闭' },
          { name: '推碗气缸', pointName: 'V2.6', status: '打开' },
          { name: '门锁1', pointName: 'V2.7', status: '关闭' },
          { name: '门锁2', pointName: 'V3.0', status: '打开' },
          { name: '门锁3', pointName: 'V3.1', status: '关闭' },
          { name: '门锁4', pointName: 'V3.2', status: '打开' },
          { name: '门锁5', pointName: 'V3.3', status: '关闭' },
          { name: '门锁6', pointName: 'V3.4', status: '打开' },
          { name: '门锁7', pointName: 'V3.5', status: '关闭' }
        ],
        alerts: [
          { time: '2023-10-01 11:30', message: '称重系统异常', level: '一级', resolved: false },
          { time: '2023-10-01 10:00', message: '温度过高', level: '二级', resolved: true }
        ],
        electricalBoxTemperature: 35,
        electricalBoxHumidity: 85,
        outputPointsStatusLeft: [
          { name: '粉丝仓气缸1', pointName: 'V7.0', status: '打开' },
          { name: '粉丝仓气缸2', pointName: 'V7.1', status: '关闭' },
          { name: '粉丝仓气缸3', pointName: 'V7.2', status: '打开' },
          { name: '粉丝仓气缸4', pointName: 'V7.3', status: '关闭' },
          { name: '粉丝仓气缸5', pointName: 'V7.4', status: '打开' },
          { name: '粉丝仓气缸6', pointName: 'V7.5', status: '关闭' },
          { name: '做汤机气缸', pointName: 'V7.6', status: '打开' },
          { name: '出餐口气缸', pointName: 'V7.7', status: '关闭' },
          { name: '推碗气缸', pointName: 'V8.0', status: '打开' },
          { name: '夹手气缸', pointName: 'V8.1', status: '关闭' },
          { name: '旋转气缸', pointName: 'V8.2', status: '打开' },
          { name: '切网机气缸', pointName: 'V8.3', status: '关闭' },
          { name: '切肉机气缸', pointName: 'V8.4', status: '打开' },
          { name: '汤桶加热蒸汽阀', pointName: 'V8.5', status: '关闭' },
          { name: '出汤电磁阀', pointName: 'V8.6', status: '打开' },
          { name: '消毒蒸汽阀', pointName: 'V8.7', status: '关闭' }
        ],
        outputPointsStatusRight: [
          { name: '备用3', pointName: 'V9.0', status: '打开' },
          { name: '备用4', pointName: 'V9.1', status: '关闭' },
          { name: '备用5', pointName: 'V9.2', status: '打开' },
          { name: '备用6', pointName: 'V9.3', status: '关闭' },
          { name: '备用7', pointName: 'V9.4', status: '打开' },
          { name: '备用8', pointName: 'V9.5', status: '关闭' },
          { name: '备用9', pointName: 'V9.6', status: '打开' },
          { name: '备用10', pointName: 'V9.7', status: '关闭' },
          { name: '门锁1', pointName: 'V10.0', status: '打开' },
          { name: '门锁2', pointName: 'V10.1', status: '关闭' },
          { name: '门锁3', pointName: 'V10.2', status: '打开' },
          { name: '门锁4', pointName: 'V10.3', status: '关闭' },
          { name: '门锁5', pointName: 'V10.4', status: '打开' },
          { name: '门锁6', pointName: 'V10.5', status: '关闭' },
          { name: '门锁7', pointName: 'V10.6', status: '打开' },
          { name: '备用4', pointName: 'V10.7', status: '关闭' }
        ],
        currentWeight: 0
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
          return '';
      }
    },
    resetAlert(alert) {
      // 处理复位逻辑
      alert.resolved = true;
    },
    getElectricalBoxStatus() {
      if (this.machineData.electricalBoxTemperature > 40) {
        return '过热';
      }
      if (this.machineData.electricalBoxHumidity > 90) {
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
          return '';
      }
    }
  },
  mounted() {
    // 模拟状态更新
    setTimeout(() => {
      this.machineData.machineStatus = '停止';
    }, 2000);
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
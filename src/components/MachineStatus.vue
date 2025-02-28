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

<!-- 新增区域：自动清洗和夜间模式状态 -->
<el-card class="status-card">
  <el-row :gutter="20">
    <el-col :span="12">
      <div class="status-item">
        <span class="label">自动清洗：</span>
        <el-tag :type="machineData.basicStatus.autoClean ? 'success' : 'danger'">
          {{ machineData.basicStatus.autoClean ? '开启' : '关闭' }}
        </el-tag>
      </div>
    </el-col>
    <el-col :span="12">
      <div class="status-item">
        <span class="label">夜间模式：</span>
        <el-tag :type="machineData.basicStatus.nightMode ? 'success' : 'danger'">
          {{ machineData.basicStatus.nightMode ? '开启' : '关闭' }}
        </el-tag>
      </div>
    </el-col>
  </el-row>
</el-card> 
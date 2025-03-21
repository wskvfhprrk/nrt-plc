# 牛肉汤自动售卖机后台管理系统

## 项目简介
本项目是一个基于 Vue.js 的牛肉汤自动售卖机后台管理系统，提供订单管理、机器状态监控、销售统计等功能。系统通过直观的界面和实时数据监控，帮助用户高效管理售卖机的运行和销售。

## 功能特性

### 1. 用户登录
- **登录页面 (Login.vue)**
  - 提供用户登录界面，验证用户身份。
  - 登录成功后跳转到主界面（订单管理页面）。

### 2. 订单管理（首页）
- **订单页面 (OrderPage.vue)**
  - 实时显示当前待处理、进行中和已完成的订单。
  - 支持微信支付功能，生成二维码进行支付。
  - 提供订单详情查看和状态更新功能。

### 3. 机器状态监控
- **机器状态页面 (MachineStatus.vue)**
  - 实时监控机器运行状态：
    - 显示机器状态（运行中/停止）
    - 显示牛肉汤温度
    - 显示菜重量
    - 显示机器运行时间
  - 机器人状态监控：
    - 显示机器人状态（运行中/空闲/未使能等）
    - 显示当前运行程序
  - 电箱状态监控：
    - 显示电箱温度
    - 显示电箱湿度
    - 显示电箱状态（正常/过热/过潮）
  - 设备输入点状态监控：
    - 粉丝到位传感器
    - 粉丝气缸状态
    - 出碗检测
    - 碗报警
    - 出碗电机
    - 做汤机气缸
    - 流量计
    - 出餐口气缸
    - 推碗气缸
    - 门锁状态等
  - 设备输出点状态监控：
    - 粉丝仓气缸控制
    - 做汤机气缸控制
    - 出餐口气缸控制
    - 推碗气缸控制
    - 夹手气缸控制
    - 旋转气缸控制
    - 切网机气缸控制
    - 切肉机气缸控制
    - 汤桶加热蒸汽阀控制
    - 出汤电磁阀控制
    - 消毒蒸汽阀控制等
  - 报警信息管理：
    - 显示报警时间
    - 显示报警级别
    - 显示报警信息
    - 显示报警状态（已处理/未处理）
    - 提供报警复位功能
  - 状态显示：
    - 使用标签（Tag）显示各种状态
    - 使用不同颜色区分状态级别
    - 实时更新状态信息

### 4. 销售统计
- **销售统计页面 (SalesStatistics.vue)**
  - 提供小时销售量统计，展示销售趋势。
  - 日销售量统计，支持按周和按月过滤数据。
  - 可视化图表展示销售数据，包括折线图和柱状图。

### 5. 报警设置
- **报警设置页面 (AlarmSettings.vue)**
  - 允许用户设置报警阈值和条件。
  - 提供报警信息的查看和处理功能。

### 6. 清洗操作
- **清洗操作页面 (CleaningOperation.vue)**
  - 提供手动清洗和自动清洗的设置和执行功能。
  - 用户可以设置自动清洗的时间间隔。

### 7. 自动运行
- **自动运行页面 (AutomaticOperation.vue)**
  - 设置汤的自动循环和保温检测参数。
  - 提供最高和最低温度的设置。

### 8. 手动操作
- **手动操作页面 (ManualOperation.vue)**
  - 提供机器人和设备组件的手动控制功能。
  - 包括紧急停止和复位功能。

### 9. 配料分发
- **配料分发页面 (ButtonsPage.vue)**
  - 控制配料的分发量。
  - 提供配料分发的操作按钮。

### 10. 系统设置
- **系统设置页面 (SetAccount.vue)**
  - 允许用户修改机器账户信息。
  - 提供密码重置功能。

### 11. 份量设置
- **份量设置页面 (PortionSettings.vue)**
  - 允许用户调整不同食材的份量设置。
  - 提供各类食材的重量输入。

### 12. 价格设置
- **价格设置页面 (PriceSettings.vue)**
  - 允许用户设置不同份量的价格。
  - 提供价格输入和提交功能。

## 技术栈
- **前端框架**: Vue 3
- **UI 组件库**: Element Plus
- **图表库**: ECharts
- **HTTP 请求**: Axios
- **WebSocket**: 用于实时数据通信

## 项目结构
```

web/
├── public/
│   ├── favicon.ico
│   └── index.html
├── src/
│   ├── assets/
│   │   ├── img/
│   │   └── styles/
│   ├── components/
│   │   ├── AlarmSettings.vue      # 报警设置
│   │   ├── AutomaticOperation.vue # 自动运行
│   │   ├── ButtonsPage.vue        # 配料分发
│   │   ├── CleaningOperation.vue  # 清洗操作
│   │   ├── Login.vue              # 登录
│   │   ├── MachineStatus.vue      # 机器状态
│   │   ├── MainLayout.vue         # 主布局
│   │   ├── ManualOperation.vue    # 手动操作
│   │   ├── Menu.vue              # 菜单
│   │   ├── OrderPage.vue         # 订单页面
│   │   ├── OrderingSettings.vue  # 订单设置
│   │   ├── PortionSettings.vue   # 份量设置
│   │   ├── PriceSettings.vue     # 价格设置
│   │   ├── SalesStatistics.vue   # 销售统计
│   │   └── SetAccount.vue        # 账户设置
│   ├── App.vue
│   └── main.js
```

## 安装和运行

### 环境要求
- Node.js >= 12.0.0
- npm >= 6.0.0

### 安装步骤
1. 克隆项目
```
git clone [项目地址]
```

2. 安装依赖
```
cd web
npm install
```

3. 运行开发服务器
```
npm run serve
```

4. 构建生产版本
```
npm run build
```

## 配置说明
- 在 `.env` 文件中配置环境变量
- 在 `vue.config.js` 中配置项目构建选项

## 开发团队
- [开发团队成员信息]

## 版本历史
- v1.0.0 (2024-03-xx)
  - 初始版本发布
  - 实现基本功能

## 注意事项
- 确保后端服务正常运行
- 注意WebSocket连接的稳定性
- 定期检查系统日志

## 许可证
[许可证信息]
# 531 Training - Professional Strength Training Companion

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg" alt="UI"/>
  <img src="https://img.shields.io/badge/Architecture-MVVM-red.svg" alt="Architecture"/>
  <img src="https://img.shields.io/badge/Database-Room-purple.svg" alt="Database"/>
  <img src="https://img.shields.io/badge/DI-Hilt-yellow.svg" alt="DI"/>
</div>

## 📱 项目概述

531 Training 是一款专为力量训练爱好者设计的专业Android应用，基于经典的"5/3/1训练法"构建。应用提供智能训练计划生成、精确配重计算、训练进度跟踪等核心功能，让用户更专注于训练本身。

## ✨ 核心特性

### 🎯 智能训练系统
- **531训练法核心算法**：支持5/5/5+、3/3/3+、5/3/1+三种经典模板
- **智能TM管理**：自动计算训练最大重量（90% 1RM），支持周期递增
- **AMRAP记录**：精确记录最后一组的实际完成次数
- **训练进度跟踪**：自动管理4周训练周期和训练进度

### ⚖️ 智能配重计算器
- **贪心算法优化**：智能计算最优杠铃片组合
- **可视化配重方案**：清晰显示每侧所需杠铃片
- **误差提示**：精确显示重量误差，确保训练准确性
- **多方案对比**：提供多种配重方案供用户选择

### ⏱️ 专业训练计时器
- **智能休息时间**：根据训练类型推荐合适的休息时间
- **圆形进度显示**：直观的倒计时进度可视化
- **预设时间选择**：快速选择常用休息时间
- **声音振动提醒**：训练完成智能提醒

### 📊 全面统计分析
- **训练概览**：全面展示训练完成情况和AMRAP平均值
- **四大项进步**：详细跟踪卧推、深蹲、硬拉、站姿推举的进步
- **历史记录管理**：完整的训练历史记录和数据导出
- **个人记录追踪**：自动计算最佳AMRAP记录和TM进步

### 🌍 完整多语言支持
- **英文**（默认）
- **简体中文**
- **繁体中文**
- **印度尼西亚语**

## 🏗️ 技术架构

### 核心技术栈
- **开发语言**：Kotlin 100%
- **UI框架**：Jetpack Compose + Material Design 3
- **架构模式**：MVVM + Repository Pattern
- **依赖注入**：Hilt
- **数据库**：Room Persistence Library
- **数据序列化**：Kotlinx Serialization
- **状态管理**：StateFlow + Compose State
- **导航**：Navigation Compose

### 项目结构
```
app/src/main/java/com/jmin/five3one/
├── data/                           # 数据层
│   ├── converter/                  # Room类型转换器
│   ├── dao/                        # 数据访问对象
│   ├── database/                   # Room数据库配置
│   ├── model/                      # 数据模型
│   └── repository/                 # Repository层
├── di/                             # 依赖注入模块
├── navigation/                     # 导航配置
├── ui/                             # UI层
│   ├── screen/                     # 页面组件
│   ├── theme/                      # 主题配置
│   └── viewmodel/                  # ViewModel层
├── MainActivity.kt                 # 主Activity
└── Five3OneApplication.kt         # Application类
```

## 🔧 核心功能实现

### 数据模型设计
- **OneRM**：用户最大单次重量记录
- **TrainingMax**：训练最大重量管理
- **WorkoutTemplate**：531训练模板配置
- **PlateConfig**：杠铃片配置管理
- **WorkoutHistory**：训练历史记录

### 算法实现
- **531计算引擎**：精确实现531训练法的重量计算逻辑
- **配重优化算法**：贪心算法实现最优杠铃片组合
- **进度管理系统**：自动化训练周期和TM递增管理
- **统计分析引擎**：全面的训练数据分析和可视化

## 📱 用户界面

### 设计原则
- **Material Design 3**：遵循最新设计规范
- **响应式设计**：适配不同屏幕尺寸
- **无障碍支持**：完整的无障碍功能支持
- **一致性体验**：统一的交互模式和视觉风格

### 页面结构
1. **欢迎页面**：应用介绍和功能特点展示
2. **设置流程**：1RM设置 → 杠铃片配置 → 训练模板选择
3. **主界面**：今日训练概览、快速操作、TM展示
4. **训练详情**：531计算结果、AMRAP记录、配重集成
5. **配重计算器**：智能配重方案和可视化显示
6. **训练计时器**：专业计时功能和休息时间管理
7. **统计分析**：全面的训练数据统计和进步追踪
8. **设置页面**：语言切换、数据管理、应用信息

## 🚀 开发环境设置

### 前置要求
- Android Studio Hedgehog | 2023.1.1+
- JDK 11+
- Android SDK 24+ (目标SDK 36)
- Kotlin 2.0.21+

### 构建步骤
```bash
# 克隆项目
git clone <repository-url>
cd five3one

# 构建调试版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 运行测试
./gradlew test
```

### 依赖管理
项目使用 `gradle/libs.versions.toml` 进行版本管理：
- **Compose BOM**: 2024.09.00
- **Navigation**: 2.8.5
- **Room**: 2.6.1
- **Hilt**: 2.52
- **Serialization**: 1.7.3

## 📋 功能检查清单

### ✅ 已完成功能
- [x] 完整的项目架构（MVVM + Repository + Hilt）
- [x] Room数据库和数据持久化
- [x] 531训练法核心算法实现
- [x] 智能杠铃配重计算器
- [x] 专业训练计时器
- [x] 完整的用户设置流程
- [x] 主界面和今日训练展示
- [x] 训练详情页面和AMRAP记录
- [x] 统计功能和进度分析
- [x] 设置页面和数据管理
- [x] 四语言国际化支持
- [x] Material Design 3 UI设计

### 🔄 待优化功能
- [ ] UI细节优化和动画效果
- [ ] 性能优化和内存管理
- [ ] 单元测试和UI测试
- [ ] Google Play发布准备
- [ ] 声音振动提醒功能
- [ ] 数据导出功能完善

## 🎯 531训练法简介

531训练法是由Jim Wendler创建的经典力量训练系统：

### 训练原理
- **训练最大重量（TM）**：使用90%的1RM作为计算基础
- **渐进式负载**：通过百分比系统逐步增加训练强度
- **AMRAP组**：最后一组尽力完成最多次数
- **周期递增**：每个周期结束后系统性增加TM

### 训练模板
1. **5/5/5+ Week**：65%×5, 75%×5, 85%×5+
2. **3/3/3+ Week**：70%×3, 80%×3, 90%×3+
3. **5/3/1+ Week**：75%×5, 85%×3, 95%×1+

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 贡献

欢迎提交 Pull Request 和 Issue！

## 📞 联系方式

- 项目地址：[GitHub Repository]
- 开发团队：531 Training Team
- 版本：1.0.0

---

<div align="center">
  <p><strong>🏋️‍♂️ 让训练更科学，让进步更可见 🏋️‍♀️</strong></p>
  <p><em>专业的531训练法Android应用</em></p>
</div>

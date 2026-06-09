
---

# 📄 esp32-frontend/README.md

```markdown
# 🎨 暖心伴机器人 — 前端应用 (Vue 3)

[![Vue](https://img.shields.io/badge/Vue-3.4-42b983)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-blue)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.x-purple)](https://vitejs.dev/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.14-409EFF)](https://element-plus.org/)

**为老年人和家属设计的 Web 控制台**，提供机器人控制、健康数据可视化、紧急事件响应和 AI 聊天界面。

---

## ✨ 功能亮点

- 📱 **响应式设计**：适配桌面与平板，毛玻璃风格 + 大按钮，易于触摸操作
- 🕹️ **机器人控制台**  
  - D-Pad 方向键（按压运动，松开停止）  
  - 速度滑块（0~255），急停按钮  
  - 实时电量、WiFi 信号、温度进度条  
  - 内置简易音乐播放器（模拟音乐切换）
- 🗣️ **AI 语音交互**  
  文本输入/语音录入（Web Speech API），展示 AI 回复并播放 TTS 音频
- 📊 **健康仪表盘**  
  ECharts 展示心率、血氧、血压趋势，自动生成日/周报告
- 🚨 **紧急事件看板**  
  - Leaflet 地图显示用户位置和附近医院（按距离排序）  
  - 事件列表支持状态、类型、时间筛选  
  - 一键处理（接单/解决/标记为误报）
- 💊 **提醒与用药管理**  
  时间轴展示今日待办，支持创建/编辑/删除提醒，查看用药依从率图表
- 📸 **照片墙**  
  上传机器人拍摄的照片，按时间/设备/紧急事件归类
- 📈 **心理分析报告**  
  基于聊天记录生成情绪分布（积极/中性/消极），提供中文建议

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Vue 3 (Composition API) | 3.4+ |
| 构建工具 | Vite | 5.x |
| 语言 | TypeScript | 5.9+ |
| 状态管理 | Pinia | 2.3+ |
| UI 组件 | Element Plus | 2.14+ |
| 图表 | ECharts + vue-echarts | 5.6+ |
| 地图 | Leaflet (vue-leaflet) | 1.9+ |
| HTTP 客户端 | Axios | 1.16+ |
| WebSocket | 原生 WebSocket API | - |
| 路由 | Vue Router 4 | 4.3+ |

---

## 🚀 快速开始

### 环境要求

- Node.js 18+
- npm 9+ 或 pnpm

### 安装依赖

```bash
cd esp32-frontend
npm install

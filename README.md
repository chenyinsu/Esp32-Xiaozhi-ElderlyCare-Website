# 🤖 ESP32 暖心伴机器人 · 智能陪伴控制系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b983)](https://vuejs.org/)
[![ESP32](https://img.shields.io/badge/ESP32-PlatformIO-red)](https://platformio.org/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

> 为长者及需要陪伴的人群设计的智能机器人控制系统。通过 ESP32 硬件、后端云服务与 Web 前端，实现语音对话、健康监测、紧急求助、用药提醒等功能，打造有温度的“暖心伴”。

---

## 🎯 项目简介

本项目是一个**完整的端到端物联网机器人控制系统**，包含三个核心模块：

| 模块 | 路径 | 技术栈 |
|------|------|--------|
| **后端服务** | `esp32-backend` | Spring Boot 3.2.5, Java 17, WebSocket, MQTT, JPA, OpenAI API |
| **前端应用** | `esp32-frontend` | Vue 3, TypeScript, Vite, Pinia, Element Plus, ECharts, Leaflet |
| **机器人固件** | `esp32-firmware` | PlatformIO, ESP32 Arduino Core, MQTT, 串口通信, 电机驱动 |

系统支持**双模通信**（串口 / MQTT），可通过 Web 端实时控制机器人、查看健康数据、进行 AI 语音对话，并能自动处理跌倒检测等紧急事件。

---

## ✨ 核心功能

- 🗣️ **AI 语音对话**：集成 LLM（OpenAI 兼容 API）+ TTS 语音合成，支持情感分析（HanLP）
- 📡 **实时双模通信**：串口 (jSerialComm) ↔ MQTT (EMQX) 无缝切换
- 🚨 **紧急事件全生命周期**：支持跌倒/健康异常/无移动等多类型告警，PENDING → HANDLING → RESOLVED 状态机
- 💊 **智能提醒与用药追踪**：定时提醒（每日/每周/工作日等），用药依从率统计
- 📊 **健康管理与自动报告**：心率、血氧、体温、步数等趋势分析，每日健康报告自动生成
- 🎛️ **远程机器人控制**：方向控制、调速、急停，附带电机平滑加速与电量保护模拟器
- 📸 **多媒体记录**：照片上传与管理，紧急事件关联录音/录像 URL
- 👥 **多角色系统**：老人、家属、社区人员、管理员，基于 JWT 的认证与权限
- 🖥️ **机器人模拟器**：无硬件时自动启动软件模拟 ESP32，便于开发调试

---

## 🏗️ 系统架构图（简略）
[ Vue 前端 ] --(WebSocket/REST)--> [ Spring Boot 后端 ] --(串口/MQTT)--> [ ESP32 机器人 ]
|
+---> [ MySQL ]
+---> [ OpenAI API / TTS ]
+---> [ EMQX MQTT Broker ]


---

## 🚀 快速开始

### 环境要求

- **后端**：JDK 17, Maven 3.8+, MySQL 8.0+
- **前端**：Node.js 18+, npm 9+
- **硬件（可选）**：ESP32 开发板、串口线、电机驱动板（如 L298N）
- **MQTT Broker（可选）**：EMQX / Mosquitto（默认使用串口模式）

### 一键启动（开发环境）

```bash
# 1. 克隆仓库
git clone https://github.com/your-org/esp32-robot.git
cd esp32-robot

# 2. 启动 MySQL，创建数据库 esp32_robot（执行后端 resources/db/schema.sql）

# 3. 启动后端（默认端口 8080）
cd esp32-backend
mvn spring-boot:run

# 4. 启动前端（默认端口 5173）
cd ../esp32-frontend
npm install
npm run dev

# 5. （可选）连接真实 ESP32 或使用内置模拟器

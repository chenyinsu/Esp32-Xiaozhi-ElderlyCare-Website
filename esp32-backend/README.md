
---

# 📄 esp32-backend/README.md

```markdown
# 🧠 暖心伴机器人 — 后端服务 (Spring Boot)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![MQTT](https://img.shields.io/badge/MQTT-Eclipse%20Paho-yellow)](https://www.eclipse.org/paho/)

本后端是整个机器人的**核心大脑**，负责设备通信、业务逻辑、数据持久化和 API 服务。

---

## 🚀 主要功能

- 🔌 **双模设备通信**  
  支持**串口 (jSerialComm)** 和 **MQTT (Eclipse Paho)** 两种方式与 ESP32 交互，通过 `@ConditionalOnProperty` 动态切换。

- 🌐 **WebSocket 实时推送**  
  四个独立通道：`/ws/device`（设备上报）、`/ws/frontend`（前端实时数据）、`/ws/alert`（紧急告警）、`/ws/chat`（聊天消息）。

- 🚨 **紧急事件管理**  
  完整状态机（PENDING → HANDLING → RESOLVED → CLOSED / FALSE_ALARM），支持关联多媒体 URL、传感器快照、联系人通知链。

- 🗣️ **AI 对话与语音**  
  - LLM：OpenAI 兼容 API（可配置），无 Key 时自动降级为本地关键词回复。  
  - TTS：生成 MP3 并通过 `javax.sound` 播放。

- ❤️ **情感分析 (HanLP)**  
  基于词库计算情感评分（positive/neutral/negative），异步更新聊天记录，并提供 7 日情绪趋势和心理分析报告。

- ⏰ **定时提醒与用药追踪**  
  基于 `@Scheduled` 轮询到期提醒，通过硬件指令推送。支持 6 种重复规则，计算用药依从率。

- 📊 **健康报告**  
  多维度健康数据（心率、血氧、体温、步数等）趋势分析，自动/手动生成日报告。

- 📸 **文件管理**  
  照片上传（设备/用户/紧急事件三级关联），本地存储并记录尺寸信息。

- 🔐 **认证授权**  
  JWT + BCrypt，支持 ADMIN / ELDERLY / FAMILY / COMMUNITY 四种角色（⚠️ 目前安全配置为全部放行，**生产环境需开启**）。

---

## 🛠️ 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot, Spring Data JPA, Spring Security, Spring Integration MQTT | 3.2.5 |
| 数据库 | MySQL, Hibernate | 8.0+ |
| 硬件通信 | jSerialComm, Eclipse Paho MQTT Client | 2.10.3 / 1.2.5 |
| AI/NLP | OpenAI API (RestTemplate), HanLP | portable-2.8.4 |
| 实时 | WebSocket (原生), STOMP (可选) | - |
| 文档 | SpringDoc OpenAPI (Swagger) | 2.2.0 |
| 构建 | Maven | 3.8+ |

---

## 📦 快速运行

### 1. 环境准备

- JDK 17
- MySQL 8.0（创建数据库 `esp32_robot`）
- Maven

### 2. 配置修改

编辑 `src/main/resources/application.properties`：

```properties
# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/esp32_robot
spring.datasource.username=root
spring.datasource.password=your_password

# 串口配置（默认 COM11）
serial.port=COM11
serial.baudrate=115200

# 通信模式：serial 或 mqtt（缺省为 serial）
robot.control.mode=serial

# MQTT 配置（当 mode=mqtt 时生效）
mqtt.broker=tcp://localhost:1883
mqtt.client.id=esp32-backend
mqtt.default.topic=esp32/#

# LLM & TTS API（可选，留空则降级）
llm.api.key=sk-xxx
llm.api.url=https://api.openai.com/v1/chat/completions
tts.api.key=sk-xxx
tts.api.url=https://api.openai.com/v1/audio/speech

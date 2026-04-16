@echo off
chcp 65001 >nul
title ESP32健康助手系统部署
echo.
echo ========================================
echo   ESP32-S3 老年人健康助手系统
echo   （学校网络专用简化版）
echo ========================================
echo.

REM 检查必要软件
echo [1/5] 检查必要软件...
where java >nul 2>nul
if %errorLevel% neq 0 (
    echo [错误] 未找到Java，请安装OpenJDK 17+
    pause
    exit /b 1
)
echo [OK] Java 已安装

where node >nul 2>nul
if %errorLevel% neq 0 (
    echo [错误] 未找到Node.js，请安装Node.js 18+
    pause
    exit /b 1
)
echo [OK] Node.js 已安装

where git >nul 2>nul
if %errorLevel% neq 0 (
    echo [错误] 未找到Git
    pause
    exit /b 1
)
echo [OK] Git 已安装

echo.

REM 检查项目结构
echo [2/5] 检查项目结构...
if not exist esp32-backend (
    echo [错误] 缺少后端目录: esp32-backend
    pause
    exit /b 1
)
echo [OK] 后端目录存在

if not exist esp32-frontend (
    echo [错误] 缺少前端目录: esp32-frontend
    pause
    exit /b 1
)
echo [OK] 前端目录存在

echo.

REM 启动后端
echo [3/5] 启动后端服务 (端口: 8080)...
start "ESP32-Backend" cmd /c "cd esp32-backend && java -jar target\esp32-robot-0.0.1-SNAPSHOT.jar"
timeout /t 5 /nobreak >nul

REM 启动前端
echo [4/5] 启动前端服务 (端口: 5173)...
start "ESP32-Frontend" cmd /c "cd esp32-frontend && npm run dev"
timeout /t 3 /nobreak >nul

echo.

REM 显示结果
echo [5/5] 部署完成！
echo ========================================
echo.
echo 访问地址：
echo   前端界面: http://localhost:5173
echo   后端API:  http://localhost:8080
echo.
echo 如果服务启动失败，请检查：
echo   1. 端口是否被占用
echo   2. 依赖是否安装完整
echo   3. 学校网络是否允许本地服务
echo.
echo 按任意键查看网络配置帮助...
pause >nul

echo.
echo 学校网络特别说明：
echo   1. 可能需要配置防火墙
echo   2. 如果手机无法访问，检查是否在同一网络
echo   3. 可以尝试关闭Windows Defender防火墙临时测试
echo   4. 或者配置入站规则允许端口8080和5173
echo.
echo 按任意键退出...
pause >nul
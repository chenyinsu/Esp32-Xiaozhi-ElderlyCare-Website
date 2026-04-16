@echo off
chcp 65001 >nul
title ESP32健康助手系统部署脚本

:: 颜色定义
echo.
echo [36m=======================================[0m
echo [36m  ESP32-S3 老年人健康助手系统[0m
echo [36m      Windows 部署脚本[0m
echo [36m=======================================[0m
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [31m[错误] 请以管理员身份运行此脚本[0m
    pause
    exit /b 1
)

:: 检查必需软件
echo [33m[1/7] 检查系统依赖...[0m

:: 检查Java
where java >nul 2>&1
if %errorLevel% neq 0 (
    echo [31m[错误] 未找到Java，请安装OpenJDK 17+[0m
    goto :error
)
echo [32m[成功] Java 已安装[0m

:: 检查Node.js
where node >nul 2>&1
if %errorLevel% neq 0 (
    echo [31m[错误] 未找到Node.js，请安装Node.js 18+[0m
    goto :error
)
echo [32m[成功] Node.js 已安装[0m

:: 检查Maven
where mvn >nul 2>&1
if %errorLevel% neq 0 (
    echo [31m[错误] 未找到Maven，请安装Maven 3.6+[0m
    goto :error
)
echo [32m[成功] Maven 已安装[0m

:: 检查Git
where git >nul 2>&1
if %errorLevel% neq 0 (
    echo [31m[错误] 未找到Git[0m
    goto :error
)
echo [32m[成功] Git 已安装[0m

:: 检查Docker
where docker >nul 2>&1
if %errorLevel% neq 0 (
    echo [33m[警告] 未找到Docker，将使用原生部署[0m
    set USE_DOCKER=false
) else (
    set USE_DOCKER=true
    echo [32m[成功] Docker 已安装[0m
)

:: 加载环境变量
echo.
echo [33m[2/7] 加载环境变量...[0m
if exist .env (
    for /f "tokens=1,2 delims==" %%a in (.env) do (
        set %%a=%%b
    )
) else if exist .env.example (
    copy .env.example .env
    for /f "tokens=1,2 delims==" %%a in (.env) do (
        set %%a=%%b
    )
) else (
    echo [31m[错误] 未找到环境变量文件[0m
    goto :error
)

:: 设置默认值
if "%DB_PASSWORD%"=="" set DB_PASSWORD=esp32_health_2024
if "%DB_NAME%"=="" set DB_NAME=esp32_robot_db
if "%DB_USER%"=="" set DB_USER=esp32_user
if "%BACKEND_PORT%"=="" set BACKEND_PORT=8080
if "%FRONTEND_PORT%"=="" set FRONTEND_PORT=80
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

echo [32m[成功] 环境变量加载完成[0m

:: 初始化项目
echo.
echo [33m[3/7] 初始化项目...[0m
if not exist esp32-backend (
    echo 正在克隆后端代码...
    git clone https://github.com/your-username/esp32-robot.git esp32-backend
) else (
    echo 更新后端代码...
    cd esp32-backend
    git pull origin main
    cd ..
)

if not exist esp32-frontend (
    echo 正在克隆前端代码...
    git clone https://github.com/your-username/esp32-frontend.git esp32-frontend
) else (
    echo 更新前端代码...
    cd esp32-frontend
    git pull origin main
    cd ..
)

:: 创建目录
mkdir logs 2>nul
mkdir uploads 2>nul
mkdir database\backups 2>nul

echo [32m[成功] 项目初始化完成[0m

:: 构建后端
echo.
echo [33m[4/7] 构建后端...[0m
cd esp32-backend

:: 更新配置文件
echo 更新应用配置...
(
echo # 服务器配置
echo server.port=%BACKEND_PORT%
echo server.servlet.context-path=/
echo.
echo # 数据库配置
echo spring.datasource.url=jdbc:mysql://localhost:%MYSQL_PORT%/%DB_NAME%?useUnicode^=true^&characterEncoding^=utf8^&serverTimezone^=Asia/Shanghai^&useSSL^=false
echo spring.datasource.username=%DB_USER%
echo spring.datasource.password=%DB_PASSWORD%
echo spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
echo.
echo # JPA配置
echo spring.jpa.hibernate.ddl-auto=update
echo spring.jpa.show-sql=true
echo spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
echo spring.jpa.open-in-view=false
) > src\main\resources\application.properties

:: 构建
echo 构建后端应用...
call mvn clean package -DskipTests
if %errorLevel% neq 0 (
    echo [31m[错误] 后端构建失败[0m
    goto :error
)

cd ..
echo [32m[成功] 后端构建完成[0m

:: 构建前端
echo.
echo [33m[5/7] 构建前端...[0m
cd esp32-frontend

:: 更新环境配置
echo 更新前端环境配置...
(
echo VITE_API_BASE_URL=http://localhost:%BACKEND_PORT%
echo VITE_WS_BASE_URL=ws://localhost:%BACKEND_PORT%
echo VITE_APP_TITLE=ESP32健康助手
echo VITE_APP_VERSION=1.0.0
) > .env.production

:: 安装依赖
echo 安装前端依赖...
call npm install
if %errorLevel% neq 0 (
    echo [31m[错误] 前端依赖安装失败[0m
    goto :error
)

:: 构建
echo 构建前端应用...
call npm run build
if %errorLevel% neq 0 (
    echo [31m[错误] 前端构建失败[0m
    goto :error
)

cd ..
echo [32m[成功] 前端构建完成[0m

:: 启动服务
echo.
echo [33m[6/7] 启动服务...[0m

:: 检查端口占用
for /f "tokens=5" %%p in ('netstat -aon ^| findstr :%BACKEND_PORT%') do (
    echo [33m[警告] 端口 %BACKEND_PORT% 被进程 %%p 占用[0m
    set /p kill=是否杀死该进程？(y/n):
    if /i "!kill!"=="y" taskkill /F /PID %%p
)

:: 启动后端
echo 启动后端服务...
start "ESP32 Backend" cmd /c "cd esp32-backend && java -jar target\esp32-robot-0.0.1-SNAPSHOT.jar"
timeout /t 10 /nobreak >nul

:: 启动前端
echo 启动前端服务...
start "ESP32 Frontend" cmd /c "cd esp32-frontend && npm run dev"
timeout /t 5 /nobreak >nul

echo [32m[成功] 服务启动完成[0m

:: 显示部署信息
echo.
echo [33m[7/7] 部署完成[0m
echo [36m=======================================[0m
echo [36m     ESP32健康助手系统[0m
echo [36m        部署完成！[0m
echo [36m=======================================[0m
echo.
echo 访问地址:
echo     前端界面: http://localhost:%FRONTEND_PORT%
echo     后端API: http://localhost:%BACKEND_PORT%
echo.
echo 管理命令:
echo     查看日志: 查看 logs\ 目录
echo     停止服务: 关闭命令行窗口
echo.
pause
exit /b 0

:error
echo.
echo [31m部署失败，请检查错误信息[0m
pause
exit /b 1
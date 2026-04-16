# ============================================================
# ESP32健康助手系统 - 简化版Windows部署脚本
# 作者: 计算机232陈银素 2301020103
# 版本: 1.1.0 (修复语法错误)
# ============================================================

# 清除屏幕
Clear-Host

# 显示横幅
Write-Host ""
Write-Host "========================================"
Write-Host "  ESP32-S3 老年人健康助手系统"
Write-Host "  学校网络专用部署脚本"
Write-Host "========================================"
Write-Host ""

# 步骤1: 检查必要软件
Write-Host "[步骤1] 检查系统依赖" -ForegroundColor Yellow

# 检查Java
try {
    $null = java -version 2>&1
    $javaVersion = (java -version 2>&1 | Select-Object -First 1)
    Write-Host "[OK] Java 已安装 ($javaVersion)" -ForegroundColor Green
} catch {
    Write-Host "[错误] 未找到Java，请安装OpenJDK 17+" -ForegroundColor Red
    Write-Host "下载地址: https://adoptium.net/"
    exit 1
}

# 检查Node.js
try {
    $nodeVersion = node -v
    Write-Host "[OK] Node.js 已安装 ($nodeVersion)" -ForegroundColor Green
} catch {
    Write-Host "[错误] 未找到Node.js，请安装Node.js 18+" -ForegroundColor Red
    Write-Host "下载地址: https://nodejs.org/"
    exit 1
}

Write-Host ""

# 步骤2: 检查项目结构
Write-Host "[步骤2] 检查项目结构" -ForegroundColor Yellow

# 检查后端目录
if (Test-Path "esp32-backend") {
    Write-Host "[OK] 后端目录存在" -ForegroundColor Green
} else {
    Write-Host "[错误] 缺少后端目录: esp32-backend" -ForegroundColor Red
    exit 1
}

# 检查前端目录
if (Test-Path "esp32-frontend") {
    Write-Host "[OK] 前端目录存在" -ForegroundColor Green
} else {
    Write-Host "[错误] 缺少前端目录: esp32-frontend" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤3: 启动后端
Write-Host "[步骤3] 启动后端服务" -ForegroundColor Yellow

# 检查后端JAR
$jarPath = "esp32-backend\target\esp32-robot-0.0.1-SNAPSHOT.jar"

if (Test-Path $jarPath) {
    Write-Host "找到后端JAR文件" -ForegroundColor Green
} else {
    Write-Host "[警告] 未找到JAR文件，尝试构建..." -ForegroundColor Yellow

    # 进入后端目录
    Set-Location esp32-backend

    # 检查mvnw
    if (Test-Path "mvnw") {
        Write-Host "使用Maven Wrapper构建..."
        .\mvnw clean package -DskipTests
    } else {
        Write-Host "[错误] 未找到Maven Wrapper" -ForegroundColor Red
        Set-Location ..
        exit 1
    }

    Set-Location ..

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[错误] 后端构建失败" -ForegroundColor Red
        exit 1
    }
}

# 启动后端进程
Write-Host "启动后端服务 (端口: 8080)..."

# 先检查端口是否被占用
$backendConn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($backendConn) {
    Write-Host "[警告] 端口8080被占用" -ForegroundColor Yellow
    $choice = Read-Host "是否停止占用进程? (y/n)"
    if ($choice -eq 'y') {
        $backendConn | ForEach-Object {
            try {
                Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
            } catch {}
        }
    }
}

# 启动后端
$backendProcess = Start-Process -WindowStyle Hidden -FilePath "java" -ArgumentList "-jar", "`"$jarPath`"" -PassThru

if ($backendProcess) {
    Write-Host "[OK] 后端已启动 (PID: $($backendProcess.Id))" -ForegroundColor Green
    $backendProcess.Id | Out-File "backend.pid" -Encoding UTF8
} else {
    Write-Host "[警告] 后端启动失败" -ForegroundColor Yellow
}

Write-Host ""

# 步骤4: 启动前端
Write-Host "[步骤4] 启动前端服务" -ForegroundColor Yellow

# 进入前端目录
Set-Location esp32-frontend

# 检查依赖
if (-not (Test-Path "node_modules")) {
    Write-Host "安装前端依赖..."
    npm install

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[警告] npm安装失败，尝试国内镜像..." -ForegroundColor Yellow
        npm config set registry https://registry.npmmirror.com
        npm install
    }
}

# 启动前端
Write-Host "启动前端开发服务器 (端口: 5173)..."
$frontendProcess = Start-Process -WindowStyle Hidden -FilePath "npm" -ArgumentList "run", "dev" -PassThru

Set-Location ..

if ($frontendProcess) {
    Write-Host "[OK] 前端已启动 (PID: $($frontendProcess.Id))" -ForegroundColor Green
    $frontendProcess.Id | Out-File "frontend.pid" -Encoding UTF8
} else {
    Write-Host "[警告] 前端启动失败" -ForegroundColor Yellow
}

Write-Host ""

# 步骤5: 等待服务启动
Write-Host "[步骤5] 等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 步骤6: 验证服务
Write-Host "[步骤6] 验证服务状态" -ForegroundColor Yellow

# 测试后端
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "[OK] 后端服务正常运行" -ForegroundColor Green
    } else {
        Write-Host "[警告] 后端返回状态: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[警告] 后端暂时不可访问 (可能正在启动)" -ForegroundColor Yellow
}

# 测试前端
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "[OK] 前端服务正常运行" -ForegroundColor Green
    } else {
        Write-Host "[警告] 前端返回状态: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[警告] 前端暂时不可访问 (可能正在启动)" -ForegroundColor Yellow
}

Write-Host ""

# 步骤7: 显示结果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "        部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "访问地址:" -ForegroundColor White
Write-Host "  前端界面: http://localhost:5173" -ForegroundColor Blue
Write-Host "  后端API:  http://localhost:8080" -ForegroundColor Blue
Write-Host ""
Write-Host "管理命令:" -ForegroundColor White
Write-Host "  查看进程: Get-Process java, node" -ForegroundColor Gray
Write-Host "  停止后端: Stop-Process -Id (获取backend.pid中的ID)" -ForegroundColor Gray
Write-Host "  停止前端: Stop-Process -Id (获取frontend.pid中的ID)" -ForegroundColor Gray
Write-Host ""
Write-Host "学校网络说明:" -ForegroundColor White
Write-Host "  1. 确保防火墙允许端口8080和5173" -ForegroundColor Gray
WriteHost "  2. 手机需在同一WiFi网络" -ForegroundColor Gray
WriteHost "  3. 获取本机IP: ipconfig" -ForegroundColor Gray
WriteHost ""

# 获取本机IP
$ipAddress = Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" } | Select-Object -First 1
if ($ipAddress) {
    Write-Host "本机IP地址: $($ipAddress.IPAddress)" -ForegroundColor Blue
    Write-Host "手机访问: http://$($ipAddress.IPAddress):5173" -ForegroundColor Blue
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
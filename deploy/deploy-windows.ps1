# ============================================================================
# ESP32-S3老年人健康助手系统 - Windows一键部署脚本
# 版本: 2.1.0
# 作者: 计算机232陈银素 2301020103
# 描述: Windows PowerShell自动化部署脚本
# ============================================================================

# 设置脚本执行策略
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force

# 颜色定义
$ESC = [char]27
$Red = "$ESC[31m"
$Green = "$ESC[32m"
$Yellow = "$ESC[33m"
$Blue = "$ESC[34m"
$Cyan = "$ESC[36m"
$Magenta = "$ESC[35m"
$Reset = "$ESC[0m"

# 日志函数
function Write-Info {
    param($Message)
    Write-Host "$Blue[INFO]$Reset $Message" -ForegroundColor Blue
}

function Write-Success {
    param($Message)
    Write-Host "$Green[SUCCESS]$Reset $Message" -ForegroundColor Green
}

function Write-Warning {
    param($Message)
    Write-Host "$Yellow[WARNING]$Reset $Message" -ForegroundColor Yellow
}

function Write-Error {
    param($Message)
    Write-Host "$Red[ERROR]$Reset $Message" -ForegroundColor Red
}

function Write-Step {
    param($Message)
    Write-Host "`n$Cyan========================================"
    Write-Host $Message
    Write-Host "========================================$Reset`n" -ForegroundColor Cyan
}

# 显示横幅
function Show-Banner {
    Clear-Host
    Write-Host "$Magenta"
    Write-Host "███████╗██████╗ ███████╗██████╗ ██╗  ██╗"
    Write-Host "██╔════╝██╔══██╗██╔════╝██╔══██╗╚██╗██╔╝"
    Write-Host "█████╗  ██████╔╝███████╗██║  ██║ ╚███╔╝ "
    Write-Host "██╔══╝  ██╔══██╗╚════██║██║  ██║ ██╔██╗ "
    Write-Host "███████╗██║  ██║███████║██████╔╝██╔╝ ██╗"
    Write-Host "╚══════╝╚═╝  ╚═╝╚══════╝╚═════╝ ╚═╝  ╚═╝"
    Write-Host "$Reset"
    Write-Host "$Cyan  ESP32-S3 老年人健康助手系统$Reset"
    Write-Host "$Cyan  Windows 自动化部署脚本 v2.1.0$Reset"
    Write-Host ""
}

# 检查命令是否存在
function Test-CommandExists {
    param($Command, $InstallGuide)

    $exists = Get-Command $Command -ErrorAction SilentlyContinue
    if (-not $exists) {
        Write-Error "未找到命令: $Command"
        Write-Info "请先安装: $InstallGuide"
        return $false
    }
    Write-Success "$Command 已安装"
    return $true
}

# 检查系统依赖
function Test-Dependencies {
    Write-Step "1. 检查系统依赖"

    $dependencies = @(
        @{Command = "java"; Guide = "OpenJDK 17+ (下载: https://adoptium.net/)"},
        @{Command = "node"; Guide = "Node.js 18+ (下载: https://nodejs.org/)"},
        @{Command = "npm"; Guide = "Node Package Manager (随Node.js安装)"},
        @{Command = "git"; Guide = "Git (下载: https://git-scm.com/)"}
    )

    $allOk = $true
    foreach ($dep in $dependencies) {
        if (-not (Test-CommandExists $dep.Command $dep.Guide)) {
            $allOk = $false
        }
    }

    # 检查Maven
    $mavenExists = Get-Command "mvn" -ErrorAction SilentlyContinue
    if (-not $mavenExists) {
        Write-Warning "未找到Maven，尝试在项目目录中查找"
        if (Test-Path "esp32-backend\mvnw") {
            Write-Success "找到Maven Wrapper"
        } else {
            Write-Error "未找到Maven，请安装Maven 3.6+ (下载: https://maven.apache.org/)"
            $allOk = $false
        }
    } else {
        Write-Success "Maven 已安装"
    }

    if (-not $allOk) {
        Write-Error "依赖检查失败，请先安装缺失的软件"
        exit 1
    }

    # 检查Node.js版本
    $nodeVersion = (node -v).Substring(1).Split('.')[0]
    if ([int]$nodeVersion -lt 18) {
        Write-Error "Node.js 版本过低 (需要 >= 18.x)"
        exit 1
    }

    # 检查Java版本
    $javaVersion = (java -version 2>&1 | Select-String -Pattern "version").Line.Split('"')[1].Split('.')[0]
    if ([int]$javaVersion -lt 17) {
        Write-Error "Java 版本过低 (需要 >= 17)"
        exit 1
    }

    return $true
}

# 加载环境变量
function Load-Environment {
    Write-Step "2. 加载环境变量"

    $envFile = ".env"
    $envExample = ".env.example"

    if (Test-Path $envFile) {
        Write-Info "从 .env 文件加载环境变量"
        Get-Content $envFile | ForEach-Object {
            if ($_ -match '^([^=]+)=(.*)$') {
                [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
            }
        }
    } elseif (Test-Path $envExample) {
        Write-Warning ".env 文件不存在，从 .env.example 创建"
        Copy-Item $envExample $envFile
        Load-Environment
    } else {
        Write-Error "未找到 .env 或 .env.example 文件"
        exit 1
    }

    # 设置默认值
    if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = "esp32_health_2024" }
    if (-not $env:DB_NAME) { $env:DB_NAME = "esp32_robot_db" }
    if (-not $env:DB_USER) { $env:DB_USER = "esp32_user" }
    if (-not $env:BACKEND_PORT) { $env:BACKEND_PORT = 8080 }
    if (-not $env:FRONTEND_PORT) { $env:FRONTEND_PORT = 5173 }
    if (-not $env:MYSQL_PORT) { $env:MYSQL_PORT = 3306 }

    Write-Success "环境变量加载完成"
}

# 检查端口占用
function Test-PortInUse {
    param($Port)

    $connection = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Warning "端口 $Port 已被占用"
        $choice = Read-Host "是否杀死占用进程？(y/n)"
        if ($choice -eq 'y') {
            $connection | ForEach-Object {
                try {
                    Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
                } catch {
                    Write-Warning "无法杀死进程 $($_.OwningProcess)，可能需要管理员权限"
                }
            }
            Write-Success "已尝试释放端口 $Port"
        } else {
            Write-Error "请手动释放端口 $Port 后再试"
            exit 1
        }
    }
}

# 初始化项目目录
function Initialize-Project {
    Write-Step "3. 初始化项目目录"

    # 创建必要目录
    $directories = @("logs", "backups", "uploads", "database\backups", "deploy")
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }

    Write-Success "项目目录初始化完成"
}

# 构建后端
function Build-Backend {
    Write-Step "4. 构建SpringBoot后端"

    if (-not (Test-Path "esp32-backend")) {
        Write-Error "后端目录不存在: esp32-backend"
        exit 1
    }

    Set-Location esp32-backend

    # 更新配置文件
    Write-Info "更新应用配置文件..."

    # 转义 & 符号
    $dbUrl = "jdbc:mysql://localhost:$env:MYSQL_PORT/$env:DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
    $dbUrl = $dbUrl -replace '&', '&amp;'

    $configContent = @"
# 服务器配置
server.port=$env:BACKEND_PORT
server.servlet.context-path=/

# 数据库配置
spring.datasource.url=$dbUrl
spring.datasource.username=$env:DB_USER
spring.datasource.password=$env:DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false

# 文件上传配置
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 调度配置
spring.task.scheduling.enabled=true

# 跨域配置
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true

# 日志配置
logging.level.com.example.esp32robot=DEBUG
logging.file.name=../logs/esp32-robot.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
"@

    $configContent | Out-File -FilePath "src\main\resources\application.properties" -Encoding UTF8

    # 构建项目
    Write-Info "开始构建后端应用..."

    # 使用Maven Wrapper或系统Maven
    if (Test-Path "mvnw") {
        Write-Info "使用Maven Wrapper构建..."
        .\mvnw clean package -DskipTests
    } else {
        Write-Info "使用系统Maven构建..."
        mvn clean package -DskipTests
    }

    if ($LASTEXITCODE -ne 0) {
        Write-Error "后端构建失败"
        exit 1
    }

    Set-Location ..
    Write-Success "后端构建完成"
}

# 构建前端
function Build-Frontend {
    Write-Step "5. 构建Vue前端"

    if (-not (Test-Path "esp32-frontend")) {
        Write-Error "前端目录不存在: esp32-frontend"
        exit 1
    }

    Set-Location esp32-frontend

    # 检查是否有package.json
    if (-not (Test-Path "package.json")) {
        Write-Warning "前端目录为空，初始化Vue项目..."
        # 这里可以添加初始化Vue项目的代码
    }

    # 更新环境配置
    Write-Info "更新前端环境配置..."
    $envContent = @"
VITE_API_BASE_URL=http://localhost:$env:BACKEND_PORT
VITE_WS_BASE_URL=ws://localhost:$env:BACKEND_PORT
VITE_APP_TITLE=ESP32健康助手
VITE_APP_VERSION=1.0.0
"@

    $envContent | Out-File -FilePath ".env.production" -Encoding UTF8

    # 安装依赖
    Write-Info "安装前端依赖..."
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "前端依赖安装失败"
        exit 1
    }

    # 构建生产版本
    Write-Info "构建前端生产版本..."
    npm run build
    if ($LASTEXITCODE -ne 0) {
        Write-Error "前端构建失败"
        exit 1
    }

    Set-Location ..
    Write-Success "前端构建完成"
}

# 启动服务
function Start-Services {
    Write-Step "6. 启动服务"

    # 检查端口占用
    Test-PortInUse $env:BACKEND_PORT
    Test-PortInUse $env:FRONTEND_PORT

    # 启动后端
    Write-Info "启动SpringBoot后端服务..."

    $jarFile = "esp32-backend\target\esp32-robot-0.0.1-SNAPSHOT.jar"
    if (-not (Test-Path $jarFile)) {
        Write-Error "找不到后端JAR文件: $jarFile"
        exit 1
    }

    # 启动后端进程
    $backendProcess = Start-Process java -ArgumentList "-jar", $jarFile -PassThru -NoNewWindow
    $backendProcess.Id | Out-File -FilePath "backend.pid" -Encoding UTF8
    Write-Info "后端进程已启动，PID: $($backendProcess.Id)"

    # 等待后端启动
    Write-Info "等待后端服务启动..."
    $maxWait = 30
    $waited = 0
    $backendReady = $false

    while ($waited -lt $maxWait) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$env:BACKEND_PORT/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $backendReady = $true
                break
            }
        } catch {
            # 服务还没启动，继续等待
        }
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Info "等待后端启动... ($waited/$maxWait 秒)"
    }

    if (-not $backendReady) {
        Write-Warning "后端服务启动较慢，继续等待..."
    } else {
        Write-Success "后端服务启动成功"
    }

    # 启动前端
    Write-Info "启动前端开发服务器..."

    # 在新的PowerShell窗口中启动前端
    $frontendScript = @"
Set-Location '$PWD\esp32-frontend'
npm run dev
"@

    $frontendScript | Out-File -FilePath "deploy\start-frontend.ps1" -Encoding UTF8

    $frontendProcess = Start-Process powershell -ArgumentList "-NoExit", "-File", "deploy\start-frontend.ps1" -PassThru
    $frontendProcess.Id | Out-File -FilePath "frontend.pid" -Encoding UTF8
    Write-Info "前端进程已启动，PID: $($frontendProcess.Id)"

    Start-Sleep -Seconds 5

    Write-Success "服务启动完成"
}

# 检查服务状态
function Test-ServiceStatus {
    Write-Step "7. 检查服务状态"

    $services = @(
        @{Url = "http://localhost:$env:FRONTEND_PORT"; Name = "前端"},
        @{Url = "http://localhost:$env:BACKEND_PORT/actuator/health"; Name = "后端"}
    )

    $allOk = $true
    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri $service.Url -TimeoutSec 5 -ErrorAction SilentlyContinue
            Write-Success "$($service.Name) 服务运行正常 ($($service.Url))"
        } catch {
            Write-Warning "$($service.Name) 服务可能未就绪 ($($service.Url))"
            $allOk = $false
        }
    }

    if ($allOk) {
        Write-Success "所有服务运行正常"
    } else {
        Write-Warning "部分服务可能正在启动，请稍后访问"
    }
}

# 显示部署信息
function Show-DeploymentInfo {
    Write-Step "8. 部署信息"

    try {
        $localIp = (Test-Connection -ComputerName (hostname) -Count 1).IPV4Address.IPAddressToString
    } catch {
        $localIp = "localhost"
    }

    Write-Host "$Cyan================================================"
    Write-Host "    ESP32-S3 老年人健康助手系统"
    Write-Host "           Windows 部署完成！"
    Write-Host "================================================$Reset"
    Write-Host ""
    Write-Host "🌐 访问地址:"
    Write-Host "    前端界面: http://localhost:$env:FRONTEND_PORT"
    Write-Host "    后端API: http://localhost:$env:BACKEND_PORT"
    Write-Host "    健康检查: http://localhost:$env:BACKEND_PORT/actuator/health"
    Write-Host ""
    Write-Host "🔧 管理信息:"
    Write-Host "    后端进程PID: $(if (Test-Path 'backend.pid') { Get-Content 'backend.pid' } else { '未记录' })"
    Write-Host "    前端进程PID: $(if (Test-Path 'frontend.pid') { Get-Content 'frontend.pid' } else { '未记录' })"
    Write-Host "    日志目录: logs\"
    Write-Host ""
    Write-Host "📁 项目目录:"
    Write-Host "    后端代码: esp32-backend\"
    Write-Host "    前端代码: esp32-frontend\"
    Write-Host "    部署脚本: deploy\"
    Write-Host ""
    Write-Host "⚙️ 管理命令:"
    Write-Host "    查看后端日志: Get-Content logs\backend.log -Wait"
    Write-Host "    查看前端日志: Get-Content logs\frontend.log -Wait"
    Write-Host "    停止服务: .\deploy\stop-windows.ps1"
    Write-Host "    重启服务: .\deploy\restart-windows.ps1"
    Write-Host ""
    Write-Host "📱 ESP32设备配置:"
    Write-Host "    服务器地址: $localIp"
    Write-Host "    设备ID: esp32-01 (可在前端修改)"
    Write-Host "================================================"
}

# 停止服务
function Stop-Services {
    Write-Step "停止服务"

    if (Test-Path "backend.pid") {
        Write-Info "停止后端服务..."
        $backendId = Get-Content "backend.pid"
        try {
            Stop-Process -Id $backendId -Force -ErrorAction SilentlyContinue
            Write-Success "后端服务已停止"
        } catch {
            Write-Warning "无法停止后端进程，可能已结束"
        }
        Remove-Item "backend.pid" -Force -ErrorAction SilentlyContinue
    }

    if (Test-Path "frontend.pid") {
        Write-Info "停止前端服务..."
        $frontendId = Get-Content "frontend.pid"
        try {
            Stop-Process -Id $frontendId -Force -ErrorAction SilentlyContinue
            Write-Success "前端服务已停止"
        } catch {
            Write-Warning "无法停止前端进程，可能已结束"
        }
        Remove-Item "frontend.pid" -Force -ErrorAction SilentlyContinue
    }

    # 停止可能的Java进程
    $javaProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -eq "java" }
    foreach ($process in $javaProcesses) {
        if ($process.Path -like "*esp32-robot*") {
            Write-Info "停止Java进程: $($process.Id)"
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }

    # 停止可能的Node进程
    $nodeProcesses = Get-Process node -ErrorAction SilentlyContinue
    foreach ($process in $nodeProcesses) {
        if ($process.Path -like "*esp32-frontend*") {
            Write-Info "停止Node进程: $($process.Id)"
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }

    Write-Success "服务已停止"
}

# 清理环境
function Cleanup-Environment {
    Write-Step "清理临时文件"

    Remove-Item "backend.pid", "frontend.pid" -ErrorAction SilentlyContinue
    Remove-Item "deploy\start-frontend.ps1" -ErrorAction SilentlyContinue

    if (Test-Path "esp32-backend\target") {
        Write-Info "清理后端构建产物..."
        Remove-Item "esp32-backend\target" -Recurse -Force -ErrorAction SilentlyContinue
    }

    if (Test-Path "esp32-frontend\dist") {
        Write-Info "清理前端构建产物..."
        Remove-Item "esp32-frontend\dist" -Recurse -Force -ErrorAction SilentlyContinue
    }

    if (Test-Path "esp32-frontend\node_modules") {
        Write-Info "清理前端依赖..."
        Remove-Item "esp32-frontend\node_modules" -Recurse -Force -ErrorAction SilentlyContinue
    }

    Write-Success "清理完成"
}

# 显示帮助
function Show-Help {
    Write-Host "$Cyan用法: .\deploy\deploy-windows.ps1 [选项]$Reset"
    Write-Host ""
    Write-Host "选项:"
    Write-Host "  -Help              显示此帮助信息"
    Write-Host "  -Cleanup           清理临时文件和构建产物"
    Write-Host "  -Stop              停止所有服务"
    Write-Host ""
    Write-Host "示例:"
    Write-Host "  .\deploy\deploy-windows.ps1                     # 完整部署"
    Write-Host "  .\deploy\deploy-windows.ps1 -Stop              # 停止服务"
    Write-Host "  .\deploy\deploy-windows.ps1 -Cleanup           # 清理环境"
    Write-Host ""
    Write-Host "注意: 首次运行可能需要管理员权限"
}

# 主函数
function Main {
    param(
        [switch]$Cleanup,
        [switch]$Stop,
        [switch]$Help
    )

    if ($Help) {
        Show-Help
        return
    }

    Show-Banner

    if ($Stop) {
        Stop-Services
        return
    }

    if ($Cleanup) {
        Cleanup-Environment
        return
    }

    # 开始部署
    Test-Dependencies
    Load-Environment
    Initialize-Project
    Build-Backend
    Build-Frontend
    Start-Services
    Test-ServiceStatus
    Show-DeploymentInfo

    # 记录部署日志
    "$(Get-Date): 系统部署完成" | Out-File -Append -FilePath "logs\deploy.log" -Encoding UTF8
}

# 异常处理
trap {
    Write-Error "部署过程中断: $_"
    Write-Info "正在清理..."
    Stop-Services
    exit 1
}

# 执行主函数
Main @args
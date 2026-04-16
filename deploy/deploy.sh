#!/bin/bash

# ============================================================================
# ESP32-S3老年人健康助手系统 - 一键部署脚本
# 版本: 1.0.0
# 作者: 计算机232陈银素 2301020103
# 描述: 自动化部署SpringBoot后端、Vue前端、MySQL数据库
# ============================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${CYAN}==============================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}==============================${NC}"
}

# 显示横幅
show_banner() {
    clear
    echo -e "${MAGENTA}"
    echo "███████╗██████╗ ███████╗██████╗ ██╗  ██╗"
    echo "██╔════╝██╔══██╗██╔════╝██╔══██╗╚██╗██╔╝"
    echo "█████╗  ██████╔╝███████╗██║  ██║ ╚███╔╝ "
    echo "██╔══╝  ██╔══██╗╚════██║██║  ██║ ██╔██╗ "
    echo "███████╗██║  ██║███████║██████╔╝██╔╝ ██╗"
    echo "╚══════╝╚═╝  ╚═╝╚══════╝╚═════╝ ╚═╝  ╚═╝"
    echo -e "${NC}"
    echo -e "${CYAN}  ESP32-S3 老年人健康助手系统${NC}"
    echo -e "${CYAN}  自动化部署脚本 v1.0.0${NC}"
    echo ""
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "未找到命令: $1"
        log_info "请先安装: $2"
        exit 1
    fi
    log_success "$1 已安装"
}

# 检查系统依赖
check_dependencies() {
    log_step "1. 检查系统依赖"

    check_command "java" "OpenJDK 17+ (apt install openjdk-17-jdk)"
    check_command "node" "Node.js 18+ (https://nodejs.org/)"
    check_command "npm" "Node Package Manager"
    check_command "mvn" "Maven 3.6+ (apt install maven)"
    check_command "git" "Git (apt install git)"
    check_command "docker" "Docker (https://docs.docker.com/engine/install/)"
    check_command "docker-compose" "Docker Compose (https://docs.docker.com/compose/install/)"
    check_command "mysql" "MySQL Client (apt install mysql-client)"

    # 检查Node.js版本
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ $NODE_VERSION -lt 18 ]; then
        log_error "Node.js 版本过低 (需要 >= 18.x)"
        exit 1
    fi

    # 检查Java版本
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ $JAVA_VERSION -lt 17 ]; then
        log_error "Java 版本过低 (需要 >= 17)"
        exit 1
    fi
}

# 检查端口占用
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "端口 $1 已被占用"
        read -p "是否杀死占用进程？(y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            lsof -ti:$1 | xargs kill -9
            log_success "已杀死端口 $1 的占用进程"
        else
            log_error "请手动释放端口 $1 后再试"
            exit 1
        fi
    fi
}

# 加载环境变量
load_env() {
    log_step "2. 加载环境变量"

    if [ -f .env ]; then
        log_info "从 .env 文件加载环境变量"
        source .env
    else
        if [ -f .env.example ]; then
            log_warning ".env 文件不存在，从 .env.example 创建"
            cp .env.example .env
            source .env
        else
            log_error "未找到 .env 或 .env.example 文件"
            exit 1
        fi
    fi

    # 设置默认值
    DB_PASSWORD=${DB_PASSWORD:-"esp32_health_2024"}
    DB_NAME=${DB_NAME:-"esp32_robot_db"}
    DB_USER=${DB_USER:-"esp32_user"}
    BACKEND_PORT=${BACKEND_PORT:-8080}
    FRONTEND_PORT=${FRONTEND_PORT:-80}
    MYSQL_PORT=${MYSQL_PORT:-3306}

    log_success "环境变量加载完成"
}

# 初始化项目目录
init_project() {
    log_step "3. 初始化项目目录"

    if [ ! -d "esp32-backend" ]; then
        log_info "克隆后端代码..."
        git clone https://github.com/your-username/esp32-robot.git esp32-backend
    else
        log_info "更新后端代码..."
        cd esp32-backend
        git pull origin main
        cd ..
    fi

    if [ ! -d "esp32-frontend" ]; then
        log_info "克隆前端代码..."
        git clone https://github.com/your-username/esp32-frontend.git esp32-frontend
    else
        log_info "更新前端代码..."
        cd esp32-frontend
        git pull origin main
        cd ..
    fi

    # 创建必要目录
    mkdir -p logs backups uploads database/backups

    log_success "项目目录初始化完成"
}

# 设置数据库
setup_database() {
    log_step "4. 设置MySQL数据库"

    # 检查MySQL服务状态
    if ! systemctl is-active --quiet mysql; then
        log_warning "MySQL服务未运行，尝试启动..."
        sudo systemctl start mysql
    fi

    # 创建数据库
    log_info "创建数据库: $DB_NAME"
    mysql -u root -p$DB_PASSWORD -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || {
        log_error "数据库创建失败"
        exit 1
    }

    # 创建用户并授权
    log_info "创建数据库用户: $DB_USER"
    mysql -u root -p$DB_PASSWORD -e "CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';" || {
        log_error "用户创建失败"
        exit 1
    }

    mysql -u root -p$DB_PASSWORD -e "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';" || {
        log_error "授权失败"
        exit 1
    }

    mysql -u root -p$DB_PASSWORD -e "FLUSH PRIVILEGES;" || {
        log_error "刷新权限失败"
        exit 1
    }

    # 执行SQL脚本
    if [ -f "esp32-backend/src/main/resources/schema.sql" ]; then
        log_info "执行数据库初始化脚本..."
        mysql -u root -p$DB_PASSWORD $DB_NAME < esp32-backend/src/main/resources/schema.sql
    fi

    if [ -f "esp32-backend/src/main/resources/data.sql" ]; then
        log_info "导入初始数据..."
        mysql -u root -p$DB_PASSWORD $DB_NAME < esp32-backend/src/main/resources/data.sql
    fi

    log_success "数据库设置完成"
}

# 构建后端
build_backend() {
    log_step "5. 构建SpringBoot后端"

    cd esp32-backend

    # 更新配置文件
    log_info "更新应用配置文件..."
    cat > src/main/resources/application.properties << EOF
# 服务器配置
server.port=$BACKEND_PORT
server.servlet.context-path=/

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:$MYSQL_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=$DB_USER
spring.datasource.password=$DB_PASSWORD
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
EOF

    # 构建项目
    log_info "开始构建后端应用..."
    mvn clean package -DskipTests

    if [ $? -ne 0 ]; then
        log_error "后端构建失败"
        exit 1
    fi

    cd ..
    log_success "后端构建完成"
}

# 构建前端
build_frontend() {
    log_step "6. 构建Vue前端"

    cd esp32-frontend

    # 更新环境配置
    log_info "更新前端环境配置..."
    cat > .env.production << EOF
VITE_API_BASE_URL=http://localhost:$BACKEND_PORT
VITE_WS_BASE_URL=ws://localhost:$BACKEND_PORT
VITE_APP_TITLE=ESP32健康助手
VITE_APP_VERSION=1.0.0
EOF

    cat > .env.development << EOF
VITE_API_BASE_URL=http://localhost:$BACKEND_PORT
VITE_WS_BASE_URL=ws://localhost:$BACKEND_PORT
VITE_APP_TITLE=ESP32健康助手(开发版)
VITE_APP_VERSION=1.0.0-dev
EOF

    # 安装依赖
    log_info "安装前端依赖..."
    npm install --registry=https://registry.npmmirror.com

    if [ $? -ne 0 ]; then
        log_error "前端依赖安装失败"
        exit 1
    fi

    # 构建生产版本
    log_info "构建前端生产版本..."
    npm run build

    if [ $? -ne 0 ]; then
        log_error "前端构建失败"
        exit 1
    fi

    cd ..
    log_success "前端构建完成"
}

# 启动服务
start_services() {
    log_step "7. 启动服务"

    # 检查端口占用
    check_port $BACKEND_PORT
    check_port $FRONTEND_PORT
    check_port $MYSQL_PORT

    # 启动MySQL
    log_info "启动MySQL服务..."
    sudo systemctl start mysql
    sleep 3

    # 启动后端
    log_info "启动SpringBoot后端服务..."
    cd esp32-backend
    nohup java -jar target/esp32-robot-0.0.1-SNAPSHOT.jar > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../backend.pid
    cd ..

    sleep 10  # 等待后端启动

    # 检查后端是否启动成功
    if ! curl -s -f http://localhost:$BACKEND_PORT/actuator/health > /dev/null; then
        log_error "后端服务启动失败，查看日志: logs/backend.log"
        exit 1
    fi

    # 启动前端
    log_info "启动前端开发服务器..."
    cd esp32-frontend
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../frontend.pid
    cd ..

    sleep 5  # 等待前端启动

    # 检查前端是否启动成功
    if ! curl -s -f http://localhost:5173 > /dev/null; then
        log_warning "前端开发服务器可能启动较慢，请稍后访问"
    fi

    log_success "服务启动完成"
}

# Docker部署
docker_deploy() {
    log_step "8. Docker容器化部署"

    # 生成docker-compose.yml
    cat > docker-compose.yml << EOF
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: esp32-mysql
    ports:
      - "$MYSQL_PORT:3306"
    environment:
      MYSQL_ROOT_PASSWORD: $DB_PASSWORD
      MYSQL_DATABASE: $DB_NAME
      MYSQL_USER: $DB_USER
      MYSQL_PASSWORD: $DB_PASSWORD
    volumes:
      - ./database/data:/var/lib/mysql
      - ./database/backups:/backups
      - ./esp32-backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - esp32-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  # SpringBoot后端
  backend:
    build: ./esp32-backend
    container_name: esp32-backend
    ports:
      - "$BACKEND_PORT:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:$MYSQL_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
      SPRING_DATASOURCE_USERNAME: $DB_USER
      SPRING_DATASOURCE_PASSWORD: $DB_PASSWORD
      SPRING_PROFILES_ACTIVE: docker
    volumes:
      - ./uploads:/app/uploads
      - ./logs:/app/logs
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - esp32-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Nginx前端代理
  nginx:
    image: nginx:alpine
    container_name: esp32-nginx
    ports:
      - "$FRONTEND_PORT:80"
      - "443:443"
    volumes:
      - ./esp32-frontend/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - backend
    networks:
      - esp32-network
    restart: unless-stopped

networks:
  esp32-network:
    driver: bridge

volumes:
  mysql-data:
  uploads:
  logs:
EOF

    # 生成Nginx配置
    cat > nginx.conf << EOF
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # 启用gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private auth;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml application/javascript application/json;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_cache_bypass \$http_upgrade;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://backend:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_read_timeout 86400;
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 文件上传
    location /uploads/ {
        proxy_pass http://backend:8080/uploads/;
    }

    # 视频流
    location /video/ {
        proxy_pass http://backend:8080/video/;
    }
}
EOF

    # 停止并删除旧容器
    log_info "停止并清理旧容器..."
    docker-compose down --remove-orphans

    # 构建并启动
    log_info "构建并启动容器..."
    docker-compose up -d --build

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 检查服务状态
    check_service_status

    log_success "Docker部署完成"
}

# 检查服务状态
check_service_status() {
    log_step "9. 检查服务状态"

    services=(
        "http://localhost:$FRONTEND_PORT 前端"
        "http://localhost:$BACKEND_PORT/actuator/health 后端"
        "http://localhost:$BACKEND_PORT/ws/alerts WebSocket"
    )

    all_ok=true
    for service in "${services[@]}"; do
        url=$(echo $service | cut -d' ' -f1)
        name=$(echo $service | cut -d' ' -f2-)

        if curl -s -f $url > /dev/null 2>&1; then
            log_success "$name 服务运行正常"
        else
            log_error "$name 服务无法访问"
            all_ok=false
        fi
    done

    if [ "$all_ok" = true ]; then
        log_success "所有服务运行正常"
    else
        log_warning "部分服务可能启动较慢，请稍后检查"
    fi
}

# 显示部署信息
show_deployment_info() {
    log_step "10. 部署信息"

    local_ip=$(hostname -I | awk '{print $1}')

    echo -e "${CYAN}"
    echo "================================================"
    echo "    ESP32-S3 老年人健康助手系统"
    echo "           部署完成！"
    echo "================================================"
    echo -e "${NC}"
    echo "🌐 访问地址:"
    echo "    前端界面: http://$local_ip:$FRONTEND_PORT"
    echo "    后端API: http://$local_ip:$BACKEND_PORT"
    echo "    WebSocket: ws://$local_ip:$BACKEND_PORT/ws/device/{deviceId}"
    echo ""
    echo "🔧 管理地址:"
    echo "    数据库: localhost:$MYSQL_PORT (root:$DB_PASSWORD)"
    echo "    健康检查: http://$local_ip:$BACKEND_PORT/actuator/health"
    echo "    日志查看: tail -f logs/backend.log"
    echo ""
    echo "📁 文件目录:"
    echo "    后端代码: esp32-backend/"
    echo "    前端代码: esp32-frontend/"
    echo "    日志文件: logs/"
    echo "    上传文件: uploads/"
    echo "    数据库备份: database/backups/"
    echo ""
    echo "⚙️ 管理命令:"
    echo "    查看日志: tail -f logs/*.log"
    echo "    停止服务: ./deploy/stop.sh"
    echo "    重启服务: ./deploy/restart.sh"
    echo "    备份数据: ./deploy/backup-database.sh"
    echo "    更新系统: ./deploy/update-system.sh"
    echo ""
    echo "📱 ESP32设备配置:"
    echo "    服务器地址: $local_ip"
    echo "    设备ID: esp32-01 (可在前端修改)"
    echo "    紧急按钮: GPIO0 (默认)"
    echo "================================================"

    # 显示二维码
    if command -v qrencode &> /dev/null; then
        echo ""
        echo "📱 手机扫描二维码访问:"
        qrencode -t ASCII "http://$local_ip:$FRONTEND_PORT"
    fi
}

# 停止服务
stop_services() {
    log_step "停止服务"

    if [ -f "backend.pid" ]; then
        log_info "停止后端服务..."
        kill $(cat backend.pid) 2>/dev/null || true
        rm -f backend.pid
    fi

    if [ -f "frontend.pid" ]; then
        log_info "停止前端服务..."
        kill $(cat frontend.pid) 2>/dev/null || true
        rm -f frontend.pid
    fi

    if [ -f "docker-compose.yml" ]; then
        log_info "停止Docker容器..."
        docker-compose down
    fi

    log_success "服务已停止"
}

# 清理环境
cleanup() {
    log_step "清理临时文件"

    rm -f backend.pid frontend.pid
    rm -rf esp32-backend/target
    rm -rf esp32-frontend/dist
    rm -rf esp32-frontend/node_modules

    log_success "清理完成"
}

# 主函数
main() {
    show_banner

    # 解析命令行参数
    DEPLOY_MODE="native"  # native 或 docker
    CLEANUP=false
    STOP=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            -m|--mode)
                DEPLOY_MODE="$2"
                shift 2
                ;;
            -c|--cleanup)
                CLEANUP=true
                shift
                ;;
            -s|--stop)
                STOP=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done

    if [ "$STOP" = true ]; then
        stop_services
        exit 0
    fi

    if [ "$CLEANUP" = true ]; then
        cleanup
        exit 0
    fi

    # 开始部署
    check_dependencies
    load_env
    init_project
    setup_database

    if [ "$DEPLOY_MODE" = "docker" ]; then
        docker_deploy
    else
        build_backend
        build_frontend
        start_services
    fi

    check_service_status
    show_deployment_info

    # 记录部署日志
    echo "$(date): 系统部署完成" >> logs/deploy.log
}

# 显示帮助
show_help() {
    echo -e "${CYAN}用法: ./deploy.sh [选项]${NC}"
    echo ""
    echo "选项:"
    echo "  -m, --mode MODE     部署模式: native(默认) 或 docker"
    echo "  -c, --cleanup       清理临时文件和构建产物"
    echo "  -s, --stop          停止所有服务"
    echo "  -h, --help          显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  ./deploy.sh                     # 原生部署"
    echo "  ./deploy.sh -m docker           # Docker容器部署"
    echo "  ./deploy.sh -s                  # 停止服务"
    echo "  ./deploy.sh -c                  # 清理环境"
}

# 异常处理
trap 'log_error "部署过程中断，正在清理..."; stop_services; exit 1' INT TERM

# 执行主函数
main "$@"
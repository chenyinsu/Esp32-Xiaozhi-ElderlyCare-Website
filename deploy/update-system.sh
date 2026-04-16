#!/bin/bash

# 系统更新脚本
echo "更新系统..."
git pull origin main

echo "更新后端..."
cd esp32-backend
git pull origin main
mvn clean package -DskipTests
cd ..

echo "更新前端..."
cd esp32-frontend
git pull origin main
npm install
npm run build
cd ..

echo "重启服务..."
./deploy.sh --stop
./deploy.sh

echo "系统更新完成"
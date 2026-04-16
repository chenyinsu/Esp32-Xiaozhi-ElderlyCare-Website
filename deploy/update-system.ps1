# 系统更新脚本
Write-Host "正在更新ESP32健康助手系统..." -ForegroundColor Yellow

# 更新代码
git pull origin main

# 更新后端
Write-Host "更新后端..." -ForegroundColor Cyan
Set-Location esp32-backend
git pull origin main
mvn clean package -DskipTests
Set-Location ..

# 更新前端
Write-Host "更新前端..." -ForegroundColor Cyan
Set-Location esp32-frontend
git pull origin main
npm install
npm run build
Set-Location ..

# 重启服务
Write-Host "重启服务..." -ForegroundColor Cyan
.\deploy-windows.ps1

Write-Host "`n✅ 系统更新完成" -ForegroundColor Green
# 重启服务脚本
Write-Host "正在重启ESP32健康助手系统..." -ForegroundColor Yellow

# 先停止
.\stop-windows.ps1

# 等待3秒
Start-Sleep -Seconds 3

# 再启动
Write-Host "`n正在启动服务..." -ForegroundColor Cyan
..\deploy-windows.ps1
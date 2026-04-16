# 停止服务脚本
Write-Host "正在停止ESP32健康助手系统..." -ForegroundColor Yellow

# 读取PID文件
if (Test-Path "..\backend.pid") {
    $backendId = Get-Content "..\backend.pid"
    Write-Host "停止后端服务 (PID: $backendId)..." -ForegroundColor Cyan
    Stop-Process -Id $backendId -Force -ErrorAction SilentlyContinue
    Remove-Item "..\backend.pid" -Force -ErrorAction SilentlyContinue
}

if (Test-Path "..\frontend.pid") {
    $frontendId = Get-Content "..\frontend.pid"
    Write-Host "停止前端服务 (PID: $frontendId)..." -ForegroundColor Cyan
    Stop-Process -Id $frontendId -Force -ErrorAction SilentlyContinue
    Remove-Item "..\frontend.pid" -Force -ErrorAction SilentlyContinue
}

Write-Host "`n✅ 服务已停止" -ForegroundColor Green
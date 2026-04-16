# 数据库备份脚本
$backupDir = "database\backups"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "$backupDir\esp32_db_$timestamp.sql"

# 加载环境变量
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
}

# 执行备份
mysqldump -u root -p$env:DB_PASSWORD $env:DB_NAME > $backupFile

# 压缩备份
Compress-Archive -Path $backupFile -DestinationPath "$backupFile.zip" -Force
Remove-Item $backupFile

Write-Host "✅ 数据库备份完成: $backupFile.zip" -ForegroundColor Green
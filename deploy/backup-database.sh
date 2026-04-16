#!/bin/bash

# 数据库备份脚本
DB_BACKUP_DIR="database/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$DB_BACKUP_DIR/esp32_db_$TIMESTAMP.sql"

# 加载环境变量
source .env 2>/dev/null || source .env.example

# 执行备份
mysqldump -u root -p$DB_PASSWORD $DB_NAME > $BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_FILE

echo "数据库备份完成: $BACKUP_FILE.gz"
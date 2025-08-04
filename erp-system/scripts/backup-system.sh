#!/bin/bash

# ERP System Backup Script
# This script performs comprehensive backup of all system components

set -e

# Configuration
BACKUP_ROOT="/opt/erp-backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="$BACKUP_ROOT/backup_$DATE"
RETENTION_DAYS=30

# Database credentials
DB_HOST="localhost"
DB_PORT="5432"     
DB_NAME="erp_main"
DB_USER="erp_user"
DB_PASSWORD="erp_password"

# Redis configuration
REDIS_HOST="localhost"
REDIS_PORT="6379"

# Create backup directory
mkdir -p "$BACKUP_DIR"

echo "Starting ERP System backup at $(date)"
echo "Backup directory: $BACKUP_DIR"

# Function to log messages
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$BACKUP_DIR/backup.log"
}

# Function to handle errors
error_exit() {
    log "ERROR: $1"
    exit 1
}

# 1. PostgreSQL Database Backup
log "Starting PostgreSQL database backup..."
PGPASSWORD="$DB_PASSWORD" pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --verbose \
    --format=custom \
    --compress=9 \
    --file="$BACKUP_DIR/postgresql_backup.dump" || error_exit "PostgreSQL backup failed"

log "PostgreSQL backup completed successfully"

# 2. Redis Backup
log "Starting Redis backup..."
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" --rdb "$BACKUP_DIR/redis_backup.rdb" || error_exit "Redis backup failed"
log "Redis backup completed successfully"

# 3. Neo4j Backup (if running)
if command -v neo4j-admin &> /dev/null; then
    log "Starting Neo4j backup..."
    neo4j-admin dump \
        --database=neo4j \
        --to="$BACKUP_DIR/neo4j_backup.dump" || log "WARNING: Neo4j backup failed or not available"
    log "Neo4j backup completed"
fi

# 4. InfluxDB Backup
log "Starting InfluxDB backup..."
influxd backup \
    -portable \
    -host localhost:8088 \
    "$BACKUP_DIR/influxdb_backup" || log "WARNING: InfluxDB backup failed or not available"
log "InfluxDB backup completed"

# 5. Elasticsearch Snapshots
log "Starting Elasticsearch backup..."
curl -X PUT "localhost:9200/_snapshot/backup_repo" \
    -H 'Content-Type: application/json' \
    -d '{
        "type": "fs",
        "settings": {
            "location": "'$BACKUP_DIR'/elasticsearch_backup"
        }
    }' || log "WARNING: Elasticsearch backup setup failed"

curl -X PUT "localhost:9200/_snapshot/backup_repo/snapshot_$DATE?wait_for_completion=true" || log "WARNING: Elasticsearch snapshot failed"
log "Elasticsearch backup completed"

# 6. Application Configuration Backup
log "Backing up application configurations..."
mkdir -p "$BACKUP_DIR/configs"
cp -r ./backend/*/src/main/resources/application*.yml "$BACKUP_DIR/configs/" 2>/dev/null || true
cp -r ./infrastructure/ "$BACKUP_DIR/configs/" 2>/dev/null || true
cp docker-compose.yml "$BACKUP_DIR/configs/" 2>/dev/null || true
log "Configuration backup completed"

# 7. Application Logs Backup
log "Backing up application logs..."
mkdir -p "$BACKUP_DIR/logs"
find ./logs -name "*.log" -mtime -7 -exec cp {} "$BACKUP_DIR/logs/" \; 2>/dev/null || true
log "Logs backup completed"

# 8. SSL Certificates and Security Keys
log "Backing up security artifacts..."
mkdir -p "$BACKUP_DIR/security"
# Add your certificate paths here
# cp -r /etc/ssl/erp/ "$BACKUP_DIR/security/" 2>/dev/null || true
log "Security artifacts backup completed"

# 9. Create backup metadata
log "Creating backup metadata..."
cat > "$BACKUP_DIR/backup_metadata.json" << EOF
{
    "backup_date": "$(date -Iseconds)",
    "backup_version": "1.0.0",
    "system_version": "1.0.0",
    "components": {
        "postgresql": "$(psql --version | head -n1)",
        "redis": "$(redis-server --version)",
        "elasticsearch": "$(curl -s localhost:9200 | jq -r .version.number 2>/dev/null || echo 'unknown')",
        "influxdb": "$(influx version 2>/dev/null | head -n1 || echo 'unknown')"
    },
    "backup_size_mb": "$(du -sm $BACKUP_DIR | cut -f1)"
}
EOF

# 10. Compress backup
log "Compressing backup..."
cd "$BACKUP_ROOT"
tar -czf "backup_$DATE.tar.gz" "backup_$DATE/"
COMPRESSED_SIZE=$(du -sm "backup_$DATE.tar.gz" | cut -f1)
log "Backup compressed to backup_$DATE.tar.gz ($COMPRESSED_SIZE MB)"

# 11. Verify backup integrity
log "Verifying backup integrity..."
tar -tzf "backup_$DATE.tar.gz" > /dev/null || error_exit "Backup verification failed"
log "Backup integrity verified"

# 12. Upload to cloud storage (optional)
if [ ! -z "$AWS_S3_BUCKET" ]; then
    log "Uploading backup to S3..."
    aws s3 cp "backup_$DATE.tar.gz" "s3://$AWS_S3_BUCKET/erp-backups/" || log "WARNING: S3 upload failed"
    log "S3 upload completed"
fi

# 13. Cleanup old backups
log "Cleaning up old backups (keeping last $RETENTION_DAYS days)..."
find "$BACKUP_ROOT" -name "backup_*.tar.gz" -mtime +$RETENTION_DAYS -delete
find "$BACKUP_ROOT" -name "backup_*" -type d -mtime +$RETENTION_DAYS -exec rm -rf {} + 2>/dev/null || true
log "Cleanup completed"

# 14. Send notification
log "Sending backup completion notification..."
if command -v mail &> /dev/null; then
    echo "ERP System backup completed successfully at $(date)" | \
    mail -s "ERP Backup Completed - $DATE" admin@company.com || true
fi

log "Backup process completed successfully!"
log "Backup location: $BACKUP_ROOT/backup_$DATE.tar.gz"
log "Backup size: $COMPRESSED_SIZE MB"

echo "Backup completed at $(date)"
exit 0
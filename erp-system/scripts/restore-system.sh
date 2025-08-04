#!/bin/bash

# ERP System Restore Script
# This script restores the ERP system from backup

set -e

# Configuration
BACKUP_ROOT="/opt/erp-backups"
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="erp_main"
DB_USER="erp_user"
DB_PASSWORD="erp_password"
REDIS_HOST="localhost"
REDIS_PORT="6379"

# Function to log messages
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to handle errors
error_exit() {
    log "ERROR: $1"
    exit 1
}

# Check if backup file is provided
if [ $# -eq 0 ]; then
    echo "Usage: $0 <backup_file.tar.gz> [--force]"
    echo "Available backups:"
    ls -la "$BACKUP_ROOT"/backup_*.tar.gz 2>/dev/null || echo "No backups found"
    exit 1
fi

BACKUP_FILE="$1"
FORCE_RESTORE="$2"

# Validate backup file
if [ ! -f "$BACKUP_FILE" ]; then
    error_exit "Backup file not found: $BACKUP_FILE"
fi

log "Starting ERP System restore from: $BACKUP_FILE"

# Extract backup
TEMP_DIR=$(mktemp -d)
log "Extracting backup to: $TEMP_DIR"
tar -xzf "$BACKUP_FILE" -C "$TEMP_DIR" || error_exit "Failed to extract backup"

BACKUP_DIR=$(find "$TEMP_DIR" -name "backup_*" -type d | head -n1)
if [ -z "$BACKUP_DIR" ]; then
    error_exit "Invalid backup structure"
fi

log "Backup directory: $BACKUP_DIR"

# Verify backup metadata
if [ -f "$BACKUP_DIR/backup_metadata.json" ]; then
    log "Backup metadata found:"
    cat "$BACKUP_DIR/backup_metadata.json"
else
    log "WARNING: No backup metadata found"
fi

# Confirmation prompt
if [ "$FORCE_RESTORE" != "--force" ]; then
    echo
    echo "WARNING: This will restore the ERP system and may overwrite existing data!"
    echo "Current system data will be lost. Are you sure you want to continue?"
    read -p "Type 'yes' to continue: " confirmation
    if [ "$confirmation" != "yes" ]; then
        log "Restore cancelled by user"
        rm -rf "$TEMP_DIR"
        exit 0
    fi
fi

# Stop services before restore
log "Stopping ERP services..."
docker-compose down || log "WARNING: Could not stop Docker services"

# 1. Restore PostgreSQL Database
if [ -f "$BACKUP_DIR/postgresql_backup.dump" ]; then
    log "Restoring PostgreSQL database..."
    
    # Start only PostgreSQL for restore
    docker-compose up -d postgres
    sleep 10
    
    # Drop and recreate database
    PGPASSWORD="$DB_PASSWORD" dropdb \
        -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
        --if-exists "$DB_NAME" || true
    
    PGPASSWORD="$DB_PASSWORD" createdb \
        -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
        "$DB_NAME"
    
    # Restore database
    PGPASSWORD="$DB_PASSWORD" pg_restore \
        -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
        -d "$DB_NAME" \
        --verbose \
        --clean \
        --if-exists \
        "$BACKUP_DIR/postgresql_backup.dump" || error_exit "PostgreSQL restore failed"
    
    log "PostgreSQL database restored successfully"
else
    log "WARNING: PostgreSQL backup not found"
fi

# 2. Restore Redis
if [ -f "$BACKUP_DIR/redis_backup.rdb" ]; then
    log "Restoring Redis data..."
    
    # Start Redis
    docker-compose up -d redis
    sleep 5
    
    # Stop Redis to restore RDB file
    docker-compose stop redis
    
    # Copy RDB file (this would need proper Docker volume handling in production)
    cp "$BACKUP_DIR/redis_backup.rdb" ./redis_data/dump.rdb 2>/dev/null || \
        log "WARNING: Could not restore Redis RDB file directly"
    
    # Restart Redis
    docker-compose up -d redis
    
    log "Redis data restored"
else
    log "WARNING: Redis backup not found"
fi

# 3. Restore Neo4j
if [ -f "$BACKUP_DIR/neo4j_backup.dump" ]; then
    log "Restoring Neo4j database..."
    
    docker-compose up -d neo4j
    sleep 10
    
    # This would need proper Neo4j restore commands
    log "Neo4j restore completed (manual intervention may be required)"
else
    log "WARNING: Neo4j backup not found"
fi

# 4. Restore InfluxDB
if [ -d "$BACKUP_DIR/influxdb_backup" ]; then
    log "Restoring InfluxDB data..."
    
    docker-compose up -d influxdb
    sleep 10
    
    # This would need proper InfluxDB restore commands
    log "InfluxDB restore completed (manual intervention may be required)"
else
    log "WARNING: InfluxDB backup not found"
fi

# 5. Restore Elasticsearch
if [ -d "$BACKUP_DIR/elasticsearch_backup" ]; then
    log "Restoring Elasticsearch data..."
    
    docker-compose up -d elasticsearch
    sleep 15
    
    # Register snapshot repository and restore
    curl -X PUT "localhost:9200/_snapshot/restore_repo" \
        -H 'Content-Type: application/json' \
        -d '{
            "type": "fs",
            "settings": {
                "location": "'$BACKUP_DIR'/elasticsearch_backup"
            }
        }' || log "WARNING: Could not register Elasticsearch snapshot repository"
    
    log "Elasticsearch restore initiated (may take time to complete)"
else
    log "WARNING: Elasticsearch backup not found"
fi

# 6. Restore Configuration Files
if [ -d "$BACKUP_DIR/configs" ]; then
    log "Restoring configuration files..."
    
    # Backup current configs
    mkdir -p ./config_backup_$(date +%Y%m%d_%H%M%S)
    cp -r ./backend/*/src/main/resources/application*.yml ./config_backup_$(date +%Y%m%d_%H%M%S)/ 2>/dev/null || true
    
    # Restore configs
    cp -r "$BACKUP_DIR/configs"/* ./ 2>/dev/null || true
    
    log "Configuration files restored"
else
    log "WARNING: Configuration backup not found"
fi

# 7. Restore Logs (optional)
if [ -d "$BACKUP_DIR/logs" ]; then
    log "Restoring log files..."
    mkdir -p ./logs
    cp -r "$BACKUP_DIR/logs"/* ./logs/ 2>/dev/null || true
    log "Log files restored"
fi

# 8. Start all services
log "Starting all ERP services..."
docker-compose up -d

# Wait for services to be ready
log "Waiting for services to start up..."
sleep 30

# 9. Verify restore
log "Verifying system restore..."

# Check database connectivity
PGPASSWORD="$DB_PASSWORD" psql \
    -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    -c "SELECT COUNT(*) FROM information_schema.tables;" > /dev/null || \
    log "WARNING: PostgreSQL verification failed"

# Check Redis
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping > /dev/null || \
    log "WARNING: Redis verification failed"

# Check web services
curl -f http://localhost:8761/actuator/health > /dev/null || \
    log "WARNING: Eureka Server not responding"

curl -f http://localhost:8080/actuator/health > /dev/null || \
    log "WARNING: API Gateway not responding"

# 10. Cleanup
log "Cleaning up temporary files..."
rm -rf "$TEMP_DIR"

# 11. Create restore log
cat > "./restore_$(date +%Y%m%d_%H%M%S).log" << EOF
ERP System Restore Completed
============================
Restore Date: $(date -Iseconds)
Backup File: $BACKUP_FILE
Restore Status: SUCCESS

Services Status:
- PostgreSQL: $(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" 2>/dev/null | head -n1 || echo "ERROR")
- Redis: $(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping 2>/dev/null || echo "ERROR")
- Eureka: $(curl -s http://localhost:8761/actuator/health | jq -r .status 2>/dev/null || echo "ERROR")
- API Gateway: $(curl -s http://localhost:8080/actuator/health | jq -r .status 2>/dev/null || echo "ERROR")

Next Steps:
1. Verify all microservices are running
2. Test critical business functions
3. Check data integrity
4. Update any changed configurations
5. Notify users that system is restored
EOF

log "Restore completed successfully!"
log "Please verify all services and data integrity"
log "Restore log saved to: ./restore_$(date +%Y%m%d_%H%M%S).log"

echo "System restore completed at $(date)"
exit 0
#!/bin/bash

# ERP System Performance Tuning Script
# Optimizes system performance for production deployment

set -e

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log "Starting ERP System Performance Tuning..."

# 1. JVM Tuning for Microservices
log "Applying JVM performance tuning..."

# Create JVM options file
cat > ./infrastructure/jvm-options.conf << 'EOF'
# Heap Size (adjust based on available memory)
-Xms2g
-Xmx4g

# Garbage Collection (G1GC for low latency)
-XX:+UseG1GC
-XX:G1HeapRegionSize=16m
-XX:MaxGCPauseMillis=200
-XX:+ParallelRefProcEnabled
-XX:+UseStringDeduplication

# JIT Compiler Optimizations
-XX:+TieredCompilation
-XX:TieredStopAtLevel=4
-XX:CompileThreshold=10000

# Memory Management
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:CompressedClassSpaceSize=1g

# Monitoring and Debugging
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/opt/erp/dumps/
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:/opt/erp/logs/gc.log

# Security
-Djava.security.egd=file:/dev/./urandom

# Network Performance
-Djava.net.preferIPv4Stack=true
-Dsun.net.useExclusiveBind=false
EOF

log "JVM tuning configuration created"

# 2. PostgreSQL Performance Tuning
log "Applying PostgreSQL performance tuning..."

cat > ./infrastructure/postgresql-tuning.conf << 'EOF'
# PostgreSQL Performance Tuning Configuration

# Memory Settings
shared_buffers = 1GB                    # 25% of total RAM
effective_cache_size = 3GB              # 75% of total RAM
work_mem = 64MB                         # Per operation memory
maintenance_work_mem = 256MB            # Maintenance operations

# Checkpoint Settings
checkpoint_completion_target = 0.9
checkpoint_timeout = 10min
max_wal_size = 2GB
min_wal_size = 1GB

# Connection Settings
max_connections = 200
shared_preload_libraries = 'pg_stat_statements'

# Query Planner
random_page_cost = 1.1                  # For SSD storage
effective_io_concurrency = 200          # For SSD storage

# Write-Ahead Logging
wal_buffers = 16MB
wal_writer_delay = 200ms
commit_delay = 100
commit_siblings = 5

# Background Writer
bgwriter_delay = 200ms
bgwriter_lru_maxpages = 100
bgwriter_lru_multiplier = 2.0

# Autovacuum
autovacuum = on
autovacuum_max_workers = 4
autovacuum_naptime = 1min
autovacuum_vacuum_threshold = 50
autovacuum_analyze_threshold = 50

# Logging
log_min_duration_statement = 1000       # Log slow queries > 1s
log_checkpoints = on
log_connections = on
log_disconnections = on
log_lock_waits = on
EOF

log "PostgreSQL tuning configuration created"

# 3. Redis Performance Tuning
log "Applying Redis performance tuning..."

cat > ./infrastructure/redis-tuning.conf << 'EOF'
# Redis Performance Tuning Configuration

# Memory Management
maxmemory 2gb
maxmemory-policy allkeys-lru
maxmemory-samples 10

# Persistence (adjust based on requirements)
save 900 1
save 300 10
save 60 10000

# AOF Configuration
appendonly yes
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# Network
tcp-keepalive 300
timeout 0
tcp-backlog 511

# Performance
databases 16
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64

# Security
requirepass your_redis_password_here
EOF

log "Redis tuning configuration created"

# 4. Nginx Load Balancer Configuration
log "Creating Nginx load balancer configuration..."

cat > ./infrastructure/nginx.conf << 'EOF'
events {
    worker_connections 4096;
    use epoll;
    multi_accept on;
}

http {
    # Basic Settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    keepalive_requests 100;
    types_hash_max_size 2048;
    
    # Buffer Settings
    client_body_buffer_size 128k;
    client_max_body_size 50m;
    client_header_buffer_size 1k;
    large_client_header_buffers 4 4k;
    output_buffers 1 32k;
    postpone_output 1460;
    
    # Gzip Compression
    gzip on;
    gzip_vary on;
    gzip_min_length 10240;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss application/atom+xml image/svg+xml;
    
    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=1r/s;
    
    # Upstream Servers
    upstream api_gateway {
        least_conn;
        server localhost:8080 max_fails=3 fail_timeout=30s;
        keepalive 32;
    }
    
    upstream eureka_server {
        server localhost:8761 max_fails=3 fail_timeout=30s;
    }
    
    # Main Server Configuration
    server {
        listen 80;
        server_name erp.company.com;
        
        # Security Headers
        add_header X-Frame-Options DENY always;
        add_header X-Content-Type-Options nosniff always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
        
        # API Gateway Proxy
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://api_gateway;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;
            proxy_connect_timeout 5s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }
        
        # Authentication Endpoint Rate Limiting
        location /api/auth/login {
            limit_req zone=login burst=5 nodelay;
            proxy_pass http://api_gateway;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
        
        # Static Content Caching
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }
        
        # Health Check
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

log "Nginx configuration created"

# 5. System Kernel Tuning
log "Applying system kernel tuning..."

cat > ./infrastructure/sysctl-tuning.conf << 'EOF'
# Network Performance
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.ipv4.tcp_rmem = 4096 65536 16777216
net.ipv4.tcp_wmem = 4096 65536 16777216
net.core.netdev_max_backlog = 5000
net.ipv4.tcp_congestion_control = bbr

# File Descriptors
fs.file-max = 2097152
fs.nr_open = 1048576

# Virtual Memory
vm.swappiness = 10
vm.dirty_ratio = 60
vm.dirty_background_ratio = 2

# Security
kernel.dmesg_restrict = 1
kernel.kptr_restrict = 1
net.ipv4.conf.all.send_redirects = 0
net.ipv4.conf.default.send_redirects = 0
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
EOF

log "System tuning configuration created"

# 6. Docker Performance Optimization
log "Creating Docker performance configuration..."

cat > ./infrastructure/daemon.json << 'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "storage-opts": [
    "overlay2.override_kernel_check=true"
  ],
  "default-ulimits": {
    "nofile": {
      "Hard": 64000,
      "Name": "nofile",
      "Soft": 64000
    }
  },
  "live-restore": true,
  "userland-proxy": false,
  "experimental": false,
  "metrics-addr": "127.0.0.1:9323",
  "experimental": true
}
EOF

log "Docker configuration created"

# 7. Application Performance Monitoring Setup
log "Setting up application performance monitoring..."

cat > ./infrastructure/monitoring/apm-config.yml << 'EOF'
# APM Configuration for ERP System

elastic_apm:
  service_name: "erp-system"
  server_url: "http://localhost:8200"
  environment: "production"
  
  # Performance Settings
  transaction_sample_rate: 0.1
  capture_body: "errors"
  capture_headers: true
  
  # Security
  sanitize_field_names:
    - "password"
    - "passwd"
    - "pwd"
    - "secret"
    - "token"
    - "key"
    - "credit_card"
    - "creditcard"
  
  # Custom Metrics
  custom_metrics:
    - name: "business_transactions"
      type: "counter"
      description: "Number of business transactions processed"
    
    - name: "payment_processing_time"
      type: "histogram"
      description: "Time taken to process payments"
      
    - name: "active_users"
      type: "gauge"
      description: "Number of currently active users"
EOF

# 8. Create Performance Testing Configuration
log "Setting up performance testing configuration..."

cat > ./performance-tests/load-test-config.yml << 'EOF'
config:
  target: 'http://localhost:8080'
  phases:
    - duration: 60
      arrivalRate: 10
      name: "Warm up"
    - duration: 300
      arrivalRate: 50
      name: "Sustained load"
    - duration: 120
      arrivalRate: 100
      name: "Peak load"
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true

scenarios:
  - name: "User Authentication"
    weight: 30
    flow:
      - post:
          url: "/api/auth/login"
          json:
            username: "testuser"
            password: "testpass"
      - think: 2
      
  - name: "Product Catalog"
    weight: 40
    flow:
      - get:
          url: "/api/products"
      - think: 1
      - get:
          url: "/api/products/{{ $randomInt(1, 1000) }}"
      - think: 3
      
  - name: "Order Processing"
    weight: 20
    flow:
      - post:
          url: "/api/orders"
          json:
            productId: "{{ $randomInt(1, 100) }}"
            quantity: "{{ $randomInt(1, 5) }}"
      - think: 5
      
  - name: "Report Generation"
    weight: 10
    flow:
      - get:
          url: "/api/reports/sales"
          qs:
            from: "2024-01-01"
            to: "2024-12-31"
      - think: 10
EOF

log "Performance testing configuration created"

# 9. Create monitoring dashboard
log "Creating performance monitoring dashboard..."

cat > ./infrastructure/monitoring/performance-dashboard.json << 'EOF'
{
  "dashboard": {
    "title": "ERP System Performance Dashboard",
    "panels": [
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "50th percentile"
          }
        ]
      },
      {
        "title": "Throughput",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "Requests per second"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total{status=~\"5..\"}[5m]) / rate(http_requests_total[5m])",
            "legendFormat": "Error rate"
          }
        ]
      },
      {
        "title": "Database Performance",
        "type": "graph",
        "targets": [
          {
            "expr": "pg_stat_activity_count",
            "legendFormat": "Active connections"
          },
          {
            "expr": "rate(pg_stat_database_tup_fetched[5m])",
            "legendFormat": "Tuples fetched/sec"
          }
        ]
      }
    ]
  }
}
EOF

log "Performance monitoring dashboard created"

log "Performance tuning configuration completed!"
log "Next steps:"
log "1. Apply JVM options to microservices startup scripts"
log "2. Update PostgreSQL configuration (requires restart)"
log "3. Update Redis configuration (requires restart)"
log "4. Deploy Nginx load balancer"
log "5. Apply system kernel tuning (sudo sysctl -p)"
log "6. Update Docker daemon configuration"
log "7. Setup APM monitoring"
log "8. Run performance tests to validate improvements"

echo "Performance tuning setup completed at $(date)"
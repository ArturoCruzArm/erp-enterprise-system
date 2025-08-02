#!/bin/bash

# Script para iniciar todos los servicios del ERP
echo "🚀 Iniciando Sistema ERP..."

# 1. Levantar infraestructura con Docker
echo "📦 Levantando infraestructura (PostgreSQL, Redis, Kafka, Monitoring)..."
docker-compose up -d

# Esperar a que la base de datos esté lista
echo "⏳ Esperando que PostgreSQL esté listo..."
sleep 10

# 2. Compilar shared library
echo "🔧 Compilando shared library..."
cd backend/shared-lib
mvn clean install -q
cd ../..

# 3. Iniciar Eureka Server
echo "🌐 Iniciando Eureka Server..."
cd backend/eureka-server
mvn spring-boot:run &
EUREKA_PID=$!
cd ../..

# Esperar a que Eureka esté listo
echo "⏳ Esperando que Eureka Server esté listo..."
sleep 15

# 4. Iniciar API Gateway
echo "🚪 Iniciando API Gateway..."
cd backend/api-gateway
mvn spring-boot:run &
GATEWAY_PID=$!
cd ../..

# 5. Iniciar User Service
echo "👤 Iniciando User Service..."
cd backend/user-service
mvn spring-boot:run &
USER_PID=$!
cd ../..

# 6. Iniciar Finance Service
echo "💰 Iniciando Finance Service..."
cd backend/finance-service
mvn spring-boot:run &
FINANCE_PID=$!
cd ../..

echo "✅ Servicios iniciados!"
echo "📋 Información de servicios:"
echo "  - Eureka Server: http://localhost:8761"
echo "  - API Gateway: http://localhost:8080"
echo "  - User Service: http://localhost:8081"
echo "  - Finance Service: http://localhost:8082"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3001 (admin/admin)"

echo ""
echo "🧪 Para probar el login:"
echo "POST http://localhost:8080/api/auth/login"
echo "Body: {\"usernameOrEmail\": \"admin\", \"password\": \"admin123\"}"

# Guardar PIDs para poder detener los servicios
echo $EUREKA_PID > .eureka.pid
echo $GATEWAY_PID > .gateway.pid  
echo $USER_PID > .user.pid
echo $FINANCE_PID > .finance.pid

echo ""
echo "🛑 Para detener los servicios ejecuta: ./scripts/stop-services.sh"
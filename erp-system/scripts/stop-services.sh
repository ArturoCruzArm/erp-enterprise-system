#!/bin/bash

echo "🛑 Deteniendo Sistema ERP..."

# Detener servicios Java
if [ -f .eureka.pid ]; then
    echo "🌐 Deteniendo Eureka Server..."
    kill $(cat .eureka.pid) 2>/dev/null
    rm .eureka.pid
fi

if [ -f .gateway.pid ]; then
    echo "🚪 Deteniendo API Gateway..."
    kill $(cat .gateway.pid) 2>/dev/null
    rm .gateway.pid
fi

if [ -f .user.pid ]; then
    echo "👤 Deteniendo User Service..."
    kill $(cat .user.pid) 2>/dev/null
    rm .user.pid
fi

if [ -f .finance.pid ]; then
    echo "💰 Deteniendo Finance Service..."
    kill $(cat .finance.pid) 2>/dev/null
    rm .finance.pid
fi

# Detener infraestructura Docker
echo "📦 Deteniendo infraestructura Docker..."
docker-compose down

echo "✅ Todos los servicios han sido detenidos."
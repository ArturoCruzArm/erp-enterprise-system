# ERP System Deployment Guide

## Overview

This document provides comprehensive instructions for deploying the ERP System using modern best practices including containerization, orchestration, monitoring, and security.

## Architecture

The ERP System follows a microservices architecture with the following components:

### Backend Services
- **API Gateway** (Port 8080) - Central entry point with routing and security
- **Eureka Server** (Port 8761) - Service discovery and registration
- **User Service** (Port 8081) - Authentication and user management
- **Finance Service** (Port 8082) - Financial operations and accounting
- **Inventory Service** (Port 8083) - Stock and warehouse management
- **HR Service** (Port 8086) - Human resources management
- **Sales Service** (Port 8085) - Sales and CRM operations
- **Purchase Service** (Port 8084) - Procurement and suppliers
- **Production Service** (Port 8087) - Manufacturing and production

### Frontend
- **React Frontend** (Port 3000) - Modern responsive web interface

### Infrastructure
- **PostgreSQL** - Primary database with separate schemas per service
- **Redis** - Caching and session storage
- **Kafka** - Event streaming and inter-service communication
- **Prometheus** - Metrics collection and monitoring
- **Grafana** - Visualization and dashboards

## Prerequisites

### Development Environment
- Java 17+
- Node.js 18+
- Docker 20.10+
- Docker Compose 2.0+
- Maven 3.8+

### Production Environment
- Kubernetes 1.25+
- kubectl configured
- Helm 3.0+
- Container registry (Docker Hub, ECR, GCR, etc.)

## Local Development Setup

### 1. Clone and Setup
```bash
git clone <repository-url>
cd erp-system
```

### 2. Start Infrastructure Services
```bash
docker-compose up -d postgres redis kafka zookeeper prometheus grafana
```

### 3. Build Shared Library
```bash
cd backend/shared-lib
mvn clean install
```

### 4. Start Backend Services
```bash
# Start Eureka Server first
cd backend/eureka-server
mvn spring-boot:run

# Start other services (in separate terminals)
cd backend/api-gateway && mvn spring-boot:run
cd backend/user-service && mvn spring-boot:run
cd backend/finance-service && mvn spring-boot:run
cd backend/inventory-service && mvn spring-boot:run
cd backend/hr-service && mvn spring-boot:run
cd backend/sales-service && mvn spring-boot:run
```

### 5. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

### 6. Access Applications
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Grafana: http://localhost:3001 (admin/admin)
- Prometheus: http://localhost:9090

## Production Deployment

### 1. Container Images

Build and push container images:

```bash
# Build all services
./scripts/build-images.sh

# Push to registry
./scripts/push-images.sh
```

### 2. Kubernetes Deployment

#### Create Namespace and Secrets
```bash
kubectl apply -f infrastructure/kubernetes/namespace.yaml
kubectl apply -f infrastructure/kubernetes/secrets.yaml
kubectl apply -f infrastructure/kubernetes/configmaps.yaml
```

#### Deploy Infrastructure
```bash
# Deploy PostgreSQL
kubectl apply -f infrastructure/kubernetes/postgres.yaml

# Deploy Redis
kubectl apply -f infrastructure/kubernetes/redis.yaml

# Deploy Kafka
kubectl apply -f infrastructure/kubernetes/kafka.yaml
```

#### Deploy Application Services
```bash
# Deploy service discovery
kubectl apply -f infrastructure/kubernetes/eureka-server.yaml

# Deploy API Gateway
kubectl apply -f infrastructure/kubernetes/api-gateway.yaml

# Deploy microservices
kubectl apply -f infrastructure/kubernetes/services/
```

#### Deploy Frontend
```bash
kubectl apply -f infrastructure/kubernetes/frontend.yaml
```

#### Deploy Monitoring
```bash
kubectl apply -f infrastructure/kubernetes/monitoring/
```

### 3. Security Configuration

#### Apply Network Policies
```bash
kubectl apply -f infrastructure/security/network-policies.yaml
```

#### Apply Pod Security Policies
```bash
kubectl apply -f infrastructure/security/pod-security-policies.yaml
```

### 4. Ingress Configuration

```bash
kubectl apply -f infrastructure/kubernetes/ingress.yaml
```

## Environment Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_USER` | Database user | erp_user |
| `DB_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | localhost |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | localhost:9092 |
| `JWT_SECRET` | JWT signing secret | - |
| `EUREKA_URL` | Eureka server URL | http://localhost:8761/eureka |

### Database Configuration

Each service uses its own database schema:
- `erp_users` - User service
- `erp_finance` - Finance service
- `erp_inventory` - Inventory service
- `erp_hr` - HR service
- `erp_sales` - Sales service
- `erp_purchase` - Purchase service
- `erp_production` - Production service

## Monitoring and Observability

### Metrics
- **Prometheus**: Collects metrics from all services
- **Grafana**: Provides dashboards and visualization
- **Custom Metrics**: Business-specific metrics for each domain

### Logging
- **Structured Logging**: JSON format with correlation IDs
- **Centralized**: ELK stack or cloud logging solutions
- **Log Levels**: DEBUG, INFO, WARN, ERROR

### Tracing
- **Distributed Tracing**: Jaeger or Zipkin integration
- **Request Correlation**: Trace requests across services
- **Performance Monitoring**: Identify bottlenecks

### Health Checks
- **Actuator Endpoints**: `/actuator/health` for all services
- **Kubernetes Probes**: Readiness and liveness probes
- **Dependency Checks**: Database, Redis, Kafka connectivity

## Security Best Practices

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication
- **Role-Based Access Control**: Granular permissions
- **OAuth 2.0**: Integration with external identity providers

### Network Security
- **Network Policies**: Kubernetes network segmentation
- **TLS Encryption**: End-to-end encryption
- **Service Mesh**: Consider Istio for advanced security

### Container Security
- **Non-Root Users**: All containers run as non-root
- **Read-Only Filesystems**: Where possible
- **Security Scanning**: Vulnerability assessments
- **Pod Security Standards**: Restricted security context

### Data Security
- **Encryption at Rest**: Database encryption
- **Secrets Management**: Kubernetes secrets or external vaults
- **Data Classification**: Sensitive data handling
- **Audit Logging**: Security event logging

## Scaling and Performance

### Horizontal Scaling
```bash
# Scale specific service
kubectl scale deployment hr-service --replicas=3

# Auto-scaling
kubectl apply -f infrastructure/kubernetes/hpa.yaml
```

### Performance Tuning
- **JVM Options**: Container-optimized settings
- **Connection Pooling**: Database and Redis connections
- **Caching Strategy**: Redis and application-level caching
- **Async Processing**: Event-driven architecture

### Load Testing
```bash
# Run performance tests
k6 run tests/performance/load-test.js
```

## Backup and Recovery

### Database Backups
```bash
# Automated backup script
./scripts/backup-database.sh
```

### Configuration Backups
```bash
# Backup Kubernetes configurations
kubectl get all,configmap,secret -n erp-system -o yaml > backup.yaml
```

### Disaster Recovery
- **Multi-Region Deployment**: For high availability
- **Backup Strategies**: Automated and tested backups
- **Recovery Procedures**: Documented and practiced

## Troubleshooting

### Common Issues

1. **Service Discovery Problems**
   ```bash
   kubectl logs deployment/eureka-server -n erp-system
   ```

2. **Database Connection Issues**
   ```bash
   kubectl exec -it postgres-pod -n erp-system -- psql -U erp_user -d erp_main
   ```

3. **Memory Issues**
   ```bash
   kubectl top pods -n erp-system
   ```

### Debug Commands
```bash
# Check service status
kubectl get pods -n erp-system

# View logs
kubectl logs -f deployment/hr-service -n erp-system

# Port forward for debugging
kubectl port-forward svc/hr-service 8086:8086 -n erp-system

# Execute into container
kubectl exec -it deployment/hr-service -n erp-system -- /bin/sh
```

## CI/CD Pipeline

The project includes a complete CI/CD pipeline with:
- **Automated Testing**: Unit, integration, and e2e tests
- **Security Scanning**: Vulnerability and dependency checks
- **Container Building**: Multi-stage Docker builds
- **Deployment**: Automated deployment to staging/production
- **Rollback**: Automated rollback on failures

See `.github/workflows/ci-cd.yml` for complete pipeline configuration.

## Support and Maintenance

### Regular Tasks
- **Security Updates**: Monthly dependency updates
- **Performance Review**: Quarterly performance analysis
- **Backup Verification**: Monthly backup testing
- **Capacity Planning**: Quarterly scaling review

### Monitoring Alerts
- **Service Health**: Critical service failures
- **Performance**: Response time degradation
- **Security**: Failed authentication attempts
- **Business**: Domain-specific alerts

For additional support, refer to the runbooks in the `docs/runbooks/` directory.
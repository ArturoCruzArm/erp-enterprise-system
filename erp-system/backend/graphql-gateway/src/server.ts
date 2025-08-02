import { ApolloServer } from '@apollo/server';
import { expressMiddleware } from '@apollo/server/express4';
import { ApolloGateway, IntrospectAndCompose, RemoteGraphQLDataSource } from '@apollo/gateway';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import jwt from 'jsonwebtoken';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { GraphQLError } from 'graphql';
import depthLimit from 'graphql-depth-limit';
import { createComplexityLimitRule } from 'graphql-query-complexity';
import { shield, rule, and, or, not } from 'graphql-shield';
import { RateLimiterRedis } from 'rate-limiter-flexible';
import Redis from 'ioredis';
import winston from 'winston';
import { register, collectDefaultMetrics, Counter, Histogram } from 'prom-client';

// Initialize metrics collection
collectDefaultMetrics();

const graphqlRequestsTotal = new Counter({
  name: 'graphql_requests_total',
  help: 'Total number of GraphQL requests',
  labelNames: ['operation_name', 'operation_type', 'status']
});

const graphqlRequestDuration = new Histogram({
  name: 'graphql_request_duration_seconds',
  help: 'Duration of GraphQL requests in seconds',
  labelNames: ['operation_name', 'operation_type']
});

// Logger configuration
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'graphql-gateway' },
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
});

// Redis client for rate limiting and caching
const redis = new Redis({
  host: process.env.REDIS_HOST || 'localhost',
  port: parseInt(process.env.REDIS_PORT || '6379'),
  retryDelayOnFailover: 100,
  enableReadyCheck: false,
  maxRetriesPerRequest: null
});

// Rate limiter configuration
const rateLimiter = new RateLimiterRedis({
  storeClient: redis,
  keyPrefix: 'graphql_rate_limit',
  points: 100, // Number of requests
  duration: 60, // Per 60 seconds
});

// Authentication middleware
const isAuthenticated = rule({ cache: 'contextual' })(
  async (parent, args, context) => {
    const token = context.req.headers.authorization?.replace('Bearer ', '');
    if (!token) {
      return new GraphQLError('Authentication required', {
        extensions: { code: 'UNAUTHENTICATED' }
      });
    }

    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'default-secret');
      context.user = decoded;
      return true;
    } catch (error) {
      return new GraphQLError('Invalid token', {
        extensions: { code: 'UNAUTHENTICATED' }
      });
    }
  }
);

// Authorization rules
const hasRole = (role: string) => rule({ cache: 'contextual' })(
  async (parent, args, context) => {
    return context.user?.roles?.includes(role) || false;
  }
);

// GraphQL Shield permissions
const permissions = shield({
  Query: {
    sensitiveData: and(isAuthenticated, hasRole('ADMIN')),
    financialData: and(isAuthenticated, or(hasRole('FINANCE_MANAGER'), hasRole('ADMIN'))),
    hrData: and(isAuthenticated, or(hasRole('HR_MANAGER'), hasRole('ADMIN')))
  },
  Mutation: {
    createUser: and(isAuthenticated, hasRole('ADMIN')),
    updateFinancialRecord: and(isAuthenticated, hasRole('FINANCE_MANAGER')),
    processPayroll: and(isAuthenticated, hasRole('HR_MANAGER'))
  }
});

// Custom data source with authentication
class AuthenticatedDataSource extends RemoteGraphQLDataSource {
  willSendRequest({ request, context }: any) {
    // Forward authentication headers
    if (context.req.headers.authorization) {
      request.http.headers.set('authorization', context.req.headers.authorization);
    }
    
    // Add correlation ID for tracing
    const correlationId = context.req.headers['x-correlation-id'] || 
                         require('crypto').randomUUID();
    request.http.headers.set('x-correlation-id', correlationId);
    
    // Add user context
    if (context.user) {
      request.http.headers.set('x-user-id', context.user.sub);
      request.http.headers.set('x-user-roles', JSON.stringify(context.user.roles || []));
    }
  }

  didReceiveResponse({ response, request, context }: any) {
    // Log response for monitoring
    logger.info('GraphQL subgraph response', {
      url: request.url,
      status: response.status,
      correlationId: context.correlationId
    });
    
    return response;
  }

  didEncounterError(error: any, request: any) {
    logger.error('GraphQL subgraph error', {
      error: error.message,
      url: request.url,
      stack: error.stack
    });
  }
}

// Gateway configuration
const gateway = new ApolloGateway({
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      {
        name: 'users',
        url: process.env.USER_SERVICE_URL || 'http://user-service:8081/graphql'
      },
      {
        name: 'finance',
        url: process.env.FINANCE_SERVICE_URL || 'http://finance-service:8082/graphql'
      },
      {
        name: 'inventory',
        url: process.env.INVENTORY_SERVICE_URL || 'http://inventory-service:8083/graphql'
      },
      {
        name: 'purchase',
        url: process.env.PURCHASE_SERVICE_URL || 'http://purchase-service:8084/graphql'
      },
      {
        name: 'sales',
        url: process.env.SALES_SERVICE_URL || 'http://sales-service:8085/graphql'
      },
      {
        name: 'hr',
        url: process.env.HR_SERVICE_URL || 'http://hr-service:8086/graphql'
      },
      {
        name: 'production',
        url: process.env.PRODUCTION_SERVICE_URL || 'http://production-service:8087/graphql'
      },
      {
        name: 'analytics',
        url: process.env.ANALYTICS_SERVICE_URL || 'http://analytics-service:8088/graphql'
      },
      {
        name: 'workflow',
        url: process.env.WORKFLOW_SERVICE_URL || 'http://workflow-service:8089/graphql'
      },
      {
        name: 'bigdata',
        url: process.env.BIGDATA_SERVICE_URL || 'http://bigdata-service:8090/graphql'
      }
    ]
  }),
  buildService({ url }) {
    return new AuthenticatedDataSource({ url });
  },
  debug: process.env.NODE_ENV !== 'production'
});

// Apollo Server configuration
const server = new ApolloServer({
  gateway,
  introspection: process.env.NODE_ENV !== 'production',
  plugins: [
    // Response caching
    {
      requestDidStart() {
        return {
          didResolveOperation(requestContext) {
            const { request, operationName } = requestContext;
            const operationType = request.query?.match(/^\s*(query|mutation|subscription)/)?.[1];
            
            // Start timing
            const startTime = Date.now();
            requestContext.request.http?.body?.correlationId && 
              (requestContext.correlationId = requestContext.request.http.body.correlationId);
            
            return {
              didEncounterErrors(requestContext) {
                const { errors } = requestContext;
                errors?.forEach(error => {
                  logger.error('GraphQL execution error', {
                    error: error.message,
                    operationName,
                    correlationId: requestContext.correlationId
                  });
                });
                
                graphqlRequestsTotal
                  .labels(operationName || 'unknown', operationType || 'unknown', 'error')
                  .inc();
              },
              
              willSendResponse(requestContext) {
                const duration = (Date.now() - startTime) / 1000;
                
                graphqlRequestDuration
                  .labels(operationName || 'unknown', operationType || 'unknown')
                  .observe(duration);
                
                graphqlRequestsTotal
                  .labels(operationName || 'unknown', operationType || 'unknown', 'success')
                  .inc();
                
                logger.info('GraphQL request completed', {
                  operationName,
                  operationType,
                  duration,
                  correlationId: requestContext.correlationId
                });
              }
            };
          }
        };
      }
    }
  ],
  validationRules: [
    depthLimit(10), // Limit query depth
    createComplexityLimitRule(1000) // Limit query complexity
  ],
  formatError: (error) => {
    logger.error('GraphQL error', {
      message: error.message,
      code: error.extensions?.code,
      path: error.path,
      stack: error.stack
    });
    
    // Don't expose internal errors in production
    if (process.env.NODE_ENV === 'production' && 
        error.extensions?.code === 'INTERNAL_SERVER_ERROR') {
      return new GraphQLError('Internal server error');
    }
    
    return error;
  }
});

async function startServer() {
  const app = express();
  
  // Security middleware
  app.use(helmet({
    contentSecurityPolicy: process.env.NODE_ENV === 'production' ? undefined : false,
    crossOriginEmbedderPolicy: false
  }));
  
  app.use(compression());
  app.use(cors({
    origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
    credentials: true
  }));
  
  // Rate limiting middleware
  app.use('/graphql', async (req, res, next) => {
    try {
      const key = req.ip + ':' + (req.headers.authorization || 'anonymous');
      await rateLimiter.consume(key);
      next();
    } catch (rejRes) {
      res.status(429).json({
        error: 'Too many requests',
        retryAfter: Math.round(rejRes.msBeforeNext / 1000)
      });
    }
  });
  
  // Health check endpoint
  app.get('/health', (req, res) => {
    res.json({
      status: 'healthy',
      timestamp: new Date().toISOString(),
      uptime: process.uptime()
    });
  });
  
  // Metrics endpoint for Prometheus
  app.get('/metrics', async (req, res) => {
    res.set('Content-Type', register.contentType);
    res.end(await register.metrics());
  });
  
  // GraphQL Playground (development only)
  if (process.env.NODE_ENV !== 'production') {
    app.get('/playground', (req, res) => {
      res.send(`
        <!DOCTYPE html>
        <html>
        <head>
          <title>GraphQL Playground</title>
          <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/graphql-playground-react/build/static/css/index.css" />
          <link rel="shortcut icon" href="//cdn.jsdelivr.net/npm/graphql-playground-react/build/favicon.png" />
          <script src="//cdn.jsdelivr.net/npm/graphql-playground-react/build/static/js/middleware.js"></script>
        </head>
        <body>
          <div id="root">
            <style>
              body { margin: 0; font-family: Open Sans, sans-serif; overflow: hidden; }
              #root { height: 100vh; }
            </style>
          </div>
          <script>
            window.addEventListener('load', function (event) {
              GraphQLPlayground.init(document.getElementById('root'), {
                endpoint: '/graphql',
                settings: {
                  'general.betaUpdates': false,
                  'editor.theme': 'dark',
                  'editor.reuseHeaders': true,
                  'tracing.hideTracingResponse': true,
                  'editor.fontSize': 14,
                },
              })
            })
          </script>
        </body>
        </html>
      `);
    });
  }
  
  await server.start();
  
  // Apply GraphQL middleware
  app.use('/graphql', expressMiddleware(server, {
    context: async ({ req, res }) => {
      const correlationId = req.headers['x-correlation-id'] || 
                           require('crypto').randomUUID();
      
      return {
        req,
        res,
        correlationId,
        dataSources: {
          // Add any additional data sources here
        }
      };
    }
  }));
  
  const port = process.env.PORT || 4000;
  
  app.listen(port, () => {
    logger.info(`🚀 GraphQL Gateway ready at http://localhost:${port}/graphql`);
    logger.info(`📊 Metrics available at http://localhost:${port}/metrics`);
    if (process.env.NODE_ENV !== 'production') {
      logger.info(`🎮 Playground available at http://localhost:${port}/playground`);
    }
  });
  
  // Graceful shutdown
  process.on('SIGTERM', async () => {
    logger.info('SIGTERM received, shutting down gracefully');
    await server.stop();
    redis.disconnect();
    process.exit(0);
  });
}

startServer().catch(error => {
  logger.error('Failed to start server', error);
  process.exit(1);
});
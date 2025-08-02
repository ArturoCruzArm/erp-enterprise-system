-- ERP System Database Initialization Script

-- Create databases for each microservice
CREATE DATABASE erp_main;
CREATE DATABASE erp_users;
CREATE DATABASE erp_finance;
CREATE DATABASE erp_inventory;
CREATE DATABASE erp_hr;
CREATE DATABASE erp_sales;
CREATE DATABASE erp_purchase;
CREATE DATABASE erp_production;

-- Grant privileges to erp_user (shared user for simplicity)
GRANT ALL PRIVILEGES ON DATABASE erp_main TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_users TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_finance TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_inventory TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_hr TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_sales TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_purchase TO erp_user;
GRANT ALL PRIVILEGES ON DATABASE erp_production TO erp_user;
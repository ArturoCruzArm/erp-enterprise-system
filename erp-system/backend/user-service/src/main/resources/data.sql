-- Insertar permisos básicos
INSERT INTO permissions (name, description, module, action, active, created_at) VALUES
('USER_READ', 'Read user information', 'USER', 'READ', true, NOW()),
('USER_CREATE', 'Create new users', 'USER', 'CREATE', true, NOW()),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE', true, NOW()),
('USER_DELETE', 'Delete users', 'USER', 'DELETE', true, NOW()),
('FINANCE_READ', 'Read financial data', 'FINANCE', 'READ', true, NOW()),
('FINANCE_CREATE', 'Create financial records', 'FINANCE', 'CREATE', true, NOW()),
('FINANCE_UPDATE', 'Update financial records', 'FINANCE', 'UPDATE', true, NOW()),
('INVENTORY_READ', 'Read inventory data', 'INVENTORY', 'READ', true, NOW()),
('INVENTORY_CREATE', 'Create inventory items', 'INVENTORY', 'CREATE', true, NOW()),
('INVENTORY_UPDATE', 'Update inventory items', 'INVENTORY', 'UPDATE', true, NOW())
ON CONFLICT (name) DO NOTHING;

-- Insertar roles básicos
INSERT INTO roles (name, description, active, created_at) VALUES
('ADMIN', 'System Administrator', true, NOW()),
('FINANCE_MANAGER', 'Finance Manager', true, NOW()),
('INVENTORY_MANAGER', 'Inventory Manager', true, NOW()),
('USER', 'Regular User', true, NOW())
ON CONFLICT (name) DO NOTHING;

-- Asignar permisos a roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'FINANCE_MANAGER' AND p.module = 'FINANCE'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'INVENTORY_MANAGER' AND p.module = 'INVENTORY'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'USER' AND p.action = 'READ'
ON CONFLICT DO NOTHING;

-- Crear usuario administrador por defecto
-- Password: admin123
INSERT INTO users (username, email, password, first_name, last_name, email_verified, active, created_at) VALUES
('admin', 'admin@erp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9ignJIjku.Rz3zG', 'Admin', 'User', true, true, NOW())
ON CONFLICT (username) DO NOTHING;

-- Asignar rol de admin al usuario admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
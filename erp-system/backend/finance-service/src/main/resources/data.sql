-- Plan de cuentas básico
INSERT INTO chart_of_accounts (account_code, account_name, description, account_type, current_balance, is_header, level, active, created_at) VALUES
-- ACTIVOS
('1000', 'ACTIVOS', 'Activos totales', 'ASSET', 0, true, 1, true, NOW()),
('1100', 'ACTIVO CORRIENTE', 'Activos corrientes', 'ASSET', 0, true, 2, true, NOW()),
('1101', 'Efectivo y Equivalentes', 'Caja y bancos', 'ASSET', 0, false, 3, true, NOW()),
('1102', 'Cuentas por Cobrar', 'Cuentas por cobrar clientes', 'ASSET', 0, false, 3, true, NOW()),
('1103', 'Inventarios', 'Inventario de mercancías', 'ASSET', 0, false, 3, true, NOW()),
('1200', 'ACTIVO NO CORRIENTE', 'Activos no corrientes', 'ASSET', 0, true, 2, true, NOW()),
('1201', 'Propiedades y Equipos', 'Propiedades, planta y equipo', 'ASSET', 0, false, 3, true, NOW()),

-- PASIVOS
('2000', 'PASIVOS', 'Pasivos totales', 'LIABILITY', 0, true, 1, true, NOW()),
('2100', 'PASIVO CORRIENTE', 'Pasivos corrientes', 'LIABILITY', 0, true, 2, true, NOW()),
('2101', 'Cuentas por Pagar', 'Cuentas por pagar proveedores', 'LIABILITY', 0, false, 3, true, NOW()),
('2102', 'Impuestos por Pagar', 'Impuestos por pagar', 'LIABILITY', 0, false, 3, true, NOW()),

-- PATRIMONIO
('3000', 'PATRIMONIO', 'Patrimonio total', 'EQUITY', 0, true, 1, true, NOW()),
('3101', 'Capital Social', 'Capital social', 'EQUITY', 0, false, 2, true, NOW()),
('3102', 'Utilidades Retenidas', 'Utilidades retenidas', 'EQUITY', 0, false, 2, true, NOW()),

-- INGRESOS
('4000', 'INGRESOS', 'Ingresos totales', 'REVENUE', 0, true, 1, true, NOW()),
('4101', 'Ventas', 'Ingresos por ventas', 'REVENUE', 0, false, 2, true, NOW()),
('4102', 'Otros Ingresos', 'Otros ingresos operacionales', 'REVENUE', 0, false, 2, true, NOW()),

-- GASTOS
('5000', 'GASTOS', 'Gastos totales', 'EXPENSE', 0, true, 1, true, NOW()),
('5101', 'Costo de Ventas', 'Costo de mercancías vendidas', 'COST_OF_SALES', 0, false, 2, true, NOW()),
('5201', 'Gastos Administrativos', 'Gastos administrativos', 'EXPENSE', 0, false, 2, true, NOW()),
('5202', 'Gastos de Ventas', 'Gastos de ventas y marketing', 'EXPENSE', 0, false, 2, true, NOW())
ON CONFLICT (account_code) DO NOTHING;

-- Actualizar parent_account_id
UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '1000') WHERE account_code = '1100';
UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '1000') WHERE account_code = '1200';
UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '1100') WHERE account_code IN ('1101', '1102', '1103');
UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '1200') WHERE account_code = '1201';

UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '2000') WHERE account_code = '2100';
UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '2100') WHERE account_code IN ('2101', '2102');

UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '3000') WHERE account_code IN ('3101', '3102');

UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '4000') WHERE account_code IN ('4101', '4102');

UPDATE chart_of_accounts SET parent_account_id = (SELECT id FROM chart_of_accounts WHERE account_code = '5000') WHERE account_code IN ('5101', '5201', '5202');

-- Cuenta bancaria por defecto
INSERT INTO bank_accounts (account_name, account_number, bank_name, routing_number, current_balance, currency_code, is_default, active, created_at) VALUES
('Cuenta Principal', '1234567890', 'Banco Nacional', '987654321', 50000.00, 'USD', true, true, NOW())
ON CONFLICT (account_number) DO NOTHING;
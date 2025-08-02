-- Initialize HR Service Data

-- Insert sample departments
INSERT INTO departments (id, code, name, description, location, is_active, created_at, updated_at) VALUES
(1, 'IT', 'Information Technology', 'Technology and software development', 'Floor 3', true, NOW(), NOW()),
(2, 'HR', 'Human Resources', 'Employee management and development', 'Floor 1', true, NOW(), NOW()),
(3, 'FIN', 'Finance', 'Financial operations and accounting', 'Floor 2', true, NOW(), NOW()),
(4, 'SAL', 'Sales', 'Sales and customer relations', 'Floor 1', true, NOW(), NOW()),
(5, 'MKT', 'Marketing', 'Marketing and brand management', 'Floor 2', true, NOW(), NOW()),
(6, 'OPS', 'Operations', 'Day-to-day operations and logistics', 'Floor 4', true, NOW(), NOW());

-- Insert sample positions
INSERT INTO positions (id, code, title, description, min_salary, max_salary, level, department_id, is_active, created_at, updated_at) VALUES
(1, 'IT001', 'Software Developer', 'Develops and maintains software applications', 50000, 80000, 2, 1, true, NOW(), NOW()),
(2, 'IT002', 'Senior Software Developer', 'Senior level software development and mentoring', 70000, 100000, 3, 1, true, NOW(), NOW()),
(3, 'IT003', 'IT Manager', 'Manages IT department operations', 90000, 120000, 4, 1, true, NOW(), NOW()),
(4, 'HR001', 'HR Specialist', 'Handles employee relations and HR processes', 40000, 60000, 2, 2, true, NOW(), NOW()),
(5, 'HR002', 'HR Manager', 'Manages HR department operations', 70000, 90000, 4, 2, true, NOW(), NOW()),
(6, 'FIN001', 'Accountant', 'Manages financial records and transactions', 45000, 65000, 2, 3, true, NOW(), NOW()),
(7, 'SAL001', 'Sales Representative', 'Manages customer accounts and sales', 35000, 55000, 2, 4, true, NOW(), NOW()),
(8, 'MKT001', 'Marketing Specialist', 'Develops and executes marketing campaigns', 45000, 65000, 2, 5, true, NOW(), NOW());

-- Insert sample employees
INSERT INTO employees (id, employee_code, first_name, last_name, email, phone, hire_date, status, salary, department_id, position_id, created_at, updated_at) VALUES
(1, 'EMP001', 'John', 'Doe', 'john.doe@company.com', '+1234567890', '2023-01-15', 'ACTIVE', 75000, 1, 2, NOW(), NOW()),
(2, 'EMP002', 'Jane', 'Smith', 'jane.smith@company.com', '+1234567891', '2023-02-01', 'ACTIVE', 95000, 1, 3, NOW(), NOW()),
(3, 'EMP003', 'Mike', 'Johnson', 'mike.johnson@company.com', '+1234567892', '2023-01-20', 'ACTIVE', 55000, 2, 4, NOW(), NOW()),
(4, 'EMP004', 'Sarah', 'Williams', 'sarah.williams@company.com', '+1234567893', '2023-03-01', 'ACTIVE', 80000, 2, 5, NOW(), NOW()),
(5, 'EMP005', 'David', 'Brown', 'david.brown@company.com', '+1234567894', '2023-02-15', 'ACTIVE', 60000, 3, 6, NOW(), NOW());

-- Update manager relationships
UPDATE employees SET manager_id = 2 WHERE id = 1;
UPDATE employees SET manager_id = 4 WHERE id = 3;

-- Set sequence values
SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));
SELECT setval('positions_id_seq', (SELECT MAX(id) FROM positions));
SELECT setval('employees_id_seq', (SELECT MAX(id) FROM employees));
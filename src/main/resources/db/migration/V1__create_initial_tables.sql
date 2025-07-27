-- Tabela para os departamentos
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    cost_center_code VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela para os cargos
CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    position_level VARCHAR(255) NOT NULL
);

-- Tabela para os funcionários
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id UUID,
    position_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id)
);
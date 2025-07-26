-- Tabela para os departamentos
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    cost_center_code VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela para os níveis de cargo (Lookup Table)
CREATE TABLE IF NOT EXISTS position_levels (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, 
    description VARCHAR(255)
);

-- Insere os valores iniciais para os níveis de cargo
INSERT INTO position_levels (id, name) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Junior'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Pleno'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Sênior');

-- Tabela para os cargos
CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    position_level_id UUID NOT NULL,
    FOREIGN KEY (position_level_id) REFERENCES position_levels(id)
);

-- Tabela para os funcionários
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id UUID,
    position_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (position_id) REFERENCES positions(id)
);
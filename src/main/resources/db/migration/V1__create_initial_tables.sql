-- Tabela para os departamentos
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    cost_center_code VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela para os cargos
CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL, 
    position_level VARCHAR(50) NOT NULL,
    CONSTRAINT uk_positions_title_level UNIQUE (title, position_level) 
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

-- Tabela para armazenar mensagens do Outbox Relay
CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id UUID,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    retry_attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_status_occurred_on ON outbox_messages (status, occurred_on);
CREATE INDEX idx_outbox_next_attempt ON outbox_messages (next_attempt_at);

-- Tabela para os eventos de funcionários
CREATE TABLE employee_events_history (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(1000) NOT NULL,
    event_data TEXT NOT NULL
);

CREATE INDEX idx_employee_id ON employee_events_history (employee_id);
CREATE INDEX idx_event_type ON employee_events_history (event_type);
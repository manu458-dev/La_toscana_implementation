-- Creación de tablas para el módulo Orders
CREATE TABLE IF NOT EXISTS orders.orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders.order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders.orders(id),
    FOREIGN KEY (product_id) REFERENCES catalog.products(id)
);

-- Creación de tablas para el módulo Financial
CREATE TABLE IF NOT EXISTS financial.tickets (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders.orders(id)
);

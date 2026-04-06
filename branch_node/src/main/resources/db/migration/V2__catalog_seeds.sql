-- Creación de tablas para el módulo Catalog
CREATE TABLE IF NOT EXISTS catalog.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS catalog.products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES catalog.categories(id)
);

-- Semilla de datos para el prototipo
INSERT INTO catalog.categories (id, name, description) VALUES
(1, 'Pizzas', 'Pizzas artesanales al horno de leña'),
(2, 'Pastas', 'Pastas frescas preparadas al momento'),
(3, 'Bebidas', 'Bebidas frías y vinos');

-- Ajuste de secuencia (por inserción forzada de ID)
SELECT setval('catalog.categories_id_seq', (SELECT MAX(id) FROM catalog.categories));

INSERT INTO catalog.products (id, category_id, name, description, price, available) VALUES
(1, 1, 'Pizza Margherita', 'Salsa de tomate, mozzarella fresca y albahaca', 180.00, true),
(2, 1, 'Pizza Pepperoni', 'Salsa de tomate, mozzarella y pepperoni', 210.00, true),
(3, 2, 'Spaghetti Bolognese', 'Pasta con salsa de carne tradicional', 160.00, true),
(4, 2, 'Fettuccine Alfredo', 'Pasta con salsa cremosa de queso', 170.00, true),
(5, 3, 'Agua Mineral', 'Botella de agua mineral 355ml', 40.00, true),
(6, 3, 'Cerveza Artesanal', 'Cerveza local 355ml', 80.00, true);

-- Ajuste de secuencia (por inserción forzada de ID)
SELECT setval('catalog.products_id_seq', (SELECT MAX(id) FROM catalog.products));

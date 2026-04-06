CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE IF NOT EXISTS inventory.ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit_of_measure VARCHAR(10) NOT NULL, -- 'g', 'L', 'unit'
    current_stock DECIMAL(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS inventory.recipes (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL, -- references catalog.products, but since we have modularity we can keep it decoupled or use schema ref.
    ingredient_id BIGINT NOT NULL,
    quantity_required DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES catalog.products(id),
    FOREIGN KEY (ingredient_id) REFERENCES inventory.ingredients(id)
);

CREATE TABLE IF NOT EXISTS inventory.inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    ingredient_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- e.g., 'SALE_DEDUCTION', 'MANUAL_ADJUSTMENT'
    quantity_change DECIMAL(10, 2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES inventory.ingredients(id)
);

-- INSERCIÓN DE INGREDIENTES BASE
INSERT INTO inventory.ingredients (id, name, unit_of_measure, current_stock) VALUES
(1, 'Masa de Pizza', 'g', 10000.00),
(2, 'Salsa de Tomate', 'L', 5.00),
(3, 'Queso Mozzarella', 'g', 5000.00),
(4, 'Albahaca Fresca', 'g', 500.00),
(5, 'Pepperoni', 'g', 2000.00),
(6, 'Pasta Spaghetti', 'g', 5000.00),
(7, 'Salsa Bolognesa', 'L', 4.00),
(8, 'Pasta Fettuccine', 'g', 5000.00),
(9, 'Salsa Alfredo', 'L', 4.00),
(10, 'Agua Mineral 355ml', 'unit', 100.00),
(11, 'Cerveza Artesanal 355ml', 'unit', 100.00);

SELECT setval('inventory.ingredients_id_seq', (SELECT MAX(id) FROM inventory.ingredients));

-- INSERCIÓN DE RECETAS EN BASE A LOS PRODUCTOS (1 al 6)
-- Pizza Margherita (ID 1): 250g Masa, 0.1L Salsa, 150g Queso, 10g Albahaca
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(1, 1, 250.00),
(1, 2, 0.10),
(1, 3, 150.00),
(1, 4, 10.00);

-- Pizza Pepperoni (ID 2): 250g Masa, 0.1L Salsa, 200g Queso, 50g Pepperoni
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(2, 1, 250.00),
(2, 2, 0.10),
(2, 3, 200.00),
(2, 5, 50.00);

-- Spaghetti Bolognese (ID 3): 200g Spaghetti, 0.2L Bolognesa
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(3, 6, 200.00),
(3, 7, 0.20);

-- Fettuccine Alfredo (ID 4): 200g Fettuccine, 0.2L Alfredo, 20g Queso
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(4, 8, 200.00),
(4, 9, 0.20),
(4, 3, 20.00);

-- Agua Mineral (ID 5): 1 unit Agua
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(5, 10, 1.00);

-- Cerveza (ID 6): 1 unit Cerveza
INSERT INTO inventory.recipes (product_id, ingredient_id, quantity_required) VALUES
(6, 11, 1.00);

SELECT setval('inventory.recipes_id_seq', (SELECT MAX(id) FROM inventory.recipes));

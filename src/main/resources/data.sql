-- Datos de ejemplo para probar los endpoints inmediatamente despues de levantar el entorno.

INSERT INTO products (name, description, category, price, available, created_at, updated_at) VALUES
('Margherita Pizza', 'Tomato sauce, mozzarella and fresh basil', 'PIZZA', 8.99, true, now(), now()),
('Pepperoni Pizza', 'Tomato sauce, mozzarella and pepperoni', 'PIZZA', 10.99, true, now(), now()),
('Hawaiian Pizza', 'Tomato sauce, mozzarella, ham and pineapple', 'PIZZA', 10.49, true, now(), now()),
('Four Cheese Pizza', 'Mozzarella, gorgonzola, parmesan and provolone', 'PIZZA', 11.99, true, now(), now()),
('Coca-Cola 500ml', 'Chilled soft drink', 'BEVERAGE', 2.50, true, now(), now()),
('Sparkling Water 500ml', 'Chilled sparkling water', 'BEVERAGE', 2.00, true, now(), now()),
('Tiramisu', 'Classic Italian dessert', 'DESSERT', 4.50, true, now(), now()),
('Garlic Bread', 'Oven baked garlic bread', 'INGREDIENT', 3.99, true, now(), now()),
('Pineapple Topping', 'Extra pineapple topping', 'INGREDIENT', 1.00, false, now(), now());

INSERT INTO orders (customer_name, customer_phone, customer_address, status, coupon_code, subtotal, discount_amount, total, created_at, updated_at) VALUES
('Juan Perez', '+51999111222', 'Av. Los Pinos 123, Lima', 'DELIVERED', NULL, 21.98, 0.00, 21.98, now(), now()),
('Maria Gomez', '+51999333444', 'Jr. Las Flores 456, Lima', 'IN_PREPARATION', 'PIZZA10', 25.98, 2.60, 23.38, now(), now()),
('Carlos Ruiz', '+51999555666', 'Calle Sol 789, Lima', 'CREATED', NULL, 35.97, 0.00, 35.97, now(), now());

-- Order #1 (Juan Perez): 2x Pepperoni Pizza
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 2, p.price * 2
FROM orders o, products p
WHERE o.customer_name = 'Juan Perez' AND p.name = 'Pepperoni Pizza';

-- Order #2 (Maria Gomez): 1x Four Cheese Pizza + 1x Margherita Pizza + 2x Coca-Cola
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 1, p.price * 1
FROM orders o, products p
WHERE o.customer_name = 'Maria Gomez' AND p.name = 'Four Cheese Pizza';

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 1, p.price * 1
FROM orders o, products p
WHERE o.customer_name = 'Maria Gomez' AND p.name = 'Margherita Pizza';

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 2, p.price * 2
FROM orders o, products p
WHERE o.customer_name = 'Maria Gomez' AND p.name = 'Coca-Cola 500ml';

-- Order #3 (Carlos Ruiz): 3x Hawaiian Pizza + 1x Tiramisu
INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 3, p.price * 3
FROM orders o, products p
WHERE o.customer_name = 'Carlos Ruiz' AND p.name = 'Hawaiian Pizza';

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, line_total)
SELECT o.id, p.id, p.name, p.price, 1, p.price * 1
FROM orders o, products p
WHERE o.customer_name = 'Carlos Ruiz' AND p.name = 'Tiramisu';

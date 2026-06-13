INSERT INTO Users (user_id, name, email, created_at) VALUES
    (1, 'Alice', 'alice@gmail.com', '2023-06-01'),
    (2, 'Bob', 'bob@yahoo.com', '2024-03-15'),
    (3, 'Carol', 'carol@gmail.com', '2024-06-01'),
    (4, 'Dave', 'dave@company.com', '2025-01-10'),
    (5, 'Eve', 'eve@outlook.com', '2024-05-01'),
    (6, 'Alice Dup', 'alice@gmail.com', '2024-05-01');

INSERT INTO Orders (order_id, user_id, product_name, amount, order_date) VALUES
    (1, 1, 'Laptop', 899.99, '2024-07-15'),
    (2, 1, 'Phone', 599.00, '2024-08-20'),
    (3, 1, 'Tablet', 49.99, '2024-01-05'),
    (4, 2, 'Laptop', 1200.00, '2024-09-10'),
    (5, 2, 'Phone', 150.00, '2024-11-01'),
    (6, 3, 'Phone', 75.00, '2024-06-15'),
    (7, 3, 'Tablet', 80.00, '2024-12-20'),
    (8, 2, 'Laptop', 600.00, '2025-02-14'),
    (9, 1, 'Phone', 550.00, '2025-03-01'),
    (10, 5, 'Phone', 99.00, '2024-03-01');

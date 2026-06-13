-- =============================================================================
-- BASIC QUERIES
-- =============================================================================

-- 1. SELECT all users
-- Returns every row from the Users table with all columns.
SELECT *
FROM Users;


-- 2. SELECT users created after a specific date
-- Filters users whose account was created strictly after the given date.
SELECT user_id, name, email, created_at
FROM Users
WHERE created_at > '2024-01-01';


-- 3. INNER JOIN Users and Orders
-- Returns only users who have placed at least one order,
-- with one row per user-order pair.
SELECT
    u.user_id,
    u.name,
    u.email,
    o.order_id,
    o.product_name,
    o.amount,
    o.order_date
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id;


-- 4. Total amount spent by each user
-- Groups orders by user and sums the amount column to show lifetime spend.
SELECT
    u.user_id,
    u.name,
    SUM(o.amount) AS total_spent
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
ORDER BY total_spent DESC;


-- 5. Orders above a certain amount
-- Returns individual orders whose amount exceeds the threshold.
SELECT
    o.order_id,
    o.user_id,
    u.name,
    o.product_name,
    o.amount,
    o.order_date
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
WHERE o.amount > 100.00
ORDER BY o.amount DESC;


-- =============================================================================
-- INTERMEDIATE QUERIES
-- =============================================================================

-- 6. LEFT JOIN — users with and without orders
-- Keeps all users; order columns are NULL when a user has never ordered.
SELECT
    u.user_id,
    u.name,
    o.order_id,
    o.product_name,
    o.amount
FROM Users u
LEFT JOIN Orders o ON u.user_id = o.user_id
ORDER BY u.user_id, o.order_id;


-- 7. Users who have never placed an order
-- Anti-join pattern: find users with no matching row in Orders.
SELECT u.user_id, u.name, u.email, u.created_at
FROM Users u
LEFT JOIN Orders o ON u.user_id = o.user_id
WHERE o.order_id IS NULL;


-- 8. Order count and average order value per user
-- Multiple aggregate functions in a single GROUP BY.
SELECT
    u.user_id,
    u.name,
    COUNT(o.order_id) AS order_count,
    ROUND(AVG(o.amount), 2) AS avg_order_amount,
    SUM(o.amount) AS total_spent
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
HAVING COUNT(o.order_id) >= 2
ORDER BY order_count DESC, total_spent DESC;


-- 9. Most popular products by order count and revenue
-- Ranks products across all users using GROUP BY on product_name.
SELECT
    product_name,
    COUNT(*) AS times_ordered,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_price
FROM Orders
GROUP BY product_name
ORDER BY total_revenue DESC, times_ordered DESC;


-- 10. Orders placed within a date range
-- Useful for monthly or quarterly reporting windows.
SELECT
    o.order_id,
    u.name,
    o.product_name,
    o.amount,
    o.order_date
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
WHERE o.order_date BETWEEN '2024-06-01' AND '2024-12-31'
ORDER BY o.order_date;


-- 11. Search users by email domain
-- Pattern matching with LIKE for partial string filters.
SELECT user_id, name, email, created_at
FROM Users
WHERE email LIKE '%@gmail.com'
ORDER BY created_at DESC;


-- 12. Distinct users who ordered a specific product
-- DISTINCT removes duplicate user rows when they bought the same item multiple times.
SELECT DISTINCT
    u.user_id,
    u.name,
    u.email
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
WHERE o.product_name = 'Laptop';


-- =============================================================================
-- SUBQUERIES & CTEs
-- =============================================================================

-- 13. Users who spent more than the overall average order amount
-- Scalar subquery in HAVING compares each user's total against the global average.
SELECT
    u.user_id,
    u.name,
    SUM(o.amount) AS total_spent
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
HAVING SUM(o.amount) > (
    SELECT AVG(amount) FROM Orders
)
ORDER BY total_spent DESC;


-- 14. Users whose total spend exceeds the average spend across all users
-- Nested aggregation: inner query averages per-user totals, outer filters users.
SELECT
    u.user_id,
    u.name,
    user_totals.total_spent
FROM (
    SELECT
        user_id,
        SUM(amount) AS total_spent
    FROM Orders
    GROUP BY user_id
) AS user_totals
INNER JOIN Users u ON u.user_id = user_totals.user_id
WHERE user_totals.total_spent > (
    SELECT AVG(inner_totals.total_spent)
    FROM (
        SELECT SUM(amount) AS total_spent
        FROM Orders
        GROUP BY user_id
    ) AS inner_totals
)
ORDER BY user_totals.total_spent DESC;


-- 15. Monthly revenue report using a CTE
-- WITH clause names an intermediate result set before the final SELECT.
WITH monthly_sales AS (
    SELECT
        DATE_TRUNC('month', order_date) AS month,
        COUNT(*) AS order_count,
        SUM(amount) AS revenue
    FROM Orders
    GROUP BY DATE_TRUNC('month', order_date)
)
SELECT
    month,
    order_count,
    revenue,
    ROUND(revenue / order_count, 2) AS avg_order_value
FROM monthly_sales
ORDER BY month;


-- 16. Each user's latest order using a correlated subquery
-- For every order row, the subquery finds the max order_date for that user.
SELECT
    o.order_id,
    u.name,
    o.product_name,
    o.amount,
    o.order_date
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
WHERE o.order_date = (
    SELECT MAX(o2.order_date)
    FROM Orders o2
    WHERE o2.user_id = o.user_id
)
ORDER BY o.order_date DESC;


-- 17. EXISTS — users with at least one high-value order
-- EXISTS stops at the first match; often more readable than a JOIN + DISTINCT.
SELECT u.user_id, u.name, u.email
FROM Users u
WHERE EXISTS (
    SELECT 1
    FROM Orders o
    WHERE o.user_id = u.user_id
      AND o.amount > 500.00
);


-- =============================================================================
-- WINDOW FUNCTIONS & ANALYTICS
-- =============================================================================

-- 18. Rank users by total spend
-- RANK assigns the same rank to ties and skips the next rank (1, 2, 2, 4).
SELECT
    u.user_id,
    u.name,
    SUM(o.amount) AS total_spent,
    RANK() OVER (ORDER BY SUM(o.amount) DESC) AS spend_rank
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
ORDER BY spend_rank;


-- 19. Running total of spend per user over time
-- SUM() OVER with ORDER BY gives a cumulative total for each user's orders.
SELECT
    u.name,
    o.order_id,
    o.product_name,
    o.amount,
    o.order_date,
    SUM(o.amount) OVER (
        PARTITION BY o.user_id
        ORDER BY o.order_date, o.order_id
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS running_total
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
ORDER BY u.user_id, o.order_date, o.order_id;


-- 20. Compare each order to the user's average order amount
-- Window AVG partitioned by user_id avoids a self-join or subquery per row.
SELECT
    u.name,
    o.order_id,
    o.product_name,
    o.amount,
    ROUND(AVG(o.amount) OVER (PARTITION BY o.user_id), 2) AS user_avg_amount,
    o.amount - AVG(o.amount) OVER (PARTITION BY o.user_id) AS diff_from_avg
FROM Orders o
INNER JOIN Users u ON o.user_id = u.user_id
ORDER BY u.user_id, o.order_date;


-- 21. Top 3 orders per user by amount
-- ROW_NUMBER ranks orders within each user; outer query keeps rank <= 3.
SELECT user_id, name, order_id, product_name, amount, order_date, rn
FROM (
    SELECT
        u.user_id,
        u.name,
        o.order_id,
        o.product_name,
        o.amount,
        o.order_date,
        ROW_NUMBER() OVER (
            PARTITION BY o.user_id
            ORDER BY o.amount DESC, o.order_date DESC
        ) AS rn
    FROM Orders o
    INNER JOIN Users u ON o.user_id = u.user_id
) AS ranked_orders
WHERE rn <= 3
ORDER BY user_id, rn;


-- =============================================================================
-- CONDITIONAL LOGIC & ADVANCED PATTERNS
-- =============================================================================

-- 22. Classify users into spending tiers with CASE
-- Buckets users by lifetime spend for segmentation or reporting.
SELECT
    u.user_id,
    u.name,
    COALESCE(SUM(o.amount), 0) AS total_spent,
    CASE
        WHEN COALESCE(SUM(o.amount), 0) = 0 THEN 'No orders'
        WHEN SUM(o.amount) < 100 THEN 'Bronze'
        WHEN SUM(o.amount) < 500 THEN 'Silver'
        ELSE 'Gold'
    END AS spending_tier
FROM Users u
LEFT JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
ORDER BY total_spent DESC;


-- 23. Pivot-style summary: order count and total per product per user
-- Conditional aggregation counts/sums rows matching each product name.
SELECT
    u.user_id,
    u.name,
    SUM(CASE WHEN o.product_name = 'Laptop' THEN 1 ELSE 0 END) AS laptop_orders,
    SUM(CASE WHEN o.product_name = 'Phone' THEN 1 ELSE 0 END) AS phone_orders,
    SUM(CASE WHEN o.product_name = 'Tablet' THEN 1 ELSE 0 END) AS tablet_orders,
    SUM(o.amount) AS total_spent
FROM Users u
LEFT JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name
ORDER BY total_spent DESC;


-- 24. Users registered before their first order (data quality check)
-- Compares account creation date to earliest order date per user.
SELECT
    u.user_id,
    u.name,
    u.created_at AS registered_on,
    MIN(o.order_date) AS first_order_date,
    MIN(o.order_date) - u.created_at AS days_until_first_order
FROM Users u
INNER JOIN Orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.name, u.created_at
HAVING MIN(o.order_date) < u.created_at;


-- 25. Full outer join simulation with UNION
-- Combines unmatched users and unmatched orders (if any orphan rows exist).
SELECT
    u.user_id,
    u.name,
    o.order_id,
    o.amount,
    'user_only' AS match_type
FROM Users u
LEFT JOIN Orders o ON u.user_id = o.user_id
WHERE o.order_id IS NULL

UNION ALL

SELECT
    u.user_id,
    u.name,
    o.order_id,
    o.amount,
    'order_only' AS match_type
FROM Orders o
LEFT JOIN Users u ON o.user_id = u.user_id
WHERE u.user_id IS NULL;


-- 26. Recursive CTE — generate a date series for gap-filling reports
-- Useful when you need every calendar day even if no orders occurred.
WITH RECURSIVE date_series AS (
    SELECT DATE '2024-01-01' AS report_date
    UNION ALL
    SELECT report_date + INTERVAL '1 day'
    FROM date_series
    WHERE report_date < DATE '2024-01-31'
)
SELECT
    d.report_date,
    COUNT(o.order_id) AS orders_on_day,
    COALESCE(SUM(o.amount), 0) AS daily_revenue
FROM date_series d
LEFT JOIN Orders o ON o.order_date = d.report_date
GROUP BY d.report_date
ORDER BY d.report_date;


-- 27. Percentage of total revenue contributed by each user
-- Window SUM without PARTITION BY gives the grand total on every row.
SELECT
    u.user_id,
    u.name,
    user_revenue.total_spent,
    ROUND(
        100.0 * user_revenue.total_spent / SUM(user_revenue.total_spent) OVER (),
        2
    ) AS pct_of_total_revenue
FROM (
    SELECT user_id, SUM(amount) AS total_spent
    FROM Orders
    GROUP BY user_id
) AS user_revenue
INNER JOIN Users u ON u.user_id = user_revenue.user_id
ORDER BY pct_of_total_revenue DESC;


-- 28. Find duplicate emails (data integrity)
-- GROUP BY + HAVING COUNT > 1 surfaces rows that violate a unique-email rule.
SELECT email, COUNT(*) AS duplicate_count
FROM Users
GROUP BY email
HAVING COUNT(*) > 1;


-- 29. Orders in the top 10% by amount
-- NTILE splits rows into buckets; bucket 10 is the highest decile.
SELECT order_id, user_id, product_name, amount, order_date, spend_decile
FROM (
    SELECT
        order_id,
        user_id,
        product_name,
        amount,
        order_date,
        NTILE(10) OVER (ORDER BY amount DESC) AS spend_decile
    FROM Orders
) AS deciled
WHERE spend_decile = 1
ORDER BY amount DESC;


-- 30. Year-over-year order growth by user
-- Self-join on the same user across two calendar years.
SELECT
    curr.user_id,
    u.name,
    COUNT(DISTINCT CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2024 THEN curr.order_id END) AS orders_2024,
    COUNT(DISTINCT CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2025 THEN curr.order_id END) AS orders_2025,
    SUM(CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2024 THEN curr.amount ELSE 0 END) AS revenue_2024,
    SUM(CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2025 THEN curr.amount ELSE 0 END) AS revenue_2025
FROM Orders curr
INNER JOIN Users u ON u.user_id = curr.user_id
WHERE EXTRACT(YEAR FROM curr.order_date) IN (2024, 2025)
GROUP BY curr.user_id, u.name
ORDER BY revenue_2025 DESC;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QueriesTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:queries" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        RunScript.execute(connection, new InputStreamReader(
                getClass().getResourceAsStream("/schema.sql"), StandardCharsets.UTF_8));
        RunScript.execute(connection, new InputStreamReader(
                getClass().getResourceAsStream("/sql/test-data.sql"), StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private int countRows(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            int count = 0;
            while (resultSet.next()) {
                count++;
            }
            return count;
        }
    }

    private BigDecimal sumColumn(String sql, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertTrue(resultSet.next(), "Expected at least one row");
            return resultSet.getBigDecimal(column);
        }
    }

    private List<String> columnValues(String sql, String column) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString(column));
            }
        }
        return values;
    }

    @Nested
    @DisplayName("Basic queries (1-5)")
    class BasicQueries {

        @Test
        void selectAllUsers() throws SQLException {
            assertEquals(6, countRows("SELECT * FROM Users"));
        }

        @Test
        void selectUsersCreatedAfterDate() throws SQLException {
            String sql = """
                    SELECT user_id, name, email, created_at
                    FROM Users
                    WHERE created_at > '2024-01-01'
                    """;
            assertEquals(5, countRows(sql));
            assertFalse(columnValues(sql, "name").contains("Alice"));
        }

        @Test
        void innerJoinUsersAndOrders() throws SQLException {
            String sql = """
                    SELECT u.user_id, u.name, o.order_id
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    """;
            assertEquals(10, countRows(sql));
        }

        @Test
        void totalAmountSpentByEachUser() throws SQLException {
            String sql = """
                    SELECT u.user_id, u.name, SUM(o.amount) AS total_spent
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    ORDER BY total_spent DESC
                    """;
            List<String> topSpenders = columnValues(sql, "name");
            assertEquals("Alice", topSpenders.get(0));
            assertEquals(4, topSpenders.size());
        }

        @Test
        void ordersAboveThreshold() throws SQLException {
            String sql = """
                    SELECT o.order_id
                    FROM Orders o
                    WHERE o.amount > 100.00
                    """;
            assertEquals(6, countRows(sql));
        }
    }

    @Nested
    @DisplayName("Intermediate queries (6-12)")
    class IntermediateQueries {

        @Test
        void leftJoinIncludesUsersWithoutOrders() throws SQLException {
            String sql = """
                    SELECT u.user_id, o.order_id
                    FROM Users u
                    LEFT JOIN Orders o ON u.user_id = o.user_id
                    """;
            assertEquals(12, countRows(sql));
        }

        @Test
        void usersWhoNeverOrdered() throws SQLException {
            String sql = """
                    SELECT u.user_id, u.name
                    FROM Users u
                    LEFT JOIN Orders o ON u.user_id = o.user_id
                    WHERE o.order_id IS NULL
                    """;
            assertEquals(2, countRows(sql));
            List<String> names = columnValues(sql, "name");
            assertTrue(names.contains("Dave"));
            assertTrue(names.contains("Alice Dup"));
        }

        @Test
        void orderCountAndAveragePerUser() throws SQLException {
            String sql = """
                    SELECT u.user_id, COUNT(o.order_id) AS order_count
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    HAVING COUNT(o.order_id) >= 2
                    """;
            assertEquals(3, countRows(sql));
        }

        @Test
        void mostPopularProducts() throws SQLException {
            String sql = """
                    SELECT product_name, COUNT(*) AS times_ordered
                    FROM Orders
                    GROUP BY product_name
                    ORDER BY times_ordered DESC
                    """;
            assertEquals("Phone", columnValues(sql, "product_name").get(0));
        }

        @Test
        void ordersWithinDateRange() throws SQLException {
            String sql = """
                    SELECT o.order_id
                    FROM Orders o
                    WHERE o.order_date BETWEEN '2024-06-01' AND '2024-12-31'
                    """;
            assertEquals(6, countRows(sql));
        }

        @Test
        void searchUsersByEmailDomain() throws SQLException {
            String sql = """
                    SELECT user_id
                    FROM Users
                    WHERE email LIKE '%@gmail.com'
                    """;
            assertEquals(3, countRows(sql));
        }

        @Test
        void distinctUsersWhoOrderedLaptop() throws SQLException {
            String sql = """
                    SELECT DISTINCT u.user_id, u.name
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    WHERE o.product_name = 'Laptop'
                    """;
            assertEquals(2, countRows(sql));
        }
    }

    @Nested
    @DisplayName("Subqueries and CTEs (13-17)")
    class SubqueryQueries {

        @Test
        void usersSpentMoreThanAverageOrderAmount() throws SQLException {
            String sql = """
                    SELECT u.user_id, SUM(o.amount) AS total_spent
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    HAVING SUM(o.amount) > (SELECT AVG(amount) FROM Orders)
                    """;
            assertTrue(countRows(sql) >= 2);
        }

        @Test
        void usersAboveAverageTotalSpend() throws SQLException {
            String sql = """
                    SELECT u.user_id, user_totals.total_spent
                    FROM (
                        SELECT user_id, SUM(amount) AS total_spent
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
                    """;
            assertEquals(2, countRows(sql));
        }

        @Test
        void monthlyRevenueReport() throws SQLException {
            String sql = """
                    WITH monthly_sales AS (
                        SELECT
                            DATE_TRUNC('month', order_date) AS sales_month,
                            COUNT(*) AS order_count,
                            SUM(amount) AS revenue
                        FROM Orders
                        GROUP BY DATE_TRUNC('month', order_date)
                    )
                    SELECT sales_month, order_count, revenue
                    FROM monthly_sales
                    ORDER BY sales_month
                    """;
            assertTrue(countRows(sql) >= 5);
        }

        @Test
        void latestOrderPerUser() throws SQLException {
            String sql = """
                    SELECT o.order_id, u.name, o.order_date
                    FROM Orders o
                    INNER JOIN Users u ON o.user_id = u.user_id
                    WHERE o.order_date = (
                        SELECT MAX(o2.order_date)
                        FROM Orders o2
                        WHERE o2.user_id = o.user_id
                    )
                    """;
            assertEquals(4, countRows(sql));
        }

        @Test
        void existsHighValueOrders() throws SQLException {
            String sql = """
                    SELECT u.user_id, u.name
                    FROM Users u
                    WHERE EXISTS (
                        SELECT 1
                        FROM Orders o
                        WHERE o.user_id = u.user_id
                          AND o.amount > 500.00
                    )
                    """;
            List<String> names = columnValues(sql, "name");
            assertTrue(names.contains("Alice"));
            assertTrue(names.contains("Bob"));
            assertEquals(2, names.size());
        }
    }

    @Nested
    @DisplayName("Window functions (18-21)")
    class WindowQueries {

        @Test
        void rankUsersBySpend() throws SQLException {
            String sql = """
                    SELECT u.name, RANK() OVER (ORDER BY SUM(o.amount) DESC) AS spend_rank
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    ORDER BY spend_rank
                    """;
            assertEquals(1, Integer.parseInt(columnValues(sql, "spend_rank").get(0)));
        }

        @Test
        void runningTotalPerUser() throws SQLException {
            String sql = """
                    SELECT running_total
                    FROM (
                        SELECT
                            o.order_id,
                            SUM(o.amount) OVER (
                                PARTITION BY o.user_id
                                ORDER BY o.order_date, o.order_id
                                ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                            ) AS running_total
                        FROM Orders o
                        WHERE o.user_id = 1
                        ORDER BY o.order_date, o.order_id
                    ) t
                    ORDER BY order_id DESC
                    LIMIT 1
                    """;
            assertEquals(0, sumColumn(sql, "running_total").compareTo(new BigDecimal("2098.98")));
        }

        @Test
        void compareOrderToUserAverage() throws SQLException {
            String sql = """
                    SELECT
                        o.order_id,
                        ROUND(AVG(o.amount) OVER (PARTITION BY o.user_id), 2) AS user_avg_amount
                    FROM Orders o
                    WHERE o.user_id = 3
                    """;
            assertEquals(2, countRows(sql));
        }

        @Test
        void topThreeOrdersPerUser() throws SQLException {
            String sql = """
                    SELECT user_id, order_id, rn
                    FROM (
                        SELECT
                            o.user_id,
                            o.order_id,
                            ROW_NUMBER() OVER (
                                PARTITION BY o.user_id
                                ORDER BY o.amount DESC, o.order_date DESC
                            ) AS rn
                        FROM Orders o
                    ) AS ranked_orders
                    WHERE rn <= 3
                    """;
            assertEquals(9, countRows(sql));
        }
    }

    @Nested
    @DisplayName("Advanced patterns (22-30)")
    class AdvancedQueries {

        @Test
        void spendingTiers() throws SQLException {
            String sql = """
                    SELECT u.name,
                        CASE
                            WHEN COALESCE(SUM(o.amount), 0) = 0 THEN 'No orders'
                            WHEN SUM(o.amount) < 100 THEN 'Bronze'
                            WHEN SUM(o.amount) < 500 THEN 'Silver'
                            ELSE 'Gold'
                        END AS spending_tier
                    FROM Users u
                    LEFT JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    """;
            assertTrue(columnValues(sql, "spending_tier").contains("No orders"));
            assertTrue(columnValues(sql, "spending_tier").contains("Gold"));
        }

        @Test
        void pivotStyleProductCounts() throws SQLException {
            String sql = """
                    SELECT
                        u.name,
                        SUM(CASE WHEN o.product_name = 'Laptop' THEN 1 ELSE 0 END) AS laptop_orders,
                        SUM(CASE WHEN o.product_name = 'Phone' THEN 1 ELSE 0 END) AS phone_orders
                    FROM Users u
                    LEFT JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name
                    HAVING SUM(CASE WHEN o.product_name = 'Laptop' THEN 1 ELSE 0 END) > 0
                    """;
            assertEquals(2, countRows(sql));
        }

        @Test
        void usersRegisteredAfterFirstOrder() throws SQLException {
            String sql = """
                    SELECT u.name
                    FROM Users u
                    INNER JOIN Orders o ON u.user_id = o.user_id
                    GROUP BY u.user_id, u.name, u.created_at
                    HAVING MIN(o.order_date) < u.created_at
                    """;
            assertEquals(1, countRows(sql));
            assertEquals("Eve", columnValues(sql, "name").get(0));
        }

        @Test
        void unionUnmatchedUsersAndOrders() throws SQLException {
            String sql = """
                    SELECT u.user_id, 'user_only' AS match_type
                    FROM Users u
                    LEFT JOIN Orders o ON u.user_id = o.user_id
                    WHERE o.order_id IS NULL
                    UNION ALL
                    SELECT o.user_id, 'order_only' AS match_type
                    FROM Orders o
                    LEFT JOIN Users u ON o.user_id = u.user_id
                    WHERE u.user_id IS NULL
                    """;
            assertEquals(2, countRows(sql));
        }

        @Test
        void recursiveDateSeries() throws SQLException {
            String sql = """
                    WITH RECURSIVE date_series(report_date) AS (
                        SELECT CAST('2024-01-01' AS DATE)
                        UNION ALL
                        SELECT DATEADD('DAY', 1, report_date)
                        FROM date_series
                        WHERE report_date < CAST('2024-01-31' AS DATE)
                    )
                    SELECT d.report_date, COUNT(o.order_id) AS orders_on_day
                    FROM date_series d
                    LEFT JOIN Orders o ON o.order_date = d.report_date
                    GROUP BY d.report_date
                    """;
            assertEquals(31, countRows(sql));
        }

        @Test
        void revenuePercentagePerUser() throws SQLException {
            String sql = """
                    SELECT
                        u.name,
                        ROUND(
                            100.0 * user_revenue.total_spent
                                / SUM(user_revenue.total_spent) OVER (),
                            2
                        ) AS pct_of_total_revenue
                    FROM (
                        SELECT user_id, SUM(amount) AS total_spent
                        FROM Orders
                        GROUP BY user_id
                    ) AS user_revenue
                    INNER JOIN Users u ON u.user_id = user_revenue.user_id
                    ORDER BY pct_of_total_revenue DESC
                    """;
            List<String> names = columnValues(sql, "name");
            assertEquals("Alice", names.get(0));
            BigDecimal topPct = sumColumn(sql, "pct_of_total_revenue");
            assertTrue(topPct.compareTo(new BigDecimal("40")) > 0);
        }

        @Test
        void duplicateEmails() throws SQLException {
            String sql = """
                    SELECT email, COUNT(*) AS duplicate_count
                    FROM Users
                    GROUP BY email
                    HAVING COUNT(*) > 1
                    """;
            assertEquals(1, countRows(sql));
            assertEquals("alice@gmail.com", columnValues(sql, "email").get(0));
        }

        @Test
        void topDecileOrdersByAmount() throws SQLException {
            String sql = """
                    SELECT order_id
                    FROM (
                        SELECT
                            order_id,
                            NTILE(10) OVER (ORDER BY amount DESC) AS spend_decile
                        FROM Orders
                    ) AS deciled
                    WHERE spend_decile = 1
                    """;
            assertTrue(countRows(sql) >= 1);
        }

        @Test
        void yearOverYearGrowth() throws SQLException {
            String sql = """
                    SELECT
                        curr.user_id,
                        COUNT(DISTINCT CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2024
                            THEN curr.order_id END) AS orders_2024,
                        COUNT(DISTINCT CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2025
                            THEN curr.order_id END) AS orders_2025
                    FROM Orders curr
                    WHERE EXTRACT(YEAR FROM curr.order_date) IN (2024, 2025)
                    GROUP BY curr.user_id
                    HAVING COUNT(DISTINCT CASE WHEN EXTRACT(YEAR FROM curr.order_date) = 2025
                            THEN curr.order_id END) > 0
                    """;
            assertEquals(2, countRows(sql));
        }
    }
}

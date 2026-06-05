import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CalculatorTest {

    @Test
    void testDivideNormal() {
        assertEquals("5", Calculator.divide(10, 2));
    }

    @Test
    void testDivideByZero() {
        assertEquals(
                "Cannot divide by zero",
                Calculator.divide(10, 0)
        );
    }
}

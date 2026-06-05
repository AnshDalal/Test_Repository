import java.util.logging.Logger;

public class Calculator {

    private static final Logger logger = Logger.getLogger(Calculator.class.getName());

    public static String divide(int a, int b) {

        logger.info("Dividing " + a + " by " + b);

        if (b == 0) {
            logger.severe("Division by zero attempted");
            return "Cannot divide by zero";
        }

        return String.valueOf(a / b);
    }
}

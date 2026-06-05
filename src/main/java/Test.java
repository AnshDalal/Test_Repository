import java.util.logging.Logger;

public class Test {

    private static final Logger logger = Logger.getLogger(Test.class.getName());

    public static String divide(int a, int b) {
        if(b == 0) {
            logger.severe("Division by zero attempted");
            return "Error: Division by zero";
        }
        logger.info("Division successful: " + a + " / " + b + " = " + (a / b));
        return String.valueOf(a / b);
    }

    public static void main(String[] args) {
        System.out.println(divide(10, 0));
        System.out.println(divide(10, 2));
    }
}

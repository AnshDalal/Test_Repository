/**
 * Utility for searching a value in a sorted array using the binary search algorithm.
 */
public class BinarySearch {

    /** Returned when the target value is not present in the array. */
    private static final int NOT_FOUND = -1;

    private BinarySearch() {
        // Utility class; prevent instantiation.
    }

    /**
     * Searches for {@code target} in a sorted array.
     *
     * @param sortedArray array sorted in ascending order
     * @param target      value to locate
     * @return the index of {@code target} if found; otherwise {@link #NOT_FOUND}
     */
    public static int binarySearch(int[] sortedArray, int target) {
        int lowIndex = 0;
        int highIndex = sortedArray.length - 1;

        while (lowIndex <= highIndex) {
            // Avoids integer overflow compared to (lowIndex + highIndex) / 2
            int midIndex = lowIndex + (highIndex - lowIndex) / 2;
            int midValue = sortedArray[midIndex];

            if (midValue == target) {
                return midIndex;
            }

            if (midValue < target) {
                lowIndex = midIndex + 1;
            } else {
                highIndex = midIndex - 1;
            }
        }

        return NOT_FOUND;
    }

    public static void main(String[] args) {
        int[] sortedNumbers = {2, 4, 6, 8, 10, 12, 14};
        int searchTarget = 10;

        System.out.println(binarySearch(sortedNumbers, searchTarget));
    }
}

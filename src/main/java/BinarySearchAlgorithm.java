/**
 * Pure binary search implementation over a validated, non-empty, sorted array.
 * <p>
 * Input validation is intentionally omitted; callers must validate before invoking
 * {@link #search(int[], int)}.
 */
final class BinarySearchAlgorithm {

    private BinarySearchAlgorithm() {
        // Package-private algorithm helper; not part of the public API.
    }

    /**
     * Locates {@code target} in a sorted array using the binary search algorithm.
     *
     * @param sortedArray a non-empty array sorted in non-decreasing order
     * @param target      the value to find
     * @return the index of {@code target}, or {@code -1} if not found
     */
    static int search(int[] sortedArray, int target) {
        int lowIndex = 0;
        int highIndex = sortedArray.length - 1;

        while (lowIndex <= highIndex) {
            int midIndex = computeMidIndex(lowIndex, highIndex);
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

        return BinarySearch.NOT_FOUND;
    }

    /**
     * Computes the midpoint without integer overflow.
     */
    private static int computeMidIndex(int lowIndex, int highIndex) {
        return lowIndex + (highIndex - lowIndex) / 2;
    }
}

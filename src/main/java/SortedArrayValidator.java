import java.util.Objects;

/**
 * Validates array preconditions required by binary search.
 * <p>
 * Binary search assumes a sorted collection; searching an unsorted array can
 * return incorrect results. This validator enforces that contract at the boundary.
 */
public final class SortedArrayValidator {

    private static final String NULL_ARRAY_MESSAGE = "Array must not be null";
    private static final String UNSORTED_ARRAY_MESSAGE =
            "Array must be sorted in non-decreasing (ascending) order";

    /**
     * Ensures the given array is non-null and sorted in non-decreasing order.
     *
     * @param array the array to validate
     * @throws NullPointerException     if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array} is not sorted in ascending order
     */
    public void requireSorted(int[] array) {
        Objects.requireNonNull(array, NULL_ARRAY_MESSAGE);
        validateNonDecreasingOrder(array);
    }

    private void validateNonDecreasingOrder(int[] array) {
        for (int index = 1; index < array.length; index++) {
            if (array[index] < array[index - 1]) {
                throw new IllegalArgumentException(UNSORTED_ARRAY_MESSAGE);
            }
        }
    }
}

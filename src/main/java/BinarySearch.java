import java.util.Objects;

/**
 * Entry point for searching values in sorted integer arrays.
 * <p>
 * Responsibilities are delegated as follows:
 * <ul>
 *   <li>{@link SortedArrayValidator} — input validation</li>
 *   <li>{@link BinarySearchAlgorithm} — search algorithm</li>
 *   <li>{@link BinarySearch} — orchestration and public API</li>
 * </ul>
 */
public final class BinarySearch {

    /** Sentinel value returned when the target is not present in the array. */
    public static final int NOT_FOUND = -1;

    private static final BinarySearch DEFAULT_INSTANCE = new BinarySearch(new SortedArrayValidator());

    private final SortedArrayValidator validator;

    /**
     * Creates a {@code BinarySearch} with the default validator.
     */
    public BinarySearch() {
        this(new SortedArrayValidator());
    }

    /**
     * Creates a {@code BinarySearch} with a custom validator.
     * <p>
     * Package-private for unit testing with test doubles.
     *
     * @param validator validates array preconditions before searching
     */
    BinarySearch(SortedArrayValidator validator) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
    }

    /**
     * Searches for {@code target} in a sorted array using the shared default instance.
     *
     * @param sortedArray array sorted in non-decreasing order; must not be {@code null}
     * @param target      value to locate
     * @return the index of {@code target} if found; otherwise {@link #NOT_FOUND}
     * @throws NullPointerException     if {@code sortedArray} is {@code null}
     * @throws IllegalArgumentException if {@code sortedArray} is not sorted in ascending order
     * @see #search(int[], int)
     */
    public static int binarySearch(int[] sortedArray, int target) {
        return DEFAULT_INSTANCE.search(sortedArray, target);
    }

    /**
     * Searches for {@code target} in a sorted array.
     *
     * @param sortedArray array sorted in non-decreasing order; must not be {@code null}
     * @param target      value to locate
     * @return the index of {@code target} if found; otherwise {@link #NOT_FOUND}
     * @throws NullPointerException     if {@code sortedArray} is {@code null}
     * @throws IllegalArgumentException if {@code sortedArray} is not sorted in ascending order
     */
    public int search(int[] sortedArray, int target) {
        validator.requireSorted(sortedArray);

        if (sortedArray.length == 0) {
            return NOT_FOUND;
        }

        return BinarySearchAlgorithm.search(sortedArray, target);
    }
}

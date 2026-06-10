import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BinarySearchTest {

    private static final int[] SORTED = {2, 4, 6, 8, 10, 12, 14};

    @Test
    void targetPresentInMiddle() {
        assertEquals(4, BinarySearch.binarySearch(SORTED, 10));
    }

    @Test
    void targetPresentAtVariousIndices() {
        assertEquals(1, BinarySearch.binarySearch(SORTED, 4));
        assertEquals(3, BinarySearch.binarySearch(SORTED, 8));
        assertEquals(5, BinarySearch.binarySearch(SORTED, 12));
    }

    @Test
    void targetAbsent() {
        assertEquals(-1, BinarySearch.binarySearch(SORTED, 0));
        assertEquals(-1, BinarySearch.binarySearch(SORTED, 3));
        assertEquals(-1, BinarySearch.binarySearch(SORTED, 11));
        assertEquals(-1, BinarySearch.binarySearch(SORTED, 99));
    }

    @Test
    void firstElement() {
        assertEquals(0, BinarySearch.binarySearch(SORTED, 2));
    }

    @Test
    void lastElement() {
        assertEquals(6, BinarySearch.binarySearch(SORTED, 14));
    }

    @Test
    void singleElementArrayTargetPresent() {
        assertEquals(0, BinarySearch.binarySearch(new int[] {7}, 7));
    }

    @Test
    void singleElementArrayTargetAbsent() {
        assertEquals(-1, BinarySearch.binarySearch(new int[] {7}, 5));
    }

    @Test
    void emptyArray() {
        assertEquals(-1, BinarySearch.binarySearch(new int[] {}, 5));
    }

    @Test
    void nullArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> BinarySearch.binarySearch(null, 5));
    }

    @Test
    void unsortedArrayThrowsIllegalArgumentException() {
        int[] unsorted = {1, 5, 3, 7, 9};

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BinarySearch.binarySearch(unsorted, 5));

        assertTrue(exception.getMessage().contains("sorted"));
    }

    @Test
    void duplicateValuesReturnsValidIndex() {
        int[] withDuplicates = {1, 2, 2, 2, 3, 4};

        int index = BinarySearch.binarySearch(withDuplicates, 2);

        assertTrue(index >= 0 && index < withDuplicates.length);
        assertEquals(2, withDuplicates[index]);
    }

    @Test
    void duplicateValuesAtBoundaries() {
        int[] duplicatesAtEnds = {5, 5, 5, 8, 8};

        int firstRun = BinarySearch.binarySearch(duplicatesAtEnds, 5);
        int lastRun = BinarySearch.binarySearch(duplicatesAtEnds, 8);

        assertEquals(5, duplicatesAtEnds[firstRun]);
        assertEquals(8, duplicatesAtEnds[lastRun]);
    }

    @Test
    void duplicateValuesAbsentBetweenDuplicates() {
        int[] values = {2, 4, 4, 4, 6};

        assertEquals(-1, BinarySearch.binarySearch(values, 5));
    }
}

package utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MergeSortTest {
    @Test
    void sortIntegers(){
        List<Integer> list = new ArrayList<>(List.of(5,3,1,4,2));
        MergeSort.sort(list, Integer::compare);
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }
    @Test
    void sortDescending() {
        List<Integer> list = new ArrayList<>(List.of(1, 3, 2, 5, 4));
        MergeSort.sort(list, (a, b) -> b - a);
        assertEquals(List.of(5, 4, 3, 2, 1), list);
    }
    @Test
    void singleElement() {
        List<Integer> list = new ArrayList<>(List.of(1));
        MergeSort.sort(list, Integer::compareTo);
        assertEquals(List.of(1), list);
    }
    @Test
    void emptyList() {
        List<Integer> list = new ArrayList<>();
        MergeSort.sort(list, Integer::compareTo);
        assertEquals(List.of(), list);
    }
    @Test
    void alreadySorted() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        MergeSort.sort(list, Integer::compareTo);
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }
    @Test
    void duplicateValues() {
        List<Integer> list = new ArrayList<>(List.of(3, 1, 3, 2, 1));
        MergeSort.sort(list, Integer::compareTo);
        assertEquals(List.of(1, 1, 2, 3, 3), list);
    }

}
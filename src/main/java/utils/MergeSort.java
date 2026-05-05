package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MergeSort {

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list.size() <= 1) return;

        int mid = list.size() / 2;
        //split list into two halves
        List<T> left = new ArrayList<>();
        for (int i = 0; i < mid; i++) {
            left.add(list.get(i));
        }
        List<T> right = new ArrayList<>();
        for (int i = mid; i < list.size(); i++) {
            right.add(list.get(i));
        }
        //recursively sort each half
        sort(left, comparator);
        sort(right, comparator);
        //merge the sorted halves
        merge(list, left, right, comparator);

    }
    private static <T> void merge(List<T> list, List<T> left, List<T> right, Comparator<T> comparator) {
        int i = 0, j = 0, k=0; //i position in left, j position in right, k position in original list
        //compare the elements from each half and merge
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        while(i < left.size()) list.set(k++, left.get(i++));
        while(j < right.size()) list.set(k++, right.get(j++));

    }

}

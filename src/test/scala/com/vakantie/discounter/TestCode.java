package com.vakantie.discounter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCode {

    public static void main(String[] args) {
        TestCode tc = new TestCode();
        int[] array = new int[]{5, 1, 4, 2};
        int[] result = tc.arrayOfProducts1(array);
        int[][] arr1 = new int[][]{{1, 2}, {3, 5}, {4, 7}, {6, 8}, {9, 10}};
        int[][] result1 = tc.mergeOverlappingIntervals(arr1);

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
        }

        for (int i = 0; i < result1.length; i++) {
            System.out.println(result1[i][0]+","+result1[i][1]);
        }

    }

    public int[] arrayOfProducts(int[] array) {
        // Write your code here.
        List<Integer> visited = new ArrayList<>();
        int[] finalArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            int idx = i;
            int multipliedValue = 1;
            while (idx < array.length - 1) {
                multipliedValue *= array[idx + 1];
                idx++;
            }
            for (Integer integer : visited) {
                multipliedValue *= integer;
            }

            visited.add(array[i]);
            finalArray[i] = multipliedValue;
        }
        return finalArray;
    }

    public int[] arrayOfProducts1(int[] array) {
        int[] finalArray = new int[array.length];
        int leftRunningProduct = 1;

        for (int i = 0; i < array.length; i++) {
            finalArray[i] = leftRunningProduct;
            leftRunningProduct *= array[i];
        }
        int rightRunningProduct = 1;
        for (int i = array.length - 1; i >= 0; i--) {
            finalArray[i] *= rightRunningProduct;
            rightRunningProduct *= array[i];
        }
        return finalArray;
    }

    public int[][] mergeOverlappingIntervals(int[][] intervals) {
        // Write your code here.
        if (intervals.length == 0) return new int[][]{};
        int[][] sortedIntervals = intervals.clone();
        Arrays.sort(sortedIntervals, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> mergedIntervals = new ArrayList<int[]>();
        int[] currentInterval = sortedIntervals[0];
        mergedIntervals.add(currentInterval);

        for (int[] nextInterval : sortedIntervals) {
            int currentIntervalEnd = currentInterval[1];
            int nextIntervalStart = nextInterval[0];
            int nextIntervalEnd = nextInterval[1];

            if (currentIntervalEnd >= nextIntervalStart) {
                currentInterval[1] = Math.max(currentIntervalEnd, nextIntervalEnd);
            } else {
                currentInterval = nextInterval;
                mergedIntervals.add(currentInterval);
            }
        }

        return mergedIntervals.toArray(new int[mergedIntervals.size()][]);
    }

}

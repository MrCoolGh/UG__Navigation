package dcit204.map;

import java.util.ArrayList;
import java.util.List;

public class SortingAlgorithms {

    // Quick sort implementation for RouteOption objects
    public List<RouteOption> quickSort(List<RouteOption> routes) {
        if (routes.size() <= 1) {
            return routes;
        }

        List<RouteOption> sorted = new ArrayList<>(routes);
        quickSort(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    private void quickSort(List<RouteOption> routes, int low, int high) {
        if (low < high) {
            int partitionIndex = partition(routes, low, high);

            // Sort the elements before and after the partition
            quickSort(routes, low, partitionIndex - 1);
            quickSort(routes, partitionIndex + 1, high);
        }
    }

    private int partition(List<RouteOption> routes, int low, int high) {
        // Using time as the sorting criterion
        int pivotTime = routes.get(high).getTime();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            // If current element is smaller than pivot
            if (routes.get(j).getTime() < pivotTime) {
                i++;

                // Swap routes[i] and routes[j]
                RouteOption temp = routes.get(i);
                routes.set(i, routes.get(j));
                routes.set(j, temp);
            }
        }

        // Swap routes[i+1] and routes[high] (pivot)
        RouteOption temp = routes.get(i + 1);
        routes.set(i + 1, routes.get(high));
        routes.set(high, temp);

        return i + 1;
    }

    // Merge sort implementation for RouteOption objects
    public List<RouteOption> mergeSort(List<RouteOption> routes) {
        if (routes.size() <= 1) {
            return routes;
        }

        List<RouteOption> sorted = new ArrayList<>(routes);
        mergeSort(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    private void mergeSort(List<RouteOption> routes, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // Sort first and second halves
            mergeSort(routes, left, mid);
            mergeSort(routes, mid + 1, right);

            // Merge the sorted halves
            merge(routes, left, mid, right);
        }
    }

    private void merge(List<RouteOption> routes, int left, int mid, int right) {
        // Find sizes of subarrays to be merged
        int n1 = mid - left + 1;
        int n2 = right - mid;

        // Create temporary arrays
        List<RouteOption> leftArray = new ArrayList<>();
        List<RouteOption> rightArray = new ArrayList<>();

        // Copy data to temporary arrays
        for (int i = 0; i < n1; i++) {
            leftArray.add(routes.get(left + i));
        }

        for (int j = 0; j < n2; j++) {
            rightArray.add(routes.get(mid + 1 + j));
        }

        // Merge the temporary arrays
        int i = 0, j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            // Compare by distance this time
            if (leftArray.get(i).getDistance() <= rightArray.get(j).getDistance()) {
                routes.set(k, leftArray.get(i));
                i++;
            } else {
                routes.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }

        // Copy remaining elements of leftArray if any
        while (i < n1) {
            routes.set(k, leftArray.get(i));
            i++;
            k++;
        }

        // Copy remaining elements of rightArray if any
        while (j < n2) {
            routes.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }

    // Sort routes by landmarks (how many landmarks they pass)
    public List<RouteOption> sortByLandmarkCount(List<RouteOption> routes) {
        List<RouteOption> sorted = new ArrayList<>(routes);

        // Using selection sort for demonstration
        for (int i = 0; i < sorted.size() - 1; i++) {
            int maxIndex = i;

            for (int j = i + 1; j < sorted.size(); j++) {
                if (sorted.get(j).getLandmarks().size() > sorted.get(maxIndex).getLandmarks().size()) {
                    maxIndex = j;
                }
            }

            // Swap the found maximum element with the element at index i
            RouteOption temp = sorted.get(maxIndex);
            sorted.set(maxIndex, sorted.get(i));
            sorted.set(i, temp);
        }

        return sorted;
    }
}
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class Sorting {

    private static final int THREAD_THRESHOLD = 10000; // Size below which normal recursion is used
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors(); // Dynamic thread pool

    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    public static void mergeSort(int[] array, int left, int right) throws ExecutionException, InterruptedException {
        if (left < right) {
            if (right - left < THREAD_THRESHOLD) {
                // Normal recursive merge sort
                int mid = (left + right) / 2;
                mergeSort(array, left, mid);
                mergeSort(array, mid + 1, right);
                merge(array, left, mid, right);
            } else {
                int mid = (left + right) / 2;

                // Submit left and right tasks to executor
                Future<?> leftFuture = executor.submit(() -> {
                    try {
                        mergeSort(array, left, mid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                Future<?> rightFuture = executor.submit(() -> {
                    try {
                        mergeSort(array, mid + 1, right);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Wait for both to finish
                leftFuture.get();
                rightFuture.get();

                merge(array, left, mid, right);
            }
        }
    }

    private static void merge(int[] array, int left, int mid, int right) {
        int[] leftArray = Arrays.copyOfRange(array, left, mid + 1);
        int[] rightArray = Arrays.copyOfRange(array, mid + 1, right + 1);

        int i = 0, j = 0, k = left;

        while (i < leftArray.length && j < rightArray.length) {
            array[k++] = (leftArray[i] <= rightArray[j]) ? leftArray[i++] : rightArray[j++];
        }

        while (i < leftArray.length) array[k++] = leftArray[i++];
        while (j < rightArray.length) array[k++] = rightArray[j++];
    }

    public static void main(String[] args) {
        int size = 1000;
        int[] array = new int[size];
        Random random = new Random();

        // Fill array with random integers
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1_000_000);
        }

        System.out.println("Sorting started...");
        // Time when we are going to start sorting the array
        long start = System.currentTimeMillis();

        try {
            mergeSort(array, 0, array.length - 1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // Always shut down the executor
        }

        long end = System.currentTimeMillis();
        System.out.println("Sorting completed in " + (end - start) + " ms");

        // Uncomment to verify sorted array
        System.out.println(Arrays.toString(array));
    }
}

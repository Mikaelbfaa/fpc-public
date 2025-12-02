import java.io.*;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class FileSimilarity {

    // Total sum of all files
    static long totalSum = 0;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        int len = args.length;
        int threadLimit = len / 2;
        Semaphore concurrentFiles = new Semaphore(threadLimit);
        Semaphore mutex = new Semaphore(1);
        Thread[] threads = new Thread[len];
        CyclicBarrier barrier = new CyclicBarrier(len);

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();

        for (int i = 0; i < len; i++) {
            Thread new_thread = new Thread(new SimilarityRunnable(mutex, concurrentFiles, fileFingerprints, args, i, barrier));
            threads[i] = new_thread;
        }

        for(Thread thread :threads) {
            thread.start();
        }

        for(Thread thread :threads) {
            thread.join();
        }

        // Printing totalSum
        System.out.println("Total sum: " + totalSum);
    }
}

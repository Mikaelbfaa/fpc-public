import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class SimilarityRunnable implements Runnable {
    private final Semaphore mutex;
    private final Semaphore concurrentThreads;
    private final Map<String, List<Long>> fileFingerprints;
    private final CyclicBarrier barrier;
    private final int index;
    private final String[] args;
    private static final int NUM_SUM_THREADS = 2;

    public SimilarityRunnable(Semaphore mutex, Semaphore concurrentThreads, Map<String, List<Long>> fileFingerprints, String[] args, int index, CyclicBarrier barrier) {
        this.mutex = mutex;
        this.concurrentThreads = concurrentThreads;
        this.fileFingerprints = fileFingerprints;
        this.args = args;
        this.index = index;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            this.concurrentThreads.acquire();
            String path = this.args[index];
            List<Long> fingerprint = fileSum(path);
            mutex.acquire();
            fileFingerprints.put(path, fingerprint);
            mutex.release();
            this.concurrentThreads.release();

            barrier.await();

            List<Thread> similarityThreads = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                if (this.index < i) {
                    String file1 = args[index];
                    String file2 = args[i];
                    mutex.acquire();
                    List<Long> fingerprint1 = fileFingerprints.get(file1);
                    List<Long> fingerprint2 = fileFingerprints.get(file2);
                    mutex.release();

                    Thread t = new Thread(new SimilarityPairRunnable(file1, file2, fingerprint1, fingerprint2, concurrentThreads));
                    similarityThreads.add(t);
                    t.start();
                }
            }

            for (Thread t : similarityThreads) {
                t.join();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Long> fileSum(String filePath) {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long chunkSum = concurrentSum(buffer, bytesRead);
                chunks.add(chunkSum);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return chunks;
    }

    private long concurrentSum(byte[] buffer, int length) throws InterruptedException {
        int chunkSize = length / NUM_SUM_THREADS;
        SumRunnable[] runnables = new SumRunnable[NUM_SUM_THREADS];
        Thread[] threads = new Thread[NUM_SUM_THREADS];

        for (int t = 0; t < NUM_SUM_THREADS; t++) {
            int start = t * chunkSize;
            int end = (t == NUM_SUM_THREADS - 1) ? length : start + chunkSize;
            runnables[t] = new SumRunnable(buffer, start, end, mutex);
            threads[t] = new Thread(runnables[t]);
            threads[t].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long totalChunkSum = 0;
        for (SumRunnable r : runnables) {
            totalChunkSum += r.getResult();
        }

        return totalChunkSum;
    }
}

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
        try{
            this.concurrentThreads.acquire();
            String path = this.args[index];
            List<Long> fingerprint = fileSum(path);
            mutex.acquire();
            fileFingerprints.put(path, fingerprint);
            mutex.release();
            this.concurrentThreads.release();


            barrier.await();

            this.concurrentThreads.acquire();
            for (int i = 0; i < args.length; i++) {
                if(this.index < i) {
                    String file1 = args[index];
                    String file2 = args[i];
                    mutex.acquire();
                    List<Long> fingerprint1 = fileFingerprints.get(file1);
                    List<Long> fingerprint2 = fileFingerprints.get(file2);
                    mutex.release();
                    float similarityScore = similarity(fingerprint1, fingerprint2);
                    System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
                }
            }

            this.concurrentThreads.release();
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
                long sum = sum(buffer, bytesRead);
                chunks.add(Long.valueOf(sum));
                this.mutex.acquire();
                FileSimilarity.totalSum += sum;
                this.mutex.release();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return chunks;
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }

        return (float) counter / base.size();
    }
}

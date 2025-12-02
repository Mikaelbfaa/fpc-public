import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SimilarityPairRunnable implements Runnable {
    private final String file1;
    private final String file2;
    private final List<Long> fingerprint1;
    private final List<Long> fingerprint2;
    private final Semaphore concurrentThreads;

    public SimilarityPairRunnable(String file1, String file2, List<Long> fingerprint1, List<Long> fingerprint2, Semaphore concurrentThreads) {
        this.file1 = file1;
        this.file2 = file2;
        this.fingerprint1 = fingerprint1;
        this.fingerprint2 = fingerprint2;
        this.concurrentThreads = concurrentThreads;
    }

    @Override
    public void run() {
        try {
            concurrentThreads.acquire();
            float similarityScore = similarity(fingerprint1, fingerprint2);
            System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
            concurrentThreads.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

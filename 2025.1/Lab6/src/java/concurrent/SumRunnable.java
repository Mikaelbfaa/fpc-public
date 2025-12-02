import java.util.concurrent.Semaphore;

public class SumRunnable implements Runnable {
    private final byte[] buffer;
    private final int start;
    private final int end;
    private final Semaphore mutex;
    private long result;

    public SumRunnable(byte[] buffer, int start, int end, Semaphore mutex) {
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.mutex = mutex;
    }

    public long getResult() {
        return result;
    }

    @Override
    public void run() {
        result = 0;
        for (int i = start; i < end; i++) {
            result += Byte.toUnsignedInt(buffer[i]);
        }

        try {
            mutex.acquire();
            FileSimilarity.totalSum += result;
            mutex.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

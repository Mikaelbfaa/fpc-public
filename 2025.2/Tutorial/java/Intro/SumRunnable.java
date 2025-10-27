import java.util.concurrent.Semaphore;

public class SumRunnable implements Runnable {
    private int value;
    private Semaphore sem;
    private Sum sumIntance;

    public SumRunnable(int X, Semaphore sem, Sum mainInstance) {
        this.value = X;
        this.sem = sem;
        this.sumIntance = mainInstance;
    }

    @Override
    public void run() {

        try {
            System.out.println(Thread.currentThread().getName() + " will sleep for " + this.value + " ms.");
            Thread.sleep(this.value);
            System.out.println(Thread.currentThread().getName() + " woke up!");
            this.sumX();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void sumX() throws InterruptedException {
        sem.acquire();

        try {
            sumIntance.incrementX(this.value);
            System.out.println(Thread.currentThread().getName() + " adding " + this.value);
        } finally {
            sem.release();
        }
    }
}
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeCounter {
    AtomicInteger counter;

    public ThreadSafeCounter() {
        this.counter = new AtomicInteger(0);
    }

    public void increment() {
        this.counter.incrementAndGet();
    }

    public void decrement() {
        this.counter.decrementAndGet();
    }

    public void reset() {
        this.counter.set(0);
    }

    public int getValue() {
        return this.counter.get();
    }

    public boolean addIfPositive(int n) {
        while (true) {
            int current = this.counter.get();

            if (current < 0) {
                if (this.counter.compareAndSet(current, current)) {
                    return false;
                }

                continue;
            }

            if (this.counter.compareAndSet(current, current + n)) {
                return true;
            }
        }
    }
}
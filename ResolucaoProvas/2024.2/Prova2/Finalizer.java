import java.util.concurrent.atomic.AtomicBoolean;

public class Finalizer {
    AtomicBoolean executed;
    
    public Finalizer() {
        this.executed = new AtomicBoolean(false);
    }
    
    public void shutdown() {
        if (this.executed.compareAndSet(false, true)) {
            this.destroy();
        }
    }

    private void destroy() {
        System.out.println("Destroying something idk");
    }
}

import java.util.LinkedList;
import java.util.Queue;

public class Buffer<T> {
    private final Queue<T> queue = new LinkedList<>();

    public void insert(T item) {
        queue.add(item);
    }

    public T remove() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}


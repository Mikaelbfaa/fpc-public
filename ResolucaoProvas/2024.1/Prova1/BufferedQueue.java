import java.util.LinkedList;
import java.util.concurrent.Semaphore;

class BufferedQueue {
    private Semaphore emptySpaces;
    private Semaphore availableItems;
    private Semaphore mutex;
    private final LinkedList<Integer> queue;

    public BufferedQueue(int capacity) {
        this.queue = new LinkedList<>();
        this.emptySpaces = new Semaphore(capacity);
        this.availableItems = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    public int dequeue() throws InterruptedException {
        this.availableItems.acquire();
        this.mutex.acquire();
        int item = this.queue.pop();
        this.mutex.release();
        this.emptySpaces.release();
        return item;
    }

    public void enqueue(int n) throws InterruptedException {
        this.emptySpaces.acquire();
        this.mutex.acquire();
        this.queue.add(n);
        this.mutex.release();
        this.availableItems.release();
    }

    public static void main(String[] args) {
        BufferedQueue buffer = new BufferedQueue(5);
        
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    buffer.enqueue(i);
                    System.out.println("Produtor adicionou: " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    int item = buffer.dequeue();
                    System.out.println("Consumidor removeu: " + item);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        producer.start();
        consumer.start();
        
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Teste concluído!");
    }
}

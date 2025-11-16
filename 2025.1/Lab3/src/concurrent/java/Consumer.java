import java.util.concurrent.Semaphore;

class Consumer implements Runnable {
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private int type;
    private Semaphore consumerBuffer;
    private Semaphore producerBuffer;
    private Semaphore mutex;
    
    public Consumer(int id, Buffer buffer, int sleepTime, int type, Semaphore cSemaphore, Semaphore pSemaphore, Semaphore mutex) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.consumerBuffer = cSemaphore;
        this.producerBuffer = pSemaphore;
        this.mutex = mutex;
        this.type = type;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.consumerBuffer.acquire();
                this.mutex.acquire();
                int item =  buffer.remove();
                
                if ((this.type == 0 && item % 2 != 0) || (this.type == 1 && item % 2 == 0)) {
                    buffer.put(item);
                    System.out.println("Consumer " + id + " did not consumed item (invalid type) " + item);
                    this.consumerBuffer.release();

                } else {
                    System.out.println("Consumer " + id + " consumed item: " + item);
                    this.producerBuffer.release();
                }
                
                this.mutex.release();
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
}
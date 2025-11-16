import java.util.concurrent.Semaphore;

class Producer implements Runnable{
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private Semaphore consumerBuffer;
    private Semaphore producerBuffer;
    private Semaphore mutex;
    
    public Producer(int id, Buffer buffer, int sleepTime, Semaphore cSemaphore, Semaphore pSemaphore, Semaphore mutex) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.consumerBuffer = cSemaphore;
        this.producerBuffer = pSemaphore;
        this.mutex = mutex;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.producerBuffer.acquire();
                mutex.acquire();
                int item = (int) (Math.random() * 100);
                buffer.put(item);
                System.out.println("Producer " + id + " produced item " + item);
                this.consumerBuffer.release();
                mutex.release();
                Thread.sleep(this.sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_consumers> <consuming_time>");
            return;
        }
        
        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);
        Thread[] pThreads = new Thread[numProducers];
        Thread[] cThreads = new Thread[numConsumers];

        Semaphore pSemaphore = new Semaphore(maxItemsPerProducer);
        Semaphore cSemaphore = new Semaphore(0);
        Semaphore mutex = new Semaphore(1);
        
        Buffer buffer = new Buffer();
        
        for (int i = 1; i <= numProducers; i++) {
            pThreads[i - 1] = new Thread(new Producer(i, buffer, producingTime, cSemaphore, pSemaphore, mutex));
        }
        
        for (int i = 1; i <= numConsumers; i++) {
            cThreads[i - 1] = new Thread(new Consumer(i, buffer, consumingTime,(i % 2), cSemaphore, pSemaphore, mutex));
        }

        for (Thread thread : cThreads) {
            thread.start();
        }

        for (Thread thread : pThreads) {
            thread.start();
        }
        
        try {
            
            for (Thread thread : cThreads) {
                thread.join();
            }
    
            for (Thread thread : pThreads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

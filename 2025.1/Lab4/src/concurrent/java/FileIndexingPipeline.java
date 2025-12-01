import java.util.*;
import java.util.concurrent.Semaphore;

public class FileIndexingPipeline {

    static Map<String, Map<String, Integer>> fileIndex = new HashMap<>();
    static final int BUFFER_SIZE = 50;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java FileIndexingPipeline <arquivo1.txt> <arquivo2.txt> ...");
            return;
        }

        // Buffers
        Buffer<FileData> readBuffer = new Buffer<>();
        Buffer<FileData> tokenBuffer = new Buffer<>();

        // Semáforos para readBuffer
        Semaphore items1 = new Semaphore(0);
        Semaphore empty1 = new Semaphore(BUFFER_SIZE);

        // Semáforos para tokenBuffer
        Semaphore items2 = new Semaphore(0);
        Semaphore empty2 = new Semaphore(BUFFER_SIZE);

        // Mutex global
        Semaphore mutex = new Semaphore(1);

        // Criar threads
        Thread reader = new Thread(new ReaderRunnable(items1, empty1, mutex, args, readBuffer));
        Thread tokenizer = new Thread(new TokenizeRunnable(items1, empty1, items2, empty2, mutex, readBuffer, tokenBuffer));
        Thread indexer = new Thread(new indexRunnable(items2, empty2, mutex, tokenBuffer));

        // Iniciar threads
        reader.start();
        tokenizer.start();
        indexer.start();

        // Aguardar término
        try {
            reader.join();
            tokenizer.join();
            indexer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Imprimir resultado
        System.out.println("fileIndex:");
        for (var word : fileIndex.keySet()) {
            System.out.println(word + " -> " + fileIndex.get(word));
        }
    }
}

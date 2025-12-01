import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

public class ReaderRunnable implements Runnable {
    private final Semaphore items;
    private final Semaphore empty;
    private final Semaphore mutex;
    private final String[] args;
    private final Buffer<FileData> readBuffer;

    public ReaderRunnable(Semaphore items, Semaphore empty, Semaphore mutex, String[] args, Buffer<FileData> readBuffer) {
        this.items = items;
        this.empty = empty;
        this.args = args;
        this.mutex = mutex;
        this.readBuffer = readBuffer;
    }

    @Override
    public void run() {
        for (String pathStr : args) {
            try {
                this.empty.acquire();
                Path path = Paths.get(pathStr);
                String content = Files.readString(path);
                this.mutex.acquire();
                this.readBuffer.insert(new FileData(path.getFileName().toString(), content));
                this.mutex.release();
                this.items.release();
            } catch (Exception e) {
                System.err.println("Erro ao ler arquivo " + pathStr + ": " + e.getMessage());
            }
        }

        try {
            this.empty.acquire();
            mutex.acquire();
            this.readBuffer.insert(null);
            mutex.release();
            this.items.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

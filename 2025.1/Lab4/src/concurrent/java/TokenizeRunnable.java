import java.util.concurrent.Semaphore;

public class TokenizeRunnable implements Runnable {
    private final Semaphore itemsIntermediate;
    private final Semaphore emptyIntermediate;
    private final Semaphore itemsToken;
    private final Semaphore emptyToken;
    private final Semaphore mutex;
    private final Buffer<FileData> readBuffer;
    private final Buffer<FileData> tokenBuffer; 

    public TokenizeRunnable(Semaphore itemsIntermediate, Semaphore emptyIntermediate, Semaphore itemsToken, Semaphore emptyToken, Semaphore mutex, Buffer<FileData> readBuffer, Buffer<FileData> tokBuffer) {
        this.itemsIntermediate = itemsIntermediate;
        this.emptyIntermediate = emptyIntermediate;
        this.itemsToken = itemsToken;
        this.emptyToken = emptyToken;
        this.readBuffer = readBuffer;
        this.tokenBuffer = tokBuffer;
        this.mutex = mutex;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.itemsIntermediate.acquire();
                mutex.acquire();
                FileData fileData = this.readBuffer.remove();
                mutex.release();
                this.emptyIntermediate.release();

                if (fileData == null) {
                    this.emptyToken.acquire();
                    this.mutex.acquire();
                    this.tokenBuffer.insert(null);
                    this.mutex.release();
                    this.itemsToken.release();
                    return;
                }

                String[] words = fileData.content.split("\\s+");
                String newContent = String.join(",", words);
                this.emptyToken.acquire();
                mutex.acquire();
                this.tokenBuffer.insert(new FileData(fileData.name, newContent));
                mutex.release();
                this.itemsToken.release();   
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

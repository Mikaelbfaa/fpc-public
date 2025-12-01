import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class indexRunnable implements Runnable {
    private final Semaphore items;
    private final Semaphore empty;
    private final Semaphore mutex;
    private final Buffer<FileData> tokenBuffer;

    public indexRunnable(Semaphore items, Semaphore empty, Semaphore mutex, Buffer<FileData> tokBuffer) {
        this.items = items;
        this.empty = empty;
        this.mutex = mutex;
        this.tokenBuffer = tokBuffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.items.acquire();
                mutex.acquire();
                FileData fileData = tokenBuffer.remove();
                mutex.release();
                this.empty.release();
                if (fileData == null) return;
                String[] words = fileData.content.split(",");
                for (String word : words) {
                    this.mutex.acquire();
                    FileIndexingPipeline.fileIndex.putIfAbsent(word, new HashMap<>());
                    Map<String, Integer> fileDatas = FileIndexingPipeline.fileIndex.get(word);
                    fileDatas.put(fileData.name, fileDatas.getOrDefault(fileData.name, 0) + 1);
                    this.mutex.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Sum {
    private static int total = 0;
    private static Semaphore sem = new Semaphore(1);

    public static void sum(FileInputStream fis, String path) throws IOException {
        
	    int byteRead;
        int sum = 0;

        while ((byteRead = fis.read()) != -1) {
            sum += byteRead;
        }

        try {            
            sem.acquire();
            total += sum;
            System.out.println(path + " : " + sum);
            sem.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sum_p(String path) throws IOException {

        Path f_path = Paths.get(path);

        if (Files.isRegularFile(f_path)) {
            try {
                FileInputStream fis = new FileInputStream(path);
                sum(fis, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

	//many exceptions could be thrown here. we don't care
        for (String path : args) {
            Thread thread = new Thread(new RunnableSum(path), "Thread of path " + path);
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Total: " + total);
    }
}

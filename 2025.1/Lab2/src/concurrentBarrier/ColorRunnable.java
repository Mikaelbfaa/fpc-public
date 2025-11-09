import java.util.concurrent.CyclicBarrier;

public class ColorRunnable implements Runnable {
    private float[][] image;
    private float[][] psf;
    private float[][] psfFlipped;
    private float[][] colorRestored;
    private int num_threads;
    private int iterations;

    public ColorRunnable (float[][] image, float[][] psf, float[][] psfFlipped, float[][] colorRestored, int iterations, int num_threads) {
        this.image = image;
        this.psf = psf;
        this.psfFlipped = psfFlipped;
        this.iterations = iterations;
        this.colorRestored = colorRestored;
        this.num_threads = num_threads;
    }

    @Override
    public void run() {
        this.colorRestored = richardsonLucy(this.image, this.psf, this.psfFlipped, this.iterations, this.num_threads);
    }

    public static float[][] richardsonLucy(float[][] image, float[][] psf, float[][] psfFlipped, int iterations, int num_threads) {
        int h = image.length;
        int w = image[0].length;
        int startY;
        int endY;
        int linesThread = h / num_threads;
        CyclicBarrier barrier = new CyclicBarrier(num_threads);
        Thread[] threads = new Thread[num_threads]; 
        float[][] estimate = new float[h][w];
        float[][] ratio = new float[h][w];
        float[][] estimateBlurred = new float[h][w];
        float[][] correction = new float[h][w];


        // Inicializa com valor constante (pode ser a imagem borrada)
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                estimate[y][x] = 0.5f;


        for (int i = 0; i < num_threads; i++) {
            startY = linesThread * i;

            if (i == num_threads - 1) {
                endY = h;
            } else {
                endY = linesThread * (i + 1);
            }

            threads[i] = new Thread(new RichardsonLucyRunnable(startY, endY, image, psf, psfFlipped, estimate, iterations, barrier, ratio, estimateBlurred, correction));
            threads[i].start();
        }

        try {        
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return estimate;
    }

    public float[][] getColorRestored() {
        return this.colorRestored;
    }
}

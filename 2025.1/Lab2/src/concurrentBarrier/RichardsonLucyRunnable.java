import java.util.concurrent.CyclicBarrier;

public class RichardsonLucyRunnable implements Runnable {
    private int startY;
    private int endY;
    private float[][] image;
    private float[][] psf;
    private float[][] psfFlipped;
    private float[][] estimate;
    private int iterations;
    private CyclicBarrier barrier;
    private float[][] ratio;
    private float[][] estimateBlurred;
    private float[][] correction;

    public RichardsonLucyRunnable(int startY, int endY, float[][] image, float[][] psf, float[][] psfFlipped, float[][] estimate, int iterations, CyclicBarrier barrier, float[][] ratio, float[][] estimateBlurred, float[][] correction) {
        this.startY = startY;
        this.endY = endY;
        this.image = image;
        this.psf = psf;
        this.psfFlipped = psfFlipped;
        this.estimate = estimate;
        this.iterations = iterations;
        this.barrier = barrier;
        this.ratio = ratio;
        this.estimateBlurred = estimateBlurred;
        this.correction = correction;
    }

    @Override
    public void run() {
        int w = this.image[0].length;

        try {
            for (int i = 0; i < iterations; i++) {
                // Não é necessário nesse caso, só estou resetando arrays para fins didáticos
                // 1° Fase
                this.resetArrays();
                this.barrier.await();
    
                // 2° Fase
                this.convolve(estimate, psf, estimateBlurred);
                this.barrier.await();
                
                // 3° Fase
                for (int y = this.startY; y < this.endY; y++)
                    for (int x = 0; x < w; x++) {
                        float eb = estimateBlurred[y][x];
                        ratio[y][x] = (eb > 1e-6f) ? this.image[y][x] / eb : 0f;
                    }
                
                this.barrier.await();

                // 4° Fase
                this.convolve(ratio, this.psfFlipped, correction);
                this.barrier.await();

                // 5° Fase
                for (int y = this.startY; y < this.endY; y++)
                    for (int x = 0; x < w; x++)
                        this.estimate[y][x] *= correction[y][x];
                
                this.barrier.await();
    
            }         
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    protected float[][] convolve(float[][] image, float[][] kernel, float[][] result) {
        int h = image.length, w = image[0].length;
        int kh = kernel.length, kw = kernel[0].length;
        int kyc = kh / 2, kxc = kw / 2;

        for (int y = this.startY; y < this.endY; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0f;
                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {
                        int iy = y + ky - kyc;
                        int ix = x + kx - kxc;
                        if (iy >= 0 && iy < h && ix >= 0 && ix < w) {
                            sum += image[iy][ix] * kernel[ky][kx];
                        }
                    }
                }
                result[y][x] = sum;
            }
        }
        return result;
    }

    private void resetArrays() {
        for (int i = startY; i < endY; i++) {
            for (int j = 0; j < image[0].length; j++) {
                this.ratio[i][j] = 0f;
                this.estimateBlurred[i][j] = 0f;
                this.correction[i][j] = 0f;
            }
        }
    }
}

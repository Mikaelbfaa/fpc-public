public class RunnableConvolve implements Runnable {
    private float[][] kernel;
    private float[][] img;
    private float[][] result;
    private int startY;
    private int endY;

    public RunnableConvolve(float[][] img, float[][] kernel, float[][] result, int startY, int endY) {
        this.kernel = kernel;
        this.img = img;
        this.result = result;
        this.startY = startY;
        this.endY = endY;
    }

    @Override
    public void run() {
        int h = this.img.length, w = this.img[0].length;
        int kh = this.kernel.length, kw = this.kernel[0].length;
        int kyc = kh / 2, kxc = kw / 2;

        for (int y = this.startY; y < this.endY; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0f;
                for (int ky = 0; ky < kh; ky++) {
                    for (int kx = 0; kx < kw; kx++) {
                        int iy = y + ky - kyc;
                        int ix = x + kx - kxc;
                        if (iy >= 0 && iy < h && ix >= 0 && ix < w) {
                            sum += this.img[iy][ix] * this.kernel[ky][kx];
                        }
                    }
                }
                this.result[y][x] = sum;
            }
        }
    }
}

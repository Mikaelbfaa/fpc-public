public class RunnableColor implements Runnable {
    private float[][] color;
    private float[][] psf;
    private float[][] psfFlipped;
    private int iterations;
    private float[][] result;

    public RunnableColor(float[][] color, float[][] psf, float[][] psfFlipped, int iterations) {
        this.color = color;
        this.psf = psf;
        this.psfFlipped = psfFlipped;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        this.result = Deconvolution.richardsonLucy(this.color, this.psf, this.psfFlipped, this.iterations);
    }

    public float[][] getResult() {
        return this.result;
    }
}

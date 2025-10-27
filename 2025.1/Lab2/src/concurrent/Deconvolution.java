import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Deconvolution {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Uso: java Deconvolution <imagem_borrada_path>");
            return;
        }

        String imagePath = args[0];
        BufferedImage input = ImageIO.read(new File(imagePath));
        int width = input.getWidth();
        int height = input.getHeight();

        // Separa canais R, G, B
        float[][] red = new float[height][width];
        float[][] green = new float[height][width];
        float[][] blue = new float[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(input.getRGB(x, y));
                red[y][x] = c.getRed() / 255f;
                green[y][x] = c.getGreen() / 255f;
                blue[y][x] = c.getBlue() / 255f;
            }
        }

        // Kernel Gaussiano (ajustável conforme o tipo de desfoque)
        float[][] psf = gaussianKernel(9, 2.0f);
        float[][] psfFlipped = invertKernel(psf);
        
        
        // Aplica Richardson-Lucy por canal
        int iterations = 15;

        RunnableColor redRunnable = new RunnableColor(red, psf, psfFlipped, iterations);
        RunnableColor greenRunnable = new RunnableColor(green, psf, psfFlipped, iterations);
        RunnableColor blueRunnable = new RunnableColor(blue, psf, psfFlipped, iterations);

        Thread redThread = new Thread(redRunnable);
        Thread greenThread = new Thread(greenRunnable);
        Thread blueThread = new Thread(blueRunnable);

        redThread.start();
        greenThread.start();
        blueThread.start();

        redThread.join();
        greenThread.join();
        blueThread.join();

        float[][] redRestored = redRunnable.getResult();
        float[][] greenRestored = greenRunnable.getResult();
        float[][] blueRestored = blueRunnable.getResult();

        // Salva imagem restaurada
        saveColorImage(redRestored, greenRestored, blueRestored, "restaurada.png");
        System.out.println("Imagem restaurada salva como restaurada.png");
    }

    // Richardson-Lucy Iterativo
    public static float[][] richardsonLucy(float[][] image, float[][] psf, float[][] psfFlipped, int iterations) {
        int h = image.length;
        int w = image[0].length;
        float[][] estimate = new float[h][w];

        // Inicializa com valor constante (pode ser a imagem borrada)
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                estimate[y][x] = 0.5f;

        for (int it = 0; it < iterations; it++) {
            float[][] estimateBlurred = convolveConcurrent(estimate, psf);
            float[][] ratio = new float[h][w];

            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    float eb = estimateBlurred[y][x];
                    ratio[y][x] = (eb > 1e-6f) ? image[y][x] / eb : 0f;
                }

            float[][] correction = convolveConcurrent(ratio, psfFlipped);

            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    estimate[y][x] *= correction[y][x];
        }

        return estimate;
    }

    // Convolução 2D
    public static float[][] convolve(float[][] image, float[][] kernel) {
        int h = image.length, w = image[0].length;
        int kh = kernel.length, kw = kernel[0].length;
        int kyc = kh / 2, kxc = kw / 2;

        float[][] result = new float[h][w];

        for (int y = 0; y < h; y++) {
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

    public static float[][] convolveConcurrent(float[][] image, float[][] kernel) {
        int h = image.length, w = image[0].length;
        int num_threads = 4;
        int startY, endY;
        Thread newThread;

        float[][] result = new float[h][w];

        int linesPerThread = h / num_threads;
        Thread[] threads = new Thread[num_threads];
        
        for (int i = 0; i < num_threads; i++) {
            startY = linesPerThread * i;
            
            if (i == num_threads - 1) {
                endY = h;
            } else {
                endY = linesPerThread * (i + 1);
            }

            newThread = new Thread(new RunnableConvolve(image, kernel, result, startY, endY));
            newThread.start();
            threads[i] = newThread;
        }

        try {
            for (int j = 0; j < threads.length; j++) {
                threads[j].join();
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;

    }

    // Espelha o kernel horizontal e verticalmente
    public static float[][] invertKernel(float[][] kernel) {
        int h = kernel.length, w = kernel[0].length;
        float[][] result = new float[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                result[y][x] = kernel[h - y - 1][w - x - 1];
        return result;
    }

    // Kernel Gaussiano normalizado
    public static float[][] gaussianKernel(int size, float sigma) {
        float[][] kernel = new float[size][size];
        float mean = size / 2f;
        float sum = 0f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float val = (float) Math.exp(-0.5 * (
                    Math.pow((x - mean) / sigma, 2) +
                    Math.pow((y - mean) / sigma, 2)));
                kernel[y][x] = val;
                sum += val;
            }
        }

        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                kernel[y][x] /= sum;

        return kernel;
    }

    // Salva a imagem RGB em PNG
    public static void saveColorImage(float[][] r, float[][] g, float[][] b, String filename) throws Exception {
        int h = r.length, w = r[0].length;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int red = clampToByte(r[y][x] * 255f);
                int green = clampToByte(g[y][x] * 255f);
                int blue = clampToByte(b[y][x] * 255f);
                Color color = new Color(red, green, blue);
                out.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(out, "png", new File(filename));
    }

    private static int clampToByte(float val) {
        return Math.min(255, Math.max(0, Math.round(val)));
    }
}


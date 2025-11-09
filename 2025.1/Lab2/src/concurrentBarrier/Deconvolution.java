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
        int num_threads = 4;

        // É possível (e recomendado) fazer toda essa etapa usando loops, estou fazendo linha a linha para explicitar cada cor.
        ColorRunnable redRunnable = new ColorRunnable(red, psf, psfFlipped, red, iterations, num_threads);
        ColorRunnable greenRunnable = new ColorRunnable(green, psf, psfFlipped, red, iterations, num_threads);
        ColorRunnable blueRunnable = new ColorRunnable(blue, psf, psfFlipped, red, iterations, num_threads);

        Thread redThread = new Thread(redRunnable);
        Thread greenThread = new Thread(greenRunnable);
        Thread blueThread = new Thread(blueRunnable);

        redThread.start();
        greenThread.start();
        blueThread.start();

        redThread.join();
        greenThread.join();
        blueThread.join();

        float[][] redRestored = redRunnable.getColorRestored();
        float[][] greenRestored = greenRunnable.getColorRestored();
        float[][] blueRestored = blueRunnable.getColorRestored();

        // Salva imagem restaurada
        saveColorImage(redRestored, greenRestored, blueRestored, "restaurada.png");
        System.out.println("Imagem restaurada salva como restaurada.png");
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


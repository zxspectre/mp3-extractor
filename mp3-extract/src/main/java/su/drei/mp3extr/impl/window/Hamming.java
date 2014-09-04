package su.drei.mp3extr.impl.window;

public class Hamming implements IWindowFunc {

    public double[] window(int[] data) {
        double[] res = new double[data.length];

        // Hamming - looks ok
        for (int i = 0; i < data.length; i++) {
            res[i] = data[i] * (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (data.length - 1)));
        }
        return res;
    }

}

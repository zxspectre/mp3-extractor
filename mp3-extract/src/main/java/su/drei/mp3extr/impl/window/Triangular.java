package su.drei.mp3extr.impl.window;

public class Triangular implements IWindowFunc {

    public double[] window(double[] data) {
        double[] res = new double[data.length];
        // triangular window function - too noisy
        for (int i = 0; i < data.length / 2; i++) {
            res[i] = data[i] * (i / (float) (data.length / 2));
            res[i + (data.length / 2)] = data[i + (data.length / 2)] * (1.0 - (i / (float) (data.length / 2)));
        }
        return res;
    }

}

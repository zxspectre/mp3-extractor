package su.drei.mp3extr.impl.window;

public class Blackman implements IWindowFunc {

    public float[] window(float[] data) {
        float[] res = new float[data.length];
        // Blackman -
        for (int i = 0; i < data.length; i++) {
            res[i] = (float) (data[i] * (0.42 - 0.5 * Math.cos(2 * Math.PI * i / (data.length - 1)) + 0.08 * Math.cos(4 * Math.PI * i / (data.length - 1))));
        }

        return res;
    }

}

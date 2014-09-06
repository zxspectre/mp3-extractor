package su.drei.mp3extr.impl.window;

public class BlackmanHarris implements IWindowFunc {

    public float[] window(float[] data) {
        float[] res = new float[data.length];

        // Blackman - Harris
        for (int i = 0; i < data.length; i++) {
            res[i] = (float) (data[i] * (0.35875 - 0.48829 * Math.cos(2 * Math.PI * i / (data.length - 1)) + 0.14128 * Math.cos(4 * Math.PI * i / (data.length - 1)) - 0.01168 * Math.cos(6 * Math.PI * i / (data.length - 1))));
        }
        return res;
    }

}

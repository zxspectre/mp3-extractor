package su.drei.mp3extr.impl;

import org.apache.commons.math3.complex.Complex;

import su.drei.mp3extr.exporter.IDataExporter;
import su.drei.mp3extr.impl.window.IWindowFunc;

public class HistogramCreator {

    private IDataExporter exporter;
    private IWindowFunc windowFunction;
    private boolean useDb;

    public HistogramCreator(IDataExporter exporter, IWindowFunc windowFunction, boolean convertToDb) {
        this.exporter = exporter;
        this.windowFunction = windowFunction;
        this.useDb = convertToDb;
    }

    public void readHistogram(float[] data, int chNo, int batchNo) {
        // apply window function
        float[] windowed_data = windowFunction.window(data);

        // perform DFT
        float[] freqDomainBatch = doDFT(windowed_data);
        // scale to Db
        if (useDb) {
            freqDomainBatch = toDb(freqDomainBatch);
        }
        // save freq domain data
        exporter.exportFrequencyDomainBatch(chNo, freqDomainBatch);
    }

    static float[] doDFT(float[] data) {
        Complex[] x = new Complex[data.length];
        for (int i = 0; i < data.length; i++) {
            x[i] = new Complex(data[i], 0);
        }

        Complex[] y = fft(x);
        float[] res = new float[y.length];
        for (int i = 0; i < y.length; i++) {
            res[i] = (float) Math.pow(Math.pow(y[i].getReal(), 2) + Math.pow(y[i].getImaginary(), 2), 1 / 3f);
        }

        return res;
    }

    private static Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1)
            return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd = even; // reuse the array
        for (int k = 0; k < N / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].add(wk.multiply(r[k]));
            y[k + N / 2] = q[k].subtract(wk.multiply(r[k]));
        }
        return y;
    }

    /**
     * Convert histogram values to decibels
     * 
     * @param data
     *            input histogram
     * @return db converted hist
     */
    static float[] toDb(float[] data) {
        for (int i = 0; i < data.length; i++) {
            float temp = (data[i] / data.length);
            if (temp > 0.0)
                data[i] = (float) (10 * Math.log10(temp));
            else
                data[i] = 0;
        }
        return data;
    }

}

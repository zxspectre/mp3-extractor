package su.drei.mp3extr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.jfree.ui.RefineryUtilities;

public class Main {
    public static final int bufferSize = 4096;

    // parameters specifying for what mp3 part histogram is built
    static float total_length=100f;
    static float sample_start=20f;
    static float sample_end=25f;

    static float x_pos;
    static float x_width;

    // array (by channel) of list(batch no) of doubles(histogram itself)...
    static List<double[]>[] freqDHist = null;
    static List<Integer> bytes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String filePath = null;

        //for convenient audio parts spec.
        int preset = 1;
        
        switch (preset) {
        case 1:
            total_length = 161.985f;
            sample_start = 23f;
            sample_end = 25f;
            filePath = "D:\\music\\ZZ Top\\1970 - First Album\\Zz Top - Backdoor Love Affair.mp3";
            break;
        case 2:
            filePath = "D:\\music\\effects\\SDuncan\\SH-8\\neck_cl.mp3";
            break;
        case 3:
            total_length = 106.449f;
            sample_start = 52f;
            sample_end = 53.5f;
            filePath = "D:\\music\\Graveworm\\2001 - Scourge Of Malice\\01 - Dreaded Time.mp3";
            break;
        case 4:
            filePath = "../100hz.mp3";
            break;
        case 5:
            filePath = "../5kHz-10db.mp3";
            break;
        case 6:
            filePath = "../800Hz-10db.mp3";
            break;
        case 7:
            filePath = "../5-11kHz_10db.wav";
            break;
        case 8:
            filePath = "../5-11kHz_fadeout.wav";
            break;
        }

        x_pos = (sample_start + sample_end) / 2 / total_length;
        x_width = (sample_end - sample_start) / total_length;
        readPCM(filePath);
    }

    private static void readPCM(String filename) throws Exception {
        File file = new File(filename);
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);

            // init freq domain histogram variable
            freqDHist = new ArrayList[decodedFormat.getChannels()];
            for (int i = 0; i < decodedFormat.getChannels(); i++) {
                freqDHist[i] = new ArrayList<double[]>();
            }

            process(decodedFormat, din);
        }
    }

    private static void process(AudioFormat decodedFormat, AudioInputStream din) throws Exception {
        final int channelsCount = decodedFormat.getChannels();
        byte[] data = new byte[bufferSize];
        int batchNo = 0;
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            line.start();
            int nBytesRead = 0;
            // loop over all data, until stream is empty
            while (nBytesRead != -1) {
                nBytesRead = 0;
                // try to read data according to buffer length; repeat several times if unread data exists, but buffer was not filled.
                while(nBytesRead != -1 && nBytesRead != data.length){
                    nBytesRead += din.read(data, nBytesRead, data.length - nBytesRead);
                }
                final int framesCnt = nBytesRead / (channelsCount * 2);
                // loop over channels in audio stream
                for (int chNo = 0; chNo < channelsCount; chNo++) {
                    int[] channelFrames = new int[framesCnt];
                    // loop over frames for one channel only
                    for (int pos = 0; pos < framesCnt; pos++) {
                        // get two bytes and glue 'em together
                        int b1 = data[pos * channelsCount * 2 + chNo * 2];
                        int b2 = data[pos * channelsCount * 2 + chNo * 2 + 1];
                        int value;
                        if (decodedFormat.isBigEndian()) {
                            value = (b1 << 8) | (b2 & 0xff);
                        } else {
                            value = (b2 << 8) | (b1 & 0xff);
                        }

                        // save new value to channel specific array
                        channelFrames[pos] = value;
                    }
                    // end of processing frames for one channel, continue with
                    // the by-channel loop
                    // TODO: process last batch
                    if (nBytesRead == data.length) {
                        handlePcmChannel(channelFrames, chNo, batchNo);
                    }
                }
                // finished looping over channels, read next buffer from audio
                // stream
                batchNo++;
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }

        final Plot2D demo = new Plot2D("Line Chart Demo 6", bytes);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

        // System.out.println("Writing frequency domain data");
        // File fdhOutput = new File("fdh.out");
        // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new
        // FileOutputStream(fdhOutput)));
        // for (Integer i : bytes) {
        // writer.write(i + "\n");
        // }
        // writer.close();
        // System.out.println("Writing frequency domain data. Done." +
        // fdhOutput.getAbsolutePath());

        Plot2D demo2 = new Plot2D("Freqdomain 1", toDb(computeLengths(getSum(freqDHist, Math.round(freqDHist[0].size() * x_pos)))), decodedFormat.getSampleRate() / data.length * 2 * decodedFormat.getChannels());
        // Plot2D demo2 = new Plot2D("Freqdomain 1",
        // toDb(computeLengths(freqDHist.get(301))));
        demo2.pack();
        RefineryUtilities.centerFrameOnScreen(demo2);
        demo2.setVisible(true);
    }

    private static void handlePcmChannel(int[] data, int chNo, int batchNo) {
        // TODO: handle only first ~1-2 min of file, remove it after
        if (batchNo < 2300) {
            // save amplitude data
            if (chNo == 1) {
                for (int i = 0; i < data.length; i++) {
                    bytes.add(data[i]);
                }
            }
            // apply window function
            double[] windowed_data = doWindow(data);

            // perform DFT
            freqDHist[chNo].add(doDFT(windowed_data));
            // save freq domain data
            if (batchNo % 100 == 0 && chNo == 0) {
                System.out.println("Handling batch No." + batchNo);
            }
        }
    }

    private static double[] doWindow(int[] data) {
        double[] res = new double[data.length];
        // triangular window function - too noisy
//        for (int i = 0; i < data.length / 2; i++) {
//            res[i] = data[i] * (i / (float) (data.length / 2));
//            res[i + (data.length / 2)] = data[i + (data.length / 2)] * (1.0 - (i / (float) (data.length / 2)));
//        }
        
        //Hamming - looks ok
//        for (int i = 0; i < data.length; i++) {
//            res[i] = data[i] * (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (data.length - 1)));
//        }
        
        //Blackman - 
//        for (int i = 0; i < data.length; i++) {
//            res[i] = data[i] * (0.42 - 0.5 * Math.cos (2 * Math.PI * i / (data.length - 1)) + 0.08 * Math.cos (4 * Math.PI * i / (data.length - 1)));
//        }

        //Blackman - Harris 
        for (int i = 0; i < data.length; i++) {
            res[i] = data[i] * (0.35875 - 0.48829 * Math.cos(2 * Math.PI * i /(data.length-1)) + 0.14128 * Math.cos(4 * Math.PI * i/(data.length-1)) - 0.01168 * Math.cos(6 * Math.PI  * i/(data.length-1)));
        }

        return res;
    }

    private static double[] doDFT(double[] data) {

        Complex[] x = new Complex[data.length];
        for (int i = 0; i < data.length; i++) {
            x[i] = new Complex(data[i], 0);
        }

        Complex[] y = fft(x);
        double[] res = new double[y.length];
        for (int i = 0; i < y.length; i++) {
            res[i] = Math.pow(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2), 1 / 3f);
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
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + N / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    // beware: thrash methods below - in general: process data in order to plot
    // it

    /**
     * Return histogram that is an average of hist's around pos. Vicinity
     * determined by x_width
     * 
     * @param freqDHist
     *            all hist's
     * @param pos
     *            'around 'pos'
     * @return average hist
     */
    private static double[] getSum(List<double[]>[] freqDHist, int pos) {
        double[] res = new double[freqDHist[0].get(0).length];
        int window = Math.round(freqDHist[0].size() * x_width);
        System.out.println("Windows width is " + window);
        // sum over buffer batches
        for (int i = pos - window / 2; i < pos + window / 2; i++) {
            // sum over each buffer
            for (int j = 0; j < freqDHist[0].get(0).length; j++) {
                // sum over each channel
                for (int ch = 0; ch < freqDHist.length; ch++)
                    res[j] += freqDHist[ch].get(i)[j] / window / freqDHist.length;
            }
        }
        return res;
    }

    // 's not needed, as long as FFT method returns real length of coeff-ts, w/o
    // phase info
    private static double[] computeLengths(double[] fdh) {
        /*
         * double[] res = new double[fdh.length / 2]; for (int j = 0; j <
         * fdh.length / 1; j++) { res[j / 2] = (complLength(fdh[j], fdh[j +
         * 1])); j++; }
         * 
         * return res;
         */
        return fdh;
    }

    /**
     * Convert histogram values to decibels
     * 
     * @param data
     *            input histogram
     * @return db converted hist
     */
    private static double[] toDb(double[] data) {
        for (int i = 0; i < data.length; i++) {
            double temp = (data[i] / data.length);
            if (temp > 0.0)
                data[i] = 10 * Math.log10(temp);
            else
                data[i] = 0;
        }
        return data;
    }

}

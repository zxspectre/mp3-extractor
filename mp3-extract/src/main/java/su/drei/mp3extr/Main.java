package su.drei.mp3extr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
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

    static float x_pos = 0.15216018938646675872953245216019f;
    static float x_width = 0.057f;

    static List<double[]> freqDHist = new ArrayList<>();
    static List<Integer> bytes = new ArrayList<Integer>();

    public static void main(String[] args) throws Exception {
//         String filePath = "D:\\music\\effects\\SDuncan\\SH-8\\neck_cl.mp3";
        String filePath = "D:\\music\\ZZ Top\\1970 - First Album\\Zz Top - Backdoor Love Affair.mp3";
//        String filePath = "D:\\music\\Graveworm\\2001 - Scourge Of Malice\\01 - Dreaded Time.mp3";

        // String filePath = "D:\\projects\\03-mp3-extractor\\100hz.mp3";
        readPCM(filePath);
    }

    private static void readPCM(String filename) throws Exception {
        File file = new File(filename);
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            // Play now.

            rawplay(decodedFormat, din);
        }
    }

    private static void rawplay(AudioFormat decodedFormat, AudioInputStream din) throws Exception {
        final int channelsCount = decodedFormat.getChannels();
        byte[] data = new byte[bufferSize];
        int batchNo = 0;
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            line.start();
            int nBytesRead = 0;
            // loop over all data, until stream is empty
            while (nBytesRead != -1) {
                // try to read data according to buffer length
                nBytesRead = din.read(data, 0, data.length);
                final int framesCnt = nBytesRead / (channelsCount * 2);
                // loop over channels in audio stream
                for (int chNo = 0; chNo < channelsCount; chNo++) {
                    int[] channelFrames = new int[framesCnt];
                    // loop over frames for one channel only
                    for (int pos = 0; pos < framesCnt; pos++) {
                        // get two bytes and glue 'em together
                        int b1 = data[pos * channelsCount * 2 + chNo];
                        int b2 = data[pos * channelsCount * 2 + chNo + 1];
                        /*
                         * if (b1 < 0) b1 += 0x100; if (b2 < 0) b2 += 0x100;
                         */
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

        System.out.println("Writing frequency domain data");
        File fdhOutput = new File("fdh.out");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fdhOutput)));
        for (Integer i : bytes) {
            writer.write(i + "\n");
        }
        writer.close();
        System.out.println("Writing frequency domain data. Done." + fdhOutput.getAbsolutePath());

        Plot2D demo2 = new Plot2D("Freqdomain 1", toDb(computeLengths(getSum(freqDHist, Math.round(freqDHist.size() * x_pos)))));
        // Plot2D demo2 = new Plot2D("Freqdomain 1",
        // toDb(computeLengths(freqDHist.get(301))));
        demo2.pack();
        RefineryUtilities.centerFrameOnScreen(demo2);
        demo2.setVisible(true);

    }

    private static void handlePcmChannel(int[] data, int chNo, int batchNo) {
        if (chNo == 0 && batchNo < 2200) {
            // save amplitude data
            for (int i = 0; i < data.length; i++) {
                bytes.add(data[i]);
            }

            // perform DFT
            freqDHist.add(doDFT(data));
            // save freq domain data
            if (batchNo % 100 == 0) {
                System.out.println("Handling batch No." + batchNo);
            }
        }
    }

    private static double[] doDFT(int[] data) {

//         int n = data.length;
//         double f[] = new double[n / 2];
//         for (int j = 0; j < n / 2; j++) {
//        
//         double firstSummation = 0;
//         double secondSummation = 0;
//        
//         for (int k = 0; k < n; k++) {
//         double twoPInjk = ((2 * Math.PI) / n) * (j * k);
//         firstSummation += data[k] * Math.cos(twoPInjk);
//         secondSummation += data[k] * Math.sin(twoPInjk);
//         }
//        
//         f[j] = Math.abs(Math.sqrt(Math.pow(firstSummation, 2) +
//         Math.pow(secondSummation, 2)));
//         }
//        
//               
//         return f;

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

    // beware: thrash methods below

    private static double[] getSum(List<double[]> freqDHist, int pos) {
        double[] res = new double[freqDHist.get(0).length];
        int window = Math.round(freqDHist.size() * x_width);
        System.out.println("Windows width is " + window);
        for (int i = pos - window / 2; i < pos + window / 2; i++) {
            for (int j = 0; j < freqDHist.get(0).length; j++) {
                res[j] += freqDHist.get(i)[j] / window;
            }
        }
        return res;
    }

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

    private static double complLength(double f, double g) {
        return Math.pow(f * f + g * g, 1 / 3f);
    }

    private static double[] toDb(double[] computeLengths) {

        double min = Double.MAX_VALUE;
        for (int i = 0; i < computeLengths.length; i++) {
            computeLengths[i] = Math.log10(computeLengths[i]);
            if (computeLengths[i] < min)
                min = computeLengths[i];
        }
        for (int i = 0; i < computeLengths.length; i++) {
            computeLengths[i] -= min;
        }

        return computeLengths;
    }

}

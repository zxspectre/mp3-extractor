package su.drei.mp3extr;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.jfree.ui.RefineryUtilities;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class Test {

    static float x_pos = 0.45216018938646675872953245216019f;
    static float x_width = 0.057f;

    public static void main(String[] sdsa) throws Exception {

         String filePath = "D:\\music\\effects\\SDuncan\\SH-8\\neck_cl.mp3";
        // String filePath = "D:\\projects\\03-mp3-extractor\\100hz.mp3";
//        String filePath = "D:\\music\\Graveworm\\2001 - Scourge Of Malice\\01 - Dreaded Time.mp3";
        testPlay(filePath);
    }

    public static void testPlay(String filename) throws Exception {
        File file = new File(filename);
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioInputStream din = null;
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
        din = AudioSystem.getAudioInputStream(decodedFormat, in);
        // Play now.
        rawplay(decodedFormat, din);
        in.close();

    }

    private static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Integer step = 1000;
        int midStep = 0;
        List<Byte> bytes = new ArrayList<Byte>();
        List<float[]> freqDHist = new ArrayList<float[]>();
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1) {
                nBytesRead = din.read(data, 0, data.length);

                // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                for (int posB = 0; posB < nBytesRead; posB++) {
                    midStep++;

                    if (midStep == step) {
                        midStep = 0;
                        bytes.add(data[posB]);
                    }

                }

                byte[] fft_in = new byte[data.length / 2];
                for (int j = 0; j < data.length / 4; j++) {
                    data[j * 4] = 0;
                    data[j * 4 + 1] = 0;
                    fft_in[j * 2] = data[j * 4 + 2];
                    fft_in[j * 2 + 1] = data[j * 4 + 3];
                }

                // glue bytes to int
                int[] fft_in2 = new int[fft_in.length / 2];
                for (int j = 0; j < fft_in.length / 2; j++) {
                    ByteBuffer bb = ByteBuffer.wrap(new byte[] { fft_in[j * 2], fft_in[j * 2 + 1] });

                    bb.order(ByteOrder.LITTLE_ENDIAN);

                    // read your integers using ByteBuffer's getInt().
                    // four bytes converted into an integer!
                    fft_in2[j] = bb.getShort();
                }

                if (nBytesRead == data.length) {
                    FloatFFT_1D fft = new FloatFFT_1D(fft_in2.length);
                    float[] fft_res = new float[fft_in2.length * 2];
                    for (int i = 0; i < fft_res.length / 2; i++)
                        fft_res[2 * i] = fft_in2[i];
                    fft.complexForward(fft_res);
                    freqDHist.add(fft_res);
                }

                // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // if (nBytesRead != -1) nBytesWritten = line.write(data, 0,
                // nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }

        // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("Data size = " + bytes.size());
        final Plot2D demo = new Plot2D("Line Chart Demo 6", bytes);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

        Plot2D demo2 = new Plot2D("Freqdomain 1", toDb(computeLengths(getSum(freqDHist, Math.round(freqDHist.size() * x_pos)))));
        // Plot2D demo2 = new Plot2D("Freqdomain 1",
        // toDb(computeLengths((freqDHist.get(301)))));

        demo2.pack();
        RefineryUtilities.centerFrameOnScreen(demo2);
        demo2.setVisible(true);

        System.out.println(freqDHist.size());

        /*
         * System.out.println("Writing frequency domain data"); File fdhOutput =
         * new File("fdh.out"); BufferedWriter writer = new BufferedWriter(new
         * OutputStreamWriter(new FileOutputStream(fdhOutput))); int i = 0;
         * for(float[] fdh: freqDHist){ i++; for(int j=0;j<fdh.length;j++){
         * writer.write(i+" "+(j+1)+" "+(complLength(fdh[j],fdh[j+1]))+"\n");
         * j++; } } writer.close();
         * System.out.println("Writing frequency domain data. Done.");
         */
        // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    private static float[] getSum(List<float[]> freqDHist, int pos) {
        float[] res = new float[freqDHist.get(0).length];
        int window = Math.round(freqDHist.size() * x_width);
        System.out.println("Windows width is " + window);
        for (int i = pos - window / 2; i < pos + window / 2; i++) {
            for (int j = 0; j < freqDHist.get(0).length; j++) {
                res[j] += freqDHist.get(i)[j] / window;
            }
        }
        return res;
    }

    private static double[] computeLengths(float[] fdh) {
        double[] res = new double[fdh.length / 2];
        for (int j = 0; j < fdh.length / 1; j++) {
            res[j / 2] = (complLength(fdh[j], fdh[j + 1]));
            j++;
        }

        return res;
    }

    private static double complLength(float f, float g) {
        return Math.pow(f * f + g * g, 1 / 3f);
    }

    private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

}

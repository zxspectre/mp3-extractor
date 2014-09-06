package su.drei.mp3extr.exporter;

import java.util.List;

import org.jfree.ui.RefineryUtilities;

import su.drei.mp3extr.Plot2D;

public class InMemoryPlottingExporter extends InMemoryExporter {
    private float x_pos, x_width;

    public InMemoryPlottingExporter(float x_pos, float x_width) {
        this.x_pos = x_pos;
        this.x_width = x_width;
    }

    @Override
    public void flush() {
        super.flush();

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

        Plot2D demo2 = new Plot2D("Freqdomain 1", getSum(freqDHist, Math.round(freqDHist[0].size() * x_pos)), sampleRate / freqDHist[0].get(0).length);
        // Plot2D demo2 = new Plot2D("Freqdomain 1",
        // toDb(computeLengths(freqDHist.get(301))));
        demo2.pack();
        RefineryUtilities.centerFrameOnScreen(demo2);
        demo2.setVisible(true);

    }

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
    private float[] getSum(List<float[]>[] freqDHist, int pos) {
        float[] res = new float[freqDHist[0].get(0).length];
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

}

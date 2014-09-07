package su.drei.mp3extr.exporter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.jfree.ui.RefineryUtilities;

import su.drei.mp3extr.Plot2D;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSingle;

public class PcaExporter implements IDataExporter {

    private static final int histogramBatch=100;
    // array (by channel) of list(batch no) of doubles(histogram itself)...
    protected List<float[]>[] freqDHist = null;

    protected float sampleRate;
    protected int channels;

    public void init(float sampleRate, int channels) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        // init freq domain histogram variable
        freqDHist = new ArrayList[channels];
        for (int i = 0; i < channels; i++) {
            freqDHist[i] = new ArrayList<float[]>();
        }

    }

    @Override
    public void exportPcmBatch(int channel, float[] pcm) {
        //not needed for now
        //        if (freqDHist == null) {
        //            throw new RuntimeException("Must init this exporter first");
        //        }
        //        for (int i = 0; i < pcm.length; i++) {
        //            bytes.add(pcm[i]);
        //        }
    }

    @Override
    public void exportFrequencyDomainBatch(int channel, float[] freqDomainBatch) {
        if (freqDHist == null) {
            throw new RuntimeException("Must init this exporter first");
        }
        if (freqDHist[0].size() % 100 == 0 && channel == 0) {
            System.out.println("Handling batch No." + freqDHist[0].size());
        }
        if(freqDHist[channel].size() < 2105)
            freqDHist[channel].add(freqDomainBatch);
        //        if(freqDHist[channel].size() > 128) freqDHist[channel].clear();
    }

    @Override
    public void flush() {
        int bufferSize = freqDHist[0].get(0).length;
        int batchBatch = 0;
        double[][] matrix = new double[freqDHist[0].size() / histogramBatch][bufferSize * histogramBatch];
        try {
            int subBatch = 0;
            double[] batch = new double[bufferSize * histogramBatch];
            for (int batchNo = 0; batchNo < freqDHist[0].size(); batchNo++) {
                for (int chNo = 0; chNo < channels; chNo++) {
                    for (int pos = 0; pos < bufferSize; pos++)
                        batch[pos + subBatch * bufferSize] += freqDHist[chNo].get(batchNo)[pos] / channels;
                }
                if (++subBatch == histogramBatch) {
//                    if(batchBatch == 11){
//                        Plot2D demo2 = new Plot2D("Freqdomain 1", batch, sampleRate / freqDHist[0].get(0).length);
//                        // Plot2D demo2 = new Plot2D("Freqdomain 1",
//                        // toDb(computeLengths(freqDHist.get(301))));
//                        demo2.pack();
//                        RefineryUtilities.centerFrameOnScreen(demo2);
//                        demo2.setVisible(true);
//                    }
                    //write 150 hists in 1 array
                    matrix[batchBatch++] = batch;
                    batch = new double[bufferSize * histogramBatch];
                    subBatch = 0;

                }
            }
            System.out.println(String.format("%s Created double[][]", new Date()));
            RealMatrix x = new BlockRealMatrix(matrix);
            System.out.println(String.format("%s Created RealMatrix x", new Date()));
            RealMatrix sigma = x.transpose().multiply(x).scalarMultiply(1/freqDHist[0].size() / (histogramBatch));
            System.out.println(String.format("%s Created sigma", new Date()));
            EigenDecomposition eige = new EigenDecomposition(sigma);
            System.out.println(String.format("%s Created EigenDecomposition", new Date()));
            double[] realEigenvalues = eige.getRealEigenvalues();
            System.out.println(realEigenvalues );
        } catch (Exception e) {
            e.printStackTrace();
        }
     }

}

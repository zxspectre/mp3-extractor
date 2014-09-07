package su.drei.mp3extr.exporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.ui.RefineryUtilities;

import su.drei.mp3extr.Plot2D;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSingle;

/**
 * Currently it's in-memory in fact
 * @author loki
 *
 */
public class MatFileExporter implements IDataExporter {

    // array (by channel) of list(batch no) of doubles(histogram itself)...
    protected List<float[]>[] freqDHist = null;
    protected List<Float> bytes = new ArrayList<>();

    protected float sampleRate;
    protected int channels;

    @Override
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
        // TODO Auto-generated method stub

    }

    @Override
    public void exportFrequencyDomainBatch(int channel, float[] freqDomainBatch) {
        if (freqDHist == null) {
            throw new RuntimeException("Must init this exporter first");
        }
        if (freqDHist[0].size() % 100 == 0 && channel == 0) {
            System.out.println("Handling batch No." + freqDHist[0].size());
        }
        freqDHist[channel].add(Arrays.copyOf(freqDomainBatch, freqDomainBatch.length / 2));
        //        if(freqDHist[channel].size() > 128) freqDHist[channel].clear();
    }

    @Override
    public void flush() {
        int batchSize = freqDHist[0].get(0).length;
        int batchBatchSize = 500;
        int batchBatch = 0;
        try {
            List<MLArray> list = new ArrayList<MLArray>();
            int subBatch = 0;
            Float[] batch = new Float[batchSize * batchBatchSize];
            clearArray(batch);
            for (int batchNo = 0; batchNo < freqDHist[0].size(); batchNo++) {
                for (int chNo = 0; chNo < channels; chNo++) {
                    for (int pos = 0; pos < batchSize; pos++) {
                        batch[pos + subBatch * batchSize] += freqDHist[chNo].get(batchNo)[pos] / channels;
                    }
                }
                if (++subBatch == batchBatchSize) {
                    if (batchBatch == 11) {
                        Plot2D demo2 = new Plot2D("Freqdomain 1", batch, sampleRate / freqDHist[0].get(0).length);
                        // Plot2D demo2 = new Plot2D("Freqdomain 1",
                        // toDb(computeLengths(freqDHist.get(301))));
                        demo2.pack();
                        RefineryUtilities.centerFrameOnScreen(demo2);
                        demo2.setVisible(true);
                    }
                    //write 150 hists in 1 array
                    MLSingle mlTriple = new MLSingle("batch" + batchBatch++, batch, batchSize*4);
                    list.add(mlTriple);
                    subBatch = 0;
                    batch = new Float[batchSize * batchBatchSize];
                    clearArray(batch);
                }
            }
            new MatFileWriter("mat_file.mat", list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearArray(Float[] batch) {
        for (int i = 0; i < batch.length; i++) {
            batch[i] = 0f;
        }
    }

}

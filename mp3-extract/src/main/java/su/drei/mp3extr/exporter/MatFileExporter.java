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
 * 
 * @author loki
 *
 */
public class MatFileExporter implements IDataExporter {
    
    //If song fragment has average PCM amplitude less that MaxLevel(=Short.MAX_VAL after normalization) * SILENCE_LEVEL, then skip this buffer 
    protected final float SILENCE_LEVEL = 0.1f;


    // array (by channel) of list(batch no) of doubles(histogram itself)...
    protected List<float[]>[] freqDHist = null;
    protected List<Float> bytes = new ArrayList<>();

    protected float sampleRate;
    protected int channels;
    protected String varName;
    
    public MatFileExporter(String varName){
        this.varName=varName;
    }

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
        int batchBatchSize = 125;
        int batchBatch = 0;
        try {
            List<MLArray> list = new ArrayList<MLArray>();
            int subBatch = 0;
            Float[] batch = new Float[batchSize * batchBatchSize];
            clearArray(batch);
            for (int batchNo = 0; batchNo < freqDHist[0].size(); batchNo++) {

                //crude check for silence across channels
                boolean willProcessThisBatch = true;
                for (int chNo = 0; chNo < channels; chNo++) {
                    willProcessThisBatch = willProcessThisBatch && check4Silence(freqDHist[chNo].get(batchNo)); 
                }
                if(!willProcessThisBatch){
                    continue;
                }

                for (int chNo = 0; chNo < channels; chNo++) {
                    for (int pos = 0; pos < batchSize; pos++) {
                        batch[pos + subBatch * batchSize] += freqDHist[chNo].get(batchNo)[pos] / channels;
                    }
                }
                if (++subBatch == batchBatchSize) {
                    //debug code - plot batchBatch
                    //                    if (batchBatch == 11) {
                    //                        Plot2D demo2 = new Plot2D("Freqdomain 1", batch, sampleRate / freqDHist[0].get(0).length);
                    //                        // Plot2D demo2 = new Plot2D("Freqdomain 1",
                    //                        // toDb(computeLengths(freqDHist.get(301))));
                    //                        demo2.pack();
                    //                        RefineryUtilities.centerFrameOnScreen(demo2);
                    //                        demo2.setVisible(true);
                    //                    }

                    //TODO: test code, for now export only the middle of the song
                    if ((int)(freqDHist[0].size() / batchBatchSize / 2) == batchBatch) {
                        //write 150 hists in 1 array
                        MLSingle mlTriple = new MLSingle(varName + batchBatch, batch, batchSize);
                        list.add(mlTriple);
                    }
                    subBatch = 0;
                    batchBatch++;
                    batch = new Float[batchSize * batchBatchSize];
                    clearArray(batch);
                }
            }
            //            System.out.println(String.format("Total lists %s, expected=%s",list.size(), freqDHist[0].size() / batchBatchSize));
            new MatFileWriter("../"+varName+".mat", list);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void clearArray(Float[] batch) {
        for (int i = 0; i < batch.length; i++) {
            batch[i] = 0f;
        }
    }

    /**
     * Detect silence. Should be invoked after normalization and after channel averaging.
     * @param channelFrames input data
     * @return true if data considered a silence, intro or outro, false if it's considered meaningful
     */
    protected boolean check4Silence(float[] channelFrames) {
        float avg = 0;
        for(float f: channelFrames){
            avg += Math.abs(f) / channelFrames.length;
        }
        return avg < SILENCE_LEVEL * Short.MAX_VALUE;
    }

}

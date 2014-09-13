package su.drei.mp3extr.exporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    protected File outputFolder;
    protected int histogramConcatFactor;

    public MatFileExporter(File outputFolder, String varName, int histogramConcatFactor) {
        this.varName = varName;
        this.outputFolder = outputFolder;
        this.histogramConcatFactor = histogramConcatFactor;
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
        freqDHist[channel].add(Arrays.copyOf(freqDomainBatch, freqDomainBatch.length / 2));
    }

    @Override
    public void flush() {
        int batchSize = freqDHist[0].get(0).length;
        int batchSizeCropped = freqDHist[0].size() - (freqDHist[0].size() % histogramConcatFactor);
        
        List<MLArray> list = new ArrayList<MLArray>();
        Float[] outputArray = new Float[batchSize * batchSizeCropped];
        initArray(outputArray);
        for (int batchNo = 0; batchNo < batchSizeCropped; batchNo++) {
            //crude check for silence across channels
            boolean willProcessThisBatch = true;
            for (int chNo = 0; chNo < channels; chNo++) {
                willProcessThisBatch = willProcessThisBatch && check4Silence(freqDHist[chNo].get(batchNo));
            }
            if (!willProcessThisBatch) {
                continue;
            }

            for (int chNo = 0; chNo < channels; chNo++) {
                for (int pos = 0; pos < batchSize; pos++) {
                    outputArray[pos + batchNo * batchSize] += freqDHist[chNo].get(batchNo)[pos] / channels;
                }
            }
        }
        MLSingle mlTriple = new MLSingle(varName, outputArray, batchSize * histogramConcatFactor);
        list.add(mlTriple);
        try {
            new MatFileWriter(new File(outputFolder, varName + ".mat"), list);
        } catch (IOException e) {
            throw new RuntimeException("Error writing .mat file", e);
        }
    }

    private void initArray(Float[] batch) {
        Arrays.fill(batch, Float.valueOf(0));
    }

    /**
     * Detect silence. Should be invoked after normalization and after channel
     * averaging.
     * 
     * @param channelFrames
     *            input data
     * @return true if data considered a silence, intro or outro, false if it's
     *         considered meaningful
     */
    protected boolean check4Silence(float[] channelFrames) {
        float avg = 0;
        for (float f : channelFrames) {
            avg += Math.abs(f) / channelFrames.length;
        }
        return avg < SILENCE_LEVEL * Short.MAX_VALUE;
    }

}

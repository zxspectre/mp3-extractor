package su.drei.mp3extr.exporter;

import java.util.ArrayList;
import java.util.List;

public class InMemoryExporter implements IDataExporter {

    // array (by channel) of list(batch no) of doubles(histogram itself)...
    protected List<double[]>[] freqDHist = null;
    protected List<Double> bytes = new ArrayList<>();

    protected float sampleRate;
    protected int channels;

    public void init(float sampleRate, int channels) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        // init freq domain histogram variable
        freqDHist = new ArrayList[channels];
        for (int i = 0; i < channels; i++) {
            freqDHist[i] = new ArrayList<double[]>();
        }

    }

    @Override
    public void exportPcmBatch(int channel, double[] pcm) {
        if (freqDHist == null) {
            throw new RuntimeException("Must init this exporter first");
        }
        for (int i = 0; i < pcm.length; i++) {
            bytes.add(pcm[i]);
        }
    }

    @Override
    public void exportFrequencyDomainBatch(int channel, double[] freqDomainBatch) {
        if (freqDHist == null) {
            throw new RuntimeException("Must init this exporter first");
        }
        if (freqDHist[0].size() % 100 == 0 && channel == 0) {
            System.out.println("Handling batch No." + freqDHist[0].size());
        }
        freqDHist[channel].add(freqDomainBatch);
    }

    @Override
    public void flush() {
        System.out.println("One dft batch equals to " + freqDHist[0].get(0).length / sampleRate * 1000 + "ms");
        System.out.println("Histograms have " + freqDHist.length + " channels, " + freqDHist[0].size() + " dft batches and " + freqDHist[0].get(0).length + " dft size while having samplRate=" + sampleRate);
    }

}

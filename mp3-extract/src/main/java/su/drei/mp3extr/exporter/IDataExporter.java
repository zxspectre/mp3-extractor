package su.drei.mp3extr.exporter;

public interface IDataExporter {

    void init(float sampleRate, int channels);

    void exportPcmBatch(int channel, int[] pcm);

    void exportFrequencyDomainBatch(int channel, double[] freqDomainBatch);

    void flush();

}

package su.drei.mp3extr.exporter;

public interface IDataExporter {

    void init(float sampleRate, int channels);

    void exportPcmBatch(int channel, float[] pcm);

    void exportFrequencyDomainBatch(int channel, float[] freqDomainBatch);

    void flush();

}

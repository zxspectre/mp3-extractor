package su.drei.mp3extr.impl;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import su.drei.mp3extr.exporter.IDataExporter;
import su.drei.mp3extr.impl.window.BlackmanHarris;

public class Mp3Decoder {

    protected IDataExporter exporter;
    protected final int bufferSize;

    public Mp3Decoder(IDataExporter exporter, int bufferSize) {
        this.exporter = exporter;
        this.bufferSize = bufferSize;
    }

    public void readPCM(String filename) throws Exception {
        File file = new File(filename);
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            process(decodedFormat, din);
        }
    }

    protected void process(AudioFormat decodedFormat, AudioInputStream din) throws Exception {
        long start = System.currentTimeMillis();
        exporter.init(decodedFormat.getSampleRate(), decodedFormat.getChannels());
        HistogramCreator histogrammer = new HistogramCreator(exporter, new BlackmanHarris(), true);
        final int channelsCount = decodedFormat.getChannels();
        byte[] data = new byte[bufferSize];
        int batchNo = 0;
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            
            line.start();
            int nBytesRead = 0;
            int lastBufferRead = 0;
            // loop over all data, until stream is empty
            while (lastBufferRead != -1) {
                nBytesRead = 0;
                // try to read data according to buffer length; repeat several
                // times if unread data exists, but buffer was not filled.
                while (lastBufferRead != -1 && nBytesRead != data.length) {
                    lastBufferRead = din.read(data, nBytesRead, data.length - nBytesRead);
                    nBytesRead += lastBufferRead;
                }
                final int framesCnt = nBytesRead / (channelsCount * 2);
                // loop over channels in audio stream
                for (int chNo = 0; chNo < channelsCount; chNo++) {
                    double[] channelFrames = new double[framesCnt];
                    // loop over frames for one channel only
                    for (int pos = 0; pos < framesCnt; pos++) {
                        // get two bytes and glue 'em together
                        int b1 = data[pos * channelsCount * 2 + chNo * 2];
                        int b2 = data[pos * channelsCount * 2 + chNo * 2 + 1];
                        int value;
                        if (decodedFormat.isBigEndian()) {
                            value = (b1 << 8) | (b2 & 0xff);
                        } else {
                            value = (b2 << 8) | (b1 & 0xff);
                        }

                        // save new value to channel specific array
                        channelFrames[pos] = value;
                    }

                    //TODO: move the code of processing each PCM batch to some exporter, which decides what to do with PCM
                    
                    if (chNo == 1) {
                        // save amplitude data
                        exporter.exportPcmBatch(chNo, channelFrames);
                    }

                    // end of processing frames for one channel, continue with
                    // the by-channel loop
                    if (nBytesRead == data.length) {
                        histogrammer.readHistogram(channelFrames, chNo, batchNo);
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
        System.out.println("Processed in "+(System.currentTimeMillis() - start)+", 15600 +/- 100 old stat");
        exporter.flush();
    }

    protected SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

}

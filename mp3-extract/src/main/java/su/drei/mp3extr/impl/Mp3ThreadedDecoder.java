package su.drei.mp3extr.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import su.drei.mp3extr.exporter.IDataExporter;
import su.drei.mp3extr.impl.window.BlackmanHarris;

/**
 * Stateful implementation
 * 
 * @author loki
 *
 */
public class Mp3ThreadedDecoder extends Mp3Decoder {
    // current portion of data for processing
    byte[] data;
    int batchNo = 0;
    int channelsCount;
    boolean bigEndian;

    public Mp3ThreadedDecoder(IDataExporter exporter, int bufferSize) {
        super(exporter, bufferSize);
    }

    @Override
    protected void process(AudioFormat decodedFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        System.out.println(String.format("Sample rate %s, channels=%s", decodedFormat.getSampleRate(), decodedFormat.getChannels()));

        long start = System.currentTimeMillis();
        // init exporter
        exporter.init(decodedFormat.getSampleRate(), decodedFormat.getChannels());
        // create histogram calculator
        histogrammer = new HistogramCreator(exporter, new BlackmanHarris(), true);
        // init variables
        channelsCount = decodedFormat.getChannels();
        bigEndian = decodedFormat.isBigEndian();
        data = new byte[bufferSize];
        // create thread pool
        ExecutorService pool = Executors.newFixedThreadPool(channelsCount);
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {

            line.start();
            int nBytesRead = 0;
            int lastBufferRead = 0;
            // loop over all data, until stream is empty
            while (nBytesRead != -1) {
                nBytesRead = readIntoBuffer(data, din);
                // skip leftover data
                if (nBytesRead == bufferSize) {
                    List<ChannelHandler> taskList = new ArrayList<>();
                    for (int chNo = 0; chNo < channelsCount; chNo++) {
                        taskList.add(new ChannelHandler(chNo));
                    }
                    try {
                        pool.invokeAll(taskList);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Programmer error 0_0", e);
                    }
                }

                // finished looping over channels, read next buffer from audio
                // stream
                batchNo++;
            }
            // Stop
            pool.shutdown();
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
        System.out.println("Processed in " + (System.currentTimeMillis() - start) + ", 16000 old stat");
        exporter.flush();
    }

    class ChannelHandler implements Callable<Boolean> {
        int chNo;

        public ChannelHandler(int chNo) {
            this.chNo = chNo;
        }

        @Override
        public Boolean call() throws Exception {
            final int framesCnt = bufferSize / (channelsCount * 2);
            // loop over channels in audio stream
            double[] channelFrames = new double[framesCnt];
            // loop over frames for one channel only
            for (int pos = 0; pos < framesCnt; pos++) {
                // get two bytes and glue 'em together
                int b1 = data[pos * channelsCount * 2 + chNo * 2];
                int b2 = data[pos * channelsCount * 2 + chNo * 2 + 1];
                int value;
                if (bigEndian) {
                    value = (b1 << 8) | (b2 & 0xff);
                } else {
                    value = (b2 << 8) | (b1 & 0xff);
                }

                // save new value to channel specific array
                channelFrames[pos] = value;
            }

            // TODO: move the code of processing each PCM batch to some
            // exporter, which decides what to do with PCM

            if (chNo == 1) {
                // save amplitude data
                exporter.exportPcmBatch(chNo, channelFrames);
            }

            // end of processing frames for one channel, continue with
            // the by-channel loop
            histogrammer.readHistogram(channelFrames, chNo, batchNo);
            return true;
        }

    }

}

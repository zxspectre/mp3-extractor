package su.drei.mp3extr.impl;

import java.io.File;
import java.io.IOException;

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
    protected HistogramCreator histogrammer;
    protected final int bufferSize;
    protected boolean preprocessingRequired;
    protected Preprocessor preprocessor;

    public Mp3Decoder(IDataExporter exporter, int bufferSize, boolean preprocess) {
        this.exporter = exporter;
        this.bufferSize = bufferSize;
        this.preprocessingRequired = preprocess;
    }

    public void readPCM(String filename) throws Exception {
        File file = new File(filename);
        if (preprocessingRequired) {
            //try open audio input stream from file
            try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
                preprocess(decodedFormat, din);
            }
        }

        //try open audio input stream from file
        try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            //create decoded input stream
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            if (decodedFormat.getSampleRate() != 44100 || decodedFormat.getChannels() != 2) {
                throw new RuntimeException("Only 44100Hz, 2 channel is supported ATM");
            }
            //process stream
            process(decodedFormat, din);
        }
    }

    //TODO: refactor code 'preprocess' is mostly copy-pasta of 'process'
    private void preprocess(AudioFormat decodedFormat, AudioInputStream din) throws LineUnavailableException, IOException {
        preprocessor = new Preprocessor(decodedFormat);
        final int channelsCount = decodedFormat.getChannels();
        byte[] data = new byte[bufferSize];
        int batchNo = 0;
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            line.start();
            int nBytesRead = 0;
            // loop over all data, until stream is empty
            while (nBytesRead != -1) {
                nBytesRead = readIntoBuffer(data, din);
                final int framesCnt = nBytesRead / (channelsCount * 2);
                // loop over channels in audio stream
                for (int chNo = 0; chNo < channelsCount; chNo++) {
                    int[] channelFrames = new int[framesCnt];
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
                    preprocessor.analyzeDataPart(channelFrames);
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
    }

    protected void process(AudioFormat decodedFormat, AudioInputStream din) throws LineUnavailableException, IOException {
        long start = System.currentTimeMillis();
        final float normalizationScale = preprocessingRequired ? (float) Short.MAX_VALUE / preprocessor.getMax() : 1;
        //init exporter 
        exporter.init(decodedFormat.getSampleRate(), decodedFormat.getChannels());
        //create histogram calculator
        histogrammer = new HistogramCreator(exporter, new BlackmanHarris(), true);
        //init variables
        final int channelsCount = decodedFormat.getChannels();
        byte[] data = new byte[bufferSize];
        int batchNo = 0;
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            line.start();
            int nBytesRead = 0;
            // loop over all data, until stream is empty
            while (nBytesRead != -1) {
                nBytesRead = readIntoBuffer(data, din);

                final int framesCnt = nBytesRead / (channelsCount * 2);
                // loop over channels in audio stream
                for (int chNo = 0; chNo < channelsCount; chNo++) {
                    float[] channelFrames = new float[framesCnt];
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
                        if (normalizationScale > 1.01) {
                            channelFrames[pos] = (float) Math.floor(value * normalizationScale);
                        } else {
                            channelFrames[pos] = value;
                        }
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
                        batchNo++;
                    }

                }
                // finished looping over channels, read next buffer from audio
                // stream
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }

        System.out.println("Processed in " + (System.currentTimeMillis() - start) + ", 16000 old stat, scaled by " + normalizationScale);
        exporter.flush();
    }


    protected int readIntoBuffer(byte[] data, AudioInputStream din) throws IOException {
        int totalBytesRead = 0;
        int lastBufferRead = 0;
        // try to read data according to buffer length; repeat several
        // times if unread data exists, but buffer was not filled.
        while (totalBytesRead != data.length) {
            try {
                lastBufferRead = din.read(data, totalBytesRead, data.length - totalBytesRead);
            } catch (ArrayIndexOutOfBoundsException e) {
                // workaround for javazoom bug (it doesn't like some kind of meta-info in mp3)
                // just skip the rest of the file in this case, as (hopefully) it occurs on the last buffer.
                // System.err.println("[warn] index out of bounds while reading stream batch");
                totalBytesRead = -1;
                break;
            }
            //if last read couldn't get any data, then we can't fill the buffer, abort and return -1;
            if (lastBufferRead == -1) {
                totalBytesRead = -1;
                break;
            }
            totalBytesRead += lastBufferRead;
        }
        return totalBytesRead;
    }

    protected SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

}

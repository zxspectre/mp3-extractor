package su.drei.mp3extr.impl;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

import su.drei.mp3extr.exporter.IDataExporter;
import su.drei.mp3extr.impl.model.ChannelData;
import su.drei.mp3extr.impl.streaming.PcmChannelMapper;
import su.drei.mp3extr.impl.streaming.PcmIterator;
import su.drei.mp3extr.impl.window.BlackmanHarris;
import su.drei.mp3extr.impl.window.IWindowFunc;

public class Mp3StreamingDecoder extends Mp3Decoder{

    public Mp3StreamingDecoder(IDataExporter exporter, int bufferSize) {
        super(exporter, bufferSize);
    }

    @Override
    protected void process(AudioFormat decodedFormat, AudioInputStream din) throws Exception {
        long start = System.currentTimeMillis();
        exporter.init(decodedFormat.getSampleRate(), decodedFormat.getChannels());
        final int channelsCount = decodedFormat.getChannels();
        SourceDataLine line = getLine(decodedFormat);
        if (line != null) {
            line.start();
            final IWindowFunc wind = new BlackmanHarris();
            Spliterator<byte[]> spliteraptor = Spliterators.spliteratorUnknownSize(new PcmIterator(din, bufferSize), Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.CONCURRENT);
            Stream<byte[]> stream = StreamSupport.stream(spliteraptor, false);
            
            stream
                .flatMap(new PcmChannelMapper(channelsCount, decodedFormat.isBigEndian()))
                .map(x -> {exporter.exportPcmBatch(x.channelNo, x.data); return x;})
                .map(x -> {x.data = wind.window(x.data); return x;})
                .map(x -> {x.data = HistogramCreator.doDFT(x.data); return x;})
                .map(x -> {x.data = HistogramCreator.toDb(x.data); return x;})
                .forEach(x -> {exporter.exportFrequencyDomainBatch(x.channelNo, x.data);});
//                .collect(Collectors.groupingBy(x -> x.channelNo, Collectors.reducing(op)));
                
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
        System.out.println("Processed in "+(System.currentTimeMillis() - start)+", 16000 old stat");
        exporter.flush();
    }

}

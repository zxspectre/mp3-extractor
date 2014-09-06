package su.drei.mp3extr.impl.streaming;

import java.util.function.Function;
import java.util.stream.Stream;

import su.drei.mp3extr.impl.model.ChannelData;

public class PcmChannelMapper implements Function<byte[],Stream<ChannelData>>{
    final int channelsCount;
    final boolean bigEndian;
    
    public PcmChannelMapper(int channelsCount, boolean bigEndian){
        this.channelsCount = channelsCount;
        this.bigEndian = bigEndian;
    }

    @Override
    public Stream<ChannelData> apply(byte[] data) {
        ChannelData[] res = new ChannelData[channelsCount];
        final int framesCnt = data.length / (channelsCount * 2);
        // loop over channels in audio stream
        for (int chNo = 0; chNo < channelsCount; chNo++) {
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
            res[chNo] = new ChannelData();
            res[chNo].channelNo = (byte) chNo;
            res[chNo].data = channelFrames;
        }
        return Stream.of(res);
    }
    
}

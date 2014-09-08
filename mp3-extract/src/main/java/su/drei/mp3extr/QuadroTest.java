package su.drei.mp3extr;

import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

public class QuadroTest {
    public static final int BUFFER_SIZE = 4096;

    public static void main(String[] s) throws Exception {
        String filePath = "D:\\music\\Black label society\\Black Label Society - The Blessed Hellride\\Black Label Society - Funeral Bell.mp3";
        String VAR_NAME = "BLS1";
        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        filePath = "D:\\music\\Black label society\\Black Label Society - The Blessed Hellride\\Black Label Society - Destruction Overdrive.mp3";
        VAR_NAME = "BLS2";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        filePath = "D:\\music\\Summoning\\2006 - Oath Bound\\03-summoning-mirdautas_vras-qtxmp3.mp3";
        VAR_NAME = "SUMM";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        //Max amplitude is 9222
        filePath = "D:\\music\\Classical\\Vivaldi\\Vivaldi_Spring2.mp3";
        VAR_NAME = "VIV";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);
    }
}

package su.drei.mp3extr;

import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.exporter.MatFileMiddleExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

public class QuadroTest {
    public static final int BUFFER_SIZE = 4096;

    public static void main(String[] s) throws Exception {
        String filePath = "D:\\music\\Exodus\\2010-Exhibit B - The Human Condition [Ltd.Ed.]\\12 Good Riddance.mp3";
        String VAR_NAME = "A";
        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileMiddleExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        filePath = "D:\\music\\Slayer\\1998 - Diabolus in Musica\\09 - Scrum.mp3";
        VAR_NAME = "B";
        mp3dec = new Mp3ThreadedDecoder(new MatFileMiddleExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        filePath = "D:\\music\\Helloween\\1988 Keeper Of Seventh Keys Part II\\09-I Want Out.mp3";
        VAR_NAME = "C";
        mp3dec = new Mp3ThreadedDecoder(new MatFileMiddleExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);

        //Max amplitude is 9222
        filePath = "D:\\music\\Iron Maiden\\2003 Dance Of Death\\Iron Maiden - 04 - Montsegur.mp3";
        VAR_NAME = "D";
        mp3dec = new Mp3ThreadedDecoder(new MatFileMiddleExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath);
    }
}


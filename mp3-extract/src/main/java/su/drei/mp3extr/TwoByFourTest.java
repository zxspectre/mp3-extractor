package su.drei.mp3extr;

import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

public class TwoByFourTest {
    public static final int BUFFER_SIZE = 4096;

    public static void main(String[] s) throws Exception {
        String filePath1 = "D:\\music\\Manowar\\2002a - Warriors Of The World\\08 - Warriors Of The World.Mp3";
        String VAR_NAME = "A";
        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath1);

        String filePath2 = "D:\\music\\Dream Evil\\The Book Of Heavy Metal 2004\\01_dream_evil_the_book_of_heavy_metal_march_of_the_metalians.mp3";
        VAR_NAME = "B";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath2);

        String filePath3 = "D:\\music\\Motorhead\\2004 - Inferno\\Motorhead - 09 - In The Year Of The Wolf.mp3";
        VAR_NAME = "C";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath3);

        String filePath4 = "D:\\music\\Judas Priest\\2004 - Angel Of Retribution\\06 - Wheels Of Fire.mp3";
        VAR_NAME = "D";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath4);

        
        String filePath5 = "D:\\music\\Iron Maiden\\2006 A Matter of Life and Death\\04-iron_maiden-the_pilgrim.mp3";
        VAR_NAME = "E";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath5);

        String filePath6 = "D:\\music\\Iron Maiden\\1992 Fear Of The Dark\\05 - Childhood's End.mp3";
        VAR_NAME = "F";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath6);

        String filePath7 = "D:\\music\\Helloween\\1991 Pink Bubbles Go Ape\\10-The Chance.mp3";
        VAR_NAME = "G";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath7);

        String filePath8 = "D:\\music\\Gamma Ray\\1989 - Heading For Tommorow\\04 - Space Eater.mp3";
        VAR_NAME = "H";
        mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE, true);
        mp3dec.readPCM(filePath8);

        System.out.println(filePath1);
        System.out.println(filePath2);
        System.out.println(filePath3);
        System.out.println(filePath4);
        System.out.println("-vs-");
        System.out.println(filePath5);
        System.out.println(filePath6);
        System.out.println(filePath7);
        System.out.println(filePath8);
    }
}

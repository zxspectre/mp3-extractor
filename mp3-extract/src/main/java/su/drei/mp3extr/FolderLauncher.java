package su.drei.mp3extr;

import java.io.File;
import java.io.FilenameFilter;

import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

//     java -cp mp3-extract-1.0-SNAPSHOT.jar su.drei.mp3extr.FolderLauncher

//java -jar mp3-extract-1.0-SNAPSHOT-jar-with-dependencies.jar  "D:\projects\05-mp3-data\heavy\in" "D:\projects\05-mp3-data\heavy\out4" "heavy"
//java -jar mp3-extract-1.0-SNAPSHOT-jar-with-dependencies.jar  "D:\projects\05-mp3-data\rock\in" "D:\projects\05-mp3-data\rock\out4" "rock"
//java -jar mp3-extract-1.0-SNAPSHOT-jar-with-dependencies.jar  "D:\projects\05-mp3-data\power\in" "D:\projects\05-mp3-data\power\out4" "power"
public class FolderLauncher {
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
//        args = new String[3];
//        args[0] = "D:\\projects\\05-mp3-data\\heavy\\in";
//        args[1] = "D:\\projects\\05-mp3-data\\heavy\\out";
//        args[2]="heavy";
        if (args.length != 3) {
            System.out.println("Launch with 3 arguments, first is the folder to mp3's, second is an existing folder for storing .mat files and third - prefix for .mat filename");
            return;
        }
        File inputFolder = new File(args[0]);
        File outputFolder = new File(args[1]);
        String varName = args[2];

        if(!outputFolder.exists()){
            outputFolder.mkdirs();
        }
        
        int fileNo = 0;
        for (File f : inputFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File rootDir, String fileName) {
                return fileName.toLowerCase().endsWith(".mp3");
            }
        })) {
            try {
                Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(outputFolder, varName + fileNo, 4), BUFFER_SIZE, true);
                mp3dec.readPCM(f.getAbsolutePath());
                fileNo++;
                System.out.println(fileNo + ": successfully processed "+f.getAbsolutePath());
            } catch (Exception e) {
                System.err.println(String.format("Error handling file %s due to %s", f.getAbsolutePath(), e.getMessage()));
                e.printStackTrace();
            }
        }

    }

}

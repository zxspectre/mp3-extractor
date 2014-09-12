package su.drei.mp3extr;

import java.io.File;

import org.jfree.util.StringUtils;

import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

public class FolderLauncher {

    public static void main(String[] args) {
        if(args.length != 2){
            System.out.println("Launch with 2 arguments, first is the folder to mp3's, second is an existing folder for storing .mat files");
        }
//        StringUtils.class;
//        
//        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(new File("D:\\projects\\05-mp3-data"), VAR_NAME), BUFFER_SIZE, true);
//
//        mp3dec.readPCM(filePath);
    }

}

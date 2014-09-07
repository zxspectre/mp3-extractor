package su.drei.mp3extr;

import java.util.ArrayList;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSingle;

import su.drei.mp3extr.exporter.InMemoryExporter;
import su.drei.mp3extr.exporter.InMemoryPlottingExporter;
import su.drei.mp3extr.exporter.MatFileExporter;
import su.drei.mp3extr.exporter.PcaExporter;
import su.drei.mp3extr.impl.Mp3Decoder;
import su.drei.mp3extr.impl.Mp3ThreadedDecoder;

public class Test {

    public static final int BUFFER_SIZE = 4096;
    
    // parameters specifying for what mp3 part histogram is built
    static float total_length = 100f;
    static float sample_start = 20f;
    static float sample_end = 25f;

    static float x_pos;
    static float x_width;

    public static void main(String[] args) throws Exception {
       
        String filePath = null;

        // for convenient audio parts spec.
        int preset =8;
        String VAR_NAME = "SONG";

        switch (preset) {
        case 1:
            total_length = 161.985f;
            sample_start = 23f;
            sample_end = 25f;
            filePath = "D:\\music\\Black label society\\Black Label Society - The Blessed Hellride\\Black Label Society - Funeral Bell.mp3";
            VAR_NAME = "BLS1";
            break;
        case 2:
            filePath = "D:\\music\\Black label society\\Black Label Society - The Blessed Hellride\\Black Label Society - Destruction Overdrive.mp3";
            VAR_NAME = "BLS2";
            break;
        case 3:
            total_length = 106.449f;
            sample_start = 52f;
            sample_end = 53.5f;
            filePath = "D:\\music\\Summoning\\2006 - Oath Bound\\03-summoning-mirdautas_vras-qtxmp3.mp3";
            VAR_NAME = "SUMM";
            break;
        case 4:
            filePath = "D:\\music\\Classical\\Vivaldi\\Vivaldi_Spring2.mp3";
            VAR_NAME = "VIV";
            break;
        case 5:
            filePath = "D:\\music\\AcDc\\1975 - T.N.T\\05-T.N.T..mp3";
            VAR_NAME = "ACDC";
            break;
        case 6:
            filePath = "D:\\music\\Children Of Bodom\\Children Of Bodom'2002 - 'Hate Crew Deathroll'\\05 - Angels Don't Kill.mp3";
            VAR_NAME = "COB";
            break;
        case 7:
            filePath = "D:\\downloads\\Shakira - Dare (La La La).mp3";
            VAR_NAME = "SHA";
            break;
        case 8:
            filePath = "D:\\downloads\\18-Calyx & Teebee Feat. Kemo - Pure Gold.mp3";
            VAR_NAME = "CAL";
            break;
        }

        x_pos = (sample_start + sample_end) / 2 / total_length;
        x_width = (sample_end - sample_start) / total_length;

//        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new InMemoryPlottingExporter(x_pos, x_width), BUFFER_SIZE);
//        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new PcaExporter(), BUFFER_SIZE);
        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new MatFileExporter(VAR_NAME), BUFFER_SIZE);
        

//        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new InMemoryExporter(), BUFFER_SIZE);

        mp3dec.readPCM(filePath);
        
//        while(true) Thread.sleep(10000);

    }
    
    /** 07/09/2014
     *  
"D:\\music\\Deftones\\1997 - Around The Fur\\10 - Mx + Damone (hidden track).mp3"  - GOOD SAMPLE FOR SILENCE SKIPPING    
Processed in 101248, 16000 old stat
One dft batch equals to 23.219954ms
Histograms have 2 channels, 96417 dft batches and 1024 dft size while having samplRate=44100.0 
     * 
     * 
Rate 44100.0, channels 2 for file D:\music\Accept\1979-Accept\01-Lady Lou.mp3 
One dft batch equals to 23.219954ms
Histograms have 2 channels, 637 dft batches and 1024 dft size while having samplRate=44100.0

Rate 48000.0, channels 2 for file D:\music\Arch Enemy\2014 War Eternal\01. Tempore Nihil Sanat (Prelude in F minor).mp3
One dft batch equals to 21.333334ms
Histograms have 2 channels, 3376 dft batches and 1024 dft size while having samplRate=48000.0

Rate 44100.0, channels 1 for file D:\music\Classical\Frederic Chopin\else\Piano by candlelight (disc - 3) - Classical music - Chopin -.mp3
One dft batch equals to 46.439907ms
Histograms have 1 channels, 2965 dft batches and 2048 dft size while having samplRate=44100.0

Rate 11025.0, channels 1 for file D:\music\Classical\Shalyapin\Staroe_-_Shalyapin_-_Mussorgsky_Boris_Godunov.mp3
One dft batch equals to 185.75963ms
Histograms have 1 channels, 378 dft batches and 2048 dft size while having samplRate=11025.0
     */

}

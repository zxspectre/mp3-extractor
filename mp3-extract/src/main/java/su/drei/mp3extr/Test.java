package su.drei.mp3extr;

import su.drei.mp3extr.exporter.InMemoryPlottingExporter;
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
        int preset = 2;

        switch (preset) {
        case 1:
            total_length = 161.985f;
            sample_start = 23f;
            sample_end = 25f;
            filePath = "D:\\music\\ZZ Top\\1970 - First Album\\Zz Top - Backdoor Love Affair.mp3";
            break;
        case 2:
            filePath = "D:\\music\\Classical\\Shalyapin\\Staroe_-_Shalyapin_-_Mussorgsky_Boris_Godunov.mp3";
            break;
        case 3:
            total_length = 106.449f;
            sample_start = 52f;
            sample_end = 53.5f;
            filePath = "D:\\music\\Graveworm\\2001 - Scourge Of Malice\\01 - Dreaded Time.mp3";
            break;
        case 4:
            filePath = "../100hz.mp3";
            break;
        case 5:
            filePath = "../5kHz-10db.mp3";
            break;
        case 6:
            filePath = "../800Hz-10db.mp3";
            break;
        case 7:
            filePath = "../5-11kHz_10db.wav";
            break;
        case 8:
            filePath = "../5-11kHz_fadeout.wav";
            break;
        }

        x_pos = (sample_start + sample_end) / 2 / total_length;
        x_width = (sample_end - sample_start) / total_length;

        Mp3Decoder mp3dec = new Mp3ThreadedDecoder(new InMemoryPlottingExporter(x_pos, x_width), BUFFER_SIZE);

        mp3dec.readPCM(filePath);

    }
    
    /** 07/09/2014 
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

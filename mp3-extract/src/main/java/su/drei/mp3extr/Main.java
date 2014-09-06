package su.drei.mp3extr;

import su.drei.mp3extr.exporter.InMemoryPlottingExporter;
import su.drei.mp3extr.impl.Mp3Decoder;

public class Main {

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
        int preset = 1;

        switch (preset) {
        case 1:
            total_length = 161.985f;
            sample_start = 23f;
            sample_end = 25f;
            filePath = "D:\\music\\ZZ Top\\1970 - First Album\\Zz Top - Backdoor Love Affair.mp3";
            break;
        case 2:
            filePath = "D:\\music\\effects\\SDuncan\\SH-8\\neck_cl.mp3";
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

        Mp3Decoder mp3dec = new Mp3Decoder(new InMemoryPlottingExporter(x_pos, x_width), BUFFER_SIZE);

        mp3dec.readPCM(filePath);

    }

}

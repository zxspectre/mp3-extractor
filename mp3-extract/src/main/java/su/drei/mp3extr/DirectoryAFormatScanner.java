package su.drei.mp3extr;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class DirectoryAFormatScanner {
    public static final String MUSIC_PATH = "D:\\music";

    public static void main(String[] s) throws IOException, UnsupportedAudioFileException {
        Files.walkFileTree(FileSystems.getDefault().getPath(MUSIC_PATH), new FileVisitor<Path>() {
            private int filesProcessed = 0;
            private Map<Float, File> abnormalFiles = new HashMap<>();

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().toLowerCase().endsWith(".mp3")) {
                    filesProcessed++;
                    File selectedFile = file.toFile();
                    try (AudioInputStream in = AudioSystem.getAudioInputStream(selectedFile)) {
                        AudioFormat baseFormat = in.getFormat();
                        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                        // create decoded input stream
                        AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
                        if (abnormalFiles.get(decodedFormat.getFrameRate() + decodedFormat.getChannels()) == null) {
                            abnormalFiles.put(decodedFormat.getFrameRate() + decodedFormat.getChannels(), file.toFile());
                            System.out.println(String.format("Rate %s, channels %s for file %s", decodedFormat.getSampleRate(), decodedFormat.getChannels(), selectedFile.getPath()));
                        }
                    } catch (UnsupportedAudioFileException | IOException e) {
                        System.out.println(String.format("Can't read file %s", selectedFile.getPath()));
                    }
                    if ((filesProcessed & 4095) == 1) {
//                        System.out.println("Files processed: " + filesProcessed);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Done");
    }

}

package su.drei.mp3extr.impl.streaming;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.sound.sampled.AudioInputStream;

/**
 * Iterates over AudioInputStream, returns raw data as an array buffer of specified length.
 * Will discard any leftover data that can't fill buffer up to its size.  
 * @author loki
 *
 */
public class PcmIterator implements Iterator<byte[]>{
    private final AudioInputStream din;
    private final int bufferSize;
    
    byte[] data;
    
    public PcmIterator(AudioInputStream din, int bufferSize){
        this.din = din;
        this.bufferSize = bufferSize;
    }


    @Override
    public boolean hasNext() {
        if(data== null)
            try { fillBuffer(); }
            catch(IOException ex) { throw new UncheckedIOException(ex); }
          return data != null;
    }




    @Override
    public byte[] next() {
        if(data==null && !hasNext()){
            throw new NoSuchElementException();
        }
        try {
            return data; 
        } finally {
                data=null;
        } 
    }
    
    private void fillBuffer() throws IOException {
        data = new byte[bufferSize];
        int nBytesRead = 0;
        int lastBufferRead = 0;
        while (lastBufferRead != -1 && nBytesRead != bufferSize) {
            lastBufferRead= din.read(data, nBytesRead, bufferSize - nBytesRead);
            nBytesRead +=lastBufferRead;
        }
        if(nBytesRead != bufferSize){
            data = null;
        }
    }

}

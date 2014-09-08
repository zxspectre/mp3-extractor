package su.drei.mp3extr.impl;

import javax.sound.sampled.AudioFormat;

public class Preprocessor {
    private int max = 0;
    private AudioFormat decodedFormat;
    
    public Preprocessor(AudioFormat decodedFormat){
        this.decodedFormat = decodedFormat;
    }
    
    public void analyzeDataPart(int[] data){
        for(int j = 0; j< data.length; j++){
            if(max < Math.abs(data[j])){
//                System.out.println(max);
                max = Math.abs(data[j]);
            }
        }
    }
    
    public int getMax(){
        return max;
    }

}

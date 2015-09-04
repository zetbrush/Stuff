package com.picsart.studio.gifencoder.factory;

/**
 * Created by Arman on 7/6/15.
 */
public interface GifEncoderFactory {
    public  int init(String gifName, int w, int h, int numColors, int quality,int frameDelay);
    public  int addFrame(int[] inArray);
    public  void close();

}

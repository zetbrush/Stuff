package com.gif.gifproj;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.widget.Button;

import com.picsart.studio.gifencoder.GifDrawable;
import com.picsart.studio.gifencoder.GifEncoder;
import com.picsart.studio.gifencoder.GifImageView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends ActionBarActivity {

    static {
        System.loadLibrary("pagif");
    }
    private static final boolean VERBOSE = true;
    private static String TAG = "Encoder..Drain..18";
    Button creatBtn;
    GifImageView gifview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GifDrawable gifFromResource=null;

       // gifview.setImageDrawable(gifFromResource);

        creatBtn = (Button)findViewById(R.id.craetBtn);


    }
    //////

    // OMX.Nvidia.h264.encoder  - COLOR_FormatYUV420Flexible

    ////


    @Override
    protected void onResume() {
        super.onResume();
            createMovie();

            //EncodeAndMux enc = new EncodeAndMux();
       // enc.testEncodeVideoToMp4();
        //MP4Writer wrt = MP4Writer.getInstance().initWithParams();
        //wrt.createGifMovie();

    }

    static int  FRAME_RATE = 15;
    MediaCodec mEncoder;
    int  firstImageIndex;
    int lastImageIndex = 30;
    long durationInNanosec;
    long presentationTime;
    MediaCodec.BufferInfo mBufferInfo;
    Surface mInputSurface;
    MediaMuxer mMuxer;
    int mTrackIndex;
    int mWidth = 640;
    int mHeight =480;
    boolean mMuxerStarted;
    String OUTPUT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    GifDrawable gif = null;
    public static  boolean flag;
    Matrix matrix;
    public void createMovie() {

        final String gifPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.gif";

        File giffile = new File(gifPath);
        int framecount;
        Bitmap current;

        try {
            gif = new GifDrawable(gifPath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final int i[] = new int[1];
        try {

            if (gif.getCurrentFrame().getWidth()/(gif.getCurrentFrame().getHeight()*1f) <4f/3) {
                flag = true;
            }

            prepareEncoder(gif.seekToFrameAndGet(0));
            presentationTime = 0;
            int j = 0;
            framecount = gif.getNumberOfFrames();
            lastImageIndex =framecount;
            for ( i[0] = 0; i[0] <= gif.getNumberOfFrames(); i[0]++) {
               // drainEncoderCTS(false) ;
                durationInNanosec = (long) ((float) 50 * 10000);
                Bitmap bmp = gif.seekToFrameAndGet(i[0]);
                addFrameForVideo(bmp);
                j++;
                if(i[0]==lastImageIndex)break;
                }
            drainEncoderCTS(true);

        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            // release encoder, muxer
            releaseEncoder();

        }
    }

    private static long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 10000000;
        return frameIndex * ONE_BILLION / FRAME_RATE;
    }

    public void addFrameForVideo(Bitmap bmp){
        Bitmap resizedBitmap = null;
        drainEncoderCTS(false);
        durationInNanosec = (long) ( 20 * 10000);
        mWidth = bmp.getWidth();
        mHeight = bmp.getHeight();

        byte[] yuvBuffer;
        int[] btmPixels = new int[0];
        if(flag && bmp.getWidth()%16 !=0) {
            mWidth = bmp.getWidth() / 16 * 16;
            mHeight = bmp.getHeight() / 16 * 16;

            resizedBitmap = Bitmap.createScaledBitmap(bmp, mWidth, mHeight, false);

            btmPixels = new int[resizedBitmap.getByteCount()];
            resizedBitmap.getPixels(btmPixels, 0, mWidth, 0, 0, mWidth, mHeight);
        }else {
            btmPixels = new int[bmp.getByteCount()];
            bmp.getPixels(btmPixels, 0, mWidth, 0, 0, mWidth, mHeight);

        }

        yuvBuffer   = new byte[mWidth * mHeight * 3 / 2];



        GifEncoder.convertToYUV21(btmPixels, yuvBuffer, mWidth, mHeight);
       // getNV21(mWidth, mHeight, bitmapToByteBuffer(bmp));

        ByteBuffer inputBuffer;
        int inputByteBufferIndex = mEncoder.dequeueInputBuffer(1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputBuffer = mEncoder.getInputBuffer(inputByteBufferIndex);
        } else {
            ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
            inputBuffer = inputBuffers[inputByteBufferIndex];
        }

       // int[] pixels = new int[btmPixels.length];
      //  pixels = convertYUV420_NV21toRGB8888(yuvBuffer,mWidth,mHeight);
       // Bitmap bm = Bitmap.createBitmap(pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        inputBuffer.put(yuvBuffer);

        mEncoder.queueInputBuffer(inputByteBufferIndex, 0, inputBuffer.capacity(), presentationTime, MediaCodec.CONFIGURE_FLAG_ENCODE);
        presentationTime += durationInNanosec;
    }



    private void prepareEncoder(Bitmap bmp) {

        mBufferInfo = new MediaCodec.BufferInfo();
      /*  CodecManager.Codecs codecs  = CodecManager.Selector.findCodecsFormMimeType("video/avc",false);
        if(codecs.hardwareCodec!=null){
            try {
                mEncoder = MediaCodec.createByCodecName(codecs.hardwareCodec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        try {
            mEncoder = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        int colorFormat = 21;
        if(flag &&  bmp.getWidth()%16 !=0){
            mWidth = bmp.getWidth()/16 * 16;
            mHeight = bmp.getHeight()/16 *16;

        }else{
            mWidth = bmp.getWidth();
            mHeight = bmp.getHeight();
        }
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,mWidth,mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);


        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mEncoder.start();

        String outputPath = new File(OUTPUT_DIR,
                "test." + gif.getCurrentFrame().getWidth() + "x" +  gif.getCurrentFrame().getHeight() + ".mp4").toString();
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;
    }






    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private void releaseEncoder() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            }catch (Exception e){e.printStackTrace();}
            mMuxer = null;
        }
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        //L.("couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;   // not reached
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test

            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:


                return true;
            default:
                return false;
        }
    }

    private byte[] bitmapToByteBuffer(Bitmap bitmap) {

        ByteBuffer b = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(b);
        byte[] bitmapdata = b.array();

        b.clear();

        return bitmapdata;
    }

    private void drainEncoderCTS(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encodedData = mEncoder.getOutputBuffer(encoderStatus);
                } else {
                     encodedData = encoderOutputBuffers[encoderStatus];
                }
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    private void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;

        if (endOfStream) {
            mEncoder.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            encoderOutputBuffers = mEncoder.getOutputBuffers();
        }
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                } else {
                }

            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                // ignore it
            } else {
                ByteBuffer encodedData = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encodedData = mEncoder.getOutputBuffer(encoderStatus);
                } else {
                    encodedData = encoderOutputBuffers[encoderStatus];
                }

                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                    break;
                }
            }
        }
    }






    /**
     * Extracts all pending data from the encoder.
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */

    byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }


    public static int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
        int size = width*height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for(int i=0, k=0; i < size; i+=2, k+=2) {
            y1 = data[i  ]&0xff;
            y2 = data[i+1]&0xff;
            y3 = data[width+i  ]&0xff;
            y4 = data[width+i+1]&0xff;

            u = data[offset+k  ]&0xff;
            v = data[offset+k+1]&0xff;
            u = u-128;
            v = v-128;

            pixels[i  ] = convertYUVtoRGB(y1, u, v);
            pixels[i+1] = convertYUVtoRGB(y2, u, v);
            pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
            pixels[width+i+1] = convertYUVtoRGB(y4, u, v);

            if (i!=0 && (i+2)%width==0)
                i+=width;
        }

        return pixels;
    }

    private static int convertYUVtoRGB(int y, int u, int v) {
        int r,g,b;

        r = y + (int)1.402f*v;
        g = y - (int)(0.344f*u +0.714f*v);
        b = y + (int)1.772f*u;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (b<<16) | (g<<8) | r;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used
                // obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each
                // sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note
                // the sampling is every other
                // pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255
                        : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0
                            : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0
                            : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }


    public void drainEncoder18(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs);
                    }
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void drainEncoderofMaster(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;

        if (endOfStream) {
            mEncoder.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            encoderOutputBuffers = mEncoder.getOutputBuffers();
        }
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                } else {
                }

            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                // ignore it
            } else {
                ByteBuffer encodedData = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encodedData = mEncoder.getOutputBuffer(encoderStatus);
                }else{
                    encodedData  = encoderOutputBuffers[encoderStatus];
                }

                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                    break;
                }
            }
        }
    }

    private void drainEncoderr(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    /*
    * //asset file
        GifDrawable gifFromAssets = new GifDrawable( getAssets(), "anim.gif" );

        //resource (drawable or raw)
        GifDrawable gifFromResource = new GifDrawable( getResources(), R.drawable.anim );

        //byte array
        byte[] rawGifBytes = ...
        GifDrawable gifFromBytes = new GifDrawable( rawGifBytes );

        //FileDescriptor
        FileDescriptor fd = new RandomAccessFile( "/path/anim.gif", "r" ).getFD();
        GifDrawable gifFromFd = new GifDrawable( fd );

        //file path
        GifDrawable gifFromPath = new GifDrawable( "/path/anim.gif" );

        //file
        File gifFile = new File(getFilesDir(),"anim.gif");
        GifDrawable gifFromFile = new GifDrawable(gifFile);

        //AssetFileDescriptor
        AssetFileDescriptor afd = getAssets().openFd( "anim.gif" );
        GifDrawable gifFromAfd = new GifDrawable( afd );

        //InputStream (it must support marking)
        InputStream sourceIs = ...
        BufferedInputStream bis = new BufferedInputStream( sourceIs, GIF_LENGTH );
        GifDrawable gifFromStream = new GifDrawable( bis );

        //direct ByteBuffer
        ByteBuffer rawGifBytes = ...
        GifDrawable gifFromBytes = new GifDrawable( rawGifBytes );
    *
    * */
}

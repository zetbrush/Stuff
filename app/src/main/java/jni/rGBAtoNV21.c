#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_streamingopentok_polozmuguch_convertRGBAToNV21(JNIEnv *env, jobject claz, jintArray srcBuffer, jbyteArray dstBuffer, jint width, jint height);

#ifdef __cplusplus
}
#endif
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "polozmukuch", __VA_ARGS__)

void encodeYUV420SP(JNIEnv *env, jobject claz, jintArray srcBuffer, jbyteArray dstBuffer, int width, int height) {
        jint* argbData = (jint*) (*env)->GetIntArrayElements(env, srcBuffer, NULL);
	    jbyte* yuvData = (jbyte*) (*env)->GetByteArrayElements(env, dstBuffer, NULL);

        const int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        int j;

        for ( j = 0; j < height; j++) {
            int i;
            for (i = 0; i < width; i++) {


                a = ((argbData[index] & 0xff000000) >> 24); // a is not used obviously
                B = ((argbData[index] & 0xff0000) >> 16);
                G = ((argbData[index] & 0xff00) >> 8);
                R = (argbData[index] & 0xff) >> 0;
                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;
                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.

                yuvData[yIndex++] = ((jbyte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y)));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuvData[uvIndex++] = (jbyte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuvData[uvIndex++] = (jbyte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }
                index ++;
            }
        }

        (*env)->ReleaseIntArrayElements(env, srcBuffer, argbData, 0);
        (*env)->ReleaseByteArrayElements(env, dstBuffer, yuvData, 0);
    }

JNIEXPORT void JNICALL Java_com_streamingopentok_polozmuguch_convertRGBAToNV21(JNIEnv *env, jobject claz, jintArray srcBuffer, jbyteArray dstBuffer, jint width, jint height) {
    encodeYUV420SP(env, claz, srcBuffer, dstBuffer, width, height);
}
#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

extern "C" JNIEXPORT jstring JNICALL
Java_com_mind_open_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_MainActivity_processImage(JNIEnv *env, jobject thiz, jlong matAddr) {
    cv::Mat &image = *(cv::Mat *) matAddr;
    // 灰度处理
    cv::cvtColor(image, image, cv::COLOR_RGB2GRAY);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_MainActivity_processImage2(JNIEnv *env, jobject thiz, jlong matAddr,
                                              jlong matDst) {
    cv::Mat &image = *(cv::Mat *) matAddr;
    cv::Mat &dst = *(cv::Mat *) matDst;
    // 灰度处理
    cv::cvtColor(image, dst, cv::COLOR_RGB2GRAY);
}
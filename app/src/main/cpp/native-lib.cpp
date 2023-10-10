#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include "rknn_api.h"
#include "rknn_food.h"

static std::string jstringToStdString(JNIEnv *env, jstring jstr) {
    if (jstr == nullptr) {
        return "";
    }

    const char *c_str = env->GetStringUTFChars(jstr, nullptr);
    std::string result(c_str);
    env->ReleaseStringUTFChars(jstr, c_str);

    return result;
}

static char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);

    if (alen > 0) {
        rtn = new char[alen + 1];
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_mind_open_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello;
    hello = std::to_string(test());

    //  std::string hello = "Hello from C++";
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

    // cv::resize(dst,dst,cv::Size(720, 1280), 0, 0, cv::INTER_LINEAR);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_MainActivity_initFoodModel(JNIEnv *env, jobject thiz, jstring model_path) {
    char *model_path_p = jstringToChar(env, model_path);
    return initFoodModel(model_path_p);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_MainActivity_foodInStorage(JNIEnv *env, jobject thiz, jlong native_obj_addr,
                                              jstring name) {
//    char *img_name = jstringToChar(env,name);
//   return foodStorage(native_obj_addr,img_name);

    return 0;

}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_MainActivity_foodInStorages(JNIEnv *env, jobject thiz, jlong native_obj_addr,
                                               jstring name, jstring path) {

    char *img_name = jstringToChar(env, name);
    const std::string &filePath = jstringToStdString(env, path);
    return foodStorage(native_obj_addr, img_name, filePath);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_MainActivity_readMatMemory(JNIEnv *env, jobject thiz, jstring path) {
    const std::string &filePath = jstringToStdString(env, path);
    readMatVectorsFromXML(filePath);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_MainActivity_foodRecognize(JNIEnv *env, jobject thiz, jlong native_obj_addr) {
    recognize(native_obj_addr);
}
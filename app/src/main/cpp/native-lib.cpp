#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include "rknn_api.h"
#include "rknn_food.h"
#include "yolo_image.h"
#include "object_tracker/track_link.h"

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

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_mind_open_MainActivity_stringFromJNI(
//        JNIEnv *env,
//        jobject /* this */) {
//
//    std::string hello;
//    hello = std::to_string(test());
//
//    //  std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_mind_open_MainActivity_processImage(JNIEnv *env, jobject thiz, jlong matAddr) {
//    cv::Mat &image = *(cv::Mat *) matAddr;
//    // 灰度处理
//    cv::cvtColor(image, image, cv::COLOR_RGB2GRAY);
//}
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_mind_open_MainActivity_processImage2(JNIEnv *env, jobject thiz, jlong matAddr,
//                                              jlong matDst) {
//    cv::Mat &image = *(cv::Mat *) matAddr;
//    cv::Mat &dst = *(cv::Mat *) matDst;
//    // 灰度处理
//    cv::cvtColor(image, dst, cv::COLOR_RGB2GRAY);
//
//    // cv::resize(dst,dst,cv::Size(720, 1280), 0, 0, cv::INTER_LINEAR);
//}
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_mind_open_MainActivity_initFoodModel(JNIEnv *env, jobject thiz, jstring model_path) {
//    char *model_path_p = jstringToChar(env, model_path);
//    return initFoodModel(model_path_p);
//}
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_mind_open_MainActivity_foodInStorage(JNIEnv *env, jobject thiz, jlong native_obj_addr,
//                                              jstring name) {
////    char *img_name = jstringToChar(env,name);
////   return foodStorage(native_obj_addr,img_name);
//
//    return 0;
//
//}
//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_mind_open_MainActivity_foodInStorages(JNIEnv *env, jobject thiz, jlong native_obj_addr,
//                                               jstring name, jstring path) {
//
//    char *img_name = jstringToChar(env, name);
//    const std::string &filePath = jstringToStdString(env, path);
//    return foodStorage(native_obj_addr, img_name, filePath);
//
//}
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_mind_open_MainActivity_readMatMemory(JNIEnv *env, jobject thiz, jstring path) {
//    const std::string &filePath = jstringToStdString(env, path);
//    readMatVectorsFromXML(filePath);
//}
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_mind_open_MainActivity_foodRecognize(JNIEnv *env, jobject thiz, jlong native_obj_addr) {
//    recognize(native_obj_addr);
//}


/**
 * 初始化检测模型
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_RKnnJni_initDetectModel(JNIEnv *env, jobject thiz, jint im_height, jint im_width,
                                           jint im_channel, jstring model_path) {
    char *model_path_p = jstringToChar(env, model_path);
    return create(im_height, im_width, im_channel, model_path_p);
}
/**
 * 反初始化检测模型
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_RKnnJni_deInitDetectModel(JNIEnv *env, jobject thiz) {
    destroy();
}

/**
 * 运行检测模型
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_RKnnJni_detectRun(JNIEnv *env, jobject thiz, jbyteArray in,
                                     jbyteArray grid0Out, jbyteArray grid1Out,
                                     jbyteArray grid2Out) {
    jboolean inputCopy = JNI_FALSE;
    jbyte *const inData = env->GetByteArrayElements(in, &inputCopy);

    jboolean outputCopy = JNI_FALSE;

    jbyte *const y0 = env->GetByteArrayElements(grid0Out, &outputCopy);
    jbyte *const y1 = env->GetByteArrayElements(grid1Out, &outputCopy);
    jbyte *const y2 = env->GetByteArrayElements(grid2Out, &outputCopy);

    run_yolo((char *) inData, (char *) y0, (char *) y1, (char *) y2);

    env->ReleaseByteArrayElements(in, inData, JNI_ABORT);
    env->ReleaseByteArrayElements(grid0Out, y0, 0);
    env->ReleaseByteArrayElements(grid1Out, y1, 0);
    env->ReleaseByteArrayElements(grid2Out, y2, 0);

    return 0;

}

/**
 * 检测模型数据处理
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_RKnnJni_detectPostProcess(JNIEnv *env, jobject thiz, jbyteArray grid0_out,
                                             jbyteArray grid1_out, jbyteArray grid2_out,
                                             jintArray ids, jfloatArray scores, jfloatArray boxes) {
    jint detect_counts;
    jboolean inputCopy = JNI_FALSE;
    jbyte *const grid0_buf = env->GetByteArrayElements(grid0_out, &inputCopy);
    jbyte *const grid1_buf = env->GetByteArrayElements(grid1_out, &inputCopy);
    jbyte *const grid2_buf = env->GetByteArrayElements(grid2_out, &inputCopy);

    jboolean outputCopy = JNI_FALSE;

    jint *const y0 = env->GetIntArrayElements(ids, &outputCopy);
    jfloat *const y1 = env->GetFloatArrayElements(scores, &outputCopy);
    jfloat *const y2 = env->GetFloatArrayElements(boxes, &outputCopy);

    detect_counts = yolo_post_process((char *) grid0_buf, (char *) grid1_buf, (char *) grid2_buf,
                                      (int *) y0, (float *) y1, (float *) y2);

    env->ReleaseByteArrayElements(grid0_out, grid0_buf, JNI_ABORT);
    env->ReleaseByteArrayElements(grid1_out, grid1_buf, JNI_ABORT);
    env->ReleaseByteArrayElements(grid2_out, grid2_buf, JNI_ABORT);
    env->ReleaseIntArrayElements(ids, y0, 0);
    env->ReleaseFloatArrayElements(scores, y1, 0);
    env->ReleaseFloatArrayElements(boxes, y2, 0);


    return detect_counts;
}

/**
 * 创建检测模型追踪句柄
 */
extern "C"
JNIEXPORT jlong JNICALL
Java_com_mind_open_RKnnJni_detectCreateHandle(JNIEnv *env, jobject thiz) {
    return create_tracker();
}

/**
 * 销毁检测模型追踪句柄
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_RKnnJni_detectHandlerDestroy(JNIEnv *env, jobject thiz, jlong handle) {
    destroy_tracker(handle);
}

/**
 * 检测模型追踪
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_mind_open_RKnnJni_detectTrack(
        JNIEnv *env, jobject obj,
        jlong handle,
        jint maxTrackLifetime,
        jint track_input_num,
        jfloatArray track_input_locations,
        jintArray track_input_class,
        jfloatArray track_input_score,
        jintArray track_output_num,
        jfloatArray track_output_locations,
        jintArray track_output_class,
        jfloatArray track_output_score,
        jintArray track_output_id,
        jint width,
        jint height) {
    jboolean inputCopy = JNI_FALSE;
    jfloat *const c_track_input_locations = env->GetFloatArrayElements(track_input_locations,
                                                                       &inputCopy);
    jint *const c_track_input_class = env->GetIntArrayElements(track_input_class, &inputCopy);
    jfloat *const c_track_input_score = env->GetFloatArrayElements(track_input_score, &inputCopy);
    jboolean outputCopy = JNI_FALSE;

    jint *const c_track_output_num = env->GetIntArrayElements(track_output_num, &outputCopy);
    jfloat *const c_track_output_locations = env->GetFloatArrayElements(track_output_locations,
                                                                        &outputCopy);
    jint *const c_track_output_class = env->GetIntArrayElements(track_output_class, &outputCopy);
    jfloat *const c_track_output_score = env->GetFloatArrayElements(track_output_score, &inputCopy);
    jint *const c_track_output_id = env->GetIntArrayElements(track_output_id, &outputCopy);


    track(handle, (int) maxTrackLifetime,
          (int) track_input_num, (float *) c_track_input_locations, (int *) c_track_input_class,
          (float *) c_track_input_score,
          (int *) c_track_output_num, (float *) c_track_output_locations,
          (int *) c_track_output_class, (float *) c_track_output_score,
          (int *) c_track_output_id, (int) width, (int) height);

    env->ReleaseFloatArrayElements(track_input_locations, c_track_input_locations, JNI_ABORT);
    env->ReleaseIntArrayElements(track_input_class, c_track_input_class, JNI_ABORT);
    env->ReleaseFloatArrayElements(track_input_score, c_track_input_score, JNI_ABORT);

    env->ReleaseIntArrayElements(track_output_num, c_track_output_num, 0);
    env->ReleaseFloatArrayElements(track_output_locations, c_track_output_locations, 0);
    env->ReleaseIntArrayElements(track_output_class, c_track_output_class, 0);
    env->ReleaseFloatArrayElements(track_output_score, c_track_output_score, 0);
    env->ReleaseIntArrayElements(track_output_id, c_track_output_id, 0);

}

/**
 * 检测模型数据转换与翻转
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_mind_open_RKnnJni_detectConvertAndFlip(JNIEnv *env, jobject thiz, jbyteArray src,
                                                jint src_fmt, jbyteArray dst, jint dst_fmt,
                                                jint width, jint height, jint flip) {
    jboolean copy = JNI_FALSE;
    jbyte *src_buf = env->GetByteArrayElements(src, &copy);
    jbyte *dst_buf = env->GetByteArrayElements(dst, &copy);

    jint ret = colorConvertAndFlip(src_buf, src_fmt, dst_buf, dst_fmt, width, height, flip);

    env->ReleaseByteArrayElements(src, src_buf, 0);
    env->ReleaseByteArrayElements(dst, dst_buf, 0);

    return ret;
}



/**
 * 菜品识别
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_mind_open_RKnnJni_foodRecognition(JNIEnv *env, jobject thiz, jlong mat_addr) {
    const std::pair<double, std::string> &pair = recognize(mat_addr);
    LOGI("=================recognize============Pair: %f, %s", pair.first, pair.second.c_str());
    jclass foodInfoClass = env->FindClass("com/mind/open/yolo/FoodRegInfo");
    if (foodInfoClass == nullptr) {
        return nullptr;
    }
    jmethodID personConstructor = env->GetMethodID(foodInfoClass, "<init>",
                                                   "(DLjava/lang/String;)V");
    if (personConstructor == nullptr) {
        return nullptr;  // 找不到构造函数
    }
    jstring name = env->NewStringUTF(pair.second.c_str());
    jobject food = env->NewObject(foodInfoClass, personConstructor, pair.first, name); //
    // 释放字符串对象
    env->DeleteLocalRef(name);
    return food;
}



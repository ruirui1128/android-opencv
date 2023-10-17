#include <jni.h>
#include <string>


extern "C" JNIEXPORT jstring

JNICALL
Java_com_mind_make_TestJni_getStringFromJni(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
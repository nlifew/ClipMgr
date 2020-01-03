

#include "clipmgr.h"
#include "log.h"

namespace clipmgr {
#undef LOG_TAG
#define LOG_TAG "clipmgr.cpp"

#define CLIPMGR_JAR     "/system/framework/riru_clipmgr.jar"
#define CLIPMGR_CLASS   "cn.nlifew.clipmgr.RiruCall"

static jclass mClipMgrClass = nullptr;

static void installJavaRuntime(JNIEnv *env)
{
    if (mClipMgrClass != nullptr) {
        return;
    }
    // 获取 SystemClassLoader
    jclass classLoader = env->FindClass("java/lang/ClassLoader");
    jmethodID getSystemLoader = env->GetStaticMethodID(classLoader,
            "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
    jobject systemClassLoader = env->CallStaticObjectMethod(classLoader,
            getSystemLoader);
    if (systemClassLoader == nullptr) {
        LOGE("I can\'t get SystemClassLoader");
        return;
    }

    // 构造我们自己的 ClassLoader
    jclass pathClassLoader = env->FindClass("dalvik/system/PathClassLoader");
    jmethodID initPathClassLoader = env->GetMethodID(pathClassLoader, "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    jobject myClassLoader = env->NewObject(pathClassLoader, initPathClassLoader,
            env->NewStringUTF(CLIPMGR_JAR), NULL, systemClassLoader);
    if (myClassLoader == nullptr) {
        LOGE("instance PathClassLoader failed");
        return;
    }

    // 加载我们自己的类
    jmethodID loadClass = env->GetMethodID(pathClassLoader,
            "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    auto cls = env->CallObjectMethod(myClassLoader,
            loadClass, env->NewStringUTF(CLIPMGR_CLASS));

    if (cls == nullptr) {
        LOGE("failed to loadClass: " CLIPMGR_CLASS);
        return;
    }
    mClipMgrClass = (jclass) env->NewGlobalRef(cls);
}

void onPrepareForkAndSpecialize(JNIEnv *env, jstring appDataDir)
{
    LOGI("onPrepareForkAndSpecialize: start");
    installJavaRuntime(env);
    if (mClipMgrClass != nullptr) {
        jmethodID id = env->GetStaticMethodID(mClipMgrClass,
                "onPrepareForkAndSpecialize", "(Ljava/lang/String;)V");
        env->CallStaticVoidMethod(mClipMgrClass, id, appDataDir);
        env->ExceptionClear();
    }
}

int onFinishForkAndSpecialize(JNIEnv *env)
{
    LOGI("onFinishForkAndSpecialize: start");
    installJavaRuntime(env);
    if (mClipMgrClass != nullptr) {
        jmethodID id = env->GetStaticMethodID(mClipMgrClass,
                "onFinishForkAndSpecialize", "()V");
        env->CallStaticVoidMethod(mClipMgrClass, id);
        env->ExceptionClear();
    }
    return 0;
}

void onPrepareForkSystemServer(JNIEnv *env)
{
    LOGI("onPrepareForkSystemServer: start");
    installJavaRuntime(env);
    if (mClipMgrClass != nullptr) {
        jmethodID id = env->GetStaticMethodID(mClipMgrClass,
                "onPrepareForkSystemServer", "()V");
        env->CallStaticVoidMethod(mClipMgrClass, id);
        env->ExceptionClear();
    }
}

int onFinishForkSystemServer(JNIEnv *env)
{
    LOGI("onFinishForkSystemServer: start");
    installJavaRuntime(env);
    if (mClipMgrClass != nullptr) {
        jmethodID id = env->GetStaticMethodID(mClipMgrClass,
                "onFinishForkSystemServer", "()V");
        env->CallStaticVoidMethod(mClipMgrClass, id);
        env->ExceptionClear();
    }
    return 0;
}
}
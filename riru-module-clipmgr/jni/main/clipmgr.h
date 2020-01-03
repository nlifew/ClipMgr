
#ifndef _CLIPMGR_H
#define _CLIPMGR_H

#include <jni.h>
#include <sys/types.h>

namespace clipmgr {
    void onPrepareForkAndSpecialize(JNIEnv *env, jstring appDataDir);

    int onFinishForkAndSpecialize(JNIEnv *env);

    void onPrepareForkSystemServer(JNIEnv *env);

    int onFinishForkSystemServer(JNIEnv *env);
}

#endif


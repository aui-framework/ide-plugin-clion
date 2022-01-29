#pragma once

#include <jni.h>
#include "converter.hpp"

#define CALLED_FROM_JAVA(returnType) extern "C" JNIEXPORT returnType JNICALL

namespace cjg {
    JavaVM*& vm() {
        static JavaVM* vm = nullptr;
        return vm;
    }

    JNIEnv* env_of_current_native_thread() {
        thread_local JNIEnv* storage = [] {
            JNIEnv* env;
            if (vm()->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) != JNI_OK) {
                assert(("failed to attach a threadpool thread to jvm" && false));
            }
            return env;
        }();
        return storage;
    }
}
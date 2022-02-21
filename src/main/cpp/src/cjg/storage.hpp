#pragma once

#include <jni.h>

namespace cjg {
    JavaVM*& vm() {
        static JavaVM* vm = nullptr;
        return vm;
    }

    JNIEnv*& env() {
        thread_local JNIEnv* storage;
        return storage;
    }
}
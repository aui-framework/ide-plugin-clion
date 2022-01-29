#pragma once

#include <jni.h>
#include <string>
#include <cassert>

namespace cjg {
    template<typename T>
    struct converter {};

    template<>
    struct converter<std::string> {
        using java_t = jstring;
        static std::string from_java(JNIEnv* env, java_t v) {
            assert(("string is null" && v != nullptr));
            jboolean isCopy;
            auto pCode = env->GetStringUTFChars(v, &isCopy);
            std::string code = pCode;
            env->ReleaseStringUTFChars(v, pCode);
            return code;
        }
    };

    template<typename C, typename J>
    static C from_java(JNIEnv* env, J j) {
        return converter<C>::from_java(env, j);
    }
}
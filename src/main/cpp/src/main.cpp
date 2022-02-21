
#include "JniHelper.hpp"

CALLED_FROM_JAVA(jint) JNI_OnLoad(JavaVM* vm, void* reserved) {
    cjg::vm() = vm;
    JNIEnv* env;
    vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_2);
    return JNI_VERSION_1_2;
}


#include <jni.h>
#include <cassert>
#include "AUIViewContainer.h"
#include "AUIPreviewToolWindow.h"

#define CALLED_FROM_JAVA(returnType) extern "C" JNIEXPORT returnType JNICALL


template<typename T>
struct non_null_lateinit {
    T value;
    non_null_lateinit() {

    }
    non_null_lateinit(T value): value(value) {
            assert(("this value couldn't be null" && value != nullptr));
    }

    operator T() {
        return value;
    }
};

template<typename T>
struct non_null: non_null_lateinit<T> {
    non_null(T value): non_null_lateinit<T>(value) {}
};


using myClazz = non_null_lateinit<jclass>;
using myMethod = non_null_lateinit<jmethodID>;
using myField = non_null_lateinit<jfieldID>;

struct JAUIViewContainer {
    myClazz clazz;

    myField mNativePtr;


    void init(JNIEnv* env) {
        clazz = env->FindClass("com/github/aui/clion/aui/AUIViewContainer");
        mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");
    }
} _gJAUIViewContainer;

CALLED_FROM_JAVA(jint) JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_2);
    _gJAUIViewContainer.init(env);
    return JNI_VERSION_1_2;
}



CALLED_FROM_JAVA(void) Java_com_github_aui_clion_aui_AUIViewContainer_nInit(JNIEnv* env, jobject obj) {
    env->SetLongField(obj, _gJAUIViewContainer.mNativePtr, reinterpret_cast<jlong>(new AUIViewContainer));
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_aui_AUIViewContainer_nSetSize(JNIEnv* env, jobject obj, jint width, jint height) {
    auto auiViewContainer = reinterpret_cast<AUIViewContainer*>(env->GetLongField(obj, _gJAUIViewContainer.mNativePtr));
    auiViewContainer->setViewportSize(width, height);
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_init(JNIEnv* env, jobject obj) {
    auto auiViewContainer = reinterpret_cast<AUIViewContainer*>(env->GetLongField(obj, _gJAUIViewContainer.mNativePtr));
}
CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_sendCodeToCppBackend(JNIEnv* env, jobject obj, jstring jCode) {
    auto auiViewContainer = reinterpret_cast<AUIViewContainer*>(env->GetLongField(obj, _gJAUIViewContainer.mNativePtr));
    jboolean isCopy;
    auto pCode = env->GetStringUTFChars(jCode, &isCopy);
    std::string code = pCode;
    env->ReleaseStringUTFChars(jCode, pCode);
    auiViewContainer->setContainer(_new<AUIPreviewToolWindow>(code));
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_aui_AUIViewContainer_nRender(JNIEnv* env, jobject obj, jfloat uiScale, jintArray buffer) {
    auto auiViewContainer = reinterpret_cast<AUIViewContainer*>(env->GetLongField(obj, _gJAUIViewContainer.mNativePtr));
    if (auiViewContainer->requiresRedraw()) {
        auiViewContainer->setCustomDpiRatio(uiScale);
        auto img = auiViewContainer->render();
        size_t count = img.getData().size() / sizeof(jint);
        jboolean isCopy = false;
        auto src = reinterpret_cast<glm::u8vec4*>(img.getData().data());
        auto pBuffer = env->GetIntArrayElements(buffer, &isCopy);
        auto dst = reinterpret_cast<glm::u8vec4*>(pBuffer);
        auto dstEnd = dst + count;
        for (auto p = dst; p != dstEnd; ++p, ++src) {
            glm::u8vec4 value = *src;
            *p = glm::u8vec4{value.z, value.y, value.x, value.a};
        }
        env->ReleaseIntArrayElements(buffer, pBuffer, false);
    }
}

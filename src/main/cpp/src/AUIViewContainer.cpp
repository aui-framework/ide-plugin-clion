//
// Created by Alex2772 on 1/24/2022.
//

#include "AUIViewContainer.h"
#include <AUI/Util/UIBuildingHelpers.h>
#include <AUI/View/AButton.h>
#include "JniHelper.hpp"

using namespace ass;

struct JAUIViewContainer {
    myClazz clazz;
    myField mNativePtr;

    void init(JNIEnv* env) {
        clazz = (jclass)env->NewGlobalRef(env->FindClass("com/github/aui/clion/aui/AUIViewContainer"));
        mNativePtr = env->GetFieldID(clazz, "mNativePtr", "J");
    }
} _gJAUIViewContainer;

AUIViewContainer::AUIViewContainer(JNIEnv* env, jobject object) : mEnv(env), mObject(object) {
    setContainer(Stacked {
            _new<ALabel>("no preview available") with_style { TextColor { 0x80808080_argb }, FontRendering::ANTIALIASING }
    });
}

void AUIViewContainer::onNotifyProcessMessages() {

}

AUIViewContainer* AUIViewContainer::byObject(JNIEnv* env, jobject object) {
    return reinterpret_cast<AUIViewContainer*>(env->GetLongField(object, _gJAUIViewContainer.mNativePtr));
}


CALLED_FROM_JAVA(void) Java_com_github_aui_clion_aui_AUIViewContainer_nInit(JNIEnv* env, jobject obj) {
    do_once {
        _gJAUIViewContainer.init(env);
    }
    env->SetLongField(obj, _gJAUIViewContainer.mNativePtr, reinterpret_cast<jlong>(new AUIViewContainer(env, obj)));
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_aui_AUIViewContainer_nSetSize(JNIEnv* env, jobject obj, jint width, jint height) {
    AUIViewContainer::byObject(env, obj)->setViewportSize(width, height);
}
CALLED_FROM_JAVA(jboolean) Java_com_github_aui_clion_aui_AUIViewContainer_nRender(JNIEnv* env, jobject obj, jfloat uiScale, jintArray buffer) {
    cjg::env() = env;
    ALogger::info("nRender");
    auto auiViewContainer = reinterpret_cast<AUIViewContainer*>(env->GetLongField(obj, _gJAUIViewContainer.mNativePtr));
    auiViewContainer->getWindow()->getThread()->processMessages();
    AThread::current()->processMessages();
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
        return true;
    }
    return false;
}
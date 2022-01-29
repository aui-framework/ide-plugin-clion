
#include <cassert>
#include <AUI/Logging/ALogger.h>
#include "AUIViewContainer.h"
#include "AUIPreviewUIToolWindow.h"
#include "JniHelper.hpp"


struct JAUIPreviewToolWindow {
    myClazz clazz;
    myField mNativePtr;


    void init(JNIEnv* env) {
        clazz = (jclass)env->NewGlobalRef(env->FindClass("com/github/aui/clion/toolWindow/AUIPreviewToolWindow"));
        mNativePtr = env->GetFieldID(clazz, "mPreviewNativePtr", "J");
    }
} _gJAUIPreviewToolWindow;

CALLED_FROM_JAVA(jint) JNI_OnLoad(JavaVM* vm, void* reserved) {
    cjg::vm() = vm;
    JNIEnv* env;
    vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_2);
    _gJAUIPreviewToolWindow.init(env);
    return JNI_VERSION_1_2;
}



CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nInit(JNIEnv* env, jobject obj) {

}
CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nParseLayoutCode(JNIEnv* env,
                                                                                                      jobject obj,
                                                                                                      jstring layoutCodeToRender) {
    auto previewToolWindow = reinterpret_cast<AUIPreviewUIToolWindow*>(env->GetLongField(obj, _gJAUIPreviewToolWindow.mNativePtr));
    if (!previewToolWindow) {
        auto auiViewContainer = AUIViewContainer::byObject(env, obj);
        auto previewToolWindowP =_new<AUIPreviewUIToolWindow>();
        auiViewContainer->setContainer(previewToolWindowP);
        env->SetLongField(obj, _gJAUIPreviewToolWindow.mNativePtr, reinterpret_cast<jlong>(previewToolWindow = previewToolWindowP.get()));
    }
    previewToolWindow->parseLayout(cjg::from_java<std::string>(env, layoutCodeToRender));
}


// com.github.aui.clion.toolWindow.AUIPreviewToolWindow.nSetStylesheetCode
CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nSetStylesheetCode(JNIEnv* env, jobject obj, jstring stylesheet) {

}


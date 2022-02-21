//
// Created by Alex2772 on 1/25/2022.
//

#include "AUIPreviewUIToolWindow.h"
#include "AUIViewContainer.h"
#include "JniHelper.hpp"
#include <AUI/Util/UIBuildingHelpers.h>
#include <AUI/View/AButton.h>
#include <AUI/Preview/APreview.h>
#include <AUI/Preview/Cpp/Cpp.h>
#include <AUI/IO/AStringStream.h>
#include <AUI/Preview/View/StyleWrapperContainer.h>
#include <AUI/Preview/Visitor/Layout/LayoutVisitor.h>
#include <AUI/Preview/Visitor/Layout/ViewVisitor.h>
#include <AUI/Preview/Visitor/Style/StyleVisitor.h>
#include <AUI/IO/AByteBufferInputStream.h>
#include <AUI/Util/AViewProfiler.h>

using namespace ass;

AUIPreviewUIToolWindow::AUIPreviewUIToolWindow() {
    setContents(Stacked {
        Stacked { mWrapper = _new<StyleWrapperContainer>() with_style { BackgroundSolid { 0xffffff_rgb }, BoxShadow { 0, 4_dp, 14_dp, -4_dp, 0xb0000000_argb } } }
    });
}

AUIPreviewUIToolWindow::~AUIPreviewUIToolWindow() {
}

void AUIPreviewUIToolWindow::beforeObjectRemoval() {
    AObject::beforeObjectRemoval();
}

void AUIPreviewUIToolWindow::parseLayout(const AString& code) {
    try {
        Autumn::put(_new<Runtime::Context>());
        auto ast = Cpp::parseExpression(code);
        if (!ast) return;
        getWindow()->getThread()->enqueue([this, ast = std::move(ast)]() {
            ViewVisitor visitor;
            ast->acceptVisitor(visitor);
            mWrapper->setLayout(_new<AStackedLayout>());
            if (auto view = visitor.getView()) {
                view->setExpanding();
                mWrapper->addView(view);
                mWrapper->setVisibility(Visibility::VISIBLE);
            } else {
                mWrapper->setVisibility(Visibility::GONE);
            }
            mWrapper->getParent()->setCustomAss({ });
            getWindow()->flagUpdateLayout();
            updateHighlightedView();
            redraw();
        });
    } catch (...) {
        /*
        getWindow()->getThread()->enqueue([&] {
            mWrapper->getParent()->setCustomAss({ Opacity { 0.7f } });
            redraw();
        });*/
    }
}

void AUIPreviewUIToolWindow::updateHighlightedView() {
    auto& views = mWrapper->getViews();
    if (views.empty()) return;
    _<AView> highlightedView = views.first();
    {
        for (auto it = mIndexPath.begin(); it != mIndexPath.end(); ++it) {
            if (auto c = _cast<AViewContainer>(highlightedView)) {
                if (*it < c->getViews().size()) {
                    highlightedView = c->getViews()[*it];
                    continue;
                }
            }
            highlightedView = nullptr;
            break;
        }
    }
    mHighlightedView = std::move(highlightedView);
    redraw();
}



void AUIPreviewUIToolWindow::parseStylesheet(const std::string& stylesheetCode) {
    try {
        Autumn::put(_new<Runtime::Context>());
        auto ast = Cpp::parseCode(_new<AStringStream>(stylesheetCode));
        getWindow()->getThread()->enqueue([this, ast = std::move(ast)]() {
            StyleVisitor v;
            ast->visit(v);
            auto s = v.getStylesheet();
            mWrapper->setStylesheet(s);
            getWindow()->flagUpdateLayout();
            redraw();
        });
    } catch (const AException& e) {
        ALogger::err("Stylesheet: " + e.getMessage());
    }
}

struct JAUIPreviewToolWindow {
    myClazz clazz;
    myField mNativePtr;


    void init(JNIEnv* env) {
        clazz = (jclass)env->NewGlobalRef(env->FindClass("com/github/aui/clion/toolWindow/AUIPreviewToolWindow"));
        mNativePtr = env->GetFieldID(clazz, "mPreviewNativePtr", "J");
    }
} _gJAUIPreviewToolWindow;


struct JIAssetProvider {
    myClazz clazz;
    myMethod provideAsset;


    void init(JNIEnv* env) {
        clazz = (jclass)env->NewGlobalRef(env->FindClass("com/github/aui/clion/util/IAssetProvider"));
        provideAsset = env->GetMethodID(clazz, "provideAsset", "(Ljava/lang/String;Ljava/lang/String;)[B");
    }
} _gIAssetProvider;

AUIPreviewUIToolWindow* AUIPreviewUIToolWindow::byObject(JNIEnv* env, jobject obj) {
    auto previewToolWindow = reinterpret_cast<AUIPreviewUIToolWindow*>(env->GetLongField(obj, _gJAUIPreviewToolWindow.mNativePtr));
    if (!previewToolWindow) {
        auto auiViewContainer = AUIViewContainer::byObject(env, obj);
        auto previewToolWindowP =_new<AUIPreviewUIToolWindow>();
        auiViewContainer->setContainer(previewToolWindowP);
        env->SetLongField(obj, _gJAUIPreviewToolWindow.mNativePtr, reinterpret_cast<jlong>(previewToolWindow = previewToolWindowP.get()));
    }
    return previewToolWindow;
}

void AUIPreviewUIToolWindow::render() {
    AViewContainer::render();
    if (mHighlightedView) {
        auto posInWindow = mHighlightedView->getPositionInWindow();
        const int OFFSET = 2;
        Render::drawRectBorder(ASolidBrush{ 0xc0ED6AFF_argb }, posInWindow - glm::ivec2(OFFSET), mHighlightedView->getSize() + glm::ivec2(OFFSET * 2), 2.f);
    }
}


CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nInit(JNIEnv* env, jobject obj) {
    cjg::env() = env;
    _gJAUIPreviewToolWindow.init(env);
    _gIAssetProvider.init(env);
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nParseLayoutCode(JNIEnv* env,
jobject obj, jstring layoutCodeToRender) {
    cjg::env() = env;

    AUIPreviewUIToolWindow::byObject(env, obj)->parseLayout(cjg::from_java<std::string>(env, layoutCodeToRender));
}
CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nUpdateIndices(JNIEnv* env,
jobject obj, jintArray indexPath) {
    cjg::env() = env;
    AVector<int> myIndexPath;
    if (indexPath) {
        jboolean copy;
        auto iArray = env->GetIntArrayElements(indexPath, &copy);
        size_t len = env->GetArrayLength(indexPath);
        myIndexPath = { iArray, iArray + len };
        env->ReleaseIntArrayElements(indexPath, iArray, JNI_ABORT);
    }
    AUIPreviewUIToolWindow::byObject(env, obj)->setHighlightedView(myIndexPath);
}


// com.github.aui.clion.toolWindow.AUIPreviewToolWindow.nSetStylesheetCode
CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nSetStylesheetCode(JNIEnv* env, jobject obj, jstring stylesheet) {
    // ! called from pooled thread !
    cjg::env() = env;
    AUIPreviewUIToolWindow::byObject(env, obj)->parseStylesheet(cjg::from_java<std::string>(env, stylesheet));
}

CALLED_FROM_JAVA(void) Java_com_github_aui_clion_toolWindow_AUIPreviewToolWindow_nSetAssetProvider(JNIEnv* env, jobject obj, jobject assetProvider) {
    // ! called from pooled thread !
    cjg::env() = env;
    cjg::ref r = assetProvider;
    AUrl::registerResolver("preview", [providerImpl = std::move(r)](const AUrl& url) -> _<IInputStream> {
        AUrl wrappedUrl = url.getPath();
        assert(("env is not provided for the current thread" && cjg::env() != nullptr));
        auto result = cjg::env()->CallObjectMethod(providerImpl,
                                                      _gIAssetProvider.provideAsset,
                                                      cjg::to_java<std::string>(cjg::env(), wrappedUrl.getProtocol().toStdString()),
                                                      cjg::to_java<std::string>(cjg::env(), wrappedUrl.getPath().toStdString()));
        if (result) {
            jboolean copy;
            auto bytes = cjg::env()->GetByteArrayElements(static_cast<jbyteArray>(result), &copy);
            AByteBuffer myBuffer;
            myBuffer.write(reinterpret_cast<const char*>(bytes), cjg::env()->GetArrayLength(static_cast<jarray>(result)));
            cjg::env()->ReleaseByteArrayElements(static_cast<jbyteArray>(result), bytes, JNI_COMMIT);
            return _new<AByteBufferInputStream>(std::move(myBuffer));
        }
        return nullptr;
    });
}

#pragma once


#include "AUIViewContainer.h"
#include <AUI/Thread/AFuture.h>
#include <cjg/cjg.hpp>

class StyleWrapperContainer;

class AUIPreviewUIToolWindow: public AViewContainer {
private:
    _<StyleWrapperContainer> mWrapper;
    _<AView> mHighlightedView;
    cjg::ref mAssetProvider;
    AVector<int> mIndexPath;

    void updateHighlightedView();
public:
    AUIPreviewUIToolWindow();
    void parseLayout(const AString& code);
    void setHighlightedView(AVector<int> indexPath) {
        mIndexPath = std::move(indexPath);
        updateHighlightedView();
    }

    ~AUIPreviewUIToolWindow() override;
    void beforeObjectRemoval() override;

    static AUIPreviewUIToolWindow* byObject(JNIEnv* env, jobject obj);

    void render() override;

    void parseStylesheet(const std::string& stylesheetCode);

};



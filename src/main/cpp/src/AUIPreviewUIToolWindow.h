#pragma once


#include "AUIViewContainer.h"
#include <AUI/Thread/AFuture.h>

class StyleWrapperContainer;

class AUIPreviewUIToolWindow: public AViewContainer {
private:
    _<StyleWrapperContainer> mWrapper;

public:
    AUIPreviewUIToolWindow();
    void parseLayout(const AString& code);

    ~AUIPreviewUIToolWindow() override;

    void beforeObjectRemoval() override;
};



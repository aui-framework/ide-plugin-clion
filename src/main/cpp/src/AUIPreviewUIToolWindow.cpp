//
// Created by Alex2772 on 1/25/2022.
//

#include "AUIPreviewUIToolWindow.h"
#include "AUIViewContainer.h"
#include <AUI/Util/UIBuildingHelpers.h>
#include <AUI/View/AButton.h>
#include <AUI/Preview/APreview.h>
#include <AUI/Preview/Cpp/Cpp.h>
#include <AUI/IO/AStringStream.h>
#include <AUI/Preview/View/StyleWrapperContainer.h>
#include <AUI/Preview/Visitor/Layout/LayoutVisitor.h>
#include <AUI/Preview/Visitor/Layout/ViewVisitor.h>

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

//
// Created by Alex2772 on 1/24/2022.
//

#include "AUIViewContainer.h"
#include <AUI/Util/UIBuildingHelpers.h>
#include <AUI/View/AButton.h>

using namespace ass;

AUIViewContainer::AUIViewContainer() {
    setContainer(Stacked {
        Stacked{
            Vertical {
                _new<AButton>("Hello from AUI"),
                _new<AButton>("RED") with_style {BackgroundSolid{0xff0000_rgb}},
                _new<AButton>("GREEN") with_style {BackgroundSolid{0x00ff00_rgb}},
                _new<AButton>("BLUE") with_style {BackgroundSolid{0x0000ff_rgb}},
            }
        } with_style { BackgroundSolid { 0xffffff_rgb }, BoxShadow { 0, 4_dp, 14_dp, -4_dp, 0xb0000000_argb } }
    });
}

#define with_style ^
int f() {
    getBody()->addView(
        Vertical {
            _new<ALabel>("1"),
            Horizontal {
                _new<AButton>("2"),
                _new<AButton>("3").connect(&AView::clicked, me::close)
            } with_style { Padding{ 6_dp, {} } },
        }
    );
}
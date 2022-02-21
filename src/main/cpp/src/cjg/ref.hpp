#pragma once

#include <jni.h>
#include "storage.hpp"

namespace cjg {
    class ref {
    private:
        jobject mValue;

    public:
        ref(): mValue(nullptr) {}

        ref(jobject value): mValue(cjg::env()->NewGlobalRef(value)) {

        }
        ~ref() {
            if (mValue) cjg::env()->DeleteGlobalRef(mValue);
        }
        ref(const ref& other): mValue(cjg::env()->NewGlobalRef(other.mValue)) {}
        ref (ref&& other) noexcept : mValue(other.mValue) {
            other.mValue = nullptr;
        }

        operator jobject() const {
            return mValue;
        }
    };
}
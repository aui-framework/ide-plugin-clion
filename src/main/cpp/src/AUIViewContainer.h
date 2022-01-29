#pragma once

#include <AUI/Platform/ASoftwareEmbedAuiWrap.h>
#include <cjg/cjg.hpp>

class AUIViewContainer: public ASoftwareEmbedAuiWrap {
private:
    JNIEnv* mEnv;
    jobject mObject;

public:
    AUIViewContainer(JNIEnv* env, jobject object);

protected:
    void onNotifyProcessMessages() override;

public:
    static AUIViewContainer* byObject(JNIEnv* env, jobject object);
};



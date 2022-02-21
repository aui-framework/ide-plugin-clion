#pragma once

#include <jni.h>
#include "converter.hpp"
#include "ref.hpp"
#include "storage.hpp"

#define CALLED_FROM_JAVA(returnType) extern "C" JNIEXPORT returnType JNICALL

# Standard routine
cmake_minimum_required(VERSION 3.16)
project(aui_clion)


if (NOT JDK_DIR)
    message(FATAL_ERROR "Please specify JDK_DIR (the one containing bin/, conf/, include/ dirs)")
endif()

set(JDK_INCLUDE_DIRECTORIES ${JDK_DIR}/include)

if (NOT EXISTS ${JDK_INCLUDE_DIRECTORIES})
    message(FATAL_ERROR "Your JDK_DIR does not contain include/ dir.")
endif()

if (WIN32)
    list(APPEND JDK_INCLUDE_DIRECTORIES "${JDK_INCLUDE_DIRECTORIES}/win32")
endif()

if (UNIX AND NOT APPLE)
    list(APPEND JDK_INCLUDE_DIRECTORIES "${JDK_INCLUDE_DIRECTORIES}/linux")
endif()

find_library(JDK_JVM_LIBRARY jvm PATHS ${JDK_DIR}/lib)

if (NOT JDK_JVM_LIBRARY)
    message(FATAL_ERROR "Your JDK does not contain JVM library.")
endif()

add_library(JDK::JVM SHARED IMPORTED)
set_target_properties(JDK::JVM PROPERTIES
                      IMPORTED_LOCATION "${JDK_JVM_LIBRARY}"
                      IMPORTED_IMPLIB "${JDK_JVM_LIBRARY}"
                      INTERFACE_INCLUDE_DIRECTORIES "${JDK_INCLUDE_DIRECTORIES}"
                      INTERFACE_LINK_LIBRARIES "${JDK_JVM_LIBRARY}")

message(STATUS "Using JDK_DIR=${JDK_DIR}")

set(AUI_BRANCH develop)

set(AUIB_AUI_AS ON CACHE BOOL "")
set(BUILD_SHARED_LIBS OFF CACHE BOOL "")

# Use AUI.Boot
file(
        DOWNLOAD
        "https://raw.githubusercontent.com/aui-framework/aui/${AUI_BRANCH}/aui.boot.cmake"
        ${CMAKE_CURRENT_BINARY_DIR}/aui.boot.cmake)
include(${CMAKE_CURRENT_BINARY_DIR}/aui.boot.cmake)

# import AUI
auib_import(AUI https://github.com/aui-framework/aui
            VERSION ${AUI_BRANCH}
            COMPONENTS core views)
auib_import(aui.preview https://github.com/Alex2772/AUI.Preview
            ADD_SUBDIRECTORY)

set_target_properties(aui.preview.library PROPERTIES INTERFACE_AUI_WHOLEARCHIVE TRUE)

# Create the executable. This function automatically links all sources from the src/ folder, creates CMake target and
# places the resulting executable to bin/ folder.
aui_module(aui_clion FORCE_SHARED EXPORT aui_clion)

# Link required libs
aui_link(aui_clion PRIVATE aui::core aui::views JDK::JVM aui.preview.library)

# naming
set(OS_ARCH ${CMAKE_SYSTEM_PROCESSOR})
string(TOLOWER ${OS_ARCH} OS_ARCH)
set_target_properties(aui_clion PROPERTIES OUTPUT_NAME "aui_clion-${OS_ARCH}")



cmake_minimum_required(VERSION 3.18.1)



project("jnitestproject")

set(OpenCV_DIR E:/Code/opencv-4.5.5-android-sdk/OpenCV-android-sdk/sdk/native/jni/${ANDROID_ABI})
find_package(OpenCV REQUIRED)
LINK_DIRECTORIES( ${OpenCV_INCLUDE_DIRS})

add_library( # Sets the name of the library.
        jnitestproject

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


target_link_libraries( # Specifies the target library.
        jnitestproject

        ${OpenCV_LIBS}
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
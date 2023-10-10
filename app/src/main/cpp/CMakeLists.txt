# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)


project("open")

include_directories(${OpenCV_DIR}/jni/include)
include_directories(src/main/cpp/rga)
add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)


add_library(${CMAKE_PROJECT_NAME} SHARED

        native-lib.cpp
        rknn_food.cc

#        post_process.cc
#        yolo_image.cc


        rknn_api.h
        object_tracker/track_link.cc
        object_tracker/objects_tracker.cc
        object_tracker/objects_update.cc

        )

target_link_libraries(${CMAKE_PROJECT_NAME}
        lib_opencv

        ${OpenLib_DIR}/src/main/jniLibs/${ANDROID_ABI}/librknnrt.so
        ${OpenLib_DIR}/src/main/jniLibs/${ANDROID_ABI}/librga.so

        android
        log
        )
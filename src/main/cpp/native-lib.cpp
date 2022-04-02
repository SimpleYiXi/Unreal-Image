#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"
#include <iostream>
#include<vector>
#include <android/log.h>

#define  LOG_TAG    "MYHAARDETECTION"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace std;


void whiteFace(Mat& matSelfPhoto,int alpha, int beta)
{
    for (int y = 0; y < matSelfPhoto.rows; y++)
    {
        for (int x = 0; x < matSelfPhoto.cols; x++)
        {
            for (int c = 0; c < 3; c++)
            {
                matSelfPhoto.at<Vec3b>(y, x)[c] = saturate_cast<uchar>(alpha*(matSelfPhoto.at<Vec3b>(y, x)[c]) + beta);
            }
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_jnitestproject_MainActivity2_simpleBeautify(JNIEnv *env, jobject thiz, jlong srcAddress,
                                                            jlong dstAddress,jlong kernelAddress) {
        Mat matResult;
        int bilateralFilterVal = 30;  // 双边模糊系数
        Mat& src = *(Mat*)srcAddress;//创建输入参数的引用，修改src等同于修改src_address，二者内存地址相同。
        Mat& dst = *(Mat*)dstAddress;
        whiteFace(reinterpret_cast<Mat &>(src), 1.2, 20);  // 调整对比度与亮度，参数2为对比度，参数3为亮度
        cv::GaussianBlur(src, src, Size(9, 9), 0, 0); // 高斯模糊，消除椒盐噪声
        cv::bilateralFilter(src, matResult, bilateralFilterVal, // 整体磨皮
                        bilateralFilterVal * 2, bilateralFilterVal / 2);

        Mat matFinal;
        Mat& kernel = *(Mat*)kernelAddress;
        // 图像增强，使用非锐化掩蔽（Unsharpening Mask）方案。
        cv::GaussianBlur(matResult, matFinal, cv::Size(0, 0), 9);
        cv::addWeighted(matResult, 1.2, matFinal, -0.2, 0, matFinal);
        cv::filter2D(matFinal, dst, -1, kernel);
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_jnitestproject_MainActivity2_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = CV_VERSION;
    cv::Mat a;
    return env->NewStringUTF(hello.c_str());

}
extern"C" {
//extern methods end here
}

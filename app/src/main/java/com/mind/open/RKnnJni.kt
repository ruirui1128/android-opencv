package com.mind.open

import com.mind.open.yolo.FoodRegInfo

/**
 * create by Rui on 2023-10-13
 * desc:
 */
class RKnnJni private constructor() {
    companion object {
        init {
            System.loadLibrary("open")
        }

        val jni by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { RKnnJni() }
    }

    /**
     * 初始化检测模型
     */
    external fun initDetectModel(
        imHeight: Int,
        imWidth: Int,
        imChannel: Int,
        modelPath: String
    ): Int


    /**
     * 反初始化检测模型
     */
    external fun deInitDetectModel()

    /**
     * 检测运行检测模型
     */
    external fun detectRun(
        inData: ByteArray,
        grid0Out: ByteArray,
        grid1Out: ByteArray,
        grid2Out: ByteArray
    ): Int


    /**
     * 检测模型数据处理
     */
    external fun detectPostProcess(
        grid0Out: ByteArray, grid1Out: ByteArray, grid2Out: ByteArray,
        ids: IntArray, scores: FloatArray, boxes: FloatArray
    ): Int

    /**
     * 创建检测模型追踪句柄
     */
    external fun detectCreateHandle(): Long

    /**
     *销毁检测模型追踪句柄
     */
    external fun detectHandlerDestroy(mHandle: Long);

    /**
     * 检测模型追踪
     */
    external fun detectTrack(
        mHandle: Long,
        mMaxTrackLifetime: Int,
        track_input_num: Int,
        track_input_location: FloatArray,
        track_input_class: IntArray,
        track_input_score: FloatArray,
        track_output_num: IntArray,
        track_output_location: FloatArray,
        track_output_class: IntArray,
        track_output_score: FloatArray,
        track_output_id: IntArray,
        mWidth: Int,
        mHeight: Int
    )

    /**
     * 检测模型数据转换与翻转
     */
    external fun detectConvertAndFlip(
        src: ByteArray,
        srcFmt: Int,
        dst: ByteArray,
        dstFmt: Int,
        width: Int,
        height: Int,
        flip: Int
    ): Int


    /**
     * 菜品识别
     */
    external fun foodRecognition(matAddr: Long): FoodRegInfo?


}
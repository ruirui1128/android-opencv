package com.mind.open.rkcamera

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mind.open.R
import java.io.File
import java.io.FileOutputStream

/**
 * create by Rui on 2023-10-13
 * desc:
 */
class RkPresenter(val activity: Activity) {

    companion object {
        private const val TAG = "RkPresenter"

        // 芯片类型
        private const val CHIP_TYPE = "rk3588"

        // 菜品检测模型文件名称
        private const val DETECTION_FILE_NAME = "yolov5s.rknn"

        // 菜品识别模型文件名称
        private const val RECOGNIZE_FILE_NAME = "food.rknn"

        // 菜品特征值文件名称
        private const val FEATURE_FILE_NAME = "cp_xml.xml"

    }

    /**
     * 获取检测模型文件路径
     */
    fun getDetectFilePath() = activity.cacheDir.absolutePath + "/" + DETECTION_FILE_NAME

    /**
     * 获取识别模型文件路径
     */
    fun getRecognizeFilePath() = activity.cacheDir.absolutePath + "/" + RECOGNIZE_FILE_NAME

    /**
     * 获取特征值文件路径
     */
    fun getFeatureFilePath() = activity.cacheDir.absolutePath + "/" + FEATURE_FILE_NAME

    /**
     * 创建特征值文件
     */
    private fun createFeatureFile() {
        val dir = File(activity.cacheDir.absolutePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val featureFile = File(getFeatureFilePath())
        if (!featureFile.exists()) {
            featureFile.createNewFile()
        }
    }

    /**
     * 加载模型平台
     * 1 - 判断芯片类型
     * 2 - 创建特征值文件，并将模型复制到缓存目录中
     */
    fun loadPlatForm(): Boolean {
        val platform = getPlatform()
        if (platform == CHIP_TYPE) {
            return (createModelFile(DETECTION_FILE_NAME, R.raw.yolov5s_rk3588)
                    &&
                    createModelFile(RECOGNIZE_FILE_NAME, R.raw.model_epoch_v3))
        }
        return false
    }


    /**
     * 创建模型文件（将模型文件从资源文件copy到缓存目录中）
     */
    private fun createModelFile(fileName: String, id: Int): Boolean {
        createFeatureFile()
        val filePath = activity.cacheDir.absolutePath + "/" + fileName
        val file = File(filePath)
        try {
            if (!file.exists() || isFirstRun()) {
                val ins = activity.resources.openRawResource(id) // 通过raw得到数据资源
                val fos = FileOutputStream(file)
                val buffer = ByteArray(8192)
                var count = 0
                while (ins.read(buffer).also { count = it } > 0) {
                    fos.write(buffer, 0, count)
                }
                fos.close()
                ins.close()
            }

            return true

        } catch (e: Exception) {
            Log.e(TAG, "fun createModelFile() is error:${e.message}")
        }

        return false
    }


    /**
     * 是否第一次运行
     */
    private fun isFirstRun(): Boolean {
        val sharedPreferences = activity.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val editor = sharedPreferences.edit()
        if (isFirstRun) {
            editor.putBoolean("isFirstRun", false)
            editor.commit()
        }
        return isFirstRun
    }

    /**
     * 获取平台版本
     */
    private fun getPlatform(): String //取平台版本
    {
        var platform = ""
        try {
            val classType = Class.forName("android.os.SystemProperties")
            val getMethod = classType.getDeclaredMethod(
                "get", *arrayOf<Class<*>>(
                    String::class.java
                )
            )
            platform = getMethod.invoke(classType, *arrayOf<Any>("ro.board.platform")) as String
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return platform
    }

}
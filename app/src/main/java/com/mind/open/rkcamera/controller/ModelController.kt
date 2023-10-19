package com.mind.open.rkcamera.controller

import android.R.attr.height
import android.R.attr.width
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import com.mind.open.RKnnJni
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_HEIGHT
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_WIDTH
import com.mind.open.rga.HALDefine.RK_FORMAT_RGBA_8888
import com.mind.open.rga.HALDefine.RK_FORMAT_YCrCb_420_SP
import com.mind.open.rga.RGA
import com.mind.open.yolo.ImageBufferQueue
import com.mind.open.yolo.InferenceResult
import com.mind.open.yolo.InferenceWrapper
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream


/**
 * create by Rui on 2023-10-13
 * desc:
 */
class ModelController(private val activity: Activity) {

    companion object {
        private const val FLIP_TYPE = -1
        private const val TAG = "ModelController"
    }


    private var mImageBufferQueue: ImageBufferQueue? = null
    private val mInferenceResult = InferenceResult()


    private var detectionThread: DetectionThread? = null

    private var drawListener: ((Bitmap, ArrayList<InferenceResult.Recognition>) -> Unit)? = null


    fun setDrawListener(drawListener: (Bitmap, ArrayList<InferenceResult.Recognition>) -> Unit) {
        this.drawListener = drawListener
    }


    fun startTrack() {
        mInferenceResult.init(activity.assets)
        mInferenceResult.reset()
        mImageBufferQueue = ImageBufferQueue(3, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT)
        detectionThread?.interrupt()
        detectionThread = DetectionThread()
        detectionThread?.start()

    }


    fun stopTrack() {
        detectionThread?.interrupt()
        mImageBufferQueue?.release()
    }

    fun postFrameData(data: ByteArray) {
        val imageBuffer = mImageBufferQueue?.freeBuffer
        if (imageBuffer != null) {
            RGA.colorConvertAndFlip(
                data,
                RK_FORMAT_YCrCb_420_SP,
                imageBuffer.mImage,
                RK_FORMAT_RGBA_8888,
                CAMERA_PREVIEW_WIDTH,
                CAMERA_PREVIEW_HEIGHT,
                FLIP_TYPE
            )
            imageBuffer.srcBuffer = data.clone()
            mImageBufferQueue?.postBuffer(imageBuffer)
        }
    }


    private inner class DetectionThread : Thread() {

        private var stop = false
        override fun run() {
            val mInferenceWrapper = InferenceWrapper()
            while (!stop) {
                val buffer = mImageBufferQueue?.readyBuffer
                if (buffer == null) {
                    SystemClock.sleep(10)
                    continue
                }

                val cloneBitmap = buffer.srcBuffer.clone()
                val outputs = mInferenceWrapper.run(buffer.mImage)
                val recognitions = mInferenceResult.getResult2(mInferenceWrapper)


                // TODO 抠图+菜品识别
                recognitions.forEach {
                    val dFoodBitmap = cutBitmap(byteConvertBitmap(cloneBitmap), it)
                    if (dFoodBitmap != null) {
                        val mat = Mat()
                        Utils.bitmapToMat(dFoodBitmap, mat)
                        val regInfo = RKnnJni.jni.foodRecognition(mat.nativeObjAddr)
                        if (regInfo != null) {
                            it.score = regInfo.score
                            it.title = regInfo.label
                        }
                    }
                }


                //
                if (recognitions != null && recognitions.size > 0) {
                    drawListener?.let { it(byteConvertBitmap(cloneBitmap), recognitions) }
                }


                //  updateMainUI(1, 0);
                mInferenceResult.setResult(outputs)
                mImageBufferQueue?.releaseBuffer(buffer)
            }
            mInferenceWrapper.deinit()
        }

        override fun interrupt() {
            super.interrupt()
            stop = true
        }

        private fun cutBitmap(bitmap: Bitmap, rg: InferenceResult.Recognition): Bitmap? {
           try {
               val rectF = rg.location ?: return null
               // 获取原始图像的宽度和高度
               val imageWidth = bitmap.width
               val imageHeight = bitmap.height
               val left = (rectF.left * imageWidth).toInt()
               val top = (rectF.top * imageHeight).toInt()
               val right = (rectF.right * imageWidth).toInt()
               val bottom = (rectF.bottom * imageHeight).toInt()
               val rect = Rect(left, top, right, bottom)
               return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
           }catch (e: Exception) {
               return null
           }
        }

        private fun byteConvertBitmap(previewData: ByteArray): Bitmap {
            val yuvImage = YuvImage(
                previewData, ImageFormat.NV21, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT, null
            )
            val outputStream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT), 100, outputStream
            )
            val jpegData = outputStream.toByteArray()
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)

        }


    }

}
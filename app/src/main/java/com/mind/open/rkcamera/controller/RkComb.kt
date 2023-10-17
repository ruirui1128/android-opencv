package com.mind.open.rkcamera.controller

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.view.TextureView
import com.mind.open.rga.HALDefine
import com.mind.open.yolo.InferenceResult
import java.io.ByteArrayOutputStream
import java.util.ArrayList

/**
 * create by Rui on 2023-10-13
 * desc: 相机与模型 组合
 */
class RkComb(private val activity: Activity) {

    private val cameraController by lazy { CameraController(activity) }
    private val modelController by lazy { ModelController(activity) }

    private var preViewListener: ((Bitmap) -> Unit)? = null

    fun initRkComb(
        textureView: TextureView,
        drawListener: (Bitmap,ArrayList<InferenceResult.Recognition>) -> Unit
    ) {
        cameraController.setPreviewListener {data->
            modelController.postFrameData(data)
        }
        modelController.setDrawListener(drawListener)
        modelController.startTrack()
        cameraController.initCamera(textureView)

    }

    fun startCamera() {
        cameraController.startCamera()
    }


    fun release() {
        modelController.stopTrack()
        cameraController.releaseCamera()
    }


}
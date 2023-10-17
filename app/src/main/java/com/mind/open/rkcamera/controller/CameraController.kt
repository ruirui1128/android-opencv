package com.mind.open.rkcamera.controller

import android.app.Activity
import android.hardware.Camera
import android.view.TextureView
import com.mind.open.camera.CameraHelper
import com.mind.open.camera.CameraListener

/**
 * create by Rui on 2023-10-13
 * desc:
 */
class CameraController(private val activity: Activity) : CameraListener {


    private var cameraHelper: CameraHelper? = null

    private var previewListener: ((ByteArray) -> Unit)? = null

    fun setPreviewListener(previewListener: ((ByteArray) -> Unit)) {
        this.previewListener = previewListener
    }

    fun initCamera(textureView: TextureView) {
        cameraHelper = CameraHelper.Builder()
            .rotation(activity.windowManager.defaultDisplay.rotation)
            .specificCameraId(2)
            .isMirror(false)
            .isUdMirror(true)
            .previewOn(textureView)
            .cameraListener(this)
            .additionalRotation(-90)
            .build()
        cameraHelper?.init()
    }

    fun startCamera(){
        cameraHelper?.start()
    }

    fun stopCamera() {
        cameraHelper?.stop()
    }

    fun releaseCamera() {
        cameraHelper?.release()
    }


    override fun onCameraOpened(
        camera: Camera?,
        cameraId: Int,
        displayOrientation: Int,
        isMirror: Boolean
    ) {

    }

    override fun onPreview(data: ByteArray?, camera: Camera?) {
        data ?: return
        previewListener?.let { it(data) }
    }

    override fun onCameraClosed() {

    }

    override fun onCameraError(e: Exception?) {

    }

    override fun onCameraConfigurationChanged(cameraID: Int, displayOrientation: Int) {

    }
}
package com.mind.open.rkcamera

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mind.open.databinding.ActivityRkCameraBinding
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_HEIGHT
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_WIDTH
import com.mind.open.rkcamera.controller.RkComb
import com.mind.open.yolo.InferenceResult
import com.permissionx.guolindev.PermissionX

class RkCameraActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "RkCameraActivity"
    }

    private lateinit var binding: ActivityRkCameraBinding

    private var rkComb: RkComb? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRkCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    initialize()
                } else {
                    Toast.makeText(
                        this,
                        "These permissions are denied: $deniedList",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun initialize() {
        initSelectResults()
        rkComb = RkComb(this)
        rkComb?.initRkComb(binding.textureView) { bitmap, data ->
            drawableModelView(data)
            drawableModelImaView(bitmap, data)
        }


    }

    private fun drawableModelImaView(
        bitmap: Bitmap,
        data: java.util.ArrayList<InferenceResult.Recognition>
    ) {
        if (data.size < 3) return
        val r1 = data.filter { (it.location?.left ?: 0f) > 0f }.first()

        val b = cutBitmap(bitmap, r1)
        runOnUiThread {
            binding.iv1.setImageBitmap(b)
        }

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
        } catch (e: Exception) {
            return null
        }

    }


    private fun drawableModelView(it: ArrayList<InferenceResult.Recognition>) {
        runOnUiThread {

            try {
                mTrackResultPaint?.xfermode = mPorterDuffXfermodeClear
                mTrackResultCanvas?.drawPaint(mTrackResultPaint!!)
                mTrackResultPaint?.xfermode = mPorterDuffXfermodeSRC
                for (i in it.indices) {
                    val rego = it[i]
                    val detection: RectF = rego.location
                    detection.left *= CAMERA_PREVIEW_WIDTH
                    detection.right *= CAMERA_PREVIEW_WIDTH

                    // 进行左右镜像坐标变换
                    detection.left = CAMERA_PREVIEW_WIDTH - detection.left
                    detection.right = CAMERA_PREVIEW_WIDTH - detection.right
                    detection.top *= CAMERA_PREVIEW_HEIGHT
                    detection.bottom *= CAMERA_PREVIEW_HEIGHT
                    mTrackResultCanvas?.drawRect(detection, mTrackResultPaint!!)
                    mTrackResultCanvas?.drawText(
                        rego.trackId.toString() + " - " + rego.title,
                        detection.left + 5, detection.bottom - 5, mTrackResultTextPaint!!
                    )
                }

                binding.canvasView.scaleType = ImageView.ScaleType.FIT_XY
                binding.canvasView.setImageBitmap(mTrackResultBitmap)
            } catch (e: Exception) {
            }


        }
    }


    override fun onResume() {
        super.onResume()
        rkComb?.startCamera()
        //   cameraController.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        rkComb?.release()
    }


    private var mTrackResultBitmap: Bitmap? = null
    private var mTrackResultCanvas: Canvas? = null
    private var mTrackResultPaint: Paint? = null
    private var mTrackResultTextPaint: Paint? = null
    private var mPorterDuffXfermodeClear: PorterDuffXfermode? = null
    private var mPorterDuffXfermodeSRC: PorterDuffXfermode? = null
    private fun initSelectResults() {
        val width: Int = 1280
        val height: Int = 720

        if (mTrackResultBitmap == null) {
            mTrackResultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mTrackResultCanvas = Canvas(mTrackResultBitmap!!)

            //用于画线
            mTrackResultPaint = Paint()
            mTrackResultPaint?.color = -0xf91401
            mTrackResultPaint?.strokeJoin = Paint.Join.ROUND
            mTrackResultPaint?.strokeCap = Paint.Cap.ROUND
            mTrackResultPaint?.strokeWidth = 4f
            mTrackResultPaint?.style = Paint.Style.STROKE
            mTrackResultPaint?.textAlign = Paint.Align.LEFT
            mTrackResultPaint?.textSize = sp2px(10f).toFloat()
            mTrackResultPaint?.typeface = Typeface.SANS_SERIF
            mTrackResultPaint?.isFakeBoldText = false

            //用于文字
            mTrackResultTextPaint = Paint()
            mTrackResultTextPaint?.color = -0xf91401
            mTrackResultTextPaint?.strokeWidth = 2f
            mTrackResultTextPaint?.textAlign = Paint.Align.LEFT
            mTrackResultTextPaint?.textSize = sp2px(12f).toFloat()
            mTrackResultTextPaint?.typeface = Typeface.SANS_SERIF
            mTrackResultTextPaint?.isFakeBoldText = false
            mPorterDuffXfermodeClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            mPorterDuffXfermodeSRC = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }

        // clear canvas

        // clear canvas
        mTrackResultPaint?.xfermode = mPorterDuffXfermodeClear
        mTrackResultCanvas?.drawPaint(mTrackResultPaint!!)
        mTrackResultPaint?.xfermode = mPorterDuffXfermodeSRC

    }

    private fun sp2px(spValue: Float): Int {
        val r = Resources.getSystem()
        val scale = r.displayMetrics.scaledDensity
        return (spValue * scale + 0.5f).toInt()
    }


}
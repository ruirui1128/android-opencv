package com.mind.open

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mind.make.TestJni
import com.mind.open.databinding.ActivitySplashBinding
import com.mind.open.rga.HALDefine
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_HEIGHT
import com.mind.open.rga.HALDefine.CAMERA_PREVIEW_WIDTH
import com.mind.open.rkcamera.RkCameraActivity
import com.mind.open.rkcamera.RkPresenter
import com.mind.open.yolo.PostProcess.INPUT_CHANNEL
import kotlin.concurrent.thread

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var rkPresenter: RkPresenter

    @Volatile
    private var start = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        rkPresenter = RkPresenter(this)
        RKnnJni.jni
        //TestJni.jni
        setContentView(binding.root)
        thread {
            setProgressText("步骤一: 加载模型平台")

            val r1 = rkPresenter.loadPlatForm()
            if (!r1) {
                setProgressText("步骤一: 加载模型平台-加载失败")
                return@thread
            }
            setProgressText("步骤二: 初始化菜品检测模型")
            val r2 = RKnnJni.jni.initDetectModel(
                CAMERA_PREVIEW_HEIGHT,
                CAMERA_PREVIEW_WIDTH,
                INPUT_CHANNEL,
                rkPresenter.getDetectFilePath()
            )

            if (r2 != 0) {
                setProgressText("步骤二: 初始化菜品检测模型 失败")
            } else {
                setProgressText("步骤二: 初始化菜品检测模型 成功")
            }

            start = true

        }

        binding.btnStart.setOnClickListener {
            if (start) {
                startCamera()
            } else {
                Toast.makeText(this, "模型初始化失败", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnStart.text = TestJni.jni.getStringFromJni()
        binding.btnStart.postDelayed({
            binding.btnStart.performClick()
        }, 5000)

    }

    private fun startCamera() {
        runOnUiThread {
            startActivity(Intent(this, RkCameraActivity::class.java))
        }
    }

    private fun setProgressText(text: String) {
        runOnUiThread {
            binding.tvProgress.text = text
        }
    }
}
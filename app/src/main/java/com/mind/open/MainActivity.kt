package com.mind.open

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.mind.open.databinding.ActivityMainBinding
import org.opencv.android.Utils
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sampleText.text = stringFromJNI()




        binding.btnConvert.setOnClickListener {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gril)
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            processImage(mat.nativeObjAddr)
            // 将处理后的图像显示在ImageView中
            val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, resultBitmap)
            binding.ivGirl.setImageBitmap(resultBitmap)

        }


        binding.btnConvert2.setOnClickListener {

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gril)
            val matSrc = Mat()
            val matDst = Mat()
            Utils.bitmapToMat(bitmap, matSrc)
            processImage2(matSrc.nativeObjAddr,matDst.nativeObjAddr)
            // 将处理后的图像显示在ImageView中
            val resultBitmap = Bitmap.createBitmap(matDst.cols(), matDst.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(matDst, resultBitmap)
            binding.ivGirl2.setImageBitmap(resultBitmap)

        }

    }



    external fun stringFromJNI(): String

    external fun processImage(nativeObjAddr: Long)

    external fun processImage2(nativeObjAddr: Long, dstObjAddr: Long)

    companion object {

        init {
            System.loadLibrary("open")
        }
    }
}
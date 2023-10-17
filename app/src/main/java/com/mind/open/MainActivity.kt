package com.mind.open

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.mind.open.databinding.ActivityMainBinding
import com.mind.open.presenter.RknnPresenter
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var presenter: RknnPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = RknnPresenter(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // binding.sampleText.text = stringFromJNI()

        binding.btnConvert.setOnClickListener {
//            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gril)
//            val mat = Mat()
//            Utils.bitmapToMat(bitmap, mat)
//            processImage(mat.nativeObjAddr)
//            // 将处理后的图像显示在ImageView中
//            val resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(mat, resultBitmap)
//            binding.ivGirl.setImageBitmap(resultBitmap)

            presenter.loadPlatform()
        }


        binding.btnConvert2.setOnClickListener {

//            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.geli1)
//            val matSrc = Mat()
//            val matDst = Mat()
//            Utils.bitmapToMat(bitmap, matSrc)
//            processImage2(matSrc.nativeObjAddr, matDst.nativeObjAddr)
//            // 将处理后的图像显示在ImageView中
//            val resultBitmap =
//                Bitmap.createBitmap(matDst.cols(), matDst.rows(), Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(matDst, resultBitmap)
//            binding.ivGirl2.setImageBitmap(resultBitmap)
//
//            if (initFoodModel(presenter.modelFilePath) == 0) {
//                Toast.makeText(this, "模型加载成功", Toast.LENGTH_SHORT).show()
//            }


        }

        binding.btn3.setOnClickListener {
//            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.geli1)
//            val mat = Mat()
//            Utils.bitmapToMat(bitmap, mat)
//            if (foodInStorages(mat.nativeObjAddr, "g1", presenter.engineFilePath) == 0) {
//
//                val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.gili2)
//                val mat2 = Mat()
//                Utils.bitmapToMat(bitmap2, mat2)
//
//                if (foodInStorages(mat.nativeObjAddr, "g2", presenter.engineFilePath) == 0) {
//                    Toast.makeText(this, "geli菜品入库ok", Toast.LENGTH_SHORT).show()
//                }
//
//
//            } else {
//                Toast.makeText(this, "geli菜品入库失败", Toast.LENGTH_SHORT).show()
//            }
        }


        binding.btn4.setOnClickListener {
            //readMatMemory(presenter.engineFilePath)
        }

        // 识别
        binding.btn5.setOnClickListener {
            thread {
                for (i in 0 until 8) {
                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gril)
                    val mat = Mat()
                    Utils.bitmapToMat(bitmap, mat)
                  //  foodRecognize(mat.nativeObjAddr)
                }
            }
        }

    }


//    external fun stringFromJNI(): String
//
//    external fun processImage(nativeObjAddr: Long)
//
//    external fun processImage2(nativeObjAddr: Long, dstObjAddr: Long)
//
//
//    external fun initFoodModel(modelPath: String): Int
//
//    external fun foodInStorage(nativeObjAddr: Long, name: String): Int
//
//    external fun foodInStorages(nativeObjAddr: Long, name: String, path: String): Int
//
//    //加载到内存
//    external fun readMatMemory(path: String);
//
//    /**
//     * 开始识别
//     */
//    external fun foodRecognize(nativeObjAddr: Long);
//
//    companion object {
//
//        init {
//            System.loadLibrary("open")
//        }
//    }
}
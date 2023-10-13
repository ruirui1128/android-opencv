package com.mind.open

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mind.open.databinding.ActivityRkCameraBinding
import com.permissionx.guolindev.PermissionX

class RkCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRkCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRkCameraBinding.inflate(layoutInflater)
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

    }
}
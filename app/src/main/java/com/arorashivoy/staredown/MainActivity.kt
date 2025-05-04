package com.arorashivoy.staredown

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {
    private lateinit var singlePlayerBtn: Button
    private lateinit var multiPlayerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
//            startActivity(Intent(this, RequestCamera::class.java))
//            var dialog = BottomSheetDialog(this)
//            var view = layoutInflater.inflate(R.layout.activity_request_camera, null)

            RequestCamera.newInstance().show(supportFragmentManager, "RequestCamera")

        }

        singlePlayerBtn = findViewById(R.id.singlePlayerBtn)
        multiPlayerBtn = findViewById(R.id.multiPlayerBtn)

        singlePlayerBtn.setOnClickListener {
            startActivity(Intent(this, SinglePlayerActivity::class.java))
        }

        multiPlayerBtn.setOnClickListener {
            startActivity(Intent(this, MultiPlayerActivity::class.java))
        }
    }
}
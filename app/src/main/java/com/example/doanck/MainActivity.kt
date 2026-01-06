package com.example.doanck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        // App mở lên / từ nền quay lại -> HomeFragment sẽ nhận signal
        supportFragmentManager.setFragmentResult("APP_OPENED", bundleOf())
    }
}

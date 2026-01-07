package com.example.doanck.feature.category

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.feature.home.HomeFragment

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI sạch, không phụ thuộc drawable custom
        setContentView(R.layout.activity_category)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }
}

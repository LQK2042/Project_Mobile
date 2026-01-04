package com.example.doanck.feature.main;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.doanck.R;
import com.example.doanck.core.utils.StartButtonHandler;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);

        // Gọi handler tách riêng
        new StartButtonHandler(this, btnStart);
    }
}

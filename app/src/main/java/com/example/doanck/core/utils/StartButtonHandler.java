package com.example.doanck.core.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.example.doanck.feature.auth.LoginActivity;

public class StartButtonHandler {

    private Activity activity;
    private Button btnStart;

    public StartButtonHandler(Activity activity, Button btnStart) {
        this.activity = activity;
        this.btnStart = btnStart;

        setupListener();
    }

    private void setupListener() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            }
        });
    }
}

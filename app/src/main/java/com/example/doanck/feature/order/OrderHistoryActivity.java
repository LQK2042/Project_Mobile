package com.example.doanck.feature.order;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doanck.R;

public class OrderHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        TextView orders = findViewById(R.id.order_history_list);
        String demoOrders = "1. Đơn hàng #1001 - 250.000₫\n" +
                "2. Đơn hàng #1002 - 125.000₫\n" +
                "3. Đơn hàng #1003 - 350.000₫";
        orders.setText(demoOrders);
    }
}



package com.example.doanck.feature.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanck.R;
import com.example.doanck.core.network.ApiClient;
import com.example.doanck.core.network.ApiService;
import com.example.doanck.data.model.LoginRequest;
import com.example.doanck.data.model.LoginResponse;
import com.example.doanck.feature.category.CategoryActivity;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//23162016 Dinh Quoc Dat
public class LoginActivity extends AppCompatActivity {

    TextInputEditText editEmail, editPassword;
    Button btnLogin;
    TextView textRegister;
    TextView textForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editText_TextEmailAddress);
        editPassword = findViewById(R.id.editText_TextPassword);
        btnLogin = findViewById(R.id.button_Login);
        textRegister = findViewById(R.id.textView_Register);

        textForgotPassword = findViewById(R.id.textView_ForgotPassword);

        btnLogin.setOnClickListener(v -> doLogin());

        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        textForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity_mhuyy.class));
        });
    }

    private void doLogin() {
        String account = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (account.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        // tạo request (LoginRequest nằm trong cùng package com.example.exercise1_team)
        LoginRequest req = new LoginRequest(account, password);

        // ApiClient và ApiService cũng ở cùng package -> không cần import thêm
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.login(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();

                    // nếu server trả token, lưu vào SharedPreferences
                    getSharedPreferences("App", MODE_PRIVATE).edit().putString("token", res.token).apply();

                    // chuyển sang MainActivity (hoặc HomeActivity)
                    startActivity(new Intent(LoginActivity.this, CategoryActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

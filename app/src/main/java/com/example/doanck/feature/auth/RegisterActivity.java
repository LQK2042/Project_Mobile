package com.example.doanck.feature.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doanck.R;
import com.example.doanck.core.network.ApiClient;
import com.example.doanck.core.network.ApiService;
import com.example.doanck.data.model.DefaultResponse;
import com.example.doanck.data.model.RegisterRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


//23162037 Le Nhut Quoc Khang

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText editFirstName, editLastName, editEmail, editPassword, editConfirm, editImageURL;
    Button btnRegister;
    TextView textBackLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ view (phù hợp layout bạn đã cập nhật)
        editFirstName = findViewById(R.id.editText_FirstName);
        editLastName = findViewById(R.id.editText_LastName);
        editEmail = findViewById(R.id.editText_RegisterEmail);
        editPassword = findViewById(R.id.editText_RegisterPassword);
        editConfirm = findViewById(R.id.editText_ConfirmPassword);
        editImageURL = findViewById(R.id.editText_ImageURL);

        btnRegister = findViewById(R.id.button_Register);
        textBackLogin = findViewById(R.id.textView_BackToLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        textBackLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String account = editEmail.getText().toString().trim(); // dùng làm Account
        String pass = editPassword.getText().toString().trim();
        String confirm = editConfirm.getText().toString().trim();
        String imageUrl = editImageURL.getText().toString().trim(); // optional

        // Validate
        if (firstName.isEmpty() || lastName.isEmpty() || account.isEmpty()
                || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không trùng nhau", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request phù hợp với DB (Account, Password, FirstName, LastName, ImageURL)
        RegisterRequest req = new RegisterRequest(
                account,
                pass,
                firstName,
                lastName,
                imageUrl
        );

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.register(req).enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();  // quay về trang login
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

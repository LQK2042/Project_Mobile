package com.example.doanck.feature.auth

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SignUpRequest
import com.example.doanck.data.remote.supabase.SignUpResponse
import com.example.doanck.data.remote.supabase.SupabaseAuthClient
import com.example.doanck.data.remote.supabase.UserMetadata
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var etFirst: TextInputEditText
    private lateinit var etLast: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var etAvatar: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvBack: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFirst = findViewById(R.id.etFirstName)
        etLast = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPass = findViewById(R.id.etRegisterPassword)
        etConfirm = findViewById(R.id.etRegisterConfirm)
        etAvatar = findViewById(R.id.etAvatarUrl)
        btnRegister = findViewById(R.id.btnRegister)
        tvBack = findViewById(R.id.tvBackToLogin)

        tvBack.setOnClickListener { finish() }
        btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val first = etFirst.text?.toString()?.trim().orEmpty()
        val last = etLast.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val pass = etPass.text?.toString()?.trim().orEmpty()
        val confirm = etConfirm.text?.toString()?.trim().orEmpty()
        val avatar = etAvatar.text?.toString()?.trim().orEmpty()

        if (first.isBlank() || last.isBlank() || email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show(); return
        }
        if (pass != confirm) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show(); return
        }

        val metadata = UserMetadata(
            first_name = first,
            last_name = last,
            avatar_url = if (avatar.isNotBlank()) avatar else null
        )

        val body = SignUpRequest(
            email = email,
            password = pass,
            data = metadata
        )

        Log.d(TAG, "doRegister() - email: $email")

        SupabaseAuthClient.service.signUp(body).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                Log.d(TAG, "onResponse - code: ${response.code()}, successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!
                    Log.d(TAG, "SignUp response - user: ${r.user?.id}, session: ${r.session != null}")

                    val session = r.session
                    if (!session?.accessToken.isNullOrBlank()) {
                        AuthStore.save(this@RegisterActivity, session!!.accessToken, session.refreshToken, r.user?.id, r.user?.email ?: email)
                        Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        // Supabase yêu cầu xác minh email nếu "Confirm email" được bật
                        Log.d(TAG, "No session returned - email confirmation may be required")
                        Toast.makeText(this@RegisterActivity, "Đăng ký thành công! Kiểm tra email để xác minh.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    val error = SupabaseAuthClient.parseError(response)
                    Log.e(TAG, "SignUp error: $error")
                    Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

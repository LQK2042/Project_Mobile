package com.example.doanck.feature.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.core.supabase.AuthStore
import com.example.doanck.core.supabase.SignUpResponse
import com.example.doanck.core.supabase.SupabaseAuthClient
import com.example.doanck.feature.category.CategoryActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstName: TextInputEditText
    private lateinit var lastName: TextInputEditText
    private lateinit var emailEt: TextInputEditText
    private lateinit var passEt: TextInputEditText
    private lateinit var confirmEt: TextInputEditText
    private lateinit var avatarEt: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firstName = findViewById(R.id.editText_FirstName)
        lastName = findViewById(R.id.editText_LastName)
        emailEt = findViewById(R.id.editText_RegisterEmail)
        passEt = findViewById(R.id.editText_RegisterPassword)
        confirmEt = findViewById(R.id.editText_ConfirmPassword)
        avatarEt = findViewById(R.id.editText_ImageURL)
        btnRegister = findViewById(R.id.button_Register)
        tvBack = findViewById(R.id.textView_BackToLogin)

        btnRegister.setOnClickListener { doRegister() }
        tvBack.setOnClickListener { finish() }
    }

    private fun doRegister() {
        val first = firstName.text?.toString()?.trim().orEmpty()
        val last = lastName.text?.toString()?.trim().orEmpty()
        val email = emailEt.text?.toString()?.trim().orEmpty()
        val pass = passEt.text?.toString()?.trim().orEmpty()
        val confirm = confirmEt.text?.toString()?.trim().orEmpty()
        val avatar = avatarEt.text?.toString()?.trim().orEmpty()

        if (first.isBlank() || last.isBlank() || email.isBlank() || pass.isBlank() || confirm.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        if (pass != confirm) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mutableMapOf<String, Any>(
                "first_name" to first,
        "last_name" to last
        )
        if (avatar.isNotBlank()) data["avatar_url"] = avatar

        val body = mapOf(
                "email" to email,
                "password" to pass,
                "data" to data // user_metadata
        )

        SupabaseAuthClient.service.signUp(body).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!

                            // Nếu Supabase bật Email Confirmation -> session thường null
                            val session = r.session
                    if (session?.accessToken != null) {
                        AuthStore.save(
                                this@RegisterActivity,
                        session.accessToken,
                                session.refreshToken,
                                r.user?.id,
                                r.user?.email ?: email
                        )
                        startActivity(Intent(this@RegisterActivity, CategoryActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                                this@RegisterActivity,
                        "Đăng ký thành công! Hãy kiểm tra email để xác minh tài khoản.",
                                Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

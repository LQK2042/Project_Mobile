package com.example.doanck.feature.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.core.supabase.SupabaseAuthClient
import com.example.doanck.core.supabase.SupabaseConfig
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEt: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        emailEt = findViewById(R.id.editText_ForgotEmail)
        btnSend = findViewById(R.id.button_SendReset)
        tvBack = findViewById(R.id.textView_BackToLoginFromForgot)

        tvBack.setOnClickListener { finish() }
        btnSend.setOnClickListener { sendRecover() }
    }

    private fun sendRecover() {
        val email = emailEt.text?.toString()?.trim().orEmpty()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.error = "Email không hợp lệ"
            return
        }

        val body = mapOf(
                "email" to email,
                "redirect_to" to SupabaseConfig.RECOVER_REDIRECT_TO
        )

        SupabaseAuthClient.service.recover(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                            this@ForgotPasswordActivity,
                    "Đã gửi email khôi phục. Mở email và bấm link để đặt lại mật khẩu.",
                            Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ForgotPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
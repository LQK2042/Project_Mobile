package com.example.doanck.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.core.supabase.AuthStore
import com.example.doanck.core.supabase.SupabaseAuthClient
import com.example.doanck.core.supabase.TokenResponse
import com.example.doanck.feature.category.CategoryActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val EXTRA_RETURN_RESULT = "EXTRA_RETURN_RESULT"

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgot: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editEmail = findViewById(R.id.editText_TextEmailAddress)
        editPassword = findViewById(R.id.editText_TextPassword)
        btnLogin = findViewById(R.id.button_Login)
        tvRegister = findViewById(R.id.textView_Register)
        tvForgot = findViewById(R.id.textView_ForgotPassword)

        btnLogin.setOnClickListener { doLogin() }
        tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
    }

    private fun doLogin() {
        val email = editEmail.text?.toString()?.trim().orEmpty()
        val password = editPassword.text?.toString()?.trim().orEmpty()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf(
            "email" to email,
            "password" to password
        )

        SupabaseAuthClient.service.signIn("password", body).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!
                    AuthStore.save(
                        this@LoginActivity,
                        r.accessToken,
                        r.refreshToken,
                        r.user?.id,
                        r.user?.email ?: email
                    )

                    val returnResult = intent.getBooleanExtra(EXTRA_RETURN_RESULT, false)
                    if (returnResult) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        startActivity(Intent(this@LoginActivity, CategoryActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

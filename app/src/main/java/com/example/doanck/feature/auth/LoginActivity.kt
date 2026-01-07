package com.example.doanck.feature.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SignInRequest
import com.example.doanck.data.remote.supabase.SupabaseAuthClient
import com.example.doanck.data.remote.supabase.TokenResponse
import com.example.doanck.feature.category.CategoryActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val EXTRA_RETURN_RESULT = "EXTRA_RETURN_RESULT"

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: android.widget.TextView
    private lateinit var tvForgot: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvGoRegister)
        tvForgot = findViewById(R.id.tvGoForgot)

        btnLogin.setOnClickListener { doLogin() }
        tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvForgot.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
    }

    private fun doLogin() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val pass = etPassword.text?.toString()?.trim().orEmpty()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(this, "Vui lòng nhập email & mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        val req = SignInRequest(email = email, password = pass)

        SupabaseAuthClient.service.signIn(grantType = "password", body = req)
            .enqueue(object : Callback<TokenResponse> {
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
                        Toast.makeText(
                            this@LoginActivity,
                            SupabaseAuthClient.parseError(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

}

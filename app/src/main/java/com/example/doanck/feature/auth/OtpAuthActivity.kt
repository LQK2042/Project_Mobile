package com.example.doanck.feature.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.core.supabase.SupabaseAuthClient
import com.example.doanck.core.supabase.SupabaseConfig
import com.example.doanck.core.supabase.TokenResponse
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpAuthActivity : AppCompatActivity() {

    private lateinit var editOtp: TextInputEditText
    private lateinit var buttonVerify: Button
    private lateinit var textResend: TextView
    private lateinit var textChangeEmail: TextView

    private lateinit var progress: ProgressDialog

    private var email: String? = null
    private var type: String = "recovery" // default: xác thực OTP cho quên mật khẩu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_auth)

        editOtp = findViewById(R.id.editText_Otp)
        buttonVerify = findViewById(R.id.button_VerifyOtp)
        textResend = findViewById(R.id.textView_Resend)
        textChangeEmail = findViewById(R.id.textView_ChangeEmail)

        progress = ProgressDialog(this).apply {
            setMessage("Please wait...")
            setCancelable(false)
        }

        email = intent.getStringExtra("email")
        type = intent.getStringExtra("type") ?: "recovery"

        buttonVerify.setOnClickListener {
            val otp = editOtp.text?.toString()?.trim().orEmpty()
            if (TextUtils.isEmpty(otp) || otp.length < 4) {
                editOtp.error = "Enter OTP"
                return@setOnClickListener
            }
            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Missing email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(email!!, otp)
        }

        textResend.setOnClickListener { resendOtp() }
        textChangeEmail.setOnClickListener { finish() }
    }

    private fun verifyOtp(email: String, otp: String) {
        progress.show()

        val body = mapOf(
            "type" to type,               // "recovery" hoặc "signup"...
            "email" to email,
            "token" to otp,
            "redirect_to" to SupabaseConfig.RECOVER_REDIRECT_TO
        )

        SupabaseAuthClient.service.verify(body).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                progress.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val r = response.body()!!
                    val access = r.accessToken

                    if (access.isNullOrBlank()) {
                        Toast.makeText(this@OtpAuthActivity, "Missing access_token from Supabase", Toast.LENGTH_LONG).show()
                        return
                    }

                    // ✅ Mở màn ResetPassword và truyền access_token để đổi mật khẩu
                    val i = Intent(this@OtpAuthActivity, ResetPasswordActivity::class.java)
                        .putExtra("email", email)
                        .putExtra("access_token", access)
                    startActivity(i)
                    finish()
                } else {
                    Toast.makeText(this@OtpAuthActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@OtpAuthActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun resendOtp() {
        val em = email
        if (em.isNullOrBlank()) {
            Toast.makeText(this, "No email to resend to", Toast.LENGTH_SHORT).show()
            return
        }

        progress.show()
        val body = mapOf(
            "email" to em,
            "redirect_to" to SupabaseConfig.RECOVER_REDIRECT_TO
        )

        SupabaseAuthClient.service.recover(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpAuthActivity, "OTP đã được gửi lại (check email)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@OtpAuthActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@OtpAuthActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

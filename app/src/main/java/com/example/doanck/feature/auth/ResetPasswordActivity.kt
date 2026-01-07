package com.example.doanck.feature.auth

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.core.supabase.SbUser
import com.example.doanck.core.supabase.SupabaseAuthClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPassEt: TextInputEditText
    private lateinit var confirmEt: TextInputEditText
    private lateinit var btnReset: Button
    private lateinit var tvBack: TextView

    private var accessTokenFromLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        newPassEt = findViewById(R.id.editText_NewPassword)
        confirmEt = findViewById(R.id.editText_ConfirmNewPassword)
        btnReset = findViewById(R.id.button_ResetPassword)
        tvBack = findViewById(R.id.textView_BackToLoginFromReset)

        readTokenFromDeepLink()

        tvBack.setOnClickListener { finish() }
        btnReset.setOnClickListener { updatePassword() }
    }

    // Supabase link thường mở app dạng: doanck://auth#access_token=...&type=recovery
    private fun readTokenFromDeepLink() {
        val data: Uri = intent.data ?: return
                val fragment = data.fragment ?: return

                fragment.split("&").forEach { part ->
                val kv = part.split("=")
            if (kv.size == 2 && kv[0] == "access_token") {
                accessTokenFromLink = Uri.decode(kv[1])
            }
        }
    }

    private fun updatePassword() {
        val token = accessTokenFromLink
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Thiếu access_token. Hãy bấm link reset trong email để mở app.", Toast.LENGTH_LONG).show()
            return
        }

        val pass = newPassEt.text?.toString().orEmpty()
        val confirm = confirmEt.text?.toString().orEmpty()

        if (pass.length < 6) {
            newPassEt.error = "Mật khẩu tối thiểu 6 ký tự"
            return
        }
        if (pass != confirm) {
            confirmEt.error = "Mật khẩu xác nhận không khớp"
            return
        }

        val body = mapOf("password" to pass)

        SupabaseAuthClient.service.updateUser("Bearer $token", body).enqueue(object : Callback<SbUser> {
            override fun onResponse(call: Call<SbUser>, response: Response<SbUser>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ResetPasswordActivity, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@ResetPasswordActivity, SupabaseAuthClient.parseError(response), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SbUser>, t: Throwable) {
                Toast.makeText(this@ResetPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

package com.example.doanck.feature.auth

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.SbUser
import com.example.doanck.data.remote.supabase.SupabaseAuthClient
import com.example.doanck.data.remote.supabase.UpdatePasswordRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPass: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var btnReset: MaterialButton
    private lateinit var tvBack: android.widget.TextView

    private var accessToken: String? = null // ✅ unify token source

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etNewPass = findViewById(R.id.etNewPassword)
        etConfirm = findViewById(R.id.etConfirmNewPassword)
        btnReset = findViewById(R.id.btnDoReset)
        tvBack = findViewById(R.id.tvBackReset)

        // ✅ 1) ưu tiên token từ Intent extra (OTP flow)
        accessToken = intent.getStringExtra("access_token")

        // ✅ 2) nếu không có -> đọc từ deep link (email link)
        if (accessToken.isNullOrBlank()) {
            accessToken = readTokenFromDeepLink(intent.data)
        }

        tvBack.setOnClickListener { finish() }
        btnReset.setOnClickListener { updatePassword() }
    }

    private fun readTokenFromDeepLink(uri: Uri?): String? {
        if (uri == null) return null
        val fragment = uri.fragment ?: return null
        var token: String? = null
        fragment.split("&").forEach { part ->
            val kv = part.split("=")
            if (kv.size == 2 && kv[0] == "access_token") token = Uri.decode(kv[1])
        }
        return token
    }

    private fun updatePassword() {
        val token = accessToken
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Thiếu access_token. Hãy mở link trong email (hoặc verify OTP).", Toast.LENGTH_LONG).show()
            return
        }

        val pass = etNewPass.text?.toString().orEmpty()
        val confirm = etConfirm.text?.toString().orEmpty()

        if (pass.length < 6) { etNewPass.error = "Tối thiểu 6 ký tự"; return }
        if (pass != confirm) { etConfirm.error = "Không khớp"; return }

        val body = UpdatePasswordRequest(password = pass)

        SupabaseAuthClient.service.updateUser("Bearer $token", body)
            .enqueue(object : Callback<SbUser> {
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

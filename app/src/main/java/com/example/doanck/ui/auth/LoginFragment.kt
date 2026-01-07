package com.example.doanck.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SignInRequest
import com.example.doanck.data.remote.supabase.SupabaseAuthClient
import com.example.doanck.data.remote.supabase.TokenResponse
import com.example.doanck.feature.auth.RegisterActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var progress: ProgressBar

    // Arguments từ navigation
    private val redirect: String by lazy { arguments?.getString("redirect").orEmpty() }
    private val autoCheckout: Boolean by lazy { arguments?.getBoolean("autoCheckout", false) ?: false }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvRegister = view.findViewById(R.id.tvRegister)
        btnBack = view.findViewById(R.id.btnBack)
        progress = view.findViewById(R.id.progress)

        btnLogin.setOnClickListener { doLogin() }

        // Click vào "Đăng ký" -> mở RegisterActivity
        tvRegister.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun doLogin() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val pass = etPassword.text?.toString()?.trim().orEmpty()

        if (email.isBlank() || pass.isBlank()) {
            Toast.makeText(requireContext(), "Vui lòng nhập email & mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false
        progress.visibility = View.VISIBLE

        val req = SignInRequest(email = email, password = pass)

        SupabaseAuthClient.service.signIn(grantType = "password", body = req)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    btnLogin.isEnabled = true
                    progress.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val r = response.body()!!
                        AuthStore.save(
                            requireContext(),
                            r.accessToken,
                            r.refreshToken,
                            r.user?.id,
                            r.user?.email ?: email
                        )
                        Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        // Gửi tín hiệu login_success về màn trước (Cart/Home/...)
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("login_success", true)

                        // Quay lại màn đang làm
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            SupabaseAuthClient.parseError(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    progress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}

package com.example.doanck.feature.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.repository.ProfileRepository
import com.example.doanck.feature.auth.LoginActivity
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val vm: ProfileViewModel by viewModels()
    private val profileRepo by lazy { ProfileRepository(this) }

    private lateinit var btnBack: ImageButton
    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUid: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnLogout: Button

    // Launcher để chọn ảnh từ gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            uploadAvatar(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Nếu chưa đăng nhập -> về Login
        if (!AuthStore.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bindViews()
        setupClicks()

        // Email + UID lấy từ AuthStore
        val email = AuthStore.email(this).orEmpty()
        val uid = AuthStore.userId(this).orEmpty()

        tvEmail.text = if (email.isNotBlank()) email else "Chưa có email"
        tvUid.text = if (uid.isNotBlank()) uid else "Chưa có userId"

        // Set default UI trước khi load API
        ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        tvFullName.text = "Người dùng"
        tvPhone.text = "Chưa có số điện thoại"
        tvRole.text = "user"

        // Load profile từ Supabase
        vm.load(uid)

        // Observe state
        lifecycleScope.launch {
            vm.state.collect { st ->

                if (st.loading) return@collect

                st.error?.let { msg ->
                    Toast.makeText(this@ProfileActivity, msg, Toast.LENGTH_SHORT).show()
                }

                val p = st.profile

                // Full name
                tvFullName.text = p?.fullName?.takeIf { it.isNotBlank() } ?: "Người dùng"

                // Phone
                tvPhone.text = p?.phone?.takeIf { it.isNotBlank() } ?: "Chưa có số điện thoại"

                // Role
                tvRole.text = p?.role?.takeIf { it.isNotBlank() } ?: "user"

                // Avatar
                val avatarUrl = p?.avatarUrl.orEmpty()
                ivAvatar.load(avatarUrl) {
                    placeholder(R.drawable.ic_avatar_placeholder)
                    error(R.drawable.ic_avatar_placeholder)
                    crossfade(true)
                }
            }
        }
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        ivAvatar = findViewById(R.id.ivAvatar)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvUid = findViewById(R.id.tvUid)
        tvPhone = findViewById(R.id.tvPhone)
        tvRole = findViewById(R.id.tvRole)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClicks() {
        btnBack.setOnClickListener { finish() }
        btnLogout.setOnClickListener { logout() }

        // Click vào avatar để chọn ảnh mới
        ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun uploadAvatar(uri: android.net.Uri) {
        Toast.makeText(this, "Đang upload avatar...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val newUrl = profileRepo.uploadAvatar(uri)
                Toast.makeText(this@ProfileActivity, "Upload thành công!", Toast.LENGTH_SHORT).show()

                // Reload avatar
                ivAvatar.load(newUrl) {
                    placeholder(R.drawable.ic_avatar_placeholder)
                    error(R.drawable.ic_avatar_placeholder)
                    crossfade(true)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Upload lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun logout() {
        AuthStore.clear(this)
        // nếu bạn có ProfileStore local thì clear luôn (không có cũng không sao)
        runCatching { ProfileStore.clear(this) }

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

        val i = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(i)
        finish()
    }
}

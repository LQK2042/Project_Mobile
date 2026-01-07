package com.example.doanck.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.doanck.R
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.feature.profile.ProfileStore

/**
 * ProfileFragment - hiển thị thông tin user profile
 * Dùng trong Navigation Component thay cho ProfileActivity khi cần
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var btnBack: ImageButton
    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvUid: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnLogout: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nếu chưa đăng nhập -> về màn trước
        if (!AuthStore.isLoggedIn(requireContext())) {
            findNavController().popBackStack()
            return
        }

        bindViews(view)
        setupClicks()

        // Email + UID lấy từ AuthStore
        val email = AuthStore.email(requireContext()).orEmpty()
        val uid = AuthStore.userId(requireContext()).orEmpty()

        tvEmail.text = if (email.isNotBlank()) email else "Chưa có email"
        tvUid.text = if (uid.isNotBlank()) uid else "Chưa có userId"

        // Set default UI
        ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
        tvFullName.text = "Người dùng"
        tvPhone.text = "Chưa có số điện thoại"
        tvRole.text = "user"

        // Load profile từ local store nếu có
        loadProfileFromStore()
    }

    private fun bindViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvUid = view.findViewById(R.id.tvUid)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvRole = view.findViewById(R.id.tvRole)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupClicks() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadProfileFromStore() {
        // Load từ ProfileStore local
        try {
            val p = ProfileStore.get(requireContext())
            tvFullName.text = p.fullName.takeIf { it.isNotBlank() } ?: "Người dùng"
            tvPhone.text = p.phone.takeIf { it.isNotBlank() } ?: "Chưa có số điện thoại"
            tvRole.text = p.role.takeIf { it.isNotBlank() } ?: "user"
        } catch (_: Exception) {
            // ProfileStore không tồn tại hoặc lỗi -> bỏ qua
        }
    }

    private fun logout() {
        AuthStore.clear(requireContext())
        runCatching { ProfileStore.clear(requireContext()) }

        Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()

        // Quay về Home
        findNavController().popBackStack(R.id.homeFragment, false)
    }
}


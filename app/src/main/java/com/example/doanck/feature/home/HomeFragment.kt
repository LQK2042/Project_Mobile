package com.example.doanck.feature.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.example.doanck.core.supabase.AuthStore
import com.example.doanck.feature.auth.EXTRA_RETURN_RESULT
import com.example.doanck.feature.auth.LoginActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val vm: HomeViewModel by viewModels()

    // ===== Location =====
    private val fused: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(requireActivity())
    }

    // ===== Views =====
    private lateinit var tvAddress: TextView
    private lateinit var btnLocate: ImageButton
    private lateinit var rvShops: RecyclerView
    private lateinit var adapter: ShopAdapter
    private lateinit var navCart: ImageButton

    // Pending action after login
    private var pendingAfterLogin: (() -> Unit)? = null

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK && AuthStore.isLoggedIn(requireContext())) {
                pendingAfterLogin?.invoke()
            }
            pendingAfterLogin = null
        }

    // ===== Local store =====
    private val locationPrefs by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }

    private companion object {
        const val KEY_ADDR = "addr_text"
    }

    // 1) Xin quyền
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val granted = fine || coarse

            if (granted) ensureLocationSettingsThenFetch()
            else Toast.makeText(requireContext(), "Bạn chưa cho phép quyền vị trí", Toast.LENGTH_SHORT).show()
        }

    // 2) Bật GPS/Location Services (popup hệ thống)
    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                fetchAndShowAddress()
            } else {
                Toast.makeText(requireContext(), "Bạn chưa bật vị trí", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAddress = view.findViewById(R.id.tvCurrentAddress)
        btnLocate = view.findViewById(R.id.btnLocate)
        rvShops = view.findViewById(R.id.rvShops)
        navCart = view.findViewById(R.id.navCart)

        // Hiện địa chỉ đã lưu nếu có
        tvAddress.text = locationPrefs.getString(KEY_ADDR, "Chọn địa chỉ")

        // Bấm vào địa chỉ -> hiện dialog Cập nhật / Hủy
        tvAddress.setOnClickListener { showUpdateDialog() }

        // Icon -> cập nhật luôn
        btnLocate.setOnClickListener { startAutoUpdateLocation() }

        adapter = ShopAdapter { shop ->
            requireLoginThen {
                val intent = Intent().setClassName(
                    requireContext(),
                    "com.example.doanck.feature.shop.ShopDetailActivity"
                ).putExtra("shop_id", shop.id)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Chưa tìm thấy ShopDetailActivity", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvShops.adapter = adapter

        navCart.setOnClickListener {
            requireLoginThen {
                val intent = Intent().setClassName(
                    requireContext(),
                    "com.example.doanck.feature.order.OrderHistoryActivity"
                )
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Chưa tìm thấy OrderHistoryActivity", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.shops.collect { adapter.submitList(it) }
        }
        vm.load()
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cập nhật địa chỉ giao hàng?")
            .setMessage("Dùng vị trí hiện tại để cập nhật địa chỉ giao hàng.")
            .setPositiveButton("Cập nhật") { _, _ ->
                startAutoUpdateLocation()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun startAutoUpdateLocation() {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        ensureLocationSettingsThenFetch()
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun ensureLocationSettingsThenFetch() {
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true) // luôn show popup bật vị trí nếu đang tắt
            .build()

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnSuccessListener { fetchAndShowAddress() }
            .addOnFailureListener { e ->
                val rae = e as? ResolvableApiException
                if (rae != null) {
                    val intentSender = IntentSenderRequest.Builder(rae.resolution).build()
                    locationSettingsLauncher.launch(intentSender)
                } else {
                    Toast.makeText(requireContext(), "Không kiểm tra được trạng thái vị trí", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndShowAddress() {
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Toast.makeText(requireContext(), "Không lấy được vị trí, hãy bật GPS", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val addr = reverseGeocode(loc.latitude, loc.longitude) ?: "Vị trí hiện tại"
                saveAddress(addr)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi lấy vị trí", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(requireContext(), Locale("vi", "VN"))
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.firstOrNull()?.getAddressLine(0)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveAddress(addr: String) {
        locationPrefs.edit().putString(KEY_ADDR, addr).apply()
        tvAddress.text = addr
    }

    private fun requireLoginThen(action: () -> Unit) {
        if (AuthStore.isLoggedIn(requireContext())) {
            action()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cần đăng nhập")
            .setMessage("Bạn cần đăng nhập để đặt món hoặc xem đơn hàng.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Đăng nhập") { _, _ ->
                pendingAfterLogin = action
                val i = Intent(requireContext(), LoginActivity::class.java)
                    .putExtra(EXTRA_RETURN_RESULT, true)
                loginLauncher.launch(i)
            }
            .show()
    }
}

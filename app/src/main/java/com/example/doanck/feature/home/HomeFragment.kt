package com.example.doanck.feature.home

import android.Manifest
import android.app.Activity
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
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
    private lateinit var etLocation: EditText
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton

    private lateinit var ibProfile: ImageButton
    private lateinit var navHome: ImageButton
    private lateinit var navCart: ImageButton
    private lateinit var rvFoods: RecyclerView

    // ===== Local store =====
    private val locationPrefs by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }

    private val authPrefs by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    private companion object {
        const val KEY_ADDR = "addr_text"

        // Bạn lưu token/user_id khi login thành công (tuỳ backend)
        const val KEY_TOKEN = "access_token"
        const val KEY_USER_ID = "user_id"

        // dùng để truyền “mục tiêu” sang Login (đăng nhập xong đi tiếp)
        const val ARG_NEXT = "next"
        const val NEXT_CART = "cart"
        const val NEXT_PRODUCT = "product"
        const val ARG_PRODUCT_ID = "product_id"
    }
    private fun navToIdName(idName: String, args: Bundle? = null) {
        val id = resources.getIdentifier(idName, "id", requireContext().packageName)
        if (id == 0) {
            Toast.makeText(requireContext(), "Chưa có destination id: @$idName trong nav_graph", Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(id, args)
    }

    // ===== Permission launcher =====
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) ensureLocationSettingsThenFetch()
            else Toast.makeText(requireContext(), "Bạn chưa cho phép quyền vị trí", Toast.LENGTH_SHORT).show()
        }

    // ===== Location settings launcher =====
    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) fetchAndShowAddress()
            else Toast.makeText(requireContext(), "Bạn chưa bật vị trí", Toast.LENGTH_SHORT).show()
        }

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }
    private val settingsClient by lazy { LocationServices.getSettingsClient(requireActivity()) }

    private lateinit var tvAddress: TextView
    private lateinit var btnLocate: ImageButton

    private val prefs by lazy(LazyThreadSafetyMode.NONE) {
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

        // 1️⃣ RecyclerView
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)

        adapter = ShopAdapter { shop ->
            val args = bundleOf(
                "shopId" to shop.id,
                "shopName" to shop.name
            )
            findNavController().navigate(
                R.id.action_home_to_shopDetail,
                args
            )
        }

        rv.adapter = adapter

        // 2️⃣ OBSERVE DATA  <<< ĐOẠN BẠN HỎI Ở ĐÂY
        lifecycleScope.launch {
            vm.shops.collect { shops ->
                adapter.submitList(shops)
            }
        }

        // 3️⃣ Load data
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
            .setAlwaysShow(true) // ✅ luôn show popup bật vị trí nếu đang tắt
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
        prefs.edit().putString(KEY_ADDR, addr).apply()
        tvAddress.text = addr
    }
}

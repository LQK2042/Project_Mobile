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
<<<<<<< HEAD
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
=======
import android.widget.ImageButton
import android.widget.TextView
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
<<<<<<< HEAD
=======
import androidx.core.os.bundleOf
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
<<<<<<< HEAD
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
=======
import androidx.recyclerview.widget.RecyclerView
import com.example.doanck.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
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

<<<<<<< HEAD
        // --- bind views (phải đúng id trong fragment_home.xml) ---
        etLocation = view.findViewById(R.id.etLocation)
        etSearch = view.findViewById(R.id.etSearch)
        btnSearch = view.findViewById(R.id.btnSearch)

        ibProfile = view.findViewById(R.id.ibProfile)   // nút profile ở góc trên
        navHome = view.findViewById(R.id.navHome)       // icon home dưới
        navCart = view.findViewById(R.id.navCart)       // icon cart dưới
        rvFoods = view.findViewById(R.id.rvFoods)       // recycler grid

        // --- load địa chỉ đã lưu ---
        etLocation.setText(locationPrefs.getString(KEY_ADDR, ""))

        // --- setup grid foods (demo) ---
        val foods = demoFoods()
        val adapter = FoodAdapter(foods) { item ->
            // Click món ăn -> hiện thông báo (chưa có màn chi tiết)
            Toast.makeText(requireContext(), "Food id=${item.id}", Toast.LENGTH_SHORT).show()
        }

        rvFoods.layoutManager = GridLayoutManager(requireContext(), 2)
        rvFoods.adapter = adapter

        // --- Location click ---
        etLocation.setOnClickListener { startAutoUpdateLocation() }

        // --- Search ---
        btnSearch.setOnClickListener {
            val query = etSearch.text?.toString().orEmpty().trim()
            Toast.makeText(requireContext(), if (query.isEmpty()) "Enter food to search" else "Searching food: $query", Toast.LENGTH_SHORT).show()
        }
        ibProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Profile screen not available", Toast.LENGTH_SHORT).show()
        }

        // --- Bottom nav ---
        navHome.setOnClickListener {
            // đang ở home thì thôi; hoặc pop về home nếu bạn dùng nested nav
            // findNavController().popBackStack(R.id.homeFragment, false)
            Toast.makeText(requireContext(), "Home", Toast.LENGTH_SHORT).show()
        }

        navCart.setOnClickListener {
            Toast.makeText(requireContext(), "Cart screen not available", Toast.LENGTH_SHORT).show()
        }
    }

    // ===================== LOGIN GATE =====================

    private fun isLoggedIn(): Boolean {
        // tuỳ bạn lưu gì khi login thành công:
        val token = authPrefs.getString(KEY_TOKEN, null)
        val userId = authPrefs.getString(KEY_USER_ID, null)
        return !token.isNullOrBlank() || !userId.isNullOrBlank()
    }

    /**
     * Nếu chưa login -> show dialog + chuyển sang Login.
     * Nếu đã login -> chạy action.
     *
     * @param next: bundle để truyền cho Login biết "đăng nhập xong đi đâu"
     */
    private fun requireLoginThen(next: Bundle?, actionIfAuthed: () -> Unit) {
        if (isLoggedIn()) {
            actionIfAuthed()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cần đăng nhập")
            .setMessage("Bạn cần đăng nhập để đặt món hoặc xem đơn hàng.")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Đăng nhập") { _, _ ->
                Toast.makeText(requireContext(), "Login screen not available", Toast.LENGTH_SHORT).show()
=======
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
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
            }
            .show()
    }

    // ===================== LOCATION =====================

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
            .setAlwaysShow(true)
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
                val lat = loc.latitude
                val lng = loc.longitude

                val addr = reverseGeocode(lat, lng) ?: "Vị trí hiện tại"
                saveAddress(addr)

                // Nếu bạn có LocationStore DataStore thì mở lại dòng này
                // viewLifecycleOwner.lifecycleScope.launch { LocationStore.saveLatLng(requireContext(), lat, lng) }
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
        etLocation.setText(addr)
    }

    // ===================== DEMO GRID DATA =====================

    private fun demoFoods(): List<FoodUi> {
        val placeholder = android.R.drawable.ic_menu_gallery
        return listOf(
            FoodUi("1", placeholder),
            FoodUi("2", placeholder),
            FoodUi("3", placeholder),
            FoodUi("4", placeholder),
            FoodUi("5", placeholder),
            FoodUi("6", placeholder),
            FoodUi("7", placeholder),
            FoodUi("8", placeholder),
        )
    }

    // ===================== ADAPTER (self-contained) =====================

    data class FoodUi(
        val id: String,
        val imageRes: Int
    )

    private class FoodAdapter(
        private val items: List<FoodUi>,
        private val onClick: (FoodUi) -> Unit
    ) : RecyclerView.Adapter<FoodAdapter.VH>() {

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val img: com.google.android.material.imageview.ShapeableImageView =
                itemView.findViewById(R.id.imgFood)
        }

<<<<<<< HEAD
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_food_circle, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.img.setImageResource(item.imageRes)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = items.size
=======
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAddress = view.findViewById(R.id.tvCurrentAddress)
        btnLocate = view.findViewById(R.id.btnLocate) // ⚠️ đảm bảo XML đúng id này

        // Hiện địa chỉ đã lưu nếu có
        tvAddress.text = prefs.getString(KEY_ADDR, "Chọn địa chỉ")

        // ✅ Bấm vào địa chỉ -> hiện dialog Cập nhật / Hủy
        tvAddress.setOnClickListener { showUpdateDialog() }

        // ✅ Icon vòng vàng -> cập nhật luôn
        btnLocate.setOnClickListener { startAutoUpdateLocation() }

        // RecyclerView shops (giữ như bạn)
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)
        adapter = ShopAdapter { shop ->
            val args = bundleOf("shopId" to shop.id, "shopName" to shop.name)
            findNavController().navigate(R.id.action_home_to_shopDetail, args)
        }
        rv.adapter = adapter

        lifecycleScope.launch {
            vm.shops.collect { adapter.submitList(it) }
        }
        vm.load()
>>>>>>> a069341194d7ff5844d7cd893c81f7cc4c5d0971
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

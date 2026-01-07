package com.example.doanck.feature.home

import android.Manifest
import android.app.Activity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.doanck.R
import com.example.doanck.data.local.OrderStore
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.feature.auth.EXTRA_RETURN_RESULT
import com.example.doanck.feature.auth.LoginActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val vm: HomeViewModel by viewModels()
    private lateinit var adapter: ShopAdapter

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }
    private val settingsClient by lazy { LocationServices.getSettingsClient(requireActivity()) }

    private lateinit var tvAddress: TextView
    private lateinit var btnLocate: ImageButton
    private lateinit var ibProfile: ImageButton
    private lateinit var ibCart: ImageButton
    private lateinit var btnTrackOrder: Button

    private var searchJob: Job? = null

    private val prefs by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }

    private companion object {
        const val KEY_ADDR = "addr_text"
    }

    // pending action after login
    private var pendingAfterLogin: (() -> Unit)? = null

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK && AuthStore.isLoggedIn(requireContext())) {
                pendingAfterLogin?.invoke()
            }
            pendingAfterLogin = null
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
        ibProfile = view.findViewById(R.id.ibProfile)
        ibCart = view.findViewById(R.id.ibCart)
        btnTrackOrder = view.findViewById(R.id.btnTrackOrder)

        // Hiện địa chỉ đã lưu nếu có
        tvAddress.text = prefs.getString(KEY_ADDR, "Chọn địa chỉ")

        // Bấm vào địa chỉ -> dialog cập nhật
        tvAddress.setOnClickListener { showUpdateDialog() }
        btnLocate.setOnClickListener { startAutoUpdateLocation() }

        // ✅ Cart: chuyển đến CartFragment
        ibCart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cart)
        }

        // ✅ Theo dõi đơn hàng: hiện nút nếu có đơn hiện tại
        val currentOrderId = OrderStore.currentOrderId(requireContext())
        if (!currentOrderId.isNullOrBlank()) {
            btnTrackOrder.visibility = View.VISIBLE
        }
        btnTrackOrder.setOnClickListener {
            // Không cần truyền orderId - OrderTrackingFragment sẽ tự lấy từ OrderStore
            findNavController().navigate(R.id.orderTrackingFragment)
        }

        // ✅ Profile: nếu đã login -> popup menu, nếu chưa -> chuyển login
        ibProfile.setOnClickListener {
            if (AuthStore.isLoggedIn(requireContext())) {
                showAvatarMenu()
            } else {
                findNavController().navigate(R.id.action_home_to_login)
            }
        }

        // RecyclerView shops
        val rv = view.findViewById<RecyclerView>(R.id.rvShops)
        if (rv.itemDecorationCount == 0) {
            val spacingPx = (12 * resources.displayMetrics.density).toInt()
            rv.addItemDecoration(GridSpacingItemDecoration(2, spacingPx, true))
        }

        adapter = ShopAdapter { shop ->
            // ✅ click shop -> bắt login
            requireLoginThen {
                val args = bundleOf(
                    "shopId" to shop.id,
                    "shopName" to shop.name,
                    "shopAddress" to (shop.address ?: ""),
                    "shopImageUrl" to (shop.logoUrl ?: "")
                )
                findNavController().navigate(R.id.action_home_to_shopDetail, args)
            }
        }
        rv.adapter = adapter

        // ✅ collect shops: dùng viewLifecycleOwner
        viewLifecycleOwner.lifecycleScope.launch {
            vm.shops.collect { adapter.submitList(it) }
        }
        vm.load()

        // ✅ Load và observe avatar
        vm.loadAvatar()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.avatarUrl.collectLatest { url ->
                    if (!url.isNullOrBlank()) {
                        ibProfile.load(url) {
                            placeholder(R.drawable.ic_avatar_placeholder)
                            error(R.drawable.ic_avatar_placeholder)
                            transformations(CircleCropTransformation())
                            crossfade(true)
                        }
                    } else {
                        ibProfile.setImageResource(R.drawable.ic_avatar_placeholder)
                    }
                }
            }
        }

        // ===== Search + Suggestions =====
        val sv = view.findViewById<SearchView>(R.id.svFood)
        setupSearchViewAlwaysHint(sv)

        val rvSuggest = view.findViewById<RecyclerView>(R.id.rvSuggest)
        val suggestAdapter = ProductSuggestAdapter { s ->
            // ✅ click suggestion -> bắt login
            requireLoginThen {
                val args = bundleOf("shopId" to s.shopId, "shopName" to s.shopName)
                findNavController().navigate(R.id.action_home_to_shopDetail, args)

                sv.setQuery("", false)
                sv.clearFocus()
            }
        }
        rvSuggest.adapter = suggestAdapter

        // collect suggestions
        viewLifecycleOwner.lifecycleScope.launch {
            vm.suggestions.collect { list ->
                rvSuggest.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
                suggestAdapter.submitList(list)
            }
        }

        // debounce search
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val key = newText.orEmpty()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    vm.doSearch(key)
                }
                return true
            }
        })
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
    }

    /**
     * ✅ Ép SearchView mở sẵn + hiện hint ngay từ đầu (không cần click)
     */
    private fun setupSearchViewAlwaysHint(sv: SearchView) {
        sv.setIconifiedByDefault(false)
        sv.isIconified = false
        sv.onActionViewExpanded()

        sv.queryHint = "Nay bạn muốn ăn gì?"

        val searchText = sv.findViewById<SearchView.SearchAutoComplete>(
            androidx.appcompat.R.id.search_src_text
        )
        searchText.setTextColor(Color.BLACK)
        searchText.setHintTextColor(Color.DKGRAY)

        val plate = sv.findViewById<View>(androidx.appcompat.R.id.search_plate)
        plate.setBackgroundColor(Color.TRANSPARENT)

        val magIcon = sv.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        magIcon.setColorFilter(Color.BLACK)

        sv.clearFocus()
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cập nhật địa chỉ giao hàng?")
            .setMessage("Dùng vị trí hiện tại để cập nhật địa chỉ giao hàng.")
            .setPositiveButton("Cập nhật") { _, _ -> startAutoUpdateLocation() }
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

                viewLifecycleOwner.lifecycleScope.launch {
                    val addr = withContext(Dispatchers.IO) {
                        reverseGeocode(loc.latitude, loc.longitude)
                    } ?: "Vị trí hiện tại"
                    saveAddress(addr)
                }
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

    /**
     * ✅ Popup menu khi đã đăng nhập: Profile / Logout
     */
    private fun showAvatarMenu() {
        val popup = PopupMenu(requireContext(), ibProfile)
        popup.menu.add("Profile")
        popup.menu.add("Logout")

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Profile" -> {
                    // Mở ProfileActivity
                    val intent = Intent().setClassName(
                        requireContext(),
                        "com.example.doanck.feature.profile.ProfileActivity"
                    )
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Chưa tìm thấy ProfileActivity", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                "Logout" -> {
                    AuthStore.clear(requireContext())
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    // Cập nhật UI nếu cần
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun requireLoginThen(action: () -> Unit) {
        if (AuthStore.isLoggedIn(requireContext())) {
            action()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cần đăng nhập")
            .setMessage("Bạn cần đăng nhập để xem/đặt món.")
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

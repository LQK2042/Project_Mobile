package com.example.doanck.data.repository

import android.content.Context
import android.net.Uri
import com.example.doanck.data.remote.supabase.AuthStore
import com.example.doanck.data.remote.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray

/**
 * Repository để quản lý profile user: upload avatar, lấy thông tin profile
 */
class ProfileRepository(private val context: Context) {

    private val okHttp = OkHttpClient()
    private val supabaseUrl = SupabaseConfig.SUPABASE_URL
    private val anonKey = SupabaseConfig.SUPABASE_KEY

    data class Profile(
        val avatarUrl: String? = null,
        val fullName: String? = null,
        val phone: String? = null,
        val role: String? = null
    )

    /**
     * Upload avatar lên Supabase Storage và update profiles.avatar_url
     */
    suspend fun uploadAvatar(uri: Uri): String = withContext(Dispatchers.IO) {
        val accessToken = AuthStore.accessToken(context) ?: error("Chưa login")
        val uid = AuthStore.userId(context) ?: error("Không có uid")

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Không đọc được ảnh")

        val filename = "avatar_${System.currentTimeMillis()}.jpg"
        val objectPath = "$uid/$filename"

        // 1) Upload lên Storage (bucket avatars)
        val uploadUrl = "$supabaseUrl/storage/v1/object/avatars/$objectPath"

        val uploadReq = Request.Builder()
            .url(uploadUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "image/jpeg")
            .put(bytes.toRequestBody("image/jpeg".toMediaType()))
            .build()

        okHttp.newCall(uploadReq).execute().use { res ->
            if (!res.isSuccessful) error("Upload lỗi: ${res.code} ${res.message}")
        }

        // 2) Lấy public URL
        val publicUrl = "$supabaseUrl/storage/v1/object/public/avatars/$objectPath"

        // 3) Update profiles.avatar_url qua REST
        val patchUrl = "$supabaseUrl/rest/v1/profiles?id=eq.$uid"
        val json = """{"avatar_url":"$publicUrl"}"""
        val patchReq = Request.Builder()
            .url(patchUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", anonKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .patch(json.toRequestBody("application/json".toMediaType()))
            .build()

        okHttp.newCall(patchReq).execute().use { res ->
            if (!res.isSuccessful) error("Update profile lỗi: ${res.code} ${res.message}")
        }

        publicUrl
    }

    /**
     * Lấy profile của user hiện tại từ Supabase
     */
    suspend fun getMyProfile(): Profile = withContext(Dispatchers.IO) {
        val accessToken = AuthStore.accessToken(context) ?: error("Chưa login")
        val uid = AuthStore.userId(context) ?: error("Không có uid")

        val url = "$supabaseUrl/rest/v1/profiles?id=eq.$uid&select=avatar_url,full_name,phone,role"
        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", anonKey)
            .get()
            .build()

        okHttp.newCall(req).execute().use { res ->
            if (!res.isSuccessful) error("Load profile lỗi: ${res.code}")
            val body = res.body?.string().orEmpty()
            parseProfile(body)
        }
    }

    private fun parseProfile(json: String): Profile {
        return try {
            val arr = JSONArray(json)
            if (arr.length() == 0) return Profile()

            val obj = arr.getJSONObject(0)
            Profile(
                avatarUrl = obj.optString("avatar_url").takeIf { it.isNotBlank() },
                fullName = obj.optString("full_name").takeIf { it.isNotBlank() },
                phone = obj.optString("phone").takeIf { it.isNotBlank() },
                role = obj.optString("role").takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            Profile()
        }
    }
}


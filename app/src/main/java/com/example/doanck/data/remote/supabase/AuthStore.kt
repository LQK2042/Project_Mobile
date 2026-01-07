package com.example.doanck.data.remote.supabase

import android.content.Context
import android.util.Log

object AuthStore {
    private const val TAG = "AuthStore"
    private const val PREF = "auth_prefs"
    private const val K_ACCESS = "access_token"
    private const val K_REFRESH = "refresh_token"
    private const val K_UID = "user_id"
    private const val K_EMAIL = "email"

    fun save(context: Context, access: String?, refresh: String?, uid: String?, email: String?) {
        Log.d(TAG, "save() called - access: ${access?.take(20)}..., uid: $uid, email: $email")
        val success = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(K_ACCESS, access)
            .putString(K_REFRESH, refresh)
            .putString(K_UID, uid)
            .putString(K_EMAIL, email)
            .commit() // Sử dụng commit() thay vì apply() để đảm bảo lưu ngay lập tức
        Log.d(TAG, "save() result: $success")
    }

    fun accessToken(context: Context): String? {
        val token = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_ACCESS, null)
        Log.d(TAG, "accessToken() = ${token?.take(20) ?: "null"}...")
        return token
    }

    fun isLoggedIn(context: Context): Boolean {
        val loggedIn = !accessToken(context).isNullOrBlank()
        Log.d(TAG, "isLoggedIn() = $loggedIn")
        return loggedIn
    }

    fun clear(context: Context) {
        Log.d(TAG, "clear() called")
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().commit()
    }

    fun email(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_EMAIL, null)

    fun userId(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_UID, null)
}

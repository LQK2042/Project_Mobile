package com.example.doanck.core.supabase

import android.content.Context

object AuthStore {
    private const val PREF = "auth_prefs"
    private const val K_ACCESS = "access_token"
    private const val K_REFRESH = "refresh_token"
    private const val K_UID = "user_id"
    private const val K_EMAIL = "email"

    fun save(context: Context, access: String?, refresh: String?, uid: String?, email: String?) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(K_ACCESS, access)
            .putString(K_REFRESH, refresh)
            .putString(K_UID, uid)
            .putString(K_EMAIL, email)
            .apply()
    }

    fun accessToken(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(K_ACCESS, null)

    fun isLoggedIn(context: Context): Boolean =
        !accessToken(context).isNullOrBlank()

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

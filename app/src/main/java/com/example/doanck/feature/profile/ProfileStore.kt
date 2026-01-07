package com.example.doanck.feature.profile

import android.content.Context

object ProfileStore {
    private const val PREF = "profile_prefs"
    private const val K_FULL_NAME = "full_name"
    private const val K_PHONE = "phone"
    private const val K_ROLE = "role"

    data class LocalProfile(
        val fullName: String = "",
        val phone: String = "",
        val role: String = "user"
    )

    fun get(ctx: Context): LocalProfile {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return LocalProfile(
            fullName = sp.getString(K_FULL_NAME, "") ?: "",
            phone = sp.getString(K_PHONE, "") ?: "",
            role = sp.getString(K_ROLE, "user") ?: "user"
        )
    }

    fun save(ctx: Context, p: LocalProfile) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(K_FULL_NAME, p.fullName)
            .putString(K_PHONE, p.phone)
            .putString(K_ROLE, p.role)
            .apply()
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

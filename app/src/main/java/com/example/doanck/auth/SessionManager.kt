package com.example.doanck.auth

import android.content.Context
import com.example.doanck.data.remote.supabase.AuthStore

class SessionManager(private val context: Context) {

    fun isLoggedIn(): Boolean {
        val uid = AuthStore.userId(context)
        val token = AuthStore.accessToken(context)
        return !uid.isNullOrBlank() && !token.isNullOrBlank()
    }

    fun getUserId(): String? = AuthStore.userId(context)

    fun getAccessToken(): String? = AuthStore.accessToken(context)
}

package com.example.doanck.data.remote.supabase

object SupabaseConfig {
    // ✅ base URL (KHÔNG có /rest/v1)
    const val SUPABASE_URL: String = "https://qjatgukztpwjvyuxwfoe.supabase.co"

    // ✅ anon / publishable key
    const val SUPABASE_KEY: String = "sb_publishable__sXepTd-mhK3v8M0svq4JQ_FMkMwDjF"

    // ✅ Deep link nhận token reset (phần D mình hướng dẫn cấu hình)

    const val RECOVER_REDIRECT_TO: String = "doanck://auth"
}
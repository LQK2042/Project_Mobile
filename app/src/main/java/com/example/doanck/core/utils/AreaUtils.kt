package com.example.doanck.core.utils

fun extractArea(address: String?): String {
    if (address.isNullOrBlank()) return ""
    return address.split(",").lastOrNull()?.trim().orEmpty()
}

fun normalizeArea(area: String): String {
    val a = area.lowercase()
    return when {
        a.contains("hcm") || a.contains("hồ chí minh") || a.contains("ho chi minh") -> "TP. HCM"
        a.contains("bình dương") || a.contains("binh duong") -> "Bình Dương"
        a.contains("đồng nai") || a.contains("dong nai") -> "Đồng Nai"
        a.contains("hà nội") || a.contains("ha noi") -> "Hà Nội"
        else -> area
    }
}
package com.example.doanck.data.local

import android.content.Context

/**
 * Lưu trữ orderId của đơn hàng hiện tại
 * Khi tạo đơn mới → ghi đè orderId cũ → "đơn hiện tại" luôn là đơn mới nhất
 */
object OrderStore {
    private const val PREF = "order_prefs"
    private const val KEY_CURRENT_ORDER_ID = "current_order_id"

    fun saveCurrentOrderId(context: Context, orderId: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENT_ORDER_ID, orderId)
            .apply()
    }

    fun currentOrderId(context: Context): String? {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_CURRENT_ORDER_ID, null)
    }

    fun clearCurrentOrderId(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CURRENT_ORDER_ID)
            .apply()
    }
}


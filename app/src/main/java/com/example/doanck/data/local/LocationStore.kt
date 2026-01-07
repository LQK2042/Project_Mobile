package com.example.doanck.data.local

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "location_prefs")

data class SavedAddress(val address: String, val lat: Double?, val lng: Double?)

object LocationStore {
    private val KEY_ADDR = stringPreferencesKey("addr_text")
    private val KEY_LAT = doublePreferencesKey("addr_lat")
    private val KEY_LNG = doublePreferencesKey("addr_lng")

    suspend fun get(context: Context): SavedAddress? {
        val prefs = context.dataStore.data.first()
        val addr = prefs[KEY_ADDR] ?: return null
        return SavedAddress(addr, prefs[KEY_LAT], prefs[KEY_LNG])
    }

    suspend fun save(context: Context, address: String, lat: Double?, lng: Double?) {
        context.dataStore.edit { p ->
            p[KEY_ADDR] = address
            if (lat != null && lng != null) {
                p[KEY_LAT] = lat
                p[KEY_LNG] = lng
            } else {
                p.remove(KEY_LAT); p.remove(KEY_LNG)
            }
        }
    }
}

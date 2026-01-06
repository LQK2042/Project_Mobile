package com.example.doanck.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Exposed DataStore extension so callers can use `context.locationDataStore`
val Context.locationDataStore by preferencesDataStore(name = "location_prefs")

data class SavedAddress(val address: String, val lat: Double?, val lng: Double?)

object LocationStore {
    private val KEY_ADDR = stringPreferencesKey("addr_text")
    private val KEY_LAT = longPreferencesKey("lat_bits")
    private val KEY_LNG = longPreferencesKey("lng_bits")

    suspend fun get(context: Context): SavedAddress? {
        val prefs = context.locationDataStore.data.first()
        val addr = prefs[KEY_ADDR] ?: return null
        val lat = prefs[KEY_LAT]?.let { Double.fromBits(it) }
        val lng = prefs[KEY_LNG]?.let { Double.fromBits(it) }
        return SavedAddress(addr, lat, lng)
    }

    suspend fun save(context: Context, address: String, lat: Double?, lng: Double?) {
        context.locationDataStore.edit { p ->
            p[KEY_ADDR] = address
            if (lat != null && lng != null) {
                p[KEY_LAT] = lat.toBits()
                p[KEY_LNG] = lng.toBits()
            } else {
                p.remove(KEY_LAT); p.remove(KEY_LNG)
            }
        }
    }

    // Convenience: save only lat/lng
    suspend fun saveLatLng(context: Context, lat: Double, lng: Double) {
        context.locationDataStore.edit { prefs ->
            prefs[KEY_LAT] = lat.toBits()
            prefs[KEY_LNG] = lng.toBits()
        }
    }

    // Helper: flow of lat/lng pair (defaults to 0.0 when not set)
    fun latLngFlow(context: Context): Flow<Pair<Double, Double>> =
        context.locationDataStore.data.map { prefs ->
            val lat = prefs[KEY_LAT]?.let { Double.fromBits(it) } ?: 0.0
            val lng = prefs[KEY_LNG]?.let { Double.fromBits(it) } ?: 0.0
            lat to lng
        }
}

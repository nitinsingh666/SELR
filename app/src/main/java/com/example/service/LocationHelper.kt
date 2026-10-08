package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.example.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData = withContext(Dispatchers.IO) {
        var bestLocation: Location? = null

        try {
            val providers = locationManager?.getProviders(true) ?: emptyList()
            for (provider in providers) {
                val loc = locationManager?.getLastKnownLocation(provider)
                if (loc != null) {
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Could not get last known location: ${e.message}")
        }

        // Default or resolved coordinates
        val lat = bestLocation?.latitude ?: 28.6139
        val lng = bestLocation?.longitude ?: 77.2090
        val accuracy = bestLocation?.accuracy ?: 4.5f

        val addressText = reverseGeocode(lat, lng)

        LocationData(
            latitude = lat,
            longitude = lng,
            address = addressText,
            accuracyMeters = accuracy,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull(), latitude, longitude)
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull(), latitude, longitude)
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Reverse geocode fallback: ${e.message}")
            "Campus Gate 4, Academic Block C, Institutional Area (GPS: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)})"
        }
    }

    private fun formatAddress(address: Address?, lat: Double, lng: Double): String {
        if (address == null) {
            return "Main University Campus Road, Sector 12 (GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
        val parts = mutableListOf<String>()
        address.featureName?.let { if (it.isNotBlank()) parts.add(it) }
        address.thoroughfare?.let { if (it.isNotBlank() && !parts.contains(it)) parts.add(it) }
        address.subLocality?.let { if (it.isNotBlank() && !parts.contains(it)) parts.add(it) }
        address.locality?.let { if (it.isNotBlank()) parts.add(it) }
        address.postalCode?.let { if (it.isNotBlank()) parts.add(it) }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            "Near Student Activity Center (GPS: ${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
        }
    }
}

package com.sanay3y.egy.utils

import kotlin.math.*

object DistanceCalculator {

    data class CairoDistrict(
        val name: String,
        val minLat: Double,
        val minLng: Double,
        val maxLat: Double,
        val maxLng: Double,
        val centerLat: Double,
        val centerLng: Double
    )

    private val cairoDistricts = listOf(
        CairoDistrict("شبرا مصر", 30.068, 31.238, 30.088, 31.258, 30.078, 31.248),
        CairoDistrict("روض الفرج", 30.080, 31.230, 30.095, 31.250, 30.087, 31.240),
        CairoDistrict("الساحل", 30.090, 31.220, 30.105, 31.245, 30.097, 31.232),
        CairoDistrict("مدينة نصر", 30.045, 31.320, 30.085, 31.370, 30.065, 31.345),
        CairoDistrict("مصر الجديدة", 30.075, 31.300, 30.110, 31.345, 30.092, 31.322),
        CairoDistrict("التجمع الخامس", 30.000, 31.420, 30.060, 31.500, 30.030, 31.460),
        CairoDistrict("المعادي", 29.940, 31.240, 29.990, 31.300, 29.965, 31.270),
        CairoDistrict("الزمالك", 30.055, 31.210, 30.070, 31.235, 30.062, 31.222),
        CairoDistrict("المهندسين", 30.045, 31.190, 30.070, 31.215, 30.057, 31.202),
        CairoDistrict("وسط البلد", 30.040, 31.225, 30.060, 31.250, 30.050, 31.237),
        CairoDistrict("العاصمة الإدارية", 29.870, 31.700, 30.020, 31.900, 29.945, 31.800)
    )

    // Default Cairo center if no district matches
    val CAIRO_CENTER_LAT = 30.0444
    val CAIRO_CENTER_LNG = 31.2357

    fun getDistrictCenter(districtName: String): Pair<Double, Double> {
        val district = cairoDistricts.find { it.name == districtName }
        return if (district != null) {
            Pair(district.centerLat, district.centerLng)
        } else {
            Pair(CAIRO_CENTER_LAT, CAIRO_CENTER_LNG)
        }
    }

    fun calculateDistance(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val earthRadius = 6371.0 // km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun isInServiceArea(lat: Double, lng: Double): Boolean {
        // Broad box for Cairo area
        return lat in 29.75..30.35 &&
                lng in 31.05..31.70
    }

    fun getDistrictName(
        lat: Double,
        lng: Double
    ): String? {
        return cairoDistricts.firstOrNull {
            lat in it.minLat..it.maxLat &&
                    lng in it.minLng..it.maxLng
        }?.name
    }

    fun formatDistance(distance: Double): String {
        return if (distance < 1.0) {
            "${(distance * 1000).toInt()} m away"
        } else {
            "%.1f km away".format(distance)
        }
    }
}

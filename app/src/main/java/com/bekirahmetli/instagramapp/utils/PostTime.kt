package com.bekirahmetli.instagramapp.utils

class PostTime {
    private val SECOND_MILLIS: Int = 1000
    private val MINUTE_MILLIS: Int = 60 * SECOND_MILLIS
    private val HOUR_MILLIS: Int = 60 * MINUTE_MILLIS
    private val DAY_MILLIS: Int = 24 * HOUR_MILLIS


    fun getTimeAgo(time: Long): String? {
        var time = time
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now: Long = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }


        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "Az Önce"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "1 Dakika Önce"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " Dakika Önce"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "1 Saat Önce"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " Saat Önce"
        } else if (diff < 48 * HOUR_MILLIS) {
            "Dün"
        } else {
            (diff / DAY_MILLIS).toString() + " Gün Önce"
        }
    }
}
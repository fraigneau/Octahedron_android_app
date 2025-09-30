package com.octahedron.ui.helper

import java.time.Instant

fun durationOrBlank(ms: Long?): String? =
    ms?.let { formatHms(it) }

fun formatHms(totalMs: Long): String {
    val totalSec = (totalMs / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

fun formatHm(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}min" else "${m}min"
}

fun formatInstantShort(instant: Instant?): String {
    if (instant == null) return "—"
    val dt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    val today = java.time.LocalDate.now()
    return if (dt.toLocalDate() == today) {
        "%02d:%02d:%02d".format(dt.hour, dt.minute, dt.second)
    } else {
        "${dt.dayOfMonth}/${dt.monthValue} %02d:%02d:%02d".format(dt.hour, dt.minute, dt.second)
    }
}
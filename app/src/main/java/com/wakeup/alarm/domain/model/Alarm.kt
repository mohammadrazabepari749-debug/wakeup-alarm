package com.wakeup.alarm.domain.model

import java.util.Locale

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Wake Up!",
    val isEnabled: Boolean = true,
    val daysOfWeek: Set<Int> = emptySet(),
    val verificationDurationSeconds: Int = 120,
    val ringtoneUri: String? = null,
    val vibrate: Boolean = true
) {
    fun getTimeFormatted(): String {
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        val minuteFormatted = String.format(Locale.getDefault(), "%02d", minute)
        val amPm = if (hour < 12) "AM" else "PM"
        return "$displayHour:$minuteFormatted $amPm"
    }

    fun getDaysFormatted(): String {
        if (daysOfWeek.isEmpty()) return "Once"
        if (daysOfWeek.size == 7) return "Everyday"
        if (daysOfWeek == setOf(2, 3, 4, 5, 6)) return "Weekdays"
        if (daysOfWeek == setOf(1, 7)) return "Weekends"

        val dayNames = mapOf(
            1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
            5 to "Thu", 6 to "Fri", 7 to "Sat"
        )
        return daysOfWeek.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}

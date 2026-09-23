package com.example.utils

import java.util.Locale

object TimeUtils {

    /**
     * Converts any time string (24h or 12h) to canonical 12-hour AM/PM format: "hh:mm AM" or "hh:mm PM".
     * Examples:
     *  - "14:00" -> "02:00 PM"
     *  - "09:30" -> "09:30 AM"
     *  - "0:00" -> "12:00 AM"
     *  - "12:00" -> "12:00 PM"
     *  - "19:30" -> "07:30 PM"
     *  - "1:30 PM" -> "01:30 PM"
     */
    fun formatToAmPm(raw: String): String {
        if (raw.isBlank()) return "12:00 PM"

        val trimmed = raw.trim()
        val upper = trimmed.uppercase(Locale.US)

        // Check if it already has AM or PM
        if (upper.contains("AM") || upper.contains("PM")) {
            val isPm = upper.contains("PM")
            val cleaned = upper.replace("AM", "").replace("PM", "").trim()
            val parts = cleaned.split(":")
            if (parts.isNotEmpty()) {
                val hourPart = parts[0].toIntOrNull() ?: 12
                val minPart = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
                val normalizedHour = when {
                    hourPart < 1 -> 12
                    hourPart > 12 -> if (hourPart <= 23) (hourPart - 12).coerceIn(1, 12) else 12
                    else -> hourPart
                }
                return String.format(Locale.US, "%02d:%02d %s", normalizedHour, minPart.coerceIn(0, 59), if (isPm) "PM" else "AM")
            }
        }

        // Parse as 24-hour format (e.g. "14:00" or "09:30")
        val parts = trimmed.split(":")
        if (parts.isNotEmpty()) {
            val hour24 = parts[0].toIntOrNull() ?: 12
            val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
            return formatHourMinuteToAmPm(hour24, minute)
        }

        return "12:00 PM"
    }

    /**
     * Formats 24-hour hour and minute into "hh:mm AM/PM".
     */
    fun formatHourMinuteToAmPm(hour24: Int, minute: Int): String {
        val safeHour = (hour24 % 24 + 24) % 24
        val safeMin = minute.coerceIn(0, 59)
        val isPm = safeHour >= 12
        val hour12 = when (safeHour) {
            0 -> 12
            in 1..12 -> safeHour
            else -> safeHour - 12
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, safeMin, if (isPm) "PM" else "AM")
    }

    /**
     * Formats 12-hour values: hour (1..12), minute (0..59), and isPm into "hh:mm AM/PM".
     */
    fun formatAmPm(hour12: Int, minute: Int, isPm: Boolean): String {
        val safeHour = when {
            hour12 <= 0 -> 12
            hour12 > 12 -> 12
            else -> hour12
        }
        val safeMin = minute.coerceIn(0, 59)
        return String.format(Locale.US, "%02d:%02d %s", safeHour, safeMin, if (isPm) "PM" else "AM")
    }

    /**
     * Parses any time string into Triple(hour12: Int, minute: Int, isPm: Boolean).
     */
    fun parseTo12HourMinute(raw: String): Triple<Int, Int, Boolean> {
        val canonical = formatToAmPm(raw)
        val isPm = canonical.endsWith("PM")
        val timePart = canonical.substringBefore(" ").trim()
        val parts = timePart.split(":")
        val hour12 = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Triple(hour12, minute, isPm)
    }

    /**
     * Parses time to 24-hour hour and minute Pair(hour24, minute).
     */
    fun parseToHourMinute(raw: String): Pair<Int, Int> {
        val (h12, min, isPm) = parseTo12HourMinute(raw)
        val h24 = when {
            isPm && h12 < 12 -> h12 + 12
            !isPm && h12 == 12 -> 0
            else -> h12
        }
        return Pair(h24, min)
    }

    /**
     * Returns minutes of day (0..1439) for chronological comparison and sorting.
     */
    fun timeToMinutesOfDay(timeStr: String): Int {
        val (h24, min) = parseToHourMinute(timeStr)
        return h24 * 60 + min
    }

    /**
     * Formats a schedule display string: "09:00 AM → 10:30 AM" or "09:00 AM → College"
     */
    fun formatScheduleItemDisplay(startTime: String, title: String): String {
        val formattedTime = formatToAmPm(startTime)
        return "$formattedTime → $title"
    }
}

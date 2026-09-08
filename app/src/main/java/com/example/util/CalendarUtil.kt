package com.example.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CalendarUtil {

    // Returns current Year (e.g. 2026)
    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    // Returns current Month (1..12)
    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    // Returns current Day of Month (1..31)
    fun getCurrentDayOfMonth(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    // Formats today's date in ISO YYYY-MM-DD
    fun getTodayIsoString(): String {
        val cal = Calendar.getInstance()
        return formatIsoDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun formatIsoDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    // Calculates valid scheduled date for a month handling short months (e.g. day 31 -> 28/30)
    fun calculateScheduledDate(year: Int, month: Int, targetDay: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val safeDay = targetDay.coerceIn(1, maxDays)
        return formatIsoDate(year, month, safeDay)
    }

    // Arabic Month Names
    private val arabicMonthNames = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun getArabicMonthName(month: Int): String {
        val index = (month - 1).coerceIn(0, 11)
        return arabicMonthNames[index]
    }

    fun getArabicMonthYearString(year: Int, month: Int): String {
        return "${getArabicMonthName(month)} $year"
    }

    fun formatArabicDisplayDate(isoDate: String): String {
        try {
            val parts = isoDate.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                return "$day ${getArabicMonthName(month)} $year"
            }
        } catch (_: Exception) {}
        return isoDate
    }

    fun formatDateTimeArabic(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    // Helper to get all days in a month for calendar grid
    data class CalendarDay(
        val dayNumber: Int,
        val isoDate: String,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )

    fun getMonthCalendarGrid(year: Int, month: Int): List<CalendarDay> {
        val grid = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val todayIso = getTodayIsoString()
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday=1, Saturday=7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val prevMaxDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH) + 1

        val paddingDays = firstDayOfWeek - 1
        for (i in (prevMaxDays - paddingDays + 1)..prevMaxDays) {
            val iso = formatIsoDate(prevYear, prevMonth, i)
            grid.add(CalendarDay(i, iso, isCurrentMonth = false, isToday = iso == todayIso))
        }

        // Current month days
        for (d in 1..daysInMonth) {
            val iso = formatIsoDate(year, month, d)
            grid.add(CalendarDay(d, iso, isCurrentMonth = true, isToday = iso == todayIso))
        }

        // Next month padding to fill grid 35 or 42
        val nextCal = cal.clone() as Calendar
        nextCal.add(Calendar.MONTH, 1)
        val nextYear = nextCal.get(Calendar.YEAR)
        val nextMonth = nextCal.get(Calendar.MONTH) + 1

        var nextDay = 1
        while (grid.size % 7 != 0) {
            val iso = formatIsoDate(nextYear, nextMonth, nextDay)
            grid.add(CalendarDay(nextDay, iso, isCurrentMonth = false, isToday = iso == todayIso))
            nextDay++
        }

        return grid
    }

    // Add event to Native Android Device Calendar
    fun addAssistanceToDeviceCalendar(
        context: Context,
        personName: String,
        scheduledIsoDate: String,
        totalAmount: Double
    ): Boolean {
        return try {
            val parts = scheduledIsoDate.split("-")
            if (parts.size != 3) return false
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()

            val cal = Calendar.getInstance()
            cal.set(year, month, day, 9, 0, 0) // 9:00 AM
            val startMillis = cal.timeInMillis
            val endMillis = startMillis + (60 * 60 * 1000) // 1 hour

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "مساعدة $personName الشهرية - إخوة الرب")
                putExtra(CalendarContract.Events.DESCRIPTION, "مساعدة كنسية بقيمة $totalAmount جنيه")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                putExtra(CalendarContract.Events.EVENT_LOCATION, "الكنيسة")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

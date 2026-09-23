package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CalendarEventEntity
import com.example.data.model.FocusSearchAppEntity
import com.example.data.model.FocusSearchSiteEntity
import com.example.data.model.TaskEntity
import com.example.utils.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Study Flow", appName)
    }

    @Test
    fun `time utils formatting to 12 hour AM PM`() {
        assertEquals("09:00 AM", TimeUtils.formatToAmPm("09:00"))
        assertEquals("01:00 PM", TimeUtils.formatToAmPm("13:00"))
        assertEquals("05:30 PM", TimeUtils.formatToAmPm("17:30"))
        assertEquals("07:30 PM", TimeUtils.formatToAmPm("19:30"))
        assertEquals("12:00 PM", TimeUtils.formatToAmPm("12:00"))
        assertEquals("12:00 AM", TimeUtils.formatToAmPm("00:00"))
        assertEquals("09:30 AM", TimeUtils.formatToAmPm("9:30 AM"))
        assertEquals("08:15 PM", TimeUtils.formatToAmPm("8:15 PM"))

        val (h12, min, isPm) = TimeUtils.parseTo12HourMinute("07:30 PM")
        assertEquals(7, h12)
        assertEquals(30, min)
        assertTrue(isPm)

        val canonical = TimeUtils.formatAmPm(7, 30, true)
        assertEquals("07:30 PM", canonical)
    }

    @Test
    fun `time utils chronological comparison`() {
        val morningMins = TimeUtils.timeToMinutesOfDay("09:00 AM")
        val noonMins = TimeUtils.timeToMinutesOfDay("01:00 PM")
        val eveningMins = TimeUtils.timeToMinutesOfDay("07:30 PM")

        assertTrue(morningMins < noonMins)
        assertTrue(noonMins < eveningMins)
        assertEquals(9 * 60, morningMins)
        assertEquals(13 * 60, noonMins)
        assertEquals(19 * 60 + 30, eveningMins)
    }

    @Test
    fun `task entity stores epoch day and 12 hour time`() {
        val today = LocalDate.now().toEpochDay()
        val task = TaskEntity(
            title = "Complete Calculus Assignment",
            notes = "Problems 1-15",
            priority = "High",
            category = "Study",
            dueDateEpochDay = today,
            dueTime = "05:30 PM"
        )
        assertEquals("Complete Calculus Assignment", task.title)
        assertEquals(today, task.dueDateEpochDay)
        assertEquals("05:30 PM", task.dueTime)
    }

    @Test
    fun `calendar event entity stores schedule times`() {
        val today = LocalDate.now().toEpochDay()
        val event = CalendarEventEntity(
            title = "College Lecture",
            description = "Computer Science 101",
            dateEpochDay = today,
            startTime = "09:00 AM",
            endTime = "10:30 AM",
            category = "Class"
        )
        assertEquals("College Lecture", event.title)
        assertEquals("09:00 AM", event.startTime)
        assertEquals("10:30 AM", event.endTime)
    }

    @Test
    fun `focus search app entity model creation`() {
        val app = FocusSearchAppEntity(
            appName = "YouTube",
            packageName = "com.google.android.youtube",
            searchActionType = "YOUTUBE",
            searchUrlTemplate = "https://www.youtube.com/results?search_query=%s",
            isEnabled = true
        )
        assertEquals("YouTube", app.appName)
        assertEquals("com.google.android.youtube", app.packageName)
        assertTrue(app.isEnabled)
    }
}

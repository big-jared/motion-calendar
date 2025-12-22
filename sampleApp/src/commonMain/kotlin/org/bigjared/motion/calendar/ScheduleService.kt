package org.bigjared.motion.calendar

import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object ScheduleService {
    fun getTestEvents(): Map<LocalDate, Set<SampleTimedEvent>> {
        val today = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Find the Sunday of this week
        val daysFromSunday = (today.dayOfWeek.ordinal - DayOfWeek.SUNDAY.ordinal + 7) % 7
        val sunday = today.plus(-daysFromSunday, DateTimeUnit.DAY)

        return mapOf(
            sunday.plus(1, DateTimeUnit.DAY) to mondayEvents,
            sunday.plus(2, DateTimeUnit.DAY) to tuesdayEvents,
            sunday.plus(3, DateTimeUnit.DAY) to wednesdayEvents,
            sunday.plus(4, DateTimeUnit.DAY) to thursdayEvents,
            sunday.plus(5, DateTimeUnit.DAY) to fridayEvents
        )
    }
}

val mondayEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 0),
        duration = 1.hours,
        color = blue,
        name = "Math"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 9, minute = 15),
        duration = 45.minutes,
        color = green,
        name = "Science"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 10, minute = 30),
        duration = 1.hours,
        color = purple,
        name = "English"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 0),
        duration = 45.minutes,
        color = orange,
        name = "History"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 14, minute = 0),
        duration = 1.hours + 30.minutes,
        color = lightBlue,
        name = "Art"
    ),
)

val tuesdayEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 30),
        duration = 1.hours,
        color = green,
        name = "Science"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 10, minute = 0),
        duration = 1.hours + 15.minutes,
        color = blue,
        name = "Math"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 30),
        duration = 45.minutes,
        color = red,
        name = "Music"
    ),
)

val wednesdayEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 0),
        duration = 1.hours,
        color = purple,
        name = "English"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 9, minute = 30),
        duration = 1.hours,
        color = blue,
        name = "Math"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 11, minute = 0),
        duration = 45.minutes,
        color = orange,
        name = "History"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 0),
        duration = 1.hours,
        color = green,
        name = "Science"
    ),
)

val thursdayEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 0),
        duration = 1.hours + 15.minutes,
        color = blue,
        name = "Math"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 10, minute = 0),
        duration = 1.hours,
        color = lightBlue,
        name = "Art"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 0),
        duration = 45.minutes,
        color = purple,
        name = "English"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 14, minute = 0),
        duration = 45.minutes,
        color = red,
        name = "Music"
    ),
)

val fridayEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 30),
        duration = 1.hours,
        color = green,
        name = "Science"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 10, minute = 0),
        duration = 1.hours,
        color = orange,
        name = "History"
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 11, minute = 30),
        duration = 1.hours,
        color = blue,
        name = "Math"
    ),
)

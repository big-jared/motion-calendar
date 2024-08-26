package org.bigjared.motion.calendar

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.dayGrid.TimedEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object ScheduleService {
    fun getTestEvents(): Map<LocalDate, Set<SampleTimedEvent>> = mapOf(
        now().toLocalDateTime(TimeZone.currentSystemDefault()).date to testEvents,
        now().plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date to simpleTestEvents
    )
}

val simpleTestEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 10),
        duration = 8.hours + 2.minutes,
        color = yellow
    ),
)

val testEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 10), duration = 1.hours + 2.minutes, color = blue
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 0),
        duration = 1.hours + 2.minutes,
        color = lightTurquoise
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 2), duration = 4.hours, color = red
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 20), duration = 2.hours, color = orange
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 10),
        duration = 2.hours + 2.minutes,
        color = green
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 30),
        duration = 2.hours + 2.minutes,
        color = navy
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 18, minute = 10),
        duration = 1.hours + 2.minutes,
        color = purple
    ),
)
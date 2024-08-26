package org.bigjared.motion.calendar.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

internal fun LocalDate.startOfWeek(): LocalDate {
    var start = this
    while (start.dayOfWeek != DayOfWeek.MONDAY) {
        start = start.minus(1, DateTimeUnit.DAY)
    }
    return start
}

internal fun LocalDate.startOfMonth(): LocalDate {
    var start = this
    while (start.dayOfMonth != 1) {
        start = start.minus(1, DateTimeUnit.DAY)
    }
    return start
}

internal fun LocalDate.monthDays(): List<LocalDate> {
    var current = this.startOfMonth().startOfWeek()
    val dates = mutableListOf<LocalDate>()
    while (current.month <= this.month || dates.size % 7 != 6) {
        dates.add(current)
        current = current.plus(1, DateTimeUnit.DAY)
    }
    dates.add(current)
    return dates
}

internal fun LocalTime.toHourMinuteString(showMinutes: Boolean = this.minute != 0, showAmPm: Boolean = true): String {
    val isAm = this.hour < 12
    val minuteText = if (this.minute >= 10) this.minute else "0${this.minute}"
    val hourText = if (this.hour == 0) 12 else if (this.hour < 13) this.hour else this.hour - 12
    return "$hourText${if (showMinutes) ":$minuteText" else ""}${if (showAmPm) if (isAm) " am" else " pm" else ""}"
}

internal fun LocalDate.toEpochMs(): Long = (this.toEpochDays()) * 24L * 60 * 60 * 1000
internal fun Long.toLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date


internal operator fun LocalTime.plus(duration: Duration): LocalTime {
    val rollOver = this.minute + (duration.inWholeMinutes % 60) > 60
    return LocalTime(
        hour = min(23, this.hour + duration.inWholeHours.toInt() + if (rollOver) 1 else 0),
        minute = this.minute + (duration.inWholeMinutes.toInt() % 60) % 60
    )
}
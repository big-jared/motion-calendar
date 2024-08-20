package org.bigjared.motion.calendar.dayGrid

import kotlinx.datetime.LocalTime
import org.bigjared.motion.calendar.util.plus
import kotlin.time.Duration

interface TimedEvent {
    val start: LocalTime
    val duration: Duration
    val end get() = start.plus(duration)

    fun before(event: TimedEvent) = this.start < event.start

    fun overlaps(event: TimedEvent): Boolean {
        return if (this.before(event)) {
            this.end > event.start
        } else event.end > this.start
    }
}
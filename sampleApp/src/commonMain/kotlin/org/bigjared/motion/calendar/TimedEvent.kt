package org.bigjared.motion.calendar

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalTime
import org.bigjared.motion.calendar.dayGrid.TimedEvent
import kotlin.time.Duration

data class SampleTimedEvent(
    override val start: LocalTime,
    override val duration: Duration,
    val color: Color,
    val name: String,
    val description: String = ""
) : TimedEvent

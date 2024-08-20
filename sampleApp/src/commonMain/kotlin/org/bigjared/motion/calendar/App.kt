package org.bigjared.motion.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.calendar.CalendarDayDecoration
import org.bigjared.motion.calendar.calendar.CalendarDecorationAlignment
import org.bigjared.motion.calendar.calendar.MotionCalender
import org.bigjared.motion.calendar.calendar.SwipingStates
import org.bigjared.motion.calendar.calendar.rememberMotionCalendarState
import org.bigjared.motion.calendar.dayGrid.DayGrid
import org.bigjared.motion.calendar.dayGrid.TimedEvent
import org.bigjared.motion.calendar.dayGrid.rememberDayGridState
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun getTestEvents(): Map<LocalDate, Set<SampleTimedEvent>> = mapOf(
    now().toLocalDateTime(TimeZone.currentSystemDefault()).date to testEvents,
    now().plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date to simpleTestEvents
)

@Composable
fun EventIndicator(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.padding(2.dp).size(5.dp).align(Alignment.Center).background(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val swipeState = rememberSwipeableState(initialValue = SwipingStates.Collapsed)
        val measurer = rememberTextMeasurer()
        val events = getTestEvents()

        Box(Modifier.fillMaxSize()) {
            MotionCalender(calendarState = rememberMotionCalendarState(
                selectedDateMs = now().toEpochMilliseconds(),
                swipeState = swipeState,
            ),
                dayDecoration = CalendarDayDecoration(
                    alignment = CalendarDecorationAlignment.Below,
                    content = { day ->
                        if (events.containsKey(day)) {
                            EventIndicator()
                        }
                    }),
                content = { day ->
                    DayGrid(
                        modifier = Modifier.fillMaxWidth().height(1000.dp),
                        state = rememberDayGridState(
                            day = day,
                            events = events[day] ?: emptySet(),
                        ),
                        eventUi = { rect, sampleTimedEvent ->
                            eventUi(rect, sampleTimedEvent, measurer)
                        }
                    )
                })
        }
    }
}

fun DrawScope.eventUi(bounds: Rect, event: SampleTimedEvent, measurer: TextMeasurer) {
    drawRoundRect(
        topLeft = Offset(x = bounds.left, y = bounds.top),
        size = bounds.size,
        color = event.color,
        cornerRadius = CornerRadius(16.dp.toPx())
    )

    drawText(
        text = event.name,
        textMeasurer = measurer,
        topLeft = Offset(
            x = bounds.left + 8.dp.toPx(),
            y = bounds.top + 8.dp.toPx()
        ),
        size = bounds.size
    )

    drawText(
        text = event.description,
        textMeasurer = measurer,
        topLeft = Offset(
            x = bounds.left + 8.dp.toPx(),
            y = bounds.top + 28.dp.toPx()
        ),
        size = bounds.size.copy(
            height = bounds.size.height - 32.dp.toPx(),
            width = bounds.size.width - 16.dp.toPx()
        ),
        overflow = TextOverflow.Ellipsis
    )
}

class SampleTimedEvent(
    override val start: LocalTime,
    override val duration: Duration,
    val color: Color,
    val name: String = "Meeting",
    val description: String = "Time to meet the parents!"
) : TimedEvent

val simpleTestEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 10), duration = 8.hours + 2.minutes, color = Color.Yellow
    ),
)

val testEvents = setOf(
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 10), duration = 1.hours + 2.minutes, color = Color.Blue
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 0),
        duration = 1.hours + 2.minutes,
        color = Color.Magenta
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 2), duration = 4.hours, color = Color.Red
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 8, minute = 20), duration = 2.hours, color = Color.Cyan
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 10),
        duration = 2.hours + 2.minutes,
        color = Color.Green
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 13, minute = 30),
        duration = 2.hours + 2.minutes,
        color = Color.Yellow
    ),
    SampleTimedEvent(
        start = LocalTime(hour = 18, minute = 10),
        duration = 1.hours + 2.minutes,
        color = Color.LightGray
    ),
)
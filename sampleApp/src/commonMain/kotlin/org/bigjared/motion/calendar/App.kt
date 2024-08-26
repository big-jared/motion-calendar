package org.bigjared.motion.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock.System.now
import org.bigjared.motion.calendar.ScheduleService.getTestEvents
import org.bigjared.motion.calendar.calendar.CalendarDayDecoration
import org.bigjared.motion.calendar.calendar.CalendarDecorationAlignment
import org.bigjared.motion.calendar.calendar.MotionCalender
import org.bigjared.motion.calendar.calendar.SwipingStates
import org.bigjared.motion.calendar.calendar.rememberMotionCalendarState
import org.bigjared.motion.calendar.dayGrid.DayGrid
import org.bigjared.motion.calendar.dayGrid.rememberDayGridState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EventIndicator(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.padding(2.dp).size(5.dp).align(Alignment.Center).background(
                shape = CircleShape, color = MaterialTheme.colorScheme.primary
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
        val events = getTestEvents()

        Box(Modifier.fillMaxSize()) {
            MotionCalender(calendarState = rememberMotionCalendarState(
                selectedDateMs = now().toEpochMilliseconds(),
                swipeState = swipeState,
            ),
                dayDecoration = CalendarDayDecoration(alignment = CalendarDecorationAlignment.Below,
                    content = { day ->
                        if (events.containsKey(day)) {
                            EventIndicator()
                        }
                    }),
                content = { day ->
                    DayGrid(modifier = Modifier.fillMaxWidth(), state = rememberDayGridState(
                        day = day,
                        events = events[day] ?: emptySet(),
                    ), eventUi = { sampleTimedEvent ->
                        DayEvent(event = sampleTimedEvent)
                    })
                })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayEvent(modifier: Modifier = Modifier, event: SampleTimedEvent) {
    Column(
        modifier.fillMaxSize().background(
            shape = RoundedCornerShape(8.dp), color = event.color
        ).clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        FlowRow {
            Text(
                modifier = Modifier.weight(1f),
                text = event.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = "${event.start.toHourMinuteString(showAmPm = false)}-${event.end.toHourMinuteString(showAmPm = false)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        }
        Text(
            text = event.description,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
    }
}
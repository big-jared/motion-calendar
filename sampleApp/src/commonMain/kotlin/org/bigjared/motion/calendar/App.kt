package org.bigjared.motion.calendar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.ScheduleService.getTestEvents
import org.bigjared.motion.calendar.calendar.CalendarDayDecoration
import org.bigjared.motion.calendar.calendar.CalendarDecorationAlignment
import org.bigjared.motion.calendar.calendar.CalendarViewMode
import org.bigjared.motion.calendar.calendar.MotionCalender
import org.bigjared.motion.calendar.calendar.SwipingStates
import org.bigjared.motion.calendar.calendar.rememberMotionCalendarState
import org.bigjared.motion.calendar.common.VerticalDivider
import org.bigjared.motion.calendar.dayGrid.DayGrid
import org.bigjared.motion.calendar.dayGrid.GridRange
import org.bigjared.motion.calendar.dayGrid.rememberDayGridState
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.hours

@Composable
fun EventIndicator(
    modifier: Modifier = Modifier,
    events: Set<SampleTimedEvent>?
) {
    Box(modifier.fillMaxWidth().padding(bottom = 2.dp)) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            // Always render at least one transparent dot for consistent height
            val colors = events?.map { it.color }?.toSet()?.take(3) ?: listOf(Color.Transparent)
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(5.dp)
                        .background(shape = CircleShape, color = color)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        val swipeState = rememberSwipeableState(initialValue = SwipingStates.Collapsed)
        var eventDialogState by remember { mutableStateOf<SampleTimedEvent?>(null) }
        val events = getTestEvents()

        // Shared scroll state for week view synchronization
        val sharedScrollState = rememberScrollState()

        // Fixed initial scroll position (like Homeroom)
        val initialScrollTime = LocalTime(7, 45)

        // 24-hour grid range
        val gridRange = remember {
            GridRange(
                start = LocalTime(hour = 0, minute = 0),
                duration = 24.hours
            )
        }

        eventDialogState?.let { event ->
            EventDialog(event = event, onDismiss = {
                eventDialogState = null
            })
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                val calendarState = rememberMotionCalendarState(
                    selectedDateMs = now().toEpochMilliseconds(),
                    swipeState = swipeState,
                )

                MotionCalender(
                    modifier = Modifier.weight(1f),
                    calendarState = calendarState,
                    dayDecoration = CalendarDayDecoration(
                        alignment = CalendarDecorationAlignment.Below,
                        content = { day ->
                            EventIndicator(events = events[day])
                        }
                    ),
                    getEventsForDate = { day -> events[day]?.toList() ?: emptyList() },
                    dayContent = { day ->
                        DayGrid(
                            modifier = Modifier.fillMaxWidth(),
                            state = rememberDayGridState(
                                day = day,
                                events = events[day] ?: emptySet(),
                                gridRange = gridRange
                            ),
                            initialScrollTime = initialScrollTime,
                            eventUi = { sampleTimedEvent ->
                                DayEvent(event = sampleTimedEvent, selectEvent = { event ->
                                    eventDialogState = event
                                })
                            }
                        )
                    },
                    weekContent = { weekStart ->
                        WeekView(
                            weekStart = weekStart,
                            events = events,
                            gridRange = gridRange,
                            sharedScrollState = sharedScrollState,
                            initialScrollTime = initialScrollTime,
                            onEventSelected = { event ->
                                eventDialogState = event
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun WeekView(
    weekStart: LocalDate,
    events: Map<LocalDate, Set<SampleTimedEvent>>,
    gridRange: GridRange,
    sharedScrollState: ScrollState,
    initialScrollTime: LocalTime,
    onEventSelected: (SampleTimedEvent) -> Unit
) {
    Row(Modifier.fillMaxSize()) {
        for (i in 0..6) {
            if (i > 0) {
                VerticalDivider()
            }
            val day = weekStart.plus(i, DateTimeUnit.DAY)

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    DayGrid(
                        modifier = Modifier.fillMaxSize(),
                        state = rememberDayGridState(
                            day = day,
                            events = events[day] ?: emptySet(),
                            gridRange = gridRange
                        ),
                        sharedScrollState = sharedScrollState,
                        initialScrollTime = initialScrollTime,
                        showTimeLabels = (i == 0),
                        eventLabelPaddingOverride = false,
                        horizontalDividerSolid = true,
                        eventUi = { sampleTimedEvent ->
                            DayEvent(event = sampleTimedEvent, selectEvent = { event ->
                                onEventSelected(event)
                            })
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventDialog(modifier: Modifier = Modifier, event: SampleTimedEvent, onDismiss: () -> Unit) {
    AlertDialogWithContent(
        modifier = modifier,
        title = event.name,
        content = {
            Text(event.description)
        },
        onClose = {
            onDismiss()
        }
    )
}

@Composable
fun DayEvent(
    modifier: Modifier = Modifier,
    event: SampleTimedEvent,
    selectEvent: (SampleTimedEvent) -> Unit
) {
    val showTime = event.duration.inWholeMinutes >= 20

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = event.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = event.color,
                shape = RoundedCornerShape(6.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .clickable { selectEvent(event) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = event.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = event.color,
                    maxLines = 1
                )
                if (showTime) {
                    Text(
                        text = "${event.start.toHourMinuteString(showAmPm = false)}-${event.end.toHourMinuteString(showAmPm = false)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = event.color
                    )
                }
            }
        }
    }
}

package org.bigjared.motion.calendar.dayGrid

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.calendar.MotionCalendarColors
import org.bigjared.motion.calendar.calendar.motionCalendarColors
import org.bigjared.motion.calendar.common.DashedVerticalDivider
import org.bigjared.motion.calendar.util.isToday
import org.bigjared.motion.calendar.util.plus
import org.bigjared.motion.calendar.util.toHourMinuteString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val defaultGridRange = GridRange(start = LocalTime(7, 0), duration = 12.hours)

data class GridRange(val start: LocalTime, val duration: Duration) {
    val end = start.plus(duration)

    val increment = when {
        duration >= 6.hours -> 1.hours
        duration >= 2.hours -> 30.minutes
        else -> 15.minutes
    }

    fun elements(): Int = (duration / increment).toInt()
}

@Composable
fun <T : TimedEvent> DayGrid(
    modifier: Modifier = Modifier,
    state: DayGridState<T> = rememberDayGridState(
        day = now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        events = emptySet(),
        gridRange = defaultGridRange
    ),
    colors: MotionCalendarColors = motionCalendarColors(),
    height: Dp = 1000.dp,
    eventUi: @Composable (T) -> Unit = { event -> BasicDayEvent(event = event) },
    initialScrollTime: LocalTime = LocalTime(7, 45),
    onLongClickAtTime: ((LocalTime) -> Unit)? = null,
    showTimeLabels: Boolean = true,
    sharedScrollState: ScrollState? = null,
    eventLabelPaddingOverride: Boolean? = null,
    horizontalDividerSolid: Boolean = false,
    showLeadingVerticalDivider: Boolean = false
) {
    val localDensity = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }
    val labelMaxWidth = 42.dp
    val scrollState = sharedScrollState ?: rememberScrollState()
    val currentDensity = LocalDensity.current

    // Track current time for live updates
    var currentDateTime by remember { mutableStateOf(now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    // Update current time at the beginning of each minute
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = now().toLocalDateTime(TimeZone.currentSystemDefault())

            // Calculate milliseconds until next minute
            val currentTimeMillis = now().toEpochMilliseconds()
            val millisInMinute = 60 * 1000
            val nextMinuteMillis = (currentTimeMillis / millisInMinute + 1) * millisInMinute
            val delayMillis = nextMinuteMillis - currentTimeMillis

            delay(delayMillis)
        }
    }

    // Calculate the height per increment
    val dpPerSecond = height.value / state.gridRange.duration.inWholeSeconds.toFloat()
    val totalHeight = dpPerSecond * state.gridRange.duration.inWholeSeconds

    // Calculate initial scroll position based on desired time
    val initialScrollPosition = remember(initialScrollTime, dpPerSecond, state.gridRange) {
        val scrollTimeSeconds = initialScrollTime.toSecondOfDay()
        val gridStartSeconds = state.gridRange.start.toSecondOfDay()

        val dp = if (scrollTimeSeconds > gridStartSeconds) {
            ((scrollTimeSeconds - gridStartSeconds) * dpPerSecond).dp
        } else {
            0.dp
        }

        with(currentDensity) { dp.toPx() }
    }

    // Set the initial scroll position
    LaunchedEffect(state.day, initialScrollPosition) {
        if (scrollState.value == 0) {
            scrollState.scrollTo(initialScrollPosition.toInt())
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        if (onLongClickAtTime != null) {
                            val verticalOffsetSeconds = state.gridRange.start.toSecondOfDay()
                            val ySeconds = (offset.y.toDp().value / dpPerSecond).toInt() + verticalOffsetSeconds
                            val hours = ySeconds / 3600
                            val minutes = (ySeconds % 3600) / 60
                            val time = LocalTime(
                                hour = hours.coerceIn(0, 23),
                                minute = minutes.coerceIn(0, 59)
                            )
                            onLongClickAtTime(time)
                        }
                    }
                )
            }
    ) {
        if (showLeadingVerticalDivider) {
            DashedVerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .zIndex(1f)
            )
        }
        // Main scrollable content
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalHeight.dp)
            ) {
                // Time scale column
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(color = colors.backgroundColor)
                ) {
                    repeat(state.gridRange.elements()) { gridElement ->
                        TimeScaleItem(
                            gridElement = gridElement,
                            state = state,
                            colors = colors,
                            labelMaxWidth = labelMaxWidth,
                            dpPerSecond = dpPerSecond,
                            showLabel = showTimeLabels,
                            horizontalDividerSolid = horizontalDividerSolid
                        )
                    }
                }

                // Events container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = when (eventLabelPaddingOverride) {
                                true -> labelMaxWidth + 8.dp
                                false -> 0.dp
                                null -> if (showTimeLabels) labelMaxWidth + 8.dp else 0.dp
                            },
                            end = if (eventLabelPaddingOverride == true) 8.dp else 0.dp
                        )
                        .onGloballyPositioned { coordinates ->
                            width = with(localDensity) { coordinates.size.width.toDp() }
                        }
                ) {
                    Events(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        eventUi = eventUi,
                        dpPerSecond = dpPerSecond,
                        width = width
                    )
                }
            }

            // Current time indicator
            if (state.day.isToday()) {
                val timeIndicatorOffset = (dpPerSecond * (currentDateTime.time.toSecondOfDay() - state.gridRange.start.toSecondOfDay())).dp
                if (timeIndicatorOffset >= 0.dp && currentDateTime.time.toSecondOfDay() <= state.gridRange.end.toSecondOfDay()) {
                    Box(
                        modifier = Modifier
                            .offset(y = timeIndicatorOffset - 10.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center).height(1.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                )
                                .padding(2.dp).padding(horizontal = 2.dp)
                                .align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = currentDateTime.time.toHourMinuteString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.fillMaxHeight().align(Alignment.Center),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeScaleItem(
    gridElement: Int,
    state: DayGridState<*>,
    colors: MotionCalendarColors,
    labelMaxWidth: Dp,
    dpPerSecond: Float,
    showLabel: Boolean,
    horizontalDividerSolid: Boolean
) {
    val incrementSeconds = state.gridRange.increment.inWholeSeconds
    val time = remember(gridElement, state.gridRange.start, incrementSeconds) {
        state.gridRange.start + (gridElement * incrementSeconds).seconds
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dpPerSecond.times(incrementSeconds).dp)
    ) {
        Box(Modifier.fillMaxWidth().drawWithContent {
            val dividerColor = colors.divider.copy(alpha = 0.3f)
            if (horizontalDividerSolid) {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            } else {
                HorizontalDashedDivider(thickness = 1.dp, color = colors.divider)
            }
        })
        if (showLabel) {
            Text(
                modifier = Modifier
                    .padding(top = 2.dp, start = 4.dp)
                    .widthIn(max = labelMaxWidth),
                text = time.toHourMinuteString(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.divider
            )
        }
    }
}

@Composable
internal fun <T : TimedEvent> Events(
    modifier: Modifier = Modifier,
    state: DayGridState<T>,
    eventUi: @Composable (T) -> Unit,
    dpPerSecond: Float,
    width: Dp,
) {
    Box(modifier = modifier) {
        val verticalOffset = dpPerSecond * state.gridRange.start.toSecondOfDay()
        val eventSpacing = 2.dp
        val eventLayouts = state.eventLayoutParams.value
        eventLayouts.forEach { layout ->
            val event = layout.event
            val totalColumns = layout.totalColumns
            val columnIndex = layout.columnIndex
            val availableWidth = width - (eventSpacing * (totalColumns - 1).coerceAtLeast(0))
            val eventWidth = if (totalColumns > 0) availableWidth / totalColumns else availableWidth
            val columnX = if (totalColumns > 0) (eventWidth * columnIndex) + (eventSpacing * columnIndex) else 0.dp
            val eventDisplayHeight = dpPerSecond * event.duration.inWholeSeconds
            Box(
                Modifier
                    .offset(
                        x = columnX,
                        y = (dpPerSecond * event.start.toSecondOfDay() - verticalOffset).dp
                    )
                    .height(eventDisplayHeight.dp)
                    .width(eventWidth)
                    .clipToBounds()
            ) {
                eventUi.invoke(event)
            }
        }
    }
}

internal fun ContentDrawScope.HorizontalDashedDivider(thickness: Dp, color: Color) {
    drawLine(
        color,
        Offset(0f, 0f),
        Offset(size.width, thickness.toPx()),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 30f))
    )
}

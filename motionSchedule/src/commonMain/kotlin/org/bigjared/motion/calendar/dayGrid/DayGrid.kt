package org.bigjared.motion.calendar.dayGrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.calendar.MotionCalendarColors
import org.bigjared.motion.calendar.calendar.motionCalendarColors
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
        duration >= 12.hours -> 2.hours
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
) {
    val localDensity = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }
    val labelMaxWidth = 42.dp
    Box(modifier = modifier.height(height)) {
        Timescale(
            colors = colors, gridState = state, height = height, labelMaxWidth = labelMaxWidth
        )
        Events(
            modifier = Modifier.fillMaxSize().padding(start = labelMaxWidth + 8.dp)
                .onGloballyPositioned { coordinates ->
                    width = with(localDensity) { coordinates.size.width.toDp() }
                }, state = state, eventUi = eventUi, height = height, width = width
        )
    }
}

@Composable
internal fun <T : TimedEvent> Events(
    modifier: Modifier = Modifier,
    state: DayGridState<T>,
    eventUi: @Composable (T) -> Unit,
    height: Dp,
    width: Dp,
) {
    Box(modifier = modifier) {
        val dpPerSecond: Dp = height.div(state.gridRange.duration.inWholeSeconds.toInt())
        val verticalOffset = dpPerSecond.times(state.gridRange.start.toSecondOfDay())

        state.sortedEvents.value.forEach { events ->
            var x = 0.dp
            val columnWidth = width.div(events.size)
            events.sortedBy { it.start }.forEach { event ->
                Box(
                    Modifier.offset(
                            x = x,
                            y = ((dpPerSecond * event.start.toSecondOfDay()) - verticalOffset)
                        ).height(dpPerSecond.times(event.duration.inWholeSeconds.toInt()))
                        .width(columnWidth).clipToBounds()
                ) {
                    eventUi.invoke(event)
                }

                x = x.plus(columnWidth)
            }
        }
    }
}

@Composable
internal fun <T : TimedEvent> Timescale(
    modifier: Modifier = Modifier,
    colors: MotionCalendarColors,
    gridState: DayGridState<T>,
    height: Dp,
    labelMaxWidth: Dp
) {
    Column(modifier = modifier.fillMaxSize().background(color = colors.backgroundColor)) {
        val pxPerSecond: Float = height.value / gridState.gridRange.duration.inWholeSeconds
        val incrementSeconds = gridState.gridRange.increment.inWholeSeconds
        val incrementPx = pxPerSecond * incrementSeconds
        val start = gridState.gridRange.start
        for (gridElement in 0 until gridState.gridRange.elements()) {
            Column(modifier = Modifier.height(incrementPx.dp)) {
                Box(modifier.fillMaxWidth().drawWithContent {
                    HorizontalDashedDivider(thickness = 1.dp, color = colors.divider)
                })
                Text(
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        .widthIn(max = labelMaxWidth),
                    text = (start + (gridElement * incrementSeconds).seconds).toHourMinuteString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.divider
                )
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
package org.bigjared.motion.calendar.dayGrid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
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
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.calendar.MotionCalendarColors
import org.bigjared.motion.calendar.calendar.motionCalendarColors
import org.bigjared.motion.calendar.util.toHourMinuteString

@Composable
fun <T: TimedEvent> DayGridColumn(
    modifier: Modifier = Modifier,
    state: DayGridState<T> = rememberDayGridState(
        day = now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        events = emptySet(),
    ),
    colors: MotionCalendarColors = motionCalendarColors(),
    eventUi: ContentDrawScope.(Rect, T) -> Unit
) {
    Box(modifier = modifier) {
        Timescale(colors = colors)
        Events(state = state, eventUi = eventUi)
    }
}

@Composable
internal fun <T: TimedEvent> Events(
    modifier: Modifier = Modifier,
    state: DayGridState<T>,
    eventUi: ContentDrawScope.(Rect, T) -> Unit
) {
    val localDensity = LocalDensity.current

    var columnHeightPx by remember {
        mutableStateOf(0f)
    }
    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }

    Box(modifier = modifier.fillMaxSize().padding(start = 46.dp)
        .onGloballyPositioned { coordinates ->
            columnHeightPx = coordinates.size.height.toFloat()
            columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
        }.drawWithContent {
            val pxPerSecond: Float = columnHeightPx / (24f * 60 * 60)
            state.sortedEvents.value.forEach { events ->
                var x = 0f
                val columnWidth = size.width / events.size
                events.sortedBy { it.start }.forEach { event ->
                    eventUi.invoke(this,
                        Rect(
                            left = x,
                            top = pxPerSecond * event.start.toSecondOfDay(),
                            right = x + columnWidth,
                            bottom = pxPerSecond * event.start.toSecondOfDay() + pxPerSecond * event.duration.inWholeSeconds
                        ),
                        event
                    )
                    x += columnWidth
                }
            }
        })
}

@Composable
internal fun Timescale(modifier: Modifier = Modifier, colors: MotionCalendarColors) {
    Column(modifier = modifier) {
        repeat((1..12).count()) { hours ->
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier.fillMaxWidth().drawWithContent {
                    HorizontalDashedDivider(thickness = 1.dp, color = colors.divider)
                })
                Text(
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                    text = LocalTime.fromSecondOfDay(2 * hours * (60 * 60))
                        .toHourMinuteString(showMinutes = false),
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
package org.bigjared.motion.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

enum class CalendarDecorationAlignment {
    Below, Above, Right, Left
}

data class CalendarDayDecoration(
    val alignment: CalendarDecorationAlignment,
    val content: @Composable (LocalDate) -> Unit
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RowScope.CalendarItem(
    modifier: Modifier = Modifier,
    day: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    showWeekday: Boolean,
    swipingState: SwipeableState<SwipingStates>? = null,
    onSelected: (LocalDate) -> Unit,
) {
    val shape = if (showWeekday) RoundedCornerShape(16.dp) else CircleShape
    val swipePercentage = if (swipingState == null) 1f else
        if (swipingState.progress.to == SwipingStates.Collapsed) swipingState.progress.fraction else 1f - swipingState.progress.fraction
    val containerColor =
        if (showWeekday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = swipePercentage) else MaterialTheme.colorScheme.primary
    val contentColor =
        if (showWeekday) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = swipePercentage) else MaterialTheme.colorScheme.onPrimary

    var backgroundedModifier = modifier
    backgroundedModifier = if (selected) {
        modifier.background(
            color = containerColor,
            shape = shape
        )
    } else backgroundedModifier
    backgroundedModifier = if (isToday) {
        backgroundedModifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = swipePercentage),
            shape = shape
        )
    } else backgroundedModifier

    if (showWeekday) {
        Column(backgroundedModifier.clip(shape).padding(4.dp).weight(1f)
            .clickable { onSelected(day) }) {
            Text(
                modifier = Modifier.fillMaxWidth().heightIn(
                    max = 28.dp * swipePercentage,
                    min = if (swipingState == null) 32.dp else Dp.Unspecified
                ),
                text = day.dayOfMonth.toString(),
                fontSize = 16.sp * swipePercentage,
                textAlign = TextAlign.Center,
                color = if (selected) contentColor else MaterialTheme.colorScheme.onSurface
            )
            if (showWeekday) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = day.dayOfWeek.name.lowercase().capitalize(
                        Locale.current
                    ).take(max(1.0, 3.0 * swipePercentage).roundToInt()),
                    textAlign = TextAlign.Center,
                    color = if (selected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight((400f * (1 - swipePercentage)).toInt() + 400)
                )
            }
        }
    } else {
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = backgroundedModifier.padding(2.dp).size(36.dp).align(Alignment.Center)
                    .clip(shape).clickable { onSelected(day) }.align(
                        Alignment.Center
                    ),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = day.dayOfMonth.toString(),
                    textAlign = TextAlign.Center,
                    color = if (selected) contentColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
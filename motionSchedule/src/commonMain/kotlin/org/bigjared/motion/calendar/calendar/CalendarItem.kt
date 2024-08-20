package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.math.roundToInt

// Currently calendar only supports indicators aligned at the bottom of each day view,
// In the future we should support any alignment.
enum class CalendarDecorationAlignment {
    Below
}

data class CalendarDayDecoration(
    val alignment: CalendarDecorationAlignment, val content: @Composable (LocalDate) -> Unit
)

interface CalendarDayItem {
    @Composable
    fun Content(
        modifier: Modifier,
        day: LocalDate,
        colors: MotionCalendarColors,
        state: CalendarState,
        dayDecoration: CalendarDayDecoration?
    )
}

internal class CalendarDayOfWeek : CalendarDayItem {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        modifier: Modifier,
        day: LocalDate,
        colors: MotionCalendarColors,
        state: CalendarState,
        dayDecoration: CalendarDayDecoration?
    ) {
        val coScope = rememberCoroutineScope()
        val inActiveMonth = day.monthNumber == state.shownMonth.value.monthNumber
        val selected = day == state.selectedDate.value
        val isToday = day == now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val swipingState = state.swipeState
        val swipePercentage =
            if (swipingState.progress.to == SwipingStates.Collapsed) swipingState.progress.fraction else 1f - swipingState.progress.fraction

        Column(modifier.padding(4.dp)
            .background(
                color = if (selected) colors.primaryContainer.copy(alpha = swipePercentage) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .border(
                border = BorderStroke(
                    width = if (isToday) 2.dp else 0.dp,
                    color = if (isToday) colors.primary.copy(alpha = swipePercentage) else Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                coScope.launch {
                    state.selectedDate.value = day
                    state.swipeState.animateTo(SwipingStates.Collapsed)
                }
            }
            .padding(vertical = 4.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().heightIn(
                    max = 28.dp * swipePercentage,
                    min = Dp.Unspecified
                ),
                text = day.dayOfMonth.toString(),
                fontSize = 18.sp * swipePercentage,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = (if (selected) colors.primary else colors.onBackgroundColor.copy(alpha = swipePercentage)).copy(
                    alpha = if (inActiveMonth) 1f else .5f
                )
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = day.dayOfWeek.name.lowercase().capitalize(
                    Locale.current
                ).take(max(1.0, 3.0 * swipePercentage).roundToInt()),
                textAlign = TextAlign.Center,
                color = (if (selected) colors.primary else colors.onBackgroundColor.copy(alpha = swipePercentage)).copy(
                    alpha = if (inActiveMonth) 1f else .5f
                ),
                fontWeight = FontWeight((400f * (1 - swipePercentage)).toInt() + 400)
            )

            Box(
                modifier = Modifier.heightIn(
                    max = 28.dp * swipePercentage,
                    min = Dp.Unspecified
                )
            ) {
                dayDecoration?.content?.invoke(day)
            }
        }
    }
}

internal class CalendarDayOfMonth : CalendarDayItem {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        modifier: Modifier,
        day: LocalDate,
        colors: MotionCalendarColors,
        state: CalendarState,
        dayDecoration: CalendarDayDecoration?
    ) {
        val coScope = rememberCoroutineScope()
        val inActiveMonth = day.monthNumber == state.shownMonth.value.monthNumber
        val selected = day == state.selectedDate.value
        val isToday = day == now().toLocalDateTime(TimeZone.currentSystemDefault()).date


        Box(modifier = modifier) {

            var baseModifier = if (selected) Modifier
                .background(
                    color = colors.primary,
                    CircleShape
                ) else Modifier

            baseModifier = if (isToday) baseModifier.border(
                border = BorderStroke(
                    width = 2.dp,
                    color = colors.primary
                ),
                shape = CircleShape,
            ) else baseModifier

            Box(
                modifier = baseModifier
                    .fillMaxWidth(.68f)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .clickable {
                        coScope.launch {
                            if (inActiveMonth) {
                                state.selectedDate.value = day
                                state.swipeState.animateTo(SwipingStates.Collapsed)
                            } else {
                                state.swipeState.animateTo(SwipingStates.Collapsed)
                                state.selectedDate.value = day
                            }
                        }
                    },
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Box(modifier.align(Alignment.CenterHorizontally).weight(1f)) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = day.dayOfMonth.toString(),
                            color = (if (selected) colors.onPrimary else colors.onBackgroundColor).copy(
                                alpha = if (inActiveMonth) 1f else .5f
                            )
                        )
                    }

                    if (!selected && dayDecoration != null) {
                        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            dayDecoration.content.invoke(day)
                        }
                    }
                }
            }
        }
    }
}
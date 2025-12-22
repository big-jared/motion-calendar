package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
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
        dayDecoration: CalendarDayDecoration?,
        hasEvents: Boolean
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
        dayDecoration: CalendarDayDecoration?,
        hasEvents: Boolean
    ) {
        val coScope = rememberCoroutineScope()
        val inActiveMonth = remember(day, state.shownMonth.value) {
            day.monthNumber == state.shownMonth.value.monthNumber
        }
        val selected = remember(day, state.selectedDate.value) {
            day == state.selectedDate.value
        }
        val isToday = remember(day) {
            day == now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        val swipePercentage = if (state.swipeState.progress.to == SwipingStates.Collapsed) {
            state.swipeState.progress.fraction
        } else {
            1f - state.swipeState.progress.fraction
        }

        Column(
            modifier = modifier
                .padding(4.dp)
                .background(
                    color = if (selected) {
                        colors.primaryContainer.copy(alpha = swipePercentage)
                    } else if (isToday) {
                        colors.onBackgroundColor.copy(alpha = swipePercentage * 0.07f)
                    } else {
                        Color.Transparent
                    },
                    RoundedCornerShape(16.dp)
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
            val textColor = remember(selected, inActiveMonth, swipePercentage) {
                (if (selected) colors.primary else colors.onBackgroundColor.copy(alpha = swipePercentage))
                    .copy(alpha = if (inActiveMonth) 1f else .5f)
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 28.dp * swipePercentage),
                text = day.dayOfMonth.toString(),
                fontSize = 18.sp * swipePercentage,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = textColor
            )

            val dayOfWeekText = remember(day, swipePercentage) {
                day.dayOfWeek.name.lowercase().capitalize(Locale.current)
                    .take(max(1.0, 3.0 * swipePercentage).roundToInt())
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = dayOfWeekText,
                textAlign = TextAlign.Center,
                color = textColor,
                fontWeight = FontWeight((400f * (1 - swipePercentage)).toInt() + 400)
            )

            val isDecorationSystemActive = dayDecoration != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (isDecorationSystemActive) 10.dp else 0.dp,
                        max = (28.dp * swipePercentage)
                    )
                    .alpha(if (isDecorationSystemActive) 1f else 0f),
                contentAlignment = Alignment.Center
            ) {
                key(day, isDecorationSystemActive) {
                    if (isDecorationSystemActive) {
                        Box(modifier = Modifier.alpha(if (hasEvents) 1f else 0f)) {
                            dayDecoration!!.content.invoke(day)
                        }
                        Box(modifier = Modifier.alpha(if (hasEvents) 0f else 1f)) {
                            Box(Modifier.size(10.dp))
                        }
                    }
                }
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
        dayDecoration: CalendarDayDecoration?,
        hasEvents: Boolean
    ) {
        val coScope = rememberCoroutineScope()
        val inActiveMonth = remember(day, state.shownMonth.value) {
            day.monthNumber == state.shownMonth.value.monthNumber
        }
        val selected = remember(day, state.selectedDate.value) {
            day == state.selectedDate.value
        }
        val isToday = remember(day) {
            day == now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

        Box(modifier = modifier) {
            val baseModifier = remember(selected, isToday) {
               if (selected) {
                    Modifier.background(color = colors.primaryContainer, RoundedCornerShape(12.dp))
                } else if (isToday) {
                    Modifier.background(colors.onBackgroundColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            }

            Box(
                modifier = baseModifier
                    .size(42.dp)
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
                    }
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally).weight(1f)) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = day.dayOfMonth.toString(),
                            color = (if (selected) colors.primary else colors.onBackgroundColor)
                                .copy(alpha = if (inActiveMonth) 1f else .5f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(min = if (dayDecoration != null) 10.dp else 0.dp)
                    ) {
                        key(day, dayDecoration != null, hasEvents) {
                            if (dayDecoration != null) {
                                if (hasEvents) {
                                    dayDecoration.content.invoke(day)
                                } else {
                                    Box(Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

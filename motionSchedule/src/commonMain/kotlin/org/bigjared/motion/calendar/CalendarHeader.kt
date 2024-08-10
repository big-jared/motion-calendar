@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterialApi::class
)

package org.bigjared.motion.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun DefaultHeader(modifier: Modifier = Modifier, calendarState: CalendarState) {
    val startDay = calendarState.shownMonth.value

    Row(modifier = modifier.fillMaxWidth()
        .zIndex(4f)
        .padding(horizontal = 8.dp)
        .padding(top = 8.dp)) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
            text = startDay.month.name.lowercase().capitalize(Locale.current),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
            text = "${startDay.year}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    header: (@Composable () -> Unit)?,
    weekPagerState: PagerState,
    monthPagerState: PagerState,
    startingWeekPage: Int,
    startingMonthPage: Int
) {
    val background = MaterialTheme.colorScheme.surfaceContainerLowest
    val intermediate = MaterialTheme.colorScheme.surfaceContainer
    val primary = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = modifier.fillMaxWidth().background(
            shape = RoundedCornerShape(
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ), color = MaterialTheme.colorScheme.surface
        ).drawBehind {
            this.drawRoundRect(
                Brush.linearGradient(
                    0.0f to background,
                    0.5f to intermediate,
                    1.0f to primary,
                    start = Offset(0.0f, 0.0f),
                    end = Offset(0.0f, size.height),
                ), cornerRadius = CornerRadius(64f)
            )
        }
    ) {
        header?.invoke()
        CollapsedWeekContent(
            calendarState = calendarState,
            weekPagerState = weekPagerState,
            startingWeekPage = startingWeekPage
        )
        ExpandedMonthContent(
            calendarState = calendarState,
            monthPagerState = monthPagerState,
            startingMonthPage = startingMonthPage
        )
        DragBar()
    }
}

@Composable
fun DragBar() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp)
                .size(32.dp, 4.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .align(Alignment.Center)
        )
    }
}

@Composable
fun CollapsedWeekContent(
    modifier: Modifier = Modifier,
    weekPagerState: PagerState,
    calendarState: CalendarState,
    startingWeekPage: Int
) {
    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val coScope = rememberCoroutineScope()

    Box {
        HorizontalPager(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            state = weekPagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            calendarState.startOfWeek.value =
                now.minus(startingWeekPage - page, DateTimeUnit.WEEK)
                    .startOfWeek()
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                var day = calendarState.startOfWeek.value
                repeat((1..7).count()) {
                    CalendarItem(
                        day = day,
                        selected = day == calendarState.selectedDate.value,
                        isToday = day == now,
                        showWeekday = true,
                        swipingState = calendarState.swipeState,
                    ) {
                        coScope.launch {
                            calendarState.selectedDate.value = it
                        }
                    }
                    day = day.plus(1, DateTimeUnit.DAY)
                }
            }
        }
    }
}

@Composable
fun ExpandedMonthContent(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    monthPagerState: PagerState,
    startingMonthPage: Int
) {
    val coScope = rememberCoroutineScope()
    Box(
        modifier
            .padding(vertical = 8.dp)
            .heightIn(max = 256.dp * calendarState.swipePercentage)
            .alpha(calendarState.swipePercentage)
    ) {
        HorizontalPager(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            state = monthPagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            calendarState.startOfMonth.value =
                calendarState.now.minus(startingMonthPage - page, DateTimeUnit.MONTH)
                    .startOfMonth()
            Column {
                val days = calendarState.startOfMonth.value.monthDays()
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        week.forEach { day ->
                            CalendarItem(
                                day = day,
                                selected = day == calendarState.selectedDate.value,
                                isToday = day == calendarState.now,
                                swipingState = null,
                                showWeekday = false
                            ) { date ->
                                coScope.launch {
                                    calendarState.selectedDate.value = date
                                    calendarState.swipeState.animateTo(SwipingStates.Collapsed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
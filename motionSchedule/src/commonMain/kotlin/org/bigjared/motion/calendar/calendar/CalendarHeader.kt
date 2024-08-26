@file:OptIn(
    ExperimentalFoundationApi::class
)

package org.bigjared.motion.calendar.calendar

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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.util.monthDays
import org.bigjared.motion.calendar.util.startOfMonth
import org.bigjared.motion.calendar.util.startOfWeek

@Composable
internal fun DefaultHeader(
    modifier: Modifier = Modifier, calendarState: CalendarState, colors: MotionCalendarColors
) {
    val startDay = calendarState.shownMonth.value

    Row(
        modifier = modifier.fillMaxWidth().zIndex(4f).padding(horizontal = 8.dp).padding(top = 8.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
            text = startDay.month.name.lowercase().capitalize(Locale.current),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackgroundColor
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 8.dp),
            text = "${startDay.year}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackgroundColor
        )
    }
}

@Composable
internal fun DefaultFooter(modifier: Modifier = Modifier, colors: MotionCalendarColors) {
    Box(modifier = modifier) {
        DragBar(colors)
    }
}

internal val defaultShape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    colors: MotionCalendarColors,
    shape: Shape,
    header: (@Composable () -> Unit)?,
    footer: (@Composable () -> Unit)?,
    weekDay: CalendarDayItem,
    monthDay: CalendarDayItem,
    dayDecoration: CalendarDayDecoration?,
    weekPagerState: PagerState,
    monthPagerState: PagerState,
    startingWeekPage: Int,
    startingMonthPage: Int,
) {
    val background = colors.backgroundGradientColorLow
    val intermediate = colors.backgroundGradientColorMed
    val primary = colors.backgroundGradientColorHigh

    Column(modifier = modifier.fillMaxWidth().background(
        color = colors.backgroundColor
    ).clip(RoundedCornerShape(16.dp)).drawBehind {
        this.drawRect(
            Brush.linearGradient(
                0.0f to background,
                0.5f to intermediate,
                1.0f to primary,
                start = Offset(0.0f, 0.0f),
                end = Offset(0.0f, size.height),
            )
        )
    }) {
        header?.invoke()
        CollapsedWeekContent(
            calendarState = calendarState,
            colors = colors,
            weekPagerState = weekPagerState,
            startingWeekPage = startingWeekPage,
            dayDecoration = dayDecoration,
            weekDay = weekDay
        )
        ExpandedMonthContent(
            calendarState = calendarState,
            colors = colors,
            monthPagerState = monthPagerState,
            startingMonthPage = startingMonthPage,
            dayDecoration = dayDecoration,
            monthDay = monthDay,
        )
        footer?.invoke()
    }
}

@Composable
fun DragBar(colors: MotionCalendarColors) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp).size(32.dp, 4.dp).background(
                color = colors.backgroundVariant, shape = RoundedCornerShape(8.dp)
            ).align(Alignment.Center)
        )
    }
}


@Composable
fun CollapsedWeekContent(
    modifier: Modifier = Modifier,
    colors: MotionCalendarColors,
    weekPagerState: PagerState,
    calendarState: CalendarState,
    dayDecoration: CalendarDayDecoration?,
    weekDay: CalendarDayItem,
    startingWeekPage: Int
) {
    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Box {
        HorizontalPager(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            state = weekPagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            calendarState.startOfWeek.value =
                now.minus(startingWeekPage - page, DateTimeUnit.WEEK).startOfWeek()
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                var day = calendarState.startOfWeek.value
                repeat((1..7).count()) {
                    weekDay.Content(
                        modifier = modifier.weight(1f),
                        day = day,
                        state = calendarState,
                        colors = colors,
                        dayDecoration = dayDecoration
                    )
                    day = day.plus(1, DateTimeUnit.DAY)
                }
            }
        }
    }
}

@Composable
fun ExpandedMonthContent(
    modifier: Modifier = Modifier,
    colors: MotionCalendarColors,
    calendarState: CalendarState,
    monthDay: CalendarDayItem,
    monthPagerState: PagerState,
    startingMonthPage: Int,
    dayDecoration: CalendarDayDecoration?,
) {
    Box(
        modifier.heightIn(max = 300.dp * calendarState.swipePercentage).padding(vertical = 8.dp)
            .alpha(calendarState.swipePercentage)
    ) {
        HorizontalPager(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            state = monthPagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            calendarState.startOfMonth.value =
                calendarState.now.minus(startingMonthPage - page, DateTimeUnit.MONTH).startOfMonth()
            Column {
                val days = calendarState.startOfMonth.value.monthDays()
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        week.forEach { day ->
                            monthDay.Content(
                                modifier = modifier.weight(1f),
                                day = day,
                                colors = colors,
                                state = calendarState,
                                dayDecoration = dayDecoration
                            )
                        }
                    }
                }
            }
        }
    }
}
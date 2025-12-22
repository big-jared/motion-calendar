@file:OptIn(
    ExperimentalFoundationApi::class
)

package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.common.DragBar
import org.bigjared.motion.calendar.common.LocalPlatform
import org.bigjared.motion.calendar.common.Platform
import org.bigjared.motion.calendar.common.isDesktopOrWeb
import org.bigjared.motion.calendar.util.monthDays
import org.bigjared.motion.calendar.util.startOfMonth
import org.bigjared.motion.calendar.util.startOfWeek

@Composable
internal fun DefaultHeader(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    colors: MotionCalendarColors,
    navigationControls: CalendarNavigationControls? = null
) {
    val platform = LocalPlatform.current
    val startDay = calendarState.shownMonth.value
    val viewMode = calendarState.viewMode.value
    val isExpanded = calendarState.swipePercentage > 0.5f

    Row(
        modifier = modifier.fillMaxWidth().zIndex(4f).padding(horizontal = 8.dp).padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = startDay.month.name.lowercase().capitalize(Locale.current),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            text = "${startDay.year}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .3f)
        )

        // Navigation buttons for desktop/web
        if (platform.isDesktopOrWeb() && navigationControls != null) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable {
                        if (isExpanded) {
                            navigationControls.onPreviousMonth()
                        } else {
                            navigationControls.onPrevious()
                        }
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable {
                        if (isExpanded) {
                            navigationControls.onNextMonth()
                        } else {
                            navigationControls.onNext()
                        }
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        FilterChip(
            selected = viewMode == CalendarViewMode.Day,
            onClick = { calendarState.viewMode.value = CalendarViewMode.Day },
            label = {
                Icon(
                    imageVector = Icons.Filled.CalendarViewDay,
                    contentDescription = "Day View",
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.padding(end = 8.dp)
        )
        FilterChip(
            selected = viewMode == CalendarViewMode.Week,
            onClick = { calendarState.viewMode.value = CalendarViewMode.Week },
            label = {
                Icon(
                    imageVector = Icons.Filled.CalendarViewWeek,
                    contentDescription = "Week View",
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
internal fun DefaultFooter(
    modifier: Modifier = Modifier,
    colors: MotionCalendarColors,
    calendarState: CalendarState? = null,
    onToggleExpand: (() -> Unit)? = null
) {
    val platform = LocalPlatform.current
    Box(modifier = modifier) {
        if (platform.isDesktopOrWeb() && calendarState != null && onToggleExpand != null) {
            // Desktop: Show dropdown arrow button
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                val isExpanded = calendarState.swipePercentage > 0.5f
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onToggleExpand)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse calendar" else "Expand calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Mobile: Show drag bar
            DragBar(colors.onBackgroundColor.copy(0.3f))
        }
    }
}

internal val defaultShape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)

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
    getEventsForDate: (LocalDate) -> List<Any>,
    onToggleExpand: (() -> Unit)? = null,
) {
    val platform = LocalPlatform.current
    val now = remember { now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    // Use rectangular shape on desktop/web, rounded on mobile
    val headerShape = if (platform.isDesktopOrWeb()) {
        RectangleShape
    } else {
        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(headerShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .statusBarsPadding()
    ) {
        header?.invoke()
        CollapsedWeekContent(
            calendarState = calendarState,
            colors = colors,
            weekPagerState = weekPagerState,
            startingWeekPage = startingWeekPage,
            dayDecoration = dayDecoration,
            weekDay = weekDay,
            getEventsForDate = getEventsForDate
        )
        ExpandedMonthContent(
            calendarState = calendarState,
            colors = colors,
            monthPagerState = monthPagerState,
            startingMonthPage = startingMonthPage,
            dayDecoration = dayDecoration,
            monthDay = monthDay,
            getEventsForDate = getEventsForDate
        )
        footer?.invoke()
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
    startingWeekPage: Int,
    getEventsForDate: (LocalDate) -> List<Any>,
) {
    val now = remember { now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    Box {
        HorizontalPager(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            state = weekPagerState,
            verticalAlignment = Alignment.Top,
            key = { page -> page },
            beyondViewportPageCount = 1
        ) { _ ->
            val startOfWeekForPageContent = remember(weekPagerState.currentPage) {
                now.minus(startingWeekPage - weekPagerState.currentPage, DateTimeUnit.WEEK).startOfWeek()
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                var day = startOfWeekForPageContent
                repeat(7) {
                    val events = getEventsForDate(day)
                    key(day, events.size) {
                        weekDay.Content(
                            modifier = modifier.weight(1f),
                            day = day,
                            state = calendarState,
                            colors = colors,
                            dayDecoration = dayDecoration,
                            hasEvents = events.isNotEmpty()
                        )
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
    colors: MotionCalendarColors,
    calendarState: CalendarState,
    monthDay: CalendarDayItem,
    monthPagerState: PagerState,
    startingMonthPage: Int,
    dayDecoration: CalendarDayDecoration?,
    getEventsForDate: (LocalDate) -> List<Any>,
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
                            val events = getEventsForDate(day)
                            monthDay.Content(
                                modifier = modifier.weight(1f),
                                day = day,
                                colors = colors,
                                state = calendarState,
                                dayDecoration = dayDecoration,
                                hasEvents = events.isNotEmpty()
                            )
                        }
                    }
                }
            }
        }
    }
}

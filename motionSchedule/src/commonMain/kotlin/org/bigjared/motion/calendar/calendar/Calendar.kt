package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.util.startOfMonth
import org.bigjared.motion.calendar.util.startOfWeek

@Composable
fun motionCalendarColors(
    primary: Color = MaterialTheme.colorScheme.primary,
    onPrimary: Color = MaterialTheme.colorScheme.onPrimary,
    primaryContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    border: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    backgroundVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundGradientColorLow: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    backgroundGradientColorMed: Color = MaterialTheme.colorScheme.surfaceContainer,
    backgroundGradientColorHigh: Color = MaterialTheme.colorScheme.primaryContainer,
    divider: Color = MaterialTheme.colorScheme.outline
) = MotionCalendarColors(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    border = border,
    onBackgroundColor = onBackgroundColor,
    backgroundColor = backgroundColor,
    backgroundVariant = backgroundVariant,
    backgroundGradientColorLow = backgroundGradientColorLow,
    backgroundGradientColorMed = backgroundGradientColorMed,
    backgroundGradientColorHigh = backgroundGradientColorHigh,
    divider = divider
)

data class MotionCalendarColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val border: Color,
    val onBackgroundColor: Color,
    val backgroundColor: Color,
    val backgroundVariant: Color,
    val backgroundGradientColorLow: Color,
    val backgroundGradientColorMed: Color,
    val backgroundGradientColorHigh: Color,
    val divider: Color,
)

/**
 * A configurable and expandable calendar component.
 *
 * @param calendarState - reference into the internals of the calendar, enables access to things like scroll state,
 * selected date, selected month and more.
 * @param colors - overridable colors used by the calendar
 * @param shape - override the shape of the calendar header
 * @param header - optional composable for the header of the calendar, [DefaultHeader] will provide a month year string,
 * but this can be overridden to whatever the caller prefers
 * @param weekDay - an implementor of [CalendarDayItem] to provide the collapsed UI row element to the calendar
 * @param monthDay - an implementor of [CalendarDayItem] to provide the expanded UI row element to the calendar
 * @param dayDecoration - Decorator for week / month day UI to provide event indicators.
 * @param content - Configure the Item that is the contents of the calendar,
 * this should respond to selected day to show relevant information
 */
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MotionCalender(
    modifier: Modifier = Modifier,
    calendarState: CalendarState = rememberMotionCalendarState(selectedDateMs = now().toEpochMilliseconds()),
    colors: MotionCalendarColors = motionCalendarColors(),
    shape: Shape = defaultShape,
    header: (@Composable () -> Unit)? = { DefaultHeader(calendarState = calendarState, colors = colors) },
    footer: (@Composable () -> Unit)? = { DefaultFooter(colors = colors) },
    weekDay: CalendarDayItem = CalendarDayOfWeek(),
    monthDay: CalendarDayItem = CalendarDayOfMonth(),
    dayDecoration: CalendarDayDecoration? = null,
    content: @Composable (LocalDate) -> Unit,
) {
    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val selectedDate = calendarState.selectedDate.value

    // All this paging logic should be moved to CalendarState, but for right now, it's working

    val startingWeekPage = 1000
    val startingDayPage = 4000
    val startingMonthPage = 4000

    val monthPagerState = rememberPagerState(initialPage = startingMonthPage,
        initialPageOffsetFraction = 0f,
        pageCount = { startingMonthPage * 2 })
    val weekPagerState = rememberPagerState(initialPage = startingWeekPage,
        initialPageOffsetFraction = 0f,
        pageCount = { startingWeekPage * 2 })
    val dayPagerState = rememberPagerState(initialPage = startingDayPage,
        initialPageOffsetFraction = 0f,
        pageCount = { startingDayPage * 2 })

    LaunchedEffect(calendarState.selectedDate.value) {
        val weekAnchorDate = if (selectedDate < now.startOfWeek()) selectedDate.minus(
            6, DateTimeUnit.DAY
        ) else selectedDate
        weekPagerState.scrollToPage(
            startingWeekPage + (now.startOfWeek().daysUntil(weekAnchorDate) / 7)
        )
        val monthAnchorDate = if (selectedDate < now.startOfMonth()) selectedDate.minus(
            1,
            DateTimeUnit.MONTH
        ) else selectedDate
        monthPagerState.scrollToPage(
            startingMonthPage + (now.startOfMonth().monthsUntil(monthAnchorDate))
        )
        dayPagerState.scrollToPage(startingDayPage + (now.daysUntil(selectedDate)))
    }

    LaunchedEffect(dayPagerState.currentPage) {
        calendarState.selectedDate.value =
            now.plus(dayPagerState.currentPage - startingDayPage, DateTimeUnit.DAY)
    }

    LaunchedEffect(weekPagerState.currentPage) {
        calendarState.shownMonth.value =
            now.plus(weekPagerState.currentPage - startingWeekPage, DateTimeUnit.WEEK).startOfWeek()
    }

    LaunchedEffect(monthPagerState.currentPage) {
        calendarState.shownMonth.value =
            now.plus(monthPagerState.currentPage - startingMonthPage, DateTimeUnit.MONTH)
                .startOfMonth()
    }

    SwipeableColumn(
        modifier = modifier,
        swipeableState = calendarState.swipeState,
        headerContent = {
            CalendarHeader(
                calendarState = calendarState,
                header = header,
                footer = footer,
                weekDay = weekDay,
                monthDay = monthDay,
                dayDecoration = dayDecoration,
                monthPagerState = monthPagerState,
                weekPagerState = weekPagerState,
                startingWeekPage = startingWeekPage,
                startingMonthPage = startingMonthPage,
                colors = colors,
                shape = shape
            )
        },
        bodyContent = {
            CalendarBody(
                calendarState = calendarState,
                dayPagerState = dayPagerState,
                startingDayPage = startingDayPage,
                dayContent = content
            )
        },
    )
}
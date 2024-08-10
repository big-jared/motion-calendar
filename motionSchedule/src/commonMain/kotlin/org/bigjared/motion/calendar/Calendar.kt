@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)

package org.bigjared.motion.calendar


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun MotionCalender(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    header: (@Composable () -> Unit)? = { DefaultHeader(calendarState = calendarState) },
    dayDecoration: CalendarDayDecoration? = null,
    content: @Composable (LocalDate) -> Unit,
) {
    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val selectedDate = calendarState.selectedDate.value

    // Potentially this should be moved to CalendarState, but for right now, it's working

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
                monthPagerState = monthPagerState,
                weekPagerState = weekPagerState,
                startingWeekPage = startingWeekPage,
                startingMonthPage = startingMonthPage
            )
        },
        bodyContent = {
            CalendarBody(calendarState = calendarState,
                dayPagerState = dayPagerState,
                startingDayPage = startingDayPage,
                dayContent = content
            )
        },
    )
}
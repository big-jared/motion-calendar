package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.bigjared.motion.calendar.common.Platform
import org.bigjared.motion.calendar.common.LocalPlatform
import org.bigjared.motion.calendar.common.isDesktopOrWeb
import org.bigjared.motion.calendar.util.startOfMonth
import org.bigjared.motion.calendar.util.startOfWeek
import kotlin.math.abs

@Composable
fun motionCalendarColors(
    primary: Color = MaterialTheme.colorScheme.primary,
    onPrimary: Color = MaterialTheme.colorScheme.onPrimary,
    primaryContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    border: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
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

enum class CalendarViewMode { Day, Week, Month }

/**
 * Navigation controls for the calendar pager
 */
@OptIn(ExperimentalFoundationApi::class)
data class CalendarNavigationControls(
    val onNext: () -> Unit,
    val onPrevious: () -> Unit,
    val onNextMonth: () -> Unit,
    val onPreviousMonth: () -> Unit
)

/**
 * Modifier to disable horizontal scrolling on desktop platforms while allowing vertical scrolling
 */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.disableDesktopScrolling(platform: Platform): Modifier {
    return if (platform.isDesktopOrWeb()) {
        this.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    // Only consume horizontal scroll events to allow vertical scrolling
                    event.changes.forEach { change ->
                        val scrollDelta = change.scrollDelta
                        // Only consume if it's primarily a horizontal scroll
                        if (abs(scrollDelta.x) > abs(scrollDelta.y)) {
                            change.consume()
                        }
                    }
                }
            }
        }
    } else {
        this
    }
}

/**
 * A configurable and expandable calendar component.
 *
 * @param calendarState - reference into the internals of the calendar, enables access to things like scroll state,
 * selected date, selected month and more.
 * @param viewMode - the current view mode (Day, Week, or Month)
 * @param colors - overridable colors used by the calendar
 * @param shape - override the shape of the calendar header
 * @param header - optional composable for the header of the calendar, [DefaultHeader] will provide a month year string,
 * but this can be overridden to whatever the caller prefers
 * @param footer - optional composable for the footer of the calendar
 * @param weekDay - an implementor of [CalendarDayItem] to provide the collapsed UI row element to the calendar
 * @param monthDay - an implementor of [CalendarDayItem] to provide the expanded UI row element to the calendar
 * @param dayDecoration - Decorator for week / month day UI to provide event indicators.
 * @param dayContent - Configure the content for day view mode
 * @param weekContent - Configure the content for week view mode
 * @param monthContent - Configure the content for month view mode
 * @param getEventsForDate - function to get events for a specific date (used for indicators)
 * @param onNavigationControlsReady - callback when navigation controls are available (useful for desktop)
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
    footer: (@Composable () -> Unit)? = null,
    weekDay: CalendarDayItem = CalendarDayOfWeek(),
    monthDay: CalendarDayItem = CalendarDayOfMonth(),
    dayDecoration: CalendarDayDecoration? = null,
    dayContent: @Composable (LocalDate) -> Unit = {},
    weekContent: @Composable (LocalDate) -> Unit = {},
    monthContent: @Composable (LocalDate) -> Unit = {},
    getEventsForDate: (LocalDate) -> List<Any> = { emptyList() },
    onNavigationControlsReady: (CalendarNavigationControls) -> Unit = {},
) {
    val platform = LocalPlatform.current
    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val selectedDate = calendarState.selectedDate.value
    val scope = rememberCoroutineScope()

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

    // Toggle function for expanding/collapsing calendar
    val onToggleExpand: () -> Unit = {
        scope.launch {
            val targetState = if (calendarState.swipePercentage > 0.5f) {
                SwipingStates.Collapsed
            } else {
                SwipingStates.Expanded
            }
            calendarState.swipeState.animateTo(targetState)
        }
    }

    // Create footer with toggle functionality
    val footerComposable: (@Composable () -> Unit)? = footer ?: {
        DefaultFooter(
            colors = colors,
            calendarState = calendarState,
            onToggleExpand = onToggleExpand
        )
    }

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

    val viewMode = calendarState.viewMode.value

    LaunchedEffect(viewMode, weekPagerState.currentPage) {
        if (viewMode == CalendarViewMode.Week) {
            calendarState.startOfWeek.value =
                now.plus(weekPagerState.currentPage - startingWeekPage, DateTimeUnit.WEEK).startOfWeek()
        }
    }

    // Navigation controls for desktop/web
    val navigationControls = remember(viewMode, scope) {
        CalendarNavigationControls(
            onNext = {
                scope.launch {
                    when (viewMode) {
                        CalendarViewMode.Day -> dayPagerState.animateScrollToPage(dayPagerState.currentPage + 1)
                        CalendarViewMode.Week -> weekPagerState.animateScrollToPage(weekPagerState.currentPage + 1)
                        CalendarViewMode.Month -> monthPagerState.animateScrollToPage(monthPagerState.currentPage + 1)
                    }
                }
            },
            onPrevious = {
                scope.launch {
                    when (viewMode) {
                        CalendarViewMode.Day -> dayPagerState.animateScrollToPage(dayPagerState.currentPage - 1)
                        CalendarViewMode.Week -> weekPagerState.animateScrollToPage(weekPagerState.currentPage - 1)
                        CalendarViewMode.Month -> monthPagerState.animateScrollToPage(monthPagerState.currentPage - 1)
                    }
                }
            },
            onNextMonth = {
                scope.launch {
                    monthPagerState.animateScrollToPage(monthPagerState.currentPage + 1)
                }
            },
            onPreviousMonth = {
                scope.launch {
                    monthPagerState.animateScrollToPage(monthPagerState.currentPage - 1)
                }
            }
        )
    }

    // Expose navigation controls to caller
    LaunchedEffect(navigationControls) {
        onNavigationControlsReady(navigationControls)
    }

    // Build header with navigation controls if using default
    val headerWithNav: (@Composable () -> Unit)? = if (header == null) {
        null
    } else {
        { DefaultHeader(calendarState = calendarState, colors = colors, navigationControls = navigationControls) }
    }

    SwipeableColumn(
        modifier = modifier,
        headerContent = {
            CalendarHeader(
                calendarState = calendarState,
                header = headerWithNav,
                footer = footerComposable,
                weekDay = weekDay,
                monthDay = monthDay,
                dayDecoration = dayDecoration,
                monthPagerState = monthPagerState,
                weekPagerState = weekPagerState,
                startingWeekPage = startingWeekPage,
                startingMonthPage = startingMonthPage,
                colors = colors,
                shape = shape,
                getEventsForDate = getEventsForDate,
                onToggleExpand = onToggleExpand
            )
        },
        bodyContent = {
            when (viewMode) {
                CalendarViewMode.Day -> {
                    HorizontalPager(
                        modifier = Modifier.fillMaxWidth().disableDesktopScrolling(platform),
                        state = dayPagerState,
                        userScrollEnabled = !platform.isDesktopOrWeb()
                    ) { page ->
                        val date = now.minus(startingDayPage - page, DateTimeUnit.DAY)
                        dayContent(date)
                    }
                }
                CalendarViewMode.Week -> {
                    HorizontalPager(
                        modifier = Modifier.fillMaxWidth().disableDesktopScrolling(platform),
                        state = weekPagerState,
                        userScrollEnabled = !platform.isDesktopOrWeb()
                    ) { page ->
                        val weekStart = now.minus(startingWeekPage - page, DateTimeUnit.WEEK).startOfWeek()
                        weekContent(weekStart)
                    }
                }
                CalendarViewMode.Month -> {
                    HorizontalPager(
                        modifier = Modifier.fillMaxWidth().disableDesktopScrolling(platform),
                        state = monthPagerState,
                        userScrollEnabled = !platform.isDesktopOrWeb()
                    ) { page ->
                        val monthStart = now.minus(startingMonthPage - page, DateTimeUnit.MONTH).startOfMonth()
                        monthContent(monthStart)
                    }
                }
            }
        },
        swipeableState = calendarState.swipeState
    )
}

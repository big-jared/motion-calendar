package org.bigjared.motion.calendar

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class SwipingStates {
    Collapsed,
    Expanded
}

/**
 * Creates a calendar state instance to use in a [MotionCalender] object
 *
 * @param selectedDateMs - initial date the calendar should have selected
 * @param selectableDates - input to configure which dates are selectable
 * @param highlightToday - flag to determine if today should be highlighted in the calendar
 * @param swipeState - input to receive updates / control the swipe state of the calendar (maybe you want to change
 * alpha or ui in the content dependent on animation percent)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun rememberMotionCalendarState(
    selectedDateMs: Long,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    highlightToday: Boolean = true,
    swipeState: SwipeableState<SwipingStates> = rememberSwipeableState(initialValue = SwipingStates.Collapsed),
): CalendarState {
    return rememberSaveable(
        saver = CalendarState.Saver(swipeState, selectableDates)
    ) {
        CalendarState(
            selectedDateMs = selectedDateMs,
            selectableDates = selectableDates,
            highlightToday = highlightToday,
            swipeState = swipeState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
data class CalendarState(
    val selectedDateMs: Long,
    val selectableDates: SelectableDates,
    val highlightToday: Boolean,
    val swipeState: SwipeableState<SwipingStates>,
) {
    var selectedDate = mutableStateOf(selectedDateMs.toLocalDate())
    var shownMonth = mutableStateOf(selectedDateMs.toLocalDate())
    var startOfWeek = mutableStateOf(selectedDateMs.toLocalDate().startOfWeek())
    var startOfMonth = mutableStateOf(selectedDateMs.toLocalDate().startOfMonth())

    val now = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val swipePercentage get() = if (swipeState.progress.to == SwipingStates.Expanded) swipeState.progress.fraction else 1f - swipeState.progress.fraction

    companion object {
        /**
         * The default [Saver] implementation for [CalendarState].
         *
         * @param selectableDates a [SelectableDates] instance that is consulted to check if a date
         * is allowed
         * @param swipeState a [SwipeableState] instance used to determine swipe interaction with calendar
         */
        fun Saver(
            swipeState: SwipeableState<SwipingStates>,
            selectableDates: SelectableDates
        ): Saver<CalendarState, Any> = listSaver(
            save = {
                listOf(
                    it.selectedDateMs,
                    it.highlightToday,
                )
            },
            restore = { value ->
                CalendarState(
                    selectedDateMs = value[0] as Long,
                    highlightToday = value[1] as Boolean,
                    selectableDates = selectableDates,
                    swipeState = swipeState,
                )
            }
        )
    }
}
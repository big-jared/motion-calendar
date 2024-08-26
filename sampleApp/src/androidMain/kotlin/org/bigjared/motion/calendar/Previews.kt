
package org.bigjared.motion.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock.System.now
import org.bigjared.motion.calendar.calendar.MotionCalender
import org.bigjared.motion.calendar.calendar.SwipingStates
import org.bigjared.motion.calendar.calendar.rememberMotionCalendarState
import org.bigjared.motion.calendar.dayGrid.DayGrid
import org.bigjared.motion.calendar.dayGrid.rememberDayGridState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview
@Composable
private fun CalendarPreview() {
    MaterialTheme {
        MotionCalender(
            calendarState = rememberMotionCalendarState(selectedDateMs = now().toEpochMilliseconds())
        ) {
            DayGrid(
                state = rememberDayGridState(day = it, events = simpleTestEvents)
            ) {
                DayEvent(event = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview
@Composable
private fun CalendarPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        MotionCalender(
            calendarState = rememberMotionCalendarState(selectedDateMs = now().toEpochMilliseconds())
        ) {
            DayGrid(
                state = rememberDayGridState(day = it, events = testEvents)
            ) {
                DayEvent(event = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview
@Composable
private fun CalendarExpandedPreview() {
    MaterialTheme {
        MotionCalender(
            calendarState = rememberMotionCalendarState(
                selectedDateMs = now().toEpochMilliseconds(),
                swipeState = rememberSwipeableState(initialValue = SwipingStates.Expanded)),
        ) {
            DayGrid(
                state = rememberDayGridState(day = it, events = testEvents)
            ) {
                DayEvent(event = it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview
@Composable
private fun CalendarExpandedDarkPreview() {
    MaterialTheme(darkColorScheme()) {
        MotionCalender(
            calendarState = rememberMotionCalendarState(
                selectedDateMs = now().toEpochMilliseconds(),
                swipeState = rememberSwipeableState(initialValue = SwipingStates.Expanded)),
        ) {
            DayGrid(
                state = rememberDayGridState(day = it, events = testEvents)
            ) {
                DayEvent(event = it)
            }
        }
    }
}

package org.bigjared.motion.calendar

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock.System.now

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Preview
@Composable
private fun CalendarPreview() {
    MaterialTheme {
        MotionCalender(
            calendarState = rememberMotionCalendarState(selectedDateMs = now().toEpochMilliseconds())
        ) {}
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
        ) {}
    }
}
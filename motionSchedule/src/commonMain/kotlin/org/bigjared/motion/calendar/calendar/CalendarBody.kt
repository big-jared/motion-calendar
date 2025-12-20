@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

@Composable
fun CalendarBody(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    dayPagerState: PagerState,
    startingDayPage: Int,
    dayContent: @Composable (LocalDate) -> Unit
) {
    HorizontalPager(
        modifier = modifier.fillMaxWidth(),
        state = dayPagerState,
        verticalAlignment = Alignment.Top
    ) { page ->
        val date = calendarState.now.minus(startingDayPage - page, DateTimeUnit.DAY)
        dayContent(date)
    }
}

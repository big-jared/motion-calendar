@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.bigjared.motion.calendar.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
fun CalendarBody(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
    dayPagerState: PagerState,
    startingDayPage: Int,
    dayContent: @Composable (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .nestedScroll(TopAppBarDefaults.enterAlwaysScrollBehavior().nestedScrollConnection)
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
}
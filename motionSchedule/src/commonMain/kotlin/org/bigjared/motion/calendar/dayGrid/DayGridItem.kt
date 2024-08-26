package org.bigjared.motion.calendar.dayGrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.bigjared.motion.calendar.util.toHourMinuteString

@Composable
fun BasicDayEvent(modifier: Modifier = Modifier, event: TimedEvent) {
    Column(
        modifier.fillMaxSize().background(
            shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest
        ).clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "${event.start.toHourMinuteString(showAmPm = false)}-${event.end.toHourMinuteString(showAmPm = false)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
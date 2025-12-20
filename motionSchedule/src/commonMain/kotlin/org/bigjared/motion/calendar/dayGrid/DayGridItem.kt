package org.bigjared.motion.calendar.dayGrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.bigjared.motion.calendar.util.toHourMinuteString

@Composable
fun BasicDayEvent(modifier: Modifier = Modifier, event: TimedEvent) {
    Column(
        modifier.fillMaxSize().background(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ).clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "${event.start.toHourMinuteString(showAmPm = false)}-${
                event.end.toHourMinuteString(
                    showAmPm = false
                )
            }",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun BasicInstantEvent(modifier: Modifier = Modifier, events: Collection<InstantTimedEvent>) {
    val event = events.first()
    val color = Color(event.color)
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier, color = color)
        Row(modifier = Modifier.align(Alignment.End).background(
            color = color,
            RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp),
        ).padding(2.dp).padding(horizontal = 2.dp)) {
            Text(
                modifier = Modifier,
                text = event.text,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                modifier = Modifier,
                text = " due at ${event.time.toHourMinuteString()}",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

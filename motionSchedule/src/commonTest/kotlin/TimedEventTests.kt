import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalTime
import org.bigjared.motion.calendar.dayGrid.TimedEvent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TimedEventTests {
    @Test
    fun overlapSimpleCase() {
        val first = SampleTimedEvent(
            start = LocalTime(hour = 8, minute = 10),
            duration = 1.hours + 2.minutes,
            color = Color.Blue
        )

        val second = SampleTimedEvent(
            start = LocalTime(hour = 8, minute = 0),
            duration = 1.hours + 2.minutes,
            color = Color.Magenta
        )

        assertTrue(first.overlaps(second))
    }

    @Test
    fun overlapEdgeCase() {
        val first = SampleTimedEvent(
            start = LocalTime(hour = 8, minute = 10),
            duration = 1.hours + 2.minutes,
            color = Color.Blue
        )

        val second = SampleTimedEvent(
            start = LocalTime(hour = 9, minute = 11),
            duration = 1.hours + 2.minutes,
            color = Color.Magenta
        )

        assertTrue(first.overlaps(second))
    }

    @Test
    fun doesNotOverlapEdgeCase() {
        val first = SampleTimedEvent(
            start = LocalTime(hour = 8, minute = 10),
            duration = 1.hours + 2.minutes,
            color = Color.Blue
        )

        val second = SampleTimedEvent(
            start = LocalTime(hour = 9, minute = 13),
            duration = 1.hours + 2.minutes,
            color = Color.Magenta
        )

        assertFalse(first.overlaps(second))
    }
}

class SampleTimedEvent(
    override val start: LocalTime,
    override val duration: Duration,
    val color: Color
) : TimedEvent
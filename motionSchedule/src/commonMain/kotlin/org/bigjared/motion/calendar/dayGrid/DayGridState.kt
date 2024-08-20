package org.bigjared.motion.calendar.dayGrid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import kotlinx.datetime.LocalDate

data class DayGridState<T: TimedEvent>(
    val day: LocalDate,
    val events: Set<T>,
) {
    val sortedEvents = mutableStateOf<List<List<T>>>(emptyList())
    val firstEvent by lazy { events.minOf { it.start } }

    // TODO, add tests, make this readable
    init {
        val eventGroups = mutableListOf<List<T>>()
        events.sortedBy {
            it.start
        }.forEach { event ->
            for (i in 0 until eventGroups.size) {
                val currentGroup = eventGroups[i]
                if (currentGroup.any { it.overlaps(event) }) {
                    eventGroups[i] = (currentGroup + event)
                    return@forEach
                }
            }

            eventGroups.add(listOf(event))
        }

        sortedEvents.value = eventGroups
    }

    companion object {
        fun <T: TimedEvent> Saver(
            day: LocalDate,
            events: Set<T>
        ): Saver<DayGridState<T>, Any> = listSaver(
            save = {
                listOf(
                    day.toEpochDays(),
                )
            },
            restore = { value ->
                DayGridState(
                    day = LocalDate.fromEpochDays(value[0]),
                    events = events
                )
            }
        )
    }
}

@Composable
fun <T: TimedEvent> rememberDayGridState(
    day: LocalDate,
    events: Set<T>,
): DayGridState<T> {
    return rememberSaveable(
        saver = DayGridState.Saver(day, events)
    ) {
        DayGridState(
            day = day,
            events = events
        )
    }
}
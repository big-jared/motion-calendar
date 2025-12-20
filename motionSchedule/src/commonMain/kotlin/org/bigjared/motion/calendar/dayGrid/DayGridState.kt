package org.bigjared.motion.calendar.dayGrid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.datetime.LocalDate
import org.bigjared.motion.calendar.util.plus

data class EventLayoutParams<T : TimedEvent>(
    val event: T,
    val columnIndex: Int,
    val totalColumns: Int
)

data class DayGridState<T: TimedEvent>(
    val day: LocalDate,
    var events: Set<T>,
    val gridRange: GridRange,
) {
    private val _eventLayoutParams = mutableStateOf<List<EventLayoutParams<T>>>(emptyList())
    val eventLayoutParams = derivedStateOf { _eventLayoutParams.value }

    fun reset(events: Set<T>) {
        this.events = events
        calculateEventLayoutParams()
    }

    private fun TimedEvent.overlaps(other: TimedEvent): Boolean {
        val thisEnd = start.plus(duration)
        val otherEnd = other.start.plus(other.duration)
        return !(thisEnd <= other.start || start >= otherEnd)
    }

    fun calculateEventLayoutParams() {
        if (events.isEmpty()) {
            _eventLayoutParams.value = emptyList()
            return
        }
        val eventList = events.sortedWith(compareBy({ it.start }, { it.duration.inWholeMinutes }))
        val visited = mutableSetOf<T>()
        val params = mutableListOf<EventLayoutParams<T>>()

        // Helper to find all events overlapping with a given event (directly or indirectly)
        fun findConflictGroup(startEvent: T, group: MutableSet<T>) {
            val queue = ArrayDeque<T>()
            queue.add(startEvent)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (group.add(current)) {
                    for (other in eventList) {
                        if (other != current && current.overlaps(other) && !group.contains(other)) {
                            queue.add(other)
                        }
                    }
                }
            }
        }

        for (event in eventList) {
            if (visited.contains(event)) continue
            // Find the full conflict group for this event
            val group = mutableSetOf<T>()
            findConflictGroup(event, group)
            visited.addAll(group)
            // Sort group by start time
            val groupList = group.sortedWith(compareBy({ it.start }, { it.duration.inWholeMinutes }))
            // Assign columns within this group using interval partitioning
            val groupColumnEndTimes = mutableListOf<Long>()
            val groupParams = mutableMapOf<T, Pair<Int, Int>>()
            for (e in groupList) {
                val eStart = e.start.hour * 3600 + e.start.minute * 60 + e.start.second
                val eEnd = e.start.plus(e.duration)
                val eEndSeconds = eEnd.hour * 3600 + eEnd.minute * 60 + eEnd.second
                // Free up columns that have ended
                for (i in groupColumnEndTimes.indices) {
                    if (groupColumnEndTimes[i] <= eStart) {
                        groupColumnEndTimes[i] = -1L
                    }
                }
                // Find first available column
                var assignedColumn = groupColumnEndTimes.indexOfFirst { it == -1L }
                if (assignedColumn == -1) {
                    assignedColumn = groupColumnEndTimes.size
                    groupColumnEndTimes.add(eEndSeconds.toLong())
                } else {
                    groupColumnEndTimes[assignedColumn] = eEndSeconds.toLong()
                }
                // Count active columns for this group
                val concurrentColumns = groupColumnEndTimes.count { it != -1L }
                groupParams[e] = assignedColumn to concurrentColumns
            }
            // The max number of columns in this group
            val maxColumns = groupParams.values.maxOfOrNull { it.second } ?: 1
            for ((e, pair) in groupParams) {
                params.add(EventLayoutParams(e, pair.first, maxColumns))
            }
        }
        _eventLayoutParams.value = params
    }

    companion object {
        fun <T: TimedEvent> Saver(
            day: LocalDate,
            events: Set<T>,
            gridRange: GridRange
        ): Saver<DayGridState<T>, Any> = listSaver(
            save = {
                listOf(
                    day.toEpochDays(),
                )
            },
            restore = { value ->
                DayGridState(
                    day = LocalDate.fromEpochDays(value[0]),
                    events = events,
                    gridRange,
                ).apply { calculateEventLayoutParams() }
            }
        )
    }
}

@Composable
fun <T: TimedEvent> rememberDayGridState(
    day: LocalDate,
    events: Set<T>,
    gridRange: GridRange = defaultGridRange,
): DayGridState<T> {
    return rememberSaveable(
        saver = DayGridState.Saver(day, events, gridRange)
    ) {
        DayGridState(
            day = day,
            events = events,
            gridRange = gridRange,
        ).apply { calculateEventLayoutParams() }
    }
}

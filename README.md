# Motion Calendar

Compose Multiplatform calendar and day grid components.

Targeting Android, iOS, Desktop and WASM.

## Features

- Day, Week, and Month view modes with built-in toggle
- Expandable/collapsible month calendar
- Customizable colors and theming
- Configurable day decorations and event indicators
- Platform-aware UI (swipe on mobile, buttons on desktop/web)
- DayGrid component for timed event display
- Week view with synchronized scrolling

https://github.com/user-attachments/assets/0169bc11-e860-466f-834c-ae2d056eee1b

https://github.com/user-attachments/assets/4eec7c67-9cc4-4b7b-a169-0680f219871f

https://github.com/user-attachments/assets/0a8389b6-b968-4f7f-9d00-110c8858e4e3

https://github.com/user-attachments/assets/d1eed4ff-779c-4672-a6af-0236b2803c9e


## Installation

```kotlin
implementation("io.github.big-jared:motion-calendar:1.0.1")
```

## Basic Usage

```kotlin
MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds()
    ),
    dayContent = { day ->
        // Your day view content
    },
    weekContent = { weekStart ->
        // Your week view content
    }
)
```

## Day/Week View Toggle

The calendar includes a built-in Day/Week toggle in the header. You can read or modify the view mode via the calendar state:

```kotlin
val calendarState = rememberMotionCalendarState(
    selectedDateMs = now().toEpochMilliseconds()
)

// Read current view mode
val currentMode = calendarState.viewMode.value

// Change view mode programmatically
calendarState.viewMode.value = CalendarViewMode.Week
```

## DayGrid Component

Display timed events in a scrollable day grid:

```kotlin
MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds()
    ),
    dayContent = { day ->
        DayGrid(
            modifier = Modifier.fillMaxWidth(),
            state = rememberDayGridState(
                day = day,
                events = events[day] ?: emptySet(),
                gridRange = GridRange(
                    start = LocalTime(hour = 0, minute = 0),
                    duration = 24.hours
                )
            ),
            initialScrollTime = LocalTime(8, 0),
            eventUi = { event ->
                // Your event UI
            }
        )
    }
)
```

## Week View with Synchronized Scrolling

```kotlin
val sharedScrollState = rememberScrollState()

MotionCalender(
    calendarState = calendarState,
    weekContent = { weekStart ->
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(7) { dayOffset ->
                val day = weekStart.plus(dayOffset, DateTimeUnit.DAY)
                DayGrid(
                    modifier = Modifier.weight(1f),
                    state = rememberDayGridState(day = day, events = events[day] ?: emptySet()),
                    sharedScrollState = sharedScrollState,
                    showTimeLabels = dayOffset == 0,
                    eventUi = { event -> /* Your event UI */ }
                )
            }
        }
    }
)
```

## Event Indicators

Show indicators for days with events:

```kotlin
MotionCalender(
    calendarState = calendarState,
    dayDecoration = CalendarDayDecoration(
        alignment = CalendarDecorationAlignment.Below,
        content = { day ->
            if (events.containsKey(day)) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }
        }
    ),
    getEventsForDate = { day -> events[day]?.toList() ?: emptyList() },
    dayContent = { day -> /* ... */ }
)
```

## Expand/Collapse Calendar

On mobile, swipe to expand/collapse the month view. On desktop/web, use the arrow button. You can also control it programmatically:

```kotlin
val swipeState = rememberSwipeableState(SwipingStates.Collapsed)
val scope = rememberCoroutineScope()

MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds(),
        swipeState = swipeState
    ),
    dayContent = { day -> /* ... */ }
)

// Toggle programmatically
scope.launch {
    swipeState.animateTo(
        if (swipeState.currentValue == SwipingStates.Expanded)
            SwipingStates.Collapsed
        else
            SwipingStates.Expanded
    )
}
```

## Platform Support

| Platform | Swipe Gestures | Navigation Buttons | Horizontal Paging |
|----------|---------------|-------------------|-------------------|
| Android  | Yes           | No                | Swipe             |
| iOS      | Yes           | No                | Swipe             |
| Desktop  | No            | Yes               | Buttons           |
| Web      | No            | Yes               | Buttons           |

## Sample App

Check out the sample app for a complete implementation example!

## Production Usage

This library powers the calendar in [Homeroom](https://thehomeroom.app).

## License

MIT

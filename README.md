Compose multiplatform calendar and day grid components

Targeting Android, iOS, Desktop and Wasm. 

Features:
- Overridable colors
- Configurable day decoration
- Day, Week, and Month paging
- Customizable event indicators
- iOS, Android, Desktop and Wasm support
  - The touch interactions are designed for Android and iOS, with desktop / web implementations being experimental

https://github.com/user-attachments/assets/426e5058-470c-4079-b87f-0baab161d2d8

Gradle Import:

```
implementation("io.github.big-jared:motion-calendar:(latest release)")
```

Basic Usage:

```
MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds()
    ),
    content = { day ->
        // Show your UI below calendar here
    }
)
```

Use swipeState to control calendar expansion directly

```
val coScope = rememberCoroutineScope()
val swipeState = rememberSwipeableState(SwipingStates.Expanded)
MotionCalender(calendarState = rememberMotionCalendarState(
    selectedDateMs = now().toEpochMilliseconds(),
    swipeState = swipeState
), content = { day ->
    Box(Modifier.fillMaxSize()) {
        Button(modifier = Modifier.align(Alignment.Center), onClick = {
            coScope.launch {
                swipeState.animateTo(
                    when (swipeState.currentValue) {
                        SwipingStates.Expanded -> SwipingStates.Collapsed
                        SwipingStates.Collapsed -> SwipingStates.Expanded
                    }
                )
            }
        }) {
            Text("Toggle")
        }
    }
})`
```

Implement the DayGrid component as the content of your calendar if desired.

**NOTE** DayGrid is experimental and will be changing quite a bit.

```
MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds(),
        swipeState = swipeState,
    ),
    content = { day ->
        DayGrid(modifier = Modifier.fillMaxWidth(), state = rememberDayGridState(
            day = day,
            events = events[day] ?: emptySet(),
            gridRange = GridRange(
                start = LocalTime(hour = 8, minute = 0),
                duration = 12.hours
            )
        ), eventUi = { sampleTimedEvent ->
            DayEvent(event = sampleTimedEvent)
        })
    }
)
```

Check out the sample app for an example implementation!

License: MIT

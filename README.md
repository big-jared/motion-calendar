[![Publish Artifacts](https://github.com/big-jared/motion-calendar/actions/workflows/publish.yaml/badge.svg)](https://github.com/big-jared/motion-calendar/actions/workflows/publish.yaml)

Kotlin multiplatform calendar component

Targeting Android and ios currently. Could be extended to desktop / wasm easily, but the touch interactions probably wouldn't work well.

https://github.com/user-attachments/assets/b82525ae-e204-4590-b553-10b989a2d5db

Gradle Import:

```
implementation("io.github.big-jared:motion-calendar:(latest release)")
```

Usage:

```
MotionCalender(
    calendarState = rememberMotionCalendarState(
        selectedDateMs = now().toEpochMilliseconds()
    ),
    content = { day ->
        DayColumn()
    }
)
```

Use swipeState to control calendar directly

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

Upcoming: override colors, event indicators per day, Wasm / Desktop support

License: MIT
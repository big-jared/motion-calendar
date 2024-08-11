Kotlin multiplatform calendar component

Targeting Android and ios currently. Could be extended to desktop / wasm easily, but the touch interactions probably wouldn't work well.

https://github.com/user-attachments/assets/b82525ae-e204-4590-b553-10b989a2d5db

Gradle Import:

```
implementation("io.github.big-jared:motion-calendar:0.0.1(latest release)")
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

Display whatever content you want below the component. In the sample, I'm showing gridelines, but you can do whatever.

License: MIT
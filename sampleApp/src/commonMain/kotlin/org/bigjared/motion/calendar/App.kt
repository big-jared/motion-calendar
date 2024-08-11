package org.bigjared.motion.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            MotionCalender(
                calendarState = rememberMotionCalendarState(
                    selectedDateMs = now().toEpochMilliseconds()
                ),
                content = { day ->
                    DayColumn()
                }
            )
        }
    }
}


@Composable
fun DayColumn() {
    val containerColor = MaterialTheme.colorScheme.primary
    val fontResolver = LocalFontFamilyResolver.current

    Box(modifier = Modifier.fillMaxWidth()
        .height(1000.dp)
        .drawWithContent {
            gridLines(fontResolver, containerColor)
        })
}


fun ContentDrawScope.gridLines(fontResolver: FontFamily.Resolver, primaryColor: Color) {
    val interval = size.height / 24
    repeat((1..24).count()) { hours ->
        val y = interval * hours
        drawLine(
            primaryColor,
            Offset(0f, y),
            Offset(size.width, y),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f))
        )

        drawText(
            textMeasurer = TextMeasurer(
                defaultDensity = Density(16.dp.value),
                defaultLayoutDirection = LayoutDirection.Ltr,
                defaultFontFamilyResolver = fontResolver
            ),
            style = TextStyle(
                fontSize = 12.sp
            ),
            text = LocalTime.fromSecondOfDay(hours * (60 * 60))
                .toHourMinuteString(showMinutes = false),
            topLeft = Offset(8.dp.value, y)
        )
    }
}

fun LocalTime.toHourMinuteString(showMinutes: Boolean = true): String {
    val isAm = this.hour < 12
    val minuteText =
        if (this.minute > 10) this.minute else "0${this.minute}"
    val hourText =
        if (this.hour == 0) 12 else if (this.hour < 13) this.hour else this.hour - 12
    return "$hourText${if(showMinutes)":$minuteText" else ""} ${if (isAm) "am" else "pm"}"
}
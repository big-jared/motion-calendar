package org.bigjared.motion.calendar

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalTime

// Homeroom color palette
val green = Color(0xff27ae60)
val lightGreen = Color(0xff2ecc71)
val turquoise = Color(0xff16a085)
val lightTurquoise = Color(0xff1abc9c)
val blue = Color(0xff2980b9)
val lightBlue = Color(0xff3498db)
val pink = Color(0xfff691a4)
val darkPink = Color(0xffd3697b)
val purple = Color(0xff8e44ad)
val lightPurple = Color(0xffb677d0)
val navy = Color(0xff2c3e50)
val lightNavy = Color(0xff34495e)
val yellow = Color(0xfff39c12)
val lightYellow = Color(0xfff1c40f)
val orange = Color(0xffd35400)
val lightOrange = Color(0xffe67e22)
val red = Color(0xffc0392b)
val lightRed = Color(0xffe74c3c)
val primaryBlue = Color(0xFF4599FF)

// Course theme colors
val oceanBlue = Color(0xff2980b9)
val tealGreen = Color(0xff16a085)
val royalPurple = Color(0xff8e44ad)
val grassyGreen = Color(0xff8bc34a)
val citrusSplash = Color(0xffe67e22)
val rosyRed = Color(0xfff64e60)

internal fun LocalTime.toHourMinuteString(showMinutes: Boolean = this.minute != 0, showAmPm: Boolean = true): String {
    val isAm = this.hour < 12
    val minuteText = if (this.minute >= 10) this.minute else "0${this.minute}"
    val hourText = if (this.hour == 0) 12 else if (this.hour < 13) this.hour else this.hour - 12
    return "$hourText${if (showMinutes) ":$minuteText" else ""}${if (showAmPm) if (isAm) " am" else " pm" else ""}"
}

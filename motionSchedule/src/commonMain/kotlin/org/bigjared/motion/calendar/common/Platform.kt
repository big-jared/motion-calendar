package org.bigjared.motion.calendar.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Platform enum for platform-specific behavior in the calendar.
 */
enum class Platform {
    iOS, Android, Desktop, Web
}

/**
 * Expect declaration for getting the current platform.
 */
expect fun getPlatform(): Platform

/**
 * CompositionLocal for providing the current platform.
 * Uses getPlatform() as the default.
 */
val LocalPlatform = staticCompositionLocalOf { getPlatform() }

/**
 * Returns whether the current platform is desktop.
 */
@Composable
fun isDesktopPlatform(): Boolean = LocalPlatform.current == Platform.Desktop

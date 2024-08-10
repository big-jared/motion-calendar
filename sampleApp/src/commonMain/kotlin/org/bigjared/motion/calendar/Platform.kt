package org.bigjared.motion.calendar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.bigjared.motion.calendar.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sample App",
    ) {
        App()
    }
}

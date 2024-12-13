package org.bigjared.motion.calendar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AlertDialogWithContent(
    modifier: Modifier = Modifier,
    title: String? = null,
    buttonText: String = "Ok",
    color: Color = AlertDialogDefaults.containerColor,
    onClose: () -> Unit = {},
    onConfirm: () -> Unit = {},
    content: @Composable () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        containerColor = color,
        onDismissRequest = {
            onClose()
        },
        confirmButton = {
            Button(modifier = Modifier.padding(top = 24.dp), onClick = {
                onConfirm()
                onClose()
            }) {
                Text(buttonText)
            }
        },
        title = title?.let {
            {
                Text(it)
            }
        },
        text = {
            content()
        },
    )
}
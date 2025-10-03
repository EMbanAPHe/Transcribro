package com.embanaphe.transcribro.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.embanaphe.transcribro.engine.EngineProvider

/**
 * Minimal Compose settings row to toggle using the system speech engine (e.g. Whisper+).
 * Call EngineSettings() somewhere in your Settings screen Composable.
 */
@Composable
fun EngineSettings(context: Context) {
    var useSystem by remember { mutableStateOf(EngineProvider.isSystemPreferred(context)) }
    ListItem(
        headlineText = { Text("Use system speech engine (Whisper+)") },
        supportingText = { Text("Route dictation to the device's default recognition service") },
        trailingContent = {
            Switch(checked = useSystem, onCheckedChange = {
                useSystem = it
                EngineProvider.setSystemPreferred(context, it)
            })
        },
        modifier = Modifier.fillMaxWidth()
    )
}

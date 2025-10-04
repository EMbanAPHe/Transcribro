package dev.soupslurpr.transcribro.engine

import android.content.Context

/**
 * Abstraction so UI doesn't care which backend (system vs local) is used.
 */
interface SpeechEngine {
    fun startListening(
        context: Context,
        languageTag: String? = null,
        preferOffline: Boolean = true,
        onResult: (text: String, isFinal: Boolean) -> Unit
    )

    fun stop()

    val isRunning: Boolean
}

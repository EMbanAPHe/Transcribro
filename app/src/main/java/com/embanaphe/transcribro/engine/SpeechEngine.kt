package com.embanaphe.transcribro.engine

import android.content.Context

/**
 * Simple abstraction so we can swap between the local whisper.cpp engine and
 * the System Speech (Whisper+) engine without touching the keyboard UI.
 */
interface SpeechEngine {
    /**
     * Start listening. Implementations should emit partial and final results via [onResult].
     *
     * @param languageTag BCP-47 tag (e.g., "en-AU", "en-US"). Null = system default.
     * @param preferOffline Hint to engine to avoid network usage when possible.
     * @param onResult Callback for text results. When [isFinal] is true, commit the text.
     */
    fun startListening(
        context: Context,
        languageTag: String? = null,
        preferOffline: Boolean = true,
        onResult: (text: String, isFinal: Boolean) -> Unit
    )

    /** Stop listening and release resources. */
    fun stop()

    /** True if engine is currently recording / recognizing. */
    val isRunning: Boolean
}

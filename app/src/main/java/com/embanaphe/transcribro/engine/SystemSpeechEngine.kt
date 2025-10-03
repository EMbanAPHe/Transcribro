package com.embanaphe.transcribro.engine

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

/**
 * System-backed speech engine that routes recognition through the device's
 * default RecognitionService (e.g., Whisper+ when selected in system settings).
 */
class SystemSpeechEngine : SpeechEngine {

    private var recognizer: SpeechRecognizer? = null
    override var isRunning: Boolean = false
        private set

    override fun startListening(
        context: Context,
        languageTag: String?,
        preferOffline: Boolean,
        onResult: (text: String, isFinal: Boolean) -> Unit
    ) {
        if (isRunning) return

        // Permission check (app-level). UI should have requested this earlier.
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasMic) {
            onResult.invoke("", true) // Signal failure gracefully
            return
        }

        val rec = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = rec

        rec.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { }
            override fun onBeginningOfSpeech() { }
            override fun onRmsChanged(rmsdB: Float) { }
            override fun onBufferReceived(buffer: ByteArray?) { }
            override fun onEndOfSpeech() { }
            override fun onError(error: Int) {
                isRunning = false
                onResult.invoke("", true)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    onResult.invoke(data[0], false)
                }
            }
            override fun onResults(results: Bundle?) {
                isRunning = false
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    onResult.invoke(data[0], true)
                } else {
                    onResult.invoke("", true)
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) { }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
            if (!languageTag.isNullOrBlank()) {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            }
        }

        isRunning = true
        rec.startListening(intent)
    }

    override fun stop() {
        recognizer?.apply {
            try { stopListening() } catch (_: Throwable) {}
            try { cancel() } catch (_: Throwable) {}
            try { destroy() } catch (_: Throwable) {}
        }
        recognizer = null
        isRunning = false
    }
}

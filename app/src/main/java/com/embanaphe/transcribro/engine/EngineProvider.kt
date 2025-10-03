package com.embanaphe.transcribro.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Very small preference-backed selector for which engine to use.
 * If you also ship the local whisper.cpp engine, provide it as `localEngine`.
 */
object EngineProvider {
    private const val PREFS = "engine_prefs"
    private const val KEY_USE_SYSTEM = "use_system_speech"

    var localEngine: SpeechEngine? = null // optional plug-in
    private val systemEngine by lazy { SystemSpeechEngine() }

    fun current(context: Context): SpeechEngine {
        return if (isSystemPreferred(context)) systemEngine
        else localEngine ?: systemEngine // fall back to system if local not set
    }

    fun isSystemPreferred(context: Context): Boolean =
        prefs(context).getBoolean(KEY_USE_SYSTEM, true)

    fun setSystemPreferred(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_USE_SYSTEM, value) }
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

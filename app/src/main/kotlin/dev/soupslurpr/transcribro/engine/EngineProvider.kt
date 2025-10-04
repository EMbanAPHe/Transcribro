package dev.soupslurpr.transcribro.engine

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Chooses which engine to use based on a persisted toggle.
 * Optional: set [localEngine] if you also wire a local whisper.cpp engine later.
 */
object EngineProvider {
    private const val PREFS = "engine_prefs"
    private const val KEY_USE_SYSTEM = "use_system_speech"

    var localEngine: SpeechEngine? = null
    private val systemEngine by lazy { SystemSpeechEngine() }

    fun current(context: Context): SpeechEngine {
        return if (isSystemPreferred(context)) systemEngine
        else localEngine ?: systemEngine
    }

    fun isSystemPreferred(context: Context): Boolean =
        prefs(context).getBoolean(KEY_USE_SYSTEM, true)

    fun setSystemPreferred(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_USE_SYSTEM, value) }
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

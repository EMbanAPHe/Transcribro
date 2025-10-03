# Transcribro → Whisper+ Engine Bridge (Drop-in Update)

This package adds a **System Speech Engine** to Transcribro so it can route dictation
to **Whisper+** (org.woheller69.whisperplus) via Android's `SpeechRecognizer` / `RecognizerIntent`,
while keeping Transcribro's keyboard UX. It also changes the `applicationId` so you can
**install it alongside the original** app.

> You do **not** need to merge Whisper+ source. Users install Whisper+ separately from F‑Droid
> and set it as the default voice input provider. When the "System Speech Engine" is selected
> in Transcribro's settings, dictation is handled by Whisper+ under the hood.

---

## What’s included

- `app/src/main/java/.../engine/`:
  - `SpeechEngine.kt` – small interface abstraction
  - `SystemSpeechEngine.kt` – implementation backed by Android `SpeechRecognizer`
  - `EngineProvider.kt` – selects engine based on a shared preference toggle
- `app/src/main/java/.../settings/EngineSettings.kt` – a minimal Compose settings row to toggle **Use system speech engine**.
- Patches:
  - `patches/app_build_gradle.diff` – sets a new `applicationId` / `applicationIdSuffix` so this build can be installed alongside the original Transcribro.
  - `patches/AndroidManifest.xml.diff` – adds `RECORD_AUDIO` permission and queries for speech recognition.
  - (Optional) `patches/KeyboardService_usage_example.diff` – example of how to call the engine from your keyboard / service code.

You can apply diffs in the GitHub web UI manually by editing the mentioned files.
If file names differ in your fork, follow the comments inside the code to place calls
where you already start/stop dictation.

---

## Quick steps (GitHub in browser)

1. **Create a branch** in your fork: `feature/system-speech-engine`.
2. **Upload** the following new files into your repo (create folders if needed):

   - `app/src/main/java/com/embanaphe/transcribro/engine/SpeechEngine.kt`
   - `app/src/main/java/com/embanaphe/transcribro/engine/SystemSpeechEngine.kt`
   - `app/src/main/java/com/embanaphe/transcribro/engine/EngineProvider.kt`
   - `app/src/main/java/com/embanaphe/transcribro/settings/EngineSettings.kt`

3. **Edit** these existing files in the GitHub editor (copy the snippets from the `.diff` files in this folder):

   - `app/build.gradle` (or `app/build.gradle.kts`): change `applicationId` to a unique one, e.g. `com.embanaphe.transcribro.bridge`. Also add `versionNameSuffix = "-whisperbridge"`.
   - `app/src/main/AndroidManifest.xml`: ensure `RECORD_AUDIO` permission and add an intent queries section for speech (shown below).

4. **Wire the engine** in your dictation start/stop code:
   - Replace direct calls to your local whisper.cpp path with:
     ```kotlin
     val engine = EngineProvider.current(context)
     engine.startListening(context, languageTag = "en-AU") { partial, isFinal ->
         // Update your composing text with `partial`
         // Commit text when `isFinal` == true
     }
     // Later, to stop:
     engine.stop()
     ```
   - If you already have a listener, just forward its partial/final text to the same code path.

5. **Add the settings toggle** to your Settings screen:
   - Insert `EngineSettings()` composable into your existing settings list.

6. **Build & install**. Make sure Whisper+ is installed and set as the default voice input
   (System settings → System → Languages & input → On‑device recognition service).

7. In Transcribro’s settings, enable **Use system speech engine (Whisper+)**.
   Start dictation: results should stream from Whisper+ into the Transcribro keyboard UI.

---

## Manifest snippets

Add this to `app/src/main/AndroidManifest.xml` inside `<manifest>`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

(If your minSdk ≥ 31 and you target modern SDKs, you do **not** need extra queries to call
`SpeechRecognizer`. If you do app‑side package checks, add a `<queries>` block:)

```xml
<queries>
    <intent>
        <action android:name="android.speech.RecognitionService" />
    </intent>
</queries>
```

---

## Licensing

- Your fork remains under Transcribro’s original license (ISC) because this integration
  calls **Whisper+** as a **separate app** through Android’s public APIs. You are not
  incorporating GPL code; users install Whisper+ separately.

---

## Troubleshooting

- **No partial results**: some Speech services don’t stream partials consistently. The code enables partials; Whisper+ supports them. If you still don’t see partials, keep the onFinal path as a fallback.
- **`ERROR_RECOGNIZER_BUSY`**: show a toast and call `stop()` then retry after a short delay.
- **Whisper+ not installed**: the engine gracefully falls back to local (whisper.cpp) if you keep that path; otherwise show a dialog guiding the user to install Whisper+ from F‑Droid.


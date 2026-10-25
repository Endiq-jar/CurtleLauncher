package com.app.crutlelauncher.feature.log

import com.app.crutlelauncher.feature.ai.CrutleAiGemini
import com.app.crutlelauncher.feature.ai.CrutleAiLanguage
import com.app.crutlelauncher.feature.ai.CrutleAiPrompt
import com.app.crutlelauncher.setting.AllSettings

object AiCrashAdvisor {

    private const val MAX_LOG_CHARS = 6000

    /**
     * The diagnosis persona comes from [CrutleAiPrompt] so the crash advisor, the on-device
     * assistant and any future Crutle AI backend all reason from one spec (including the
     * launcher-side memory-management and OpenGL state-optimization sections, which are
     * exactly the two subsystems a mobile Minecraft crash usually implicates).
     */
    private val SYSTEM_PROMPT: String = CrutleAiPrompt.crashAdvisorSystemPrompt()

    /**
     * The system prompt for one call: the shared Crutle AI crash persona plus the instruction to
     * write the answer in the user's own language. The crash path runs from the game JVM's exit
     * hook, which has no Context, so [languageTag] is optional and falls back to the process
     * locale - still the user's language, just read a different way.
     */
    private fun systemPromptFor(languageTag: String?): String {
        val language = languageTag?.takeIf { it.isNotBlank() } ?: CrutleAiLanguage.systemLanguage()
        return SYSTEM_PROMPT + "\n\n" +
            CrutleAiPrompt.languageInstruction(language, CrutleAiLanguage.displayName(language))
    }

    /**
     * Returns a short AI-generated fix suggestion for [logText], or null if AI crash help is
     * disabled, no API key is configured, or the request fails for any reason. Blocking —
     * only call from a background thread (this mirrors [CrashAnalyzer.analyzeGameExit], which
     * already runs off the main thread as part of the JVM-exit handling path).
     */
    @JvmStatic
    @JvmOverloads
    fun getSuggestion(logText: String, languageTag: String? = null): String? {
        if (!runCatching { AllSettings.aiCrashHelpEnabled.getValue() }.getOrDefault(false)) return null
        if (logText.isBlank()) return null
        // No key (and no key baked into the build) = no crash help; the rule engine's own
        // diagnoses are what the user sees, exactly as before this feature existed.
        if (CrutleAiGemini.apiKey().isEmpty()) return null

        return CrutleAiGemini.complete(
            systemPrompt = systemPromptFor(languageTag),
            userText = "Crash log tail:\n\n" + logText.takeLast(MAX_LOG_CHARS),
            // Cooler and shorter than a chat answer: this renders in a small dialog next to
            // the rule engine's own findings, not as a conversation.
            temperature = 0.2,
            maxOutputTokens = 400
        )
    }
}

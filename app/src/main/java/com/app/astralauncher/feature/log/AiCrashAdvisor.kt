package com.app.astralauncher.feature.log

import com.app.astralauncher.feature.ai.AstraAiGemini
import com.app.astralauncher.feature.ai.AstraAiLanguage
import com.app.astralauncher.feature.ai.AstraAiPrompt
import com.app.astralauncher.setting.AllSettings

object AiCrashAdvisor {

    private const val MAX_LOG_CHARS = 6000

    /**
     * The diagnosis persona comes from [AstraAiPrompt] so the crash advisor, the on-device
     * assistant and any future Astra AI backend all reason from one spec (including the
     * launcher-side memory-management and OpenGL state-optimization sections, which are
     * exactly the two subsystems a mobile Minecraft crash usually implicates).
     */
    private val SYSTEM_PROMPT: String = AstraAiPrompt.crashAdvisorSystemPrompt()

    /**
     * The system prompt for one call: the shared Astra AI crash persona plus the instruction to
     * write the answer in the user's own language. The crash path runs from the game JVM's exit
     * hook, which has no Context, so [languageTag] is optional and falls back to the process
     * locale - still the user's language, just read a different way.
     */
    private fun systemPromptFor(languageTag: String?): String {
        val language = languageTag?.takeIf { it.isNotBlank() } ?: AstraAiLanguage.systemLanguage()
        return SYSTEM_PROMPT + "\n\n" +
            AstraAiPrompt.languageInstruction(language, AstraAiLanguage.displayName(language))
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
        if (AstraAiGemini.apiKey().isEmpty()) return null

        return AstraAiGemini.complete(
            systemPrompt = systemPromptFor(languageTag),
            userText = "Crash log tail:\n\n" + logText.takeLast(MAX_LOG_CHARS),
            // Cooler and shorter than a chat answer: this renders in a small dialog next to
            // the rule engine's own findings, not as a conversation.
            temperature = 0.2,
            maxOutputTokens = 400
        )
    }
}

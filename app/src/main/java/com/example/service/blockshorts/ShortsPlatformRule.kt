package com.example.service.blockshorts

/**
 * Result of evaluating an activity, view hierarchy, or URL against a short-form video rule.
 */
data class DetectionResult(
    val isShortForm: Boolean,
    val platformName: String = "",
    val reason: String = ""
)

/**
 * Extensible interface for detecting and isolating platform-specific short-form video content
 * (e.g. YouTube Shorts, Instagram Reels, TikTok, etc.) while leaving regular videos, search,
 * messaging, and profiles accessible.
 */
interface ShortsPlatformRule {
    val platformId: String
    val displayName: String

    /**
     * Checks if the package name matches this platform.
     */
    fun matchesPackage(packageName: String): Boolean

    /**
     * Evaluates whether a browser URL or intent URI represents short-form video content.
     */
    fun evaluateUrl(url: String): DetectionResult

    /**
     * Evaluates native UI state (class name, text, view IDs, content descriptions).
     */
    fun evaluateUiHierarchy(
        packageName: String,
        className: String?,
        contentDescriptions: List<String>,
        texts: List<String>,
        viewIds: List<String>
    ): DetectionResult
}

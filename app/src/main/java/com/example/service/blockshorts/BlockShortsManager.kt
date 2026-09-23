package com.example.service.blockshorts

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.model.BlockShortsEntity
import com.example.data.model.BlockedAppEntity
import com.example.ui.screens.BlockShortsActivity
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicLong

/**
 * Dedicated coordinator for Block Shorts rules, schedules, and active focus enforcement.
 */
object BlockShortsManager {
    private const val TAG = "BlockShortsManager"

    // Extensible platform rules list
    val rules: List<ShortsPlatformRule> = listOf(
        YouTubeShortsRule(),
        InstagramReelsRule(),
        FacebookReelsRule(),
        TikTokRule(),
        RedditRule()
    )

    @Volatile
    var currentConfig: BlockShortsEntity = BlockShortsEntity()

    @Volatile
    var blockedApps: List<BlockedAppEntity> = emptyList()

    @Volatile
    var isGlobalStrictMode: Boolean = false

    @Volatile
    var isFocusTimerRunning: Boolean = false

    private val lastBlockTimestamp = AtomicLong(0L)
    private const val BLOCK_DEBOUNCE_MS = 2000L

    /**
     * Checks if a specific app is currently active for blocking.
     */
    fun isAppActive(app: BlockedAppEntity): Boolean {
        if (!app.enabled) return false
        if (!app.scheduleEnabled) return true // "Always"

        return try {
            val now = LocalTime.now()
            val currentMinutes = now.hour * 60 + now.minute

            val startParts = app.startTime.split(":")
            val startMinutes = startParts[0].trim().toInt() * 60 + startParts.getOrElse(1) { "0" }.trim().toInt()

            val endParts = app.endTime.split(":")
            val endMinutes = endParts[0].trim().toInt() * 60 + endParts.getOrElse(1) { "0" }.trim().toInt()

            if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Determines whether Block Shorts protection is currently enforcing.
     * With standalone user control, protection is active if at least one app is enabled and active,
     * OR if Pomodoro focus timer is running, OR if schedule window is active.
     */
    fun isBlockShortsActive(): Boolean {
        // If standalone blockedApps are configured, check if any app is active
        if (blockedApps.isNotEmpty()) {
            return blockedApps.any { isAppActive(it) }
        }

        // Fallback to legacy config
        if (!currentConfig.isEnabled) return false

        // 1. Check schedule if configured
        if (currentConfig.isScheduleEnabled) {
            val now = LocalTime.now()
            val currentMinutes = now.hour * 60 + now.minute
            val startMinutes = currentConfig.scheduleStartHour * 60 + currentConfig.scheduleStartMinute
            val endMinutes = currentConfig.scheduleEndHour * 60 + currentConfig.scheduleEndMinute

            val inSchedule = if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
            if (inSchedule) return true
        }

        // 2. Active during running Focus sessions
        return isFocusTimerRunning
    }

    /**
     * Checks if a given platform is enabled for blocking.
     */
    fun isPlatformEnabled(platformId: String): Boolean {
        if (blockedApps.isNotEmpty()) {
            val matchedApp = when (platformId) {
                "YOUTUBE_SHORTS" -> blockedApps.find { it.name.contains("youtube", ignoreCase = true) || it.url.contains("youtube", ignoreCase = true) }
                "INSTAGRAM_REELS" -> blockedApps.find { it.name.contains("instagram", ignoreCase = true) || it.url.contains("instagram", ignoreCase = true) }
                "FACEBOOK_REELS" -> blockedApps.find { it.name.contains("facebook", ignoreCase = true) || it.url.contains("facebook", ignoreCase = true) }
                "TIKTOK" -> blockedApps.find { it.name.contains("tiktok", ignoreCase = true) || it.url.contains("tiktok", ignoreCase = true) }
                "REDDIT" -> blockedApps.find { it.name.contains("reddit", ignoreCase = true) || it.url.contains("reddit", ignoreCase = true) }
                else -> null
            }
            if (matchedApp != null) {
                return isAppActive(matchedApp)
            }
        }

        return when (platformId) {
            "YOUTUBE_SHORTS" -> currentConfig.blockYouTubeShorts
            "INSTAGRAM_REELS" -> currentConfig.blockInstagramReels
            else -> true
        }
    }

    /**
     * Evaluates a URL from a browser address bar or link.
     */
    fun evaluateUrl(url: String): DetectionResult {
        if (!isBlockShortsActive()) return DetectionResult(isShortForm = false)

        val lower = url.lowercase()

        // Check standard rules
        for (rule in rules) {
            if (isPlatformEnabled(rule.platformId)) {
                val result = rule.evaluateUrl(url)
                if (result.isShortForm) return result
            }
        }

        // Check custom added apps by URL match
        for (app in blockedApps) {
            if (isAppActive(app) && app.url.isNotBlank()) {
                val domain = app.url.lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
                if (lower.contains(domain)) {
                    // Check for short-form keywords if targeting short video
                    if (lower.contains("/reel") || lower.contains("/shorts") || lower.contains("/video/") || lower.contains("/clip")) {
                        return DetectionResult(
                            isShortForm = true,
                            platformName = app.name,
                            reason = "${app.name} short-form content detected"
                        )
                    }
                }
            }
        }

        return DetectionResult(isShortForm = false)
    }

    /**
     * Evaluates an accessibility event and node tree against platform rules.
     */
    fun evaluateEvent(
        event: AccessibilityEvent,
        rootNode: AccessibilityNodeInfo?
    ): DetectionResult {
        if (!isBlockShortsActive()) return DetectionResult(isShortForm = false)

        val pkgName = event.packageName?.toString() ?: return DetectionResult(isShortForm = false)
        val className = event.className?.toString()

        // 1. Check if package is a browser to extract URL
        if (isBrowserPackage(pkgName)) {
            val url = extractUrlFromNode(rootNode)
            if (!url.isNullOrBlank()) {
                val urlResult = evaluateUrl(url)
                if (urlResult.isShortForm) return urlResult
            }
            return DetectionResult(isShortForm = false)
        }

        // 2. Check native app rules
        val activeRules = rules.filter { it.matchesPackage(pkgName) && isPlatformEnabled(it.platformId) }
        if (activeRules.isEmpty()) return DetectionResult(isShortForm = false)

        val contentDescriptions = mutableListOf<String>()
        val texts = mutableListOf<String>()
        val viewIds = mutableListOf<String>()

        event.contentDescription?.toString()?.let { contentDescriptions.add(it) }
        event.text?.forEach { texts.add(it.toString()) }

        // Harvest shallow node details
        extractNodeMetadata(rootNode, contentDescriptions, texts, viewIds, maxDepth = 4, currentDepth = 0)

        for (rule in activeRules) {
            val result = rule.evaluateUiHierarchy(
                packageName = pkgName,
                className = className,
                contentDescriptions = contentDescriptions,
                texts = texts,
                viewIds = viewIds
            )
            if (result.isShortForm) {
                return result
            }
        }

        return DetectionResult(isShortForm = false)
    }

    /**
     * Displays the clean blocking screen with debounce prevention.
     */
    fun triggerBlock(context: Context, platformName: String, reason: String = "") {
        val now = System.currentTimeMillis()
        if (now - lastBlockTimestamp.get() < BLOCK_DEBOUNCE_MS) {
            return
        }
        lastBlockTimestamp.set(now)

        Log.i(TAG, "Shorts Blocked: $platformName triggered ($reason)")

        val intent = Intent(context, BlockShortsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockShortsActivity.EXTRA_PLATFORM_NAME, platformName)
            putExtra(BlockShortsActivity.EXTRA_STRICT_MODE, currentConfig.isStrictModeEnabled)
            putExtra(BlockShortsActivity.EXTRA_BLOCK_REASON, reason)
        }
        context.startActivity(intent)
    }

    private fun isBrowserPackage(pkg: String): Boolean {
        return pkg.contains("chrome") ||
               pkg.contains("browser") ||
               pkg.contains("firefox") ||
               pkg.contains("opera") ||
               pkg.contains("duckduckgo") ||
               pkg.contains("edge")
    }

    private fun extractUrlFromNode(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains("url") || viewId.contains("address") || viewId.contains("search_box")) {
            val text = node.text?.toString()
            if (!text.isNullOrBlank() && (text.contains(".") || text.contains("/"))) {
                return text
            }
        }
        for (i in 0 until minOf(node.childCount, 6)) {
            val child = node.getChild(i) ?: continue
            val found = extractUrlFromNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun extractNodeMetadata(
        node: AccessibilityNodeInfo?,
        contentDescriptions: MutableList<String>,
        texts: MutableList<String>,
        viewIds: MutableList<String>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (node == null || currentDepth >= maxDepth) return

        node.viewIdResourceName?.let { viewIds.add(it) }
        node.contentDescription?.toString()?.let { contentDescriptions.add(it) }
        node.text?.toString()?.let { texts.add(it) }

        val childCount = minOf(node.childCount, 8)
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            extractNodeMetadata(child, contentDescriptions, texts, viewIds, maxDepth, currentDepth + 1)
        }
    }
}

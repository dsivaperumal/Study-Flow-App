package com.example.service.blockshorts

/**
 * Platform rule targeting YouTube Shorts specifically.
 *
 * Blocks:
 * - Native YouTube Shorts viewer / reel activities and player containers
 * - Web URLs targeting `youtube.com/shorts/...` or `youtu.be/shorts/...`
 *
 * Explicitly allows:
 * - Regular YouTube videos (`watch?v=...`)
 * - Search query results (`results?search_query=...`)
 * - Channel / subscriptions / library tabs
 */
class YouTubeShortsRule : ShortsPlatformRule {
    override val platformId = "YOUTUBE_SHORTS"
    override val displayName = "YouTube Shorts"

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.google.android.youtube" ||
               packageName == "com.google.android.youtube.tv" ||
               packageName == "com.google.android.apps.youtube.mango"
    }

    override fun evaluateUrl(url: String): DetectionResult {
        val lower = url.lowercase()

        // Explicitly allow search and normal watch URLs
        if (lower.contains("youtube.com/watch") ||
            lower.contains("youtube.com/results") ||
            lower.contains("youtu.be/") && !lower.contains("/shorts/")
        ) {
            return DetectionResult(isShortForm = false)
        }

        // Specifically block shorts URLs
        if (lower.contains("youtube.com/shorts/") ||
            lower.contains("youtube.com/shorts") ||
            lower.contains("youtu.be/shorts/")
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "YouTube Shorts web URL detected"
            )
        }

        return DetectionResult(isShortForm = false)
    }

    override fun evaluateUiHierarchy(
        packageName: String,
        className: String?,
        contentDescriptions: List<String>,
        texts: List<String>,
        viewIds: List<String>
    ): DetectionResult {
        if (!matchesPackage(packageName)) return DetectionResult(isShortForm = false)

        val cName = className ?: ""

        // Native YouTube Shorts Viewer Activities & Reel Controllers
        if (cName.contains("ReelWatchActivity", ignoreCase = true) ||
            cName.contains("ReelPlayerActivity", ignoreCase = true) ||
            cName.contains("ShortsActivity", ignoreCase = true) ||
            cName.contains("reel.watch", ignoreCase = true)
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "YouTube Shorts player window active ($cName)"
            )
        }

        // YouTube Shorts view containers in modern app layouts
        val hasShortsViewId = viewIds.any { id ->
            id.contains("reel_recycler", ignoreCase = true) ||
            id.contains("reel_player", ignoreCase = true) ||
            id.contains("shorts_container", ignoreCase = true) ||
            id.contains("shorts_player", ignoreCase = true) ||
            id.contains("reel_viewer", ignoreCase = true)
        }
        if (hasShortsViewId) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "YouTube Shorts layout component detected"
            )
        }

        // Check if user is on the dedicated Shorts navigation tab with video player
        val isShortsTabActive = contentDescriptions.any { it.equals("Shorts", ignoreCase = true) } &&
                (cName.contains("Player", ignoreCase = true) ||
                 texts.any { it.contains("Remix this Short", ignoreCase = true) || it.contains("Shorts sound", ignoreCase = true) })

        if (isShortsTabActive) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "YouTube Shorts feed active"
            )
        }

        return DetectionResult(isShortForm = false)
    }
}

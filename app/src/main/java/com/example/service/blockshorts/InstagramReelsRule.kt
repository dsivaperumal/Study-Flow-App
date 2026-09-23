package com.example.service.blockshorts

/**
 * Platform rule targeting Instagram Reels specifically.
 *
 * Blocks:
 * - Native Instagram Clips / Reels viewers and video feed containers
 * - Web URLs targeting `instagram.com/reel/...` or `instagram.com/reels/...`
 *
 * Explicitly allows:
 * - Instagram user profiles (`instagram.com/username`)
 * - Instagram direct messaging (`DirectActivity`, `/direct/`)
 * - Normal image & carousel posts (`instagram.com/p/...`)
 */
class InstagramReelsRule : ShortsPlatformRule {
    override val platformId = "INSTAGRAM_REELS"
    override val displayName = "Instagram Reels"

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.instagram.android" ||
               packageName == "com.instagram.lite"
    }

    override fun evaluateUrl(url: String): DetectionResult {
        val lower = url.lowercase()

        // Explicitly allow direct messages, profiles, and standard photo posts
        if (lower.contains("instagram.com/direct") ||
            lower.contains("instagram.com/p/") && !lower.contains("/reel")
        ) {
            return DetectionResult(isShortForm = false)
        }

        // Specifically block reels URLs
        if (lower.contains("instagram.com/reel/") ||
            lower.contains("instagram.com/reels/") ||
            lower.contains("instagram.com/reels")
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Instagram Reels web URL detected"
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

        // Explicitly protect Instagram Direct messaging & Inbox
        if (cName.contains("Direct", ignoreCase = true) ||
            viewIds.any { it.contains("direct_inbox", ignoreCase = true) || it.contains("thread_view", ignoreCase = true) }
        ) {
            return DetectionResult(isShortForm = false)
        }

        // Native Instagram Reels / Clips Viewers
        if (cName.contains("ClipsViewerActivity", ignoreCase = true) ||
            cName.contains("ReelViewerActivity", ignoreCase = true) ||
            cName.contains("ClipsActivity", ignoreCase = true) ||
            cName.contains("ClipsViewer", ignoreCase = true)
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Instagram Reels viewer window active ($cName)"
            )
        }

        // Instagram Reels view containers
        val hasReelsViewId = viewIds.any { id ->
            id.contains("clips_viewer", ignoreCase = true) ||
            id.contains("clips_video_container", ignoreCase = true) ||
            id.contains("clips_viewer_view_pager", ignoreCase = true) ||
            id.contains("reel_viewer", ignoreCase = true)
        }
        if (hasReelsViewId) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Instagram Reels video player container detected"
            )
        }

        return DetectionResult(isShortForm = false)
    }
}

package com.example.service.blockshorts

/**
 * Platform rule targeting Reddit short-form video & distraction feeds.
 *
 * Blocks:
 * - Reddit full-screen video player feed
 * - Short video URLs where supported
 *
 * Allows:
 * - Regular text discussions, reading posts, messaging, and search.
 */
class RedditRule : ShortsPlatformRule {
    override val platformId = "REDDIT"
    override val displayName = "Reddit"

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.reddit.frontpage"
    }

    override fun evaluateUrl(url: String): DetectionResult {
        val lower = url.lowercase()
        if (lower.contains("v.redd.it") ||
            (lower.contains("reddit.com") && lower.contains("/video/"))
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Reddit short-form video URL detected"
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
        if (cName.contains("VideoPlayerActivity", ignoreCase = true) ||
            cName.contains("ShortsPlayer", ignoreCase = true)
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Reddit video feed active"
            )
        }

        return DetectionResult(isShortForm = false)
    }
}

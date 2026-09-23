package com.example.service.blockshorts

/**
 * Platform rule targeting Facebook Reels specifically.
 *
 * Blocks:
 * - Native Facebook Reels viewer / player activities
 * - Web URLs targeting `facebook.com/reel/...` or `fb.watch/...`
 *
 * Allows:
 * - Regular Facebook feed, groups, marketplace, profile pages, and messaging.
 */
class FacebookReelsRule : ShortsPlatformRule {
    override val platformId = "FACEBOOK_REELS"
    override val displayName = "Facebook Reels"

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.facebook.katana" ||
               packageName == "com.facebook.lite"
    }

    override fun evaluateUrl(url: String): DetectionResult {
        val lower = url.lowercase()

        // Explicitly allow general feed or groups or marketplace
        if (lower.contains("facebook.com/messages") ||
            lower.contains("facebook.com/groups") ||
            lower.contains("facebook.com/marketplace")
        ) {
            return DetectionResult(isShortForm = false)
        }

        if (lower.contains("facebook.com/reel/") ||
            lower.contains("facebook.com/reels/") ||
            lower.contains("fb.watch/") ||
            (lower.contains("facebook.com/watch") && lower.contains("reel"))
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Facebook Reels URL detected"
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
        if (cName.contains("ReelViewer", ignoreCase = true) ||
            cName.contains("FbShortsViewer", ignoreCase = true)
        ) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Facebook Reels viewer activity detected"
            )
        }

        val allContent = (contentDescriptions + texts).joinToString(" ").lowercase()
        if (allContent.contains("reels") && (allContent.contains("audio") || allContent.contains("remix"))) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "Facebook Reels feed detected"
            )
        }

        return DetectionResult(isShortForm = false)
    }
}

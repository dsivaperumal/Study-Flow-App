package com.example.service.blockshorts

/**
 * Platform rule targeting TikTok short-form videos.
 *
 * Blocks:
 * - TikTok native main video player / discovery feeds
 * - TikTok web video URLs (`tiktok.com/@.../video/...`)
 */
class TikTokRule : ShortsPlatformRule {
    override val platformId = "TIKTOK"
    override val displayName = "TikTok"

    override fun matchesPackage(packageName: String): Boolean {
        return packageName == "com.zhiliaoapp.musically" ||
               packageName == "com.ss.android.ugc.trill" ||
               packageName == "com.zhiliaoapp.musically.go"
    }

    override fun evaluateUrl(url: String): DetectionResult {
        val lower = url.lowercase()
        if (lower.contains("tiktok.com/@") && lower.contains("/video/")) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "TikTok video URL detected"
            )
        }
        if (lower.contains("tiktok.com/foryou") || lower.contains("tiktok.com/t/")) {
            return DetectionResult(
                isShortForm = true,
                platformName = displayName,
                reason = "TikTok feed URL detected"
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
        if (cName.contains("DetailActivity", ignoreCase = true) ||
            cName.contains("MainActivity", ignoreCase = true)
        ) {
            val allTexts = (contentDescriptions + texts).joinToString(" ").lowercase()
            if (allTexts.contains("for you") || allTexts.contains("share to") || allTexts.contains("original sound")) {
                return DetectionResult(
                    isShortForm = true,
                    platformName = displayName,
                    reason = "TikTok short-form feed detected"
                )
            }
        }

        return DetectionResult(isShortForm = false)
    }
}

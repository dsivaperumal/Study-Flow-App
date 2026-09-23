package com.example.utils

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

data class SupportedAppPreset(
    val appName: String,
    val packageName: String,
    val actionType: String,
    val searchUrlTemplate: String,
    val description: String,
    val iconKey: String
)

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
)

object FocusSearchManager {

    private const val TAG = "FocusSearchManager"
    private const val COOLDOWN_DURATION_MS = 60_000L // 60 seconds grace period after search

    // Package -> timestamp of last allowed search
    private val allowedCooldowns = ConcurrentHashMap<String, Long>()

    // Catalogue of popular applications with built-in search intent routing
    val PRESET_APPS = listOf(
        SupportedAppPreset(
            appName = "YouTube",
            packageName = "com.google.android.youtube",
            actionType = "YOUTUBE",
            searchUrlTemplate = "https://www.youtube.com/results?search_query=%s",
            description = "Search videos & lectures directly without homepage recommendations",
            iconKey = "youtube"
        ),
        SupportedAppPreset(
            appName = "Instagram",
            packageName = "com.instagram.android",
            actionType = "INSTAGRAM",
            searchUrlTemplate = "https://www.instagram.com/explore/tags/%s",
            description = "Search tags and profiles without infinite reels feed",
            iconKey = "instagram"
        ),
        SupportedAppPreset(
            appName = "Reddit",
            packageName = "com.reddit.frontpage",
            actionType = "REDDIT",
            searchUrlTemplate = "https://www.reddit.com/search/?q=%s",
            description = "Search discussions & subreddits without home feed distraction",
            iconKey = "reddit"
        ),
        SupportedAppPreset(
            appName = "Facebook",
            packageName = "com.facebook.katana",
            actionType = "FACEBOOK",
            searchUrlTemplate = "https://www.facebook.com/search/top/?q=%s",
            description = "Search people, groups & pages directly without news feed",
            iconKey = "facebook"
        ),
        SupportedAppPreset(
            appName = "X (Twitter)",
            packageName = "com.twitter.android",
            actionType = "TWITTER",
            searchUrlTemplate = "https://twitter.com/search?q=%s",
            description = "Search keywords & topics directly without algorithmic timeline",
            iconKey = "twitter"
        ),
        SupportedAppPreset(
            appName = "TikTok",
            packageName = "com.zhiliaoapp.musically",
            actionType = "TIKTOK",
            searchUrlTemplate = "https://www.tiktok.com/search?q=%s",
            description = "Search topics directly without 'For You' infinite loop",
            iconKey = "tiktok"
        ),
        SupportedAppPreset(
            appName = "LinkedIn",
            packageName = "com.linkedin.android",
            actionType = "LINKEDIN",
            searchUrlTemplate = "https://www.linkedin.com/search/results/all/?keywords=%s",
            description = "Search jobs, skills & posts without infinite feed",
            iconKey = "linkedin"
        ),
        SupportedAppPreset(
            appName = "Google Chrome",
            packageName = "com.android.chrome",
            actionType = "CHROME",
            searchUrlTemplate = "https://www.google.com/search?q=%s",
            description = "Direct web search without new tab feed articles",
            iconKey = "chrome"
        )
    )

    fun recordSearchCompleted(packageName: String) {
        allowedCooldowns[packageName] = System.currentTimeMillis()
    }

    fun isPackageInCooldown(packageName: String): Boolean {
        val lastTimestamp = allowedCooldowns[packageName] ?: return false
        val elapsed = System.currentTimeMillis() - lastTimestamp
        if (elapsed < COOLDOWN_DURATION_MS) {
            return true
        }
        allowedCooldowns.remove(packageName)
        return false
    }

    fun clearCooldown(packageName: String) {
        allowedCooldowns.remove(packageName)
    }

    /**
     * Executes the target search flow:
     * Dispatches deep link or native search intent into the target application
     * and records cooldown to prevent looping.
     */
    fun executeSearch(context: Context, packageName: String, query: String): Boolean {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return false

        recordSearchCompleted(packageName)

        val encodedQuery = try {
            URLEncoder.encode(trimmedQuery, "UTF-8")
        } catch (e: Exception) {
            trimmedQuery
        }

        try {
            when {
                packageName.contains("youtube") -> {
                    // Try native search intent first
                    val nativeSearch = Intent(Intent.ACTION_SEARCH).apply {
                        setPackage(packageName)
                        putExtra(SearchManager.QUERY, trimmedQuery)
                        putExtra("query", trimmedQuery)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (canResolveIntent(context, nativeSearch)) {
                        context.startActivity(nativeSearch)
                        return true
                    }
                    // Fallback to youtube url intent
                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(urlIntent)
                    return true
                }

                packageName.contains("instagram") -> {
                    val tagQuery = trimmedQuery.replace(" ", "")
                    val igUrl = "https://www.instagram.com/explore/tags/$tagQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(igUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                packageName.contains("reddit") -> {
                    val url = "https://www.reddit.com/search/?q=$encodedQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                packageName.contains("facebook") -> {
                    val url = "https://www.facebook.com/search/top/?q=$encodedQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                packageName.contains("twitter") || packageName.contains("x.android") -> {
                    val url = "https://twitter.com/search?q=$encodedQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                packageName.contains("tiktok") || packageName.contains("musically") -> {
                    val url = "https://www.tiktok.com/search?q=$encodedQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                packageName.contains("linkedin") -> {
                    val url = "https://www.linkedin.com/search/results/all/?keywords=$encodedQuery"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return true
                }

                else -> {
                    // Try preset search template if available
                    val preset = PRESET_APPS.find { it.packageName == packageName }
                    if (preset != null && preset.searchUrlTemplate.isNotBlank()) {
                        val targetUrl = preset.searchUrlTemplate.replace("%s", encodedQuery)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        return true
                    }

                    // Generic Web Search fallback
                    val webSearchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, trimmedQuery)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (canResolveIntent(context, webSearchIntent)) {
                        context.startActivity(webSearchIntent)
                        return true
                    }

                    // Fallback to launching the package directly
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        launchIntent.putExtra(SearchManager.QUERY, trimmedQuery)
                        context.startActivity(launchIntent)
                        return true
                    }

                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing focus search", e)
            return false
        }
    }

    private fun canResolveIntent(context: Context, intent: Intent): Boolean {
        return try {
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lists launchable applications installed on device.
     */
    fun getLaunchableApps(context: Context): List<InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveList = pm.queryIntentActivities(mainIntent, 0)
        val result = mutableListOf<InstalledAppItem>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveList) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue // Skip our own app
            if (seenPackages.add(pkg)) {
                val label = resolveInfo.loadLabel(pm).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                result.add(InstalledAppItem(label, pkg, isSystem))
            }
        }
        return result.sortedBy { it.appName.lowercase() }
    }
}

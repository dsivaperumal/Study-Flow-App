package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.service.blockshorts.BlockShortsManager
import com.example.ui.screens.FocusSearchInterruptActivity
import com.example.utils.FocusSearchManager
import java.util.concurrent.ConcurrentHashMap

class FocusSearchAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val pkgName = event.packageName?.toString() ?: return
        if (pkgName == packageName) return // Skip our own app

        // 1. Evaluate Block Shorts distraction detection
        if (BlockShortsManager.isBlockShortsActive()) {
            val rootNode = try { rootInActiveWindow } catch (e: Exception) { null }
            val detection = BlockShortsManager.evaluateEvent(event, rootNode)
            if (detection.isShortForm) {
                Log.i(TAG, "Block Shorts intercepted: ${detection.platformName} (${detection.reason})")
                BlockShortsManager.triggerBlock(this, detection.platformName, detection.reason)
                return
            }
        }

        // 2. Focus Search app launch interception (only on window state changed)
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && activeControlledPackages.contains(pkgName)) {
            // Check if user is currently in cooldown (already searched)
            if (FocusSearchManager.isPackageInCooldown(pkgName)) {
                return
            }

            val appName = packageNamesMap[pkgName] ?: pkgName

            Log.d(TAG, "Intercepting launch of $appName ($pkgName)")

            val interruptIntent = Intent(this, FocusSearchInterruptActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(FocusSearchInterruptActivity.EXTRA_PACKAGE_NAME, pkgName)
                putExtra(FocusSearchInterruptActivity.EXTRA_APP_NAME, appName)
            }
            startActivity(interruptIntent)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "FocusSearchAccessibilityService interrupted")
    }

    companion object {
        private const val TAG = "FocusSearchService"

        // In-memory set for instantaneous, zero-latency window interception
        val activeControlledPackages: MutableSet<String> = ConcurrentHashMap.newKeySet()
        val packageNamesMap: MutableMap<String, String> = ConcurrentHashMap()

        fun updateActivePackages(activeList: Map<String, String>) {
            activeControlledPackages.clear()
            activeControlledPackages.addAll(activeList.keys)
            packageNamesMap.clear()
            packageNamesMap.putAll(activeList)
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, FocusSearchAccessibilityService::class.java)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponent = ComponentName.unflattenFromString(componentNameString)
                if (enabledComponent != null && enabledComponent == expectedComponentName) {
                    return true
                }
            }
            return false
        }
    }
}

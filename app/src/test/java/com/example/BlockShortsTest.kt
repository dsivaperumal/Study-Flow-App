package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.BlockShortsEntity
import com.example.service.blockshorts.BlockShortsManager
import com.example.service.blockshorts.InstagramReelsRule
import com.example.service.blockshorts.YouTubeShortsRule
import com.example.ui.screens.BlockShortsScreen
import com.example.ui.theme.StudyFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BlockShortsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `block shorts entity defaults are valid`() {
        val entity = BlockShortsEntity()
        assertTrue(entity.isEnabled)
        assertTrue(entity.blockYouTubeShorts)
        assertTrue(entity.blockInstagramReels)
        assertFalse(entity.isScheduleEnabled)
        assertEquals(18, entity.scheduleStartHour)
        assertEquals(22, entity.scheduleEndHour)
        assertFalse(entity.isStrictModeEnabled)
    }

    @Test
    fun `youtube shorts rule blocks shorts url and allows regular video and search`() {
        val rule = YouTubeShortsRule()

        // Should block Shorts URLs
        val shortsResult1 = rule.evaluateUrl("https://www.youtube.com/shorts/dQw4w9WgXcQ")
        assertTrue(shortsResult1.isShortForm)
        assertEquals("YouTube Shorts", shortsResult1.platformName)

        val shortsResult2 = rule.evaluateUrl("https://youtu.be/shorts/abcdef12345")
        assertTrue(shortsResult2.isShortForm)

        // Must NOT block normal videos
        val normalVideoResult = rule.evaluateUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertFalse(normalVideoResult.isShortForm)

        // Must NOT block search query results
        val searchResult = rule.evaluateUrl("https://www.youtube.com/results?search_query=calculus+lecture")
        assertFalse(searchResult.isShortForm)

        // Must NOT block youtu.be normal share link
        val shareLinkResult = rule.evaluateUrl("https://youtu.be/dQw4w9WgXcQ")
        assertFalse(shareLinkResult.isShortForm)
    }

    @Test
    fun `youtube shorts rule detects native shorts viewer activity`() {
        val rule = YouTubeShortsRule()

        val shortsDetection = rule.evaluateUiHierarchy(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.extensions.reel.watch.activity.ReelWatchActivity",
            contentDescriptions = emptyList(),
            texts = emptyList(),
            viewIds = emptyList()
        )
        assertTrue(shortsDetection.isShortForm)

        val regularPlayerDetection = rule.evaluateUiHierarchy(
            packageName = "com.google.android.youtube",
            className = "com.google.android.apps.youtube.app.WatchWhileActivity",
            contentDescriptions = listOf("Search"),
            texts = listOf("Computer Science Tutorial"),
            viewIds = listOf("player_view")
        )
        assertFalse(regularPlayerDetection.isShortForm)
    }

    @Test
    fun `instagram reels rule blocks reel url and allows profile and messages`() {
        val rule = InstagramReelsRule()

        // Should block Reels URLs
        val reelResult1 = rule.evaluateUrl("https://www.instagram.com/reel/C3x9z_ABCDE/")
        assertTrue(reelResult1.isShortForm)
        assertEquals("Instagram Reels", reelResult1.platformName)

        val reelResult2 = rule.evaluateUrl("https://www.instagram.com/reels/")
        assertTrue(reelResult2.isShortForm)

        // Must NOT block direct messages
        val directResult = rule.evaluateUrl("https://www.instagram.com/direct/t/123456789")
        assertFalse(directResult.isShortForm)

        // Must NOT block profile page
        val profileResult = rule.evaluateUrl("https://www.instagram.com/harvard/")
        assertFalse(profileResult.isShortForm)
    }

    @Test
    fun `instagram reels rule detects native clips viewer and protects direct messages`() {
        val rule = InstagramReelsRule()

        // Clips/Reels activity detected
        val clipsDetection = rule.evaluateUiHierarchy(
            packageName = "com.instagram.android",
            className = "com.instagram.clips.viewer.ClipsViewerActivity",
            contentDescriptions = emptyList(),
            texts = emptyList(),
            viewIds = emptyList()
        )
        assertTrue(clipsDetection.isShortForm)

        // Direct message inbox protected
        val directDetection = rule.evaluateUiHierarchy(
            packageName = "com.instagram.android",
            className = "com.instagram.direct.DirectActivity",
            contentDescriptions = emptyList(),
            texts = emptyList(),
            viewIds = listOf("direct_inbox")
        )
        assertFalse(directDetection.isShortForm)
    }

    @Test
    fun `block shorts manager activation logic respects timer and schedules`() {
        BlockShortsManager.currentConfig = BlockShortsEntity(
            isEnabled = true,
            isScheduleEnabled = false
        )

        // Not active if timer is not running and no schedule
        BlockShortsManager.isFocusTimerRunning = false
        assertFalse(BlockShortsManager.isBlockShortsActive())

        // Active when timer is running
        BlockShortsManager.isFocusTimerRunning = true
        assertTrue(BlockShortsManager.isBlockShortsActive())

        // Master switch OFF disables protection even if timer running
        BlockShortsManager.currentConfig = BlockShortsManager.currentConfig.copy(isEnabled = false)
        assertFalse(BlockShortsManager.isBlockShortsActive())
    }

    @Test
    fun `block shorts screen renders required title subtitle and back to focus button`() {
        var backToFocusClicked = false
        var returnHomeClicked = false

        composeTestRule.setContent {
            StudyFlowTheme {
                BlockShortsScreen(
                    platformName = "YouTube Shorts",
                    isStrictMode = true,
                    reason = "YouTube Shorts feed active",
                    onBackToFocus = { backToFocusClicked = true },
                    onReturnHome = { returnHomeClicked = true }
                )
            }
        }

        // Verify required title: "Shorts Blocked"
        composeTestRule.onNodeWithTag("shorts_blocked_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shorts Blocked").assertIsDisplayed()

        // Verify required subtitle: "You’re currently in Focus Mode. Short-form content is temporarily blocked."
        composeTestRule.onNodeWithTag("shorts_blocked_message").assertIsDisplayed()
        composeTestRule.onNodeWithText("You’re currently in Focus Mode. Short-form content is temporarily blocked.").assertIsDisplayed()

        // Verify required button: "Back to Focus"
        composeTestRule.onNodeWithTag("back_to_focus_button").assertExists()
        composeTestRule.onNodeWithText("Back to Focus").assertExists()

        // Test interaction
        composeTestRule.onNodeWithTag("back_to_focus_button").performScrollTo().performClick()
        assertTrue(backToFocusClicked)

        composeTestRule.onNodeWithTag("return_home_button").performScrollTo().performClick()
        assertTrue(returnHomeClicked)
    }
}

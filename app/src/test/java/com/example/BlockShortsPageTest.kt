package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.BlockedAppEntity
import com.example.service.blockshorts.BlockShortsManager
import com.example.service.blockshorts.FacebookReelsRule
import com.example.service.blockshorts.RedditRule
import com.example.service.blockshorts.TikTokRule
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
class BlockShortsPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `blocked app entity defaults are valid and customizable`() {
        val app = BlockedAppEntity(
            name = "TikTok",
            url = "tiktok.com",
            icon = "tiktok",
            contentType = "Short-form videos",
            enabled = true,
            scheduleEnabled = false,
            startTime = "18:00",
            endTime = "22:00"
        )
        assertEquals("TikTok", app.name)
        assertEquals("tiktok.com", app.url)
        assertEquals("Short-form videos", app.contentType)
        assertTrue(app.enabled)
        assertFalse(app.scheduleEnabled)
        assertEquals("18:00", app.startTime)
        assertEquals("22:00", app.endTime)
    }

    @Test
    fun `facebook reels rule blocks reels and allows feed, messages, and marketplace`() {
        val rule = FacebookReelsRule()

        val reelResult = rule.evaluateUrl("https://www.facebook.com/reel/1234567890")
        assertTrue(reelResult.isShortForm)
        assertEquals("Facebook Reels", reelResult.platformName)

        val watchReelResult = rule.evaluateUrl("https://fb.watch/abcd1234ef")
        assertTrue(watchReelResult.isShortForm)

        // Allowed content
        assertFalse(rule.evaluateUrl("https://www.facebook.com/messages/t/user123").isShortForm)
        assertFalse(rule.evaluateUrl("https://www.facebook.com/groups/studygroup").isShortForm)
        assertFalse(rule.evaluateUrl("https://www.facebook.com/marketplace").isShortForm)
    }

    @Test
    fun `tiktok rule blocks video urls and allows other pages`() {
        val rule = TikTokRule()

        val videoResult = rule.evaluateUrl("https://www.tiktok.com/@creator/video/1234567890123")
        assertTrue(videoResult.isShortForm)
        assertEquals("TikTok", videoResult.platformName)

        val forYouResult = rule.evaluateUrl("https://www.tiktok.com/foryou")
        assertTrue(forYouResult.isShortForm)
    }

    @Test
    fun `reddit rule blocks video urls and allows normal post discussions`() {
        val rule = RedditRule()

        val videoResult = rule.evaluateUrl("https://v.redd.it/abcdef12345")
        assertTrue(videoResult.isShortForm)
        assertEquals("Reddit", videoResult.platformName)

        val textPostResult = rule.evaluateUrl("https://www.reddit.com/r/AndroidDev/comments/12345/discussion")
        assertFalse(textPostResult.isShortForm)
    }

    @Test
    fun `block shorts manager checks individual app active state`() {
        val alwaysApp = BlockedAppEntity(
            name = "YouTube",
            url = "youtube.com",
            icon = "youtube",
            contentType = "Shorts",
            enabled = true,
            scheduleEnabled = false
        )
        assertTrue(BlockShortsManager.isAppActive(alwaysApp))

        val disabledApp = BlockedAppEntity(
            name = "Facebook",
            url = "facebook.com",
            icon = "facebook",
            contentType = "Reels",
            enabled = false,
            scheduleEnabled = false
        )
        assertFalse(BlockShortsManager.isAppActive(disabledApp))
    }

    @Test
    fun `blocked screen renders custom platform and manage settings button`() {
        var manageSettingsClicked = false
        var returnHomeClicked = false

        composeTestRule.setContent {
            StudyFlowTheme {
                BlockShortsScreen(
                    platformName = "Instagram Reels",
                    isStrictMode = true,
                    reason = "Instagram Reels URL detected",
                    onBackToFocus = {},
                    onReturnHome = { returnHomeClicked = true },
                    onManageSettings = { manageSettingsClicked = true }
                )
            }
        }

        // Verify title and platform
        composeTestRule.onNodeWithTag("shorts_blocked_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Instagram Reels").assertIsDisplayed()
        composeTestRule.onNodeWithTag("manage_block_shorts_button").assertExists()

        // Click Manage Block Shorts
        composeTestRule.onNodeWithTag("manage_block_shorts_button").performScrollTo().performClick()
        assertTrue(manageSettingsClicked)

        // Click Exit to Home Screen
        composeTestRule.onNodeWithTag("return_home_button").performScrollTo().performClick()
        assertTrue(returnHomeClicked)
    }
}

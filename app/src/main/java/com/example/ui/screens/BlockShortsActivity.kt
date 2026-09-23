package com.example.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.theme.StudyFlowTheme

/**
 * Clean distraction-free intercept screen shown whenever a user attempts to access
 * short-form video content (YouTube Shorts, Instagram Reels) during an active focus period.
 */
class BlockShortsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val platformName = intent.getStringExtra(EXTRA_PLATFORM_NAME) ?: "Short-form Content"
        val isStrictMode = intent.getBooleanExtra(EXTRA_STRICT_MODE, false)
        val reason = intent.getStringExtra(EXTRA_BLOCK_REASON) ?: ""

        setContent {
            val darkTheme = isSystemInDarkTheme()
            StudyFlowTheme(darkTheme = darkTheme) {
                BlockShortsScreen(
                    platformName = platformName,
                    isStrictMode = isStrictMode,
                    reason = reason,
                    onBackToFocus = {
                        val focusIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(MainActivity.EXTRA_NAVIGATE_TO, "focus")
                        }
                        startActivity(focusIntent)
                        finish()
                    },
                    onReturnHome = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onManageSettings = {
                        val settingsIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(MainActivity.EXTRA_NAVIGATE_TO, "block_shorts")
                        }
                        startActivity(settingsIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PLATFORM_NAME = "extra_platform_name"
        const val EXTRA_STRICT_MODE = "extra_strict_mode"
        const val EXTRA_BLOCK_REASON = "extra_block_reason"
    }
}

@Composable
fun BlockShortsScreen(
    platformName: String,
    isStrictMode: Boolean,
    reason: String = "",
    onBackToFocus: () -> Unit,
    onReturnHome: () -> Unit,
    onManageSettings: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .testTag("block_shorts_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(IndigoPrimary.copy(alpha = 0.5f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category & Platform Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndigoPrimary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STUDY FLOW · FOCUS SHIELD",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Shield Icon with Visual Halo
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(RosePriority.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RosePriority.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = RosePriority,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Platform pill indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = platformName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Required Title: "Shorts Blocked"
                    Text(
                        text = stringResource(id = R.string.block_shorts_blocked_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("shorts_blocked_title")
                    )

                    // Required Subtitle: "You’re currently in Focus Mode. Short-form content is temporarily blocked."
                    Text(
                        text = stringResource(id = R.string.block_shorts_blocked_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp,
                        modifier = Modifier.testTag("shorts_blocked_message")
                    )

                    // Strict Mode Banner if active
                    if (isStrictMode) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberStreak.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AmberStreak,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Strict Mode Active: Short-form distractions are locked until your session ends.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AmberStreak
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Button: "Back to Focus"
                    Button(
                        onClick = onBackToFocus,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("back_to_focus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.block_shorts_back_to_focus),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Secondary Button: Return to Android Home
                    OutlinedButton(
                        onClick = onReturnHome,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("return_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exit to Home Screen")
                    }

                    // Tertiary Action: Manage Block Shorts Settings
                    androidx.compose.material3.TextButton(
                        onClick = onManageSettings,
                        modifier = Modifier.testTag("manage_block_shorts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage Block Shorts")
                    }
                }
            }
        }
    }
}

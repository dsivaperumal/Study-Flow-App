package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.service.FeedbackService
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RosePriority
import com.example.ui.viewmodel.StudyFlowViewModel
import kotlinx.coroutines.launch

@Composable
fun SupportScreen(
    viewModel: StudyFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var feedbackName by remember { mutableStateOf("") }
    var feedbackEmail by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var isSubmittingFeedback by remember { mutableStateOf(false) }
    var submissionSuccessMessage by remember { mutableStateOf<String?>(null) }
    var submissionErrorMessage by remember { mutableStateOf<String?>(null) }

    var showCoffeeDialog by remember { mutableStateOf(false) }
    var showFeedbackSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = IndigoPrimary.copy(alpha = 0.1f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(IndigoPrimary.copy(alpha = 0.4f))
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RosePriority.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart",
                        tint = RosePriority,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Support Study Flow",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Crafted for students, researchers, and professionals who refuse to lose their time to digital noise.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // QR Code Support Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Support Study Flow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "Scan the QR code with Google Pay, PhonePe, Paytm, or any UPI app to support project development.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // High-resolution scannable QR Code display container
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.qr_support_study_flow),
                        contentDescription = "Scan the QR code to support Study Flow",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .size(230.dp)
                            .padding(12.dp)
                            .testTag("support_qr_code_image")
                    )
                }

                Text(
                    text = "Scan the QR code to support us",
                    style = MaterialTheme.typography.labelLarge,
                    color = IndigoPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Ways to Support
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ways You Can Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberStreak, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Share Study Flow with classmates and colleagues.", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Submit feedback and suggest focus feature requests.", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Coffee, contentDescription = null, tint = AmberStreak, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Buy the development team a coffee.", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showCoffeeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberStreak),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("support_coffee_button")
                ) {
                    Icon(Icons.Default.Coffee, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buy Us a Coffee ☕", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // Feedback Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Have questions, suggestions, or bug reports? Send your thoughts directly to the project team.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = feedbackName,
                    onValueChange = {
                        feedbackName = it
                        submissionErrorMessage = null
                    },
                    label = { Text("Your Name (optional)") },
                    placeholder = { Text("Your Name") },
                    singleLine = true,
                    enabled = !isSubmittingFeedback,
                    modifier = Modifier.fillMaxWidth().testTag("feedback_name_input")
                )

                OutlinedTextField(
                    value = feedbackEmail,
                    onValueChange = {
                        feedbackEmail = it
                        submissionErrorMessage = null
                    },
                    label = { Text("Your Email (optional)") },
                    placeholder = { Text("student@university.edu") },
                    singleLine = true,
                    enabled = !isSubmittingFeedback,
                    modifier = Modifier.fillMaxWidth().testTag("feedback_email_input")
                )

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = {
                        feedbackText = it
                        submissionErrorMessage = null
                    },
                    label = { Text("Feedback / Message *") },
                    placeholder = { Text("Tell us your thoughts, bug reports, or feature ideas...") },
                    minLines = 4,
                    maxLines = 6,
                    enabled = !isSubmittingFeedback,
                    modifier = Modifier.fillMaxWidth().testTag("feedback_message_input")
                )

                // Inline Success Banner
                if (submissionSuccessMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = submissionSuccessMessage!!,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Inline Error Banner with Email Client Fallback
                if (submissionErrorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = submissionErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    FeedbackService.openEmailClient(context, feedbackName, feedbackEmail, feedbackText)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send via Email App")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (feedbackText.isBlank()) {
                            submissionErrorMessage = "Unable to send feedback right now. Please enter your feedback."
                            return@Button
                        }

                        coroutineScope.launch {
                            isSubmittingFeedback = true
                            submissionErrorMessage = null
                            submissionSuccessMessage = null

                            val result = FeedbackService.sendFeedback(
                                name = feedbackName,
                                email = feedbackEmail,
                                feedback = feedbackText
                            )

                            isSubmittingFeedback = false

                            if (result.isSuccess) {
                                submissionSuccessMessage = result.userMessage
                                showFeedbackSuccessDialog = true
                                feedbackName = ""
                                feedbackEmail = ""
                                feedbackText = ""
                                viewModel.showNotification(
                                    title = "Feedback Sent",
                                    message = "Thank you! Your feedback has been sent successfully.",
                                    type = "FEEDBACK"
                                )
                            } else {
                                submissionErrorMessage = result.userMessage
                            }
                        }
                    },
                    enabled = !isSubmittingFeedback && feedbackText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_feedback_button")
                ) {
                    if (isSubmittingFeedback) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sending...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Feedback")
                    }
                }
            }
        }

        // Mission & Story Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Why Study Flow Was Built",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Modern students are constantly interrupted by algorithmic recommendation feeds disguised as educational tools. When you go to YouTube for a calculus lecture or to Reddit for a programming query, home feeds siphon away your attention.\n\nStudy Flow was created to empower you with full command of your attention: Plan your day, lock in deep Pomodoro focus, search resources cleanly without addictive feeds, and automate boundaries around time-sink apps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        // Developer & Build Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Study Flow v1.0.0",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Architecture: Jetpack Compose · Room SQL · MVVM · Offline First",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Built with ❤️ for focused learners everywhere.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showCoffeeDialog) {
        AlertDialog(
            onDismissRequest = { showCoffeeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = AmberStreak,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text("Thank You for Your Support! ☕", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "Your enthusiasm and encouragement keep Study Flow independent, ad-free, and laser-focused on student productivity.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCoffeeDialog = false
                        viewModel.showNotification("Gratitude", "Thank you for supporting Study Flow!", "SUPPORT")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberStreak)
                ) {
                    Text("Cheers!", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        )
    }

    if (showFeedbackSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Thank You!", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your feedback has been sent successfully.")
            },
            confirmButton = {
                Button(
                    onClick = { showFeedbackSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }
}


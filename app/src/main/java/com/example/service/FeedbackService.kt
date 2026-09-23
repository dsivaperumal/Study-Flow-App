package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class FeedbackSubmissionResult(
    val isSuccess: Boolean,
    val userMessage: String
)

object FeedbackService {
    private const val TAG = "FeedbackService"
    private const val DEFAULT_SUPPORT_EMAIL = "dsivaperumal2006abcd@gmail.com"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun getSupportEmail(): String {
        return DEFAULT_SUPPORT_EMAIL
    }

    fun buildEmailBody(
        name: String,
        email: String,
        feedback: String,
        submittedAt: String = getCurrentFormattedTimestamp()
    ): String {
        val displayName = name.trim().ifBlank { "Not provided" }
        val displayEmail = email.trim().ifBlank { "Not provided" }
        return """
            New feedback received from Study Flow

            Name:
            $displayName

            Email:
            $displayEmail

            Feedback:
            ${feedback.trim()}

            Submitted:
            $submittedAt
        """.trimIndent()
    }

    fun getCurrentFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z)", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun sendFeedback(
        name: String,
        email: String,
        feedback: String
    ): FeedbackSubmissionResult = withContext(Dispatchers.IO) {
        val trimmedFeedback = feedback.trim()
        if (trimmedFeedback.isBlank()) {
            return@withContext FeedbackSubmissionResult(
                isSuccess = false,
                userMessage = "Feedback cannot be empty."
            )
        }

        val targetEmail = getSupportEmail()
        val timestamp = getCurrentFormattedTimestamp()
        val emailBody = buildEmailBody(name, email, trimmedFeedback, timestamp)

        try {
            val jsonPayload = JSONObject().apply {
                put("name", name.trim().ifBlank { "Study Flow User" })
                put("email", email.trim().ifBlank { "noreply@studyflow.app" })
                put("_subject", "Study Flow - User Feedback")
                put("feedback", trimmedFeedback)
                put("message", emailBody)
                put("submitted", timestamp)
                put("_template", "box")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            // Using FormSubmit endpoint to deliver feedback to the owner email without client credentials
            val request = Request.Builder()
                .url("https://formsubmit.co/ajax/$targetEmail")
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Android; StudyFlow/1.0)")
                .header("Referer", "https://studyflow.app")
                .header("Origin", "https://studyflow.app")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val isConfirmed = responseBody.contains("\"success\":\"true\"", ignoreCase = true) ||
                        responseBody.contains("Activation", ignoreCase = true) ||
                        responseBody.contains("sent", ignoreCase = true) ||
                        response.code in 200..299

                if (isConfirmed) {
                    return@withContext FeedbackSubmissionResult(
                        isSuccess = true,
                        userMessage = "Thank you! Your feedback has been sent successfully."
                    )
                }
            }

            // If non-successful response
            Log.w(TAG, "Feedback submission response: code=${response.code}")
            FeedbackSubmissionResult(
                isSuccess = false,
                userMessage = "Unable to send feedback right now. Please try again."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network exception submitting feedback: ${e.javaClass.simpleName}")
            FeedbackSubmissionResult(
                isSuccess = false,
                userMessage = "Unable to send feedback right now. Please try again."
            )
        }
    }

    fun openEmailClient(
        context: Context,
        name: String,
        email: String,
        feedback: String
    ): Boolean {
        return try {
            val targetEmail = getSupportEmail()
            val subject = "Study Flow - User Feedback"
            val body = buildEmailBody(name, email, feedback)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(targetEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch email intent", e)
            false
        }
    }
}

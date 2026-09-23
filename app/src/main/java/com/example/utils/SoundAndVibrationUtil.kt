package com.example.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object SoundAndVibrationUtil {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (e: Exception) {
            Log.e("SoundAndVibration", "Could not initialize ToneGenerator", e)
        }
    }

    fun playAlertSound(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
        } catch (e: Exception) {
            Log.e("SoundAndVibration", "Error playing tone", e)
        }
    }

    fun playCompletionChime(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
        } catch (e: Exception) {
            Log.e("SoundAndVibration", "Error playing chime", e)
        }
    }

    fun vibrate(context: Context, enabled: Boolean = true) {
        if (!enabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(400)
            }
        } catch (e: Exception) {
            Log.e("SoundAndVibration", "Error triggering vibration", e)
        }
    }
}

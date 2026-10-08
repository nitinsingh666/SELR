package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SirenManager(private val context: Context) {

    private var sirenJob: Job? = null
    private var phoneRingJob: Job? = null
    private var isPlayingSiren = false

    fun startEmergencySiren(scope: CoroutineScope) {
        if (isPlayingSiren) return
        isPlayingSiren = true

        sirenJob = scope.launch(Dispatchers.Default) {
            var toneGenerator: ToneGenerator? = null
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                while (isActive && isPlayingSiren) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
                    delay(420)
                    if (!isActive || !isPlayingSiren) break
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                    delay(380)
                }
            } catch (e: Exception) {
                Log.e("SirenManager", "ToneGenerator fallback to AudioTrack: ${e.message}")
                playSynthesizedSiren(this)
            } finally {
                try {
                    toneGenerator?.release()
                } catch (_: Exception) {}
            }
        }
    }

    private suspend fun playSynthesizedSiren(scope: CoroutineScope) {
        val sampleRate = 44100
        val numSamples = sampleRate / 2
        val generatedSnd = ByteArray(2 * numSamples)
        var audioTrack: AudioTrack? = null

        try {
            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSnd.size)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_ALARM,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack.play()

            var freq = 800.0
            var direction = 1
            while (scope.isActive && isPlayingSiren) {
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val sample = (sin(angle) * 32767).toInt().toShort()
                    val idx = 2 * i
                    generatedSnd[idx] = (sample.toInt() and 0x00ff).toByte()
                    generatedSnd[idx + 1] = ((sample.toInt() and 0xff00) shr 8).toByte()
                }
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                freq += direction * 80.0
                if (freq > 1400.0) direction = -1
                if (freq < 750.0) direction = 1
            }
        } catch (e: Exception) {
            Log.e("SirenManager", "Synthesized siren error: ${e.message}")
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
        }
    }

    fun stopSiren() {
        isPlayingSiren = false
        sirenJob?.cancel()
        sirenJob = null
    }

    fun startPhoneRingtone(scope: CoroutineScope) {
        stopPhoneRingtone()
        phoneRingJob = scope.launch(Dispatchers.Default) {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_RING, 90)
                while (isActive) {
                    toneGen.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1500)
                    delay(2500)
                }
            } catch (e: Exception) {
                Log.e("SirenManager", "Ringtone error: ${e.message}")
            } finally {
                try {
                    toneGen?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopPhoneRingtone() {
        phoneRingJob?.cancel()
        phoneRingJob = null
    }
}

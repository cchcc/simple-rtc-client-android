package cchcc.simplertc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import cchcc.simplertc.ext.audioManager
import java.util.*

class RTCAudioManager(private val context: Context) {
    enum class AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE
    }

    private val audioManager: AudioManager = context.audioManager
    private var savedAudioMode = AudioManager.MODE_INVALID
    private var savedIsSpeakerPhoneOn = false
    private var savedIsMicrophoneMute = false
    private val defaultAudioDevice = AudioDevice.SPEAKER_PHONE
    private var selectedAudioDevice: AudioDevice? = null
    private val audioDevices = HashSet<AudioDevice>()
    private var wiredHeadsetReceiver: BroadcastReceiver? = null
    private var isInitialized: Boolean = false

    fun init() {
        if (isInitialized)
            return
        savedAudioMode = audioManager.getMode()
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn()
        savedIsMicrophoneMute = audioManager.isMicrophoneMute()

        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION)

        setMicrophoneMute(false)
        updateAudioDeviceState(hasWiredHeadset())
        registerForWiredHeadsetIntentBroadcast()
        isInitialized = true
    }

    fun close() {
        if (!isInitialized)
            return

        unregisterForWiredHeadsetIntentBroadcast()
        setSpeakerphoneOn(savedIsSpeakerPhoneOn)
        setMicrophoneMute(savedIsMicrophoneMute)
        audioManager.mode = savedAudioMode
        audioManager.abandonAudioFocus(null)
    }

    fun setAudioDevice(device: AudioDevice) {
        when (device) {
            RTCAudioManager.AudioDevice.SPEAKER_PHONE -> {
                setSpeakerphoneOn(true)
                selectedAudioDevice = AudioDevice.SPEAKER_PHONE
            }
            RTCAudioManager.AudioDevice.EARPIECE -> {
                setSpeakerphoneOn(false)
                selectedAudioDevice = AudioDevice.EARPIECE
            }
            RTCAudioManager.AudioDevice.WIRED_HEADSET -> {
                setSpeakerphoneOn(false)
                selectedAudioDevice = AudioDevice.WIRED_HEADSET
            }
            else -> {
            }
        }
    }

    private fun registerForWiredHeadsetIntentBroadcast() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)

        wiredHeadsetReceiver = object : BroadcastReceiver() {
            private val STATE_UNPLUGGED = 0
            private val STATE_PLUGGED = 1
//            private val HAS_NO_MIC = 0
//            private val HAS_MIC = 1

            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra("state", STATE_UNPLUGGED)
//                val microphone = intent.getIntExtra("microphone", HAS_NO_MIC)
//                val name = intent.getStringExtra("name")

                val hasWiredHeadset = state == STATE_PLUGGED
                when (state) {
                    STATE_UNPLUGGED -> updateAudioDeviceState(hasWiredHeadset)
                    STATE_PLUGGED -> if (selectedAudioDevice != AudioDevice.WIRED_HEADSET) {
                        updateAudioDeviceState(hasWiredHeadset)
                    }
                }
            }
        }

        context.registerReceiver(wiredHeadsetReceiver, filter)
    }

    private fun unregisterForWiredHeadsetIntentBroadcast() {
        context.unregisterReceiver(wiredHeadsetReceiver)
        wiredHeadsetReceiver = null
    }

    private fun setSpeakerphoneOn(on: Boolean) {
        if (audioManager.isSpeakerphoneOn)
            return

        audioManager.setSpeakerphoneOn(on)
    }

    private fun setMicrophoneMute(on: Boolean) {
        val wasMuted = audioManager.isMicrophoneMute()
        if (wasMuted == on) {
            return
        }
        audioManager.setMicrophoneMute(on)
    }

    private fun hasEarpiece(): Boolean = context.getPackageManager()
            .hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    private fun hasWiredHeadset(): Boolean = audioManager.isWiredHeadsetOn()

    private fun updateAudioDeviceState(hasWiredHeadset: Boolean) {
        audioDevices.clear()
        if (hasWiredHeadset) {
            audioDevices.add(AudioDevice.WIRED_HEADSET)
        } else {
            audioDevices.add(AudioDevice.SPEAKER_PHONE)
            if (hasEarpiece()) {
                audioDevices.add(AudioDevice.EARPIECE)
            }
        }
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET)
        } else {
            setAudioDevice(defaultAudioDevice)
        }
    }
}

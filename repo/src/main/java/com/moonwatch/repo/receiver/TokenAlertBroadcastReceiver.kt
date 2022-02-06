package com.moonwatch.repo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager

class TokenAlertBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null) return
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    if (currentVolume == 0) {
      val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
      audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
    }
    MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .apply {
          setOnCompletionListener {
            if (currentVolume == 0) {
              audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
            }
          }
          start()
        }
  }
}

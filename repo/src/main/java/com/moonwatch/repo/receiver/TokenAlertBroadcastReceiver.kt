package com.moonwatch.repo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class TokenAlertBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    Timber.tag("ALARM").d("Token alert received.")
  }
}

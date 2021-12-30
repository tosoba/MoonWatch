package com.moonwatch.repo.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.moonwatch.core.di.MainActivityIntent
import com.moonwatch.core.model.ITokenAlertWithValue
import com.moonwatch.repo.R
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class AlertNotificationManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @MainActivityIntent private val intent: Intent
) {
  fun show(sellAlerts: List<ITokenAlertWithValue>, buyAlerts: List<ITokenAlertWithValue>) {
    val alertsCount = sellAlerts.size + buyAlerts.size
    val sellNotifications =
        sellAlerts
            .map { it.alert.id to buildNotificationFor(it, ::sellNotificationTitleFor) }
            .toMap()
    val buyNotifications =
        buyAlerts.map { it.alert.id to buildNotificationFor(it, ::buyNotificationTitleFor) }.toMap()
    NotificationManagerCompat.from(context).apply {
      sellNotifications.entries.forEach { (alertId, notification) ->
        notify(alertId.toInt(), notification)
      }
      buyNotifications.entries.forEach { (alertId, notification) ->
        notify(alertId.toInt(), notification)
      }

      if (alertsCount > 1) {
        val summaryNotification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("$alertsCount price targets were hit.")
                .setContentText(
                    "Sell targets hit: ${sellAlerts.size}. Buy targets hit: ${buyAlerts.size}.")
                .setStyle(
                    NotificationCompat.InboxStyle()
                        .setBigContentTitle("$alertsCount price targets were hit.")
                        .setSummaryText(
                            "Sell targets hit: ${sellAlerts.size}. Buy targets hit: ${buyAlerts.size}."))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .build()
        notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
      }
    }
  }

  private fun buildNotificationFor(
      tokenAlertWithValue: ITokenAlertWithValue,
      notificationTitle: (ITokenAlertWithValue) -> String,
      notificationContent: (ITokenAlertWithValue) -> String = ::notificationContentFor,
  ): Notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
          .setContentTitle(notificationTitle(tokenAlertWithValue))
          .setContentText(notificationContent(tokenAlertWithValue))
          // TODO: show price from when alert was created + price increase/decrease
          // percentage (in sub text)
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
          .setAutoCancel(true)
          .setGroup(GROUP_KEY)
          .build()

  private fun sellNotificationTitleFor(tokenAlertWithValue: ITokenAlertWithValue): String =
      "${tokenAlertWithValue.token.name} sell price target was hit!"

  private fun buyNotificationTitleFor(tokenAlertWithValue: ITokenAlertWithValue): String =
      "${tokenAlertWithValue.token.name} buy price target was hit!"

  private fun notificationContentFor(tokenAlertWithValue: ITokenAlertWithValue): String {
    val currentPrice = tokenAlertWithValue.value.usd.stripTrailingZeros()
    return "Current price: ${currentPrice.toPlainString()}"
  }

  companion object {
    private const val CHANNEL_ID = "ALERT_NOTIFICATIONS"
    private const val GROUP_KEY = "ALERT_NOTIFICATIONS"
    private const val SUMMARY_NOTIFICATION_ID = 0

    fun createChannel(context: Context) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
      context
          .getSystemService(NotificationManager::class.java)
          .createNotificationChannel(
              NotificationChannel(
                      CHANNEL_ID,
                      context.getString(R.string.alert_channel_name),
                      NotificationManager.IMPORTANCE_DEFAULT)
                  .apply { description = context.getString(R.string.alert_channel_description) },
          )
    }
  }
}

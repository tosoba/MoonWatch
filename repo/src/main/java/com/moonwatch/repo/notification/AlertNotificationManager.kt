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
import com.moonwatch.core.model.ITokenAlertWithCurrentValue
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
  fun show(
      sellAlerts: List<ITokenAlertWithCurrentValue>,
      buyAlerts: List<ITokenAlertWithCurrentValue>
  ) {
    val alertsCount = sellAlerts.size + buyAlerts.size
    val sellNotifications =
        sellAlerts.associate { it.alert.id to buildNotificationFor(it, ::sellNotificationTitleFor) }
    val buyNotifications =
        buyAlerts.associate { it.alert.id to buildNotificationFor(it, ::buyNotificationTitleFor) }
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
                    "${sellAlerts.size} sell targets hit. ${buyAlerts.size} buy targets hit.")
                .setStyle(
                    NotificationCompat.InboxStyle()
                        .setBigContentTitle("$alertsCount price targets were hit.")
                        .setSummaryText(
                            "${sellAlerts.size} sell targets hit. ${buyAlerts.size} buy targets hit."))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build()
        notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
      }
    }
  }

  private fun buildNotificationFor(
      tokenAlertWithValue: ITokenAlertWithCurrentValue,
      notificationTitle: (ITokenAlertWithCurrentValue) -> String,
      notificationContent: (ITokenAlertWithCurrentValue) -> String = ::notificationContentFor,
  ): Notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
          .setContentTitle(notificationTitle(tokenAlertWithValue))
          .setContentText(notificationContent(tokenAlertWithValue))
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setContentIntent(
              PendingIntent.getActivity(
                  context,
                  1,
                  intent.apply { putExtra(ALERT_ID_EXTRA_KEY, tokenAlertWithValue.alert.id) },
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                  } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                  },
              ),
          )
          .setSmallIcon(android.R.drawable.ic_dialog_alert)
          .setAutoCancel(true)
          .setPriority(NotificationCompat.PRIORITY_MAX)
          .setGroup(GROUP_KEY)
          .build()

  private fun sellNotificationTitleFor(tokenAlertWithValue: ITokenAlertWithCurrentValue): String =
      "${tokenAlertWithValue.token.name} sell price target was hit!"

  private fun buyNotificationTitleFor(tokenAlertWithValue: ITokenAlertWithCurrentValue): String =
      "${tokenAlertWithValue.token.name} buy price target was hit!"

  private fun notificationContentFor(tokenAlertWithValue: ITokenAlertWithCurrentValue): String {
    val currentPrice = tokenAlertWithValue.currentValue.usd.stripTrailingZeros()
    return "Current price: ${currentPrice.toPlainString()}"
  }

  companion object {
    private const val CHANNEL_ID = "ALERT_NOTIFICATIONS"
    private const val GROUP_KEY = "ALERT_NOTIFICATIONS"
    private const val SUMMARY_NOTIFICATION_ID = 0
    const val ALERT_ID_EXTRA_KEY = "ALERT_ID_EXTRA_KEY"

    fun createChannel(context: Context) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
      context
          .getSystemService(NotificationManager::class.java)
          .createNotificationChannel(
              NotificationChannel(
                      CHANNEL_ID,
                      context.getString(R.string.alert_channel_name),
                      NotificationManager.IMPORTANCE_HIGH,
                  )
                  .apply {
                    description = context.getString(R.string.alert_channel_description)
                    setBypassDnd(true)
                  },
          )
    }
  }
}

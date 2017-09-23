package com.ubiqsmart.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.ubiqsmart.utils.WalletStorage;

import java.util.*;

public class NotificationLauncher {

  private static NotificationLauncher instance;

  private final Context context;
  private final SharedPreferences preferences;
  private final WalletStorage walletStorage;
  private final AlarmManager alarmManager;

  private PendingIntent pendingIntent;

  public static NotificationLauncher getInstance(final Context c, final WalletStorage walletStorage) {
    if (instance == null) {
      instance = new NotificationLauncher(c, walletStorage);
    }
    return instance;
  }

  private NotificationLauncher(final Context c, final WalletStorage walletStorage) {
    this.context = c.getApplicationContext();
    this.preferences = PreferenceManager.getDefaultSharedPreferences(c);
    this.walletStorage = walletStorage;
    this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  public void start() {
    final boolean notificationsNewMessage = preferences.getBoolean("notifications_new_message", true);

    if (notificationsNewMessage && walletStorage.get().size() >= 1) {
      final Intent i = new Intent(context, NotificationService.class);
      pendingIntent = PendingIntent.getService(context, 23, i, 0);

      final String syncFrequency = preferences.getString("sync_frequency", "4");
      final int syncInt = Integer.parseInt(syncFrequency);

      final Date now = new Date();
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTime(), AlarmManager.INTERVAL_HOUR * syncInt, pendingIntent);
    }
  }

  public void stop() {
    if (alarmManager == null || pendingIntent == null) {
      return;
    }
    alarmManager.cancel(pendingIntent);
  }

}

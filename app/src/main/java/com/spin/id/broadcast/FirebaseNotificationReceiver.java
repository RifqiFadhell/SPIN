package com.spin.id.broadcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.amplitude.api.Amplitude;
import com.appsflyer.AppsFlyerLib;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.spin.id.R;
import com.spin.id.ui.home.HomeActivity;

public class FirebaseNotificationReceiver extends FirebaseMessagingService {

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);
    AppsFlyerLib.getInstance().updateServerUninstallToken(getApplicationContext(), s);
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    //handle when receive notification via data event
    if (remoteMessage.getData().containsKey("af-uinstall-tracking")) {
      return;
    }

    if (remoteMessage.getData().size() > 0) {
      showNotification(remoteMessage.getData().get("title"),
          remoteMessage.getData().get("message"), remoteMessage.getData().get("post_id"),
          remoteMessage.getData().get("order_id"));
    }

    //handle when receive notification
    if (remoteMessage.getNotification() != null) {
      showNotification(remoteMessage.getNotification().getTitle(),
          remoteMessage.getNotification().getBody(),
          remoteMessage.getData().get("post_id"),
          remoteMessage.getData().get("order_id"));
    }

  }

  private RemoteViews getCustomDesign(String title, String message) {
    RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
        R.layout.notification);
    remoteViews.setTextViewText(R.id.title, title);
    remoteViews.setTextViewText(R.id.message, message);
    return remoteViews;
  }

  public void showNotification(String title, String message, String postId, String orderId) {
    Intent intent = new Intent(this, HomeActivity.class);
    String channel_id = "web_app_channel";
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("post_id", postId);
    intent.putExtra("order_id", orderId);
    PendingIntent pendingIntent = PendingIntent
        .getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
        channel_id)
        .setSmallIcon(R.drawable.ic_logo_notif)
        .setSound(uri)
        .setAutoCancel(true)
        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
        .setOnlyAlertOnce(true)
        .setContentIntent(pendingIntent);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      builder = builder.setContent(getCustomDesign(title, message));
    } else {
      builder = builder.setContentTitle(title)
          .setContentText(message)
          .setSmallIcon(R.drawable.ic_logo_notif);
    }

    Amplitude.getInstance().logEvent("Notification Received");

    NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(channel_id, "web_app",
          NotificationManager.IMPORTANCE_HIGH);
      notificationChannel.setSound(uri, null);
      notificationManager.createNotificationChannel(notificationChannel);
    }

    notificationManager.notify(0, builder.build());
  }
}

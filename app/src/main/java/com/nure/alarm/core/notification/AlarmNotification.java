package com.nure.alarm.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.R;

import org.json.JSONArray;

public class AlarmNotification {

    public static void sendNotification(Context context, String message, JSONArray lessons) {
        NotificationChannel channel = new NotificationChannel("alarm_channel", "Alarm Notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("NureAlarm")
                .setContentText(message);
        if (lessons != null) {
            Intent intent = new Intent(context, AlarmNotificationReceiver.class);
            intent.setAction("dismiss");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_dismiss, "Dismiss", pendingIntent);
        }
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notification);
    }
}

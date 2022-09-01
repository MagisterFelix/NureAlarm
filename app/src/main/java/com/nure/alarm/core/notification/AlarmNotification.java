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
import com.nure.alarm.views.MainActivity;

public class AlarmNotification {

    public static void sendNotification(Context context, String message, boolean haveLessons) {
        NotificationChannel channel = new NotificationChannel("alarm_channel", "Alarm Notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(resultPendingIntent)
                .setContentTitle("NureAlarm")
                .setAutoCancel(true);
        if (haveLessons) {
            PendingIntent changePendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class).setAction("change"),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_lesson, "Change lesson", changePendingIntent);

            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class).setAction("dismiss"),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent);
        }
        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notification);
    }
}

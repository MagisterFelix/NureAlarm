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

    public static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL = "alarm_channel";
    private static final String NOTIFICATION_NAME = "Alarm notification";

    public static void sendNotification(Context context, String message, boolean haveLessons) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setGroup(NOTIFICATION_NAME)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.alarm_clock))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        if (haveLessons) {
            PendingIntent changePendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class).setAction("change"),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_lesson, context.getString(R.string.change_lesson), changePendingIntent);

            PendingIntent removePendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class).setAction("remove"),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_remove, context.getString(R.string.remove), removePendingIntent);
        }
        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }
}

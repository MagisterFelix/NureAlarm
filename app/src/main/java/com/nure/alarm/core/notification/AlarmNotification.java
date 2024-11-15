package com.nure.alarm.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nure.alarm.R;
import com.nure.alarm.core.models.LessonsType;
import com.nure.alarm.core.utils.ActivityUtils;
import com.nure.alarm.views.AlarmClockActivity;

public class AlarmNotification {

    public static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL = "alarm_channel";
    private static final String NOTIFICATION_NAME = "Alarm notification";

    public static void sendNotification(Context context, String message, @Nullable Boolean haveLessons, @Nullable Integer lessonsType) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, AlarmClockActivity.class).putExtra(ActivityUtils.CHECK_LAST_ACTIVITY, false),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setGroup(NOTIFICATION_NAME)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.alarm_clock))
                .setContentText(message)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        if (haveLessons != null) {
            if (haveLessons) {
                PendingIntent changePendingIntent = PendingIntent.getBroadcast(context, 0,
                        new Intent(context, AlarmNotificationReceiver.class)
                                .setAction(AlarmNotificationReceiver.ACTION_CHANGE),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_lesson, context.getString(R.string.change_lesson), changePendingIntent);

                PendingIntent removePendingIntent = PendingIntent.getBroadcast(context, 0,
                        new Intent(context, AlarmNotificationReceiver.class)
                                .setAction(AlarmNotificationReceiver.ACTION_REMOVE),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_remove, context.getString(R.string.remove), removePendingIntent);
            } else {
                builder.setTimeoutAfter(5000);
            }
        } else {
            PendingIntent retryPendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class)
                            .putExtra(LessonsType.class.getSimpleName(), lessonsType)
                            .setAction(AlarmNotificationReceiver.ACTION_RETRY),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_retry, context.getString(R.string.retry), retryPendingIntent);
        }
        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}

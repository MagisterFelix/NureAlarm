package com.nure.alarm.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nure.alarm.R;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.notification.AlarmNotificationReceiver;
import com.nure.alarm.views.AlarmActivity;
import com.nure.alarm.views.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private Information information;

    private static final int NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_CHANNEL = "alarm_clock_channel";
    private static final String NOTIFICATION_NAME = "Alarm clock";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setUpMediaPlayer() {
        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), alarmTone);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void setUpNotification() {
        Context context = ContextManager.getLocaleContext(getApplicationContext());

        try {
            JSONObject alarm = information.getAlarm();

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, AlarmActivity.class),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmNotificationReceiver.class).setAction("dismiss"),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                    .setGroup(NOTIFICATION_NAME)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(context.getString(R.string.alarm_clock))
                    .setContentText(alarm.getString("time"))
                    .setContentIntent(resultPendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .addAction(R.drawable.ic_dismiss, context.getString(R.string.dismiss), dismissPendingIntent);

            Notification notification = builder.build();
            startForeground(NOTIFICATION_ID, notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        information = FileManager.readInfo(getApplicationContext());
        setUpMediaPlayer();
        setUpVibration();
        setUpNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{ 0, 500, 500, 500, 500 }, 0));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        vibrator.cancel();
        super.onDestroy();
    }
}

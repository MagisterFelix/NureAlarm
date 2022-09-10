package com.nure.alarm.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nure.alarm.R;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.views.dialogs.ConfirmationDialog;

import org.json.JSONException;

import java.util.Locale;

public class AlarmClockActivity extends AppCompatActivity {

    public final static String UPDATE_ACTIVITY_ACTION = "update";

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock);

        registerReceiver();

        Information information = FileManager.readInfo(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.alarm_clock);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.alarm_settings) {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        if (information.getAlarm().length() != 0) {
            RelativeLayout relativeLayoutAlarmInfo = findViewById(R.id.alarm_info);
            relativeLayoutAlarmInfo.setVisibility(View.VISIBLE);
            try {
                TextView lesson = findViewById(R.id.lesson_name);
                lesson.setText(information.getAlarm().getString("lesson"));

                TextView time = findViewById(R.id.alarm_time);
                time.setText(information.getAlarm().getString("time"));

                Button change = findViewById(R.id.change);
                change.setOnClickListener(view -> {
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.setAction("change");
                    startActivity(mainActivity);
                    finish();
                    overridePendingTransition(0, 0);
                });

                Button remove = findViewById(R.id.remove);
                remove.setOnClickListener(view -> {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog();
                    confirmationDialog.show(getSupportFragmentManager(), "ConfirmationDialog");
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            RelativeLayout relativeLayoutNoAlarmInfo = findViewById(R.id.alarm_no_info);
            relativeLayoutNoAlarmInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        Locale locale = new Locale(new SessionManager(base).fetchLocale());
        Locale.setDefault(locale);
        Configuration configuration = base.getResources().getConfiguration();
        configuration.setLocale(locale);
        super.attachBaseContext(base.createConfigurationContext(configuration));
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent alarmClockActivity = new Intent(context, AlarmClockActivity.class);
                startActivity(alarmClockActivity);
                finish();
                overridePendingTransition(0, 0);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(UPDATE_ACTIVITY_ACTION));
    }

    public static void updateActivity(Context context) {
        Intent updateIntent = new Intent(UPDATE_ACTIVITY_ACTION);
        context.sendBroadcast(updateIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}
package com.nure.alarm.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.activity.Helper;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.network.NetworkInfo;
import com.nure.alarm.core.notification.AlarmNotificationReceiver;
import com.nure.alarm.core.work.AlarmWorkerReceiver;
import com.nure.alarm.views.dialogs.ConfirmationDialog;
import com.nure.alarm.views.dialogs.NotSpecifiedInformationDialog;
import com.nure.alarm.views.dialogs.UnavailableNetworkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class AlarmClockActivity extends AppCompatActivity {

    private Information information;
    private SessionManager sessionManager;
    private BroadcastReceiver broadcastReceiver;

    public static final String UPDATE_ACTIVITY_ACTION = "update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock);

        registerReceiver();

        information = FileManager.readInfo(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.alarm_clock);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.alarm_settings) {
                startMainActivity(false);
                return true;
            }
            return false;
        });

        if (information.getAlarm().length() != 0) {
            RelativeLayout relativeLayoutAlarmInfo = findViewById(R.id.alarm_info);
            relativeLayoutAlarmInfo.setVisibility(View.VISIBLE);
            try {
                TextView lesson = findViewById(R.id.lesson_name);
                lesson.setText(information.getAlarm().getString("name"));

                TextView time = findViewById(R.id.alarm_time);
                time.setText(information.getAlarm().getString("time"));

                Button change = findViewById(R.id.change);
                change.setOnClickListener(view -> changeLesson());

                Button remove = findViewById(R.id.remove);
                remove.setOnClickListener(view -> {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog();
                    confirmationDialog.show(getSupportFragmentManager(), ConfirmationDialog.class.getSimpleName());
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            RelativeLayout relativeLayoutNoAlarmInfo = findViewById(R.id.alarm_no_info);
            relativeLayoutNoAlarmInfo.setVisibility(View.VISIBLE);
        }

        if (getIntent().getAction() != null && getIntent().getAction().equals(AlarmNotificationReceiver.ACTION_CHANGE)) {
            changeLesson();
        } else {
            if (getIntent().getExtras() == null && !getClass().getSimpleName().equals(sessionManager.fetchLastActivity())) {
                startMainActivity(true);
            }
        }
    }

    @Override
    protected void onResume() {
        sessionManager.saveLastActivity(getClass().getSimpleName());
        super.onResume();
    }

    private void startMainActivity(boolean checkLastActivity) {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra(Helper.CHECK_LAST_ACTIVITY, checkLastActivity);
        Helper.startActivity(AlarmClockActivity.this, mainActivity);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ContextManager.getLocaleContext(base));
    }

    @SuppressLint("WrongConstant")
    private void collapsePanel(Context context) {
        try {
            String className = "android.app.StatusBarManager";
            String method = "collapsePanels";
            String service = "statusbar";
            Class.forName(className).getMethod(method).invoke(context.getSystemService(service));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void changeLesson() {
        collapsePanel(getApplicationContext());

        TextView lessonTextView = findViewById(R.id.lesson);
        lessonTextView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(AlarmClockActivity.this);
            dialog.setContentView(R.layout.lesson_spinner);

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            Context context = ContextManager.getLocaleContext(getApplicationContext());

            ListView listView = dialog.findViewById(R.id.lesson_list_view);

            JSONArray lessons = FileManager.readInfo(getApplicationContext()).getLessons();
            ArrayList<String> formatted_lessons = new ArrayList<>();
            for (int i = 0; i < lessons.length(); ++i) {
                try {
                    JSONObject jsonObject = lessons.getJSONObject(i);
                    String lesson = context.getString(R.string.lesson_number) + jsonObject.getString("number") + " - " + jsonObject.getString("name");
                    formatted_lessons.add(lesson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, formatted_lessons);

            listView.setAdapter(groupAdapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                Alarm.cancelAlarm(getApplicationContext());
                try {
                    Alarm.startAlarm(context, lessons.getJSONObject(position), information.getDelay());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            });
        });
        lessonTextView.performClick();
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent alarmClockActivity = new Intent(context, AlarmClockActivity.class);
                Helper.startActivity(AlarmClockActivity.this, alarmClockActivity);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(UPDATE_ACTIVITY_ACTION));
    }

    public static void updateActivity(Context context) {
        Intent updateIntent = new Intent(UPDATE_ACTIVITY_ACTION);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_clock_menu, menu);

        Information information = FileManager.readInfo(getApplicationContext());

        if (information.getAlarm().length() == 0) {
            MenuItem addAlarm = menu.findItem(R.id.add_alarm);
            addAlarm.setVisible(true);
            addAlarm.setOnMenuItemClickListener(menuItem -> {
                if (information.getSettingHour() == -1 && information.getSettingMinute() == -1 || information.getGroup().length() == 0) {
                    NotSpecifiedInformationDialog notSpecifiedInformationDialog = new NotSpecifiedInformationDialog();
                    notSpecifiedInformationDialog.show(getSupportFragmentManager(), NotSpecifiedInformationDialog.class.getSimpleName());
                    return false;
                } else {
                    if (NetworkInfo.isNetworkAvailable(getApplication())) {
                        AlarmWorkerReceiver.startWork(getApplicationContext());
                        ProgressBar progressBar = new ProgressBar(getApplicationContext());
                        progressBar.setScaleX(0.6f);
                        progressBar.setScaleY(0.6f);
                        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));
                        menuItem.setActionView(progressBar);
                        return true;
                    } else {
                        UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                        unavailableNetworkDialog.show(getSupportFragmentManager(), UnavailableNetworkDialog.class.getSimpleName());
                        return false;
                    }
                }
            });
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}
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
import android.os.CountDownTimer;
import android.text.Spanned;
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
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.models.Time;
import com.nure.alarm.core.network.NetworkInfo;
import com.nure.alarm.core.notification.AlarmNotificationReceiver;
import com.nure.alarm.core.utils.ActivityUtils;
import com.nure.alarm.core.work.AlarmWorkerReceiver;
import com.nure.alarm.views.dialogs.DeletionConfirmationDialog;
import com.nure.alarm.views.dialogs.NotSpecifiedInformationDialog;
import com.nure.alarm.views.dialogs.UnavailableNetworkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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

                TextView alarm_time = findViewById(R.id.alarm_time);
                alarm_time.setText(information.getAlarm().getString("time"));

                TextView time_left = findViewById(R.id.alarm_time_left);

                Calendar now = Calendar.getInstance();
                Time time = new Time(information.getAlarm().getString("time"));

                Calendar date = Calendar.getInstance();
                date.set(Calendar.HOUR_OF_DAY, time.getHour());
                date.set(Calendar.MINUTE, time.getMinute());
                date.set(Calendar.SECOND, 0);

                if (date.before(now)) {
                    date.add(Calendar.DATE, 1);
                }

                new CountDownTimer(date.getTimeInMillis() - now.getTimeInMillis(),1000) {

                    @Override
                    public void onTick(long millis) {
                        int hours   = (int) ((millis / (1000 * 60 * 60)) % 24);
                        int minutes = (int) ((millis / (1000 * 60)) % 60);

                        String text = String.format(
                                Locale.getDefault(),
                                ContextManager.getLocaleContext(getApplicationContext()).getString(R.string.time_left), hours, minutes
                        );
                        time_left.setText(text);
                    }

                    @Override
                    public void onFinish() {
                        time_left.setVisibility(View.GONE);
                    }

                }.start();

                Button change = findViewById(R.id.change);
                change.setOnClickListener(view -> changeLesson());

                Button remove = findViewById(R.id.remove);
                remove.setOnClickListener(view -> {
                    DeletionConfirmationDialog deletionConfirmationDialog = new DeletionConfirmationDialog();
                    deletionConfirmationDialog.show(getSupportFragmentManager(), DeletionConfirmationDialog.class.getSimpleName());
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ContextManager.getLocaleContext(base));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_clock_menu, menu);

        information = FileManager.readInfo(getApplicationContext());

        if (information.getAlarm().length() == 0) {
            MenuItem addAlarm = menu.findItem(R.id.add_alarm);
            addAlarm.setVisible(true);
            addAlarm.setOnMenuItemClickListener(menuItem -> {
                if (information.getGroup().length() == 0) {
                    NotSpecifiedInformationDialog notSpecifiedInformationDialog = new NotSpecifiedInformationDialog();
                    notSpecifiedInformationDialog.show(getSupportFragmentManager(), NotSpecifiedInformationDialog.class.getSimpleName());

                    return false;
                }

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
            });
        }

        return true;
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

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent alarmClockActivity = new Intent(context, AlarmClockActivity.class);
                ActivityUtils.startActivity(AlarmClockActivity.this, alarmClockActivity);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(UPDATE_ACTIVITY_ACTION));
    }

    public static void updateActivity(Context context) {
        Intent updateIntent = new Intent(UPDATE_ACTIVITY_ACTION);
        context.sendBroadcast(updateIntent);
    }

    private void startMainActivity(boolean checkLastActivity) {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra(ActivityUtils.CHECK_LAST_ACTIVITY, checkLastActivity);
        ActivityUtils.startActivity(AlarmClockActivity.this, mainActivity);
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
            ArrayList<Spanned> formatted_lessons = new ArrayList<>();
            for (int i = 0; i < lessons.length(); ++i) {
                try {
                    JSONObject jsonObject = lessons.getJSONObject(i);
                    Spanned lesson = HtmlCompat.fromHtml(
                            context.getString(R.string.lesson_number) + jsonObject.getInt("number") + " - " + jsonObject.getString("name"),
                            0
                    );
                    if (information.getAlarm().getInt("number") == jsonObject.getInt("number")) {
                        lesson =  HtmlCompat.fromHtml("<b>" + lesson + "</b>", 0);
                    }
                    formatted_lessons.add(lesson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ArrayAdapter<Spanned> groupAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, formatted_lessons);

            listView.setAdapter(groupAdapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    if (information.getAlarm().getInt("number") != lessons.getJSONObject(position).getInt("number")) {
                        Alarm.cancelAlarm(getApplicationContext());
                        Alarm.startAlarm(context, lessons.getJSONObject(position), information);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            });
        });
        lessonTextView.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}
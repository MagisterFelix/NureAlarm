package com.nure.alarm.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.network.NetworkStatus;
import com.nure.alarm.views.dialogs.HelpDialog;
import com.nure.alarm.views.dialogs.NotSpecifiedInformationDialog;
import com.nure.alarm.views.dialogs.ReceivingGroupsDialog;
import com.nure.alarm.views.dialogs.UnavailableNetworkDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private Information information;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        information = FileManager.readInfo(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        Request request = new Request(getApplicationContext());
        request.getGroups(getSupportFragmentManager());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.settings);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.alarm_clock) {
                Intent alarmClockActivity = new Intent(getApplicationContext(), AlarmClockActivity.class);
                startActivity(alarmClockActivity);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        Button settingTimeButton = findViewById(R.id.setting_time_button);
        if (information.getSettingHour() != -1 && information.getSettingMinute() != -1) {
            settingTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getSettingHour(), information.getSettingMinute()));
        }
        settingTimeButton.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
                information.setSettingHour(selectedHour);
                information.setSettingMinute(selectedMinute);
                FileManager.writeInfo(getApplicationContext(), information);

                if (information.isEnabled()) {
                    Alarm.disableAlarmWork(getApplicationContext());
                    Alarm.enableAlarmWork(getApplicationContext(), information);
                }

                settingTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getSettingHour(), information.getSettingMinute()));
            };

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MainActivity.this,
                    onTimeSetListener,
                    information.getSettingHour(),
                    information.getSettingMinute(),
                    true
            );
            timePickerDialog.show();
        });

        TextView groupTextView = findViewById(R.id.group);
        if (information.getGroup().length() != 0) {
            try {
                groupTextView.setText(information.getGroup().getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        groupTextView.setOnClickListener(v -> {
            HashMap<String, Integer> groups = FileManager.readGroups(getApplicationContext());

            if (groups.size() > 0) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.group_spinner);

                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);

                dialog.getWindow().setLayout(width, height);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.group_list_view);

                ArrayList<String> sorted_groups = (ArrayList<String>) new ArrayList<>(groups.keySet()).stream().sorted().collect(Collectors.toList());
                ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, sorted_groups);

                listView.setAdapter(groupAdapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence string, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence string, int start, int before, int count) {
                        groupAdapter.getFilter().filter(string);
                    }

                    @Override
                    public void afterTextChanged(Editable string) {}
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    groupTextView.setText(groupAdapter.getItem(position));
                    JSONObject object = new JSONObject();
                    try {
                        object.put("id", groups.get(groupAdapter.getItem(position)));
                        object.put("name", groupAdapter.getItem(position));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    information.setGroup(object);
                    FileManager.writeInfo(getApplicationContext(), information);
                    dialog.dismiss();
                });
            } else {
                if (NetworkStatus.isAvailable(getApplication())) {
                    ReceivingGroupsDialog receivingGroupsDialog = new ReceivingGroupsDialog();
                    receivingGroupsDialog.show(getSupportFragmentManager(), "ReceivingGroupsDialog");
                } else{
                    UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                    unavailableNetworkDialog.show(getSupportFragmentManager(), "UnavailableNetworkDialog");
                }
            }
        });

        Spinner delay = findViewById(R.id.delay);
        ArrayList<Integer> delay_keys = new ArrayList<>(Arrays.asList(0, 10, 30, 60, 120));
        ArrayList<String> delay_values = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.delay)));
        ArrayAdapter<String> delayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, delay_values);
        delay.setAdapter(delayAdapter);
        delay.setSelection(delay_keys.indexOf(information.getDelay()));
        delay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                information.setDelay(delay_keys.get(position));
                FileManager.writeInfo(getApplicationContext(), information);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if (getIntent().getAction() != null && getIntent().getAction().equals("change")) {
            collapsePanel(getApplicationContext());

            TextView lessonTextView = findViewById(R.id.lesson);
            lessonTextView.setOnClickListener(v -> {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.lesson_spinner);

                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);

                dialog.getWindow().setLayout(width, height);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                Configuration configuration = new Configuration(getApplicationContext().getResources().getConfiguration());
                configuration.setLocale(new Locale(new SessionManager(getApplicationContext()).fetchLocale()));
                Context context = getApplicationContext().createConfigurationContext(configuration);

                ListView listView = dialog.findViewById(R.id.lesson_list_view);

                JSONArray lessons = FileManager.readInfo(getApplicationContext()).getLessons();
                ArrayList<String> formatted_lessons = new ArrayList<>();
                for (int i = 0; i < lessons.length(); ++i) {
                    try {
                        JSONObject jsonObject = lessons.getJSONObject(i);
                        formatted_lessons.add(context.getString(R.string.lesson_number) + jsonObject.getString("number") + " - " + jsonObject.getString("name"));
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

    @Override
    protected void attachBaseContext(Context base) {
        Locale locale = new Locale(new SessionManager(base).fetchLocale());
        Locale.setDefault(locale);
        Configuration configuration = base.getResources().getConfiguration();
        configuration.setLocale(locale);
        super.attachBaseContext(base.createConfigurationContext(configuration));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem help = menu.findItem(R.id.help);
        help.setOnMenuItemClickListener(menuItem -> {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.show(getSupportFragmentManager(), "HelpDialog");
            return true;
        });

        MenuItem locale = menu.findItem(R.id.locale);
        locale.setIcon(sessionManager.fetchLocale().equals("uk") ? R.mipmap.ic_uk : R.mipmap.ic_en);
        locale.setOnMenuItemClickListener(menuItem -> {
            if (sessionManager.fetchLocale().equals("en")) {
                sessionManager.saveLocale("uk");
            } else {
                sessionManager.saveLocale("en");
            }
            locale.setIcon(sessionManager.fetchLocale().equals("uk") ? R.mipmap.ic_uk : R.mipmap.ic_en);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            overridePendingTransition(0, 0);
            return true;
        });

        SwitchMaterial switchMaterial = menu.findItem(R.id.switchStatus).getActionView().findViewById(R.id.switchStatus);
        switchMaterial.setChecked(information.isEnabled());
        switchMaterial.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            information = FileManager.readInfo(getApplicationContext());

            if (information.getSettingHour() == -1 && information.getSettingMinute() == -1 || information.getGroup().length() == 0) {
                compoundButton.setChecked(false);
                NotSpecifiedInformationDialog notSpecifiedInformationDialog = new NotSpecifiedInformationDialog();
                notSpecifiedInformationDialog.show(getSupportFragmentManager(), "NotSpecifiedInformationDialog");
            } else {
                if (NetworkStatus.isAvailable(getApplication())) {
                    information.setEnabled(isChecked);
                    FileManager.writeInfo(getApplicationContext(), information);

                    if (isChecked) {
                        Alarm.enableAlarmWork(getApplicationContext(), information);
                    } else {
                        Alarm.disableAlarmWork(getApplicationContext());
                    }
                } else{
                    compoundButton.setChecked(false);
                    UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                    unavailableNetworkDialog.show(getSupportFragmentManager(), "UnavailableNetworkDialog");
                }
            }
        });

        return true;
    }
}

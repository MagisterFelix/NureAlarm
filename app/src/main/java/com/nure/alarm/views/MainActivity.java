package com.nure.alarm.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.nure.alarm.core.activity.Helper;
import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.managers.ContextManager;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.managers.SessionManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.network.NetworkInfo;
import com.nure.alarm.core.updater.Updater;
import com.nure.alarm.views.dialogs.HelpDialog;
import com.nure.alarm.views.dialogs.NotSpecifiedInformationDialog;
import com.nure.alarm.views.dialogs.UnavailableNetworkDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private Information information;
    private SessionManager sessionManager;
    private Updater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        information = FileManager.readInfo(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.alarm_settings);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.alarm_clock) {
                startAlarmClockActivity(false);
                return true;
            }
            return false;
        });

        Button settingTimeButton = findViewById(R.id.setting_time_button);
        settingTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getSettingHour(), information.getSettingMinute()));
        settingTimeButton.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
                information.setSettingHour(selectedHour);
                information.setSettingMinute(selectedMinute);
                FileManager.writeInfo(getApplicationContext(), information);

                if (information.isEnabled()) {
                    Alarm.disableAlarmWork(getApplicationContext(), getSupportFragmentManager());
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
            if (NetworkInfo.isNetworkAvailable(getApplication())) {
                Calendar now = Calendar.getInstance();
                Calendar lastTime = Calendar.getInstance();
                lastTime.setTimeInMillis(sessionManager.fetchGroupRequestTime());

                if (TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis() - lastTime.getTimeInMillis()) > 0
                        || FileManager.readGroups(getApplicationContext()).size() == 0) {
                    Request request = new Request(getApplicationContext());
                    request.getGroups(this, getSupportFragmentManager(), groupTextView);
                } else {
                    showGroups(this, ContextManager.getLocaleContext(getApplicationContext()), groupTextView);
                }
            } else {
                UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                unavailableNetworkDialog.show(getSupportFragmentManager(), UnavailableNetworkDialog.class.getSimpleName());
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

        if (NetworkInfo.isNetworkAvailable(getApplication())) {
            updater = new Updater(this, ContextManager.getLocaleContext(getApplicationContext()));
            updater.checkForUpdates(findViewById(R.id.activity_main), navigation);
        }

        if (getIntent().getExtras() == null && !getClass().getSimpleName().equals(sessionManager.fetchLastActivity())) {
            startAlarmClockActivity(true);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updater.update();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startAlarmClockActivity(boolean checkLastActivity) {
        Intent alarmClockActivity = new Intent(getApplicationContext(), AlarmClockActivity.class);
        alarmClockActivity.putExtra(Helper.CHECK_LAST_ACTIVITY, checkLastActivity);
        Helper.startActivity(MainActivity.this, alarmClockActivity);
    }

    public static void showGroups(Activity activity, Context context, TextView groupTextView) {
        HashMap<String, Integer> groups = FileManager.readGroups(context);

        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.group_spinner);

        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.8);

        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        EditText editText = dialog.findViewById(R.id.edit_text);
        ListView listView = dialog.findViewById(R.id.group_list_view);

        ArrayList<String> sorted_groups = (ArrayList<String>) new ArrayList<>(groups.keySet()).stream().sorted().collect(Collectors.toList());
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, sorted_groups);

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

            Information information = FileManager.readInfo(context);
            information.setGroup(object);
            FileManager.writeInfo(context, information);

            dialog.dismiss();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem help = menu.findItem(R.id.help);
        help.setOnMenuItemClickListener(menuItem -> {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.show(getSupportFragmentManager(), HelpDialog.class.getSimpleName());
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
            menuItem.setIcon(sessionManager.fetchLocale().equals("uk") ? R.mipmap.ic_uk : R.mipmap.ic_en);
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            Helper.startActivity(MainActivity.this, mainActivity);
            return true;
        });

        SwitchMaterial switchMaterial = menu.findItem(R.id.switchStatus).getActionView().findViewById(R.id.switchStatus);
        switchMaterial.setChecked(information.isEnabled());
        switchMaterial.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            information = FileManager.readInfo(getApplicationContext());

            if (information.getSettingHour() == -1 && information.getSettingMinute() == -1 || information.getGroup().length() == 0) {
                compoundButton.setChecked(false);
                NotSpecifiedInformationDialog notSpecifiedInformationDialog = new NotSpecifiedInformationDialog();
                notSpecifiedInformationDialog.show(getSupportFragmentManager(), NotSpecifiedInformationDialog.class.getSimpleName());
            } else {
                if (NetworkInfo.isNetworkAvailable(getApplication()) || !isChecked) {
                    information.setEnabled(isChecked);
                    FileManager.writeInfo(getApplicationContext(), information);

                    if (isChecked) {
                        Alarm.enableAlarmWork(getApplicationContext(), information);
                    } else {
                        Alarm.disableAlarmWork(getApplicationContext(), getSupportFragmentManager());
                    }
                } else {
                    compoundButton.setChecked(false);
                    UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                    unavailableNetworkDialog.show(getSupportFragmentManager(), UnavailableNetworkDialog.class.getSimpleName());
                }
            }
        });

        return true;
    }
}

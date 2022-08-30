package com.nure.alarm.views;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
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

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.FileManager;
import com.nure.alarm.core.api.Request;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.core.network.NetworkStatus;
import com.nure.alarm.views.dialogs.NotSpecifiedInformationDialog;
import com.nure.alarm.views.dialogs.PermissionDialog;
import com.nure.alarm.views.dialogs.ReceivingGroupsDialog;
import com.nure.alarm.views.dialogs.UnavailableNetworkDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private Information information;

    private Button settingTimeButton;
    private TextView groupTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        information = FileManager.readInfo(getApplicationContext());

        Request request = new Request(getApplicationContext());
        request.getGroups(getSupportFragmentManager());

        settingTimeButton = findViewById(R.id.setting_time_button);
        if (information.getSettingHour() != -1 && information.getSettingMinute() != -1) {
            settingTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getSettingHour(), information.getSettingMinute()));
        }
        settingTimeButton.setOnClickListener(view -> {
            TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
                information.setSettingHour(selectedHour);
                information.setSettingMinute(selectedMinute);
                FileManager.writeInfo(getApplicationContext(), information);

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

        groupTextView = findViewById(R.id.group);
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
                dialog.setContentView(R.layout.spinner);

                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.8);

                dialog.getWindow().setLayout(width, height);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);

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
        ArrayList<Integer> keys = new ArrayList<>(Arrays.asList(30, 60, 120));
        ArrayList<String> values = new ArrayList<>(Arrays.asList("30 minutes", "1 hour", "2 hours"));
        ArrayAdapter<String> delayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, values);
        delay.setAdapter(delayAdapter);
        delay.setSelection(keys.indexOf(information.getDelay()));
        delay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                information.setDelay(keys.get(position));
                FileManager.writeInfo(getApplicationContext(), information);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SwitchMaterial switchMaterial = menu.findItem(R.id.switchStatus).getActionView().findViewById(R.id.switchStatus);
        switchMaterial.setChecked(information.getStatus());
        switchMaterial.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                compoundButton.setChecked(false);
                PermissionDialog permissionDialog = new PermissionDialog();
                Bundle bundle = new Bundle();
                bundle.putString("packageName", getPackageName());
                permissionDialog.setArguments(bundle);
                permissionDialog.show(getSupportFragmentManager(), "PermissionDialog");
            } else {
                if (information.getSettingHour() == -1 && information.getSettingMinute() == -1 || information.getGroup().length() == 0) {
                    compoundButton.setChecked(false);
                    NotSpecifiedInformationDialog notSpecifiedInformationDialog = new NotSpecifiedInformationDialog();
                    notSpecifiedInformationDialog.show(getSupportFragmentManager(), "NotSpecifiedInformationDialog");
                } else {
                    if (NetworkStatus.isAvailable(getApplication())) {
                        information.setStatus(isChecked);
                        FileManager.writeInfo(getApplicationContext(), information);

                        if (isChecked) {
                            Alarm.enableAlarm(getApplicationContext(), information);
                        } else {
                            Alarm.disableAlarm(getApplicationContext());
                        }
                    } else{
                        compoundButton.setChecked(false);
                        UnavailableNetworkDialog unavailableNetworkDialog = new UnavailableNetworkDialog();
                        unavailableNetworkDialog.show(getSupportFragmentManager(), "UnavailableNetworkDialog");
                    }
                }
            }
        });

        return true;
    }
}

package com.nure.alarm;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Information information;
    private Button timeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        information = FileManager.getInfo(this);

        timeButton = findViewById(R.id.time_button);
        timeButton.setOnClickListener(view -> selectTime());
        timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getAlarmSettingHour(), information.getAlarmSettingMinute()));

        if (noOverlayPermission()) {
            Toast.makeText(this, R.string.permission_message, Toast.LENGTH_SHORT).show();
            openOverlayPermissionActivityForResult();
        }
    }

    private boolean noOverlayPermission() {
        return !Settings.canDrawOverlays(MainActivity.this);
    }

    private void openOverlayPermissionActivityForResult() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        overlayPermissionActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> overlayPermissionActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (noOverlayPermission()) {
                    Toast.makeText(this, R.string.permission_message, Toast.LENGTH_SHORT).show();
                    openOverlayPermissionActivityForResult();
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SwitchMaterial switchMaterial = menu.findItem(R.id.switchStatus).getActionView().findViewById(R.id.switchStatus);
        switchMaterial.setChecked(information.getStatus());
        switchMaterial.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            information.setStatus(isChecked);
            FileManager.updateInfo(this, information);

            if (isChecked) {
                enableAlarm();
            } else {
                disableAlarm();
            }
        });

        return true;
    }

    private long getDelay() {
        Calendar now = Calendar.getInstance();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, information.getAlarmSettingHour());
        startTime.set(Calendar.MINUTE, information.getAlarmSettingMinute());
        startTime.set(Calendar.SECOND, 0);

        if (startTime.before(now) || startTime.equals(now)) {
            startTime.add(Calendar.DATE, 1);
            Alarm.setAlarm(getApplicationContext());
        }

        return startTime.getTimeInMillis() - now.getTimeInMillis();
    }

    private void enableAlarm() {
        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(AlarmWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(getDelay(), TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag("AlarmWork")
                .build();

        WorkManager.getInstance(this).enqueue(request);
    }

    private void disableAlarm() {
        Alarm.cancelAlarm(getApplicationContext());
        WorkManager.getInstance(this).cancelAllWorkByTag("AlarmWork");
    }

    private void selectTime() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            information.setAlarmSettingHour(selectedHour);
            information.setAlarmSettingMinute(selectedMinute);
            FileManager.updateInfo(this, information);

            timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", information.getAlarmSettingHour(), information.getAlarmSettingMinute()));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                information.getAlarmSettingHour(), information.getAlarmSettingMinute(), true);
        timePickerDialog.show();
    }
}
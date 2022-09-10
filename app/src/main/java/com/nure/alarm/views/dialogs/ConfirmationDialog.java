package com.nure.alarm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.views.AlarmClockActivity;

public class ConfirmationDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_dialog)
                .setMessage(R.string.confirmation_message)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    Alarm.cancelAlarm(requireActivity().getApplicationContext());
                    Intent alarmClockActivity = new Intent(requireActivity().getApplicationContext(), AlarmClockActivity.class);
                    startActivity(alarmClockActivity);
                    requireActivity().finish();
                    requireActivity().overridePendingTransition(0, 0);
                })
                .setNegativeButton(R.string.no, null);
        return builder.create();
    }
}

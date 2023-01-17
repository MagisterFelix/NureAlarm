package com.nure.alarm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.nure.alarm.R;
import com.nure.alarm.core.Alarm;
import com.nure.alarm.core.managers.FileManager;
import com.nure.alarm.core.models.Information;
import com.nure.alarm.views.AlarmClockActivity;

import org.json.JSONArray;

public class DeletionConfirmationDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_dialog)
                .setMessage(R.string.deletion_confirmation_message)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    Information information = FileManager.readInfo(requireActivity().getApplicationContext());
                    information.setLessons(new JSONArray());
                    FileManager.writeInfo(requireActivity().getApplicationContext(), information);

                    Alarm.cancelAlarm(requireActivity().getApplicationContext());
                    AlarmClockActivity.updateActivity(requireActivity().getApplicationContext());
                })
                .setNegativeButton(R.string.no, null);
        return builder.create();
    }
}

package com.nure.alarm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.nure.alarm.R;

public class ReceivingGroupsDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Receiving groups")
                .setMessage(R.string.receiving_groups_message)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    requireActivity().finish();
                    requireActivity().overridePendingTransition(0, 0);
                    startActivity(requireActivity().getIntent());
                    requireActivity().overridePendingTransition(0, 0);
                });
        return builder.create();
    }
}

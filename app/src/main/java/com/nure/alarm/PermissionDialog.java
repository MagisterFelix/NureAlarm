package com.nure.alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class PermissionDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Permission")
                .setMessage(R.string.permission_message)
                .setPositiveButton("ok", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)));
        return builder.create();
    }
}

package com.nure.alarm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.nure.alarm.R;

public class PermissionDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Permission")
                .setMessage(R.string.permission_message)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                        if (getArguments() != null) {
                            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getArguments().getString("packageName"))));
                        } else {
                            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        }
                });
        return builder.create();
    }
}

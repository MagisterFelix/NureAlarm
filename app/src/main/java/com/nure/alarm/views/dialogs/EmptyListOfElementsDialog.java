package com.nure.alarm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.nure.alarm.R;

public class EmptyListOfElementsDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.empty_list_of_elements_dialog)
                .setMessage(R.string.empty_list_of_elements_message)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }
}

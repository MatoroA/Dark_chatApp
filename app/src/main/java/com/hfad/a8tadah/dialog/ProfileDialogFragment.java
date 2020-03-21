package com.hfad.a8tadah.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.hfad.a8tadah.R;

public class ProfileDialogFragment extends DialogFragment {

    private String TAG = ProfileDialogFragment.class.getName();

    private String fieldText;

    public interface ProfileDailogInterface {
        void onChangeField(String value);
    }

    private ProfileDailogInterface listener;

    public ProfileDialogFragment(String text, ProfileDailogInterface listener) {
        this.fieldText = text;
        this.listener = listener;
    }

    private EditText mEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_layout, null);

        mEditText = view.findViewById(R.id.dialog_field);
        if (fieldText != null)
            mEditText.setText(fieldText);
        builder.setView(view);
        builder.setMessage(R.string.app_name)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        Toast.makeText(view.getContext(), mEditText.getText().toString(), Toast.LENGTH_LONG).show();

                        if (mEditText.getText() != null) {
                            listener.onChangeField(mEditText.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

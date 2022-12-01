package com.example.kartapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EndreSlettDialog extends DialogFragment {
    private DialogClickListener callback;
    public interface DialogClickListener {
        public void onEndreClick();
        public void onSlettClick();
        public void onAvbrytClick();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callback=(DialogClickListener)
                    getActivity(); }
        catch (ClassCastException e) {
            throw new ClassCastException("Kallende klasse må implementere interfacet!");}
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.title).setPositiveButton(R.string.endre, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton){ callback.onEndreClick(); }} ).setNeutralButton(R.string.slett, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int whichButton){ callback.onSlettClick();}} ) .setNegativeButton(R.string.avbryt, new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int whichButton){ callback.onAvbrytClick();}} ).create();
    }

}


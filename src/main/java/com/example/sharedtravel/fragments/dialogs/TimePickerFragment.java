package com.example.sharedtravel.fragments.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.example.sharedtravel.callbacks.ConfirmationDialogCallback;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private static Date date;
    private ConfirmationDialogCallback mCallback;

    public TimePickerFragment() {
        date = new Date();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    // Create a new instance of TimePickerDialog and return it


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        date.setHours(hourOfDay);
        date.setMinutes(minute);
        mCallback.onConfirmPressed();
    }

    public void showWithCallback(FragmentManager fragmentManager, ConfirmationDialogCallback callback) {
        this.mCallback = callback;
        this.show(fragmentManager, "timePicker");
    }
    public static Date getDate() {
        return date;
    }
}

package com.example.sharedtravel.fragments.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.DatePicker;

import com.example.sharedtravel.callbacks.ConfirmationDialogCallback;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static Date getDate() {
        return date;
    }

    private static Date date;
    private static ConfirmationDialogCallback mCallback;

    public DatePickerFragment() {
        date = new Date();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        int year;
        int month;
        int day;

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);

        dialog.getDatePicker().setMinDate(c.getTimeInMillis());

        return dialog;
    }

    public void showWithCallback(FragmentManager fragmentManager, ConfirmationDialogCallback callback) {
        mCallback = callback;
        this.show(fragmentManager, "datePicker");
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        date.setYear(year - 1900);
        date.setMonth(month);
        date.setDate(day);
        mCallback.onConfirmPressed();
    }
}


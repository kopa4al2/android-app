package com.example.sharedtravel.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.sharedtravel.R;

import java.util.Calendar;
import java.util.HashMap;

public class WeekDaysConstants {

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, String> daysOfWeek = new HashMap<>();

    public WeekDaysConstants(Context ctx) {
        daysOfWeek.put(Calendar.MONDAY, ctx.getString(R.string.monday));
        daysOfWeek.put(Calendar.TUESDAY, ctx.getString(R.string.tuesday));
        daysOfWeek.put(Calendar.WEDNESDAY, ctx.getString(R.string.wednesday));
        daysOfWeek.put(Calendar.THURSDAY, ctx.getString(R.string.thursday));
        daysOfWeek.put(Calendar.FRIDAY, ctx.getString(R.string.friday));
        daysOfWeek.put(Calendar.SATURDAY, ctx.getString(R.string.saturday));
        daysOfWeek.put(Calendar.SUNDAY, ctx.getString(R.string.sunday));
    }

    public String getDayName(int dayNumber) {
        return daysOfWeek.get(dayNumber);
    }
}

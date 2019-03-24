package com.example.sharedtravel.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtils {

    public static String dateToString(Date date, int dateFormatMode) {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(dateFormatMode, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String dateToHourString(Date date) {
        DateFormat dateFormat =  SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        return dateFormat.format(date);
    }
}

package com.example.sharedtravel.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

public class BindOnScreenTouchHideSoftKeyboard {
    private static void hideSoftKeyboard(@NotNull Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ignored) {}
    }

    public static void bindListener(View view, Activity activity) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText) && !(view instanceof TextInputLayout)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup && !(view instanceof TextInputLayout)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                bindListener(innerView, activity);
            }
        }
    }
}

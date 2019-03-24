package com.example.sharedtravel.fragments.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.callbacks.ConfirmationDialogCallback;

public class DialogBuilder {

    public static void showErrorMessage(Context context, String errorMessage) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(errorMessage)
                .setIcon(R.drawable.ic_error_outline)
                .setTitle(context.getString(R.string.error_title))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.ok), (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();

        alert.setOnShowListener(dialog -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorErrorMaterial));

            TextView title = alert.getWindow().findViewById(R.id.alertTitle);
            TextView positive = alert.findViewById(android.R.id.button1);
            title.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
            positive.setTypeface(ResourcesCompat.getFont(context, R.font.crimson_text));
        });
        alert.show();
    }

    public static void showConfirmationDialog(Context context, String message, ConfirmationDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(context.getString(R.string.confirm_dialog_title))
                .setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.confirm), (dialog, id) -> callback.onConfirmPressed())
                .setNegativeButton(context.getResources().getString(R.string.cancel), (dialog, id) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialog -> {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));

            TextView title = alert.getWindow().findViewById(R.id.alertTitle);
            TextView positive = alert.findViewById(android.R.id.button1);
            TextView negative = alert.findViewById(android.R.id.button2);
            title.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
            positive.setTypeface(ResourcesCompat.getFont(context, R.font.crimson_text));
            negative.setTypeface(ResourcesCompat.getFont(context, R.font.crimson_text));
        });
        alert.show();
    }

    public static void showConfirmationDialog(Context context, String message, View view, ConfirmationDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(context.getString(R.string.are_you_sure))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.confirm), (dialog, id) -> callback.onConfirmPressed())
                .setNegativeButton(context.getResources().getString(R.string.cancel), (dialog, id) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialog -> {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.colorAccent));

            TextView title = alert.getWindow().findViewById(R.id.alertTitle);
            TextView positive = alert.findViewById(android.R.id.button1);
            TextView negative = alert.findViewById(android.R.id.button2);
            title.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
            positive.setTypeface(ResourcesCompat.getFont(context, R.font.crimson_text));
            negative.setTypeface(ResourcesCompat.getFont(context, R.font.crimson_text));
        });
        alert.show();
    }

    public static void showSuccessDialog(Context context, String successMessage) {
        Snackbar successSnackBar = Snackbar.make(((Activity) context).getCurrentFocus(), successMessage, Snackbar.LENGTH_LONG);
        TextView tv = (successSnackBar.getView()).findViewById(android.support.design.R.id.snackbar_text);
        tv.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
        successSnackBar.show();
    }

    public static void showSuccessDialog(View v, String successMessage) {
        Snackbar successSnackBar = Snackbar.make(v, successMessage, Snackbar.LENGTH_LONG);
        successSnackBar.show();
    }

    public static void showInfoDialog(Context context, String infoMessage) {
        Snackbar infoSnackBar = Snackbar.make(((Activity) context).getCurrentFocus(), infoMessage, Snackbar.LENGTH_LONG);

        TextView tv = (infoSnackBar.getView()).findViewById(android.support.design.R.id.snackbar_text);
        tv.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
        infoSnackBar.show();
    }
}

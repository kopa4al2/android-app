package com.example.sharedtravel.callbacks;

import android.support.v7.app.AlertDialog;

public interface LoginCallback {

    /**
     * @param email user email
     * @param password user password
     * @param dialog the alert dialog that called the method
     * @param rememberCredentials
     */
    void loginUser(String email, String password, AlertDialog dialog, boolean rememberCredentials);

    void loginWithFacebook(AlertDialog dialog);

}

package com.example.sharedtravel.callbacks;

import android.support.v7.app.AlertDialog;

public interface RegisterCallback {
    /**
     * @param email register email
     * @param password register password
     * @param dialog the AlertDialog that initiated the callback
     */
    void registerUser(String email, String password,String username, AlertDialog dialog);
}

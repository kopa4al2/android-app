package com.example.sharedtravel.firebase.utils;

import android.util.Log;

import com.google.firebase.firestore.QuerySnapshot;

public interface QueryCompleteListener {

    default void onStart() {
        return;
    }
    void onSuccess(QuerySnapshot querySnapshot);
    default void onFail(Exception e) {
        Log.e("DB_ERROR", "onFail: ", e);
    }
}

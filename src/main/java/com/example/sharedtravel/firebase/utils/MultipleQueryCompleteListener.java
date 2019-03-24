package com.example.sharedtravel.firebase.utils;

import android.util.Log;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public interface MultipleQueryCompleteListener {
    default void onStart() {
        return;
    }
    default void onFail(Exception e) {
        Log.e("DB_ERROR", "onFail: ", e);
    }

    void onSuccess(List<QuerySnapshot> objects);
}

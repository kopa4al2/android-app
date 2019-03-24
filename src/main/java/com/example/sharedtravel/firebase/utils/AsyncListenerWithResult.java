package com.example.sharedtravel.firebase.utils;

public interface AsyncListenerWithResult {
    default void onStart() {
        return;
    }

    void onSuccess(Object data);

    default void onFail(Exception exception) {
        exception.printStackTrace();
    }
}

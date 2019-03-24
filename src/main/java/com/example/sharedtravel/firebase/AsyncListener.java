package com.example.sharedtravel.firebase;

public interface AsyncListener {

    default void onStart() {
    }

    void onSuccess();

    default void onFail(Exception exception) {
        exception.printStackTrace();
    }
}

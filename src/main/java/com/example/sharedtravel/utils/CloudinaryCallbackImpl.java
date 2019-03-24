package com.example.sharedtravel.utils;

import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public abstract class CloudinaryCallbackImpl implements UploadCallback {
    @Override
    public void onStart(String requestId) {

    }

    @Override
    public void onProgress(String requestId, long bytes, long totalBytes) {

    }

    @Override
    public abstract void onSuccess(String requestId, Map resultData);

    @Override
    public void onError(String requestId, ErrorInfo error) {
        Log.e("CLOUDINARY_ERROR", "onError: DIDNT UPLOAD PICTURE FOR SOME REASON " + error.getDescription());
    }

    @Override
    public void onReschedule(String requestId, ErrorInfo error) {
        Log.e("CLOUDINARY_ERROR", "onError: DIDNT UPLOAD PICTURE FOR SOME REASON " + error.getDescription());
    }
}

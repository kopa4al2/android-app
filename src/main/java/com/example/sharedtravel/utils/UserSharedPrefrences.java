package com.example.sharedtravel.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class UserSharedPrefrences {
    private static final String PREF_NAME = "user_shared_pref_%s";
    private SharedPreferences mPreferences;
    private String mUserId;
    private static UserSharedPrefrences _this;

    private UserSharedPrefrences(Context pContext, String pUserId) {
        mUserId = pUserId;
        mPreferences = pContext.getSharedPreferences(String.format(PREF_NAME, pUserId), Context.MODE_PRIVATE);
    }


    private String getUserId() {
        return mUserId;
    }

    public static SharedPreferences getPreferencesForUser(Context context, String userId) {
        if (_this == null)
            _this = new UserSharedPrefrences(context, userId);

        if (_this.getUserId() != null && !TextUtils.equals(_this.getUserId(), userId)) {
            _this = new UserSharedPrefrences(context, userId);
        }

        return _this.mPreferences;
    }
}

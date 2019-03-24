package com.example.sharedtravel;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.cloudinary.android.MediaManager;
import com.example.sharedtravel.localDB.SQLiteDB;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class SharedTravelApplication extends Application {

    public static SQLiteDB dbSQL;

    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dt33hi6os");
        config.put("api_key", "555938971369868");
        config.put("api_secret", "gJ4aqs2ueMm7niWRuME7M6w1bhs");
        MediaManager.init(this, config);

        dbSQL = Room.databaseBuilder(getApplicationContext(),
                SQLiteDB.class, "hitch-db")
                .build();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

    }


}

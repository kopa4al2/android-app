package com.example.sharedtravel.firebase;

import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.model.UrbanTravel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.example.sharedtravel.firebase.utils.DtoToDBConverter.convertUser;

public class UrbanTravelRepository {

    private static final String URBAN_TRAVEL_COLLECTION = "urban_travels";
    private static final String TRAVEL = "travel ";
    private static final String TRAVEL_CREATOR = "travel_creator";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final UrbanTravelRepository _this = new UrbanTravelRepository();
    private static final CollectionReference collectionReference = _this.db.collection(URBAN_TRAVEL_COLLECTION);

    private void UrbanTravel() {
    }

    public static void addUrbanTravel(UrbanTravel travel, AsyncListenerWithResult callback) {
        callback.onStart();
        Map<String, Object> travelCreator = convertUser(travel.getCreator());
        Map<String, Object> dbTravel = new HashMap<>(2);


        dbTravel.put(TRAVEL, travel);
        dbTravel.put(TRAVEL_CREATOR, travelCreator);

        collectionReference.add(dbTravel).addOnCompleteListener(task -> {
            if(!task.isSuccessful())
                callback.onFail(task.getException());

            travel.setId(task.getResult().getId());
            callback.onSuccess(travel);
        });
    }
}

package com.example.sharedtravel.firebase;

import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.model.IntercityTravel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.sharedtravel.firebase.utils.DtoToDBConverter.convertUser;

public class IntercityTravelRepository {

    private static final String TRAVEL_INFO_COLLECTION = "travelInfo";

    private static final String DB_START_CITY = "startCity";
    private static final String DB_DESTINATION_CITY = "destinationCity";
    private static final String DB_PASSENGER_SPACE = "passengersSpace";
    private static final String DB_LUGGAGE = "spaceForLuggage";
    private static final String DB_MORE_INFO = "additionalInfo";
    private static final String DB_DEPARTURE_DATE = "departureDate";
    private static final String DB_CREATION_DATE = "dateOfCreation";
    private static final String DB_HAS_HOURS = "isHourSelected";
    private static final String DB_INTERMEDIATE_STOPS = "intermediateCityStops";
    public static final String DB_CREATOR = "userCreator";

    private final FirebaseFirestore db;

    public IntercityTravelRepository() {
        db = FirebaseFirestore.getInstance();
    }


    public void registerIntercityTravel(IntercityTravel travel, AsyncListenerWithResult listener) {
        listener.onStart();
        Map<String, Object> travelInfo = new HashMap<>();
        travelInfo.put(DB_START_CITY, travel.getStartCity());
        travelInfo.put(DB_DESTINATION_CITY, travel.getDestinationCity());
        travelInfo.put(DB_PASSENGER_SPACE, travel.getPassengersSpace());
        travelInfo.put(DB_LUGGAGE, travel.isSpaceForLuggage());
        travelInfo.put(DB_MORE_INFO, travel.getAdditionalInfo());
        travelInfo.put(DB_DEPARTURE_DATE, new Timestamp(travel.getDepartureDate()));
        travelInfo.put(DB_CREATION_DATE, new Timestamp(travel.getDateOfCreation()));
        travelInfo.put(DB_HAS_HOURS, travel.isHourSelected());
        travelInfo.put(DB_INTERMEDIATE_STOPS, Arrays.asList(travel.getIntermediateCityStops()));
        travelInfo.put(DB_CREATOR, convertUser(travel.getCreator()));



        db.collection(TRAVEL_INFO_COLLECTION)
                .add(travelInfo)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFail);

    }



    public Query getTravelsByDepartureDate() {
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.orderBy(DB_DEPARTURE_DATE, Query.Direction.ASCENDING);
    }

    public Query getTravelsByStartCity(String startCity, Query query) {
        if (query != null)
            return query.whereEqualTo(DB_START_CITY, startCity);
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereEqualTo(DB_START_CITY, startCity);
    }

    public Query getTravelsByDestination(String destination, Query query) {
        if (query != null)
            return query.whereEqualTo(DB_DESTINATION_CITY, destination);
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereEqualTo(DB_DESTINATION_CITY, destination);
    }

    public Query getTravelsAtExactDay(Date date, Query query) {
        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.DATE, date.getDate());
        c1.set(Calendar.DATE, date.getDate());
        c.set(Calendar.MINUTE, 0);
        c1.set(Calendar.MINUTE, 59);
        if (query != null)
            return query.whereGreaterThan(DB_DEPARTURE_DATE, c.getTime())
                    .whereLessThan(DB_DEPARTURE_DATE, c1.getTime());
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereGreaterThan(DB_DEPARTURE_DATE, c.getTime())
                .whereLessThan(DB_DEPARTURE_DATE, c1.getTime());
    }

    public Query getTravelsByDateGraterThan(Date date, Query query) {
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        if (query != null)
            return query.whereGreaterThan(DB_DEPARTURE_DATE, date);
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereGreaterThan(DB_DEPARTURE_DATE, date);
    }


    public Query searchDestinationInIntermediateStops(String endCity, Query query) {
        if (query != null)
            return query.whereArrayContains(DB_INTERMEDIATE_STOPS, endCity);
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereArrayContains(DB_INTERMEDIATE_STOPS, endCity);
    }

    public Query getTravelsWithSpaceForLuggage(Query query) {
        if (query != null)
            return query.whereEqualTo(DB_LUGGAGE, true);
        CollectionReference travelRef = db.collection(TRAVEL_INFO_COLLECTION);
        return travelRef.whereEqualTo(DB_LUGGAGE, true);
    }

    public Query orderByDate(Query baseQuery) {
        return baseQuery.orderBy(DB_DEPARTURE_DATE, Query.Direction.ASCENDING);
    }

    public Query removePastDatesFromQuery(Query query) {
        Date today = Calendar.getInstance().getTime();
        today.setMinutes(0);
        today.setHours(0);
        if(query == null)
            return db.collection(TRAVEL_INFO_COLLECTION).whereGreaterThanOrEqualTo(DB_DEPARTURE_DATE, today);
        return query.whereGreaterThanOrEqualTo(DB_DEPARTURE_DATE, today);
    }

    public void deleteTravelById(String travelId, AsyncListener listener) {
        listener.onStart();
        db.collection(TRAVEL_INFO_COLLECTION).document(travelId).delete().addOnCompleteListener((task -> {
            if(!task.isSuccessful())
                listener.onFail(task.getException());
            listener.onSuccess();
        }));
    }
}

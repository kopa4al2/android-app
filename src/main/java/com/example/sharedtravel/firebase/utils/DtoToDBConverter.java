package com.example.sharedtravel.firebase.utils;

import com.example.sharedtravel.model.User;

import java.util.HashMap;
import java.util.Map;

import static com.example.sharedtravel.firebase.UsersManager.DISPLAY_NAME;
import static com.example.sharedtravel.firebase.UsersManager.EMAIL;
import static com.example.sharedtravel.firebase.UsersManager.FIREBASE_USER;
import static com.example.sharedtravel.firebase.UsersManager.PHONE_NUMBER;
import static com.example.sharedtravel.firebase.UsersManager.PROFILE_PICTURE;
import static com.example.sharedtravel.firebase.UsersManager.REVIEWS;
import static com.example.sharedtravel.firebase.UsersManager.TOTAL_RATING;
import static com.example.sharedtravel.firebase.UsersManager.USER_ID;

public class DtoToDBConverter {

    public static Map<String, Object> convertUser(User creator) {
        Map<String,Object> user = new HashMap<>(4);
        Map<String, Object> firebaseUserInfo = new HashMap<>(4);
        firebaseUserInfo.put(EMAIL, creator.getEmail());
        firebaseUserInfo.put(DISPLAY_NAME, creator.getDisplayName());
        firebaseUserInfo.put(PHONE_NUMBER, creator.getPhoneNumber());
        firebaseUserInfo.put(PROFILE_PICTURE, creator.getProfilePicture());
        user.put(USER_ID, creator.getUserId());
        user.put(TOTAL_RATING, creator.getRating());
        user.put(REVIEWS, creator.getReviewsFromUsers());
        user.put(FIREBASE_USER, firebaseUserInfo);
        return user;
    }
}

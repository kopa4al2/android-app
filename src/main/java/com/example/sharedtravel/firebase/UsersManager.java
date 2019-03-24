package com.example.sharedtravel.firebase;

import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.firebase.utils.DocumentSnapshotConverter;
import com.example.sharedtravel.model.Review;
import com.example.sharedtravel.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersManager {


    public static final String USER_COLLECTION = "users";
    public static final String PROFILE_PICTURE = "profilePic";
    public static final String DISPLAY_NAME = "username";
    public static final String FIREBASE_USER = "firebase_user";
    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phoneNum";
    public static final String USER_ID = "userId";
    private static final String FB_ID = "fbId";
    private static final String PROVIDERS = "providers";
    public static final String MORE_INFO = "moreInfo";
    public static final String TOTAL_RATING = "rating";
    public static final String REVIEWS = "reviews";


    private static volatile UsersManager _this = new UsersManager();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final CollectionReference userCollection =
            _this.db.collection(USER_COLLECTION);

    private UsersManager() {
        _this = this;
    }

    public static UsersManager getInstance() {
        if (_this == null) {
            synchronized (UsersManager.class) {
                if (_this == null) {
                    _this = new UsersManager();
                }
            }
        }
        return _this;
    }

    static void addUserToDb(FirebaseUser user) {
        UserInfo userInfo = isFacebookAccount(user);
        if (userInfo != null) {
            Map<String, Object> fireBase = generateUserMap(userInfo);

            userCollection.document(user.getUid()).set(fireBase);

        } else {
            userCollection.document(user.getUid()).set(user);

        }
    }

    @NotNull
    public static Map<String, Object> generateUserMap(UserInfo userInfo) {
        Map<String, Object> userObject = new HashMap<>();
        Map<String, Object> firebaseUserInfo = new HashMap<>();
        firebaseUserInfo.put(EMAIL, userInfo.getEmail());
        firebaseUserInfo.put(DISPLAY_NAME, userInfo.getDisplayName());
        firebaseUserInfo.put(PHONE_NUMBER, userInfo.getPhoneNumber());
        firebaseUserInfo.put(PROFILE_PICTURE, userInfo.getPhotoUrl().toString());
        Map<String, String> providers = new HashMap<>();
        providers.put(FB_ID, userInfo.getUid());
        firebaseUserInfo.put(PROVIDERS, providers);
        userObject.put(FIREBASE_USER, firebaseUserInfo);
        userObject.put(TOTAL_RATING, 0);
        userObject.put(REVIEWS, new HashMap());
        userObject.put(USER_ID, AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());

        return userObject;
    }

    static void updateFirebaseUser(FirebaseUser firebaseUser) {
        if (isFacebookAccount(firebaseUser) != null)
            return;
        Map<String, String> newUserInfo = new HashMap<>();
        if (firebaseUser.getDisplayName() != null)
            newUserInfo.put(DISPLAY_NAME, firebaseUser.getDisplayName());
        if (firebaseUser.getEmail() != null) {
            newUserInfo.put(EMAIL, firebaseUser.getEmail());
        }
        if (firebaseUser.getPhotoUrl() != null) {
            newUserInfo.put(PROFILE_PICTURE, firebaseUser.getPhotoUrl().toString());
        }
        if (firebaseUser.getPhoneNumber() != null) {
            newUserInfo.put(PHONE_NUMBER, firebaseUser.getPhoneNumber());
        }
        userCollection
                .document(firebaseUser.getUid())
                .update(FIREBASE_USER, newUserInfo);

    }

    public static void addMoreInfo(String userId, String moreInfo, AsyncListener callback) {
        callback.onStart();
        userCollection
                .document(userId)
                .update(MORE_INFO, moreInfo)
                .addOnCompleteListener((task -> {
                    if (task.isSuccessful())
                        callback.onSuccess();
                    else
                        callback.onFail(task.getException());
                }));
    }

    public static void getUser(String userId, AsyncListenerWithResult callback) {
        userCollection.document(userId).get()
                .addOnSuccessListener((docuSnapShot) -> {

                    User user = DocumentSnapshotConverter.convertToUser(docuSnapShot);

                    callback.onSuccess(user);
                });
    }

    public static void getUserProfilePicture(String userId, AsyncListenerWithResult callback) {

        userCollection.document(userId).get()
                .addOnSuccessListener((docuSnapShot) -> {
                    Map firebaseUserInfo = extractFirebaseUser(docuSnapShot);
                    if (firebaseUserInfo != null) {
                        callback.onSuccess(firebaseUserInfo.get(PROFILE_PICTURE));
                    }
                });
    }

    //TODO: this can be refactored to get the actual user rating, from the backend,  and not the one sent from the app
    public static void rateUser(User user, Review review, AsyncListenerWithResult callback) {
        callback.onStart();
        int totalVotes = user.getUsersWhoRatedThisUser().size();
        float currentUserRating = 0;
        for (Review reviewsFromUser : user.getReviewsFromUsers()) {
            currentUserRating += reviewsFromUser.getRating();
        }
        float newRating = (review.getRating() + currentUserRating) / (float) (totalVotes + 1);
        user.setRating(newRating);
        user.getReviewsFromUsers().add(review);
        user.getUsersWhoRatedThisUser().add(review.getCreatorId());
        userCollection.document(user.getUserId())
                .update(TOTAL_RATING, newRating, REVIEWS, FieldValue.arrayUnion(review))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        callback.onSuccess(user);
                    if (task.getException() != null)
                        callback.onFail(task.getException());
                });
    }


    private static Map extractFirebaseUser(@NotNull DocumentSnapshot docuSnapShot) {
        return (Map) docuSnapShot.get(FIREBASE_USER);
    }


    private static UserInfo isFacebookAccount(FirebaseUser firebaseUser) {
        if (firebaseUser == null)
            return null;
        for (UserInfo providerDatum : firebaseUser.getProviderData()) {
            if (providerDatum.getProviderId().contains("facebook.com")) {
                return providerDatum;
            }
        }
        return null;
    }
}

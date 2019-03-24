package com.example.sharedtravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User implements Parcelable {

    private String userId;
    private Map<String, Object> firebaseUser;
    private String moreInfo;
    private float rating;

    private List<Review> reviewsFromUsers;
    private Set<String> usersWhoRatedThisUser;

    public User() {
        reviewsFromUsers = new ArrayList<>();
        usersWhoRatedThisUser = new HashSet<>();
    }


    protected User(Parcel in) {
        userId = in.readString();
        moreInfo = in.readString();
        rating = in.readFloat();
        reviewsFromUsers = in.createTypedArrayList(Review.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Map<String, Object> getFirebaseUser() {
        if (firebaseUser == null)
            return new HashMap<>();
        return firebaseUser;
    }

    public void setFirebaseUser(Map<String, Object> firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public float getRating() {
        if (rating < 0)
            return 0;
        if(rating > 5)
            return 5;
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public List<Review> getReviewsFromUsers() {
        return reviewsFromUsers;
    }

    public void setReviewsFromUsers(List<Review> reviewsFromUsers) {
        this.reviewsFromUsers = reviewsFromUsers;
    }

    public Set<String> getUsersWhoRatedThisUser() {
        return usersWhoRatedThisUser;
    }

    public void setUsersWhoRatedThisUser(Set<String> usersWhoRatedThisUser) {
        this.usersWhoRatedThisUser = usersWhoRatedThisUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(moreInfo);
        dest.writeFloat(rating);
        dest.writeTypedList(reviewsFromUsers);
    }

    public String getProfilePicture() {
        if(firebaseUser == null)
            return null;
        return (String) firebaseUser.get("profilePic") ;
    }
    public String getDisplayName() {
        if(firebaseUser == null)
            return null;
        return (String) firebaseUser.get("username");
    }

    public String getEmail() {
        if(firebaseUser == null)
        return null;
        return (String) firebaseUser.get("email");
    }


    public String getPhoneNumber() {
        if(firebaseUser == null)
            return null;
        return (String) firebaseUser.get("phoneNum");
    }
}

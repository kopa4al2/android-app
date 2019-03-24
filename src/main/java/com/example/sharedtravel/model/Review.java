package com.example.sharedtravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Review implements Parcelable {

    private String creatorId;
    private String reviewText;
    private String creatorUsername;
    private String creatorProfilePic;
    private float rating;

    public Review() {
    }

    public Review(String creatorId, String reviewText, float rating, String creatorUsername, String creatorProfilePic) {
        this.creatorId = creatorId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.creatorUsername = creatorUsername;
        this.creatorProfilePic = creatorProfilePic;
    }

    protected Review(Parcel in) {
        creatorProfilePic = in.readString();
        creatorUsername = in.readString();
        creatorId = in.readString();
        reviewText = in.readString();
        rating = in.readFloat();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getCreatorProfilePic() {
        return creatorProfilePic;
    }

    public void setCreatorProfilePic(String creatorProfilePic) {
        this.creatorProfilePic = creatorProfilePic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(creatorId);
        dest.writeString(creatorUsername);
        dest.writeString(reviewText);
        dest.writeString(creatorProfilePic);
        dest.writeFloat(rating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(creatorId, review.creatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creatorId);
    }
}

package com.example.sharedtravel.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class UrbanTravel {

    //Primary key for ROOM; EXCLUDE for Firestore
    @PrimaryKey
    @NotNull
    @Exclude
    private String id;

    @ColumnInfo(name = "creatorId")
    @Exclude
    private String creatorId;

    private GeoLocation startPoint;
    private GeoLocation endPoint;
    private List<GeoLocation> inBetweenStops;
    private List<Integer> frequency;
    private Date dateOfTravel;

    @Exclude
    private User creator;

    public UrbanTravel() {
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoLocation getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(GeoLocation startPoint) {
        this.startPoint = startPoint;
    }

    public GeoLocation getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(GeoLocation endPoint) {
        this.endPoint = endPoint;
    }

    public List<GeoLocation> getInBetweenStops() {
        return inBetweenStops;
    }

    public void setInBetweenStops(List<GeoLocation> inBetweenStops) {
        this.inBetweenStops = inBetweenStops;
    }

    public List<Integer> getFrequency() {
        return frequency;
    }

    public void setFrequency(List<Integer> frequency) {
        this.frequency = frequency;
    }

    public Date getDateOfTravel() {
        return dateOfTravel;
    }

    public void setDateOfTravel(Date dateOfTravel) {
        this.dateOfTravel = dateOfTravel;
    }

    @Exclude
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
        this.creatorId = creator.getUserId();
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}

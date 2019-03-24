package com.example.sharedtravel.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "travels")
//@Entity(tableName = "travels",
//        foreignKeys= @ForeignKey(entity = User.class,
//        parentColumns = "userId",
//        childColumns = "creator",
//        onDelete = CASCADE))
public class IntercityTravel implements Parcelable {

    @PrimaryKey
    @NotNull
    private String travelID;

    @ColumnInfo(name = "creatorID")
    private String creatorUUID;

//    @ColumnInfo(name = "creator")
    @Ignore
    private User creator;

    @ColumnInfo(name = "startCity")
    private String startCity;

    @ColumnInfo(name = "destinationCity")
    private String destinationCity;

    @Ignore
    private String[] intermediateCityStops;
    @ColumnInfo(name = "passengersSpace")
    private int passengersSpace;
    @ColumnInfo(name = "spaceForLuggage")
    private boolean spaceForLuggage;
    @ColumnInfo(name = "departureDate")
    private Date departureDate;
    @ColumnInfo(name = "additionalInfo")
    private String additionalInfo;

    //this field is so i don't make additional Date object for hour of departure
    @ColumnInfo(name = "isHourSelected")
    private boolean isHourSelected;

    @ColumnInfo(name = "dateOfCreation")
    private Date dateOfCreation;


    public IntercityTravel() {
    }

    @Ignore
    public IntercityTravel(String creatorUUID,
                           String startCity,
                           String destinationCity,
                           String[] intermediateCityStops,
                           int passengersSpace,
                           boolean spaceForLuggage,
                           Date departureDate,
                           String additionalInfo) {
        this.creatorUUID = creatorUUID;
        this.startCity = startCity;
        this.destinationCity = destinationCity;
        this.intermediateCityStops = intermediateCityStops;
        this.passengersSpace = passengersSpace;
        this.spaceForLuggage = spaceForLuggage;
        this.departureDate = departureDate;
        this.additionalInfo = additionalInfo;
    }

    @Ignore
    protected IntercityTravel(Parcel in) {
        creatorUUID = in.readString();
        startCity = in.readString();
        destinationCity = in.readString();
        intermediateCityStops = in.createStringArray();
        passengersSpace = in.readInt();
        spaceForLuggage = in.readByte() != 0;
        additionalInfo = in.readString();
        isHourSelected = in.readByte() != 0;
    }

    public static final Creator<IntercityTravel> CREATOR = new Creator<IntercityTravel>() {
        @Override
        public IntercityTravel createFromParcel(Parcel in) {
            return new IntercityTravel(in);
        }

        @Override
        public IntercityTravel[] newArray(int size) {
            return new IntercityTravel[size];
        }
    };

    public String getCreatorUUID() {
        return creatorUUID;
    }

    public void setCreatorUUID(String creatorUUID) {
        this.creatorUUID = creatorUUID;
    }

    public String getStartCity() {
        return startCity;
    }

    public void setStartCity(String startCity) {
        this.startCity = startCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String[] getIntermediateCityStops() {
        return intermediateCityStops;
    }

    public void setIntermediateCityStops(List<String> intermediateCityStops) {
        this.intermediateCityStops = intermediateCityStops.toArray(new String[0]);
    }

    public int getPassengersSpace() {
        return passengersSpace;
    }

    public void setPassengersSpace(int passengersSpace) {
        this.passengersSpace = passengersSpace;
    }

    public boolean isSpaceForLuggage() {
        return spaceForLuggage;
    }

    public void setSpaceForLuggage(boolean spaceForLuggage) {
        this.spaceForLuggage = spaceForLuggage;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setIsHourSelected(boolean isHourSelected) {
        this.isHourSelected = isHourSelected;
    }

    public boolean isHourSelected() {
        return isHourSelected;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    @Ignore
    public boolean getIsHourSelected() {
        return isHourSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(creatorUUID);
        dest.writeString(startCity);
        dest.writeString(destinationCity);
        dest.writeStringArray(intermediateCityStops);
        dest.writeInt(passengersSpace);
        dest.writeByte((byte) (spaceForLuggage ? 1 : 0));
        dest.writeString(additionalInfo);
        dest.writeByte((byte) (isHourSelected ? 1 : 0));
    }

    public String getTravelID() {
        return travelID;
    }

    public void setTravelID(String travelID) {
        this.travelID = travelID;
    }

    public User getCreator() {
        if (creator == null)
            return new User();
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}

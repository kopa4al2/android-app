package com.example.sharedtravel.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.sharedtravel.firebase.AuthenticationManager;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
@Entity(tableName = "message_sender")
public class MessageSender implements Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "id")
    @NotNull
    private String id;
    @ColumnInfo(name = "profilePicUrl")
    private String profilePicUrl;
    @ColumnInfo(name = "displayName")
    private String displayName;

    //THIS IS ONLY FOR SQLITE DB
    @ColumnInfo(name = "thisUser")
    private String thisUser;

    public MessageSender() {
    }

    @Ignore
    MessageSender(@NotNull String id, String profilePicUrl, String displayName) {
        this.id = id;
        this.profilePicUrl = profilePicUrl;
        this.displayName = displayName;
    }

    @Ignore
    public MessageSender(String id, Uri profilePicUrl, String displayName) {
        this.id = id;
        if (profilePicUrl != null)
            this.profilePicUrl = profilePicUrl.toString();
        this.displayName = displayName;
    }

    protected MessageSender(Parcel in) {
        id = in.readString();
        profilePicUrl = in.readString();
        displayName = in.readString();
    }

    public static final Creator<MessageSender> CREATOR = new Creator<MessageSender>() {
        @Override
        public MessageSender createFromParcel(Parcel in) {
            return new MessageSender(in);
        }

        @Override
        public MessageSender[] newArray(int size) {
            return new MessageSender[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getThisUser() {
        return thisUser;
    }

    public void setThisUser(String thisUser) {
        this.thisUser = thisUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(profilePicUrl);
        dest.writeString(displayName);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof MessageSender)
            return this.getId().equals(((MessageSender) obj).getId());
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new String[]{getId()});
    }
}

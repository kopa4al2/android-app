package com.example.sharedtravel.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

public class Message implements Parcelable {

    private MessageSender sender;
    private String messageContent;
    private Date dateOfCreation;

    public Message() {
    }


    public Message(String message, MessageSender sender) {
        this.dateOfCreation = Calendar.getInstance().getTime();
        this.messageContent = message;
        this.sender = sender;
    }


    protected Message(Parcel in) {
        sender = in.readParcelable(MessageSender.class.getClassLoader());
        messageContent = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(sender, flags);
        dest.writeString(messageContent);
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }
}

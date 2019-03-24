package com.example.sharedtravel.model;

import com.example.sharedtravel.firebase.MessageRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;

public class ModelConverter {
    public static Message convertMapToMessage(Map<String, Object> messageMap) {

        Date dateOfCreation = ((Timestamp) messageMap.get("dateOfCreation")).toDate();
        String messageContent = (String) messageMap.get("messageContent");


        Message message = new Message();
        message.setMessageContent(messageContent);
        message.setDateOfCreation(dateOfCreation);

        return message;
    }

    public static MessageSender converMapToMessageSender(Map<String, Object> senderMap) {
        if (senderMap == null)
            return null;
        MessageSender messageSender = new MessageSender();

        messageSender.setDisplayName((String) senderMap.get("displayName"));
        messageSender.setId((String) senderMap.get("id"));
        messageSender.setProfilePicUrl((String) senderMap.get("profilePicUrl"));

        return messageSender;
    }

    @NotNull
    public static MessageSender convertUserToMessageSender(@NotNull FirebaseUser loggedInUser) {
        String photoUrl = loggedInUser.getPhotoUrl() == null ? null : loggedInUser.getPhotoUrl().toString();
        return new MessageSender(loggedInUser.getUid(), photoUrl, loggedInUser.getDisplayName());
    }
}

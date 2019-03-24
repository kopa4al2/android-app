package com.example.sharedtravel.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.example.sharedtravel.R;
import com.example.sharedtravel.SharedTravelApplication;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.MessageRepository;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.model.ModelConverter;
import com.example.sharedtravel.services.NotificationService;
import com.example.sharedtravel.utils.UserSharedPrefrences;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class NotificationView extends android.support.v7.widget.AppCompatImageView implements View.OnClickListener
        , Observer {

    public static final String USERS_ENGAGED_IN_CHAT_WITH = "users_engaged";

    private static String currentlyEngagedWithChat = null;

    private NotificationListener mListener;

    private boolean hasNewNotifications;

    private SharedPreferences sharedPreferences;

    private static FirebaseUser loggedInFirebaseUser;

    private static final Set<MessageSender> usersWhoSentNewMessage = new HashSet<>();

    public static void stopNotificationsForUser(String userId) {
        currentlyEngagedWithChat = userId;
    }

    public static void resumeNotifications() {
        currentlyEngagedWithChat = null;
    }

    public NotificationView(Context context) {
        super(context);

    }

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(FirebaseUser firebaseUser) {
        loggedInFirebaseUser = firebaseUser;
        if (loggedInFirebaseUser == null) {
            return;
        }

        this.setOnClickListener(this);

        sharedPreferences = UserSharedPrefrences.getPreferencesForUser(getContext(),
                loggedInFirebaseUser.getUid());

        Set<String> serializedMessageSenders =
                sharedPreferences.getStringSet(USERS_ENGAGED_IN_CHAT_WITH, new HashSet<>());
        for (String serializedMessageSender : serializedMessageSenders) {
            MessageSender sender = deserializeMessageSender(serializedMessageSender);
            usersWhoSentNewMessage.add(sender);
        }
        if (!usersWhoSentNewMessage.isEmpty())
            hasNewNotifications = true;
        if (hasNewNotifications)
            changeImageNewNotifications();
    }

    public void setNotificationListener(NotificationListener listener) {
        this.mListener = listener;
    }


    @Override
    public void onClick(View v) {
        if (this.mListener != null) {
            this.mListener.showNotifications(usersWhoSentNewMessage);
        }
    }

    public void seenNotificationFrom(MessageSender messageSender) {
        usersWhoSentNewMessage.remove(messageSender);
        sharedPreferences.edit()
                .putStringSet(USERS_ENGAGED_IN_CHAT_WITH, serializeCollection(usersWhoSentNewMessage))
                .apply();
        if (usersWhoSentNewMessage.isEmpty()) {
            showNotificationsEmpty();
            hasNewNotifications = false;
        }
    }


    @Override
    public synchronized void update(Observable o, Object arg) {
        if (o instanceof AuthenticationManager) {
            loggedInFirebaseUser = AuthenticationManager.getInstance().getLoggedInFirebaseUser();
        }
        if(loggedInFirebaseUser == null)
            return;
        if (o instanceof NotificationService && arg != null) {
            String messageSenderId = (String) ((Map) ((Map) arg).get(MessageRepository.SENDER)).get("id");
            //If the messageSender is the sender the user is currentlyEngagedInChatWith, ignore the update
            if (messageSenderId == null)
                return;
            if (messageSenderId.equals(currentlyEngagedWithChat))
                return;
            if (messageSenderId.equals(loggedInFirebaseUser.getUid()))
                return;
            dealWithMessage((Map) arg);
        }
    }


    private void dealWithMessage(Map arg) {
        if (sharedPreferences == null)
            sharedPreferences = UserSharedPrefrences
                    .getPreferencesForUser(
                            getContext(),
                            loggedInFirebaseUser.getUid());

        int allReceivedMessagesCount = ((List) arg.get(MessageRepository.RECEIVED_MESSAGES)).size();

        if (allReceivedMessagesCount == 0)
            return;

        MessageSender messageSender = ModelConverter
                .converMapToMessageSender((Map<String, Object>) arg.get(MessageRepository.SENDER));


        //Insert every time so it updates if user has changed profile pic or username
        new updateSQLData(messageSender).execute();


        //For every userId -> the list of all messages
        int messagesWithUserSinceLastLogin = sharedPreferences.getInt(messageSender.getId(), 0);


        if (allReceivedMessagesCount > messagesWithUserSinceLastLogin) {
            usersWhoSentNewMessage.add(messageSender);
            this.hasNewNotifications = true;
            changeImageNewNotifications();
        }
        sharedPreferences.edit()
                .putStringSet(USERS_ENGAGED_IN_CHAT_WITH, serializeCollection(usersWhoSentNewMessage))
                .putInt(messageSender.getId(), allReceivedMessagesCount)
                .apply();

    }

    private void showNotificationsEmpty() {
        this.setImageResource(R.drawable.ic_notifications_empty);

    }

    private void changeImageNewNotifications() {
        if (!hasNewNotifications)
            return;
        this.setImageResource(R.drawable.ic_notifications_active);

        this.startAnimation(AnimationUtils
                .loadAnimation(getContext(),
                        R.anim.shake_animation));
    }

    private String serializeMessageSender(MessageSender messageSender) {
        String msg = "";
        msg += messageSender.getId() + ",";
        msg += messageSender.getDisplayName() + ",";
        msg += messageSender.getProfilePicUrl() + ",";
        return msg;
    }

    private MessageSender deserializeMessageSender(String serializedMessageSender) {
        String[] tokens = serializedMessageSender.split(",");
        MessageSender sender = new MessageSender();
        sender.setId(tokens[0]);
        sender.setDisplayName(tokens[1]);
        sender.setProfilePicUrl(tokens[2]);
        return sender;
    }

    private Set<String> serializeCollection(Iterable<MessageSender> usersWhoSentNewMessage) {
        Set<String> outputSet = new HashSet<>();
        for (MessageSender sender : usersWhoSentNewMessage) {
            outputSet.add(serializeMessageSender(sender));
        }
        return outputSet;
    }

    private Set<MessageSender> deserializeCollection(Iterable<String> usersWhoSentNewMessage) {
        Set<MessageSender> outputSet = new HashSet<>();
        for (String sender : usersWhoSentNewMessage) {
            outputSet.add(deserializeMessageSender(sender));
        }
        return outputSet;
    }

    public interface NotificationListener {

        void showNotifications(Set<MessageSender> usersWhoSentNewMessages);

    }

    public static class updateSQLData extends AsyncTask<Void, Void, Void> {


        private final MessageSender messageSender;

        private updateSQLData(MessageSender messageSender) {
            messageSender.setThisUser(loggedInFirebaseUser.getUid());
            this.messageSender = messageSender;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (messageSender == null)
                return null;
            MessageSender sender = SharedTravelApplication.dbSQL.messageSenderDAO().getById(messageSender.getId());
            SharedTravelApplication.dbSQL.messageSenderDAO().insertAll(messageSender);
            return null;
        }
    }

}

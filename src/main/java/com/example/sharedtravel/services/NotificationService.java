package com.example.sharedtravel.services;

import com.example.sharedtravel.firebase.MessageRepository;
import com.example.sharedtravel.firebase.UsersManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

import static com.example.sharedtravel.firebase.MessageRepository.ENGAGED_CHATS_COLLECTION;

public class NotificationService extends Observable {


//    private static final String DATE_OF_LAST_CHANGE = "dateOfLastChange";

    private static NotificationService _this = new NotificationService();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration newMessagesListener;

    private NotificationService() {

    }

    public static void addNewMessageListener(String loggedInUser, long lastSignInTimestamp) {
        if(_this.newMessagesListener != null)
            _this.newMessagesListener.remove();
        //TODO: FIX FUCKING BUG
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(lastSignInTimestamp);
//        Date d = c.getTime();
//        d.setHours(0);
        _this.newMessagesListener = _this.db.collection(UsersManager.USER_COLLECTION)
                .document(loggedInUser)
                .collection(ENGAGED_CHATS_COLLECTION)
                .whereEqualTo(MessageRepository.IS_REMOVED, false)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (querySnapshot != null) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED: {
                                    _this.setChanged();
                                    _this.notifyObservers(dc.getDocument().getData());
                                }
                                case MODIFIED:
                                    _this.setChanged();
                                    _this.notifyObservers(dc.getDocument().getData());
                                    break;
                                default:
                                    return;
                            }
                        }
                    }
                });
    }

    public static NotificationService getInstance() {
        return _this;
    }

    public static void unRegisterListener() {
        _this.newMessagesListener.remove();
    }
}

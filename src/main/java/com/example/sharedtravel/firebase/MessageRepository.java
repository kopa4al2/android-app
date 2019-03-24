package com.example.sharedtravel.firebase;

import android.support.annotation.NonNull;

import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MessageRepository {


    private static final String TAG = MessageRepository.class.getSimpleName() + "_DEBUG";

    public static final String SENT_MESSAGES = "sentMessages";
    public static final String RECEIVED_MESSAGES = "receivedMessages";
    private static final String DATE_OF_CREATION = "dateOfCreation";
    private static final String DATE_OF_LAST_CHANGE = "dateOfLastChange";
    public static final String MESSAGE_CONTENT = "messageContent";
    public static final String SENDER = "sender";
    public static final String ENGAGED_CHATS_COLLECTION = "engagedChats";
    public static final String IS_REMOVED = "isRemoved";


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static MessageRepository _this = new MessageRepository();

    private static final CollectionReference usersCollection = _this.db.collection(UsersManager.USER_COLLECTION);


    private MessageRepository() {
    }


    public static void sendMessage(@NotNull String sender,
                                   @NotNull String receiver,
                                   @NotNull Message message,
                                   AsyncListener callback) {
        callback.onStart();
        Task<Void> updateSenderChat = usersCollection
                .document(sender)
                .collection(ENGAGED_CHATS_COLLECTION)
                .document(receiver)
                .update(SENT_MESSAGES, FieldValue.arrayUnion(getMessageContentAndDate(message)),
                        DATE_OF_LAST_CHANGE, Calendar.getInstance().getTime(),
                        IS_REMOVED, false);

        Task<Void> updateReceiver = usersCollection
                .document(receiver)
                .collection(ENGAGED_CHATS_COLLECTION)
                .document(sender)
                .update(RECEIVED_MESSAGES, FieldValue.arrayUnion(getMessageContentAndDate(message)),
                        IS_REMOVED, false,
                        SENDER, message.getSender(),
                        DATE_OF_LAST_CHANGE, Calendar.getInstance().getTime());
        Tasks.whenAllComplete(updateReceiver, updateSenderChat).addOnCompleteListener((task -> {
            if (task.isSuccessful())
                callback.onSuccess();
            else
                callback.onFail(task.getException());
        }));

    }


    /**
     * @param loggedInUser
     * @param otherUserId
     * @param callback     Will return the DocumentReference of the chatChannel
     */
    public static void getOrCreateChatChannel(String loggedInUser,
                                              String otherUserId,
                                              AsyncListenerWithResult callback) {
        callback.onStart();

        usersCollection
                .document(loggedInUser)
                .collection(ENGAGED_CHATS_COLLECTION)
                .document(otherUserId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                    if (task.getResult().exists())
                        callback.onSuccess(task.getResult());
                    else {
                        Map<String, Object> chatRoom = new HashMap<>();
                        chatRoom.put(SENT_MESSAGES, new ArrayList<Message>());
                        chatRoom.put(RECEIVED_MESSAGES, new ArrayList<Message>());
                        chatRoom.put(SENDER, new HashMap<>());
                        chatRoom.put(DATE_OF_LAST_CHANGE, Calendar.getInstance().getTime());
                        usersCollection
                                .document(otherUserId)
                                .collection(ENGAGED_CHATS_COLLECTION)
                                .document(loggedInUser)
                                .set(chatRoom);

                        DocumentReference chatRoomDoc = usersCollection.document(loggedInUser)
                                .collection(ENGAGED_CHATS_COLLECTION)
                                .document(otherUserId);

                        chatRoomDoc.set(chatRoom);
                        chatRoomDoc.get().addOnSuccessListener(callback::onSuccess);
                    }
                else
                    callback.onFail(task.getException());
            }
        });
    }

    //Logged in user wants to delete chat with other user
    public static void deleteChat(String loggedInUser, String otherUser, AsyncListener callback) {
        callback.onStart();
        usersCollection
                .document(loggedInUser)
                .collection(ENGAGED_CHATS_COLLECTION)
                .document(otherUser)
                .update(IS_REMOVED, true, SENT_MESSAGES, new ArrayList<>(), RECEIVED_MESSAGES, new ArrayList<>())
                .addOnCompleteListener((task -> {
                    if (task.isSuccessful())
                        callback.onSuccess();
                    else
                        callback.onFail(task.getException());
                }));
    }

    private static Map<String, Object> getMessageContentAndDate(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE_CONTENT, message.getMessageContent());
        map.put(DATE_OF_CREATION, message.getDateOfCreation());
        return map;
    }

//      this is currently handled by SQLite
//    public static Query getAllChatChannels(String userId) {
//
//        return usersCollection
//                .document(userId)
//                .collection(ENGAGED_CHATS_COLLECTION)
//                .orderBy(DATE_OF_LAST_CHANGE, Query.Direction.ASCENDING);
//    }
}

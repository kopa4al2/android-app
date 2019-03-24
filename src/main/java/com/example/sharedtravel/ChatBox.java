package com.example.sharedtravel;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.MessageRepository;
import com.example.sharedtravel.firebase.utils.DocumentSnapshotConverter;
import com.example.sharedtravel.model.Message;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.utils.ActivityStarter;
import com.example.sharedtravel.utils.DateFormatUtils;
import com.example.sharedtravel.views.NotificationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.sharedtravel.SingleUserActivity.USER_EXTRA;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatBox extends Fragment {

    public static final String TAG = "chat_box_tag";

    public static final String RECEIVER_USER = "receiverUser";
    public static final String RECEIVER_USER_DISPLAY_NAME = "displayName";

    private RecyclerView chatBox;
    private ChatBoxAdapter adapter;

    private List<Message> messages;
    private FirebaseUser loggedInUser;
    private String receiverUserId;

    private ChatBoxListener mListener;
    private ListenerRegistration newChatMessagesListener;


    public ChatBox() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_box, container, false);
        messages = new ArrayList<>();
        if (getArguments() != null
                && getArguments().getString(RECEIVER_USER) != null) {
            this.loggedInUser = AuthenticationManager.getInstance().getLoggedInFirebaseUser();
            this.receiverUserId = getArguments().getString(RECEIVER_USER);
        } else {
            //"Can not open chat channel without users engaged"
            return null;
        }
        MessageRepository.getOrCreateChatChannel(loggedInUser.getUid(), receiverUserId, (chatChannel) -> {

            attachNewMessageListener(((DocumentSnapshot) chatChannel).getReference());
            NotificationView.stopNotificationsForUser(receiverUserId);
            messages = DocumentSnapshotConverter.convertToMessage((DocumentSnapshot) chatChannel);
            chatBox = v.findViewById(R.id.rv_chat_box);
            adapter = new ChatBoxAdapter(messages);
            chatBox.setAdapter(adapter);
            LinearLayoutManager layoutManager = new SnappingLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            chatBox.setLayoutManager(layoutManager);
            if (messages.size() > 0) {
                chatBox.smoothScrollToPosition(adapter.getItemCount() - 1);
            }

            //Set the text view's text to the user's first word of the username
            TextView topBarUsername = v.findViewById(R.id.chat_top_bar_username);
            topBarUsername.setText(
                    getArguments().getString(RECEIVER_USER_DISPLAY_NAME).split(" ")[0]);

            if(getActivity() instanceof MainActivity) {
                //If its in the main activity signal the user he can click on the username
                topBarUsername.setPaintFlags(topBarUsername.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
            topBarUsername.setOnClickListener((view) -> {

                //If its in the main activity, open the user profile and close the chat box, else do nothing
                if(getActivity() instanceof MainActivity && !receiverUserId.equals(loggedInUser.getUid())) {
                    ((MainActivity)getActivity()).detachChatBox();
                } else if(getActivity() instanceof SingleUserActivity) {
                    return;
                }
                Intent intent = new Intent(getContext(), SingleUserActivity.class);
                intent.putExtra(USER_EXTRA, receiverUserId);
                ActivityStarter.startActivity(getContext(), intent);
            });

            v.findViewById(R.id.btn_close_chat).setOnClickListener((btnView) -> {
                if (this.mListener != null) {
                    this.mListener.detachChatBox();
                }
            });


            Toast toast = Toast.makeText(getContext(),
                    getString(R.string.chat_messages_are_deleted_on_scheluded_time)
                    , Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, (v.getTop() + v.getHeight()));
            toast.show();
        });

        //SEND MESSAGE
        v.findViewById(R.id.btn_chat_box_send_message).setOnClickListener((btn) -> {
            String messageText;
            EditText messageEditText = v.findViewById(R.id.et_chat_box);
            if (messageEditText.getText().toString().isEmpty())
                return;
            messageText = messageEditText.getText().toString().trim();
            messageEditText.setText("");

            MessageSender sender = new MessageSender(loggedInUser.getUid(),
                    loggedInUser.getPhotoUrl(),
                    loggedInUser.getDisplayName());
            Message message = new Message(messageText, sender);
            MessageRepository.sendMessage(loggedInUser.getUid(), receiverUserId, message, () -> {
            });

        });
        return v;
    }

    private void attachNewMessageListener(DocumentReference chatChannel) {
        newChatMessagesListener = chatChannel.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null)
                Log.d(TAG, "onEvent: ERROR SNAPSHOT LISTENER" + e.getMessage());
            if (documentSnapshot != null) {
                newChatMessage(documentSnapshot.getData());
            }

        });
    }

    private void detachNewMessageListener() {
        if (newChatMessagesListener != null)
            newChatMessagesListener.remove();
    }

    private void newChatMessage(Map<String, Object> documentData) {
        List<Message> newMessages = DocumentSnapshotConverter
                .convertToMessage(documentData);

        if (newMessages.size() > 0)
            for (int i = messages.size(); i < newMessages.size(); i++) {
                messages.add(newMessages.get(i));
                adapter.notifyItemChanged(adapter.getItemCount() - 1);
                chatBox.smoothScrollToPosition(adapter.getItemCount() - 1);
            }

    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof ChatBoxListener)
            this.mListener = (ChatBoxListener) context;
        else {
            throw new IllegalStateException(context.getClass().getSimpleName() + "Must implement ChatBoxListener");
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        detachNewMessageListener();
        NotificationView.resumeNotifications();
        this.mListener = null;
        super.onDetach();
    }

    private class ChatBoxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int SENT_MESSAGE_VIEW = 0;
        private static final int RECEIVED_MESSAGE_VIEW = 1;

        private List<Message> messages;

        ChatBoxAdapter(List<Message> messages) {
            this.messages = messages;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            if (i == SENT_MESSAGE_VIEW) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.chat_box_sent_message, viewGroup, false);
                return new SentMessageViewHolder(v);
            } else {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.chat_box_received_message, viewGroup, false);
                return new ReceivedMessageViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder chatBoxViewHolder, int i) {
            Message message = messages.get(i);

            switch (chatBoxViewHolder.getItemViewType()) {
                case SENT_MESSAGE_VIEW:
                    ((SentMessageViewHolder) chatBoxViewHolder).bind(message);
                    break;
                case RECEIVED_MESSAGE_VIEW:
                    ((ReceivedMessageViewHolder) chatBoxViewHolder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (messages.get(position).getSender().getId()
                    .equals(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid()))
                return SENT_MESSAGE_VIEW;
            return RECEIVED_MESSAGE_VIEW;

        }
    }

    private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profilePic;
        private TextView chatMessage;
        //        private TextView senderName;
        private TextView sentDate;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.chat_box_profile_pic);
            chatMessage = itemView.findViewById(R.id.chat_box_text);
            sentDate = itemView.findViewById(R.id.chat_box_date);
//            senderName = itemView.findViewById(R.id.chat_box_sender_name);
        }

        void bind(Message message) {

            Picasso.get().load(message.getSender().getProfilePicUrl()).into(profilePic);

            chatMessage.setText(message.getMessageContent());
            sentDate.setText(DateFormatUtils.dateToHourString(message.getDateOfCreation()));
//            senderName.setText(message.getSender().getDisplayName());
        }
    }

    private class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePic;
        private TextView messageText;
        private TextView timeSent;

        SentMessageViewHolder(View v) {
            super(v);
            profilePic = v.findViewById(R.id.received_message_profile_picture);
            messageText = v.findViewById(R.id.chat_box_sent_message);
            timeSent = v.findViewById(R.id.chat_box_sent_message_date);
        }

        void bind(Message message) {
            messageText.setText(message.getMessageContent());
            timeSent.setText(DateFormatUtils.dateToHourString(message.getDateOfCreation()));

            profilePic.setVisibility(View.VISIBLE);
            Picasso.get().load(loggedInUser.getPhotoUrl()).into(profilePic);

        }
    }

    private class SnappingLinearLayoutManager extends LinearLayoutManager {

        SnappingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                           int position) {
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class TopSnappedSmoothScroller extends LinearSmoothScroller {
            TopSnappedSmoothScroller(Context context) {
                super(context);

            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SnappingLinearLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }
    }

    public interface ChatBoxListener {
        void detachChatBox();
    }


    public static void scrollToBottom(ScrollView container, View view) {
        //After 300 ms because thats the time of the animation
        new Handler().postDelayed(() -> container.smoothScrollTo(0, view.getBottom()), 300);
    }


}

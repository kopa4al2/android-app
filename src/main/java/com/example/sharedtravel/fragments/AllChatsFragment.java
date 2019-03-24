package com.example.sharedtravel.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sharedtravel.MainActivity;
import com.example.sharedtravel.R;
import com.example.sharedtravel.SharedTravelApplication;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.MessageRepository;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.utils.UserSharedPrefrences;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;


public class AllChatsFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    public static final String TAG = "all_chats_fragment";

    private static final int USER_ID = 0;
    private static final int USERNAME = 1;


    RecyclerView allChatsRecyclerView;
    AllChatsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        new getAllChatsWithUser(this).execute();
        View view = inflater.inflate(R.layout.my_chats_fragment, container, false);
        allChatsRecyclerView = view.findViewById(R.id.rv_my_chats);
        allChatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AllChatsAdapter();


        return view;
    }

    @Override
    public void onClick(View v) {
        String otherUserId = ((MessageSender) v.getTag()).getId();
        String username = ((MessageSender) v.getTag()).getDisplayName();
        ((MainActivity) getActivity()).openChatBox(otherUserId, username);

    }

    @Override
    public boolean onLongClick(View v) {
        DialogBuilder.showConfirmationDialog(getContext(), getString(R.string.confirm_delete_chat), () -> {
            MessageRepository.deleteChat(
                    AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid(),
                    ((MessageSender) v.getTag()).getId(),
                    () -> {
                        new deleteChatWithUser(AllChatsFragment.this, (MessageSender) v.getTag()).execute();
                        DialogBuilder.showSuccessDialog(getContext(), getString(R.string.successfully_deleted_chat));
                    });
        });
        return true;
    }

    public static class deleteChatWithUser extends AsyncTask<Void, Void, Void> {
        WeakReference<AllChatsFragment> fragmentWeakReference;
        MessageSender messageSender;

        deleteChatWithUser(AllChatsFragment fragmentWeakReference, MessageSender messageSender) {
            this.fragmentWeakReference = new WeakReference<>(fragmentWeakReference);
            this.messageSender = messageSender;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences preferences = UserSharedPrefrences
                    .getPreferencesForUser(fragmentWeakReference.get().getContext(),
                            AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());
                    preferences.edit()
                    .putInt(messageSender.getId(), 0)
                    .apply();
            SharedTravelApplication.dbSQL.messageSenderDAO().delete(messageSender);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (fragmentWeakReference.get() != null) {
                new getAllChatsWithUser(fragmentWeakReference.get()).execute();
            }
            super.onPostExecute(aVoid);
        }
    }

    public static class getAllChatsWithUser extends AsyncTask<Void, List<MessageSender>, List<MessageSender>> {
        WeakReference<AllChatsFragment> fragmentWeakRef;

        getAllChatsWithUser(AllChatsFragment fragment) {
            fragmentWeakRef = new WeakReference<>(fragment);
        }

        @Override
        protected List<MessageSender> doInBackground(Void... voids) {
            return SharedTravelApplication.dbSQL.messageSenderDAO()
                    .getAllForThisUser(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());
        }

        @Override
        protected void onPostExecute(List<MessageSender> senders) {
            if (fragmentWeakRef.get() != null) {
                AllChatsFragment fragment = fragmentWeakRef.get();
                fragment.adapter.setAllChats(senders);
                fragment.allChatsRecyclerView.setAdapter(fragment.adapter);
            }
            super.onPostExecute(senders);
        }
    }

    class AllChatsAdapter extends RecyclerView.Adapter<AllChatsAdapter.ChatViewHolder> {

        private List<MessageSender> usersInChatWith;

        void setAllChats(List<MessageSender> usersInChatWith) {
            this.usersInChatWith = usersInChatWith;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.my_chats_rv_row, viewGroup, false);
            return new ChatViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return usersInChatWith.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
            chatViewHolder.bindData(usersInChatWith.get(i));
        }

        private class ChatViewHolder extends RecyclerView.ViewHolder {

            private CardView container;
            private ImageView profilePic;
            private TextView userName;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.single_chat_card_view_container);
                profilePic = itemView.findViewById(R.id.my_chat_row_user_pic);
                userName = itemView.findViewById(R.id.my_chat_row_user_name);
            }

            void bindData(MessageSender messageSender) {
                if (messageSender.getProfilePicUrl() == null || messageSender.getProfilePicUrl().isEmpty())
                    Picasso.get().load(R.mipmap.blank_profile_picture).into(profilePic);
                else
                    Picasso.get().load(messageSender.getProfilePicUrl()).fit().into(profilePic);
                userName.setText(messageSender.getDisplayName());
                userName.setTag(messageSender.getId());
                container.setTag(messageSender);
                container.setOnClickListener(AllChatsFragment.this);
                container.setOnLongClickListener(AllChatsFragment.this);
            }
        }
    }

}

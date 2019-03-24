package com.example.sharedtravel.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.model.MessageSender;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.NotificationViewHolder> {

    private List<MessageSender> messageSenders;
    private Context context;
    private View.OnClickListener notificationClickListener;

    public NotificationsRecyclerAdapter(Context context,
                                        List<MessageSender> messageSenders,
                                        View.OnClickListener notificationClickListener) {
        this.messageSenders = messageSenders;
        this.context = context;
        this.notificationClickListener = notificationClickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.notification_row, viewGroup, false);

        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder notificationViewHolder, int i) {
        notificationViewHolder.bindView(messageSenders.get(i), notificationClickListener);
    }

    @Override
    public int getItemCount() {
        return this.messageSenders.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView senderName;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.notification_sender_profile_picture);
            senderName = itemView.findViewById(R.id.notification_sender_button);
        }

        void bindView(MessageSender sender, View.OnClickListener onNotificationClickListener) {
            if (sender.getProfilePicUrl() == null || sender.getProfilePicUrl().isEmpty() || sender.getProfilePicUrl().equals("null"))
                Picasso.get().load(R.mipmap.blank_profile_picture).into(profileImageView);
            else
                Picasso.get().load(sender.getProfilePicUrl()).into(profileImageView);
            //Set the tag so i can access it when opening the chat associated with the sender
            senderName.setTag(sender);
            senderName.setText(sender.getDisplayName().split(" ")[0]);
            senderName.setOnClickListener(onNotificationClickListener);
        }
    }
}

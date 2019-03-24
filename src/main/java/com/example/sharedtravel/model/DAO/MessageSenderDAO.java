package com.example.sharedtravel.model.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.MessageSender;

import java.util.List;

@Dao
public interface MessageSenderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MessageSender... senders);

    @Query("SELECT * FROM message_sender")
    List<MessageSender> getAll();

    @Query("SELECT * FROM message_sender WHERE thisUser IN (:id)")
    List<MessageSender>  getAllForThisUser(String id);

    @Query("SELECT * FROM message_sender WHERE id IN (:id)")
    MessageSender getById(String id);

    @Delete
    void delete(MessageSender messageSender);
}

package com.example.sharedtravel.model.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.sharedtravel.model.IntercityTravel;

import java.util.List;

@Dao
public interface IntercityTravelDAO {
    @Query("SELECT * FROM travels")
    List<IntercityTravel> getAll();

    @Query("SELECT * FROM travels WHERE creatorID IN (:creatorId)")
    List<IntercityTravel> getByCreator(String creatorId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(IntercityTravel... travels);

    @Delete
    void delete(IntercityTravel travel);

}

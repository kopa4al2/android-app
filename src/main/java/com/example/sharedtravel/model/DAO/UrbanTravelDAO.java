package com.example.sharedtravel.model.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.UrbanTravel;

import java.util.List;

@Dao
public interface UrbanTravelDAO {

    @Query("SELECT * FROM urban_travels WHERE creatorId IN (:creatorId)")
    List<UrbanTravel> getByCreator(String creatorId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UrbanTravel... travels);

    @Delete
    void delete(UrbanTravel travel);
}

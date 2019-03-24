package com.example.sharedtravel.localDB;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import com.example.sharedtravel.model.DAO.IntercityTravelDAO;
import com.example.sharedtravel.model.DAO.MessageSenderDAO;
import com.example.sharedtravel.model.DAO.UrbanTravelDAO;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.Message;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.model.Review;
import com.example.sharedtravel.model.UrbanTravel;

import java.util.Date;

@Database(entities = {IntercityTravel.class, MessageSender.class}, version = 2)
@TypeConverters(SQLiteDB.Converters.class)
public abstract class SQLiteDB extends RoomDatabase {
    public abstract IntercityTravelDAO intercityTravelDAO();

    public abstract MessageSenderDAO messageSenderDAO();

//    public abstract UrbanTravelDAO urbanTravelDAO();

//    public abstract MessageDAO MessageDAO();

//    public abstract ReviewDAO ReviewDAO();

    public static class Converters {
        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }

        @TypeConverter
        public static String convertArrayToString(String[] array){
            StringBuilder str = new StringBuilder(array.length);
            for (int i = 0;i<array.length; i++) {
                str.append(array[i]);
                // Do not append comma at the end of last element
                if(i<array.length-1){
                    str.append('_');
                }
            }
            return str.toString();
        }
        @TypeConverter
        public static String[] convertStringToArray(String str){
            return str.split("_");
        }
    }
}

package com.marty.georemainder.dbutilities;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Marty on 4/16/2018.
 */
@Database(entities = {GeoAlarm.class},version = 1)
public abstract class AppDB extends RoomDatabase {
    private static AppDB INSTANCE;

    public abstract GeoAlarmDao geoAlarmDao();

    public static AppDB getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AppDB.class,"test_alarm.db").build();
                }
            }
        }
        return INSTANCE;
    }
}

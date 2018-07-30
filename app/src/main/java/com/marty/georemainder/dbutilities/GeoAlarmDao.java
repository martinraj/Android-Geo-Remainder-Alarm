package com.marty.georemainder.dbutilities;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Marty on 4/16/2018.
 */
@Dao
public interface GeoAlarmDao {
    @Query("SELECT * FROM geo_alarms ORDER BY _id")
    LiveData<List<GeoAlarm>> getAllAlarms();

    @Query("SELECT * FROM geo_alarms ORDER BY _id")
    List<GeoAlarm> getAllAlarmsCheck();

    @Query("SELECT * FROM geo_alarms WHERE is_triggered =:state AND is_active =:astate ORDER BY _id")
    List<GeoAlarm> getAllActiveAlarms(boolean state,boolean astate);

    @Query("SELECT * FROM geo_alarms WHERE _id = :alarmId")
    GeoAlarm getAlarmById(int alarmId);

    @Query("UPDATE geo_alarms SET is_active =:state WHERE _id = :alarmId")
    int updateAlarmState(boolean state,int alarmId);

    @Query("UPDATE geo_alarms SET is_triggered =:state WHERE _id = :alarmId")
    void updateAlarmTrigger(boolean state,int alarmId);

    @Update(onConflict = IGNORE)
    void updateAlarm(GeoAlarm geoAlarm);

    @Query("DELETE FROM geo_alarms")
    void deleteAll();

    @Insert(onConflict = REPLACE)
    void addAlarm(GeoAlarm geoAlarm);
}

package com.marty.georemainder.dbutilities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Marty on 4/16/2018.
 */
@Entity(tableName = "geo_alarms",indices = {@Index(value = {"_id"},unique = true)})
public class GeoAlarm {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id")
    private int alarmId;

    @ColumnInfo(name = "alarm_description")
    private String alarmNote;

    @ColumnInfo(name = "is_active")
    private boolean isActive = true;

    @TypeConverters(DateConverter.class) @ColumnInfo(name = "alarm_time")
    private Date alarmTime;

    @ColumnInfo(name = "geo_meters")
    private int meters;

    @TypeConverters(LatLngConverter.class) @ColumnInfo(name ="alarm_location")
    private LatLng location;

    @ColumnInfo(name = "is_triggered")
    private boolean isTriggered = false;

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public String getAlarmNote() {
        return alarmNote;
    }

    public void setAlarmNote(String alarmNote) {
        this.alarmNote = alarmNote;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public int getMeters() {
        return meters;
    }

    public void setMeters(int meters) {
        this.meters = meters;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean isTriggered() {
        return isTriggered;
    }

    public void setTriggered(boolean triggered) {
        isTriggered = triggered;
    }
}

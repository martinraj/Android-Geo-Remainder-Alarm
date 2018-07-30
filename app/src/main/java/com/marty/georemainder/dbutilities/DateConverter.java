package com.marty.georemainder.dbutilities;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Marty on 4/16/2018.
 */

class DateConverter {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        if(value == null){
            return null;
        }
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        Date d = new Date(value + TimeZone.getTimeZone(c.getTimeZone().getID()).getOffset(value));
        return d;
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if(date == null ){
            return null;
        }
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        Date d = new Date(date.getTime() - TimeZone.getTimeZone(c.getTimeZone().getID()).getOffset(date.getTime()));
        return d.getTime();
    }
}

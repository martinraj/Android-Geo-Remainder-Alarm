package com.marty.georemainder.dbutilities;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Created by Marty on 4/16/2018.
 */

class LatLngConverter {
    @TypeConverter
    public static String fromLocation(LatLng latlng) {
        if (latlng==null) {
            return(null);
        }
        return(String.format(Locale.US, "%f,%f", latlng.latitude,
                latlng.longitude));
    }

    @TypeConverter
    public static LatLng toLocation(String latlng) {
        if (latlng==null) {
            return(null);
        }

        String[] pieces=latlng.split(",");
        LatLng result=new LatLng(Double.parseDouble(pieces[0]),Double.parseDouble(pieces[1]));

        return(result);
    }
}

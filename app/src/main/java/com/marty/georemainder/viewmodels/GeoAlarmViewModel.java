package com.marty.georemainder.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.marty.georemainder.dbutilities.AppDB;
import com.marty.georemainder.dbutilities.GeoAlarm;

import java.util.List;

/**
 * Created by Marty on 4/18/2018.
 */

public class GeoAlarmViewModel extends AndroidViewModel {

    private AppDB db;
    private LiveData<List<GeoAlarm>> geoAlarmList;

    public GeoAlarmViewModel(@NonNull Application application) {
        super(application);
        subscribeToAlarmChanges();
    }

    private void subscribeToAlarmChanges() {
        db = AppDB.getDatabase(this.getApplication());
        geoAlarmList = db.geoAlarmDao().getAllAlarms();
    }

    public LiveData<List<GeoAlarm>> getGeoAlarmList(){
        return geoAlarmList;
    }

}

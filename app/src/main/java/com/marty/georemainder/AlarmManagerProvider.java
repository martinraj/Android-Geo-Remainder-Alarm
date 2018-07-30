package com.marty.georemainder;

import android.app.AlarmManager;
import android.content.Context;

/**
 * Created by Marty on 4/18/2018.
 */

public class AlarmManagerProvider {

    private static AlarmManager sAlarmManager;

    /*package*/ static synchronized AlarmManager getAlarmManager(Context context) {
        if (sAlarmManager == null) {
            sAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return sAlarmManager;
    }
}

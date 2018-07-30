package com.marty.georemainder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Marty on 4/19/2018.
 */

public class BootAlarmSchedule extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("BootAlarmSchedule", "scheduling...");
            setAlarm(context);
        }
    }

    private void setAlarm(Context context) {
//        if(getGeoPendingIntent(getApplicationContext(), PendingIntent.FLAG_NO_CREATE) != null) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager((context));
        PendingIntent operation = getGeoPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+1000, 600 * 1000, operation); //repeat every 5 mins
        Log.d("elasped time",SystemClock.elapsedRealtime()+1000+"");
//        }else{
//            Log.w(TAG,"already alarm is set ");
//        }
    }
    private PendingIntent getGeoPendingIntent(Context context, int flag) {
        Intent action = new Intent(context, NotificationReceiver.class);
        action.setAction("GEO_ALARM_CHECK");
        return PendingIntent.getBroadcast(context, 0, action, flag);
    }
}

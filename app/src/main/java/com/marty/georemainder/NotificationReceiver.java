package com.marty.georemainder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by Marty on 4/18/2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = NotificationReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("broadcast","received");
        if(intent!=null && intent.getAction()!=null && intent.getAction().equals("GEO_ALARM_CHECK")) {
            if(!GeoAlarmService.isRunning) {
                Intent i = new Intent(context, GeoAlarmService.class);
                i.setAction(intent.getAction());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    context.startForegroundService(i);
                }else {
                    context.startService(i);
                }
            }else{
                Log.e("location checking"," ongoing");
            }
        }else{
            Log.e(TAG, "invalid Broadcast Received");
        }
    }
}

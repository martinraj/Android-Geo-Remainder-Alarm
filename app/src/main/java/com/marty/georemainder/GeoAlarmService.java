package com.marty.georemainder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.marty.georemainder.dbutilities.AppDB;
import com.marty.georemainder.dbutilities.GeoAlarm;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by Marty on 4/18/2018.
 */

public class GeoAlarmService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Handler handler;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    GoogleApiClient mGoogleApiClient;
    public static boolean isRunning = false;
    NotificationManager notificationManager;
    Thread trigger;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            createNotification(true);
            return;
        }
        createNotification(false);
    }

    private void config() {
        try {
            trigger = new Thread(new Runnable() {
                @Override
                public void run() {
                    List<GeoAlarm> alarmList = AppDB.getDatabase(getApplicationContext()).geoAlarmDao().getAllAlarmsCheck();
                    final List<GeoAlarm> activeAlarm = new ArrayList<>();
                    for(GeoAlarm ga:alarmList){
                        if(ga.isActive()){
                            activeAlarm.add(ga);
                        }
                    }
                    if (activeAlarm.size() > 0){
                        Looper.prepare();
                        handler = new Handler();
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(2000);
                        mLocationRequest.setNumUpdates(1);
                        mLocationRequest.setMaxWaitTime(10000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {

                                    if (location != null) {
                                        checkGeoCoordinates(location, activeAlarm);
                                    } else {
                                        Log.e("for loop loc", "null");
                                    }

                                }
                            }

                        };
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback,
                                null /* Looper */);
                        Looper.loop();
                    }else{
                        Log.e("no data","to check");
                        stopLocationUpdates();
                    }
                }
            }, "trackThread");
            trigger.start();
        }catch (Exception e){
            e.printStackTrace();
            stopLocationUpdates();
        }
    }

    private void checkGeoCoordinates(Location location, List<GeoAlarm> alarmList) {
        ArrayList<Integer> alarmIds = new ArrayList<>();
        Location alarmLocation = new Location("");
        for (GeoAlarm geoAlarm : alarmList) {
            alarmLocation.setLongitude(geoAlarm.getLocation().longitude);
            alarmLocation.setLatitude(geoAlarm.getLocation().latitude);
            Log.d("distance",""+alarmLocation.distanceTo(location));
            float distance = alarmLocation.distanceTo(location);
            if(distance<=geoAlarm.getMeters()){
                alarmIds.add(geoAlarm.getAlarmId());
            }
        }
        if(alarmIds.size()>0) {
            Intent i = new Intent(this, AlarmActivity.class);
            i.setAction("GEO_ALERT");
            i.putExtra("ids",alarmIds);
            startActivity(i);
        }
        stopLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.getAction() != null && intent.getAction().equalsIgnoreCase("GEO_ALARM_CHECK")) {
                buildGoogleApiClient();
                isRunning = true;
                config();
                return START_NOT_STICKY;
            }
        }
        return START_NOT_STICKY;
    }

    private void buildGoogleApiClient() {
        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }else{
            if(!mGoogleApiClient.isConnected()){
                mGoogleApiClient.connect();
            }
            Log.e("api client","not null");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        onStopService();
        Log.e("service","destroyed");
    }

    public PendingIntent getPendingIntent(boolean isNeedPerm) {
        if(isNeedPerm) {
            return null;
        }else{
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return PendingIntent.getActivity(getBaseContext(), 0, intent, FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("inTrac.JobService","google API client connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("inTrac.JobService","google API client suspended");
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("inTrac.JobService","google API client connection failed");
    }

    private void createNotification(boolean isPermissionNotify) {
        Notification.Builder mBuilder = new Notification.Builder(
                getBaseContext());
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = mBuilder.setSmallIcon((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.drawable.ic_notification : R.mipmap.ic_launcher).setTicker("Geo Check").setWhen(1)
                    .setAutoCancel(false)
                    .setCategory("Alarm")
                    .setContentTitle(isPermissionNotify ? getString(R.string.loc_perm_required) : getString(R.string.tracking_title))
                    .setContentText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking))
                    .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark))
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking)))
                    .setChannelId("loc_alerts")
                    .setContentIntent(getPendingIntent(isPermissionNotify))
                    .setShowWhen(true)
                    .setOngoing(true)
                    .build();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification = mBuilder.setSmallIcon( R.drawable.ic_notification ).setTicker("Geo Check").setWhen(1)
                        .setAutoCancel(false)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentTitle(isPermissionNotify ? getString(R.string.loc_perm_required) : getString(R.string.tracking_title))
                        .setContentText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking))
                        .setSound(null)
                        .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark))
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking)))
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setShowWhen(true)
                        .setOngoing(true)
                        .setContentIntent(getPendingIntent(isPermissionNotify))
                        .build();
            }else{
                notification = mBuilder.setSmallIcon( R.drawable.ic_notification ).setTicker("Geo Check").setWhen(1)
                        .setAutoCancel(false)
                        .setContentTitle(isPermissionNotify ? getString(R.string.loc_perm_required) : getString(R.string.tracking_title))
                        .setContentText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking))
                        .setSound(null)
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(isPermissionNotify ? getString(R.string.loc_perm_guide) : getString(R.string.tracking)))
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setShowWhen(true)
                        .setOngoing(true)
                        .setContentIntent(getPendingIntent(isPermissionNotify))
                        .build();
            }
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel("loc_alerts", "Alarm", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(mChannel);
                startForeground(101, notification);
            }else{
                notificationManager.notify(101, notification);
            }
        }

    }

    private void removeNotification(){
        if (notificationManager != null) {
            notificationManager.cancel(101);
        }
    }

    private void stopLocationUpdates() {

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        Log.d("stop location "," updates called");
        if(mLocationCallback!=null && mFusedLocationProviderClient!=null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
//            Toast.makeText(getApplicationContext(), "Location job service finished.", Toast.LENGTH_SHORT).show();
        }
        /*Intent jobStoppedMessage = new Intent(JOB_STATE_CHANGED);
        jobStoppedMessage.putExtra("isStarted",false);
        Log.d("broadcasted","job state change");*/
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        isRunning = false;
        removeNotification();
        stopSelf();
        /*LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(jobStoppedMessage);*/
    }
}

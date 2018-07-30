package com.marty.georemainder;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.marty.georemainder.dbutilities.AppDB;
import com.marty.georemainder.dbutilities.GeoAlarm;

import java.util.ArrayList;

/**
 * Created by Marty on 4/18/2018.
 */

public class AlarmActivity extends Activity {
    ArrayList<Integer> ids = new ArrayList<>();
    TextView description;
    Handler handler;
    Ringtone r;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.alarm_layout);
        Bundle b = getIntent().getExtras();
        if(b!=null){
            ids = b.getIntegerArrayList("ids");
            changeStatus(ids);
        }
        handler = new Handler();
        description = (TextView) findViewById(R.id.t_description);
        handler.post(getDescription);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                    "geo_alarm_wakelock");
            wakeLock.acquire(60000);
        }
    }

    private void playTone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Thread getDescription = new Thread(new Runnable() {
        @Override
        public void run() {
            if(ids.size()>0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final GeoAlarm ga = AppDB.getDatabase(getApplicationContext()).geoAlarmDao().getAlarmById(ids.get(0));
                        if(ga != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    description.setText(ga.getAlarmNote());
                                    playTone();
                                }
                            });
                        }
                    }
                }).start();
            }else{
                if(wakeLock!=null){
                    wakeLock.release();
                }
                finish();
            }
        }
    });

    private void changeStatus(final ArrayList<Integer> ids) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int id:ids){
                    AppDB.getDatabase(getApplicationContext()).geoAlarmDao().updateAlarmTrigger(true,id);
                }
            }
        }).start();
    }

    public void onQuitClicked(View view) {
        ids.remove(0);
        stopPlay();
        handler.post(getDescription);
    }

    private void stopPlay() {
        if(r!=null){
            if(r.isPlaying()){
                r.stop();
            }
        }
    }
}

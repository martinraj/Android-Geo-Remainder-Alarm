package com.marty.georemainder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.marty.georemainder.viewmodels.GeoAlarmViewModel;
import com.marty.georemainder.adapters.GeoAlarmAdapter;
import com.marty.georemainder.dbutilities.GeoAlarm;

import java.util.ArrayList;
import java.util.List;

import static android.widget.LinearLayout.VERTICAL;

public class MainActivity extends AppCompatActivity {

    GeoAlarmViewModel geoAlarmModel;
    GeoAlarmAdapter geoAlarmAdapter;
    RecyclerView alarmList;
    TextView tNoAlarm;
    private static final String TAG = MainActivity.class.getSimpleName();
    Observer<List<GeoAlarm>> alarmObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.b_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getPackageName()+".ADD_ALARM");
                startActivity(i);
            }
        });
        alarmList = (RecyclerView)findViewById(R.id.alarm_list);
        tNoAlarm = (TextView) findViewById(R.id.t_no_alarms);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        alarmList.setLayoutManager(mLayoutManager);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, VERTICAL);
        alarmList.addItemDecoration(itemDecor);
        geoAlarmModel = ViewModelProviders.of(MainActivity.this).get(GeoAlarmViewModel.class);
        alarmObserver = new Observer<List<GeoAlarm>>() {
            @Override
            public void onChanged(@Nullable List<GeoAlarm> geoAlarms) {
                if(geoAlarms == null) {
                    Log.e("list ","null");
                    geoAlarms = new ArrayList<>();
                }
                Log.e("geoAlarms length",geoAlarms.size()+" ----");
                if(geoAlarmAdapter==null) {
                    geoAlarmAdapter = new GeoAlarmAdapter(getBaseContext(),geoAlarms);
                    alarmList.setAdapter(geoAlarmAdapter);
                    geoAlarmAdapter.notifyDataSetChanged();
                }else{
                    geoAlarmAdapter.setData(geoAlarms);
                    geoAlarmAdapter.notifyDataSetChanged();
                }
                tNoAlarm.setVisibility(geoAlarms.size()==0?View.VISIBLE:View.INVISIBLE);
            }
        };
        subscribeToAlarmData();
        setAlarm();

    }

    private void setAlarm() {
//        if(getGeoPendingIntent(getApplicationContext(), PendingIntent.FLAG_NO_CREATE) != null) {
            AlarmManager manager = AlarmManagerProvider.getAlarmManager(getApplicationContext());
            PendingIntent operation = getGeoPendingIntent(getApplicationContext(), PendingIntent.FLAG_CANCEL_CURRENT);
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

    private void subscribeToAlarmData() {
        geoAlarmModel.getGeoAlarmList().observe(this, alarmObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

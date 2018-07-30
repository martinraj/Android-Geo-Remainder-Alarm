package com.marty.georemainder.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marty.georemainder.R;
import com.marty.georemainder.dbutilities.AppDB;
import com.marty.georemainder.dbutilities.GeoAlarm;

import java.util.List;

/**
 * Created by Marty on 4/18/2018.
 */

public class GeoAlarmAdapter extends RecyclerView.Adapter<GeoAlarmAdapter.RecyclerViewHolders> {
    private static final String TAG = GeoAlarmAdapter.class.getSimpleName();
    private List<GeoAlarm> geoAlarmList;
    private Context context;

    public GeoAlarmAdapter(Context baseContext, List<GeoAlarm> geoAlarms) {
        this.geoAlarmList = geoAlarms;
        this.context = baseContext;
    }

    @NonNull
    @Override
    public GeoAlarmAdapter.RecyclerViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_list_item, null);
        return new RecyclerViewHolders(layoutView);
    }


    @Override
    public void onBindViewHolder(@NonNull GeoAlarmAdapter.RecyclerViewHolders holder, int position) {
        holder.tTitle.setText("Alarm "+geoAlarmList.get(position).getAlarmId());
        holder.tDescription.setText(geoAlarmList.get(position).getAlarmNote());
        holder.bSwitch.setChecked(geoAlarmList.get(position).isActive());
    }

    @Override
    public int getItemCount() {
        return geoAlarmList.size();
    }

    public void setData(List<GeoAlarm> data) {
        this.geoAlarmList = data;
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        private SwitchCompat bSwitch;
        private TextView tTitle,tDescription;
        private RelativeLayout rlWhole;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            bSwitch = (SwitchCompat) itemView.findViewById(R.id.b_switch);
            tTitle = (TextView) itemView.findViewById(R.id.t_title);
            tDescription = (TextView) itemView.findViewById(R.id.t_description);
            rlWhole = (RelativeLayout) itemView.findViewById(R.id.rl_whole);
            bSwitch.setOnCheckedChangeListener(this);
            bSwitch.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.b_switch){
                final SwitchCompat sc = (SwitchCompat) view;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = AppDB.getDatabase(context).geoAlarmDao().updateAlarmState(sc.isChecked(),geoAlarmList.get(getAdapterPosition()).getAlarmId());
                        if(i == -1){
                            Log.e(TAG,"Error while updating alarm !!");
                        }
                    }
                }).start();
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppDB.getDatabase(context).geoAlarmDao().updateAlarmState(b,geoAlarmList.get(getAdapterPosition()).getAlarmId());
                }
            }).start();
        }
    }
}

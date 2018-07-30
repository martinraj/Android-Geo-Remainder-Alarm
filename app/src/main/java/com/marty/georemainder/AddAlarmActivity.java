package com.marty.georemainder;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.marty.georemainder.dbutilities.AppDB;
import com.marty.georemainder.dbutilities.GeoAlarm;

/**
 * Created by Marty on 4/17/2018.
 */

public class AddAlarmActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int DEFAULT_METERS = 100; //in meters
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    TextInputEditText eDescription;
    SupportMapFragment mapFragment;
    Handler handler;
    GoogleMap gMap;
    AppCompatImageView iTarget,iMarker;
    Animator.AnimatorListener markerAnimListener;
    ProgressBar mapLoadProgress;
    GoogleApiClient mGoogleApiClient;
    Intent placeIntent;
    TextView tSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        mapLoadProgress = (ProgressBar) findViewById(R.id.map_progress);
        mapLoadProgress.setVisibility(View.VISIBLE);
        handler = new Handler();
        NonUIThread nt = new NonUIThread();
        handler.post(nt);
        buildGoogleApiClient();
    }

    public void onSetAlarmClicked(View view) {
        if(gMap!=null) {
            final LatLng centerLatLng = gMap.getCameraPosition().target;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GeoAlarm ga = new GeoAlarm();
                    ga.setActive(true);
                    ga.setAlarmNote(eDescription.getText().toString().trim());
                    ga.setLocation(centerLatLng);
                    ga.setMeters(DEFAULT_METERS);
                    ga.setTriggered(false);
                    AppDB.getDatabase(getBaseContext()).geoAlarmDao().addAlarm(ga);
                    finish();
                    /*Log.d("i value",i+"");
                    if(i != -1){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Alarm added successfully",Toast.LENGTH_SHORT).show();
                            }
                        });
                        finish();
                    }else {
                        showSnackBar("Error while creating alarm");
                    }*/
                }
            }).start();
        }else{
            showSnackBar("Location required !!");
        }
    }

    public void onSearchLayoutClicked(View view) {
        if(placeIntent == null) {
            try {
                placeIntent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(this);
                startActivityForResult(placeIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }else {
            startActivityForResult(placeIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        }

    }

    private class NonUIThread implements Runnable {
        @Override
        public void run() {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.map_layout, mapFragment).commitAllowingStateLoss();
            iTarget = (AppCompatImageView) findViewById(R.id.i_target);
            iMarker = (AppCompatImageView) findViewById(R.id.i_marker);
            tSearch = (TextView) findViewById(R.id.t_address);
            eDescription = (TextInputEditText) findViewById(R.id.e_description);
            markerAnimListener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    iTarget.setVisibility(View.INVISIBLE);
                    iMarker.clearAnimation();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            };
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    gMap = googleMap;
                    changeMapStyle(gMap,getApplicationContext());
                    gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            mapLoadProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                    gMap.getUiSettings().setCompassEnabled(false);
                    gMap.getUiSettings().setTiltGesturesEnabled(false);
                    gMap.getUiSettings().setScrollGesturesEnabled(true);
                    gMap.getUiSettings().setZoomGesturesEnabled(true);
                    gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                        @Override
                        public void onCameraMoveStarted(int i) {
                            if(iMarker.getVisibility()==View.VISIBLE) {
                                iMarker.animate().translationY(-50).setListener(null);
                                iTarget.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            if(iMarker.getVisibility()==View.VISIBLE) {
                                iMarker.animate().translationY(0).setListener(markerAnimListener);
                            }
                        }
                    });
                }
            });
        }

        private void changeMapStyle(GoogleMap mMap, Context context) {
            try {

                if(mMap!=null){

                    boolean success = mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    context, R.raw.map_theme_dark));

                    if (!success) {
                        Log.e("Map", "Style parsing failed.");
                    }
                }
            } catch (Resources.NotFoundException e) {
                Log.e("Map", "Can't find style. Error: ", e);
            }
        }
    }

    private void showSnackBar(String text) {
        try {
            Snackbar.make(findViewById(R.id.coordinator), text, Snackbar.LENGTH_LONG)
                    .setAction("", null).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }else{
            Log.e("api client","not null");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            if (placeIntent == null) {
                placeIntent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(this);
            }
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.d("Place: ", ""+place.getName());
                tSearch.setText(place.getAddress());
                CameraUpdate point = CameraUpdateFactory.newLatLngZoom(place.getLatLng(),16);
                if(gMap!=null) {
                    gMap.animateCamera(point);
                }
            }
        }

    }
}

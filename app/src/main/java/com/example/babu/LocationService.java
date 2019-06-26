package com.example.babu;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static com.example.babu.MainActivity.activePlaylistBeforeGPSMode;

public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    public static double distance = 0;
    public static double speed;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        distance = 0;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
        updateUI();
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }

    //The live feed of Distance and Speed are being set in the method below .
    private void updateUI() {
        if (MainActivity.p == 0) {
            if(currentTrainingTab.endSession.getVisibility() == View.INVISIBLE && MainActivity.isGPSmodeActive){
                currentTrainingTab.warning.setText("Session Started");
                currentTrainingTab.endSession.setVisibility(View.VISIBLE);
                currentTrainingTab.c1.setVisibility(View.VISIBLE);
                currentTrainingTab.c2.setVisibility(View.VISIBLE);
                currentTrainingTab.c3.setVisibility(View.VISIBLE);
                currentTrainingTab.c4.setVisibility(View.VISIBLE);
                currentTrainingTab.c5.setVisibility(View.VISIBLE);
                currentTrainingTab.c6.setVisibility(View.VISIBLE);
                currentTrainingTab.dist.setVisibility(View.VISIBLE);
                currentTrainingTab.time.setVisibility(View.VISIBLE);
                currentTrainingTab.speed.setVisibility(View.VISIBLE);
                currentTrainingTab.maxheartRate.setVisibility(View.VISIBLE);
                currentTrainingTab.heartRate.setVisibility(View.VISIBLE);
                currentTrainingTab.topSpeed.setVisibility(View.VISIBLE);
            }

            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            currentTrainingTab.time.setText(""+diff);
            if (speed > 0.0)
                currentTrainingTab.speed.setText(new DecimalFormat("#.##").format(speed));
            else
                currentTrainingTab.speed.setText("0");

            currentTrainingTab.dist.setText(new DecimalFormat("#.##").format(distance));
            lStart = lEnd;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        AudioPlayer.pauseSong();
        ArrayAdapter<String> songListAdapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_list_item_1, MainActivity.CurrentPlaylist.songNames
        );
        FragmentSongs.songListView.setAdapter(songListAdapter);
        FragmentSongs.songListView.invalidateViews();
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}
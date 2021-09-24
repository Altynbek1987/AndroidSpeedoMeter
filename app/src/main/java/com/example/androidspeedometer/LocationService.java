package com.example.androidspeedometer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;

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

    @SuppressLint("RestrictedApi")
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

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (mGoogleApiClient, mLocationRequest,this);
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

        //Вызов метода ниже обновляет текущие значения расстояния и скорости для TextViews.
        updateUI();
        //вычисляя скорость с помощью метода getSpeed, он возвращает скорость в м/с, поэтому мы конвертируем ее в км/ч
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
    //Прямая трансляция расстояния и скорости настраивается по методу ниже .
    @SuppressLint("SetTextI18n")
    private void updateUI() {
        if (MainActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            MainActivity.time.setText("Общее время: " + diff + " минут");
            if (speed > 0.0)
                MainActivity.speed.setText("Текущая скорость: " + new DecimalFormat("#.##").format(speed) + " км / ч");
            else
                MainActivity.speed.setText(".......");

            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " Расстояние");
            lStart = lEnd;
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}

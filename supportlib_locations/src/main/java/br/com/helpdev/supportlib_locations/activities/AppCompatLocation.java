package br.com.helpdev.supportlib_locations.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import br.com.helpdev.supportlib_locations.locations.LocationUtils;

/**
 * Created by Guilherme Biff Zarelli on 25/11/16.
 */

public abstract class AppCompatLocation extends AppCompatActivity implements LocationUtils.ConnectionCallback {

    private static final int REQUEST_ERRO_PLAY_SERVICES = 3334;
    private static final int REQUEST_ENABLE_GPS = 4333;

    private static LocationUtils locationUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState || locationUtils == null) {
            checkLocationUtils();
            if (locationUtils != null && locationUtils.getGoogleApiClient().isConnected()) {
                onConnectedLocation();
            }
        }
    }

    @Override
    public void onConnected() {
        checkLocationUtils();
        onConnectedLocation();
    }

    public void checkLocationUtils() {
        if (null == locationUtils) {
            initLocationUtils();
        } else if (locationUtils.getStatus() == LocationUtils.EnumServiceStatus.SUCCESS) {
            LocationUtils.checkAndEnableGps(locationUtils, AppCompatLocation.this, REQUEST_ENABLE_GPS);
        }
    }

    private void initLocationUtils() {
        locationUtils = LocationUtils.init(this, this);
    }

    @Override
    public void onConnectFailed(ConnectionResult connectionResult) {
        try {
            LocationUtils.resolveConnectionResult(this, connectionResult, REQUEST_ERRO_PLAY_SERVICES);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        } finally {
            locationUtils = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_GPS && resultCode == RESULT_CANCELED) {
            checkLocationUtils();
        } else if (requestCode == REQUEST_ERRO_PLAY_SERVICES) {
            initLocationUtils();
        }
    }

    public void stopLocationRequest(LocationListener locationListener) {
        checkLocationUtils();
        locationUtils.stopLocationRequest(locationListener);
    }

    public void startLocationRequest(LocationRequest locationRequest, LocationListener locationListener) throws SecurityException {
        checkLocationUtils();
        locationUtils.startLocationRequest(locationRequest, locationListener);
    }

    public void startLocationRequest(LocationListener locationListener) throws SecurityException {
        checkLocationUtils();
        locationUtils.startLocationRequest(locationListener);
    }

    public Location getLastLocation() throws SecurityException {
        checkLocationUtils();
        return locationUtils.getLastLocation();
    }

    public void disconnect() {
        if (null == locationUtils) return;
        locationUtils.disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        checkLocationUtils();
        return locationUtils.getGoogleApiClient();
    }

    public abstract void onConnectedLocation();
}

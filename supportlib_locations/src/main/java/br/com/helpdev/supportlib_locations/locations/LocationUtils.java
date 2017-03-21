package br.com.helpdev.supportlib_locations.locations;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.RuntimeExecutionException;

/**
 * Created by Guilherme Biff Zarelli on 16/11/16.
 */

public class LocationUtils implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public enum EnumServiceStatus {
        LOADING("LOADING"), SUCCESS("SUCCESS"), ERROR("ERROR");
        String mensagem;

        EnumServiceStatus(String mensagem) {
            this.mensagem = mensagem;
        }
    }

    public interface ConnectionCallback {
        void onConnected();

        /**
         * Use resolveConnectionResult()
         *
         * @param connectionResult
         */
        void onConnectFailed(ConnectionResult connectionResult);
    }

    public static LocationUtils init(Context context) {
        return init(context, null);
    }

    public static LocationUtils init(Context context, ConnectionCallback connectionCallback) {
        LocationUtils serviceLocation = new LocationUtils();
        serviceLocation.configure(context, connectionCallback);
        return serviceLocation;
    }


    /**
     * * verify result in onActivityResult:
     * Sample:
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     * super.onActivityResult(requestCode, resultCode, data);
     * if (requestCode == MY_REQUEST_CODE) {
     * LocationUtils.init
     * }
     */
    public static void resolveConnectionResult(Activity activity, ConnectionResult connectionResult, int requestCode) throws IntentSender.SendIntentException {
        if (connectionResult.hasResolution()) {
            connectionResult.startResolutionForResult(activity, requestCode);
        } else {
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult.getErrorCode(), activity, null, requestCode, null);
        }
    }

    /**
     * verify result in onActivityResult:
     * Sample:
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     * super.onActivityResult(requestCode, resultCode, data);
     * if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_CANCELED && locationUtils != null) {
     * locationUtils.checkAndEnableGps(MainActivityAbs.this, true, 1);
     * }
     * }
     */
    public static void checkAndEnableGps(final LocationUtils locationUtils, final Activity activity, final int requestCode) {
        locationUtils.checkStatusThrowable();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.setNeedBle(true);
        builder.addLocationRequest(locationUtils.mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(locationUtils.mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(activity, requestCode);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private volatile EnumServiceStatus status;
    private ConnectionCallback connectionCallback;

    protected LocationUtils() {
    }

    protected void configure(Context context, ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
        status = EnumServiceStatus.LOADING;

        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mGoogleApiClient.isConnected()) {
            onConnected(null);
        } else {
            mGoogleApiClient.connect();
        }

        if (null == mLocationRequest) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setFastestInterval(1000);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        status = EnumServiceStatus.ERROR;
        status.mensagem = connectionResult.getErrorMessage();
        if (connectionCallback != null) {
            connectionCallback.onConnectFailed(connectionResult);
            connectionCallback = null;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        status = EnumServiceStatus.SUCCESS;
        if (null != connectionCallback) {
            connectionCallback.onConnected();
            connectionCallback = null;
        }
    }


    public void startLocationRequest(LocationListener locationListener) throws SecurityException {
        startLocationRequest(mLocationRequest, locationListener);
    }

    public void startLocationRequest(LocationRequest locationRequest, LocationListener locationListener) throws SecurityException {
        this.mLocationRequest = locationRequest;
        checkStatusThrowable();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, locationListener);
    }

    public void stopLocationRequest(LocationListener locationListener) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public EnumServiceStatus getStatus() {
        return status;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @RequiresPermission(
            anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}
    )
    public Location getLastLocation() throws SecurityException {
        checkStatusThrowable();
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    protected void checkStatusThrowable() throws SecurityException {
        if (status != EnumServiceStatus.SUCCESS) {
            throw new RuntimeExecutionException(new Throwable("Ilegal status. Check if getStatus==SUCCESS"));
        }
    }

    public void disconnect() {
        try {
            if (null != mGoogleApiClient) mGoogleApiClient.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public LocationRequest getLocationRequest() {
        return mLocationRequest;
    }

    public void setLocationRequest(LocationRequest mLocationRequest) {
        this.mLocationRequest = mLocationRequest;
    }
}

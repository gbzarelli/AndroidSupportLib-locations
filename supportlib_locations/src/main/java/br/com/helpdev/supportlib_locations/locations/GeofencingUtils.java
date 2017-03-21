package br.com.helpdev.supportlib_locations.locations;

import android.app.PendingIntent;
import android.content.Context;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * Created by Guilherme Biff Zarelli on 24/11/16.
 */

public class GeofencingUtils extends LocationUtils {

    public static GeofencingUtils init(Context context) {
        return init(context, null);
    }

    public static GeofencingUtils init(Context context, ConnectionCallback connectionCallback) {
        GeofencingUtils serviceLocation = new GeofencingUtils();
        serviceLocation.configure(context, connectionCallback);
        return serviceLocation;
    }

    private GeofencingUtils() {
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public void addGeofences(List<Geofence> list, PendingIntent pendingIntent) throws SecurityException {
        addGeofences(list, null, pendingIntent);
    }

    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    public void addGeofences(List<Geofence> list, GeofencingRequest.Builder builderFencing, PendingIntent pendingIntent) throws SecurityException {
        checkStatusThrowable();
        if (null == builderFencing) {
            builderFencing = new GeofencingRequest.Builder();
        }
        builderFencing.addGeofences(list);
        LocationServices.GeofencingApi.addGeofences(getGoogleApiClient(), builderFencing.build(), pendingIntent);
    }

    public void removeGeofences(PendingIntent pendingIntent) {
        checkStatusThrowable();
        LocationServices.GeofencingApi.removeGeofences(getGoogleApiClient(), pendingIntent);
    }
}

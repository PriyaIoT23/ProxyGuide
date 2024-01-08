package com.stockholmiot.proxyguide.ui.map;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeofenceHelper extends ContextWrapper {

    private static final String TAG = "GeofenceHelper";
    PendingIntent pendingIntent;
    private Context context;
    public GeofenceHelper(Context base) {
        super(base);
        this.context = base;
    }

    public GeofencingRequest getGeofencingRequest(Geofence geofence){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.addGeofence(geofence);
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        return builder.build();
    }

    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionType){

        return  new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionType)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(){
        if(pendingIntent !=null){
            return  pendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.putExtra("place", "Gamla Stan");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE );
        return pendingIntent;

    }

    public String getErrorString(Exception e){
        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;

            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return  e.getLocalizedMessage();
    }
}

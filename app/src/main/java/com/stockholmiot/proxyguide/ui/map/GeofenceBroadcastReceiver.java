package com.stockholmiot.proxyguide.ui.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stockholmiot.proxyguide.ui.home.models.PoIModel;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofencing event or event has error");
            return;
        }
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        String geofenceId = "";
        for (Geofence geofence : geofenceList) {
            // Identify the specific geofence based on its ID or other properties
           geofenceId = geofence.getRequestId();
        }

        Log.d(TAG, geofenceId);

        Location location = geofencingEvent.getTriggeringLocation();
        //location.getLon
        double latitudeGeofence = location.getLatitude();
        double longitudeGeofence = location.getLongitude();
        GeoPoint geofencePoint = new GeoPoint(latitudeGeofence, longitudeGeofence);
        int transitionType = geofencingEvent.getGeofenceTransition();

        /*FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference poiRef = db.collection("PoI");
        poiRef.whereEqualTo("latitude", geofencePoint);
        poiRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "Succesfully getting documents: ");
                        PoIModel content = document.toObject(PoIModel.class);

                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        String locationName = content.getName();
                        Log.d(TAG, content.getName());

                        switch (transitionType){
                            case Geofence.GEOFENCE_TRANSITION_ENTER:
                                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                                //Log.d(TAG, "GeofenceBroadcastReceiver");
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "You're close to "+locationName, MapsFragment.class);
                                break;
                            case Geofence.GEOFENCE_TRANSITION_DWELL:
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "You're DWELL into "+locationName, MapsFragment.class);
                                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "GeofenceBroadcastReceiver");
                                break;
                            case Geofence.GEOFENCE_TRANSITION_EXIT:
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "You're EXIT to "+locationName, MapsFragment.class);
                                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "GeofenceBroadcastReceiver");
                                break;
                        }
                        return;
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

            }
        });*/

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference poiRef = db.collection("PoI").document(geofenceId);
        poiRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        PoIModel content = document.toObject(PoIModel.class);
                        // Get the name of the POI
                        String locationName = content.getName();

                        // Determine the action based on the geofence transition type
                        switch (transitionType) {
                            case Geofence.GEOFENCE_TRANSITION_ENTER:
                                // Send a notification for entering the geofence area
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "You're nearby "+locationName, MapsFragment.class);
                                break;
                            case Geofence.GEOFENCE_TRANSITION_DWELL:
                                // Send a notification for entering and dwelling within the geofence area
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "You're nearby "+locationName, MapsFragment.class);
                                break;
                            case Geofence.GEOFENCE_TRANSITION_EXIT:
                                // Send a notification for exiting the geofence area
                                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "You've left "+locationName, MapsFragment.class);
                                break;
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



    }
}
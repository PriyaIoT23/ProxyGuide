package com.stockholmiot.proxyguide.ui.map;

import static com.stockholmiot.proxyguide.ui.home.models.PoIModel.ADDRESS;
import static com.stockholmiot.proxyguide.ui.home.models.PoIModel.TITTLE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MapsFragment extends Fragment implements OnMapReadyCallback {


    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private static final float GEOFENCE_RADIUS = 100;
    private static final int YOUR_PERMISSION_REQUEST_CODE = 2;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private FragmentActivity myContext;
    private GoogleMap mMap;
    private final  String TAG = MapsFragment.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private MainActivity mainActivity;

    private GeofenceHelper geofenceHelper;
    private GeofencingClient geofencingClient;

    public MapsFragment() {

    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_maps, container, false);

        geofenceHelper = new GeofenceHelper(myContext);
        geofencingClient = LocationServices.getGeofencingClient(myContext);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mainActivity = (MainActivity) getActivity();
        FloatingActionButton fab = mainActivity.findViewById(R.id.fab);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_track_changes_24));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                } else {
                    //mMap.setMyLocationEnabled(true);
                    requestPermissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }

            }
        });


        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(myContext,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }

        return root;
    }


    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        FirebaseFirestore.getInstance().collection("PoI")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                showData(document, mMap);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //ContextCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            //mMap.setMyLocationEnabled(true);
            requestPermissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }


    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(myContext,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }
    }

    private void showData(QueryDocumentSnapshot dataSnapshot, GoogleMap mMap) {

        GeoPoint geoPoint = (GeoPoint) dataSnapshot.getData().get("latitude");
        double latitude = geoPoint.getLatitude();
        double longitude = geoPoint.getLongitude();
        LatLng geoPt = new LatLng(latitude, longitude);
        String title = dataSnapshot.getData().get(TITTLE).toString();
        String address = dataSnapshot.getData().get(ADDRESS).toString();
        String pointOfInterestId = dataSnapshot.getId();
        addMarker(geoPt, title, address);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            addGeofence(pointOfInterestId, geoPt, GEOFENCE_RADIUS);
        } else {
            //mMap.setMyLocationEnabled(true);
            requestPermissionBGLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

    }


    private void addGeofence(String Id, LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(Id, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(myContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: Geofence Added...");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessage =  geofenceHelper.getErrorString(e);
                            Log.d(TAG, errorMessage);
                            Log.d(TAG, "Failled to add : Geofence...");
                        }
                    });
        }else{
            ActivityCompat.requestPermissions(myContext, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

    }



    private void addMarker(LatLng latLng, String title, String address){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title + " - " + address);
        mMap.addMarker(markerOptions);

        addCircle(latLng, GEOFENCE_RADIUS);

        //float zoom = 15;
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(64,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
        float zoom = 15;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode,permissions, grantResults);

        Log.d(TAG, "calling enableMyLocation function..");
        // Check if location permissions are granted and if so enable the
        // location data layer.

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "calling enableMyLocation function..");
                enableMyLocation();

                //checkBackground();
            }
        }else if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                //Toast.makeText(myContext, "You can add geofences...", Toast.LENGTH_SHORT).show();
                //checkNotification();
                enableMyLocation();
            } else {
                //We do not have the permission..
                Toast.makeText(myContext, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == YOUR_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with creating the notification
                //createNotification();`
                //enableMyLocation();
                //checkLocationPermission();
                checkBackground();
                //enableMyLocation();

            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }

    }

    private void checkBackground() {
        Log.d(TAG, "Background permission is calling");

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            //mMap.setMyLocationEnabled(true);
            requestPermissionBGLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

    }

    private void checkNotification() {

        if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permission
            ActivityCompat.requestPermissions(myContext, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, YOUR_PERMISSION_REQUEST_CODE);
            //return;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(myContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }else{
            enableMyLocation();
        }
    }
    private ActivityResultLauncher<String> requestPermissionBGLocationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            Log.d(TAG, "calling BG location function works");
            enableMyLocation();
            //onMapReady(mMap);
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    });

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Log.d(TAG, "calling function works");
                    //enableMyLocation();
                    checkBackground();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private ActivityResultLauncher<String> requestPermissionNotificationLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            Log.d(TAG, "calling notification function works");
            enableMyLocation();
            //onMapReady(mMap);
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    });



}
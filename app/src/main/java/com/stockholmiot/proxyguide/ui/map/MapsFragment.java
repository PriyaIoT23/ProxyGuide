package com.stockholmiot.proxyguide.ui.map;

import static com.stockholmiot.proxyguide.ui.home.models.PoIModel.ADDRESS;
import static com.stockholmiot.proxyguide.ui.home.models.PoIModel.TITTLE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;

import org.jetbrains.annotations.NotNull;

public class MapsFragment extends Fragment implements OnMapReadyCallback {


    private FragmentActivity myContext;
    private GoogleMap mMap;
    private final  String TAG = MapsFragment.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String HOUSING_APARTMENT_TYPE = "Appartement";
    private static final String HOUSING_STUDIO_TYPE = "Studio";
    private static final String HOUSING_HOME_TYPE = "Maison";
    private static final String HOUSING_OTHER_TYPE = "Autre";
    private MainActivity mainActivity;

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

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mainActivity = (MainActivity) getActivity();
        FloatingActionButton fab = mainActivity.findViewById(R.id.fab);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_track_changes_24));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableMyLocation();
            }
        });

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

        enableMyLocation();

    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(myContext, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void showData(QueryDocumentSnapshot dataSnapshot, GoogleMap mMap) {

        GeoPoint geoPoint = (GeoPoint) dataSnapshot.getData().get("latitude");
        double latitude = geoPoint.getLatitude();
        double longitude = geoPoint.getLongitude();
        LatLng geoPt = new LatLng(latitude, longitude);
        String title = dataSnapshot.getData().get(TITTLE).toString();
        String address = dataSnapshot.getData().get(ADDRESS).toString();

        mMap.addMarker(new MarkerOptions().position(geoPt)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title + " - " + address));

        float zoom = 12;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoPt, zoom));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }
}
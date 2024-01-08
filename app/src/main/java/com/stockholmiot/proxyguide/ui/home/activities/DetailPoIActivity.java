package com.stockholmiot.proxyguide.ui.home.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.home.adapters.PictureAdapter;
import com.stockholmiot.proxyguide.ui.home.models.Picture;
import com.stockholmiot.proxyguide.ui.home.models.PoIModel;
import com.stockholmiot.proxyguide.ui.map.MapsActivity;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailPoIActivity extends AppCompatActivity {

    public static final String KEY_POI_ID = "key_poi_id";
    private static final String TAG = DetailPoIActivity.class.getSimpleName();
    private FirebaseFirestore mFirestore;
    private DocumentReference mPoIRef;
    private ProgressDialog loadingBar;

    private TextView poiName;
    private TextView poiDescription;
    private TextView address;
    private ImageView addPicture;
    private MaterialButton interestButton;

    private PictureAdapter pictureAdapter;
    private RecyclerView mDetailsRecycler, pictureRecycler;
    private TextView toolbarTitle;

    private String pOIId, clientPaidStatus;
    private String postedByUid;
    private TextView interestInfo;
    private DatabaseReference contactsRef;
    private MaterialButton showInMapBtn;
    private LinearLayout ownerInfoContainer;
    private MainActivity mainActivity;
    private View ownerInfoView;
    private CoordinatorLayout container;
    private TextView showImages;
    private TextView messageInterest;

    List<Picture> pictures;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_po_iactivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        try {
            pictureRecycler = findViewById(R.id.recycler_pictures);

            container = findViewById(R.id.container);
            contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
            //poiName = findViewById(R.id.ads_description);
            poiDescription = findViewById(R.id.post_description_id);
            address = findViewById(R.id.ads_address);
            showInMapBtn = findViewById(R.id.show_in_map);
            addPicture = findViewById(R.id.add_principal_picture);
            showImages = findViewById(R.id.show_all_images);
            //messageInterest = findViewById(R.id.message_for_interest);

            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
            setSupportActionBar(toolbar);


            getSupportActionBar().setDisplayShowHomeEnabled(true);

            AppBarLayout appBarLayout = findViewById(R.id.app_bar);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {

                        //collapsingToolbarLayout.setTitle("Details Ads");
                        toolbarTitle.setText(getString(R.string.detail_poi));
                        isShow = false;
                    } else if (!isShow) {
                        collapsingToolbarLayout.setTitle(" ");
                        toolbarTitle.setText("");
                        isShow = true;
                    }
                }
            });


            // Get restaurant ID from extras
            pOIId = getIntent().getExtras().getString(KEY_POI_ID);
            if (pOIId == null) {
                throw new IllegalArgumentException("Must pass extra " + KEY_POI_ID);
            }

            // Initialize Firestore
            mFirestore = FirebaseUtil.getFirestore();
            // Get reference to the restaurant
            mPoIRef = mFirestore.collection("PoI").document(pOIId);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            pictureRecycler.setLayoutManager(layoutManager);

            pictures = new ArrayList<>();
            mPoIRef.collection("albums")
                    .limit(6)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                            pictureAdapter = new PictureAdapter(pictures, DetailPoIActivity.this);
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Picture picture = document.toObject(Picture.class);
                                    pictures.add(picture);
                                    pictureAdapter.setPicture(pictures);
                                    pictureRecycler.setAdapter(pictureAdapter);
                                }
                            } else {
                                pictureRecycler.setVisibility(View.GONE);
                                showImages.setVisibility(View.GONE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    });

            mPoIRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.w(TAG, "point of interest:onEvent", error);
                        return;
                    }
                    PoIModel adsModel = value.toObject(PoIModel.class);
                    adsModel.setPoiUid(value.getId());
                    onAdsLoaded(adsModel);
                }
            });



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void onAdsLoaded(PoIModel adsModel) {

        try {

            //poiName.setText(adsModel.getName());
            poiDescription.setText(adsModel.getDescription());
            address.setText(adsModel.getAddress());

            showInMapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   Intent intent = new Intent(DetailPoIActivity.this, MapsActivity.class);
                    intent.putExtra("ADS_EXTRA", adsModel.getPoiUid());
                    startActivity(intent);

                }
            });

            showImages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String adsId = adsModel.getPoiUid();
                    Intent intent = new Intent(DetailPoIActivity.this, MainActivity.class);
                    intent.putExtra("ADS_EXTRA", adsModel.getPoiUid());
                    startActivity(intent);
                }
            });

            if (adsModel.getPhoto_url() != null) {
                try {
                    Glide.with(addPicture.getContext())
                            .load(adsModel.getPhoto_url())
                            .into(addPicture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error: " + e.getMessage());
        }

    }
}
package com.stockholmiot.proxyguide.ui.nearby;

import static com.stockholmiot.proxyguide.ui.home.FilterDialogFragment.TAG;
import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.animation.Animator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.home.adapters.ContactAdapter;
import com.stockholmiot.proxyguide.ui.home.adapters.PoIAdapter;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class ContactActivity extends ConnectionsActivity implements ContactAdapter.OnContactSelectedListener{



    private static final String SERVICE_ID =  "com.stockholmiot.nearby.SERVICE_ID";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private String mName;
    private State mState = State.UNKNOWN;


    private TextView mPreviousStateView;

    /** Displays the current state. */
    private TextView mCurrentStateView;

    /** An animator that controls the animation from previous state to current state. */
    @Nullable
    private Animator mCurrentAnimator;

    /** A running log of debug messages. Only visible when DEBUG=true. */
    private TextView mDebugLogView;

    private FirebaseFirestore mFirestore;
    private DocumentReference mAdsRef;
    private Query mQuery;
    private static final int LIMIT = 50;
    private ArrayList<String> userIDs;

    private ConnectionsClient mConnectionsClient;
    private Map<String, Endpoint> mDiscoveredEndpoints;
    private String mServiceId;
    private boolean mIsDiscovering;
    private ContactAdapter mAdapter;

    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mName = generateRandomName();
        //((TextView) findViewById(R.id.name)).setText(mName);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Create the RecyclerView and its adapter
        mRecyclerView = findViewById(R.id.providerList);
        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseUtil.getFirestore();
        userIDs = new ArrayList<>();

        FirebaseFirestore.setLoggingEnabled(true);
        //mFirestore = FirebaseUtil.getFirestore();
        initRecyclerView();


    }

    private void initRecyclerView() {

        String currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();
        ArrayList<String> listIDs = new ArrayList();
        listIDs.add(currentUserId);

        mQuery = mFirestore.collection("users")
                .whereEqualTo("isNearby", true)
                .whereNotIn("uid", Arrays.asList(currentUserId))
                .orderBy("username", Query.Direction.DESCENDING)
                .limit(LIMIT);
        // Initialize the adapter
        mAdapter = new ContactAdapter(this, mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    //mRecyclerView.setVisibility(View.GONE);
                    //mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    showAdsDataView();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    //mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                Log.d(TAG, "Error occured: " + e.getMessage());
            }
        };

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Connect the adapter to the RecyclerView
        mRecyclerView.setAdapter(mAdapter);
    }



    private void showAdsDataView() {
        /* First, hide the loading indicator */
        //mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setState(State.SEARCHING);
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        //initRecyclerView();
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private static String generateRandomName() {
        String name = "";
       /* Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }*/
        name = FirebaseUtil.getAuth().getCurrentUser().getUid();
        return name;
    }

    private void setState(State state) {
        if (mState == state) {
            logW("State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        State oldState = mState;
        mState = state;
        onStateChanged(oldState, state);
    }


    private State getState() {
        return mState;
    }

    @Override
    public void onContactSelected(DocumentSnapshot ads) {
        Toast.makeText(this, "working...", Toast.LENGTH_SHORT).show();
    }

    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }



    private void onStateChanged(State oldState, State newState) {
        // Update Nearby Connections to the new state.
        switch (newState) {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                startAdvertising();
                break;
            case CONNECTED:
                stopDiscovering();
                stopAdvertising();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;
            default:
                // no-op
                break;
        }

    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        super.onEndpointDiscovered(endpoint);
        logD("Endpoint discovered in ContactActivity: " + endpoint);
        logD("Endpoint discovered in ContactActivity ID: " + getmDiscoveredList());

        // Get the latest list of discovered IDs
        ArrayList<String> getIDs = getmDiscoveredList();

        if (mAdapter != null) {
            // Create a new query based on the updated ID
            String currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();
            mQuery = mFirestore.collection("users")
                    .whereEqualTo("isNearby", true)
                    .whereNotIn("uid", Arrays.asList(currentUserId))
                    .orderBy("username", Query.Direction.DESCENDING)
                    .limit(LIMIT);

            // Set the new query for the adapter
            mAdapter.setQuery(mQuery);

            // Notify the adapter that the data has changed
            mAdapter.notifyDataSetChanged();
        }


    }


    @Override
    public boolean onSupportNavigateUp() {
        //onBackPressed();
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //startAdvertising();
        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();

        FirebaseFirestore mFirestore = FirebaseUtil.getFirestore();
        CollectionReference users = mFirestore.collection("users");
        DocumentReference docRef = mFirestore.collection("users").document(currentUser.getUid());
        docRef.update("isNearby", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(Constants.TAG, "Success update nearby status: ");
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAdvertising();

    }
}
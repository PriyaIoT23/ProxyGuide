package com.stockholmiot.proxyguide.ui.home;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.databinding.FragmentHomeBinding;
import com.stockholmiot.proxyguide.ui.home.activities.DetailPoIActivity;
import com.stockholmiot.proxyguide.ui.home.adapters.PoIAdapter;
import com.stockholmiot.proxyguide.ui.home.models.Filters;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment  implements PoIAdapter.OnPoISelectedListener,
        FilterDialogFragment.FilterListener{

    private static final int LIMIT = 50;
    private static final String SUCCESS_TRANSACTION_STATUS = "SUCCESS";
    private static final String TAG = "MainActivity";
    public static final String KEY_POI_ID = "key_poi_id";
    private static final int SPLASH_SCREEN = 60000;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private PoIAdapter mAdapter;
    private RecyclerView madsRecycler;
    private HomeViewModel homeViewModel;
    private FilterDialogFragment mFilterDialog;
    private ProgressBar mLoadingIndicator;
    private MainActivity mainActivity;

    public HomeFragment() {
    }


    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mainActivity = (MainActivity) getActivity();

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            FloatingActionButton fab = mainActivity.findViewById(R.id.fab);
            madsRecycler = root.findViewById(R.id.poi_recyclerView);
            //final TextView textView = root.findViewById(R.id.text);
            mLoadingIndicator = root.findViewById(R.id.pb_loading_indicator);

            // Enable Firestore logging
            FirebaseFirestore.setLoggingEnabled(true);

            // Initialize Firestore and the main RecyclerView
            mFirestore = FirebaseUtil.getFirestore();
            // Get the 50 highest rated restaurants
            mQuery = mFirestore.collection("PoI")
                    .orderBy("number_views", Query.Direction.DESCENDING)
                    .limit(LIMIT);

            initRecyclerView();
            mFilterDialog = new FilterDialogFragment(getContext(), HomeFragment.this);
            //fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_24));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_sort_24));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Fonctionnne Fab");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    mFilterDialog.show(mainActivity.getSupportFragmentManager(), FilterDialogFragment.TAG);
                    //mFilterDialog.show(getParentFragmentManager(),FilterDialogFragment.TAG);
                    //Intent startSearchActivity = new Intent(mainActivity, SearchActivity.class);
                    //startActivity(startSearchActivity);
                }
            });


            showLoading();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Error: "+e.getMessage());
        }
        return root;
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new PoIAdapter(getContext(), mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    madsRecycler.setVisibility(View.GONE);
                    //mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    showAdsDataView();
                    madsRecycler.setVisibility(View.VISIBLE);
                    //mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                Log.d(TAG, "Error occured: " + e.getMessage());
            }
        };

        madsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        madsRecycler.setAdapter(mAdapter);


    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bottom_app_bar_home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();

        onFilter(homeViewModel.getFilters());
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onFilter(Filters filters) {
        // TODO(developer): Construct new query
        //showTodoToast();
        // Construct query basic query
        Query query = mFirestore.collection("PoI");

        // Category (equality filter)
        if (filters.hasCountry()) {
            query = query.whereEqualTo("country", filters.getCountry());
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo("city", filters.getCity());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mQuery = query;
        mAdapter.setQuery(query);

        // Set header

        // Save filters
        homeViewModel.setFilters(filters);

    }

    private void showLoading() {
        /* Then, hide the weather data */
        madsRecycler.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showAdsDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        madsRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPoISelected(DocumentSnapshot poi) {

        Intent startDetailAds = new Intent(getContext(), DetailPoIActivity.class);
        startDetailAds.putExtra(KEY_POI_ID, poi.getId());
        startActivity(startDetailAds);
    }
}
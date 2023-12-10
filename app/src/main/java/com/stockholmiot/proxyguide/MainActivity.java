package com.stockholmiot.proxyguide;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stockholmiot.proxyguide.databinding.ActivityMainBinding;
import com.stockholmiot.proxyguide.ui.NavigationHost;
import com.stockholmiot.proxyguide.ui.home.HomeFragment;
import com.stockholmiot.proxyguide.ui.home.models.PoIModel;
import com.stockholmiot.proxyguide.ui.map.MapsFragment;
import com.stockholmiot.proxyguide.ui.signin.SigninActivity;
import com.stockholmiot.proxyguide.ui.signin.models.User;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationHost, View.OnClickListener {

    private BottomAppBar navView;
    private FloatingActionButton fab;
    private ImageView drawableButton;
    private ImageView logoApp;
    private TextView navigationProfile;

    private TextView navigationMessage;
    private TextView navigationFavorite;
    private TextView navigationLogout;

    private CoordinatorLayout coordinatorLayout;
    private LinearLayout bottomAppbarLayoutContainer;
    private CircleImageView menuUserProfile;
    private FirebaseAuth mAuth;
    private AuthUI authUI;
    private FirebaseFirestore mFirestore;
    private StorageReference userProfileImageRef;
    private String currentUserId;
    private PoIModel poiModel;
    private CoordinatorLayout backdroopNav;
    private FrameLayout containerMenu;


    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            mAuth = FirebaseAuth.getInstance();
            authUI = FirebaseUtil.getAuthUI();
            // Enable Firestore logging
            FirebaseFirestore.setLoggingEnabled(true);
            // Initialize Firestore and the main RecyclerView
            mFirestore = FirebaseUtil.getFirestore();

            CollectionReference users = mFirestore.collection("users"); //V5uD3MP2dDtwWNhuZPA9
            DocumentReference currentUserRef = users
                    .document(FirebaseUtil.getAuth().getCurrentUser().getUid());

            userProfileImageRef = FirebaseStorage.getInstance().getReference("user_pictures");
            currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();

            //Get Ids references
            navView = findViewById(R.id.bottom_app_bar);
            fab = findViewById(R.id.fab);
            logoApp = findViewById(R.id.bottom_app_bar_logo);
            menuUserProfile = findViewById(R.id.profile_image_view);
            bottomAppbarLayoutContainer = findViewById(R.id.bottom_app_bar_content_container);
            drawableButton = findViewById(R.id.bottom_app_bar_chevron);
            coordinatorLayout = findViewById(R.id.backdrop);
            containerMenu = coordinatorLayout.findViewById(R.id.background_container);

            navigationProfile = findViewById(R.id.navigation_profile);
            navigationMessage = findViewById(R.id.navigation_message);
            navigationLogout = findViewById(R.id.navigation_logout);
            navigationFavorite = findViewById(R.id.navigation_favourite_poi);

            //Handle OnclickListener
            navigationProfile.setOnClickListener(this);
            //navigationListener.setOnClickListener(this);
            navigationMessage.setOnClickListener(this);
            navigationFavorite.setOnClickListener(this);
            navigationLogout.setOnClickListener(this);
            logoApp.setOnClickListener(this);
            bottomAppbarLayoutContainer.setOnClickListener(this);
            setSupportActionBar(navView);

            coordinatorLayout.setNestedScrollingEnabled(true);

            if (savedInstanceState == null)
                ((NavigationHost) Objects.requireNonNull(this)).navigateTo(new HomeFragment(), false);

            String url = null;
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                String adsID = getIntent().getStringExtra("ADS_EXTRA");
                url = getIntent().getStringExtra("ADS_EXTRA");

                if (url.equals("Home Fragment")) {

                    HomeFragment fragment = new HomeFragment();
                    ((NavigationHost) Objects.requireNonNull(this)).navigateTo(fragment, true);
                } else {

                   /* GalleryFragment fragment = new GalleryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(ADS_EXTRA, adsID);
                    fragment.setArguments(bundle);
                    ((NavigationHost) Objects.requireNonNull(this)).navigateTo(fragment, true);*/
                }
            }

            //Show drawable menu
            drawableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (coordinatorLayout.getVisibility() != View.VISIBLE) {
                        fab.hide();
                        coordinatorLayout.setVisibility(View.VISIBLE);
                        drawableButton.setImageDrawable(getDrawable(R.drawable.ic__arrow_down_24));
                    } else {
                        drawableButton.setImageDrawable(getDrawable(R.drawable.ic_arrow_up_24));
                        coordinatorLayout.setVisibility(View.INVISIBLE);
                        fab.show();
                    }
                }
            });

            navView.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.navigation_forum) {
                        //navigateTo(new NotificationFragment(), true);
                    } else if (id == R.id.navigation_map) {
                        navigateToMap(new MapsFragment(), true);
                    } else if (id == R.id.logout_navigation) {
                        authUI = FirebaseUtil.getAuthUI();
                        mAuth.signOut();
                        authUI.signOut(MainActivity.this);
                        startSignIn();
                    }

                    return true;
                }
            });





        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Testtsts", Toast.LENGTH_SHORT).show();
            }
        });



            currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User userModel = task.getResult().toObject(User.class);
                        String token = userModel.getToken();
                        String profileUrlValue = userModel.getProfileUrl();


                        if (profileUrlValue != null) {

                            Glide.with(menuUserProfile.getContext())
                                    .load(profileUrlValue)
                                    .into(menuUserProfile);
                        } else {

                            String userPicture = "";
                            if (FirebaseUtil.getAuth().getCurrentUser().getPhotoUrl() != null) {

                                userPicture = FirebaseUtil.getAuth().getCurrentUser().getPhotoUrl().toString();
                                Glide.with(menuUserProfile.getContext())
                                        .load(userPicture)
                                        .into(menuUserProfile);
                            }
                        }

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController); */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_app_bar_home_menu, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        try {

            if(viewId ==R.id.navigation_profile){

            }else if(viewId ==R.id.navigation_message){

            }else if (viewId ==R.id.navigation_logout){

            }else if(viewId ==R.id.bottom_app_bar_logo || viewId == R.id.bottom_app_bar_content_container ){

            }

            switch (viewId) {

               /* case R.id.navigation_profile:
                    drawableButton.setImageDrawable(getDrawable(R.drawable.ic_arrow_up_24));
                    coordinatorLayout.setVisibility(View.INVISIBLE);
                    fab.show();
                    //navigateToCustom(new ProfileFragment(), true);
                    Toast.makeText(this, "RAS", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.navigation_message:
                    drawableButton.setImageDrawable(getDrawable(R.drawable.ic_arrow_up_24));
                    coordinatorLayout.setVisibility(View.INVISIBLE);
                    fab.show();
                    //navigateToCustom(new MessageFragment(), true);
                    break;
                case R.id.navigation_logout:
                    authUI = FirebaseUtil.getAuthUI();
                    mAuth.signOut();
                    authUI.signOut(this);
                    startSignIn();
                    break;
                case R.id.bottom_app_bar_logo:
                case R.id.bottom_app_bar_content_container:
                    drawableButton.setImageDrawable(getDrawable(R.drawable.ic_arrow_up_24));
                    coordinatorLayout.setVisibility(View.INVISIBLE);
                    fab.show();
                    navigateToCustom(new HomeFragment(), true);
                    break; */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        final FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        new CountDownTimer(700, 700) {
            @Override
            public void onTick(long l) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(coordinatorLayout, "translationY", 0);
                animator.setDuration(400);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                animator.start();
            }

            @Override
            public void onFinish() {
                transaction.commit();
            }
        }.start();
    }

    @Override
    public void navigateToCustom(Fragment fragment, boolean addToBackstack) {

    }

    @Override
    public void navigateToMap(Fragment fragment, boolean addToBackstack) {
        final FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        new CountDownTimer(100, 100) {
            @Override
            public void onTick(long l) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(coordinatorLayout, "translationY", 0);
                animator.setDuration(10);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(animator);
                animator.start();
            }

            @Override
            public void onFinish() {
                transaction.commit();
            }
        }.start();
    }

    private void startSignIn() {
        Intent loginIntent = new Intent(MainActivity.this, SigninActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseUtil.getAuthUI() == null)
            startSignIn();
    }
}
package com.stockholmiot.proxyguide.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.signin.models.User;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

public class Profile extends AppCompatActivity {

    private String currentUserId;
    private FirebaseFirestore mFirestore;
    private StorageReference userProfileImageRef;

    private TextView fullNameTv;
    private TextView emailTv;
    private TextView phoneTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseUtil.getFirestore();
        userProfileImageRef = FirebaseStorage.getInstance().getReference("user_pictures");
        currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();

        fullNameTv = (TextView) findViewById(R.id.tv_fullName);
        emailTv = (TextView) findViewById(R.id.tv_email);
        phoneTv = (TextView) findViewById(R.id.tv_phone);

        CollectionReference users = mFirestore.collection("users"); //V5uD3MP2dDtwWNhuZPA9
        DocumentReference currentUserRef = users
                .document(FirebaseUtil.getAuth().getCurrentUser().getUid());

        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    User userModel = task.getResult().toObject(User.class);
                    String token = userModel.getToken();

                    String phoneValue = userModel.getPhone();
                    String profileUrlValue = userModel.getProfileUrl();
                    String userNameValue = userModel.getUsername();
                    String emailAddress = FirebaseUtil.getAuth().getCurrentUser().getEmail();


                    if (phoneValue != null)
                        phoneTv.setText(phoneValue);
                    if (fullNameTv != null)
                        fullNameTv.setText(userNameValue);

                    emailTv.setText(emailAddress);

                    if (profileUrlValue != null) {

                        /*Glide.with(profileImage.getContext())
                                .load(profileUrlValue)
                                .into(profileImage);
                        profileImage.setVisibility(View.VISIBLE);
                        profileContainer.setVisibility(View.GONE); */
                    } else {

                        /*String userPicture = "";
                        if (FirebaseUtil.getAuth().getCurrentUser().getPhotoUrl() != null) {

                            userPicture = FirebaseUtil.getAuth().getCurrentUser().getPhotoUrl().toString();
                            Glide.with(profileImage.getContext())
                                    .load(userPicture)
                                    .into(profileImage);

                            profileImage.setVisibility(View.VISIBLE);
                            profileContainer.setVisibility(View.GONE);
                        }*/
                    }


                }
            }
        });
    }
}
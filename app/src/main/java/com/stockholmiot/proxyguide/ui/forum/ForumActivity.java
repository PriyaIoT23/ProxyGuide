package com.stockholmiot.proxyguide.ui.forum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.databinding.ActivityForumBinding;
import com.stockholmiot.proxyguide.databinding.ActivityMainBinding;
import com.stockholmiot.proxyguide.ui.forum.Adapters.ButtonObserver;
import com.stockholmiot.proxyguide.ui.forum.Adapters.MessageViewHolder;
import com.stockholmiot.proxyguide.ui.forum.Adapters.ScrollToBottomObserver;
import com.stockholmiot.proxyguide.ui.forum.models.FriendlyMessage;
import com.stockholmiot.proxyguide.ui.forum.models.IntentUtility;
import com.stockholmiot.proxyguide.ui.forum.models.VerticalListItemSpacingDecoration;
import com.stockholmiot.proxyguide.ui.nearby.ContactActivity;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

public class ForumActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String MESSAGES_CHILD = "messages";
    public static final String ANONYMOUS = "anonymous";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private static final int REQUEST_IMAGE = 2;

    private FirebaseAuth mFirebaseAuth;
    private AuthUI authUI;
    private FirebaseFirestore mFirestore;
    private FirebaseDatabase mFirebaseDatabase;
    private ActivityForumBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityForumBinding.inflate(getLayoutInflater());
        // Set the root view from ViewBinding instance
        setContentView(mBinding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //setContentView(R.layout.activity_forum);

        mFirebaseAuth = FirebaseAuth.getInstance();
        authUI = FirebaseUtil.getAuthUI();
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseUtil.getFirestore();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        try {
            DatabaseReference messagesRef = mFirebaseDatabase.getReference().child(MESSAGES_CHILD);

            FirebaseRecyclerOptions<FriendlyMessage> options = new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                    .setQuery(messagesRef, FriendlyMessage.class)
                    // Listen to the changes in the Query and automatically update to the UI
                    .setLifecycleOwner(this)
                    .build();

            // Construct the FirebaseRecyclerAdapter with the options set
            FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
                @NonNull
                @Override
                public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new MessageViewHolder(
                            LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_message, parent, false)
                    );
                }

                @Override
                protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull FriendlyMessage message) {
                    mBinding.progressBar.setVisibility(ProgressBar.INVISIBLE);
                    holder.bindMessage(message);
                }
            };

            // Initialize LinearLayoutManager and RecyclerView
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(false);
            mBinding.messageRecyclerView.setLayoutManager(linearLayoutManager);
            mBinding.messageRecyclerView.setAdapter(firebaseRecyclerAdapter);
            mBinding.messageRecyclerView.addItemDecoration(new VerticalListItemSpacingDecoration(
                    getResources().getDimensionPixelSize(R.dimen.main_item_list_spacing),
                    getResources().getDimensionPixelSize(R.dimen.main_item_parent_spacing)
            ));

            // Register an observer for watching changes in the Adapter data in order to scroll
            // to the bottom of the list when the user is at the bottom of the list
            // in order to show newly added messages
            firebaseRecyclerAdapter.registerAdapterDataObserver(
                    new ScrollToBottomObserver(
                            mBinding.messageRecyclerView,
                            firebaseRecyclerAdapter,
                            linearLayoutManager
                    )
            );

            // Disable the send button when there is no text in this input message field
            mBinding.messageEditText.addTextChangedListener(new ButtonObserver(mBinding.sendButton));

            // Register a click listener on the Send Button to send messages on click
            mBinding.sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FriendlyMessage friendlyMessage = new FriendlyMessage(
                            getMessageToSend(),
                            getUserName(),
                            getUserPhotoUrl(),
                            null /* not an image based message */
                    );

                    // Create a child reference and set the user's message at that location
                    mFirebaseDatabase.getReference().child(MESSAGES_CHILD)
                            .push().setValue(friendlyMessage);
                    // Clear the input message field for the next message
                    mBinding.messageEditText.setText("");
                }
            });

            // Register a click listener on the Add Image Button to send messages with Image on click
            mBinding.addMessageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Launch Gallery Intent for Image selection
                    IntentUtility.launchGallery(ForumActivity.this, REQUEST_IMAGE);
                }
            });
        } catch (Resources.NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if( item.getItemId()  == R.id.action_connected_users){
            Intent intent = new Intent(this, ContactActivity.class);
            startActivity(intent);
            //Toast.makeText(this, "working....", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        // Sign-Out user from the backend and identity provider
        //mFirebaseAuth.signOut();
        //mSignInClient.signOut();
        // Start the SignInActivity and finish the current activity
        //startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    /**
     * Returns the URL to the User's profile picture as stored in Firebase Project's user database.
     * Can be {@code null} when not present or if user is not authenticated.
     */
    @Nullable
    private String getUserPhotoUrl() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }
        return null;
    }

    /**
     * Returns the display name of the User as stored in Firebase Project's user database.
     * Can be {@link this.ANONYMOUS} if the user is not authenticated.
     */
    private String getUserName() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            return user.getDisplayName();
        }
        return ANONYMOUS;
    }

    /**
     * Extracts the user typed message from 'R.id.messageEditText' EditText and returns the same.
     * Can be an empty string when there is no message typed in.
     */
    private String getMessageToSend() {
        if (mBinding.messageEditText.getText() == null) {
            return "";
        } else {
            return mBinding.messageEditText.getText().toString();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            // If the request was for Image selection

            if (resultCode == RESULT_OK && data != null) {
                // If we have the result and its data

                // Get the URI to the image file selected
                final Uri imageUri = data.getData();

                // Construct a message with temporary loading image
                final FriendlyMessage tempMessage = new FriendlyMessage(
                        getMessageToSend(),  // If user has entered some message, publish it as well
                        getUserName(),
                        getUserPhotoUrl(),
                        LOADING_IMAGE_URL  // Temporary image with loading indicator
                );

                // Create a child reference and set the user's message at that location
                mFirebaseDatabase.getReference().child(MESSAGES_CHILD)
                        .push().setValue(tempMessage, new DatabaseReference.CompletionListener() {
                            /**
                             * This method will be triggered when the operation has either succeeded or failed. If it has
                             * failed, an error will be given. If it has succeeded, the error will be null
                             *
                             * @param error A description of any errors that occurred or null on success
                             * @param ref A reference to the specified Firebase Database location
                             */
                            @Override
                            public void onComplete(
                                    @Nullable DatabaseError error,
                                    @NonNull DatabaseReference ref) {
                                // Check the error
                                if (error != null) {
                                    // Log the error and return
                                    Log.w(TAG,
                                            "Unable to write message to the database.",
                                            error.toException()
                                    );
                                    return;
                                }

                                // Get the key to this database reference
                                String databaseKey = ref.getKey();

                                // Create a StorageReference for the Image to be uploaded
                                // in the hierarchy of the database key reference
                                //noinspection ConstantConditions
                                StorageReference storageReference = FirebaseStorage.getInstance()
                                        // Create a child location for the current user
                                        .getReference(mFirebaseAuth.getCurrentUser().getUid())
                                        // Create a child location for the database key
                                        .child(databaseKey)
                                        // Create a child with the filename
                                        .child(imageUri.getLastPathSegment());

                                // Begin upload of selected image
                                putImageInStorage(storageReference, imageUri, databaseKey, tempMessage);

                                // Clear the input message field if any for the next message
                                mBinding.messageEditText.setText("");
                            }
                        });

            }
        }
    }
    private void putImageInStorage(final  StorageReference storageReference,
                                   final Uri imageUri,
                                   final String databaseKey,
                                   final FriendlyMessage tempMessage) {
        // Upload the selected image
        UploadTask uploadTask = storageReference.putFile(imageUri);

        // Chain UploadTask to get the resulting URI Task of the uploaded image
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                // Return the resulting URI Task of the uploaded image
                //noinspection ConstantConditions
                return task.getResult().getStorage().getDownloadUrl();
            }
        }).addOnSuccessListener(this, new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // When all tasks have completed successfully, update the corresponding reference
                // in the database with the URI of the uploaded image
                tempMessage.setImageUrl(uri.toString());
                mFirebaseDatabase.getReference().child(MESSAGES_CHILD)
                        .child(databaseKey)
                        .setValue(tempMessage);
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log the exception in case of failure
                Log.w(TAG, "Image upload task was not successful.", e);
            }
        });
    }

}
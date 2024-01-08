package com.stockholmiot.proxyguide.ui.home.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.home.models.PoIModel;
import com.stockholmiot.proxyguide.ui.signin.models.User;
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends FirestoreAdapter<ContactAdapter.ViewHolder>{

    private static final String TAG = ContactAdapter.class.getSimpleName();
    private FirebaseFirestore mFirestore;
    private DocumentReference mAdsRef;

    public interface OnContactSelectedListener {
        void onContactSelected(DocumentSnapshot ads);
    }

    private Context mContext;
    private ContactAdapter.OnContactSelectedListener mListener;

    public ContactAdapter(Context context, Query query, ContactAdapter.OnContactSelectedListener listener) {
        super(query);
        mContext = context;
        mListener = listener;
    }


    @NonNull
    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ContactAdapter.ViewHolder(inflater.inflate(R.layout.item_provider_process, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        CircleImageView dialogAvatar;
        TextView dialogName;
        TextView dialogLastMessage;
        TextView textOptions;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dialogAvatar = itemView.findViewById(R.id.dialogAvatar);
            dialogName = itemView.findViewById(R.id.dialogName);
            dialogLastMessage = itemView.findViewById(R.id.dialogLastMessage);
            textOptions = itemView.findViewById(R.id.textOptions);

        }

        public void bind(final DocumentSnapshot snapshot,
                         final ContactAdapter.OnContactSelectedListener listener) {
            try {

                User userModel = snapshot.toObject(User.class);
                Resources resources = itemView.getResources();

                // Initialize Firestore
                mFirestore = FirebaseUtil.getFirestore();
                // Get reference to the restaurant
                //mAdsRef = mFirestore.collection("PoI").document(poiId);
                String currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();


                // Load image
              /*  Glide.with(poiImage.getContext())
                        .load(poiModel.getPhoto_url())
                        .into(poiImage);*/

                dialogName.setText(userModel.getUsername());
                //poiDescription.setText(poiModel.getDescription());
                //address.setText(poiModel.getAddress());
                // Click listener
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            //listener.onPoISelected(snapshot);
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error: " + e.getMessage());
            }
        }
    }

}

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
import com.stockholmiot.proxyguide.util.FirebaseUtil;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PoIAdapter extends FirestoreAdapter<PoIAdapter.ViewHolder> {

    private static final String TAG = PoIAdapter.class.getSimpleName();
    private FirebaseFirestore mFirestore;
    private DocumentReference mAdsRef;

    public interface OnPoISelectedListener {
        void onPoISelected(DocumentSnapshot ads);
    }

    private Context mContext;
    private OnPoISelectedListener mListener;

    public PoIAdapter(Context context, Query query, OnPoISelectedListener listener) {
        super(query);
        mContext = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public PoIAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.poi_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PoIAdapter.ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView poiImage;
        TextView poiName;
        TextView poiDescription;
        TextView address;
        FloatingActionButton favoriteFab;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            poiImage = itemView.findViewById(R.id.poi_picture);
            poiName = itemView.findViewById(R.id.poi_name);
            poiDescription = itemView.findViewById(R.id.poi_description);
            address = itemView.findViewById(R.id.poi_address);
            favoriteFab = itemView.findViewById(R.id.favourite);

        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnPoISelectedListener listener) {
            try {

                PoIModel poiModel = snapshot.toObject(PoIModel.class);
                Resources resources = itemView.getResources();
                String poiId = snapshot.getId();

                // Initialize Firestore
                mFirestore = FirebaseUtil.getFirestore();
                // Get reference to the restaurant
                mAdsRef = mFirestore.collection("PoI").document(poiId);
                String currentUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();

                Query favoriteQuery = mAdsRef
                        .collection("favorite").whereEqualTo(currentUserId, true);

                Query favoriteQueryFalse = mAdsRef
                        .collection("favorite").whereEqualTo(currentUserId, false);

                favoriteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                if (isfavorite)
                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pleine_favorite_24));
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG, "Error: " + e.getMessage());
                    }
                });

                favoriteQueryFalse.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                if (isfavorite)
                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_white_border_24));
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG, "Error: " + e.getMessage());
                    }
                });

                // Load image
                Glide.with(poiImage.getContext())
                        .load(poiModel.getPhoto_url())
                        .into(poiImage);

                poiName.setText(poiModel.getName());
                poiDescription.setText(poiModel.getDescription());
                address.setText(poiModel.getAddress());
                // Click listener
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onPoISelected(snapshot);
                        }
                    }
                });

                favoriteFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        favoriteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                        if (isfavorite) {

                                            document.getReference().update(currentUserId, false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_white_border_24));
                                                    //gotoMainActivity();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                }
                                            });
                                        } else {

                                            document.getReference().update(currentUserId, true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pleine_favorite_24));
                                                    //gotoMainActivity();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                }
                                            });
                                        }

                                    }
                                } else {

                                    favoriteQueryFalse.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                    boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                                    if (isfavorite) {

                                                        document.getReference().update(currentUserId, false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_white_border_24));
                                                                //gotoMainActivity();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                                Log.d(TAG, "Error: " + e.getMessage());
                                                            }
                                                        });
                                                    } else {

                                                        document.getReference().update(currentUserId, true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pleine_favorite_24));
                                                                //gotoMainActivity();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                                Log.d(TAG, "Error: " + e.getMessage());
                                                            }
                                                        });
                                                    }

                                                }
                                            } else {
                                                Log.d(TAG, "Start");
                                                Map<String, Boolean> favoriteMap = new HashMap<String, Boolean>();
                                                favoriteMap.put(currentUserId, true);
                                                mAdsRef.collection("favorite").document().set(favoriteMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        favoriteQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                        boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                                                        if (isfavorite) {
                                                                            document.getReference().update(currentUserId, true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pleine_favorite_24));

                                                                                    //gotoMainActivity();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                                                }
                                                                            });
                                                                        } else {

                                                                            document.getReference().update(currentUserId, false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_white_border_24));
                                                                                    //gotoMainActivity();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                                                }
                                                                            });
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull @NotNull Exception e) {
                                                        Log.d(TAG, "Error: " + e.getMessage());
                                                    }
                                                });

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Log.d(TAG, "Error: " + e.getMessage());
                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Log.d(TAG, "Error: " + e.getMessage());
                            }
                        });

                        favoriteQueryFalse.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        boolean isfavorite = Boolean.valueOf(document.getData().get(currentUserId).toString());
                                        if (isfavorite) {

                                            document.getReference().update(currentUserId, false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_white_border_24));
                                                    //gotoMainActivity();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                }
                                            });
                                        } else {

                                            document.getReference().update(currentUserId, true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    favoriteFab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pleine_favorite_24));
                                                    //gotoMainActivity();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    Log.d(TAG, "Error: " + e.getMessage());
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Log.d(TAG, "Error: " + e.getMessage());
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error: " + e.getMessage());
            }
        }
    }
}

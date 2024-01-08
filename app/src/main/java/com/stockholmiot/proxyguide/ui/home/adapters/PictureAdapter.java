package com.stockholmiot.proxyguide.ui.home.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.home.DetailGallery;
import com.stockholmiot.proxyguide.ui.home.FilterDialogFragment;
import com.stockholmiot.proxyguide.ui.home.models.Picture;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolader> {
    private static final String TAG = PictureAdapter.class.getSimpleName();
    private List<Picture> pictureList;
    private Context mContext;
    private DetailGallery detailGallery;

    public PictureAdapter(List<Picture> pictureList, Context mContext) {
        this.pictureList = pictureList;
        this.mContext = mContext;
    }

    @NonNull
    @NotNull
    @Override
    public PictureAdapter.ViewHolader onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_photo_item, parent, false);

        return new ViewHolader(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PictureAdapter.ViewHolader holder, int position) {
        final Picture picture = pictureList.get(position);
        holder.bind(picture);
    }

    @Override
    public int getItemCount() {
        if (pictureList != null)
            return pictureList.size();
        else
            return 0;
    }

    public class ViewHolader extends RecyclerView.ViewHolder {

        ImageView image;

        public ViewHolader(@NonNull @NotNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.picture_Id);
        }

        public void bind(Picture picture) {

            try {
                Glide.with(image.getContext())
                        .asBitmap()
                        .load(picture.getUrl())
                        .into(image);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String pictureUrl = picture.getUrl();
                        detailGallery = new DetailGallery(mContext, pictureUrl);
                        detailGallery.show(((AppCompatActivity) mContext).getSupportFragmentManager(), FilterDialogFragment.TAG);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error: " + e.getMessage());
            }
        }
    }

    public void setPicture(List<Picture> pictures) {
        pictureList = pictures;
        notifyDataSetChanged();
    }
}

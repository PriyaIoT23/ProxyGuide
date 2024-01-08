package com.stockholmiot.proxyguide.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.stockholmiot.proxyguide.R;


public class DetailGallery extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private String imageUrl;
    private View mRootView;
    public static final String URL_EXTRA = "url_extra";
    private String url;

    public DetailGallery() {
        // Required empty public constructor
    }

    public DetailGallery(Context mContext) {
        this.mContext = mContext;
    }

    public DetailGallery(Context mContext, String murl) {
        this.mContext = mContext;
        url = murl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_detail_gallery, container, false);
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        try {

            ImageView image = mRootView.findViewById(R.id.image_load);
            Glide.with(image.getContext())
                    .asBitmap()
                    .load(url)
                    .into(image);
        } catch (Exception e) {

            e.printStackTrace();
        }

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
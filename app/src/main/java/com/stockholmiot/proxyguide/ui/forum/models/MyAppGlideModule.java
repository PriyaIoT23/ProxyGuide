package com.stockholmiot.proxyguide.ui.forum.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import java.io.InputStream;

public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(
            @NonNull Context context,
            @NonNull Glide glide,
            @NonNull Registry registry) {

        // Register FirebaseImageLoader component to handle StorageReference
        registry.append(
                // Model class
                StorageReference.class,
                // Data class
                InputStream.class,
                // ModelLoaderFactory
                new FirebaseImageLoader.Factory()
        );

    }
}

package com.stockholmiot.proxyguide.ui.splashscreen;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;
import com.stockholmiot.proxyguide.ui.signin.SigninActivity;

public class Splashscreen extends AppCompatActivity {

    private static int SPLASH_SCREEN = 2000;
    private Animation topAnimation, bottomAnimation;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splashscreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);*/

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        mImage = findViewById(R.id.splash_image);
        mImage.setAnimation(topAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splashscreen.this, SigninActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN);


    }
}
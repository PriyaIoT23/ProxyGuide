package com.stockholmiot.proxyguide.ui.splashscreen;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

import android.content.Intent;
import android.os.Bundle;

import com.stockholmiot.proxyguide.MainActivity;
import com.stockholmiot.proxyguide.R;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splashscreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
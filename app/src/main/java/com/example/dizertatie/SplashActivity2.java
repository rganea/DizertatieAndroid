package com.example.dizertatie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity2 extends AppCompatActivity {

    //splash screen delay

    private static int SPLASH_DELAY =4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);

        Runnable secondThread = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity2.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        };

        new Handler().postDelayed(secondThread,SPLASH_DELAY);
    }
}
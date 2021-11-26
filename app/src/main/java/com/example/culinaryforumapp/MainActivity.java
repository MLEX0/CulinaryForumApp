package com.example.culinaryforumapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ImageView cos;
    ImageView alians;
    ImageView pikls;
    ImageView tarelka;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cos = findViewById(R.id.imageView);
        alians = findViewById(R.id.imageView2);
        pikls = findViewById(R.id.imageView3);
        tarelka =findViewById(R.id.imageView4);

        Animation animtarelka =  AnimationUtils.loadAnimation(this,R.anim.animtarelka);
        Animation anim =  AnimationUtils.loadAnimation(this,R.anim.shake);
        Animation animpikls =  AnimationUtils.loadAnimation(this,R.anim.animpikls);
        Animation animalians =  AnimationUtils.loadAnimation(this,R.anim.animalians);

        Thread threadLoading = new Thread() {
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(6);
                    Intent intentLoading = new Intent(MainActivity.this, RegistrationAuthorizationActivity.class);
                    startActivity(intentLoading);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };threadLoading.start();

        cos.startAnimation(anim);
        alians.startAnimation(animalians);
        pikls.startAnimation(animpikls);
        tarelka.startAnimation(animtarelka);

    }
}
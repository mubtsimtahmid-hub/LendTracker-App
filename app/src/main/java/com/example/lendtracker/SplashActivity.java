package com.example.lendtracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Screen 1: shows the logo for 1.5 seconds, then decides where to go.
 * - Logged in AND "Remember me" was ticked -> Home
 * - Otherwise -> Login
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::goNext, 1500);
    }

    private void goNext() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        boolean rememberMe = Prefs.isRememberMe(this);

        Intent intent;
        if (user != null && rememberMe) {
            intent = new Intent(this, MainActivity.class);   // stay logged in
        } else {
            if (user != null) auth.signOut();                 // user did not want to be remembered
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

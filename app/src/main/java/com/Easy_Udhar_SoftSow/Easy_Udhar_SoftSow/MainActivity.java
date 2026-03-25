package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.Easy_Udhar_SoftSow.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "🚀 MainActivity started");

        // Delay for splash animation
        new Handler().postDelayed(() -> {
            checkAndNavigate();
        }, SPLASH_DELAY);
    }

    private void checkAndNavigate() {
        // Check if user already logged in
        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("logged_in", false);

        Log.d(TAG, "👤 User logged in: " + isLoggedIn);

        if (isLoggedIn) {
            // User already logged in → go to dashboard
            Log.d(TAG, "➡️ Going to dashboard (user logged in)");
            navigateToDashboard();
        } else {
            // User not logged in → go to dashboard directly
            Log.d(TAG, "➡️ Going to dashboard (first login)");
            navigateToDashboard();
        }
    }

    private void navigateToDashboard() {
        Log.d(TAG, "📍 Navigating to dashboard");

        Intent intent = new Intent(MainActivity.this, dashboard.class);
        startActivity(intent);
        finish(); // close MainActivity
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "❌ MainActivity destroyed");
        super.onDestroy();
    }
}
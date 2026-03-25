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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);// dark mode off kia maynay
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "🚀 MainActivity started");

        // Debug: Current app lock status
        applock_helper.debugAppLockStatus(this);

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
            // User already logged in → check app lock
            checkAppLockAndNavigate();
        } else {
            // User not logged in → go to dashboard directly (no lock for first time)
            Log.d(TAG, "➡️ Going to dashboard (first login)");
            Intent intent = new Intent(MainActivity.this, dashboard.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkAppLockAndNavigate() {
        try {
            // Debug
            applock_helper.debugAppLockStatus(this);

            // ✅ IMPORTANT: Properly check if user is logged in
            SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("logged_in", false);

            Log.d(TAG, "👤 User logged in: " + isLoggedIn);

            if (!isLoggedIn) {
                Log.d(TAG, "➡️ User not logged in - going directly to dashboard");
                navigateToDashboard();
                return;
            }

            // ✅ Check if app lock is enabled
            boolean isAppLockEnabled = applock_helper.isAppLockEnabled(this);
            boolean isFingerprintLock = applock_helper.isFingerprintLockEnabled(this);

            Log.d(TAG, "🔒 App Lock Enabled: " + isAppLockEnabled);
            Log.d(TAG, "🔐 Fingerprint Lock Enabled: " + isFingerprintLock);

            if (isAppLockEnabled && isFingerprintLock) {
                Log.d(TAG, "✅ App lock and fingerprint BOTH enabled - showing authentication");

                // Check fingerprint availability
                if (applock_helper.isFingerprintAvailable(this)) {
                    Log.d(TAG, "✅ Fingerprint available - showing dialog");
                    showFingerprintAuthentication();
                } else {
                    Log.d(TAG, "❌ Fingerprint not available - showing password option");
                    showPasswordDialog();
                }
            } else {
                Log.d(TAG, "➡️ App lock NOT enabled - going directly to dashboard");
                navigateToDashboard();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in checkAppLockAndNavigate: " + e.getMessage());
            navigateToDashboard();
        }
    }

    private void showFingerprintAuthentication() {
        try {
            Log.d(TAG, "🔄 Showing fingerprint authentication dialog");

            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);

            builder.setTitle("🔒 App Lock")
                    .setMessage("Fingerprint lock is enabled\n\nAuthenticate to open Asan Khata")
                    .setCancelable(false)
                    .setPositiveButton("Use Password", (dialog, which) -> {
                        Log.d(TAG, "✅ User chose password option");
                        showPasswordDialog();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Log.d(TAG, "❌ User clicked Cancel");
                        // App band karo
                        finishAffinity();
                    });

            // ✅ Dialog show karne se pehle check karo ke activity still alive hai
            if (!isFinishing() && !isDestroyed()) {
                androidx.appcompat.app.AlertDialog dialog = builder.create();
                dialog.show();
                Log.d(TAG, "✅ Fingerprint dialog shown successfully");
            } else {
                Log.e(TAG, "❌ Activity finishing/destroyed - cannot show dialog");
                navigateToDashboard();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing fingerprint auth: " + e.getMessage());
            navigateToDashboard();
        }
    }
    private void showPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setTitle("Enter Password")
                .setMessage("Enter your password to unlock the app")
                .setCancelable(false)
                .setPositiveButton("Unlock", (dialog, which) -> {
                    Log.d(TAG, "✅ Password accepted");
                    navigateToDashboard();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    finishAffinity();
                });

        builder.create().show();
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
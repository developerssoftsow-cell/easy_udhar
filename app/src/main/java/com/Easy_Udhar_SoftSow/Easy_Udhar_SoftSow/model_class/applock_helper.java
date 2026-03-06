package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.hardware.fingerprint.FingerprintManager;
import android.util.Log;

public class applock_helper {

    private static final String TAG = "AppLockHelper";
    private static final String PREFS_NAME = "AppLockPrefs";
    private static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";
    private static final String KEY_LOCK_TYPE = "lock_type";

    // Check if app lock is enabled
    public static boolean isAppLockEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false);
            String lockType = prefs.getString(KEY_LOCK_TYPE, "none");

            Log.d(TAG, "🔒 isAppLockEnabled(): " + enabled + ", Lock Type: " + lockType);

            // صرف اس صورت میں true واپس کریں اگر fingerprint فعال ہے
            return enabled && lockType.equals("fingerprint");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in isAppLockEnabled: " + e.getMessage());
            return false;
        }
    }

    // Debug method to print all preferences
    public static void debugAppLockStatus(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            boolean appLockEnabled = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false);
            String lockType = prefs.getString(KEY_LOCK_TYPE, "none");

            Log.d(TAG, "🔍 === APP LOCK STATUS ===");
            Log.d(TAG, "🔍 App Lock Enabled: " + appLockEnabled);
            Log.d(TAG, "🔍 Lock Type: " + lockType);
            Log.d(TAG, "🔍 Is Fingerprint Lock: " + (appLockEnabled && lockType.equals("fingerprint")));
            Log.d(TAG, "🔍 === END STATUS ===");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in debugAppLockStatus: " + e.getMessage());
        }
    }

    // Check if fingerprint lock is enabled
    public static boolean isFingerprintLockEnabled(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false);
            String lockType = prefs.getString(KEY_LOCK_TYPE, "none");

            Log.d(TAG, "🔐 isFingerprintLockEnabled() - Enabled: " + enabled + ", Type: " + lockType);

            // ✅ Check: Fingerprint lock should be enabled AND type should be "fingerprint"
            return enabled && "fingerprint".equals(lockType);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in isFingerprintLockEnabled: " + e.getMessage());
            return false;
        }
    }

    // Check fingerprint availability
    public static boolean isFingerprintAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                FingerprintManager fingerprintManager =
                        (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

                if (fingerprintManager == null) {
                    Log.d(TAG, "📱 Fingerprint manager is null");
                    return false;
                }

                boolean hasHardware = fingerprintManager.isHardwareDetected();
                boolean hasFingerprints = fingerprintManager.hasEnrolledFingerprints();

                Log.d(TAG, "📱 Fingerprint Available - Hardware: " + hasHardware + ", Enrolled: " + hasFingerprints);

                return hasHardware && hasFingerprints;
            } catch (Exception e) {
                Log.e(TAG, "❌ Error checking fingerprint availability: " + e.getMessage());
                return false;
            }
        }
        Log.d(TAG, "📱 Android version too low for fingerprint");
        return false;
    }

    // ✅ Save app lock settings method
    public static void setAppLockEnabled(Context context, boolean enabled, String lockType) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean(KEY_APP_LOCK_ENABLED, enabled);
            editor.putString(KEY_LOCK_TYPE, lockType);

            editor.apply();

            Log.d(TAG, "💾 setAppLockEnabled() - Enabled: " + enabled +
                    ", Type: " + lockType + " - Saved successfully");

            // Debug کے لیے status print کریں
            debugAppLockStatus(context);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error in setAppLockEnabled: " + e.getMessage());
        }
    }
}
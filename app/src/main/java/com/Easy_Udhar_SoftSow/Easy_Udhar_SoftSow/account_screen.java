package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Easy_Udhar_SoftSow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class account_screen extends AppCompatActivity {

    LinearLayout layoutParent, layoutChild;
    ImageView arrowImg;
    TextView del;
    BottomNavigationView bottomNavigationView;
    LinearLayout layoutShareApp, layoutRateApp;
    LinearLayout languageLayout;
    LinearLayout privacyPolicyLayout;

    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);// dark mode off kia maynay
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_screen);

        initializeViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        layoutParent = findViewById(R.id.layout_app_settings);
        layoutChild = findViewById(R.id.layout_app_settings_child);
        arrowImg = findViewById(R.id.img_arrow);
        del = findViewById(R.id.dlet);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        layoutShareApp = findViewById(R.id.layout_share_app);
        layoutRateApp = findViewById(R.id.layout_rate_app);
        languageLayout = findViewById(R.id.language);
        privacyPolicyLayout = findViewById(R.id.privacy_pol);
    }

    private void setupClickListeners() {
        if (del != null) {
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToDeleteScreen();
                }
            });
        }

        if (layoutShareApp != null) {
            layoutShareApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareApp();
                }
            });
        }

        if (layoutRateApp != null) {
            layoutRateApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rateApp();
                }
            });
        }

        if (languageLayout != null) {
            languageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLanguageBottomSheet();
                }
            });
        }

        if (privacyPolicyLayout != null) {
            privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(account_screen.this, privacy_policy.class);
                    startActivity(intent);
                }
            });
        }

        if (layoutParent != null) {
            layoutParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleAppSettings();
                }
            });
        }
    }

    private void shareApp() {
        try {
            String shareText = "🌟 Asan Khata - Money Manager 🌟\n\n" +
                    "Maine yeh amazing app use ki hai jo aapke hisaab kitaab ko aasan banata hai!\n\n" +
                    "📊 Customer management\n" +
                    "💰 Income/Expense tracking\n" +
                    "📱 Easy to use\n" +
                    "🔒 Secure & Private\n\n" +
                    "Download karein: https://play.google.com/store/apps/details?id=" + getPackageName() +
                    "\n\n#AsanKhata #MoneyManager";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Asan Khata - Money Manager");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share Asan Khata via"));
        } catch (Exception e) {
        }
    }

    private void rateApp() {
        try {
            Uri marketUri = Uri.parse("market://details?id=" + getPackageName());
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            try {
                startActivity(marketIntent);
            } catch (ActivityNotFoundException e) {
                Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } catch (Exception e) {
        }
    }

    private void showLanguageBottomSheet() {
        View sheetView = getLayoutInflater().inflate(R.layout.language_bottom, null);

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(sheetView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        dialog.show();

        LinearLayout optionEnglish = sheetView.findViewById(R.id.optionEnglish);
        LinearLayout optionUrdu = sheetView.findViewById(R.id.optionUrdu);
        ImageView checkEnglish = sheetView.findViewById(R.id.checkEnglish);
        ImageView checkUrdu = sheetView.findViewById(R.id.checkUrdu);

        String currentLang = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("selected_language", "en");

        if (currentLang.equals("en")) {
            checkEnglish.setVisibility(View.VISIBLE);
            checkUrdu.setVisibility(View.GONE);
        } else if (currentLang.equals("ur")) {
            checkEnglish.setVisibility(View.GONE);
            checkUrdu.setVisibility(View.VISIBLE);
        }

        optionEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("en");
                dialog.dismiss();
            }
        });

        optionUrdu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("ur");
                dialog.dismiss();
            }
        });
    }

    private void changeLanguage(String languageCode) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("selected_language", languageCode)
                .apply();

        restartApp();
    }

    private void restartApp() {
        Intent intent = new Intent(this, account_screen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void toggleAppSettings() {
        if (isExpanded) {
            layoutChild.setVisibility(View.GONE);
            arrowImg.setImageResource(R.drawable.drop_down);
            isExpanded = false;
        } else {
            layoutChild.setVisibility(View.VISIBLE);
            arrowImg.setImageResource(R.drawable.arrow_up);
            isExpanded = true;
        }
    }

    private void navigateToDeleteScreen() {
        try {
            Intent intent = new Intent(account_screen.this, delete_screen.class);
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_khata) {
                navigateToDashboard();
                return true;

            } else if (id == R.id.nav_batwa) {
                Intent intent = new Intent(account_screen.this, report_screen.class);
                startActivity(intent);
                return true;

            } else if (id == R.id.nav_account) {
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_account);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, dashboard.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToDashboard();
    }
}
package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.Easy_Udhar_SoftSow.R;


public class privacy_policy extends AppCompatActivity {

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        backArrow = findViewById(R.id.btnBack);

        backArrow.setOnClickListener(v -> {
            finish();  // ← back screen
        });
    }
}

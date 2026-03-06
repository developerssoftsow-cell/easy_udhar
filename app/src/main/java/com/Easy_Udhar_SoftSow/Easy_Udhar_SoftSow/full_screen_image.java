package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.Easy_Udhar_SoftSow.R;


public class full_screen_image extends AppCompatActivity {

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);
        ImageButton btnClose = findViewById(R.id.btnClose);
        Button btnRemoveImage = findViewById(R.id.btnRemoveImage);

        // Get image path from intent
        imagePath = getIntent().getStringExtra("image_path");

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                fullScreenImageView.setImageBitmap(bitmap);
                btnRemoveImage.setVisibility(View.VISIBLE);
            }
        }

        // Close button - sirf close karega
        btnClose.setOnClickListener(v -> finish());

        // Remove button - image remove karega aur result return karega
        btnRemoveImage.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("remove_image", true);
            resultIntent.putExtra("image_path", imagePath);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Tap anywhere to close
        fullScreenImageView.setOnClickListener(v -> finish());
    }
}
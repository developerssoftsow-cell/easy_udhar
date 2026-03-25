package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.customer_class;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class profile_screen extends AppCompatActivity {

    private TextView tvNameTitle, tvNaam, tvPhone;
    private ImageView btnBack, profileImage, editIcon;
    private Button btnDelete, btnEdit;
    private data_holder dbHelper;
    private int customerId;
    private String customerName;

    // 🔹 NEW: Profile image variables
    private String currentProfileImagePath = "";
    private Uri currentImageUri;

    // 🔹 NEW: Permission constants
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    private static final int CAMERA_PERMISSION_REQUEST = 300;
    private static final int STORAGE_PERMISSION_REQUEST = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        // Initialize Database Helper
        dbHelper = new data_holder(this);

        // Get customer data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            customerId = intent.getIntExtra("customer_id", -1);
            customerName = intent.getStringExtra("customer_name");

            Log.d("ProfileScreen", "📋 Customer ID: " + customerId + ", Name: " + customerName);
        }

        if (customerId == -1) {
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load customer data
        loadCustomerData();
    }

    private void initializeViews() {
        tvNameTitle = findViewById(R.id.tvNameTitle);
        tvNaam = findViewById(R.id.tvNaam);
        tvPhone = findViewById(R.id.tvPhone);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);

        // 🔹 NEW: Profile image views
        profileImage = findViewById(R.id.profileImage);
        editIcon = findViewById(R.id.editIcon);
    }

    private void setupClickListeners() {
        // Back Button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Delete Button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 🔹 Delete alert box show karo
                showDeleteAlert();
            }
        });

        // Edit Button
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 🔹 Edit functionality - add_screen open karo edit mode mein
                openEditScreen();
            }
        });

        // 🔹 NEW: Naam par click karne par bhi edit screen open ho
        tvNaam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditScreen();
            }
        });

        // 🔹 NEW: Phone par click karne par bhi edit screen open ho
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditScreen();
            }
        });

        // 🔹 NEW: Edit Icon par click listener - Profile image change karne ke liye
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerBottomSheet();
            }
        });

        // 🔹 NEW: Profile image par bhi click listener
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerBottomSheet();
            }
        });
    }

    // 🔹 NEW: Image Picker Bottom Sheet
    private void showImagePickerBottomSheet() {
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom_add_image, null);

        // ✅ Dialog ka istemal karein
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(sheetView);

        // ✅ Window settings
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);

            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        dialog.show();

        LinearLayout optionCamera = sheetView.findViewById(R.id.optionCamera);
        LinearLayout optionGallery = sheetView.findViewById(R.id.optionGallery);

        optionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openCameraForProfile();
            }
        });

        optionGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openGalleryForProfile();
            }
        });
    }

    // 🔹 NEW: Camera for Profile Image
    private void openCameraForProfile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startCameraForProfile();
        }
    }

    private void startCameraForProfile() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            currentImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider",
                    photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    // 🔹 NEW: Gallery for Profile Image
    private void openGalleryForProfile() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_REQUEST);
            } else {
                startGalleryForProfile();
            }
        } else {
            // Android 12 or lower
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST);
            } else {
                startGalleryForProfile();
            }
        }
    }

    private void startGalleryForProfile() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    // 🔹 NEW: Create temporary image file
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "PROFILE_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentProfileImagePath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 NEW: Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraForProfile();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryForProfile();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 NEW: Handle activity results for image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                // Camera result
                handleCameraResultForProfile();
            } else if (requestCode == GALLERY_REQUEST) {
                // Gallery result
                handleGalleryResultForProfile(data);
            } else if (requestCode == 100) {
                // Edit screen se result
                if (resultCode == RESULT_OK) {
                    // Customer update hua hai - refresh karo
                    loadCustomerData();

                    // Customer detail screen ko bhi update karne ke liye result bhejo
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                }
            }
        }
    }

    // 🔹 NEW: Handle camera result for profile
    private void handleCameraResultForProfile() {
        if (currentProfileImagePath != null && !currentProfileImagePath.isEmpty()) {
            File imgFile = new File(currentProfileImagePath);
            if (imgFile.exists()) {
                displayProfileImage(currentProfileImagePath);
                saveProfileImageToDatabase();
                Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Image not saved", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 NEW: Handle gallery result for profile
    private void handleGalleryResultForProfile(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    currentProfileImagePath = picturePath;
                    displayProfileImage(picturePath);
                    saveProfileImageToDatabase();
                    Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                } else {
                    // Alternative method if cursor is null
                    currentProfileImagePath = selectedImageUri.toString();
                    displayProfileImageFromUri(selectedImageUri);
                    saveProfileImageToDatabase();
                    Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 NEW: Display Profile Image from file path
    private void displayProfileImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                profileImage.setImageBitmap(circularBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying image", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 NEW: Display Profile Image from URI
    private void displayProfileImageFromUri(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                profileImage.setImageBitmap(circularBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 NEW: Circular Bitmap Banaye
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, min, min);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, min/2, min/2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,
                new Rect((width - min)/2, (height - min)/2, (width + min)/2, (height + min)/2),
                rect,
                paint);

        return output;
    }

    // 🔹 NEW: Database mein Profile Image Save Karein
    private void saveProfileImageToDatabase() {
        if (currentProfileImagePath != null && !currentProfileImagePath.isEmpty()) {
            boolean success = dbHelper.updateCustomerProfileImage(customerId, currentProfileImagePath);
            if (success) {
                Toast.makeText(this, "Profile photo saved!", Toast.LENGTH_SHORT).show();

                // 🔹 Refresh other screens ko batao
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profile_updated", true);
                setResult(RESULT_OK, resultIntent);

                Log.d("ProfileScreen", "✅ Profile image saved to database: " + currentProfileImagePath);
            } else {
                Toast.makeText(this, "Failed to save profile photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 NEW: Delete Alert Box Show Karne Ka Method
    private void showDeleteAlert() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_alert);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Transparent background
        dialog.setCancelable(false);

        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // DELETE button click
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCustomer();
                dialog.dismiss();
            }
        });

        // CANCEL button click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(profile_screen.this, "Delete canceled", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // 🔹 NEW: Customer Delete Karne Ka Method
    private void deleteCustomer() {
        // Database se customer delete karein
        boolean deleteSuccess = dbHelper.deleteCustomer(customerId);

        if (deleteSuccess) {
            Toast.makeText(this, "✅ Customer delete ho gaya!", Toast.LENGTH_SHORT).show();

            // Dashboard par wapas jao
            Intent intent = new Intent(this, dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "❌ Customer delete nahi ho paya!", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 NEW: Edit screen open karne ka method
    private void openEditScreen() {
        // Database se customer details load karein
        customer_class customer = dbHelper.getCustomerById(customerId);

        if (customer != null) {
            Intent intent = new Intent(profile_screen.this, add_screen.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("customer_id", customerId);
            intent.putExtra("customer_name", customer.getName());
            intent.putExtra("customer_phone", customer.getPhone());
            startActivityForResult(intent, 100); // Edit mode ka request code
        } else {
            Toast.makeText(this, "Customer data load nahi ho paya!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCustomerData() {
        // Database se customer details load karein
        customer_class customer = dbHelper.getCustomerById(customerId);

        if (customer != null) {
            // Set name in both places
            String name = customer.getName();
            tvNameTitle.setText(name);
            tvNaam.setText(name);

            // Set phone number (agar available hai to)
            String phone = customer.getPhone();
            if (phone != null && !phone.isEmpty() && !phone.equals("null")) {
                tvPhone.setText(phone);
                tvPhone.setTextColor(getResources().getColor(android.R.color.black));
            } else {
                // Phone number nahi hai
                tvPhone.setText("Phone number not available");
                tvPhone.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            // 🔹 NEW: Profile image load karein
            String profileImagePath = customer.getProfileImagePath();
            if (profileImagePath != null && !profileImagePath.isEmpty() && !profileImagePath.equals("null")) {
                displayProfileImage(profileImagePath);
                currentProfileImagePath = profileImagePath;
                Log.d("ProfileScreen", "✅ Profile image loaded: " + profileImagePath);
            } else {
                // Default profile image
                profileImage.setImageResource(R.drawable.profile);
                currentProfileImagePath = "";
                Log.d("ProfileScreen", "ℹ️ No profile image, using default");
            }

            Log.d("ProfileScreen", "✅ Customer loaded - Name: " + name + ", Phone: " + phone);
        } else {
            Toast.makeText(this, "Customer data load nahi ho paya!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Customer detail screen par wapas jao
        Intent intent = new Intent(this, customer_detail.class);
        intent.putExtra("customer_id", customerId);
        intent.putExtra("customer_name", customerName);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
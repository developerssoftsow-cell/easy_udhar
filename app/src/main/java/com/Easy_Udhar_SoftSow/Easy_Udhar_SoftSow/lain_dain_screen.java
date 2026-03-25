package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.personal_transaction;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.transaction_class;
import com.Easy_Udhar_SoftSow.R;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class lain_dain_screen extends AppCompatActivity {

    private TextView tvExpression, tvResult, tvSelectedDate, titleText;
    private EditText tafaseelInput;
    private GridLayout calculatorLayout;
    private Button btnSave;
    private ImageButton btnBackspace;
    private LinearLayout dateBillsLayout, selectedImageLayout, addBillsLayout;
    private ImageView selectedImageView;
    private String mode = ""; // "liye" or "diye"
    private StringBuilder currentInput = new StringBuilder();

    // Camera and Gallery constants
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    private static final int CAMERA_PERMISSION_REQUEST = 300;
    private static final int STORAGE_PERMISSION_REQUEST = 400;

    private String currentImagePath = "";
    private Uri currentImageUri;

    // 🔹 Edit mode fields (UPDATED)
    private boolean isEditMode = false;
    private transaction_class existingTransaction;
    private personal_transaction existingPersonalTransaction; // NEW: For personal transaction edit

    // 🔹 Cursor blinking fields
    private boolean showCursor = true;
    private boolean isBlinkingActive = false;
    private final Handler handler = new Handler();
    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBlinkingActive) return;
            if (tvResult != null) {
                String base = "Rs " + (currentInput.length() > 0 ? currentInput.toString() : "0");
                tvResult.setText(showCursor ? base + "|" : base);
                showCursor = !showCursor;
            }
            handler.postDelayed(this, 500);
        }
    };

    // 🔹 NEW: Max digits limit constants
    private static final int MAX_DIGITS = 8;
    private static final String MAX_AMOUNT_MESSAGE = "Maximum 8 digits allowed!";

    // 🔹 NEW: Source tracking
    private String source = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lain_dain_screen);

        // 🔹 Initialize views
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        tafaseelInput = findViewById(R.id.tafaseelInput);
        calculatorLayout = findViewById(R.id.calculatorLayout);
        btnSave = findViewById(R.id.btnSave);
        btnBackspace = findViewById(R.id.btnBackspace);
        dateBillsLayout = findViewById(R.id.dateBillsLayout);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        titleText = findViewById(R.id.title);

        // 🔹 Initialize addBillsLayout
        addBillsLayout = findViewById(R.id.addBillsLayout);

        // 🔹 Initialize image views
        selectedImageLayout = findViewById(R.id.selectedImageLayout);
        selectedImageView = findViewById(R.id.selectedImageView);

        // --- Image preview par click listener
        selectedImageView.setOnClickListener(v -> {
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                Intent intent = new Intent(this, full_screen_image.class);
                intent.putExtra("image_path", currentImagePath);
                startActivityForResult(intent, 500); // New request code for full screen
            }
        });

        // 🔹 Get mode and source from Intent
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            mode = receivedIntent.getStringExtra("mode");
            source = receivedIntent.getStringExtra("source"); // Get source

            // 🔹 CHECK IF EDIT MODE HAI
            isEditMode = receivedIntent.getBooleanExtra("edit_mode", false);

            // Check for customer transaction edit
            if (isEditMode && receivedIntent.hasExtra("existing_transaction")) {
                existingTransaction = (transaction_class) receivedIntent.getSerializableExtra("existing_transaction");
                if (existingTransaction != null) {
                    loadExistingTransaction(existingTransaction);
                }
            }
            // Check for personal transaction edit (NEW)
            else if (isEditMode && receivedIntent.hasExtra("existing_personal_transaction")) {
                existingPersonalTransaction = (personal_transaction) receivedIntent.getSerializableExtra("existing_personal_transaction");
                if (existingPersonalTransaction != null) {
                    loadExistingPersonalTransaction(existingPersonalTransaction);
                }
            }
        }

        // 🔹 Set title text based on mode
        if ("diye".equals(mode)) {
            titleText.setText("Maine Diye");
        } else {
            titleText.setText("Maine Liye");
        }

        // 🔹 Default Save button color
        btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));

        // 🔹 Set current date & time by default (only if not in edit mode)
        if (!isEditMode) {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault());
            String currentDateTime = sdf.format(Calendar.getInstance().getTime());
            tvSelectedDate.setText(currentDateTime);
        }

        // 🔹 Check source and hide add bills if from report screen
        checkSourceAndHideAddBills();

        // 🔹 Initially show custom calculator and hide system keyboard
        hideKeyboard(tvResult);
        calculatorLayout.setVisibility(View.VISIBLE);

        // --- Handle back arrow click
        findViewById(R.id.backArrow).setOnClickListener(v -> {
            onBackPressed();
        });

        // --- Handle tafaseel input focus (hide/show calculator)
        tafaseelInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                stopBlinking();
                calculatorLayout.setVisibility(View.GONE);
                showKeyboard(tafaseelInput);

                // Keyboard ke upar SAVE button dikhaye
                btnSave.setVisibility(View.VISIBLE);
            } else {
                hideKeyboard(v);
                v.postDelayed(() -> {
                    calculatorLayout.setVisibility(View.VISIBLE);
                    startBlinking();
                    updateDisplay();
                }, 120);
            }
        });


        // --- Handle tvResult click (bring calculator back)
        tvResult.setOnClickListener(v -> {
            tafaseelInput.clearFocus();
            hideKeyboard(v);
            v.postDelayed(() -> {
                calculatorLayout.setVisibility(View.VISIBLE);
                startBlinking();
                updateDisplay();
            }, 120);
        });

        // --- Calculator button setup
        for (int i = 0; i < calculatorLayout.getChildCount(); i++) {
            View view = calculatorLayout.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setOnClickListener(v -> handleButtonClick(button.getText().toString()));
            }
        }

        // --- Backspace button
        btnBackspace.setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                updateDisplay();
            }
        });

        // --- Date picker click
        findViewById(R.id.dateLayout).setOnClickListener(v -> showDatePicker());

        // --- Add bills click (only if visible)
        addBillsLayout.setOnClickListener(v -> {
            if (addBillsLayout.getVisibility() == View.VISIBLE) {
                showAddImageBottomSheet();
            }
        });

        // 🔹 UPDATED: Save button click (BOTH CUSTOMER AND PERSONAL TRANSACTIONS)
        btnSave.setOnClickListener(v -> {
            String resultText = tvResult.getText().toString().replace("Rs ", "").replace("|", "").trim();
            if (resultText.isEmpty() || resultText.equals("0")) {
                Toast.makeText(this, "Pehle amount likho!", Toast.LENGTH_SHORT).show();
                return;
            }

            String tafseel = tafaseelInput.getText().toString().trim();
            String date = tvSelectedDate.getText().toString();

            // Check karo kahan se aaye hain
            if ("report_screen".equals(source)) {
                // 🔹 PERSONAL TRANSACTION HANDLING
                handlePersonalTransactionSave(resultText, tafseel, date);
            } else {
                // 🔹 CUSTOMER TRANSACTION HANDLING
                handleCustomerTransactionSave(resultText, tafseel, date);
            }
        });
    }

    // 🔹 NEW: Check source and hide add bills if from report screen
    private void checkSourceAndHideAddBills() {
        if ("report_screen".equals(source)) {
            // Report screen se aaye hain - Add bills hide karo
            if (addBillsLayout != null) {
                addBillsLayout.setVisibility(View.GONE);

                // Date layout ko full width de do
                LinearLayout dateLayout = findViewById(R.id.dateLayout);
                if (dateLayout != null) {
                    dateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f  // Full weight
                    ));
                }

                Log.d("SourceCheck", "✅ Add bills hidden - Source: report_screen");
            }
        } else {
            // Other sources se aaye hain - Add bills show karo
            if (addBillsLayout != null) {
                addBillsLayout.setVisibility(View.VISIBLE);
                Log.d("SourceCheck", "✅ Add bills shown - Source: " + source);
            }
        }
    }

    // 🔹 EXISTING CUSTOMER TRANSACTION LOAD KARNE KA METHOD
    private void loadExistingTransaction(transaction_class existingTransaction) {
        Log.d("EditMode", "🔄 Loading existing customer transaction for editing");

        // Amount set karo
        if (existingTransaction.getAmount() != null) {
            currentInput.setLength(0);
            String amount = existingTransaction.getAmount().replace("Rs", "").replace(" ", "").trim();
            currentInput.append(amount);
            updateDisplay();
            calculateResult(); // Calculate karo taki extra fields show ho jayein
        }

        // Description set karo
        if (existingTransaction.getDescription() != null && !existingTransaction.getDescription().isEmpty()) {
            tafaseelInput.setText(existingTransaction.getDescription());
        }

        // Date set karo
        if (existingTransaction.getDateTime() != null && !existingTransaction.getDateTime().isEmpty()) {
            tvSelectedDate.setText(existingTransaction.getDateTime());
        }

        // Image set karo (agar hai to)
        if (existingTransaction.hasImage()) {
            currentImagePath = existingTransaction.getImagePath();
            displaySelectedImage(currentImagePath, "Existing_Image");
        }

        // Save button text change karo (Edit ke liye)
        btnSave.setText("UPDATE");

        Log.d("EditMode", "✅ Existing customer transaction loaded - Amount: " + existingTransaction.getAmount() +
                ", Description: " + existingTransaction.getDescription() +
                ", Image: " + (existingTransaction.hasImage() ? "Yes" : "No"));
    }

    // 🔹 EXISTING PERSONAL TRANSACTION LOAD KARNE KA METHOD (NEW)
    private void loadExistingPersonalTransaction(personal_transaction existingTransaction) {
        Log.d("EditMode", "🔄 Loading existing personal transaction for editing");

        // Amount set karo
        if (existingTransaction.getAmount() != null) {
            currentInput.setLength(0);
            String amount = existingTransaction.getAmount().replace("Rs", "").replace(" ", "").trim();
            currentInput.append(amount);
            updateDisplay();
            calculateResult(); // Calculate karo taki extra fields show ho jayein
        }

        // Description set karo
        if (existingTransaction.getDescription() != null && !existingTransaction.getDescription().isEmpty()) {
            tafaseelInput.setText(existingTransaction.getDescription());
        }

        // Date set karo
        if (existingTransaction.getDateTime() != null && !existingTransaction.getDateTime().isEmpty()) {
            tvSelectedDate.setText(existingTransaction.getDateTime());
        }

        // Image set karo (agar hai to)
        if (existingTransaction.hasImage()) {
            currentImagePath = existingTransaction.getImagePath();
            displaySelectedImage(currentImagePath, "Existing_Image");
        }

        // Save button text change karo (Edit ke liye)
        btnSave.setText("UPDATE");

        Log.d("EditMode", "✅ Existing personal transaction loaded - ID: " + existingTransaction.getId() +
                ", Amount: " + existingTransaction.getAmount() +
                ", Description: " + existingTransaction.getDescription() +
                ", Image: " + (existingTransaction.hasImage() ? "Yes" : "No"));
    }

    // 🔹 PERSONAL TRANSACTION SAVE/UPDATE HANDLE KARNE KA METHOD
    private void handlePersonalTransactionSave(String resultText, String tafseel, String date) {
        personal_transaction personalTransaction = new personal_transaction();
        personalTransaction.setType(mode);
        personalTransaction.setAmount(resultText);
        personalTransaction.setDescription(tafseel);
        personalTransaction.setDateTime(date);

        // Personal transactions mein image sirf tab save karo agar add bills visible hai
        if (addBillsLayout.getVisibility() == View.VISIBLE) {
            personalTransaction.setImagePath(currentImagePath != null ? currentImagePath : "");
        } else {
            personalTransaction.setImagePath(""); // No image for personal transactions
        }

        // 🔹 CHECK IF EDIT MODE HAI (PERSONAL TRANSACTION)
        if (isEditMode && existingPersonalTransaction != null) {
            // EDIT MODE - UPDATE EXISTING PERSONAL TRANSACTION
            personalTransaction.setId(existingPersonalTransaction.getId());

            data_holder dbHelper = new data_holder(this);
            boolean updateSuccess = dbHelper.updatePersonalTransaction(personalTransaction);
            dbHelper.close();

            if (updateSuccess) {
                String msg = "diye".equals(mode)
                        ? "💸 Expense Updated ✅"
                        : "💰 Income Updated ✅";
                Toast.makeText(this, msg + "\nAmount: Rs " + resultText, Toast.LENGTH_LONG).show();

                Intent resultIntent = new Intent();
                // ✅ ALL THREE FLAGS SET KARO
                resultIntent.putExtra("updated_personal_transaction", personalTransaction);
                resultIntent.putExtra("personal_transaction_updated", true);
                resultIntent.putExtra("transaction_added", true); // Yeh bhi important hai

                setResult(RESULT_OK, resultIntent);
                Log.d("LainDain", "✅ Personal transaction updated, result sent");
            } else {
                Toast.makeText(this, "Error updating transaction", Toast.LENGTH_SHORT).show();
            }
        } else {
            // NEW PERSONAL TRANSACTION MODE
            data_holder dbHelper = new data_holder(this);
            long id = dbHelper.insertPersonalTransaction(personalTransaction);
            dbHelper.close();

            if (id > 0) {
                String msg = "diye".equals(mode)
                        ? "💸 Expense Saved ✅"
                        : "💰 Income Saved ✅";
                Toast.makeText(this, msg + "\nAmount: Rs " + resultText, Toast.LENGTH_LONG).show();

                Intent resultIntent = new Intent();
                // ✅ ALL FLAGS SET KARO
                resultIntent.putExtra("personal_transaction_added", true);
                resultIntent.putExtra("transaction_added", true);

                setResult(RESULT_OK, resultIntent);
                Log.d("LainDain", "✅ New personal transaction saved, result sent");
            } else {
                Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    // 🔹 CUSTOMER TRANSACTION SAVE/UPDATE HANDLE KARNE KA METHOD
    private void handleCustomerTransactionSave(String resultText, String tafseel, String date) {
        transaction_class transaction = new transaction_class();
        transaction.setType(mode);
        transaction.setAmount(resultText);
        transaction.setDescription(tafseel);
        transaction.setDateTime(date);
        transaction.setBalance("Bal. Rs. " + resultText);

        // 🔹 FIX: Proper image path handling
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            transaction.setImagePath(currentImagePath);
            Log.d("ImageDebug", "✅ Image path saved to transaction: " + currentImagePath);
        } else {
            transaction.setImagePath("");
            Log.d("ImageDebug", "❌ No image path available");
        }

        // 🔹 CHECK IF EDIT MODE HAI (CUSTOMER TRANSACTION)
        if (isEditMode) {
            // EDIT MODE - UPDATED TRANSACTION RETURN KARO
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_transaction", transaction);
            resultIntent.putExtra("old_transaction", existingTransaction);
            setResult(RESULT_OK, resultIntent);

            String msg = "diye".equals(mode)
                    ? "💸 Maine Diye Updated ✅"
                    : "💰 Maine Liye Updated ✅";
            Toast.makeText(this, msg + "\nAmount: Rs " + resultText, Toast.LENGTH_LONG).show();
        } else {
            // NEW TRANSACTION MODE - NAYA TRANSACTION BANAO
            Intent resultIntent = new Intent();
            resultIntent.putExtra("transaction", transaction);
            setResult(RESULT_OK, resultIntent);

            String msg = "diye".equals(mode)
                    ? "💸 Maine Diye Saved ✅"
                    : "💰 Maine Liye Saved ✅";
            Toast.makeText(this, msg + "\nAmount: Rs " + resultText, Toast.LENGTH_LONG).show();
        }

        finish();
    }

    // 🔹 UPDATED: Calculator button handling with max digits check
    private void handleButtonClick(String value) {
        switch (value) {
            case "C":
                currentInput.setLength(0);
                tvExpression.setVisibility(View.GONE);
                tvResult.setText("Rs 0|");
                tafaseelInput.setVisibility(View.GONE);
                dateBillsLayout.setVisibility(View.GONE);
                selectedImageLayout.setVisibility(View.GONE);
                currentImagePath = "";
                btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
                break;

            case "=":
                calculateResult();
                break;

            default:
                // 🔹 NEW: Check for maximum digits before appending
                if (isMaxDigitsReached(value)) {
                    showMaxDigitsToast();
                    return;
                }

                currentInput.append(value);
                updateDisplay();
                break;
        }
    }

    // 🔹 NEW: Check if maximum digits reached
    private boolean isMaxDigitsReached(String newInput) {
        // Agar operator ya special character hai to allow karo
        if (isOperator(newInput) || newInput.equals(".") || newInput.equals("=")) {
            return false;
        }

        // "00" ko handle karo
        if (newInput.equals("00")) {
            // Agar "00" add kar rahe hain to 2 digits count karo
            String tempInput = currentInput.toString() + "00";
            String digitsOnly = getDigitsOnly(tempInput);
            return digitsOnly.length() > MAX_DIGITS;
        }

        // Normal digit add kar rahe hain
        if (isDigit(newInput)) {
            String tempInput = currentInput.toString() + newInput;
            String digitsOnly = getDigitsOnly(tempInput);
            return digitsOnly.length() > MAX_DIGITS;
        }

        return false;
    }

    // 🔹 NEW: Get only digits from input (remove operators and decimal point)
    private String getDigitsOnly(String input) {
        // Remove all non-digit characters except decimal point for counting
        return input.replaceAll("[^0-9]", "");
    }

    // 🔹 NEW: Check if input is a digit
    private boolean isDigit(String input) {
        return input.matches("[0-9]");
    }

    // 🔹 NEW: Check if input is an operator
    private boolean isOperator(String input) {
        return input.equals("+") || input.equals("-") ||
                input.equals("×") || input.equals("÷");
    }

    // 🔹 NEW: Show max digits toast
    private void showMaxDigitsToast() {
        // Custom toast design ke liye
        Toast toast = Toast.makeText(this, MAX_AMOUNT_MESSAGE, Toast.LENGTH_SHORT);

        // Toast position set karo (top mein)
        toast.setGravity(android.view.Gravity.TOP|android.view.Gravity.CENTER_HORIZONTAL, 0, 100);

        // Optional: Toast view customize karo
        View toastView = toast.getView();
        if (toastView != null) {
            toastView.setBackgroundResource(R.drawable.toast_background);
            TextView textView = toastView.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setTextColor(android.graphics.Color.WHITE);
                textView.setTextSize(14);
                textView.setPadding(20, 15, 20, 15);
            }
        }

        toast.show();

        // Optional: Vibration feedback
        try {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(50); // 50 milliseconds
            }
        } catch (Exception e) {
            // Ignore vibration errors
        }
    }

    // 🔹 UPDATED: Expression evaluation with max digits check
    private void calculateResult() {
        if (currentInput.length() == 0) return;

        String expressionText = currentInput.toString()
                .replace("×", "*")
                .replace("÷", "/");

        try {
            Expression expression = new ExpressionBuilder(expressionText).build();
            double result = expression.evaluate();

            String formatted = (result == (long) result)
                    ? String.valueOf((long) result)
                    : String.format("%.2f", result);

            // 🔹 NEW: Check if result has more than 8 digits
            String digitsOnly = getDigitsOnly(formatted);
            if (digitsOnly.length() > MAX_DIGITS) {
                showMaxDigitsToast();
                return;
            }

            tvExpression.setText(expressionText);
            tvExpression.setVisibility(View.VISIBLE);
            tvResult.setText("Rs " + formatted + "|");

            currentInput.setLength(0);
            currentInput.append(formatted);

            toggleExtraFields();
            updateSaveButtonColor();

        } catch (Exception e) {
            // Expression error handle karo
            Toast.makeText(this, "Invalid calculation", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Update display
    private void updateDisplay() {
        if (currentInput.length() == 0) {
            tvResult.setText("Rs 0|");
            tvExpression.setVisibility(View.GONE);
            tafaseelInput.setVisibility(View.GONE);
            dateBillsLayout.setVisibility(View.GONE);
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        } else {
            tvResult.setText("Rs " + currentInput.toString() + "|");
            tvExpression.setVisibility(View.GONE);
            toggleExtraFields();
            updateSaveButtonColor();
        }
    }

    // 🔹 Show/hide tafseel and date section
    private void toggleExtraFields() {
        boolean hasAmount = currentInput.length() > 0;
        tafaseelInput.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
        dateBillsLayout.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
    }

    // 🔹 Save button color logic
    private void updateSaveButtonColor() {
        if (currentInput.length() > 0) {
            if ("diye".equals(mode)) {
                btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
            } else {
                btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
            }
        } else {
            btnSave.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        }
    }

    private void showAddImageBottomSheet() {
        // Sirf tab show karo agar add bills visible hai
        if (addBillsLayout.getVisibility() != View.VISIBLE) {
            return;
        }

        // Inflate bottom sheet view
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_bottom_add_image, null);

        // ✅ Dialog ka istemal karein
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(sheetView);

        // ✅ Window settings
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        dialog.show();

        // Handle options
        LinearLayout optionCamera = sheetView.findViewById(R.id.optionCamera);
        LinearLayout optionGallery = sheetView.findViewById(R.id.optionGallery);

        optionCamera.setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });

        optionGallery.setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });
    }

    // Camera open method
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a file to save the image
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

    // Gallery open method
    private void openGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_REQUEST);
            } else {
                startGallery();
            }
        } else {
            // Android 12 or lower
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST);
            } else {
                startGallery();
            }
        }
    }

    private void startGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    // Create temporary image file
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(null);
            File imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentImagePath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle camera and gallery results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                // Camera result
                handleCameraResult();
            } else if (requestCode == GALLERY_REQUEST) {
                // Gallery result
                handleGalleryResult(data);
            } else if (requestCode == 500) {
                // Full screen activity se aaya hai
                if (data != null && data.getBooleanExtra("remove_image", false)) {
                    // User ne remove image button click kiya hai
                    removeSelectedImage();
                }
            }
        }
    }

    private void handleCameraResult() {
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            File imgFile = new File(currentImagePath);
            if (imgFile.exists()) {
                String fileName = "Camera_" + new SimpleDateFormat("ddMMyy_HHmm", Locale.getDefault()).format(new Date()) + ".jpg";
                displaySelectedImage(currentImagePath, fileName);
            }
        } else {
            Toast.makeText(this, "Image not saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                // Get file path from URI
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    currentImagePath = picturePath;
                    currentImageUri = selectedImageUri;

                    // Extract file name
                    String fileName = "Gallery_" + new SimpleDateFormat("ddMMyy_HHmm", Locale.getDefault()).format(new Date()) + ".jpg";
                    File file = new File(picturePath);
                    if (file.exists()) {
                        fileName = file.getName();
                    }

                    displaySelectedImage(picturePath, fileName);
                } else {
                    // Alternative method if cursor is null
                    currentImagePath = selectedImageUri.toString();
                    displaySelectedImageFromUri(selectedImageUri, "Gallery_Image.jpg");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Alternative approach
                currentImagePath = selectedImageUri.toString();
                displaySelectedImageFromUri(selectedImageUri, "Gallery_Image.jpg");
            }
        }
    }

    // Display selected image in small preview from file path
    private void displaySelectedImage(String imagePath, String fileName) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                selectedImageView.setImageBitmap(bitmap);
                selectedImageLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Display selected image from URI
    private void displaySelectedImageFromUri(Uri imageUri, String fileName) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap != null) {
                selectedImageView.setImageBitmap(bitmap);
                selectedImageLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove selected image
    private void removeSelectedImage() {
        selectedImageLayout.setVisibility(View.GONE);
        currentImagePath = "";
        currentImageUri = null;
    }

    // 🔹 Date picker
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    showTimePicker(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // 🔹 Time picker
    private void showTimePicker(Calendar selectedDate) {
        int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDate.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDate.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat sdf = new SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault());
                    String formatted = sdf.format(selectedDate.getTime());
                    tvSelectedDate.setText(formatted);
                },
                hour, minute, false
        );

        timePickerDialog.show();
    }

    // 🔹 Blinking controls
    private void startBlinking() {
        if (isBlinkingActive) return;
        isBlinkingActive = true;
        handler.post(cursorRunnable);
    }

    private void stopBlinking() {
        isBlinkingActive = false;
        handler.removeCallbacks(cursorRunnable);
        if (tvResult != null) tvResult.setText("Rs " + (currentInput.length() > 0 ? currentInput : "0"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBlinking();
    }

    // 🔹 Keyboard helpers
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        // If user presses back without saving, just finish
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
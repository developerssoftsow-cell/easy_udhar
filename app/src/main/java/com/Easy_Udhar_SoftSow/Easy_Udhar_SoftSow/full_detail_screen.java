package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.transaction_class;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.personal_transaction;
import com.Easy_Udhar_SoftSow.R;

import java.io.File;

public class full_detail_screen extends AppCompatActivity {

    private TextView tvTypeText, tvAmount, tvDescription, tvDate;
    private ImageView ivTypeIcon, ivAttachment, btnBack, btnDelete;
    private LinearLayout descriptionLayout, amountField;

    // दोनों प्रकार के transactions handle करने के लिए
    private transaction_class currentTransaction;
    private personal_transaction currentPersonalTransaction;

    private data_holder dbHelper;
    private int customerId = -1;
    private String transactionType = ""; // "customer" or "personal"

    private static final int FULL_SCREEN_IMAGE_REQUEST = 1001;
    private static final int EDIT_TRANSACTION_REQUEST = 1002;
    private static final int EDIT_PERSONAL_TRANSACTION_REQUEST = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_detail_screen);

        Log.d("FullDetailDebug", "✅ Activity started");

        // Initialize Database Helper
        dbHelper = new data_holder(this);

        initializeViews();
        loadTransactionData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvTypeText = findViewById(R.id.tvTypeText);
        tvAmount = findViewById(R.id.tvAmount);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        ivTypeIcon = findViewById(R.id.ivTypeIcon);
        ivAttachment = findViewById(R.id.ivAttachment);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        amountField = findViewById(R.id.amountField);
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Check क्या यह customer transaction है
            if (intent.hasExtra("transaction")) {
                currentTransaction = (transaction_class) intent.getSerializableExtra("transaction");
                customerId = intent.getIntExtra("customer_id", -1);
                transactionType = "customer";

                if (currentTransaction != null) {
                    Log.d("FullDetailDebug", "✅ Customer Transaction received: " + currentTransaction.getType());
                    displayCustomerTransactionDetails(currentTransaction);
                }
            }
            // Check क्या यह personal transaction है
            else if (intent.hasExtra("personal_transaction")) {
                currentPersonalTransaction = (personal_transaction) intent.getSerializableExtra("personal_transaction");
                transactionType = "personal";

                if (currentPersonalTransaction != null) {
                    Log.d("FullDetailDebug", "✅ Personal Transaction received: " + currentPersonalTransaction.getType());
                    displayPersonalTransactionDetails(currentPersonalTransaction);
                }
            }
        }
    }

    private void displayCustomerTransactionDetails(transaction_class transaction) {
        // Set transaction type
        setupTransactionType(transaction.getType());

        // Set amount
        if (transaction.getAmount() != null) {
            tvAmount.setText("Rs " + transaction.getAmount());
        }

        // Set description
        setupDescription(transaction.getDescription());

        // Set date
        if (transaction.getDateTime() != null) {
            tvDate.setText(transaction.getDateTime());
        }

        // Load and display image
        if (transaction.hasImage()) {
            setupTransactionImage(transaction.getImagePath());
        } else {
            hideImageSection();
        }
    }

    private void displayPersonalTransactionDetails(personal_transaction transaction) {
        // Set transaction type
        setupTransactionType(transaction.getType());

        // Set amount
        if (transaction.getAmount() != null) {
            tvAmount.setText("Rs " + transaction.getAmount());
        }

        // Set description
        setupDescription(transaction.getDescription());

        // Set date
        if (transaction.getDateTime() != null) {
            tvDate.setText(transaction.getDateTime());
        }

        // Load and display image
        if (transaction.hasImage()) {
            setupTransactionImage(transaction.getImagePath());
        } else {
            hideImageSection();
        }
    }

    private void setupTransactionType(String type) {
        if ("liye".equals(type)) {
            tvTypeText.setText("Maine Liye");
            tvTypeText.setTextColor(getResources().getColor(R.color.green));
            ivTypeIcon.setImageResource(R.drawable.arrow_up);
            ivTypeIcon.setColorFilter(getResources().getColor(R.color.green));
        } else {
            tvTypeText.setText("Maine Diye");
            tvTypeText.setTextColor(getResources().getColor(R.color.red));
            ivTypeIcon.setImageResource(R.drawable.arrow_down);
            ivTypeIcon.setColorFilter(getResources().getColor(R.color.red));
        }
    }

    private void setupDescription(String description) {
        if (description != null && !description.trim().isEmpty() && !description.equals("null")) {
            tvDescription.setText(description);
            descriptionLayout.setVisibility(View.VISIBLE);
        } else {
            descriptionLayout.setVisibility(View.GONE);
        }
    }

    private void setupTransactionImage(String imagePath) {
        Log.d("ImageLoad", "🔄 Setting up image: " + imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (bitmap != null) {
                        ivAttachment.setImageBitmap(bitmap);
                        ivAttachment.setVisibility(View.VISIBLE);
                        setupImageClickListener(imagePath);
                    } else {
                        hideImageSection();
                    }
                } else {
                    hideImageSection();
                }
            } catch (Exception e) {
                Log.e("ImageLoad", "❌ Error loading image: " + e.getMessage());
                hideImageSection();
            }
        } else {
            hideImageSection();
        }
    }

    private void hideImageSection() {
        if (ivAttachment != null) {
            ivAttachment.setVisibility(View.GONE);
        }
    }

    private void setupImageClickListener(String imagePath) {
        ivAttachment.setOnClickListener(v -> {
            // Check which transaction type has image
            boolean hasImage = false;
            String currentImagePath = "";

            if (transactionType.equals("customer") && currentTransaction != null) {
                hasImage = currentTransaction.hasImage();
                currentImagePath = currentTransaction.getImagePath();
            } else if (transactionType.equals("personal") && currentPersonalTransaction != null) {
                hasImage = currentPersonalTransaction.hasImage();
                currentImagePath = currentPersonalTransaction.getImagePath();
            }

            if (hasImage) {
                // Full screen image view open karo
                Intent fullScreenIntent = new Intent(this, full_screen_image.class);
                fullScreenIntent.putExtra("image_path", currentImagePath);
                startActivityForResult(fullScreenIntent, FULL_SCREEN_IMAGE_REQUEST);
            }
        });
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            Log.d("FullDetailDebug", "🗑️ Delete button clicked");
            showDeleteConfirmation();
        });

        // Edit button - EDIT MODE LAUNCH KARO
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            launchEditMode();
        });

//        // Share button
//        findViewById(R.id.btnShare).setOnClickListener(v -> {
//            Toast.makeText(this, "Share functionality coming soon", Toast.LENGTH_SHORT).show();
//        });

        // 🔹 AMOUNT FIELD PAR CLICK LISTENER - EDIT MODE LAUNCH KARO
        amountField.setOnClickListener(v -> {
            launchEditMode();
        });

        // 🔹 DESCRIPTION FIELD PAR CLICK LISTENER - EDIT MODE LAUNCH KARO
        descriptionLayout.setOnClickListener(v -> {
            launchEditMode();
        });

        // 🔹 DATE FIELD PAR CLICK LISTENER - EDIT MODE LAUNCH KARO
        findViewById(R.id.tvDate).setOnClickListener(v -> {
            launchEditMode();
        });
    }

    // 🔹 EDIT MODE LAUNCH KARNE KA METHOD
    private void launchEditMode() {
        if (transactionType.equals("customer")) {
            launchEditCustomerTransaction();
        } else if (transactionType.equals("personal")) {
            launchEditPersonalTransaction();
        }
    }

    private void launchEditCustomerTransaction() {
        if (currentTransaction == null) {
            Toast.makeText(this, "Transaction data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent editIntent = new Intent(this, lain_dain_screen.class);
        editIntent.putExtra("mode", currentTransaction.getType());
        editIntent.putExtra("customer_id", customerId);
        editIntent.putExtra("customer_name", getIntent().getStringExtra("customer_name"));

        // 🔹 EXISTING TRANSACTION DATA PASS KARO (EDIT MODE KE LIYE)
        editIntent.putExtra("edit_mode", true);
        editIntent.putExtra("existing_transaction", currentTransaction);

        startActivityForResult(editIntent, EDIT_TRANSACTION_REQUEST);
    }

    private void launchEditPersonalTransaction() {
        if (currentPersonalTransaction == null) {
            Toast.makeText(this, "Transaction data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent editIntent = new Intent(this, lain_dain_screen.class);
        editIntent.putExtra("mode", currentPersonalTransaction.getType());
        editIntent.putExtra("source", "report_screen"); // Important: यह report screen से है

        // 🔹 EXISTING PERSONAL TRANSACTION DATA PASS KARO (EDIT MODE KE LIYE)
        editIntent.putExtra("edit_mode", true);
        editIntent.putExtra("existing_personal_transaction", currentPersonalTransaction);

        startActivityForResult(editIntent, EDIT_PERSONAL_TRANSACTION_REQUEST);
    }

    // 🔹 Full screen image activity se result handle karo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FULL_SCREEN_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("remove_image", false)) {
                // User ne remove image button click kiya hai
                removeImageFromTransaction();
            }
        }
        else if (requestCode == EDIT_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            // 🔹 EDIT MODE SE WAPAS AAYE HAIN - UPDATED TRANSACTION MILA HAI
            handleCustomerTransactionUpdate(data);
        }
        else if (requestCode == EDIT_PERSONAL_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            // 🔹 PERSONAL TRANSACTION EDIT SE WAPAS AAYE HAIN
            handlePersonalTransactionUpdate(data);
        }
    }

    private void handleCustomerTransactionUpdate(Intent data) {
        if (data != null && data.hasExtra("updated_transaction")) {
            transaction_class updatedTransaction = (transaction_class) data.getSerializableExtra("updated_transaction");
            if (updatedTransaction != null) {
                // Database mein transaction update karo
                boolean updateSuccess = dbHelper.updateTransaction(
                        customerId,
                        currentTransaction, // Old transaction
                        updatedTransaction  // New transaction
                );

                if (updateSuccess) {
                    // UI update karo
                    currentTransaction = updatedTransaction;
                    displayCustomerTransactionDetails(currentTransaction);

                    Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show();

                    // Result bhejo ki transaction update hua hai
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transaction_updated", true);
                    resultIntent.putExtra("customer_id", customerId);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void handlePersonalTransactionUpdate(Intent data) {
        if (data != null && data.hasExtra("updated_personal_transaction")) {
            personal_transaction updatedTransaction = (personal_transaction) data.getSerializableExtra("updated_personal_transaction");
            if (updatedTransaction != null) {
                // Database mein personal transaction update karo
                boolean updateSuccess = dbHelper.updatePersonalTransaction(updatedTransaction);

                if (updateSuccess) {
                    // UI update karo
                    currentPersonalTransaction = updatedTransaction;
                    displayPersonalTransactionDetails(currentPersonalTransaction);

                    Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show();

                    // Result bhejo report screen ko
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("personal_transaction_updated", true);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 🔹 Transaction se image remove karo
    private void removeImageFromTransaction() {
        try {
            boolean updateSuccess = false;

            if (transactionType.equals("customer") && currentTransaction != null) {
                // Database se image path remove karo (customer transaction)
                updateSuccess = dbHelper.removeImageFromTransaction(
                        customerId,
                        currentTransaction.getType(),
                        currentTransaction.getAmount(),
                        currentTransaction.getDateTime()
                );

                if (updateSuccess) {
                    // UI update karo
                    currentTransaction.setImagePath("");
                    hideImageSection();

                    // Result bhejein customer_detail screen ko
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("image_removed", true);
                    resultIntent.putExtra("customer_id", customerId);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                }
            }
            else if (transactionType.equals("personal") && currentPersonalTransaction != null) {
                // Personal transaction से image remove करो
                currentPersonalTransaction.setImagePath("");
                updateSuccess = dbHelper.updatePersonalTransaction(currentPersonalTransaction);

                if (updateSuccess) {
                    hideImageSection();
                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();

                    // Result bhejo report screen ko
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("personal_transaction_updated", true);
                    setResult(RESULT_OK, resultIntent);
                }
            }

        } catch (Exception e) {
            Log.e("RemoveImage", "❌ Error removing image: " + e.getMessage());
            Toast.makeText(this, "Error removing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        // Custom dialog show karen
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_transaction, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Dialog window settings
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(true);

        // Dialog views
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Dynamic message set karen based on transaction type
        String message = "";
        if (transactionType.equals("customer")) {
            message = "Kya aap ye customer transaction delete karna chahte hain?\n\nYe action permanent hai aur undo nahi ho sakta.";
        } else if (transactionType.equals("personal")) {
            message = "Kya aap ye personal transaction delete karna chahte hain?\n\nYe action permanent hai aur undo nahi ho sakta.";
        }

        dialogMessage.setText(message);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTransactionFromDatabase();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void deleteTransactionFromDatabase() {
        try {
            boolean deleteSuccess = false;

            if (transactionType.equals("customer")) {
                if (currentTransaction == null || customerId == -1) {
                    Toast.makeText(this, "Transaction data not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                deleteSuccess = dbHelper.deleteTransactionByDetails(
                        customerId,
                        currentTransaction.getType(),
                        currentTransaction.getAmount(),
                        currentTransaction.getDateTime()
                );

                if (deleteSuccess) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transaction_deleted", true);
                    resultIntent.putExtra("customer_id", customerId);
                    setResult(RESULT_OK, resultIntent);

                    finish();
                }
            }
            else if (transactionType.equals("personal")) {
                if (currentPersonalTransaction == null) {
                    Toast.makeText(this, "Transaction data not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                deleteSuccess = dbHelper.deletePersonalTransaction(currentPersonalTransaction.getId());

                if (deleteSuccess) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("personal_transaction_deleted", true);
                    setResult(RESULT_OK, resultIntent);

                    finish();
                }
            }

            if (deleteSuccess) {
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("DeleteTransaction", "❌ Error deleting transaction: " + e.getMessage());
            Toast.makeText(this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
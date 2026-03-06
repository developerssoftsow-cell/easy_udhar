package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.data_holder;
import com.Easy_Udhar_SoftSow.R;

public class add_screen extends AppCompatActivity {

    private EditText nameEditText, phoneEditText;
    private LinearLayout phoneSection;
    private ImageView backArrow;
    private TextView phoneErrorText, nameErrorText, titleText;
    private Button addButton;

    // 🔹 Edit mode fields
    private boolean isEditMode = false;
    private int customerId;
    private String originalName, originalPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_screen);

        // 🔹 Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        phoneSection = findViewById(R.id.phoneSection);
        backArrow = findViewById(R.id.backArrow);
        phoneErrorText = findViewById(R.id.phoneErrorText);
        nameErrorText = findViewById(R.id.nameErrorText);
        titleText = findViewById(R.id.titleText);
        addButton = findViewById(R.id.addButton);

        // 🔹 Check if edit mode
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("edit_mode", false)) {
            isEditMode = true;
            customerId = intent.getIntExtra("customer_id", -1);
            originalName = intent.getStringExtra("customer_name");
            originalPhone = intent.getStringExtra("customer_phone");

            setupEditMode();
        }

        // 🔹 Back arrow click
        backArrow.setOnClickListener(v -> handleBackAction());

        // 🔹 Show phone field when name typed
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneSection.setVisibility(s.length() > 0 ? LinearLayout.VISIBLE : LinearLayout.GONE);
                if(s.length() > 0){
                    nameEditText.setBackgroundResource(R.drawable.edittext_bg);
                    nameErrorText.setVisibility(TextView.GONE);
                }
            }
        });

        // 🔹 Handle "Done" button on keyboard for phone
        phoneEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validatePhoneOptional();
                return true;
            }
            return false;
        });

        addButton.setOnClickListener(v -> {
            if (isEditMode) {
                handleUpdate();
            } else {
                handleAdd();
            }
        });
    }

    // 🔹 EDIT MODE SETUP
    private void setupEditMode() {
        titleText.setText("Update Customer");
        addButton.setText("SAVE");

        // Pre-fill existing values
        if (originalName != null) {
            nameEditText.setText(originalName);
            phoneSection.setVisibility(LinearLayout.VISIBLE);
        }

        if (originalPhone != null && !originalPhone.isEmpty() && !originalPhone.equals("null")) {
            phoneEditText.setText(originalPhone);
        }
    }

    // 🔹 HANDLE UPDATE (Edit Mode)
    private void handleUpdate() {
        boolean isNameValid = validateName();
        boolean isPhoneValid = validatePhoneOptional();

        if(isNameValid && isPhoneValid){
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // 🔹 DATABASE MEIN UPDATE KAREN
            data_holder dbHelper = new data_holder(add_screen.this);
            boolean updateSuccess = dbHelper.updateCustomer(customerId, name, phone);

            if(updateSuccess) {
                // Success - result bhejo aur finish karo
                Intent resultIntent = new Intent();
                resultIntent.putExtra("customer_updated", true);
                resultIntent.putExtra("customer_id", customerId);
                resultIntent.putExtra("customer_name", name);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(add_screen.this, "Customer update nahi ho paya!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 HANDLE ADD (New Customer Mode)
    private void handleAdd() {
        boolean isNameValid = validateName();
        boolean isPhoneValid = validatePhoneOptional();

        if(isNameValid && isPhoneValid){
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // 🔹 DATABASE MEIN SAVE KAREN
            data_holder dbHelper = new data_holder(add_screen.this);
            long customerId = dbHelper.addCustomer(name, phone);

            if(customerId != -1) {
                // Success - customer_detail screen par jao aur customerId pass karo
                Intent intent = new Intent(add_screen.this, customer_detail.class);
                intent.putExtra("customer_id", (int) customerId);
                intent.putExtra("customer_name", name);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(add_screen.this, "Customer save nahi ho paya!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //  🔹 Handle device back button
    @Override
    public void onBackPressed() {
        handleBackAction();
    }

    // 🔹 Handle back logic
    private void handleBackAction() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Name empty → go back
        if (name.isEmpty()) {
            if (isEditMode) {
                // Edit mode se wapas profile screen par jao
                setResult(RESULT_CANCELED);
                finish();
            } else {
                Intent intent = new Intent(add_screen.this, add_customer.class);
                startActivity(intent);
                finish();
            }
            return;
        }

        // Name filled, check phone
        boolean isPhoneValid = phone.isEmpty() || (phone.length() >= 10 && phone.length() <= 11);

        if (isEditMode) {
            // Edit mode mein back press par directly save karein
            if(phone.isEmpty() || isPhoneValid){
                handleUpdate();
            } else {
                // Phone invalid → stay on add_screen to correct
                phoneEditText.requestFocus();
                phoneEditText.setBackgroundResource(R.drawable.edittext_bg_error);
                phoneErrorText.setVisibility(TextView.VISIBLE);
            }
        } else {
            // New customer mode - show save alert
            showSaveAlert(name, phone, isPhoneValid);
        }
    }

    // 🔹 Show save alert dialog (Only for new customer)
    private void showSaveAlert(String name, String phone, boolean isPhoneValid) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_alert_box);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 👈 add this line
        dialog.setCancelable(false);

        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // SAVE button click
        btnSave.setOnClickListener(v -> {
            if(phone.isEmpty() || isPhoneValid){
                // Phone empty or valid → go to customer_detail
                Intent intent = new Intent(add_screen.this, customer_detail.class);
                intent.putExtra("customer_name", nameEditText.getText().toString().trim());
                startActivity(intent);

                dialog.dismiss();
                finish();
            } else {
                // Phone invalid → stay on add_screen to correct
                phoneEditText.requestFocus();
                phoneEditText.setBackgroundResource(R.drawable.edittext_bg_error);
                phoneErrorText.setVisibility(TextView.VISIBLE);
                dialog.dismiss();
            }
        });

        // CANCEL button click
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(add_screen.this, add_customer.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    // 🔹 Validate Name
    private boolean validateName() {
        String name = nameEditText.getText().toString().trim();
        if(name.isEmpty()){
            nameEditText.setBackgroundResource(R.drawable.edittext_bg_error);
            nameErrorText.setVisibility(TextView.VISIBLE);
            return false;
        } else {
            nameEditText.setBackgroundResource(R.drawable.edittext_bg);
            nameErrorText.setVisibility(TextView.GONE);
            return true;
        }
    }

    // 🔹 Validate Phone (optional)
    private boolean validatePhoneOptional() {
        String phone = phoneEditText.getText().toString().trim();
        if(phone.isEmpty()){
            phoneEditText.setBackgroundResource(R.drawable.edittext_bg);
            phoneErrorText.setVisibility(TextView.GONE);
            return true; // optional
        } else if(phone.length() < 10 || phone.length() > 11){
            phoneEditText.setBackgroundResource(R.drawable.edittext_bg_error);
            phoneErrorText.setText("Phone number darust karein (10-11 digits)");
            phoneErrorText.setVisibility(TextView.VISIBLE);
            return false;
        } else {
            phoneEditText.setBackgroundResource(R.drawable.edittext_bg);
            phoneErrorText.setVisibility(TextView.GONE);
            return true;
        }
    }
}
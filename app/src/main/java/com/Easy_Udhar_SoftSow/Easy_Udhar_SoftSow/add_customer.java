package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter.add_contact_adapter;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.add_contact_class;
import com.Easy_Udhar_SoftSow.R;

import java.util.ArrayList;
import java.util.List;

public class add_customer extends AppCompatActivity {

    private static final int REQUEST_CONTACT_PERMISSION = 1;
    private RecyclerView recyclerView;
    private add_contact_adapter adapter;
    private List<add_contact_class> contactList;
    private List<add_contact_class> filteredList;
    private EditText searchCustomer;
    private ImageView clearSearch;
    private LinearLayout addNewCustomerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        recyclerView = findViewById(R.id.recyclerContacts);
        searchCustomer = findViewById(R.id.searchCustomer);
        clearSearch = findViewById(R.id.clearSearch);
        ImageView backArrow = findViewById(R.id.backArrow);
        addNewCustomerLayout = findViewById(R.id.addNewCustomerLayout);

        // 🔹 Back arrow action
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(add_customer.this, dashboard.class);
            startActivity(intent);
            finish();
        });

        // 🔹 Add New Customer Layout → open add_screen
        addNewCustomerLayout.setOnClickListener(v -> {
            Intent intent = new Intent(add_customer.this, add_screen.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // 🔹 Contact permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            loadContacts();
        }

        // 🔹 Search text listener
        searchCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());

                // Show/hide clear button based on text
                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 🔹 Clear button listener
        clearSearch.setOnClickListener(v -> {
            String currentText = searchCustomer.getText().toString();
            if (!currentText.isEmpty()) {
                searchCustomer.setText(""); // clear the text
            }
        });

        // Initially hide the clear button
        clearSearch.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CONTACT_DEBUG", "onResume called - Refreshing contacts");

        // Refresh contacts when returning to this screen
        if (adapter != null) {
            adapter.refreshContacts();
        } else {
            loadContacts();
        }
    }

    private void loadContacts() {
        contactList.clear();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                // Clean the phone number
                number = cleanPhoneNumber(number);

                contactList.add(new add_contact_class(name, number, false));
            }
            cursor.close();
        }

        filteredList.clear();
        filteredList.addAll(contactList);

        Log.d("CONTACT_DEBUG", "Total contacts loaded: " + contactList.size());

        // Adapter refresh karein
        if (adapter == null) {
            adapter = new add_contact_adapter(this, filteredList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.refreshContacts();
        }
    }

    private String cleanPhoneNumber(String phone) {
        // Remove spaces, dashes, and other non-digit characters
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9]", "");

        // Remove leading 0 if present
        if (cleaned.startsWith("0") && cleaned.length() > 10) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }

    private void filterContacts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(contactList);
        } else {
            for (add_contact_class contact : contactList) {
                if ((contact.getName() != null && contact.getName().toLowerCase().contains(query.toLowerCase()))
                        || (contact.getNumber() != null && contact.getNumber().contains(query))) {
                    filteredList.add(contact);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
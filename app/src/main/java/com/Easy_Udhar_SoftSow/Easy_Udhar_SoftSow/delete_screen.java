package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter.deleted_customers_adapter;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.deleted_customer;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class delete_screen extends AppCompatActivity {

    private LinearLayout emptyState;
    private RecyclerView recyclerDeletedCustomers;
    private List<deleted_customer> deletedCustomersList;
    private deleted_customers_adapter adapter;
    private data_holder dbHelper;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DeleteScreen", "🔴 DELETE SCREEN onCreate CALLED");

        try {
            setContentView(R.layout.activity_delete_screen);

            // ✅ STEP 3: BACK BUTTON
            ImageButton btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> finish());

            // ✅ STEP 5: EMPTY STATE
            emptyState = findViewById(R.id.emptyState);

            // ✅ STEP 7: SIMPLE ADAPTER WITH RESTORE LISTENER
            deletedCustomersList = new ArrayList<>();
            recyclerDeletedCustomers = findViewById(R.id.recyclerDeletedCustomers);
            recyclerDeletedCustomers.setLayoutManager(new LinearLayoutManager(this));

            // ✅ STEP 11: ADD BOTH RESTORE AND DELETE LISTENERS
            adapter = new deleted_customers_adapter(deletedCustomersList,
                    new deleted_customers_adapter.OnRestoreClickListener() {
                        @Override
                        public void onRestoreClick(deleted_customer deletedCustomer, int position) {
                            restoreCustomer(deletedCustomer, position);
                        }
                    },
                    new deleted_customers_adapter.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick(deleted_customer deletedCustomer, int position) {
                            permanentlyDeleteCustomer(deletedCustomer, position);
                        }
                    });
            recyclerDeletedCustomers.setAdapter(adapter);

            // ✅ STEP 8: DATABASE HELPER
            dbHelper = new data_holder(this);

            // ✅ STEP 10: LOAD DATA
            loadDeletedCustomers();

            // ✅ STEP 4: BOTTOM NAVIGATION - UPDATED
            setupBottomNavigation();

            Log.d("DeleteScreen", "✅ All components setup completed");

        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ NEW: BOTTOM NAVIGATION SETUP METHOD
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_khata) {
                    // Go to Dashboard (Khata)
                    Intent intent = new Intent(delete_screen.this, dashboard.class);
                    startActivity(intent);
                    finish();
                    return true;

                } else if (id == R.id.nav_batwa) {
                    // Go to Batwa screen (Report Screen)
                    Intent intent = new Intent(delete_screen.this, report_screen.class);
                    startActivity(intent);
                    finish();
                    return true;

                } else if (id == R.id.nav_account) {
                    // Already on delete screen (which is part of account section)
                    return true;
                }
                return false;
            });

            // Set current item as selected (Account tab)
            bottomNavigationView.setSelectedItemId(R.id.nav_account);
        } else {
            Log.e("DeleteScreen", "❌ BottomNavigationView not found!");
        }
    }

    // ✅ STEP 10: LOAD DATA METHOD
    private void loadDeletedCustomers() {
        Log.d("DeleteScreen", "🔄 Loading deleted customers...");

        try {
            List<deleted_customer> customers = dbHelper.getDeletedCustomers();
            Log.d("DeleteScreen", "✅ Database returned: " + customers.size() + " customers");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        deletedCustomersList.clear();

                        if (customers != null && !customers.isEmpty()) {
                            deletedCustomersList.addAll(customers);
                            showDeletedCustomersList();
                            Log.d("DeleteScreen", "✅ Showing " + deletedCustomersList.size() + " deleted customers");
                        } else {
                            showEmptyState();
                            Log.d("DeleteScreen", "ℹ️ No deleted customers found");
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("DeleteScreen", "❌ Error updating UI: " + e.getMessage());
                        showErrorState();
                    }
                }
            });

        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error loading deleted customers: " + e.getMessage());
            showErrorState();
        }
    }

    // ✅ STEP 11: RESTORE CUSTOMER METHOD
    private void restoreCustomer(deleted_customer deletedCustomer, int position) {
        Log.d("DeleteScreen", "🔄 Restoring customer: " + deletedCustomer.getName());

        try {
            boolean success = dbHelper.restoreCustomer(deletedCustomer.getId());

            if (success) {
                deletedCustomersList.remove(position);
                adapter.notifyItemRemoved(position);

                if (deletedCustomersList.size() > 0) {
                    adapter.notifyItemRangeChanged(position, deletedCustomersList.size());
                }

                if (deletedCustomersList.isEmpty()) {
                    showEmptyState();
                }

                Log.d("DeleteScreen", "✅ Customer restored: " + deletedCustomer.getName());
            } else {
                Toast.makeText(this, "Failed to restore customer", Toast.LENGTH_SHORT).show();
                Log.e("DeleteScreen", "❌ Failed to restore customer");
            }
        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error restoring customer: " + e.getMessage());
            Toast.makeText(this, "Error restoring customer", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ STEP 12: PERMANENT DELETE METHOD
    private void permanentlyDeleteCustomer(deleted_customer deletedCustomer, int position) {
        Log.d("DeleteScreen", "🗑️ Permanently deleting customer: " + deletedCustomer.getName());

        try {
            boolean success = dbHelper.permanentlyDeleteCustomer(deletedCustomer.getId());

            if (success) {
                deletedCustomersList.remove(position);
                adapter.notifyItemRemoved(position);

                if (deletedCustomersList.size() > 0) {
                    adapter.notifyItemRangeChanged(position, deletedCustomersList.size());
                }

                if (deletedCustomersList.isEmpty()) {
                    showEmptyState();
                }

                Toast.makeText(this, "Customer permanently deleted", Toast.LENGTH_SHORT).show();
                Log.d("DeleteScreen", "✅ Customer permanently deleted: " + deletedCustomer.getName());
            } else {
                Toast.makeText(this, "Failed to delete customer", Toast.LENGTH_SHORT).show();
                Log.e("DeleteScreen", "❌ Failed to permanently delete customer");
            }
        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error deleting customer: " + e.getMessage());
            Toast.makeText(this, "Error deleting customer", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        try {
            emptyState.setVisibility(View.VISIBLE);
            recyclerDeletedCustomers.setVisibility(View.GONE);
            Log.d("DeleteScreen", "📭 Empty state shown");
        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error showing empty state: " + e.getMessage());
        }
    }

    private void showDeletedCustomersList() {
        try {
            emptyState.setVisibility(View.GONE);
            recyclerDeletedCustomers.setVisibility(View.VISIBLE);
            Log.d("DeleteScreen", "📋 Deleted customers list shown");
        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error showing list: " + e.getMessage());
        }
    }

    private void showErrorState() {
        try {
            emptyState.setVisibility(View.VISIBLE);
            recyclerDeletedCustomers.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            Log.d("DeleteScreen", "❌ Error state shown");
        } catch (Exception e) {
            Log.e("DeleteScreen", "❌ Error showing error state: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DeleteScreen", "🔄 onResume - reloading data");
        loadDeletedCustomers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DeleteScreen", "🟢 DELETE SCREEN onStart CALLED");
    }

    @Override
    public void onBackPressed() {
        // Back press par account_screen mein jao
        Intent intent = new Intent(this, account_screen.class);
        startActivity(intent);
        finish();
    }
}
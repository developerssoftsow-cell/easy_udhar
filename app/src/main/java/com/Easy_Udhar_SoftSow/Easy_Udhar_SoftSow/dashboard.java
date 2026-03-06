package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter.dashboard_adapter;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.customer;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.data_holder;
import com.Easy_Udhar_SoftSow.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class dashboard extends AppCompatActivity implements dashboard_adapter.OnCustomerClickListener {

    private RecyclerView recyclerView;
    private dashboard_adapter adapter;
    private List<customer> customerList;
    private List<customer> allCustomersList;
    private List<customer> filteredCustomerList;
    private data_holder dbHelper;
    private LinearLayout middleSection;
    private LinearLayout redCard, greenCard;
    private LinearLayout searchSection;
    private TextView redAmount, greenAmount, redText, greenText;
    private ImageView redIcon, greenIcon;
//    private Button btnAddContact;
    private ImageView btnAddContact;

    private BottomNavigationView bottomNavigationView;
    private EditText searchEditText;
    private ImageView clearSearch;
    private TextView btnPdf;
    private ImageView btnFilter;

    // NEW VARIABLES FOR TOGGLE FUNCTIONALITY
    private LinearLayout balanceCardsContainer;
    private CardView cardShowBalance;
    private ImageView arrowIcon;
    private TextView txtShowBalance;
    private String currentFilter = "all";
    private String currentSearchQuery = "";
    private boolean isBalanceVisible = false;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Database Helper
        dbHelper = new data_holder(this);

        // Initialize Views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Set click listeners
        setupClickListeners();

        // Setup Search Functionality
        setupSearchFunctionality();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Setup PDF Button
        setupPdfButton();

        // Load initial data
        loadCustomers();
        updateTotalCards();

        // Default state - no card selected
        resetCardSelection();

        // Setup Broadcast Receiver for instant updates
        setupBroadcastReceiver();
    }


    private void setupBroadcastReceiver() {
        BroadcastReceiver customerDeleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("CUSTOMER_DELETED".equals(intent.getAction())) {
                    Log.d("Dashboard", "🔔 Customer deletion broadcast received!");

                    // INSTANT UPDATE KAREN
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 1. Refresh all data
                            allCustomersList.clear();
                            allCustomersList.addAll(dbHelper.getAllCustomers());

                            // 2. Apply current filter
                            applyCurrentFilter();

                            // 3. Update totals (YEH IMPORTANT HAI)
                            updateTotalCards();

                            // 4. Update UI
                            updateUI();

                            Log.d("Dashboard", "✅ Dashboard INSTANTLY updated after customer deletion");
                        }
                    });
                }
            }
        };

        // ✅ FIX: Android 12+ ke liye flag add karein
        IntentFilter filter = new IntentFilter("CUSTOMER_DELETED");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ ke liye
            registerReceiver(customerDeleteReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // Older Android versions ke liye
            registerReceiver(customerDeleteReceiver, filter);
        }
    }

    private void initializeViews() {
        // Find card layouts
        redCard = findViewById(R.id.red_card);
        greenCard = findViewById(R.id.green_card);
        btnAddContact = findViewById(R.id.btn_add_contact);

        // Find amount text views
        redAmount = findViewById(R.id.red_amount);
        greenAmount = findViewById(R.id.green_amount);
        redText = findViewById(R.id.red_text);
        greenText = findViewById(R.id.green_text);

        // Find icons
        redIcon = findViewById(R.id.red_icon);
        greenIcon = findViewById(R.id.green_icon);

        // NEW: Find toggle views
        cardShowBalance = findViewById(R.id.cardShowBalance);
        balanceCardsContainer = findViewById(R.id.balanceCardsContainer);
        arrowIcon = findViewById(R.id.arrowIcon);
        txtShowBalance = findViewById(R.id.txtShowBalance);

        // Search section
        searchSection = findViewById(R.id.search_section);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearch = findViewById(R.id.clearSearch);

        // PDF Button
        btnPdf = findViewById(R.id.btnPdf);

        // Filter Button
        btnFilter = findViewById(R.id.btnfilter);

        // RecyclerView and empty state
        recyclerView = findViewById(R.id.recyclerCustomers);
        middleSection = findViewById(R.id.middle_section);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // DEBUG: Check if views are found
        Log.d("Dashboard", "cardShowBalance: " + (cardShowBalance != null));
        Log.d("Dashboard", "balanceCardsContainer: " + (balanceCardsContainer != null));
        Log.d("Dashboard", "arrowIcon: " + (arrowIcon != null));
        Log.d("Dashboard", "redIcon: " + (redIcon != null));
        Log.d("Dashboard", "greenIcon: " + (greenIcon != null));
        Log.d("Dashboard", "btnFilter: " + (btnFilter != null));
    }

    private void setupRecyclerView() {
        customerList = new ArrayList<>();
        allCustomersList = new ArrayList<>();
        filteredCustomerList = new ArrayList<>();

        adapter = new dashboard_adapter(customerList, this, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Show Balance Card Click Listener
        if (cardShowBalance != null) {
            cardShowBalance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Dashboard", "Show Balance card clicked!");
                    toggleBalanceCards();
                }
            });
        } else {
            Log.e("Dashboard", "cardShowBalance is NULL!");
        }

        // Red Card Click Listener
        redCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFilter.equals("red")) {
                    resetCardSelection();
                    showAllCustomers();
                } else {
                    selectRedCard();
                    filterCustomersByType("red");
                }
            }
        });

        // Green Card Click Listener
        greenCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFilter.equals("green")) {
                    resetCardSelection();
                    showAllCustomers();
                } else {
                    selectGreenCard();
                    filterCustomersByType("green");
                }
            }
        });

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibrate effect
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                // Show ProgressDialog
                ProgressDialog progressDialog = new ProgressDialog(dashboard.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Button animation
                v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();

                            // Open screen WITHOUT transition animation
                            Intent intent = new Intent(dashboard.this, add_customer.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0); // NO ANIMATION

                            // Dismiss progress when screen opens
                            progressDialog.dismiss();
                        })
                        .start();
            }
        });

        // Filter Button Click Listener
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_customers, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        // Initialize radio buttons
        RadioButton radioAll = dialogView.findViewById(R.id.radioAll);
        RadioButton radioAmountAsc = dialogView.findViewById(R.id.radioAmountAsc);
        RadioButton radioAmountDesc = dialogView.findViewById(R.id.radioAmountDesc);
        RadioButton radioNameAsc = dialogView.findViewById(R.id.radioNameAsc);
        RadioButton radioNameDesc = dialogView.findViewById(R.id.radioNameDesc);
        RadioButton radioNumeric = dialogView.findViewById(R.id.radioNumeric);

        // ✅ RADIO GROUP INITIALIZE KARO
        RadioGroup radioFilterGroup = dialogView.findViewById(R.id.radioFilterGroup);

        // Set default selection
        radioAll.setChecked(true);

        // ✅ CORRECTED: Direct layout IDs use karo with RadioGroup
        dialogView.findViewById(R.id.layoutAll).setOnClickListener(v -> {
            radioAll.setChecked(true);
            radioFilterGroup.check(R.id.radioAll);
        });

        dialogView.findViewById(R.id.layoutAmountAsc).setOnClickListener(v -> {
            radioAmountAsc.setChecked(true);
            radioFilterGroup.check(R.id.radioAmountAsc);
        });

        dialogView.findViewById(R.id.layoutAmountDesc).setOnClickListener(v -> {
            radioAmountDesc.setChecked(true);
            radioFilterGroup.check(R.id.radioAmountDesc);
        });

        dialogView.findViewById(R.id.layoutNameAsc).setOnClickListener(v -> {
            radioNameAsc.setChecked(true);
            radioFilterGroup.check(R.id.radioNameAsc);
        });

        dialogView.findViewById(R.id.layoutNameDesc).setOnClickListener(v -> {
            radioNameDesc.setChecked(true);
            radioFilterGroup.check(R.id.radioNameDesc);
        });

        dialogView.findViewById(R.id.layoutNumeric).setOnClickListener(v -> {
            radioNumeric.setChecked(true);
            radioFilterGroup.check(R.id.radioNumeric);
        });

        // Apply Button
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedFilter = "all";

                // ✅ RADIO GROUP SE CHECK KARO KONSA SELECT HAI
                int selectedId = radioFilterGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.radioAll) {
                    selectedFilter = "all";
                    Toast.makeText(dashboard.this, "All customers shown", Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radioAmountAsc) {
                    selectedFilter = "amount_asc";
                    Toast.makeText(dashboard.this, "Amount (Low to High)", Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radioAmountDesc) {
                    selectedFilter = "amount_desc";
                    Toast.makeText(dashboard.this, "Amount (High to Low)", Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radioNameAsc) {
                    selectedFilter = "name_asc";
                    Toast.makeText(dashboard.this, "Name (A to Z)", Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radioNameDesc) {
                    selectedFilter = "name_desc";
                    Toast.makeText(dashboard.this, "Name (Z to A)", Toast.LENGTH_SHORT).show();
                } else if (selectedId == R.id.radioNumeric) {
                    selectedFilter = "numeric";
                    Toast.makeText(dashboard.this, "Numeric names only", Toast.LENGTH_SHORT).show();
                }

                applyCustomerFilter(selectedFilter);
                dialog.dismiss();
            }
        });
    }

    private void applyCustomerFilter(String filterType) {
        List<customer> filteredList = new ArrayList<>();

        switch (filterType) {
            case "all":
                filteredList.addAll(allCustomersList);
                break;

            case "amount_asc":
                filteredList.addAll(allCustomersList);
                // Sort by absolute net amount ascending
                Collections.sort(filteredList, (c1, c2) ->
                        Double.compare(Math.abs(c1.getNetAmount()), Math.abs(c2.getNetAmount())));
                break;

            case "amount_desc":
                filteredList.addAll(allCustomersList);
                // Sort by absolute net amount descending
                Collections.sort(filteredList, (c1, c2) ->
                        Double.compare(Math.abs(c2.getNetAmount()), Math.abs(c1.getNetAmount())));
                break;

            case "name_asc":
                filteredList.addAll(allCustomersList);
                // Sort by name A to Z
                Collections.sort(filteredList, (c1, c2) ->
                        c1.getName().compareToIgnoreCase(c2.getName()));
                break;

            case "name_desc":
                filteredList.addAll(allCustomersList);
                // Sort by name Z to A
                Collections.sort(filteredList, (c1, c2) ->
                        c2.getName().compareToIgnoreCase(c1.getName()));
                break;

            case "numeric":
                // Filter only numeric names
                for (customer customer : allCustomersList) {
                    if (isNumeric(customer.getName())) {
                        filteredList.add(customer);
                    }
                }
                break;
        }

        // Update the displayed list
        customerList.clear();
        customerList.addAll(filteredList);
        applySearchFilter(); // Apply current search if any

        // Update UI based on filtered list
        updateUI();
    }

    // Helper method to check if string is numeric
    // Helper method to check if string contains any numbers
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // Check each character - agar koi bhi digit (0-9) mila to true return karo
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    // TOGGLE BALANCE CARDS METHOD
    private void toggleBalanceCards() {
        if (isBalanceVisible) {
            // Hide balance cards
            balanceCardsContainer.setVisibility(View.GONE);
            if (arrowIcon != null) {
                arrowIcon.setRotation(0);
            }
            if (txtShowBalance != null) {
                txtShowBalance.setText("Show Balance");
            }
            isBalanceVisible = false;
            Log.d("Dashboard", "Balance cards hidden");
        } else {
            // Show balance cards
            balanceCardsContainer.setVisibility(View.VISIBLE);
            if (arrowIcon != null) {
                arrowIcon.setRotation(90);
            }
            if (txtShowBalance != null) {
                txtShowBalance.setText("Hide Balance");
            }
            isBalanceVisible = true;
            Log.d("Dashboard", "Balance cards shown");

            // Update amounts when showing
            updateTotalCards();
        }
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applySearchFilter();

                if (s.length() > 0) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                clearSearch.setVisibility(View.INVISIBLE);
                currentSearchQuery = "";
                applySearchFilter();
            }
        });

        clearSearch.setVisibility(View.INVISIBLE);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_khata) {
                // Already on dashboard, kuch nahi karna
                return true;
            } else if (id == R.id.nav_batwa) {
                Intent intent = new Intent(dashboard.this, report_screen.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_account) {
                // Account screen par navigate karein
                Intent intent = new Intent(dashboard.this, account_screen.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupPdfButton() {
        btnPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateFilterDialog();
            }
        });
    }

    private void showDateFilterDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_filter, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // ✅ Dialog window ka background transparent karein
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        TextView tvStartDate = dialogView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button btnDownload = dialogView.findViewById(R.id.btnDownload);
        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);

        LinearLayout dateSection = dialogView.findViewById(R.id.dateSection);
        dateSection.setVisibility(View.GONE);
        tvStartDate.setEnabled(false);
        tvEndDate.setEnabled(false);

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(tvStartDate);
            }
        });

        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(tvEndDate);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioAll) {
                    dateSection.setVisibility(View.GONE);
                    tvStartDate.setText("");
                    tvEndDate.setText("");
                } else {
                    dateSection.setVisibility(View.VISIBLE);
                    tvStartDate.setEnabled(true);
                    tvEndDate.setEnabled(true);
                }
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAllSelected = radioGroup.getCheckedRadioButtonId() == R.id.radioAll;
                String startDate = tvStartDate.getText().toString();
                String endDate = tvEndDate.getText().toString();

                if (!isAllSelected && (startDate.isEmpty() || endDate.isEmpty())) {
                    Toast.makeText(dashboard.this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                    return;
                }

                generatePdfReport(isAllSelected ? "All" : startDate, isAllSelected ? "All" : endDate);
                dialog.dismiss();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        textView.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void loadCustomers() {
        allCustomersList.clear();
        List<customer> customers = dbHelper.getAllCustomers();
        allCustomersList.addAll(customers);

        // Current filter ke according customers show karo
        applyCurrentFilter();

        // Update UI based on whether customers exist or not
        updateUI();
    }

    private void applyCurrentFilter() {
        switch (currentFilter) {
            case "red":
                filterCustomersByType("red");
                break;
            case "green":
                filterCustomersByType("green");
                break;
            default:
                showAllCustomers();
                break;
        }

        // Apply search filter after type filter
        applySearchFilter();
    }

    private void applySearchFilter() {
        filteredCustomerList.clear();

        if (currentSearchQuery.isEmpty()) {
            // AGAR SEARCH EMPTY HAI TO SAB CUSTOMERS SHOW KARO
            filteredCustomerList.addAll(customerList);
            Log.d("Dashboard", "🔍 Search empty - showing all " + customerList.size() + " customers");

            // EMPTY STATE CHECK KARO
            if (filteredCustomerList.isEmpty()) {
                showEmptyState();
            } else {
                showCustomerList();
            }
        } else {
            // SEARCH QUERY KE ACCORDING FILTER KARO
            String query = currentSearchQuery.toLowerCase().trim();
            for (customer customer : customerList) {
                if (customer.getName() != null && customer.getName().toLowerCase().contains(query)) {
                    filteredCustomerList.add(customer);
                }
            }
            Log.d("Dashboard", "🔍 Search '" + query + "' - found " + filteredCustomerList.size() + " customers");

            // SEARCH RESULT KE BAAD BHI EMPTY STATE CHECK KARO
            if (filteredCustomerList.isEmpty()) {
                showEmptyState();
                Toast.makeText(this, "Koi customer nahi mila", Toast.LENGTH_SHORT).show();
            } else {
                showCustomerList();
            }
        }

        updateRecyclerView();
    }

    private void showEmptyState() {
        middleSection.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showCustomerList() {
        middleSection.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        if (!customerList.isEmpty()) {
            // Customers hain - show search bar and customers list
            middleSection.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            searchSection.setVisibility(View.VISIBLE);

            // Apply current search filter
            applySearchFilter();
        } else {
            // Koi customer nahi hai - show empty state
            middleSection.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            searchSection.setVisibility(View.GONE);
        }
    }

    private void updateRecyclerView() {
        // Adapter ko filtered list update karo
        adapter.updateCustomerList(filteredCustomerList);
        adapter.notifyDataSetChanged();

        // Check if filtered list is empty after search
        if (!currentSearchQuery.isEmpty() && filteredCustomerList.isEmpty()) {
            Toast.makeText(this, "Koi customer nahi mila", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalCards() {
        double totalRed = 0;
        double totalGreen = 0;

        // ✅ IMPORTANT: Fresh data lein database se (NOT from allCustomersList)
        List<customer> freshCustomers = dbHelper.getAllCustomers();

        Log.d("Dashboard", "=== UPDATING TOTAL CARDS ===");
        Log.d("Dashboard", "Fresh customers from DB: " + freshCustomers.size());

        for (customer customer : freshCustomers) {
            double netAmount = customer.getNetAmount();

            if (netAmount < 0) {
                totalRed += Math.abs(netAmount);
                Log.d("Dashboard", "🔴 RED - " + customer.getName() + ": Rs. " + Math.abs(netAmount));
            } else if (netAmount > 0) {
                totalGreen += netAmount;
                Log.d("Dashboard", "🟢 GREEN - " + customer.getName() + ": Rs. " + netAmount);
            }
        }

        Log.d("Dashboard", "📊 FINAL TOTALS - RED: Rs. " + totalRed + ", GREEN: Rs. " + totalGreen);

        // Update UI
        if (redAmount != null) {
            redAmount.setText("Rs. " + String.format("%.2f", totalRed));
        }

        if (greenAmount != null) {
            greenAmount.setText("Rs. " + String.format("%.2f", totalGreen));
        }
    }

    private double calculateTotalRedAmount() {
        double total = 0;
        for (customer customer : allCustomersList) {
            if (customer.getNetAmount() < 0) {
                total += Math.abs(customer.getNetAmount());
            }
        }
        return total;
    }

    private double calculateTotalGreenAmount() {
        double total = 0;
        for (customer customer : allCustomersList) {
            if (customer.getNetAmount() > 0) {
                total += customer.getNetAmount();
            }
        }
        return total;
    }

    private void filterCustomersByType(String type) {
        customerList.clear();
        currentFilter = type;

        for (customer customer : allCustomersList) {
            if ("red".equals(type) && customer.getNetAmount() < 0) {
                customerList.add(customer);
            } else if ("green".equals(type) && customer.getNetAmount() > 0) {
                customerList.add(customer);
            }
        }

        updateUI();
    }

    private void showAllCustomers() {
        customerList.clear();
        customerList.addAll(allCustomersList);
        currentFilter = "all";
        updateUI();
    }

    // FIXED: selectRedCard() method with proper icon color change
    private void selectRedCard() {
        // Red card selected state
        redCard.setBackgroundResource(R.drawable.red_card_selected);
        redCard.setElevation(8f);

        // Red icon and text white
        if (redIcon != null) {
            redIcon.setColorFilter(Color.WHITE);
        }
        if (redAmount != null) {
            redAmount.setTextColor(Color.WHITE);
        }
        if (redText != null) {
            redText.setTextColor(Color.WHITE);
        }

        // Green card normal state
        greenCard.setBackgroundResource(R.drawable.green_card_normal);
        greenCard.setElevation(2f);

        // Green icon and text original color
        if (greenIcon != null) {
            // Use hardcoded color if resource color not working
            greenIcon.setColorFilter(Color.parseColor("#4CAF50"));
        }
        if (greenAmount != null) {
            greenAmount.setTextColor(Color.parseColor("#4CAF50"));
        }
        if (greenText != null) {
            greenText.setTextColor(Color.parseColor("#4CAF50"));
        }

        currentFilter = "red";
    }

    // FIXED: selectGreenCard() method with proper icon color change
    private void selectGreenCard() {
        // Green card selected state
        greenCard.setBackgroundResource(R.drawable.green_card_selected);
        greenCard.setElevation(8f);

        // Green icon and text white
        if (greenIcon != null) {
            greenIcon.setColorFilter(Color.WHITE);
        }
        if (greenAmount != null) {
            greenAmount.setTextColor(Color.WHITE);
        }
        if (greenText != null) {
            greenText.setTextColor(Color.WHITE);
        }

        // Red card normal state
        redCard.setBackgroundResource(R.drawable.red_card_normal);
        redCard.setElevation(2f);

        // Red icon and text original color
        if (redIcon != null) {
            // Use hardcoded color if resource color not working
            redIcon.setColorFilter(Color.parseColor("#F44336"));
        }
        if (redAmount != null) {
            redAmount.setTextColor(Color.parseColor("#F44336"));
        }
        if (redText != null) {
            redText.setTextColor(Color.parseColor("#F44336"));
        }

        currentFilter = "green";
    }

    // FIXED: resetCardSelection() method with proper icon color reset
    private void resetCardSelection() {
        // Both cards normal state
        redCard.setBackgroundResource(R.drawable.red_card_normal);
        greenCard.setBackgroundResource(R.drawable.green_card_normal);

        redCard.setElevation(2f);
        greenCard.setElevation(2f);

        // Reset colors to original
        if (redIcon != null) {
            redIcon.setColorFilter(Color.parseColor("#F44336")); // Red color
        }
        if (greenIcon != null) {
            greenIcon.setColorFilter(Color.parseColor("#4CAF50")); // Green color
        }

        if (redAmount != null) {
            redAmount.setTextColor(Color.parseColor("#F44336"));
        }
        if (greenAmount != null) {
            greenAmount.setTextColor(Color.parseColor("#4CAF50"));
        }

        if (redText != null) {
            redText.setTextColor(Color.parseColor("#F44336"));
        }
        if (greenText != null) {
            greenText.setTextColor(Color.parseColor("#4CAF50"));
        }

        currentFilter = "all";
    }

    @Override
    public void onBackPressed() {
        // AGAR SEARCH BAR MEIN KUCH HAI TO CLEAR KARO
        if (!currentSearchQuery.isEmpty()) {
            searchEditText.setText("");
            currentSearchQuery = "";
            applySearchFilter();
            clearSearch.setVisibility(View.INVISIBLE);
        }
        // AGAR BALANCE CARDS OPEN HAIN TO CLOSE KARO
        else if (isBalanceVisible) {
            toggleBalanceCards();
        }
        // AGAR CARD FILTER ACTIVE HAI TO RESET KARO
        else if (!currentFilter.equals("all")) {
            resetCardSelection();
            showAllCustomers();
        }
        // WARNA APP EXIT KARO (DOUBLE BACK PRESS)
        else {
            // Double back press to exit
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public void onCustomerClick(customer customer) {
        // Customer card click par customer_detail screen par jao
        Intent intent = new Intent(dashboard.this, customer_detail.class);
        intent.putExtra("customer_id", customer.getId());
        intent.putExtra("customer_name", customer.getName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data with delay to avoid glitch
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadCustomers();
                updateTotalCards();
            }
        }, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Database helper close karein
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // PDF RELATED METHODS
    private void generatePdfReport(String startDate, String endDate) {
        try {
            // Filter customers based on date range (currently showing all, you can implement date filtering)
            List<customer> customersForPdf = getCustomersForPdf(startDate, endDate);

            String fileName = "Khatay_Report_" + System.currentTimeMillis() + ".pdf";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Khatay ki Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add date range only if not "All"
            if (!startDate.equals("All")) {
                Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                Paragraph dateRange = new Paragraph();
                dateRange.add(new Chunk("Start Date: " + startDate + "\n", dateFont));
                dateRange.add(new Chunk("End Date: " + endDate + "\n\n", dateFont));
                document.add(dateRange);
            } else {
                Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                Paragraph dateRange = new Paragraph();
                dateRange.add(new Chunk("Date Range: All Customers\n\n", dateFont));
                document.add(dateRange);
            }

            // Create table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {3f, 2f, 2f};
            table.setWidths(columnWidths);

            // Table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new Phrase("Name", headerFont));
            table.addCell(new Phrase("Maine diye Rs.", headerFont));
            table.addCell(new Phrase("Maine liye Rs.", headerFont));

            // Table data
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            double totalDiye = 0;
            double totalLiye = 0;

            for (customer customer : customersForPdf) {
                table.addCell(new Phrase(customer.getName(), dataFont));

                if (customer.getNetAmount() > 0) {
                    table.addCell(new Phrase(String.format("%.2f", customer.getNetAmount()), dataFont));
                    table.addCell(new Phrase("0", dataFont));
                    totalDiye += customer.getNetAmount();
                } else {
                    table.addCell(new Phrase("0", dataFont));
                    table.addCell(new Phrase(String.format("%.2f", Math.abs(customer.getNetAmount())), dataFont));
                    totalLiye += Math.abs(customer.getNetAmount());
                }
            }

            // Add totals row
            table.addCell(new Phrase("TOTAL", headerFont));
            table.addCell(new Phrase(String.format("%.2f", totalDiye), headerFont));
            table.addCell(new Phrase(String.format("%.2f", totalLiye), headerFont));

            document.add(table);
            document.close();
            outputStream.close();

            // Show success dialog with open and share options
            showPdfSuccessDialog(uri, fileName);

        } catch (Exception e) {
            Toast.makeText(this, "PDF Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDF_ERROR", "Error generating PDF", e);
        }
    }

    private List<customer> getCustomersForPdf(String startDate, String endDate) {
        // Currently returning all customers
        // You can implement date-based filtering here
        return allCustomersList;
    }

    private void showPdfSuccessDialog(Uri fileUri, String fileName) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pdf_success, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOpen = dialogView.findViewById(R.id.btnOpen);
        Button btnShare = dialogView.findViewById(R.id.btnShare);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfFile(fileUri);
                dialog.dismiss();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePdfFile(fileUri);
                dialog.dismiss();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openPdfFile(Uri fileUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No PDF viewer app found", Toast.LENGTH_SHORT).show();

            // Open with any available app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "*/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sharePdfFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Khatay ki Report");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file", Toast.LENGTH_SHORT).show();
        }
    }
}
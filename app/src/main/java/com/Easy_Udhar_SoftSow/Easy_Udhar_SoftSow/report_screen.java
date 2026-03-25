package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter.transaction_adapter;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.personal_transaction;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class report_screen extends AppCompatActivity {

    // Views from layout
    private CardView cardHisaab;
    private TextView tvCurrentBalance, tvTotalIncome, tvTotalExpense, tvNegativeWarning;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;
    private BottomNavigationView bottomNavigationView;
    private TextView tvDate;
    private ImageView menuIcon;

    // Data variables
    private double currentBalance = 0.0;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;

    // Transaction list
    private List<personal_transaction> transactionList = new ArrayList<>();
    private transaction_adapter adapter;
    private data_holder dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_screen);

        Log.d("ReportScreen", "onCreate() called");

        // Initialize database helper
        dbHelper = new data_holder(this);

        // Initialize views
        initViews();

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup button click listeners
        setupClickListeners();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadData();

        // Update UI
        updateUI();
    }

    private void initViews() {
        // Main card
        cardHisaab = findViewById(R.id.card_hisaab);

        // Text views
        tvCurrentBalance = findViewById(R.id.tv_current_balance);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvNegativeWarning = findViewById(R.id.tv_negative_warning);
        tvDate = findViewById(R.id.date);

        // Recycler view
        rvTransactions = findViewById(R.id.rv_transactions);

        // Empty state
        emptyState = findViewById(R.id.empty_state);

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Menu icon
        menuIcon = findViewById(R.id.menu_icon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> showPopupMenu(v));
        }

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.report_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_clear_all) {
                    clearAllTransactions();
                    return true;
                } else if (id == R.id.menu_pdf_report) {
                    showDateFilterDialog();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void clearAllTransactions() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clear_transactions, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(true);

        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performClearAll();
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

    private void performClearAll() {
        try {
            boolean success = dbHelper.deleteAllPersonalTransactions();

            if (success) {
                transactionList.clear();
                currentBalance = 0.0;
                totalIncome = 0.0;
                totalExpense = 0.0;
                updateUI();
                Log.d("ReportScreen", "All transactions cleared");
            }
        } catch (Exception e) {
            Log.e("ReportScreen", "Error clearing transactions: " + e.getMessage());
        }
    }

    private void showDateFilterDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_filter, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

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
                    tvStartDate.setText("Select Date");
                    tvEndDate.setText("Select Date");
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

                if (!isAllSelected && (startDate.equals("Select Date") || endDate.equals("Select Date"))) {
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

    private void generatePdfReport(String startDate, String endDate) {
        try {
            List<personal_transaction> transactionsForPdf = getTransactionsForPdf(startDate, endDate);

            if (transactionsForPdf.isEmpty()) {
                return;
            }

            String fileName = "Hisaab_Report_" + System.currentTimeMillis() + ".pdf";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Hisaab Tracker - Personal Transactions Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Paragraph dateRange = new Paragraph();
            if (!startDate.equals("All")) {
                dateRange.add(new Chunk("Start Date: " + startDate + "\n", dateFont));
                dateRange.add(new Chunk("End Date: " + endDate + "\n\n", dateFont));
            } else {
                dateRange.add(new Chunk("Date Range: All Transactions\n\n", dateFont));
            }

            dateRange.add(new Chunk("Current Balance: Rs. " + String.format(Locale.getDefault(), "%.2f", currentBalance) + "\n", dateFont));
            dateRange.add(new Chunk("Total Income: Rs. " + String.format(Locale.getDefault(), "%.2f", totalIncome) + "\n", dateFont));
            dateRange.add(new Chunk("Total Expense: Rs. " + String.format(Locale.getDefault(), "%.2f", totalExpense) + "\n\n", dateFont));
            document.add(dateRange);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {2f, 1.5f, 2f, 2f, 3f};
            table.setWidths(columnWidths);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            table.addCell(new Phrase("Date", headerFont));
            table.addCell(new Phrase("Type", headerFont));
            table.addCell(new Phrase("Amount", headerFont));
            table.addCell(new Phrase("Category", headerFont));
            table.addCell(new Phrase("Description", headerFont));

            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            com.itextpdf.text.BaseColor incomeColor = new com.itextpdf.text.BaseColor(0, 128, 0);
            com.itextpdf.text.BaseColor expenseColor = new com.itextpdf.text.BaseColor(255, 0, 0);

            Font incomeFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, incomeColor);
            Font expenseFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, expenseColor);

            double totalIncomePdf = 0;
            double totalExpensePdf = 0;

            for (personal_transaction transaction : transactionsForPdf) {
                table.addCell(new Phrase(transaction.getDateTime(), dataFont));

                String typeText = "liye".equals(transaction.getType()) ? "Aamdani" : "Kharchay";
                table.addCell(new Phrase(typeText, dataFont));

                double amount = parseAmount(transaction.getAmount());
                if ("liye".equals(transaction.getType())) {
                    table.addCell(new Phrase("Rs. " + String.format(Locale.getDefault(), "%.2f", amount), incomeFont));
                    totalIncomePdf += amount;
                } else {
                    table.addCell(new Phrase("Rs. " + String.format(Locale.getDefault(), "%.2f", amount), expenseFont));
                    totalExpensePdf += amount;
                }

                String description = transaction.getDescription() != null ? transaction.getDescription() : "";
                table.addCell(new Phrase(description, dataFont));
            }

            document.add(table);

            document.add(new Paragraph("\n\n"));

            Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Paragraph totals = new Paragraph();
            totals.add(new Chunk("SUMMARY\n\n", totalFont));
            totals.add(new Chunk("Total Income: Rs. " + String.format(Locale.getDefault(), "%.2f", totalIncomePdf) + "\n", totalFont));
            totals.add(new Chunk("Total Expense: Rs. " + String.format(Locale.getDefault(), "%.2f", totalExpensePdf) + "\n", totalFont));

            double netBalance = totalIncomePdf - totalExpensePdf;
            Font balanceFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,
                    netBalance >= 0 ? new com.itextpdf.text.BaseColor(0, 128, 0) : new com.itextpdf.text.BaseColor(255, 0, 0));
            totals.add(new Chunk("Net Balance: Rs. " + String.format(Locale.getDefault(), "%.2f", netBalance) + "\n\n", balanceFont));

            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
            Paragraph footer = new Paragraph();
            footer.add(new Chunk("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "\n", footerFont));
            footer.add(new Chunk("Hisaab Tracker App", footerFont));
            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(totals);
            document.add(footer);
            document.close();
            outputStream.close();

            showPdfSuccessDialog(uri, fileName);

        } catch (Exception e) {
            Log.e("ReportPDF_ERROR", "Error generating PDF", e);
        }
    }

    private List<personal_transaction> getTransactionsForPdf(String startDate, String endDate) {
        if (startDate.equals("All")) {
            return transactionList;
        } else {
            List<personal_transaction> filteredList = new ArrayList<>();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                for (personal_transaction transaction : transactionList) {
                    try {
                        String transDateStr = transaction.getDateTime().split(" ")[0];
                        Date transDate = sdf.parse(transDateStr);

                        if (!transDate.before(start) && !transDate.after(end)) {
                            filteredList.add(transaction);
                        }
                    } catch (Exception e) {
                        Log.e("PDF_FILTER", "Error parsing transaction date: " + transaction.getDateTime());
                    }
                }
            } catch (Exception e) {
                Log.e("PDF_FILTER", "Error parsing dates: " + e.getMessage());
                return transactionList;
            }
            return filteredList;
        }
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
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "*/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
            }
        }
    }

    private void sharePdfFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hisaab Tracker Report");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share PDF Report via"));
        } catch (Exception e) {
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_khata) {
                Intent intent = new Intent(report_screen.this, dashboard.class);
                startActivity(intent);
                finish();
                return true;

            } else if (id == R.id.nav_batwa) {
                return true;

            } else if (id == R.id.nav_account) {
                Intent intent = new Intent(report_screen.this, account_screen.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_batwa);
    }

    // 🔹 CRITICAL FIX: RecyclerView setup
    private void setupRecyclerView() {
        Log.d("ReportScreen", "setupRecyclerView() called");

        adapter = new transaction_adapter(transactionList, new transaction_adapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(personal_transaction transaction) {
                // ✅ NORMAL CLICK: Adapter khud handle karega
                Log.d("ReportScreen", "Normal click handled by adapter");
            }

            @Override
            public void onTransactionLongClick(personal_transaction transaction, int position) {
                // ✅ LONG CLICK: Adapter khud handle karega
                Log.d("ReportScreen", "Long press handled by adapter");
            }

            @Override
            public void onTransactionDeleted() {
                // ✅ Transaction delete ho gaya hai, data refresh karo
                loadData();
                updateUI();
                Toast.makeText(report_screen.this, "✅ Transaction deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTransactionEdited(personal_transaction transaction) {
                // ✅ Edit ke liye lain_dain_screen open karo
                Log.d("ReportScreen", "Transaction edit requested: " + transaction.getId());

                Intent intent = new Intent(report_screen.this, lain_dain_screen.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("existing_personal_transaction", transaction);
                intent.putExtra("mode", transaction.getType());
                intent.putExtra("source", "report_screen");
                startActivityForResult(intent, 104);
            }
        }, this);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        Log.d("ReportScreen", "RecyclerView setup complete.");
    }

    private void setupClickListeners() {
        LinearLayout btnAddIncome = findViewById(R.id.btn_add_income);
        if (btnAddIncome != null) {
            btnAddIncome.setOnClickListener(v -> {
                Intent intent = new Intent(report_screen.this, lain_dain_screen.class);
                intent.putExtra("mode", "liye");
                intent.putExtra("source", "report_screen");
                startActivityForResult(intent, 101);
            });
        }

        LinearLayout btnAddExpense = findViewById(R.id.btn_add_expense);
        if (btnAddExpense != null) {
            btnAddExpense.setOnClickListener(v -> {
                Intent intent = new Intent(report_screen.this, lain_dain_screen.class);
                intent.putExtra("mode", "diye");
                intent.putExtra("source", "report_screen");
                startActivityForResult(intent, 102);
            });
        }
    }

    private void loadData() {
        setCurrentDate();
        loadTransactionsFromDatabase();
        calculateTotals();

        if (adapter != null) {
            adapter.setTransactionList(transactionList);
            Log.d("ReportScreen", "Adapter notified in loadData");
        }

        Log.d("ReportScreen", "Data loaded - Transactions: " + transactionList.size() +
                ", Income: " + totalIncome +
                ", Expense: " + totalExpense +
                ", Balance: " + currentBalance);
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        if (tvDate != null) {
            tvDate.setText(currentDate);
        }

        Log.d("ReportScreen", "Current date set: " + currentDate);
    }

    private void loadTransactionsFromDatabase() {
        try {
            transactionList.clear();
            List<personal_transaction> transactions = dbHelper.getAllPersonalTransactions();

            if (transactions != null && !transactions.isEmpty()) {
                transactionList.addAll(transactions);
                Log.d("ReportScreen", "Database se load hua: " + transactionList.size() + " transactions");

                for (int i = 0; i < transactionList.size(); i++) {
                    personal_transaction t = transactionList.get(i);
                    Log.d("ReportScreen", "Transaction " + i + ": " +
                            "ID: " + t.getId() +
                            ", Amount: " + t.getAmount() +
                            ", Type: " + t.getType());
                }
            } else {
                Log.d("ReportScreen", "Database mein koi transaction nahi hai");
                transactionList = new ArrayList<>();
            }

        } catch (Exception e) {
            Log.e("ReportScreen", "Error loading transactions: " + e.getMessage());
            transactionList = new ArrayList<>();
        }
    }

    private void calculateTotals() {
        totalIncome = 0.0;
        totalExpense = 0.0;
        currentBalance = 0.0;

        for (personal_transaction transaction : transactionList) {
            if ("liye".equals(transaction.getType())) {
                double amount = parseAmount(transaction.getAmount());
                totalIncome += amount;
                currentBalance += amount;
            } else if ("diye".equals(transaction.getType())) {
                double amount = parseAmount(transaction.getAmount());
                totalExpense += amount;
                currentBalance -= amount;
            }
        }

        Log.d("ReportScreen", "Calculated - Income: " + totalIncome +
                ", Expense: " + totalExpense + ", Balance: " + currentBalance);
    }

    private double parseAmount(String amountStr) {
        try {
            String cleaned = amountStr.replace("Rs", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void updateUI() {
        Log.d("ReportScreen", "updateUI() called - Start");

        tvCurrentBalance.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", currentBalance));
        tvTotalIncome.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", totalIncome));
        tvTotalExpense.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", totalExpense));

        if (currentBalance < 0) {
            cardHisaab.setCardBackgroundColor(Color.parseColor("#b71540"));
            tvNegativeWarning.setVisibility(View.VISIBLE);
            tvNegativeWarning.setText("⚠️ Warning! Aapka balance negative hai: Rs. " +
                    String.format(Locale.getDefault(), "%.2f", currentBalance));
            tvCurrentBalance.setTextColor(Color.parseColor("#ffcccc"));
        } else {
            cardHisaab.setCardBackgroundColor(Color.parseColor("#0c2461"));
            tvNegativeWarning.setVisibility(View.GONE);
            tvCurrentBalance.setTextColor(Color.WHITE);
        }

        if (rvTransactions == null) {
            Log.e("ReportScreen", "rvTransactions NULL hai!");
            rvTransactions = findViewById(R.id.rv_transactions);
        }

        if (adapter == null) {
            Log.w("ReportScreen", "Adapter NULL hai, setup kar raha hoon");
            setupRecyclerView();
        }

        if (transactionList.isEmpty()) {
            Log.d("ReportScreen", "Transaction list EMPTY hai");
            rvTransactions.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
                Log.d("ReportScreen", "Empty state show kiya");
            }
        } else {
            Log.d("ReportScreen", "Transaction list mein " + transactionList.size() + " items hain");

            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
                Log.d("ReportScreen", "Empty state hide kiya");
            }

            rvTransactions.setVisibility(View.VISIBLE);
            Log.d("ReportScreen", "RecyclerView VISIBLE kiya");

            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d("ReportScreen", "Adapter notified. Item count: " + adapter.getItemCount());
            } else {
                Log.e("ReportScreen", "Adapter abhi bhi NULL hai!");
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) rvTransactions.getLayoutManager();
            if (layoutManager != null) {
                Log.d("ReportScreen", "LayoutManager findViewPosition: " +
                        layoutManager.findFirstVisibleItemPosition() + " to " +
                        layoutManager.findLastVisibleItemPosition());
            }
        }

        Log.d("ReportScreen", "updateUI() completed - Balance: " + currentBalance +
                ", Transactions: " + transactionList.size());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ReportScreen", "onActivityResult() called - Request: " + requestCode + ", Result: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == 101 || requestCode == 102 || requestCode == 104) {
                Log.d("ReportScreen", "Transaction saved/updated result received");
                forceRefreshData();

            } else if (requestCode == 103) {
                Log.d("ReportScreen", "Transaction detail result received");
                if (data != null) {
                    boolean personalTransactionUpdated = data.getBooleanExtra("personal_transaction_updated", false);
                    boolean personalTransactionDeleted = data.getBooleanExtra("personal_transaction_deleted", false);

                    Log.d("ReportScreen", "Detail result - Updated: " + personalTransactionUpdated +
                            ", Deleted: " + personalTransactionDeleted);

                    if (personalTransactionUpdated || personalTransactionDeleted) {
                        forceRefreshData();
                    }
                }
            } else if (data != null && data.getBooleanExtra("transaction_added", false)) {
                Log.d("ReportScreen", "Transaction added flag received");
                forceRefreshData();
            }
        }
    }

    private void forceRefreshData() {
        Log.d("ReportScreen", "FORCE REFRESH STARTING...");

        transactionList.clear();
        loadData();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("ReportScreen", "UI Thread mein update kar raha hoon");

                if (adapter != null) {
                    adapter.setTransactionList(transactionList);
                    adapter.notifyDataSetChanged();
                    Log.d("ReportScreen", "Adapter updated with " + transactionList.size() + " items");
                } else {
                    Log.e("ReportScreen", "Adapter NULL hai force refresh mein!");
                    setupRecyclerView();
                }

                updateUI();

                Log.d("ReportScreen", "FORCE REFRESH COMPLETE");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ReportScreen", "onResume() called");
        forceRefreshData();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, dashboard.class);
        startActivity(intent);
        finish();
    }
}
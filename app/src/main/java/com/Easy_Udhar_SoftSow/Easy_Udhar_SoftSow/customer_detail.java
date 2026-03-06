package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.customer;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.data_holder;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.transaction;
import com.Easy_Udhar_SoftSow.R;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class customer_detail extends AppCompatActivity {

    private TextView tvName, profileInitial, tvAmount, tvStatus;
    private LinearLayout btnMaineLiye, btnMaineDiye, middleSection, transactionsContainer;
    private ImageView btnBack, profileImage, menuIcon; // ✅ menuIcon add kiya
    private FrameLayout imgStatusIcon, liyeHainSub, lenayHain;
    private List<transaction> transactionsList = new ArrayList<>();
    private View reportsSection;
    private data_holder dbHelper;
    private int customerId;
    private String customerName;

    // Permission constants
    private static final int SMS_PERMISSION_REQUEST = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        // Initialize Database Helper
        dbHelper = new data_holder(this);

        // Get customer data from Intent
        customerId = getIntent().getIntExtra("customer_id", -1);
        customerName = getIntent().getStringExtra("customer_name");

        if (customerId == -1) {
            Toast.makeText(this, "Customer data load nahi ho paya!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load customer data and transactions
        loadCustomerData();
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadCustomerData();
        loadTransactions();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tv_name);
        profileInitial = findViewById(R.id.profile_initial);
        profileImage = findViewById(R.id.profile_image);
        btnMaineLiye = findViewById(R.id.btn_maine_liye);
        btnMaineDiye = findViewById(R.id.btn_maine_diye);
        btnBack = findViewById(R.id.btn_back);
        menuIcon = findViewById(R.id.menu_icon); // ✅ menuIcon initialize kiya
        tvAmount = findViewById(R.id.tv_amount);
        tvStatus = findViewById(R.id.tv_status);
        middleSection = findViewById(R.id.middle_section);
        transactionsContainer = findViewById(R.id.transactions_container);
        reportsSection = findViewById(R.id.reports_section);

        // Icon frames initialize karein
        imgStatusIcon = findViewById(R.id.img_status_icon);
        liyeHainSub = findViewById(R.id.liye_hain_sub);
        lenayHain = findViewById(R.id.lenay_hain);

        // ✅ NEW: Reports section ke click listeners setup karo
        setupReportsClickListeners();
    }

    private void setupReportsClickListeners() {
        // ✅ REPORTS (PDF) CLICK LISTENER
        ImageView icReports = findViewById(R.id.ic_reports);
        if (icReports != null) {
            icReports.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generateCustomerPdfReport();
                }
            });
        }

        // ✅ WHATSAPP OVERLAY CLICK LISTENER
        ImageView icWhatsappOverlay = findViewById(R.id.ic_whatsapp_overlay);
        if (icWhatsappOverlay != null) {
            icWhatsappOverlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendWhatsAppMessage();
                }
            });
        }

        // ✅ SMS OVERLAY CLICK LISTENER
        ImageView icSmsOverlay = findViewById(R.id.ic_sms_overlay);
        if (icSmsOverlay != null) {
            icSmsOverlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check SMS permission
                    if (ContextCompat.checkSelfPermission(customer_detail.this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(customer_detail.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                SMS_PERMISSION_REQUEST);
                    } else {
                        sendSmsMessage();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsMessage();
            } else {
                Toast.makeText(this, "SMS permission required to send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupClickListeners() {
        // Profile name click listener
        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileScreen();
            }
        });

        // ✅ Profile image click listener
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileScreen();
            }
        });

        // ✅ Profile initial click listener
        profileInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileScreen();
            }
        });

        // ✅ NEW: Menu Icon Click Listener - Popup Menu Show Karen
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        // Back Button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Maine Liye Button
        btnMaineLiye.setOnClickListener(v -> {
            Intent intent = new Intent(customer_detail.this, lain_dain_screen.class);
            intent.putExtra("mode", "liye");
            intent.putExtra("customer_id", customerId);
            intent.putExtra("customer_name", customerName);
            startActivityForResult(intent, 1);
        });

        // Maine Diye Button
        btnMaineDiye.setOnClickListener(v -> {
            Intent intent = new Intent(customer_detail.this, lain_dain_screen.class);
            intent.putExtra("mode", "diye");
            intent.putExtra("customer_id", customerId);
            intent.putExtra("customer_name", customerName);
            startActivityForResult(intent, 1);
        });
    }

    // ✅ NEW: POPUP MENU SHOW KARNE KA METHOD
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_customer_detail, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_view_customer) {
                    // Profile screen open karo
                    openProfileScreen();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    // ✅ NEW: PROFILE SCREEN OPEN KARNE KA COMMON METHOD
    private void openProfileScreen() {
        Intent intent = new Intent(customer_detail.this, profile_screen.class);
        intent.putExtra("customer_id", customerId);
        intent.putExtra("customer_name", customerName);
        startActivityForResult(intent, 100);
    }

    private void loadCustomerData() {
        // ✅ UPDATED: Database se customer details load karein including profile image
        customer customer = dbHelper.getCustomerById(customerId);

        if (customer != null) {
            customerName = customer.getName(); // Update customer name
            tvName.setText(customerName);

            // ✅ NEW: Profile image load karein
            if (customer.hasProfileImage()) {
                // Profile image show karein, initial hide karein
                profileImage.setVisibility(View.VISIBLE);
                profileInitial.setVisibility(View.GONE);

                // Load profile image
                loadProfileImage(customer.getProfileImagePath());
                Log.d("CustomerDetail", "✅ Profile image loaded: " + customer.getProfileImagePath());
            } else {
                // Default - initial show karein, image hide karein
                profileImage.setVisibility(View.GONE);
                profileInitial.setVisibility(View.VISIBLE);

                // Set initial
                String firstLetter = customer.getInitial();
                profileInitial.setText(firstLetter);
                Log.d("CustomerDetail", "ℹ️ No profile image, using initial: " + firstLetter);
            }
        } else {
            tvName.setText("Unknown");
            profileInitial.setText("?");
            profileImage.setVisibility(View.GONE);
            profileInitial.setVisibility(View.VISIBLE);
            Log.e("CustomerDetail", "❌ Customer not found in database");
        }
    }

    // ✅ Profile image load karne ka method
    private void loadProfileImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                Bitmap circularBitmap = getCircularBitmap(bitmap);
                profileImage.setImageBitmap(circularBitmap);
                Log.d("CustomerDetail", "✅ Profile image displayed successfully");
            } else {
                // Agar image load nahi ho payi to initial show karo
                profileImage.setVisibility(View.GONE);
                profileInitial.setVisibility(View.VISIBLE);
                profileInitial.setText(customerName.substring(0, 1).toUpperCase());
                Log.e("CustomerDetail", "❌ Failed to decode profile image");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Agar error aaye to initial show karo
            profileImage.setVisibility(View.GONE);
            profileInitial.setVisibility(View.VISIBLE);
            if (customerName != null && !customerName.isEmpty()) {
                profileInitial.setText(customerName.substring(0, 1).toUpperCase());
            }
            Log.e("CustomerDetail", "❌ Error loading profile image: " + e.getMessage());
        }
    }

    // ✅ Circular Bitmap Banaye (same as profile_screen)
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

    private void loadTransactions() {
        transactionsList.clear();

        // Database se transactions load karein
        List<transaction> dbTransactions = dbHelper.getCustomerTransactions(customerId);

        // ✅ SORT TRANSACTIONS - NEWEST FIRST (BY DATE TIME)
        Collections.sort(dbTransactions, new Comparator<transaction>() {
            @Override
            public int compare(transaction t1, transaction t2) {
                // Naya transaction pehle aaye
                return t2.getDateTime().compareTo(t1.getDateTime());
            }
        });

        transactionsList.addAll(dbTransactions);

        // UI update karein
        updateDisplay();

        // Transaction cards add karein (SORTED ORDER MEIN)
        transactionsContainer.removeAllViews(); // Pehle clear karein
        for (transaction transaction : transactionsList) {
            addTransactionCard(transaction);
        }

        // Hisaab status update karein
        updateHisaabStatus();

        Log.d("CustomerDetail", "📊 Total transactions: " + transactionsList.size());
        for (int i = 0; i < transactionsList.size(); i++) {
            Log.d("CustomerDetail", "Card " + i + ": " + transactionsList.get(i).getDateTime());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CustomerDetail", "🔄 onActivityResult called - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == 1) {
            // Naya transaction add hua hai
            if (resultCode == RESULT_OK && data != null && data.hasExtra("transaction")) {
                transaction newTransaction = (transaction) data.getSerializableExtra("transaction");
                if (newTransaction != null) {
                    Log.d("CustomerDetail", "🆕 New transaction received - Date: " + newTransaction.getDateTime());

                    // 🔹 DATABASE MEIN TRANSACTION SAVE KAREN
                    long transactionId = dbHelper.addTransaction(customerId, newTransaction);

                    if (transactionId != -1) {
                        // Refresh transactions list
                        loadTransactions();
                        Toast.makeText(this, "✅ Transaction save ho gaya!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Transaction save nahi ho paya!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.d("CustomerDetail", "❌ No transaction data received");
            }
        }
        else if (requestCode == 2) {
            // ✅ Full detail screen se wapas aaye hain
            if (resultCode == RESULT_OK && data != null) {
                if (data.hasExtra("transaction_deleted") && data.getBooleanExtra("transaction_deleted", false)) {
                    Log.d("CustomerDetail", "🗑️ Transaction deleted signal received");
                    loadTransactions();
                    Toast.makeText(this, "✅ Transaction deleted successfully", Toast.LENGTH_SHORT).show();
                }
                else if (data.hasExtra("transaction_updated") && data.getBooleanExtra("transaction_updated", false)) {
                    Log.d("CustomerDetail", "✏️ Transaction updated signal received");
                    loadTransactions();
                    Toast.makeText(this, "✅ Transaction updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == 100) {
            // 🔹 Profile screen se aaye hain - customer update hua hai
            if (resultCode == RESULT_OK) {
                Log.d("CustomerDetail", "👤 Customer updated signal received");
                // Refresh customer data including profile image
                loadCustomerData();
                loadTransactions();
                Toast.makeText(this, "✅ Customer updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ NEW METHOD: Saare transaction cards dobara add karein sorted order mein
    private void refreshAllTransactionCards() {
        transactionsContainer.removeAllViews();

        // ✅ Pehle transactions ko sort karein - newest first
        Collections.sort(transactionsList, new Comparator<transaction>() {
            @Override
            public int compare(transaction t1, transaction t2) {
                return t2.getDateTime().compareTo(t1.getDateTime());
            }
        });

        // ✅ Ab sorted list ke hisaab se cards add karein
        for (transaction transaction : transactionsList) {
            addTransactionCard(transaction);
        }

        updateDisplay();
    }

    private void updateDisplay() {
        if (transactionsList.isEmpty()) {
            // Koi transaction nahi hai - middle section show karein
            if (middleSection != null) {
                middleSection.setVisibility(View.VISIBLE);
            }
            if (transactionsContainer != null) {
                transactionsContainer.setVisibility(View.GONE);
            }
            if (reportsSection != null) {
                reportsSection.setVisibility(View.GONE);
            }

            // Default hisaab status
            tvAmount.setText("Rs. 0");
            tvStatus.setText("Hisaab clear hai");
            tvAmount.setTextColor(getResources().getColor(R.color.green));
            tvStatus.setTextColor(getResources().getColor(R.color.green));

            // Default icon show karein
            imgStatusIcon.setVisibility(View.VISIBLE);
            liyeHainSub.setVisibility(View.GONE);
            lenayHain.setVisibility(View.GONE);

        } else {
            // Transactions hain - middle section hide karein
            if (middleSection != null) {
                middleSection.setVisibility(View.GONE);
            }
            if (transactionsContainer != null) {
                transactionsContainer.setVisibility(View.VISIBLE);
            }
            if (reportsSection != null) {
                reportsSection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateHisaabStatus() {
        // Database se net amount calculate karein
        double netAmount = dbHelper.getCustomerNetAmount(customerId);

        // Card view find karein
        CardView cardHisaab = findViewById(R.id.card_hisaab);

        // ✅ NEW: Reports section ke icons ke liye
        ImageView icSmsBackground = findViewById(R.id.ic_sms_background);
        ImageView icSmsOverlay = findViewById(R.id.ic_sms_overlay);
        ImageView icWhatsappBackground = findViewById(R.id.ic_whatsapp_background);
        ImageView icWhatsappOverlay = findViewById(R.id.ic_whatsapp_overlay);

        if (netAmount > 0) {
            // Maine Liye zyada hai - Customer ne aapko diya hai (GREEN STATUS)
            tvAmount.setText("Rs. " + String.format("%.2f", Math.abs(netAmount)));
            tvStatus.setText("Maine dene hain");
            tvAmount.setTextColor(getResources().getColor(R.color.green));
            tvStatus.setTextColor(getResources().getColor(R.color.green));

            // Icons update karein - Liye wala icon show karo
            imgStatusIcon.setVisibility(View.GONE);
            liyeHainSub.setVisibility(View.VISIBLE);
            lenayHain.setVisibility(View.GONE);

            // Card background - Green
            cardHisaab.setCardBackgroundColor(getResources().getColor(R.color.light_green));

            // ✅ SMS & WHATSAPP - BACKGROUND SHOW, OVERLAY HIDE (GREEN STATUS)
            if (icSmsBackground != null) icSmsBackground.setVisibility(View.VISIBLE);
            if (icSmsOverlay != null) icSmsOverlay.setVisibility(View.GONE);
            if (icWhatsappBackground != null) icWhatsappBackground.setVisibility(View.VISIBLE);
            if (icWhatsappOverlay != null) icWhatsappOverlay.setVisibility(View.GONE);

        } else if (netAmount < 0) {
            // Maine Diye zyada hai - Aapne customer ko diya hai (RED STATUS)
            tvAmount.setText("Rs. " + String.format("%.2f", Math.abs(netAmount)));
            tvStatus.setText("Maine lene hain");
            tvAmount.setTextColor(getResources().getColor(R.color.red));
            tvStatus.setTextColor(getResources().getColor(R.color.red));

            // Icons update karein - Diye wala icon show karo
            imgStatusIcon.setVisibility(View.GONE);
            liyeHainSub.setVisibility(View.GONE);
            lenayHain.setVisibility(View.VISIBLE);

            // Card background - Light Red
            cardHisaab.setCardBackgroundColor(getResources().getColor(R.color.light_red));

            // ✅ SMS & WHATSAPP - BACKGROUND HIDE, OVERLAY SHOW (RED STATUS)
            if (icSmsBackground != null) icSmsBackground.setVisibility(View.GONE);
            if (icSmsOverlay != null) icSmsOverlay.setVisibility(View.VISIBLE);
            if (icWhatsappBackground != null) icWhatsappBackground.setVisibility(View.GONE);
            if (icWhatsappOverlay != null) icWhatsappOverlay.setVisibility(View.VISIBLE);

        } else {
            // Hisaab clear (GREEN STATUS)
            tvAmount.setText("Rs. 0");
            tvStatus.setText("Hisaab clear hai");
            tvAmount.setTextColor(getResources().getColor(R.color.green));
            tvStatus.setTextColor(getResources().getColor(R.color.green));

            // Icons update karein - Default icon show karo
            imgStatusIcon.setVisibility(View.VISIBLE);
            liyeHainSub.setVisibility(View.GONE);
            lenayHain.setVisibility(View.GONE);

            // Card background - Green
            cardHisaab.setCardBackgroundColor(getResources().getColor(R.color.light_green));

            // ✅ SMS & WHATSAPP - BACKGROUND SHOW, OVERLAY HIDE (GREEN STATUS)
            if (icSmsBackground != null) icSmsBackground.setVisibility(View.VISIBLE);
            if (icSmsOverlay != null) icSmsOverlay.setVisibility(View.GONE);
            if (icWhatsappBackground != null) icWhatsappBackground.setVisibility(View.VISIBLE);
            if (icWhatsappOverlay != null) icWhatsappOverlay.setVisibility(View.GONE);
        }
    }

    private void addTransactionCard(transaction transaction) {
        if (transactionsContainer == null) return;

        // Layout inflate karein
        View cardView = LayoutInflater.from(this).inflate(R.layout.customer_detail_card, transactionsContainer, false);

        // Views find karein
        TextView tvDateTime = cardView.findViewById(R.id.tvDateTime);
        TextView tvAmountGreen = cardView.findViewById(R.id.tvAmountGreen);
        TextView tvAmountRed = cardView.findViewById(R.id.tvAmountRed);
        TextView tvDescription = cardView.findViewById(R.id.tvDescription);
        TextView tvBalance = cardView.findViewById(R.id.tvBalance);

        // Data set karein
        tvDateTime.setText(transaction.getDateTime());

        // Description set karein (agar empty hai to hide karein)
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty() && !transaction.getDescription().equals("null")) {
            tvDescription.setText(transaction.getDescription());
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // 🔹 IMPORTANT: TV Balance mein card_hisaab ke tv_amount wali amount show karo
        String netBalanceAmount = tvAmount.getText().toString(); // "Rs. X" format
        tvBalance.setText("Bal. " + netBalanceAmount);

        // Amount type ke hisaab se show karein
        if ("liye".equals(transaction.getType())) {
            // Maine Liye - Green amount show karein
            tvAmountGreen.setText("Rs. " + transaction.getAmount());
            tvAmountGreen.setVisibility(View.VISIBLE);
            tvAmountRed.setVisibility(View.GONE);
        } else {
            // Maine Diye - Red amount show karein
            tvAmountRed.setText("Rs. " + transaction.getAmount());
            tvAmountRed.setVisibility(View.VISIBLE);
            tvAmountGreen.setVisibility(View.GONE);
        }

        // 🔹 Balance ka color bhi card_hisaab ke according set karo
        if (lenayHain.getVisibility() == View.VISIBLE) {
            // Red status - Maine lene hain
            tvBalance.setTextColor(getResources().getColor(R.color.red));
            tvBalance.setBackgroundResource(R.drawable.bg_balance_red);
        } else {
            // Green status - Maine dene hain ya hisaab clear
            tvBalance.setTextColor(getResources().getColor(R.color.green));
            tvBalance.setBackgroundResource(R.drawable.bg_balance_green);
        }

        // 🔹 UPDATED: CARD CLICK LISTENER - Customer ID bhi pass karein
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(customer_detail.this, full_detail_screen.class);
                intent.putExtra("transaction", transaction);
                intent.putExtra("customer_id", customerId); // ✅ IMPORTANT: Customer ID pass karein
                startActivityForResult(intent, 2); // ✅ Different request code use karein
            }
        });

        // Card ko container mein add karein
        transactionsContainer.addView(cardView);

        Log.d("CustomerDetail", "➕ Card added - Date: " + transaction.getDateTime());
    }

    // ✅ CUSTOMER PDF REPORT GENERATE KARNE KA METHOD
    private void generateCustomerPdfReport() {
        try {
            // Database se customer details aur transactions load karein
            customer customer = dbHelper.getCustomerById(customerId);
            List<transaction> transactions = dbHelper.getCustomerTransactions(customerId);

            if (customer == null) {
                Toast.makeText(this, "Customer data not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = customer.getName() + "_Report_" + System.currentTimeMillis() + ".pdf";

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
            Paragraph title = new Paragraph(customer.getName() + " - Khatay ki Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add customer details
            Font detailFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Paragraph details = new Paragraph();
            details.add(new Chunk("Customer Name: " + customer.getName() + "\n", detailFont));
            if (customer.getPhone() != null && !customer.getPhone().isEmpty() && !customer.getPhone().equals("null")) {
                details.add(new Chunk("Phone: " + customer.getPhone() + "\n", detailFont));
            }
            details.add(new Chunk("Net Balance: " + tvAmount.getText().toString() + "\n", detailFont));
            details.add(new Chunk("Status: " + tvStatus.getText().toString() + "\n\n", detailFont));
            document.add(details);

            // Create transactions table
            if (!transactions.isEmpty()) {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                float[] columnWidths = {3f, 2f, 2f, 2f};
                table.setWidths(columnWidths);

                // Table headers
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                table.addCell(new Phrase("Date Time", headerFont));
                table.addCell(new Phrase("Type", headerFont));
                table.addCell(new Phrase("Amount", headerFont));
                table.addCell(new Phrase("Description", headerFont));

                // Table data
                Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
                for (transaction t : transactions) {
                    table.addCell(new Phrase(t.getDateTime(), dataFont));
                    table.addCell(new Phrase("liye".equals(t.getType()) ? "Maine Liye" : "Maine Diye", dataFont));
                    table.addCell(new Phrase("Rs. " + t.getAmount(), dataFont));
                    table.addCell(new Phrase(t.getDescription() != null ? t.getDescription() : "", dataFont));
                }

                document.add(table);
            } else {
                Paragraph noTransactions = new Paragraph("No transactions found", detailFont);
                noTransactions.setAlignment(Element.ALIGN_CENTER);
                document.add(noTransactions);
            }

            document.close();
            outputStream.close();

            // Show success dialog
            showPdfSuccessDialog(uri, fileName);

        } catch (Exception e) {
            Toast.makeText(this, "PDF Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PDF_ERROR", "Error generating customer PDF", e);
        }
    }

    // ✅ WHATSAPP MESSAGE BHEJNE KA METHOD
    private void sendWhatsAppMessage() {
        // Database se customer details load karein
        customer customer = dbHelper.getCustomerById(customerId);

        if (customer == null || customer.getPhone() == null || customer.getPhone().isEmpty() || customer.getPhone().equals("null")) {
            Toast.makeText(this, "Customer ka phone number available nahi hai", Toast.LENGTH_SHORT).show();

            // Phone number nahi hai to profile screen par redirect karo
            Intent intent = new Intent(customer_detail.this, profile_screen.class);
            intent.putExtra("customer_id", customerId);
            intent.putExtra("customer_name", customerName);
            startActivityForResult(intent, 100);
            return;
        }

        String phoneNumber = customer.getPhone();

        // Phone number format karo (country code ke saath)
        if (!phoneNumber.startsWith("+")) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "92" + phoneNumber.substring(1); // Pakistan ke liye
            } else if (phoneNumber.length() == 10) {
                phoneNumber = "91" + phoneNumber; // India ke liye
            }
            phoneNumber = "+" + phoneNumber;
        }

        // Message content banaye
        String amount = tvAmount.getText().toString();
        String status = tvStatus.getText().toString();

        String message = "Moaziz customer,\n\n" +
                "Aap ne " + customerName + " ko " + amount + " adaa karne hain.\n\n" +
                "Jab khata asaan, banao grocery shopping bhi asaan! Karachi walon, Bazaar app download karo aur behtareen rates par rashan mangwao";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp install nahi hai", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ SMS MESSAGE BHEJNE KA METHOD
    private void sendSmsMessage() {
        // Database se customer details load karein
        customer customer = dbHelper.getCustomerById(customerId);

        if (customer == null || customer.getPhone() == null || customer.getPhone().isEmpty() || customer.getPhone().equals("null")) {
            Toast.makeText(this, "Customer ka phone number available nahi hai", Toast.LENGTH_SHORT).show();

            // Phone number nahi hai to profile screen par redirect karo
            Intent intent = new Intent(customer_detail.this, profile_screen.class);
            intent.putExtra("customer_id", customerId);
            intent.putExtra("customer_name", customerName);
            startActivityForResult(intent, 100);
            return;
        }

        String phoneNumber = customer.getPhone();

        // Message content banaye
        String amount = tvAmount.getText().toString();

        String message = "Moaziz customer,\n\n" +
                "Aap ne " + customerName + " ko " + amount + " adaa karne hain.\n\n" +
                "Jab khata asaan, banao grocery shopping bhi asaan! Karachi walon, Bazaar app download karo aur behtareen rates par rashan mangwao";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "SMS app nahi hai", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ PDF SUCCESS DIALOG
    private void showPdfSuccessDialog(Uri fileUri, String fileName) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pdf_success, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOpen = dialogView.findViewById(R.id.btnOpen);
        Button btnShare = dialogView.findViewById(R.id.btnShare);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        btnOpen.setOnClickListener(v -> {
            openPdfFile(fileUri);
            dialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            sharePdfFile(fileUri);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    // ✅ PDF FILE OPEN KARNE KA METHOD
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

    // ✅ PDF FILE SHARE KARNE KA METHOD
    private void sharePdfFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, customerName + " - Khatay ki Report");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // ✅ SMOOTH TRANSITION: Animation add karo
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Dashboard par wapas jao
        Intent intent = new Intent(customer_detail.this, dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Database helper close karein
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
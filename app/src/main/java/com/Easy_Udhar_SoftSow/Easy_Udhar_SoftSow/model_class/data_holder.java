package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class data_holder extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "KhataApp.db";
    private static final int DATABASE_VERSION = 5; // Version 5 کر دیا

    // Customers table
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String COLUMN_CUSTOMER_ID = "customer_id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_CUSTOMER_PHONE = "customer_phone";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image_path";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Customer transactions table
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "transaction_id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE_TIME = "date_time";
    private static final String COLUMN_BALANCE = "balance";
    private static final String COLUMN_IMAGE_PATH = "image_path";

    // Personal transactions table
    private static final String TABLE_PERSONAL_TRANSACTIONS = "personal_transactions";
    private static final String COLUMN_ID = "id"; // ID column for personal transactions

    // Deleted customers table
    private static final String TABLE_DELETED_CUSTOMERS = "deleted_customers";
    private static final String COLUMN_DELETED_ID = "deleted_id";
    private static final String COLUMN_ORIGINAL_ID = "original_id";
    private static final String COLUMN_DELETED_NAME = "deleted_name";
    private static final String COLUMN_DELETED_PHONE = "deleted_phone";
    private static final String COLUMN_DELETED_PROFILE_IMAGE = "deleted_profile_image";
    private static final String COLUMN_DELETED_NET_AMOUNT = "deleted_net_amount";
    private static final String COLUMN_DELETED_AT = "deleted_at";

    public data_holder(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create customers table
        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + "("
                + COLUMN_CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CUSTOMER_NAME + " TEXT,"
                + COLUMN_CUSTOMER_PHONE + " TEXT,"
                + COLUMN_PROFILE_IMAGE + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_CUSTOMERS_TABLE);

        // Create customer transactions table
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CUSTOMER_ID + " INTEGER,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE_TIME + " TEXT,"
                + COLUMN_BALANCE + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_CUSTOMER_ID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + COLUMN_CUSTOMER_ID + ")"
                + ")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

        // Create personal transactions table
        String CREATE_PERSONAL_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_PERSONAL_TRANSACTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " TEXT," // "liye" or "diye"
                + COLUMN_AMOUNT + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATE_TIME + " TEXT,"
                + COLUMN_IMAGE_PATH + " TEXT"
                + ")";
        db.execSQL(CREATE_PERSONAL_TRANSACTIONS_TABLE);

        // Create deleted customers table
        String CREATE_DELETED_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_DELETED_CUSTOMERS + "("
                + COLUMN_DELETED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORIGINAL_ID + " INTEGER,"
                + COLUMN_DELETED_NAME + " TEXT,"
                + COLUMN_DELETED_PHONE + " TEXT,"
                + COLUMN_DELETED_PROFILE_IMAGE + " TEXT,"
                + COLUMN_DELETED_NET_AMOUNT + " REAL,"
                + COLUMN_DELETED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_DELETED_CUSTOMERS_TABLE);

        Log.d("Database", "✅ Database created with all tables including personal transactions");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COLUMN_BALANCE + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COLUMN_IMAGE_PATH + " TEXT");
                Log.d("Database", "✅ Database upgraded to version 2 with new columns");
            } catch (Exception e) {
                Log.e("Database", "❌ Error upgrading database to version 2: " + e.getMessage());
                recreateDatabase(db);
            }
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COLUMN_PROFILE_IMAGE + " TEXT");
                Log.d("Database", "✅ Database upgraded to version 3 with profile_image_path column");
            } catch (Exception e) {
                Log.e("Database", "❌ Error adding profile image column: " + e.getMessage());
                recreateDatabase(db);
            }
        }

        if (oldVersion < 4) {
            try {
                String CREATE_DELETED_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_DELETED_CUSTOMERS + "("
                        + COLUMN_DELETED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_ORIGINAL_ID + " INTEGER,"
                        + COLUMN_DELETED_NAME + " TEXT,"
                        + COLUMN_DELETED_PHONE + " TEXT,"
                        + COLUMN_DELETED_PROFILE_IMAGE + " TEXT,"
                        + COLUMN_DELETED_NET_AMOUNT + " REAL,"
                        + COLUMN_DELETED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                db.execSQL(CREATE_DELETED_CUSTOMERS_TABLE);
                Log.d("Database", "✅ Database upgraded to version 4 with deleted customers table");
            } catch (Exception e) {
                Log.e("Database", "❌ Error creating deleted customers table: " + e.getMessage());
                recreateDatabase(db);
            }
        }

        if (oldVersion < 5) {
            try {
                // Personal transactions table add karein
                String CREATE_PERSONAL_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_PERSONAL_TRANSACTIONS + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_TYPE + " TEXT,"
                        + COLUMN_AMOUNT + " TEXT,"
                        + COLUMN_DESCRIPTION + " TEXT,"
                        + COLUMN_DATE_TIME + " TEXT,"
                        + COLUMN_IMAGE_PATH + " TEXT"
                        + ")";
                db.execSQL(CREATE_PERSONAL_TRANSACTIONS_TABLE);
                Log.d("Database", "✅ Database upgraded to version 5 with personal transactions table");
            } catch (Exception e) {
                Log.e("Database", "❌ Error creating personal transactions table: " + e.getMessage());
                recreateDatabase(db);
            }
        }
    }

    private void recreateDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONAL_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_CUSTOMERS);
        onCreate(db);
    }

    // ==================== PERSONAL TRANSACTIONS METHODS ====================

    // Personal transactions ke liye methods
    public long insertPersonalTransaction(personal_transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_DATE_TIME, transaction.getDateTime());
        values.put(COLUMN_IMAGE_PATH, transaction.getImagePath());

        long id = db.insert(TABLE_PERSONAL_TRANSACTIONS, null, values);
        db.close();

        if (id != -1) {
            Log.d("Database", "✅ Personal transaction added with ID: " + id);
            Log.d("Database", "📊 Details - Type: " + transaction.getType() +
                    ", Amount: " + transaction.getAmount() +
                    ", Description: " + transaction.getDescription());
        } else {
            Log.e("Database", "❌ Failed to add personal transaction");
        }

        return id;
    }

    // data_holder.java mein yeh method add karen
    public boolean deleteAllPersonalTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_PERSONAL_TRANSACTIONS, null, null);
            Log.d("Database", "Deleted " + rowsDeleted + " personal transactions");
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("Database", "Error deleting transactions: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    public List<personal_transaction> getAllPersonalTransactions() {
        List<personal_transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // ✅ FIXED: ORDER BY id DESC - ID se sort karo (hamesha latest sabse upar)
        String query = "SELECT * FROM " + TABLE_PERSONAL_TRANSACTIONS +
                " ORDER BY " + COLUMN_ID + " DESC";  // ID se sorting

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                personal_transaction transaction = new personal_transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                transaction.setAmount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                transaction.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)));
                transaction.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));

                transactions.add(transaction);

                Log.d("Database", "📖 Loaded personal transaction - ID: " + transaction.getId() +
                        ", Type: " + transaction.getType() +
                        ", Amount: " + transaction.getAmount());

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return transactions;
    }
    public boolean deletePersonalTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PERSONAL_TRANSACTIONS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(transactionId)});
        db.close();

        boolean success = result > 0;
        Log.d("Database", "🗑️ Personal transaction deletion " + (success ? "successful" : "failed"));
        return success;
    }

    public boolean updatePersonalTransaction(personal_transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_DATE_TIME, transaction.getDateTime());
        values.put(COLUMN_IMAGE_PATH, transaction.getImagePath());

        int result = db.update(TABLE_PERSONAL_TRANSACTIONS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});
        db.close();

        boolean success = result > 0;
        Log.d("Database", "✏️ Personal transaction update " + (success ? "successful" : "failed"));
        return success;
    }

    public personal_transaction getPersonalTransactionById(int transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PERSONAL_TRANSACTIONS +
                " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(transactionId)});

        personal_transaction transaction = null;
        if (cursor.moveToFirst()) {
            transaction = new personal_transaction();
            transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            transaction.setAmount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            transaction.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_TIME)));
            transaction.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));

            Log.d("Database", "📄 Personal transaction details loaded - ID: " + transactionId);
        }

        cursor.close();
        db.close();
        return transaction;
    }

    public double getPersonalTransactionsSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        double netAmount = 0;

        try {
            // Personal transactions ka net amount calculate karein
            String query = "SELECT " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'liye' THEN CAST(" + COLUMN_AMOUNT + " AS REAL) ELSE 0 END) - " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'diye' THEN CAST(" + COLUMN_AMOUNT + " AS REAL) ELSE 0 END) as net_amount " +
                    "FROM " + TABLE_PERSONAL_TRANSACTIONS;

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                netAmount = cursor.getDouble(0);
            }
            cursor.close();

            Log.d("Database", "💰 Personal transactions net amount: " + netAmount);
        } catch (Exception e) {
            Log.e("Database", "❌ Error calculating personal transactions summary: " + e.getMessage());
        } finally {
            db.close();
        }

        return netAmount;
    }

    // ==================== CUSTOMER METHODS ====================

    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, name);
        values.put(COLUMN_CUSTOMER_PHONE, phone);
        values.put(COLUMN_PROFILE_IMAGE, "");
        long result = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();

        if (result != -1) {
            Log.d("Database", "✅ Customer added: " + name);
        } else {
            Log.e("Database", "❌ Failed to add customer: " + name);
        }
        return result;
    }

    public List<customer> getAllCustomers() {
        List<customer> customerList = new ArrayList<>();
        // ✅ DESCENDING ORDER - NAYA CARD TOP PAR
        String query = "SELECT * FROM " + TABLE_CUSTOMERS + " ORDER BY " + COLUMN_CUSTOMER_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                customer customer = new customer();
                customer.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
                customer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
                customer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));

                String profileImagePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_IMAGE));
                customer.setProfileImagePath(profileImagePath);

                double netAmount = getCustomerNetAmount(customer.getId());
                customer.setNetAmount(netAmount);

                customerList.add(customer);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return customerList;
    }

    public customer getCustomerById(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CUSTOMERS +
                " WHERE " + COLUMN_CUSTOMER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(customerId)});

        customer customer = null;
        if (cursor.moveToFirst()) {
            customer = new customer();
            customer.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
            customer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
            customer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));

            String profileImagePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_IMAGE));
            customer.setProfileImagePath(profileImagePath);

            double netAmount = getCustomerNetAmount(customerId);
            customer.setNetAmount(netAmount);

            Log.d("Database", "👤 Customer details loaded - ID: " + customerId +
                    ", Name: " + customer.getName() +
                    ", Profile Image: " + (customer.hasProfileImage() ? customer.getProfileImagePath() : "No Image"));
        }

        cursor.close();
        db.close();
        return customer;
    }

    public boolean checkCustomerExists(String name, String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CUSTOMERS +
                " WHERE " + COLUMN_CUSTOMER_NAME + " = ? OR " + COLUMN_CUSTOMER_PHONE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, phone});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ✅ CORRECTED: Delete customer method
    public boolean deleteCustomer(int customerId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Pehle customer details get karein (SAME DATABASE CONNECTION USE KARO)
            String customerQuery = "SELECT * FROM " + TABLE_CUSTOMERS +
                    " WHERE " + COLUMN_CUSTOMER_ID + " = ?";
            Cursor cursor = db.rawQuery(customerQuery, new String[]{String.valueOf(customerId)});

            customer customerToDelete = null;
            if (cursor.moveToFirst()) {
                customerToDelete = new customer();
                customerToDelete.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
                customerToDelete.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
                customerToDelete.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));
                customerToDelete.setProfileImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_IMAGE)));

                // Net amount calculate karo directly
                String netAmountQuery = "SELECT " +
                        "SUM(CASE WHEN " + COLUMN_TYPE + " = 'liye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) - " +
                        "SUM(CASE WHEN " + COLUMN_TYPE + " = 'diye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) as net_amount " +
                        "FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COLUMN_CUSTOMER_ID + " = ?";
                Cursor amountCursor = db.rawQuery(netAmountQuery, new String[]{String.valueOf(customerId)});
                if (amountCursor.moveToFirst()) {
                    customerToDelete.setNetAmount(amountCursor.getDouble(0));
                }
                amountCursor.close();
            }
            cursor.close();

            if (customerToDelete == null) {
                Log.e("Database", "❌ Customer not found: " + customerId);
                db.close();
                return false;
            }

            // Pehle transactions delete karo
            int transactionsDeleted = db.delete(TABLE_TRANSACTIONS,
                    COLUMN_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)});
            Log.d("Database", "🗑️ Deleted " + transactionsDeleted + " transactions");

            // Ab deleted customers table mein save karein
            ContentValues deletedValues = new ContentValues();
            deletedValues.put(COLUMN_ORIGINAL_ID, customerToDelete.getId());
            deletedValues.put(COLUMN_DELETED_NAME, customerToDelete.getName());
            deletedValues.put(COLUMN_DELETED_PHONE, customerToDelete.getPhone());
            deletedValues.put(COLUMN_DELETED_PROFILE_IMAGE, customerToDelete.getProfileImagePath());
            deletedValues.put(COLUMN_DELETED_NET_AMOUNT, customerToDelete.getNetAmount());

            long deletedResult = db.insert(TABLE_DELETED_CUSTOMERS, null, deletedValues);

            if (deletedResult == -1) {
                Log.e("Database", "❌ Failed to save to deleted table");
                db.close();
                return false;
            }

            // Ab original customer delete karo
            int customerDeleteResult = db.delete(TABLE_CUSTOMERS,
                    COLUMN_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customerId)});

            boolean success = customerDeleteResult > 0;

            if (success) {
                Log.d("Database", "✅ Customer successfully deleted: " + customerToDelete.getName());
            } else {
                Log.e("Database", "❌ Failed to delete customer");
            }

            return success;

        } catch (Exception e) {
            Log.e("Database", "❌ Error in deleteCustomer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // FINALLY BLOCK MEIN CLOSE KARO
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean updateCustomer(int customerId, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, name);
        values.put(COLUMN_CUSTOMER_PHONE, phone);

        int result = db.update(TABLE_CUSTOMERS, values,
                COLUMN_CUSTOMER_ID + " = ?",
                new String[]{String.valueOf(customerId)});
        db.close();

        boolean success = result > 0;
        Log.d("Database", "✏️ Customer update " + (success ? "successful" : "failed"));
        return success;
    }

    public boolean updateCustomerProfileImage(int customerId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_IMAGE, imagePath);

        int result = db.update(TABLE_CUSTOMERS, values,
                COLUMN_CUSTOMER_ID + " = ?",
                new String[]{String.valueOf(customerId)});
        db.close();

        boolean success = result > 0;
        Log.d("Database", "📸 Profile image update " + (success ? "successful" : "failed") +
                " - Customer: " + customerId + ", Image Path: " + imagePath);
        return success;
    }

    public List<customer> searchCustomers(String query) {
        List<customer> customerList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String searchQuery = "SELECT * FROM " + TABLE_CUSTOMERS +
                " WHERE " + COLUMN_CUSTOMER_NAME + " LIKE ? OR " + COLUMN_CUSTOMER_PHONE + " LIKE ?" +
                " ORDER BY " + COLUMN_CUSTOMER_NAME + " ASC";

        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + query + "%", "%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                customer customer = new customer();
                customer.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_CUSTOMER_ID)));
                customer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)));
                customer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_PHONE)));

                String profileImagePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_IMAGE));
                customer.setProfileImagePath(profileImagePath);

                double netAmount = getCustomerNetAmount(customer.getId());
                customer.setNetAmount(netAmount);

                customerList.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return customerList;
    }

    // ==================== DELETED CUSTOMERS METHODS ====================

    // ✅ CORRECTED: Get deleted customers method
    public List<deleted_customer> getDeletedCustomers() {
        List<deleted_customer> deletedCustomersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_DELETED_CUSTOMERS +
                    " ORDER BY " + COLUMN_DELETED_AT + " DESC";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    deleted_customer deletedCustomer = new deleted_customer();

                    // SAB FIELDS PROPERLY SET KARO
                    deletedCustomer.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_DELETED_ID)));
                    deletedCustomer.setOriginalId(cursor.getInt(cursor.getColumnIndex(COLUMN_ORIGINAL_ID)));
                    deletedCustomer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_NAME)));
                    deletedCustomer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PHONE)));

                    // Profile image path
                    String profileImage = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PROFILE_IMAGE));
                    deletedCustomer.setProfileImagePath(profileImage != null ? profileImage : "");

                    deletedCustomer.setNetAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_DELETED_NET_AMOUNT)));

                    // Deleted date
                    String deletedDate = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_AT));
                    deletedCustomer.setDeletedDate(deletedDate != null ? deletedDate : "");

                    deletedCustomersList.add(deletedCustomer);

                    Log.d("Database", "🗑️ Loaded deleted customer: " + deletedCustomer.getName() +
                            ", ID: " + deletedCustomer.getId() +
                            ", Original ID: " + deletedCustomer.getOriginalId());

                } while (cursor.moveToNext());
            } else {
                Log.d("Database", "ℹ️ No deleted customers found");
            }

        } catch (Exception e) {
            Log.e("Database", "❌ Error loading deleted customers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return deletedCustomersList;
    }

    // ✅ CORRECTED: Restore customer method
    // ✅ CORRECTED: Restore customer method with transactions
    public boolean restoreCustomer(int deletedCustomerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        try {
            // 1. Pehle deleted customer details get karein
            String query = "SELECT * FROM " + TABLE_DELETED_CUSTOMERS +
                    " WHERE " + COLUMN_DELETED_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(deletedCustomerId)});

            if (!cursor.moveToFirst()) {
                Log.e("Database", "❌ Deleted customer not found: " + deletedCustomerId);
                return false;
            }

            // 2. Data extract karo
            int originalId = cursor.getInt(cursor.getColumnIndex(COLUMN_ORIGINAL_ID));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PHONE));
            String profileImage = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PROFILE_IMAGE));
            double netAmount = cursor.getDouble(cursor.getColumnIndex(COLUMN_DELETED_NET_AMOUNT));

            Log.d("Database", "🔄 Restoring customer: " + name +
                    ", Original ID: " + originalId +
                    ", Net Amount: " + netAmount);

            // 3. Naya customer create karein (original ID use karein agar possible ho)
            ContentValues values = new ContentValues();
            values.put(COLUMN_CUSTOMER_NAME, name);
            values.put(COLUMN_CUSTOMER_PHONE, phone);
            values.put(COLUMN_PROFILE_IMAGE, profileImage != null ? profileImage : "");

            // Try to use original ID if available
            long newCustomerId;
            if (originalId > 0) {
                // Original ID restore karein (agar possible ho)
                try {
                    values.put(COLUMN_CUSTOMER_ID, originalId);
                    newCustomerId = db.insertWithOnConflict(TABLE_CUSTOMERS, null, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    Log.d("Database", "✅ Customer restored with original ID: " + originalId);
                } catch (Exception e) {
                    // Agar original ID nahi chal raha to new ID generate karein
                    values.remove(COLUMN_CUSTOMER_ID);
                    newCustomerId = db.insert(TABLE_CUSTOMERS, null, values);
                    Log.d("Database", "🆕 Customer restored with new ID: " + newCustomerId);
                }
            } else {
                newCustomerId = db.insert(TABLE_CUSTOMERS, null, values);
                Log.d("Database", "🆕 Customer restored with new ID: " + newCustomerId);
            }

            if (newCustomerId == -1) {
                Log.e("Database", "❌ Failed to restore customer");
                return false;
            }

            // 4. ✅ IMPORTANT: Customer ki dummy transaction add karein (taki amount zero na ho)
            if (netAmount != 0) {
                addDummyTransactionForRestoredCustomer(db, (int) newCustomerId, netAmount);
            }

            // 5. Ab deleted customer ko delete karein
            int deleteResult = db.delete(TABLE_DELETED_CUSTOMERS,
                    COLUMN_DELETED_ID + " = ?",
                    new String[]{String.valueOf(deletedCustomerId)});

            boolean success = deleteResult > 0;
            Log.d("Database", "🔄 Customer restore " + (success ? "successful" : "failed"));
            return success;

        } catch (Exception e) {
            Log.e("Database", "❌ Error restoring customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // ✅ NEW METHOD: Dummy transaction add karein restored customer ke liye
    private void addDummyTransactionForRestoredCustomer(SQLiteDatabase db, int customerId, double netAmount) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CUSTOMER_ID, customerId);

            if (netAmount > 0) {
                // Agar positive amount hai to "liye" transaction
                values.put(COLUMN_TYPE, "liye");
                values.put(COLUMN_AMOUNT, netAmount);
                values.put(COLUMN_DESCRIPTION, "Restored customer - Previous balance");
            } else {
                // Agar negative amount hai to "diye" transaction
                values.put(COLUMN_TYPE, "diye");
                values.put(COLUMN_AMOUNT, Math.abs(netAmount));
                values.put(COLUMN_DESCRIPTION, "Restored customer - Previous balance");
            }

            values.put(COLUMN_DATE_TIME, getCurrentDateTime());
            values.put(COLUMN_BALANCE, "");
            values.put(COLUMN_IMAGE_PATH, "");

            long result = db.insert(TABLE_TRANSACTIONS, null, values);

            if (result != -1) {
                Log.d("Database", "✅ Dummy transaction added for restored customer: " +
                        customerId + ", Amount: " + netAmount);
            } else {
                Log.e("Database", "❌ Failed to add dummy transaction");
            }

        } catch (Exception e) {
            Log.e("Database", "❌ Error adding dummy transaction: " + e.getMessage());
        }
    }

    // ✅ Helper method for current date time
    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM, yy hh:mm a");
        return sdf.format(new java.util.Date());
    }

    // Permanently delete from deleted customers table
    public boolean permanentlyDeleteCustomer(int deletedCustomerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_DELETED_CUSTOMERS,
                COLUMN_DELETED_ID + " = ?",
                new String[]{String.valueOf(deletedCustomerId)});
        db.close();

        boolean success = result > 0;
        Log.d("Database", "🔥 Permanent deletion " + (success ? "successful" : "failed"));
        return success;
    }

    // Helper method to get deleted customer by ID
    private deleted_customer getDeletedCustomerById(int deletedCustomerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DELETED_CUSTOMERS +
                " WHERE " + COLUMN_DELETED_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(deletedCustomerId)});

        deleted_customer deletedCustomer = null;
        if (cursor.moveToFirst()) {
            deletedCustomer = new deleted_customer();
            deletedCustomer.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_DELETED_ID)));
            deletedCustomer.setName(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_NAME)));
            deletedCustomer.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PHONE)));
            deletedCustomer.setProfileImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_PROFILE_IMAGE)));
            deletedCustomer.setNetAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_DELETED_NET_AMOUNT)));
            deletedCustomer.setDeletedDate(cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_AT)));
        }

        cursor.close();
        db.close();
        return deletedCustomer;
    }

    // ==================== TRANSACTION METHODS ====================

    public long addTransaction(int customerId, transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CUSTOMER_ID, customerId);
        values.put(COLUMN_TYPE, transaction.getType());

        // Amount handling
        try {
            String amountStr = transaction.getAmount().replace("Rs", "").replace(" ", "").trim();
            double amount = Double.parseDouble(amountStr);
            values.put(COLUMN_AMOUNT, amount);
            Log.d("Database", "💰 Amount saved: " + amount);
        } catch (NumberFormatException e) {
            Log.e("Database", "❌ Invalid amount format: " + transaction.getAmount());
            values.put(COLUMN_AMOUNT, 0.0);
        }

        values.put(COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_DATE_TIME, transaction.getDateTime());
        values.put(COLUMN_BALANCE, transaction.getBalance());

        // Image path save karein
        if (transaction.getImagePath() != null && !transaction.getImagePath().isEmpty()) {
            values.put(COLUMN_IMAGE_PATH, transaction.getImagePath());
            Log.d("Database", "✅ Image path saved to DB: " + transaction.getImagePath());
        } else {
            values.put(COLUMN_IMAGE_PATH, "");
            Log.d("Database", "📝 Empty image path saved to DB");
        }

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();

        if (result != -1) {
            Log.d("Database", "✅ Transaction saved with ID: " + result);
            Log.d("Database", "📊 Details - Type: " + transaction.getType() +
                    ", Amount: " + transaction.getAmount() +
                    ", Image: " + (transaction.getImagePath() != null && !transaction.getImagePath().isEmpty() ? "Yes" : "No"));
        } else {
            Log.e("Database", "❌ Failed to save transaction");
        }

        return result;
    }

    public List<transaction> getCustomerTransactions(int customerId) {
        List<transaction> transactionList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_CUSTOMER_ID + " = ?" +
                " ORDER BY " + COLUMN_DATE_TIME + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(customerId)});

        if (cursor.moveToFirst()) {
            do {
                transaction transaction = new transaction();

                // Basic transaction data
                transaction.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));

                // Amount
                double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                transaction.setAmount(String.valueOf(amount));

                transaction.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                transaction.setDateTime(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_TIME)));
                transaction.setBalance(cursor.getString(cursor.getColumnIndex(COLUMN_BALANCE)));

                // Image path get karein
                String imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH));
                transaction.setImagePath(imagePath);

                Log.d("Database", "📖 Loaded transaction - Type: " + transaction.getType() +
                        ", Amount: " + transaction.getAmount() +
                        ", Image: " + (imagePath != null && !imagePath.isEmpty() ? imagePath : "No Image"));

                transactionList.add(transaction);
            } while (cursor.moveToNext());
        } else {
            Log.d("Database", "ℹ️ No transactions found for customer ID: " + customerId);
        }

        cursor.close();
        db.close();
        return transactionList;
    }

    public double getCustomerNetAmount(int customerId) {
        String query = "SELECT " +
                "SUM(CASE WHEN " + COLUMN_TYPE + " = 'liye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) - " +
                "SUM(CASE WHEN " + COLUMN_TYPE + " = 'diye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) as net_amount " +
                "FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_CUSTOMER_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(customerId)});

        double netAmount = 0;
        if (cursor.moveToFirst()) {
            netAmount = cursor.getDouble(cursor.getColumnIndex("net_amount"));
            Log.d("Database", "📊 Net amount for customer " + customerId + ": " + netAmount);
        }
        cursor.close();
        db.close();
        return netAmount;
    }

    public boolean deleteTransactionByDetails(int customerId, String type, String amount, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Amount ko numeric format mein convert karein
            double amountValue;
            try {
                String amountStr = amount.replace("Rs", "").replace(" ", "").trim();
                amountValue = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Log.e("Database", "❌ Invalid amount format: " + amount);
                return false;
            }

            // Transaction delete karein
            int result = db.delete(TABLE_TRANSACTIONS,
                    COLUMN_CUSTOMER_ID + " = ? AND " +
                            COLUMN_TYPE + " = ? AND " +
                            COLUMN_AMOUNT + " = ? AND " +
                            COLUMN_DATE_TIME + " = ?",
                    new String[]{
                            String.valueOf(customerId),
                            type,
                            String.valueOf(amountValue),
                            dateTime
                    });

            db.close();

            boolean success = result > 0;
            Log.d("Database", "🗑️ Transaction deletion " + (success ? "successful" : "failed") +
                    " - Customer: " + customerId + ", Type: " + type + ", Amount: " + amount);

            return success;

        } catch (Exception e) {
            Log.e("Database", "❌ Error deleting transaction: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public boolean removeImageFromTransaction(int customerId, String type, String amount, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IMAGE_PATH, ""); // Empty string set karo

            int rowsAffected = db.update(
                    TABLE_TRANSACTIONS,
                    values,
                    COLUMN_CUSTOMER_ID + " = ? AND " +
                            COLUMN_TYPE + " = ? AND " +
                            COLUMN_AMOUNT + " = ? AND " +
                            COLUMN_DATE_TIME + " = ?",
                    new String[]{
                            String.valueOf(customerId),
                            type,
                            amount,
                            dateTime
                    }
            );

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("Database", "Error removing image: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    public boolean updateTransaction(int customerId, transaction oldTransaction, transaction newTransaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            // New values set karo
            values.put(COLUMN_TYPE, newTransaction.getType());

            // Amount handling
            try {
                String amountStr = newTransaction.getAmount().replace("Rs", "").replace(" ", "").trim();
                double amount = Double.parseDouble(amountStr);
                values.put(COLUMN_AMOUNT, amount);
                Log.d("Database", "💰 Updated amount: " + amount);
            } catch (NumberFormatException e) {
                Log.e("Database", "❌ Invalid amount format: " + newTransaction.getAmount());
                values.put(COLUMN_AMOUNT, 0.0);
            }

            values.put(COLUMN_DESCRIPTION, newTransaction.getDescription());
            values.put(COLUMN_DATE_TIME, newTransaction.getDateTime());
            values.put(COLUMN_BALANCE, newTransaction.getBalance());

            // Image path update karo
            if (newTransaction.getImagePath() != null && !newTransaction.getImagePath().isEmpty()) {
                values.put(COLUMN_IMAGE_PATH, newTransaction.getImagePath());
                Log.d("Database", "✅ Updated image path: " + newTransaction.getImagePath());
            } else {
                values.put(COLUMN_IMAGE_PATH, "");
                Log.d("Database", "📝 Empty image path set");
            }

            // Old transaction ke details use karke update karo
            int rowsAffected = db.update(
                    TABLE_TRANSACTIONS,
                    values,
                    COLUMN_CUSTOMER_ID + " = ? AND " +
                            COLUMN_TYPE + " = ? AND " +
                            COLUMN_AMOUNT + " = ? AND " +
                            COLUMN_DATE_TIME + " = ?",
                    new String[]{
                            String.valueOf(customerId),
                            oldTransaction.getType(),
                            oldTransaction.getAmount(),
                            oldTransaction.getDateTime()
                    }
            );

            boolean success = rowsAffected > 0;
            Log.d("Database", "✏️ Transaction update " + (success ? "successful" : "failed") +
                    " - Customer: " + customerId +
                    ", Old Type: " + oldTransaction.getType() +
                    ", New Type: " + newTransaction.getType());

            return success;
        } catch (Exception e) {
            Log.e("Database", "❌ Error updating transaction: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // ==================== DASHBOARD STATISTICS ====================

    public DashboardStats getDashboardStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        DashboardStats stats = new DashboardStats();

        try {
            // Total customers count
            String customersQuery = "SELECT COUNT(*) as total FROM " + TABLE_CUSTOMERS;
            Cursor cursor1 = db.rawQuery(customersQuery, null);
            if (cursor1.moveToFirst()) {
                stats.setTotalCustomers(cursor1.getInt(0));
            }
            cursor1.close();

            // Total transactions count (customer + personal)
            String customerTransactionsQuery = "SELECT COUNT(*) as total FROM " + TABLE_TRANSACTIONS;
            Cursor cursor2 = db.rawQuery(customerTransactionsQuery, null);
            int customerTransactions = 0;
            if (cursor2.moveToFirst()) {
                customerTransactions = cursor2.getInt(0);
            }
            cursor2.close();

            String personalTransactionsQuery = "SELECT COUNT(*) as total FROM " + TABLE_PERSONAL_TRANSACTIONS;
            Cursor cursor3 = db.rawQuery(personalTransactionsQuery, null);
            int personalTransactions = 0;
            if (cursor3.moveToFirst()) {
                personalTransactions = cursor3.getInt(0);
            }
            cursor3.close();

            stats.setTotalTransactions(customerTransactions + personalTransactions);

            // Total amount (customer transactions + personal transactions)
            // Customer transactions ka amount
            String customerAmountQuery = "SELECT " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'liye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) as total_liye, " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'diye' THEN " + COLUMN_AMOUNT + " ELSE 0 END) as total_diye " +
                    "FROM " + TABLE_TRANSACTIONS;
            Cursor cursor4 = db.rawQuery(customerAmountQuery, null);
            double customerLiye = 0, customerDiye = 0;
            if (cursor4.moveToFirst()) {
                customerLiye = cursor4.getDouble(0);
                customerDiye = cursor4.getDouble(1);
            }
            cursor4.close();

            // Personal transactions ka amount
            String personalAmountQuery = "SELECT " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'liye' THEN CAST(" + COLUMN_AMOUNT + " AS REAL) ELSE 0 END) as total_liye, " +
                    "SUM(CASE WHEN " + COLUMN_TYPE + " = 'diye' THEN CAST(" + COLUMN_AMOUNT + " AS REAL) ELSE 0 END) as total_diye " +
                    "FROM " + TABLE_PERSONAL_TRANSACTIONS;
            Cursor cursor5 = db.rawQuery(personalAmountQuery, null);
            double personalLiye = 0, personalDiye = 0;
            if (cursor5.moveToFirst()) {
                personalLiye = cursor5.getDouble(0);
                personalDiye = cursor5.getDouble(1);
            }
            cursor5.close();

            // Combine both
            double totalLiye = customerLiye + personalLiye;
            double totalDiye = customerDiye + personalDiye;

            stats.setTotalLiye(totalLiye);
            stats.setTotalDiye(totalDiye);
            stats.setNetAmount(totalLiye - totalDiye);

        } catch (Exception e) {
            Log.e("Database", "❌ Error getting dashboard stats: " + e.getMessage());
        } finally {
            db.close();
        }

        return stats;
    }

    // Dashboard statistics class
    public static class DashboardStats {
        private int totalCustomers;
        private int totalTransactions;
        private double totalLiye;
        private double totalDiye;
        private double netAmount;

        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

        public int getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }

        public double getTotalLiye() { return totalLiye; }
        public void setTotalLiye(double totalLiye) { this.totalLiye = totalLiye; }

        public double getTotalDiye() { return totalDiye; }
        public void setTotalDiye(double totalDiye) { this.totalDiye = totalDiye; }

        public double getNetAmount() { return netAmount; }
        public void setNetAmount(double netAmount) { this.netAmount = netAmount; }
    }
}
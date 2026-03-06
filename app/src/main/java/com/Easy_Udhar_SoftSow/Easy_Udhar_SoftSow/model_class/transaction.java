package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

import java.io.Serializable;

public class transaction implements Serializable {
    private String id;
    private String type; // "liye" or "diye"
    private String amount;
    private String description;
    private String dateTime;
    private String balance;
    private String imagePath;
    private int transactionId;
    private int customerId; // ✅ NEW FIELD ADDED

    // Constructors
    public transaction() {}

    public transaction(String type, String amount, String description, String dateTime, String balance) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        this.balance = balance;
    }

    // Constructor with image path
    public transaction(String type, String amount, String description, String dateTime, String balance, String imagePath) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        this.balance = balance;
        this.imagePath = imagePath;
    }

    // ✅ UPDATED: Constructor with all fields including customerId
    public transaction(String type, String amount, String description, String dateTime, String balance, String imagePath, int transactionId, int customerId) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
        this.balance = balance;
        this.imagePath = imagePath;
        this.transactionId = transactionId;
        this.customerId = customerId;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getBalance() { return balance; }
    public void setBalance(String balance) { this.balance = balance; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    // ✅ NEW: Customer ID getter setter
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    // ✅ Helper method to check if image exists
    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null");
    }

    // ✅ Helper method to get formatted amount
    public String getFormattedAmount() {
        if (amount == null || amount.isEmpty()) {
            return "Rs 0";
        }
        try {
            // Remove any existing "Rs" and format properly
            String cleanAmount = amount.replace("Rs", "").replace(" ", "").trim();
            return "Rs " + cleanAmount;
        } catch (Exception e) {
            return "Rs " + amount;
        }
    }

    // ✅ Helper method to get numeric amount for calculations
    public double getNumericAmount() {
        if (amount == null || amount.isEmpty()) {
            return 0.0;
        }
        try {
            String cleanAmount = amount.replace("Rs", "").replace(" ", "").trim();
            return Double.parseDouble(cleanAmount);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ✅ Helper method to get transaction type in Hindi
    public String getHindiType() {
        if ("liye".equals(type)) {
            return "Maine Liye";
        } else if ("diye".equals(type)) {
            return "Maine Diye";
        }
        return type;
    }

    // ✅ Helper method to get transaction color
    public String getTransactionColor() {
        if ("liye".equals(type)) {
            return "#4CAF50"; // Green for liye
        } else if ("diye".equals(type)) {
            return "#F44336"; // Red for diye
        }
        return "#2196F3"; // Blue for others
    }

    // ✅ toString method for debugging
    @Override
    public String toString() {
        return "transaction{" +
                "type='" + type + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", balance='" + balance + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", transactionId=" + transactionId +
                ", customerId=" + customerId +
                ", hasImage=" + hasImage() +
                '}';
    }

    // ✅ equals method for comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        transaction that = (transaction) obj;
        return transactionId == that.transactionId &&
                customerId == that.customerId &&
                java.util.Objects.equals(type, that.type) &&
                java.util.Objects.equals(amount, that.amount) &&
                java.util.Objects.equals(description, that.description) &&
                java.util.Objects.equals(dateTime, that.dateTime) &&
                java.util.Objects.equals(balance, that.balance) &&
                java.util.Objects.equals(imagePath, that.imagePath);
    }

    // ✅ hashCode method
    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, amount, description, dateTime, balance, imagePath, transactionId, customerId);
    }

    // ✅ Copy constructor
    public transaction(transaction other) {
        this.type = other.type;
        this.amount = other.amount;
        this.description = other.description;
        this.dateTime = other.dateTime;
        this.balance = other.balance;
        this.imagePath = other.imagePath;
        this.transactionId = other.transactionId;
        this.customerId = other.customerId;
        this.id = other.id;
    }
}
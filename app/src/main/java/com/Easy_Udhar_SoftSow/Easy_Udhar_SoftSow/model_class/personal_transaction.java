package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

import java.io.Serializable;

public class personal_transaction implements Serializable {
    private int id;
    private String type; // "liye" or "diye"
    private String amount;
    private String description;
    private String dateTime;
    private String imagePath;

    // Constructors
    public personal_transaction() {}

    public personal_transaction(String type, String amount, String description, String dateTime) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.dateTime = dateTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // Helper methods
    public boolean hasImage() {
        return imagePath != null && !imagePath.isEmpty();
    }

    @Override
    public String toString() {
        return "PersonalTransaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", imagePath='" + (imagePath != null ? imagePath : "empty") + '\'' +
                '}';
    }
}
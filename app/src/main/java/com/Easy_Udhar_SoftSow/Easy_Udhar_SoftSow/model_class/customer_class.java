package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

public class customer_class {
    private int id;
    private String name;
    private String phone;
    private double netAmount;
    private String profileImagePath; // ✅ NEW FIELD

    public customer_class() {}

    // 🔹 Constructor without profile image (backward compatibility)
    public customer_class(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.profileImagePath = "";
    }

    // 🔹 Constructor with all fields
    public customer_class(String name, String phone, String profileImagePath) {
        this.name = name;
        this.phone = phone;
        this.profileImagePath = profileImagePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(double netAmount) {
        this.netAmount = netAmount;
    }

    // ✅ NEW: Profile Image Path getter setter
    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    // ✅ Helper method to check if profile image exists
    public boolean hasProfileImage() {
        return profileImagePath != null &&
                !profileImagePath.isEmpty() &&
                !profileImagePath.equals("null");
    }

    // ✅ Helper method to get customer initial for avatar
    public String getInitial() {
        if (name != null && !name.isEmpty()) {
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
        return "?";
    }

    // ✅ Helper method to get formatted net amount
    public String getFormattedNetAmount() {
        if (netAmount > 0) {
            return "Rs. " + String.format("%.2f", Math.abs(netAmount));
        } else if (netAmount < 0) {
            return "Rs. " + String.format("%.2f", Math.abs(netAmount));
        } else {
            return "Rs. 0";
        }
    }

    // ✅ Helper method to get amount color based on net amount
    public String getAmountColorType() {
        if (netAmount > 0) {
            return "green"; // Maine Liye zyada
        } else if (netAmount < 0) {
            return "red";   // Maine Diye zyada
        } else {
            return "green"; // Hisaab clear
        }
    }

    // ✅ Helper method to get status text
    public String getStatusText() {
        if (netAmount > 0) {
            return "Maine dene hain";
        } else if (netAmount < 0) {
            return "Maine lene hain";
        } else {
            return "Hisaab clear hai";
        }
    }

    // ✅ toString method for debugging
    @Override
    public String toString() {
        return "customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", netAmount=" + netAmount +
                ", profileImagePath='" + profileImagePath + '\'' +
                ", hasProfileImage=" + hasProfileImage() +
                '}';
    }

    // ✅ equals method for comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        customer_class customer = (customer_class) obj;
        return id == customer.id &&
                Double.compare(customer.netAmount, netAmount) == 0 &&
                java.util.Objects.equals(name, customer.name) &&
                java.util.Objects.equals(phone, customer.phone) &&
                java.util.Objects.equals(profileImagePath, customer.profileImagePath);
    }

    // ✅ hashCode method
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, phone, netAmount, profileImagePath);
    }

    // ✅ Copy constructor
    public customer_class(customer_class other) {
        this.id = other.id;
        this.name = other.name;
        this.phone = other.phone;
        this.netAmount = other.netAmount;
        this.profileImagePath = other.profileImagePath;
    }
}
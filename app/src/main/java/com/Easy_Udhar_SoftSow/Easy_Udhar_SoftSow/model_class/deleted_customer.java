package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

import java.io.Serializable;

public class deleted_customer implements Serializable {
    private int id;
    private String name;
    private String phone;
    private String deletedDate;
    private double netAmount;
    private String profileImagePath;
    private int originalId;

    public deleted_customer() {}

    // ✅ COMPLETE GETTERS AND SETTERS ADD KARO
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDeletedDate() { return deletedDate; }
    public void setDeletedDate(String deletedDate) { this.deletedDate = deletedDate; }

    public double getNetAmount() { return netAmount; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }

    // ✅ YE DONO GETTER/SETTER ADD KARO (missing thay)
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public int getOriginalId() { return originalId; }
    public void setOriginalId(int originalId) { this.originalId = originalId; }

    // Helper methods
    public String getInitial() {
        if (name != null && !name.isEmpty()) {
            return String.valueOf(name.charAt(0)).toUpperCase();
        }
        return "?";
    }

    public boolean hasProfileImage() {
        return profileImagePath != null &&
                !profileImagePath.isEmpty() &&
                !profileImagePath.equals("null");
    }

    @Override
    public String toString() {
        return "DeletedCustomer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", deletedDate='" + deletedDate + '\'' +
                ", netAmount=" + netAmount +
                ", profileImagePath='" + profileImagePath + '\'' +
                ", originalId=" + originalId +
                '}';
    }
}
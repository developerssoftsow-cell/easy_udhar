package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class;

public class add_contact_class {
    private String name;
    private String number;
    private boolean isAdded;

    public add_contact_class(String name, String number, boolean isAdded) {
        this.name = name;
        this.number = number;
        this.isAdded = isAdded;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }
}
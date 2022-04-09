package com.example.mytestapp;

public class Contact {
    private String name, phone;

    public Contact() {

    }

    public Contact(String mName, String mPhone) {
        this.name = mName;
        this.phone = mPhone;
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
}

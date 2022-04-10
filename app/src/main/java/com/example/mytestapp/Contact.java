package com.example.mytestapp;

public class Contact {
    private String name, phone, id;

    public Contact() {

    }

    public Contact(String mName, String mPhone, String mId) {
        this.name = mName;
        this.phone = mPhone;
        this.id = mId;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

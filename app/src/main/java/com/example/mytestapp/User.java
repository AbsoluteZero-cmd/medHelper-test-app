package com.example.mytestapp;

public class User {

    String fullName, password, email, date, phone, imageUri;

    public User(){

    }

    public User(String mFullName, String mEmail, String mPassword, String mPhone, String mDate, String mImageUri){
        this.fullName = mFullName;
        this.email = mEmail;
        this.password = mPassword;
        this.phone = mPhone;
        this.date = mDate;
        this.imageUri = mImageUri;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}

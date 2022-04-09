package com.example.mytestapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Hospital implements Serializable {
    String lat, lng, name, phone, webAddress, detailText, imageUri;

    public Hospital(){

    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public Hospital(String mLat, String mLng, String mName, String mPhone, String mWebAddress, String mDetailText, String mImageUri){
        this.lat = mLat;
        this.lng = mLng;
        this.name = mName;
        this.phone = mPhone;
        this.webAddress = mWebAddress;
        this.detailText = mDetailText;
        this.imageUri = mImageUri;
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

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public String getDetailText() {
        return detailText;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}

package com.example.mytestapp;

public class Document {

    String title, info, date, documentUri, documentExtension, fileName;

    public Document() {

    }

    public Document(String mTitle, String mInfo, String mDate, String mDocumentUri, String mDocumentExtension, String mFileName) {
        this.title = mTitle;
        this.info = mInfo;
        this.date = mDate;
        this.documentUri = mDocumentUri;
        this.documentExtension = mDocumentExtension;
        this.fileName = mFileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getDocumentExtension() {
        return documentExtension;
    }

    public void setDocumentExtension(String documentExtension) {
        this.documentExtension = documentExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

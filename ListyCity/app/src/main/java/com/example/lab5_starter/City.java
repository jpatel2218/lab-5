package com.example.lab5_starter;

import java.io.Serializable;

// Movie object
public class City implements Serializable {
    // attributes
    private String name;
    private String province;
    private String documentId; // Added to track Firestore document ID

    // constructor
    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}

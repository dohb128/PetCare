package com.inhatc.petcare.model;

// User.java
public class User {
    public String nickname;
    public String email;
    public String profilePhotoURL; // Cloud Storage에 저장된 URL

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String nickname, String email, String profilePhotoURL) {
        this.nickname = nickname;
        this.email = email;
        this.profilePhotoURL = profilePhotoURL;
    }
}

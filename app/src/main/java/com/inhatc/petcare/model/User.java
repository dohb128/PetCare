package com.inhatc.petcare.model;

public class User {
    public String email;
    public String nickname;
    public String profilePhotoURL;

    public User() {
        // Default constructor required for Firebase Realtime Database
    }

    public User(String email) {
        this.email = email;
    }

    // 새로운 생성자 (email과 nickname을 모두 받는 경우)
    public User(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    // Getters and setters for all fields
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfilePhotoURL() {
        return profilePhotoURL;
    }

    public void setProfilePhotoURL(String profilePhotoURL) {
        this.profilePhotoURL = profilePhotoURL;
    }
}
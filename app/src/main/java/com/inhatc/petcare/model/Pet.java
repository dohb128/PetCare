package com.inhatc.petcare.model;

import java.io.Serializable;

public class Pet implements Serializable {

    // Realtime Database 키를 저장할 필드 추가
    public String petId;

    public String ownerId;
    public String name;
    public String breed; // 견종 필드 추가
    public String photoURL;
    public int age;
    public double weight;
    public String birthday; // 생년월일은 나이 계산을 위해 유지

    public Pet() {
        // Default constructor required for Firebase
    }

    public Pet(String ownerId, String name, String breed, String photoURL, int age, double weight, String birthday) {
        this.ownerId = ownerId;
        this.name = name;
        this.breed = breed;
        this.photoURL = photoURL;
        this.age = age;
        this.weight = weight;
        this.birthday = birthday;
    }

    // Getters and Setters
    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
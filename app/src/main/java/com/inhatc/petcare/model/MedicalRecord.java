package com.inhatc.petcare.model;

// MedicalRecord.java
public class MedicalRecord {
    public String ownerId; // 주인의 UID
    public String petId;   // 반려동물 ID
    public String date;    // "YYYY-MM-DDTHH:mm:ssZ" 또는 타임스탬프 (long)
    public String memo;

    public MedicalRecord() {
        // Default constructor required
    }

    public MedicalRecord(String ownerId, String petId, String date, String memo) {
        this.ownerId = ownerId;
        this.petId = petId;
        this.date = date;
        this.memo = memo;
    }
}
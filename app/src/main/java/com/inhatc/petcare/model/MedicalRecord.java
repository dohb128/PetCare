package com.inhatc.petcare.model;

public class MedicalRecord {
    private String recordId;
    private String ownerId; // 소유자 UID 추가
    private String petId;
    private String date;
    private String memo;
    private String type; // "진료", "접종", "약 복용"
    private long timestamp;

    public MedicalRecord() {
        // Firebase에서 필요
    }

    public MedicalRecord(String ownerId, String petId, String date, String memo, String type) {
        this.ownerId = ownerId;
        this.petId = petId;
        this.date = date;
        this.memo = memo;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
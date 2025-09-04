package com.inhatc.petcare;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inhatc.petcare.model.Pet;
import com.inhatc.petcare.util.EnvUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer; // Java 8 Consumer interface

public class PetCareApplication extends Application {

    private static final String TAG = "PetCareApplication";
    private static FirebaseDatabase database;
    private static FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();
        EnvUtil.loadEnvFile(this);
        // Firebase Realtime Database는 기본적으로 오프라인 기능을 지원하므로 별도 코드 불필요
    }

    public static FirebaseDatabase getRealtimeDatabaseInstance() {
        if (database == null) {
            database = FirebaseDatabase.getInstance("https://petcare-3be1c-default-rtdb.firebaseio.com/"); // Realtime Database URL 지정
        }
        return database;
    }

    public static FirebaseAuth getAuthInstance() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static void getPetsForCurrentUser(Consumer<List<Pet>> callback) {
        FirebaseUser currentUser = getAuthInstance().getCurrentUser();
        if (currentUser != null) {
            String ownerId = currentUser.getUid();
            DatabaseReference petsRef = getRealtimeDatabaseInstance().getReference("pets");

            petsRef.orderByChild("ownerId").equalTo(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Pet> pets = new ArrayList<>();
                    for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                        Pet pet = petSnapshot.getValue(Pet.class);
                        if (pet != null) {
                            pet.setPetId(petSnapshot.getKey()); // Realtime Database의 키를 petId로 설정
                            pets.add(pet);
                        }
                    }
                    callback.accept(pets);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error getting pets from Realtime Database: " + databaseError.getMessage());
                    callback.accept(new ArrayList<>()); // 오류 발생 시 빈 리스트 반환
                }
            });
        } else {
            Log.w(TAG, "No current user logged in.");
            callback.accept(new ArrayList<>()); // 로그인된 사용자 없으면 빈 리스트 반환
        }
    }
}
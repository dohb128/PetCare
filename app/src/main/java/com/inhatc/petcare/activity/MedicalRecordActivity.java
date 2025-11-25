package com.inhatc.petcare.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.annotation.NonNull;
import android.view.MenuItem;

import com.inhatc.petcare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inhatc.petcare.model.Pet;
import com.inhatc.petcare.model.MedicalRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicalRecordActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout recordContainer;
    private Button addButton;
    private TextView petNameTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // Firebase 관련 변수
    private FirebaseAuth mAuth;
    private DatabaseReference petsRef;
    private DatabaseReference medicalRecordsRef;
    private List<Pet> petList;
    private Pet currentPet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MedicalRecordActivity.this, LoginActivity.class));
            finish();
            return;
        }

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        medicalRecordsRef = FirebaseDatabase.getInstance().getReference("medicalRecords");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title_textview);
        if (toolbarTitle != null) {
            toolbarTitle.setOnClickListener(v -> {
                Intent intent = new Intent(MedicalRecordActivity.this, MainActivity.class);
                // 다른 액티비티 스택을 모두 지우고 새롭게 시작
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // 네비게이션 헤더 타이틀 클릭 시 메인으로 이동
        View headerView = navigationView.getHeaderView(0);
        TextView navTitleView = headerView.findViewById(R.id.nav_Title);
        navTitleView.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalRecordActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        recordContainer = findViewById(R.id.recordContainer);
        addButton = findViewById(R.id.addButton);
        petNameTitle = findViewById(R.id.petNameTitle);

        // 반려동물 목록 로드 및 현재 반려동물 설정
        loadPetsFromDatabase();

        // 반려동물 이름 클릭 시 변경 다이얼로그
        petNameTitle.setOnClickListener(v -> showChangePetDialog());

        addButton.setOnClickListener(v -> showAddDialog());
    }

    private void loadPetsFromDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            return;

        petsRef.orderByChild("ownerId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        petList = new ArrayList<>();
                        for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                            Pet pet = petSnapshot.getValue(Pet.class);
                            if (pet != null) {
                                pet.setPetId(petSnapshot.getKey());
                                petList.add(pet);
                            }
                        }

                        if (!petList.isEmpty()) {
                            // 인텐트로 전달된 반려동물 이름이 있으면 해당 반려동물을 찾아 설정
                            String intentPetName = getIntent() != null ? getIntent().getStringExtra("petName") : null;
                            if (intentPetName != null && !intentPetName.trim().isEmpty()) {
                                for (Pet pet : petList) {
                                    if (pet.getName().equals(intentPetName.trim())) {
                                        currentPet = pet;
                                        break;
                                    }
                                }
                            }

                            // 찾지 못했거나 인텐트가 없으면 첫 번째 반려동물 사용
                            if (currentPet == null) {
                                currentPet = petList.get(0);
                            }

                            petNameTitle.setText(currentPet.getName());
                            loadMedicalRecords();
                        } else {
                            petNameTitle.setText("반려동물 없음");
                            Toast.makeText(MedicalRecordActivity.this, "등록된 반려동물이 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MedicalRecordActivity.this, "반려동물 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showChangePetDialog() {
        if (petList == null || petList.size() <= 1) {
            Toast.makeText(this, "변경할 반려동물이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("반려동물 선택");

        List<String> petNames = new ArrayList<>();
        for (Pet pet : petList) {
            petNames.add(pet.getName());
        }

        String[] petNamesArray = petNames.toArray(new String[0]);

        builder.setItems(petNamesArray, (dialog, which) -> {
            currentPet = petList.get(which);
            petNameTitle.setText(currentPet.getName());
            loadMedicalRecords();
            Toast.makeText(MedicalRecordActivity.this, currentPet.getName() + "의 진료 기록을 표시합니다.", Toast.LENGTH_SHORT)
                    .show();
        });

        builder.show();
    }

    private void loadMedicalRecords() {
        if (currentPet == null)
            return;

        recordContainer.removeAllViews();

        medicalRecordsRef.orderByChild("petId").equalTo(currentPet.getPetId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                            MedicalRecord record = recordSnapshot.getValue(MedicalRecord.class);
                            if (record != null) {
                                addRecordItemToView(record);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MedicalRecordActivity", "Failed to load medical records: " + error.getMessage());
                        Toast.makeText(MedicalRecordActivity.this, "진료 기록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_record, null, false);
        TextView dialogDate = dialogView.findViewById(R.id.dialogDate);
        EditText dialogMemo = dialogView.findViewById(R.id.dialogMemo);
        ImageView dialogIcon = dialogView.findViewById(R.id.dialogIcon);
        android.widget.RadioGroup typeSelector = dialogView.findViewById(R.id.typeSelector);
        android.widget.RadioButton rbTreatment = dialogView.findViewById(R.id.rbTreatment);
        android.widget.RadioButton rbVaccination = dialogView.findViewById(R.id.rbVaccination);
        android.widget.RadioButton rbMedication = dialogView.findViewById(R.id.rbMedication);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // 오늘 날짜 기본 설정
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH) + 1, d = cal.get(Calendar.DAY_OF_MONTH);
        dialogDate.setText(String.format("%04d-%02d-%02d", y, m, d));

        // 날짜 클릭 시 DatePicker
        dialogDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        dialogDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 유형 선택: 라디오 버튼으로 선택 → 아이콘 미리보기 변경
        final int[] selectedIconRes = { R.drawable.hospital };
        rbTreatment.setChecked(true);
        typeSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTreatment) {
                selectedIconRes[0] = R.drawable.hospital;
            } else if (checkedId == R.id.rbVaccination) {
                selectedIconRes[0] = R.drawable.injection;
            } else if (checkedId == R.id.rbMedication) {
                selectedIconRes[0] = R.drawable.medication;
            }
            dialogIcon.setImageResource(selectedIconRes[0]);
        });

        saveButton.setOnClickListener(v -> {
            String date = dialogDate.getText().toString();
            String memo = dialogMemo.getText().toString();

            if (memo.trim().isEmpty()) {
                Toast.makeText(MedicalRecordActivity.this, "메모를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 유형 결정
            String type;
            if (rbTreatment.isChecked()) {
                type = "진료";
            } else if (rbVaccination.isChecked()) {
                type = "접종";
            } else if (rbMedication.isChecked()) {
                type = "약 복용";
            } else {
                type = "진료"; // 기본값
            }

            saveMedicalRecord(date, memo, type);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveMedicalRecord(String date, String memo, String type) {
        if (currentPet == null) {
            Toast.makeText(this, "반려동물을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        MedicalRecord record = new MedicalRecord(mAuth.getCurrentUser().getUid(), currentPet.getPetId(), date, memo,
                type);
        String recordId = medicalRecordsRef.push().getKey();
        if (recordId != null) {
            record.setRecordId(recordId);
            medicalRecordsRef.child(recordId).setValue(record)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MedicalRecordActivity.this, "진료 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        loadMedicalRecords(); // 화면 새로고침
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MedicalRecordActivity.this, "저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
        }
    }

    private void addRecordItemToView(MedicalRecord record) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_medical_record, recordContainer, false);
        TextView recordDate = item.findViewById(R.id.recordDate);
        TextView recordMemo = item.findViewById(R.id.recordMemo);
        ImageView recordIcon = item.findViewById(R.id.recordIcon);

        recordDate.setText(record.getDate());
        recordMemo.setText(record.getMemo());

        // 유형에 따른 아이콘 설정
        int iconRes;
        String type = record.getType();
        if (type == null)
            type = "진료";

        switch (type) {
            case "진료":
                iconRes = R.drawable.hospital;
                break;
            case "접종":
                iconRes = R.drawable.injection;
                break;
            case "약 복용":
                iconRes = R.drawable.medication;
                break;
            default:
                iconRes = R.drawable.hospital;
                break;
        }
        recordIcon.setImageResource(iconRes);

        // 아이템 클릭 시 수정/삭제 다이얼로그 표시
        item.setOnClickListener(v -> showEditDeleteDialog(record));

        recordContainer.addView(item, 0);
    }

    private void showEditDeleteDialog(MedicalRecord record) {
        CharSequence[] options = { "수정", "삭제" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("작업 선택");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditDialog(record);
            } else if (which == 1) {
                showDeleteConfirmDialog(record);
            }
        });
        builder.show();
    }

    private void showDeleteConfirmDialog(MedicalRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("정말로 이 기록을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteMedicalRecord(record))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteMedicalRecord(MedicalRecord record) {
        if (record.getRecordId() == null)
            return;

        medicalRecordsRef.child(record.getRecordId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MedicalRecordActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    loadMedicalRecords();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MedicalRecordActivity.this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditDialog(MedicalRecord record) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_record, null, false);
        TextView dialogDate = dialogView.findViewById(R.id.dialogDate);
        EditText dialogMemo = dialogView.findViewById(R.id.dialogMemo);
        ImageView dialogIcon = dialogView.findViewById(R.id.dialogIcon);
        android.widget.RadioGroup typeSelector = dialogView.findViewById(R.id.typeSelector);
        android.widget.RadioButton rbTreatment = dialogView.findViewById(R.id.rbTreatment);
        android.widget.RadioButton rbVaccination = dialogView.findViewById(R.id.rbVaccination);
        android.widget.RadioButton rbMedication = dialogView.findViewById(R.id.rbMedication);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // 기존 데이터 채우기
        dialogDate.setText(record.getDate());
        dialogMemo.setText(record.getMemo());

        String type = record.getType();
        if ("접종".equals(type)) {
            rbVaccination.setChecked(true);
            dialogIcon.setImageResource(R.drawable.injection);
        } else if ("약 복용".equals(type)) {
            rbMedication.setChecked(true);
            dialogIcon.setImageResource(R.drawable.medication);
        } else {
            rbTreatment.setChecked(true);
            dialogIcon.setImageResource(R.drawable.hospital);
        }

        // 날짜 클릭 시 DatePicker
        dialogDate.setOnClickListener(v -> {
            String[] dateParts = dialogDate.getText().toString().split("-");
            int y = Integer.parseInt(dateParts[0]);
            int m = Integer.parseInt(dateParts[1]) - 1;
            int d = Integer.parseInt(dateParts[2]);

            DatePickerDialog dp = new DatePickerDialog(this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        dialogDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                    }, y, m, d);
            dp.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // 유형 선택 리스너
        final int[] selectedIconRes = { R.drawable.hospital };
        // 초기값 설정
        if (rbVaccination.isChecked())
            selectedIconRes[0] = R.drawable.injection;
        else if (rbMedication.isChecked())
            selectedIconRes[0] = R.drawable.medication;

        typeSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTreatment) {
                selectedIconRes[0] = R.drawable.hospital;
            } else if (checkedId == R.id.rbVaccination) {
                selectedIconRes[0] = R.drawable.injection;
            } else if (checkedId == R.id.rbMedication) {
                selectedIconRes[0] = R.drawable.medication;
            }
            dialogIcon.setImageResource(selectedIconRes[0]);
        });

        saveButton.setOnClickListener(v -> {
            String date = dialogDate.getText().toString();
            String memo = dialogMemo.getText().toString();

            if (memo.trim().isEmpty()) {
                Toast.makeText(MedicalRecordActivity.this, "메모를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 유형 결정
            String newType;
            if (rbTreatment.isChecked()) {
                newType = "진료";
            } else if (rbVaccination.isChecked()) {
                newType = "접종";
            } else if (rbMedication.isChecked()) {
                newType = "약 복용";
            } else {
                newType = "진료";
            }

            updateMedicalRecord(record, date, memo, newType);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateMedicalRecord(MedicalRecord record, String date, String memo, String type) {
        record.setDate(date);
        record.setMemo(memo);
        record.setType(type);

        medicalRecordsRef.child(record.getRecordId()).setValue(record)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MedicalRecordActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                    loadMedicalRecords();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MedicalRecordActivity.this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_my_page) {
            startActivity(new android.content.Intent(this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            // 현재 화면이므로 닫기만
        } else if (id == R.id.nav_chatbot) {
            startActivity(new android.content.Intent(this, ChatbotActivity.class));
        } else if (id == R.id.nav_nearby_hospitals) {
            startActivity(new android.content.Intent(this, NearbyHospitalsActivity.class));
        } else if (id == R.id.nav_logout) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
        } else {
            android.widget.Toast.makeText(this, item.getTitle() + " 클릭", android.widget.Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(navigationView);
        return true;
    }
}

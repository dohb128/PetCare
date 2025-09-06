package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import androidx.exifinterface.media.ExifInterface;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.os.Build;
import android.app.Activity;
import android.widget.ImageView;

import com.inhatc.petcare.model.Pet;
import com.inhatc.petcare.adapter.PetAdapter;
import com.inhatc.petcare.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddPetDialogFragment.OnPetAddedListener {

    private static final String TAG = "MyProfileActivity";
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 201;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView navTitleView;
    private TextView emailProfileTextView;
    private EditText nicknameEditText;
    private Button addPetButton;
    private Button editNicknameButton;
    private CircleImageView profileImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference petsRef;
    private DatabaseReference usersRef;

    private RecyclerView registeredPetsRecyclerView;
    private PetAdapter petAdapter;
    private List<Pet> petList;

    private final ActivityResultLauncher<Intent> profileGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    updateProfilePhoto(selectedImageUri);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        petsRef = FirebaseDatabase.getInstance().getReference("pets");
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title_textview);
        if (toolbarTitle != null) {
            toolbarTitle.setOnClickListener(v -> {
                Intent intent = new Intent(MyProfileActivity.this, MainActivity.class);
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

        View headerView = navigationView.getHeaderView(0);
        navTitleView = headerView.findViewById(R.id.nav_Title);
        emailProfileTextView = findViewById(R.id.txt_email);
        nicknameEditText = findViewById(R.id.txt_nickname);
        editNicknameButton = findViewById(R.id.btn_editNickname);
        profileImageView = findViewById(R.id.profileImageView);

        emailProfileTextView.setText(currentUser.getEmail());

        // 1. RecyclerView와 어댑터 초기화
        registeredPetsRecyclerView = findViewById(R.id.registeredPetsRecyclerView);
        registeredPetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        registeredPetsRecyclerView.setAdapter(petAdapter);

        // 2. 어댑터가 초기화된 후에 리스너를 설정
        petAdapter.setOnItemClickListener(new PetAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Pet petToEdit = petList.get(position);
                AddPetDialogFragment editPetDialogFragment = AddPetDialogFragment.newInstance(petToEdit.getPetId());
                editPetDialogFragment.setOnPetAddedListener(MyProfileActivity.this);
                editPetDialogFragment.show(getSupportFragmentManager(), "AddPetDialogFragment");
            }

            @Override
            public void onDeleteClick(int position) {
                deletePetFromRealtimeDatabase(position);
            }
        });

        // 3. 다른 UI 요소의 리스너 설정
        addPetButton = findViewById(R.id.btn_addPet);
        addPetButton.setOnClickListener(v -> {
            AddPetDialogFragment addPetDialogFragment = new AddPetDialogFragment();
            addPetDialogFragment.setOnPetAddedListener(this);
            addPetDialogFragment.show(getSupportFragmentManager(), "AddPetDialogFragment");
        });

        editNicknameButton.setOnClickListener(v -> {
            String newNickname = nicknameEditText.getText().toString().trim();
            if (!newNickname.isEmpty()) {
                updateNickname(newNickname);
            } else {
                Toast.makeText(MyProfileActivity.this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. 프로필 이미지 클릭 리스너 추가
        profileImageView.setOnClickListener(v -> checkGalleryPermissionAndOpenGallery());

        // 5. 데이터 로드
        loadUserProfile();
        loadPetsFromRealtimeDatabase();
    }

    private void checkGalleryPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        profileGalleryLauncher.launch(pickGalleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfilePhoto(Uri uri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Bitmap rotatedBitmap = rotateBitmap(originalBitmap, uri);
            String base64Image = bitmapToBase64(rotatedBitmap);

            if (base64Image != null) {
                usersRef.child("profilePhotoURL").setValue(base64Image)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MyProfileActivity.this, "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                            profileImageView.setImageBitmap(rotatedBitmap);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MyProfileActivity.this, "프로필 사진 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "프로필 사진 업데이트 실패", e);
                        });
            }
        } catch (IOException e) {
            Log.e(TAG, "프로필 사진 처리 실패", e);
            Toast.makeText(MyProfileActivity.this, "프로필 사진 처리 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    if (user.getNickname() != null) {
                        nicknameEditText.setText(user.getNickname());
                    } else {
                        nicknameEditText.setText("닉네임 미설정");
                    }
                    if (user.getProfilePhotoURL() != null && !user.getProfilePhotoURL().isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(user.getProfilePhotoURL(), Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profileImageView.setImageBitmap(decodedBitmap);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Base64 프로필 이미지를 디코딩하는 데 실패했습니다.", e);
                            profileImageView.setImageResource(R.drawable.user);
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "사용자 정보를 불러오는 데 실패했습니다: " + error.getMessage());
                Toast.makeText(MyProfileActivity.this, "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNickname(String newNickname) {
        if (mAuth.getCurrentUser() == null) return;

        usersRef.child("nickname").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyProfileActivity.this, "닉네임이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyProfileActivity.this, "닉네임 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "닉네임 업데이트 실패", e);
                });
    }

    private Bitmap rotateBitmap(Bitmap bitmap, Uri uri) throws IOException {
        ExifInterface exifInterface = new ExifInterface(this.getContentResolver().openInputStream(uri));
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Bitmap to Base64 conversion failed", e);
            return null;
        }
    }

    private void loadPetsFromRealtimeDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        petsRef.orderByChild("ownerId").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                petList.clear();
                for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                    Pet pet = petSnapshot.getValue(Pet.class);
                    if (pet != null) {
                        pet.setPetId(petSnapshot.getKey());
                        petList.add(pet);
                    }
                }
                petAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "반려동물 정보를 불러오는 데 실패했습니다: " + error.getMessage(), error.toException());
                Toast.makeText(MyProfileActivity.this, "반려동물 정보를 불러오는 데 실패했습니다: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePetToRealtimeDatabase(Pet pet) {
        String petId = petsRef.push().getKey();
        if (petId != null) {
            pet.setPetId(petId);
            petsRef.child(petId).setValue(pet)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MyProfileActivity.this, pet.getName() + "이(가) 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        petAdapter.addPet(pet);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyProfileActivity.this, "반려동물 등록에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updatePetInRealtimeDatabase(Pet petToUpdate) {
        if (petToUpdate.getPetId() == null || petToUpdate.getPetId().isEmpty()) {
            Toast.makeText(this, "업데이트할 반려동물 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        petsRef.child(petToUpdate.getPetId()).setValue(petToUpdate)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyProfileActivity.this, petToUpdate.getName() + "의 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                    petAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyProfileActivity.this, "정보 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deletePetFromRealtimeDatabase(int position) {
        if (position >= petList.size()) return;

        Pet petToDelete = petList.get(position);
        if (petToDelete.getPetId() == null || petToDelete.getPetId().isEmpty()) {
            Toast.makeText(this, "삭제할 반려동물 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        petsRef.child(petToDelete.getPetId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    petAdapter.removePet(position);
                    Toast.makeText(MyProfileActivity.this, "반려동물 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MyProfileActivity.this, "반려동물 삭제에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            // 현재 화면: 아무 동작 없음
        } else if (id == R.id.nav_medical_records) {
            startActivity(new Intent(MyProfileActivity.this, MedicalRecordActivity.class));
        } else if (id == R.id.nav_chatbot) {
            startActivity(new Intent(MyProfileActivity.this, ChatbotActivity.class));
        } else if (id == R.id.nav_nearby_hospitals) {
            startActivity(new Intent(MyProfileActivity.this, NearbyHospitalsActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onPetAdded(Pet pet) {
        if (pet.getPetId() == null || pet.getPetId().isEmpty()) {
            if (pet.getPhotoURL() != null && pet.getPhotoURL().startsWith("content://")) {
                try {
                    Uri photoUri = Uri.parse(pet.getPhotoURL());
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    Bitmap rotatedBitmap = rotateBitmap(originalBitmap, photoUri);
                    String base64Image = bitmapToBase64(rotatedBitmap);
                    pet.setPhotoURL(base64Image);
                } catch (IOException e) {
                    Log.e(TAG, "Error processing pet photo for new pet", e);
                    Toast.makeText(this, "반려동물 사진 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            savePetToRealtimeDatabase(pet);
        } else {
            if (pet.getPhotoURL() != null && pet.getPhotoURL().startsWith("content://")) {
                try {
                    Uri photoUri = Uri.parse(pet.getPhotoURL());
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    Bitmap rotatedBitmap = rotateBitmap(originalBitmap, photoUri);
                    String base64Image = bitmapToBase64(rotatedBitmap);
                    pet.setPhotoURL(base64Image);
                } catch (IOException e) {
                    Log.e(TAG, "Error processing pet photo for existing pet", e);
                    Toast.makeText(this, "반려동물 사진 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            updatePetInRealtimeDatabase(pet);
        }
    }
}
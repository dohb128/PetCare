package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.inhatc.petcare.model.Pet;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView navTitleView;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabPetEyeHealth;
    private DatabaseReference petsRef;

    // 홈 화면 반려동물 정보 UI
    private CircleImageView mainPetImageView;
    private TextView mainPetNameTextView;
    private TextView mainPetDetailsTextView;
    private Button changePetButton;

    private List<Pet> petList; // 등록된 반려동물 목록
    private Pet currentPet; // 현재 홈 화면에 표시되는 반려동물

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Realtime Database 참조 초기화
        petsRef = FirebaseDatabase.getInstance().getReference("pets");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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
        navTitleView.setText("PetCare");

        fabPetEyeHealth = findViewById(R.id.FabPetEyeHealth);
        fabPetEyeHealth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EyeHealthCheckActivity.class);
            startActivity(intent);
        });

        // 홈 화면 반려동물 정보 UI 초기화
        mainPetImageView = findViewById(R.id.mainPetImageView);
        mainPetNameTextView = findViewById(R.id.mainPetNameTextView);
        mainPetDetailsTextView = findViewById(R.id.mainPetDetailsTextView);
        changePetButton = findViewById(R.id.changePetButton);

        // '변경' 버튼 클릭 리스너 설정
        changePetButton.setOnClickListener(v -> {
            if (petList != null && petList.size() > 1) {
                showChangePetDialog();
            } else {
                Toast.makeText(MainActivity.this, "등록된 반려동물이 한 마리 뿐입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        loadPetsFromRealtimeDatabase();
    }

    private void loadPetsFromRealtimeDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        petsRef.orderByChild("ownerId").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    currentPet = petList.get(0); // 가장 먼저 등록된 반려동물
                    displayCurrentPet();
                } else {
                    mainPetNameTextView.setText("반려동물을 등록해주세요");
                    mainPetDetailsTextView.setText("");
                    mainPetImageView.setImageResource(R.drawable.pet);
                    changePetButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "반려동물 정보를 불러오는 데 실패했습니다: " + error.getMessage(), error.toException());
                Toast.makeText(MainActivity.this, "반려동물 정보를 불러오는 데 실패했습니다: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCurrentPet() {
        if (currentPet == null) return;

        mainPetNameTextView.setText(currentPet.getName());
        mainPetDetailsTextView.setText(String.format(Locale.getDefault(), "품종: %s, 나이: %d세, 체중: %.1fkg", currentPet.getBreed(), currentPet.getAge(), currentPet.getWeight()));

        // 이미지 로딩 로직 (Base64)
        if (currentPet.getPhotoURL() != null && !currentPet.getPhotoURL().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(currentPet.getPhotoURL(), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                mainPetImageView.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Base64 디코딩 실패. 기본 이미지 사용.", e);
                mainPetImageView.setImageResource(R.drawable.pet);
            }
        } else {
            mainPetImageView.setImageResource(R.drawable.pet);
        }

        if (petList.size() > 1) {
            changePetButton.setVisibility(View.VISIBLE);
        } else {
            changePetButton.setVisibility(View.GONE);
        }
    }

    private void showChangePetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("홈 화면에 표시할 반려동물 선택");

        List<String> petNames = new ArrayList<>();
        for (Pet pet : petList) {
            petNames.add(pet.getName());
        }

        String[] petNamesArray = petNames.toArray(new String[0]);

        builder.setItems(petNamesArray, (dialog, which) -> {
            currentPet = petList.get(which);
            displayCurrentPet();
            Toast.makeText(MainActivity.this, currentPet.getName() + "이(가) 홈 화면에 표시됩니다.", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            Intent intent = new Intent(MainActivity.this, MedicalRecordActivity.class);
            if (currentPet != null && currentPet.getName() != null) {
                intent.putExtra("petName", currentPet.getName());
            }
            startActivity(intent);
        } else if (id == R.id.nav_chatbot) {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_nearby_hospitals) {
            Intent intent = new Intent(MainActivity.this, NearbyHospitalsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }
}
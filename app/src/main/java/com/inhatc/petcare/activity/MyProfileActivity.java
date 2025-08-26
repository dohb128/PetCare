package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import com.inhatc.petcare.model.Pet;
import com.inhatc.petcare.adapter.PetAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddPetDialogFragment.OnPetAddedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView navTitleView;
    private TextView emailProfileTextView;
    private FirebaseAuth mAuth;

    private RecyclerView registeredPetsRecyclerView;
    private PetAdapter petAdapter;
    private List<Pet> petList;
    private Button addPetButton;

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

        // Find views in the navigation header
        View headerView = navigationView.getHeaderView(0);
        navTitleView = headerView.findViewById(R.id.nav_Title);

        // Set user email in profile content
        emailProfileTextView = findViewById(R.id.txt_email);
        emailProfileTextView.setText(currentUser.getEmail());

        // Initialize profileImage from the toolbar
        profileImage = toolbar.findViewById(R.id.profile_image);
        // Set click listener for profile image in toolbar
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                Toast.makeText(MyProfileActivity.this, "프로필 사진 변경", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Log an error or handle the case where profileImage is not found in the toolbar
            // For now, we'll just skip setting the click listener
        }

        // Initialize RecyclerView
        registeredPetsRecyclerView = findViewById(R.id.registeredPetsRecyclerView);
        registeredPetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        petAdapter.setOnItemClickListener(new PetAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                // TODO: Handle edit pet
                Toast.makeText(MyProfileActivity.this, "Edit pet at position " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(int position) {
                // TODO: Handle delete pet
                petAdapter.removePet(position);
                Toast.makeText(MyProfileActivity.this, "Delete pet at position " + position, Toast.LENGTH_SHORT).show();
            }
        });
        registeredPetsRecyclerView.setAdapter(petAdapter);

        // Set click listener for Add Pet Button
        addPetButton = findViewById(R.id.btn_addPet);
        addPetButton.setOnClickListener(v -> {
            AddPetDialogFragment addPetDialogFragment = new AddPetDialogFragment();
            addPetDialogFragment.setOnPetAddedListener(this);
            addPetDialogFragment.show(getSupportFragmentManager(), "AddPetDialogFragment");
        });

        // TODO: Initialize other UI elements for profile management (e.g., RecyclerView, Buttons)
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            // Already in MyProfileActivity, just close the drawer
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(MyProfileActivity.this, LoginActivity.class));
            finish();
        } else {
            // Handle other menu item clicks if needed, for now just show a toast
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public void onPetAdded(Pet pet) {
        petAdapter.addPet(pet);
        Toast.makeText(this, pet.getName() + "이(가) 등록되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
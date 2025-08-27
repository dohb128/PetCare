package com.inhatc.petcare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.inhatc.petcare.R;

public class NearbyHospitalsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_hospitals);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title_textview);
        if (toolbarTitle != null) {
            toolbarTitle.setOnClickListener(v -> {
                Intent intent = new Intent(NearbyHospitalsActivity.this, MainActivity.class);
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
            Intent intent = new Intent(NearbyHospitalsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            startActivity(new Intent(this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            startActivity(new Intent(this, MedicalRecordActivity.class));
        } else if (id == R.id.nav_chatbot) {
            startActivity(new Intent(this, ChatbotActivity.class));
        } else if (id == R.id.nav_nearby_hospitals) {
            // 현재 화면
        } else if (id == R.id.nav_logout) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }
}

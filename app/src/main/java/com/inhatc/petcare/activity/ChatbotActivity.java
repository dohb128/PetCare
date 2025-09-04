package com.inhatc.petcare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.inhatc.petcare.R;
import com.inhatc.petcare.adapter.ChatAdapter;
import com.inhatc.petcare.model.ChatMessage;
import com.inhatc.petcare.model.MedicalRecord;
import com.inhatc.petcare.service.OpenAIService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private OpenAIService openAIService;
    private List<com.inhatc.petcare.model.Pet> userPets; // 사용자 반려동물 목록
    private List<com.inhatc.petcare.model.MedicalRecord> userMedicalRecords; // 사용자 반려동물 진료 기록 목록

    private FirebaseAuth mAuth;
    private DatabaseReference medicalRecordsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        mAuth = FirebaseAuth.getInstance();
        medicalRecordsRef = FirebaseDatabase.getInstance().getReference("medicalRecords");

        // OpenAI 서비스 초기화
        openAIService = new OpenAIService(this);

        // UI 초기화
        initViews();
        initChat();
        setupToolbar();
        setupNavigation();

        // 반려동물 정보 로드
        com.inhatc.petcare.PetCareApplication.getPetsForCurrentUser(pets -> {
            userPets = pets;
            loadMedicalRecordsForUserPets(); // 반려동물 정보 로드 후 진료 기록 로드
            // 환영 메시지 추가
            addBotMessage("안녕하세요! 🐾 반려동물 건강 상담사입니다.\n\n반려동물의 건강에 대해 궁금한 점이 있으시면 언제든 물어보세요.\n\n예시 질문:\n• 강아지가 밥을 안 먹어요\n• 고양이가 기침을 해요\n• 예방접종은 언제 받아야 하나요?");
        });
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // 전송 버튼 클릭 리스너
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initChat() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title_textview);
        if (toolbarTitle != null) {
            toolbarTitle.setOnClickListener(v -> {
                Intent intent = new Intent(ChatbotActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }

    private void setupNavigation() {
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
            Intent intent = new Intent(ChatbotActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void loadMedicalRecordsForUserPets() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || userPets == null || userPets.isEmpty()) {
            userMedicalRecords = new ArrayList<>(); // 초기화
            return;
        }

        userMedicalRecords = new ArrayList<>();
        // 모든 반려동물의 진료 기록을 가져오기 위해 각 petId로 쿼리
        // Firebase의 단일 쿼리로는 여러 petId를 동시에 검색하기 어려우므로, 각 반려동물별로 쿼리 수행
        // 또는 ownerId로 모든 진료 기록을 가져온 후 클라이언트에서 필터링

        // 여기서는 ownerId로 모든 진료 기록을 가져온 후 필터링하는 방식을 사용합니다.
        medicalRecordsRef.orderByChild("ownerId").equalTo(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    com.inhatc.petcare.model.MedicalRecord record = recordSnapshot.getValue(com.inhatc.petcare.model.MedicalRecord.class);
                    if (record != null) {
                        // 현재 사용자의 반려동물에 해당하는 진료 기록만 추가
                        for (com.inhatc.petcare.model.Pet pet : userPets) {
                            if (pet.getPetId().equals(record.getPetId())) {
                                userMedicalRecords.add(record);
                                break;
                            }
                        }
                    }
                }
                // 진료 기록 로드 완료 후 필요한 추가 작업 (예: 챗봇 시작 메시지 업데이트 등)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatbotActivity", "Failed to load medical records: " + error.getMessage());
            }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // API 키 확인
        if (openAIService.getApiKey().isEmpty()) {
            addBotMessage("⚠️ OpenAI API 키를 찾을 수 없습니다.\n\n.env 파일에 OPENAI_API_KEY가 설정되어 있는지 확인해주세요.");
            return;
        }

        // 사용자 메시지 추가
        addUserMessage(message);
        messageInput.setText("");

        // 로딩 메시지 추가
        addBotMessage("🤔 생각 중입니다...");

        // OpenAI API 호출
        openAIService.sendMessage(message, userPets, userMedicalRecords, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    removeLastMessage(); // 로딩 메시지 제거
                    addBotMessage("죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해주세요.");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject message = choice.getJSONObject("message");
                            String content = message.getString("content");
                            
                            runOnUiThread(() -> {
                                removeLastMessage(); // 로딩 메시지 제거
                                addBotMessage(content);
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            removeLastMessage(); // 로딩 메시지 제거
                            addBotMessage("응답을 처리하는 중 오류가 발생했습니다.");
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        removeLastMessage(); // 로딩 메시지 제거
                        addBotMessage("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                    });
                }
            }
        });
    }

    private void addUserMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void removeLastMessage() {
        if (!chatMessages.isEmpty()) {
            chatMessages.remove(chatMessages.size() - 1);
            chatAdapter.notifyItemRemoved(chatMessages.size());
        }
    }

    private void scrollToBottom() {
        chatRecyclerView.post(() -> chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            startActivity(new Intent(this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            startActivity(new Intent(this, MedicalRecordActivity.class));
        } else if (id == R.id.nav_chatbot) {
            // 현재 화면
        } else if (id == R.id.nav_nearby_hospitals) {
            startActivity(new Intent(this, NearbyHospitalsActivity.class));
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
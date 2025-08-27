package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import android.graphics.drawable.BitmapDrawable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;



public class EyeHealthCheckActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 101;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    
    private TextView emailTextView;
    private ImageView imageView;
    private Button cameraButton, galleryButton, eyeHealthCheckButton;
    private TextView predictionResultText;
    private final OkHttpClient httpClient = new OkHttpClient();
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_health_check);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(EyeHealthCheckActivity.this, LoginActivity.class));
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title_textview);
        if (toolbarTitle != null) {
            toolbarTitle.setOnClickListener(v -> {
                Intent intent = new Intent(EyeHealthCheckActivity.this, MainActivity.class);
                // 다른 액티비티 스택을 모두 지우고 새롭게 시작
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        // 네비게이션 헤더 타이틀 클릭 시 메인으로 이동
        View headerView = navigationView.getHeaderView(0);
        TextView navTitleView = headerView.findViewById(R.id.nav_Title);
        navTitleView.setOnClickListener(v -> {
            Intent intent = new Intent(EyeHealthCheckActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Find views in the main content area
        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        eyeHealthCheckButton = findViewById(R.id.eye_health_check_button);
        predictionResultText = findViewById(R.id.prediction_result_text);

        

        

        // Set click listeners for camera and gallery buttons
        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });

        galleryButton.setOnClickListener(v -> {
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
        });

        eyeHealthCheckButton.setOnClickListener(v -> {
            if (predictionResultText != null) {
                predictionResultText.setVisibility(View.GONE);
                predictionResultText.setText("");
            }
            uploadCurrentImage();
        });
    }

    private void uploadCurrentImage() {
        if (imageView.getDrawable() == null) {
            Toast.makeText(this, "이미지를 먼저 선택하거나 촬영해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap;
        if (imageView.getDrawable() instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
            imageView.setDrawingCacheEnabled(false);
        }

        if (bitmap == null) {
            Toast.makeText(this, "이미지를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos);
        byte[] imageBytes = baos.toByteArray();

        RequestBody imageRequestBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "upload.jpg", imageRequestBody)
                .build();

        Request request = new Request.Builder()
                .url("http://54.167.80.112:5000/predict")
                .post(requestBody)
                .build();

        Toast.makeText(this, "이미지를 업로드합니다...", Toast.LENGTH_SHORT).show();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(EyeHealthCheckActivity.this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        String predicted = parsePredictedClass(body);
                        if (predicted == null || predicted.isEmpty()) {
                            // predicted_class가 없으면 raw_output의 argmax로 보정
                            predicted = parsePredictedFromRawOutput(body);
                        }
                        if (predicted == null || predicted.isEmpty()) {
                            predictionResultText.setVisibility(View.GONE);
                            Toast.makeText(EyeHealthCheckActivity.this, "응답 파싱 실패", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = formatPredictionMessage(predicted);
                            // 예측 라벨만 굵게 표시하기 위해 HTML 사용
                            String html = highlightLabelBold(predicted, message);
                            predictionResultText.setText(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
                            predictionResultText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        predictionResultText.setVisibility(View.GONE);
                        Toast.makeText(EyeHealthCheckActivity.this, "서버 오류: " + response.code() + "\n" + body, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String parsePredictedClass(String json) {
        if (json == null) return null;
        try {
            JSONObject obj = new JSONObject(json);
            String value = obj.optString("predicted_class", null);
            if (value == null) return null;
            return value.trim();
        } catch (JSONException e) {
            // 폴백: 간단한 문자열 검색
            int keyIndex = json.indexOf("\"predicted_class\"");
            if (keyIndex < 0) return null;
            int colon = json.indexOf(":", keyIndex);
            if (colon < 0) return null;
            int firstQuote = json.indexOf('"', colon + 1);
            if (firstQuote < 0) return null;
            int secondQuote = json.indexOf('"', firstQuote + 1);
            if (secondQuote < 0) return null;
            return json.substring(firstQuote + 1, secondQuote).trim();
        }
    }

    private String parsePredictedFromRawOutput(String json) {
        if (json == null) return null;
        try {
            JSONObject obj = new JSONObject(json);
            // raw_output: [[p1,p2,p3,p4]] 형식 가정
            if (!obj.has("raw_output")) return null;
            // JSONArray 2중 구조를 직접 파싱
            String raw = obj.get("raw_output").toString();
            // 가장 간단한 방식: 숫자만 추출하여 4개 확률로 해석
            // 예: [[0.1, 0.2, 0.3, 0.4]]
            raw = raw.replace("[", "").replace("]", "");
            String[] parts = raw.split(",");
            if (parts.length < 4) return null;
            double max = -1.0;
            int maxIdx = -1;
            for (int i = 0; i < 4; i++) {
                try {
                    double v = Double.parseDouble(parts[i].trim());
                    if (v > max) {
                        max = v;
                        maxIdx = i;
                    }
                } catch (Exception ignore) {}
            }
            if (maxIdx < 0) return null;
            // 서버 모델 클래스 순서를 다음과 같이 가정합니다.
            // 0: 결막염, 1: 백내장, 2: 안검종양, 3: 무증상
            switch (maxIdx) {
                case 0: return "결막염";
                case 1: return "백내장";
                case 2: return "안검종양";
                case 3: return "무증상";
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String formatPredictionMessage(String predictedClass) {
        switch (predictedClass) {
            case "결막염":
                return "결막염으로 예상됩니다.";
            case "백내장":
                return "백내장으로 예상됩니다.";
            case "안검종양":
                return "안검종양으로 예상됩니다.";
            case "무증상":
            case "질병없음":
            case "정상":
                return "무증상/질병없음으로 예상됩니다.";
            default:
                return predictedClass + "으로 예상됩니다.";
        }
    }

    private String highlightLabelBold(String predictedClass, String message) {
        // message에서 predictedClass 부분만 <b>로 감싸기
        // "무증상/질병없음으로 예상됩니다."의 경우 "무증상" 또는 "질병없음" 둘 다 처리
        if (message == null) return "";
        if (predictedClass == null) return message;
        try {
            if (predictedClass.equals("무증상") || predictedClass.equals("질병없음") || predictedClass.equals("정상")) {
                // 문구는 "무증상/질병없음으로 예상됩니다." → 앞 단어만 강조
                return message.replaceFirst("무증상/질병없음", "<b>무증상/질병없음</b>");
            }
            return message.replaceFirst(predictedClass, "<b>" + predictedClass + "</b>");
        } catch (Exception e) {
            return message;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) { // Check if "My Page" was clicked
            startActivity(new Intent(EyeHealthCheckActivity.this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            startActivity(new Intent(EyeHealthCheckActivity.this, MedicalRecordActivity.class));
        } else if (id == R.id.nav_chatbot) {
            startActivity(new Intent(EyeHealthCheckActivity.this, ChatbotActivity.class));
        } else if (id == R.id.nav_nearby_hospitals) {
            startActivity(new Intent(EyeHealthCheckActivity.this, NearbyHospitalsActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(EyeHealthCheckActivity.this, LoginActivity.class));
            finish();
        } else {
            // Handle other menu item clicks if needed, for now just show a toast
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void openGallery() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickGalleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Gallery permission is required to access the gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
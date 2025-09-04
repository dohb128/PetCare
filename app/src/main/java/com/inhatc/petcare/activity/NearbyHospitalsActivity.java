package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.inhatc.petcare.model.Hospital;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NearbyHospitalsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "NearbyHospitalsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private GoogleMap googleMap;

    private FusedLocationProviderClient fusedLocationClient;
    private OkHttpClient httpClient;
    private String googleMapsApiKey;

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
        TextView navTitleView = headerView.findViewById(R.id.nav_Title);
        navTitleView.setOnClickListener(v -> {
            Intent intent = new Intent(NearbyHospitalsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Google Maps 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // 위치 서비스 및 HTTP 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        httpClient = new OkHttpClient();

        try {
            googleMapsApiKey = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
            if (googleMapsApiKey == null || googleMapsApiKey.equals("YOUR_API_KEY_HERE")) {
                Log.e(TAG, "Google Maps API Key not found in AndroidManifest.xml or is a placeholder.");
                Toast.makeText(this, "Google Maps API Key not found.", Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting application info", e);
            Toast.makeText(this, "Error loading Google Maps API Key.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);

        // onMapReady에서 권한을 확인하고, 권한이 있을 때만 현재 위치 표시를 활성화합니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getCurrentLocationAndSearch();
        } else {
            // 권한이 없을 경우 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    // 권한이 허용되면 현재 위치 표시 활성화
                    googleMap.setMyLocationEnabled(true);
                    getCurrentLocationAndSearch();
                }
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocationAndSearch() {
        // 이 메서드에서도 다시 한번 권한을 확인합니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

                        searchNearbyHospitalsWebService(currentLatLng);
                    } else {
                        Log.w(TAG, "위치 정보를 가져올 수 없습니다.");
                        Toast.makeText(this, "현재 위치를 찾을 수 없습니다. 기본 위치로 이동합니다.", Toast.LENGTH_SHORT).show();

                        LatLng seoulCityHall = new LatLng(37.5665, 126.9780);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoulCityHall, 15));
                    }
                });
    }

    private void searchNearbyHospitalsWebService(LatLng currentLatLng) {
        if (googleMapsApiKey == null || googleMapsApiKey.equals("YOUR_API_KEY_HERE")) {
            Log.e(TAG, "Google Maps API Key is not available for web service call.");
            Toast.makeText(this, "Google Maps API Key is not configured.", Toast.LENGTH_LONG).show();
            return;
        }

        // Places API Nearby Search 요청 URL 구성
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&keyword=%s&language=ko&key=%s",
                currentLatLng.latitude, currentLatLng.longitude, 1000, "동물병원", googleMapsApiKey
        );

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Places API Web Service 요청 실패", e);
                runOnUiThread(() -> Toast.makeText(NearbyHospitalsActivity.this, "주변 동물병원 검색에 실패했습니다.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String status = jsonObject.optString("status", "UNKNOWN");
                            Log.d(TAG, "Places API 응답 상태: " + status);

                            if ("OK".equals(status)) {
                                JSONArray results = jsonObject.getJSONArray("results");

                                googleMap.clear();
                                Log.d(TAG, "장소 검색 성공. 찾은 장소 수: " + results.length());

                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject placeJson = results.getJSONObject(i);
                                    String placeName = placeJson.getString("name");
                                    String placeId = placeJson.getString("place_id");
                                    JSONObject geometry = placeJson.getJSONObject("geometry");
                                    JSONObject location = geometry.getJSONObject("location");
                                    double lat = location.getDouble("lat");
                                    double lng = location.getDouble("lng");

                                    // 상세 정보 (주소, 전화번호)는 Place Details API로 별도 요청
                                    fetchPlaceDetails(placeId, placeName, new LatLng(lat, lng));
                                }
                            } else if ("ZERO_RESULTS".equals(status)) {
                                Log.d(TAG, "장소 검색 성공. 찾은 장소 수: 0 (ZERO_RESULTS)");
                                runOnUiThread(() -> Toast.makeText(NearbyHospitalsActivity.this, "주변 동물병원을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                            } else {
                                Log.e(TAG, "Places API 오류 상태: " + status);
                                runOnUiThread(() -> Toast.makeText(NearbyHospitalsActivity.this, "장소 검색 중 오류 발생: " + status, Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON 파싱 오류", e);
                            Toast.makeText(NearbyHospitalsActivity.this, "장소 정보 파싱에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Places API Web Service 응답 실패: " + response.code() + " " + response.message());
                    runOnUiThread(() -> Toast.makeText(NearbyHospitalsActivity.this, "주변 동물병원 검색에 실패했습니다.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchPlaceDetails(String placeId, String placeName, LatLng latLng) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=name,formatted_address,international_phone_number&key=%s",
                placeId, googleMapsApiKey
        );

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Place Details API 요청 실패", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONObject result = jsonObject.getJSONObject("result");

                            String name = result.optString("name", placeName);
                            String address = result.optString("formatted_address", "주소 정보 없음");
                            String phoneNumber = result.optString("international_phone_number", "전화번호 정보 없음");

                            Hospital hospital = new Hospital(name, address, phoneNumber, latLng.latitude, latLng.longitude, placeId);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title(name);
                            googleMap.addMarker(markerOptions).setTag(hospital);
                            Log.d(TAG, "동물병원 마커 추가: " + name);

                        } catch (JSONException e) {
                            Log.e(TAG, "Place Details JSON 파싱 오류", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Place Details API 응답 실패: " + response.code() + " " + response.message());
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_my_page) {
            startActivity(new Intent(this, MyProfileActivity.class));
        } else if (id == R.id.nav_medical_records) {
            // MedicalRecordActivity로 이동하도록 수정
            // startActivity(new Intent(this, MedicalRecordActivity.class));
        } else if (id == R.id.nav_chatbot) {
            startActivity(new Intent(this, ChatbotActivity.class));
        } else if (id == R.id.nav_nearby_hospitals) {
            // 현재 화면
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, item.getTitle() + " 클릭", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public boolean onMarkerClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
        // 마커 클릭 시 정보 창을 보여주기 위해 커스텀 InfoWindowAdapter 설정
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull com.google.android.gms.maps.model.Marker marker) {
                return null; // 기본 정보 창 사용 안 함
            }

            @Override
            public View getInfoContents(@NonNull com.google.android.gms.maps.model.Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView title = infoWindow.findViewById(R.id.info_window_title);
                TextView address = infoWindow.findViewById(R.id.info_window_address);
                TextView phone = infoWindow.findViewById(R.id.info_window_phone);

                Hospital hospital = (Hospital) marker.getTag();
                if (hospital != null) {
                    title.setText(hospital.getName());
                    address.setText(hospital.getAddress() != null ? hospital.getAddress() : "주소 정보 없음");
                    phone.setText(hospital.getPhoneNumber() != null ? hospital.getPhoneNumber() : "전화번호 정보 없음");
                } else {
                    title.setText(marker.getTitle());
                    address.setText("상세 정보 없음");
                    phone.setText("상세 정보 없음");
                }
                return infoWindow;
            }
        });
        marker.showInfoWindow(); // 정보 창 표시
        return true; // 이벤트 소비
    }
}
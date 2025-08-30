package com.inhatc.petcare.service;

import android.content.Context;
import android.util.Log;
import com.inhatc.petcare.util.EnvUtil;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenAIService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private String apiKey;

    public OpenAIService() {
        this.client = new OkHttpClient();
        this.apiKey = "";
    }

    public OpenAIService(Context context) {
        this.client = new OkHttpClient();
        loadApiKey(context);
    }

    private void loadApiKey(Context context) {
        // .env 파일에서 API 키 로드
        EnvUtil.loadEnvFile(context);
        this.apiKey = EnvUtil.get("OPENAI_API_KEY", "");
        
        if (this.apiKey.isEmpty()) {
            Log.w("OpenAIService", "OPENAI_API_KEY not found in .env file");
        }
    }

    public void sendMessage(String userMessage, Callback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);
            
            JSONArray messages = new JSONArray();
            
            // 시스템 메시지 (반려동물 전문가 역할 설정)
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "당신은 반려동물 건강 전문가입니다. 반려동물의 질병, 증상, 예방접종, 영양 등에 대해 전문적이고 친근하게 답변해주세요. 항상 한국어로 답변하고, 필요시 수의사 상담을 권장하세요.");
            messages.put(systemMessage);
            
            // 사용자 메시지
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}

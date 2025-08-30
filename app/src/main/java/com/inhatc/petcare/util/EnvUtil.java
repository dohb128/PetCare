package com.inhatc.petcare.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EnvUtil {
    private static final String TAG = "EnvUtil";
    private static Map<String, String> envVars = new HashMap<>();

    public static void loadEnvFile(Context context) {
        try {
            // assets 폴더의 .env 파일 읽기
            InputStream inputStream = context.getAssets().open("api.env");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim().replaceAll("^\"|\"$", "");
                        envVars.put(key, value);
                        Log.d(TAG, "Loaded env var: " + key + " = " + value);
                    }
                }
            }
            
            reader.close();
            inputStream.close();
            Log.i(TAG, "Successfully loaded .env file from assets");
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading .env file from assets", e);
        }
    }

    public static String get(String key, String defaultValue) {
        return envVars.getOrDefault(key, defaultValue);
    }

    public static String get(String key) {
        return envVars.get(key);
    }

    public static boolean has(String key) {
        return envVars.containsKey(key);
    }
}

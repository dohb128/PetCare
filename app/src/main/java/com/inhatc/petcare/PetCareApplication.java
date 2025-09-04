package com.inhatc.petcare;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PetCareApplication extends Application {

    private static final String TAG = "PetCareApplication";
    

    @Override
    public void onCreate() {
        super.onCreate();

        com.inhatc.petcare.util.EnvUtil.loadEnvFile(this);
    }
}
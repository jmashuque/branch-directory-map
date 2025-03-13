package com.example.branchdirectorymap;

import android.app.Application;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class BranchDirectoryMap extends Application {

    public static BranchDirectoryMap instance;
    public final String SHARED_PREFS = "sharedPrefs";
    public final String SECURE_PREFS = "securePrefs";
    public final String KEY_APIKEY = "geocode_api_key";
    public final String KEY_ALIAS = "geocodeAPIAESKey";
    public final String ANDROID_KEYSTORE = "AndroidKeyStore";
    public final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    public final int IV_SIZE = 12;
    public final int GCM_TAG_SIZE = 128;
    public final String KEY_LOAD_FINISHED = "loadFinished";
    public final String KEY_LOAD_ORDER = "loadOrder";
    public final String KEY_LOAD_OVERRIDE = "loadOverride";
    public final String KEY_USE_LAST = "useLast";
    public final String KEY_APIKEY_LOADED = "apiKeyLoaded";
    public final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    public final String DIR_URL = "https://www.google.com/maps/dir/?api=1&dir_action=navigate&destination=";
    public final String ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    public final int NUM_OF_MYITEM_VARS = 5;
    public final int SEARCH_LEVELS = 4;
    public final Type VARMAP_TYPE = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static BranchDirectoryMap getInstance() {
        return instance;
    }
}

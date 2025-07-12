package com.example.branchdirectorymap;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BranchDirectoryMap extends Application {

    public static final String[] DEFAULT_TRAFFIC_MODE = {"Best Guess", "Optimal Aware"};
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SECURE_PREFS = "securePrefs";
    public static final String KEY_APIKEY = "geocode_api_key";
    public static final String KEY_ALIAS = "geocodeAPIAESKey";
    public static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    public static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    public static final int IV_SIZE = 12;
    public static final int GCM_TAG_SIZE = 128;
    public static final int MAX_INTERMEDIATES_EXT = 9;
    public static final String KEY_FIRST_RUN = "firstRun";
    public static final String KEY_LOAD_FINISHED = "loadFinished";
    public static final String KEY_LOAD_REDO = "loadRedo";
    public static final String KEY_LOAD_REDO_WAYPOINT = "loadRedoWaypoint";
    public static final String KEY_LOAD_OVERRIDE = "loadOverride";
    public static final String KEY_USE_LAST = "useLast";
    public static final String KEY_APIKEY_LOADED = "apiKeyLoaded";
    public static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    public static final String DIR_URL = "https://www.google.com/maps/dir/?api=1&dir_action=navigate&travelmode=driving&destination=";
    public static final String ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    public static final int NUM_OF_MYITEM_VARS = 5;
    public static final int SEARCH_LEVELS = 4;
    public static final Type VARMAP_TYPE = new TypeToken<ConcurrentHashMap<String, Map<String, Object>>>(){}.getType();
    public static final Type POSITIONS_TYPE = new TypeToken<Map<String, LatLng>>(){}.getType();
    public static final boolean SCREENSHOT_MODE = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

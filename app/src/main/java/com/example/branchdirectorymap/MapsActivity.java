package com.example.branchdirectorymap;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowCloseListener, CustomInfoWindowAdapter.InfoWindowOpenListener {

    private static final String TAG = "SYS-MAPS";
    private Context context;
    private GoogleMap mMap;
    private boolean mapReady = false;
    private final List<Runnable> afterMapReady = new ArrayList<>();
    private final Set<String> afterMapReadyKeys = new HashSet<>();
    private FusedLocationProviderClient fusedLocationClient;
    private DialogUtils dialogUtils;
    private String linkApiKey;
    private String varMapStr;
    private List<String> stylesList;
    private Map<String, MapStyleOptions> styles;
    private double[] trafficMetrics;
    private Map<String, Map<String, Object>> varMap;
    private final ArrayList<String> tables = new ArrayList<>();
    private final HashSet<String> tablesSet = new HashSet<>();
    private MapLoaderTask task;
    private GetInformationTask getInformationTask;
    private Location lastKnownLocation;
    private ConstraintLayout searchViewLayout;
    private ImageButton menuButton;
    private Spinner searchSpinner;
    private TextView loadTextView;
    private LinearLayout markerButtonsLayout;
    private MaterialButton viewMarkerButton;
    private MaterialButton addMarkerButton;
    private MaterialButton removeMarkerButton;
    private MaterialButton callMarkerButton;
    private SearchSpinnerAdapter searchAdapter;
    private FloatingActionButton routeButton;
    private LinearLayout routeMenu;
    private FloatingActionButton layersButton;
    private LinearLayout layersMenu;
    private Spinner appearanceSpinner;
    private Spinner themeSpinner;
    private MaterialSwitch switchTraffic;
    private View buttonToChange;
    private View menuToChange;
    private MaterialSwitch switchAlt;
    private MaterialSwitch switchMono;
    private FloatingActionButton directionsRouteButton;
    private FloatingActionButton clearRouteButton;
    private Spinner trafficSpinner;
    private MaterialSwitch switchHighways;
    private MaterialSwitch switchTolls;
    private MaterialSwitch switchFerries;
    private RelativeLayout.LayoutParams clearRouteButtonParams;
    private ClusterManager<CustomItem> clusterManager;
    private CustomClusterRenderer customRenderer;
    private CustomInfoWindowAdapter customAdapter;
    private Map<String, List<CustomItem>> markers;
    private Map<String, List<CustomItem>> badMarkers;
    private Marker currentMarker;
    private Marker tempMarker;
    private CustomItem currentItem;
    private final Map<String, Map<CustomItem, Marker>> clusterItemMarkerMap = new HashMap<>();
    private final List<Marker> waypointList = new ArrayList<>();
    private SearchView searchView;
    private SearchView.OnQueryTextListener queryListener;
    private SimpleCursorAdapter suggestionAdapter;
    private final ArrayList<ArrayList<CustomItem>> filteredSuggestions = new ArrayList<>();
    private final Map<String, ArrayList<Object>> routeMarkers = new LinkedHashMap<>();
    private LocationDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String oldDistance;
    private String oldDuration;
    private String oldArrival;
    private List<List<String>> encodedPolyline;
    private List<List<String>> oldPolyline;
    private final List<Polyline> polylines = new ArrayList<>();
    private SpannableString oldTrafficText;
    private java.text.SimpleDateFormat sdf;
    private Cluster<CustomItem> lastCluster;
    private boolean lastSelected;
    private boolean suppressQuery = false;
    private boolean suppressListener = true;
    private Animation animDisappear;
    private String currentTable;
    private long start;
    private String trafficMode;
    private int local_intermediates;
    private int intermediates_count = 0;
    private int mapMode = BranchDirectoryMap.DEFAULT_MODE;
    private int appTheme = BranchDirectoryMap.DEFAULT_THEME;
    private boolean locationPermissionGranted = false;
    private boolean isCentered = false;
    private boolean isInfoWindowOpen = false;
    private boolean isInfoWindowRedraw = false;
    private boolean isMenuActive = false;
    private boolean isHighwaysEnabled = true;
    private boolean isTollsEnabled = true;
    private boolean isFerriesEnabled = true;
    private boolean isDarkEnabled = false;
    private boolean isAmbientEnabled = false;
    private boolean isTrafficEnabled = true;
    private boolean isAltEnabled = false;
    private boolean isMonoEnabled = false;
    private boolean doubleBackToExitPressedOnce = false;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightListener;
    private volatile float lastLux = Float.NaN;
    private ScheduledExecutorService ambientExecutor;
    private ScheduledFuture<?> ambientFuture;
    private volatile Boolean lastAppliedDark = null;
    private static final int VIOLATION_TOLLS    = 1;      // 001
    private static final int VIOLATION_FERRIES  = 1 << 1; // 010
    private static final int VIOLATION_HIGHWAYS = 1 << 2; // 100

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme(appTheme, false);
        setContentView(R.layout.activity_maps);
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        context = this;

        searchViewLayout = findViewById(R.id.layout_searchview);
        searchView = findViewById(R.id.searchView);
        markerButtonsLayout = findViewById(R.id.layout_marker_buttons);
        dbHelper = new LocationDatabaseHelper(this);
        dialogUtils = new DialogUtils();
        sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());

        sharedPreferences = getSharedPreferences(BranchDirectoryMap.SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (!sharedPreferences.contains(BranchDirectoryMap.KEY_LOAD_FINISHED)) {
            editor.putBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false).apply();
        }

        String[] trafficMetricsStr = BuildConfig.TRAFFIC_METRICS.split(",");
        trafficMetrics = new double[trafficMetricsStr.length];
        for (int i = 0; i < trafficMetricsStr.length; i++) {
            trafficMetrics[i] = Double.parseDouble(trafficMetricsStr[i].trim());
//            Log.i(TAG, "trafficMetrics[" + i + "]: " + trafficMetrics[i]);
        }

        if (BranchDirectoryMap.SCREENSHOT_MODE) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        Log.i(TAG, "MapsActivity created");

        getLocationPermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation(this::centerOnUser);
        setupInterface();
        setupSearchView();
        restyleAfterThemeChange();
        setAutoCompleteThreshold(searchView);   // show suggestions immediately
        suggestionPopupResize(searchView);      // gap between suggestion dropdown and IME
        handleIntent(getIntent());
        mapFragment.getMapAsync(this);
    }
    
    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.i(TAG, "location permission already granted");
        } else {
            Toast.makeText(this, getString(R.string.location_denied), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
        }
    }

    public void setupInterface() {
        menuButton = findViewById(R.id.button_menu);
        menuButton.setOnClickListener(v -> {
            menuToggler();
        });
        menuButton.setVisibility(View.GONE);

        searchSpinner = findViewById(R.id.spinner_search);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "triggered searchSpinner onItemSelected");
                Log.i(TAG, "position: " + position + " item: " + parent.getItemAtPosition(position).toString());

                currentTable = parent.getItemAtPosition(position).toString();
                searchAdapter.setSelectedItemPosition(position);
                updateSuggestions("");
                routeMarkers.clear();
                moveRouteButtons();
                clusterManager.clearItems();
                for (CustomItem marker : markers.get(currentTable)) {
                    clusterManager.addItem(marker);
                }
                clusterManager.cluster();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "triggered searchSpinner onNothingSelected");
            }
        });
        searchSpinner.setVisibility(View.GONE);

        Animation animLeftMenu = AnimationUtils.loadAnimation(this, R.anim.menu_left_anim);
        animDisappear = AnimationUtils.loadAnimation(this, R.anim.menu_disappear);
        animDisappear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (menuToChange != null) {
                    menuToChange.setVisibility(View.GONE);
                    if (menuToChange == layersMenu) {
                        moveRouteButtons();
                    }
                    menuToChange = null;
                }
                if (buttonToChange != null) {
                    buttonToChange.setVisibility(View.VISIBLE);
                    buttonToChange = null;
                }
            }
        });

        routeButton = findViewById(R.id.button_route);
        routeButton.setOnClickListener(v -> {
            routeMenu.setVisibility(View.VISIBLE);
            routeMenu.bringToFront();
            routeMenu.startAnimation(animLeftMenu);
            routeButton.setVisibility(View.GONE);
            clearMenus("R");
            clearMap();
        });
        routeButton.setVisibility(View.GONE);

        routeMenu = findViewById(R.id.menu_route);
        routeMenu.setVisibility(View.GONE);

        trafficSpinner = findViewById(R.id.spinner_traffic);
        ArrayAdapter<CharSequence> trafficAdapter;
        int arrayResId;
        if (BuildConfig.USE_ADVANCED_ROUTING) {
            arrayResId = R.array.traffic_adv_dropdown;
        } else {
            arrayResId = R.array.traffic_dropdown;
        }
        trafficAdapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                R.layout.spinner_selected_item);
        trafficAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trafficSpinner.setAdapter(trafficAdapter);
        trafficSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trafficMode = parent.getItemAtPosition(position).toString();
                Log.i(TAG, "trafficMode: " + trafficMode);
                clearMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        trafficMode = BranchDirectoryMap.DEFAULT_TRAFFIC_MODE[BuildConfig.USE_ADVANCED_ROUTING ? 1 : 0];
        Log.i(TAG, "default trafficMode: " + trafficMode);
        if (!trafficMode.isEmpty()) {
            trafficSpinner.setSelection(trafficAdapter.getPosition(trafficMode));
        }

        switchHighways = findViewById(R.id.switch_highways);
        switchHighways.setChecked(isHighwaysEnabled);
        switchHighways.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighwaysEnabled = isChecked;
            clearMap();
        });

        switchTolls = findViewById(R.id.switch_tolls);
        switchTolls.setChecked(isTollsEnabled);
        switchTolls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTollsEnabled = isChecked;
            clearMap();
        });

        switchFerries = findViewById(R.id.switch_ferries);
        switchFerries.setChecked(isFerriesEnabled);
        switchFerries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFerriesEnabled = isChecked;
            clearMap();
        });

        Animation animRightMenu = AnimationUtils.loadAnimation(this, R.anim.menu_right_anim);

        layersButton = findViewById(R.id.button_layers);
        layersButton.setOnClickListener(v -> {
            layersMenu.setVisibility(View.VISIBLE);
            layersMenu.bringToFront();
            layersMenu.startAnimation(animRightMenu);
            layersButton.setVisibility(View.GONE);
            clearMenus("L");
            clearMap();
            moveRouteButtons();
        });
        layersButton.setVisibility(View.GONE);

        layersMenu = findViewById(R.id.menu_layers);
        layersMenu.setVisibility(View.GONE);

        appearanceSpinner = findViewById(R.id.spinner_appearance);
        ArrayAdapter<CharSequence> appearanceAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.appearance_dropdown,
                R.layout.spinner_selected_item);
        appearanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appearanceSpinner.setAdapter(appearanceAdapter);
        appearanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "triggered appearanceSpinner onItemSelected");
                if (!suppressListener) {
                    switch (position) {
                        case 0:
                            mapMode = GoogleMap.MAP_TYPE_NORMAL;
                            switchToggler(switchAlt, true);
                            switchToggler(switchMono, true);
                            break;
                        case 1:
                            mapMode = GoogleMap.MAP_TYPE_SATELLITE;
                            switchToggler(switchAlt, false);
                            switchToggler(switchMono, false);
                            break;
                        case 2:
                            mapMode = GoogleMap.MAP_TYPE_TERRAIN;
                            switchToggler(switchAlt, false);
                            switchToggler(switchMono, false);
                            break;
                        case 3:
                            mapMode = GoogleMap.MAP_TYPE_HYBRID;
                            switchToggler(switchAlt, false);
                            switchToggler(switchMono, false);
                            break;
                        default:
                            Log.e(TAG, "unknown map mode in appearanceSpinner");
                            break;
                    }
                    mMap.setMapType(mapMode);
                    clearMap();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        appearanceSpinner.setSelection(mapMode);

        themeSpinner = findViewById(R.id.spinner_theme);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                this,
                BuildConfig.USE_LIGHT_SENSOR ? R.array.theme_dropdown_sensor : R.array.theme_dropdown,
                R.layout.spinner_selected_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.i(TAG, "triggered themeSpinner onItemSelected");
//                Log.i(TAG, "position: " + position + " item: " + parent.getItemAtPosition(position).toString());
                if (!suppressListener) {
                    appTheme = position;
                    setAppTheme(appTheme, true);
                }
                suppressListener = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        themeSpinner.setSelection(appTheme);

        switchTraffic = findViewById(R.id.switch_traffic);
        switchTraffic.setChecked(isTrafficEnabled);
        switchTraffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTrafficEnabled = isChecked;
            mMap.setTrafficEnabled(isChecked);
            clearMap();
        });

        switchAlt = findViewById(R.id.switch_alt);
        switchAlt.setChecked(isAltEnabled);
        switchAlt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAltEnabled = isChecked;
            if (isAltEnabled) {
                switchMono.setChecked(false);
            }
            setMapStyle();
        });

        switchMono = findViewById(R.id.switch_mono);
        switchMono.setChecked(isMonoEnabled);
        switchMono.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMonoEnabled = isChecked;
            if (isMonoEnabled) {
                switchAlt.setChecked(false);
            }
            setMapStyle();
        });

        directionsRouteButton = findViewById(R.id.button_directions);
        directionsRouteButton.setOnClickListener(v -> {
            LinkedHashMap<String, ArrayList<Object>> snapshotMap = deepCopyMap(routeMarkers);
            String lastKey = getLastKey(routeMarkers);
            ArrayList<Object> lastList = routeMarkers.get(lastKey);
            CustomItem snapshotItem = currentItem.deepCopy();
            currentItem = ((CustomItem) lastList.get(1)).deepCopy();
            routeMarkers.remove(lastKey);

            LatLng position;
            if (currentItem.getLastWaypoint() != null && !currentItem.getLastWaypoint().isEmpty()) {
                position = currentItem.getWaypoints().get(currentItem.getLastWaypoint());
            } else {
                position = currentItem.getPosition();
            }
            StringBuilder url = new StringBuilder(BranchDirectoryMap.DIR_URL);
            url.append(position.latitude).append(",").append(position.longitude);
            if (!routeMarkers.isEmpty() || currentItem.getWaypointsCount() > 1) {
                StringBuilder params = waypointCombiner("|", true);
                if (params.length() > 0) {
                    url.append(params);
                }
            }
            openMapsApp(Uri.parse(url.toString()));

            currentItem = snapshotItem.deepCopy();
            routeMarkers.clear();
            routeMarkers.putAll(snapshotMap);
        });

        clearRouteButton = findViewById(R.id.button_clear);
        clearRouteButton.setOnClickListener(v -> clearRoute());
        clearRouteButtonParams = (RelativeLayout.LayoutParams) clearRouteButton.getLayoutParams();

        viewMarkerButton = findViewById(R.id.button_marker_view);
        viewMarkerButton.setOnClickListener(view -> {
            String address = "geo:0,0?q=" + currentItem.getPosition().latitude + "," + currentItem.getPosition().longitude
                    + "(" + Uri.encode(currentItem.getTitle()) + ")";
            openMapsApp(Uri.parse(address));
        });

        addMarkerButton = findViewById(R.id.button_marker_add);
        addMarkerButton.setOnClickListener(view -> addMarkerToRoute());

        removeMarkerButton = findViewById(R.id.button_marker_remove);
        removeMarkerButton.setOnClickListener(view -> removeMarkerFromRoute());
        removeMarkerButton.setVisibility(View.GONE);

        callMarkerButton = findViewById(R.id.button_marker_call);
        callMarkerButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + currentItem.getPhone()));
            startActivity(intent);
        });
    }

    public void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        String[] from = {"location", "address"};
        int[] to = {R.id.suggestion_text, R.id.suggestion_description};
        suggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.suggestion_item, null, from, to, 0);
        searchView.setSuggestionsAdapter(suggestionAdapter);

        searchView.setOnClickListener(v -> searchView.setIconified(false));

        queryListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "triggered onQueryTextSubmit");
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "triggered onQueryTextChange");
                Log.i(TAG, "suppressQuery: " + suppressQuery);
                if (!suppressQuery) {
                    updateSuggestions(newText);
                }
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryListener);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                MatrixCursor cursor = (MatrixCursor) suggestionAdapter.getCursor();
                cursor.moveToPosition(position);
                String location = cursor.getString(cursor.getColumnIndex("location"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                searchView.setQuery(location + "+" + address, true);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearMenus("");
                clearMap();
                Log.i(TAG, "triggered setOnQueryTextFocusChangeListener");
                updateSuggestions("");
                searchView.setQuery(searchView.getQuery().toString(), false);
            }
        });
        searchView.setVisibility(View.GONE);
    }

    public void setAutoCompleteThreshold(SearchView searchView) {
        try {
            Field searchAutoCompleteField = SearchView.class.getDeclaredField("mSearchSrcTextView");
            searchAutoCompleteField.setAccessible(true);
            AutoCompleteTextView searchAutoComplete = (AutoCompleteTextView) searchAutoCompleteField.get(searchView);
            searchAutoComplete.setThreshold(0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();    // FIX THIS: add logging
        }
    }

    private void suggestionPopupResize(SearchView searchView) {
        final AutoCompleteTextView auto = getSearchAutoComplete(searchView);
        if (auto == null) {
            Log.e(TAG, "Could not find mSearchSrcTextView");
            return;
        }

        auto.setImeOptions(auto.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        Runnable applyFixed = () -> {
            int desired = preImeCapPx(searchView);
            int available = availableBelowPx(searchView, auto);
            int fixedHeight = Math.max(0, Math.min(desired, available));

            int width = searchView.getWidth();
            if (width > 0) {
                auto.setDropDownWidth(width);
                auto.setDropDownHorizontalOffset(0);
            }
            auto.setDropDownHeight(fixedHeight);
//            Log.i(TAG, "fixedHeight=" + fixedHeight + " desired=" + desired + " available=" + available);
        };

        searchView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> applyFixed.run());

        auto.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                applyFixed.run();
                auto.post(auto::showDropDown);
            }
        });

        searchView.post(applyFixed);
    }

    private int preImeCapPx(View anyView) {
        int h = anyView.getRootView().getHeight();
        return Math.max(dp(anyView.getContext(), 160), (int) (h * BranchDirectoryMap.SUGGESTION_RATIO));
    }

    private int availableBelowPx(View host, AutoCompleteTextView auto) {
        int[] loc = new int[2];
        auto.getLocationOnScreen(loc);
        int autoBottom = loc[1] + auto.getHeight();

        int screenH = host.getRootView().getHeight();
        int systemBottomInset = 0;
        try {
            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(host);
            if (insets != null) {
                systemBottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            }
        } catch (Throwable ignored) {}
        int windowBottom = screenH - systemBottomInset;

        int gap = dp(host.getContext(), 2);
        return Math.max(0, windowBottom - autoBottom - gap);
    }

    private AutoCompleteTextView getSearchAutoComplete(SearchView searchView) {
        try {
            Field f = SearchView.class.getDeclaredField("mSearchSrcTextView");
            f.setAccessible(true);
            Object obj = f.get(searchView);
            if (obj instanceof AutoCompleteTextView) return (AutoCompleteTextView) obj;
        } catch (Throwable t) {
            Log.e(TAG, "Reflection failed: " + t);
        }
        return null;
    }

    private int dp(Context c, int d) {
        return (int) (d * c.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;

        updateLocationUI();
        if (lastKnownLocation != null) {
            centerOnUser();
        }

        if ((BuildConfig.USE_ADVANCED_ROUTING && BuildConfig.INTERMEDIATE_STEPS > 25) ||
                (!BuildConfig.USE_ADVANCED_ROUTING && BuildConfig.INTERMEDIATE_STEPS > 23)) {
            Toast.makeText(this, getString(R.string.exceed_intermediates), Toast.LENGTH_SHORT).show();
            local_intermediates = BuildConfig.USE_ADVANCED_ROUTING ? 25 : 23;
        } else {
            local_intermediates = BuildConfig.INTERMEDIATE_STEPS;
        }

        mMap.setOnMapClickListener(latLng -> {
            if (routeMenu.getVisibility() == View.VISIBLE || layersMenu.getVisibility() == View.VISIBLE) {
                clearMenus("");
            }
            if (isInfoWindowOpen) {
                clearInfoWindow();
            }
            searchView.setQuery("", false);
            Log.i(TAG, "clearfocus 1");
            searchView.clearFocus();
        });

        clusterManager = new ClusterManager<>(this, mMap) {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (waypointList.contains(marker)) {
                    return true;        // waypoint markers are not interactive
                }
                return super.onMarkerClick(marker);
            }
        };
        customRenderer = new CustomClusterRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(customRenderer);

        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnCameraMoveListener(() -> {
            if (currentMarker != null) {
                Projection projection = mMap.getProjection();
                Point screenPosition = projection.toScreenLocation(currentMarker.getPosition());
                moveButtonsWithMarker(screenPosition);
            }
            isCentered = false;
        });

        customAdapter = new CustomInfoWindowAdapter(this);
        clusterManager.getMarkerCollection().setInfoWindowAdapter(customAdapter);
        customAdapter.setInterface(locationPermissionGranted, BuildConfig.USE_ADVANCED_ROUTING);
        customAdapter.setInfoWindowOpenListener(this);
        clusterManager.getMarkerCollection().setOnInfoWindowClickListener(marker -> {
            Log.i(TAG, "triggered onInfoWindowClick");
            if (locationPermissionGranted) {
                LatLng position;
                if (currentItem.getLastWaypoint() != null && !currentItem.getLastWaypoint().isEmpty()) {
                    position = currentItem.getWaypoints().get(currentItem.getLastWaypoint());
                } else {
                    position = marker.getPosition();
                }
                StringBuilder url = new StringBuilder(BranchDirectoryMap.DIR_URL);
                url.append(position.latitude).append(",").append(position.longitude);
                if (!routeMarkers.isEmpty() || currentItem.getWaypointsCount() > 1) {
                    StringBuilder params = waypointCombiner("|", true);
                    if (params.length() > 0) {
                        url.append(params);
                    }
                }
//                Log.i(TAG, "url: " + url);
                openMapsApp(Uri.parse(url.toString()));
            }
        });
        clusterManager.setOnClusterClickListener(cluster -> {
            if (cluster == lastCluster && isCentered) {
                customRenderer.setShouldCluster(false);
                clusterManager.cluster();
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(cluster.getPosition()), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    isCentered = true;
                    lastCluster = cluster;
                }

                @Override
                public void onCancel() {}
            });
            return true;
        });
        clusterManager.setOnClusterItemClickListener(clusterItem -> {
            currentMarker = clusterItemMarkerMap.get(currentTable).get(clusterItem);
            if (currentMarker != null) {
                startMarker();
                Log.i(TAG, "triggered setOnClusterItemClickListener");
                Log.i(TAG, "marker: " + currentMarker.getTitle());
            } else {
                Log.i(TAG, "marker is null at setOnClusterItemClickListener");
            }
            return true;
        });

        loadTextView = findViewById(R.id.textview_load);
        loadTextView.setVisibility(View.VISIBLE);

        varMapStr = getIntent().getStringExtra("places");
        varMap = BranchDirectoryMap.gson.fromJson(varMapStr, BranchDirectoryMap.VARMAP_TYPE);
//        Log.i(TAG, "varMap from places: " + varMap);

        task = new MapLoaderTask(this);
        if (!sharedPreferences.getBoolean(BranchDirectoryMap.KEY_APIKEY_LOADED, false)) {
            Log.i(TAG, "fetchGeocodeApiKey started");
            Secrets.fetchGeocodeApiKey(this, keyReceived -> {
                if (keyReceived) {
                    Log.i(TAG, "fetchGeocodeApiKey successful");
                    editor.putBoolean(BranchDirectoryMap.KEY_APIKEY_LOADED, true).apply();
                    task.execute();
                } else {
                    Log.e(TAG, "fetchGeocodeApiKey failed/timed out");
                    dialogUtils.showOkDialog(context, getString(R.string.warning), getString(R.string.key_error),
                            (dialog, id) -> dialog.dismiss());
                    locationPermissionGranted = false;
                    customAdapter.setInterface(locationPermissionGranted, BuildConfig.USE_ADVANCED_ROUTING);
                    loadTextView.setVisibility(View.GONE);
                }
            });
        } else {
            task.execute();
        }

        if (!BuildConfig.USE_MAP_ID) {
            createMapStyles();
        } else {
            Log.i(TAG, "map id used: " + getString(R.string.map_id));
            switchToggler(switchAlt, false);
            switchToggler(switchMono, false);
        }
        mMap.setTrafficEnabled(isTrafficEnabled);
        mMap.setMapType(mapMode + 1);

        runWhenMapReady("setMapStyle", this::setMapStyle);
        for (Runnable r : new ArrayList<>(afterMapReady)) r.run();
        afterMapReady.clear();
        afterMapReadyKeys.clear();

        Log.i(TAG, "reached end of onMapReady");
        clusterManager.cluster();
    }

    private void runWhenMapReady(String key, Runnable r) {
        if (mapReady && mMap != null) {
            r.run();
        } else if (afterMapReadyKeys.add(key)) {
            // only added if key wasn't present
            afterMapReady.add(() -> {
                try { r.run(); }
                finally { afterMapReadyKeys.remove(key); } // clean up after running
            });
        }
    }

    public void performSearch(String query) {
        CustomItem correctSuggestion = null;
        if (query.contains("+")) {      // chosen from suggestions list
            query = query.split("\\+", 2)[1].toLowerCase();
//            Log.i(TAG, "query 1: " + query);
            for (CustomItem suggestion : markers.get(currentTable)) {
                if (suggestion.getSnippet().toLowerCase().equals(query)) {
                    correctSuggestion = suggestion;
//                    Log.i(TAG, "case 1: " + suggestion.getSnippet());
                    break;
                }
            }
        } else {    // chosen based on query
            int filteredSize = filteredSuggestions.get(0).size() + filteredSuggestions.get(1).size()
                    + filteredSuggestions.get(2).size();
            if (filteredSize < 1) {
                Toast.makeText(this, getString(R.string.no_results), Toast.LENGTH_SHORT).show();
                return;
            } else if (filteredSize == 1) {
                if (!filteredSuggestions.get(0).isEmpty()) {
                    correctSuggestion = filteredSuggestions.get(0).get(0);
                } else if (!filteredSuggestions.get(1).isEmpty()) {
                    correctSuggestion = filteredSuggestions.get(1).get(0);
                } else {
                    correctSuggestion = filteredSuggestions.get(2).get(0);
                }
            } else {
                Toast.makeText(this, getString(R.string.many_results), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (correctSuggestion != null) {
            Log.i(TAG, "address found");
            Log.i(TAG, "correctSuggestion: " + correctSuggestion);
            Log.i(TAG, "currentTable: " + currentTable);
            Log.i(TAG, "clusterItemMarkerMap: " + clusterItemMarkerMap);
            currentMarker = clusterItemMarkerMap.get(currentTable).get(correctSuggestion);
            if (currentMarker != null) {
                startMarker();
            } else {
                Log.i(TAG, "marker is null at performSearch");
            }
        } else {
            Log.e(TAG, "---Unknown result: not programmed---");
        }
    }

    public void updateSuggestions(String query) {
        Log.i(TAG, "query: " + query);
        filteredSuggestions.clear();
        // search levels in order of importance: code, code with prefix, name, address
        for (int i = 0; i < BranchDirectoryMap.SEARCH_LEVELS; i++) {
            filteredSuggestions.add(new ArrayList<>());
        }
        if (!query.isEmpty()) {
            query = query.toLowerCase();
            if (currentTable.equals(getString(R.string.header_all))) {
                for (CustomItem suggestion : markers.get(currentTable)) {
                    // match code
                    String code = suggestion.getCode();
                    if (code != null && code.toLowerCase().contains(query)) {
                        filteredSuggestions.get(0).add(suggestion);
                        continue;
                    }
                    // match name
                    String name = suggestion.getName();
                    if (name != null && name.toLowerCase().contains(query)) {
                        filteredSuggestions.get(2).add(suggestion);
                        continue;
                    }
                    // match address
                    String addr = suggestion.getSnippet();
                    if (addr != null && addr.toLowerCase().contains(query)) {
                        filteredSuggestions.get(3).add(suggestion);
                    }
                }
            } else {
                String prefix = ((String) varMap.get(currentTable).get("code_prefix")).toLowerCase(Locale.ROOT);
                String delim  = (String) varMap.get(currentTable).get("code_delimiter");
                final String splitRegex = (delim == null || delim.isEmpty()) ? null : Pattern.quote(delim);
                boolean queryHasPrefix = !prefix.isEmpty() && query.startsWith(prefix);
                String qAfterPrefix = queryHasPrefix ? query.substring(prefix.length()) : "";
                outer:
                for (CustomItem suggestion : markers.get(currentTable)) {
                    String codeRaw = suggestion.getCode();
                    if (codeRaw != null && !codeRaw.isEmpty()) {
                        List<String> tokens = (splitRegex == null)
                                ? Collections.singletonList(codeRaw)
                                : Arrays.asList(codeRaw.split(splitRegex, 0));
                        if (queryHasPrefix && qAfterPrefix.length() > 0) {
                            // match query with prefix
                            for (String t : tokens) {
                                String tl = t == null ? "" : t.trim().toLowerCase(Locale.ROOT);
                                String suffix = tl.startsWith(prefix) ? tl.substring(prefix.length()) : tl;
                                if (suffix.startsWith(qAfterPrefix)) {
                                    filteredSuggestions.get(1).add(suggestion);
                                    continue outer;
                                }
                            }
                        } else {
                            // match query without prefix
                            for (String t : tokens) {
                                String tl = t == null ? "" : t.trim().toLowerCase(Locale.ROOT);
                                String suffix = tl.startsWith(prefix) ? tl.substring(prefix.length()) : tl;
                                if (suffix.contains(query)) {
                                    filteredSuggestions.get(0).add(suggestion);
                                    continue outer;
                                }
                            }
                        }
                    }

                    // match name
                    String name = suggestion.getName();
                    if (name != null && name.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredSuggestions.get(2).add(suggestion);
                        continue; // skip address if name matched
                    }

                    // match address
                    String addr = suggestion.getSnippet();
                    if (addr != null && addr.toLowerCase(Locale.ROOT).contains(query)) {
                        filteredSuggestions.get(3).add(suggestion);
                    }
                }
            }
        } else {    // show all suggestions
            Log.i(TAG, "currentTable: " + currentTable);
            filteredSuggestions.get(0).addAll(markers.get(currentTable));
        }
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "location", "address"});
        for (int i = 0; i < BranchDirectoryMap.SEARCH_LEVELS; i++) {
            for (int j = 0; j < filteredSuggestions.get(i).size(); j++) {
                CustomItem suggestion = filteredSuggestions.get(i).get(j);
                cursor.addRow(new Object[]{j, suggestion.getTitle(), suggestion.getSnippet()});
            }
        }
        Cursor old = suggestionAdapter.swapCursor(cursor);
        if (old != null) old.close();
//        suggestionAdapter.changeCursor(cursor);
    }

    private void applySystemBars() {
        @ColorInt int bg = ContextCompat.getColor(this, R.color.pri_text_low_alt);

        Window window = getWindow();
        View decor = window.getDecorView();

        window.setStatusBarColor(bg);
        if (Build.VERSION.SDK_INT >= 26) {
            window.setNavigationBarColor(bg);
        }

        boolean lightBackground = isColorLight(bg);
        WindowInsetsControllerCompat wic = new WindowInsetsControllerCompat(window, decor);
        wic.setAppearanceLightStatusBars(lightBackground);
        if (Build.VERSION.SDK_INT >= 26) {
            wic.setAppearanceLightNavigationBars(lightBackground);
        }
    }

    private boolean isColorLight(@ColorInt int c) {
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        double lum = (0.299*r + 0.587*g + 0.114*b) / 255d;
        return lum >= 0.5;
    }

    private void startAmbientMonitoring() {
        stopAmbientMonitoring();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = (sensorManager != null) ? sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) : null;
        float darkOn, darkOff;
        if (BranchDirectoryMap.LUX_OVERRIDES) {
            darkOn = BranchDirectoryMap.LUX_DARK_ON;
            darkOff = BranchDirectoryMap.LUX_DARK_OFF;
        } else {
            float max = (lightSensor != null ? lightSensor.getMaximumRange() : 40000f);
            darkOn = Math.max(2f, 0.002f * max);
            darkOff = darkOn * 2.0f;
            Log.i(TAG, "lux max: " + max + " darkOn: " + darkOn + " darkOff: " + darkOff);
        }

        if (lightSensor != null) {
            lightListener = new SensorEventListener() {
                @Override public void onSensorChanged(SensorEvent event) {
                    lastLux = event.values[0];
                }
                @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };
            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e(TAG, "No ambient light sensor");
            lastLux = Float.NaN;
        }

        ambientExecutor = Executors.newSingleThreadScheduledExecutor();
        ambientFuture = ambientExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (!isAmbientEnabled) return;

                float lux = lastLux;

                Log.i(TAG, "lux: " + lux);

                if (Float.isNaN(lux)) return;

                boolean shouldDark;
                if (lastAppliedDark == null) {
                    shouldDark = (lux < (darkOn + darkOff) / 2f);
                } else if (lastAppliedDark) {   // currently dark
                    shouldDark = (lux < darkOff);
                } else {    // currently light
                    shouldDark = (lux < darkOn);
                }

                if (lastAppliedDark == null || shouldDark != lastAppliedDark) {
                    lastAppliedDark = shouldDark;
                    runOnUiThread(() -> {
                        searchView.setOnQueryTextListener(null);
                        suppressQuery = true;
                        isDarkEnabled = shouldDark;
                        AppCompatDelegate.setDefaultNightMode(
                                shouldDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                        );
                        getDelegate().applyDayNight();
                        applySystemBars();
                        setMapStyle();
                        searchView.setOnQueryTextListener(queryListener);
                        suppressQuery = false;
                    });
                }
            } catch (Throwable t) {
                Log.e(TAG, "Ambient monitor tick failed", t);
            }
        }, 0, BranchDirectoryMap.LUX_PING_SECONDS, TimeUnit.SECONDS);
    }

    private void stopAmbientMonitoring() {
        if (sensorManager != null && lightListener != null) {
            sensorManager.unregisterListener(lightListener);
        }
        lightListener = null;
        lightSensor = null;

        if (ambientFuture != null) {
            ambientFuture.cancel(true);
            ambientFuture = null;
        }
        if (ambientExecutor != null) {
            ambientExecutor.shutdownNow();
            ambientExecutor = null;
        }
        lastAppliedDark = null;
        lastLux = Float.NaN;
    }

    public void updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public void openMapsApp(Uri uri) {
        if (intermediates_count > BranchDirectoryMap.MAX_INTERMEDIATES_EXT) {
            Toast.makeText(this, getString(R.string.max_intermediates_ext), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.gm_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public void moveButtonsWithMarker(Point screenPosition) {
        int x = screenPosition.x - markerButtonsLayout.getWidth() / 2;
        int y = screenPosition.y + 20;

        markerButtonsLayout.setX(x);
        markerButtonsLayout.setY(y);

        markerButtonsLayout.setVisibility(View.VISIBLE);
    }

    public void setMapStyle() {
        Log.i(TAG, "setMapStyle");
        if (isAltEnabled) {
            int styleKey = isDarkEnabled ? 5 : 4; // 5 = dark alt, 4 = light alt
            mMap.setMapStyle(styles.get(stylesList.get(styleKey)));
            clearMap();
            return;
        }
        int styleKey = (isDarkEnabled ? 1 : 0) + (isMonoEnabled ? 2 : 0);
        switch (styleKey) {
            case 0:     // light
                mMap.setMapStyle(styles.get(stylesList.get(0)));
                break;
            case 1:     // dark
                mMap.setMapStyle(styles.get(stylesList.get(1)));
                break;
            case 2:     // light mono
                mMap.setMapStyle(styles.get(stylesList.get(2)));
                switchAlt.setChecked(false);
                break;
            case 3:     // dark mono
                mMap.setMapStyle(styles.get(stylesList.get(3)));
                switchAlt.setChecked(false);
                break;
            default:
                Log.e(TAG, "invalid styleKey in setMapStyle");
                break;
        }
        clearMap();
    }

    public void createMapStyles() {
        Log.i(TAG, "createMapStyles");
        String[] stylesStr = BuildConfig.STYLE_JSON.split(",");
        stylesList = new ArrayList<>();
        styles = new HashMap<>();
        for (String style : stylesStr) {
            try {
                String styleStr = style.replace(".json", "");
                int resId = getResources().getIdentifier(styleStr, "raw", getPackageName());
                InputStream styleStream = getResources().openRawResource(resId);
                String styleJson = new Scanner(styleStream).useDelimiter("\\A").next();
                stylesList.add(styleStr);
                styles.put(styleStr, new MapStyleOptions(styleJson));
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style: ", e);
            }
        }
//        Log.i(TAG, "styles: " + styles.toString());
    }

    public void setAppTheme(int theme, boolean setStyleToo) {
        if (setStyleToo) {
            searchView.setOnQueryTextListener(null);
        }
        suppressQuery = true;
        switch (theme) {
            case 0:     // system (default)
                isAmbientEnabled = false;
                stopAmbientMonitoring();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                getDelegate().applyDayNight();
                int nightMask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                isDarkEnabled = (nightMask == Configuration.UI_MODE_NIGHT_YES);
                applySystemBars();
                break;
            case 1:     // light
                isAmbientEnabled = false;
                stopAmbientMonitoring();
                isDarkEnabled = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().applyDayNight();
                applySystemBars();
                break;
            case 2:     // dark
                isAmbientEnabled = false;
                stopAmbientMonitoring();
                isDarkEnabled = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().applyDayNight();
                applySystemBars();
                break;
            case 3:     // ambient light sensor
                isAmbientEnabled = true;
                startAmbientMonitoring();
                break;
            default:
                Log.e(TAG, "unknown theme in themeSpinner");
        }
        if (setStyleToo) {
            setMapStyle();
            searchView.setOnQueryTextListener(queryListener);
        }
        suppressQuery = false;
    }

    private void restyleAfterThemeChange() {
        Log.i(TAG, "restyleAfterThemeChange");

        @ColorInt int fg = ContextCompat.getColor(this, R.color.pri_text_high);
        @ColorInt int bg = ContextCompat.getColor(this, R.color.pri_text_low_alt);
        ColorStateList bgList = ColorStateList.valueOf(bg);
        ColorStateList fgList = ColorStateList.valueOf(fg);
        Drawable popupBg = new ColorDrawable(ContextCompat.getColor(this, R.color.pri_text_medium_alt));

        searchViewLayout.setBackgroundColor(bg);
        int iconRes = isMenuActive ? R.drawable.ic_close : R.drawable.ic_menu;
        Drawable icon = AppCompatResources.getDrawable(this, iconRes);
        icon = icon.mutate();
        DrawableCompat.setTint(icon, fg);
        menuButton.setImageDrawable(icon);

        AutoCompleteTextView acText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (acText != null) {
            acText.setDropDownBackgroundDrawable(popupBg);
            acText.setTextColor(fg);
            acText.setHintTextColor(adjustAlpha(fg, 0.2f));
        }

        if (suggestionAdapter != null) {
            final int mg = ContextCompat.getColor(this, R.color.pri_text_medium);
            suggestionAdapter.setViewBinder((view, cursor, columnIndex) -> {
                int id = view.getId();
                if (id == R.id.suggestion_text) {
                    ((TextView) view).setTextColor(fg);
                    return false;
                } else if (id == R.id.suggestion_description) {
                    ((TextView) view).setTextColor(mg);
                    return false;
                }
                return false;
            });
            suggestionAdapter.notifyDataSetChanged();
        }

        ImageView btn = searchView.findViewById(androidx.appcompat.R.id.search_button);
        ImageView mag = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        ImageView close = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (btn != null) ImageViewCompat.setImageTintList(btn, fgList);
        if (mag != null) ImageViewCompat.setImageTintList(mag, fgList);
        if (close != null) ImageViewCompat.setImageTintList(close, fgList);

        searchSpinner.setPopupBackgroundDrawable(popupBg.getConstantState().newDrawable());
        ViewCompat.setBackgroundTintList(searchSpinner, fgList);
        SpinnerAdapter base = searchSpinner.getAdapter();
        if (base instanceof SearchSpinnerAdapter) {
            ((SearchSpinnerAdapter) base).updateTheme();
        }

        themeSpinner.setPopupBackgroundDrawable(popupBg.getConstantState().newDrawable());
        trafficSpinner.setPopupBackgroundDrawable(popupBg.getConstantState().newDrawable());
        appearanceSpinner.setPopupBackgroundDrawable(popupBg.getConstantState().newDrawable());

        ViewCompat.setBackgroundTintList(themeSpinner, fgList);
        ViewCompat.setBackgroundTintList(trafficSpinner, fgList);
        ViewCompat.setBackgroundTintList(appearanceSpinner, fgList);

        rethemeSpinner(themeSpinner);
        forceSelectedTextColor(themeSpinner, fg);

        rebuildSpinnerAdapter(trafficSpinner, BuildConfig.USE_ADVANCED_ROUTING
                        ? R.array.traffic_adv_dropdown : R.array.traffic_dropdown,
                R.layout.spinner_selected_item, android.R.layout.simple_spinner_dropdown_item);
        rebuildSpinnerAdapter(appearanceSpinner, R.array.appearance_dropdown,
                R.layout.spinner_selected_item, android.R.layout.simple_spinner_dropdown_item);

        styleFab(directionsRouteButton, bgList, fgList);
        styleFab(clearRouteButton, bgList, fgList);
        styleFab(routeButton, bgList, fgList);
        styleFab(layersButton, bgList, fgList);

        styleMenuPanel(routeMenu, bg, fg, fgList);
        styleMenuPanel(layersMenu, bg, fg, fgList);

        tintMaterialSwitch(switchFerries, fg, bg);
        tintMaterialSwitch(switchTolls, fg, bg);
        tintMaterialSwitch(switchHighways, fg, bg);
        tintMaterialSwitch(switchTraffic, fg, bg);
        tintMaterialSwitch(switchAlt, fg, bg);
        tintMaterialSwitch(switchMono, fg, bg);
    }

    private void styleFab(FloatingActionButton fab, ColorStateList bg, ColorStateList fg) {
        fab.setBackgroundTintList(bg);
        fab.setImageTintList(fg);
        fab.refreshDrawableState();
    }

    private void styleMenuPanel(LinearLayout panel, @ColorInt int background, @ColorInt int text, ColorStateList iconTint) {
        ViewCompat.setBackgroundTintList(panel, ColorStateList.valueOf(background));
        ViewCompat.setBackgroundTintMode(panel, PorterDuff.Mode.SRC_IN);
        tintChildrenForeground(panel, text);
    }

    private void tintChildrenForeground(View root, @ColorInt int textColor) {
        if (!(root instanceof ViewGroup)) return;
        ViewGroup vg = (ViewGroup) root;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof TextView && !(child instanceof CompoundButton)) {
                ((TextView) child).setTextColor(textColor);
            }
            if (child instanceof ViewGroup) {
                tintChildrenForeground(child, textColor);
            }
        }
    }

    private void rethemeSpinner(Spinner sp) {
        SpinnerAdapter a = sp.getAdapter();
        if (a instanceof ThemedSpinnerAdapter) {
            ThemedSpinnerAdapter ta = (ThemedSpinnerAdapter) a;
            ta.setDropDownViewTheme(getTheme());
        }
        if (a instanceof BaseAdapter) {
            ((BaseAdapter) a).notifyDataSetChanged();
        }
    }

    private void forceSelectedTextColor(Spinner sp, @ColorInt int color) {
        View v = sp.getSelectedView();
        if (v instanceof TextView) ((TextView) v).setTextColor(color);
        sp.post(() -> {
            View v2 = sp.getSelectedView();
            if (v2 instanceof TextView) ((TextView) v2).setTextColor(color);
        });
    }

    private void rebuildSpinnerAdapter(Spinner sp, @ArrayRes int entries, @LayoutRes int itemLayout, @LayoutRes int ddLayout) {
        int sel = sp.getSelectedItemPosition();
        ArrayAdapter<CharSequence> a =
                ArrayAdapter.createFromResource(this, entries, itemLayout);
        a.setDropDownViewResource(ddLayout);
        sp.setAdapter(a);
        if (sel >= 0 && sel < a.getCount()) sp.setSelection(sel, false);
    }

    private void tintMaterialSwitch(MaterialSwitch sw, @ColorInt int fg, @ColorInt int bg) {
        if (sw == null) return;
        final int[][] states = new int[][]{
                new int[]{ android.R.attr.state_enabled,  android.R.attr.state_checked },
                new int[]{ android.R.attr.state_enabled, -android.R.attr.state_checked },
                new int[]{-android.R.attr.state_enabled,  android.R.attr.state_checked },
                new int[]{-android.R.attr.state_enabled, -android.R.attr.state_checked }
        };
        final int[] thumbColors = new int[]{
                fg,
                adjustAlpha(fg, 0.54f),
                adjustAlpha(fg, 0.38f),
                adjustAlpha(fg, 0.24f)
        };
        final int[] trackColors = new int[]{
                adjustAlpha(fg, 0.32f),
                adjustAlpha(fg, 0.18f),
                adjustAlpha(fg, 0.20f),
                adjustAlpha(fg, 0.12f)
        };
        sw.setThumbTintList(new ColorStateList(states, thumbColors));
        sw.setTrackTintList(new ColorStateList(states, trackColors));
        sw.setTextColor(fg);
    }

    private static int adjustAlpha(int color, float alphaFraction) {
        int a = Math.round(Color.alpha(color) * alphaFraction);
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }

    public void menuToggler() {
        int iconRes = isMenuActive ? R.drawable.ic_menu : R.drawable.ic_close;
        Drawable icon = AppCompatResources.getDrawable(this, iconRes);
        icon = icon.mutate();
        int color = ContextCompat.getColor(this, R.color.pri_text_high);
        DrawableCompat.setTint(icon, color);
        menuButton.setImageDrawable(icon);
        if (!isMenuActive) {
            routeButton.setVisibility(View.VISIBLE);
            layersButton.setVisibility(View.VISIBLE);
            Log.i(TAG, "clearfocus 2");
            searchView.clearFocus();
            isMenuActive = true;
        } else {
            routeButton.setVisibility(View.GONE);
            layersButton.setVisibility(View.GONE);
            routeMenu.setVisibility(View.GONE);
            layersMenu.setVisibility(View.GONE);
            isMenuActive = false;
        }
        moveRouteButtons();
    }

    public void switchToggler(MaterialSwitch switchButton, boolean enabled) {
        if (enabled) {
            switchButton.setEnabled(true);
            switchButton.setTextColor(ContextCompat.getColor(this, R.color.pri_text_high));
        } else {
            switchButton.setEnabled(false);
            switchButton.setTextColor(ContextCompat.getColor(this, R.color.pri_text_medium));
        }
    }

    public StringBuilder waypointCombiner(String delimiter, boolean cutbody) {
        // cutbody is for app calls to restrict all marker waypoints to last one
        StringBuilder waypointsBuilder = new StringBuilder();
        Iterator<String> keyIterator = routeMarkers.keySet().iterator();
        // iterate through routeMarkers
        while (keyIterator.hasNext()) {
            String title = keyIterator.next();
            CustomItem thisItem = (CustomItem) routeMarkers.get(title).get(1);
            // no waypoints
            if (thisItem.getWaypoints().isEmpty()) {
                Marker thisMarker = (Marker) routeMarkers.get(title).get(0);
                if (!keyIterator.hasNext() && thisMarker.equals(currentMarker)) {
                    Log.i(TAG, "last routeMarker is currentMarker");
                    break;
                }
                if (waypointsBuilder.length() > 0) {
                    waypointsBuilder.append(delimiter);
                }
                LatLng waypointPosition = thisMarker.getPosition();
                waypointsBuilder.append(waypointPosition.latitude).append(",")
                        .append(waypointPosition.longitude);
            // waypoints exist
            } else {
                Iterator<String> waypointIterator;
                if (cutbody) {
                    waypointIterator = Collections.singletonList(thisItem.getLastWaypoint()).iterator();
                } else {
                    waypointIterator = thisItem.getWaypoints().keySet().iterator();
                }
                while (waypointIterator.hasNext()) {
                    String waypoint = waypointIterator.next();
                    if (waypointsBuilder.length() > 0) {
                        waypointsBuilder.append(delimiter);
                    }
                    // last waypoint treated as a stop
                    if (waypointIterator.hasNext() && !cutbody) {
                        waypointsBuilder.append("via:");
                    }
                    LatLng position = thisItem.getWaypoints().get(waypoint);
                    waypointsBuilder.append(position.latitude).append(",")
                            .append(position.longitude);
                }
            }
        }
        // iterate through currentItem waypoints excluding last one which is destination
        if (!currentItem.getWaypoints().isEmpty()) {
            List<String> waypoints = new ArrayList<>(currentItem.getWaypoints().keySet());
            if (cutbody) {
                waypoints.subList(0, waypoints.size() - 1).clear();
            }
            if (!waypoints.isEmpty()) {
                waypoints.remove(waypoints.size() - 1);
            }
            for (String waypoint : waypoints) {
                if (waypointsBuilder.length() > 0) {
                    waypointsBuilder.append("|");
                }
                LatLng position = currentItem.getWaypoints().get(waypoint);
                waypointsBuilder.append("via:").append(position.latitude).append(",")
                        .append(position.longitude);
            }
        }
        if (waypointsBuilder.length() > 0) {
            waypointsBuilder.insert(0,"&waypoints=");
        }
        return waypointsBuilder;
    }

    public void getCurrentLocation(LocationUpdateAction callback) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        lastKnownLocation = location;
                        callback.LocationUpdateOnDemand();
                        break;
                    }
                }
            }, null);
        }
    }

    public void centerOnUser() {
        if (!isCentered) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude()), 15f));
            isCentered = true;
        }
    }

    private static <K, V> K getLastKey (Map<K, V> map) {
        K last = null;
        for (K e : map.keySet()) {
            last = e;
        }
        return last;
    }

    public void startMarker() {
        Log.i(TAG, "startMarker");
        if (currentMarker == null) {
            Log.i(TAG, "marker is null at startMarker");
            return;
        }
        currentItem = getKeyByValue(clusterItemMarkerMap.get(currentTable), currentMarker);
        clearMenus("");
        customAdapter.setInfoText("Loading...");
        if (BuildConfig.USE_ADVANCED_ROUTING) {
            customAdapter.setTrafficText(new SpannableString("Loading..."));
        }
        redrawInfoWindow();
        if ((BuildConfig.SHOW_ALL_HEADER || (Boolean) varMap.get(currentTable).get("use_phone")) && !currentItem.getPhone().isEmpty()) {
            callMarkerButton.setVisibility(View.VISIBLE);
        } else {
            callMarkerButton.setVisibility(View.GONE);
        }
        if (currentItem.getSelected() > -1) {
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
        } else {
            addMarkerButton.setVisibility(View.VISIBLE);
            removeMarkerButton.setVisibility(View.GONE);
        }
        if (locationPermissionGranted) {
//            Log.i(TAG, "currentItem: " + BranchDirectoryMap.gson.toJson(currentItem, CustomItem.class));
            Map<String, LatLng> waypoints = currentItem.getWaypoints();
            if (!waypoints.isEmpty()) {
                for (String waypoint : waypoints.keySet()) {
//                    Log.i(TAG, "waypoint: " + waypoint);
//                    Log.i(TAG, "name: " + currentItem.getTitle());
//                    Log.i(TAG, "getWaypoints: " + currentItem.getWaypoints());
//                    Log.i(TAG, "getPositions: " + currentItem.getPositions());
                    Marker pos = mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoint))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                    waypointList.add(pos);
                }
            }
            getCurrentLocation(this::GetInformationTaskCreator);
        } else {
            addMarkerButton.setVisibility(View.GONE);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.i(TAG, "animateCamera onFinish");
                Projection projection = mMap.getProjection();
                if (currentMarker == null) {
                    Log.i(TAG, "currentMarker is null at animateCamera onFinish");
                    currentMarker = tempMarker;
                }
                Point screenPosition = projection.toScreenLocation(currentMarker.getPosition());
                moveButtonsWithMarker(screenPosition);
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "animateCamera onCancel");
                markerButtonsLayout.setVisibility(View.GONE);
            }
        });
    }

    public void addMarkerToRoute() {
        Log.i(TAG, "addMarkerToRoute");
        if (routeMarkers.size() < local_intermediates && intermediates_count < local_intermediates) {
            routeMarkers.put(currentMarker.getTitle(), new ArrayList<>());
            routeMarkers.get(currentMarker.getTitle()).add(currentMarker);
            routeMarkers.get(currentMarker.getTitle()).add(currentItem);
            Map<String, LatLng> waypoints = currentItem.getWaypoints();
            if (waypoints.isEmpty()) {
                routeMarkers.get(currentMarker.getTitle()).add(new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                intermediates_count++;
            } else {
                for (String waypoint : waypoints.keySet()) {
                    routeMarkers.get(currentMarker.getTitle()).add(waypoints.get(waypoint));
                    intermediates_count++;
                }
            }
            currentItem.setSelected(0);
            lastSelected = true;
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
            moveRouteButtons();
            customRenderer.setShouldCluster(false);
            clusterManager.cluster();
        } else {
            Toast.makeText(this, getString(R.string.max_intermediates), Toast.LENGTH_SHORT).show();
        }
    }

    public void removeMarkerFromRoute() {
        Log.i(TAG, "removeMarkerFromRoute");
        routeMarkers.remove(currentMarker.getTitle());
        intermediates_count--;
        currentItem.setSelected(-1);
        lastSelected = false;
        addMarkerButton.setVisibility(View.VISIBLE);
        removeMarkerButton.setVisibility(View.GONE);
        if (routeMarkers.isEmpty()) {
            moveRouteButtons();
            customRenderer.setShouldCluster(true);
        } else {
            getCurrentLocation(this::GetInformationTaskCreator);
            redrawInfoWindow();
        }
        clusterManager.cluster();
    }

    // requires unique values
    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if(Objects.equals(value, entry.getValue())) {
//                Log.i(TAG, "getKeyByValue: " + entry.getKey());
                return entry.getKey();
            }
        }
        Log.i(TAG, "getKeyByValue: value not found");
        return null;
    }

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.i(TAG, "handleOnBackPressed");

            if (closeImeIfVisible()) return;
            if (dismissSearchSuggestions()) return;
            if (clearSearchFocusIfNeeded()) return;

            if (isInfoWindowOpen) {
                clearInfoWindow();
                return;
            }
            if (routeMenu.getVisibility() == View.VISIBLE || layersMenu.getVisibility() == View.VISIBLE) {
                clearMenus("");
                return;
            }
            if (isMenuActive) {
                menuToggler();
                return;
            }

            if (doubleBackToExitPressedOnce) {
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                if (task != null) task.cancel(true);
                finishAffinity();
                return;
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(MapsActivity.this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> doubleBackToExitPressedOnce = false, 2000
            );
        }
    };

    private boolean closeImeIfVisible() {
        View root = getWindow().getDecorView();
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
        boolean imeVisible = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
        Log.i(TAG, "closeImeIfVisible: " + imeVisible);
        if (!imeVisible) return false;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v == null) v = root;
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return true;
    }

    private boolean dismissSearchSuggestions() {
        if (searchView == null) return false;
        AutoCompleteTextView ac = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (ac != null && ac.isPopupShowing()) {
            ac.dismissDropDown();
            return true;
        }
        return false;
    }

    private boolean clearSearchFocusIfNeeded() {
        if (searchView != null && searchView.hasFocus()) {
            searchView.clearFocus();
            return true;
        }
        return false;
    }


    public void moveRouteButtons() {
        if (!routeMarkers.isEmpty()) {
//            Log.i(TAG, "moveRouteButtons 1");
            clearRouteButtonParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            clearRouteButtonParams.removeRule(RelativeLayout.ABOVE);
            if (isMenuActive && layersMenu.getVisibility() == View.VISIBLE) {
//                Log.i(TAG, "moveRouteButtons a");
                clearRouteButtonParams.addRule(RelativeLayout.ABOVE, R.id.menu_layers);
                clearRouteButton.requestLayout();
                directionsRouteButton.requestLayout();
            } else if (isMenuActive) {
//                Log.i(TAG, "moveRouteButtons b");
                clearRouteButtonParams.addRule(RelativeLayout.ABOVE, R.id.button_layers);
                clearRouteButton.requestLayout();
                directionsRouteButton.requestLayout();
            } else {
//                Log.i(TAG, "moveRouteButtons c");
                clearRouteButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                clearRouteButton.requestLayout();
                directionsRouteButton.requestLayout();
            }
            directionsRouteButton.setVisibility(View.VISIBLE);
            clearRouteButton.setVisibility(View.VISIBLE);
        } else {
//            Log.i(TAG, "moveRouteButtons 2");
            directionsRouteButton.setVisibility(View.GONE);
            clearRouteButton.setVisibility(View.GONE);
        }
    }

    public void clearMenus(String side) {
        Log.i(TAG, "clearMenus side: " + side);
        switch (side) {
            case "L":
                hideMenu(routeMenu, routeButton);
                break;
            case "R":
                hideMenu(layersMenu, layersButton);
                break;
            default:
                hideMenu(routeMenu, routeButton);
                hideMenu(layersMenu, layersButton);
                break;
        }
    }

    public void hideMenu(View menu, View button) {
        if (menu.getVisibility() == View.VISIBLE) {
            menu.startAnimation(animDisappear);
            menu.setVisibility(View.INVISIBLE);
            buttonToChange = button;
            menuToChange = menu;
        }
    }

    public void clearMap() {
        Log.i(TAG, "clearMap");
        clearPolys();
//        for (Marker marker : clusterManager.getMarkerCollection().getMarkers()) {
//            marker.hideInfoWindow();
//        }
        if (currentMarker != null) {
            currentMarker.hideInfoWindow();
            currentMarker = null;
            if (!waypointList.isEmpty()) {
                for (Marker marker : waypointList) {
                    marker.remove();
                }
                waypointList.clear();
            }
        }
        markerButtonsLayout.setVisibility(View.GONE);
    }

    public void clearInfoWindow() {
        Log.i(TAG, "clearInfoWindow");
        clearMap();
        if (getInformationTask != null) {
            getInformationTask.cancel(true);
        }
    }

    public void redrawInfoWindow() {
        Log.i(TAG, "redrawInfoWindow");
        isInfoWindowRedraw = true;
        currentMarker.hideInfoWindow();
        currentMarker.showInfoWindow();
    }

    public void clearRoute() {
        Log.i(TAG, "clearRoute");
        clearMap();
        for (String title : routeMarkers.keySet()) {
            CustomItem item = (CustomItem) routeMarkers.get(title).get(1);
            item.setSelected(-1);
        }
        routeMarkers.clear();
        intermediates_count = 0;
        moveRouteButtons();
        addMarkerButton.setVisibility(View.VISIBLE);
        removeMarkerButton.setVisibility(View.GONE);
        customRenderer.setShouldCluster(true);
        clusterManager.cluster();
        startMarker();
    }

    public void clearPolys() {
        Log.i(TAG, "clearPolys");
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    public void createPolys() {
        clearPolys();
        List<LatLng> decodedPolyline = new ArrayList<>();
        for (List<String> legs : encodedPolyline) {
            for (String steps : legs) {
                decodedPolyline.addAll(PolyUtil.decode(steps));
            }
        }
        polylines.add(mMap.addPolyline(new PolylineOptions().addAll(decodedPolyline)
                .color(Color.CYAN).width(20f).zIndex(1000)));
        polylines.add(mMap.addPolyline(new PolylineOptions().addAll(decodedPolyline)
                .color(isDarkEnabled ? Color.WHITE : Color.BLACK).width(24f).zIndex(900)));
    }

    @Override
    public void onInfoWindowOpened() {
        Log.i(TAG, "onInfoWindowOpened");
        isInfoWindowOpen = true;
    }

    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {
        Log.i(TAG, "onInfoWindowClose");
        if (!isInfoWindowRedraw) {
            Log.i(TAG, "isInfoWindowRedraw is false");
            if (getInformationTask != null) {
                getInformationTask.cancel(true);
            }
            clearPolys();
            markerButtonsLayout.setVisibility(View.GONE);
            lastSelected = currentItem.getSelected() > -1;
            if (searchView.getQuery().length() != 0) {
                searchView.setQuery("", false);
                Log.i(TAG, "clearfocus 3");
                searchView.clearFocus();
            }
            tempMarker = currentMarker;
            currentMarker = null;
            if (!waypointList.isEmpty()) {
                for (Marker waypointMarker : waypointList) {
                    waypointMarker.remove();
                }
                waypointList.clear();
            }
            isInfoWindowOpen = false;
            customRenderer.setShouldCluster(true);
        }
        isInfoWindowRedraw = false;
    }

    public int getMapSize(Map<String, List<CustomItem>> map) {
        int markersSize = 0;
        for (String table : tables) {
            if (map.containsKey(table)) {
                markersSize += map.get(table).size();
            }
        }
        return markersSize;
    }

    public int getMapSize3D(Map<String, Map<String, Map<String, LatLng>>> map) {
        int markersSize = 0;
        for (String table : map.keySet()) {
            for (String name : map.get(table).keySet()) {
                markersSize += map.get(table).get(name).size();
            }
        }
        return markersSize;
    }

    public boolean mapHasInnerElements(Map<String, Map<String, List<String>>> map, boolean deeper) {
        for (Map<String, List<String>> outermap : map.values()) {
            if (!deeper && !outermap.isEmpty()) {
                return true;
            } else if (deeper) {
                for (List<String> innerlist : outermap.values()) {
                    if (!innerlist.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Map<String, Map<String, List<String>>> deepCopy3d(Map<String, Map<String, List<String>>> original) {
        Map<String, Map<String, List<String>>> copy = new HashMap<>();
        for (Map.Entry<String, Map<String, List<String>>> entry : original.entrySet()) {
            String outerKey = entry.getKey();
            Map<String, List<String>> innerMap = entry.getValue();
            Map<String, List<String>> innerMapCopy = new HashMap<>();
            for (Map.Entry<String, List<String>> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                List<String> list = innerEntry.getValue();
                List<String> listCopy = new ArrayList<>(list);
                innerMapCopy.put(innerKey, listCopy);
            }
            copy.put(outerKey, innerMapCopy);
        }
        return copy;
    }

    public static List<List<String>> deepCopy2d(List<List<String>> original) {
        List<List<String>> copy = new ArrayList<>(original.size());
        for (List<String> innerList : original) {
            copy.add(new ArrayList<>(innerList));
        }
        return copy;
    }

    public static LinkedHashMap<String, ArrayList<Object>> deepCopyMap(Map<String, ArrayList<Object>> src) {
        LinkedHashMap<String, ArrayList<Object>> out = new LinkedHashMap<>(src.size());
        for (Map.Entry<String, ArrayList<Object>> e : src.entrySet()) {
            ArrayList<Object> v = e.getValue();
            out.put(e.getKey(), v == null ? null : new ArrayList<>(v));
        }
        return out;
    }

    private void showAvoidanceViolationToast(int violated) {
        if (violated == 0) return;

        List<String> parts = new ArrayList<>(3);
        if ((violated & VIOLATION_TOLLS) != 0)    parts.add(getString(R.string.tolls));
        if ((violated & VIOLATION_FERRIES) != 0)  parts.add(getString(R.string.ferries));
        if ((violated & VIOLATION_HIGHWAYS) != 0) parts.add(getString(R.string.highways));

        String msg;
        if (parts.size() == 1) {
            msg = getString(R.string.couldnt_avoid_one, parts.get(0));
        } else if (parts.size() == 2) {
            msg = getString(R.string.couldnt_avoid_two, parts.get(0), parts.get(1));
        } else {
            msg = getString(R.string.couldnt_avoid_three, parts.get(0), parts.get(1), parts.get(2));
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // does not work, FIX THIS
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    // does not work, FIX THIS
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        getDelegate().applyDayNight();
        invalidateOptionsMenu();
        applySystemBars();
        runWhenMapReady("restyleAfterThemeChange", this::restyleAfterThemeChange);
        runWhenMapReady("setMapStyle", this::setMapStyle);
    }

    public class CustomClusterRenderer extends DefaultClusterRenderer<CustomItem> {

        private boolean shouldCluster = true;

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<CustomItem> clusterManager) {
            super(context, map, clusterManager);
//            setMinClusterSize(BuildConfig.MIN_CLUSTER_SIZE);
        }

        @Override
        protected void onBeforeClusterItemRendered(CustomItem item, MarkerOptions markerOptions) {
//            Log.i(TAG, "onBeforeClusterItemRendered");
            markerOptions.title(item.getTitle()).snippet(item.getSnippet());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(item.getHue()));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onClusterItemRendered(CustomItem item, Marker marker) {
//            Log.i(TAG, "triggered onClusterItemRendered");
            clusterItemMarkerMap.get(currentTable).put(item, marker);
            marker.setTag(item);
            super.onClusterItemRendered(item, marker);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull CustomItem item, @NonNull Marker marker) {
//            Log.i(TAG, "onClusterItemUpdated");
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(item.getHue()));
            super.onClusterItemUpdated(item, marker);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<CustomItem> cluster) {
            if (shouldCluster) {
                return cluster.getSize() >= BuildConfig.MIN_CLUSTER_SIZE;
            } else {
                return false;
            }
        }

        public void setShouldCluster(boolean shouldCluster) {
            this.shouldCluster = shouldCluster;
        }
    }

    public abstract class MapMerger<K, V> {

        public Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
            Set<K> allKeys = new LinkedHashSet<>(map1.keySet());
            allKeys.addAll(map2.keySet());
            Map<K, V> merged = new LinkedHashMap<>();
            for (K key : allKeys) {
                V v1 = map1.get(key);
                V v2 = map2.get(key);
                merged.put(key, mergeValues(v1, v2));
            }
            return merged;
        }

        protected abstract V mergeValues(V v1, V v2);
    }

    public class LatLngMerger extends MapMerger<String, LatLng> {

        @Override
        protected LatLng mergeValues(LatLng v1, LatLng v2) {
            return v1 != null ? v1 : v2;
        }
    }

    public class CustomItemListMerger extends MapMerger<String, List<CustomItem>> {

        @Override
        protected List<CustomItem> mergeValues(List<CustomItem> list1, List<CustomItem> list2) {
            Set<CustomItem> combined = new LinkedHashSet<>();
            if (list1 != null) combined.addAll(list1);
            if (list2 != null) combined.addAll(list2);
            return new ArrayList<>(combined);
        }
    }

    public class MapLoaderTask extends AsyncTask<ArrayList<String>, Void, Void> {

        private final Context context;
        private final boolean databaseExists;
        private Map<String, List<ContentValues>> values;
        private CountDownLatch latch;
        private int latchCount = 0;
        private Map<String, Map<String, List<String>>> geocoderMaps = new HashMap<>();
        private Map<String, Map<String, List<String>>> missingMaps = new HashMap<>();
        private Map<String, Map<String, List<String>>> waypointMaps = new HashMap<>();
        private Map<String, Map<String, List<String>>> missingWaypointMaps = new HashMap<>();
        private Map<String, Map<String, Map<String, LatLng>>> waypointValues = new HashMap<>();
        private final LatLngTracker tracker = new LatLngTracker();

        public MapLoaderTask(Context context) {
            this.context = context;
            databaseExists = LocationDatabaseHelper.DatabaseChecker.isDatabaseExistsAndPopulated(context, BuildConfig.DATABASE_NAME);
//            Log.i(TAG, "databaseExists: " + databaseExists);
        }

        @Override
        protected Void doInBackground(ArrayList<String>... places) {
            start = System.currentTimeMillis();
            markers = new HashMap<>();
            db = dbHelper.getWritableDatabase();
            values = new HashMap<>();

            if (sharedPreferences.contains(BranchDirectoryMap.KEY_LOAD_FINISHED)) {
                Log.i(TAG, "KEY_LOAD_FINISHED: " + sharedPreferences.getBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false));
            }

            Random random = new Random();
            GeocodingService service = RetrofitClient.getClient(BranchDirectoryMap.BASE_URL).create(GeocodingService.class);
//            ExecutorService executor = Executors.newSingleThreadExecutor();
            ExecutorService executor = Executors.newFixedThreadPool(BuildConfig.MAX_THREADS);

            if (varMap != null && !varMap.isEmpty()) {
                Log.i(TAG, "varmap passed, reading");
                for (String table : varMap.keySet()) {
                    Map<String, List<String>> geocoderMap = new HashMap<>();
                    Map<String, List<String>> waypointMap = new HashMap<>();
                    ArrayList<String> place = (ArrayList<String>) varMap.get(table).get("array");
                    Log.i(TAG, "tablesSet: " + tablesSet);
                    Log.i(TAG, "table: " + table);
                    if (tablesSet.add(table)) {
                        tables.add(table);
                    } else {
                        Log.i(TAG, "table already added: " + table);
                    }
//                    Log.i(TAG, "place: " + place.toString());
                    for (int i = 0; i < place.size(); i += BranchDirectoryMap.NUM_OF_CUSTOMITEM_VARS) {
                        if ((Boolean) varMap.get(table).get("use_refined")) {
                            // contains a plus code
                            if (place.get(i + 2).contains("+")) {
                                if (place.get(i + 2).contains(",")) {
                                    String[] refined = place.get(i + 2).split(",");
                                    geocoderMap.put(place.get(i), Arrays.asList(refined[refined.length - 1], place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                    waypointMap.put(place.get(i), Arrays.asList(refined).subList(0, refined.length - 1));
                                    latchCount += refined.length - 1;
                                } else {
                                    geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 2), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                }
                            } else {
                                // contains a refined address
                                if (!place.get(i + 2).isEmpty()) {
                                    geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1) + ", " + place.get(i + 2), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                // no refined address
                                } else {
                                    geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                }
                            }
                        } else {
                            // use address only
                            geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                        }
                        latchCount++;
                    }
                    geocoderMaps.put(table, geocoderMap);
                    if (!waypointMap.isEmpty()) {
                        waypointMaps.put(table, waypointMap);
                    }
                }
            }

            Log.i(TAG, "latchcount 1: " + latchCount);
//            Log.i(TAG, "geocoderMaps: " + geocoderMaps.toString());


            Type mapType = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
            if (sharedPreferences.contains(BranchDirectoryMap.KEY_LOAD_REDO)) {
                missingMaps = BranchDirectoryMap.gson.fromJson(sharedPreferences.getString(BranchDirectoryMap.KEY_LOAD_REDO, ""), mapType);
//                Log.i(TAG, "missingMaps: " + missingMaps.toString());
                for (String table : missingMaps.keySet()) {
                    latchCount += missingMaps.get(table).size();
                }
            }
            if (sharedPreferences.contains(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT)) {
                missingWaypointMaps = BranchDirectoryMap.gson.fromJson(sharedPreferences.getString(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT, ""), mapType);
//                Log.i(TAG, "missingWaypointMaps: " + missingWaypointMaps.toString());
                for (String table : missingWaypointMaps.keySet()) {
                    for (String name : missingWaypointMaps.get(table).keySet()) {
                        latchCount += missingWaypointMaps.get(table).get(name).size();
                    }
                }
            }

            Log.i(TAG, "latchcount 2: " + latchCount);

            if (!databaseExists) {
                Log.i(TAG, "database does not exist");
            } else {
                varMapStr = dbHelper.getVarMapStr(false);
                varMap = BranchDirectoryMap.gson.fromJson(varMapStr, BranchDirectoryMap.VARMAP_TYPE);
                for (String table : varMap.keySet()) {
                    if (tablesSet.add(table)) {
                        tables.add(table);
                    } else {
                        Log.i(TAG, "table already added: " + table);
                    }
                }
                Log.i(TAG, "return size: " + getMapSize(markers));
            }

            latch = new CountDownLatch(latchCount);

            if (mapHasInnerElements(missingMaps, false)) {
                Log.i(TAG, "missingMaps loaded to geocoderMaps");
                geocoderMaps = deepCopy3d(missingMaps);
                missingMaps.clear();
            }
//            Log.i(TAG, "geocoderMaps: " + geocoderMaps.toString());
            linkApiKey = Secrets.getStoredGeocodeApiKey(context);
            for (String table : geocoderMaps.keySet()) {
                Log.i(TAG, "table: " + table);
                Map<String, List<String>> geocoderMap = geocoderMaps.get(table);
                missingMaps.put(table, new HashMap<>());
                String delim = (String) varMap.get(table).get("delimiter");
                for (String name : geocoderMap.keySet()) {
                    String address = geocoderMap.get(name).get(0);
                    values.put(table, Collections.synchronizedList(new ArrayList<>()));
                    markers.put(table, Collections.synchronizedList(new ArrayList<>()));
                    StringBuilder finalAddress = new StringBuilder(address);
                    String geoRegion = (String) varMap.get(table).get("geocode_region");
                    if (!address.contains("+") && !geoRegion.isEmpty()) {
                        finalAddress.append(", " + geoRegion);
                    }
//                    Log.i(TAG, "finalAddress: " + finalAddress);

                    boolean randomFail;
                    if (BranchDirectoryMap.FORCED_FAIL && random.nextInt(100) >= BranchDirectoryMap.PASS_PERCENT) {
                        randomFail = true;
                    } else {
                        randomFail = false;
                    }

                    executor.submit(() -> {
                        try {
                            if (finalAddress.length() != 0 && !randomFail) {
                                service.getGeocode(finalAddress.toString(), linkApiKey).enqueue(new Callback<>() {
                                    @Override
                                    public void onResponse(Call<GeocodingResponse> call, retrofit2.Response<GeocodingResponse> response) {
                                        if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                                            if (response.body().getResults().size() > 1) {
                                                Log.i(TAG, "More than one result found for: " + finalAddress + " in table: " + table);
                                            }
                                            GeocodingResponse.Result.Geometry.Location location = response.body().getResults().get(0).getGeometry().getLocation();
                                            if (BuildConfig.ALLOW_DUPLICATES || tracker.addLatLng(location.getLat(), location.getLng())) {
                                                populateMaps(table, name, delim, location.getLat(), location.getLng());
                                            } else {
                                                Log.i(TAG, "Duplicate LatLng not added: " + table + ", " + name + ", "
                                                        + location.getLat() + ", " + location.getLng());
                                            }
                                        } else {
                                            Log.i(TAG, "No results found for: " + finalAddress);
                                            missingMaps.get(table).put(name, geocoderMap.get(name));
                                            removeFromVarMap(table, name);
                                        }
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                                        Log.i(TAG, "Geocode request failed for: " + finalAddress, t);
                                        missingMaps.get(table).put(name, geocoderMap.get(name));
                                        latch.countDown();
                                    }
                                });
                            } else {
                                if (!randomFail) {
                                    Log.i(TAG, "No address found for: " + name + ", this is unexpected");
                                } else {
                                    Log.i(TAG, "Geocode request forced to fail: " + finalAddress);
                                    missingMaps.get(table).put(name, geocoderMap.get(name));
                                    latch.countDown();
                                }
                            }
                            long sleepTime = BuildConfig.BASE_DELAY_MS + random.nextInt(BuildConfig.RANDOM_DELAY_MS + 1);
                            TimeUnit.MILLISECONDS.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
            if (mapHasInnerElements(missingWaypointMaps, true)) {
                Log.i(TAG, "missingWaypointMaps loaded to waypointMaps");
                waypointMaps = deepCopy3d(missingWaypointMaps);
                missingWaypointMaps.clear();
                missingWaypointMaps = new HashMap<>();
            }
            if (!waypointMaps.isEmpty()) {
                Log.i(TAG, "waypointMaps populated");
                for (String table : waypointMaps.keySet()) {
                    Map<String, List<String>> waypointMap = waypointMaps.get(table);
                    waypointValues.put(table, new HashMap<>());
                    missingWaypointMaps.put(table, new HashMap<>());
                    for (String name : waypointMap.keySet()) {
                        waypointValues.get(table).put(name, new HashMap<>());
                        missingWaypointMaps.get(table).put(name, new ArrayList<>());
                        for (String pluscode : waypointMap.get(name)) {
                            if (BranchDirectoryMap.FORCED_FAIL && random.nextInt(100) >= 75) {
                                Log.i(TAG, "Geocode request forced to fail for waypoint: " + name +", " + pluscode);
                                missingWaypointMaps.get(table).get(name).add(pluscode);
                                latch.countDown();
                                continue;
                            }
                            executor.submit(() -> {
                                try {
                                    service.getGeocode(pluscode, linkApiKey).enqueue(new Callback<>() {
                                        @Override
                                        public void onResponse(Call<GeocodingResponse> call, retrofit2.Response<GeocodingResponse> response) {
                                            if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                                                GeocodingResponse.Result.Geometry.Location location = response.body().getResults().get(0).getGeometry().getLocation();
                                                waypointValues.get(table).get(name).put(pluscode, new LatLng(location.getLat(), location.getLng()));
                                                populateWaypoints(table, name, pluscode);
                                            } else {
                                                Log.i(TAG, "No results found for: " + name +", " + pluscode);
                                                missingWaypointMaps.get(table).get(name).add(pluscode);
                                            }
                                            latch.countDown();
                                        }

                                        @Override
                                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                                            Log.i(TAG, "Geocode request failed for: " + name +", " + pluscode, t);
                                            missingWaypointMaps.get(table).get(name).add(pluscode);
                                            latch.countDown();
                                        }
                                    });
                                    long sleepTime = BuildConfig.BASE_DELAY_MS + random.nextInt(BuildConfig.RANDOM_DELAY_MS + 1);
                                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                        }
                    }
                }
            }
            executor.shutdown();
            try {
                latch.await();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            linkApiKey = null;

            for (String table : waypointMaps.keySet()) {
//                Log.i(TAG, "table: " + table);
                for (String name : waypointMaps.get(table).keySet()) {
//                    Log.i(TAG, "name: " + name);
                    if (missingMaps.containsKey(table) && missingMaps.get(table).containsKey(name)) {
                        Log.i(TAG, "waypointMaps contains orphaned entry: " + table + ", " + name
                                + ", will be moved to missingWaypointMaps");
                        if (!missingWaypointMaps.containsKey(table)) {
                            missingWaypointMaps.put(table, new HashMap<>());
                        }
                        missingWaypointMaps.get(table).put(name, waypointMaps.get(table).get(name));
                    }
                }
            }

            Map<String, List<CustomItem>> oldMarkers = dbHelper.getAllLocations(db);
            int mapsize = getMapSize(markers);
            Log.i(TAG, "markers size prior: " + mapsize);
            if (!oldMarkers.isEmpty()) {
                CustomItemListMerger merger = new CustomItemListMerger();
                markers = merger.merge(markers, oldMarkers);
            }
            mapsize = getMapSize(markers);
            if (mapsize > 0) {
                addToMap(varMap, markers);
            }
            if (getMapSize3D(waypointValues) > 0) {
                addToMapWaypoints(varMap, waypointValues);
//                applyWaypoints(markers, waypointValues);
            }
//            if (mapHasInnerElements(missingMaps, false)) {
//                Log.i(TAG, "missingMaps: " + missingMaps.toString());
//            }
//            if (mapHasInnerElements(missingWaypointMaps, true)) {
//                Log.i(TAG, "missingWaypointMaps: " + missingWaypointMaps.toString());
//            }
            Log.i(TAG, "markers size: " + mapsize);
            Log.i(TAG, "waypointValues size: " + getMapSize3D(waypointValues));
            Log.i(TAG, "latchCount: " + latchCount);

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.i(TAG, "Elapsed time doInBackground: " + (System.currentTimeMillis() - start));
            boolean mapsMissing = mapHasInnerElements(missingMaps, false);
            boolean waypointsMissing = mapHasInnerElements(missingWaypointMaps, true);
            if (mapsMissing || waypointsMissing) {
                editor.putBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false).apply();
                Log.i(TAG, "tables: " + tables.toString());

                dialogUtils.showOkDialog(context, getString(R.string.warning), getString(R.string.marker_error),
                        (dialog, id) -> dialog.dismiss());

                String jsonString;
                if (mapsMissing) {
                    for (String table : missingMaps.keySet()) {
                        for (String title : missingMaps.get(table).keySet()) {
                            Log.i(TAG, "marker not found: " + title + " in table: " + table);
                            Log.i(TAG, "data: " + missingMaps.get(table).get(title).toString());
                        }
                    }
                    jsonString = BranchDirectoryMap.gson.toJson(missingMaps);
                    editor.putString(BranchDirectoryMap.KEY_LOAD_REDO, jsonString).apply();
                }
                if (waypointsMissing) {
                    for (String table : missingWaypointMaps.keySet()) {
                        for (String title : missingWaypointMaps.get(table).keySet()) {
                            if (!missingWaypointMaps.get(table).get(title).isEmpty()) {
                                Log.i(TAG, "waypoint not found: " + title + " in table: " + table);
                                Log.i(TAG, "data: " + missingWaypointMaps.get(table).get(title).toString());
                            }
                        }
                    }
                    jsonString = BranchDirectoryMap.gson.toJson(missingWaypointMaps);
                    editor.putString(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT, jsonString).apply();
                }
            } else {
                editor.remove(BranchDirectoryMap.KEY_LOAD_REDO);
                editor.remove(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT);
                editor.putBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, true).apply();
            }

            Collections.sort(tables);
            if (db == null || !db.isOpen()) {
                db = dbHelper.getWritableDatabase();
            }
            db.beginTransaction();
            try {
                for (String table : tables) {
//                    Log.i(TAG, "tables from db 1: " + dbHelper.getTables(db));
                    if (!dbHelper.tableExists(db, table)) {
                        Log.i(TAG, "added table: " + table);
                        dbHelper.createTable(db, table);
                    }
//                    Log.i(TAG, "values: " + values.toString());
                    if (!values.isEmpty() && values.containsKey(table) && !values.get(table).isEmpty()) {
                        for (ContentValues value : values.get(table)) {
                            if (!entryExists(table, value.getAsString("code"), value.getAsString("name"))) {
//                                Log.i(TAG, value.getAsString("code") + ", " + value.getAsString("name") +
//                                        ", " + value.getAsDouble("latitude") + ", " + value.getAsDouble("longitude"));
                                db.insert(table, null, value);
                            } else {
                                updateEntry(table, value);
                            }
                        }
                    }
                }
                dbHelper.deleteTable(db, "varmap");
                dbHelper.createTable(db, "varMap.create");  // special command for varMap specifically
                String jsonString = BranchDirectoryMap.gson.toJson(varMap);
                ContentValues varMapStrValues = new ContentValues();
                varMapStrValues.put("string", jsonString);
                db.insert("varmap", null, varMapStrValues);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();    // FIX THIS: add logging
            } finally {
                db.endTransaction();
                db.close();
            }
            if (BuildConfig.EMBEDDED_DB.isEmpty() && BuildConfig.EXPORT_DB
                    && sharedPreferences.getBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false)) {
                dbHelper.exportDatabase(BuildConfig.DATABASE_NAME);
            }
            Log.i(TAG, "tables being added: " + tables);
            for (String table : tables) {
                clusterItemMarkerMap.put(table, new HashMap<>());
            }
            // populate CustomItem waypoints from varMap
            for (String table : varMap.keySet()) {
                Map<String, Map<String, Object>> waypoints = (Map<String, Map<String, Object>>) varMap.get(table).get("waypoints");
                if (waypoints != null) for (String name : waypoints.keySet()) {
                    for (CustomItem item : markers.get(table)) {
                        if (item.getTitle().equals(name)) {
                            Map<String, Object> byPluscode = waypoints.get(name);
                            for (String pluscode : byPluscode.keySet()) {
                                Object o = byPluscode.get(pluscode);
                                LatLng ll;
                                if (o instanceof LatLng) {
                                    ll = (LatLng) o;
                                } else {
                                    Map<String, Object> m = (Map<String, Object>) o;
                                    Object latObj = m.get("l");
                                    if (latObj == null) latObj = m.get("latitude");
                                    if (latObj == null) latObj = m.get("lat");
                                    Object lngObj = m.get("m");
                                    if (lngObj == null) lngObj = m.get("longitude");
                                    if (lngObj == null) lngObj = m.get("lng");
                                    double lat = ((Number) latObj).doubleValue();
                                    double lng = ((Number) lngObj).doubleValue();
                                    ll = new LatLng(lat, lng);
                                }
                                item.setWaypoint(pluscode, ll);
                            }
                            break;
                        }
                    }
                }
            }
            if (BuildConfig.SHOW_ALL_HEADER && tables.size() > 1) {
                clusterItemMarkerMap.put(getString(R.string.header_all), new HashMap<>());
                markers.put(getString(R.string.header_all), Collections.synchronizedList(new ArrayList<>()));

                List<CustomItem> all = new ArrayList<>();
                for (Map.Entry<String, List<CustomItem>> e : markers.entrySet()) {
                    if (getString(R.string.header_all).equals(e.getKey())) continue;
                    List<CustomItem> list = e.getValue();
                    if (list != null) {
                        for (CustomItem item : list) {
                            if (item != null) all.add(item);
                        }
                    }
                }
                markers.put(getString(R.string.header_all), all);
                searchAdapter = new SearchSpinnerAdapter(context, tables, getString(R.string.header_all));
            } else {
                searchAdapter = new SearchSpinnerAdapter(context, tables, null);
            }
            searchAdapter.notifyDataSetChanged();
            if (!BuildConfig.DEFAULT_FILE.isEmpty() && tables.size() > 1) {
                currentTable = BuildConfig.DEFAULT_FILE.substring(0,
                        BuildConfig.DEFAULT_FILE.contains(".") ? BuildConfig.DEFAULT_FILE.lastIndexOf(".") : BuildConfig.DEFAULT_FILE.length());
            } else {
                currentTable = tables.get(0);
            }
            searchSpinner.setAdapter(searchAdapter);
            int position = Collections.binarySearch(tables, currentTable) + 1;
            searchSpinner.setSelection(position);
            searchAdapter.setSelectedItemPosition(position);
            CustomItem.CustomItemSorter.sortAllListsByCode(markers);
            for (CustomItem marker : markers.get(currentTable)) {
                clusterManager.addItem(marker);
            }

            searchSpinner.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            loadTextView.setVisibility(View.GONE);
            menuButton.setVisibility(View.VISIBLE);

            Toast.makeText(context, getString(R.string.finished), Toast.LENGTH_SHORT).show();
        }

        public void addToMap(Map<String, Map<String, Object>> outer, Map<String, List<CustomItem>> inner) {
            for (String table : inner.keySet()) {
                if (outer.containsKey(table)) {
//                Log.i(TAG, "outer table: " + outer.get(table));
//                Log.i(TAG, "inner table: " + inner.get(table));
                    ArrayList<String> outerList = (ArrayList<String>) outer.get(table).get("array");
                    for (CustomItem item : inner.get(table)) {
                        List<String> newItem = Arrays.asList(item.getTitle(), item.getSnippet(), item.getRefined(), item.getPhone(), item.getColour());
//                    Log.i(TAG, "to be added: " + item.getTitle() + ", " + item.getSnippet() + ", " + item.getRefined() + ", " + item.getPhone());
                        int matchIndex = outerList.indexOf(item.getTitle());
                        if (matchIndex > -1) {
//                        Log.i(TAG, "match index: " + matchIndex + " item: " + item.getTitle());
                            outerList.subList(matchIndex, matchIndex + BranchDirectoryMap.NUM_OF_CUSTOMITEM_VARS).clear();
                            outerList.addAll(matchIndex, newItem);
                        } else {
                            outerList.addAll(newItem);
                        }
                    }
//                Log.i(TAG, "new outer array: " + outer.get(table).get("array"));
                } else {
                    Log.e(TAG, "WARNING: addToMap - outer table not found");
                }
            }
        }

        public void addToMapWaypoints(Map<String, Map<String, Object>> outer, Map<String, Map<String, Map<String, LatLng>>> inner) {
            for (String reftable : inner.keySet()) {
                if (outer.containsKey(reftable)) {
                    Map<String, Object> outerMap = outer.get(reftable);
                    Map<String, Map<String, LatLng>> innerMap = new HashMap<>();
                    for (String refname : inner.get(reftable).keySet()) {
                        if (((ArrayList<String>) outer.get(reftable).get("array")).contains(refname)) {
                            innerMap.put(refname, new HashMap<>());
                            innerMap.get(refname).putAll(inner.get(reftable).get(refname));
                        } else {
                            Log.e(TAG, "WARNING: addToMapWaypoints - outer name not found: " + refname + " from table: " + reftable);
                        }
                    }
                    outerMap.put("waypoints", innerMap);
//                Log.i(TAG, "new outer: " + outer.get(reftable).get("waypoints"));
//                Log.i(TAG, "outer array: " + outer.get(reftable).get("array"));
                } else {
                    Log.e(TAG, "WARNING: addToMapWaypoints - outer table not found:" + reftable);
                }
            }
        }

        public void removeFromVarMap(String table, String name) {
//            Log.i(TAG, "removeFromVarMap table: " + table + ", name: " + name);
            if (!varMap.containsKey(table)) {
                Log.i(TAG, "varMap doesn't contain table " + table);
                return;
            }
            if (!varMap.get(table).containsKey("array")) {
                Log.i(TAG, "varMap table " + table + " doesn't contain an array");
            }
            ArrayList<String> markerList = (ArrayList<String>) varMap.get(table).get("array");
            for (int i = 0; i < markerList.size(); i += BranchDirectoryMap.NUM_OF_CUSTOMITEM_VARS) {
                if (markerList.get(i).equals(name)) {
//                    Log.i(TAG, "removing marker: " + name + " from table: " + table);
                    markerList.subList(i, i + BranchDirectoryMap.NUM_OF_CUSTOMITEM_VARS).clear();
//                    Log.i(TAG, "new varMap(" + table + ") array: " + markerList);
                    return;
                }
            }
        }

        public void populateMaps(String table, String name, String delim, double latitude, double longitude) {
            Map<String, List<String>> geocoderMap = geocoderMaps.get(table);
            String waypoints = geocoderMap.get(name).get(2).split("\\+").length > 2
                    ? geocoderMap.get(name).get(2) : "";
            String[] nameParsed = parse(name, delim, (String) varMap.get(table).get("code_prefix"), (boolean) varMap.get(table).get("name_first"));
            String nameSnippet = nameParsed[0] == null ? "" : nameParsed[0];
            String nameCode = nameParsed[1] == null ? "" : nameParsed[1];
            String json;
            Map<String, LatLng> map = new LinkedHashMap<>();
            if (!waypoints.isEmpty()) {
                waypoints = waypoints.substring(0, waypoints.lastIndexOf(","));
                for (String waypoint : waypoints.split(",")) {
                    map.put(waypoint, null);
                }
            }
            json = BranchDirectoryMap.gson.toJson(map, BranchDirectoryMap.WAYPOINTS_TYPE);
            ContentValues value = new ContentValues();
            value.put("latitude", latitude);
            value.put("longitude", longitude);
            value.put("code", nameCode);
            value.put("name", nameSnippet);
            value.put("address", geocoderMap.get(name).get(1));
            value.put("refined", geocoderMap.get(name).get(2));
            value.put("waypoints", json);
            value.put("phone", geocoderMap.get(name).get(3));
            value.put("colour", geocoderMap.get(name).get(4));
            value.put("nameFirst", ((boolean) varMap.get(table).get("name_first")) ? 1 : 0);
            values.get(table).add(value);
            markers.get(table).add(new CustomItem(latitude, longitude, nameCode, nameSnippet,
                    geocoderMap.get(name).get(1), geocoderMap.get(name).get(2), waypoints,
                    geocoderMap.get(name).get(3), geocoderMap.get(name).get(4),
                    ((boolean) varMap.get(table).get("name_first"))));
        }

        public void populateWaypoints(String table, String name, String pluscode) {
//            Log.i(TAG, "populateWaypoints: " + table + ", " + name + ", " + pluscode);
            List<ContentValues> cv = values.get(table);
            for (ContentValues value : cv) {
                if (value.containsKey("code") && value.containsKey("name")) {
                    String vcode = value.getAsString("code");
                    String vname = value.getAsString("name");
                    String vtitle = vcode + (vcode.isEmpty() ? "" : vname.isEmpty() ? "" : " ") + (vname.isEmpty() ? "" : vname);
                    if (vtitle.equals(name)) {
                        Map<String, LatLng> waypointsMap = new LinkedHashMap<>();
                        Map<String, LatLng> valuesMap = waypointValues.get(table).get(name);
                        Map<String, LatLng> mergedMap;
                        if (value.containsKey("waypoints")) {
                            waypointsMap = BranchDirectoryMap.gson.fromJson(value.getAsString("waypoints"), BranchDirectoryMap.WAYPOINTS_TYPE);
                            value.remove("waypoints");
                            LatLngMerger merger = new LatLngMerger();
                            mergedMap = merger.merge(waypointsMap, valuesMap);
                        } else {
                            Log.e(TAG, "Error at populateWaypoints: waypoints key not found in: " + table + ", " + vcode + ", " + vname);
                            return;
                        }
                        value.put("waypoints", BranchDirectoryMap.gson.toJson(mergedMap, BranchDirectoryMap.WAYPOINTS_TYPE));
                        break;      // break since only one entry should match
                    }
                }
            }
            List<CustomItem> items = markers.get(table);
            for (CustomItem item : items) {
                if (item.getTitle().equals(name)) {
                    item.setWaypoint(pluscode, waypointValues.get(table).get(name).get(pluscode));
//                    Log.i(TAG, "waypointValues: " + waypointValues.get(table).get(name));
//                    Log.i(TAG, "item: " + item.toString());
                    return;     // return since only one entry should match
                }
            }
            Log.e(TAG,"ERROR: populateWaypoints did not match: " + name + " in table: " + table);
        }

        public void updateEntry(String tableName, ContentValues values) {
            String codeValue = values.getAsString("code");
            String nameValue = values.getAsString("name");
            for (String key : values.keySet()) {
                Object value = values.get(key);
                if (value != null) {
                    if (key.equals("code") || key.equals("name") || (value instanceof Double && (double) value == 1000) || (value instanceof String && value.equals("unchanged"))) {
                        values.remove(key);
                    }
                }
            }
            String whereClause = "code = ? AND name = ?";
            String[] whereArgs = new String[]{codeValue, nameValue};
            db.update(tableName, values, whereClause, whereArgs);
        }

        public boolean entryExists(String tableName, String code, String name) {
            String query = "SELECT 1 FROM " + tableName + " WHERE code = ? AND name = ?";
            Cursor cursor = db.rawQuery(query, new String[]{code, name});

            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            return exists;
        }

        public static String[] parse(String full, String delim, String prefix, boolean nameFirst) {
            if (full == null || delim == null || delim.isEmpty()) {
                return new String[]{full, null};
            }
            if (!full.contains(delim) && prefix != null && !prefix.isEmpty() && full.contains(prefix)) {
                return new String[]{null, full};
            }

            String nameSnippet;
            String nameCode;

            if (nameFirst) {
                int idx = full.lastIndexOf(delim);
                if (idx < 0) {
                    nameSnippet = full;
                    nameCode    = null;
                } else {
                    nameSnippet = full.substring(0, idx);
                    nameCode    = full.substring(idx + delim.length());
                }
            } else {
                int idx = full.indexOf(delim);
                if (idx < 0) {
                    nameSnippet = full;
                    nameCode    = null;
                } else {
                    nameCode    = full.substring(0, idx);
                    nameSnippet = full.substring(idx + delim.length());
                }
            }

//            Log.i(TAG, "full: " + full + ", delim: " + delim + ", nameFirst: " + nameFirst);
//            Log.i(TAG, "nameSnippet: " + nameSnippet);
//            Log.i(TAG, "nameCode: " + nameCode);

            return new String[]{nameSnippet, nameCode};
        }
    }

    public void GetInformationTaskCreator() {
        getInformationTask = new GetInformationTask(this);
        getInformationTask.execute();
    }

    public class GetInformationTask extends AsyncTask<Void, Void, Object[]> {

        private WeakReference<Activity> activityRef;
        private final Context context;
        private final Marker marker;
        private final double markerLat;
        private final double markerLng;

        public GetInformationTask(Activity activity) {
            if (currentMarker != null) {
                marker = currentMarker;
                if (currentItem.getWaypoints().isEmpty()) {
                    markerLat = marker.getPosition().latitude;
                    markerLng = marker.getPosition().longitude;
                    Log.i(TAG, "A markerLat: " + markerLat + ", markerLng: " + markerLng);
                } else {
                    Log.i(TAG, "currentItem: " + currentItem.toString());
                    markerLat = currentItem.getWaypoints().get(currentItem.getLastWaypoint()).latitude;
                    markerLng = currentItem.getWaypoints().get(currentItem.getLastWaypoint()).longitude;
                    Log.i(TAG, "B markerLat: " + markerLat + ", markerLng: " + markerLng);
                }
            } else {
                Log.e(TAG, "currentMarker is null, FIX THIS");
                marker = null;
                markerLat = 1000;
                markerLng = 1000;
            }
            activityRef = new WeakReference<>(activity);
            context = activityRef.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (marker == null) {
                cancel(true);
                Log.i(TAG, "task cancelled");
            }
        }

        @Override
        protected Object[] doInBackground(Void... voids) {
            if (currentItem.getSelected() == -1) {
                OkHttpClient client = new OkHttpClient();
                if (!BuildConfig.USE_ADVANCED_ROUTING) {
                    linkApiKey = Secrets.getStoredGeocodeApiKey(context);
                    final StringBuilder url = new StringBuilder(BranchDirectoryMap.BASE_URL + "directions/json"
                            + "?origin=" + lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude()
                            + "&destination=" + markerLat + "," + markerLng + "&key=" + linkApiKey);
                    linkApiKey = null;

                    // add waypoints from route markers first then add waypoints from currentItem
                    if (!routeMarkers.isEmpty() || currentItem.getWaypointsCount() > 1) {
                        CountDownLatch latch = new CountDownLatch(1);
                        Activity activity = activityRef.get();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder params = waypointCombiner("|", false);
                                if (params.length() > 0) {
                                    url.append(params);
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();    // FIX THIS: add logging
                        }
                    }

                    StringBuilder avoidOptions = new StringBuilder();
                    if (!isTollsEnabled) avoidOptions.append("tolls|");
                    if (!isHighwaysEnabled) avoidOptions.append("highways|");
                    if (!isFerriesEnabled) avoidOptions.append("ferries|");
                    if (avoidOptions.length() > 0) {
                        avoidOptions.insert(0, "&avoid=");
                        avoidOptions.setLength(avoidOptions.length() - 1);
                    }
                    url.append(avoidOptions);

                    Log.i(TAG, "trafficMode: " + trafficMode);

                    switch (trafficMode) {
                        case "No Traffic":
                            break;
                        case "Best Guess":
                            url.append("&traffic_model=best_guess" + "&departure_time=now");
                            break;
                        case "Optimistic":
                            url.append("&traffic_model=optimistic" + "&departure_time=now");
                            break;
                        case "Pessimistic":
                            url.append("&traffic_model=pessimistic" + "&departure_time=now");
                    }

                    Request computeDirectionsRequest = new Request.Builder().url(url.toString()).build();
//                    Log.i(TAG, "url: " + url);

                    try (Response computeDirectionsResponse = client.newCall(computeDirectionsRequest).execute()) {
                        if (computeDirectionsResponse.isSuccessful()) {
                            String responseString = computeDirectionsResponse.body().string();
                            JsonObject computeDirectionsJson = JsonParser.parseString(responseString).getAsJsonObject();
                            Log.i(TAG, "response: " + computeDirectionsJson.toString());
                            if (computeDirectionsJson.has("routes") && !computeDirectionsJson.getAsJsonArray("routes").isEmpty()) {
                                JsonObject route = computeDirectionsJson.getAsJsonArray("routes").get(0).getAsJsonObject();
                                JsonArray legs = route.getAsJsonArray("legs");
                                int totalDuration = 0;
                                int totalDistance = 0;
                                for (int i = 0; i < legs.size(); i++) {
                                    JsonObject leg = legs.get(i).getAsJsonObject();

                                    JsonObject duration = leg.getAsJsonObject("duration");
                                    totalDuration += duration.get("value").getAsInt();

                                    JsonObject distance = leg.getAsJsonObject("distance");
                                    totalDistance += distance.get("value").getAsInt();
                                }

                                JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
                                String polyline = overviewPolyline.get("points").getAsString();

                                // encodedPolyline expects a 2D String List so return Object is a
                                // 2D String List with polyline as the [0][0] element
                                return new Object[]{formatDistanceForLocale(totalDistance),
                                        formatComputeRouteDuration(totalDuration, false),
                                        formatComputeRouteDuration(totalDuration, true),
                                        new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(polyline)))),
                                        null};
                            } else {
                                Log.e(TAG, "Error status: " + computeDirectionsJson.get("status").getAsString());
                                Log.e(TAG, "Error message: " + computeDirectionsJson.get("error_message").getAsString());
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "HTTP Request Failed: " + e.getMessage());
                    }
                } else {
                    JsonObject root = new JsonObject();
                    JsonObject originObj = new JsonObject();
                    JsonObject originLocationObj = new JsonObject();
                    JsonObject originLatLngObj = new JsonObject();
                    originLatLngObj.addProperty("latitude", lastKnownLocation.getLatitude());
                    originLatLngObj.addProperty("longitude", lastKnownLocation.getLongitude());
                    originLocationObj.add("latLng", originLatLngObj);
                    originObj.add("location", originLocationObj);
                    root.add("origin", originObj);

                    JsonObject destinationObj = new JsonObject();
                    JsonObject destLocationObj = new JsonObject();
                    JsonObject destLatLngObj = new JsonObject();
                    destLatLngObj.addProperty("latitude", markerLat);
                    destLatLngObj.addProperty("longitude", markerLng);
                    destLocationObj.add("latLng", destLatLngObj);
                    destinationObj.add("location", destLocationObj);
                    root.add("destination", destinationObj);
                    JsonArray intermediatesArray = new JsonArray();

                    if (!routeMarkers.isEmpty()) {
                        Iterator<String> iterator = routeMarkers.keySet().iterator();
                        while (iterator.hasNext()) {
                            String title = iterator.next();
                            CustomItem item = (CustomItem) routeMarkers.get(title).get(1);
                            if (item.getWaypoints().isEmpty()) {
                                Marker marker = (Marker) routeMarkers.get(title).get(0);
                                if (!iterator.hasNext() && marker.equals(currentMarker)) {
                                    Log.i(TAG, "adv route: last marker is currentMarker");
                                    break;
                                }
                                JsonObject waypointObject = new JsonObject();
                                JsonObject locationObject = new JsonObject();
                                JsonObject latLngObject = new JsonObject();

                                latLngObject.addProperty("latitude", ((LatLng) routeMarkers.get(title).get(2)).latitude);
                                latLngObject.addProperty("longitude", ((LatLng) routeMarkers.get(title).get(2)).longitude);

                                locationObject.add("latLng", latLngObject);
                                waypointObject.add("location", locationObject);
                                intermediatesArray.add(waypointObject);
                            } else {
                                Iterator<String> waypointIterator = new ArrayList<>(item.getWaypoints().keySet()).iterator();
                                while (waypointIterator.hasNext()) {
                                    String waypoint = waypointIterator.next();
                                    JsonObject waypointObject = new JsonObject();
                                    JsonObject locationObject = new JsonObject();
                                    JsonObject latLngObject = new JsonObject();

                                    latLngObject.addProperty("latitude", item.getWaypoints().get(waypoint).latitude);
                                    latLngObject.addProperty("longitude", item.getWaypoints().get(waypoint).longitude);

                                    locationObject.add("latLng", latLngObject);
                                    waypointObject.add("location", locationObject);

                                    if (waypointIterator.hasNext()) {
                                        waypointObject.addProperty("via", true);
                                    }

                                    intermediatesArray.add(waypointObject);
                                }
                            }
                        }
                    }
                    if (!currentItem.getWaypoints().isEmpty()) {
                        List<String> waypoints = new ArrayList<>(currentItem.getWaypoints().keySet());
                        waypoints.remove(currentItem.getWaypointsCount() - 1);
                        for (String waypoint : waypoints) {
                            JsonObject waypointObject = new JsonObject();
                            JsonObject locationObject = new JsonObject();
                            JsonObject latLngObject = new JsonObject();

                            latLngObject.addProperty("latitude", currentItem.getWaypoints().get(waypoint).latitude);
                            latLngObject.addProperty("longitude", currentItem.getWaypoints().get(waypoint).longitude);

                            locationObject.add("latLng", latLngObject);
                            waypointObject.add("location", locationObject);
                            waypointObject.addProperty("via", true);

                            intermediatesArray.add(waypointObject);
                        }
                    }
                    if (!intermediatesArray.isEmpty()) {
                        root.add("intermediates", intermediatesArray);
                    }

                    root.addProperty("travelMode", "DRIVE");
                    root.addProperty("routingPreference", "TRAFFIC_AWARE_OPTIMAL");
                    root.addProperty("computeAlternativeRoutes", false);
                    root.addProperty("polylineQuality", "HIGH_QUALITY");
                    JsonObject routeModifiers = new JsonObject();
                    routeModifiers.addProperty("avoidTolls", !isTollsEnabled);
                    routeModifiers.addProperty("avoidHighways", !isHighwaysEnabled);
                    routeModifiers.addProperty("avoidFerries", !isFerriesEnabled);
                    root.add("routeModifiers", routeModifiers);
                    Locale locale = Locale.getDefault();
                    String country = locale.getCountry();
                    root.addProperty("languageCode", locale.toLanguageTag());
                    root.addProperty("units", useMiles(country) ? "imperial" : "metric");

                    Log.i(TAG, "computeRoutesRequestBody: " + root.toString());

                    linkApiKey = Secrets.getStoredGeocodeApiKey(context);
                    try {
                        Request computeRoutesRequest = new Request.Builder()
                                .url(BranchDirectoryMap.ROUTES_URL)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("X-Goog-Api-Key", linkApiKey)
                                .addHeader("X-Goog-FieldMask",
                                        "routes.duration," +
                                                "routes.staticDuration," +
                                                "routes.distanceMeters," +
                                                "routes.legs.steps.polyline.encodedPolyline")
                                .post(RequestBody.create(root.toString(), MediaType.parse("application/json")))
                                .build();
                        try (Response computeRoutesResponse = client.newCall(computeRoutesRequest).execute()) {
                            linkApiKey = null;
                            if (computeRoutesResponse.isSuccessful()) {

//                                int code = computeRoutesResponse.code();
//                                ResponseBody body = computeRoutesResponse.body();
//                                Log.i(TAG, "Routes HTTP code=" + code
//                                        + " message=" + computeRoutesResponse.message()
//                                        + " bodyNull=" + (body == null)
//                                        + " contentType=" + (body != null ? body.contentType() : null)
//                                        + " contentLength=" + (body != null ? body.contentLength() : -1));
//                                if (body == null) {
//                                    Log.i(TAG, "No response body");
//                                } else {
//                                    // For debugging, this copies up to N bytes without consuming the real stream:
//                                    String peek = computeRoutesResponse.peekBody(1024 * 1024).string();
//                                    Log.i(TAG, "peekBody len=" + peek.length());
//
//                                    // Then read the real body ONCE:
//                                    String responseString = body.string();
//                                    Log.i(TAG, "responseString len=" + responseString.length());
//                                }

                                String responseString = computeRoutesResponse.body().string();
                                JsonObject computeRoutesJson = JsonParser.parseString(responseString).getAsJsonObject();
                                if (computeRoutesJson.has("routes") && !computeRoutesJson.getAsJsonArray("routes").isEmpty()) {
                                    JsonObject route = computeRoutesJson.getAsJsonArray("routes").get(0).getAsJsonObject();
                                    JsonArray legs = route.getAsJsonArray("legs");
                                    String routeDurationStr = route.get("duration").getAsString();
                                    double routeDurationDouble = Double.parseDouble(routeDurationStr.replace("s", ""));
                                    long totalSeconds = Math.round(routeDurationDouble);
                                    String routeStaticStr = route.get("staticDuration").getAsString();
                                    double routeStaticDouble = Double.parseDouble(routeStaticStr.replace("s", ""));
                                    long totalStaticSeconds = Math.round(routeStaticDouble);
                                    long totalDistanceMeters = route.get("distanceMeters").getAsLong();
                                    List<List<String>> allLegPolys = new ArrayList<>();
                                    for (JsonElement legElement : legs) {
                                        List<String> allStepPolys = new ArrayList<>();
                                        JsonObject leg = legElement.getAsJsonObject();
                                        JsonArray steps = leg.getAsJsonArray("steps");
                                        for (JsonElement stepElement : steps) {
                                            JsonObject stepObj = stepElement.getAsJsonObject();
                                            String stepPolyline = stepObj
                                                    .getAsJsonObject("polyline")
                                                    .get("encodedPolyline").getAsString();
                                            allStepPolys.add(stepPolyline);
                                        }
                                        allLegPolys.add(allStepPolys);
                                    }

                                    String formattedDistance = formatDistanceForLocale(totalDistanceMeters);
                                    String formattedDuration;
                                    long differenceMinutes = 0;
                                    if (trafficMode.equals("No Traffic")) {
                                        formattedDuration = formatComputeRouteDuration(totalStaticSeconds, false);
                                    } else {
                                        formattedDuration = formatComputeRouteDuration(totalSeconds, false);
                                        differenceMinutes = (totalSeconds - totalStaticSeconds) / 60;
                                    }

//                                    Log.i(TAG, "distance: " + totalDistanceMeters);
//                                    Log.i(TAG, "unaware: " + totalStaticSeconds);
//                                    Log.i(TAG, "aware: " + totalSeconds);

                                    String trafficLevel = "Traffic: ";
                                    int spanStart = trafficLevel.length();
                                    ForegroundColorSpan trafficColour;
                                    if (trafficMode.equals("No Traffic")) {
                                        trafficLevel += "No Traffic Data";
                                        trafficColour = new ForegroundColorSpan(Color.GRAY);
                                    } else if (((double) totalSeconds / (double) totalStaticSeconds) >= trafficMetrics[3]) {
                                        trafficLevel += "Severe ";
                                        trafficLevel += "(+" + differenceMinutes + " mins)";
                                        trafficColour = new ForegroundColorSpan(Color.RED);
                                    } else if (((double) totalSeconds / (double) totalStaticSeconds) >= trafficMetrics[2]) {
                                        trafficLevel += "High ";
                                        trafficLevel += "(+" + differenceMinutes + " mins)";
                                        trafficColour = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange));
                                    } else if (((double) totalSeconds / (double) totalStaticSeconds) >= trafficMetrics[1]) {
                                        trafficLevel += "Moderate ";
                                        trafficLevel += "(+" + differenceMinutes + " mins)";
                                        trafficColour = new ForegroundColorSpan(Color.YELLOW);
                                    } else if (((double) totalSeconds / (double) totalStaticSeconds) >= trafficMetrics[0]) {
                                        trafficLevel += "Light ";
                                        trafficLevel += "(+" + differenceMinutes + " mins)";
                                        trafficColour = new ForegroundColorSpan(Color.GREEN);
                                    } else {
                                        trafficLevel += "None";
                                        trafficColour = new ForegroundColorSpan(Color.WHITE);
                                    }
                                    SpannableString spannable = new SpannableString(trafficLevel);
                                    spannable.setSpan(
                                            trafficColour,
                                            spanStart,
                                            trafficLevel.length(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    );
                                    int violated = 0;
                                    if (isTollsEnabled && routeHasTolls(route)) {
                                        violated |= VIOLATION_TOLLS;
                                    }
                                    if (isFerriesEnabled && routeHasFerry(legs)) {
                                        violated |= VIOLATION_FERRIES;
                                    }
                                    if (isHighwaysEnabled && routeHasHighways(legs)) {
                                        violated |= VIOLATION_HIGHWAYS;
                                    }

                                    return new Object[]{formattedDistance, formattedDuration,
                                            formatComputeRouteDuration(totalSeconds, true),
                                            allLegPolys, spannable, violated};
                                }
                            } else {
                                Log.e(TAG, "computeRoutesResponse error: " + computeRoutesResponse.message());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing requests: " + e.getMessage(), e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object[] result) {
            String distance;
            String duration;
            String arrival;
            if (result != null) {
                distance = (String) result[0];
                oldDistance = distance;
                duration = (String) result[1];
                oldDuration = duration;
                arrival = (String) result[2];
                oldArrival = arrival;
                encodedPolyline = (List<List<String>>) result[3];
                oldPolyline = deepCopy2d(encodedPolyline);
                if (result[4] != null) {
                    customAdapter.setTrafficText((SpannableString) result[4]);
                    oldTrafficText = customAdapter.getTrafficText();
                }
            } else {
                distance = oldDistance;
                duration = oldDuration;
                arrival = oldArrival;
                encodedPolyline = oldPolyline;
                if (oldPolyline != null) {
                    oldPolyline = oldPolyline.subList(0, oldPolyline.size() - (lastSelected ? 0 : 1));
                    encodedPolyline = deepCopy2d(oldPolyline);
                } else {
                    oldPolyline = null;
                    encodedPolyline = null;
                }
                if (oldTrafficText != null) {
                    customAdapter.setTrafficText(oldTrafficText);
                }
            }
            Log.i(TAG, "lastSelected: " + lastSelected);
            customAdapter.setInfoText(distance + " | " + duration + " | " + arrival);
            customRenderer.setShouldCluster(false);
            clusterManager.cluster();
            redrawInfoWindow();
            if (encodedPolyline != null) {
                createPolys();
            } else {
                Log.i(TAG, "encodedPolyline is null");
            }
            if (result[5] != null) {
                showAvoidanceViolationToast((int) result[5]);
            }
        }

        public String formatDistanceForLocale(long distanceMeters) {
            Locale locale = Locale.getDefault();
            String country = locale.getCountry();
            if (useMiles(country)) {
                double distanceMiles = distanceMeters * 0.000621371;
                return String.format(locale, "%.2f mi", distanceMiles);
            } else {
                double distanceKilometers = distanceMeters / 1000.0;
                return String.format(locale, "%.2f km", distanceKilometers);
            }
        }

        public boolean useMiles(String country) {
            return "US".equalsIgnoreCase(country) ||
                    "LR".equalsIgnoreCase(country) ||
                    "MM".equalsIgnoreCase(country) ||
                    "GB".equalsIgnoreCase(country);
        }

        public String formatComputeRouteDuration(long totalSeconds, boolean returnArrivalTime) {
            if (!returnArrivalTime) {
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                StringBuilder formatted = new StringBuilder();
                if (hours > 0) {
                    formatted.append(hours).append(" hr").append(hours > 1 ? "s " : " ");
                }
                if (minutes > 0) {
                    formatted.append(minutes).append(" min").append(minutes > 1 ? "s" : "");
                }
                if (hours == 0 && minutes == 0) {
                    formatted.append("0 mins");
                }
                return formatted.toString().trim();
            } else {
                long currentMillis = System.currentTimeMillis();
                long arrivalMillis = currentMillis + totalSeconds * 1000;
                return sdf.format(new java.util.Date(arrivalMillis));
            }
        }

        private boolean routeHasTolls(JsonObject route) {
            // Routes API: routeLabels may contain "TOLLS"
            if (route.has("routeLabels")) {
                for (JsonElement e : route.getAsJsonArray("routeLabels")) {
                    String v = e.getAsString();
                    if ("TOLLS".equalsIgnoreCase(v) || "TOLL".equalsIgnoreCase(v)) return true;
                }
            }
            // Routes API: travelAdvisory.tollInfo present -> tolls involved
            if (route.has("travelAdvisory")) {
                JsonObject adv = route.getAsJsonObject("travelAdvisory");
                if (adv.has("tollInfo")) return true;
                if (adv.has("advisoryMessage")) {
                    if (adv.get("advisoryMessage").getAsString().toLowerCase().contains("toll")) return true;
                }
            }
            // Directions API (if you reuse logic): warnings like "This route has tolls"
            if (route.has("warnings")) {
                for (JsonElement w : route.getAsJsonArray("warnings")) {
                    if (w.getAsString().toLowerCase().contains("toll")) return true;
                }
            }
            return false;
        }

        private boolean routeHasFerry(JsonArray legs) {
            for (JsonElement legEl : legs) {
                JsonObject leg = legEl.getAsJsonObject();
                if (!leg.has("steps")) continue;
                for (JsonElement stepEl : leg.getAsJsonArray("steps")) {
                    JsonObject step = stepEl.getAsJsonObject();
                    // Routes API: navigationInstruction.maneuver or instructions may mention ferry
                    if (step.has("navigationInstruction")) {
                        JsonObject ni = step.getAsJsonObject("navigationInstruction");
                        if (ni.has("maneuver") && ni.get("maneuver").getAsString().toUpperCase().contains("FERRY"))
                            return true;
                        if (ni.has("instructions") && ni.get("instructions").getAsString().toLowerCase().contains("ferry"))
                            return true;
                    }
                    // Some payloads expose a travelMode per step
                    if (step.has("travelMode") && "FERRY".equalsIgnoreCase(step.get("travelMode").getAsString()))
                        return true;
                }
            }
            return false;
        }

        private boolean routeHasHighways(JsonArray legs) {
            // Heuristics: look for road shields / instructions that clearly indicate highways
            final Pattern HWY = Pattern.compile("\\b(I-\\d+|US-\\d+|Hwy\\b|Highway\\b|Route\\s+\\d+|M-\\d+|A\\d+|E\\d+)\\b",
                    Pattern.CASE_INSENSITIVE);

            for (JsonElement legEl : legs) {
                JsonObject leg = legEl.getAsJsonObject();
                if (!leg.has("steps")) continue;
                for (JsonElement stepEl : leg.getAsJsonArray("steps")) {
                    JsonObject step = stepEl.getAsJsonObject();

                    // Some payloads include a roadShield/roadName
                    if (step.has("roadName")) {
                        if (HWY.matcher(step.get("roadName").getAsString()).find()) return true;
                    }
                    if (step.has("navigationInstruction")) {
                        JsonObject ni = step.getAsJsonObject("navigationInstruction");
                        if (ni.has("instructions") && HWY.matcher(ni.get("instructions").getAsString()).find())
                            return true;
                    }
                    if (step.has("roadShield")) {
                        JsonObject rs = step.getAsJsonObject("roadShield");
                        if (rs.has("type")) {
                            String t = rs.get("type").getAsString().toUpperCase();
                            if (t.contains("INTERSTATE") || t.contains("US_HIGHWAY") || t.contains("STATE")) return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
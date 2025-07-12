package com.example.branchdirectorymap;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentActivity;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import retrofit2.Call;
import retrofit2.Callback;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowCloseListener, CustomInfoWindowAdapter.InfoWindowOpenListener {

    private static final String TAG = "SYS-MAPS";
    private Context context;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DialogUtils dialogUtils;
    private Gson gson;
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
    private ImageButton menuButton;
    private Spinner searchSpinner;
    private TextView loadTextView;
    private LinearLayout markerButtonsLayout;
    private Button viewMarkerButton;
    private Button addMarkerButton;
    private Button removeMarkerButton;
    private Button callMarkerButton;
    private SearchSpinnerAdapter searchAdapter;
    private FloatingActionButton routeButton;
    private LinearLayout routeMenu;
    private FloatingActionButton layersButton;
    private LinearLayout layersMenu;
    private SwitchCompat switchDark;
    private SwitchCompat switchMono;
    private FloatingActionButton clearRouteButton;
    private ClusterManager<MyItem> clusterManager;
    private CustomClusterRenderer customRenderer;
    private CustomInfoWindowAdapter customAdapter;
    private Map<String, List<MyItem>> markers;
    private Marker currentMarker;
    private Marker tempMarker;
    private MyItem currentItem;
    private final Map<String, Map<MyItem, Marker>> clusterItemMarkerMap = new HashMap<>();
    private final List<Marker> waypointList = new ArrayList<>();
    private CustomSearchView searchView;
    private SimpleCursorAdapter suggestionAdapter;
    private final ArrayList<ArrayList<MyItem>> filteredSuggestions = new ArrayList<>();
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
    private Cluster<MyItem> lastCluster;
    private boolean lastSelected;
    private Animation animDisappear;
    private String currentTable;
    private long start;
    private String trafficMode;
    private int local_intermediates;
    private int intermediates_count = 0;
    private int mapMode = GoogleMap.MAP_TYPE_NORMAL;
    private boolean locationPermissionGranted = false;
    private boolean isCentered = false;
    private boolean isInfoWindowOpen = false;
    private boolean isInfoWindowRedraw = false;
    private boolean isMenuActive = false;
    private boolean isHighwaysEnabled = true;
    private boolean isTollsEnabled = true;
    private boolean isFerriesEnabled = true;
    private boolean isTrafficEnabled = true;
    private boolean isDarkEnabled = false;
    private boolean isMonoEnabled = false;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        context = this;

        searchView = findViewById(R.id.searchView);
        markerButtonsLayout = findViewById(R.id.layout_marker_buttons);
        gson = new Gson();
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
        setAutoCompleteThreshold(searchView);    // show suggestions immediately
        handleIntent(getIntent());
        mapFragment.getMapAsync(this);
    }
    
    private void getLocationPermission() {
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

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
        }
    }

    private void setupInterface() {
        menuButton = findViewById(R.id.button_menu);
        menuButton.setOnClickListener(v -> {
            menuToggler();
        });
        menuButton.setVisibility(View.GONE);

        searchSpinner = findViewById(R.id.spinner_search);
        searchAdapter = new SearchSpinnerAdapter(this, tables);
        searchSpinner.setAdapter(searchAdapter);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTable = parent.getItemAtPosition(position).toString();
                searchAdapter.setSelectedItemPosition(position);
                updateSuggestions("");
                clusterManager.clearItems();
                for (MyItem marker : MyItem.MyItemSorter.sortMyItemsByCode(markers.get(currentTable))) {
                    clusterManager.addItem(marker);
                }
                clusterManager.cluster();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        searchSpinner.setVisibility(View.GONE);

        Animation animLeftMenu = AnimationUtils.loadAnimation(this, R.anim.menu_left_anim);
        animDisappear = AnimationUtils.loadAnimation(this, R.anim.menu_disappear);

        routeButton = findViewById(R.id.button_route);
        routeButton.setOnClickListener(v -> {
            routeMenu.setVisibility(View.VISIBLE);
            routeMenu.startAnimation(animLeftMenu);
            routeButton.setVisibility(View.GONE);
            clearMenus("R");
            clearMap();
        });
        routeButton.setVisibility(View.GONE);

        routeMenu = findViewById(R.id.menu_route);
        routeMenu.setVisibility(View.GONE);

        Spinner trafficSpinner = findViewById(R.id.spinner_traffic);
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

        SwitchCompat switchHighways = findViewById(R.id.switch_highways);
        switchHighways.setChecked(isHighwaysEnabled);
        switchHighways.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighwaysEnabled = isChecked;
            clearMap();
        });

        SwitchCompat switchTolls = findViewById(R.id.switch_tolls);
        switchTolls.setChecked(isTollsEnabled);
        switchTolls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTollsEnabled = isChecked;
            clearMap();
        });

        SwitchCompat switchFerries = findViewById(R.id.switch_ferries);
        switchFerries.setChecked(isFerriesEnabled);
        switchFerries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFerriesEnabled = isChecked;
            clearMap();
        });

        Animation animRightMenu = AnimationUtils.loadAnimation(this, R.anim.menu_right_anim);

        layersButton = findViewById(R.id.button_layers);
        layersButton.setOnClickListener(v -> {
            layersMenu.setVisibility(View.VISIBLE);
            layersMenu.startAnimation(animRightMenu);
            layersButton.setVisibility(View.GONE);
            clearMenus("L");
            clearMap();
        });
        layersButton.setVisibility(View.GONE);

        layersMenu = findViewById(R.id.menu_layers);
        layersMenu.setVisibility(View.GONE);

        Spinner appearanceSpinner = findViewById(R.id.spinner_appearance);
        ArrayAdapter<CharSequence> appearanceAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.appearance_dropdown,
                R.layout.spinner_selected_item);
        appearanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appearanceSpinner.setAdapter(appearanceAdapter);
        appearanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mapMode = GoogleMap.MAP_TYPE_NORMAL;
                        switchToggler(switchDark, true);
                        switchToggler(switchMono, true);
                        break;
                    case 1:
                        mapMode = GoogleMap.MAP_TYPE_TERRAIN;
                        switchToggler(switchDark, false);
                        switchToggler(switchMono, false);
                        break;
                    case 2:
                        mapMode = GoogleMap.MAP_TYPE_SATELLITE;
                        switchToggler(switchDark, false);
                        switchToggler(switchMono, false);
                        break;
                    case 3:
                        mapMode = GoogleMap.MAP_TYPE_HYBRID;
                        switchToggler(switchDark, false);
                        switchToggler(switchMono, false);
                        break;
                    default:
                        Log.i(TAG, "unknown map mode in appearanceSpinner");
                        break;
                }
                mMap.setMapType(mapMode);
                clearMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        SwitchCompat switchTraffic = findViewById(R.id.switch_traffic);
        switchTraffic.setChecked(isTrafficEnabled);
        switchTraffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTrafficEnabled = isChecked;
            mMap.setTrafficEnabled(isChecked);
            clearMap();
        });

        switchDark = findViewById(R.id.switch_dark);
        switchDark.setChecked(isDarkEnabled);
        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDarkEnabled = isChecked;
            setMapStyle();
        });

        switchMono = findViewById(R.id.switch_mono);
        switchMono.setChecked(isMonoEnabled);
        switchMono.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isMonoEnabled = isChecked;
            setMapStyle();
        });

        clearRouteButton = findViewById(R.id.button_clear);
        clearRouteButton.setOnClickListener(v -> clearRoute());

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

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        String[] from = {"location", "address"};
        int[] to = {R.id.suggestion_text, R.id.suggestion_description};
        suggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.suggestion_item, null, from, to, 0);
        searchView.setSuggestionsAdapter(suggestionAdapter);

        searchView.setOnClickListener(v -> searchView.setIconified(false));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "triggered onQueryTextChange");
                updateSuggestions(newText);
                return false;
            }
        });

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

    private void setAutoCompleteThreshold(SearchView searchView) {
        try {
            Field searchAutoCompleteField = SearchView.class.getDeclaredField("mSearchSrcTextView");
            searchAutoCompleteField.setAccessible(true);
            AutoCompleteTextView searchAutoComplete = (AutoCompleteTextView) searchAutoCompleteField.get(searchView);
            searchAutoComplete.setThreshold(0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();    // FIX THIS: add logging
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
                String waypoints = currentItem.getWaypoints();
                LatLng position;
                if (!waypoints.isEmpty()) {
                    position = currentItem.getPositions().get(waypoints.split(",")[waypoints.split(",").length - 1].trim());
                } else {
                    position = marker.getPosition();
                }
                StringBuilder url = new StringBuilder(BranchDirectoryMap.DIR_URL);
                url.append(position.latitude).append(",").append(position.longitude);
                if (!routeMarkers.isEmpty() || currentItem.getWaypoints().split(",").length > 1) {
                    StringBuilder params = waypointCombiner("|", true);
                    if (params.length() > 0) {
                        url.append(params);
                    }
                }
                Log.i(TAG, "url: " + url);
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
        varMap = gson.fromJson(varMapStr, BranchDirectoryMap.VARMAP_TYPE);
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
            switchToggler(switchDark, false);
            switchToggler(switchMono, false);
        }
        mMap.setTrafficEnabled(isTrafficEnabled);
        mMap.setMapType(mapMode);
        Log.i(TAG, "reached end of onMapReady");
        clusterManager.cluster();
    }

    private void performSearch(String query) {
        MyItem correctSuggestion = null;
        if (query.contains("+")) {      // chosen from suggestions list
            query = query.split("\\+", 2)[1].toLowerCase();
            Log.i(TAG, "query 1: " + query);
            for (MyItem suggestion : markers.get(currentTable)) {
                if (suggestion.getSnippet().toLowerCase().equals(query)) {
                    correctSuggestion = suggestion;
                    Log.i(TAG, "case 1: " + suggestion.getSnippet());
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

    private void updateSuggestions(String query) {
        filteredSuggestions.clear();
        for (int i = 0; i < BranchDirectoryMap.SEARCH_LEVELS; i++) {
            filteredSuggestions.add(new ArrayList<>());
        }
        if (!query.isEmpty()) {
            query = query.toLowerCase();
            Log.i(TAG, "query: " + query);
            for (MyItem suggestion : markers.get(currentTable)) {
                boolean found = false;
                // matches branch code
                if (!((String) varMap.get(currentTable).get("code_prefix")).isEmpty()) {
//                    Log.i(TAG, "code prefix: " + varMap.get(currentTable).get("code_prefix"));
                    ArrayList<String> suggestionCodes = new ArrayList<>();
                    String codeDelim = (String) varMap.get(currentTable).get("code_delimiter");
                    if (codeDelim.isEmpty()) {
                        suggestionCodes.add(suggestion.getCode());
                    } else {
                        suggestionCodes.addAll(Arrays.asList(suggestion.getCode().split((String) varMap.get(currentTable).get("code_delimiter"), 0)));
                    }
                    String prefix = ((String) varMap.get(currentTable).get("code_prefix")).toLowerCase();
                    int prefixLen = prefix.length();
                    int index = suggestionCodes.get(0).toLowerCase().indexOf(prefix);
//                    Log.i(TAG, "sc: " + suggestionCodes.toString());
//                    Log.i(TAG, "data: " + suggestionCodes.get(0).toLowerCase());
//                    Log.i(TAG, "prefix: " + prefix + " prefixLen: " + prefixLen + " index: " + index);
                    suggestionCodes.set(0, suggestionCodes.get(0).substring(index + prefixLen));
                    for (String code : suggestionCodes) {
                        code = code.toLowerCase();
                        if (code.contains(prefix + query)) {
//                            Log.i(TAG, "updatesuggestion case 1a code: " + code);
                            filteredSuggestions.get(0).add(suggestion);
                            found = true;
                            break;
                        } else if ((prefix + code).substring((prefix + code).length() - Math.min((prefix + code).length(), query.length())).contains(query)) {
//                            Log.i(TAG, "updatesuggestion case 1b code: " + code);
                            filteredSuggestions.get(0).add(suggestion);
                            found = true;
                            break;
                        } else if (code.contains(query)) {
//                            Log.i(TAG, "updatesuggestion case 2 code: " + code);
                            filteredSuggestions.get(1).add(suggestion);
                            found = true;
                            break;
                        } else {
                            index = query.indexOf(prefix);
                            if (index != -1 && code.contains(query.substring(index + prefixLen))) {
//                                Log.i(TAG, "updatesuggestion case 3 code: " + code);
                                filteredSuggestions.get(1).add(suggestion);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    // matches branch name
//                    Log.i(TAG, "branch name: " + suggestion.getName());
                    if (suggestion.getName().toLowerCase().contains(query)) {
//                        Log.i(TAG, "updatesuggestion case 4 name: " + suggestion.getName());
                        filteredSuggestions.get(2).add(suggestion);
                        // matches branch address
                    } else if (suggestion.getSnippet().toLowerCase().contains(query)) {
//                        Log.i(TAG, "updatesuggestion case 5 snippet: " + suggestion.getSnippet());
                        filteredSuggestions.get(3).add(suggestion);
                    }
                }
            }
        } else {
//            Log.i(TAG, "currentTable: " + currentTable);
//            for (String key : markers.keySet()) {
//                Log.i(TAG, "key: " + key);
//                for (MyItem item : markers.get(key)) {
//                    Log.i(TAG, "item: " + item);
//                }
//            }
            filteredSuggestions.get(0).addAll(markers.get(currentTable));
        }
//        Log.i(TAG, "suggestion size: " + filteredSuggestions.size());
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "location", "address"});
        for (int i = 0; i < BranchDirectoryMap.SEARCH_LEVELS; i++) {
            for (int j = 0; j < filteredSuggestions.get(i).size(); j++) {
                MyItem suggestion = filteredSuggestions.get(i).get(j);
                cursor.addRow(new Object[]{j, suggestion.getTitle(), suggestion.getSnippet()});
            }
        }
//        Log.i(TAG, "cursor size: " + cursor.getCount());
        suggestionAdapter.changeCursor(cursor);
    }

    private void updateLocationUI() {
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

    private void openMapsApp(Uri uri) {
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

    private void moveButtonsWithMarker(Point screenPosition) {
        int x = screenPosition.x - markerButtonsLayout.getWidth() / 2;
        int y = screenPosition.y + 20;

        markerButtonsLayout.setX(x);
        markerButtonsLayout.setY(y);

        markerButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void setMapStyle() {
        int styleKey = (isDarkEnabled ? 1 : 0) + (isMonoEnabled ? 2 : 0);
        Log.i(TAG, "setMapStyle: " + styleKey);
        switch (styleKey) {
            case 0:
                mMap.setMapStyle(styles.get(stylesList.get(0)));
                break;
            case 1:
                mMap.setMapStyle(styles.get(stylesList.get(1)));
                break;
            case 2:
                mMap.setMapStyle(styles.get(stylesList.get(2)));
                break;
            case 3:
                mMap.setMapStyle(styles.get(stylesList.get(3)));
                break;
            default:
                Log.e(TAG, "invalid styleKey in setMapStyle");
                break;
        }
        clearMap();
    }

    private void createMapStyles() {
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

    private void menuToggler() {
        if (!isMenuActive) {
            menuButton.setImageResource(R.drawable.ic_close);
            routeButton.setVisibility(View.VISIBLE);
            layersButton.setVisibility(View.VISIBLE);
            searchView.closeKeyboard();
            searchView.clearFocus();
            isMenuActive = true;
            RelativeLayout.LayoutParams layoutparam = (RelativeLayout.LayoutParams) clearRouteButton.getLayoutParams();
            layoutparam.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutparam.addRule(RelativeLayout.ABOVE, R.id.button_layers);
            clearRouteButton.setLayoutParams(layoutparam);
        } else {
            menuButton.setImageResource(R.drawable.ic_menu);
            routeButton.setVisibility(View.GONE);
            layersButton.setVisibility(View.GONE);
            routeMenu.setVisibility(View.GONE);
            layersMenu.setVisibility(View.GONE);
            isMenuActive = false;
            RelativeLayout.LayoutParams layoutparam = (RelativeLayout.LayoutParams) clearRouteButton.getLayoutParams();
            layoutparam.removeRule(RelativeLayout.ABOVE);
            layoutparam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            clearRouteButton.setLayoutParams(layoutparam);
        }
    }

    private void switchToggler(SwitchCompat switchButton, boolean enabled) {
        if (enabled){
            switchButton.setEnabled(true);
            switchButton.setTextColor(Color.BLACK);
        } else {
            switchButton.setEnabled(false);
            switchButton.setTextColor(Color.GRAY);
        }
    }

    private StringBuilder waypointCombiner(String delimiter, boolean cutbody) {
        // cutbody is for app calls to restrict all marker waypoints to last one
        StringBuilder waypointsBuilder = new StringBuilder();
        Iterator<String> keyIterator = routeMarkers.keySet().iterator();
        // iterate through routeMarkers
        while (keyIterator.hasNext()) {
            String title = keyIterator.next();
            MyItem thisItem = (MyItem) routeMarkers.get(title).get(1);
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
                    String lastWaypoint = thisItem.getWaypoints().split(",")[thisItem.getWaypoints().split(",").length - 1];
                    waypointIterator = Collections.singletonList(lastWaypoint).iterator();
                } else {
                    waypointIterator = Arrays.asList(thisItem.getWaypoints().split(",")).iterator();
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
                    LatLng position = thisItem.getPositions().get(waypoint.trim());
                    waypointsBuilder.append(position.latitude).append(",")
                            .append(position.longitude);
                }
            }
        }
        // iterate through currentItem waypoints excluding last one which is destination
        if (!currentItem.getWaypoints().isEmpty()) {
            List<String> waypoints = new ArrayList<>(Arrays.asList(currentItem.getWaypoints().split(",")));
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
                LatLng position = currentItem.getPositions().get(waypoint.trim());
                waypointsBuilder.append("via:").append(position.latitude).append(",")
                        .append(position.longitude);
            }
        }
        if (waypointsBuilder.length() > 0) {
            waypointsBuilder.insert(0,"&waypoints=");
        }
        return waypointsBuilder;
    }

    private void getCurrentLocation(LocationUpdateAction callback) {
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

    private void startMarker() {
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
        if ((Boolean) varMap.get(currentTable).get("use_phone") && !currentItem.getPhone().isEmpty()) {
            callMarkerButton.setVisibility(View.VISIBLE);
        } else {
            callMarkerButton.setVisibility(View.GONE);
        }
        // FIX THIS: add ability to add marker to route multiple times
        if (currentItem.getSelected() > -1) {
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
        } else {
            addMarkerButton.setVisibility(View.VISIBLE);
            removeMarkerButton.setVisibility(View.GONE);
        }
        if (locationPermissionGranted) {
            if (!currentItem.getWaypoints().isEmpty()) {
                for (String waypoint : currentItem.getPositions().keySet()) {
//                    Log.i(TAG, "waypoint: " + waypoint);
//                    Log.i(TAG, "name: " + currentItem.getTitle());
//                    Log.i(TAG, "getWaypoints: " + currentItem.getWaypoints());
//                    Log.i(TAG, "getPositions: " + currentItem.getPositions());
                    Marker pos = mMap.addMarker(new MarkerOptions().position(currentItem.getPositions().get(waypoint))
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
            if (currentItem.getWaypoints().isEmpty()) {
                routeMarkers.get(currentMarker.getTitle()).add(new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                intermediates_count++;
            } else {
                for (String waypoint : currentItem.getWaypoints().split(",")) {
                    routeMarkers.get(currentMarker.getTitle()).add(currentItem.getPositions().get(waypoint.trim()));
                    intermediates_count++;
                }
            }
            currentItem.setSelected(0);
            lastSelected = true;
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
            clearRouteButton.setVisibility(View.VISIBLE);
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
            clearRouteButton.setVisibility(View.GONE);
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
                Log.i(TAG, "getKeyByValue: " + entry.getKey());
                return entry.getKey();
            }
        }
        Log.i(TAG, "getKeyByValue: value not found");
        return null;
    }

    @Override
    public void onBackPressed() {
//        Log.i(TAG, "ONBACKPRESSED");
        if (isInfoWindowOpen) {
//            Log.i(TAG, "isInfoWindowOpen is true");
            clearInfoWindow();
//            Log.i(TAG, "isInfoWindowOpen is: " + isInfoWindowOpen);
        } else if (routeMenu.getVisibility() == View.VISIBLE || layersMenu.getVisibility() == View.VISIBLE) {
            clearMenus("");
        } else if (isMenuActive) {
            menuToggler();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                task.cancel(true);
                finishAffinity();
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    private void clearMenus(String side) {
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

    private void hideMenu(View menu, View button) {
        if (menu.getVisibility() == View.VISIBLE) {
            menu.startAnimation(animDisappear);
            menu.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }

    private void clearMap() {
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
            MyItem item = (MyItem) routeMarkers.get(title).get(1);
            item.setSelected(-1);
        }
        routeMarkers.clear();
        intermediates_count = 0;
        clearRouteButton.setVisibility(View.GONE);
        addMarkerButton.setVisibility(View.VISIBLE);
        removeMarkerButton.setVisibility(View.GONE);
        customRenderer.setShouldCluster(true);
        clusterManager.cluster();
        startMarker();
    }

    private void clearPolys() {
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
        polylines.add(mMap.addPolyline(new PolylineOptions().addAll(decodedPolyline).color(Color.BLUE).width(20f).zIndex(1000)));
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

    private int getMapSize(Map<String, List<MyItem>> map) {
        int markersSize = 0;
        for (String table : tables) {
            if (map.containsKey(table)) {
                markersSize += map.get(table).size();
            }
        }
        return markersSize;
    }

    private int getMapSize3D(Map<String, Map<String, Map<String, LatLng>>> map) {
        int markersSize = 0;
        for (String table : map.keySet()) {
            for (String name : map.get(table).keySet()) {
                markersSize += map.get(table).get(name).size();
            }
        }
        return markersSize;
    }

    public boolean mapHasInnerElements(Map<String, Map<String, List<String>>> map) {
        for (String key : map.keySet()) {
            if (!map.get(key).isEmpty()) {
                return true;
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

    public void addToMap(Map<String, Map<String, Object>> outer, Map<String, List<MyItem>> inner) {
        for (String table : inner.keySet()) {
            if (outer.containsKey(table)) {
//                Log.i(TAG, "outer table: " + outer.get(table));
//                Log.i(TAG, "inner table: " + inner.get(table));
                ArrayList<String> outerList = (ArrayList<String>) outer.get(table).get("array");
                for (MyItem item : inner.get(table)) {
                    List<String> newItem = Arrays.asList(item.getTitle(), item.getSnippet(), item.getRefined(), item.getPhone(), item.getColour());
//                    Log.i(TAG, "to be added: " + item.getTitle() + ", " + item.getSnippet() + ", " + item.getRefined() + ", " + item.getPhone());
                    int matchIndex = outerList.indexOf(item.getTitle());
                    if (matchIndex > -1) {
//                        Log.i(TAG, "match index: " + matchIndex + " item: " + item.getTitle());
                        outerList.subList(matchIndex, matchIndex + BranchDirectoryMap.NUM_OF_MYITEM_VARS).clear();
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

    // does not work, FIX THIS
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");

//        outState.putSerializable("markers", (Serializable) markers);
//        outState.putString("currentTable", currentTable);

        clearMenus("");
        clearMap();
    }

    // does not work, FIX THIS
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestoreInstanceState");

//        if (savedInstanceState != null) {
//            markers = (Map<String, List<MyItem>>) savedInstanceState.getSerializable("markers");
//            currentTable = savedInstanceState.getString("currentTable");
//        }
    }

    private class CustomClusterRenderer extends DefaultClusterRenderer<MyItem> {
        private boolean shouldCluster = true;

        public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
//            setMinClusterSize(BuildConfig.MIN_CLUSTER_SIZE);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
//            Log.i(TAG, "onBeforeClusterItemRendered");
            markerOptions.title(item.getTitle()).snippet(item.getSnippet());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(item.getHue()));
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onClusterItemRendered(MyItem item, Marker marker) {
            clusterItemMarkerMap.get(currentTable).put(item, marker);
            marker.setTag(item);
            super.onClusterItemRendered(item, marker);
        }

        @Override
        protected void onClusterItemUpdated(@NonNull MyItem item, @NonNull Marker marker) {
//            Log.i(TAG, "onClusterItemUpdated");
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(item.getHue()));
            super.onClusterItemUpdated(item, marker);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<MyItem> cluster) {
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

    private class MapLoaderTask extends AsyncTask<ArrayList<String>, Void, Void> {

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
        private final Gson gson = new Gson();
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
                    }
//                    Log.i(TAG, "place: " + place.toString());
                    for (int i = 0; i < place.size(); i += BranchDirectoryMap.NUM_OF_MYITEM_VARS) {
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
                missingMaps = gson.fromJson(sharedPreferences.getString(BranchDirectoryMap.KEY_LOAD_REDO, ""), mapType);
                for (String table : missingMaps.keySet()) {
                    latchCount += missingMaps.get(table).size();
                }
            }
            if (sharedPreferences.contains(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT)) {
                missingWaypointMaps = gson.fromJson(sharedPreferences.getString(BranchDirectoryMap.KEY_LOAD_REDO_WAYPOINT, ""), mapType);
                for (String table : missingWaypointMaps.keySet()) {
                    for (String name : missingWaypointMaps.get(table).keySet()) {
                        latchCount += missingMaps.get(table).get(name).size();
                    }
                }
            }

            Log.i(TAG, "latchcount 2: " + latchCount);

            if (!databaseExists) {
                Log.i(TAG, "database does not exist");
            } else {
                varMapStr = dbHelper.getVarMapStr(false);
                varMap = gson.fromJson(varMapStr, BranchDirectoryMap.VARMAP_TYPE);
//                Log.i(TAG, "new markers: " + markers.toString());
//                Log.i(TAG, "varMap 1: " + varMap);
                for (String table : varMap.keySet()) {
//                    Log.i(TAG, "tablesSet 2: " + tablesSet);
//                    Log.i(TAG, "table 2: " + table);
                    if (tablesSet.add(table)) {
                        tables.add(table);
                    }
                }
                Log.i(TAG, "return size: " + getMapSize(markers));
            }

            latch = new CountDownLatch(latchCount);

            if (mapHasInnerElements(missingMaps)) {
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
                    if (databaseExists) {
                        ArrayList<String> entry = (ArrayList<String>) varMap.get(table).get("array");
                        if (entry != null) {
                            List<String> geocoderData = geocoderMap.get(name);
                            if (geocoderData != null) {
                                ArrayList<String> subList = new ArrayList<>(Arrays.asList(name, geocoderData.get(1), geocoderData.get(2)));
                                Log.i(TAG, "entry: " + entry);
                                Log.i(TAG, "geocoderData: " + geocoderData);
                                Log.i(TAG, "subList: " + subList);
                                int startIndex = Collections.indexOfSubList(entry, subList);
                                if (startIndex != -1) {
                                    address = "";
                                    subList.add(geocoderData.get(3));
                                    startIndex = Collections.indexOfSubList(entry, subList);
                                    if (startIndex != -1) {
                                        Log.i(TAG, "entry skipped");
//                                        Log.i(TAG, "   geocoderData: " + geocoderData);
                                        latch.countDown();
                                        continue;
                                    } else {
                                        Log.i(TAG, "entry address match: " + subList);
                                        populateMaps(table, name, delim, 1000, 1000);
                                    }
                                } else {
                                    Log.i(TAG, "entry not found: " + subList);
                                }
                            }
                        }
                    }
                    StringBuilder finalAddress = new StringBuilder(address);
                    String geoRegion = (String) varMap.get(table).get("geocode_region");
                    if (!address.contains("+") && !geoRegion.isEmpty()) {
                        finalAddress.append(", " + geoRegion);
                    }
//                    Log.i(TAG, "finalAddress: " + finalAddress);
                    executor.submit(() -> {
                        try {
                            if (finalAddress.length() != 0) {
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
                                Log.i(TAG, "No address found for: " + name + ", this is unexpected");
                            }
                            long sleepTime = BuildConfig.BASE_DELAY_MS + random.nextInt(BuildConfig.RANDOM_DELAY_MS + 1);
                            TimeUnit.MILLISECONDS.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
            if (mapHasInnerElements(missingWaypointMaps)) {
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
                        for (String pluscode : waypointMap.get(name)) {
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
            Log.i(TAG, "markers size: " + getMapSize(markers));
            Log.i(TAG, "latchCount: " + latchCount);
            if (getMapSize(markers) > 0) {
                addToMap(varMap, markers);
            }
            if (getMapSize3D(waypointValues) > 0) {
                addToMapWaypoints(varMap, waypointValues);
            }
            if (mapHasInnerElements(missingMaps)) {
                Log.i(TAG, "missingMaps: " + missingMaps.toString());
            }
            if (mapHasInnerElements(missingWaypointMaps)) {
                Log.i(TAG, "missingWaypointMaps: " + missingWaypointMaps.toString());
            }
//            if (latchCount > getMapSize(markers)) {
//                Log.i(TAG, "latchCount > getMapSize(markers), FIX THIS");
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            Log.i(TAG, "Elapsed time doInBackground: " + (System.currentTimeMillis() - start));
            if (mapHasInnerElements(missingMaps)) {
                editor.putBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false).apply();
                Log.i(TAG, "tables: " + tables.toString());
                for (String table : missingMaps.keySet()) {
                    for (String title : missingMaps.get(table).keySet()) {
                        Log.i(TAG, "marker not found: " + title + " in table: " + table);
                        Log.i(TAG, "data: " + missingMaps.get(table).get(title).toString());
                    }
                }

                dialogUtils.showOkDialog(context, getString(R.string.warning), getString(R.string.marker_error),
                        (dialog, id) -> dialog.dismiss());

                String jsonString = gson.toJson(missingMaps);
//                Log.i(TAG, "jsonString: " + jsonString);
                editor.putString(BranchDirectoryMap.KEY_LOAD_REDO, jsonString).apply();
            } else {
                editor.remove(BranchDirectoryMap.KEY_LOAD_REDO);
                editor.putBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, true).apply();
            }

            Collections.sort(tables);
            searchAdapter.notifyDataSetChanged();
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
                markers = dbHelper.getAllLocations(db);
                dbHelper.deleteTable(db, "varmap");
                dbHelper.createTable(db, "varMap.create");  // special command for varmap specifically
                String jsonString = gson.toJson(varMap);
                ContentValues varMapStrValues = new ContentValues();
                varMapStrValues.put("string", jsonString);
                db.insert("varmap", null, varMapStrValues);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "--EXCEPTION-- " + e.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
            if (BuildConfig.EMBEDDED_DB.isEmpty() && BuildConfig.EXPORT_DB
                    && sharedPreferences.getBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false)) {
                dbHelper.exportDatabase(BuildConfig.DATABASE_NAME);
            }
            for (String table : tables) {
                clusterItemMarkerMap.put(table, new HashMap<>());
            }
            if (!BuildConfig.DEFAULT_FILE.isEmpty()) {
                currentTable = BuildConfig.DEFAULT_FILE.substring(0,
                        BuildConfig.DEFAULT_FILE.contains(".") ? BuildConfig.DEFAULT_FILE.lastIndexOf(".") : BuildConfig.DEFAULT_FILE.length());
            } else {
                currentTable = tables.get(0);
            }
            int position = Collections.binarySearch(tables, currentTable);
            searchSpinner.setSelection(position);
            searchAdapter.setSelectedItemPosition(position);
            for (MyItem marker : MyItem.MyItemSorter.sortMyItemsByCode(markers.get(currentTable))) {
                clusterManager.addItem(marker);
            }

            searchSpinner.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            loadTextView.setVisibility(View.GONE);
            menuButton.setVisibility(View.VISIBLE);

            Toast.makeText(context, getString(R.string.finished), Toast.LENGTH_SHORT).show();
        }

        private void removeFromVarMap(String table, String name) {
//            Log.i(TAG, "removeFromVarMap table: " + table + ", name: " + name);
            if (!varMap.containsKey(table)) {
                Log.i(TAG, "varMap doesn't contain table " + table);
                return;
            }
            if (!varMap.get(table).containsKey("array")) {
                Log.i(TAG, "varMap table " + table + " doesn't contain an array");
            }
            ArrayList<String> markerList = (ArrayList<String>) varMap.get(table).get("array");
            for (int i = 0; i < markerList.size(); i += BranchDirectoryMap.NUM_OF_MYITEM_VARS) {
                if (markerList.get(i).equals(name)) {
//                    Log.i(TAG, "removing marker: " + name + " from table: " + table);
                    markerList.subList(i, i + BranchDirectoryMap.NUM_OF_MYITEM_VARS).clear();
//                    Log.i(TAG, "new varMap(" + table + ") array: " + markerList);
                    return;
                }
            }
        }

        private void populateMaps(String table, String name, String delim, double latitude, double longitude) {
//            Log.i(TAG, "populateMaps: " + table + ", " + name + ", " + delim + ", " + latitude + ", " + longitude);
            Map<String, List<String>> geocoderMap = geocoderMaps.get(table);
//            Log.i(TAG, "geocoderMap: " + geocoderMap);
            String nameCode = delim.isEmpty() ? "" : name.split(delim, 2)[0];
            String nameSnippet = "";
            if (name.split(delim, 2).length > 1) {
                nameSnippet = delim.isEmpty() ? name : name.split(delim, 2)[1];
            }
//            Log.i(TAG, "get(2): " + geocoderMap.get(name).get(2));
            String waypoints = geocoderMap.get(name).get(2).split("\\+").length > 2
                    ? geocoderMap.get(name).get(2) : "";
            if (!waypoints.isEmpty()) {
                waypoints = waypoints.substring(0, waypoints.lastIndexOf(","));
            }
            ContentValues value = new ContentValues();
            value.put("latitude", latitude);
            value.put("longitude", longitude);
            value.put("code", nameCode);
            value.put("name", nameSnippet);
            value.put("address", geocoderMap.get(name).get(1));
            value.put("refined", geocoderMap.get(name).get(2));
            value.put("waypoints", waypoints);
            value.put("positions", "");
            value.put("phone", geocoderMap.get(name).get(3));
            value.put("colour", geocoderMap.get(name).get(4));
            values.get(table).add(value);
            markers.get(table).add(new MyItem(latitude, longitude, nameCode, nameSnippet,
                    geocoderMap.get(name).get(1), geocoderMap.get(name).get(2), waypoints, geocoderMap.get(name).get(3), geocoderMap.get(name).get(4)));
        }

        private void populateWaypoints(String table, String name, String pluscode) {
            List<ContentValues> cv = values.get(table);
            for (ContentValues value : cv) {
                if (value.containsKey("code") && value.containsKey("name")) {
                    String vcode = value.getAsString("code");
                    String vname = value.getAsString("name");
                    String vtitle = vcode + (vcode.isEmpty() ? "" : vname.isEmpty() ? "" : " ") + (vname.isEmpty() ? "" : vname);
                    if (vtitle.equals(name)) {
                        value.put("positions", gson.toJson(waypointValues.get(table).get(name), BranchDirectoryMap.POSITIONS_TYPE));
                        return;
                    }
                }
            }
            List<MyItem> items = markers.get(table);
            for (MyItem item : items) {
                if (item.getTitle().equals(name)) {
                    item.setPositions(pluscode, waypointValues.get(table).get(name).get(pluscode));
                    Log.i(TAG, "added waypoint to title: " + name + " positions: " + item.getPositions());
                    return;
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
    }

    private void GetInformationTaskCreator() {
        getInformationTask = new GetInformationTask(this);
        getInformationTask.execute();
    }

    private class GetInformationTask extends AsyncTask<Void, Void, Object[]> {

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
                } else {
                    String lastWaypoint = currentItem.getWaypoints().split(",")[currentItem.getWaypoints().split(",").length - 1].trim();
                    markerLat = currentItem.getPositions().get(lastWaypoint).latitude;
                    markerLng = currentItem.getPositions().get(lastWaypoint).longitude;
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
                    if (!routeMarkers.isEmpty() || currentItem.getWaypoints().split(",").length > 1) {
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
                            MyItem item = (MyItem) routeMarkers.get(title).get(1);
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
                                Iterator<String> waypointIterator = Arrays.asList(item.getWaypoints().split(",")).iterator();
                                while (waypointIterator.hasNext()) {
                                    String waypoint = waypointIterator.next();
                                    JsonObject waypointObject = new JsonObject();
                                    JsonObject locationObject = new JsonObject();
                                    JsonObject latLngObject = new JsonObject();

                                    latLngObject.addProperty("latitude", item.getPositions().get(waypoint.trim()).latitude);
                                    latLngObject.addProperty("longitude", item.getPositions().get(waypoint.trim()).longitude);

                                    locationObject.add("latLng", latLngObject);
                                    waypointObject.add("location", locationObject);

                                    if (waypointIterator.hasNext()) {
                                        waypointObject.addProperty("via", true);
                                    }

                                    intermediatesArray.add(waypointObject);
                                }
                            }
                        }
//                        for (String title : routeMarkers.keySet()) {
//                            JsonObject waypointObject = new JsonObject();
//                            JsonObject locationObject = new JsonObject();
//                            JsonObject latLngObject = new JsonObject();
//
//                            latLngObject.addProperty("latitude", ((LatLng) routeMarkers.get(title).get(2)).latitude);
//                            latLngObject.addProperty("longitude", ((LatLng) routeMarkers.get(title).get(2)).longitude);
//
//                            locationObject.add("latLng", latLngObject);
//                            waypointObject.add("location", locationObject);
//
//                            // "via" is optional, via implies that the waypoint is not a stop
////                            waypointObject.addProperty("via", true);
//
//                            intermediatesArray.add(waypointObject);
//                        }
                    }
                    if (!currentItem.getWaypoints().isEmpty()) {
                        String[] waypoints = currentItem.getWaypoints().split(",");
                        for (String waypoint : Arrays.copyOfRange(waypoints, 0, waypoints.length - 1)) {
                            JsonObject waypointObject = new JsonObject();
                            JsonObject locationObject = new JsonObject();
                            JsonObject latLngObject = new JsonObject();

                            latLngObject.addProperty("latitude", currentItem.getPositions().get(waypoint.trim()).latitude);
                            latLngObject.addProperty("longitude", currentItem.getPositions().get(waypoint.trim()).longitude);

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
//                    Log.i(TAG, "computeRoutesRequestBody: " + root.toString());

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
                                String responseString = computeRoutesResponse.body().string();
//                                Log.i(TAG, "responseString: " + responseString);
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

                                    Log.i(TAG, "distance: " + totalDistanceMeters);
                                    Log.i(TAG, "unaware: " + totalStaticSeconds);
                                    Log.i(TAG, "aware: " + totalSeconds);

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
                                        trafficColour = new ForegroundColorSpan(getResources().getColor(R.color.orange));
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

                                    return new Object[]{formattedDistance, formattedDuration, formatComputeRouteDuration(totalSeconds, true), allLegPolys, spannable};
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
        }

        private String formatDistanceForLocale(long distanceMeters) {
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

        private boolean useMiles(String country) {
            return "US".equalsIgnoreCase(country) ||
                    "LR".equalsIgnoreCase(country) ||
                    "MM".equalsIgnoreCase(country) ||
                    "GB".equalsIgnoreCase(country);
        }

        private String formatComputeRouteDuration(long totalSeconds, boolean returnArrivalTime) {
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
    }
}
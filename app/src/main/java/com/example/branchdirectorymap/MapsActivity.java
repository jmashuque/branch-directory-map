package com.example.branchdirectorymap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.io.Serializable;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowCloseListener {

    private static final String TAG = "SYS-MAPS";
    private static final int REQUEST_CODE = 102;
    private BranchDirectoryMap app;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DialogUtils dialogUtils;
    private boolean locationPermissionGranted = false;
    private String linkApiKey;
    private String varMapStr;
    private double[] trafficMetrics;
    private Map<String, Map<String, Object>> varMap;
    private ArrayList<String> tables = new ArrayList<>();
    private HashSet<String> tablesSet = new HashSet<>();
    private MapLoaderTask task;
    private GetInformationTask getInformationTask;
    private Location lastKnownLocation;
    private LocationCallback locationCallback;
    private ImageButton menuButton;
    private Spinner searchSpinner;
    private TextView loadTextView;
    private RelativeLayout mapLayout;
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
    private FloatingActionButton clearRouteButton;
    private String infoText;
    private SpannableString trafficText;
    private ClusterManager<MyItem> clusterManager;
    private CustomClusterRenderer customRenderer;
    private Map<String, List<MyItem>> markers;
    private Marker currentMarker;
    private Marker tempMarker;
    private MyItem currentItem;
    private Map<String, Map<MyItem, Marker>> clusterItemMarkerMap = new HashMap<>();
    private SupportMapFragment mapFragment;
    private CustomSearchView searchView;
    private SimpleCursorAdapter suggestionAdapter;
    private ArrayList<ArrayList<MyItem>> filteredSuggestions = new ArrayList<>();
    private Map<String, ArrayList<Object>> routeMarkers = new LinkedHashMap<>();
    private View mWindow;
    private LocationDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String distance;
    private String duration;
    private String oldDistance;
    private String oldDuration;
    private List<List<String>> encodedPolyline;
    private List<List<String>> oldPolyline;
    private List<Polyline> polylines = new ArrayList<>();
    private SpannableString oldTrafficText;
    private boolean lastSelected;
    private Animation animDisappear;
    private String currentTable;
    private long start;
    private String trafficMode = "Best Guess";
    private static int mapMode = GoogleMap.MAP_TYPE_NORMAL;
    private boolean isMenuActive = false;
    private boolean isHighwaysEnabled = true;
    private boolean isTollsEnabled = true;
    private boolean isFerriesEnabled = true;
    private boolean isTrafficEnabled = true;
    private boolean isDarkEnabled = false;
    private static boolean infoWindowOpen = false;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        app = (BranchDirectoryMap) getApplication();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.searchView);
        mapLayout = findViewById(R.id.layout_map);
        markerButtonsLayout = findViewById(R.id.layout_marker_buttons);
//        markerButtonsLayout.setVisibility(View.GONE);
        dbHelper = new LocationDatabaseHelper(this);
        dialogUtils = new DialogUtils(this);
//        varMap = null;

        sharedPreferences = getSharedPreferences(app.SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (!sharedPreferences.contains(app.KEY_LOAD_FINISHED)) {
            editor.putBoolean(app.KEY_LOAD_FINISHED, false).apply();
        }

        String[] trafficMetricsStr = BuildConfig.TRAFFIC_METRICS.split(",");
        trafficMetrics = new double[trafficMetricsStr.length];
        for (int i = 0; i < trafficMetricsStr.length; i++) {
            trafficMetrics[i] = Double.parseDouble(trafficMetricsStr[i].trim());
//            Log.i(TAG, "trafficMetrics[" + i + "]: " + trafficMetrics[i]);
        }

        setupInterface();
        setupSearchView();
        setAutoCompleteThreshold(searchView, 0);    // show suggestions immediately
        handleIntent(getIntent());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.i(TAG, "locationResult is null");
                return;
            }
            for (Location location : locationResult.getLocations()) {
                lastKnownLocation = location;
                break;
            }
            }
        };
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
            public void onNothingSelected(AdapterView<?> parent) {

            }
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
            clearStuff();
        });
        routeButton.setVisibility(View.GONE);

        routeMenu = findViewById(R.id.menu_route);
        routeMenu.setVisibility(View.GONE);

        Spinner trafficSpinner = findViewById(R.id.spinner_traffic);
        ArrayAdapter<CharSequence> trafficAdapter;
        if (BuildConfig.USE_ADVANCED_ROUTING) {
            trafficAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.traffic_adv_dropdown,
                    R.layout.spinner_selected_item);
        } else {
            trafficMode = "Optimal Aware";
            trafficAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.traffic_dropdown,
                    R.layout.spinner_selected_item);
        }
        trafficAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trafficSpinner.setAdapter(trafficAdapter);
        trafficSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trafficMode = parent.getItemAtPosition(position).toString();
                Log.i(TAG, "trafficMode: " + trafficMode);
                clearStuff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        trafficSpinner.setSelection(Collections.binarySearch(Arrays.asList(getResources().getStringArray(R.array.traffic_dropdown)), trafficMode));

        SwitchCompat switchHighways = findViewById(R.id.switch_highways);
        switchHighways.setChecked(isHighwaysEnabled);
        switchHighways.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHighwaysEnabled = isChecked;
                clearStuff();
            }
        });

        SwitchCompat switchTolls = findViewById(R.id.switch_tolls);
        switchTolls.setChecked(isTollsEnabled);
        switchTolls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTollsEnabled = isChecked;
                clearStuff();
            }
        });

        SwitchCompat switchFerries = findViewById(R.id.switch_ferries);
        switchFerries.setChecked(isFerriesEnabled);
        switchFerries.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFerriesEnabled = isChecked;
                clearStuff();
            }
        });

        Animation animRightMenu = AnimationUtils.loadAnimation(this, R.anim.menu_right_anim);

        layersButton = findViewById(R.id.button_layers);
        layersButton.setOnClickListener(v -> {
            layersMenu.setVisibility(View.VISIBLE);
            layersMenu.startAnimation(animRightMenu);
            layersButton.setVisibility(View.GONE);
            clearMenus("L");
            clearStuff();
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
                        switchDark.setEnabled(true);
                        switchDark.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        mapMode = GoogleMap.MAP_TYPE_TERRAIN;
                        switchDark.setEnabled(false);
                        switchDark.setTextColor(Color.GRAY);
                        break;
                    case 2:
                        mapMode = GoogleMap.MAP_TYPE_SATELLITE;
                        switchDark.setEnabled(false);
                        switchDark.setTextColor(Color.GRAY);
                        break;
                    case 3:
                        mapMode = GoogleMap.MAP_TYPE_HYBRID;
                        switchDark.setEnabled(false);
                        switchDark.setTextColor(Color.GRAY);
                }
                mMap.setMapType(mapMode);
                clearStuff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where nothing is selected
            }
        });
        appearanceSpinner.setSelection(0);

        SwitchCompat switchTraffic = findViewById(R.id.switch_traffic);
        switchTraffic.setChecked(isTrafficEnabled);
        switchTraffic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTrafficEnabled = isChecked;
                mMap.setTrafficEnabled(isChecked);
                clearStuff();
            }
        });

        switchDark = findViewById(R.id.switch_dark);
        switchDark.setChecked(isDarkEnabled);
        switchDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDarkEnabled = isChecked;
                if (isDarkEnabled) {
                    setNightMode();
                } else {
                    mMap.setMapStyle(null);
                }
                clearStuff();
            }
        });

        clearRouteButton = findViewById(R.id.button_clear);
        clearRouteButton.setOnClickListener(v -> {
            clearRoute();
        });

        viewMarkerButton = findViewById(R.id.button_marker_view);
        viewMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = currentItem.getRefined().contains("+") ? currentItem.getRefined() : currentItem.getSnippet() + ", " + currentItem.getRefined();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        addMarkerButton = findViewById(R.id.button_marker_add);
        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMarkerToRoute();
            }
        });

        removeMarkerButton = findViewById(R.id.button_marker_remove);
        removeMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeMarkerFromRoute();
            }
        });
        removeMarkerButton.setVisibility(View.GONE);

        callMarkerButton = findViewById(R.id.button_marker_call);
        callMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentItem.getPhone()));
                startActivity(intent);
            }
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

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

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
                clearStuff();
                Log.i(TAG, "triggered setOnQueryTextFocusChangeListener");
                updateSuggestions("");
                searchView.setQuery(searchView.getQuery().toString(), false);
            }
        });
        searchView.setVisibility(View.GONE);
    }

    private void setAutoCompleteThreshold(SearchView searchView, int threshold) {
        try {
            Field searchAutoCompleteField = SearchView.class.getDeclaredField("mSearchSrcTextView");
            searchAutoCompleteField.setAccessible(true);
            AutoCompleteTextView searchAutoComplete = (AutoCompleteTextView) searchAutoCompleteField.get(searchView);
            searchAutoComplete.setThreshold(threshold);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();    // FIX THIS: add logging
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (routeMenu.getVisibility() == View.VISIBLE || layersMenu.getVisibility() == View.VISIBLE) {
                    clearMenus("");
                }
                if (infoWindowOpen) {
                    clearStuff();
                }
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
        });

        clusterManager = new ClusterManager<MyItem>(this, mMap);
        customRenderer = new CustomClusterRenderer(this, mMap, clusterManager);
        clusterManager.setRenderer(customRenderer);
        mWindow = getLayoutInflater().inflate(R.layout.info_window, null);

        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnInfoWindowCloseListener(this);
        mMap.setOnCameraMoveListener(() -> {
            if (currentMarker != null) {
                Projection projection = mMap.getProjection();
                Point screenPosition = projection.toScreenLocation(currentMarker.getPosition());
                moveButtonsWithMarker(screenPosition);
            }
        });

        clusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                Log.i(TAG, "triggered getinfocontents");
                return null;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                Log.i(TAG, "triggered getinfowindow");

                TextView titleTextView = mWindow.findViewById(R.id.title);
                titleTextView.setText(marker.getTitle());

                TextView snippetTextView = mWindow.findViewById(R.id.snippet);
                snippetTextView.setText(marker.getSnippet());

                TextView infoTextView = mWindow.findViewById(R.id.information);
                infoTextView.setText(infoText);

                TextView trafficTextView = mWindow.findViewById(R.id.traffic);
                trafficTextView.setText(trafficText);

                Button directionsButton = mWindow.findViewById(R.id.directionsButton);

                infoWindowOpen = true;

                return mWindow;
            }
        });
        clusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Log.i(TAG, "triggered onInfoWindowClick");

                LatLng markerPosition = marker.getPosition();
                String url = app.DIR_URL + markerPosition.latitude + "," + markerPosition.longitude;

                if (!routeMarkers.isEmpty()) {
                    StringBuilder waypointsBuilder = new StringBuilder();
                    Iterator<String> keyIterator = routeMarkers.keySet().iterator();
                    while (keyIterator.hasNext()) {
                        String title = keyIterator.next();
                        if (waypointsBuilder.length() > 0) {
                            waypointsBuilder.append("|");
                        }
                        Marker thisMarker = (Marker) routeMarkers.get(title).get(0);
                        if (!keyIterator.hasNext() && thisMarker.equals(currentMarker)) {
                            Log.i(TAG, "last routeMarker is currentMarker");
                            break;
                        }
                        LatLng waypointPosition = thisMarker.getPosition();
                        waypointsBuilder.append(waypointPosition.latitude).append(",")
                                .append(waypointPosition.longitude);
                    }
                    if (waypointsBuilder.length() > 0) {
                        url += "&waypoints=" + waypointsBuilder;
                    }
                }

                StringBuilder avoidOptions = new StringBuilder();
                if (!isTollsEnabled) avoidOptions.append("tolls|");
                if (!isHighwaysEnabled) avoidOptions.append("highways|");
                if (!isFerriesEnabled) avoidOptions.append("ferries|");
                if (avoidOptions.length() > 0) {
                    avoidOptions.setLength(avoidOptions.length() - 1);
                    url += "&avoid=" + avoidOptions;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        getLocationPermission();

        loadTextView = findViewById(R.id.textview_load);
        loadTextView.setVisibility(View.VISIBLE);

        varMapStr = getIntent().getStringExtra("places");
        Gson gson = new Gson();
        Type mapType = new TypeToken<ConcurrentHashMap<String, Map<String, Object>>>() {}.getType();
        varMap = gson.fromJson(varMapStr, mapType);
//        Log.i(TAG, "varMap from places: " + varMap);

        task = new MapLoaderTask(this);
        if (!sharedPreferences.getBoolean(app.KEY_APIKEY_LOADED, false)) {
            Log.i(TAG, "fetchGeocodeApiKey started");
            Secrets.fetchGeocodeApiKey(this, new Secrets.OnApiKeyReceivedListener() {
                @Override
                public void onApiKeyReceived(boolean keyReceived) {
                    if (keyReceived) {
                        Log.i(TAG, "fetchGeocodeApiKey successful");
                        editor.putBoolean(app.KEY_APIKEY_LOADED, true).apply();
                        task.execute();
                    } else {
                        Log.i(TAG, "fetchGeocodeApiKey failed/timed out");
                        dialogUtils.showOkDialog("Warning", getString(R.string.key_error),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                }
            });
        } else {
            task.execute();
        }

        clusterManager.setOnClusterItemClickListener(clusterItem -> {
            currentMarker = clusterItemMarkerMap.get(currentTable).get(clusterItem);
            if (currentMarker != null) {
                Log.i(TAG, "triggered setOnClusterItemClickListener");
                Log.i(TAG, "marker: " + currentMarker.getTitle());
                startMarker();
            } else {
                Log.i(TAG, "marker is null at setOnClusterItemClickListener");
            }
            return true;
        });

        updateLocationUI();
        mMap.setTrafficEnabled(isTrafficEnabled);
        mMap.setMapType(mapMode);
        Log.i(TAG, "reached end of mapready");
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        locationPermissionGranted = false;
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation(this::centerOnUser);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
//                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    private void moveButtonsWithMarker(Point screenPosition) {
        int x = screenPosition.x - markerButtonsLayout.getWidth() / 2;
        int y = screenPosition.y + 20;

        markerButtonsLayout.setX(x);
        markerButtonsLayout.setY(y);

        markerButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void setNightMode() {
        try {
            InputStream inputStream = getAssets().open("dark_style.json");
            String style = new Scanner(inputStream).useDelimiter("\\A").next();
            mMap.setMapStyle(new MapStyleOptions(style));
        } catch (IOException e) {
            Log.i(TAG, "Error loading night mode style");
        }
    }

    private void performSearch(String query) {
        MyItem correctSuggestion = null;
        if (query.contains("+")) {      // chosen from suggestions list
            query = query.split("\\+", 2)[1].toLowerCase();
            Log.i(TAG, "query 1: " + query);
            breakPoint1:
            for (MyItem suggestion : markers.get(currentTable)) {
                if (suggestion.getSnippet().toLowerCase().equals(query)) {
                    correctSuggestion = suggestion;
                    Log.i(TAG, "case 1: " + suggestion.getSnippet());
                    break breakPoint1;
                }
            }
        } else {    // chosen based on query
            int filteredSize = filteredSuggestions.get(0).size() + filteredSuggestions.get(1).size()
                    + filteredSuggestions.get(2).size();
            if (filteredSize < 1) {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Too many results, please select from suggestions", Toast.LENGTH_SHORT).show();
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
            Log.i(TAG, "---Unknown result: not programmed---");
        }
    }

    private void updateSuggestions(String query) {
        filteredSuggestions.clear();
        for (int i = 0; i < app.SEARCH_LEVELS; i++) {
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
        for (int i = 0; i < app.SEARCH_LEVELS; i++) {
            for (int j = 0; j < filteredSuggestions.get(i).size(); j++) {
                MyItem suggestion = filteredSuggestions.get(i).get(j);
                cursor.addRow(new Object[]{j, suggestion.getTitle(), suggestion.getSnippet()});
            }
        }
//        Log.i(TAG, "cursor size: " + cursor.getCount());
        suggestionAdapter.changeCursor(cursor);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(),
                lastKnownLocation.getLongitude()), 15f));
    }

    private void startMarker() {
        if (currentMarker == null) {
            Log.i(TAG, "marker is null at startMarker");
            return;
        }
        currentItem = getKeyByValue(clusterItemMarkerMap.get(currentTable), currentMarker);
        clearMenus("");
        infoText = "Loading...";
        trafficText = new SpannableString("Loading...");
        currentMarker.showInfoWindow();
        if (!currentItem.getPhone().isEmpty()) {
            callMarkerButton.setVisibility(View.VISIBLE);
        } else {
            callMarkerButton.setVisibility(View.GONE);
        }
        if (currentItem.getSelected()) {
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
//            lastSelected = true;
        } else {
            addMarkerButton.setVisibility(View.VISIBLE);
            removeMarkerButton.setVisibility(View.GONE);
//            lastSelected = false;
        }
        getCurrentLocation(this::GetInformationTaskCreator);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.i(TAG, "animateCamera onFinish");
                Projection projection = mMap.getProjection();
                if (currentMarker == null) {
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
        if (routeMarkers.size() < BuildConfig.INTERMEDIATE_STEPS) {
            routeMarkers.put(currentMarker.getTitle(), new ArrayList<>());
            routeMarkers.get(currentMarker.getTitle()).add(currentMarker);
            routeMarkers.get(currentMarker.getTitle()).add(currentItem);
            routeMarkers.get(currentMarker.getTitle()).add(new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
            currentItem.setSelected(true);
            lastSelected = true;
            addMarkerButton.setVisibility(View.GONE);
            removeMarkerButton.setVisibility(View.VISIBLE);
            clearRouteButton.setVisibility(View.VISIBLE);
            customRenderer.setShouldCluster(false);
            clusterManager.cluster();
        } else {
            Toast.makeText(this, "Maximum number of intermediate steps reached", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeMarkerFromRoute() {
        routeMarkers.remove(currentMarker.getTitle());
        currentItem.setSelected(false);
        lastSelected = false;
        addMarkerButton.setVisibility(View.VISIBLE);
        removeMarkerButton.setVisibility(View.GONE);
        if (routeMarkers.isEmpty()) {
            clearRouteButton.setVisibility(View.GONE);
            customRenderer.setShouldCluster(true);
        } else {
            getCurrentLocation(this::GetInformationTaskCreator);
            currentMarker.showInfoWindow();
        }
        clusterManager.cluster();
    }

    public void clearRoute() {
        for (String key : routeMarkers.keySet()) {
            MyItem item = (MyItem) routeMarkers.get(key).get(1);
            item.setSelected(false);
        }
//        lastSelected = false;
        routeMarkers.clear();
        clearRouteButton.setVisibility(View.GONE);
        addMarkerButton.setVisibility(View.VISIBLE);
        removeMarkerButton.setVisibility(View.GONE);
        customRenderer.setShouldCluster(true);
        clusterManager.cluster();
        startMarker();
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
        if (infoWindowOpen) {
            clearStuff();
            getInformationTask.cancel(true);
        } else if (routeMenu.getVisibility() == View.VISIBLE || layersMenu.getVisibility() == View.VISIBLE) {
            clearMenus("");
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                task.cancel(true);
                finish();
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please press back again to exit", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void clearMenus(String side) {
        Log.i(TAG, "clearMenus side: " + side);
        switch (side) {
            case "L":
                if (routeMenu.getVisibility() == View.VISIBLE) {
                    routeMenu.startAnimation(animDisappear);
                    routeMenu.setVisibility(View.GONE);
                    routeButton.setVisibility(View.VISIBLE);
                }
                break;
            case "R":
                if (layersMenu.getVisibility() == View.VISIBLE) {
                    layersMenu.startAnimation(animDisappear);
                    layersMenu.setVisibility(View.GONE);
                    layersButton.setVisibility(View.VISIBLE);
                }
                break;
            default:
                if (routeMenu.getVisibility() == View.VISIBLE) {
                    routeMenu.startAnimation(animDisappear);
                    routeMenu.setVisibility(View.GONE);
                    routeButton.setVisibility(View.VISIBLE);
                }
                if (layersMenu.getVisibility() == View.VISIBLE) {
                    layersMenu.startAnimation(animDisappear);
                    layersMenu.setVisibility(View.GONE);
                    layersButton.setVisibility(View.VISIBLE);
                }
        }
    }

    private void clearStuff() {
        Log.i(TAG, "clearStuff");
        clearPolys();
        for (Marker item : clusterManager.getMarkerCollection().getMarkers()) {
            item.hideInfoWindow();
        }
        markerButtonsLayout.setVisibility(View.GONE);
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
        polylines.add(mMap.addPolyline(new PolylineOptions().addAll(decodedPolyline).color(Color.BLUE).width(18f).zIndex(1.0f)));
    }

    @Override
    public void onInfoWindowClose(@NonNull Marker marker) {
        Log.i(TAG, "onInfoWindowClose");
        clearPolys();
        markerButtonsLayout.setVisibility(View.GONE);
        infoWindowOpen = false;
        tempMarker = currentMarker;
        currentMarker = null;
        lastSelected = currentItem.getSelected();
        if (searchView.getQuery().length() != 0) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
        customRenderer.setShouldCluster(true);
//        clusterManager.cluster();
    }

    private int getMapSize(Map<String, List<MyItem>> map) {
        int markersSize = 0;
        for (String table : tables) {
//            Log.i(TAG, "markers in question: " + map.keySet().toString());
//            Log.i(TAG, "table in question: " + table);
//            Log.i(TAG, "table: " + table);
//            Log.i(TAG, "markers.get: " + markers.keySet().toString());
            if (map.containsKey(table)) {
                markersSize += map.get(table).size();
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
            if (outer.keySet().contains(table)) {
//                Log.i(TAG, "outer table: " + outer.get(table));
//                Log.i(TAG, "inner table: " + inner.get(table));
                ArrayList<String> outerList = (ArrayList<String>) outer.get(table).get("array");
                for (MyItem item : inner.get(table)) {
                    List<String> newItem = Arrays.asList(item.getTitle(), item.getSnippet(), item.getRefined(), item.getPhone(), item.getColour());
//                    Log.i(TAG, "to be added: " + item.getTitle() + ", " + item.getSnippet() + ", " + item.getRefined() + ", " + item.getPhone());
                    int matchIndex = outerList.indexOf(item.getTitle());
                    if (matchIndex > -1) {
//                        Log.i(TAG, "match index: " + matchIndex + " item: " + item.getTitle());
                        outerList.subList(matchIndex, matchIndex + app.NUM_OF_MYITEM_VARS).clear();
                        outerList.addAll(matchIndex, newItem);
                    } else {
                        outerList.addAll(newItem);
                    }
                }
//                Log.i(TAG, "new outer array: " + outer.get(table).get("array"));
            } else {
                Log.i(TAG, "WARNING: addToMap - outer table not found");
            }
        }
    }

    // does not work, FIX THIS
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");

        outState.putSerializable("markers", (Serializable) markers);
        outState.putString("currentTable", currentTable);

        clearMenus("");
        clearStuff();
    }

    // does not work, FIX THIS
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestoreInstanceState");

        if (savedInstanceState != null) {
            markers = (Map<String, List<MyItem>>) savedInstanceState.getSerializable("markers");
            currentTable = savedInstanceState.getString("currentTable");
        }
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

        private final MapsActivity activity;
        private boolean databaseExists;
        private Map<String, List<ContentValues>> values;
        private CountDownLatch latch;
        private int latchCount = 0;
        private Map<String, Map<String, List<String>>> geocoderMaps = new HashMap<>();
        private Map<String, Map<String, List<String>>> missingMaps = new HashMap<>();
        private Gson gson = new Gson();
        private LatLngTracker tracker = new LatLngTracker();

        public MapLoaderTask(MapsActivity activity) {
            this.activity = activity;
            databaseExists = LocationDatabaseHelper.DatabaseChecker.isDatabaseExistsAndPopulated(activity, BuildConfig.DATABASE_NAME);
//            Log.i(TAG, "databaseExists: " + databaseExists);
        }

        @Override
        protected Void doInBackground(ArrayList<String>... places) {
            start = System.currentTimeMillis();
            markers = new HashMap<>();
            db = dbHelper.getWritableDatabase();
            values = new HashMap<>();

            if (sharedPreferences.contains(app.KEY_LOAD_FINISHED)) {
                Log.i(TAG, "KEY_LOAD_FINISHED: " + sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false));
            }
//            if (!sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false)) {
//                if (!sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false)) {
//                    dbHelper.clearDatabase();
//                }
            Random random = new Random();

            GeocodingService service = RetrofitClient.getClient(app.BASE_URL).create(GeocodingService.class);;
//                ExecutorService executor = Executors.newSingleThreadExecutor();
            ExecutorService executor = Executors.newFixedThreadPool(BuildConfig.MAX_THREADS);

//                tables.add("ALL SETS");
//                Log.i(TAG, "varMap keyset " + varMap.keySet().toString());
//                for (String key : varMap.keySet()) {
//                    for (String key2 : varMap.get(key).keySet()) {
//                        Log.i(TAG, key + " key: " + key2 + " value: " + varMap.get(key).get(key2).toString());
//                    }
//                }
            if (varMap != null && !varMap.keySet().isEmpty()) {
                Log.i(TAG, "varmap passed, reading");
                for (String table : varMap.keySet()) {
                    Map<String, List<String>> geocoderMap = new HashMap<>();
                    ArrayList<String> place = (ArrayList<String>) varMap.get(table).get("array");
                    Log.i(TAG, "tablesSet: " + tablesSet);
                    Log.i(TAG, "table: " + table);
                    if (tablesSet.add(table)) {
                        tables.add(table);
                    }
//                    Log.i(TAG, "place: " + place.toString());
                    for (int i = 0; i < place.size(); i += app.NUM_OF_MYITEM_VARS) {
                        if ((Boolean) varMap.get(table).get("use_refined")) {
                            if (place.get(i + 2).contains("+")) {
                                geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 2), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                            } else {
                                if (!place.get(i + 2).isEmpty()) {
                                    geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1) + ", " + place.get(i + 2), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                } else {
                                    geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                                }
                            }
                        } else {
                            geocoderMap.put(place.get(i), Arrays.asList(place.get(i + 1), place.get(i + 1), place.get(i + 2), place.get(i + 3), place.get(i + 4)));
                        }
                        latchCount++;
                    }
                    geocoderMaps.put(table, geocoderMap);
                }
            }
            Log.i(TAG, "latchcount 1: " + latchCount);
//            Log.i(TAG, "geocoderMaps: " + geocoderMaps.toString());

            if (sharedPreferences.contains(app.KEY_LOAD_ORDER)) {
                Type mapType = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
                missingMaps = gson.fromJson(sharedPreferences.getString(app.KEY_LOAD_ORDER, ""), mapType);
                for (String table : missingMaps.keySet()) {
                    latchCount += missingMaps.get(table).size();
                }
            }

            Log.i(TAG, "latchcount 2: " + latchCount);

            if (!databaseExists) {
                Log.i(TAG, "database not exists");
            } else {
                varMapStr = dbHelper.getVarMapStr(false);
                varMap = gson.fromJson(varMapStr, app.VARMAP_TYPE);
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
            Log.i(TAG, "geocoderMaps: " + geocoderMaps.toString());
            linkApiKey = Secrets.getStoredGeocodeApiKey(activity.getApplicationContext());
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
                    String finalAddress = address;
//                    Log.i(TAG, "geocoderMap value: " + geocoderMap.get(name).toString());
                    executor.submit(() -> {
                        try {
                            if (!finalAddress.isEmpty()) {
                                // simulate fails to test reloading
//                                String linkTestKey = linkApiKey;
//                                Random rand = new Random();
//                                if (rand.nextInt(100) < 10) {
//                                    linkTestKey="INVALID_FOR_TESTING_PURPOSES";
//                                }
                                service.getGeocode(finalAddress, linkApiKey).enqueue(new Callback<GeocodingResponse>() {
                                    @Override
                                    public void onResponse(Call<GeocodingResponse> call, retrofit2.Response<GeocodingResponse> response) {
                                        if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                                            if (response.body().getResults().size() > 1) {
                                                Log.i(TAG, "More than one result found for: " + finalAddress);
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
//            Log.i(TAG, "varMap: " + varMap.toString());
            if (getMapSize(markers) > 0) {
//                Log.i(TAG, "varmap before: " + varMap.toString());
                addToMap(varMap, markers);
//                Log.i(TAG, "varmap after: " + varMap.toString());
//                Log.i(TAG, "markers: " + markers.toString());
//                Log.i(TAG, "varMap after addToMap: " + varMap.toString());
            }
            if (mapHasInnerElements(missingMaps)) {
                Log.i(TAG, "missingMaps: " + missingMaps.toString());
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
                editor.putBoolean(app.KEY_LOAD_FINISHED, false).apply();
                Log.i(TAG, "tables: " + tables.toString());
                for (String table : missingMaps.keySet()) {
                    for (String title : missingMaps.get(table).keySet()) {
                        Log.i(TAG, "marker not found: " + title + " in table: " + table);
                        Log.i(TAG, "data: " + missingMaps.get(table).get(title).toString());
                    }
                }

                dialogUtils.showOkDialog("Warning", getString(R.string.marker_error),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                String jsonString = gson.toJson(missingMaps);
//                Log.i(TAG, "jsonString: " + jsonString);
                editor.putString(app.KEY_LOAD_ORDER, jsonString).apply();
            } else {
                editor.remove(app.KEY_LOAD_ORDER);
                editor.putBoolean(app.KEY_LOAD_FINISHED, true).apply();
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
//                Log.i(TAG, "markers from dbHelper: " + markers.toString());
                dbHelper.deleteTable(db, "varmap");
//                Log.i(TAG, "tables from db 2: " + dbHelper.getTables(db));
                dbHelper.createTable(db, "varMap.create");
//                Log.i(TAG, "tables from db 3: " + dbHelper.getTables(db));
//                Log.i(TAG, "varMap 2: " + varMap);
                String jsonString = gson.toJson(varMap);
                ContentValues varMapStrValues = new ContentValues();
                varMapStrValues.put("string", jsonString);
                db.insert("varmap", null, varMapStrValues);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.i(TAG, "--EXCEPTION-- " + e.getMessage());
            } finally {
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "getvarmapstr: " + dbHelper.getVarMapStr(true));
            if (BuildConfig.EXPORT_DB && sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false)) {
                dbHelper.exportDatabase(BuildConfig.DATABASE_NAME);
            }
            for (String table : tables) {
                clusterItemMarkerMap.put(table, new HashMap<>());
            }
            if (!BuildConfig.DEFAULT_FILE.isEmpty()) {
                currentTable = BuildConfig.DEFAULT_FILE.substring(0, BuildConfig.DEFAULT_FILE.contains(".") ? BuildConfig.DEFAULT_FILE.lastIndexOf(".") : BuildConfig.DEFAULT_FILE.length());
            } else {
                currentTable = tables.get(0);
            }
            int position = Collections.binarySearch(tables, currentTable);
            searchSpinner.setSelection(position);
            searchAdapter.setSelectedItemPosition(position);
            for (MyItem marker : MyItem.MyItemSorter.sortMyItemsByCode(markers.get(currentTable))) {
                clusterManager.addItem(marker);
            }
            clusterManager.cluster();

            searchSpinner.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            loadTextView.setVisibility(View.GONE);
            menuButton.setVisibility(View.VISIBLE);

            Toast.makeText(activity, "Finished loading markers", Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i < markerList.size(); i += app.NUM_OF_MYITEM_VARS) {
                if (markerList.get(i).equals(name)) {
//                    Log.i(TAG, "removing marker: " + name + " from table: " + table);
                    markerList.subList(i, i + app.NUM_OF_MYITEM_VARS).clear();
//                    Log.i(TAG, "new varMap(" + table + ") array: " + markerList);
                    return;
                }
            }
        }

        private void populateMaps(String table, String name, String delim, double latitude, double longitude) {
//            Log.i(TAG, "populateMaps: " + table + ", " + name + ", " + delim + ", " + latitude + ", " + longitude);
            Map<String, List<String>> geocoderMap = geocoderMaps.get(table);
            String nameCode = delim.isEmpty() ? "" : name.split(delim, 2)[0];
            String nameSnippet = "";
            if (name.split(delim, 2).length > 1) {
                nameSnippet = delim.isEmpty() ? name : name.split(delim, 2)[1];
            }
            ContentValues value = new ContentValues();
            value.put("latitude", latitude);
            value.put("longitude", longitude);
            value.put("code", nameCode);
            value.put("name", nameSnippet);
            value.put("address", geocoderMap.get(name).get(1));
            value.put("refined", geocoderMap.get(name).get(2));
            value.put("phone", geocoderMap.get(name).get(3));
            value.put("colour", geocoderMap.get(name).get(4));
            values.get(table).add(value);
            markers.get(table).add(new MyItem(latitude, longitude, nameCode, nameSnippet,
                    geocoderMap.get(name).get(1), geocoderMap.get(name).get(2), geocoderMap.get(name).get(3), geocoderMap.get(name).get(4)));
        }

        public int updateEntry(String tableName, ContentValues values) {
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
            int rowsAffected = db.update(tableName, values, whereClause, whereArgs);
            return rowsAffected;
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

        private Context context;
        private final Marker marker;
        private final double markerLat;
        private final double markerLng;

        public GetInformationTask(Context context) {
            if (currentMarker != null) {
                marker = currentMarker;
                markerLat = marker.getPosition().latitude;
                markerLng = marker.getPosition().longitude;
            } else {
                Log.i(TAG, "currentMarker is null, FIX THIS");
                marker = null;
                markerLat = 1000;
                markerLng = 1000;
            }
            this.context = context.getApplicationContext();
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
            if (!currentItem.getSelected()) {
                OkHttpClient client = new OkHttpClient();
                if (!BuildConfig.USE_ADVANCED_ROUTING) {
                    String url = app.BASE_URL + "directions/json"
                            + "?origin=" + lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude()
                            + "&destination=" + markerLat + "," + markerLng
                            + "&key=" + linkApiKey;

                    switch (trafficMode) {
                        case "No Traffic":
                            break;
                        case "Best Guess":
                            url += "&traffic_model=best_guess" + "&departure_time=now";
                            break;
                        case "Optimistic":
                            url += "&traffic_model=optimistic" + "&departure_time=now";
                            break;
                        case "Pessimistic":
                            url += "&traffic_model=pessimistic" + "&departure_time=now";
                    }

                    StringBuilder avoidOptions = new StringBuilder();
                    if (!isTollsEnabled) avoidOptions.append("tolls|");
                    if (!isHighwaysEnabled) avoidOptions.append("highways|");
                    if (!isFerriesEnabled) avoidOptions.append("ferries|");
                    if (avoidOptions.length() > 0) {
                        avoidOptions.setLength(avoidOptions.length() - 1);
                        url += "&avoid=" + avoidOptions;
                    }

                    Request request = new Request.Builder().url(url).build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseData = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
                            if ("OK".equals(jsonObject.get("status").getAsString())) {
                                JsonArray routes = jsonObject.getAsJsonArray("routes");
                                if (routes.size() > 0) {
                                    JsonObject route = routes.get(0).getAsJsonObject();
                                    JsonObject leg = route.getAsJsonArray("legs").get(0).getAsJsonObject();
                                    String distance = leg.getAsJsonObject("distance").get("text").getAsString();
                                    String durationStr = "duration";
                                    if (!trafficMode.equals("No Traffic")) {
                                        durationStr += "_in_traffic";
                                    }
                                    String duration = leg.getAsJsonObject(durationStr).get("text").getAsString();
                                    String polyline = route.getAsJsonObject("overview_polyline").get("points").getAsString();
                                    return new String[]{distance, duration, polyline};
                                } else {
                                    Log.i(TAG, "No routes found");
                                }
                            } else {
                                Log.i(TAG, "Error response: " + jsonObject.get("status").getAsString());
                                Log.i(TAG, "Error response: " + jsonObject.get("error_message").getAsString());
                            }
                        }
                    } catch (IOException e) {
                        Log.i(TAG, "HTTP Request Failed: " + e.getMessage());
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

                    if (!routeMarkers.isEmpty()) {
                        JsonArray intermediatesArray = new JsonArray();
                        for (String title : routeMarkers.keySet()) {
                            JsonObject waypointObject = new JsonObject();
                            JsonObject locationObject = new JsonObject();
                            JsonObject latLngObject = new JsonObject();

                            latLngObject.addProperty("latitude", ((LatLng) routeMarkers.get(title).get(2)).latitude);
                            latLngObject.addProperty("longitude", ((LatLng) routeMarkers.get(title).get(2)).longitude);

                            locationObject.add("latLng", latLngObject);
                            waypointObject.add("location", locationObject);

                            // "via" is optional
//                            waypointObject.addProperty("via", true);

                            intermediatesArray.add(waypointObject);
                        }
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
                                .url(app.ROUTES_URL)
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
                            if (computeRoutesResponse.isSuccessful() && computeRoutesResponse.body() != null) {
                                String computeRoutesResponseData = computeRoutesResponse.body().string();
//                                Log.i(TAG, "computeRoutesResponseData: " + computeRoutesResponseData);
                                JsonObject computeRoutesJson = JsonParser.parseString(computeRoutesResponseData).getAsJsonObject();
                                if (computeRoutesJson.has("routes") && computeRoutesJson.getAsJsonArray("routes").size() > 0) {
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

                                    String formattedDistance = formatDistanceForLocale((int) totalDistanceMeters);
                                    String formattedDuration;
                                    int differenceMinutes = 0;
                                    if (trafficMode.equals("No Traffic")) {
                                        formattedDuration = formatComputeRouteDuration(totalStaticSeconds + "s");
                                    } else {
                                        formattedDuration = formatComputeRouteDuration(totalSeconds + "s");
                                        differenceMinutes = (int) ((totalSeconds - totalStaticSeconds) / 60);
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

                                    return new Object[]{formattedDistance, formattedDuration, allLegPolys, spannable};
                                }
                            } else {
                                Log.i(TAG, "computeRoutesResponse error: " + computeRoutesResponse.message());
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Error processing requests: " + e.getMessage(), e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object[] result) {
            if (result != null) {
                distance = (String) result[0];
                oldDistance = distance;
                duration = (String) result[1];
                oldDuration = duration;
                encodedPolyline = (List<List<String>>) result[2];
                oldPolyline = deepCopy2d(encodedPolyline);
                trafficText = (SpannableString) result[3];
                oldTrafficText = trafficText;
            } else {
                distance = oldDistance;
                duration = oldDuration;
                if (oldPolyline != null) {
                    oldPolyline = oldPolyline.subList(0, oldPolyline.size() - (lastSelected ? 0 : 1));
                    encodedPolyline = new ArrayList<>(oldPolyline);
                } else {
                    oldPolyline = null;
                    encodedPolyline = null;
                }
                trafficText = oldTrafficText;
            }
            Log.i(TAG, "old size: " + oldPolyline.size());
            Log.i(TAG, "encoded size: " + encodedPolyline.size());
            Log.i(TAG, "lastSelected: " + lastSelected);
            infoText = distance + " | " + duration;
            customRenderer.setShouldCluster(false);
            clusterManager.cluster();
            marker.showInfoWindow();
            if (encodedPolyline != null) {
                createPolys();
            }
        }

        private String formatDistanceForLocale(int distanceMeters) {
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
            if ("US".equalsIgnoreCase(country) ||
                    "LR".equalsIgnoreCase(country) ||
                    "MM".equalsIgnoreCase(country) ||
                    "GB".equalsIgnoreCase(country)) {
                return true;
            } else {
                return false;
            }
        }

        private String formatComputeRouteDuration(String durationString) {
            if (durationString.endsWith("s")) {
                durationString = durationString.substring(0, durationString.length() - 1);
            }
            double totalSecondsDouble = Double.parseDouble(durationString);
            int totalSeconds = (int) Math.round(totalSecondsDouble);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
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
        }
    }
}
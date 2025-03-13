package com.example.branchdirectorymap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.scottyab.rootbeer.RootBeer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SYS-MAIN";
    private static final int REQUEST_CODE = 101;
    private BranchDirectoryMap app;
    private DialogUtils dialogUtils;
    private boolean locationPermissionGranted = false;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialogUtils = new DialogUtils(this);

        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted() || Debug.isDebuggerConnected()) {
            dialogUtils.showOkDialog("Fatal Error", getString(R.string.root_error),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
        }

        setContentView(R.layout.activity_main);

        app = (BranchDirectoryMap) getApplication();

        getPermissions();
        Log.i(TAG, "MainActivity created");
        if (locationPermissionGranted) {
            toNextActivity();
        }
    }

    private void toNextActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences(app.SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(app.KEY_USE_LAST, false)) {
            if (!sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false)) {
//                Log.i(TAG, "scenario1");
//                gotoNext(true);
//            } else {
                Log.i(TAG, "scenario2");
                dialogUtils.showOkDialog("Warning", getString(R.string.file_not_loaded),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                gotoNext();
                            }
                        });
            }
//        } else {
//            Log.i(TAG, "scenario3");
//            if (sharedPreferences.contains(MapsActivity.KEY_LOAD_ORDER)) {
//                Log.i(TAG, "scenario4");
//                gotoNext(true);
//            } else {
//                gotoNext(false);
//            }
        }
        gotoNext();
    }

    private void gotoNext() {
        Intent intent = new Intent(this, FileSelectionActivity.class);
//        if (useLast) {
//            intent.putExtra("skipToNextActivity", true);
//        }
        startActivity(intent);
        finish();
    }

    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        locationPermissionGranted = false;
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                toNextActivity();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            TextView textView = findViewById(R.id.textView_location);
            textView.setText(R.string.location_warn);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
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
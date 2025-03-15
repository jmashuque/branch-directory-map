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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SYS-MAIN";
    private static final int REQUEST_CODE = 101;
    private BranchDirectoryMap app;
    private DialogUtils dialogUtils;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialogUtils = new DialogUtils(this);
        Log.i(TAG, "MainActivity started");

        if (!BuildConfig.ALLOW_ROOT) {
            try {
                Class<?> rootBeerClass = Class.forName("com.scottyab.rootbeer.RootBeer");
                java.lang.reflect.Constructor<?> constructor = rootBeerClass.getConstructor(Context.class);
                Object rootBeerInstance = constructor.newInstance(this);
                java.lang.reflect.Method isRootedMethod = rootBeerClass.getMethod("isRooted");
                boolean isRooted = (Boolean) isRootedMethod.invoke(rootBeerInstance);

                if (isRooted || Debug.isDebuggerConnected()) {
                    dialogUtils.showOkDialog("Fatal Error", getString(R.string.root_error),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                } else {
                    Log.i(TAG, "Root check passed");
                    firstStep();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for root: " + e);
                finish();
            }
        } else {
            Log.i(TAG, "ROOT CHECK SKIPPED");
            firstStep();
        }
    }

    private void firstStep() {
        Log.i(TAG, "firstStep()");

        app = (BranchDirectoryMap) getApplication();

        getPermissions();
    }

    private void secondStep() {
        Log.i(TAG, "secondStep()");
        SharedPreferences sharedPreferences = getSharedPreferences(app.SHARED_PREFS, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(app.KEY_USE_LAST, false)) {
            if (!sharedPreferences.getBoolean(app.KEY_LOAD_FINISHED, false)) {
                dialogUtils.showOkDialog("Warning", getString(R.string.file_not_loaded),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                thirdStep();
                            }
                        });
            } else {
                thirdStep();
            }
        } else {
            thirdStep();
        }
    }

    private void thirdStep() {
        Log.i(TAG, "thirdStep()");
        Intent intent = new Intent(this, FileSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            secondStep();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                secondStep();
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                TextView textView = findViewById(R.id.textView_location);
                textView.setText(R.string.location_warn);
            }
        } else {
            Log.e(TAG, "Unknown request code, not anticipated: " + requestCode);
            finish();
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
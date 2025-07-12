package com.example.branchdirectorymap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SYS-MAIN";
    private static final int REQUEST_CODE = 101;
    private Context context;
    private DialogUtils dialogUtils;
    private SharedPreferences sharedPreferences;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        dialogUtils = new DialogUtils();
        sharedPreferences = getSharedPreferences(BranchDirectoryMap.SHARED_PREFS, Context.MODE_PRIVATE);
        Log.i(TAG, "MainActivity started");
        if (sharedPreferences.getBoolean(BranchDirectoryMap.KEY_FIRST_RUN, false)) {
            if (savedInstanceState == null) {
                Log.i(TAG, "savedInstanceState is null");
                rootStep();
            }
        } else {
            Log.i(TAG, "first run");
            permissionStep();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.i(TAG, "onPause() called");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.i(TAG, "onStop() called");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.i(TAG, "onDestroy() called");
//    }

    private void permissionStep() {
        Log.i(TAG, "permissionStep");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BranchDirectoryMap.KEY_FIRST_RUN, true).apply();
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "permission already granted");
            rootStep();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "permission granted by user");
                rootStep();
            } else {
                Log.i(TAG, "permission denied by user");
                dialogUtils.showOkDialog(context, getString(R.string.warning), getString(R.string.location_warn),
                        (dialog, id) -> {
                            dialog.dismiss();
                            rootStep();
                        });
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            Log.e(TAG, "Unknown request code, not anticipated: " + requestCode);
            finishAffinity();
        }
    }

    private void rootStep() {
        Log.i(TAG, "rootStep");
        if (!BuildConfig.ALLOW_ROOT) {
            Boolean isTerminate = Secrets.isRootOrDebug(this);
            if (isTerminate) {
                dialogUtils.showOkDialog(context, "Fatal Error", getString(R.string.root_error),
                        (dialog, id) -> {
                            dialog.dismiss();
                            finishAffinity();
                        });
            } else if (!isTerminate) {
                Log.i(TAG, "Root check passed");
                fileStep();
            } else {
                finishAffinity();
            }
        } else {
            Log.i(TAG, "ROOT CHECK SKIPPED");
            fileStep();
        }
    }

    private void fileStep() {
        Log.i(TAG, "fileStep");
        if (sharedPreferences.getBoolean(BranchDirectoryMap.KEY_USE_LAST, false)) {
            if (!sharedPreferences.getBoolean(BranchDirectoryMap.KEY_LOAD_FINISHED, false)) {
                dialogUtils.showOkDialog(context, getString(R.string.warning), getString(R.string.file_not_loaded),
                        (dialog, id) -> {
                            dialog.dismiss();
                            callNextActivity();
                        });
            } else {
                callNextActivity();
            }
        } else {
            callNextActivity();
        }
    }

    private void callNextActivity() {
        Log.i(TAG, "callNextActivity");
        Intent intent = new Intent(this, FileSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("dummy", true);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
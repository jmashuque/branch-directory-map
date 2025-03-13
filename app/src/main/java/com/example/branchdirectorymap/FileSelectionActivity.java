package com.example.branchdirectorymap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileSelectionActivity extends AppCompatActivity {

    private static final String TAG = "SYS-FILES";
    private BranchDirectoryMap app;
    private ActivityResultLauncher<String> pickCsvFile;
    private ContentResolver contentResolver;
    private InputStream inputStream;
    private Map<String, List<String[]>> entryPre;
    private Map<String, Map<String, Object>> varMap;
    private ArrayList<String> entries;
    private DialogUtils dialogUtils;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LocationDatabaseHelper dbHelper;
    private Gson gson;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileselection);

        app = (BranchDirectoryMap) getApplication();
        sharedPreferences = getSharedPreferences(app.SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        dbHelper = new LocationDatabaseHelper(this);
        gson = new Gson();

        Log.i(TAG, "FileSelectionActivity created");

        if (sharedPreferences.getBoolean(app.KEY_USE_LAST, false) ||
                sharedPreferences.contains(app.KEY_LOAD_OVERRIDE)) {
            reloadState();
        } else {
            pickCsvFile = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        Log.i(TAG, "uri: " + uri.toString());
                        TextView textView = findViewById(R.id.textView_file);
                        try {
                            inputStream = contentResolver.openInputStream(uri);
                            String fileName = uri.toString();
                            String fileExt = null;
                            Log.i(TAG, "fileName: " + fileName);
                            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                                File file = new File(uri.getPath());
                                fileName = file.getName();
                            } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                                Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
                                if (returnCursor != null && returnCursor.moveToFirst()) {
                                    int index = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                    fileName = returnCursor.getString(index);
                                    returnCursor.close();
                                }
    //                            fileExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
                            } else {
                                Log.i(TAG, "---Unknown scheme: not programmed---");
                                finish();
                            }
    //                        fileName = fileName.substring(fileName.lastIndexOf("/") == -1 ? 0 : fileName.lastIndexOf("/") + 1);
    //                        fileName = fileName.substring(0, fileName.contains(".") ? fileName.lastIndexOf(".") : fileName.length());
                            fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                            Log.i(TAG, "fileName Extracted: " + fileName);
                            Log.i(TAG, "fileExt Extracted: " + fileExt);
                            if (fileExt.equals("csv")) {
                                entries.add(fileName);
                                csvToArray(inputStream, entries.get(0));
                                prepMaps(1, entries);
//                                Log.i(TAG, "varmap after file selection: " + varMap.toString());
                            } else if (fileExt.equals("db")) {
                                if (!dbLoader(uri)) {
                                    throw new IOException();
                                }
                            } else {
                                Log.i(TAG, "Type Error");
                                textView.setText(R.string.type_warn);
                            }
                            parseArray(entries);
                            callMap();
                        } catch (FileNotFoundException e) {
                            Log.i(TAG, "---FileNotFoundException---");
                            textView.setText(R.string.file_not_found);
                        } catch (IOException e) {
                            Log.i(TAG, "---IOException--- while trying to read db file - 1");
                            textView.setText(R.string.db_warn1);
                        }
                    }
                }
            });

            context = this;
            contentResolver = getContentResolver();
            entries = new ArrayList<>();
            varMap = new ConcurrentHashMap<>();
            entryPre = new HashMap<>();
            dialogUtils = new DialogUtils(this);

            boolean isDbFine = false;
            if (!BuildConfig.EMBEDDED_DB.isEmpty()) {
                Log.i(TAG, "loading from: " + BuildConfig.EMBEDDED_DB);
                isDbFine = dbLoader(null);
            }
            if (isDbFine || BuildConfig.EMBEDDED_DB.isEmpty()) {
                fileLoader();
            } else {
                dialogUtils.showOkDialog("Fatal Error", getString(R.string.db_warn1),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
            }
        }
    }

    private boolean dbLoader(Uri from) {
        Log.i(TAG, "dbLoader");
        boolean isSuccessful = false;
        try {
            if (from == null) {
                Log.i(TAG, "dbL case1 embedded");
                inputStream = getAssets().open(BuildConfig.EMBEDDED_DB);
            } else {
                Log.i(TAG, "dbL case2 from uri");
                inputStream = contentResolver.openInputStream(from);
            }
            String path = this.getDatabasePath(BuildConfig.DATABASE_NAME).getPath();
//            if (dbHelper.isDatabaseValid(path)) {
//                Log.i(TAG, "app db exists: " + path);
//            }
            if (from != null) {
                if (ContentResolver.SCHEME_FILE.equals(from.getScheme())) {
                    path = from.getPath();
                } else if (ContentResolver.SCHEME_CONTENT.equals(from.getScheme())) {
                    path = from.toString();
                } else {
                    Log.i(TAG, "---Unknown scenario: not programmed---");
                    finish();
                }
            }
            isSuccessful = dbHelper.importDatabase(inputStream);
            Log.i(TAG, "db valid and imported: " + path);
        } catch (IOException e) {
            Log.i(TAG, "---IOException--- while trying to read db file - 2");
//            dialogUtils.showOkDialog("Warning", getString(R.string.db_warn2),
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            recreate();
//                        }
//                    });
            isSuccessful = false;
        }
        return isSuccessful;
    }

    private void fileLoader() {
        Log.i(TAG, "pickCSV");
        Log.i(TAG, "FILE_NAMES empty: " + BuildConfig.FILE_NAMES.isEmpty());
        Log.i(TAG, "DEFAULT_FILE empty: " + BuildConfig.DEFAULT_FILE.isEmpty());
        if (!BuildConfig.EMBEDDED_FILE) {
            Log.i(TAG, "case1 file picker");
            pickCsvFile.launch("*/*");
        } else if (!BuildConfig.FILE_NAMES.isEmpty()) {
            Log.i(TAG, "case2 filenames provided");
            for (String entry : BuildConfig.FILE_NAMES.split(",")) {
                entries.add(entry.trim());
            }
            Log.i(TAG, "len: " + entries.size());
            prepMaps(BuildConfig.SETTINGS_PER_FILE ? entries.size() : 1, entries);
            for (String entry : entries) {
                try {
                    if (entry.isEmpty()) {
                        continue;
                    }
                    inputStream = getAssets().open(entry);
                    csvToArray(inputStream, entry);
                } catch (IOException e) {
                    Log.i(TAG, "---RuntimeException--- while trying to read embedded csv");
                    TextView textView = findViewById(R.id.textView_file);
                    textView.setText(R.string.file_warn);
                }
            }
            parseArray(entries);
            callMap();
        } else {
            callMap();
        }
    }

    private void prepMaps(int len, ArrayList<String> entries) {
        Log.i(TAG, "prepMaps");
        Log.i(TAG, "using entries: " + entries);
        Map<String, Object> entryMap;
        for (int i = 0; i < len; i++) {
            if (entries.get(i).equals("")) {
                continue;
            }
            entryMap = new HashMap<>();
            String tableName = entries.get(i);
            Log.i(TAG, "tableName: " + tableName);
            try {
                entryMap.put("array", new ArrayList<>());
//                Log.i(TAG, "colour: " + BuildConfig.MARKER_COLOURS.split(",", -1)[i].trim());
                entryMap.put("colour", BuildConfig.MARKER_COLOURS.split(",", -1)[i].trim());
//                Log.i(TAG, "multi_row_sets: " + Boolean.parseBoolean(BuildConfig.MULTI_ROW_SETS.split(",")[i].trim()));
                entryMap.put("multi_row_sets", Boolean.parseBoolean(BuildConfig.MULTI_ROW_SETS.split(",")[i].trim()));
//                Log.i(TAG, "use_phones: " + Boolean.parseBoolean(BuildConfig.USE_PHONE.split(",")[i].trim()));
                entryMap.put("use_phone", Boolean.parseBoolean(BuildConfig.USE_PHONE.split(",")[i].trim()));
//                Log.i(TAG, "use_refined: " + Boolean.parseBoolean(BuildConfig.USE_REFINED.split(",")[i].trim()));
                entryMap.put("use_refined", Boolean.parseBoolean(BuildConfig.USE_REFINED.split(",")[i].trim()));
//                Log.i(TAG, "delimiter: " + BuildConfig.DELIMITER.split(",", -1)[i]);
                entryMap.put("delimiter", BuildConfig.DELIMITER.split(",", -1)[i]);
//                Log.i(TAG, "code_prefix: " + BuildConfig.CODE_PREFIX.split(",", -1)[i].trim());
                entryMap.put("code_prefix", BuildConfig.CODE_PREFIX.split(",", -1)[i].trim());
//                Log.i(TAG, "code_delimiter: " + BuildConfig.CODE_DELIMITER.split(",", -1)[i]);
                entryMap.put("code_delimiter", BuildConfig.CODE_DELIMITER.split(",", -1)[i]);
//                Log.i(TAG, "columns_per_row: " + Integer.parseInt(BuildConfig.COLUMNS_PER_ROW.split(",")[i].trim()));
                entryMap.put("columns_per_row", Integer.parseInt(BuildConfig.COLUMNS_PER_ROW.split(",")[i].trim()));
//                Log.i(TAG, "ignore_rows_begin: " + Integer.parseInt(BuildConfig.IGNORE_ROWS_BEGIN.split(",")[i].trim()));
                entryMap.put("ignore_rows_begin", Integer.parseInt(BuildConfig.IGNORE_ROWS_BEGIN.split(",")[i].trim()));
//                Log.i(TAG, "ignore_rows_end: " + Integer.parseInt(BuildConfig.IGNORE_ROWS_END.split(",")[i].trim()));
                entryMap.put("ignore_rows_end", Integer.parseInt(BuildConfig.IGNORE_ROWS_END.split(",")[i].trim()));
//                Log.i(TAG, "title_index: " + Integer.parseInt(BuildConfig.TITLE_INDEX.split(",")[i].trim()));
                entryMap.put("title_index", Integer.parseInt(BuildConfig.TITLE_INDEX.split(",")[i].trim()));
//                Log.i(TAG, "address_index: " + Integer.parseInt(BuildConfig.ADDRESS_INDEX.split(",")[i].trim()));
                entryMap.put("address_index", Integer.parseInt(BuildConfig.ADDRESS_INDEX.split(",")[i].trim()));
//                Log.i(TAG, "refined_address_index: " + Integer.parseInt(BuildConfig.REFINED_ADDRESS_INDEX.split(",")[i].trim()));
                entryMap.put("refined_address_index", Integer.parseInt(BuildConfig.REFINED_ADDRESS_INDEX.split(",")[i].trim()));
//                Log.i(TAG, "phone_index: " + Integer.parseInt(BuildConfig.PHONE_INDEX.split(",")[i].trim()));
                entryMap.put("phone_index", Integer.parseInt(BuildConfig.PHONE_INDEX.split(",")[i].trim()));
//                Log.i(TAG, "rows_per_set: " + Integer.parseInt(BuildConfig.ROWS_PER_SET.split(",")[i].trim()));
                entryMap.put("rows_per_set", Integer.parseInt(BuildConfig.ROWS_PER_SET.split(",")[i].trim()));
//                Log.i(TAG, "title_offset: " + Integer.parseInt(BuildConfig.TITLE_OFFSET.split(",")[i].trim()));
                entryMap.put("title_offset", Integer.parseInt(BuildConfig.TITLE_OFFSET.split(",")[i].trim()));
//                Log.i(TAG, "address_offset: " + Integer.parseInt(BuildConfig.ADDRESS_OFFSET.split(",")[i].trim()));
                entryMap.put("address_offset", Integer.parseInt(BuildConfig.ADDRESS_OFFSET.split(",")[i].trim()));
//                Log.i(TAG, "refined_address_offset: " + Integer.parseInt(BuildConfig.REFINED_ADDRESS_OFFSET.split(",")[i].trim()));
                entryMap.put("refined_address_offset", Integer.parseInt(BuildConfig.REFINED_ADDRESS_OFFSET.split(",")[i].trim()));
//                Log.i(TAG, "phone_offset: " + Integer.parseInt(BuildConfig.PHONE_OFFSET.split(",")[i].trim()));
                entryMap.put("phone_offset", Integer.parseInt(BuildConfig.PHONE_OFFSET.split(",")[i].trim()));
            } catch (IndexOutOfBoundsException e) {
                Log.i(TAG, "---IndexOutOfBoundsException--- check each array's length matches number of file names");
            }
            varMap.put(tableName, entryMap);
        }
    }

    private void csvToArray(InputStream csv, String entry) {
        Log.i(TAG, "csvtoarray");
        try {
            BufferedReader readerPre = new BufferedReader(new InputStreamReader(csv));
            CSVReader reader = new CSVReader(readerPre);
            entryPre.put(entry, reader.readAll());
        } catch (IOException e) {
            Log.i(TAG, "---IOException--- while trying to read chosen csv");
            TextView textView = findViewById(R.id.textView_file);
            textView.setText(R.string.file_not_read);
        } catch (RuntimeException e) {
            Log.i(TAG, "---RuntimeException---");
            TextView textView = findViewById(R.id.textView_file);
            textView.setText(R.string.file_not_read);
        } catch (CsvException e) {
            Log.i(TAG, "---CsvException--- while trying to read chosen csv");
            TextView textView = findViewById(R.id.textView_file);
            textView.setText(R.string.file_not_read);
        }
    }

    private void parseArray(ArrayList<String> entries) {
        Log.i(TAG, "parseArray");
        for (String entry : entries) {
            if (entry.equals("")) {
                continue;
            }
            Log.i(TAG, "parseArray entry: " + entry);
            Log.i(TAG, "entries: " + entryPre.get(entry).size());
            int rowsPerSet, titleOffset, addressOffset, refinedAddressOffset, phoneOffset;
            if (!(Boolean) varMap.get(entry).get("multi_row_sets")) {
                rowsPerSet = 1;
                titleOffset = addressOffset = refinedAddressOffset = phoneOffset = 0;
            } else {
                rowsPerSet = (int) varMap.get(entry).get("rows_per_set");
                titleOffset = (int) varMap.get(entry).get("title_offset");
                addressOffset = (int) varMap.get(entry).get("address_offset");
                refinedAddressOffset = (int) varMap.get(entry).get("refined_address_offset");
                phoneOffset = (int) varMap.get(entry).get("phone_offset");
            }
            Log.i(TAG, "entry: " + entry + " rowsPerSet: " + rowsPerSet + " titleOffset: " + titleOffset + " addressOffset: " + addressOffset + " refinedAddressOffset: " + refinedAddressOffset + " phoneOffset: " + phoneOffset);
            Log.i(TAG, "ignore_rows_begin: " + varMap.get(entry).get("ignore_rows_begin") + " ignore_rows_end: " + varMap.get(entry).get("ignore_rows_end"));
            for (int i = (int) varMap.get(entry).get("ignore_rows_begin"); i < entryPre.get(entry).size()-(int) varMap.get(entry).get("ignore_rows_end"); i += rowsPerSet) {
                ArrayList<String> entryPost = new ArrayList<String>();
                // add full name and address, stripping leading and trailing double quotes
                entryPost.add(stripQuotesToArray(entryPre.get(entry).get(i + titleOffset)).get((int) varMap.get(entry).get("title_index")));
                entryPost.add(stripQuotesToArray(entryPre.get(entry).get(i + addressOffset)).get((int) varMap.get(entry).get("address_index")));
                // add refined
                if ((Boolean) varMap.get(entry).get("use_refined")) {
                    entryPost.add(new ArrayList<String>(Arrays.asList(entryPre.get(entry).get(i + refinedAddressOffset))).get((int) varMap.get(entry).get("refined_address_index")));
                } else {
                    entryPost.add("");
                }
                // add phone
                if ((Boolean) varMap.get(entry).get("use_phone")) {
                    entryPost.add(new ArrayList<String>(Arrays.asList(entryPre.get(entry).get(i + phoneOffset))).get((int) varMap.get(entry).get("phone_index")));
                } else {
                    entryPost.add("");
                }
                // add colour
                entryPost.add((String) varMap.get(entry).get("colour"));
                ArrayList<String> newEntry = (ArrayList<String>) varMap.get(entry).get("array");
                newEntry.addAll(entryPost);
                // NEEDED TO DISASSOCIATE THE ARRAY
                newEntry = null;
            }
        }
    }

    private ArrayList<String> stripQuotesToArray(String[] input) {
        ArrayList<String> result = new ArrayList<>();
        if (input != null) {
            for (String str : input) {
                if (str != null && str.length() >= 2 && str.startsWith("\"") && str.endsWith("\"")) {
                    str = str.substring(1, str.length() - 1);
                }
                result.add(str);
            }
        }
        return result;
    }

    private void callMap() {
        Log.i(TAG, "callMap");
//        Log.i(TAG, "varMap 1st: " + varMap.toString());
        for (String key : varMap.keySet()) {        // remove extension from keys
            varMap.put(key.substring(0, key.contains(".") ? key.lastIndexOf(".") : key.length()), varMap.remove(key));
        }
//        List<String> keys = new ArrayList<>(varMap.keySet());
//        Collections.sort(keys);
        if (!BuildConfig.EMBEDDED_FILE) {
//            Log.i(TAG, "case3");
            showDialog();
        } else {
            Log.i(TAG, "embedded db conf loaded, will override");
            editor.putBoolean(app.KEY_LOAD_OVERRIDE, true);
            editor.apply();
            callNextActivity();
        }
    }

    private void reloadState() {
        Log.i(TAG, "reloadState");
        String varMapStr = dbHelper.getVarMapStr(true);
        if (sharedPreferences.contains(app.KEY_LOAD_OVERRIDE)) {
            varMap = null;
        } else {
            varMap = gson.fromJson(varMapStr, app.VARMAP_TYPE);
        }
        callNextActivity();
    }

    private void showDialog() {
//        Log.i(TAG, "case3.1");

        dialogUtils.showYesNoDialog("Confirm", "Load this file at startup from now on?",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
//                        Log.i(TAG, "case3.2");
                    editor.putBoolean(app.KEY_USE_LAST, true);
                    editor.apply();
                    dialog.dismiss();
                    callNextActivity();
                }
            },
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
//                        Log.i(TAG, "case3.3");
                    editor.putBoolean(app.KEY_USE_LAST, false);
                    editor.apply();
                    dialog.dismiss();
                    callNextActivity();
                }
            });
    }

    public void callNextActivity() {
//        Log.i(TAG, "case4");
//        for (String key : varMap.keySet()) {
//            for (String key2 : varMap.get(key).keySet()) {
//                Log.i(TAG, key + " key: " + key2 + " value: " + varMap.get(key).get(key2).toString());
//            }
//        }
//        for (String key : varMap.keySet()) {
//            Log.i(TAG, key + " array: " + varMap.get(key).get("array"));
//        }
        Intent i = new Intent(this, MapsActivity.class);
//        Log.i(TAG, "varMap before map activity: " + varMap.toString());
        String serializedData = gson.toJson(varMap);
        i.putExtra("places", serializedData);
        Log.i(TAG, "starting map activity");
        startActivity(i);
        finish();
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
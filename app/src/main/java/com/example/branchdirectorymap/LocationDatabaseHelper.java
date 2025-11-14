package com.example.branchdirectorymap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SYS-DBHELPER";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public LocationDatabaseHelper(Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        clearDatabase();
        onCreate(db);
    }

    public void createTable(SQLiteDatabase db, String tableName) {
        String sql;
        if (tableName.equals("varMap.create")) {
            sql = "CREATE TABLE IF NOT EXISTS varmap (" +
                    "string TEXT PRIMARY KEY" +
                    ")";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "latitude REAL NOT NULL," +
                    "longitude REAL NOT NULL," +
                    "code TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "address TEXT NOT NULL," +
                    "refined TEXT NOT NULL," +
                    "waypoints TEXT NOT NULL," +
                    "phone TEXT NOT NULL," +
                    "colour TEXT NOT NULL," +
                    "nameFirst INTEGER NOT NULL," +
                    "PRIMARY KEY (code, name)" +
                    ")";
        }
        db.execSQL(sql);
    }

    public ArrayList<String> getTables(SQLiteDatabase db) {
        ArrayList<String> tableNames = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT name FROM sqlite_master WHERE type='table'";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String tableName = cursor.getString(0);
                    tableNames.add(tableName);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tableNames;
    }

    public void clearDatabase() {

        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> tableNames = getTables(db);
        db.beginTransaction();
        try {
            for (String tableName : tableNames) {
                String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
                db.execSQL(dropTableSQL);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName});
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteTable(SQLiteDatabase db, String tableName) {
        try {
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
            db.execSQL(dropTableSQL);
        } catch (Exception e) {
            Log.i(TAG, "error deleting table: " + tableName);
        }
    }

    public Map<String, List<CustomItem>> getAllLocations(SQLiteDatabase db) {
        db.beginTransaction();
        ArrayList<String> tableNames = getTables(db);
        Map<String, List<CustomItem>> markers = new HashMap<>();
        List<CustomItem> mapmarkers;
        Cursor cursor;
//        Log.i(TAG, "tableNames: " + tableNames.toString());
        for (String table : tableNames) {
            if (table.equals("android_metadata") || table.equals("sqlite_sequence") || table.equals("varmap")) {
                continue;
            }
//            Log.i(TAG, "getAllLocations table: " + table);
//            table = table.substring(0, table.contains(".") ? table.lastIndexOf(".") : table.length());
            cursor = db.query(table, null, null, null, null, null, null);
            mapmarkers = new ArrayList<>();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") double lat = cursor.getDouble(cursor.getColumnIndex("latitude"));
                @SuppressLint("Range") double lng = cursor.getDouble(cursor.getColumnIndex("longitude"));
                @SuppressLint("Range") String cod = cursor.getString(cursor.getColumnIndex("code"));
                @SuppressLint("Range") String nam = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String add = cursor.getString(cursor.getColumnIndex("address"));
                @SuppressLint("Range") String ref = cursor.getString(cursor.getColumnIndex("refined"));
                @SuppressLint("Range") String json = cursor.getString(cursor.getColumnIndex("waypoints"));
                @SuppressLint("Range") String tel = cursor.getString(cursor.getColumnIndex("phone"));
                @SuppressLint("Range") String clr = cursor.getString(cursor.getColumnIndex("colour"));
                @SuppressLint("Range") boolean first = cursor.getInt(cursor.getColumnIndex("nameFirst")) != 0;
//                Log.i(TAG, "json: " + json);
                Map<String, LatLng> way = BranchDirectoryMap.gson.fromJson(json, BranchDirectoryMap.WAYPOINTS_TYPE);
                mapmarkers.add(new CustomItem(lat, lng, cod, nam, add, ref, way, tel, clr, first));
            }
            cursor.close();
            markers.put(table, mapmarkers);
        }
        db.endTransaction();
        db.close();
//        Log.i(TAG, "mapmarkers size: " + mapmarkers.size());
        return markers;
    }

    public String getVarMapStr(boolean close) {
        SQLiteDatabase db = this.getReadableDatabase();
//        Log.i(TAG, "getvarmapstr tables: " + getTables().toString());
        String query = "SELECT * FROM varmap LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        String value = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                value = cursor.getString(0); // Get value from the first column (index 0)
            }
            cursor.close();
        }
        if (close) {
            db.close();
        }
        return value;
    }

    public boolean importDatabase(InputStream inputStream) {
        boolean success = true;
        try {
            File file = context.getDatabasePath(BuildConfig.DATABASE_NAME);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            if (!isDatabaseValid(file.getPath())) {
                throw new Exception();
            }
//            Log.i(TAG, "database imported successfully");
        } catch (IOException e) {
            Log.i(TAG, "error importing database");
            success= false;
        } catch (Exception e) {
            Log.i(TAG, "error trying to read database");
            success= false;
        } finally {
            if (!success) {
                context.deleteDatabase(BuildConfig.DATABASE_NAME);
                Log.i(TAG, "error result: database deleted");
            }
        }
        return success;
    }

    public boolean isDatabaseValid(String path) {
        Log.i(TAG, "isDatabaseValid path: " + path);
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            db.close();
            return true;
        } catch (Exception e) {
            Log.i(TAG, "Database doesn't exist or is invalid: " + path);
            return false;
        }
    }

    public void exportDatabase(String databaseName) {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (sd.canWrite()) {
                String currentDBPath = context.getDatabasePath(BuildConfig.DATABASE_NAME).getPath();
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, databaseName);

                Log.i(TAG, "currentDB: " + currentDB.getAbsolutePath());
                Log.i(TAG, "backupDB: " + backupDB.getAbsolutePath());

                if (currentDB.exists()) {
                    if (!backupDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        Log.i(TAG, "Database exported successfully to " + backupDB.getAbsolutePath());
                    } else {
                        Log.i(TAG, "Backup database already exists.");
                    }
                } else {
                    Log.i(TAG, "Database not found.");
                }
            } else {
                Log.i(TAG, "External storage not writable.");
            }
        } catch (IOException e) {
            Log.i(TAG, "Er   ror exporting database: " + e.getMessage());
        }
    }

    public static class DatabaseChecker {

        public static boolean isDatabaseExistsAndPopulated(Context context, String databaseName) {
            String dbPath = context.getDatabasePath(databaseName).getAbsolutePath();
            boolean dbExists = context.getDatabasePath(databaseName).exists();

            if (dbExists) {
                SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
                int totalCount = 0;

                Cursor tablesCursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

                if (tablesCursor != null) {
                    while (tablesCursor.moveToNext()) {
                        String tableName = tablesCursor.getString(0);
                        if (tableName.equals("android_metadata") || tableName.equals("sqlite_sequence")) {
                            continue;
                        }
                        Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
                        if (countCursor != null) {
                            if (countCursor.moveToFirst()) {
                                int count = countCursor.getInt(0);
                                totalCount += count;
                            }
                            countCursor.close();
                        }
                    }
                    tablesCursor.close();
                }
                db.close();
                return totalCount > 0;
            }
            return false;
        }
    }
}

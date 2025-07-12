package com.example.branchdirectorymap;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterItem;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyItem implements ClusterItem, Parcelable {

    private static final String TAG = "SYS-MYITEM";
    private final LatLng position;  // latitude and longitude, derived from address or refined
    private final String code;      // location code, derived from full name
    private final String name;      // location name, derived from full name
    private final String snippet;   // location address
    private final String refined;   // refined address
    private String waypoints;   // optional waypoints
    private Map<String, LatLng> positions;    // LatLngs for optional waypoints
    private final String phone;     // phone number
    private final String colour;    // marker colour
    private final float hue;        // marker hue, derived from colour
    private int selected;           // whether the marker is selected
    private final static Gson gson = new Gson();

    public MyItem(double lat, double lng, String code, String name, String snippet, String refined, String waypoints, String phone, String colour) {
        this.position = new LatLng(lat, lng);
        this.code = code;
        this.name = name;
        this.snippet = snippet;
        this.refined = refined;
        this.waypoints = waypoints;
        this.phone = phone;
        this.colour = colour;
        this.hue = calcClr(colour);
        this.selected = -1;
        this.positions = new HashMap<>();
        String[] waypointsArray = waypoints.split(",");
        for (String waypoint : waypointsArray) {
            this.positions.put(waypoint.trim(), null);
        }
    }

    public MyItem(double lat, double lng, String code, String name, String snippet, String refined, String waypoints, String jsonPositions, String phone, String colour) {
        this.position = new LatLng(lat, lng);
        this.code = code;
        this.name = name;
        this.snippet = snippet;
        this.refined = refined;
        this.waypoints = waypoints;
        this.phone = phone;
        this.colour = colour;
        this.hue = calcClr(colour);
        this.selected = -1;
        this.positions = gson.fromJson(jsonPositions, BranchDirectoryMap.POSITIONS_TYPE);
    }

    // Parcelable implementation
    protected MyItem(Parcel parcel) {
        position = parcel.readParcelable(LatLng.class.getClassLoader());
        code = parcel.readString();
        name = parcel.readString();
        snippet = parcel.readString();
        refined = parcel.readString();
        waypoints = parcel.readString();
        phone = parcel.readString();
        colour = parcel.readString();
        hue = parcel.readFloat();
        selected = parcel.readInt();
        int mapSize = parcel.readInt();
        positions = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            String key = parcel.readString();
            LatLng value = parcel.readParcelable(LatLng.class.getClassLoader());
            positions.put(key, value);
        }
    }

    public static final Creator<MyItem> CREATOR = new Creator<>() {
        @Override
        public MyItem createFromParcel(Parcel in) {
            return new MyItem(in);
        }

        @Override
        public MyItem[] newArray(int size) {
            return new MyItem[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(position, flags);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeString(snippet);
        parcel.writeString(refined);
        parcel.writeString(waypoints);
        parcel.writeString(phone);
        parcel.writeString(colour);
        parcel.writeFloat(hue);
        parcel.writeInt(selected);
        parcel.writeInt(positions.size());
        for (Map.Entry<String, LatLng> entry : positions.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeParcelable(entry.getValue(), flags);
        }
    }

    private float calcClr (String colour) {
        // blue reserved for route markers
        return switch (colour) {
            case "azure" -> BitmapDescriptorFactory.HUE_AZURE;
            case "blue" -> BitmapDescriptorFactory.HUE_RED;  // blue reserved for route markers
            case "cyan" -> BitmapDescriptorFactory.HUE_CYAN;
            case "green" -> BitmapDescriptorFactory.HUE_GREEN;
            case "magenta" -> BitmapDescriptorFactory.HUE_MAGENTA;
            case "orange" -> BitmapDescriptorFactory.HUE_ORANGE;
            case "rose" -> BitmapDescriptorFactory.HUE_RED;     // rose reserved for waypoint markers
            case "violet" -> BitmapDescriptorFactory.HUE_VIOLET;
            case "yellow" -> BitmapDescriptorFactory.HUE_YELLOW;
            default -> BitmapDescriptorFactory.HUE_RED;
        };
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return code + (code.isEmpty() ? "" : name.isEmpty() ? "" : " ") + (name.isEmpty() ? "" : name);
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRefined() {
        return refined;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public Map<String, LatLng> getPositions() {
        return positions;
    }

    public void setPositions(String waypoint, LatLng position) {
        if (this.positions.containsKey(waypoint)) {
            this.positions.put(waypoint, position);
            Log.i(TAG, "MyItem setPositions new waypoint: " + this.positions);
        } else {
            Log.e(TAG, "MyItem setPositions - waypoint not found: " + waypoint);
        }
    }

    public String getPhone() {
        return phone;
    }

    public String getColour() {
        return colour;
    }

    public float getHue() {
        return selected > -1 ? BitmapDescriptorFactory.HUE_BLUE : hue;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class MyItemSorter {

        public static List<MyItem> sortMyItemsByCode(List<MyItem> items) {
            Collections.sort(items, (item1, item2) -> {
                if (item1.getCode().isEmpty() && item2.getCode().isEmpty()) {
                    return item1.getName().compareTo(item2.getName());
                } else {
                    return item1.getCode().compareTo(item2.getCode());
                }
            });
            return items;
        }
    }
}
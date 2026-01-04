package com.example.branchdirectorymap;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomItem implements ClusterItem, Parcelable {

    private static final String TAG = "SYS-CUSTOMITEM";
    private final LatLng position;  // latitude and longitude, derived from address or refined
    private final String code;      // location code, derived from full name
    private final String name;      // location name, derived from full name
    private final String snippet;   // location address
    private final String refined;   // refined address
    private final Map<String, LatLng> waypoints;    // LatLngs for optional waypoints, insertion
                                                    // order preserved, derived from refined
    private String lastWaypoint;    // last waypoint, derived from waypoints
    private int waypointsCount;      // number of waypoints, derived from waypoints
    private final String phone;     // phone number
    private final String colour;    // marker colour
    private final float hue;        // marker hue, derived from colour
    private int selected;           // whether the marker is selected
    private boolean nameFirst;      // whether the name should be displayed first

    public CustomItem(double lat, double lng, String code, String name, String snippet, String refined,
                      String waypointsStr, String phone, String colour, boolean nameFirst) {
        this.position = new LatLng(lat, lng);
        this.code = code;
        this.name = name;
        this.snippet = snippet;
        this.refined = refined;
        this.waypoints = new LinkedHashMap<>();
        if (!waypointsStr.isEmpty()) {
            Iterator<String> it = Arrays.asList(waypointsStr.split(",")).iterator();
            while (it.hasNext()) {
                String waypoint = it.next().trim();
                this.waypoints.put(waypoint, null);
                if (!it.hasNext()) {
                    this.lastWaypoint = waypoint;
                }
            }
        } else {
            this.lastWaypoint = "";
        }
        this.waypointsCount = this.waypoints.size();
        this.phone = phone;
        this.colour = colour;
        this.hue = calcClr(colour);
        this.selected = -1;
        this.nameFirst = nameFirst;
    }

    public CustomItem(double lat, double lng, String code, String name, String snippet, String refined,
                      Map<String, LatLng> waypointsMap, String phone, String colour, boolean nameFirst) {
        this.position = new LatLng(lat, lng);
        this.code = code;
        this.name = name;
        this.snippet = snippet;
        this.refined = refined;
        if (waypointsMap != null) {
            this.waypoints = waypointsMap;
            for (String waypoint : this.waypoints.keySet()) {
                this.lastWaypoint = waypoint;
            }
            this.waypointsCount = this.waypoints.size();
        } else {
            this.waypoints = new LinkedHashMap<>();
            this.lastWaypoint = "";
            this.waypointsCount = 0;
        }
        this.phone = phone;
        this.colour = colour;
        this.hue = calcClr(colour);
        this.selected = -1;
        this.nameFirst = nameFirst;
    }

    public CustomItem(CustomItem item) {
        this.position = item.position;
        this.code = item.code;
        this.name = item.name;
        this.snippet = item.snippet;
        this.refined = item.refined;
        this.waypoints = item.waypoints;
        this.lastWaypoint = item.lastWaypoint;
        this.waypointsCount = item.waypointsCount;
        this.phone = item.phone;
        this.colour = item.colour;
        this.hue = item.hue;
        this.selected = item.selected;
        this.nameFirst = item.nameFirst;
    }

    // Parcelable implementation
    protected CustomItem(Parcel parcel) {
        position = parcel.readParcelable(LatLng.class.getClassLoader());
        code = parcel.readString();
        name = parcel.readString();
        snippet = parcel.readString();
        refined = parcel.readString();
        phone = parcel.readString();
        colour = parcel.readString();
        hue = parcel.readFloat();
        selected = parcel.readInt();
        nameFirst = parcel.readByte() != 0;
        lastWaypoint = parcel.readString();
        waypointsCount = parcel.readInt();
        int mapSize = parcel.readInt();
        waypoints = new LinkedHashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            String key = parcel.readString();
            LatLng value = parcel.readParcelable(LatLng.class.getClassLoader());
            waypoints.put(key, value);
        }
    }

    public static final Creator<CustomItem> CREATOR = new Creator<>() {
        @Override
        public CustomItem createFromParcel(Parcel in) {
            return new CustomItem(in);
        }

        @Override
        public CustomItem[] newArray(int size) {
            return new CustomItem[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(position, flags);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeString(snippet);
        parcel.writeString(refined);
        parcel.writeString(phone);
        parcel.writeString(colour);
        parcel.writeFloat(hue);
        parcel.writeInt(selected);
        parcel.writeByte((byte) (nameFirst ? 1 : 0));
        parcel.writeString(lastWaypoint);
        parcel.writeInt(waypointsCount);
        parcel.writeInt(waypoints.size());
        for (Map.Entry<String, LatLng> entry : waypoints.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeParcelable(entry.getValue(), flags);
        }
    }

    public float calcClr (String colour) {
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

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @NonNull
    @Override
    public String getTitle() {
        if (nameFirst) {
            return name + (name.isEmpty() ? "" : code.isEmpty() ? "" : " ") + (code.isEmpty() ? "" : code);
        } else {
            return code + (code.isEmpty() ? "" : name.isEmpty() ? "" : " ") + (name.isEmpty() ? "" : name);
        }
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
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

    public Map<String, LatLng> getWaypoints() {
        return waypoints;
    }

    public void setWaypoint(String waypoint, LatLng position) {
        if (this.waypoints.containsKey(waypoint)) {
            this.waypoints.put(waypoint, position);
        } else {
            Log.e(TAG, "CustomItem setWaypoints - waypoint not found: " + waypoint);
        }
    }

    public String getLastWaypoint() {
        return lastWaypoint;
    }

    public int getWaypointsCount() {
        return waypointsCount;
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

    @NonNull
    @Override
    public String toString() {
        return "CustomItem{" +
                "position=" + position +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", snippet='" + snippet + '\'' +
                ", refined='" + refined + '\'' +
                ", waypoints=" + (waypoints == null ? "null" : waypoints) +
                ", lastWaypoint='" + lastWaypoint + '\'' +
                ", waypointsCount=" + waypointsCount +
                ", phone='" + phone + '\'' +
                ", colour='" + colour + '\'' +
                ", hue=" + hue +
                ", selected=" + selected +
                ", nameFirst=" + nameFirst +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public CustomItem deepCopy() {
        return new CustomItem(this);
    }

    public static class CustomItemSorter {

        public static List<CustomItem> sortCustomItemsByCode(List<CustomItem> items) {
            Collections.sort(items, (item1, item2) -> {
                if (item1.getCode().isEmpty() && item2.getCode().isEmpty()) {
                    return item1.getName().compareTo(item2.getName());
                } else {
                    return item1.getCode().compareTo(item2.getCode());
                }
            });
            return items;
        }

        public static void sortAllListsByCode(Map<String, List<CustomItem>> map) {
            if (map == null) return;
            for (List<CustomItem> list : map.values()) {
                if (list != null) {
                    sortCustomItemsByCode(list); // reuse your existing method
                }
            }
        }
    }
}
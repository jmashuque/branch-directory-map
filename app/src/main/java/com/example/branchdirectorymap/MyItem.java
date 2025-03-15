package com.example.branchdirectorymap;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyItem implements ClusterItem, Parcelable {
    private final LatLng position;  // latitude and longitude, derived from address or refined
    private final String code;      // location code, derived from full name
    private final String name;      // location name, derived from full name
    private final String snippet;   // location address
    private final String refined;   // refined address
    private final String phone;     // phone number
    private final String colour;    // marker colour
    private final float hue;        // marker hue, derived from colour
    private int selected;           // whether the marker is selected

    public MyItem(double lat, double lng, String code, String name, String snippet, String refined, String phone, String colour) {
        this.position = new LatLng(lat, lng);
        this.code = code;
        this.name = name;
        this.snippet = snippet;
        this.refined = refined;
        this.phone = phone;
        this.colour = colour;
        this.hue = calcClr(colour);
        this.selected = -1;
    }

    // Parcelable implementation
    protected MyItem(Parcel parcel) {
        position = parcel.readParcelable(LatLng.class.getClassLoader());
        code = parcel.readString();
        name = parcel.readString();
        snippet = parcel.readString();
        refined = parcel.readString();
        phone = parcel.readString();
        colour = parcel.readString();
        hue = parcel.readFloat();
        selected = parcel.readInt();
    }

    public static final Creator<MyItem> CREATOR = new Creator<MyItem>() {
        @Override
        public MyItem createFromParcel(Parcel in) {
            return new MyItem(in);
        }

        @Override
        public MyItem[] newArray(int size) {
            return new MyItem[size];
        }
    };

    private float calcClr (String colour) {
        float clr = switch (colour) {
            case "azure" -> BitmapDescriptorFactory.HUE_AZURE;
            case "blue" -> BitmapDescriptorFactory.HUE_RED;  // blue reserved for route markers
            case "cyan" -> BitmapDescriptorFactory.HUE_CYAN;
            case "green" -> BitmapDescriptorFactory.HUE_GREEN;
            case "magenta" -> BitmapDescriptorFactory.HUE_MAGENTA;
            case "orange" -> BitmapDescriptorFactory.HUE_ORANGE;
            case "rose" -> BitmapDescriptorFactory.HUE_ROSE;
            case "violet" -> BitmapDescriptorFactory.HUE_VIOLET;
            case "yellow" -> BitmapDescriptorFactory.HUE_YELLOW;
            default -> BitmapDescriptorFactory.HUE_RED;
        };
        return clr;
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

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeParcelable(position, i);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeString(snippet);
        parcel.writeString(refined);
    }

    public static class MyItemSorter {

        public static List<MyItem> sortMyItemsByCode(List<MyItem> items) {
            Collections.sort(items, new Comparator<MyItem>() {
                @Override
                public int compare(MyItem item1, MyItem item2) {
                    if (item1.getCode().isEmpty() && item2.getCode().isEmpty()) {
                        return item1.getName().compareTo(item2.getName());
                    } else {
                        return item1.getCode().compareTo(item2.getCode());
                    }
                }
            });
            return items;
        }
    }
}
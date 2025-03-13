package com.example.branchdirectorymap;

import java.util.HashSet;
import java.util.Objects;

public class LatLngTracker {

    private HashSet<LatLng> latLngSet;

    public LatLngTracker() {
        latLngSet = new HashSet<>();
    }

    public boolean addLatLng(double latitude, double longitude) {
        LatLng newLatLng = new LatLng(latitude, longitude);
        return latLngSet.add(newLatLng);
    }

    public boolean containsLatLng(double latitude, double longitude) {
        return latLngSet.contains(new LatLng(latitude, longitude));
    }

    private static class LatLng {

        private final double latitude;
        private final double longitude;

        public LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LatLng latLng = (LatLng) o;
            return Double.compare(latLng.latitude, latitude) == 0 &&
                    Double.compare(latLng.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }

        @Override
        public String toString() {
            return "LatLng{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
        }
    }
}
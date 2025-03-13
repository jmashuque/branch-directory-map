package com.example.branchdirectorymap;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodingResponse {
    @SerializedName("results")
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        @SerializedName("formatted_address")
        private String formattedAddress;

        @SerializedName("geometry")
        private Geometry geometry;

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public static class Geometry {
            @SerializedName("location")
            private Location location;

            public Location getLocation() {
                return location;
            }

            public static class Location {
                @SerializedName("lat")
                private double lat;

                @SerializedName("lng")
                private double lng;

                public double getLat() {
                    return lat;
                }

                public double getLng() {
                    return lng;
                }
            }
        }
    }
}
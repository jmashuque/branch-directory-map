package com.example.branchdirectorymap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {

    @GET("geocode/json")
    Call<GeocodingResponse> getGeocode(@Query("address") String address, @Query("key") String apiKey);
}
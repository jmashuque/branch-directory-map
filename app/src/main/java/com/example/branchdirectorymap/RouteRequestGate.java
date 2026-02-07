package com.example.branchdirectorymap;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

public final class RouteRequestGate {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "SYS-GATE";

    private final long cooldownMs;
    private final float samePlaceMeters;

    // Separate state per request type
    private final Lane regularLane = new Lane("REG");
    private final Lane optimizationLane = new Lane("OPT");

    public RouteRequestGate(double cooldownSeconds, float samePlaceMeters) {
        this.cooldownMs = secondsToMs(cooldownSeconds);
        this.samePlaceMeters = samePlaceMeters;
    }

    /**
     * @param isOptimization true for route optimization requests, false for regular route requests
     */
    public boolean shouldSkip(@Nullable Location originLastKnown,
                              @Nullable Location destLocation,
                              boolean isOptimization) {
        if (cooldownMs <= 0) return false;

        final Lane lane = isOptimization ? optimizationLane : regularLane;

        boolean sameOrigin = isSamePlace(lane.lastOriginSnapshot, originLastKnown, samePlaceMeters);
        boolean sameDest = isSamePlace(lane.lastDestSnapshot, destLocation, samePlaceMeters);

        Log.i(TAG, "lane=" + lane.name
                + " cooldownActive=" + lane.cooldownActive
                + " sameOrigin=" + sameOrigin
                + " sameDest=" + sameDest);

        if (lane.cooldownActive && sameOrigin && sameDest) {
            return true;
        }

        lane.rememberAndStart(handler, originLastKnown, destLocation, cooldownMs);
        return false;
    }

    private boolean isSamePlace(@Nullable Location a, @Nullable Location b, float meters) {
        if (a == null || b == null) return false;
        return a.distanceTo(b) <= meters;
    }

    public void cancel() {
        regularLane.cancel(handler);
        optimizationLane.cancel(handler);
    }

    private static long secondsToMs(double seconds) {
        if (seconds <= 0) return 0;
        double ms = seconds * 1000.0;
        if (ms > Long.MAX_VALUE) return Long.MAX_VALUE;
        return (long) ms;
    }

    private static final class Lane {
        final String name;

        boolean cooldownActive = false;
        Location lastOriginSnapshot = null;
        Location lastDestSnapshot = null;

        private final Runnable clearCooldown = () -> cooldownActive = false;

        Lane(String name) {
            this.name = name;
        }

        void rememberAndStart(Handler handler,
                              @Nullable Location origin,
                              @Nullable Location dest,
                              long cooldownMs) {
            lastOriginSnapshot = (origin == null) ? null : new Location(origin);
            lastDestSnapshot = (dest == null) ? null : new Location(dest);

            cooldownActive = true;
            handler.removeCallbacks(clearCooldown);
            handler.postDelayed(clearCooldown, cooldownMs);

            Log.i(TAG, "Cooldown (re)started [" + name + "]: " + cooldownMs + "ms");
        }

        void cancel(Handler handler) {
            handler.removeCallbacks(clearCooldown);
            cooldownActive = false;
        }
    }
}
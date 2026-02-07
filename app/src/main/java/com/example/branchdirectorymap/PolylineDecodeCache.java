package com.example.branchdirectorymap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PolylineDecodeCache {

    // Tune this. 200 keeps memory sane while covering most recent routes.
    private static final int MAX_ENTRIES = 200;

    private static final Map<String, List<LatLng>> CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, List<LatLng>>(MAX_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<LatLng>> eldest) {
                    return size() > MAX_ENTRIES;
                }
            });

    private PolylineDecodeCache() {}

    public static List<LatLng> getOrDecode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return Collections.emptyList();

        List<LatLng> cached = CACHE.get(encoded);
        if (cached != null) return cached;

        List<LatLng> decoded = PolyUtil.decode(encoded);
        if (decoded == null || decoded.isEmpty()) {
            CACHE.put(encoded, Collections.emptyList());
            return Collections.emptyList();
        }

        // Store immutable copy to prevent accidental mutation
        List<LatLng> immutable = Collections.unmodifiableList(new ArrayList<>(decoded));
        CACHE.put(encoded, immutable);
        return immutable;
    }

    public static void clear() {
        CACHE.clear();
    }

    public static int size() {
        return CACHE.size();
    }
}
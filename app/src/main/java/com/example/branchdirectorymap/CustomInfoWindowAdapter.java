package com.example.branchdirectorymap;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = "SYS-INFO";
    private final View mWindow;
    private final TextView titleTextView;
    private final TextView snippetTextView;
    private final TextView infoTextView;
    private final TextView trafficTextView;
    private final Button naviButton;
    private String infoText;
    private SpannableString trafficText;
    private InfoWindowOpenListener listener;

    public CustomInfoWindowAdapter(Context context) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null);
        titleTextView = mWindow.findViewById(R.id.title);
        snippetTextView = mWindow.findViewById(R.id.snippet);
        infoTextView = mWindow.findViewById(R.id.information);
        trafficTextView = mWindow.findViewById(R.id.traffic);
        naviButton = mWindow.findViewById(R.id.directionsButton);
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.i(TAG, "triggered getinfocontents");
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Log.i(TAG, "triggered getinfowindow");

        if (listener != null) {
            listener.onInfoWindowOpened();
        }

        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());
        infoTextView.setText(infoText);
        trafficTextView.setText(trafficText);

        return mWindow;
    }

    public void setInfoWindowOpenListener(InfoWindowOpenListener listener) {
        this.listener = listener;
    }

    public interface InfoWindowOpenListener {
        void onInfoWindowOpened();
    }

    public void setInterface(boolean metricsAndNavi, boolean advTraffic) {
        if (metricsAndNavi) {
            infoTextView.setVisibility(View.VISIBLE);
            naviButton.setVisibility(View.VISIBLE);
            if (advTraffic) {
                trafficTextView.setVisibility(View.VISIBLE);
            } else {
                trafficTextView.setVisibility(View.GONE);
            }
        } else {
            infoTextView.setVisibility(View.GONE);
            naviButton.setVisibility(View.GONE);
            trafficTextView.setVisibility(View.GONE);
        }
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public void setTrafficText(SpannableString trafficText) {
        this.trafficText = trafficText;
    }

    public SpannableString getTrafficText() {
        return trafficText;
    }
}

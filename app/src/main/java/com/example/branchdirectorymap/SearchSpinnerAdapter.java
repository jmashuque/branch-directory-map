package com.example.branchdirectorymap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class SearchSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

    private final Context context;
    private final List<String> items;
    @Nullable private final String headerItem;
    private int selectedItemPosition = -1;
    private int ddBg;
    private int ddText;

    public SearchSpinnerAdapter(Context context, List<String> items, @Nullable String headerItem) {
        super(context, 0);
        this.context = context;
        this.items = items;
        this.headerItem = headerItem;
        updateTheme();
    }

    public void updateTheme() {
        ddBg = ContextCompat.getColor(context, R.color.pri_text_medium_alt);
        ddText = ContextCompat.getColor(context, R.color.pri_text_high);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (headerItem != null ? 1 : 0) + items.size();
    }

    @Override
    public String getItem(int position) {
        if (headerItem != null) {
            return position == 0 ? headerItem : items.get(position - 1);
        } else {
            return items.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = (convertView == null)
                ? LayoutInflater.from(context).inflate(R.layout.spinner_selected_item_t, parent, false)
                : convertView;
        TextView tv = v.findViewById(R.id.spinnerTextViewT);
        tv.setText(getItem(position));
        tv.setTextColor(ddText);
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = (convertView != null)
                ? convertView
                : LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        view.setBackgroundColor(ddBg);

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        if (headerItem != null && position == 0) {
            if (selectedItemPosition == 0) {
                textView.setTypeface(null, Typeface.BOLD_ITALIC);
                textView.setTextColor(ddText);
            } else {
                textView.setTypeface(null, Typeface.ITALIC);
                textView.setTextColor(ddText);
            }
        } else if (position == selectedItemPosition) {
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(ddText);
        } else {
            textView.setTypeface(null, Typeface.NORMAL);
            textView.setTextColor(ddText);
        }
        return view;
    }

    public void setSelectedItemPosition(int position) {
        this.selectedItemPosition = position;
        notifyDataSetChanged();
    }
}
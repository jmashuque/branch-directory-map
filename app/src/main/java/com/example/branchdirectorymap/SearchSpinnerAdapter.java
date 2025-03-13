package com.example.branchdirectorymap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SearchSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;
//    private HashSet<String> uniqueItems;
    private int selectedItemPosition = -1;

    public SearchSpinnerAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
//        uniqueItems = new HashSet<>();
//        uniqueItems.addAll(items);
//        Log.i("SYS005", "uniqueItems: " + uniqueItems);
    }

//    @Override
//    public void addAll(String... newItems) {
////        Log.i("SYS005", "addall1");
//        List<String> newItemsCopy;
//        newItemsCopy = new ArrayList<>(Arrays.asList(newItems));
//        for (String item : newItemsCopy) {
//            if (uniqueItems.add(item)) {
//                super.add(item);
//            }
//        }
//    }
//
//    public void addAll(List<String> newItems) {
//        Log.i("SYS005", "addall2");
//        Log.i("SYS005", "newItems: " + newItems);
//        List<String> newItemsCopy;
//        newItemsCopy = new ArrayList<>(newItems);
//        for (String item : newItemsCopy) {
//            if (uniqueItems.add(item)) {
//                super.add(item);
//            }
//        }
//    }

    @Override
    public void clear() {
        super.clear();
//        uniqueItems.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_selected_item_t, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.spinnerTextView);
        textView.setText(items.get(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        } else {
            view = convertView;
        }
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(items.get(position));

        if (position == selectedItemPosition) {
            textView.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            textView.setTypeface(null, Typeface.NORMAL);
        }

        return view;
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }
}
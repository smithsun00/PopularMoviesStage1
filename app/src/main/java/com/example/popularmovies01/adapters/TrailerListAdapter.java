package com.example.popularmovies01.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.popularmovies01.R;

import java.util.ArrayList;

public class TrailerListAdapter extends ArrayAdapter<String> {

    private final static String PREFIX_TRAILERS = "Trailer ";
    private ArrayList<String> mTrailerKeyListData;

    public TrailerListAdapter(Activity context, ArrayList<String> trailerKeyList) {
        super(context, 0, trailerKeyList);
        mTrailerKeyListData = trailerKeyList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String trailerKey = mTrailerKeyListData.get(position);
        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_layout, parent, false);

        if(trailerKey != null) {
            TextView author = (TextView) rootView.findViewById(R.id.trailer_label_tv);
            author.setText(PREFIX_TRAILERS.concat(String.valueOf(position + 1)));
        }

        return rootView;
    }

    @Override
    public int getCount() {
        return mTrailerKeyListData != null ? mTrailerKeyListData.size() : 0;
    }

    @Override
    public String getItem(int position) {
        return mTrailerKeyListData.get(position);
    }
}
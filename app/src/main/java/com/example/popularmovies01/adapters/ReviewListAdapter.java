package com.example.popularmovies01.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.popularmovies01.R;
import com.example.popularmovies01.data.ReviewData;

import java.util.ArrayList;

import android.widget.TextView;

public class ReviewListAdapter extends ArrayAdapter<ReviewData> {

    private ArrayList<ReviewData> mReviewListData;

    public ReviewListAdapter(Activity context, ArrayList<ReviewData> reviewList) {
        super(context, 0, reviewList);
        mReviewListData = reviewList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReviewData reviewData = mReviewListData.get(position);
        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(getContext()).inflate(R.layout.review_layout, parent, false);

        if(reviewData != null) {
            TextView author = (TextView) rootView.findViewById(R.id.review_author_tv);
            author.setText(reviewData.getAuthor());

            TextView content = (TextView) rootView.findViewById(R.id.review_content_tv);
            content.setText(reviewData.getContent());
        }

        return rootView;
    }

    @Override
    public int getCount() {
        return mReviewListData != null ? mReviewListData.size() : 0;
    }

    @Override
    public ReviewData getItem(int position) {
        return mReviewListData.get(position);
    }
}

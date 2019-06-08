package com.example.popularmovies01.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable
{
    private String mPosterPath;
    private String mMovieTitle;
    private String mOverview;
    private double mUserRating;
    private String mReleaseDate;

    // Constructors
    public MovieData(String movieTitle, String posterPath, String overview, double userRating, String releaseDate)
    {
        mMovieTitle = movieTitle;
        mPosterPath = posterPath;
        mOverview = overview;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
    }

    private MovieData(Parcel parcel)
    {
        mMovieTitle = parcel.readString();
        mPosterPath = parcel.readString();
        mOverview = parcel.readString();
        mUserRating = parcel.readDouble();
        mReleaseDate = parcel.readString();
    }

    // Get methods
    public String GetPosterPath()
    {
        return mPosterPath != null ? mPosterPath : "NA";
    }

    public String GetMovieTitle()
    {
        return mMovieTitle != null ? mMovieTitle : "NA";
    }

    public String GetOverview()
    {
        return mOverview != null ? mOverview : "NA";
    }

    public double GetUserRating()
    {
        return mUserRating;
    }

    public String GetReleaseDate()
    {
        return mReleaseDate != null ? mReleaseDate : "NA";
    }

    // Set methods - will be left for C'tor so it cannot be changed after it has been created.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mMovieTitle);
        parcel.writeString(mPosterPath);
        parcel.writeString(mOverview);
        parcel.writeDouble(mUserRating);
        parcel.writeString(mReleaseDate);
    }

    public final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>(){

        @Override
        public MovieData createFromParcel(Parcel parcel) {
            return new MovieData(parcel);
        }

        @Override
        public MovieData[] newArray(int i) {
            return new MovieData[i];
        }
    };
}

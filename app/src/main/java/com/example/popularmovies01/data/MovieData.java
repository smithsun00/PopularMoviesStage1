package com.example.popularmovies01.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "movies")
public class MovieData implements Parcelable
{
    @PrimaryKey
    @ColumnInfo(name = "movie_id")
    private int movieId;
    @ColumnInfo(name = "poster_path")
    private String posterPath;
    @ColumnInfo(name = "movie_title")
    private String movieTitle;
    @ColumnInfo(name = "overview")
    private String overview;
    @ColumnInfo(name = "user_rating")
    private double userRating;
    @ColumnInfo(name = "release_date")
    private String releaseDate;

    // Constructors
    public MovieData(int movieId, String movieTitle, String posterPath, String overview, double userRating, String releaseDate)
    {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    @Ignore
    private MovieData(Parcel parcel)
    {
        movieId = parcel.readInt();
        movieTitle = parcel.readString();
        posterPath = parcel.readString();
        overview = parcel.readString();
        userRating = parcel.readDouble();
        releaseDate = parcel.readString();
    }

    // Get methods
    public int getMovieId()
    {
        return movieId;
    }

    public String getPosterPath()
    {
        return posterPath != null ? posterPath : "NA";
    }

    public String getMovieTitle()
    {
        return movieTitle != null ? movieTitle : "NA";
    }

    public String getOverview()
    {
        return overview != null ? overview : "NA";
    }

    public double getUserRating()
    {
        return userRating;
    }

    public String getReleaseDate()
    {
        return releaseDate != null ? releaseDate : "NA";
    }

    // Set methods - will be left for C'tor so it cannot be changed after it has been created.

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(movieTitle);
        parcel.writeString(posterPath);
        parcel.writeString(overview);
        parcel.writeDouble(userRating);
        parcel.writeString(releaseDate);
    }

    @Ignore
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

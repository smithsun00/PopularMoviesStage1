package com.example.popularmovies01.data;

public class ReviewData {

    private String author;
    private String content;

    public ReviewData(String author, String content)
    {
        this.author = author;
        this.content = content;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getContent()
    {
        return content;
    }
}

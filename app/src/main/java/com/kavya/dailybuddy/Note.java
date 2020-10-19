package com.kavya.dailybuddy;

public class Note {

    String title, subtitle,datetime, text, color,imagepath,webURL,pinned;
    int id;

    public Note(){

    }

    public Note(String title, String subtitle, String datetime, String text, String color, int id,String imagepath,String webURL,String pinned) {
        this.title = title;
        this.subtitle = subtitle;
        this.datetime= datetime;
        this.text = text;
        this.color = color;
        this.id = id;
        this.imagepath = imagepath;
        this.webURL = webURL;
        this.pinned = pinned;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() { return subtitle; }

    public String getDatetime() { return datetime; }

    public String getText() { return text; }

    public String getColor() { return color; }

    public String getImagepath() { return imagepath; }

    public String getWebURL() { return webURL; }

    public String getPinned() { return pinned; }
}

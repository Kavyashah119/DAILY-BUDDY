package com.kavya.dailybuddy;

public class Note {

    String title, subtitle,datetime, text, color;
    int id;

    public Note(){

    }

    public Note(String title, String subtitle, String datetime, String text, String color, int id) {
        this.title = title;
        this.subtitle = subtitle;
        this.datetime= datetime;
        this.text = text;
        this.color = color;
        this.id = id;
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
}

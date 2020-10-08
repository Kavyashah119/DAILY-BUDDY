package com.kavya.dailybuddy;

public class notes {

    String title, description, time,date;

    public notes() {
    }

    public notes(String title, String description, String time) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() { return time; }

    public String getDate() { return date; }


}

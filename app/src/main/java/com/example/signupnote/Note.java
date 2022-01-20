package com.example.signupnote;

import java.io.Serializable;

public class Note implements Serializable {
    private String title,description,id;

    public Note(){ }

    public Note(String id,String title, String description) {
        this.title = title;
        this.description = description;
        this.id = id;
    }

    public String getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

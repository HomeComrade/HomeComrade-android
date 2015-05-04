package com.homecomrade;

import java.util.ArrayList;

public class EntityRandomShowCategory {
    public String title;
    public ArrayList<EntityRandomShow> shows = new ArrayList<>();

    public EntityRandomShowCategory(String title, ArrayList<EntityRandomShow> shows) {
        this.title = title;
        this.shows = shows;
    }
}
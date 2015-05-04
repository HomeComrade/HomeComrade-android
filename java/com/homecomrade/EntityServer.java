package com.homecomrade;

import java.util.ArrayList;

public class EntityServer {
    public int serverid;
    public String url;
    public ArrayList<String> options = new ArrayList<>();

    public EntityServer(int serverid, String url) {
        this.serverid = serverid;
        this.url = url;
    }

    public boolean hasOption(String option) {
        if (null == this.options) {
            return false;
        }

        return this.options.contains(option);
    }
}